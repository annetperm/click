package com.messme.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper
{
	public DB(Context pContext) 
	{
		super(pContext, "MessMeDB", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase pDB)
	{
		pDB.execSQL("create table users (" 
				+ "id integer primary key,"
				+ "login text,"
				+ "name text,"
				+ "surname text,"
				+ "sex text,"
				+ "age int,"
				+ "country int,"
				+ "city int,"
				+ "cityname text,"
				+ "birthdate text,"
				+ "likes int,"
				+ "liked int,"
				+ "status int,"
				+ "geostatus int,"
				+ "lat double,"
				+ "lng double,"
				+ "url text,"
				+ "isfriend int,"
				+ "statusdate text,"
				+ "locked int);");
		
		pDB.execSQL("create table images (" 
				+ "url text primary key,"
				+ "image blob);");

		pDB.execSQL("create table dialogheaders ("
				+ "messageid text,"			//0
				+ "message text,"			//1
	            + "datetime text,"			//2
	            + "type int,"				//3
	            + "archive int,"			//4
	            + "id int primary key,"		//5
	            + "unreaded int,"			//6
	            + "unreadflag int);");		//7
		pDB.execSQL("create table dialogs ("
	            + "mid text key," 			//0
	            + "datetime text,"			//1
	            + "status int,"				//2
	            + "message text,"			//3
	            + "count int,"				//4
	            + "lat double,"				//5
	    	    + "lng double,"				//6
	            + "sync int,"				//7
	            + "issnap int,"				//8
	            + "timer int,"				//9
	            + "type int,"				//10 - 1: моя, 0: не моя
				+ "id int key,"				//11 - ключ диалога а не id отправителя
        		+ "entryid text);");		//12 - из рассылки
		
		pDB.execSQL("create table deliveryheaders ("
				+ "messageid text,"			//0
				+ "message text,"			//1
	            + "datetime text,"			//2
	            + "type int,"				//3
	            + "archive int,"			//4
	            + "id text primary key,"	//5
	            + "createt text,"			//6
				+ "name text);");			//7
		pDB.execSQL("create table deliveries ("
	            + "mid text key,"			//0
	            + "datetime text,"			//1	
	            + "status int,"				//2
	            + "message text,"			//3
	            + "count int,"				//4
	            + "lat double,"				//5
	    	    + "lng double,"				//6
	            + "sync int,"				//7
	            + "issnap int,"				//8
	            + "timer int,"				//9
				+ "id text key);");			//10 - id рассылки
		
		pDB.execSQL("create table chatheaders ("
				+ "messageid text,"			//0
				+ "message text,"			//1
	            + "datetime text,"			//2
	            + "type int,"				//3
	            + "archive int,"			//4
	            + "id text primary key,"	//5
	            + "createt text,"			//6
				+ "name text,"				//7
				+ "description text,"		//8
				+ "avatar text,"			//9
				+ "adminid int,"			//10
        		+ "unreaded int,"			//11
        		+ "unreadflag int,"			//12
        		+ "senderid int,"			//13
        		+ "member int);");			//14
		pDB.execSQL("create table chats ("
	            + "mid text key,"			//0
	            + "datetime text,"			//1
	            + "status int,"				//2
	            + "message text,"			//3
	            + "count int,"				//4
	            + "lat double,"				//5
	    	    + "lng double,"				//6
	            + "sync int,"				//7
				+ "id text key,"			//8 - id чата
				+ "senderid int);");		//9 - 
		
		pDB.execSQL("create table pairs ("
	            + "id text,"
				+ "userid int);");
		
		pDB.execSQL("create table attachments ("
	            + "id text,"
	            + "mid text,"
	            + "type int,"	
	            + "title text,"	
	            + "description text,"
	            + "filename text,"
	            + "contenttype text,"
	            + "filesize int,"
	            + "realpath text);");
		
		pDB.execSQL("create table notsended (" 
				+ "mid tex primary key,"
				+ "action tex,"
				+ "options text);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}
}
