package com.messme.chats;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.messages.AbstractMessageContainer;
import com.messme.chats.messages.chat.Chat;
import com.messme.chats.messages.delivery.Delivery;
import com.messme.chats.messages.dialog.Dialog;
import com.messme.data.DB;
import com.messme.user.User;
import com.messme.view.MessmeFragment;
import com.messme.view.TextWatcher;
import com.messme.view.TextWatcher.iOnTextChanged;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.LongSparseArray;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class FragmentChats extends MessmeFragment implements iOnTextChanged, OnClickListener
{
	private class ChatsFragmentPagerAdapter extends android.support.v13.app.FragmentPagerAdapter 
	{
		private ArchiveListFragment _ArchiveListFragment = null;
		private ChatsListFragment _ChatsListFragment = null;
		private SecretListFragment _SecretListFragment = null;
		private ArrayList<Fragment> _Items = new ArrayList<Fragment>();
		
		public ChatsFragmentPagerAdapter(FragmentManager pFragmentManager) 
		{
			super(pFragmentManager);
			_ArchiveListFragment = new ArchiveListFragment(__GetMainActivity(), FragmentChats.this);
			_ChatsListFragment = new ChatsListFragment(__GetMainActivity(), FragmentChats.this);
			_SecretListFragment = new SecretListFragment(__GetMainActivity(), FragmentChats.this);
			_Items.add(_ArchiveListFragment);
			_Items.add(_ChatsListFragment);
			_Items.add(_SecretListFragment);
		}
		@Override
		public Fragment getItem(int pPosition) 
		{
			return _Items.get(pPosition);
		}
		@Override
		public int getCount() 
		{
			return _Items.size();
		}
		@Override
		public CharSequence getPageTitle(int pPosition)
		{
			if (pPosition == 0)
			{
				return __GetMainActivity().getText(R.string.ChatsArchive);
			}
			else if (pPosition == 1)
			{
				return __GetMainActivity().getText(R.string.ChatsTitle);
			}
			else //if (pPosition == 2)
			{
				return __GetMainActivity().getText(R.string.ChatsSecret);
			}
		}
		@Override
		public void destroyItem(ViewGroup container, int pPosition, Object pObject)
		{
			//_Items.remove(pPosition);
			try
			{
			    FragmentManager manager = ((Fragment) pObject).getFragmentManager();
			    FragmentTransaction trans = manager.beginTransaction();
			    trans.remove((Fragment) pObject);
			    trans.commit();
			}
			catch (NullPointerException e)
			{
				//Util.CreateBreakPoint("destroyItem");
			}
		}
		@Override
		public int getItemPosition(Object object) 
		{
		    return POSITION_NONE;
		}
		
		public void SetFilter(String pText)
		{
			_ArchiveListFragment.SetFilter(pText);
			_ChatsListFragment.SetFilter(pText);
			_SecretListFragment.SetFilter(pText);
		}
		
		public void Update()
		{
			_ArchiveListFragment.Update();
			_ChatsListFragment.Update();
			_SecretListFragment.Update();
		}
	}
	
	
	private LinearLayout _llTitle = null;
	private LinearLayout _llSearch = null;
	private EditText _etSearch = null;
	private ViewPager _ViewPager = null; 
	private ChatsFragmentPagerAdapter _PagerAdapter = null;
	private RelativeLayout _rlDialogNew = null;
	private RelativeLayout _rlDialogHeader = null;
	private TextView _tvChats = null;
	private TextView _tvArchive = null;
	private TextView _tvSecret = null;
	private TextView _tvNotReaded = null;
	
	private TextWatcher _SearchTextWatcher = null;
	private AbstractMessageContainer<?> _hSelectedContainer = null;
	
	
	public FragmentChats(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
		_SearchTextWatcher = new TextWatcher(this, 1);
	}

	
	@SuppressWarnings("deprecation")
	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		View view = pInflater.inflate(R.layout.chats, pContainer, false);

		view.findViewById(R.id.ivChatsMenu).setOnClickListener(this);

		_llTitle = (LinearLayout) view.findViewById(R.id.llChatsTitle);
		view.findViewById(R.id.ivChatsSearchBack).setOnClickListener(this);
		view.findViewById(R.id.ivChatsSearch).setOnClickListener(this);
		
		_llSearch = (LinearLayout) view.findViewById(R.id.llChatsSearch);
		_etSearch = (EditText) view.findViewById(R.id.etChatsSearch);
		_etSearch.addTextChangedListener(_SearchTextWatcher);
		view.findViewById(R.id.ivChatsClose).setOnClickListener(this);
		
		view.findViewById(R.id.ivChatsAdd).setOnClickListener(this);
		view.findViewById(R.id.ivChatsAdd).bringToFront();
		
		view.findViewById(R.id.rlChats).setBackgroundDrawable(__GetMainActivity().GetProfile().GetTheme());
		
		_ViewPager = (ViewPager) view.findViewById(R.id.vpChats);
		_PagerAdapter = new ChatsFragmentPagerAdapter(__GetMainActivity().getFragmentManager());
		_ViewPager.setAdapter(_PagerAdapter);
		_ViewPager.setCurrentItem(1);
		_ViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{
			@Override
			public void onPageSelected(int pPosition)
			{
				_PagerAdapter._SecretListFragment.Lock();
			}
			@Override
			public void onPageScrolled(int pPosition, float arg1, int arg2)
			{
				if (pPosition != 2)
				{
					_PagerAdapter._SecretListFragment.Lock();
				}
			}
			@Override
			public void onPageScrollStateChanged(int arg0)
			{
				_PagerAdapter._SecretListFragment.Lock();
			}
		});
		
		_rlDialogNew = (RelativeLayout) view.findViewById(R.id.rlChatsDialogNew);
		_rlDialogNew.setOnClickListener(this);
		
		view.findViewById(R.id.llChatsDialogNewDialog).setOnClickListener(this);
		view.findViewById(R.id.llChatsDialogNewChat).setOnClickListener(this);
		view.findViewById(R.id.llChatsDialogNewDelivery).setOnClickListener(this);
		
		_rlDialogHeader = (RelativeLayout) view.findViewById(R.id.rlChatsDialogHeader);
		_rlDialogHeader.setOnClickListener(this);

		view.findViewById(R.id.tvChatsDialogHeaderInfo).setOnClickListener(this);
		_tvChats = (TextView) view.findViewById(R.id.tvChatsDialogHeaderChats);
		_tvChats.setOnClickListener(this);
		_tvArchive = (TextView) view.findViewById(R.id.tvChatsDialogHeaderArchive);
		_tvArchive.setOnClickListener(this);
		_tvSecret = (TextView) view.findViewById(R.id.tvChatsDialogHeaderSecret);
		_tvSecret.setOnClickListener(this);
		_tvNotReaded = (TextView) view.findViewById(R.id.tvChatsDialogHeaderNotreaded);
		_tvNotReaded.setOnClickListener(this);
		view.findViewById(R.id.tvChatsDialogHeaderRemove).setOnClickListener(this);
		
		return view;
	}
	
	@Override
	protected void __OnResume()
	{
		if (_etSearch.getText().length() > 0 && getView() != null && getView().findViewById(R.id.ivChatsSearch) != null)
		{
			getView().findViewById(R.id.ivChatsSearch).callOnClick();
		}
	}
	
	@Override
	protected void __OnDestroy()
	{
		_ViewPager.removeAllViews();
		//_ViewPager.setAdapter(null);
		try
		{
			_PagerAdapter.notifyDataSetChanged();
			_etSearch.removeTextChangedListener(_SearchTextWatcher);
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public void onClick(View pView)
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(_etSearch.getWindowToken(), 0);
		switch (pView.getId())
		{
			case R.id.ivChatsMenu:
				__GetMainActivity().MenuOpen();
				break;
			case R.id.ivChatsSearch:
				_llTitle.setVisibility(View.GONE);
				_llSearch.setVisibility(View.VISIBLE);
				_etSearch.setVisibility(View.VISIBLE);
				_etSearch.setEnabled(true);
				if (_etSearch.requestFocus())
				{
					imm.showSoftInput(_etSearch, InputMethodManager.SHOW_IMPLICIT);
				}
				break;
			case R.id.ivChatsSearchBack:
			case R.id.ivChatsClose:
				_llTitle.setVisibility(View.VISIBLE);
				_llSearch.setVisibility(View.GONE);
				if (pView.getId() == R.id.ivChatsClose)
				{
					_etSearch.setText("");
				}
				break;
			case R.id.ivChatsAdd:
				_rlDialogNew.setVisibility(View.VISIBLE);
				break;
				
			case R.id.rlChatsDialogNew:
				_rlDialogNew.setVisibility(View.GONE);
				break;
			case R.id.llChatsDialogNewDialog:
				if (__GetMainActivity().GetUsers().GetFriends().size() == 0)
				{
					__GetMainActivity().GetManager().GoToContacts();
				}
				else
				{
					__GetMainActivity().GetManager().GoToSelect();
				}
				break;
			case R.id.llChatsDialogNewChat:
				__GetMainActivity().GetManager().GoToGroup();
				break;
			case R.id.llChatsDialogNewDelivery:
				__GetMainActivity().GetManager().GoToDelivery();
				break;

			case R.id.rlChatsDialogHeader:
				_rlDialogHeader.setVisibility(View.GONE);
				break;
			case R.id.tvChatsDialogHeaderInfo:
				_rlDialogHeader.setVisibility(View.GONE);
				if (_hSelectedContainer instanceof Dialog)
				{
					Dialog dialog = (Dialog) _hSelectedContainer;
					__GetMainActivity().GetManager().GoToUser(dialog.GetUserID());
				}
				else if (_hSelectedContainer instanceof Chat)
				{
					Chat chat = (Chat) _hSelectedContainer;
					__GetMainActivity().GetManager().GoToGroup(chat);
				}
				else if (_hSelectedContainer instanceof Delivery)
				{
					Delivery delivery = (Delivery) _hSelectedContainer;
					__GetMainActivity().GetManager().GoToDelivery(delivery);
				}
				_hSelectedContainer = null;
				break;
			case R.id.tvChatsDialogHeaderChats:
			case R.id.tvChatsDialogHeaderArchive:
			case R.id.tvChatsDialogHeaderSecret:
			case R.id.tvChatsDialogHeaderNotreaded:
			case R.id.tvChatsDialogHeaderRemove:
				_rlDialogHeader.setVisibility(View.GONE);		
				DB db = new DB(__GetMainActivity());
				SQLiteDatabase writer = db.getWritableDatabase();
				switch (pView.getId())
				{
					case R.id.tvChatsDialogHeaderChats:
						_hSelectedContainer.SetType(AbstractMessageContainer.MAIN, writer);
						break;
					case R.id.tvChatsDialogHeaderArchive:
						_hSelectedContainer.SetType(AbstractMessageContainer.ARCHIVE, writer);
						break;
					case R.id.tvChatsDialogHeaderSecret:
						_hSelectedContainer.SetType(AbstractMessageContainer.SECRET, writer);
						break;
					case R.id.tvChatsDialogHeaderNotreaded:
						if (_hSelectedContainer instanceof Dialog)
						{
							Dialog dialog = (Dialog) _hSelectedContainer;
							dialog.SetUnreadFlag(!dialog.IsUnReadFlag(), writer);
						}
						else if (_hSelectedContainer instanceof Chat)
						{
							Chat chat = (Chat) _hSelectedContainer;
							chat.SetUnreadFlag(!chat.IsUnReadFlag(), writer);
						}
						break;
					case R.id.tvChatsDialogHeaderRemove:
						if (_hSelectedContainer instanceof Dialog)
						{
							try
							{
								Dialog dialog = (Dialog) _hSelectedContainer;
								JSONObject options = new JSONObject();
								options.put("userid", dialog.GetUserID());
								__SendToServer("message.clearhistory", options);
							}
							catch (JSONException e)
							{
							}
						}
						else if (_hSelectedContainer instanceof Chat)
						{
							try
							{
								Chat chat = (Chat) _hSelectedContainer;
								JSONObject options = new JSONObject();
								options.put("id", chat.GetID());
								__SendToServer("groupchat.delete", options);
							}
							catch (JSONException e)
							{
							}
						}
						else if (_hSelectedContainer instanceof Delivery)
						{
							try
							{
								Delivery delivery = (Delivery) _hSelectedContainer;
								JSONObject options = new JSONObject();
								options.put("id", delivery.GetID());
								__SendToServer("mailing.delete", options);
							}
							catch (JSONException e)
							{
							}
						}
						break;
				}
				_hSelectedContainer = null;
				_PagerAdapter.Update();
				writer.close();
				db.close();
				break;
		}
	}
	
	@Override
	public void OnTextChanged(int pID, String pText)
	{
		if (_PagerAdapter != null)
		{
			_PagerAdapter.SetFilter(pText);
		}
	}
	
	@Override
	protected boolean __OnBackPressed()
	{
		if (_rlDialogHeader.getVisibility() == View.VISIBLE)
		{
			_rlDialogHeader.setVisibility(View.GONE);
			_hSelectedContainer = null;
			return false;
		}
		else if (_rlDialogNew.getVisibility() == View.VISIBLE)
		{
			_rlDialogNew.setVisibility(View.GONE);
			return false;
		}
		return true;
	}
	
	@Override
	protected void __OnHeadersChanged(ArrayList<AbstractMessageContainer<?>> pChangedContainers)
	{
		_PagerAdapter.Update();
	}
	@Override
	protected void __OnContactsChanged()
	{
		_PagerAdapter.Update();
	}
	@Override
	protected void __OnUsersChanged(LongSparseArray<User> pUsers)
	{
		_PagerAdapter.Update();
	}

	public void Click(AbstractMessageContainer<?> pContainer)
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(_etSearch.getWindowToken(), 0);
	    
		if (pContainer instanceof Dialog)
		{
			Dialog dialog = (Dialog) pContainer;
			__GetMainActivity().GetManager().GoToDialog(dialog.GetUserID(), true, false);
		}
		else if (pContainer instanceof Chat)
		{
			Chat chat = (Chat) pContainer;
			__GetMainActivity().GetManager().GoToChat(chat, true, false);
		}
		else if (pContainer instanceof Delivery)
		{
			Delivery delivery = (Delivery) pContainer;
			__GetMainActivity().GetManager().GoToDeliveryChat(delivery, true, false);
		}
	}
	public void LongClick(AbstractMessageContainer<?> pContainer, int pType)
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(_etSearch.getWindowToken(), 0);
	    
		if (pType == AbstractMessageContainer.MAIN)
		{
			_tvChats.setVisibility(View.GONE);
			_tvArchive.setVisibility(View.VISIBLE);
			_tvSecret.setVisibility(View.VISIBLE);
		}
		else if (pType == AbstractMessageContainer.ARCHIVE)
		{
			_tvChats.setVisibility(View.VISIBLE);
			_tvArchive.setVisibility(View.GONE);
			_tvSecret.setVisibility(View.VISIBLE);
		}
		else if (pType == AbstractMessageContainer.SECRET)
		{
			_tvChats.setVisibility(View.VISIBLE);
			_tvArchive.setVisibility(View.VISIBLE);
			_tvSecret.setVisibility(View.GONE);
		}
		
		if (pContainer instanceof Delivery)
		{
			_tvNotReaded.setVisibility(View.GONE);
		}
		else if (pContainer instanceof Dialog)
		{
			Dialog dialog = (Dialog) pContainer;
			if (dialog.GetUnReadedCount() > 0)
			{
				_tvNotReaded.setVisibility(View.GONE);
			}
			else
			{
				if (dialog.IsUnReadFlag())
				{
					_tvNotReaded.setText(R.string.ChatsCancelNotReaded);
				}
				else
				{
					_tvNotReaded.setText(R.string.ChatsNotReaded);
				}
				_tvNotReaded.setVisibility(View.VISIBLE);
			}
		}
		else if (pContainer instanceof Chat)
		{
			Chat chat = (Chat) pContainer;
			if (chat.GetUnReadedCount() > 0)
			{
				_tvNotReaded.setVisibility(View.GONE);
			}
			else
			{
				if (chat.IsUnReadFlag())
				{
					_tvNotReaded.setText(R.string.ChatsCancelNotReaded);
				}
				else
				{
					_tvNotReaded.setText(R.string.ChatsNotReaded);
				}
				_tvNotReaded.setVisibility(View.VISIBLE);
			}
		}
		_rlDialogHeader.setVisibility(View.VISIBLE);
		_hSelectedContainer = pContainer;
	}
}