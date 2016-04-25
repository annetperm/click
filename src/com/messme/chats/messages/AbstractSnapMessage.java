package com.messme.chats.messages;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.data.DB;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public abstract class AbstractSnapMessage extends AbstractMessage
{
	private final boolean _IsSnap;
	private int _Timer;
	
	private boolean _SnapTimerStarted = false;	
	private int _TimerTick = 0;
	
	
	public AbstractSnapMessage(AbstractMessageContainer<? extends AbstractSnapMessage> pContainer, Cursor pCursor)
	{
		super(pContainer, pCursor);
		_IsSnap	= pCursor.getInt(8) == 1 ? true : false;
		_Timer = pCursor.getInt(9);
	}
    public AbstractSnapMessage(AbstractMessageContainer<? extends AbstractSnapMessage> pContainer, JSONObject pJSON) throws JSONException
    {
		super(pContainer, pJSON);
		_IsSnap	= pJSON.getInt("type") == 0 ? false : true;
		if (_IsSnap)
		{
			try
			{
				_Timer = pJSON.getInt("timer");
			}
			catch (Exception e)
			{
				_Timer = 0;
			}
		}
		else
		{
			_Timer = 0;
		}
    }
    public AbstractSnapMessage(AbstractMessageContainer<? extends AbstractSnapMessage> pContainer, int pTimer, double pLat, double pLng, String pMessage, boolean pIsAttachmentUploaded, ArrayList<Attachment> pAttachments)
    {
		super(pContainer, pLat, pLng, pMessage, pIsAttachmentUploaded, pAttachments);
		_Timer = pTimer;
		if (_Timer == 0)
		{
			_IsSnap = false;
		}
		else
		{
			_IsSnap = true;
		}
    }
    
    
	public final boolean IsSnap()
	{
		return _IsSnap;
	}
    
    
	public final int GetSnapTimer()
	{
		return _Timer;
	}
	
	public final String GetTickedSnapTimer()
	{
		return Integer.toString(_TimerTick);
	}


	public void StartSnapTimer(ActivityMain pActivity)
	{
		if (_IsSnap && GetStatus() == AbstractMessage.STATUS_READED && !_SnapTimerStarted)
		{
			_SnapTimerStarted = true;
			new AsyncTask<ActivityMain, Integer, ActivityMain>()
			{
				@Override
				protected ActivityMain doInBackground(ActivityMain... pParams)
				{
					_TimerTick = _Timer;
					for (int i = 0 ; i < _Timer; i++)
					{
						publishProgress(i);
						try
						{
							Thread.sleep(1000);
						}
						catch (InterruptedException e)
						{
						}
					}
					return pParams[0];
				}
				@Override
				protected void onPostExecute(ActivityMain pResult)
				{
					DB db = new DB(pResult);
					SQLiteDatabase writer = db.getWritableDatabase();
					__hContainer.RemoveMessage(pResult, GetID(), writer);
					writer.close();
					db.close();
				}
				@Override
				protected void onProgressUpdate(Integer... pValues)
				{
					_TimerTick = _Timer - pValues[0];
					RefreshView();
				}
			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pActivity);
		}
	}

	
	@Override
	public ContentValues GetCV()
	{
		ContentValues cv = super.GetCV();
	    cv.put("issnap", _IsSnap ? 1 : 0);
	    cv.put("timer", _Timer);
	    return cv;
	}
	
	@Override
	protected JSONObject __GetOptions() throws JSONException
	{
		JSONObject options = super.__GetOptions();
		options.put("type", _IsSnap ? 1 : 0);
		options.put("timer", _Timer);
		return options;
	}
}