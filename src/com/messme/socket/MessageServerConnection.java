package com.messme.socket;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.messme.ActivityMain;
import com.messme.NotSended;
import com.messme.Push;
import com.messme.R;
import com.messme.chats.ChatHeader;
import com.messme.chats.messages.AbstractMessage;
import com.messme.chats.messages.AbstractMessageContainer;
import com.messme.chats.messages.chat.Chat;
import com.messme.chats.messages.chat.ChatMessage;
import com.messme.chats.messages.chat.FragmentChat;
import com.messme.chats.messages.delivery.Delivery;
import com.messme.chats.messages.dialog.Dialog;
import com.messme.chats.messages.dialog.DialogMessage;
import com.messme.chats.messages.dialog.FragmentDialog;
import com.messme.data.DB;
import com.messme.user.Users;
import com.messme.util.Util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings.Secure;
import android.util.Base64;


public class MessageServerConnection implements CommunicationSocket.iSocketListener
{
	private final ActivityMain _hActivity;
	private NotSended _NotSended = null;
	private CommunicationSocket _Socket = null;
	
	private String _GetUsersInfoMID = "";
	private String _GetChatInfoMID = "";
	private String _ConnectMID = "";
	
	private boolean _Started = false;
	
	
	public MessageServerConnection(ActivityMain pActivity)
	{
		_hActivity = pActivity;
		
		GCMRegistrar.checkDevice(_hActivity);
        GCMRegistrar.checkManifest(_hActivity);
        
        _Socket = new CommunicationSocket(MessageServerConnection.this);
        
        new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				String regId = GCMRegistrar.getRegistrationId(_hActivity);
				while (regId.equals(""))
				{
	    			try
					{
	    				regId = GoogleCloudMessaging.getInstance(_hActivity.getApplicationContext()).register("1063835987475");
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
	    			try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
		        
				_Socket.Connect("Message/?id=" + Long.toString(_hActivity.GetProfile().GetID())
					+ "&uuid=" + _hActivity.GetProfile().GetUUID()
					+ "&deviceid=" + Secure.getString(_hActivity.getContentResolver(), Secure.ANDROID_ID)
					+ "&pushid=" + Base64.encodeToString(regId.getBytes(), Base64.DEFAULT)
					+ "&osname=Android"
					+ "&osversion=" + android.os.Build.VERSION.SDK_INT);
			}
		}).start();
        
//        String regId = GCMRegistrar.getRegistrationId(_hActivity);
//    	//FIXME
//        int i = 0;
//    	while (regId.equals(""))
//    	{
//    		if (i == 0)
//    		{
//    			new Thread(new Runnable()
//				{
//					
//					@Override
//					public void run()
//					{
//		    			try
//						{
//							String result = GoogleCloudMessaging.getInstance(_hActivity.getApplicationContext()).register("1063835987475");
//							result = "";
//						}
//						catch (IOException e)
//						{
//							e.printStackTrace();
//						}
//					}
//				}).start();
//            	//GCMRegistrar.register(_hActivity, "1063835987475");
//    		}
//    		else
//    		{
//        		try
//    			{
//    				Thread.sleep(1000);
//    			}
//    			catch (InterruptedException e)
//    			{
//    			}
//    		}
//    		i++;
//            if (i > 20)
//            {
//            	i = 0;
//            }
//            regId = GCMRegistrar.getRegistrationId(_hActivity);
//    	}
        
        _NotSended = new NotSended(_hActivity);
        
//		_Socket = new CommunicationSocket(this, "Message/?id=" + Long.toString(_hActivity.GetProfile().GetID())
//			+ "&uuid=" + _hActivity.GetProfile().GetUUID()
//			+ "&deviceid=" + Secure.getString(_hActivity.getContentResolver(), Secure.ANDROID_ID)
//			+ "&pushid=" + Base64.encodeToString(regId.getBytes(), Base64.DEFAULT)
//			+ "&osname=Android"
//			+ "&osversion=" + android.os.Build.VERSION.SDK_INT);
	}


	public boolean IsStrated()
	{
		return _Started;
	}
	

	public void Disconnect(SQLiteDatabase pWriter)
	{
		_Started = false;
		_Socket.Disconnect();
		
		try
		{
			GoogleCloudMessaging.getInstance(_hActivity).unregister();
		}
		catch (IOException e)
		{
		}
		
		_NotSended.Clear(pWriter);
	}

	public void Send(String pMID, String pAction, JSONObject pOptions)
	{
		MessageItem item = new MessageItem(pMID, pAction, pOptions);
		_Socket.Send(item);
	}


	public void RemoveNotSended(String pMID)
	{
		_NotSended.Remove(pMID);
	}


	public void PushStartSequence()
	{
		if (!_hActivity.GetContacts().IsSynchronized() && (_hActivity.GetContacts().Update() || _hActivity.GetUsers().GetFriends().size() == 0))
		{
			// отправка на сервер
			_hActivity.GetContacts().Synchronizing();
		}
		else if (_hActivity.GetUsers().IsSynchronizeTime()) // обновляем раз в 5 мин
		{
			// получаем актуальные статусы
			_hActivity.GetUsers().SynchronizingFriends(_hActivity);
		}
		else
		{
			try
			{
				JSONObject options = new JSONObject();
				options.put("locale", _hActivity.GetProfile().GetLocale());
				_Socket.Send("dialog.lastlist", options);
			}
			catch (JSONException e)
			{
			}
		}
	}


	public void Reconnect(Context pContext)
	{
		_Socket.Reconnect(pContext);
	}
	

	@Override
	public void OnConnect()
	{
	}


	@Override
	public void OnMessageSendError(MessageItem pItem, boolean pTimeOut)
	{
		if (pItem.GetType() == MessageItem.MESSAGE_TYPE_RESEND)
		{
			return;
		}
		
		String action = pItem.GetAction();

		if (action.equals("user.setfriend"))
		{
		}
		else if (action.equals("dialog.lastlist"))
		{
		}
		else if (action.equals("user.setstatus"))
		{
		}
		else if (action.equals("user.setgeo"))
		{
		}
		else if (action.equals("message.setstatus"))
		{
		}
		else if (action.equals("message.clearhistory") || action.equals("groupchat.delete") || action.equals("mailing.delete"))
		{
			_hActivity.OpenDialog(105, _hActivity.getString(R.string.Dialog42Title), _hActivity.getString(R.string.Dialog42Description));
		}
		else if (action.equals("message.delete"))
		{
			_hActivity.OpenDialog(105, _hActivity.getString(R.string.Dialog41Title), _hActivity.getString(R.string.Dialog41Description));
		}
		else if (action.equals("user.list") && _hActivity.GetUsers().GetMessageId().equals(pItem.GetMID()))
		{		
			// не получилось получить статусы друзей
		}
		else if (action.equals("user.list") && _GetUsersInfoMID.equals(pItem.GetMID()))
		{	
			// не получилось получить инфу о пользователе
		}
		else if (action.equals("groupchat.info") && _GetChatInfoMID.equals(pItem.GetMID()))
		{	
			// не получилось получить инфу о чате
		}
		else if (action.equals("message.send") || action.equals("groupchat.send") || action.equals("mailing.send"))
		{
			_NotSended.Add(pItem);
		}
		else
		{
			if (_hActivity.GetManager().GetCurrentFragment() != null)
			{
				_hActivity.GetManager().GetCurrentFragment().ErrorSended(pItem.GetMID(), action, pItem.GetOptions());
			}
		}
	}
	@Override
	public void OnMessageSendSuccess(MessageItem pItem)
	{
		if (pItem.GetAction().equals("message.send"))
		{	
			try
			{
				Dialog dialog = _hActivity.GetDialogs().GetDialog(pItem.GetOptions().getJSONArray("userlist").getLong(0));
				DialogMessage message = dialog.GetMessage(pItem.GetMID());
				DB db = new DB(_hActivity);
				SQLiteDatabase writer = db.getWritableDatabase();
				message.Sended(writer);
				writer.close();
				db.close();
			}
			catch (Exception e)
			{
				Util.CreateBreakPoint("message.send");
			}
		}
	}
	@Override
	public void OnMessageReceive(String pMID, String pAction, int pStatus, String pResult, int pType, JSONObject pOptions)
	{
		if (pAction.equals("onopen"))
		{  
			switch (pStatus)
			{
				case 2202://S_USER_CHECKLOGIN_ERROR
					_hActivity.OpenDialog(101, _hActivity.getString(R.string.Dialog28Title), _hActivity.getString(R.string.Dialog28Description));
					break;
				case 2203://S_USER_NOT_FOUND
					_hActivity.OpenDialog(102, _hActivity.getString(R.string.Dialog29Title), _hActivity.getString(R.string.Dialog29Description));
					break;
				case 2204://S_USER_NOT_ENABLED
					_hActivity.OpenDialog(103, _hActivity.getString(R.string.Dialog30Title), _hActivity.getString(R.string.Dialog30Description));
					break;
				case 2205://S_USER_NEW_DEVICE
					_hActivity.OpenDialog(104, _hActivity.getString(R.string.Dialog31Title), _hActivity.getString(R.string.Dialog31Description));
					break;
				case 2206://S_USER_NOT_ACTIVATED
					_hActivity.OpenDialog(105, _hActivity.getString(R.string.Dialog32Title), _hActivity.getString(R.string.Dialog32Description));
					break;
				case 1000:
					try
					{
						// узнаем актуальное количесвто лайков
						JSONObject result = new JSONObject(pResult);
						_hActivity.GetProfile().SetLikes(result.getInt("likes"));
					}
					catch (JSONException e)
					{
						Util.CreateBreakPoint("onopen");
					}
					break;
			}
			if (pStatus == 2202 || pStatus == 2203 || pStatus == 2204 || pStatus == 2205 || pStatus == 2206)
			{
				_hActivity.GetProfile().Clear(_hActivity);
			}
			else
			{
				if (!_hActivity.GetProfile().IsInitial())
				{
					// не инициализирован пользователь
				}
				else if (!_hActivity.GetContacts().IsSynchronized() && (_hActivity.GetContacts().Update() || _hActivity.GetUsers().GetFriends().size() == 0))
				{
					// отправка на сервер
					_hActivity.GetContacts().Synchronizing();
				}
				else if (_hActivity.GetUsers().IsSynchronizeTime()) // обновляем раз в 5 мин
				{
					_hActivity.GetContacts().Synchronized();
					// получаем актуальные статусы
					_hActivity.GetUsers().SynchronizingFriends(_hActivity);
				}
				else
				{
					_hActivity.GetContacts().Synchronized();
					try
					{
						JSONObject options = new JSONObject();
						options.put("locale", _hActivity.GetProfile().GetLocale());
						_Socket.Send("dialog.lastlist", options);
					}
					catch (JSONException e)
					{
					}
				}
			}
		}
		else if (pAction.equals("user.setfriend"))
		{
			if (pStatus == 1000)
			{
				_hActivity.GetContacts().Synchronized();
			}
			
			_hActivity.GetUsers().SynchronizingFriends(_hActivity);
		}
		else if (pAction.equals("user.list") && _hActivity.GetUsers().GetMessageId().equals(pMID))
		{		
			if (pStatus == 1000)
			{			
				_hActivity.GetUsers().UpdateFriends(pResult, _hActivity);
			}
			
			try
			{
				JSONObject options = new JSONObject();
				options.put("locale", _hActivity.GetProfile().GetLocale());
				_Socket.Send("dialog.lastlist", options);
			}
			catch (JSONException e)
			{
				Util.CreateBreakPoint("user.list friends");
			}
		}
		else if (pAction.equals("dialog.lastlist"))
		{
			if (pStatus == 1000)
			{		
				DB db = new DB(_hActivity);
				SQLiteDatabase writer = db.getWritableDatabase();
				for (int i = 0; i < _hActivity.GetDialogs().GetDialogs().size(); i++)
				{
					_hActivity.GetDialogs().GetDialogs().valueAt(i).ResetRemoveFlag();
				}
				for (Chat chat: _hActivity.GetChats().GetChats().values())
				{
					chat.ResetRemoveFlag();
				}
				for (Delivery delivery: _hActivity.GetDeliveries().GetDeliveries().values())
				{
					delivery.ResetRemoveFlag();
				}
				try
				{
					ArrayList<Long> users = new ArrayList<Long>();
					ArrayList<AbstractMessageContainer<?>> changed = new ArrayList<AbstractMessageContainer<?>>();
					JSONArray list = new JSONArray(pResult);
					for (int i = 0; i < list.length(); i++)
					{
			        	ChatHeader chatHeader = new ChatHeader(_hActivity.GetProfile().GetID(), list.getJSONObject(i));
			    		if (chatHeader.Type == 0)
			    		{
			    			Dialog dialog = _hActivity.GetDialogs().GetDialog(chatHeader.GetUserId());
			    			dialog.SetNotRemoveFlag();
			    			if (dialog.UpdateHeader(chatHeader, writer))
			    			{
			    				changed.add(dialog);
			    			}
							if (_hActivity.GetUsers().GetUser(chatHeader.GetUserId()) == null)
							{
								users.add(chatHeader.GetUserId());
							}
			    		}
			    		else if (chatHeader.Type == 1)
			    		{
			    			Chat chat = _hActivity.GetChats().GetChat(chatHeader.Entry);
			    			chat.SetNotRemoveFlag();
			    			if (chat.UpdateHeader(chatHeader, writer))
			    			{
			    				changed.add(chat);
			    			}
			    		}
			    		else if (chatHeader.Type == 2)
			    		{
			    			Delivery delivery = _hActivity.GetDeliveries().GetDelivery(chatHeader.Entry);
			    			delivery.SetNotRemoveFlag();
			    			if (delivery.UpdateHeader(chatHeader, writer))
			    			{
			    				changed.add(delivery);
			    			}
			    		}
					}
					
					boolean removed = false;
					
					ArrayList<Long> toRemoveDialog = new ArrayList<Long>();
					for (int i = 0; i < _hActivity.GetDialogs().GetDialogs().size(); i++)
					{
						if (_hActivity.GetDialogs().GetDialogs().valueAt(i).IsRemoving())
						{
							toRemoveDialog.add(_hActivity.GetDialogs().GetDialogs().valueAt(i).GetUserID());
						}
					}
					for (int i = 0; i < toRemoveDialog.size(); i++)
					{
						removed = true;
						_hActivity.GetDialogs().Remove(toRemoveDialog.get(i));
					}
					
					ArrayList<String> toRemoveChat = new ArrayList<String>();
					for (Chat chat: _hActivity.GetChats().GetChats().values())
					{
						if (chat.IsRemoving())
						{
							toRemoveChat.add(chat.GetID());
						}
					}
					for (int i = 0; i < toRemoveChat.size(); i++)
					{
						removed = true;
						_hActivity.GetChats().Remove(toRemoveChat.get(i));
					}

					ArrayList<String> toRemoveDelivery = new ArrayList<String>();
					for (Delivery delivery: _hActivity.GetDeliveries().GetDeliveries().values())
					{
						if (delivery.IsRemoving())
						{
							toRemoveDelivery.add(delivery.GetID());
						}
					}
					for (int i = 0; i < toRemoveDelivery.size(); i++)
					{
						removed = true;
						_hActivity.GetDeliveries().Remove(toRemoveDelivery.get(i));
					}
					
					if ((changed.size() > 0 || removed) && _hActivity.GetManager().GetCurrentFragment() != null)
					{
						_hActivity.GetManager().GetCurrentFragment().HeadersChanged(changed);
					}
					
					if (users.size() > 0)
					{
						// нет инфо об этом пользователе
						_GetUsersInfoMID = _Socket.Send("user.list", Users.GetUsersOptions(_hActivity, users));
					}
					
					Push.Call(_hActivity);
				}
				catch (JSONException e)
				{
					Util.CreateBreakPoint("dialog.lastlist");
				}
				writer.close();
				db.close();
			}
			
			try
			{
				JSONObject options = new JSONObject();
				options.put("status", 1);
				_ConnectMID = _Socket.Send("user.setstatus", options);
			}
			catch (JSONException e)
			{
			}
		}
		else if (pAction.equals("user.setstatus") && _ConnectMID.equals(pMID))
		{
			_Started = true;
			// указали свой статус серверу
			MessageItem item = _NotSended.GetFirst();
			if (item != null)
			{
				_Socket.Send(item);
			}
			
			_hActivity.GetDialogs().StartUploading(_hActivity);
			_hActivity.GetChats().StartUploading(_hActivity);
			_hActivity.GetDeliveries().StartUploading(_hActivity);
			
			if (_hActivity.GetManager().GetCurrentFragment() != null)
			{
				_hActivity.GetManager().GetCurrentFragment().Connected();
			}
		}
		else if (pAction.equals("user.setgeo"))
		{
			// указали свои кординаты серверу
		}
		// пришло сообщение
		else if (pAction.equals("onmessage"))
		{  
			if (pStatus == 1000)
			{			
				try
				{
					JSONObject result = new JSONObject(pResult);
					long userID = result.getLong("userid");
					int type = result.getInt("dialogtype");
					if (type == 0 || type == 2)
					{
						if (_hActivity.GetManager().GetCurrentFragment() != null
								&& _hActivity.GetManager().GetCurrentFragment() instanceof FragmentDialog 
								&& ((FragmentDialog) _hActivity.GetManager().GetCurrentFragment()).GetUserID() == userID
								&& ((FragmentDialog) _hActivity.GetManager().GetCurrentFragment()).IsInited())
						{
							_hActivity.GetManager().GetCurrentFragment().MessageReceived(pMID, pAction, pStatus, pResult, pType, pOptions);
						}
						else 
						{
							Dialog dialog = _hActivity.GetDialogs().GetDialog(userID);
							DialogMessage message = new DialogMessage(dialog, result);
							DB db = new DB(_hActivity);
							SQLiteDatabase writer = db.getWritableDatabase();
							dialog.AddMessage(message, writer);
							dialog.IncrementUnread(writer);
							writer.close();
							db.close();
							
							if (_hActivity.GetManager().GetCurrentFragment() != null)
							{
								_hActivity.GetManager().GetCurrentFragment().HeadersChanged(dialog);
							}

							JSONObject options = new JSONObject();
							options.put("userid", userID);
							options.put("status", 1);
							JSONArray list = new JSONArray();
							list.put(message.GetID());
							options.put("list", list);
							_Socket.Send("message.setstatus", options);
						}
					}//if (type == 0 || type == 2)
					else //if (type == 1)
					{
						String chatID = result.getString("entryid");
						
						if (_hActivity.GetManager().GetCurrentFragment() != null
								&& _hActivity.GetManager().GetCurrentFragment() instanceof FragmentChat 
								&& ((FragmentChat) _hActivity.GetManager().GetCurrentFragment()).GetChat().GetID().equals(chatID)
								&& ((FragmentChat) _hActivity.GetManager().GetCurrentFragment()).IsInited())
						{
							_hActivity.GetManager().GetCurrentFragment().MessageReceived(pMID, pAction, pStatus, pResult, pType, pOptions);
						}
						else
						{
							Chat chat = _hActivity.GetChats().GetChat(chatID);
							ChatMessage message = new ChatMessage(chat, result);
							DB db = new DB(_hActivity);
							SQLiteDatabase writer = db.getWritableDatabase();
							chat.AddMessage(message, writer);
							chat.IncrementUnread(writer);
							writer.close();
							db.close();
							
							if (chat.GetName().length() == 0)
							{
								// чата такого нет
								JSONObject options = new JSONObject();
								options.put("id", chatID);
								_GetChatInfoMID = _Socket.Send("groupchat.info", options);
							}
							
							if (_hActivity.GetManager().GetCurrentFragment() != null)
							{
								_hActivity.GetManager().GetCurrentFragment().HeadersChanged(chat);
							}

							JSONObject options = new JSONObject();
							options.put("id", chatID);
							options.put("status", 1);
							JSONArray list = new JSONArray();
							list.put(message.GetID());
							options.put("list", list);
							_Socket.Send("message.setstatus", options);
						}
					}//if (type == 2)
					
					if (_hActivity.GetUsers().GetUser(userID) == null)
					{
						// нет инфо об этом пользователе
						_GetUsersInfoMID = _Socket.Send("user.list", Users.GetUsersOptions(_hActivity, userID));
					}
					
					Push.Call(_hActivity);
				}
				catch (JSONException e)
				{
					Util.CreateBreakPoint("onmessage");
				}
			}
		}
		// получаем доп информацию
		else if (pAction.equals("user.list") && _GetUsersInfoMID.equals(pMID))
		{	
			if (pStatus == 1000)
			{		
				try
				{	
					_hActivity.GetUsers().AddUsers(new JSONArray(pResult), _hActivity);
				}
				catch (JSONException e)
				{
					Util.CreateBreakPoint("user.list");
				}
			}
		}
		else if (pAction.equals("groupchat.info") && _GetChatInfoMID.equals(pMID))
		{
			if (pStatus == 1000)
			{		
				try
				{	
					JSONObject result = new JSONObject(pResult);
					String chatID = result.getString("id");
					Chat chat = _hActivity.GetChats().GetChat(chatID);
					chat.Update(result, _hActivity.GetProfile().GetID());
					
					if (_hActivity.GetManager().GetCurrentFragment() != null)
					{
						_hActivity.GetManager().GetCurrentFragment().HeadersChanged(chat);
					}
				}
				catch (JSONException e)
				{
					Util.CreateBreakPoint("groupchat.info");
				}
			}
		}
		// отправили сообщение
		else if (pAction.equals("message.send"))
		{  
			if (pStatus == 1000)
			{		
				try
				{
					Dialog dialog = _hActivity.GetDialogs().GetDialog(pOptions.getJSONArray("userlist").getLong(0));
					DialogMessage message = dialog.GetMessage(pMID);
					JSONObject result = new JSONObject(pResult);		
					DB db = new DB(_hActivity);
					SQLiteDatabase writer = db.getWritableDatabase();
					message.Sended(_hActivity, result, writer);
					writer.close();
					db.close();
				}
				catch (Exception e)
				{
					Util.CreateBreakPoint("message.send");
				}
			}	
		}
		else if (pAction.equals("mailing.send"))
		{ 
			if (pStatus == 1000)
			{		
				try
				{
//					Delivery delivery = _hActivity.GetDeliveries().GetDelivery(pOptions.getString("id"));
//					DeliveryMessage deliveryMessage = delivery.GetMessage(pMID);
//					JSONObject result = new JSONObject(pResult);
//					deliveryMessage.Sended_DB(_hActivity, result);
					
//					for (int i = 0; i < delivery.GetUsers().size(); i++)
//					{
//						long userID = delivery.GetUsers().get(i);
//						
//						Dialog dialog = _hActivity.GetDialogs().GetDialog(userID, false);
//						if (dialog != null)
//						{
//							// теперь надо определить загружены ли сообщения из локальной БД
//							dialog.GetMessages();
//							dialog.AddMessage(deliveryMessage, delivery.GetID());
//						}
//					}
				}
				catch (Exception e)
				{
					Util.CreateBreakPoint("mailing.send");
				}
			}	
		}
		else if (pAction.equals("groupchat.send"))
		{
			if (pStatus == 1000)
			{	
//				try
//				{
//					Chat chat = _hActivity.GetChats().GetChat(pOptions.getString("id"));
//					ChatMessage message = chat.GetMessage(pMID);
//					JSONObject result = new JSONObject(pResult);
//					message.Sended_DB(_hActivity, result);
//				}
//				catch (Exception e)
//				{
//					Util.CreateBreakPoint("groupchat.send");
//				}
			}
		}
		// ставим статус сообщениям из диалога и группы
		else if (pAction.equals("message.setstatus") || pAction.equals("onmessagestatus"))
		{
			if (pStatus == 1000)
			{
				DB db = new DB(_hActivity);
				SQLiteDatabase writer = db.getWritableDatabase();
				try
				{
					if (pAction.equals("message.setstatus"))
					{
						JSONArray list = pOptions.getJSONArray("list");
						int status = pOptions.getInt("status");
						if (pOptions.has("userid"))
						{
							// диалог
							long userID = pOptions.getLong("userid");
							Dialog dialog = _hActivity.GetDialogs().GetDialog(userID);
							for (int i = 0; i < list.length(); i++)
							{
								String messageID = list.getString(i);
								DialogMessage message = dialog.GetMessage(messageID);
								if (message != null && message.SetStatus(status))
								{
									dialog.UpdateMessage(message, writer);
									if (message.GetStatus() == AbstractMessage.STATUS_READED)
									{
										dialog.DicrementUnread(writer);
									}
								}
							}
						}
						else
						{
							// чат
							String chatID = pOptions.getString("id");
							Chat chat = _hActivity.GetChats().GetChat(chatID);
							for (int i = 0; i < list.length(); i++)
							{
								String messageID = list.getString(i);
								ChatMessage message = chat.GetMessage(messageID);
								if (message != null && message.SetStatus(status))
								{
									chat.UpdateMessage(message, writer);
									if (message.GetStatus() == AbstractMessage.STATUS_READED)
									{
										chat.DicrementUnread(writer);
									}
								}
							}
						}
					}
					else// if (pAction.equals("onmessagestatus"))
					{
						JSONObject result = new JSONObject(pResult);
						long userID = result.getLong("fromuserid");
						Dialog dialog = _hActivity.GetDialogs().GetDialog(userID, false);
						if (dialog != null)
						{
							String mid = result.getString("id");
							DialogMessage message = dialog.GetMessage(mid);
							if (message != null)
							{
								if (message != null && message.SetStatus(result.getInt("status")))
								{
									dialog.UpdateMessage(message, writer);
									if (message.GetStatus() == AbstractMessage.STATUS_READED)
									{
										dialog.DicrementUnread(writer);
									}
								}
							}
						}
					}// if (pAction.equals("message.setstatus"))
				}
				catch (JSONException e)
				{
					Util.CreateBreakPoint("message.setstatus");
				}
				writer.close();
				db.close();
			}
		}
		else if (pAction.equals("onmailingstatus"))
		{
//			if (messageStatus == 1000)
//			{
//				try
//				{
//				}
//				catch (JSONException e)
//				{
//				}
//			}
		}
		// удалем диалог группу и рассылку
		else if (pAction.equals("message.clearhistory"))
		{
			if (pStatus == 1000)
			{
				try
				{
					long userID = pOptions.getLong("userid");
					Dialog dialog = _hActivity.GetDialogs().Remove(userID);
					
					if (_hActivity.GetManager().GetCurrentFragment() != null)
					{
						_hActivity.GetManager().GetCurrentFragment().HeadersChanged(dialog);
					}
				}
				catch (JSONException e)
				{
					Util.CreateBreakPoint("message.clearhistory");
				}
			}
		}
		else if (pAction.equals("groupchat.delete"))
		{
			if (pStatus == 1000)
			{
				try
				{
					String id = pOptions.getString("id");
					Chat chat = _hActivity.GetChats().Remove(id);
					
					if (_hActivity.GetManager().GetCurrentFragment() != null)
					{
						_hActivity.GetManager().GetCurrentFragment().HeadersChanged(chat);
					}
				}
				catch (JSONException e)
				{
					Util.CreateBreakPoint("groupchat.delete");
				}
			}
		}
		else if (pAction.equals("mailing.delete"))
		{
			if (pStatus == 1000)
			{
				try
				{
					String id = pOptions.getString("id");
					Delivery delivery = _hActivity.GetDeliveries().Remove(id);
					
					if (_hActivity.GetManager().GetCurrentFragment() != null)
					{
						_hActivity.GetManager().GetCurrentFragment().HeadersChanged(delivery);
					}
				}
				catch (JSONException e)
				{
					Util.CreateBreakPoint("mailing.delete");
				}
			}
		}
		// удалем сообщение из диалога и рассылки
		else if (pAction.equals("message.delete")) 
		{
			if (pStatus == 1000)
			{					
				DB db = new DB(_hActivity);
				SQLiteDatabase writer = db.getWritableDatabase();
				try
				{
					if (pOptions.has("userid"))
					{
						long userID = pOptions.getLong("userid");
						Dialog dialog =_hActivity.GetDialogs().GetDialog(userID);
						dialog.RemoveMessage(_hActivity, pOptions.getJSONArray("list").getString(0), writer);
					}
					else
					{
						String id = pOptions.getString("id");
						if (pOptions.has("chat"))
						{
							Chat chat = _hActivity.GetChats().GetChat(id);
							chat.RemoveMessage(_hActivity, pOptions.getJSONArray("list").getString(0), writer);
						}
						else
						{
							Delivery delivery = _hActivity.GetDeliveries().GetDelivery(id);
							delivery.RemoveMessage(_hActivity, pOptions.getJSONArray("list").getString(0), writer);
						}
					}
				}
				catch (JSONException e)
				{
					Util.CreateBreakPoint("message.delete");
				}
				writer.close();
				db.close();
			}
		}
		// добавили в группу
		else if (pAction.equals("ongroupchatappend"))
		{
			if (pStatus == 1000)
			{
				try
				{
					JSONObject result = new JSONObject(pResult);
					String chatID = result.getString("id");
					Chat chat = _hActivity.GetChats().GetChat(chatID);
					chat.Update(result, _hActivity.GetProfile().GetID());
					
					if (_hActivity.GetManager().GetCurrentFragment() != null)
					{
						_hActivity.GetManager().GetCurrentFragment().HeadersChanged(chat);
					}
				}
				catch (JSONException e)
				{
					Util.CreateBreakPoint("ongroupchatappend");
				}
			}
		}
		else
		{
			if (_hActivity.GetManager().GetCurrentFragment() != null)
			{
				_hActivity.GetManager().GetCurrentFragment().MessageReceived(pMID, pAction, pStatus, pResult, pType, pOptions);
			}
		}

		if (pType == MessageItem.MESSAGE_TYPE_RESEND)
		{
			// отправляем следующие
			_NotSended.SetSended(pMID);
			MessageItem item = _NotSended.GetFirst();
			if (item != null)
			{
				_Socket.Send(item);
			}
		}
	}
}