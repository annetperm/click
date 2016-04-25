package com.messme.chats.messages.chat;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.chats.messages.AbstractMessage;
import com.messme.chats.messages.Attachment;

import android.content.ContentValues;
import android.database.Cursor;


public class ChatMessage extends AbstractMessage
{
	private final String _ChatID;
	private final long _SenderID;

	
	public ChatMessage(Chat pChat, Cursor pCursor)
	{
		super(pChat, pCursor);
		_ChatID = pCursor.getString(8);
		_SenderID = pCursor.getLong(9);
	}
	public ChatMessage(Chat pChat, JSONObject pJSON) throws JSONException
	{
		super(pChat, pJSON);
	    _ChatID = pJSON.getString("entryid");
		_SenderID = pJSON.getLong("userid");
	}
	public ChatMessage(Chat pChat, long pMyID, double pLat, double pLng, String pMessage, boolean pIsAttachmentUploaded, ArrayList<Attachment> pAttachments)
	{
		super(pChat, pLat, pLng, pMessage, pIsAttachmentUploaded, pAttachments);
		_ChatID = pChat.GetID();
		_SenderID = pMyID;
	}
	
	
	public long GetSenderID()
	{
		return _SenderID;
	}
	
	
	@Override
	public ContentValues GetCV()
	{
		ContentValues cv = super.GetCV();
		cv.put("id", _ChatID);
	    cv.put("senderid", _SenderID);
	    return cv;
	}
	
	@Override
	public void Sending(ActivityMain pActivity) throws JSONException
	{
		pActivity.GetServerConnection().Send(GetID(), "groupchat.send", __GetOptions());
	}
	
	@Override
	protected JSONObject __GetOptions() throws JSONException
	{
		JSONObject options = super.__GetOptions();
		Chat chat = (Chat) __hContainer;
		options.put("id", _ChatID);
		options.put("name", chat.GetName());
		JSONArray userlist = new JSONArray();
		ArrayList<Long> users = chat.GetUsers();
		for (int i = 0; i < users.size(); i++)
		{
			userlist.put(users.get(i));
		}
		options.put("userlist", userlist);
		options.put("type", 0);
		options.put("dialogtype", 1);
		return options;
	}
}