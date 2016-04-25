package com.messme.chats.messages.delivery;

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

import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class FragmentDeliveryChat extends AbstractMessageFragment<DeliveryMessage>
{
	private static final int _PRESSED_TIMER = 200;

	private ImageView _ivTimer = null;
	
	private Delivery _hDelivery;

	private boolean _TimerPressed;
	
	
	public FragmentDeliveryChat(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	
	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		String deliveryID = pStore.get(DELIVERY_ID);
		_hDelivery = __GetMainActivity().GetDeliveries().GetDelivery(deliveryID);
		
		__Adapter = new AdapterDeliveryChat(__GetMainActivity(), this, _hDelivery);
		
		View view = pInflater.inflate(R.layout.delivery_chat, pContainer, false);
		if (_hDelivery.GetType() == AbstractMessageContainer.SECRET)
		{
			if (Boolean.parseBoolean(pStore.get(UNLOCKED)))
			{
				__CreateView(view, pStore, _hDelivery, false);
			}
			else
			{
				__CreateView(view, pStore, _hDelivery, true);
			}
		}
		else
		{
			__CreateView(view, pStore, _hDelivery, false);
		}
		
		__tvTitle.setText(_hDelivery.GetName());

		_ivTimer = (ImageView) view.findViewById(R.id.ivDeliveryChatTimer);
		_ivTimer.setOnClickListener(this);
		_ivTimer.setSelected(_TimerPressed);
		
		return view;
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
		_hDelivery.SetAdapter(null);
		_hDelivery = null;
	}
	
	@Override
	public void onClick(View pView)
	{
		super.onClick(pView);
		switch (pView.getId())
		{
			case R.id.ivDeliveryChatTimer:
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
		}
	}


	@Override
	protected void __SynchronizeMessages() throws JSONException
	{
		JSONObject options = new JSONObject();
		options.put("id", _hDelivery.GetID());
		String lastID = _hDelivery.GetLastSyncID();
		options.put("size", __LOAD_COUNT);
		options.put("page", 1);
		if (lastID.equals(""))
		{
			__FirstMID = "";
			__SendToServer("mailing.messagelist", options);
		}
		else
		{
			options.put("lastid", lastID);
			__FirstMID = __SendToServer("mailing.messagelist", options);
		}
	}
	@Override
	protected void __GetMessagesNext(int pOffset) throws JSONException
	{
		JSONObject options = new JSONObject();
		options.put("id", _hDelivery.GetID());
		options.put("page", pOffset + 1);
		options.put("size", __LOAD_COUNT);
		__SendToServer("mailing.messagelist", options);
	}
	
	@Override
	protected void __OnErrorSended(String pMessageId, String pAction, JSONObject pOptions)
	{
		if (pAction.equals("mailing.messagelist"))
		{
			// не смогли получить данные
			__UnlockScroll();
		}
	}
	@Override
	protected void __OnMessageReceived(String pMessageId, String pAction, int pStatus, String pResult, JSONObject pOptions)
	{
		if (pAction.equals("mailing.messagelist"))
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
						for (int i = _hDelivery.GetMessages().size() - 1; i != -1 ; i--)
						{
							DeliveryMessage message = _hDelivery.GetMessages().get(i);
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
							_hDelivery.RemoveMessage(__GetMainActivity(), toRemove.get(i), writer);
						}
						
						JSONArray list = new JSONArray(pResult);
						for (int i = 0; i < list.length(); i++)
						{
							JSONObject row = list.getJSONObject(i);
							DeliveryMessage message = new DeliveryMessage(_hDelivery, row);
							message.SetSynchronized(writer);
							_hDelivery.AddMessage(message, writer);
						}
					}
					else if (pStatus == 1100)
					{
						// грузим заного
						_hDelivery.ClearMessages(writer);
						
						JSONArray list = new JSONArray(pResult);
						for (int i = 0; i < list.length(); i++)
						{
							JSONObject row = list.getJSONObject(i);
							DeliveryMessage message = new DeliveryMessage(_hDelivery, row);
							message.SetSynchronized(writer);
							_hDelivery.AddMessage(message, writer);
						}
						
						if (pOptions.getInt("size") != list.length())
						{
							// получили меньше чем запросили
							__MessageOver = true;
						}
					}
					
					__Syncronizing = true;
					
					if (_hDelivery.GetMessages().size() < __LOAD_COUNT && !__MessageOver)
					{
						__GetMessagesNext(0);
					}
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
							DeliveryMessage message = new DeliveryMessage(_hDelivery, row);
							message.SetSynchronized(writer);
							_hDelivery.AddMessage(message, writer);
						}
						
						if (pOptions.getInt("size") != list.length())
						{
							// получили меньше чем запросили
							__MessageOver = true;
						}
					}
				}
			}
			catch (JSONException e)
			{
			}
			
			writer.close();
			db.close();
	
			__UnlockScroll();
		}
	}

	@Override
	protected void __SendMessage(String pMessage, boolean pIsAttachmentUploaded, ArrayList<Attachment> pAttachment) throws Exception
	{
		DeliveryMessage message = null;
		if (__PlacePressed)
		{
			Location location = __GetMainActivity().GetLocation();
			if (location == null)
			{
				message = new DeliveryMessage(_hDelivery, _TimerPressed ? __GetMainActivity().GetProfile().GetMessagesTimer() : 0, 0.0d, 0.0d, pMessage, pIsAttachmentUploaded, pAttachment);
			}
			else
			{
				message = new DeliveryMessage(_hDelivery, _TimerPressed ? __GetMainActivity().GetProfile().GetMessagesTimer() : 0, location.getLatitude(), location.getLongitude(), pMessage, pIsAttachmentUploaded, pAttachment);
			}
		}
		else
		{
			message = new DeliveryMessage(_hDelivery, _TimerPressed ? __GetMainActivity().GetProfile().GetMessagesTimer() : 0, 0.0d, 0.0d, pMessage, pIsAttachmentUploaded, pAttachment);
		}
		DB db = new DB(__GetMainActivity());
		SQLiteDatabase writer = db.getWritableDatabase();
		_hDelivery.AddMessage(message, writer);
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
	protected void __ResendMessage(DeliveryMessage pMessage)
	{
		if (pMessage.IsSnap())
		{
			__GetMainActivity().OpenDialog(12, __GetMainActivity().getString(R.string.Dialog46Title), __GetMainActivity().getString(R.string.Dialog46Description));
			return;
		}
		super.__ResendMessage(pMessage);
	}
	
	@Override
	protected void __Init()
	{
		super.__Init();
		__llSend.setVisibility(View.VISIBLE);
	}
}