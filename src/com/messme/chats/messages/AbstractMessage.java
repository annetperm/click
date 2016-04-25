package com.messme.chats.messages;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.socket.AttachmentsUploader;
import com.messme.util.DateUtil;
import com.messme.util.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;


public abstract class AbstractMessage
{
	public final static int STATUS_UPLOADING	= -4;	// только мои сообщения
	public final static int STATUS_NOTUPLOADED	= -3;	// только мои сообщения
	public final static int STATUS_NOTSENDED	= -2;	// только мои сообщения
	public final static int STATUS_SENDED_E		= -1;	// только мои сообщения
	public final static int STATUS_SENDED		= 0;	// только мои сообщения
	public final static int STATUS_RECEIVED		= 1;
	public final static int STATUS_READED		= 2;

	public final static int TYPE_TEXT		= 10;	
	public final static int TYPE_PHOTO		= 20;	
	public final static int TYPE_VIDEO		= 30;	
	public final static int TYPE_AUDIO		= 40;	
	public final static int TYPE_VOICE		= 50;	
	public final static int TYPE_FILE		= 60;	
	public final static int TYPE_PLACE		= 70;	
	public final static int TYPE_CONTACT	= 80;	
	public final static int TYPE_FILES		= 90;	
	
	private String _ID;
	private Date _DateTime; 
	protected int __Status;			
	private String _Message;
	protected int __Count;	
	private double _Lat;	
	private double _Lng;	
	private boolean _Sync;			
	
	protected final ArrayList<Attachment> __Attachments = new ArrayList<Attachment>();
	
	protected final AbstractMessageContainer<? extends AbstractMessage> __hContainer;
    
    private int _Progress = 0;
	
	
    public AbstractMessage(AbstractMessageContainer<? extends AbstractMessage> pContainer, Cursor pCursor)
    {
    	__hContainer = pContainer;
    	
		_ID	= pCursor.getString(0);
		try 
		{  
			_DateTime = DateUtil.DATE_TIME.parse(pCursor.getString(1));  //2015-10-01T08:10:53
		} 
		catch (ParseException e) 
		{  
			_DateTime = new Date();
		}
		__Status	= pCursor.getInt(2);
		_Message = pCursor.getString(3);
		__Count	= pCursor.getInt(4);
		_Lat = pCursor.getDouble(5);
		_Lng = pCursor.getDouble(6);
		_Sync = pCursor.getInt(7) == 1 ? true : false;
    }
 	public final void AddAttachmentFromDB(Attachment pAttachment)
	{
		pAttachment.SetMessage(this);
		__Attachments.add(pAttachment);
	}
    public AbstractMessage(AbstractMessageContainer<? extends AbstractMessage> pContainer, JSONObject pJSON) throws JSONException
    {
    	__hContainer = pContainer;
    	
		_ID	= pJSON.getString("id");
		try 
		{  
			_DateTime = DateUtil.DATE_TIME.parse(pJSON.getString("date"));  //2015-10-01T08:10:53
		} 
		catch (ParseException e) 
		{  
			_DateTime = new Date();
		}
		__Status	= pJSON.getInt("status");
		_Message = pJSON.getString("message");
		if (pJSON.getJSONArray("attach") != null && pJSON.getJSONArray("attach").length() > 0)
		{
			__Count = pJSON.getJSONArray("attach").length();
			for (int i = 0; i < pJSON.getJSONArray("attach").length(); i++)
			{
				JSONObject attach = pJSON.getJSONArray("attach").getJSONObject(i);
				__Attachments.add(new Attachment(attach, this));
			}
		}
		else
		{
			__Count = 0;
		}
		if (pJSON.has("lat"))
		{
			_Lat = pJSON.getDouble("lat");
			_Lng = pJSON.getDouble("lng");
		}
		else
		{
			_Lat = 0.0d;
			_Lng = 0.0d;
		}
		_Sync = false;
    }
    public AbstractMessage(AbstractMessageContainer<? extends AbstractMessage> pContainer, double pLat, double pLng, String pMessage, boolean pIsAttachmentUploaded, ArrayList<Attachment> pAttachments)
    {
    	__hContainer = pContainer;

		_ID = Util.GenerateMID();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, -1 * TimeZone.getDefault().getRawOffset() / 1000 / 60 / 60);
		_DateTime = calendar.getTime();
		_Message = pMessage;
		__Count = pAttachments.size();
		if (__Count == 0)
		{
			__Status = STATUS_NOTSENDED;
		}
		else
		{
			__Status = STATUS_NOTUPLOADED;
		}
		for (int i = 0; i < __Count; i++)
		{
			pAttachments.get(i).SetMessage(this);
			__Attachments.add(pAttachments.get(i));
		}
		if (__Status == STATUS_NOTUPLOADED && pIsAttachmentUploaded)
		{
			__Status = STATUS_NOTSENDED;
		}
		_Lat = pLat;
		_Lng = pLng;
		_Sync = false;
    }
    
    
	public final String GetID()
	{
		return _ID;
	}
	public final String GetDate()
	{
		return DateUtil.GetDate(_DateTime);
	}
	public final Date GetDateTime()
	{
		return _DateTime;
	}
	public final String GetTime()
	{
		return DateUtil.GetTime(_DateTime);
	}
	public final String GetMessage()
	{
		return _Message;
	}
	public final int GetStatus()
	{
		return __Status;
	}
	public final boolean IsGeo()
	{
		return _Lat >= 0.00001d && _Lng >= 0.00001d;
	}
	public final double GetLat()
	{
		return _Lat;
	}
	public final double GetLng()
	{
		return _Lng;
	}
	public final boolean IsTranslate()
	{
		// TODO Переводимое сообщени
		return false;
	}
	public final int GetType()
	{
		if (__Count == 0)
		{
			return TYPE_TEXT;
		}
		else if (__Count == 1)
		{
			switch (__Attachments.get(0).GetType())
			{
				case Attachment.TYPE_IMAGE:
					return TYPE_PHOTO;
				case Attachment.TYPE_VIDEO:
					return TYPE_VIDEO;
				case Attachment.TYPE_AUDIO:
					return TYPE_AUDIO;
				case Attachment.TYPE_VOICE:
					return TYPE_VOICE;
				case Attachment.TYPE_FILE:
					return TYPE_FILE;
				case Attachment.TYPE_PLACE:
					return TYPE_PLACE;
				case Attachment.TYPE_CONTACT:
					return TYPE_CONTACT;
			}
		}
		return TYPE_FILES;
	}
	
	public final boolean IsSynchronized()
	{
		return _Sync;
	}
	public final void SetSynchronized(SQLiteDatabase pWriter)
	{
		_Sync = true;
		__hContainer.UpdateMessage(this, pWriter);
	}
	
	public final String GetProgress()
	{
		return Integer.toString(_Progress) + "%";
	}
	public final void SetProgress(int pProgress)
	{
		_Progress = pProgress;
		RefreshView();
	}
	
	
	public final int GetAttachmentsCount()
	{
		return __Count;
	}
	public final Attachment GetAttachment(int pIndex)
	{
		return __Attachments.get(pIndex);
	}
	public final ArrayList<Attachment> GetAttachments()
	{
		return __Attachments;
	}
	public final ArrayList<Attachment> GetAttachmentsCopy()
	{
		ArrayList<Attachment> attachments = new ArrayList<Attachment>();
		for (int i = 0; i < __Attachments.size(); i++)
		{
			attachments.add(__Attachments.get(i));
		}
		return attachments;
	}

	
	public final void Uploading(ActivityMain pActivity)
	{
		__Status = STATUS_UPLOADING;
		AttachmentsUploader uploader = new AttachmentsUploader(pActivity, this);
		uploader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	public final void NotUploaded(SQLiteDatabase pWriter)
	{
		__Status = STATUS_NOTUPLOADED;
		__hContainer.UpdateMessage(this, pWriter);
	}
	public final void NotSended()
	{
		__Status = STATUS_NOTSENDED;
		RefreshView();
	}
	public abstract void Sending(ActivityMain pActivity) throws JSONException;
	public final void Sended(SQLiteDatabase pWriter)
	{
		__Status = STATUS_SENDED_E;
		__hContainer.UpdateMessage(this, pWriter);
	}
	public final boolean SetStatus(int pStatus)
	{
		if (pStatus > __Status)
		{
			__Status = pStatus;
			return true;
		}
		return false;
	}	
	public final void Sended(Context pContext, JSONObject pResult, SQLiteDatabase pWriter) throws JSONException
	{
		String oldMID = _ID;
		
		_ID = pResult.getString("id");
		__Status = pResult.getInt("status");
		try 
		{  
			_DateTime = DateUtil.DATE_TIME.parse(pResult.getString("date"));  //2015-10-01T08:10:53
		} 
		catch (ParseException e) 
		{  
			_DateTime = new Date();
		}
		_Sync = true;
		
		// удаляем по старым ключам
		pWriter.delete(__hContainer.__GetTableMessageName(), "mid = ?", new String[]{oldMID});
		pWriter.delete("attachments", "mid = ?", new String[]{oldMID});
		
		pWriter.insert(__hContainer.__GetTableMessageName(), null, GetCV());
		for (int i = 0; i < __Attachments.size(); i++)
		{
			pWriter.insert("attachments", null, __Attachments.get(i).GetCV(_ID));
		}
		
//		if (oldMID.equals(__hContainer.GetLastMessageID()))
//		{
//			__hContainer.SetLastMessage(this, writer);
//		}
		
		//RefreshView();
		__hContainer.Move(_ID, pWriter);
	}
	
	
	public final void RemoveAttachments(ActivityMain pActivity)
	{
		for (int i = 0; i < __Attachments.size(); i++)
		{
			__Attachments.get(i).StopDownloading();
			try
			{
				File file = new File(__Attachments.get(i).GetFilePath(pActivity));
				file.delete();
			}
			catch (Exception e)
			{
			}
		}
	}
	
	
	public final boolean DownloadingAttachments(ActivityMain pActivity, boolean pActive)
	{
		boolean b = false;
		for (int i = 0; i < __Attachments.size(); i++)
		{
			int status = __Attachments.get(i).GetLoadStatus();
			if (status == Attachment.STATUS_INITIAL || status == Attachment.STATUS_ERROR || status == Attachment.STATUS_STOPTED)
			{
				b = true;
				__Attachments.get(i).Downloading(pActivity, pActive);
			}
		}
		return b;
	}
	
	
	public final void RefreshView()
	{
		__hContainer.RefreshView(_ID);
	}
	
	
	public ContentValues GetCV()
	{
		ContentValues cv = new ContentValues();
	    cv.put("mid", _ID);
	    cv.put("datetime", DateUtil.DATE_TIME.format(_DateTime));
	    cv.put("status", __Status);
	    cv.put("message", _Message);
	    cv.put("count", __Count);
	    cv.put("lat", _Lat);
	    cv.put("lng", _Lng);
	    cv.put("sync", _Sync ? 1 : 0);
	    return cv;
	}
	
	protected JSONObject __GetOptions() throws JSONException
	{
		JSONObject options = new JSONObject();
		options.put("message", _Message);
		JSONArray attachArray = new JSONArray();
		for (int i = 0; i < __Attachments.size(); i++)
		{
			attachArray.put(__Attachments.get(i).GetID());
		}
		options.put("attach", attachArray);
		if (IsGeo())
		{
			options.put("lat", _Lat);
			options.put("lng", _Lng);
		}
		return options;
	}
}