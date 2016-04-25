package com.messme.socket;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.codebutler.android_websockets.WebSocketClient;
import com.messme.ActivityMain;
import com.messme.Push;
import com.messme.chats.ChatHeader;
import com.messme.data.DB;
import com.messme.profile.Profile;
import com.messme.chats.messages.chat.Chat;
import com.messme.chats.messages.chat.ChatMessage;
import com.messme.chats.messages.chat.Chats;
import com.messme.chats.messages.dialog.Dialog;
import com.messme.chats.messages.dialog.DialogMessage;
import com.messme.chats.messages.dialog.Dialogs;
import com.messme.user.Users;
import com.messme.util.Util;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


public class MessmeService //extends Service implements WebSocketClient.Listener
{
//	public final static String ENTRY 			= "Entry";
//	public final static String EXIT 			= "Exit";
//
//	public final static String CONNECT			= "Connect";
//	public final static String CONNECT_FOLDER	= "ConnectFolder";
//	public final static String CONNECT_PARAMETR	= "ConnectParametr";
//	
//	public final static String DISCONNECT		= "Disconnect";
//
//	public final static String SEND		= "Send";
//	public final static String MID 		= "MID";
//	public final static String ACTION 	= "Action";
//	public final static String OPTIONS 	= "Options";
//
//
//	private static final int _CONNECTED = 1;
//	private static final int _DISCONNECTED = 2;
//	
//	private static final int _SEND = 3;
//	private static final int _ERROR_SEND = 5;
//	
//	private static final int _REQUEST = 4;
//	
//	private static final int _ERROR_SERVER = 6;
//	private static final int _ERROR_NOCONNECTION = 7;
//	
//	
//	private static final String _CONNECTION_HEADER = "wss://login.messme.me:8101/";
//	//private static final String _CONNECTION_HEADER = "ws://192.168.0.27:8101/";
//	
//	private class Reconnector extends TimerTask 
//	{
//		private String _ConnectionString;
//		
//		public Reconnector(String pConnectionString)
//		{
//			_ConnectionString = pConnectionString;
//		}
//		
//		@Override
//		public void run() 
//		{
//			boolean b = true;
//			if (_ConnectionAlive && _Socket != null)
//			{
//				if (_ConnectionString.equals(_CONNECTION_HEADER + _ConnectionFolder + _ConnectionParameters))
//				{
//					_Log("Reconnection...");
//					b = false;
//					_Socket.connect();
//				}
//			}
//			if (b)
//			{
//				_Log("Reconnection CANCELED");
//			}
//		}
//	}
//	private class WaitServerRespond extends TimerTask 
//	{
//		@Override
//		public void run() 
//		{
//			long value = new Date().getTime();
//			synchronized (_Messages)
//			{
//				for (String key : _Messages.keySet()) 
//				{
//				    MessageItem item = _Messages.get(key);
//				    if (item.GetDifferent(value) > 30 * 1000)
//				    {
//				    	// это сообщение висит
//						Message threadMessage = new Message();
//						threadMessage.what = _ERROR_SEND;
//						threadMessage.obj = item.toString();
//						_UpdateHandler.sendMessage(threadMessage);
//				    }
//				}
//			}
//		}
//	}
//	
//	private boolean _ActivityAlive = false;
//	
//	private boolean _ConnectionAlive = false;
//	private String _ConnectionFolder = null;
//	private String _ConnectionParameters = null;
//
//	private WebSocketClient _Socket = null;
//	private int _ConnectCount = 0;
//	
//	private NetworkStateReceiver _NetSateReceiver = null;
//	
//	private volatile HashMap<String, MessageItem> _Messages = new HashMap<String, MessageItem>();
//	
//	private long _Timeout = 0;
//	private int _TryConnection = 0;
//	private Timer _Timer = new Timer();
//	private Timer _ServerRespondTimer = new Timer();
//	
//	private Dialogs _Dialogs = null;
//	private Profile _Profile = null;
//	private Chats _Chats = null;
//	
//	
//	public void OnReceive()
//	{
//	    ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
//	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
//		if (netInfo != null && netInfo.isConnectedOrConnecting())
//		{
//			_Log("OnReceive - OnLine");
//			// появился интернет
//			_Timeout = 0;
//			_TryConnection = 0;
//		}
//		else
//		{
//			_Log("OnReceive - OffLine");
//			// нет инета
//			_TryConnection = 100;
//		}
//	}
//	
//	
//	public void onCreate() 
//	{
//		super.onCreate();
//		_Log("onCreate");
//		
//		_ConnectCount = 0;
//		
//		_NetSateReceiver = new NetworkStateReceiver();
//		registerReceiver(_NetSateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
//
//		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//		long id = preferences.getLong(Profile.PREFERENCE_ID, 0);
//		String uuid = preferences.getString(Profile.PREFERENCE_UUID, "");
//		String login = preferences.getString(Profile.PREFERENCE_LOGIN, "");
//		if (!login.equals(""))
//		{
//			_ConnectionAlive = true;
//			_ConnectionFolder = "Message";
//			_ConnectionParameters = "?id=" + id + "&uuid=" + uuid;
//			_Connect();
//		}
//		else
//		{
//			_ConnectionAlive = false;
//			_ConnectionFolder = "";
//			_ConnectionParameters = "";
//		}
//		
//		_ServerRespondTimer.schedule(new WaitServerRespond(), 10000, 10000);
//	}
//
//	public void onDestroy() 
//	{
//	    super.onDestroy();
//		_Log("onDestroy");
//		unregisterReceiver(_NetSateReceiver);
//		_NetSateReceiver = null;
//		if (_Socket != null)
//		{
//			_Log("Disconnecting from server...");
//			_UpdateHandler = null;
//			_Socket.disconnect(false);
//			_Socket = null;
//		}
//	}
//
//
//	public int onStartCommand(Intent pIntent, int flags, int startId)
//	{		
//		if (pIntent != null)
//		{
//			if (pIntent.hasExtra(ENTRY))
//			{
//				_Log("onStartCommand ENTRY");
//				_ActivityAlive = true;
//			}
//			if (pIntent.hasExtra(EXIT))
//			{
//				_Log("onStartCommand EXIT");
//				_ActivityAlive = false;
//			}
//
//			if (pIntent.hasExtra(CONNECT))
//			{
//				_Log("onStartCommand CONNECT");
//				_ConnectionAlive = true;
//				_ConnectionFolder = pIntent.getStringExtra(CONNECT_FOLDER);
//				_ConnectionParameters = pIntent.getStringExtra(CONNECT_PARAMETR);
//				
//				_Connect();
//			}
//			
//			if (pIntent.hasExtra(DISCONNECT))
//			{
//				_Log("onStartCommand DISCONNECT");
//				_ConnectionAlive = false;
//				
//				if (_Socket == null)
//				{
//					Message threadMessage = new Message();
//					threadMessage.what = _DISCONNECTED;
//					_UpdateHandler.sendMessage(threadMessage);
//				}
//				else if (_Socket.isConnected())
//				{
//					_Socket.disconnect(true);
//				}
//				else
//				{
//					Message threadMessage = new Message();
//					threadMessage.what = _DISCONNECTED;
//					_UpdateHandler.sendMessage(threadMessage);
//				}
//			}
//			
//			if (pIntent.hasExtra(SEND))
//			{
//				String mid		= pIntent.getStringExtra(MID);
//				String action	= pIntent.getStringExtra(ACTION);
//				String options	= null;
//				if (pIntent.hasExtra(OPTIONS))
//				{
//					options	= pIntent.getStringExtra(OPTIONS);
//				}
//				
//				_Log("onStartCommand SEND mid: " + mid);
//
//				MessageItem message = new MessageItem(mid, action, options, pIntent.getBooleanExtra(SEND, false) ? MessageItem.MESSAGE_TYPE_SEND : MessageItem.MESSAGE_TYPE_RESEND);
//				synchronized (_Messages)
//				{
//					_Messages.put(mid, message);
//				}
//				
//				
//				if (_Socket == null)
//				{
//					Message threadMessage = new Message();
//					threadMessage.what = _ERROR_SEND;
//					threadMessage.obj = message.toString();
//					_UpdateHandler.sendMessage(threadMessage);
//				}
//				else if (_Socket.isConnected())
//				{
//					_Socket.send(message.toString());
//				}
//				else
//				{
//					Message threadMessage = new Message();
//					threadMessage.what = _ERROR_SEND;
//					threadMessage.obj = message.toString();
//					_UpdateHandler.sendMessage(threadMessage);
//				}
//			}
//		}
//		
//	    return START_STICKY;//super.onStartCommand(pIntent, flags, startId);//;START_REDELIVER_INTENT;
//	}
//	
//	
//	private Handler _UpdateHandler = new Handler() 
//	{
//		public void handleMessage(Message pMessage) 
//		{
//			switch (pMessage.what)
//			{
//				case _CONNECTED:
//					_Log("Handler message _CONNECTED");
//					_Timeout = 0;
//					_TryConnection = 0;
//					_ConnectCount++;
//					if (_ActivityAlive)
//					{
//						Intent intent = new Intent(ActivityMain.BROADCAST_ACTION);
//				        intent.putExtra(ActivityMain.BROADCAST_COMMAND, ActivityMain.SERVICE_CONNECTED);
//						intent.putExtra(ActivityMain.CONNECTION_FOLDER, _ConnectionFolder);
//				        sendBroadcast(intent);
//					}
//					Toast.makeText(MessmeService.this, "MessmeService Connected: " + Integer.toString(_ConnectCount), Toast.LENGTH_SHORT).show();
//					break;
//				case _DISCONNECTED: 
//					_Log("Handler message _DISCONNECTED");
//					if (_ConnectionAlive)
//					{
//						_Timer.schedule(new Reconnector(_CONNECTION_HEADER + _ConnectionFolder + _ConnectionParameters), _Timeout);
//					}
//					else
//					{
//						if (_ActivityAlive)
//						{
//							Intent intent = new Intent(ActivityMain.BROADCAST_ACTION);
//					        intent.putExtra(ActivityMain.BROADCAST_COMMAND, ActivityMain.SERVICE_DISCONNECTED);
//							intent.putExtra(ActivityMain.CONNECTION_FOLDER, _ConnectionFolder);
//					        sendBroadcast(intent);
//						}
//
//						_Socket = null;
//						_ConnectionFolder = "";
//						_ConnectionParameters = "";
//					}
//					break;
//				case _SEND:
//					// Устанавливаем флаг отправки сообщения
//					try
//					{
//						JSONObject message = new JSONObject(pMessage.obj.toString());
//						String mid = message.getString("mid");
//						_Log("Handler message _SEND mid: " + mid);
//
////						if (_ActivityAlive)
////						{
////							Intent intent = new Intent(ActivityMain.BROADCAST_ACTION);
////					        intent.putExtra(ActivityMain.BROADCAST_COMMAND, ActivityMain.SERVICE_SEND);
////							intent.putExtra(ActivityMain.MESSAGE_MID, mid);
////					        sendBroadcast(intent);
////						}
////						else
////						{
////							DB db = new DB(MessmeService.this);
////							SQLiteDatabase writer = db.getWritableDatabase();
////							writer.delete("notsended", "mid = ?", new String[]{mid});
////							writer.close();
////							db.close();
////						}
//					}
//					catch (JSONException e)
//					{
//					}
//					break;
//				case _ERROR_SEND:
//					try
//					{
//						JSONObject message = new JSONObject(pMessage.obj.toString());
//						String mid = message.getString("mid");
//						if (_ActivityAlive)
//						{
//							_Log("Handler message _ERROR_SEND mid: " + mid);
//							MessageItem item;
//							synchronized (_Messages)
//							{
//								item = _Messages.remove(mid);
//							}
//							if (item != null && item.GetType() == MessageItem.MESSAGE_TYPE_SEND) // незачем посылать в активити то что не смогли отправть сообщение повторно
//							{
//								Intent intent = new Intent(ActivityMain.BROADCAST_ACTION);
//						        intent.putExtra(ActivityMain.BROADCAST_COMMAND,	ActivityMain.SERVICE_SENDERROR);
//						        intent.putExtra(ActivityMain.MESSAGE_MID,		mid);
//						        intent.putExtra(ActivityMain.MESSAGE_ACTION,	item.GetAction());
//						        intent.putExtra(ActivityMain.MESSAGE_OPTIONS,	item.GetOptions());
//						        sendBroadcast(intent);
//								_Log(">>>>>>>");
//							}
//							else
//							{
//							}
//						}
//					}
//					catch (JSONException e)
//					{
//					}
//					if (_Socket != null)
//					{
//						_Socket.disconnect(true);
//					}
//					break;
//				case _REQUEST:
//					try
//					{
//						JSONObject message = new JSONObject(pMessage.obj.toString());
//						String action = message.getString("action");
//						int status = message.getInt("status");
//						String result = message.getString("result");
//						String mid = "";
//						if (action.equals("onopen"))
//						{
//							if (_ConnectionFolder.equals("User") && status == 1002)
//							{
//							}
//							else if (_ConnectionFolder.equals("Message") && status == 1000)
//							{
//							}
//							else
//							{
//								_Socket.disconnect(false);
//								_ConnectionAlive = false;
//								_ConnectionFolder = "";
//								_ConnectionParameters = "";
//								try
//								{
//									Thread.sleep(1000);
//								}
//								catch (InterruptedException e)
//								{
//								}
//								_Socket = null;
//							}
//						}
//						else
//						{
//							mid = message.getString("mid");
//						}
//						_Log("Handler message _REQUEST mid: " + mid);
//
//						MessageItem item;
//						synchronized (_Messages)
//						{
//							item = _Messages.remove(mid);
//						}
//						
//						if (_ActivityAlive)
//						{
//							_Profile = null;
//							_Dialogs = null;
//							_Chats = null;
//							
//							Intent intent = new Intent(ActivityMain.BROADCAST_ACTION);
//					        intent.putExtra(ActivityMain.BROADCAST_COMMAND,	ActivityMain.SERVICE_MESSAGE);
//					        intent.putExtra(ActivityMain.MESSAGE_MID,		mid);
//					        intent.putExtra(ActivityMain.MESSAGE_ACTION,	action);
//					        intent.putExtra(ActivityMain.MESSAGE_RESULT,	result);
//					        intent.putExtra(ActivityMain.MESSAGE_STATUS,	status);
//							if (item == null)
//							{
//								// сообщение от сервера
//						        intent.putExtra(ActivityMain.MESSAGE_TYPE, MessageItem.MESSAGE_TYPE_MESSAGE);
//							}
//							else 
//							{
//								intent.putExtra(ActivityMain.MESSAGE_TYPE, item.GetType());
//						        intent.putExtra(ActivityMain.MESSAGE_OPTIONS, item.GetOptions());
//							}
//					        sendBroadcast(intent);
//						}
//						else
//						{
//							if (_Profile == null)
//							{
//								_Profile = new Profile(MessmeService.this);
//								_Dialogs = new Dialogs(MessmeService.this);
//								_Chats = new Chats(MessmeService.this, _Profile);
//							}
//							
//							if (action.equals("onopen") && status == 1000)
//							{
//								JSONObject options = new JSONObject();
//								options.put("locale", Locale.getDefault().getLanguage());
//								MessageItem messageItem = new MessageItem(Util.GenerateMID(), "dialog.lastlist", options.toString(), 0);
//								_Socket.send(messageItem.toString());
//							}
//							else if (action.equals("dialog.lastlist") && status == 1000)
//							{		
//								DB db = new DB(MessmeService.this);
//								SQLiteDatabase writer = db.getWritableDatabase();
//								try
//								{
//									JSONArray list = new JSONArray(result);
//									for (int i = 0; i < list.length(); i++)
//									{
//							        	ChatHeader chatHeader = new ChatHeader(_Profile.GetID(), list.getJSONObject(i));
//							    		if (chatHeader.Type == 0)
//							    		{
//							    			Dialog dialog = _Dialogs.GetDialog(chatHeader.GetUserId());
//							    			dialog.Update(chatHeader, writer);
//							    		}
//							    		else if (chatHeader.Type == 1)
//							    		{
//							    			Chat chat = _Chats.GetChat(chatHeader.Entry);
//							    			chat.Update(chatHeader, writer);
//							    		}
//									}
//									
//									Push.Call(MessmeService.this, _Profile, new Users(MessmeService.this), _Dialogs, _Chats);
//								}
//								catch (JSONException e)
//								{
//								}
//								writer.close();
//								db.close();
//							}	
//							else if (action.equals("onmessage") && status == 1000)
//							{
//								try
//								{
//									JSONObject resultJSON = new JSONObject(result);
//									long userID = resultJSON.getLong("userid");
//									Dialog dialog = _Dialogs.GetDialog(userID);
//									DialogMessage dialogMessage = dialog.AddMessage(resultJSON);
//
//									JSONObject options = new JSONObject();
//									options.put("userid", userID);
//									options.put("status", 1);
//									JSONArray list = new JSONArray();
//									list.put(dialogMessage.GetID());
//									options.put("list", list);
//
//									MessageItem messageItem = new MessageItem(Util.GenerateMID(), "message.setstatus", options.toString(), MessageItem.MESSAGE_TYPE_SERVICE);
//									_Messages.put(messageItem.GetMID(), messageItem);
//									_Socket.send(messageItem.toString());	
//
//									Push.Call(MessmeService.this, _Profile, new Users(MessmeService.this), _Dialogs, _Chats);
//								}
//								catch (JSONException e)
//								{
//								}					
//							}
//							else if (action.equals("ongroupchatmessage") && status == 1000)
//							{  		
//								try
//								{
//									JSONObject resultJSON = new JSONObject(result);
//									if (resultJSON.getLong("id") != _Profile.GetID())
//									{
//										String chatID = resultJSON.getString("entryid");
//										Chat chat = _Chats.GetChat(chatID);
//										ChatMessage chatMessage = chat.AddMessage(resultJSON);
//
//										JSONObject options = new JSONObject();
//										options.put("id", chatID);
//										options.put("status", 1);
//										JSONArray list = new JSONArray();
//										list.put(chatMessage.GetID());
//										options.put("list", list);
//										
//										MessageItem messageItem = new MessageItem(Util.GenerateMID(), "message.setstatus", options.toString(), MessageItem.MESSAGE_TYPE_SERVICE);
//										_Messages.put(messageItem.GetMID(), messageItem);
//										_Socket.send(messageItem.toString());	
//
//										Push.Call(MessmeService.this, _Profile, new Users(MessmeService.this), _Dialogs, _Chats);
//									}
//								}
//								catch (JSONException e)
//								{
//								}
//							}
//							else if (action.equals("ongroupchatappend") && status == 1000)
//							{
//								try
//								{
//									JSONObject resultJSON = new JSONObject(result);
//									String chatID = resultJSON.getString("id");
//									Chat chat = _Chats.GetChat(chatID);
//									chat.Update(resultJSON, _Profile.GetID());
//								}
//								catch (JSONException e)
//								{
//									Util.CreateBreakPoint("ongroupchatappend");
//								}
//							}
//							else if (action.equals("message.setstatus") && status == 1000)
//							{
//								try
//								{
//									DB db = new DB(MessmeService.this);
//									SQLiteDatabase writer = db.getWritableDatabase();
//									JSONObject options = new JSONObject(item.GetOptions());
//									JSONArray list = options.getJSONArray("list");
//									if (options.has("userid"))
//									{
//										// диалог
//										long userID = options.getLong("userid");
//										Dialog dialog = _Dialogs.GetDialog(userID);
//										for (int i = 0; i < list.length(); i++)
//										{
//											String messageID = list.getString(i);
//											DialogMessage dialogMessage = dialog.GetMessage(messageID);
//											if (dialogMessage != null)
//											{
//												dialogMessage.SetStatus(options.getInt("status"), writer, true);
//											}
//											else
//											{
//												Util.CreateBreakPoint("Такого быть не может");
//											}
//										}
//										dialog.ChangeUnread(writer);
//									}
//									else
//									{
//										// чат
//										String chatID = options.getString("id");
//										Chat chat = _Chats.GetChat(chatID);
//										for (int i = 0; i < list.length(); i++)
//										{
//											String messageID = list.getString(i);
//											ChatMessage chatMessage = chat.GetMessage(messageID);
//											if (chatMessage != null)
//											{
//												chatMessage.SetStatus(options.getInt("status"), writer, true);
//											}
//											else
//											{
//												Util.CreateBreakPoint("Такого быть не может");
//											}
//										}
//										chat.ChangeUnread(writer);
//									}
//									writer.close();
//									db.close();
//								}
//								catch (JSONException e)
//								{
//									Util.CreateBreakPoint("message.setstatus");
//								}
//							}
//						}//if (_ActivityAlive)
//					}
//					catch (JSONException e)
//					{
//					}
//					break;
//				case _ERROR_SERVER:
//					_Log("Handler message _ERROR_SERVER");
//					if (_ConnectionAlive)
//					{
//						_Timeout += 1000;
//						_Timer.schedule(new Reconnector(_CONNECTION_HEADER + _ConnectionFolder + _ConnectionParameters), _Timeout);
//					}
//					break;
//				case _ERROR_NOCONNECTION:
//					_Log("Handler message _ERROR_NOCONNECTION");
//					if (_ConnectionAlive)
//					{
//						_TryConnection++;
//						if (_TryConnection > 100)
//						{
//							_Timeout += 1000;
//						}
//						_Timer.schedule(new Reconnector(_CONNECTION_HEADER + _ConnectionFolder + _ConnectionParameters), _Timeout);
//					}
//					break;
//			}
//			super.handleMessage(pMessage);
//		}
//	};
//
//	@Override
//	public IBinder onBind(Intent pIntent)
//	{
//		_Log("onBind");
//	    return new Binder();
//	}
//
//	@Override
//	public void onConnect()
//	{
//		_Log("Socket connection open");
//		Message threadMessage = new Message();
//		threadMessage.what = _CONNECTED;
//		_UpdateHandler.sendMessage(threadMessage);
//	}
//	@Override
//	public void onDisconnect(boolean pError)
//	{
//		if (pError)
//		{
//			_Log("Socket disconnected ERROR");
//		}
//		else
//		{
//			_Log("Socket disconnected SUCCESS");
//		}
//		Message threadMessage = new Message();
//		threadMessage.what = _DISCONNECTED;
//		_UpdateHandler.sendMessage(threadMessage);
//	}
//	@Override
//	public void onSend(String pMessage)
//	{
//		_Log("Socket sending success: " + pMessage);
//		Message threadMessage = new Message();
//		threadMessage.what = _SEND;
//		threadMessage.obj = pMessage;
//		_UpdateHandler.sendMessage(threadMessage);
//	}
//	@Override
//	public void onErrorSend(Exception error, String pMessage)
//	{
//		_Log("Socket sending error: " + pMessage);
//		Message threadMessage = new Message();
//		threadMessage.what = _ERROR_SEND;
//		threadMessage.obj = pMessage;
//		_UpdateHandler.sendMessage(threadMessage);
//	}
//	@Override
//	public void onMessage(String pMessage)
//	{
//		_Log("Socket receiving: " + pMessage);
//		Message threadMessage = new Message();
//		threadMessage.what = _REQUEST;
//		threadMessage.obj = pMessage;
//		_UpdateHandler.sendMessage(threadMessage);
//	}
//	@Override
//	public void onMessage(byte[] data)
//	{
//		_Log("Socket receiving binary data");
//	}
//	@Override
//	public void onError(Exception pError)
//	{
//		Message threadMessage = new Message();
//		if (pError == null)
//		{
//			_Log("Socket error: null");
//			threadMessage.what = _ERROR_SERVER;
//		}
//		else if (pError.getMessage() == null)
//		{
//			_Log("Socket error: " + pError.getMessage());
//			threadMessage.what = _ERROR_SERVER;
//		}
//		else if (pError.getMessage().equals("Unable to resolve host \"login.messme.me\": No address associated with hostname"))
//		{
//			_Log("Socket error: " + pError.getMessage());
//			threadMessage.what = _ERROR_NOCONNECTION;
//		}
//		else
//		{
//			_Log("Socket error: " + pError.getMessage());
//			threadMessage.what = _ERROR_SERVER;
//		}
//		_UpdateHandler.sendMessage(threadMessage);
//	}
//	
//	
//	private void _Connect()
//	{
//		_Log("Connecting to: " + _ConnectionParameters);
//		if (_Socket == null)
//		{
//			_Log("New connection");
//			 _Socket = new WebSocketClient(URI.create(_CONNECTION_HEADER + _ConnectionFolder + _ConnectionParameters), this, null);
//		}
//		else if (!_Socket.GetUri().toString().equals(_CONNECTION_HEADER + _ConnectionFolder + _ConnectionParameters))
//		{
//			_Log("New connection");
//			_Socket.disconnect(false);
//			try
//			{
//				Thread.sleep(1000);
//			}
//			catch (InterruptedException e)
//			{
//			}
//			 _Socket = new WebSocketClient(URI.create(_CONNECTION_HEADER + _ConnectionFolder + _ConnectionParameters), this, null);
//		}
//		
//		if (_Socket.isConnected())
//		{
//			Message threadMessage = new Message();
//			threadMessage.what = _CONNECTED;
//			_UpdateHandler.sendMessage(threadMessage);
////			_Socket.disconnect(false);
////			try
////			{
////				Thread.sleep(1000);
////			}
////			catch (InterruptedException e)
////			{
////			}
////			 _Socket = new WebSocketClient(URI.create(_CONNECTION_HEADER + _ConnectionFolder + _ConnectionParameters), this, null);
////			 _Socket.connect();
//		}
//		else
//		{
//			 _Socket.connect();
//		}
//	}
//	
//	private void _Log(String pMessage)
//	{
//		Log.d("MessmeService", pMessage);
//	}
}