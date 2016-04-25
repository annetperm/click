package com.messme.chats.messages.resend;

import java.util.ArrayList;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.socket.ImageLoader;
import com.messme.user.User;

import android.content.Context;
import android.support.v4.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AdapterResendContacts extends BaseAdapter
{
	private final ActivityMain _hActivity;
	private final LayoutInflater _Inflater;
	private final ImageLoader _Loader;
	private final ArrayList<User> _Items;
	
	public AdapterResendContacts(ActivityMain pActivity)
	{
		_hActivity = pActivity;
		_Inflater = (LayoutInflater)_hActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_Loader = new ImageLoader(_hActivity);
		_Items = new ArrayList<User>();
		LongSparseArray<User> friends = _hActivity.GetUsers().GetFriends();
		for (int i = 0; i < friends.size(); i++)
		{
			User friend = friends.valueAt(i);
			_Items.add(friend);
		}
	}
	
	@Override
	public int getCount()
	{
		return _Items.size();
	}

	@Override
	public Long getItem(int pPosition)
	{
		return _Items.get(pPosition).Id;
	}

	@Override
	public long getItemId(int pPosition)
	{
		return _Items.get(pPosition).Id;
	}

	@Override
	public View getView(int pPosition, View pConvertView, ViewGroup pParent)
	{
		pConvertView = _Inflater.inflate(R.layout.resend_contacts_listitem, pParent, false);
		
		User user = _Items.get(pPosition);
		
		((TextView) pConvertView.findViewById(R.id.tvResendListitem1)).setText(user.GetName(_hActivity));
		((TextView) pConvertView.findViewById(R.id.tvResendListitem2)).setText(user.GetStatus(_hActivity));
		
		if (user.HasImage())
		{
			((ImageView) pConvertView.findViewById(R.id.ivResendListitem)).setImageBitmap(user.GetImage());
		}
		else
		{
			ImageView ivAvatar = (ImageView)pConvertView.findViewById(R.id.ivResendListitem);
			ivAvatar.setImageResource(R.drawable.ic_load);
			_Loader.Load(ivAvatar, user);
		}
		
		return pConvertView;
	}
}