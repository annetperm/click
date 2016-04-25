package com.messme.chats.messages.chat;

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
import com.messme.chats.messages.dialog.DialogMessage;
import com.messme.data.DB;
import com.messme.user.User;
import com.messme.user.Users;

import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.support.v4.util.LongSparseArray;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class FragmentChat extends AbstractMessageFragment<ChatMessage>
{
	private ImageView _ivAvatar = null;
	private TextView _tvStatus = null;
	
	private Chat _hChat;
	
	
	public FragmentChat(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	
	public Chat GetChat()
	{
		return _hChat;
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		String chatID = pStore.get(CHAT_ID);
		_hChat = __GetMainActivity().GetChats().GetChat(chatID);
		
		__Adapter = new AdapterChat(__GetMainActivity(), this, _hChat);
		
		View view = pInflater.inflate(R.layout.chat, pContainer, false);
		if (_hChat.GetType() == AbstractMessageContainer.SECRET)
		{
			if (Boolean.parseBoolean(pStore.get(UNLOCKED)))
			{
				__CreateView(view, pStore, _hChat, false);
			}
			else
			{
				__CreateView(view, pStore, _hChat, true);
			}
		}
		else
		{
			__CreateView(view, pStore, _hChat, false);
		}
		
		_ivAvatar = (ImageView) view.findViewById(R.id.ivChatAvatar);
		_ivAvatar.setOnClickListener(this);
		
		_tvStatus = (TextView) view.findViewById(R.id.tvChatStatus);
		
		_SetChatData();
		
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
	protected void __OnDestroy()
	{
		super.__OnDestroy();
		_hChat.SetAdapter(null);
		_hChat = null;
	}
	
	@Override
	public void onClick(View pView)
	{
		super.onClick(pView);
		switch (pView.getId())
		{
			case R.id.ivChatAvatar:
				__GetMainActivity().GetManager().GoToGroup(_hChat);
				break;
		}
	}

	@Override
	protected void __SynchronizeMessages() throws JSONException
	{
		JSONObject options = new JSONObject();
		options.put("id", _hChat.GetID());
		String lastID = _hChat.GetLastSyncID();
		options.put("size", __LOAD_COUNT);
		options.put("page", 1);
		if (lastID.equals(""))
		{
			__FirstMID = "";
			__SendToServer("groupchat.messagelist", options);
		}
		else
		{
			options.put("lastid", lastID);
			__FirstMID = __SendToServer("groupchat.messagelist", options);
		}
	}
	@Override
	protected void __GetMessagesNext(int pOffset) throws JSONException
	{
		JSONObject options = new JSONObject();
		options.put("id", _hChat.GetID());
		options.put("page", pOffset + 1);
		options.put("size", __LOAD_COUNT);
		__SendToServer("groupchat.messagelist", options);
	}
	
	@Override
	protected void __OnErrorSended(String pMessageId, String pAction, JSONObject pOptions)
	{
		if (pAction.equals("groupchat.messagelist"))
		{
			// не смогли получить данные
			__UnlockScroll();
		}
	}
	@Override
	protected void __OnMessageReceived(String pMessageId, String pAction, int pStatus, String pResult, JSONObject pOptions)
	{
		if (pAction.equals("groupchat.messagelist"))
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
						for (int i = _hChat.GetMessages().size() - 1; i != -1 ; i--)
						{
							ChatMessage message = _hChat.GetMessages().get(i);
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
							_hChat.RemoveMessage(__GetMainActivity(), toRemove.get(i), writer);
						}
						
						JSONArray list = new JSONArray(pResult);
						for (int i = 0; i < list.length(); i++)
						{
							JSONObject row = list.getJSONObject(i);
							ChatMessage message = new ChatMessage(_hChat, row);
							message.SetSynchronized(writer);
							_hChat.AddMessage(message, writer);
						}
					}
					else if (pStatus == 1100)
					{
						// грузим заного
						_hChat.ClearMessages(writer);
						
						JSONArray list = new JSONArray(pResult);
						for (int i = 0; i < list.length(); i++)
						{
							JSONObject row = list.getJSONObject(i);
							ChatMessage message = new ChatMessage(_hChat, row);
							message.SetSynchronized(writer);
							_hChat.AddMessage(message, writer);
						}
						
						if (pOptions.getInt("size") != list.length())
						{
							// получили меньше чем запросили
							__MessageOver = true;
						}
					}

					__SendToServer("groupchat.info", Chat.GetInfo(_hChat));
					
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
							ChatMessage message = new ChatMessage(_hChat, row);
							message.SetSynchronized(writer);
							_hChat.AddMessage(message, writer);
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
				ChatMessage message = new ChatMessage(_hChat, result);
				if (__Syncronizing)
				{
					message.SetSynchronized(writer);
				}
				_hChat.AddMessage(message, writer);
				
				JSONObject options = new JSONObject();
				options.put("id", _hChat.GetID());
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
		else if (pAction.equals("groupchat.info") && pStatus == 1000)
		{
			try
			{
				JSONObject result = new JSONObject(pResult);
				_hChat.Update(result, __GetMainActivity().GetProfile().GetID());
				
				_SetChatData();
				
				ArrayList<Long> empty = _hChat.GetEmptyUsers(__GetMainActivity().GetUsers());
				if (empty.size() > 0)
				{
					__SendToServer("user.list", Users.GetUsersOptions(__GetMainActivity(), empty));
				}
				else
				{
					if (_hChat.GetMessages().size() < __LOAD_COUNT && !__MessageOver)
					{
						__GetMessagesNext(0);
					}
					else
					{
						// отправляем не прочитанные
						_SendUnread();
					}
				}
			}
			catch (JSONException e)
			{
			}
		}
		else if (pAction.equals("user.list") && pStatus == 1000)
		{
			try
			{
				JSONArray result = new JSONArray(pResult);
				__GetMainActivity().GetUsers().AddUsers(result, __GetMainActivity());
				
				if (_hChat.GetMessages().size() < __LOAD_COUNT && !__MessageOver)
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
		if (_hChat.IsMember())
		{
			ChatMessage message = null;
			if (__PlacePressed)
			{
				Location location = __GetMainActivity().GetLocation();
				if (location == null)
				{
					message = new ChatMessage(_hChat, __GetMainActivity().GetProfile().GetID(), 0.0d, 0.0d, pMessage, pIsAttachmentUploaded, pAttachment);
				}
				else
				{
					message =new ChatMessage(_hChat, __GetMainActivity().GetProfile().GetID(), location.getLatitude(), location.getLongitude(), pMessage, pIsAttachmentUploaded, pAttachment);
				}
			}
			else
			{
				message = new ChatMessage(_hChat, __GetMainActivity().GetProfile().GetID(), 0.0d, 0.0d, pMessage, pIsAttachmentUploaded, pAttachment);
			}
			DB db = new DB(__GetMainActivity());
			SQLiteDatabase writer = db.getWritableDatabase();
			_hChat.AddMessage(message, writer);
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
		else
		{
			__GetMainActivity().OpenDialog(20, __GetMainActivity().getString(R.string.Dialog50Title), __GetMainActivity().getString(R.string.Dialog50Description));
		}
	}
	
	@Override
	protected void __OnContactsChanged()
	{
		__Adapter.notifyDataSetChanged();
	}
	@Override
	protected void __OnUsersChanged(LongSparseArray<User> pUsers)
	{
		for (int i = 0; i < _hChat.GetUsers().size(); i++)
		{
			if (pUsers.get(_hChat.GetUsers().get(i)) != null)
			{
				__Adapter.notifyDataSetChanged();
				return;
			}
		}
	}
	
	@Override
	protected void __Init()
	{
		super.__Init();
		if (_hChat.IsMember())
		{
			__llSend.setVisibility(View.VISIBLE);
		}
		else
		{
			__llSend.setVisibility(View.GONE);
		}
	}
	
	private void _SetChatData()
	{
		if (_hChat.HasLoadedImage())
		{
			_ivAvatar.setImageBitmap(_hChat.GetImage());
		}
		else
		{
			__Loader.Load(_ivAvatar, _hChat);
		}
		
		__tvTitle.setText(_hChat.GetName());
		
		_tvStatus.setText("TODO");
		
		if (IsInited())
		{
			if (_hChat.IsMember())
			{
				__llSend.setVisibility(View.VISIBLE);
			}
			else
			{
				__llSend.setVisibility(View.GONE);
			}
		}
	}

	private void _SendUnread() throws JSONException
	{
		JSONArray list = new JSONArray();
		for (int i = _hChat.GetMessages().size() - 1; i != -1 ; i--)
		{
			ChatMessage message = _hChat.GetMessages().get(i);
			if (message.GetSenderID() != __GetMainActivity().GetProfile().GetID())
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
			options.put("id", _hChat.GetID());
			options.put("status", __GetMainActivity().IsVisible() ? 2 : 1);
			options.put("list", list);
			__SendToServer("message.setstatus", options);
		}
	}
}