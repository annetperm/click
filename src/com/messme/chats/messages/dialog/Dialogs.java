package com.messme.chats.messages.dialog;

import java.util.ArrayList;

import com.messme.ActivityMain;
import com.messme.data.DB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.LongSparseArray;


public class Dialogs
{
	private final Context _hContext;
	private final LongSparseArray<Dialog> _Dialogs = new LongSparseArray<Dialog>();
	
	
	public Dialogs(Context pContext)
	{
		_hContext = pContext;
		
		DB db = new DB(_hContext);
		SQLiteDatabase reader = db.getReadableDatabase();
		Cursor cursor = reader.query("dialogheaders", null, null, null, null, null, null);
		if (cursor.moveToFirst()) 
		{
	        do 
	        {
	        	Dialog dialog = new Dialog(_hContext, cursor);
	        	_Dialogs.append(dialog.GetUserID(), dialog);
	        } while (cursor.moveToNext());
		}
		reader.close();
		db.close();
	}
	public Dialogs(Context pContext, SQLiteDatabase pReader)
	{
		_hContext = pContext;
		
		Cursor cursor = pReader.query("dialogheaders", null, null, null, null, null, null);
		if (cursor.moveToFirst()) 
		{
	        do 
	        {
	        	Dialog dialog = new Dialog(_hContext, cursor);
	        	_Dialogs.append(dialog.GetUserID(), dialog);
	        } while (cursor.moveToNext());
		}
	}
	
	
	public LongSparseArray<Dialog> GetDialogs()
	{
		ArrayList<Long> ids = new ArrayList<Long>();
		for (int i = 0; i < _Dialogs.size(); i++)
		{
			if (_Dialogs.valueAt(i).GetLastMessageID().length() == 0)
			{
				ids.add(_Dialogs.keyAt(i));
			}
		}
		for (int i = 0; i < ids.size(); i++)
		{
			Remove(ids.get(i));
		}
		return _Dialogs;
	}
	public Dialog GetDialog(long pUserID)
	{
		return GetDialog(pUserID, true);
	}
	public Dialog GetDialog(long pUserID, boolean pCreateIfNull)
	{
		Dialog dialog = _Dialogs.get(pUserID);
		if (dialog == null && pCreateIfNull)
		{
			dialog = new Dialog(_hContext, pUserID);
			DB db = new DB(_hContext);
			SQLiteDatabase writer = db.getWritableDatabase();
			writer.insert("dialogheaders", null, dialog.GetCV(true));
			writer.close();
			db.close();
			_Dialogs.append(dialog.GetUserID(), dialog);
		}
		return dialog;
	}

	public void StartUploading(ActivityMain pActivity)
	{
		for (int i = 0; i < _Dialogs.size(); i++)
		{
			_Dialogs.valueAt(i).StartUploading(pActivity);
		}
	}

	public void Clear(SQLiteDatabase pWriter)
	{
		pWriter.delete("dialogheaders", null, null);
		pWriter.delete("dialogs", null, null);
		_Dialogs.clear();
	}

	public Dialog Remove(long pUserID)
	{
		Dialog dialog = _Dialogs.get(pUserID);
		if (dialog != null)
		{
			_Dialogs.remove(pUserID);
			DB db = new DB(_hContext);
			SQLiteDatabase writer = db.getWritableDatabase();
			writer.delete("dialogheaders", "id = ?", new String[]{Long.toString(pUserID)});
			writer.delete("dialogs", "id = ?", new String[]{Long.toString(pUserID)});
			writer.close();
			db.close();
		}
		return dialog;
	}
}