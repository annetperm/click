package com.messme;

import com.google.android.gcm.GCMBaseIntentService;
import com.messme.chats.ChatHeader;
import com.messme.chats.messages.chat.Chat;
import com.messme.chats.messages.chat.Chats;
import com.messme.chats.messages.dialog.Dialog;
import com.messme.chats.messages.dialog.Dialogs;
import com.messme.data.DB;
import com.messme.profile.Profile;
import com.messme.user.Users;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class GCMIntentService extends GCMBaseIntentService
{
    public GCMIntentService() 
    {
        super("1063835987475");
    }

    @Override
    protected void onRegistered(Context context, String registrationId) 
    {
        _Log("onRegistered");
        // Здесь мы должны отправить registrationId на наш сервер, чтобы он смог на него отправлять уведомления
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) 
    {
        _Log("onUnregistered");
    }

    @Override
    protected void onMessage(Context pContext, Intent pIntent) 
    {
        _Log("onMessage:" + pIntent.toString());
        if (ActivityMain.Active)
        {
        	return;
        }
        String action = pIntent.getStringExtra("action");
        if (action.equals("onopen"))
        {
        }
        else if (action.equals("onmessage"))
        {
    		DB db = new DB(pContext);
    		SQLiteDatabase reader = db.getReadableDatabase();
            Profile profile = new Profile(pContext);
            Dialogs dialogs = new Dialogs(pContext, reader);
            Chats chats = new Chats(pContext, profile, reader);
            Users users = new Users(pContext, reader);
            reader.close();

            String entryid = pIntent.getStringExtra("entryid");
            String message = pIntent.getStringExtra("message");
            String date = pIntent.getStringExtra("date");
            String dialogtype = pIntent.getStringExtra("dialogtype");

    		SQLiteDatabase writer = db.getWritableDatabase();
    		if (dialogtype.equals("0"))
    		{
    	        long userID = Long.parseLong(pIntent.getStringExtra("userid"));
    	        Dialog dialog = dialogs.GetDialog(userID);
    	        
    	        ChatHeader dialogHeader = new ChatHeader(dialog.GetUnReadedCount() + 1, entryid, message, date);

    	        dialog.UpdateHeader(dialogHeader, writer);
    		}
    		else if (dialogtype.equals("1"))
    		{
    			Chat chat = chats.GetChat(entryid);
    	        String fromname = pIntent.getStringExtra("fromname");
    	        String fromavatar = pIntent.getStringExtra("fromavatar");
    	        
    	        ChatHeader chatHeader = new ChatHeader(chat.GetUnReadedCount() + 1, entryid, message, date, fromname, fromavatar);

    	        chat.UpdateHeader(chatHeader, writer);
    		}
            writer.close();
            db.close();
            
            Push.Call(pContext, profile, users, dialogs, chats);
        }
    }

    @Override
    protected void onDeletedMessages(Context context, int total) 
    {
        _Log("onDeletedMessages");
    }

    @Override
    public void onError(Context context, String errorId) 
    {
        _Log("onError: " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) 
    {
        _Log("onRecoverableError: " + errorId);
        return super.onRecoverableError(context, errorId);
    }
	
	private void _Log(String pMessage)
	{
		Log.d("GCMIntentService", pMessage);
	}
}
