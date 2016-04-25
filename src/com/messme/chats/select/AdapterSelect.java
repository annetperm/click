package com.messme.chats.select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.contacts.Contact;
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


public class AdapterSelect extends BaseAdapter
{	
	private final ActivityMain _hActivity;
	private final LayoutInflater _Inflater;
	private final ImageLoader _Loader;
	private final ArrayList<User> _Items = new ArrayList<User>();
	private final ArrayList<Long> _hInList;
	private String _Filter = "";
	private ArrayList<Long> _Selected = new ArrayList<Long>();
	private ArrayList<User> _GlobalSearch = new ArrayList<User>();
	
	
	public AdapterSelect(ActivityMain pActivity, ArrayList<Long> pInList)
	{
		_hActivity = pActivity;
		_Filter = "";
		_Inflater = (LayoutInflater)_hActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_Loader = new ImageLoader(_hActivity);
		_hInList = pInList;
		_RefreshData();
	}
	
	@Override
	public void notifyDataSetChanged()
	{
		_RefreshData();
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		return _Items.size();
	}

	@Override
	public User getItem(int pPosition)
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
		User user = _Items.get(pPosition);
		
		if (pConvertView == null)
		{
			if (user == null)
			{
				pConvertView = _Inflater.inflate(R.layout.select_listitem_header, pParent, false);
				pConvertView.setTag("header");
			}
			else if (user.IsFriend)
			{
				pConvertView = _Inflater.inflate(R.layout.select_listitem_friend, pParent, false);
				pConvertView.setTag("friend");
			}
			else
			{
				pConvertView = _Inflater.inflate(R.layout.select_listitem_user, pParent, false);
				pConvertView.setTag("user");
			}
		}
		else if (pConvertView.getTag().toString().equals("header"))
		{
			if (user == null)
			{
			}
			else if (user.IsFriend)
			{
				pConvertView = _Inflater.inflate(R.layout.select_listitem_friend, pParent, false);
				pConvertView.setTag("friend");
			}
			else
			{
				pConvertView = _Inflater.inflate(R.layout.select_listitem_user, pParent, false);
				pConvertView.setTag("user");
			}
		}
		else if (pConvertView.getTag().toString().equals("friend"))
		{
			if (user == null)
			{
				pConvertView = _Inflater.inflate(R.layout.select_listitem_header, pParent, false);
				pConvertView.setTag("header");
			}
			else if (user.IsFriend)
			{
			}
			else
			{
				pConvertView = _Inflater.inflate(R.layout.select_listitem_user, pParent, false);
				pConvertView.setTag("user");
			}
		}
		else //if (pConvertView.getTag().toString().equals("user"))
		{
			if (user == null)
			{
				pConvertView = _Inflater.inflate(R.layout.select_listitem_header, pParent, false);
				pConvertView.setTag("header");
			}
			else if (user.IsFriend)
			{
				pConvertView = _Inflater.inflate(R.layout.select_listitem_friend, pParent, false);
				pConvertView.setTag("friend");
			}
			else
			{
			}
		}
		
		if (user == null)
		{
		}
		else if (user.IsFriend)
		{
			((TextView) pConvertView.findViewById(R.id.tvSelectListitemFriendName)).setText(user.GetName(_hActivity));
			
			if (user.HasImage())
			{
				((ImageView) pConvertView.findViewById(R.id.ivSelectListitemFriendAvatar)).setImageBitmap(user.GetImage());
			}
			else
			{
				ImageView ivAvatar = (ImageView)pConvertView.findViewById(R.id.ivSelectListitemFriendAvatar);
				ivAvatar.setImageResource(R.drawable.ic_load);
				_Loader.Load(ivAvatar, user);
			}
			
			//((TextView) pConvertView.findViewById(R.id.tvSelectListitemFriendPhone)).setText(user.GetPhone());
			
			((TextView) pConvertView.findViewById(R.id.tvSelectListitemFriendTime)).setText(user.GetStatus(_hActivity));
			
			int i = 0;
			for (i = 0; i < _Selected.size(); i++)
			{
				if (_Selected.get(i).longValue() == user.Id)
				{
					((ImageView) pConvertView.findViewById(R.id.ivSelectListitemFriendCheck)).setImageResource(R.drawable.ic_check_pressed);
					break;
				}
			}
			if (i == _Selected.size())
			{
				((ImageView) pConvertView.findViewById(R.id.ivSelectListitemFriendCheck)).setImageResource(R.drawable.ic_check);
			}
		}
		else
		{
			((TextView) pConvertView.findViewById(R.id.tvSelectListitemUserName)).setText(user.Login);
			
			if (user.HasImage())
			{
				((ImageView) pConvertView.findViewById(R.id.ivSelectListitemUserAvatar)).setImageBitmap(user.GetImage());
			}
			else
			{
				ImageView ivAvatar = (ImageView)pConvertView.findViewById(R.id.ivSelectListitemUserAvatar);
				ivAvatar.setImageResource(R.drawable.ic_load);
				_Loader.Load(ivAvatar, user);
			}
			
			int i = 0;
			for (i = 0; i < _Selected.size(); i++)
			{
				if (_Selected.get(i).longValue() == user.Id)
				{
					((ImageView) pConvertView.findViewById(R.id.ivSelectListitemUserCheck)).setImageResource(R.drawable.ic_check_pressed);
					break;
				}
			}
			if (i == _Selected.size())
			{
				((ImageView) pConvertView.findViewById(R.id.ivSelectListitemUserCheck)).setImageResource(R.drawable.ic_check);
			}
		}
		
		return pConvertView;
	}

	public void SetFilter(String pText)
	{
		_Filter = pText.toLowerCase().trim();
	}
	public String GetFilter()
	{
		return _Filter;
	}

	public int GetSelectedCount()
	{
		int count = 0;
		for (int i = 0; i < _Items.size(); i++)
		{
			User user = _Items.get(i);
			if (user != null)
			{
				for (int j = 0; j < _Selected.size(); j++)
				{
					if (_Selected.get(j).longValue() == user.Id)
					{
						count++;
						break;
					}
				}
			}
		}
		return count;
	}

	public void SetSelected(int pPosition)
	{
		User user = _Items.get(pPosition);
		if (user != null)
		{
			long id = user.Id;
			for (int i = 0; i < _Selected.size(); i++)
			{
				if (_Selected.get(i).longValue() == id)
				{
					_Selected.remove(i);
					return;
				}
			}
			_Selected.add(id);
		}
	}

	public void AddUser(User pUser)
	{
		if (pUser.IsFriend)
		{
			return;
		}
		for (int i = 0; i < _GlobalSearch.size(); i++)
		{
			if (_GlobalSearch.get(i).Id == pUser.Id)
			{
				return;
			}
		}
		_GlobalSearch.add(pUser);
	}

	public ArrayList<User> GetSelected()
	{
		ArrayList<User> result = new ArrayList<User>();
		for (int i = 0; i < _Items.size(); i++)
		{
			if (_Items.get(i) != null)
			{
				if (_Selected.contains(_Items.get(i).Id))
				{
					result.add(_Items.get(i));
				}
			}
		}
		return result;
	}
	
	
	private void _RefreshData()
	{
		_Items.clear();
		LongSparseArray<User> friends = _hActivity.GetUsers().GetFriends();
		for (int i = 0; i < friends.size(); i++)
		{
			User friend = friends.valueAt(i);
			Contact contact = _hActivity.GetContacts().GetContact(friend.Id);
			if (_Filter.length() == 0)
			{
				if (!_hInList.contains(friend.Id))
				{
					_Items.add(friend);
				}
			}
			else
			{
				if (friend.Name.toLowerCase().contains(_Filter) 
						|| friend.Surname.toLowerCase().contains(_Filter) 
						|| friend.Login.toLowerCase().contains(_Filter)
						|| contact != null && contact.NameLowerCase.equals(_Filter))
				{
					if (!_hInList.contains(friend.Id))
					{
						_Items.add(friend);
					}
				}
			}
		}
		
		Collections.sort(_Items, new Comparator<User>()
		{
			@Override
			public int compare(User pUser1, User pUser2)
			{
				return pUser1.GetName(_hActivity).toLowerCase().compareTo(pUser2.GetName(_hActivity).toLowerCase());
			}
		});
		
		if (_Filter.length() >= 5)
		{
			_Items.add(null);
			
			Collections.sort(_GlobalSearch, new Comparator<User>()
			{
				@Override
				public int compare(User pUser1, User pUser2)
				{
					return pUser1.GetName(_hActivity).toLowerCase().compareTo(pUser2.GetName(_hActivity).toLowerCase());
				}
			});
			
			for (int i = 0; i < _GlobalSearch.size(); i++)
			{
				if (_GlobalSearch.get(i).Id == _hActivity.GetProfile().GetID())
				{
					// не даем добавить самого себя
					continue;
				}
				if (_GlobalSearch.get(i).Login.toLowerCase().contains(_Filter))
				{
					if (!_hInList.contains(_GlobalSearch.get(i).Id))
					{
						_Items.add(_GlobalSearch.get(i));
					}
				}
			}
		}
		else
		{
			_GlobalSearch.clear();
		}
	}
}