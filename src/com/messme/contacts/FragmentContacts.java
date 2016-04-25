package com.messme.contacts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.contacts.AdapterContacts.Item;
import com.messme.user.User;
import com.messme.view.MessmeFragment;
import com.messme.view.TextWatcher;
import com.messme.view.TextWatcher.iOnTextChanged;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v4.util.LongSparseArray;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;


public class FragmentContacts extends MessmeFragment implements OnClickListener, OnItemClickListener, iOnTextChanged, OnItemLongClickListener
{
	private LinearLayout _llTitle = null;
	private LinearLayout _llSearch = null;
	private EditText _etSearch = null;
	private ListView _lv = null;
	
	private RelativeLayout _rlDialog = null;
	
	private AdapterContacts _Adapter;
	private TextWatcher _SearchTextWatcher = null;
	
	private long _SelectedID = 0;

	
	public FragmentContacts(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
		
		_SearchTextWatcher = new TextWatcher(this, 1);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		_Adapter = new AdapterContacts(__GetMainActivity());
		
		View view = pInflater.inflate(R.layout.contacts, pContainer, false);
		
		view.findViewById(R.id.ivContactsMenu).setOnClickListener(this);

		_llTitle = (LinearLayout) view.findViewById(R.id.llContactsTitle);
		view.findViewById(R.id.ivContactsSearchBack).setOnClickListener(this);
		view.findViewById(R.id.ivContactsSearch).setOnClickListener(this);

		_llSearch = (LinearLayout) view.findViewById(R.id.llContactsSearch);
		_etSearch = (EditText) view.findViewById(R.id.etContactsSearch);
		_etSearch.addTextChangedListener(_SearchTextWatcher);
		view.findViewById(R.id.ivContactsClose).setOnClickListener(this);
		
		view.findViewById(R.id.llContactsShadow).bringToFront();
		
		_lv = (ListView) view.findViewById(R.id.lvContacts);
		LayoutInflater ltInflater = getActivity().getLayoutInflater();
        View headerView = ltInflater.inflate(R.layout.empty_listitem_header, null, false);
        _lv.addHeaderView(headerView);
		_lv.setOnItemClickListener(this);
		_lv.setAdapter(_Adapter);
		_lv.setOnItemLongClickListener(this);
		
		view.findViewById(R.id.ivContactsAdd).setOnClickListener(this);
		view.findViewById(R.id.ivContactsAdd).bringToFront();
		
		_rlDialog = (RelativeLayout) view.findViewById(R.id.rlContactsDialog);
		_rlDialog.setVisibility(View.GONE);
		_rlDialog.setOnClickListener(this);
		view.findViewById(R.id.llContactsDialogMessages).setOnClickListener(this);
		view.findViewById(R.id.llContactsDialogInfo).setOnClickListener(this);
		view.findViewById(R.id.llContactsDialogDelete).setOnClickListener(this);
		
		return view;
	}
	
	@Override
	protected void __OnResume()
	{
		if (_etSearch.getText().length() > 0 && getView() != null && getView().findViewById(R.id.ivContactsSearch) != null)
		{
			getView().findViewById(R.id.ivContactsSearch).callOnClick();
		}
	}

	@Override
	protected void __OnDestroy()
	{
		_etSearch.removeTextChangedListener(_SearchTextWatcher);
	}

	@Override
	public void onClick(View pView)
	{	    
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(_etSearch.getWindowToken(), 0);
		switch (pView.getId())
		{
			case R.id.ivContactsMenu:
				__GetMainActivity().MenuOpen();
				break;
			case R.id.ivContactsSearch:
				_llTitle.setVisibility(View.GONE);
				_llSearch.setVisibility(View.VISIBLE);
				_etSearch.setVisibility(View.VISIBLE);
				_etSearch.setEnabled(true);
				if (_etSearch.requestFocus())
				{
					imm.showSoftInput(_etSearch, InputMethodManager.SHOW_IMPLICIT);
				}
				break;
			case R.id.ivContactsSearchBack:
			case R.id.ivContactsClose:
				_llTitle.setVisibility(View.VISIBLE);
				_llSearch.setVisibility(View.GONE);
				if (pView.getId() == R.id.ivContactsClose)
				{
					_etSearch.setText("");
				}
				break;
			case R.id.ivContactsAdd:
				Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
				startActivity(intent);
				break;
				
			case R.id.rlContactsDialog:
			case R.id.llContactsDialogMessages:
			case R.id.llContactsDialogInfo:
			case R.id.llContactsDialogDelete:
				_rlDialog.setVisibility(View.GONE);
				switch (pView.getId())
				{
					case R.id.llContactsDialogMessages:
						__GetMainActivity().GetManager().GoToDialog(_SelectedID, false, false);
						break;
					case R.id.llContactsDialogInfo:
						__GetMainActivity().GetManager().GoToUser(_SelectedID);
						break;
					case R.id.llContactsDialogDelete:
						try
						{
							JSONObject options = new JSONObject();
							JSONArray userlist = new JSONArray();
							userlist.put(_SelectedID);
							options.put("userlist", userlist);
							__SendToServer("user.removefriend", options);
							__GetMainActivity().GetUsers().GetUser(_SelectedID).IsFriend = false;
							_Adapter.notifyDataSetChanged();
						}
						catch (JSONException e)
						{
						}
						break;
				}
				_SelectedID = 0;
				break;
		}
	}

	@Override
	protected boolean __OnBackPressed()
	{
		if (_rlDialog.getVisibility() == View.VISIBLE)
		{
			_rlDialog.setVisibility(View.GONE);
			return false;
		}
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pPosition, long arg3)
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(_etSearch.getWindowToken(), 0);
		pPosition--;
		if (pPosition >= 0)
		{
			if (_Adapter.getItem(pPosition).Type == AdapterContacts.TYPE_FRIEND)
			{
				User friend = _Adapter.getItem(pPosition).hFriend;
				if (friend != null)
				{
					__GetMainActivity().GetManager().GoToDialog(friend.Id, false, false);
				}
			}
			else if (_Adapter.getItem(pPosition).Type == AdapterContacts.TYPE_USER)
			{
				User user = ((Item)_Adapter.getItem(pPosition)).hUser;
				if (user != null)
				{
					user = __GetMainActivity().GetUsers().AddUser(user, __GetMainActivity());
					__GetMainActivity().GetManager().GoToDialog(user.Id, false, false);
				}
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> pParent, View pView, int pPosition, long pID)
	{
		pPosition--;
		if (pPosition >= 0)
		{
			if (_Adapter.getItem(pPosition).Type == AdapterContacts.TYPE_FRIEND)
			{
				User friend = _Adapter.getItem(pPosition).hFriend;
				if (friend != null)
				{
					_SelectedID = friend.Id;
					_rlDialog.setVisibility(View.VISIBLE);
				}
			}
		}
		
		return true;
	}
	
	@Override
	protected void __OnContactsChanged()
	{
		_Adapter.notifyDataSetChanged();
	}
	@Override
	protected void __OnUsersChanged(LongSparseArray<User> pUser)
	{
		_Adapter.notifyDataSetChanged();
	}

	@Override
	public void OnTextChanged(int pID, String pText)
	{
		_Adapter.SetFilter(pText);
		if (pText.length() >= 5)
		{
			try
			{
				JSONObject options = new JSONObject();
				options.put("userlist", new JSONArray());
				options.put("locale", __GetMainActivity().GetProfile().GetLocale());
				options.put("status", -1);
				options.put("isfriend", 0);
				options.put("country", 0);
				options.put("city", 0);
				options.put("sex", "");
				options.put("minage", 0);
				options.put("maxage", 0);
				options.put("mask", _Adapter.GetFilter());
				options.put("lat1", 0);
				options.put("lng1", 0);
				options.put("lat2", 0);
				options.put("lng2", 0);
				__SendToServer("user.list", options);
			}
			catch (JSONException e)
			{
			}
		}
		else
		{
			_Adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	protected void __OnMessageReceived(String pMessageId, String pAction, int pStatus, String pResult, JSONObject pOptions)
	{
		if (pAction.equals("user.list") && pStatus == 1000)
		{
			try
			{
				JSONArray users = new JSONArray(pResult);
				for (int i = 0; i < users.length(); i++)
				{
					User user = new User(users.getJSONObject(i));
					_Adapter.AddUser(user);
				}
			}
			catch (JSONException e)
			{
			}
			_Adapter.notifyDataSetChanged();
		}
	}
}