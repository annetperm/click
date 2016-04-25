package com.messme.chats.messages.dialog;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.chats.messages.AbstractSnapMessage;
import com.messme.chats.messages.Attachment;
import com.messme.chats.messages.delivery.DeliveryMessage;

import android.content.ContentValues;
import android.database.Cursor;


public final class DialogMessage extends AbstractSnapMessage
{			
	private final boolean _IsMy;
	private final long _DialogID; // ид пользователя с которым идет диалог
	private final String _EntryID;	
	
	
	public DialogMessage(Dialog pDialog, Cursor pCursor)
	{
		super(pDialog, pCursor);
		_IsMy = pCursor.getInt(10) == 1 ? true : false;
		_DialogID = pCursor.getLong(11);
		_EntryID = pCursor.getString(12);
	}
	public DialogMessage(Dialog pDialog, JSONObject pJSON) throws JSONException	
	{
		super(pDialog, pJSON);
		_IsMy = pJSON.getInt("owner") == 0 ? false : true;
		_DialogID = pDialog.GetUserID();
		_EntryID = pJSON.getString("entryid");
	}
	public DialogMessage(Dialog pDialog, int pTimer, double pLat, double pLng, String pMessage, boolean pIsAttachmentUploaded, ArrayList<Attachment> pAttachments)
	{
		super(pDialog, pTimer, pLat, pLng, pMessage, pIsAttachmentUploaded, pAttachments);
		_IsMy = true;
		_DialogID = pDialog.GetUserID();
		_EntryID = "00000000-0000-0000-0000-000000000000";
	}
	public DialogMessage(Dialog pDialog, DeliveryMessage pDeliveryMessage, String pEntryID)
	{
		super(pDialog, pDeliveryMessage.GetSnapTimer(), pDeliveryMessage.GetLat(), pDeliveryMessage.GetLng(), pDeliveryMessage.GetMessage(), true, pDeliveryMessage.GetAttachmentsCopy());
		_IsMy = true;
		_DialogID = pDialog.GetUserID();
		_EntryID = pEntryID;
		__Status = STATUS_SENDED;
	}
	
	
	public long GetDialogID()
	{
		return _DialogID;
	}
	public boolean IsMy()
	{
		return _IsMy;
	}
	public boolean IsDeliveryMessage()
	{
		if (_EntryID.length() == 0)
		{
			return false;
		}
		return !_EntryID.equals("00000000-0000-0000-0000-000000000000");
	}

	
	@Override
	public ContentValues GetCV()
	{
		ContentValues cv = super.GetCV();
		cv.put("id", _DialogID);
	    cv.put("type", _IsMy ? 1 : 0);
	    cv.put("entryid", _EntryID);
	    return cv;
	}
	
	@Override
	public void Sending(ActivityMain pActivity) throws JSONException
	{
		pActivity.GetServerConnection().Send(GetID(), "message.send", __GetOptions());
	}
	
	@Override
	protected JSONObject __GetOptions() throws JSONException
	{
		JSONObject options = super.__GetOptions();
		JSONArray userlist = new JSONArray();
		userlist.put(_DialogID);
		options.put("userlist", userlist);
		options.put("dialogtype", 0);
		return options;
	}
}