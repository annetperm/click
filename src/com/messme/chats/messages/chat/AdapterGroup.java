package com.messme.chats.messages.chat;

import java.util.ArrayList;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.socket.ImageLoader;
import com.messme.user.User;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class AdapterGroup extends BaseAdapter implements OnClickListener
{
	private final ActivityMain _hActivity;
	private final LayoutInflater _Inflater;
	private final ImageLoader _Loader;
	private final ArrayList<Long> _Items = new ArrayList<Long>();
	private final boolean _Admin;
	
	public AdapterGroup(ActivityMain pActivity, boolean pAdmin)
	{
		_hActivity = pActivity;
		_Inflater = (LayoutInflater)_hActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_Loader = new ImageLoader(_hActivity);
		_Admin = pAdmin;
	}
	
	
	public void AddUser(long pUserID)
	{
		_Items.add(pUserID);
	}
	
	public ArrayList<Long> GetItems()
	{
		return _Items;
	}

	@Override
	public int getCount()
	{
		return _Items.size();
	}

	@Override
	public Long getItem(int pPosition)
	{
		return _Items.get(pPosition);
	}

	@Override
	public long getItemId(int pPosition)
	{
		return _Items.get(pPosition);
	}
	
	@Override
	public View getView(int pPosition, View pConvertView, ViewGroup pParent)
	{
		User user = _hActivity.GetUsers().GetUser(_Items.get(pPosition));
		
		if (pConvertView == null)
		{
			pConvertView = _Inflater.inflate(R.layout.group_listitem, pParent, false);
		}
		
		TextView tvName = (TextView) pConvertView.findViewById(R.id.tvGroupListitemName);
		ImageView ivAvatar = (ImageView) pConvertView.findViewById(R.id.ivGroupListitemAvatar);
		ivAvatar.setOnClickListener(this);
		ivAvatar.setTag(_Items.get(pPosition));
		
		if (user == null)
		{
			tvName.setText(_hActivity.getText(R.string.Load).toString());
			
			ivAvatar.setImageResource(R.drawable.ic_load);
		}
		else
		{
			tvName.setText(user.GetName(_hActivity));

			if (user.HasImage())
			{
				ivAvatar.setImageBitmap(user.GetImage());
			}
			else
			{
				ivAvatar.setImageResource(R.drawable.ic_load);
				_Loader.Load(ivAvatar, user);
			}
		}
		
		ImageView ivDelete = (ImageView) pConvertView.findViewById(R.id.ivGroupListitemDelete);
		if (_Admin)
		{
			ivDelete.setVisibility(View.VISIBLE);
			ivDelete.setTag(_Items.get(pPosition));
			ivDelete.setOnClickListener(this);
		}
		else
		{
			ivDelete.setVisibility(View.GONE);
			ivDelete.setOnClickListener(null);
		}
		
		return pConvertView;
	}

	@Override
	public void onClick(View pView)
	{
		Long userID = (Long) pView.getTag();
		switch (pView.getId())
		{
			case R.id.ivGroupListitemAvatar:
				_hActivity.GetManager().GoToUser(userID);
				break;
			case R.id.ivGroupListitemDelete:
				for (int i = 0; i < _Items.size(); i++)
				{
					if (_Items.get(i) == userID)
					{
						_Items.remove(i);
						break;
					}
				}
				notifyDataSetChanged();
				break;
		}
	}


	public boolean Contains(ArrayList<Long> pUsers)
	{
		if (pUsers.size() != _Items.size())
		{
			return false;
		}
		else
		{
			for (int i = 0; i < pUsers.size(); i++)
			{
				boolean b = false;
				for (int j = 0; j < _Items.size(); j++)
				{
					if (pUsers.get(i).equals(_Items.get(j)))
					{
						b = true;
						break;
					}
				}
				if (!b)
				{
					return false;
				}
			}
			return true;
		}
	}
}