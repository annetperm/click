package com.messme.chats.messages.chat;

import java.io.InputStream;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.messme.R;
import com.messme.chats.ChatHeader;
import com.messme.data.DB;
import com.messme.profile.Profile;
import com.messme.chats.messages.AbstractMessageUserContainer;
import com.messme.socket.iImageContainer;
import com.messme.util.ImageUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public final class Chat extends AbstractMessageUserContainer<ChatMessage> implements iImageContainer
{
	private String _Description;
	private String _AvatarUrl;
	private long _AdminID;
	private int _UnReaded;
	private boolean _UnReadFlag;
	private long _LastSenderID;
	private boolean _Member;
	
	private Bitmap _Avatar;
	private boolean _AvatarLoaded;
	private final long _MyID;
	
	public Chat(Context pContext, Cursor pCursor, long pMyID)
	{
		super(pContext, pCursor);
		_Description = pCursor.getString(8);
		_AvatarUrl = pCursor.getString(9);
		_AdminID = pCursor.getLong(10);
		_UnReaded = pCursor.getInt(11);
		_UnReadFlag = pCursor.getInt(12) == 1 ? true : false;
		_LastSenderID = pCursor.getLong(13);
		_Member = pCursor.getInt(14) == 1 ? true : false;
		_Avatar = null;
		_MyID = pMyID;
	}
	public Chat(Context pContext, String pID, long pMyID)
	{
		super(pContext, pID);
		_Description = "";
		_AvatarUrl = "";
		_AdminID = 0;
		_UnReaded = 0;
		_UnReadFlag = false;
		_LastSenderID = 0;
		_Member = true;
		_Avatar = null;
		_MyID = pMyID;
	}
	
	
//	public ChatMessage AddMessage(long pMyId, double pLat, double pLng, String pText, boolean pIsAttachmentUploaded, ArrayList<Attachment> pAttachments)
//	{
//		ChatMessage message = new ChatMessage(this, pMyId, pLat, pLng, pText, pIsAttachmentUploaded, pAttachments);
//		__AddMessage(message);	
//		return message;
//	}
//	public ChatMessage AddMessage(JSONObject pJSON) throws JSONException
//	{
//		if (__Messages == null)
//		{
//			__LoadFromDB();
//		}
//		ChatMessage message = new ChatMessage(this, pJSON);
//		__AddMessage(message);
//		return message;
//	}
//	public ArrayList<ChatMessage> AddMessages(JSONArray pJSONArray, boolean pFirst) throws JSONException
//	{
//		DB db = new DB(__hContext);
//		SQLiteDatabase writer = db.getWritableDatabase();
//		ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
//		ArrayList<ChatMessage> newMessages = new ArrayList<ChatMessage>();
//		for (int i = 0; i < pJSONArray.length(); i++)
//		{
//			JSONObject row = pJSONArray.getJSONObject(i);
//			ChatMessage message = GetMessage(row.getString("id"));
//			if (message == null)
//			{
//				message = new ChatMessage(this, row);
//				newMessages.add(message);
//			}
//			else
//			{
//				message.SetStatus(row.getInt("status"), writer, true);
//			}
//			messages.add(message);
//		}
//		
//		if (pFirst && newMessages.size() == messages.size())
//		{
//			// все новые
//			writer.delete(__GetTableMessageName(), "id = ?", new String[]{__ID});
//			__Messages.clear();
//		}
//		
//		int index = -2;
//		ChatMessage lastMessage = null;
//		for (int i = 0; i < newMessages.size(); i++)
//		{
//			ChatMessage message = newMessages.get(i);
//			int curIndex = __Messages.add(message);
//			if (curIndex > index)
//			{
//				index = curIndex;
//				lastMessage = message;
//			}
//			
//			writer.insert(__GetTableMessageName(), null, message.GetCV());
//			for (int j = 0; j < message.GetAttachmentsCount(); j++)
//			{
//				writer.insert("attachments", null, message.GetAttachment(j).GetCV(message.GetID()));
//			}
//		}
//		if (index == __Messages.size() - 1)
//		{
//			__UpdateHeaderOnAddMessage(lastMessage, writer);
//		}
//		
//		writer.close();
//		db.close();
//		
//		return messages;
//	}
	
	
	public long GetAdminID()
	{
		return _AdminID;
	}
	public String GetDescription()
	{
		return _Description;
	}
	public String GetAvatarUrl()
	{
		return _AvatarUrl;
	}
	public long GetLastSenderID()
	{
		return _LastSenderID;
	}
	public boolean IsMember()
	{
		return _Member;
	}
	
	public void DeleteAvatar()
	{
		_AvatarUrl = "";
		_Avatar = null;
		_AvatarLoaded = false;
		DB db = new DB(__hContext);
		SQLiteDatabase writer = db.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("avatar", "");
		writer.update(__GetTableHeaderName(), cv, "id = ?", new String[]{__ID});
		writer.close();
		db.close();
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
	
	@Override
	public ContentValues GetCV(boolean pNew)
	{
		ContentValues cv = super.GetCV(pNew);
	    cv.put("description", _Description);
	    cv.put("avatar", _AvatarUrl);
	    cv.put("adminid", _AdminID);
	    cv.put("unreaded", _UnReaded);
		cv.put("unreadflag", _UnReadFlag ? 1 : 0);
	    cv.put("senderid", _LastSenderID);
		cv.put("member", _Member ? 1 : 0);
	    return cv;
	}
	
	@Override
	public boolean UpdateHeader(ChatHeader pChatHeader, SQLiteDatabase pWriter)
	{
		if (pChatHeader.MsgUnread == 0 && __LastMessageID.length() > 0)
		{
			return false;
		}
		else
		{
			__Name = pChatHeader.FromName;
			_UnReaded = pChatHeader.MsgUnread;
			if (!pChatHeader.FromAvatar.equals(_AvatarUrl))
			{
				_AvatarUrl = pChatHeader.FromAvatar;
				_Avatar = null;
			}
			__LastMessageID = "";
			__LastMessageText = pChatHeader.Message;
			__LastMessageDateTime = pChatHeader.GetDate();
			__LastMessageType = 1;
			__Archive = MAIN;
			
			pWriter.update(__GetTableHeaderName(), GetCV(false), "id = ?", new String[]{GetID()});
			return true;
		}
	}
	@Override
	public void Update(JSONObject pResult, long pMyID) throws JSONException
	{
		_Description = pResult.getString("description");
		_AdminID = pResult.getLong("adminid");
		_UnReaded = 0;
		String avatar = pResult.getString("avatar");
		if (!_AvatarUrl.equals(avatar))
		{
			_AvatarUrl = avatar;
			_Avatar = null;
		}
		JSONArray userlist = pResult.getJSONArray("userlist");
		_Member = false;
		for (int i = 0; i < userlist.length(); i++)
		{
			if (userlist.getLong(i) == _MyID)
			{
				_Member = true;
			}
		}
		super.Update(pResult, pMyID);
	}
	
//	@Override
//	protected void __UpdateHeaderOnRemoveMessage(ChatMessage pMessage, SQLiteDatabase pWriter)
//	{
////		_UnReaded - не меняется т.к. удаляются либо свом ссобщения либо по снапчату, которые уже прочитаны
//		if (pMessage != null)
//		{
//			if (pMessage.GetSenderID() != _MyID)
//			{
//				_LastSenderID = pMessage.GetSenderID();
//			}
//			else
//			{
//				_LastSenderID = _MyID;
//			}
//		}
//		else
//		{
//			_LastSenderID = 0;
//		}
//		
//		super.__UpdateHeaderOnRemoveMessage(pMessage, pWriter);
//	}
//	@Override
//	protected void __UpdateHeaderOnAddMessage(ChatMessage pMessage, SQLiteDatabase pWriter)
//	{
//		if (pMessage.GetSenderID() == _MyID)
//		{
//			_LastSenderID = _MyID;
//		}
//		else
//		{
//			_UnReaded++;
//			_LastSenderID = pMessage.GetSenderID();
//		}
//
//		super.__UpdateHeaderOnAddMessage(pMessage, pWriter);
//	}
	
	@Override
	protected void __OnLoadFromDB(Cursor pCursor)
	{
		if (pCursor.moveToFirst()) 
		{
	        do 
	        {
	        	ChatMessage message = new ChatMessage(this, pCursor);
	        	__Messages.add(message);
	        } while (pCursor.moveToNext());
		}
	}
	
	@Override
	public String __GetTableMessageName()
	{
		return "chats";
	}
	@Override
	protected String __GetTableHeaderName()
	{
		return "chatheaders";
	}
	
	@Override
	public void LoadImage(Context pContext, Profile pProfile)
	{
		if (_AvatarUrl.length() == 0)
		{
			_AvatarLoaded = false;
			_Avatar = BitmapFactory.decodeResource(pContext.getResources(), R.drawable.ic_chat_chat);
		}
		else
		{
			_Avatar = ImageUtil.LoadImageFromDB(__hContext, _AvatarUrl);
			if (_Avatar == null)
			{
				try 
				{			
					_Avatar = BitmapFactory.decodeStream((InputStream)new URL("https://files.messme.me:8102/avatar/" + _AvatarUrl).getContent());
					_AvatarLoaded = true;
					ImageUtil.SaveImageToDB(__hContext, _Avatar, _AvatarUrl);
				} 
				catch(Throwable e) 
				{
					_AvatarLoaded = false;
					_Avatar = BitmapFactory.decodeResource(pContext.getResources(), R.drawable.ic_chat_chat);
				}
			}
			else
			{
				_AvatarLoaded = true;
			}
		}
	}
	@Override
	public Bitmap GetImage()
	{
		return _Avatar;
	}
	@Override
	public boolean HasLoadedImage()
	{
		return _AvatarLoaded;
	}
	
	public static JSONObject GetInfo(Chat pChat) throws JSONException
	{
		JSONObject options = new JSONObject();
		options.put("id", pChat.GetID());
		return options;
	}
}