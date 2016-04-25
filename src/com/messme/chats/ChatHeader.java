package com.messme.chats;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.messme.util.DateUtil;


public class ChatHeader
{
	private final long _MyID;
	
	public String Entry; 
	public int Type; 			// тип чата (0 - обычный диалог, 1 - групповой чат, 2 - рассылка)
	
	public long FromID; 		// идентификатор отправителя последнего сообщения
	public String FromName; 	// Иван - имя отправителя
	public String FromSurname; 	// фамилия отправителя
	public String FromLogin; 	// логин отправителя
	public String FromAvatar; 	// код файла аватарки отправителя
	public int FromStatus; 	// статус отправителя
	
	public long ToID; 			// идентификатор получателя
	public String ToName; 		// имя получателя
	public String ToSurname; 	// фамилия получателя
	public String ToLogin; 		// логин получателя
	public String ToAvatar; 	// код файла аватарки получателя
	public int ToStatus; 	// статус получателя
	
	public int MsgType; 		// тип сообщения
	public String Message; 		// содержимое сообщения
	public String DateTime; 	// дата отправления сообщения
	public int Status; 			// статус последнего сообщения
	public int CntTotal; 		// общее количество людей (для групповых чатов и рассылок)
	public int CntOnline; 		// колчество людей онлайн (для групповых чатов и рассылок)
	public int MsgTotal; 		// общее количество сообщений в диалоге
	public int MsgUnread; 		// количество непрочитанных сообщений в диалоге
	
	public int CntAttach;
	
	public ChatHeader(long pMyID, JSONObject pJSON) throws JSONException
	{
		super();
		_MyID = pMyID;
		
		Type = pJSON.getInt("type"); 		
		Entry = pJSON.getString("entryid");
		FromID = pJSON.getLong("fromuserid"); 			
		FromName = pJSON.getString("fromname"); 	
		FromSurname = pJSON.getString("fromsurname"); 	
		FromLogin = pJSON.getString("fromlogin"); 	
		FromAvatar = pJSON.getString("fromavatar"); 	
		FromStatus = pJSON.getInt("fromstatus"); 
		ToID = pJSON.getLong("touserid"); 			
		ToName = pJSON.getString("toname"); 		
		ToSurname = pJSON.getString("tosurname"); 	
		ToLogin = pJSON.getString("tologin"); 	
		ToAvatar = pJSON.getString("toavatar"); 	
		ToStatus = pJSON.getInt("tostatus"); 	
		MsgType = pJSON.getInt("msgtype"); 		
		Message = pJSON.getString("message"); 
		DateTime = pJSON.getString("date");
		Status = pJSON.getInt("status"); 			
		CntTotal = pJSON.getInt("cnttotal"); 		
		CntOnline = pJSON.getInt("cntonline"); 		
		MsgTotal = pJSON.getInt("msgtotal"); 		
		MsgUnread = pJSON.getInt("msgunread"); 
		CntAttach = pJSON.getInt("cntattach");
	}
	public ChatHeader(int pMsgUnread, String pEntry, String pMessage, String pDateTime) 
	{
		super();
		_MyID = 0;
		
		Type = 0; 		
		Entry = pEntry;
		FromID = 0; 			
		FromName = ""; 	
		FromSurname = ""; 	
		FromLogin = ""; 	
		FromAvatar = ""; 	
		FromStatus = 0; 
		ToID = 0; 			
		ToName = ""; 		
		ToSurname = ""; 	
		ToLogin = ""; 	
		ToAvatar = ""; 	
		ToStatus = 0; 	
		MsgType = 0; 		
		Message = pMessage; 
		DateTime = pDateTime;
		Status = 0; 			
		CntTotal = 0; 		
		CntOnline = 0; 		
		MsgTotal = 0; 		
		MsgUnread = pMsgUnread; 
		CntAttach = 0;
	}
	public ChatHeader(int pMsgUnread, String pEntry, String pMessage, String pDateTime, String pFromName, String pFromAvatar) 
	{
		super();
		_MyID = 0;
		
		Type = 0; 		
		Entry = pEntry;
		FromID = 0; 			
		FromName = pFromName; 	
		FromSurname = ""; 	
		FromLogin = ""; 	
		FromAvatar = pFromAvatar; 	
		FromStatus = 0; 
		ToID = 0; 			
		ToName = ""; 		
		ToSurname = ""; 	
		ToLogin = ""; 	
		ToAvatar = ""; 	
		ToStatus = 0; 	
		MsgType = 0; 		
		Message = pMessage; 
		DateTime = pDateTime;
		Status = 0; 			
		CntTotal = 0; 		
		CntOnline = 0; 		
		MsgTotal = 0; 		
		MsgUnread = pMsgUnread; 
		CntAttach = 0;
	}
	
	public Date GetDate()
	{
		Date date;
		try 
		{  
			date = DateUtil.DATE_TIME.parse(DateTime);  //2015-10-01T08:10:53
		} 
		catch (ParseException e) 
		{  
			date = new Date();
		}
		return date;
	}
	
	public long GetUserId()
	{
		if (Type == 0)
		{
			if (_MyID == FromID)
			{
				return ToID;
			}
			else
			{
				return FromID;
			}
		}
		return 0;
	}
	
	public String GetUserLogin()
	{
		if (Type == 0)
		{
			if (_MyID == FromID)
			{
				return ToLogin;
			}
			else
			{
				return FromLogin;
			}
		}
		return FromLogin;
	}
	
	public String GetUserName()
	{
		if (Type == 0)
		{
			if (_MyID == FromID)
			{
				return ToName;
			}
			else
			{
				return FromName;
			}
		}
		return FromName;
	}
	
	public String GetAvatarID()
	{
		if (Type == 0)
		{
			if (_MyID == FromID)
			{
				return ToAvatar;
			}
			else
			{
				return FromAvatar;
			}
		}
		else if (Type == 1)
		{
			return ToAvatar;
		}
		return FromAvatar;
	}
}