package com.messme.chats.messages.dialog;

import com.messme.chats.ChatHeader;
import com.messme.chats.messages.AbstractMessage;
import com.messme.chats.messages.AbstractMessageContainer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public final class Dialog extends AbstractMessageContainer<DialogMessage>
{		
	private final long _UserID;
	private int _UnReaded;
	private boolean _UnReadFlag;
	
	
	public Dialog(Context pContext, Cursor pCursor)
	{
		super(pContext, pCursor);
		_UserID = pCursor.getLong(5);
		_UnReaded = pCursor.getInt(6);
		_UnReadFlag = pCursor.getInt(7) == 1 ? true : false;
	}
	public Dialog(Context pContext, long pUserID)
	{
		super(pContext);
		_UserID = pUserID;
		_UnReaded = 0;
		_UnReadFlag = false;
	}
	
	
	public long GetUserID()
	{
		return _UserID;
	}
	
	
	public int GetUnReadedCount()
	{
		return _UnReaded;
	}
	public String GetUnReadedText()
	{
		return Integer.toString(_UnReaded);
	}
	public void IncrementUnread(SQLiteDatabase pWriter)
	{
		_UnReaded++;
		UpdateContainer(pWriter);
	}
	public void DicrementUnread(SQLiteDatabase pWriter)
	{
		_UnReaded--;
		UpdateContainer(pWriter);
	}
	
	public boolean IsUnReadFlag()
	{
		return _UnReadFlag;
	}
	public void SetUnreadFlag(boolean pFlag, SQLiteDatabase pWriter)
	{
		_UnReadFlag = pFlag;
		UpdateContainer(pWriter);
	}
	
	
	@Override
	public ContentValues GetCV(boolean pNew)
	{
		ContentValues cv = super.GetCV(pNew);
		if (pNew)
		{
			cv.put("id", _UserID);
		}
		cv.put("unreaded", _UnReaded);
		cv.put("unreadflag", _UnReadFlag ? 1 : 0);
		return cv;
	}
	
	@Override
	public final String GetID()
	{
		return Long.toString(_UserID);
	}
	
	@Override
	protected final void __OnLoadFromDB(Cursor pCursor)
	{
		if (pCursor.moveToFirst()) 
		{
	        do 
	        {
	        	DialogMessage message = new DialogMessage(this, pCursor);
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
	protected final String __GetTableHeaderName()
	{
		return "dialogheaders";
	}
	@Override
	protected final String __GetTableMessageName()
	{
		return "dialogs";
	}
	@Override
	public boolean UpdateHeader(ChatHeader pChatHeader, SQLiteDatabase pWriter)
	{
		if (pChatHeader.Entry.equals(GetLastMessageID()))
		{
			return false;
		}
		else if (pChatHeader.MsgUnread == 0 && __LastMessageID.length() > 0)
		{
			return false;
		}
		else
		{
			_UnReaded = pChatHeader.MsgUnread;
			__LastMessageID = pChatHeader.Entry;
			__LastMessageText = pChatHeader.Message;
			__LastMessageDateTime = pChatHeader.GetDate();
			__LastMessageType = 1;
			__Archive = MAIN;
			
			pWriter.update("dialogheaders", GetCV(false), "id = ?", new String[]{GetID()});
			return true;
		}
	}
	
	
//	@Override
//	protected void __UpdateHeaderOnRemoveMessage(DialogMessage pMessage, SQLiteDatabase pWriter)
//	{
////		_UnReaded - не меняется т.к. удаляются либо свом ссобщения либо по снапчату, которые уже прочитаны
//		
//		super.__UpdateHeaderOnRemoveMessage(pMessage, pWriter);
//	}
//	@Override
//	protected void __UpdateHeaderOnAddMessage(DialogMessage pMessage, SQLiteDatabase pWriter)
//	{
//		if (!pMessage.IsMy())
//		{
//			_UnReaded++;
//		}
//
//		super.__UpdateHeaderOnAddMessage(pMessage, pWriter);
//	}
}