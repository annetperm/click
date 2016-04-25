package com.messme;

import java.util.ArrayList;

import com.messme.chats.FragmentChats;
import com.messme.chats.messages.AbstractMessageContainer;
import com.messme.chats.messages.chat.Chat;
import com.messme.chats.messages.chat.Chats;
import com.messme.chats.messages.chat.FragmentChat;
import com.messme.chats.messages.dialog.Dialog;
import com.messme.chats.messages.dialog.Dialogs;
import com.messme.chats.messages.dialog.FragmentDialog;
import com.messme.profile.Profile;
import com.messme.user.User;
import com.messme.user.Users;
import com.messme.util.ImageUtil;
import com.messme.util.Util;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.Notification.InboxStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;


public class Push
{
	public final static String TYPE = "Type";
	public final static String ID = "ID";
	
	public final static int TYPE_ALL = 1;
	public final static int TYPE_DIALOG = 2;
	public final static int TYPE_CHAT = 3;
	
	
	public static void Call(ActivityMain pActivity)
	{
		if (!pActivity.GetProfile().IsNotification())
		{
			return;
		}

		if (pActivity.GetManager().GetCurrentFragment() != null 
				&& pActivity.GetManager().GetCurrentFragment() instanceof FragmentChats
				&& pActivity.IsVisible())
		{
			return;
		}
		
		ArrayList<AbstractMessageContainer<?>> items = new ArrayList<AbstractMessageContainer<?>>();
		int count = 0;
		
		for (int i = 0; i < pActivity.GetDialogs().GetDialogs().size(); i++)
		{
			Dialog dialog = pActivity.GetDialogs().GetDialogs().valueAt(i);
			if (dialog.GetUnReadedCount() <= 0)
			{
				continue;
			}
			if (dialog.GetType() == AbstractMessageContainer.SECRET)
			{
				continue;
			}
			if (pActivity.GetManager().GetCurrentFragment() != null 
					&& pActivity.GetManager().GetCurrentFragment() instanceof FragmentDialog 
					&& ((FragmentDialog) pActivity.GetManager().GetCurrentFragment()).GetUserID() == dialog.GetUserID()
					&& pActivity.IsVisible())
			{
				continue;
			}
			User user = pActivity.GetUsers().GetUser(dialog.GetUserID());
			if (user != null && user.IsLocked())
			{
				continue;
			}
			
			count += dialog.GetUnReadedCount();
			items.add(dialog);
		}
		for (Chat chat: pActivity.GetChats().GetChats().values())
		{
			if (chat.GetUnReadedCount() <= 0)
			{
				continue;
			}
			if (chat.GetType() == AbstractMessageContainer.SECRET)
			{
				continue;
			}
			if (pActivity.GetManager().GetCurrentFragment() != null 
					&& pActivity.GetManager().GetCurrentFragment() instanceof FragmentChat 
					&& ((FragmentChat) pActivity.GetManager().GetCurrentFragment()).GetChat().GetID().equals(chat.GetID())
					&& pActivity.IsVisible())
			{
				continue;
			}
			
			count += chat.GetUnReadedCount();
			items.add(chat);
		}
		
		_Call(pActivity, pActivity.GetProfile(), pActivity.GetUsers(), items, count);
	}
	
	public static void Call(Context pContext, Profile pProfile, Users pUsers, Dialogs pDialogs, Chats pChats)
	{
		if (!pProfile.IsNotification())
		{
			return;
		}

		ArrayList<AbstractMessageContainer<?>> items = new ArrayList<AbstractMessageContainer<?>>();
		int count = 0;
		
		for (int i = 0; i < pDialogs.GetDialogs().size(); i++)
		{
			Dialog dialog = pDialogs.GetDialogs().valueAt(i);
			if (dialog.GetUnReadedCount() <= 0)
			{
				continue;
			}
			if (dialog.GetType() == AbstractMessageContainer.SECRET)
			{
				continue;
			}
			User user = pUsers.GetUser(dialog.GetUserID());
			if (user != null && user.IsLocked())
			{
				continue;
			}
			
			count += dialog.GetUnReadedCount();
			items.add(dialog);
		}
		for (Chat chat: pChats.GetChats().values())
		{
			if (chat.GetUnReadedCount() <= 0)
			{
				continue;
			}
			if (chat.GetType() == AbstractMessageContainer.SECRET)
			{
				continue;
			}
			
			count += chat.GetUnReadedCount();
			items.add(chat);
		}
		
		_Call(pContext, pProfile, pUsers, items, count);
	}

	
	private static void _Call(Context pContext, Profile pProfile, Users pUsers, ArrayList<AbstractMessageContainer<?>> pItems, int pCount)
	{
		if (pItems.size() > 0)
		{
			NotificationManager notificationManager = (NotificationManager) pContext.getSystemService(Context.NOTIFICATION_SERVICE);
			
		    Intent intent = new Intent(pContext.getApplicationContext(), ActivityMain.class);
			String countText;
			if (pCount == 1)
			{
				countText = pContext.getString(R.string.Notification1, pCount);
			}
			else if (pCount <= 4)
			{
				countText = pContext.getString(R.string.Notification234, pCount);
			}
			else if (pCount <= 20)
			{
				countText = pContext.getString(R.string.Notification567890, pCount);
			}
			else if (pCount % 100 == 11)
			{
				countText = pContext.getString(R.string.Notification567890, pCount);
			}
			else if (pCount % 10 == 1)
			{
				countText = pContext.getString(R.string.Notification1, pCount);
			}
			else if (pCount % 10 <= 4)
			{
				countText = pContext.getString(R.string.Notification234, pCount);
			}
			else
			{
				countText = pContext.getString(R.string.Notification567890, pCount);
			}

			Bitmap avatar;
			CharSequence text;
			if (pItems.size() == 1)
			{
				if (pItems.get(0) instanceof Dialog)
				{
					Dialog dialog = (Dialog) pItems.get(0);
					intent.putExtra(TYPE, TYPE_DIALOG);
					intent.putExtra(ID, dialog.GetUserID());
					
					User user = pUsers.GetUser(dialog.GetUserID());
					
					if (user == null)
					{
						avatar = BitmapFactory.decodeResource(pContext.getResources(), R.drawable.ic_photo_contact);
						
						text = dialog.GetLastMessage(Long.toString(dialog.GetUserID()), dialog.GetUnReadedCount() < 2 ? "" : dialog.GetUnReadedText(), false);
					}
					else
					{
						avatar = ImageUtil.LoadImageFromDB(pContext, user.Avatar);
						if (avatar == null)
						{
							avatar = BitmapFactory.decodeResource(pContext.getResources(), R.drawable.ic_photo_contact);
						}
						
						text = dialog.GetLastMessage(User.GetName(pContext, user), dialog.GetUnReadedCount() < 2 ? "" : dialog.GetUnReadedText(), false);
					}
				}
				else
				{
					Chat chat = (Chat) pItems.get(0);
					intent.putExtra(TYPE, TYPE_CHAT);
					intent.putExtra(ID, chat.GetID());
					
					User user = pUsers.GetUser(chat.GetLastSenderID());
					
					avatar = ImageUtil.LoadImageFromDB(pContext, chat.GetAvatarUrl());
					if (avatar == null)
					{
						avatar = BitmapFactory.decodeResource(pContext.getResources(), R.drawable.ic_chat_chat);
					}

					if (user == null)
					{
						text = chat.GetLastMessage(chat.GetName() + ": " + Long.toString(chat.GetLastSenderID()), chat.GetUnReadedCount() < 2 ? "" : chat.GetUnReadedText(), false);
					}
					else
					{
						text = chat.GetLastMessage(chat.GetName() + ": " + User.GetName(pContext, user), chat.GetUnReadedCount() < 2 ? "" : chat.GetUnReadedText(), false);
					}
				}
			}
			else
			{
				intent.putExtra(TYPE, TYPE_ALL);
				
				avatar = BitmapFactory.decodeResource(pContext.getResources(), R.drawable.ic_chat_chat);
				
				text = countText;
			}
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			
			PendingIntent pendingIntent = PendingIntent.getActivity(pContext.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			avatar = ImageUtil.GetCircle(avatar, Util.GetPixelsFromDp(pContext, 56));
			
			Builder builder = new Notification.Builder(pContext)
				.setTicker(countText)
				.setContentText(text)
				.setContentTitle(pContext.getText(R.string.app_name))
				.setSmallIcon(R.drawable.ic_push)
				.setLargeIcon(avatar)
				.setNumber(pCount)
				.setContentIntent(pendingIntent);
			
			Notification notification;
			
			if (pItems.size() == 1)
			{
				notification = builder.build();
			}
			else
			{
				InboxStyle inboxStyle = new Notification.InboxStyle(builder);
				inboxStyle.setSummaryText(countText);
				for (int i = 0; i < pItems.size(); i++)
				{
					CharSequence item;
					if (pItems.get(i) instanceof Dialog)
					{
						Dialog dialog = (Dialog) pItems.get(i);
						User user = pUsers.GetUser(dialog.GetUserID());
						
						if (user == null)
						{
							item = dialog.GetLastMessage(Long.toString(dialog.GetUserID()), dialog.GetUnReadedCount() < 2 ? "" : dialog.GetUnReadedText(), false);
						}
						else
						{
							item = dialog.GetLastMessage(User.GetName(pContext, user), dialog.GetUnReadedCount() < 2 ? "" : dialog.GetUnReadedText(), false);
						}
					}
					else
					{
						Chat chat = (Chat) pItems.get(i);
						User user = pUsers.GetUser(chat.GetLastSenderID());

						if (user == null)
						{
							item = chat.GetLastMessage(chat.GetName() + ": " + Long.toString(chat.GetLastSenderID()), chat.GetUnReadedCount() < 2 ? "" : chat.GetUnReadedText(), false);
						}
						else
						{
							item = chat.GetLastMessage(chat.GetName() + ": " + User.GetName(pContext, user), chat.GetUnReadedCount() < 2 ? "" : chat.GetUnReadedText(), false);
						}
					}
					inboxStyle.addLine(item);
				}
				notification = inboxStyle.build();
			}
			
			notification.ledARGB = pProfile.GetNotificationColor();
			notification.ledOffMS = 0;
			notification.ledOnMS = 1;
		    notification.flags |= Notification.FLAG_AUTO_CANCEL;
		    notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		    if (pProfile.IsNotificationVibrate())
		    {
				notification.vibrate = new long[]{1000, 1000};
		    }
			notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			
			notificationManager.cancel(0);
			notificationManager.notify(0, notification);
		}
	}
}