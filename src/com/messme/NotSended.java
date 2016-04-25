package com.messme;

import java.util.ArrayList;

import com.messme.data.DB;
import com.messme.socket.MessageItem;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NotSended
{
	private ActivityMain _hContext;
	private ArrayList<MessageItem> _Messages = new ArrayList<MessageItem>();
	
	public NotSended(ActivityMain pContext)
	{
		_hContext = pContext;
		
		DB db = new DB(_hContext);
		SQLiteDatabase reader = db.getReadableDatabase();
		Cursor cursor = reader.query("notsended", null, null, null, null, null, null);
		if (cursor.moveToFirst()) 
		{
	        do 
	        {
	        	_Messages.add(new MessageItem(cursor.getString(0), cursor.getString(1), cursor.getString(2)));
	        } while (cursor.moveToNext());
		}
		reader.close();
		db.close();
	}
	
	public void Add(MessageItem pItem)
	{
		pItem.ToResend();
    	_Messages.add(pItem);
		DB db = new DB(_hContext);
		SQLiteDatabase writer = db.getWritableDatabase();
		writer.insert("notsended", null, pItem.GetCV());
		writer.close();
		db.close();
	}
	
	public MessageItem GetFirst()
	{
		if (_Messages.size() == 0)
		{
			return null;
		}
		return _Messages.get(0);
	}

	public void Clear(SQLiteDatabase pWriter)
	{
		pWriter.delete("notsended", null, null);
		_Messages.clear();
	}

	public void SetSended(String pMID)
	{
		for (int i = 0; i < _Messages.size(); i++)
		{
			if (_Messages.get(i).GetMID().equals(pMID))
			{
				DB db = new DB(_hContext);
				SQLiteDatabase writer = db.getWritableDatabase();
				writer.delete("notsended", "mid = ?", new String[]{_Messages.get(0).GetMID()});
				writer.close();
				db.close();
				_Messages.remove(i);
				break;
			}
		}
	}

	public void Remove(String pMID)
	{
		for (int i = 0; i < _Messages.size(); i++)
		{
			if (_Messages.get(i).GetMID().equals(pMID))
			{
				DB db = new DB(_hContext);
				SQLiteDatabase writer = db.getWritableDatabase();
				writer.delete("notsended", "mid = ?", new String[]{_Messages.get(0).GetMID()});
				writer.close();
				db.close();
				_Messages.remove(i);
				break;
			}
		}
	}
}