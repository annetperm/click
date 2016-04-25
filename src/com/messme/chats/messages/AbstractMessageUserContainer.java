package com.messme.chats.messages;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.messme.data.DB;
import com.messme.user.User;
import com.messme.user.Users;
import com.messme.util.DateUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public abstract class AbstractMessageUserContainer<T extends AbstractMessage> extends AbstractMessageContainer<T>
{
	protected final String __ID;
	protected String __Name;
	private Date _DateTime;
	private final ArrayList<Long> _UsersID = new ArrayList<Long>();
	
	
	public AbstractMessageUserContainer(Context pContext, Cursor pCursor)
	{
		super(pContext, pCursor);
		__ID = pCursor.getString(5);
		try 
		{  
			_DateTime = DateUtil.DATE_TIME.parse(pCursor.getString(6));  //2015-10-01T08:10:53
		} 
		catch (ParseException e) 
		{  
			_DateTime = new Date();
		}
		__Name = pCursor.getString(7);
	}
	public final void AddUserFromDB(long pUserID)
	{
		_UsersID.add(pUserID);
	}
	public AbstractMessageUserContainer(Context pContext, String pID)
	{
		super(pContext);
		__ID = pID;
		__Name = "";
		_DateTime = new Date();
	}
	
	
	public final ArrayList<Long> GetUsers()
	{
		return _UsersID;
	}	
	public ArrayList<Long> GetEmptyUsers(Users pUsers)
	{
		ArrayList<Long> result = new ArrayList<Long>();
		for (int i = 0; i < _UsersID.size(); i++)
		{
			User user = pUsers.GetUser(_UsersID.get(i));
			if (user == null)
			{
				result.add(_UsersID.get(i));
			}
		}
		return result;
	}
	public boolean IsContainUser(long pUserID)
	{
		for (int i = 0; i < _UsersID.size(); i++)
		{
			if (_UsersID.get(i).equals(pUserID))
			{
				return true;
			}
		}
		return false;
	}
	
	public final String GetName()
	{
		return __Name;
	}
	public final String GetDateTime()
	{
		return DateUtil.GetDateTime(_DateTime);
	}
	
	public void Update(JSONObject pResult, long pMyID) throws JSONException
	{
		__Name = pResult.getString("name");
		try 
		{  
			_DateTime = DateUtil.DATE_TIME.parse(pResult.getString("date"));  //2015-10-01T08:10:53
		} 
		catch (ParseException e) 
		{  
			_DateTime = new Date();
		}
		
		_UsersID.clear();
		JSONArray userlist = pResult.getJSONArray("userlist");
		for (int i = 0; i < userlist.length(); i++)
		{
			long id = userlist.getLong(i);
			// чтобы сам не попал
			if (id != pMyID)
			{
				_UsersID.add(id);
			}
		}
		
		DB db = new DB(__hContext);
		SQLiteDatabase writer = db.getWritableDatabase();
		writer.update(__GetTableHeaderName(), GetCV(false), "id = ?", new String[]{__ID});
		writer.delete("pairs", "id = ?", new String[]{__ID});
		for (int i = 0; i < _UsersID.size(); i++)
		{
			ContentValues cv = new ContentValues();
			cv.put("id", __ID);
			cv.put("userid", _UsersID.get(i));
			writer.insert("pairs", null, cv);
		}
		writer.close();
		db.close();
	}
	
	
	@Override
	public ContentValues GetCV(boolean pNew)
	{
		ContentValues cv = super.GetCV(pNew);
		if (pNew)
		{
			cv.put("id", __ID);
			cv.put("createt", DateUtil.DATE_TIME.format(_DateTime));
		}
		cv.put("name", __Name);
		return cv;
	}
	
	@Override
	public final String GetID()
	{
		return __ID;
	}
	
	@Override
	protected Date __GetDateToSort()
	{
		if (__LastMessageID.equals(""))
		{
			return _DateTime;
		}
		return super.__GetDateToSort();
	}
}