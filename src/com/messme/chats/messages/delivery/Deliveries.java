package com.messme.chats.messages.delivery;

import java.util.HashMap;
import java.util.Map;

import com.messme.ActivityMain;
import com.messme.data.DB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class Deliveries 
{
	private final Context _hContext;
	private final HashMap<String, Delivery> _Deliveries = new HashMap<String, Delivery>();
	
	
	public Deliveries(Context pContext)
	{
		_hContext = pContext;
		
		DB db = new DB(_hContext);
		SQLiteDatabase reader = db.getReadableDatabase();
		Cursor cursor = reader.query("deliveryheaders", null, null, null, null, null, null);
		if (cursor.moveToFirst()) 
		{
	        do 
	        {
	        	Delivery delivery = new Delivery(_hContext, cursor);
	        	_Deliveries.put(delivery.GetID(), delivery);
	        } while (cursor.moveToNext());
		}
		cursor.close();
		for (Delivery delivery: _Deliveries.values())
		{
			cursor = reader.query("pairs", new String[]{"userid"}, "id = ?", new String[]{delivery.GetID()}, null, null, null);
			if (cursor.moveToFirst()) 
			{
				do
				{
					long userID = cursor.getLong(0);
					delivery.AddUserFromDB(userID);
				} while(cursor.moveToNext());
			}
			cursor.close();
		}
		reader.close();
		db.close();
	}
	
	
	public HashMap<String, Delivery> GetDeliveries()
	{
		return _Deliveries;
	}
	public Delivery GetDelivery(String pDeliveryId)
	{
		Delivery delivery = _Deliveries.get(pDeliveryId);
		if (delivery == null)
		{
			delivery = new Delivery(_hContext, pDeliveryId);
			DB db = new DB(_hContext);
			SQLiteDatabase writer = db.getWritableDatabase();
			writer.insert("deliveryheaders", null, delivery.GetCV(true));
			writer.close();
			db.close();
			_Deliveries.put(pDeliveryId, delivery);
		}
		return delivery;
	}

	public void StartUploading(ActivityMain pActivity)
	{
		for (Map.Entry<String, Delivery> delivery : _Deliveries.entrySet())
		{
			delivery.getValue().StartUploading(pActivity);
		}
	}

	public void Clear(SQLiteDatabase pWriter)
	{
		pWriter.delete("deliveryheaders", null, null);
		pWriter.delete("deliveries", null, null);
		_Deliveries.clear();
	}


	public Delivery Remove(String pID)
	{
		Delivery delivery = _Deliveries.remove(pID);
		DB db = new DB(_hContext);
		SQLiteDatabase writer = db.getWritableDatabase();
		writer.delete("deliveryheaders", "id = ?", new String[]{pID});
		writer.delete("deliveries", "id = ?", new String[]{pID});
		writer.close();
		db.close();
		return delivery;
	}
}