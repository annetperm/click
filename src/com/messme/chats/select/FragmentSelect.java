package com.messme.chats.select;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.user.User;
import com.messme.view.MessmeFragment;
import com.messme.view.TextWatcher;
import com.messme.view.TextWatcher.iOnTextChanged;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;


public class FragmentSelect extends MessmeFragment implements OnClickListener, OnItemClickListener, iOnTextChanged
{
	private LinearLayout _llTitle = null;
	private LinearLayout _llSearch = null;
	private EditText _etSearch = null;
	private ListView _lv = null;
	private ImageView _ivOk = null;

	private TextWatcher _SearchTextWatcher = null;
	private AdapterSelect _Adapter = null;
	private boolean _ForDialog = false;
	private int _Count = 0;
	
	public FragmentSelect(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
		_SearchTextWatcher = new TextWatcher(this, 1);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		int type = Integer.parseInt(pStore.get(TYPE));
		ArrayList<Long> selected = new ArrayList<Long>();
		if (pStore.get(COUNT) != null)
		{
			_Count = Integer.parseInt(pStore.get(COUNT));
			for (int i = 0; i < _Count; i++)
			{
				long id = Long.parseLong(pStore.get(1000 + i));
				selected.add(id);
			}
			_ForDialog = false;
		}
		else
		{
			_ForDialog = true;
		}
		_Adapter = new AdapterSelect(__GetMainActivity(), selected);
		
		
		View view = pInflater.inflate(R.layout.select, pContainer, false);

		view.findViewById(R.id.ivSelectBack).setOnClickListener(this);
		
		_ivOk = (ImageView) view.findViewById(R.id.ivSelectOk);
		_ivOk.setOnClickListener(this);

		_llTitle = (LinearLayout) view.findViewById(R.id.llSelectTitle);
		view.findViewById(R.id.ivSelectSearchBack).setOnClickListener(this);
		view.findViewById(R.id.ivSelectSearch).setOnClickListener(this);

		_llSearch = (LinearLayout) view.findViewById(R.id.llSelectSearch);
		_etSearch = (EditText) view.findViewById(R.id.etSelectSearch);
		_etSearch.addTextChangedListener(_SearchTextWatcher);
		view.findViewById(R.id.ivSelectClose).setOnClickListener(this);
		
		view.findViewById(R.id.llSelectShadow).bringToFront();
		
		_lv = (ListView) view.findViewById(R.id.lvSelect);
		_lv.setAdapter(_Adapter);
		_lv.setOnItemClickListener(this);
		
		switch (type)
		{
			case 0:
				view.findViewById(R.id.ivSelectSearch).setOnClickListener(this);
				((TextView) view.findViewById(R.id.tvSelect)).setText(R.string.SelectText0);
				break;
			case 1:
				view.findViewById(R.id.ivSelectSearch).setVisibility(View.GONE);
				((TextView) view.findViewById(R.id.tvSelect)).setText(R.string.SelectText1);
				break;
			case 2:
				view.findViewById(R.id.ivSelectSearch).setVisibility(View.GONE);
				((TextView) view.findViewById(R.id.tvSelect)).setText(R.string.SelectText2);
				break;
		}
		
		return view;
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
			case R.id.ivSelectBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.ivSelectSearch:
				_llTitle.setVisibility(View.GONE);
				_llSearch.setVisibility(View.VISIBLE);
				_etSearch.setVisibility(View.VISIBLE);
				_etSearch.setEnabled(true);
				if (_etSearch.requestFocus())
				{
					imm.showSoftInput(_etSearch, InputMethodManager.SHOW_IMPLICIT);
				}
				break;
			case R.id.ivSelectSearchBack:
			case R.id.ivSelectClose:
				_llTitle.setVisibility(View.VISIBLE);
				_llSearch.setVisibility(View.GONE);
				if (pView.getId() == R.id.ivSelectClose)
				{
					_etSearch.setText("");
				}
				break;
			case R.id.ivSelectOk:
				SparseArray<String> store = new SparseArray<String>();
				ArrayList<User> selectedUser = _Adapter.GetSelected();
				store.put(COUNT, Integer.toString(selectedUser.size() + _Count));
				for (int i = 0; i < selectedUser.size(); i++)
				{
					User user = __GetMainActivity().GetUsers().AddUser(selectedUser.get(i), __GetMainActivity());
					store.append(1000 + i + _Count, Long.toString(user.Id));
				}
				__GetMainActivity().GetManager().GoToBackWithResult(store);
				break;
		}
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View pView, final int pPosition, long arg3)
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(_etSearch.getWindowToken(), 0);
		_Adapter.SetSelected(pPosition);
		if (_ForDialog)
		{
			if (_Adapter.getItem(pPosition) != null)
			{
				getView().post(new Runnable()
				{
					@Override
					public void run()
					{
						__GetMainActivity().runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								User user = __GetMainActivity().GetUsers().AddUser(_Adapter.getItem(pPosition), __GetMainActivity());
								__GetMainActivity().GetManager().GoToDialog(user.Id, false, true);
							}
						});
					}
				});
			}
		}
		else
		{
			if (_Adapter.GetSelectedCount() == 0)
			{
				_ivOk.setVisibility(View.GONE);
			}
			else
			{
				_ivOk.setVisibility(View.VISIBLE);
			}
		}
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
	protected void __OnErrorSended(String pMessageId, String pAction, JSONObject pOptions)
	{
		if (pAction.equals("user.list"))
		{
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