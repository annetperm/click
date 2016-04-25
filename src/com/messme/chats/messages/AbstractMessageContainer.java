package com.messme.chats.messages;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.ChatHeader;
import com.messme.data.DB;
import com.messme.util.DateUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.support.v7.util.SortedList;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;


public abstract class AbstractMessageContainer<T extends AbstractMessage> implements Comparable<AbstractMessageContainer<?>>
{
	public static final int MAIN = 1;
	public static final int ARCHIVE = 2;
	public static final int SECRET = 3;
	
	
	private class MessagesCallback extends SortedList.Callback<T>
	{
		@Override
		public boolean areContentsTheSame(T pArg1, T pArg2)
		{
			return pArg1.GetID().equals(pArg2.GetID());
		}
		@Override
		public boolean areItemsTheSame(T pArg1, T pArg2)
		{
			return pArg1.GetID().equals(pArg2.GetID());
		}
		@Override
		public int compare(T pArg1, T pArg2)
		{
			return pArg1.GetDateTime().compareTo(pArg2.GetDateTime());
		}
		@Override
		public void onInserted(int pPosition, int pCount)
		{
			try
			{
				_hAdapter.notifyItemRangeInserted(pPosition, pCount);
				if (pPosition - 1 != -1)
				{
					_hAdapter.notifyItemChanged(pPosition - 1);
				}
				if (pPosition + pCount < __Messages.size())
				{
					_hAdapter.notifyItemChanged(pPosition + pCount);
				}
				if (pPosition == __Messages.size() - 1)
				{
					_hAdapter.OnBottomAdded(pPosition);
				}
			}
			catch(NullPointerException e)
			{
			}
		}
		
		@Override
		public void onChanged(int pPosition, int pCount)
		{
			try
			{
				_hAdapter.notifyItemRangeChanged(pPosition, pCount);
			}
			catch(NullPointerException e)
			{
			}
		}
		@Override
		public void onRemoved(int pPosition, int pCount)
		{
			try
			{
				if (pPosition + pCount > __Messages.size())
				{
					_hAdapter.notifyItemChanged(pPosition + pCount);
				}
				_hAdapter.notifyItemRangeRemoved(pPosition, pCount);
			}
			catch(NullPointerException e)
			{
			}
		}
		@Override
		public void onMoved(int pPositionFrom, int pPositionTo)
		{
			try
			{
				_hAdapter.notifyItemMoved(pPositionFrom, pPositionTo);
			}
			catch(NullPointerException e)
			{
			}
		}
	};
	
	
	protected final Context __hContext;
	protected String __LastMessageID;
	protected String __LastMessageText;
	protected Date __LastMessageDateTime;
	protected int __LastMessageType;
	protected int __Archive;
	protected SortedList<T> __Messages = null;
	private String _TypedText = "";
	
	private AbstractMessageAdapter<T> _hAdapter = null;
	
	// отвечает за удаелние при синхронизации dialog.lastlist
	private boolean _RemoveFlag = false;
	
	
	public AbstractMessageContainer(Context pContext, Cursor pCursor)
	{
		__hContext = pContext;
		__LastMessageID = pCursor.getString(0);
		__LastMessageText = pCursor.getString(1);
		try 
		{  
			__LastMessageDateTime = DateUtil.DATE_TIME.parse(pCursor.getString(2));  //2015-10-01T08:10:53
		} 
		catch (ParseException e) 
		{  
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR, -1 * TimeZone.getDefault().getRawOffset() / 1000 / 60 / 60);
			__LastMessageDateTime = calendar.getTime();
		}
		__LastMessageType = pCursor.getInt(3);
		__Archive = pCursor.getInt(4);
	}
	public AbstractMessageContainer(Context pContext)
	{
		__hContext = pContext;
		__LastMessageID = "";
		__LastMessageText = "";
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, -1 * TimeZone.getDefault().getRawOffset() / 1000 / 60 / 60);
		__LastMessageDateTime = calendar.getTime();
		__LastMessageType = AbstractMessage.TYPE_TEXT;
		__Archive = MAIN;
	}
	
	
	public String GetLastMessageID()
	{
		return __LastMessageID;
	}
	public String GetLastMessageText()
	{
		return __LastMessageText;
	}
	public CharSequence GetLastMessage(String pPrefix)
	{
		return GetLastMessage(pPrefix, "", true);
	}
	@SuppressWarnings("deprecation")
	public CharSequence GetLastMessage(String pPrefix, String pSuffix, boolean pUseIcon)
	{
		if (pPrefix.length() > 0)
		{
			pPrefix += ": ";
		}
		if (pSuffix.length() > 0)
		{
			pSuffix = " (" + pSuffix + ")";
		}
		switch (__LastMessageType)
		{
			case AbstractMessage.TYPE_TEXT:
				return pPrefix + __LastMessageText + pSuffix;
			case AbstractMessage.TYPE_PHOTO:
				if (pUseIcon)
				{
					SpannableString textPhoto = new SpannableString(pPrefix + " " + __hContext.getString(R.string.MessagesAttachPhoto) + pSuffix);
					textPhoto.setSpan(new ImageSpan(BitmapFactory.decodeResource(__hContext.getResources(), R.drawable.ic_photo)), pPrefix.length(), pPrefix.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					return textPhoto;
				}
				else
				{
					return new SpannableString(pPrefix + new String(Character.toChars(0x1f4f7)) + __hContext.getString(R.string.MessagesAttachPhoto) + pSuffix);
				}
			case AbstractMessage.TYPE_VIDEO:
				if (pUseIcon)
				{
					SpannableString textVideo = new SpannableString(pPrefix + " " + __hContext.getString(R.string.MessagesAttachVideo) + pSuffix);
					textVideo.setSpan(new ImageSpan(BitmapFactory.decodeResource(__hContext.getResources(), R.drawable.ic_video)), pPrefix.length(), pPrefix.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					return textVideo;
				}
				else
				{
					return new SpannableString(pPrefix + new String(Character.toChars(0x1F4F9)) + __hContext.getString(R.string.MessagesAttachVideo) + pSuffix);
				}
			case AbstractMessage.TYPE_AUDIO:
				if (pUseIcon)
				{
					SpannableString textAudio = new SpannableString(pPrefix + " " + __hContext.getString(R.string.MessagesAttachAudio) + pSuffix);
					textAudio.setSpan(new ImageSpan(BitmapFactory.decodeResource(__hContext.getResources(), R.drawable.ic_audio)), pPrefix.length(), pPrefix.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					return textAudio;
				}
				else
				{
					return new SpannableString(pPrefix + new String(Character.toChars(0x1F3A7)) + __hContext.getString(R.string.MessagesAttachAudio) + pSuffix);
				}
			case AbstractMessage.TYPE_VOICE:
				if (pUseIcon)
				{
					SpannableString textVoice = new SpannableString(pPrefix + " " + __hContext.getString(R.string.MessagesAttachVoice) + pSuffix);
					textVoice.setSpan(new ImageSpan(BitmapFactory.decodeResource(__hContext.getResources(), R.drawable.ic_voice)), pPrefix.length(), pPrefix.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					return textVoice;
				}
				else
				{
					return new SpannableString(pPrefix + new String(Character.toChars(0x1F3A4)) + __hContext.getString(R.string.MessagesAttachVoice) + pSuffix);
				}
			case AbstractMessage.TYPE_FILE:
			case AbstractMessage.TYPE_FILES:
				if (pUseIcon)
				{
					SpannableString textFile = new SpannableString(pPrefix + " " + __hContext.getString(R.string.MessagesAttachFile) + pSuffix);
					textFile.setSpan(new ImageSpan(BitmapFactory.decodeResource(__hContext.getResources(), R.drawable.ic_file)), pPrefix.length(), pPrefix.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					return textFile;
				}
				else
				{
					return new SpannableString(pPrefix + new String(Character.toChars(0x1F4C1)) + __hContext.getString(R.string.MessagesAttachFile) + pSuffix);
				}
			case AbstractMessage.TYPE_PLACE:
				if (pUseIcon)
				{
					SpannableString textPlace = new SpannableString(pPrefix + " " + __hContext.getString(R.string.MessagesAttachPlace) + pSuffix);
					textPlace.setSpan(new ImageSpan(BitmapFactory.decodeResource(__hContext.getResources(), R.drawable.ic_place)), pPrefix.length(), pPrefix.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					return textPlace;
				}
				else
				{
					return new SpannableString(pPrefix + new String(Character.toChars(0x1F4CC)) + __hContext.getString(R.string.MessagesAttachPlace) + pSuffix);
				}
			case AbstractMessage.TYPE_CONTACT:
				if (pUseIcon)
				{
					SpannableString textContact = new SpannableString(pPrefix + " " + __hContext.getString(R.string.MessagesAttachContact) + pSuffix);
					textContact.setSpan(new ImageSpan(BitmapFactory.decodeResource(__hContext.getResources(), R.drawable.ic_contact)), pPrefix.length(), pPrefix.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					return textContact;
				}
				else
				{
					return new SpannableString(pPrefix + new String(Character.toChars(0x1F4DE)) + __hContext.getString(R.string.MessagesAttachContact) + pSuffix);
				}
			default:
				return pPrefix + __LastMessageText + pSuffix;
		}
	}
	public String GetLastMessageDateTime()
	{
		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("EST"));
		long different = now.getTime().getTime() - __LastMessageDateTime.getTime() - TimeZone.getDefault().getRawOffset();
		int min = (int) (different / 1000 / 60);
		if (min <= 0)
		{
			return __hContext.getString(R.string.Time0).toString();
		}
		else if (min < 10)
		{
			return Integer.toString(min) + " " + __hContext.getString(R.string.Time1).toString();
		}
		else if (min < 30)
		{
			return __hContext.getString(R.string.Time2).toString();
		}
		else if (min < 60)
		{
			return __hContext.getString(R.string.Time3).toString();
		}
		else if (min < 120)
		{
			return __hContext.getString(R.string.Time4).toString();
		}
		else if (min < 60 * 24)
		{
			return __hContext.getString(R.string.Time5).toString();
		}
		else
		{
			Calendar cal = Calendar.getInstance();
			cal.setTime(__LastMessageDateTime);
			if (now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR))
			{
				return DateUtil.GetTime(__LastMessageDateTime);
			}
			else
			{
				return DateUtil.GetDate(__LastMessageDateTime);
			}
		}
	}
	public int GetLastMessageType()
	{
		return __LastMessageType;
	}
	
	
	public int GetType()
	{
		return __Archive;
	}
	public void SetType(int pArchive, SQLiteDatabase pWriter)
	{
		__Archive = pArchive;
		UpdateContainer(pWriter);
	}
	
	
	public void SetTypedText(String pText)
	{
		_TypedText = pText;
	}
	public String GetTypedText()
	{
		return _TypedText;
	}
	
	
	public void ResetRemoveFlag()
	{
		_RemoveFlag = true;
	}
	public void SetNotRemoveFlag()
	{
		_RemoveFlag = false;
	}
	public boolean IsRemoving()
	{
		return _RemoveFlag;
	}
	
	
	public final void SetAdapter(AbstractMessageAdapter<T> pAdapter)
	{
		_hAdapter = pAdapter;
	}
	
	
	public String GetLastSyncID()
	{
		for (int i = __Messages.size() - 1; i != -1 ; i--)
		{
			AbstractMessage message = __Messages.get(i);
			if (message.IsSynchronized() && message.GetStatus() == AbstractMessage.STATUS_READED)
			{
				return message.GetID();
			}
		}
		return "";
	}
	
	
	public final SortedList<T> GetMessages()
	{
		if (__Messages == null)
		{
			_LoadFromDB();
		}
		
		return __Messages;
	}
	public final T GetMessage(String pMessageID)
	{
		if (__Messages == null)
		{
			_LoadFromDB();
		}
		
		for (int i = __Messages.size() - 1; i != -1 ; i--)
		{
			if (__Messages.get(i).GetID().equals(pMessageID))
			{
				return __Messages.get(i);
			}
		}
		
		return null;
	}
	
	
	public final void AddMessage(T pMessage, SQLiteDatabase pWriter)
	{
		if (__Messages == null)
		{
			_LoadFromDB();
		}

		T message = GetMessage(pMessage.GetID());
		if (message == null)
		{
			int index = __Messages.add(pMessage);
			if (index == __Messages.size() - 1)
			{
				__LastMessageID = pMessage.GetID();
				__LastMessageText = pMessage.GetMessage();
				__LastMessageDateTime = pMessage.GetDateTime();
				__LastMessageType = pMessage.GetType();
				
				if (__Archive != SECRET)
				{
					__Archive = MAIN;
				}

				pWriter.update(__GetTableHeaderName(), GetCV(false), "id = ?", new String[]{GetID()});
			}

			pWriter.insert(__GetTableMessageName(), null, pMessage.GetCV());
			for (int i = 0; i < pMessage.GetAttachmentsCount(); i++)
			{
				pWriter.insert("attachments", null, pMessage.GetAttachment(i).GetCV(pMessage.GetID()));
			}
		}
		else
		{
			if (message.SetStatus(pMessage.GetStatus()) || pMessage.IsSynchronized())
			{
				message.SetSynchronized(pWriter);
				ContentValues cv = new ContentValues();
			    cv.put("status", message.GetStatus());
			    pWriter.update(__GetTableMessageName(), cv, "mid = ?", new String[]{message.GetID()});
			    
			    RefreshView(message.GetID());
			}
		}
	}

	public void RemoveMessage(ActivityMain pActivity, String pID, SQLiteDatabase pWriter)
	{
		for (int i = __Messages.size() - 1; i != -1; i--)
		{
			if (__Messages.get(i).GetID().equals(pID))
			{
				pWriter.delete(__GetTableMessageName(), "mid = ?", new String[]{pID});
				//writer.delete("attachments", "mid = ?", new String[]{pMessage.GetID()})
				
				if (i == __Messages.size() - 1)
				{
					if (__Messages.size() > 1)
					{
						T message = __Messages.get(i - 1);
						__LastMessageID = message.GetID();
						__LastMessageText = message.GetMessage();
						__LastMessageDateTime = message.GetDateTime();
						__LastMessageType = message.GetType();
					}
					else
					{
						__LastMessageID = "";
						__LastMessageText = "";
						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.HOUR, -1 * TimeZone.getDefault().getRawOffset() / 1000 / 60 / 60);
						__LastMessageDateTime = calendar.getTime();
						__LastMessageType = AbstractMessage.TYPE_TEXT;
					}
					pWriter.update(__GetTableHeaderName(), GetCV(false), "id = ?", new String[]{GetID()});
				}

				T message = __Messages.removeItemAt(i);
				message.RemoveAttachments(pActivity);
				
				if (pActivity.GetManager().GetCurrentFragment() != null)
				{
					ArrayList<AbstractMessageContainer<?>> list = new ArrayList<AbstractMessageContainer<?>>();
					list.add(this);
					pActivity.GetManager().GetCurrentFragment().HeadersChanged(list);
				}
				
				return;
			}
		}
	}
	
	public void ClearMessages(SQLiteDatabase pWriter)
	{
		pWriter.delete(__GetTableMessageName(), "id = ?", new String[]{GetID()});
		__Messages.clear();
	}
	
	
	public void Move(String pMessageID, SQLiteDatabase pWriter)
	{
		for (int i = __Messages.size() - 1; i != -1; i--)
		{
			if (__Messages.get(i).GetID().equals(pMessageID))
			{
				__Messages.recalculatePositionOfItemAt(i);
				break;
			}
		}
		AbstractMessage message = GetMessage(pMessageID);
		if (pMessageID.equals(__LastMessageID) && __LastMessageDateTime.equals(message.GetDateTime()))
		{
			__LastMessageDateTime = message.GetDateTime();
			pWriter.update(__GetTableHeaderName(), GetCV(false), "id = ?", new String[]{GetID()});
		}
	}

	
	public final void RefreshView(String pMessageID)
	{
		try
		{
			_hAdapter.RefreshView(pMessageID);
		}
		catch(NullPointerException e)
		{
		}
	}
	
		
	public ContentValues GetCV(boolean pNew)
	{
		ContentValues cv = new ContentValues();
		cv.put("messageid", __LastMessageID);
		cv.put("message", __LastMessageText);
		cv.put("datetime", DateUtil.DATE_TIME.format(__LastMessageDateTime));
		cv.put("type", __LastMessageType);
		cv.put("archive", __Archive);
		return cv;
	}


	public final void StartUploading(ActivityMain pActivity)
	{
		if (__Messages == null)
		{
			// не ишем сообщения не в открытых
			return;
		}
		
		for (int i = __Messages.size() - 1; i != -1; i--)
		{
			if (__Messages.get(i).GetStatus() == AbstractMessage.STATUS_NOTUPLOADED)
			{
				__Messages.get(i).Uploading(pActivity);
			}
		}
	}


	public final void UpdateMessage(AbstractMessage pMessage, SQLiteDatabase pWriter)
	{
		pWriter.update(__GetTableMessageName(), pMessage.GetCV(), "mid = ?", new String[]{pMessage.GetID()});
		
		RefreshView(pMessage.GetID());
	}
	
	
	public void UpdateContainer(SQLiteDatabase pWriter)
	{
		pWriter.update(__GetTableHeaderName(), GetCV(false), "id = ?", new String[]{GetID()});
	}
	
	@Override
	public final int compareTo(AbstractMessageContainer<?> pAnother)
	{
		return -1 * __GetDateToSort().compareTo(pAnother.__GetDateToSort());
	}
	
	protected Date __GetDateToSort()
	{
		return __LastMessageDateTime;
	}

	@SuppressWarnings("unchecked")
	private final void _LoadFromDB()
	{
		__Messages = new SortedList<T>((Class<T>) AbstractMessage.class, new MessagesCallback());
		
		DB db = new DB(__hContext);
		SQLiteDatabase reader = db.getReadableDatabase();

		Cursor cursor = reader.query(__GetTableMessageName(), null, "id = ?", new String[]{GetID()}, null, null, null);
		__OnLoadFromDB(cursor);
		cursor.close();
		
		for (int i = 0; i < __Messages.size(); i++)
		{
			AbstractMessage message = __Messages.get(i);
			if (message.GetAttachmentsCount() != 0)
			{
				cursor = reader.query("attachments", null, "mid = ?", new String[]{message.GetID()}, null, null, null);
				if (cursor.moveToFirst()) 
				{
			        do 
			        {
			        	message.AddAttachmentFromDB(new Attachment(cursor));
			        } while (cursor.moveToNext());
				}
				cursor.close();
			}
		}
		reader.close();
		db.close();
	}
	
	
	public abstract boolean UpdateHeader(ChatHeader pChatHeader, SQLiteDatabase pWriter);

	public abstract String GetID();
	
	protected abstract String __GetTableHeaderName();
	protected abstract String __GetTableMessageName();
	
	protected abstract void __OnLoadFromDB(Cursor pCursor);
}