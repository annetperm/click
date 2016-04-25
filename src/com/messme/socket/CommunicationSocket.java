package com.messme.socket;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.codebutler.android_websockets.WebSocketClient;
import com.messme.util.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class CommunicationSocket implements WebSocketClient.Listener
{	
	private static final String _CONNECTION_HEADER = "wss://login.messme.me:8101/";

	private static final int _CONNECTED = 1;
	private static final int _REQUEST 	= 2;
	private static final int _ERROR_S 	= 3;
	private static final int _SEND	 	= 4;
	private static final int _ERROR_T	= 5;
	
	public interface iSocketListener
	{
		public void OnConnect();
		public void OnMessageSendError(MessageItem pItem, boolean pTimeOut);
		public void OnMessageSendSuccess(MessageItem pItem);
		public void OnMessageReceive(String pMID, String pAction, int pStatus, String pResult, int pType, JSONObject pOptions);
	}
	
	private class WaitServerRespond extends TimerTask 
	{
		@Override
		public void run() 
		{
			long value = new Date().getTime();
			synchronized (_Messages)
			{
				for (String key : _Messages.keySet()) 
				{
				    MessageItem item = _Messages.get(key);
				    if (item.GetDifferent(value) > 30 * 1000)
				    {
				    	// это сообщение висит
				    	onErrorSend(new Exception("TimeOut"), item.toString());
				    }
				}
			}
		}
	}
	
	private class Reconnector extends TimerTask 
	{		
		@Override
		public void run() 
		{
			if (_ConnectionAlive && _Socket != null)
			{
				_Log("Reconnection...");
				if (!_Socket.isConnected())
				{
					_Socket.connect();
				}
			}
		}
	}
	
	private Handler _MessageHandler = new Handler()
	{
		@Override
		public void handleMessage(Message pMessage)
		{
			switch (pMessage.what)
			{
				case _CONNECTED:
			        _hListener.OnConnect();
					break;
					
				case _REQUEST:
					try
					{
						JSONObject message = new JSONObject(pMessage.obj.toString());
						String action = message.getString("action");
						String mid;
						if (action.equals("onopen"))
						{
							mid = "";
						}
						else
						{
							mid = message.getString("mid");
						}
						int status = message.getInt("status");
						String result = message.getString("result");
						MessageItem sendItem;
						synchronized (_Messages)
						{
							sendItem = _Messages.remove(mid);
						}		
						if (sendItem == null)
						{
					        _hListener.OnMessageReceive(mid, action, status, result, MessageItem.MESSAGE_TYPE_MESSAGE, null);
						}
						else
						{
							_hListener.OnMessageReceive(mid, action, status, result, sendItem.GetType(), sendItem.GetOptions());
						}
					}
					catch (JSONException e)
					{
					}
					break;
					
				case _ERROR_S:
				case _ERROR_T:
					MessageItem errorItem;
					synchronized (_Messages)
					{
						errorItem = _Messages.remove(pMessage.obj.toString());
					}			
					if (errorItem != null)
					{
				        _hListener.OnMessageSendError(errorItem, pMessage.what == _ERROR_T);
					}
					break;
					
				case _SEND:
					MessageItem successItem;
					synchronized (_Messages)
					{
						successItem = _Messages.get(pMessage.obj.toString());
					}			
					if (successItem != null)
					{
				        _hListener.OnMessageSendSuccess(successItem);
					}
					break;
			}
			super.handleMessage(pMessage);
		}
	};
	
	private boolean _ConnectionAlive = true;
	private WebSocketClient _Socket = null;
	
	private final iSocketListener _hListener;
	private String _ConnectionParameters;
	
	private volatile HashMap<String, MessageItem> _Messages = new HashMap<String, MessageItem>();
	
	private long _Timeout = 0;
	private int _TryConnection = 0;
	private Timer _Timer = new Timer();
	private Timer _ServerRespondTimer = new Timer();
	
	
	public CommunicationSocket(iSocketListener pListener)
	{
		_hListener = pListener;
		_ServerRespondTimer.schedule(new WaitServerRespond(), 10000, 10000);
	}
	
	public void Connect(String pParameters)
	{
		_ConnectionParameters = pParameters.replace("\n", "");
		
		_ConnectionAlive = true;
		if (_Socket == null)
		{
			_Log("New connection - " + _CONNECTION_HEADER + _ConnectionParameters);
			 _Socket = new WebSocketClient(URI.create(_CONNECTION_HEADER + _ConnectionParameters), this, null);
		}
		else if (!_Socket.GetUri().toString().equals(_CONNECTION_HEADER + _ConnectionParameters))
		{
			_Log("New connection - " + _CONNECTION_HEADER + _ConnectionParameters);
			_Socket.disconnect(false);
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
			}
			 _Socket = new WebSocketClient(URI.create(_CONNECTION_HEADER + _ConnectionParameters), this, null);
		}
		
		if (_Socket.isConnected())
		{
			onConnect();
		}
		else
		{
			_Socket.connect();
		}
	}
	public void Disconnect()
	{
		_ConnectionAlive = false;
		if (_Socket != null && _Socket.isConnected())
		{
			_Socket.disconnect(false);
		}
		_Socket = null;
	}

	public void Reconnect(Context pContext)
	{
		if (_Socket != null)
		{
		    ConnectivityManager cm = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnectedOrConnecting())
			{
				_Log("Reconnect - OnLine");
				// появился интернет
				_Timeout = 0;
				_TryConnection = 0;
			}
			else
			{
				_Log("Reconnect - OffLine");
				// нет инета
				_TryConnection = 100;
			}
		}
	}
	
	public String Send(String pAction, JSONObject pOptions)
	{
		return Send(new MessageItem(Util.GenerateMID(), pAction, pOptions));
	}
	public String Send(String pMID, String pAction, JSONObject pOptions)
	{
		return Send(new MessageItem(pMID, pAction, pOptions));
	}
	public String Send(MessageItem pItem)
	{
		synchronized (_Messages)
		{
			_Messages.put(pItem.GetMID(), pItem);
		}
		
		if (_Socket == null)
		{
	    	onErrorSend(null, pItem.toString());
		}
		else if (_Socket.isConnected())
		{
			_Socket.send(pItem.toString());
		}
		else
		{
	    	onErrorSend(null, pItem.toString());
		}
		return pItem.GetMID();
	}
	
	
	@Override
	public void onConnect()
	{
        _Log("onConnect");
		Message threadMessage = new Message();
		threadMessage.what = _CONNECTED;
		_MessageHandler.sendMessage(threadMessage);
	}
	@Override
	public void onDisconnect(boolean pError)
	{
		if (pError)
		{
			_Log("onDisconnect ERROR");
		}
		else
		{
			_Log("onDisconnect SUCCESS");
		}
		
		if (_ConnectionAlive)
		{
			_Timer.schedule(new Reconnector(), _Timeout);
		}
		else
		{
			_Socket = null;
		}
	}
	@Override
	public void onSend(String pMessage)
	{
		_Log("onSend: " + pMessage);
		
		try
		{
			JSONObject message = new JSONObject(pMessage);
			String mid = message.getString("mid");
			
			Message threadMessage = new Message();
			threadMessage.what = _SEND;
			threadMessage.obj = mid;
			_MessageHandler.sendMessage(threadMessage);
		}
		catch (JSONException e)
		{
		}
	}
	@Override
	public void onErrorSend(Exception error, String pMessage)
	{
		_Log("onErrorSend: " + pMessage);
		try
		{
			JSONObject message = new JSONObject(pMessage);
			String mid = message.getString("mid");
			
			Message threadMessage = new Message();
			if (error != null && error.getMessage().equals("TimeOut"))
			{
				threadMessage.what = _ERROR_T;
			}
			else
			{
				threadMessage.what = _ERROR_S;
			}
			threadMessage.obj = mid;
			_MessageHandler.sendMessage(threadMessage);
		}
		catch (JSONException e)
		{
		}
		if (_Socket != null)
		{
			_Socket.disconnect(true);
		}
	}
	@Override
	public void onMessage(String pMessage)
	{
		_Log("onMessage: " + pMessage);
		
		Message threadMessage = new Message();
		threadMessage.what = _REQUEST;
		threadMessage.obj = pMessage;
		_MessageHandler.sendMessage(threadMessage);
	}
	@Override
	public void onMessage(byte[] data)
	{
		_Log("Socket receiving binary data");
	}
	@Override
	public void onError(Exception pError)
	{
		if (pError == null || pError.getMessage() == null)
		{
			_Log("onError ERROR_SERVER");
			_Timeout += 1000;
			_Timer.schedule(new Reconnector(), _Timeout);
		}
		else if (pError.getMessage().equals("Unable to resolve host \"login.messme.me\": No address associated with hostname"))
		{
			_Log("onError ERROR_NOCONNECTION");
			_TryConnection++;
			if (_TryConnection > 100)
			{
				_Timeout += 1000;
			}
			_Timer.schedule(new Reconnector(), _Timeout);
		}
		else
		{
			_Log("onError ERROR_SERVER");
			_Timeout += 1000;
			_Timer.schedule(new Reconnector(), _Timeout);
		}
	}
	
	private void _Log(String pMessage)
	{
		Log.d("MessageSocket", pMessage);
	}
}