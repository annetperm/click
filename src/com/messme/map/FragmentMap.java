package com.messme.map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.SwipeLayout.DragEdge;
import com.daimajia.swipe.SwipeLayout.Status;
import com.florescu.android.rangeseekbar.RangeSeekBar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.kyleduo.switchbutton.SwitchButton;
import com.messme.ActivityMain;
import com.messme.R;
import com.messme.countries.Countries.Country;
import com.messme.user.User;
import com.messme.util.ImageUtil;
import com.messme.util.Util;
import com.messme.view.MessmeFragment;
import com.messme.view.TextWatcher;
import com.messme.view.TextWatcher.iOnTextChanged;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.LongSparseArray;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class FragmentMap extends MessmeFragment implements OnClickListener, iOnTextChanged
{
	private static final int _FILTER_SHOWME = 200;
	private static final int _FILTER_MINAGE = 201;
	private static final int _FILTER_MAXAGE = 202;
	private static final int _FILTER_SEX = 203;
	private static final int _FILTER_COUNTRY = 204;
	private static final int _FILTER_CITY = 205;
	private static final int _FILTER_CITYNAME = 206;
	private static final int _FILTER_FRIEND = 207;
	private static final int _FILTER_MASK = 208;
	
	private static final int _CAMERA_LAT = 300;
	private static final int _CAMERA_LNG = 301;
	private static final int _CAMERA_ZOOM = 302;
	private static final int _CAMERA_TILT = 303;
	private static final int _CAMERA_BEARING = 304;
	
	
	private ImageView _ivCancel = null;
	private ImageView _ivOk = null;
	private ImageView _ivSearch = null;
	private ImageView _ivFilter = null;

	private LinearLayout _llSearchBtn = null;
	private LinearLayout _llSearchMask = null;
	private SwipeLayout _slFilter = null;
	private SwitchButton _sbShowMe = null;
	private TextView _tvSex = null;
	private TextView _tvAge = null;
	private ImageView _ivCountry = null;
	private TextView _tvCountry = null;
	private ImageView _ivCountryNext = null;
	private TextView _tvCity = null;
	private ImageView _ivCityNext = null;
	private TextView _tvMask = null;
	private EditText _etMask = null;
	private LinearLayout _llSearch1 = null;
	private LinearLayout _llSearch2 = null;
	
	private SupportMapFragment _MapFragment = null;
	private GoogleMap _Map = null;
	private MapWrapperLayout _MapWrapperLayout = null;
	
	private ViewGroup _InfoWindow = null;
	private ImageButton _ibtnAvatar = null;
	private TextView _tvName = null;
	private TextView _tvLikes = null;
	private ImageButton _ibtnLikes = null;
	private TextView _tvBottom = null;
	private ImageButton _ibtnMessage = null;
	
	private RelativeLayout _rlDialogSex = null;
	private ImageView _ivDialogSexMale = null;
	private ImageView _ivDialogSexFemale = null;
	private ImageView _ivDialogSexNot = null;
	
	private RelativeLayout _rlDialogAge = null;
	private RangeSeekBar<Integer> _sbDialogAge = null;
	
	private OnInfoWindowButtonTouchListener _InfoWindowClickListener = null;
	
	private LongSparseArray<User> _Points = new LongSparseArray<User>();
	private ClusterManager<User> _ClusterManager;
	private CameraPosition _CameraPosition = null;

	private TextWatcher _MaskTextWatcher = null;
	
	class Filter
	{
		boolean ShowMe = true;
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
	
	
	public FragmentMap(ActivityMain pActivity, SparseArray<String> pStore)
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
			_FilterMain.ShowMe = Boolean.parseBoolean(pStore.get(_FILTER_SHOWME));
			_FilterMain.MinAge = Integer.parseInt(pStore.get(_FILTER_MINAGE));
			_FilterMain.MaxAge = Integer.parseInt(pStore.get(_FILTER_MAXAGE));
			_FilterMain.Sex = pStore.get(_FILTER_SEX);
			_FilterMain.CountryId = Integer.parseInt(pStore.get(_FILTER_COUNTRY));
			_FilterMain.CityId = Integer.parseInt(pStore.get(_FILTER_CITY));
			_FilterMain.CityName = pStore.get(_FILTER_CITYNAME);
			_FilterMain.Friend = Boolean.parseBoolean(pStore.get(_FILTER_FRIEND));
			_FilterMain.Mask = pStore.get(_FILTER_MASK);
		}
		else
		{
			_FilterMain.ShowMe = __GetMainActivity().GetProfile().GetMapStatus() == 0 ? false : true;
		}
		
		if (pStore.get(_CAMERA_LAT) != null)
		{
			double lat = Double.parseDouble(pStore.get(_CAMERA_LAT));
			double lng = Double.parseDouble(pStore.get(_CAMERA_LNG));
			float zoom = Float.parseFloat(pStore.get(_CAMERA_ZOOM));
			float tilt = Float.parseFloat(pStore.get(_CAMERA_TILT));
			float bearing = Float.parseFloat(pStore.get(_CAMERA_BEARING));
			_CameraPosition = new CameraPosition(new LatLng(lat, lng), zoom, tilt, bearing);
		}
		else if (pStore.get(USER_ID) != null)
		{
			long userID = Long.parseLong(pStore.get(USER_ID));
			User user = __GetMainActivity().GetUsers().GetUser(userID);
			if (user.IsShowableInMap())
			{
				double lat = user.Lat;
				double lng = user.Lng;
				_CameraPosition = new CameraPosition(new LatLng(lat, lng), 13, 0, 0);
			}
		}
		
		if (_CameraPosition == null)
		{
			Location location = __GetMainActivity().GetLocation();
			if (location == null)
			{
				_CameraPosition = new CameraPosition(new LatLng(0, 0), 13, 0, 0);
			}
			else
			{
				_CameraPosition = new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()), 13, 0, 0);
			}
		}
		
		View view = pInflater.inflate(R.layout.map, pContainer, false);
		
		view.findViewById(R.id.ivMapMenu).setOnClickListener(this);
		
		_ivCancel = (ImageView) view.findViewById(R.id.ivMapCancel);
		_ivCancel.setOnClickListener(this);
		_ivOk = (ImageView) view.findViewById(R.id.ivMapOk);
		_ivOk.setOnClickListener(this);
		
		_ivSearch = (ImageView) view.findViewById(R.id.ivMapSearch);
		_ivSearch.setOnClickListener(this);
		_ivFilter = (ImageView) view.findViewById(R.id.ivMapFilter);
		_ivFilter.setOnClickListener(this);
		
		_llSearchBtn = (LinearLayout) view.findViewById(R.id.llMapSearchBtn);
		_llSearchMask = (LinearLayout) view.findViewById(R.id.llMapSearchMask);
		
		_slFilter = (SwipeLayout) view.findViewById(R.id.slMap);
		_slFilter.setSwipeEnabled(false);
		_slFilter.addSwipeListener(new SwipeLayout.SwipeListener()
		{
			@Override
			public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset)
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

		view.findViewById(R.id.llMapShowMe).setOnClickListener(this);
		_sbShowMe = (SwitchButton) view.findViewById(R.id.sbMapShowMe);
		
		view.findViewById(R.id.llMapAge).setOnClickListener(this);
		_tvAge = (TextView) view.findViewById(R.id.tvMapAge);
		
		view.findViewById(R.id.llMapSex).setOnClickListener(this);
		_tvSex = (TextView) view.findViewById(R.id.tvMapSex);
		
		view.findViewById(R.id.llMapCountry).setOnClickListener(this);
		_ivCountry = (ImageView) view.findViewById(R.id.ivMapCountry);
		_tvCountry = (TextView) view.findViewById(R.id.tvMapCountry);
		_ivCountryNext = (ImageView) view.findViewById(R.id.ivMapCountryNext);
		
		view.findViewById(R.id.llMapCity).setOnClickListener(this);
		_tvCity = (TextView) view.findViewById(R.id.tvMapCity);
		_ivCityNext = (ImageView) view.findViewById(R.id.ivMapCityNext);
		
		view.findViewById(R.id.btnMapSearch1).setOnClickListener(this);
		_llSearch1 = (LinearLayout) view.findViewById(R.id.llMapSearch1);
		
		view.findViewById(R.id.btnMapSearch2).setOnClickListener(this);
		_llSearch2 = (LinearLayout) view.findViewById(R.id.llMapSearch2);
		
		_tvMask = ((TextView) view.findViewById(R.id.tvMapSearch));
		_tvMask.setOnClickListener(this);
		_etMask = ((EditText) view.findViewById(R.id.etMapSearch));
		_etMask.addTextChangedListener(_MaskTextWatcher);
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
		
		view.findViewById(R.id.ivMapSpeaker).setOnClickListener(this);
		
		view.findViewById(R.id.llMapShadow).bringToFront();
		
		_MapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
		_Map = _MapFragment.getMap();
		_Map.setMyLocationEnabled(true);
		_Map.getUiSettings().setZoomControlsEnabled(true);
		_Map.setOnMapClickListener(new GoogleMap.OnMapClickListener()
		{
			@Override
			public void onMapClick(LatLng pLatLng)
			{
			    _etMask.setVisibility(View.GONE);
			    _tvMask.setText(_etMask.getText());
			    _tvMask.setVisibility(View.VISIBLE);
			}
		});
		_Map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener()
		{
			@Override
			public boolean onMyLocationButtonClick()
			{
				if (__GetMainActivity().GetLocation() == null)
				{
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					return true;
				}
				else
				{
					return false;
				}
			}
		});
		_ClusterManager = new ClusterManager<User>(__GetMainActivity(), _Map)
		{
			@Override
			public void onCameraChange(CameraPosition pCameraPosition)
			{
				super.onCameraChange(pCameraPosition);

				if (_llSearchMask.getVisibility() == View.VISIBLE)
				{
					_etMask.setVisibility(View.GONE);
					_tvMask.setText(_etMask.getText());
					_tvMask.setVisibility(View.VISIBLE);
				}
				_GetUsers();
			}
		};
		_ClusterManager.setRenderer(new PointRenderer(__GetMainActivity(), _Map, _ClusterManager));
		_ClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<User>()
		{
			@Override
			public boolean onClusterClick(Cluster<User> pCluster)
			{
				_Map.animateCamera(CameraUpdateFactory.zoomIn());
				return true;
			}
		});
		_Map.moveCamera(CameraUpdateFactory.newCameraPosition(_CameraPosition));
		_Map.setOnCameraChangeListener(_ClusterManager);
		_Map.setOnMarkerClickListener(_ClusterManager);
		if (_FilterMain.ShowMe)
		{
			_Map.setMyLocationEnabled(true);
		}
		else
		{
			_Map.setMyLocationEnabled(false);
		}
		
		_MapWrapperLayout = (MapWrapperLayout)view.findViewById(R.id.map_relative_layout);
		_MapWrapperLayout.Init(_Map, Util.GetPixelsFromDp(getActivity(), 56) + 20 - 15);
		
		_InfoWindow = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.map_info_window, null);
		_ibtnAvatar = (ImageButton) _InfoWindow.findViewById(R.id.ibtnMapInfoWindowAvatar);
		_tvName = (TextView) _InfoWindow.findViewById(R.id.tvMapInfoWindowName);
		//_tvAge = (TextView) _InfoWindow.findViewById(R.id.tvMapInfoWindowAge);
		_tvLikes = (TextView) _InfoWindow.findViewById(R.id.tvMapInfoWindowLike);
		_ibtnLikes = (ImageButton) _InfoWindow.findViewById(R.id.ibtnMapInfoWindowLike);
		_tvBottom = (TextView) _InfoWindow.findViewById(R.id.tvMapInfoWindowBottom);
		_ibtnMessage = (ImageButton) _InfoWindow.findViewById(R.id.ibtnMapInfoWindowMessage);
		
		_InfoWindowClickListener = new OnInfoWindowButtonTouchListener(_ibtnAvatar, _ibtnLikes, _ibtnMessage)
		{
			@Override
			protected void __OnAvatarClick(Marker pMarker, User pUser)
			{
				__GetMainActivity().GetManager().GoToUser(pUser.Id);
			}
			@Override
			protected void __OnLikesClick(Marker pMarker, User pUser)
			{
				if (pUser.Liked)
				{
					pUser.Likes--;
	            }
	            else
	            {
	            	pUser.Likes++;
				}
				pUser.Liked = !pUser.Liked;
				try
				{
					JSONObject options = new JSONObject();
					options.put("id", pUser.Id);
					options.put("value", pUser.Liked ? 1 : 0);
					__SendToServer("user.like", options);
				}
				catch (JSONException e)
				{
				}
				pMarker.hideInfoWindow();
				pMarker.showInfoWindow();
			}
			@Override
			protected void __OnMessageClick(Marker pMarker, User pUser)
			{
				__GetMainActivity().GetManager().GoToDialog(pUser.Id, false, false);
			}
		};
		
		_ibtnAvatar.setOnTouchListener(_InfoWindowClickListener);
		_ibtnLikes.setOnTouchListener(_InfoWindowClickListener);
		_ibtnMessage.setOnTouchListener(_InfoWindowClickListener);
        
		_Map.setInfoWindowAdapter(new InfoWindowAdapter()
		{
			@Override
			public View getInfoWindow(Marker pMarker)
			{
				long id = Long.parseLong(pMarker.getTitle());
				User user = _Points.get(id);
				
				String name = user.GetName(__GetMainActivity());
				Spannable text = new SpannableString(name + ", " + user.GetAge(getActivity()));
				text.setSpan(new ForegroundColorSpan(__GetMainActivity().getResources().getColor(R.color.TextSub)), name.length(), text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	            _tvName.setText(text);
	            _tvBottom.setText(user.Login);
	            _tvLikes.setText(Integer.toString(user.Likes));
	            
	            if (user.Liked)
	            {
	            	_tvLikes.setTextColor(getResources().getColor(R.color.TextBlue));
	            	_ibtnLikes.setImageResource(R.drawable.ic_like_pressed);
	            }
	            else
	            {
	            	_tvLikes.setTextColor(getResources().getColor(R.color.TextSub));
	            	_ibtnLikes.setImageResource(R.drawable.ic_like);
	            }
	            
	            _ibtnAvatar.setImageBitmap(ImageUtil.GetCircle(user.GetImage(), Util.GetPixelsFromDp(__GetMainActivity(), 56)));
				
				_InfoWindowClickListener.SetMarker(pMarker, user);
				_MapWrapperLayout.SetMarkerWithInfoWindow(pMarker, _InfoWindow);
                return _InfoWindow;
			}
			@Override
			public View getInfoContents(Marker pMarker)
			{
				return null;
			}
		});

		_rlDialogSex = (RelativeLayout) view.findViewById(R.id.rlMapDialogSex);
		_rlDialogSex.setVisibility(View.GONE);
		_rlDialogSex.setOnClickListener(this);
		view.findViewById(R.id.llMapDialogSexMale).setOnClickListener(this);
		view.findViewById(R.id.llMapDialogSexFemale).setOnClickListener(this);
		view.findViewById(R.id.llMapDialogSexNot).setOnClickListener(this);
		_ivDialogSexMale = ((ImageView) view.findViewById(R.id.ivMapDialogSexMale));
		_ivDialogSexFemale = ((ImageView) view.findViewById(R.id.ivMapDialogSexFemale));
		_ivDialogSexNot = ((ImageView) view.findViewById(R.id.ivMapDialogSexNot));
		
		_rlDialogAge = (RelativeLayout) view.findViewById(R.id.rlMapDialogAge);
		_rlDialogAge.setVisibility(View.GONE);
		_rlDialogAge.setOnClickListener(this);
		_sbDialogAge = (RangeSeekBar<Integer>) view.findViewById(R.id.sbMapDialogAge);
		_sbDialogAge.setTextAboveThumbsColorResource(R.color.TextBlack);
		view.findViewById(R.id.btnMapDialogAge).setOnClickListener(this);

		if (pStore.get(COUNTRY_ID) != null)
		{
			_SelectedCountry = Integer.parseInt(pStore.get(COUNTRY_ID));
//			if (_FilterTemp != null)
//			{
//				int id = Integer.parseInt(pStore.get(COUNTRY_ID));
//				if (_FilterTemp.CountryId != id)
//				{
//					_FilterTemp.CityId = 0;
//					_FilterTemp.CityName = "";
//				}
//				_FilterTemp.CountryId = id;
//			}
			pStore.remove(COUNTRY_ID);
		}
		if (pStore.get(CITY_ID) != null)
		{
			_SelectedCity = Integer.parseInt(pStore.get(CITY_ID));
			_SelectedName = pStore.get(CITY_NAME);
//			if (_FilterTemp != null)
//			{
//				_FilterTemp.CityId = Integer.parseInt(pStore.get(CITY_ID));
//				_FilterTemp.CityName = pStore.get(CITY_NAME);
//			}
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
		_CameraPosition = _Map.getCameraPosition();
		pStore.put(_CAMERA_LAT, Double.toString(_CameraPosition.target.latitude));
		pStore.put(_CAMERA_LNG, Double.toString(_CameraPosition.target.longitude));
		pStore.put(_CAMERA_ZOOM, Float.toString(_CameraPosition.zoom));
		pStore.put(_CAMERA_TILT, Float.toString(_CameraPosition.tilt));
		pStore.put(_CAMERA_BEARING, Float.toString(_CameraPosition.bearing));
	
		pStore.put(_FILTER_SHOWME, Boolean.toString(_FilterMain.ShowMe));
		pStore.put(_FILTER_MINAGE, Integer.toString(_FilterMain.MinAge));
		pStore.put(_FILTER_MAXAGE, Integer.toString(_FilterMain.MaxAge));
		pStore.put(_FILTER_SEX, _FilterMain.Sex);
		pStore.put(_FILTER_COUNTRY, Integer.toString(_FilterMain.CountryId));
		pStore.put(_FILTER_CITY, Integer.toString(_FilterMain.CityId));
		pStore.put(_FILTER_CITYNAME, _FilterMain.CityName);
		pStore.put(_FILTER_FRIEND, Boolean.toString(_FilterMain.Friend));
		pStore.put(_FILTER_MASK, _FilterMain.Mask);
	}
	
	@Override
	protected void __OnDestroy()
	{
		_Points.clear();
		_ClusterManager.clearItems();
		_Map.setOnCameraChangeListener(null);
		
		_etMask.removeTextChangedListener(_MaskTextWatcher);
		
		FragmentTransaction ft = __GetMainActivity().getSupportFragmentManager().beginTransaction();
		ft.remove(_MapFragment);
		ft.commit();
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
			case R.id.ivMapMenu:
				__GetMainActivity().MenuOpen();
				break;
				
			case R.id.ivMapCancel:
			case R.id.ivMapOk:	
				if (_slFilter.getOpenStatus() == Status.Open)
				{
					_slFilter.close();
				}
				
				if (pView.getId() == R.id.ivMapOk)
				{
					_Points.clear();
					_ClusterManager.clearItems();
					
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
				_llSearchBtn.setVisibility(View.GONE);
				_llSearchMask.setVisibility(View.GONE);
				_slFilter.setSwipeEnabled(false);

				if (pView.getId() == R.id.ivMapOk)
				{
					_GetUsers();
				}
				
				if (_FilterMain.ShowMe != _sbShowMe.isChecked())
				{
					try
					{
						JSONObject options = new JSONObject();
						options.put("status", _sbShowMe.isChecked() ? 1 : 0);
						__SendToServer("user.setgeostatus", options);
					}
					catch (JSONException e)
					{
					}
				}
				break;
				
			case R.id.ivMapSearch:	
			case R.id.ivMapFilter:
				if (_llSearchMask.getVisibility() == View.GONE)
				{
					if (_FilterTemp == null)
					{
						_FilterTemp = new Filter();
					}
					_FilterTemp.ShowMe = _FilterMain.ShowMe;
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
					
					_sbShowMe.setChecked(_FilterTemp.ShowMe);
					
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

					_llSearchBtn.setVisibility(View.VISIBLE);
					_llSearchMask.setVisibility(View.VISIBLE);
					_slFilter.setSwipeEnabled(true);
				}
				
			    _etMask.setVisibility(View.GONE);
			    _tvMask.setText(_etMask.getText());
			    _tvMask.setVisibility(View.VISIBLE);
				
				if (_slFilter.getOpenStatus() == Status.Close && pView.getId() == R.id.ivMapFilter)
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
				
			case R.id.llMapShowMe:
				_sbShowMe.setChecked(!_sbShowMe.isChecked());
				break;
				
			case R.id.llMapAge:	
				__GetMainActivity().MenuLock();
				_rlDialogAge.setVisibility(View.VISIBLE);
				break;
			case R.id.rlMapDialogAge:
			case R.id.btnMapDialogAge:
				__GetMainActivity().MenuUnlock();
				if (pView.getId() == R.id.btnMapDialogAge)
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
				
			case R.id.llMapSex:	
				__GetMainActivity().MenuLock();
				_rlDialogSex.setVisibility(View.VISIBLE);
				break;
			case R.id.rlMapDialogSex:
			case R.id.llMapDialogSexMale:
			case R.id.llMapDialogSexFemale:
			case R.id.llMapDialogSexNot:
				__GetMainActivity().MenuUnlock();
				if (pView.getId() == R.id.llMapDialogSexMale)
				{
					_FilterTemp.Sex = "m";
					_tvSex.setText(R.string.SexMale);
					_ivDialogSexMale.setImageResource(R.drawable.ic_select_pressed);
					_ivDialogSexFemale.setImageResource(R.drawable.ic_select);
					_ivDialogSexNot.setImageResource(R.drawable.ic_select);
				}
				else if (pView.getId() == R.id.llMapDialogSexFemale)
				{
					_FilterTemp.Sex = "f";
					_tvSex.setText(R.string.SexFemale);
					_ivDialogSexMale.setImageResource(R.drawable.ic_select);
					_ivDialogSexFemale.setImageResource(R.drawable.ic_select_pressed);
					_ivDialogSexNot.setImageResource(R.drawable.ic_select);
				}
				else if (pView.getId() == R.id.llMapDialogSexNot)
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
				
			case R.id.llMapCountry:	
				__GetMainActivity().GetManager().GoToCountries(_FilterTemp.CountryId, false);
				break;
			case R.id.ivMapCountryNext:
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
				
			case R.id.llMapCity:	
				__GetMainActivity().GetManager().GoToCities(_FilterTemp.CountryId, _FilterTemp.CityId);
				break;
			case R.id.ivMapCityNext:
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
				
			case R.id.btnMapSearch1:	
				_FilterTemp.Friend = false;
				_llSearch1.setVisibility(View.VISIBLE);
				_llSearch2.setVisibility(View.INVISIBLE);
				break;
			case R.id.btnMapSearch2:	
				_FilterTemp.Friend = true;
				_llSearch1.setVisibility(View.INVISIBLE);
				_llSearch2.setVisibility(View.VISIBLE);
				break;
			case R.id.tvMapSearch:
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
			case R.id.ivMapSpeaker:	
				//FIXME голосовой ввод
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
		if (pAction.equals("user.list") && pStatus == 1000)
		{
			int index = 0;
			try
			{
				JSONArray users = new JSONArray(pResult);
				for (index = 0; index < users.length(); index++)
				{
					long id = users.getJSONObject(index).getLong("id");
					if (id == __GetMainActivity().GetProfile().GetID())
					{
						// чтобы самогосебя не добавить
						continue;
					}

					User user = new User(users.getJSONObject(index));
					if (user.IsFriend)
					{
						user = __GetMainActivity().GetUsers().AddUser(user, __GetMainActivity());
					}
					if (_Points.get(user.Id) == null)
					{
						_ClusterManager.addItem(user);
						_Points.put(id, user);
					}
					else
					{
						_Points.get(user.Id).Likes = user.Likes;
					}
				}
				_ClusterManager.cluster();
			}
			catch (JSONException e)
			{
				__Log("Bad data index: " + index);
			}
		}
		else if (pAction.equals("user.setgeostatus") && pStatus == 1000)
		{
			try
			{
				int status = pOptions.getInt("status");
				_FilterMain.ShowMe = status == 1 ? true : false;
				__GetMainActivity().GetProfile().SetMapStatus(status);
				if (_FilterMain.ShowMe)
				{
					_Map.setMyLocationEnabled(true);
				}
				else
				{
					_Map.setMyLocationEnabled(false);
				}
			}
			catch (JSONException e)
			{
			}
		}
	}
	

	private void _GetUsers()
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
			message.put("lat1", _Map.getProjection().getVisibleRegion().latLngBounds.southwest.latitude);
			message.put("lng1", _Map.getProjection().getVisibleRegion().latLngBounds.southwest.longitude);
			message.put("lat2", _Map.getProjection().getVisibleRegion().latLngBounds.northeast.latitude);
			message.put("lng2", _Map.getProjection().getVisibleRegion().latLngBounds.northeast.longitude);
			__SendToServer("user.list", message);
		}
		catch (JSONException e)
		{
			__Log("Bad request for filter: " + e.getMessage());
		}
	}
}