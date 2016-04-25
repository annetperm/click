package com.messme.contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.socket.ImageLoader;
import com.messme.user.User;

import android.content.Context;
import android.content.Intent;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AdapterContacts extends BaseAdapter implements OnClickListener
{	
	public class Item
	{
		public final int Type;
		public Contact hContact = null;
		public User hFriend = null;
		public User hUser = null;
		
		public Item(int pType)
		{
			Type = pType;
		}
	}
	
	
	public final static int TYPE_FRIEND = 1;
	public final static int TYPE_CONTACT_HEADER = 2;
	public final static int TYPE_CONTACT = 3;
	public final static int TYPE_SEPARATOR = 4;
	public final static int TYPE_HEADER = 5;
	public final static int TYPE_USER = 6;
	public final static int TYPE_SEPARATOR2 = 7;

	
	private final ActivityMain _hActivity;
	private final ArrayList<Item> _Items = new ArrayList<Item>();
	private final LayoutInflater _Inflater;
	private final ImageLoader _Loader;
	private String _Filter = "";
	private final ArrayList<User> _GlobalSearch = new ArrayList<User>();
	
	
	public AdapterContacts(ActivityMain pActivity)
	{
		_hActivity = pActivity;
		_Filter = "";
		_Inflater = (LayoutInflater)_hActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_Loader = new ImageLoader(_hActivity);
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
	public Item getItem(int pPosition)
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
		Item item = _Items.get(pPosition);
		boolean b = false;
		if (pConvertView == null)
		{
			b = true;
		}
		else if (Integer.parseInt(pConvertView.getTag().toString()) != item.Type)
		{
			b = true;
		}
		if (b)
		{
			switch (item.Type)
			{
				case TYPE_FRIEND:
					pConvertView = _Inflater.inflate(R.layout.contacts_listitem_friend, pParent, false);
					break;
				case TYPE_CONTACT_HEADER:
				case TYPE_CONTACT:
					pConvertView = _Inflater.inflate(R.layout.contacts_listitem, pParent, false);
					break;
				case TYPE_SEPARATOR:
					pConvertView = _Inflater.inflate(R.layout.contacts_listitem_separator, pParent, false);
					break;
				case TYPE_HEADER:
					pConvertView = _Inflater.inflate(R.layout.contacts_listitem_header, pParent, false);
					break;
				case TYPE_USER:
					pConvertView = _Inflater.inflate(R.layout.contacts_listitem_user, pParent, false);
					break;
				case TYPE_SEPARATOR2:
					pConvertView = _Inflater.inflate(R.layout.contacts_listitem_separator2, pParent, false);
					break;
			}
			pConvertView.setTag(item.Type);
		}
		
		switch (item.Type)
		{
			case TYPE_FRIEND:
				TextView tvFriendName = (TextView) pConvertView.findViewById(R.id.tvContactsListitemFriendName);
				TextView tvFriendLogin = (TextView) pConvertView.findViewById(R.id.tvContactsListitemFriendLogin);
				TextView tvFriendMessage = (TextView) pConvertView.findViewById(R.id.tvContactsListitemFriendMessage);
				
				if (item.hFriend.GetName(_hActivity).equals(item.hFriend.Login.trim()))
				{
					tvFriendName.setVisibility(View.GONE);
					tvFriendLogin.setGravity(Gravity.CENTER_VERTICAL);
					tvFriendMessage.setGravity(Gravity.CENTER_VERTICAL);
				}
				else
				{
					tvFriendName.setVisibility(View.VISIBLE);
					tvFriendName.setText(item.hFriend.GetName(_hActivity));
					tvFriendLogin.setGravity(Gravity.TOP);
					tvFriendMessage.setGravity(Gravity.TOP);
				}
				
				tvFriendLogin.setText(item.hFriend.Login);
				
//				ChatHeader chatHeader = _hContext.GetChats().GetDialogHeader(item.hFriend.Id);
//				if (chatHeader == null)
//				{
					tvFriendMessage.setVisibility(View.GONE);
//				}
//				else
//				{
//					tvFriendMessage.setVisibility(View.VISIBLE);
//					tvFriendMessage.setText(" - " + chatHeader.GetLastMessage());
//				}
				
				ImageView ivFriendAvatar = (ImageView)pConvertView.findViewById(R.id.ivContactsListitemFriendAvatar);
				ivFriendAvatar.setOnClickListener(this);
				ivFriendAvatar.setTag(item.hFriend);
				if (item.hFriend.HasImage())
				{	
					ivFriendAvatar.setImageBitmap(item.hFriend.GetImage());
				}
				else
				{
					ivFriendAvatar.setImageResource(R.drawable.ic_load);
					_Loader.Load(ivFriendAvatar, item.hFriend);
				}
				
				if (_Items.get(pPosition + 1) != null)
				{
					if (_Items.get(pPosition + 1).Type == TYPE_SEPARATOR2)
					{
						pConvertView.findViewById(R.id.llContactsListitemFriendNext).setVisibility(View.INVISIBLE);
					}
					else
					{
						pConvertView.findViewById(R.id.llContactsListitemFriendNext).setVisibility(View.VISIBLE);
					}
				}
				else
				{
					pConvertView.findViewById(R.id.llContactsListitemFriendNext).setVisibility(View.INVISIBLE);
				}
				break;
			case TYPE_CONTACT_HEADER:
				((TextView)pConvertView.findViewById(R.id.tvContactsListitemGroup)).setText(item.hContact.Group);
				((TextView)pConvertView.findViewById(R.id.tvContactsListitemName)).setText(item.hContact.Name);
				pConvertView.findViewById(R.id.btnContactsListitemInvite).setOnClickListener(this);
				pConvertView.findViewById(R.id.btnContactsListitemInvite).setTag(item.hContact);
				break;
			case TYPE_CONTACT:
				pConvertView.findViewById(R.id.tvContactsListitemGroup).setVisibility(View.INVISIBLE);
				((TextView)pConvertView.findViewById(R.id.tvContactsListitemName)).setText(item.hContact.Name);
				pConvertView.findViewById(R.id.btnContactsListitemInvite).setOnClickListener(this);
				pConvertView.findViewById(R.id.btnContactsListitemInvite).setTag(item.hContact);
				break;
			case TYPE_USER:
				((TextView)pConvertView.findViewById(R.id.tvContactsListitemUserName)).setText(item.hUser.GetName(_hActivity));
				ImageView ivUserAvatar = (ImageView)pConvertView.findViewById(R.id.ivContactsListitemUserAvatar);
				ivUserAvatar.setOnClickListener(this);
				ivUserAvatar.setTag(item.hUser);
				if (item.hUser.HasImage())
				{	
					ivUserAvatar.setImageBitmap(item.hUser.GetImage());
				}
				else
				{
					ivUserAvatar.setImageResource(R.drawable.ic_load);
					_Loader.Load(ivUserAvatar, item.hUser);
				}
				break;
		}
		
		return pConvertView;
	}


	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivContactsListitemFriendAvatar:
				User friend = (User) pView.getTag();
				_hActivity.GetManager().GoToUser(friend.Id);
				break;
			case R.id.btnContactsListitemInvite:
				Contact contact = (Contact) pView.getTag();
				Log.d("Messme", "AdapterContacts: click to invite, contact name: " + contact.Name);
				Intent smsIntent = new Intent(Intent.ACTION_VIEW);
				smsIntent.setType("vnd.android-dir/mms-sms");
				smsIntent.putExtra("address", Long.toString(contact.Phone));
				smsIntent.putExtra("sms_body","Body of Message");
				_hActivity.startActivity(smsIntent);
				break;
			case R.id.ivContactsListitemUserAvatar:
				User user = (User) pView.getTag();
				user = _hActivity.GetUsers().AddUser(user, _hActivity);
				_hActivity.GetManager().GoToUser(user.Id);
				break;
		}
	}

	public void SetFilter(String pText)
	{
		_Filter = pText.toLowerCase().trim();
	}
	public String GetFilter()
	{
		return _Filter;
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
	
	
	private void _RefreshData()
	{
		_Items.clear();
		ArrayList<User> add = new ArrayList<User>();
		LongSparseArray<User> friends = _hActivity.GetUsers().GetFriends();
		for (int i = 0; i < friends.size(); i++)
		{
			User friend = friends.valueAt(i);
			Contact contact = _hActivity.GetContacts().GetContact(friend.Id);
			if (_Filter.length() == 0)
			{
				add.add(friend);
			}
			else
			{
				if (friend.Name.toLowerCase().contains(_Filter) 
						|| friend.Surname.toLowerCase().contains(_Filter) 
						|| friend.Login.toLowerCase().contains(_Filter)
						|| contact != null && contact.NameLowerCase.contains(_Filter))
				{
					add.add(friend);
				}
			}
		}
		Collections.sort(add, new Comparator<User>()
		{
			@Override
			public int compare(User pUser1, User pUser2)
			{
				return pUser1.GetName(_hActivity).toLowerCase().compareTo(pUser2.GetName(_hActivity).toLowerCase());
			}
		});
		for (int i = 0; i < add.size(); i++)
		{
			Item item = new Item(TYPE_FRIEND);
			item.hFriend = add.get(i);
			_Items.add(item);
		}
		add.clear();
		
		LongSparseArray<Contact> contacts = _hActivity.GetContacts().GetContacts();
		ArrayList<Contact> contactsSort = new ArrayList<Contact>();
		for (int i = 0; i < contacts.size(); i++)
		{
			contactsSort.add(contacts.valueAt(i));
		}
		Collections.sort(contactsSort);
		String group = "";
		if (_Items.size() > 0)
		{
			Item item = new Item(TYPE_SEPARATOR2);
			_Items.add(item);
		}
		boolean first = true;
		for (int i = 0; i < contactsSort.size(); i++)
		{
			if (friends.get(contactsSort.get(i).Phone) == null)
			{
				Contact contact = contactsSort.get(i);
				if (_Filter.length() == 0 || contact.NameLowerCase.contains(_Filter)) 
				{
					if (group.equals(contact.Group))
					{
						Item item = new Item(TYPE_CONTACT);
						item.hContact = contact;
						_Items.add(item);
					}
					else
					{
						if (!first)
						{
							_Items.add(new Item(TYPE_SEPARATOR));
						}
						Item item = new Item(TYPE_CONTACT_HEADER);
						item.hContact = contact;
						group = contact.Group;
						_Items.add(item);
					}
					first = false;
				}
			}
		}
		
		if (_Filter.length() >= 5)
		{
			Item item = new Item(TYPE_HEADER);
			_Items.add(item);
			
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
					add.add(_GlobalSearch.get(i));
				}
			}
			
			Collections.sort(add, new Comparator<User>()
			{
				@Override
				public int compare(User pUser1, User pUser2)
				{
					return pUser1.GetName(_hActivity).toLowerCase().compareTo(pUser2.GetName(_hActivity).toLowerCase());
				}
			});
			for (int i = 0; i < add.size(); i++)
			{
				item = new Item(TYPE_USER);
				item.hUser = add.get(i);
				_Items.add(item);
			}
			add.clear();
		}
		else
		{
			_GlobalSearch.clear();
		}
	}
}