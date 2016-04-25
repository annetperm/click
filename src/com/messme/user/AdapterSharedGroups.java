package com.messme.user;

import java.util.ArrayList;
import java.util.Collections;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.messages.chat.Chat;
import com.messme.socket.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class AdapterSharedGroups extends BaseAdapter
{
	private final ActivityMain _hActivity;
	private final LayoutInflater _Inflater;
	private final ImageLoader _Loader;
	private final ArrayList<Chat> _Items;
	
	public AdapterSharedGroups(ActivityMain pActivity, long pUserID)
	{
		_hActivity = pActivity;
		_Inflater = (LayoutInflater)_hActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_Loader = new ImageLoader(_hActivity);
		_Items = new ArrayList<Chat>();
		
		for (Chat chat: _hActivity.GetChats().GetChats().values())
		{
			if (chat.IsContainUser(pUserID))
			{
				_Items.add(chat);
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
	public Chat getItem(int pPosition)
	{
		return _Items.get(pPosition);
	}

	@Override
	public long getItemId(int pPosition)
	{
		return pPosition;
	}

	@Override
	public View getView(int pPosition, View pConvertView, ViewGroup pParent)
	{
		pConvertView = _Inflater.inflate(R.layout.sharedgroups_listitem, pParent, false);
		
		Chat chat = _Items.get(pPosition);

		TextView tvMain = (TextView) pConvertView.findViewById(R.id.tvSharedGroupsListitem1);
		TextView tvSub = (TextView) pConvertView.findViewById(R.id.tvSharedGroupsListitem2);
		ImageView ivAvatar = (ImageView) pConvertView.findViewById(R.id.ivSharedGroupsListitem);
		
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
		
		
		return pConvertView;
	}
}