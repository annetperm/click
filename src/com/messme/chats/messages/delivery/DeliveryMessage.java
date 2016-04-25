package com.messme.chats.messages.delivery;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.chats.messages.AbstractSnapMessage;
import com.messme.chats.messages.Attachment;

import android.content.ContentValues;
import android.database.Cursor;


public class DeliveryMessage extends AbstractSnapMessage
{
	private final String _DeliveryID;
	
	
	public DeliveryMessage(Delivery pDelivery, Cursor pCursor)
	{
		super(pDelivery, pCursor);
		_DeliveryID = pCursor.getString(10);
	}
	public DeliveryMessage(Delivery pDelivery, JSONObject pJSON) throws JSONException
	{
		super(pDelivery, pJSON);
		_DeliveryID = pJSON.getString("entryid");
	}
	public DeliveryMessage(Delivery pDelivery, int pTimer, double pLat, double pLng, String pMessage, boolean pIsAttachmentUploaded, ArrayList<Attachment> pAttachments)
	{
		super(pDelivery, pTimer, pLat, pLng, pMessage, pIsAttachmentUploaded, pAttachments);
		_DeliveryID = pDelivery.GetID();
	}
	
	
	@Override
	public ContentValues GetCV()
	{
		ContentValues cv = super.GetCV();
	    cv.put("id", _DeliveryID);
	    return cv;
	}
	
	@Override
	public void Sending(ActivityMain pActivity) throws JSONException
	{
		pActivity.GetServerConnection().Send(GetID(), "mailing.send", __GetOptions());
	}
	
	@Override
	protected JSONObject __GetOptions() throws JSONException
	{
		JSONObject options = super.__GetOptions();
		options.put("id", _DeliveryID);
		options.put("dialogtype", 2);
		return options;
	}
}