package com.messme.contacts;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.util.Util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.util.LongSparseArray;
import android.telephony.PhoneNumberUtils;

public class Contacts
{
	private final ActivityMain _hActivity;
	private final LongSparseArray<Contact> _Contacts;
	
	private boolean _Synchronized;
	
	
	public Contacts(ActivityMain pActivity)
	{
		_hActivity = pActivity;

		_Contacts = _GetContacts();
		_Synchronized = false;
	}
	
	
	public LongSparseArray<Contact> GetContacts()
	{
		return _Contacts;
	}
	
	
	public boolean Update()
	{
		boolean sync = false;
		boolean ref = false;
		LongSparseArray<Contact> contacts = _GetContacts();
		for (int i = 0; i < contacts.size(); i++)
		{
			long key = contacts.keyAt(i);
			Contact value = contacts.valueAt(i);
			Contact contact = _Contacts.get(key);
			if (contact == null)
			{
				_Contacts.append(key, value);
				sync = true;
				ref = true;
			}
			else
			{
				if (contact.Update(value))
				{
					ref = true;
				}
			}
		}
		
		ArrayList<Long> toRemove = new ArrayList<Long>();
		for (int i = 0; i < _Contacts.size(); i++)
		{
			long key = _Contacts.keyAt(i);
			if (contacts.get(key) == null)
			{
				toRemove.add(key);
			}
		}
		for (int i = 0; i < toRemove.size(); i++)
		{
			_Contacts.remove(toRemove.get(i));
			ref = true;
		}
		
		if (ref && _hActivity.GetManager().GetCurrentFragment() != null)
		{
			_hActivity.GetManager().GetCurrentFragment().ContactsChanged();
		}
		
		return sync;
	}
	
	
	public void Synchronizing()
	{
		_Synchronized = false;
		try
		{
			JSONObject options = new JSONObject();
			JSONArray users = new JSONArray();
			options.put("userlist", users);
			for (int i = 0; i < _Contacts.size(); i++)
			{
				users.put(_Contacts.valueAt(i).Phone);
			}
			_hActivity.GetServerConnection().Send(Util.GenerateMID(), "user.setfriend", options);
		}
		catch (JSONException e)
		{
		}
	}
	
	
	public void Synchronized()
	{
		_Synchronized = true;
	}
	public void NotSynchronized()
	{
		_Synchronized = false;
	}
	
	public boolean IsSynchronized()
	{
		return _Synchronized;
	}
	
	
	private LongSparseArray<Contact> _GetContacts() 
	{
		LongSparseArray<Contact> result = new LongSparseArray<Contact>();
		ContentResolver cr = _hActivity.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor.getCount() > 0) 
        {
        	int columnIndexId = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        	int columnIndexName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        	int columnIndexHasPhone = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
	        while (cursor.moveToNext()) 
	        {
				String id = cursor.getString(columnIndexId);
				String name = cursor.getString(columnIndexName);
			  
				if (Integer.parseInt(cursor.getString(columnIndexHasPhone)) > 0) 
				{
					Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI
						, null
						, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?"
						, new String[]{id}
						, null);
		        	int columnIndexPhone = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
					while (phoneCursor.moveToNext()) 
					{
						try 
						{
							String phoneStr = PhoneNumberUtils.stripSeparators(phoneCursor.getString(columnIndexPhone)).replaceAll("[^0-9]+", "");
							long phone = Long.parseLong(phoneStr);
							
							if (phoneStr.length() == 11 && phoneStr.substring(0, 1).equals("8"))
							{
								phoneStr = 7 + phoneStr.substring(1); 
								phone = Long.parseLong(phoneStr);
							}
							
							int i = 0;
							for (i = 0; i < result.size(); i++)
							{
								if (result.valueAt(i).Phone == phone)
								{
									break;
								}
							}
							if (i == result.size())
							{
								result.append(phone, new Contact(id, name, phone));
							}
						}
						catch(NumberFormatException e)
						{
						}
			    	}
					phoneCursor.close();
				}
	        }
        }
        cursor.close();
        return result;
	}


	public Contact GetContact(long pID)
	{
		return _Contacts.get(pID);
	}
	
	
	public static String GetContactName(Context pContext, String pPhoneNumber) 
	{
	    ContentResolver cr = pContext.getContentResolver();
	    Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(pPhoneNumber));
	    Cursor cursor = cr.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
	    if (cursor == null) 
	    {
	        return null;
	    }
	    String contactName = null;
	    if(cursor.moveToFirst()) 
	    {
	        contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
	    }

	    if(cursor != null && !cursor.isClosed()) 
	    {
	        cursor.close();
	    }

	    return contactName;
	}
}