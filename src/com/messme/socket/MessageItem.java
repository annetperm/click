package com.messme.socket;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;


import android.content.ContentValues;


public class MessageItem
{
	public final static int	   MESSAGE_TYPE_SEND 	= 100;	
	public final static int	   MESSAGE_TYPE_RESEND 	= 200;	
	public final static int	   MESSAGE_TYPE_MESSAGE	= 300;
	//public final static int	   MESSAGE_TYPE_SERVICE	= 400;	
	
	
	private final String _MID;
	private final String _Action;
	private JSONObject _Options;
	private int _Type;
	private final Date _SendTime;
	
	public MessageItem(String pMID, String pAction, JSONObject pOptions)
	{
		_MID = pMID;
		_Action = pAction;
		_Options = pOptions;
		_Type = MESSAGE_TYPE_SEND;
		_SendTime = new Date();
	}
	public MessageItem(String pMID, String pAction, String pOptions)
	{
		_MID = pMID;
		_Action = pAction;
		if (pOptions.equals(""))
		{
			_Options = null;
		}
		else
		{
			try
			{
				_Options = new JSONObject(pOptions);
			}
			catch (JSONException e)
			{
				_Options = null;
			}
		}
		_Type = MESSAGE_TYPE_RESEND;
		_SendTime = new Date();
	}
	
	@Override
	public String toString()
	{
		try
		{
			JSONObject message = new JSONObject();
			message.put("mid", _MID);
			message.put("action", _Action);
			if (_Options != null)
			{
				message.put("options", _Options);
			}
			return message.toString();
		}
		catch (JSONException e)
		{
		}
		return "";
	}

	public int GetType()
	{
		return _Type;
	}

	public String GetMID()
	{
		return _MID;
	}

	public String GetAction()
	{
		return _Action;
	}

	public JSONObject GetOptions()
	{
		if (_Options == null)
		{
			return null;
		}
		return _Options;
	}
	
	public long GetDifferent(long pValue)
	{
		return pValue - _SendTime.getTime();
	}

	public ContentValues GetCV()
	{
		ContentValues cv = new ContentValues();
		cv.put("mid", _MID);
	    cv.put("action", _Action);
	    cv.put("options", _Options == null ? "" : _Options.toString());
	    return cv;
	}

	public void ToResend()
	{
		_Type = MESSAGE_TYPE_RESEND;
	}
}