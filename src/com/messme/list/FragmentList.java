package com.messme.list;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.SwipeLayout.DragEdge;
import com.daimajia.swipe.SwipeLayout.Status;
import com.florescu.android.rangeseekbar.RangeSeekBar;
import com.messme.ActivityMain;
import com.messme.R;
import com.messme.countries.Countries.Country;
import com.messme.user.User;
import com.messme.view.MessmeFragment;
import com.messme.view.TextWatcher;
import com.messme.view.TextWatcher.iOnTextChanged;

import android.content.Context;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class FragmentList extends MessmeFragment implements OnClickListener, iOnTextChanged
{
	private static final int _FILTER_MINAGE = 200;
	private static final int _FILTER_MAXAGE = 201;
	private static final int _FILTER_SEX = 202;
	private static final int _FILTER_COUNTRY = 203;
	private static final int _FILTER_CITY = 204;
	private static final int _FILTER_CITYNAME = 205;
	private static final int _FILTER_FRIEND = 206;
	private static final int _FILTER_MASK = 207;
	
	private static final int _LIST_COUNT = 208;
	
	
	private ImageView _ivCancel = null;
	private ImageView _ivOk = null;
	private ImageView _ivSearch = null;
	private ImageView _ivFilter= null;
	private LinearLayout _llSearch1 = null;
	private LinearLayout _llSearch2 = null;
	private TextView _tvSex = null;
	private TextView _tvAge = null;
	private ImageView _ivCountry = null;
	private TextView _tvCountry = null;
	private ImageView _ivCountryNext = null;
	private TextView _tvCity = null;
	private ImageView _ivCityNext = null;
	private TextView _tvMask = null;
	private EditText _etMask = null;
	private SwipeLayout _slFilter = null;
	private LinearLayout _llSearch = null;
	private ListView _lv = null;
	
	private RelativeLayout _rlDialogSex = null;
	private ImageView _ivDialogSexMale = null;
	private ImageView _ivDialogSexFemale = null;
	private ImageView _ivDialogSexNot = null;
	
	private RelativeLayout _rlDialogAge = null;
	private RangeSeekBar<Integer> _sbDialogAge = null;
	
	private LongSparseArray<User> _Users = new LongSparseArray<User>();
	private AdapterList _Adapter = null;

	private TextWatcher _MaskTextWatcher = null;
	
	class Filter
	{
		int MinAge = -1;
		int MaxAge = -1;
		String Sex = "";
		int CountryId = 0;
		int CityId = 0;
		String CityName = "";
		boolean Friend = false;
		String Mask = "";
	}
	
	private Filter _FilterMain = new Filter();
	private Filter _FilterTemp = null;
	private int _SelectedCountry = -1;
	private int _SelectedCity = -1;
	private String _SelectedName = "";
	
	private int _Count = 0;

	
	public FragmentList(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
		_MaskTextWatcher = new TextWatcher(this, 1);
	}

	
	@SuppressWarnings("unchecked")
	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		if (pStore.get(_FILTER_MINAGE) != null)
		{
			_FilterMain.MinAge = Integer.parseInt(pStore.get(_FILTER_MINAGE));
			_FilterMain.MaxAge = Integer.parseInt(pStore.get(_FILTER_MAXAGE));
			_FilterMain.Sex = pStore.get(_FILTER_SEX);
			_FilterMain.CountryId = Integer.parseInt(pStore.get(_FILTER_COUNTRY));
			_FilterMain.CityId = Integer.parseInt(pStore.get(_FILTER_CITY));
			_FilterMain.CityName = pStore.get(_FILTER_CITYNAME);
			_FilterMain.Friend = Boolean.parseBoolean(pStore.get(_FILTER_FRIEND));
			_FilterMain.Mask = pStore.get(_FILTER_MASK);
		}
		
		if (pStore.get(_LIST_COUNT) != null)
		{
			_Count = Integer.parseInt(pStore.get(_LIST_COUNT));
		}
		else
		{
			_Count = 10;
		}
		
		_Adapter = new AdapterList(__GetMainActivity(), _Users);
		if (_Count != _Users.size())
		{
			// значит нужно грузить данные
			_GetUsers();
		}
		
		View view = pInflater.inflate(R.layout.list, pContainer, false);
	
		view.findViewById(R.id.ivListMenu).setOnClickListener(this);
		
		_ivCancel = (ImageView) view.findViewById(R.id.ivListCancel);
		_ivCancel.setOnClickListener(this);
		_ivOk = (ImageView) view.findViewById(R.id.ivListOk);
		_ivOk.setOnClickListener(this);
		
		_ivSearch = (ImageView) view.findViewById(R.id.ivListSearch);
		_ivSearch.setOnClickListener(this);
		_ivFilter = (ImageView) view.findViewById(R.id.ivListFilter);
		_ivFilter.setOnClickListener(this);
		
		_llSearch = (LinearLayout) view.findViewById(R.id.llListSearch);
		
		_slFilter = (SwipeLayout) view.findViewById(R.id.slList);
		_slFilter.setSwipeEnabled(false);
		_slFilter.addSwipeListener(new SwipeLayout.SwipeListener()
		{
			@Override
			public void onUpdate(SwipeLayout layout, int leftOffset, final int topOffset)
			{
			}
			@Override
			public void onStartOpen(SwipeLayout layout)
			{
			    _etMask.setVisibility(View.GONE);
			    _tvMask.setText(_etMask.getText());
			    _tvMask.setVisibility(View.VISIBLE);
			}
			@Override
			public void onStartClose(SwipeLayout layout)
			{
			}
			@Override
			public void onOpen(SwipeLayout layout)
			{
			}
			@Override
			public void onHandRelease(SwipeLayout layout, float xvel, float yvel)
			{
			}
			@Override
			public void onClose(SwipeLayout layout)
			{
			    _etMask.setVisibility(View.GONE);
			    _tvMask.setText(_etMask.getText());
			    _tvMask.setVisibility(View.VISIBLE);
			}
		});
		
		view.findViewById(R.id.llListAge).setOnClickListener(this);
		_tvAge = (TextView) view.findViewById(R.id.tvListAge);
		
		view.findViewById(R.id.llListSex).setOnClickListener(this);
		_tvSex = (TextView) view.findViewById(R.id.tvListSex);
		
		view.findViewById(R.id.llListPlace).setOnClickListener(this);
		_ivCountry = (ImageView) view.findViewById(R.id.ivListPlace);
		_tvCountry = (TextView) view.findViewById(R.id.tvListPlace);
		_ivCountryNext = (ImageView) view.findViewById(R.id.ivListPlaceNext);
		
		view.findViewById(R.id.llListCity).setOnClickListener(this);
		_tvCity = (TextView) view.findViewById(R.id.tvListCity);
		_ivCityNext = (ImageView) view.findViewById(R.id.ivListCityNext);
		
		view.findViewById(R.id.btnListSearch1).setOnClickListener(this);
		_llSearch1 = (LinearLayout) view.findViewById(R.id.llListSearch1);
		
		view.findViewById(R.id.btnListSearch2).setOnClickListener(this);
		_llSearch2 = (LinearLayout) view.findViewById(R.id.llListSearch2);
		
		_tvMask = ((TextView) view.findViewById(R.id.tvListSearch));
		_tvMask.setOnClickListener(this);
		_etMask = ((EditText) view.findViewById(R.id.etListSearch));
		_etMask.setOnKeyListener(new View.OnKeyListener()
		{
			@Override
			public boolean onKey(View pView, int pKeyCode, KeyEvent pEvent)
			{
				if (pKeyCode == KeyEvent.KEYCODE_ENTER && pEvent.getAction() == KeyEvent.ACTION_DOWN)
				{
				    _etMask.setVisibility(View.GONE);
				    _tvMask.setText(_etMask.getText());
				    _tvMask.setVisibility(View.VISIBLE);
				}
				return false;
			}
		});
		_etMask.setOnFocusChangeListener(new EditText.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View pView, boolean pHasFocus)
			{
				if (!pHasFocus)
				{
					//без фокуса
					InputMethodManager imm = (InputMethodManager)__GetMainActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				    imm.hideSoftInputFromWindow(_etMask.getWindowToken(), 0);
				    
				    _etMask.setVisibility(View.GONE);
				    _tvMask.setText(_etMask.getText());
				    _tvMask.setVisibility(View.VISIBLE);
				}
			}
		});
		
		view.findViewById(R.id.ivListSpeaker).setOnClickListener(this);
		
		_lv = (ListView) view.findViewById(R.id.lvList);
		_lv.setAdapter(_Adapter);
		
		view.findViewById(R.id.llListShadow).bringToFront();
		
		_rlDialogSex = (RelativeLayout) view.findViewById(R.id.rlListDialogSex);
		_rlDialogSex.setVisibility(View.GONE);
		_rlDialogSex.setOnClickListener(this);
		view.findViewById(R.id.llListDialogSexMale).setOnClickListener(this);
		view.findViewById(R.id.llListDialogSexFemale).setOnClickListener(this);
		view.findViewById(R.id.llListDialogSexNot).setOnClickListener(this);
		_ivDialogSexMale = ((ImageView) view.findViewById(R.id.ivListDialogSexMale));
		_ivDialogSexFemale = ((ImageView) view.findViewById(R.id.ivListDialogSexFemale));
		_ivDialogSexNot = ((ImageView) view.findViewById(R.id.ivListDialogSexNot));
		
		_rlDialogAge = (RelativeLayout) view.findViewById(R.id.rlListDialogAge);
		_rlDialogAge.setVisibility(View.GONE);
		_rlDialogAge.setOnClickListener(this);
		_sbDialogAge = (RangeSeekBar<Integer>) view.findViewById(R.id.sbListDialogAge);
		_sbDialogAge.setTextAboveThumbsColorResource(R.color.TextBlack);
		view.findViewById(R.id.btnListDialogAge).setOnClickListener(this);

		if (pStore.get(COUNTRY_ID) != null)
		{
			_SelectedCountry = Integer.parseInt(pStore.get(COUNTRY_ID));
			pStore.remove(COUNTRY_ID);
		}
		if (pStore.get(CITY_ID) != null)
		{
			_SelectedCity = Integer.parseInt(pStore.get(CITY_ID));
			_SelectedName = pStore.get(CITY_NAME);
			pStore.remove(CITY_ID);
			pStore.remove(CITY_NAME);
		}
		
		return view;
	}
	
	@Override
	protected void __OnResume()
	{
		if (_FilterTemp != null)
		{
			_slFilter.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					_ivFilter.callOnClick();
				}
			}, 500);
		}
	}
	
	@Override
	protected void __OnSave(SparseArray<String> pStore)
	{
		pStore.put(_FILTER_MINAGE, Integer.toString(_FilterMain.MinAge));
		pStore.put(_FILTER_MAXAGE, Integer.toString(_FilterMain.MaxAge));
		pStore.put(_FILTER_SEX, _FilterMain.Sex);
		pStore.put(_FILTER_COUNTRY, Integer.toString(_FilterMain.CountryId));
		pStore.put(_FILTER_CITY, Integer.toString(_FilterMain.CityId));
		pStore.put(_FILTER_CITYNAME, _FilterMain.CityName);
		pStore.put(_FILTER_FRIEND, Boolean.toString(_FilterMain.Friend));
		pStore.put(_FILTER_MASK, _FilterMain.Mask);
		
		pStore.put(_LIST_COUNT, Integer.toString(_Count));
	}
	
	@Override
	protected void __OnDestroy()
	{
		_etMask.removeTextChangedListener(_MaskTextWatcher);
	}
	
	@Override
	protected boolean __OnBackPressed()
	{
		if (_rlDialogSex.getVisibility() == View.VISIBLE)
		{
			_rlDialogSex.setVisibility(View.GONE);
			return false;
		}
		else if (_rlDialogAge.getVisibility() == View.VISIBLE)
		{
			_rlDialogAge.setVisibility(View.GONE);
			return false;
		}
		return true;
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivListMenu:
				__GetMainActivity().MenuOpen();
				break;
				
			case R.id.ivListCancel:
			case R.id.ivListOk:	
				if (_slFilter.getOpenStatus() == Status.Open)
				{
					_slFilter.close();
				}
				
				if (pView.getId() == R.id.ivListOk)
				{
					_Users.clear();
					_Adapter.notifyDataSetChanged();
					
					_FilterMain.MinAge = _FilterTemp.MinAge;
					_FilterMain.MaxAge = _FilterTemp.MaxAge;
					_FilterMain.Sex = _FilterTemp.Sex;
					_FilterMain.CountryId = _FilterTemp.CountryId;
					_FilterMain.CityId = _FilterTemp.CityId;
					_FilterMain.CityName = _FilterTemp.CityName;
					_FilterMain.Friend = _FilterTemp.Friend;
					_FilterMain.Mask = _FilterTemp.Mask;
				}
				
				_SelectedCountry = -1;
				_SelectedCity = -1;
				_SelectedName = "";
				
				_FilterTemp = null;

				_ivOk.setVisibility(View.GONE);
				_ivCancel.setVisibility(View.GONE);
				_ivSearch.setVisibility(View.VISIBLE);
				_llSearch.setVisibility(View.GONE);
				_slFilter.setSwipeEnabled(false);

				if (pView.getId() == R.id.ivListOk)
				{
					//_GetUsers();
				}
				break;
				
			case R.id.ivListSearch:	
			case R.id.ivListFilter:
				if (_llSearch.getVisibility() == View.GONE)
				{
					if (_FilterTemp == null)
					{
						_FilterTemp = new Filter();
					}
					_FilterTemp.MinAge = _FilterMain.MinAge;
					_FilterTemp.MaxAge = _FilterMain.MaxAge;
					_FilterTemp.Sex = _FilterMain.Sex;
					_FilterTemp.CountryId = _FilterMain.CountryId;
					_FilterTemp.CityId = _FilterMain.CityId;
					_FilterTemp.CityName = _FilterMain.CityName;
					_FilterTemp.Friend = _FilterMain.Friend;
					_FilterTemp.Mask = _FilterMain.Mask;
					
					if (_SelectedCountry >= 0)
					{
						if (_FilterTemp.CountryId != _SelectedCountry)
						{
							_FilterTemp.CityId = 0;
							_FilterTemp.CityName = "";
						}
						
						_FilterTemp.CountryId = _SelectedCountry;
					}
					if (_SelectedCity >= 0)
					{
						_FilterTemp.CityId = _SelectedCity;
						_FilterTemp.CityName = _SelectedName;
					}
					
					if (_FilterTemp.MinAge == -1)
					{
						_sbDialogAge.setSelectedMinValue(0);
						_sbDialogAge.setSelectedMaxValue(100);
						_tvAge.setText(R.string.AgeNot);
					}
					else
					{
						_sbDialogAge.setSelectedMinValue(_FilterTemp.MinAge);
						_sbDialogAge.setSelectedMaxValue(_FilterTemp.MaxAge);
						_tvAge.setText(Integer.toString(_FilterTemp.MinAge) + "-" + Integer.toString(_FilterTemp.MaxAge));
						
					}
					
					if (_FilterTemp.Sex.equals("m"))
					{
						_tvSex.setText(R.string.SexMale);
						_ivDialogSexMale.setImageResource(R.drawable.ic_select_pressed);
						_ivDialogSexFemale.setImageResource(R.drawable.ic_select);
						_ivDialogSexNot.setImageResource(R.drawable.ic_select);
					}
					else if (_FilterTemp.Sex.equals("f"))
					{
						_tvSex.setText(R.string.SexFemale);
						_ivDialogSexMale.setImageResource(R.drawable.ic_select);
						_ivDialogSexFemale.setImageResource(R.drawable.ic_select_pressed);
						_ivDialogSexNot.setImageResource(R.drawable.ic_select);
					}
					else
					{
						_tvSex.setText(R.string.SexNot);
						_ivDialogSexMale.setImageResource(R.drawable.ic_select);
						_ivDialogSexFemale.setImageResource(R.drawable.ic_select);
						_ivDialogSexNot.setImageResource(R.drawable.ic_select_pressed);
					}
					
					Country country = __GetMainActivity().GetCountries().GetById(_FilterTemp.CountryId);
					if (country == null)
					{
						_ivCountry.setImageResource(R.drawable.ic_flag_0);
						_tvCountry.setText(R.string.MapAllCountry);
						_ivCountryNext.setOnClickListener(null);
						_ivCountryNext.setClickable(false);
						_ivCountryNext.setImageResource(R.drawable.selector_next);
					}
					else
					{
						_ivCountry.setImageBitmap(country.Flag);
						_tvCountry.setText(country.Name);
						_ivCountryNext.setOnClickListener(this);
						_ivCountryNext.setClickable(true);
						_ivCountryNext.setImageResource(R.drawable.selector_delete);
					}
					
					if (_FilterTemp.CityName.length() == 0)
					{
						_tvCity.setText(R.string.SelectCity);
						_ivCityNext.setOnClickListener(null);
						_ivCityNext.setClickable(false);
						_ivCityNext.setImageResource(R.drawable.selector_next);
					}
					else
					{
						_tvCity.setText(_FilterTemp.CityName);
						_ivCityNext.setOnClickListener(this);
						_ivCityNext.setClickable(true);
						_ivCityNext.setImageResource(R.drawable.selector_delete);
					}
					
					if (_FilterTemp.Friend)
					{
						_llSearch1.setVisibility(View.INVISIBLE);
						_llSearch2.setVisibility(View.VISIBLE);
					}
					else
					{
						_llSearch1.setVisibility(View.VISIBLE);
						_llSearch2.setVisibility(View.INVISIBLE);
					}
					
					_etMask.setText(_FilterTemp.Mask);
					_tvMask.setText(_FilterTemp.Mask);
					
					_ivSearch.setVisibility(View.GONE);
					_ivCancel.setVisibility(View.VISIBLE);
					_ivOk.setVisibility(View.VISIBLE);

					_llSearch.setVisibility(View.VISIBLE);
					_slFilter.setSwipeEnabled(true);
				}
				
			    _etMask.setVisibility(View.GONE);
			    _tvMask.setText(_etMask.getText());
			    _tvMask.setVisibility(View.VISIBLE);
				
				if (_slFilter.getOpenStatus() == Status.Close && pView.getId() == R.id.ivListFilter)
				{
					_slFilter.postDelayed(new Runnable()
					{
						@Override
						public void run()
						{
							_slFilter.open(DragEdge.Top);
						}
					}, 200);
				}
				break;
				
			case R.id.llListAge:	
				__GetMainActivity().MenuLock();
				_rlDialogAge.setVisibility(View.VISIBLE);
				break;
			case R.id.rlListDialogAge:
			case R.id.btnListDialogAge:
				__GetMainActivity().MenuUnlock();
				if (pView.getId() == R.id.btnListDialogAge)
				{
					if (_sbDialogAge.getSelectedMinValue() == 0 && _sbDialogAge.getSelectedMaxValue() == 100)
					{
						_tvAge.setText(R.string.AgeNot);
						_FilterTemp.MinAge = -1;
						_FilterTemp.MaxAge = -1;
					}
					else
					{
						_tvAge.setText(Integer.toString(_sbDialogAge.getSelectedMinValue()) + "-" + Integer.toString(_sbDialogAge.getSelectedMaxValue()));
						_FilterTemp.MinAge = _sbDialogAge.getSelectedMinValue();
						_FilterTemp.MaxAge = _sbDialogAge.getSelectedMaxValue();
					}
				}
				_rlDialogAge.setVisibility(View.GONE);
				_slFilter.postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						_slFilter.open(DragEdge.Top);
					}
				}, 200);
				break;
				
			case R.id.llListSex:	
				__GetMainActivity().MenuLock();
				_rlDialogSex.setVisibility(View.VISIBLE);
				break;
			case R.id.rlListDialogSex:
			case R.id.llListDialogSexMale:
			case R.id.llListDialogSexFemale:
			case R.id.llListDialogSexNot:
				__GetMainActivity().MenuUnlock();
				if (pView.getId() == R.id.llListDialogSexMale)
				{
					_FilterTemp.Sex = "m";
					_tvSex.setText(R.string.SexMale);
					_ivDialogSexMale.setImageResource(R.drawable.ic_select_pressed);
					_ivDialogSexFemale.setImageResource(R.drawable.ic_select);
					_ivDialogSexNot.setImageResource(R.drawable.ic_select);
				}
				else if (pView.getId() == R.id.llListDialogSexFemale)
				{
					_FilterTemp.Sex = "f";
					_tvSex.setText(R.string.SexFemale);
					_ivDialogSexMale.setImageResource(R.drawable.ic_select);
					_ivDialogSexFemale.setImageResource(R.drawable.ic_select_pressed);
					_ivDialogSexNot.setImageResource(R.drawable.ic_select);
				}
				else if (pView.getId() == R.id.llListDialogSexNot)
				{
					_FilterTemp.Sex = "";
					_tvSex.setText(R.string.SexNot);
					_ivDialogSexMale.setImageResource(R.drawable.ic_select);
					_ivDialogSexFemale.setImageResource(R.drawable.ic_select);
					_ivDialogSexNot.setImageResource(R.drawable.ic_select_pressed);
				}
				_rlDialogSex.setVisibility(View.GONE);
				_slFilter.postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						_slFilter.open(DragEdge.Top);
					}
				}, 200);
				break;

			case R.id.llListPlace:	
				__GetMainActivity().GetManager().GoToCountries(_FilterTemp.CountryId, false);
				break;
			case R.id.ivListPlaceNext:
				_FilterTemp.CountryId = 0;
				_ivCountry.setImageResource(R.drawable.ic_flag_0);
				_tvCountry.setText(R.string.MapAllCountry);
				_ivCountryNext.setOnClickListener(null);
				_ivCountryNext.setClickable(false);
				_ivCountryNext.setImageResource(R.drawable.selector_next);
				_FilterTemp.CityId = 0;
				_FilterTemp.CityName = "";
				_tvCity.setText(R.string.SelectCity);
				_ivCityNext.setOnClickListener(null);
				_ivCityNext.setClickable(false);
				_ivCityNext.setImageResource(R.drawable.selector_next);
				_slFilter.postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						_slFilter.open(DragEdge.Top);
					}
				}, 200);
				break;
				
			case R.id.llListCity:	
				__GetMainActivity().GetManager().GoToCities(_FilterTemp.CountryId, _FilterTemp.CityId);
				break;
			case R.id.ivListCityNext:
				_FilterTemp.CityId = 0;
				_FilterTemp.CityName = "";
				_tvCity.setText(R.string.SelectCity);
				_ivCityNext.setOnClickListener(null);
				_ivCityNext.setClickable(false);
				_ivCityNext.setImageResource(R.drawable.selector_next);
				_slFilter.postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						_slFilter.open(DragEdge.Top);
					}
				}, 200);
				break;
				
			case R.id.btnListSearch1:	
				_FilterTemp.Friend = false;
				_llSearch1.setVisibility(View.VISIBLE);
				_llSearch2.setVisibility(View.INVISIBLE);
				break;
			case R.id.btnListSearch2:	
				_FilterTemp.Friend = true;
				_llSearch1.setVisibility(View.INVISIBLE);
				_llSearch2.setVisibility(View.VISIBLE);
				break;
			case R.id.tvListSearch:
				_tvMask.setVisibility(View.GONE);
				_etMask.setVisibility(View.VISIBLE);
				_etMask.postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						_etMask.requestFocus();
						_slFilter.close();
						InputMethodManager imm = (InputMethodManager)__GetMainActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					    imm.showSoftInput(_etMask, 0);
					}
				}, 200);
				break;
			case R.id.ivListSpeaker:	
				//TODO голосовой ввод
				break;
		}
	}

	@Override
	public void OnTextChanged(int pID, String pText)
	{
		if (_FilterTemp != null)
		{
			_FilterTemp.Mask = pText;
		}
	}

	@Override
	protected void __OnMessageReceived(String pMessageId, String pAction, int pStatus, String pResult, JSONObject pOptions)
	{
		if (pAction.equals("user.geolist") && pStatus == 1000)
		{
			int index = 0;
			try
			{
				JSONArray users = new JSONArray(pResult);
				for (index = 0; index < users.length(); index++)
				{
					long id = users.getJSONObject(index).getLong("id");
					// не добавляем пользователей которые уже есть
					if (_Users.get(id) == null)
					{
						User user = new User(users.getJSONObject(index));
						if (user.IsFriend)
						{
							user = __GetMainActivity().GetUsers().AddUser(user, __GetMainActivity());
						}
						_Users.append(id, user);
					}
				}
				_Adapter.notifyDataSetChanged();
			}
			catch (JSONException e)
			{
				__Log("Bad data index: " + index);
			}
		}
	}


	private void _GetUsers()
	{
		if (__GetMainActivity().GetLocation() != null)
		{
			try
			{
				JSONObject message = new JSONObject();
				message.put("userlist", new JSONArray());
				message.put("status", 1);
				message.put("locale", __GetMainActivity().GetProfile().GetLocale());
				message.put("isfriend", _FilterMain.Friend ? 1 : -1);
				message.put("country", _FilterMain.CountryId);
				message.put("city", _FilterMain.CityId);
				message.put("sex", _FilterMain.Sex);
				if (_FilterMain.MinAge == -1)
				{
					message.put("minage", 0);
					message.put("maxage", 0);
				}
				else
				{
					message.put("minage", _FilterMain.MinAge);
					message.put("maxage", _FilterMain.MaxAge);
				}
				message.put("mask", _FilterMain.Mask);
				message.put("lat", __GetMainActivity().GetLocation().getLatitude());
				message.put("lng", __GetMainActivity().GetLocation().getLongitude());
				message.put("radius", 0);
				message.put("count", _Count);
				__SendToServer("user.geolist", message);
			}
			catch (JSONException e)
			{
				__Log("Bad request for filter: " + e.getMessage());
			}
		}
	}
}