package com.messme.chats.messages.dialog;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.messages.AbstractMessage;
import com.messme.chats.messages.AbstractMessageContainer;
import com.messme.chats.messages.AbstractMessageFragment;
import com.messme.chats.messages.Attachment;
import com.messme.data.DB;
import com.messme.user.User;

import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.support.v4.util.LongSparseArray;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class FragmentDialog extends AbstractMessageFragment<DialogMessage>
{
	private static final int _PRESSED_TIMER = 200;

	
	private ImageView _ivAvatar = null;
	private TextView _tvStatus = null;
	private ImageView _ivTimer = null;

	private long _UserID;
	private User _hUser;
	private Dialog _hDialog;

	private boolean _TimerPressed;
	
	
	public FragmentDialog(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	
	public long GetUserID()
	{
		return _UserID;
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{				
		_UserID = Long.parseLong(pStore.get(USER_ID));
		_hUser = __GetMainActivity().GetUsers().GetUser(_UserID);
		_hDialog = __GetMainActivity().GetDialogs().GetDialog(_UserID);
		_TimerPressed = pStore.get(_PRESSED_TIMER) == null ? false : Boolean.parseBoolean(pStore.get(_PRESSED_TIMER));
		
		__Adapter = new AdapterDialog(__GetMainActivity(), this, _hDialog);
		
		View view = pInflater.inflate(R.layout.dialog, pContainer, false);
		if (_hDialog.GetType() == AbstractMessageContainer.SECRET)
		{
			if (Boolean.parseBoolean(pStore.get(UNLOCKED)))
			{
				__CreateView(view, pStore, _hDialog, false);
			}
			else
			{
				__CreateView(view, pStore, _hDialog, true);
			}
		}
		else
		{
			__CreateView(view, pStore, _hDialog, false);
		}
		
		_ivAvatar = (ImageView) view.findViewById(R.id.ivDialogAvatar);
		_ivAvatar.setOnClickListener(this);
		_tvStatus = (TextView) view.findViewById(R.id.tvDialogStatus);
		_tvStatus.setSelected(true);

		_ivTimer = (ImageView) view.findViewById(R.id.ivDialogTimer);
		_ivTimer.setOnClickListener(this);
		_ivTimer.setSelected(_TimerPressed);
		
		_SetUserData();
		
		return view;
	}	
	
	@Override
	protected void __OnResume()
	{
		if (__Syncronizing)
		{
			// отправляем не прочитанные
			try
			{
				_SendUnread();
			}
			catch (JSONException e)
			{
			}
		}
	}
	
	@Override
	protected final void __OnSave(SparseArray<String> pStore)
	{
		super.__OnSave(pStore);
		pStore.put(_PRESSED_TIMER, Boolean.toString(_TimerPressed));
	}
	
	@Override
	protected void __OnDestroy()
	{
		super.__OnDestroy();
		_hDialog.SetAdapter(null);
		_hDialog = null;
		_hUser = null;
	}
	
	@Override
	public void onClick(View pView)
	{
		super.onClick(pView);
		switch (pView.getId())
		{
			case R.id.ivDialogTimer:
				if (_ivTimer.isSelected())
				{
					_ivTimer.setSelected(false);
					_TimerPressed = false;
				}
				else
				{
					_ivTimer.setSelected(true);
					_TimerPressed = true;
				}
				break;
			case R.id.ivDialogAvatar:
				__GetMainActivity().GetManager().GoToUser(_UserID);
				break;
		}
	}
	
	@Override
	protected void __SynchronizeMessages() throws JSONException
	{
		JSONObject options = new JSONObject();
		options.put("userid", _UserID);
		String lastID = _hDialog.GetLastSyncID();
		options.put("size", __LOAD_COUNT);
		options.put("page", 1);
		if (lastID.equals(""))
		{
			__FirstMID = "";
			__SendToServer("message.list", options);
		}
		else
		{
			options.put("lastid", lastID);
			__FirstMID = __SendToServer("message.list", options);
		}
	}
	@Override
	protected void __GetMessagesNext(int pOffset) throws JSONException
	{
		JSONObject options = new JSONObject();
		options.put("userid", _UserID);
		options.put("page", pOffset + 1);
		options.put("size", __LOAD_COUNT);
		__SendToServer("message.list", options);
	}
	
	@Override
	protected void __OnErrorSended(String pMessageId, String pAction, JSONObject pOptions)
	{
		if (pAction.equals("message.list"))
		{
			// не смогли получить данные
			__UnlockScroll();
		}
	}
	@Override
	protected void __OnMessageReceived(String pMessageId, String pAction, int pStatus, String pResult, JSONObject pOptions)
	{
		if (pAction.equals("message.list"))
		{
			DB db = new DB(__GetMainActivity());
			SQLiteDatabase writer = db.getWritableDatabase();
			try
			{
				if (__FirstMID.equals(pMessageId))
				{
					if (pStatus == 1000)
					{
						// дополняем
						// удаляем те которые типо отправленные на сервер но ответ от сервака не пришел
						String lastID = pOptions.getString("lastid");
						ArrayList<String> toRemove = new ArrayList<String>();
						for (int i = _hDialog.GetMessages().size() - 1; i != -1 ; i--)
						{
							DialogMessage message = _hDialog.GetMessages().get(i);
							if (message.GetID().equals(lastID))
							{
								break;
							}
							if (message.GetStatus() == DialogMessage.STATUS_SENDED_E)
							{
								toRemove.add(message.GetID());
							}
						}
						for (int i = 0; i < toRemove.size(); i++)
						{
							_hDialog.RemoveMessage(__GetMainActivity(), toRemove.get(i), writer);
						}
						
						JSONArray list = new JSONArray(pResult);
						for (int i = 0; i < list.length(); i++)
						{
							JSONObject row = list.getJSONObject(i);
							DialogMessage message = new DialogMessage(_hDialog, row);
							message.SetSynchronized(writer);
							_hDialog.AddMessage(message, writer);
						}
					}
					else if (pStatus == 1100)
					{
						// грузим заного
						_hDialog.ClearMessages(writer);
						
						JSONArray list = new JSONArray(pResult);
						for (int i = 0; i < list.length(); i++)
						{
							JSONObject row = list.getJSONObject(i);
							DialogMessage message = new DialogMessage(_hDialog, row);
							message.SetSynchronized(writer);
							_hDialog.AddMessage(message, writer);
						}
						
						if (pOptions.getInt("size") != list.length())
						{
							// получили меньше чем запросили
							__MessageOver = true;
						}
					}
					
					__SendToServer("user.info", User.GetOptions(__GetMainActivity(), _UserID));
					
					__Syncronizing = true;
				}
				else
				{
					if (pStatus == 1000)
					{
						// дополняем						
						JSONArray list = new JSONArray(pResult);
						for (int i = 0; i < list.length(); i++)
						{
							JSONObject row = list.getJSONObject(i);
							DialogMessage message = new DialogMessage(_hDialog, row);
							message.SetSynchronized(writer);
							_hDialog.AddMessage(message, writer);
						}
						
						if (pOptions.getInt("size") != list.length())
						{
							// получили меньше чем запросили
							__MessageOver = true;
						}
					}
					
					// отправляем не прочитанные
					_SendUnread();
				}
			}
			catch (JSONException e)
			{
			}
			
			writer.close();
			db.close();

			__UnlockScroll();
		}
		else if (pAction.equals("onmessage") && pStatus == 1000)
		{
			DB db = new DB(__GetMainActivity());
			SQLiteDatabase writer = db.getWritableDatabase();
			try
			{
				JSONObject result = new JSONObject(pResult);
				DialogMessage message = new DialogMessage(_hDialog, result);
				if (__Syncronizing)
				{
					message.SetSynchronized(writer);
				}
				_hDialog.AddMessage(message, writer);
				
				JSONObject options = new JSONObject();
				options.put("userid", _hDialog.GetUserID());
				options.put("status", __GetMainActivity().IsVisible() ? 2 : 1);
				JSONArray list = new JSONArray();
				list.put(message.GetID());
				options.put("list", list);
				__SendToServer("message.setstatus", options);
			}
			catch (JSONException e)
			{
			}
			writer.close();
			db.close();
		}
		else if (pAction.equals("user.info") && pStatus == 1000)
		{
			try
			{
				JSONObject result = new JSONObject(pResult);
				_hUser = __GetMainActivity().GetUsers().AddUser(result, __GetMainActivity());
				
				if (_hDialog.GetMessages().size() < __LOAD_COUNT && !__MessageOver)
				{
					__GetMessagesNext(0);
				}
				else
				{
					// отправляем не прочитанные
					_SendUnread();
				}
			}
			catch (JSONException e)
			{
			}
		}
	}


	@Override
	protected void __SendMessage(String pMessage, boolean pIsAttachmentUploaded, ArrayList<Attachment> pAttachment) throws Exception
	{
		DialogMessage message = null;
		if (__PlacePressed)
		{
			Location location = __GetMainActivity().GetLocation();
			if (location == null)
			{
				message = new DialogMessage(_hDialog, _TimerPressed ? __GetMainActivity().GetProfile().GetMessagesTimer() : 0, 0.0d, 0.0d, pMessage, pIsAttachmentUploaded, pAttachment);
			}
			else
			{
				message = new DialogMessage(_hDialog, _TimerPressed ? __GetMainActivity().GetProfile().GetMessagesTimer() : 0, location.getLatitude(), location.getLongitude(), pMessage, pIsAttachmentUploaded, pAttachment);
			}
		}
		else
		{
			message = new DialogMessage(_hDialog, _TimerPressed ? __GetMainActivity().GetProfile().GetMessagesTimer() : 0, 0.0d, 0.0d, pMessage, pIsAttachmentUploaded, pAttachment);
		}
		DB db = new DB(__GetMainActivity());
		SQLiteDatabase writer = db.getWritableDatabase();
		_hDialog.AddMessage(message, writer);
		writer.close();
		db.close();
		if (message.GetStatus() == AbstractMessage.STATUS_NOTSENDED)
		{
			message.Sending(__GetMainActivity());
		}
		else
		{
			message.Uploading(__GetMainActivity());
		}
	}
	@Override
	protected void __ResendMessage(DialogMessage pMessage)
	{
		if (pMessage.IsSnap())
		{
			__GetMainActivity().OpenDialog(12, __GetMainActivity().getString(R.string.Dialog46Title), __GetMainActivity().getString(R.string.Dialog46Description));
			return;
		}
		super.__ResendMessage(pMessage);
	}
	
	@Override
	protected void __OnContactsChanged()
	{
		_SetUserData();
	}
	@Override
	protected void __OnUsersChanged(LongSparseArray<User> pUsers)
	{
		if (pUsers.get(_UserID) != null)
		{
			_SetUserData();
		}
	}
	
	@Override
	protected void __Init()
	{
		super.__Init();
		__llSend.setVisibility(View.VISIBLE);
	}
	
	private void _SetUserData()
	{
		_hUser = __GetMainActivity().GetUsers().GetUser(_UserID);
		if (_hUser == null)
		{
			__tvTitle.setText("");
			_tvStatus.setText("");
		}
		else
		{
			__tvTitle.setText(_hUser.GetName(__GetMainActivity()));
			_tvStatus.setText(_hUser.GetStatus(__GetMainActivity()));
			_tvStatus.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);
			_tvStatus.setSingleLine(true);
			_tvStatus.setMarqueeRepeatLimit(5);
			_tvStatus.setSelected(true);
			if (_hUser.HasImage())
			{
				_ivAvatar.setImageBitmap(_hUser.GetImage());
			}
			else
			{
				__Loader.Load(_ivAvatar, _hUser);
			}
		}
	}

	private void _SendUnread() throws JSONException
	{
		JSONArray list = new JSONArray();
		for (int i = _hDialog.GetMessages().size() - 1; i != -1 ; i--)
		{
			DialogMessage message = _hDialog.GetMessages().get(i);
			if (!message.IsMy())
			{
				if (message.GetStatus() == AbstractMessage.STATUS_READED)
				{
					break;
				}
				else
				{
					list.put(message.GetID());
				}
			}
		}		
		if (list.length() > 0)
		{
			JSONObject options = new JSONObject();
			options.put("userid", _hDialog.GetUserID());
			options.put("status", __GetMainActivity().IsVisible() ? 2 : 1);
			options.put("list", list);
			__SendToServer("message.setstatus", options);
		}
	}
}