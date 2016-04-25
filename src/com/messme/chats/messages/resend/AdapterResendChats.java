package com.messme.chats.messages.resend;

import java.util.ArrayList;
import java.util.Collections;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.messages.AbstractMessageContainer;
import com.messme.chats.messages.chat.Chat;
import com.messme.chats.messages.dialog.Dialog;
import com.messme.socket.ImageLoader;
import com.messme.user.User;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class AdapterResendChats extends BaseAdapter
{
	private final ActivityMain _hActivity;
	private final LayoutInflater _Inflater;
	private final ImageLoader _Loader;
	private final ArrayList<AbstractMessageContainer<?>> _Items;
	
	public AdapterResendChats(ActivityMain pActivity, String pContainerID, int pContainerType, boolean pAll)
	{
		_hActivity = pActivity;
		_Inflater = (LayoutInflater)_hActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_Loader = new ImageLoader(_hActivity);
		_Items = new ArrayList<AbstractMessageContainer<?>>();
		
		if (pContainerType == 0)
		{
	 		for (int i = 0; i < _hActivity.GetDialogs().GetDialogs().size(); i++)
			{
	 			Dialog dialog = _hActivity.GetDialogs().GetDialogs().valueAt(i);
				if (!pContainerID.equals(dialog.GetID()))
				{
					if (pAll && dialog.GetType() == AbstractMessageContainer.MAIN || !pAll && dialog.GetType() == AbstractMessageContainer.SECRET)
					{
						_Items.add(dialog);
					}
				}
			}
			for (Chat chat: _hActivity.GetChats().GetChats().values())
			{
				if (pAll && chat.GetType() == AbstractMessageContainer.MAIN || !pAll && chat.GetType() == AbstractMessageContainer.SECRET)
				{
					_Items.add(chat);
				}
			}
		}
		else if (pContainerType == 1)
		{
	 		for (int i = 0; i < _hActivity.GetDialogs().GetDialogs().size(); i++)
			{
	 			Dialog dialog = _hActivity.GetDialogs().GetDialogs().valueAt(i);
				if (pAll && dialog.GetType() == AbstractMessageContainer.MAIN || !pAll && dialog.GetType() == AbstractMessageContainer.SECRET)
				{
					_Items.add(dialog);
				}
			}
			for (Chat chat: _hActivity.GetChats().GetChats().values())
			{
				if (!pContainerID.equals(chat.GetID()))
				{
					if (pAll && chat.GetType() == AbstractMessageContainer.MAIN || !pAll && chat.GetType() == AbstractMessageContainer.SECRET)
					{
						_Items.add(chat);
					}
				}
			}
		}
		else
		{
	 		for (int i = 0; i < _hActivity.GetDialogs().GetDialogs().size(); i++)
			{
	 			Dialog dialog = _hActivity.GetDialogs().GetDialogs().valueAt(i);
				if (pAll && dialog.GetType() == AbstractMessageContainer.MAIN || !pAll && dialog.GetType() == AbstractMessageContainer.SECRET)
				{
					_Items.add(dialog);
				}
			}
			for (Chat chat: _hActivity.GetChats().GetChats().values())
			{
				if (pAll && chat.GetType() == AbstractMessageContainer.MAIN || !pAll && chat.GetType() == AbstractMessageContainer.SECRET)
				{
					_Items.add(chat);
				}
			}
		}
		
		Collections.sort(_Items);
	}
	
	@Override
	public int getCount()
	{
		return _Items.size();
	}

	@Override
	public AbstractMessageContainer<?> getItem(int pPosition)
	{
		return _Items.get(pPosition);
	}

	@Override
	public long getItemId(int pPosition)
	{
		return pPosition;
	}

	@Override
	public View getView(int pPosition, View pContentView, ViewGroup pParent)
	{
		pContentView = _Inflater.inflate(R.layout.resend_chats_listitem, pParent, false);
		
		AbstractMessageContainer<?> container = _Items.get(pPosition);

		TextView tvMain = (TextView) pContentView.findViewById(R.id.tvResendListitem1);
		TextView tvSub = (TextView) pContentView.findViewById(R.id.tvResendListitem2);
		TextView tvTime = (TextView) pContentView.findViewById(R.id.tvResendListitem3);
		TextView tvCount = (TextView) pContentView.findViewById(R.id.tvResendListitem4);
		ImageView ivAvatar = (ImageView) pContentView.findViewById(R.id.ivResendListitem);
		
		if (container instanceof Dialog)
		{
			Dialog dialog = (Dialog) container;
			
			User user = _hActivity.GetUsers().GetUser(dialog.GetUserID());
			if (user == null)
			{
				ivAvatar.setImageResource(R.drawable.ic_load);
				
				tvMain.setText(R.string.Load);
			}
			else
			{
				if (user.HasImage())
				{
					ivAvatar.setImageBitmap(user.GetImage());
				}
				else
				{
					ivAvatar.setImageResource(R.drawable.ic_load);
					_Loader.Load(ivAvatar, user);
				}
				
				tvMain.setText(user.GetName(_hActivity));
			}
			tvSub.setText(dialog.GetLastMessage(""));
			
			if (dialog.GetUnReadedCount() > 0)
			{
				tvCount.setVisibility(View.VISIBLE);
				tvCount.setText(dialog.GetUnReadedText());
			}
			else if (dialog.IsUnReadFlag())
			{
				tvCount.setVisibility(View.VISIBLE);
				tvCount.setText("  ");
			}
			else
			{
				tvCount.setVisibility(View.GONE);
			}
		}
		else
		{
			Chat chat = (Chat) container;
			
			tvMain.setText(chat.GetName());
			
			if (chat.GetImage() == null)
			{
				ivAvatar.setImageResource(R.drawable.ic_load);
				_Loader.Load(ivAvatar, chat);
			}
			else
			{
				ivAvatar.setImageBitmap(chat.GetImage());
			}
			
			if (chat.GetLastSenderID() != _hActivity.GetProfile().GetID())
			{
				if (chat.GetLastSenderID() == 0)
				{
					tvSub.setText("");
				}
				else
				{
					String userName;
					User user = _hActivity.GetUsers().GetUser(chat.GetLastSenderID());
					if (user == null)
					{
						userName = _hActivity.getString(R.string.Load);
					}
					else
					{
						userName = user.GetName(_hActivity);
					}
					
					tvSub.setText(chat.GetLastMessage(userName));
				}
			}
			else
			{
				tvSub.setText(chat.GetLastMessage(""));
			}
			
			if (chat.GetUnReadedCount() > 0)
			{
				tvCount.setVisibility(View.VISIBLE);
				tvCount.setText(chat.GetUnReadedText());
			}
			else if (chat.IsUnReadFlag())
			{
				tvCount.setVisibility(View.VISIBLE);
				tvCount.setText("  ");
			}
			else
			{
				tvCount.setVisibility(View.GONE);
			}
		}
		
		if (container.GetLastMessageID().length() > 0)
		{
			tvTime.setVisibility(View.VISIBLE);
			tvTime.setText(container.GetLastMessageDateTime());
		}
		else
		{
			tvTime.setVisibility(View.GONE);
		}
		
		return pContentView;
	}
}