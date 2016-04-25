package com.messme.chats.messages.chat;

import java.util.HashMap;
import java.util.Map;

import com.messme.ActivityMain;
import com.messme.data.DB;
import com.messme.profile.Profile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class Chats
{
	private final Context _hContext;
	private final Profile _hProfile;
	private final HashMap<String, Chat> _Chats = new HashMap<String, Chat>();
	
	
	public Chats(Context pContext, Profile pProfile)
	{
		_hContext = pContext;
		_hProfile = pProfile;
		
		DB db = new DB(_hContext);
		SQLiteDatabase reader = db.getReadableDatabase();
		Cursor cursor = reader.query("chatheaders", null, null, null, null, null, null);
		if (cursor.moveToFirst()) 
		{
	        do 
	        {
	        	Chat chat = new Chat(_hContext, cursor, _hProfile.GetID());
	        	_Chats.put(chat.GetID(), chat);
	        } while (cursor.moveToNext());
		}
		cursor.close();
		for (Chat chat: _Chats.values())
		{
			cursor = reader.query("pairs", new String[]{"userid"}, "id = ?", new String[]{chat.GetID()}, null, null, null);
			if (cursor.moveToFirst()) 
			{
				do
				{
					long userID = cursor.getLong(0);
					chat.AddUserFromDB(userID);
				} while(cursor.moveToNext());
			}
			cursor.close();
		}
		reader.close();
		db.close();
	}
	public Chats(Context pContext, Profile pProfile, SQLiteDatabase pReader)
	{
		_hContext = pContext;
		_hProfile = pProfile;
		
		Cursor cursor = pReader.query("chatheaders", null, null, null, null, null, null);
		if (cursor.moveToFirst()) 
		{
	        do 
	        {
	        	Chat chat = new Chat(_hContext, cursor, _hProfile.GetID());
	        	_Chats.put(chat.GetID(), chat);
	        } while (cursor.moveToNext());
		}
		cursor.close();
		for (Chat chat: _Chats.values())
		{
			cursor = pReader.query("pairs", new String[]{"userid"}, "id = ?", new String[]{chat.GetID()}, null, null, null);
			if (cursor.moveToFirst()) 
			{
				do
				{
					long userID = cursor.getLong(0);
					chat.AddUserFromDB(userID);
				} while(cursor.moveToNext());
			}
			cursor.close();
		}
	}
	
	
	public HashMap<String, Chat> GetChats()
	{
		return _Chats;
	}
	public Chat GetChat(String pChatId)
	{
		Chat chat = _Chats.get(pChatId);
		if (chat == null)
		{
			chat = new Chat(_hContext, pChatId, _hProfile.GetID());
			DB db = new DB(_hContext);
			SQLiteDatabase writer = db.getWritableDatabase();
			writer.insert("chatheaders", null, chat.GetCV(true));
			writer.close();
			db.close();
			_Chats.put(pChatId, chat);
		}
		
		return chat;
	}

	public void StartUploading(ActivityMain pActivity)
	{
		for (Map.Entry<String, Chat> chat : _Chats.entrySet())
		{
			chat.getValue().StartUploading(pActivity);
		}
	}
	
	public void Clear(SQLiteDatabase pWriter)
	{
		pWriter.delete("chats", null, null);
		pWriter.delete("chatheaders", null, null);
		_Chats.clear();
	}


	public Chat Remove(String pID)
	{
		Chat chat = _Chats.remove(pID);
		DB db = new DB(_hContext);
		SQLiteDatabase writer = db.getWritableDatabase();
		writer.delete("chatheaders", "id = ?", new String[]{pID});
		writer.delete("chats", "id = ?", new String[]{pID});
		writer.close();
		db.close();
		return chat;
	}
}