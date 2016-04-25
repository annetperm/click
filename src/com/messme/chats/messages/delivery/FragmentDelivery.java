package com.messme.chats.messages.delivery;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.user.Users;
import com.messme.util.DateUtil;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class FragmentDelivery extends MessmeFragment implements OnClickListener, iOnTextChanged
{
	private static final int _NAME			= 100;
	private static final int _FIRST			= 101;

	private EditText _etName = null;
	private TextView _tvDate = null;
	private ListView _lv = null;
	private LinearLayout _llAddUser = null;
	private RelativeLayout _rlProgress = null;
	
	private RelativeLayout _rlDialogChanges = null;
	
	private AdapterDelivery _Adapter = null;
	
	private TextWatcher _NameTextWatcher;
	
	private Delivery _hDelivery;
	private String _Name;
	
	private boolean _Canceled = false;

	
	public FragmentDelivery(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
		_NameTextWatcher = new TextWatcher(this, 1);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		if (pStore.get(DELIVERY_ID) != null)
		{
			_hDelivery = __GetMainActivity().GetDeliveries().GetDelivery(pStore.get(DELIVERY_ID));
		}
		else
		{
			_hDelivery = null;
		}
		_Name = pStore.get(_NAME) == null ? _hDelivery == null ? __GetMainActivity().GetProfile().GetDeliveryIndex() : _hDelivery.GetName() : pStore.get(_NAME);
		ArrayList<Long> users = new ArrayList<Long>();
		if (pStore.get(COUNT) != null)
		{
			int count = Integer.parseInt(pStore.get(COUNT));
			pStore.remove(COUNT);
			for (int i = 0; i < count; i++)
			{
				long id = Long.parseLong(pStore.get(1000 + i));
				pStore.remove(1000 + i);
				users.add(id);
			}
		}
		else
		{
			if (_hDelivery != null)
			{
				users = _hDelivery.GetUsers();
			}
		}
		boolean first;
		if (pStore.get(_FIRST) == null)
		{
			first = true;
		}
		else
		{
			first = false;
		}
		
		
		View view = pInflater.inflate(R.layout.delivery, pContainer, false);
		
		view.findViewById(R.id.ivDeliveryBack).setOnClickListener(this);
		
		view.findViewById(R.id.llDeliveryShadow).bringToFront();
		
		LayoutInflater ltInflater = getActivity().getLayoutInflater();
        View headerView = ltInflater.inflate(R.layout.delivery_header, null, false);
        
        _llAddUser = (LinearLayout) headerView.findViewById(R.id.llDeliveryAdd);
		_llAddUser.setOnClickListener(this);
        
		_lv = (ListView) view.findViewById(R.id.lvDelivery);
		_lv.addHeaderView(headerView);

		_tvDate = (TextView) headerView.findViewById(R.id.tvDeliveryDateValue);
		
		_etName = (EditText) headerView.findViewById(R.id.etDeliveryName);
		_etName.addTextChangedListener(_NameTextWatcher);
		_etName.setText(_Name);
		
		view.findViewById(R.id.ivDeliverySend).setOnClickListener(this);
		view.findViewById(R.id.ivDeliverySend).bringToFront();
		
		_rlProgress = (RelativeLayout) view.findViewById(R.id.rlDeliveryProgress);
		_rlProgress.bringToFront();
		_rlProgress.setVisibility(View.GONE);
		
		_rlDialogChanges = (RelativeLayout) view.findViewById(R.id.rlDeliveryChanges);
		_rlDialogChanges.setVisibility(View.GONE);
		_rlDialogChanges.setOnClickListener(this);
		view.findViewById(R.id.btnDeliveryChangesNo).setOnClickListener(this);
		view.findViewById(R.id.btnDeliveryChangesYes).setOnClickListener(this);

		_Adapter = new AdapterDelivery(__GetMainActivity());
		for (int i = 0; i < users.size(); i++)
		{
			_Adapter.AddUser(users.get(i));
		}
		_lv.setAdapter(_Adapter);
		
		if (_hDelivery == null)
		{
			_tvDate.setText(DateUtil.GetDateTime());
			
			_llAddUser.setVisibility(View.VISIBLE);
		}
		else
		{
			_tvDate.setText(_hDelivery.GetDateTime());
			
			if (_hDelivery.GetUsers().size() == 0)
			{
				_llAddUser.setVisibility(View.GONE);
				
				try
				{
					JSONObject options = new JSONObject();
					options.put("id", _hDelivery.GetID());
					__SendToServer("mailing.info", options);
				}
				catch (JSONException e)
				{
				}
			}
			else
			{
				_llAddUser.setVisibility(View.VISIBLE);
			}
			
			if (first)
			{
			}
			else
			{
				try
				{
					ArrayList<Long> empty = _hDelivery.GetEmptyUsers(__GetMainActivity().GetUsers());
					if (empty.size() > 0)
					{
						__SendToServer("user.list", Users.GetUsersOptions(__GetMainActivity(), empty));
					}
				}
				catch (JSONException e)
				{
				}
			}
		}
		
		
		return view;
	}
	
	@Override
	protected void __OnSave(SparseArray<String> pStore)
	{
		pStore.put(_FIRST, "not");
		pStore.put(_NAME, _Name);
		pStore.append(MessmeFragment.COUNT, Integer.toString(_Adapter.getCount()));
		for (int i = 0; i < _Adapter.getCount(); i++)
		{
			pStore.put(1000 + i, Long.toString(_Adapter.getItemId(i)));
		}
	}
	
	@Override
	protected void __OnDestroy()
	{
		_etName.removeTextChangedListener(_NameTextWatcher);
	}
	
	@Override
	protected boolean __OnBackPressed()
	{
		if (_Canceled)
		{
			return true;
		}
		if (_CheckChanges())
		{
			_rlDialogChanges.setVisibility(View.VISIBLE);
			return false;
		}
		return true;
	}

	@Override
	public void onClick(View pView)
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(_etName.getWindowToken(), 0);
		switch (pView.getId())
		{
			case R.id.ivDeliveryBack:
				if (_CheckChanges())
				{
					_rlDialogChanges.setVisibility(View.VISIBLE);
				}
				else
				{
					__GetMainActivity().GetManager().GoToBack();
				}
				break;
			case R.id.llDeliveryAdd:
				if (__GetMainActivity().GetUsers().GetFriends().size() == 0)
				{
					__GetMainActivity().GetManager().GoToContacts();
				}
				else
				{
					__GetMainActivity().GetManager().GoToSelect(_Adapter.GetItems(), false);
				}
				break;
			case R.id.ivDeliverySend:
				_Update();
				break;
				
			case R.id.rlGroupChanges:
				_rlDialogChanges.setVisibility(View.GONE);
				break;
			case R.id.btnGroupChangesNo:
				_Canceled = true;
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.btnGroupChangesYes:
				_rlDialogChanges.setVisibility(View.GONE);
				_Update();
				break;
		}
	}

	@Override
	protected void __OnErrorSended(String pMessageId, String pAction, JSONObject pOptions)
	{
		_rlProgress.setVisibility(View.GONE);
		if (pAction.equals("mailing.info"))
		{
			__GetMainActivity().OpenDialog(3, __GetMainActivity().getString(R.string.Dialog2Title), __GetMainActivity().getString(R.string.Dialog2Description));
		}
		if (pAction.equals("user.list"))
		{
			__GetMainActivity().OpenDialog(4, __GetMainActivity().getString(R.string.Dialog3Title), __GetMainActivity().getString(R.string.Dialog3Description));
		}
		if (pAction.equals("mailing.save"))
		{
			__GetMainActivity().OpenDialog(2, __GetMainActivity().getString(R.string.Dialog14Title), __GetMainActivity().getString(R.string.Dialog14Description));
		}
		else if (pAction.equals("user.list"))
		{
			__GetMainActivity().OpenDialog(4, __GetMainActivity().getString(R.string.Dialog3Title), __GetMainActivity().getString(R.string.Dialog3Description));
		}			
	}
	
	@Override
	protected void __OnMessageReceived(String pMessageId, String pAction, int pStatus, String pResult, JSONObject pOptions)
	{
		if (pAction.equals("mailing.info"))
		{
			try
			{
				if (pStatus != 1000)
				{
					throw new JSONException("");
				}
				
				JSONObject result = new JSONObject(pResult);
				_hDelivery.Update(result, __GetMainActivity().GetProfile().GetID());

				_tvDate.setText(_hDelivery.GetDateTime());
				_etName.setText(_hDelivery.GetName());
				_llAddUser.setVisibility(View.VISIBLE);

				_Adapter = new AdapterDelivery(__GetMainActivity());
				for (int i = 0; i < _hDelivery.GetUsers().size(); i++)
				{
					_Adapter.AddUser(_hDelivery.GetUsers().get(i));
				}
				
				ArrayList<Long> empty = _hDelivery.GetEmptyUsers(__GetMainActivity().GetUsers());
				if (empty.size() > 0)
				{
					__SendToServer("user.list", Users.GetUsersOptions(__GetMainActivity(), empty));
				}
				
				_lv.setAdapter(_Adapter);
			}
			catch (JSONException e)
			{
				__GetMainActivity().OpenDialog(3, __GetMainActivity().getString(R.string.Dialog5Title), __GetMainActivity().getString(R.string.Dialog5Description));
			}
		}
		else if (pAction.equals("user.list"))
		{
			try
			{
				if (pStatus != 1000)
				{
					throw new JSONException("");
				}
				// добавили новых пользователей
				JSONArray usersJSON = new JSONArray(pResult);
				__GetMainActivity().GetUsers().AddUsers(usersJSON, __GetMainActivity());
				_Adapter.notifyDataSetChanged();
			}
			catch (JSONException e)
			{
				__GetMainActivity().OpenDialog(6, __GetMainActivity().getString(R.string.Dialog7Title), __GetMainActivity().getString(R.string.Dialog7Description));
			}
		}
		else if (pAction.equals("mailing.save"))
		{
			try
			{
				if (pStatus != 1000)
				{
					throw new JSONException("");
				}
				
				JSONObject result = new JSONObject(pResult);
				_hDelivery = __GetMainActivity().GetDeliveries().GetDelivery(result.getString("id"));
				_hDelivery.Update(result, __GetMainActivity().GetProfile().GetID());

				__GetMainActivity().GetManager().GoToDeliveryChat(_hDelivery, true, true);
			}
			catch (JSONException e)
			{
				_rlProgress.setVisibility(View.GONE);
				__GetMainActivity().OpenDialog(3, __GetMainActivity().getString(R.string.Dialog15Title), __GetMainActivity().getString(R.string.Dialog15Description));
			}
		}
	}

	@Override
	public void OnTextChanged(int pID, String pText)
	{
		switch (pID)
		{
			case 1:
				_Name = pText;
				break;
		}
	}

	private boolean _CheckChanges()
	{
		if (_hDelivery == null)
		{
			return false;
		}
		else if (_Adapter.Contains(_hDelivery.GetUsers()) 
				&& _hDelivery.GetName().equals(_Name))
		{
			return false;
		}
		return true;
	}
	
	private void _Update()
	{
		if (_Adapter.getCount() < 2)
		{
			__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog9Title), __GetMainActivity().getString(R.string.Dialog9Description));
		}
		else
		{
			if (_hDelivery == null)
			{
				// создание
				try
				{
					JSONObject options = new JSONObject();
					options.put("id", "");
					if (_Name.length() == 0)
					{
						options.put("name", __GetMainActivity().GetProfile().GetDeliveryIndex());
					}
					else
					{
						options.put("name", _Name);
					}
					options.put("avatar", "");
					JSONArray userlist = new JSONArray();
					for (int i = 0; i < _Adapter.getCount(); i++)
					{
						userlist.put(_Adapter.getItemId(i));
					}
					options.put("userlist", userlist);
					_rlProgress.setVisibility(View.VISIBLE);
					__SendToServer("mailing.save", options);
				}
				catch (JSONException e)
				{
				}
			}
			else if (_Adapter.Contains(_hDelivery.GetUsers()) 
					&& _hDelivery.GetName().equals(_Name))
			{
				// не изменилось
				__GetMainActivity().GetManager().GoToDeliveryChat(_hDelivery, true, true);
			}
			else
			{
				// изменилось
				try
				{
					JSONObject options = new JSONObject();
					options.put("id", _hDelivery.GetID());
					options.put("name", _Name);
					options.put("avatar", "");
					JSONArray userlist = new JSONArray();
					for (int i = 0; i < _Adapter.getCount(); i++)
					{
						userlist.put(_Adapter.getItemId(i));
					}
					options.put("userlist", userlist);
					_rlProgress.setVisibility(View.VISIBLE);
					__SendToServer("mailing.save", options);
				}
				catch (JSONException e)
				{
				}
			}
		}
	}
}