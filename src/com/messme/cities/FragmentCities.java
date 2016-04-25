package com.messme.cities;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.socket.CommunicationSocket;
import com.messme.socket.MessageItem;
import com.messme.view.MessmeFragment;
import com.messme.view.TextWatcher;
import com.messme.view.TextWatcher.iOnTextChanged;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

public class FragmentCities extends MessmeFragment implements OnClickListener, OnItemClickListener, iOnTextChanged, CommunicationSocket.iSocketListener
{
	private LinearLayout _llTitle = null;
	private LinearLayout _llSearch = null;
	private EditText _etSearch = null;
	private ListView _lv = null;
//	private ImageView _ivOK = null;
	private ProgressBar _pb = null;

	private AdapterCities _Adapter = null;
	private String _StartFilter = "";
	
	private TextWatcher _SearchTextWatcher = null;
	
	private int _Country = 0;
	private int _SelectedCityID = -1;
	
	private boolean _Loaded = false;
	
	private CommunicationSocket _Socket;	
	
	public FragmentCities(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
		_SearchTextWatcher = new TextWatcher(this, 1);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		_Country = Integer.parseInt(pStore.get(COUNTRY_ID));
		_SelectedCityID = Integer.parseInt(pStore.get(CITY_ID));
		
		View view = pInflater.inflate(R.layout.cities, pContainer, false);
		
		view.findViewById(R.id.ivCitiesBack).setOnClickListener(this);
		view.findViewById(R.id.ivCitiesSearchBack).setOnClickListener(this);
		view.findViewById(R.id.ivCitiesSearch).setOnClickListener(this);
		view.findViewById(R.id.ivCitiesClose).setOnClickListener(this);
		
//		_ivOK = (ImageView) view.findViewById(R.id.ivCitiesOk);
//		_ivOK.setOnClickListener(this);
		
		_llTitle = (LinearLayout) view.findViewById(R.id.llCitiesTitle);
		_llSearch = (LinearLayout) view.findViewById(R.id.llCitiesSearch);
		
		_etSearch = (EditText) view.findViewById(R.id.etCitiesSearch);
		_etSearch.addTextChangedListener(_SearchTextWatcher);
		
		view.findViewById(R.id.llCitiesShadow).bringToFront();
		
		_lv = (ListView) view.findViewById(R.id.lvCities);
		_lv.setOnItemClickListener(this);
		
		_pb = (ProgressBar) view.findViewById(R.id.pbCities);
		
		if (__GetMainActivity().GetServerConnection() == null)
		{
			_Socket = new CommunicationSocket(this);
			_Socket.Connect("User/?id=" + __GetMainActivity().GetProfile().GetID());
		}
		else
		{
			_Socket = null;
		}
		
		ArrayList<City> cities = __GetMainActivity().GetCities().get(_Country);
		if (cities == null)
		{
			_Loaded = false;
			_pb.bringToFront();
			_pb.setVisibility(View.VISIBLE);
//			_ivOK.setVisibility(View.INVISIBLE);
			
			if (_Socket == null)
			{
				_OnConnect();
			}
		}
		else
		{
			_Loaded = true;
			_Adapter = new AdapterCities(__GetMainActivity(), _SelectedCityID, cities, "");
			_lv.setAdapter(_Adapter);
			_pb.setVisibility(View.GONE);
//			if (_Adapter.GetSelected() == null)
//			{
//				_ivOK.setVisibility(View.INVISIBLE);
//			}
//			else
//			{
//				_ivOK.setVisibility(View.VISIBLE);
//			}
		}
		
		return view;
	}
	
	@Override
	protected void __OnResume()
	{
		_etSearch.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			    imm.hideSoftInputFromWindow(_etSearch.getWindowToken(), 0);
			}
		}, 50);
	}

	@Override
	protected void __OnDestroy()
	{
		_etSearch.removeTextChangedListener(_SearchTextWatcher);
		if (_Socket != null)
		{
			_Socket.Disconnect();
			_Socket = null;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pIndex, long arg3)
	{
		_Adapter.SetSelected(pIndex);
		_llTitle.setVisibility(View.VISIBLE);
		_llSearch.setVisibility(View.GONE);
		_etSearch.setEnabled(false);
		_etSearch.setFocusable(false);
//		_ivOK.setVisibility(View.VISIBLE);
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(_etSearch.getWindowToken(), 0);
		SparseArray<String> store = new SparseArray<String>();
		
		City selectedCity = _Adapter.GetSelected();
		store.put(CITY_ID, Integer.toString(selectedCity.ID));
		store.put(CITY_NAME, selectedCity.Name);
		store.put(CITY_COUNTRY, Integer.toString(selectedCity.CountryID));
		__GetMainActivity().GetManager().GoToBackWithResult(store);
	}

	@Override
	public void onClick(View pView)
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(_etSearch.getWindowToken(), 0);
		switch (pView.getId())
		{
			case R.id.ivCitiesBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.ivCitiesSearch:
				_llTitle.setVisibility(View.GONE);
				_llSearch.setVisibility(View.VISIBLE);
				_etSearch.postDelayed(new Runnable() 
				{
					@Override
					public void run() {
						_etSearch.requestFocus();
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput((_etSearch), 0);
					}
				}, 200);
				break;
			case R.id.ivCitiesOk:
				SparseArray<String> store = new SparseArray<String>();
				City selectedCity = _Adapter.GetSelected();
				if (selectedCity == null)
				{
					__GetMainActivity().OpenDialog(2, __GetMainActivity().getString(R.string.Dialog16Title), __GetMainActivity().getString(R.string.Dialog16Description));
				}
				else
				{
					store.put(CITY_ID, Integer.toString(selectedCity.ID));
					store.put(CITY_NAME, selectedCity.Name);
					store.put(CITY_COUNTRY, Integer.toString(selectedCity.CountryID));
					__GetMainActivity().GetManager().GoToBackWithResult(store);
				}
				break;
			case R.id.ivCitiesSearchBack:
			case R.id.ivCitiesClose:
				_llTitle.setVisibility(View.VISIBLE);
				_llSearch.setVisibility(View.GONE);
				if (pView.getId() == R.id.ivCitiesClose)
				{
					_etSearch.setText("");
				}
				break;
		}
	}

	@Override
	public void OnTextChanged(int pID, String pText)
	{
		if (_Adapter != null)
		{
			_Adapter.SetFilter(pText);
		}
		else
		{
			_StartFilter = pText;
		}
	}

	
	@Override
	protected void __OnConnected()
	{
		_OnConnect();
	}
	@Override
	protected void __OnErrorSended(String pMessageId, String pAction, JSONObject pOptions)
	{
		__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog17Title), __GetMainActivity().getString(R.string.Dialog17Description));
	}
	@Override
	protected void __OnMessageReceived(String pMID, String pAction, int pStatus, String pResult, JSONObject pOptions)
	{
		_OnMessageReceived(pMID, pAction, pStatus, pResult, pOptions);
	}

	
	@Override
	public void OnConnect()
	{
		_OnConnect();
	}
	@Override
	public void OnMessageSendError(MessageItem pItem, boolean pTimeOut)
	{
		__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog17Title), __GetMainActivity().getString(R.string.Dialog17Description));
	}
	@Override
	public void OnMessageSendSuccess(MessageItem pItem)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void OnMessageReceive(String pMID, String pAction, int pStatus, String pResult, int pType, JSONObject pOptions)
	{
		_OnMessageReceived(pMID, pAction, pStatus, pResult, pOptions);
	}
	
	
	private void _OnConnect()
	{
		if (!_Loaded)
		{
			try
			{
				JSONObject options = new JSONObject();
				options.put("continent", new JSONArray());
				JSONArray country = new JSONArray();
				country.put(_Country);
				options.put("country", country);
				options.put("region", new JSONArray());
				options.put("locale", Locale.getDefault().getLanguage());
				options.put("mask", "");
				if (_Socket == null)
				{
					__SendToServer("geo.city.list", options);
				}
				else
				{
					_Socket.Send("geo.city.list", options);
				}
			}
			catch (JSONException e)
			{
			}
		}
	}
	private void _OnMessageReceived(String pMessageId, String pAction, int pStatus, String pResult, JSONObject pOptions)
	{
		if (pAction.equals("geo.city.list"))
		{
			if (pStatus == 1000)
			{
				new AsyncTask<String, Void, ArrayList<City>>()
				{
					@Override
					protected ArrayList<City> doInBackground(String... pParams)
					{
						ArrayList<City> array = new ArrayList<City>();
						try
						{
							JSONArray cities = new JSONArray(pParams[0]);
							for (int i = 0; i < cities.length(); i++)
							{
								City city = new City(cities.getJSONObject(i));
								array.add(city);
							}
						}
						catch (JSONException e)
						{
						}
						return array;
					}
					protected void onPostExecute(java.util.ArrayList<City> pResult) 
					{
						_Loaded = true;
						__GetMainActivity().GetCities().append(_Country, pResult);
						_Adapter = new AdapterCities(__GetMainActivity(), _SelectedCityID, pResult, _StartFilter);
						_lv.setAdapter(_Adapter);
						_pb.setVisibility(View.GONE);
//						if (_Adapter.GetSelected() == null)
//						{
//							_ivOK.setVisibility(View.INVISIBLE);
//						}
//						else
//						{
//							_ivOK.setVisibility(View.VISIBLE);
//						}
					}
				}.execute(pResult);
			}
			else
			{
				_pb.setVisibility(View.GONE);
			}
		}
	}
}