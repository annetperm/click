package com.messme.user;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.data.DB;
import com.messme.util.Util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.LongSparseArray;


public class Users
{
	private final Context _hContext; 
	private final LongSparseArray<User> _Users = new LongSparseArray<User>();
	
	private String _MessageId = "";
	
	private Date _LastSynchronizingTime = null;
	
	public Users(Context pContext)
	{
		_hContext = pContext;
		
		// грузим список друзей
		DB db = new DB(_hContext);
		SQLiteDatabase reader = db.getReadableDatabase();
		Cursor cursor = reader.query("users", null, "isfriend = 1", null, null, null, null);
		if (cursor.moveToFirst()) 
		{
			do 
	        {
	        	User user = new User(cursor);
	        	_Users.append(user.Id, user);
	        } while (cursor.moveToNext());
		}
		cursor.close();
		reader.close();
		db.close();
	}
	public Users(Context pContext, SQLiteDatabase pReader)
	{
		_hContext = pContext;
		
		// грузим список друзей
		Cursor cursor = pReader.query("users", null, "isfriend = 1", null, null, null, null);
		if (cursor.moveToFirst()) 
		{
			do 
	        {
	        	User user = new User(cursor);
	        	_Users.append(user.Id, user);
	        } while (cursor.moveToNext());
		}
		cursor.close();
	}

	
	public void SynchronizingFriends(ActivityMain pActivity)
	{
		try
		{
			_LastSynchronizingTime = new Date();
			JSONObject options = new JSONObject();
			options.put("userlist", new JSONArray());
			options.put("locale", pActivity.GetProfile().GetLocale());
			options.put("status", -1);
			options.put("isfriend", 1);
			options.put("country", 0);
			options.put("city", 0);
			options.put("sex", "");
			options.put("minage", 0);
			options.put("maxage", 0);
			options.put("mask", "");
			options.put("lat1", 0);
			options.put("lng1", 0);
			options.put("lat2", 0);
			options.put("lng2", 0);
	        _MessageId = Util.GenerateMID();
	        pActivity.GetServerConnection().Send(_MessageId, "user.list", options);
		}
		catch (JSONException e)
		{
		}
	}
	public boolean UpdateFriends(String pResult, ActivityMain pActivity)
	{
		LongSparseArray<User> changed = new LongSparseArray<User>();
		DB db = new DB(_hContext);
		SQLiteDatabase writer = db.getWritableDatabase();
		try
		{
			LongSparseArray<User> friends = new LongSparseArray<User>();
			JSONArray friendsJSON = new JSONArray(pResult);
			
			for (int i = 0; i < friendsJSON.length(); i++)
			{
				User friend = new User(friendsJSON.getJSONObject(i));
				friends.append(friend.Id, friend);
				User user = GetUser(friend.Id);
				if (user == null)
				{
					_Users.append(friend.Id, friend);
					writer.delete("users", "id = ?", new String[]{Long.toString(friend.Id)});
					writer.insert("users", null, friend.GetCV(true));
					changed.append(friend.Id, friend);
				}
				else if (user.Update(friend))
				{
					writer.update("users", user.GetCV(false), "id = ?", new String[]{Long.toString(user.Id)});
					changed.append(user.Id, user);
				}
			}
			
			for (int i = 0; i < _Users.size(); i++)
			{
				User user = _Users.valueAt(i);
				if (user.IsFriend && friends.get(user.Id) == null)
				{
					// значит это уже не друг
					user.IsFriend = false;
					writer.update("users", user.GetCV(false), "id = ?", new String[]{Long.toString(user.Id)});
					changed.append(user.Id, user);
				}
			}
		}
		catch (JSONException e)
		{
		}
		writer.close();
		db.close();
		
		if (changed.size() > 0 && pActivity.GetManager().GetCurrentFragment() != null)
		{
			pActivity.GetManager().GetCurrentFragment().UsersChanged(changed);
		}
		
		return changed.size() > 0;
	}
	public String GetMessageId()
	{
		return _MessageId;
	}
	
	
	public boolean IsSynchronizeTime()
	{
		if (_LastSynchronizingTime == null)
		{
			return true;
		}
		Date now = new Date();
		if (now.getTime() - _LastSynchronizingTime.getTime() > 1000 * 60 * 5)
		{
			return true;
		}
		return false;
	}


	public User AddUser(JSONObject pJSONUser, ActivityMain pActivity) throws JSONException
	{
		return AddUser(new User(pJSONUser), pActivity);
	}
	public ArrayList<User> AddUsers(JSONArray pJSON, ActivityMain pActivity) throws JSONException
	{
		ArrayList<User> users = new ArrayList<User>();
		for (int i = 0; i < pJSON.length(); i++)
		{
			users.add(AddUser(pJSON.getJSONObject(i), pActivity));
		}
		return users;
	}
	public User AddUser(User pUser, ActivityMain pActivity) 
	{
		User user = GetUser(pUser.Id);
		DB db = new DB(_hContext);
		SQLiteDatabase writer = db.getWritableDatabase();
		boolean b = false;
		if (user == null)
		{
			user = pUser;
			_Users.append(user.Id, user);
			writer.delete("users", "id = ?", new String[]{Long.toString(user.Id)});
			writer.insert("users", null, user.GetCV(true));
			b = true;
		}
		else if (user.Update(pUser))
		{
			writer.update("users", user.GetCV(false), "id = ?", new String[]{Long.toString(user.Id)});
			b = true;
		}
		writer.close();
		db.close();
		
		if (b && pActivity.GetManager().GetCurrentFragment() != null)
		{
			LongSparseArray<User> users = new LongSparseArray<User>();
			users.append(user.Id, user);
			pActivity.GetManager().GetCurrentFragment().UsersChanged(users);
		}
		
		return user;
	}
	
	
	public User GetUser(long pID)
	{
		User user = _Users.get(pID);

		if (user == null)
		{
			DB db = new DB(_hContext);
			SQLiteDatabase reader = db.getReadableDatabase();
			Cursor cursor = reader.query("users", null, "id = ?", new String[]{Long.toString(pID)}, null, null, null);
			if (cursor.moveToFirst()) 
			{
				user = new User(cursor);
				_Users.append(user.Id, user);
			}
			cursor.close();
			reader.close();
			db.close();
		}
		
		return user;
	}


	public LongSparseArray<User> GetFriends()
	{
		LongSparseArray<User> friends = new LongSparseArray<User>();
		for (int i = 0; i < _Users.size(); i++)
		{
			User user = _Users.valueAt(i);
			if (user.IsFriend)
			{
				friends.append(user.Id, user);
			}
		}
		return friends;
	}


	public void Clear(SQLiteDatabase pWriter)
	{
		 pWriter.delete("users", null, null);
		_Users.clear();
	}


	public void SaveChanges(User pUser, ActivityMain pActivity)
	{
		DB db = new DB(_hContext);
		SQLiteDatabase writer = db.getWritableDatabase();
		writer.update("users", pUser.GetCV(false), "id = ?", new String[]{Long.toString(pUser.Id)});
		writer.close();
		db.close();
		
		if (pActivity.GetManager().GetCurrentFragment() != null)
		{
			LongSparseArray<User> users = new LongSparseArray<User>();
			users.append(pUser.Id, pUser);
			pActivity.GetManager().GetCurrentFragment().UsersChanged(users);
		}
	}
	
	public static JSONObject GetUsersOptions(ActivityMain pContext, ArrayList<Long> pUsers) throws JSONException
	{
		JSONArray userlist = new JSONArray();
		for (int i = 0; i < pUsers.size(); i++)
		{
			userlist.put(pUsers.get(i));
		}
		return _GetUsersOptions(pContext, userlist);
	}
	public static JSONObject GetUsersOptions(ActivityMain pContext, Long... pUsers) throws JSONException
	{
		JSONArray userlist = new JSONArray();
		for (int i = 0; i < pUsers.length; i++)
		{
			userlist.put(pUsers[i]);
		}
		return _GetUsersOptions(pContext, userlist);
	}
	private static JSONObject _GetUsersOptions(ActivityMain pContext, JSONArray pUserList) throws JSONException
	{
		JSONObject options = new JSONObject();
		options.put("userlist", pUserList);
		options.put("locale", pContext.GetProfile().GetLocale());
		options.put("status", -1);
		options.put("isfriend", -1);
		options.put("country", 0);
		options.put("city", 0);
		options.put("sex", "");
		options.put("minage", 0);
		options.put("maxage", 0);
		options.put("mask", "");
		options.put("lat1", 0);
		options.put("lng1", 0);
		options.put("lat2", 0);
		options.put("lng2", 0);
		return options;
	}
}