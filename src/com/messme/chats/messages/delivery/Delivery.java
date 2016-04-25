package com.messme.chats.messages.delivery;

import com.messme.chats.ChatHeader;
import com.messme.chats.messages.AbstractMessage;
import com.messme.chats.messages.AbstractMessageUserContainer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class Delivery extends AbstractMessageUserContainer<DeliveryMessage>
{	
	public Delivery(Context pContext, Cursor pCursor)
	{
		super(pContext, pCursor);
	}
	public Delivery(Context pContext, String pID)
	{
		super(pContext, pID);
	}

	@Override
	public boolean UpdateHeader(ChatHeader pChatHeader, SQLiteDatabase pWriter)
	{
		if (GetLastMessageID().length() > 0)
		{
			return false;
		}
		else
		{
			__Name = pChatHeader.FromName;
			__LastMessageID = pChatHeader.Entry;
			__LastMessageText = pChatHeader.Message;
			__LastMessageDateTime = pChatHeader.GetDate();
			__LastMessageType = 1;
			__Archive = MAIN;
			
			pWriter.update(__GetTableHeaderName(), GetCV(false), "id = ?", new String[]{GetID()});
			return true;
		}
	}
	
	@Override
	protected void __OnLoadFromDB(Cursor pCursor)
	{
		if (pCursor.moveToFirst()) 
		{
	        do 
	        {
	        	DeliveryMessage message = new DeliveryMessage(this, pCursor);
	        	if (message.IsSnap() && message.GetStatus() == AbstractMessage.STATUS_READED)
	        	{
	        	}
	        	else
	        	{
		        	__Messages.add(message);
	        	}
	        } while (pCursor.moveToNext());
		}
	}
	
	@Override
	public String __GetTableHeaderName()
	{
		return "deliveryheaders";
	}
	@Override
	public String __GetTableMessageName()
	{
		return "deliveries";
	}
}