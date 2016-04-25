package com.messme;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.messme.chats.FragmentChats;
import com.messme.cities.City;
import com.messme.contacts.Contacts;
import com.messme.contacts.FragmentContacts;
import com.messme.countries.Countries;
import com.messme.feed.FragmentFeed;
import com.messme.list.FragmentList;
import com.messme.map.FragmentMap;
import com.messme.profile.Profile;
import com.messme.chats.messages.chat.Chats;
import com.messme.chats.messages.chat.FragmentChat;
import com.messme.chats.messages.delivery.Deliveries;
import com.messme.chats.messages.dialog.Dialogs;
import com.messme.chats.messages.dialog.FragmentDialog;
import com.messme.settings.FragmentSettings;
import com.messme.socket.MessageServerConnection;
import com.messme.user.Users;
import com.messme.util.Util;
import com.messme.view.MessmeFragment;
import com.messme.R;


public class ActivityMain extends FragmentActivity implements OnClickListener, LocationListener
{	
	private DrawerLayout _DrawerLayout;
	private ImageView _ivAvatar = null;
	private TextView _tvName = null;
	private TextView _tvLogin = null;
	private ImageView _ivChats = null;
	private ImageView _ivContacts = null;
	private ImageView _ivMap = null;
	private ImageView _ivList = null;
	private ImageView _ivFeed = null;
	private ImageView _ivSettings = null;
	private TextView _tvChats = null;
	private TextView _tvContacts = null;
	private TextView _tvMap = null;
	private TextView _tvList = null;
	private TextView _tvFeed = null;
	private TextView _tvSettings = null;
	
	private RelativeLayout _rlMessage = null;
	private TextView _tvMessageHeader = null;
	private TextView _tvMessageText = null;
	private Button _btnMessage = null;
	
	private int _DialogMessageId = 0;
	
	private FragmentManager _Manager = null;

	private MessageServerConnection _Connection = null;
	
	private Countries _Countries = null;
	private Contacts _Contacts = null;
	private SparseArray<ArrayList<City>> _Cities = new SparseArray<ArrayList<City>>();
	private Profile _Profile = null;
	private Dialogs _Dialogs = null;
	private Chats _Chats = null;
	private Deliveries _Deliveries = null;
	private Users _Users = null;
	
	private Location _LastLocation = null;
	private LocationManager _LocationManager;
	
	private boolean _ActivityVisibility = false;
	
	private NetworkStateReceiver _NetSateReceiver = null;
	
	static boolean Active = false;
	

	private Timer _MainTimer = new Timer();
	private class MainTimer extends TimerTask 
	{
		private final ActivityMain _hContext;
		
		public MainTimer(ActivityMain pContext)
		{
			_hContext = pContext;
		}
		
		@Override
		public void run() 
		{
		    try 
		    {
		    	MemoryInfo mi = new MemoryInfo();
		    	ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		    	activityManager.getMemoryInfo(mi);
		    	long availableMegs = mi.availMem / 1048576L;
		        _Log("Memory usage: " + Long.toString(availableMegs));
		    } 
		    catch (Exception e) 
		    {
		    }
		    
			_hContext.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					if (_Manager.GetCurrentFragment() != null)
					{
						_Manager.GetCurrentFragment().TimerTicked();
					}
				}
			});
		}
	}
	
	
	@Override
	protected void onCreate(Bundle pSavedInstanceState) 
	{
		super.onCreate(pSavedInstanceState);
		_Log("onCreate");
		
        setContentView(R.layout.activity_main);
        
        _Manager = new FragmentManager(this);

        _DrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        _ivAvatar = (ImageView) findViewById(R.id.ivMenuAvatar);
        _tvName = (TextView) findViewById(R.id.tvMenuName);
        _tvLogin = (TextView) findViewById(R.id.tvMenuLogin);
        findViewById(R.id.llMenuChats).setOnClickListener(this);
        _ivChats = (ImageView) findViewById(R.id.ivMenuChats);
        _tvChats = (TextView) findViewById(R.id.tvMenuChats);
        findViewById(R.id.llMenuContacts).setOnClickListener(this);
        _ivContacts = (ImageView) findViewById(R.id.ivMenuContacts);
        _tvContacts = (TextView) findViewById(R.id.tvMenuContacts);
        findViewById(R.id.llMenuMap).setOnClickListener(this);
        _ivMap = (ImageView) findViewById(R.id.ivMenuMap);
        _tvMap = (TextView) findViewById(R.id.tvMenuMap);
        findViewById(R.id.llMenuList).setOnClickListener(this);
        _ivList = (ImageView) findViewById(R.id.ivMenuList);
        _tvList = (TextView) findViewById(R.id.tvMenuList);
        findViewById(R.id.llMenuFeed).setOnClickListener(this);
        _ivFeed = (ImageView) findViewById(R.id.ivMenuFeed);
        _tvFeed = (TextView) findViewById(R.id.tvMenuFeed);
        findViewById(R.id.llMenuSettings).setOnClickListener(this);
        _ivSettings = (ImageView) findViewById(R.id.ivMenuSettings);
        _tvSettings = (TextView) findViewById(R.id.tvMenuSettings);
        
        _rlMessage = (RelativeLayout) findViewById(R.id.rlMessage);
        _rlMessage.setOnClickListener(this);
        _tvMessageHeader = (TextView) findViewById(R.id.tvMessageHeader);
        _tvMessageText = (TextView) findViewById(R.id.tvMessageText);
        _btnMessage = (Button) findViewById(R.id.btnMessage);
        _btnMessage.setOnClickListener(this);

		_LocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		_Profile = new Profile(this);
		_Contacts = new Contacts(this);
		_Users = new Users(this);
		_Dialogs = new Dialogs(this);
		_Chats = new Chats(this, _Profile);
		_Deliveries = new Deliveries(this);
        _Countries = new Countries(this);
		
        _MainTimer.schedule(new MainTimer(this), 60000, 60000);
        
        _NetSateReceiver = new NetworkStateReceiver();
		registerReceiver(_NetSateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(Push.TYPE))
        {
        	_Log("<<<NOTIFICATION>>>");
	    	_Connection = new MessageServerConnection(this);
			MenuUpdate();
        	onNewIntent(getIntent());
        }
        else
        {
        	// проверка запушено приложение или востановлено
            if (pSavedInstanceState == null)
            {
            	_Log("<<<START>>>");
            	//запущено
            	if (_Profile.GetID() == 0)
        	    {
        	    	// приложение не зарегистрированно
        	    	_Manager.GoToStart();
        	    }
        	    else if (_Profile.GetLogin().equals(""))
        	    {
        	    	// пользователь не авторизован 
        	    	//ConnectToServer("User", "");
        	    	_Manager.GoToProfile();
        	    }
        	    else
        	    {
        	    	_Connection = new MessageServerConnection(this);
        			MenuUpdate();
    	        	_Manager.GoToChats();
        	    }
            }
            else
            {
            	_Log("<<<RESTORE>>>");
            	//ворстановление
            	if (_Profile.GetID() == 0 || _Profile.GetLogin().equals(""))
    		    {
    		    }
    		    else
    		    {
        	    	_Connection = new MessageServerConnection(this);
    		    	MenuUpdate();
    		    }
            	_Manager.Restore(pSavedInstanceState);
            }
        }
        
        Active = true;
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		_Log("onStart");
		if (_Connection != null && _Connection.IsStrated() && _Profile.IsInitial() && _Contacts.IsSynchronized() && _Contacts.Update())
		{
			_Contacts.Synchronizing();
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		_Log("onResume");
		LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false);
		criteria.setPowerRequirement(Criteria.POWER_LOW);      
		 
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		String providerFine = manager.getBestProvider(criteria, true);
		 
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		String providerCoarse = manager.getBestProvider(criteria, true);
		 
		if (providerCoarse != null) 
		{
			_LocationManager.requestLocationUpdates(providerCoarse, 10000, 1, this);
		}
		if (providerFine != null) 
		{
			_LocationManager.requestLocationUpdates(providerFine, 10000, 1, this);
		}
		_ActivityVisibility = true;
		if (_Connection != null && _Connection.IsStrated())
		{
			try
			{
				JSONObject options = new JSONObject();
				options.put("status", 1);
				_Connection.Send(Util.GenerateMID(), "user.setstatus", options);
			}
			catch (JSONException e)
			{
			}
		}
	}
	@Override
	protected void onPause()
	{
		_Log("onPause");
		_LocationManager.removeUpdates(this);
		super.onPause();
		_ActivityVisibility = false;
		if (_Connection != null && _Connection.IsStrated())
		{
			try
			{
				JSONObject options = new JSONObject();
				options.put("status", 0);
				_Connection.Send(Util.GenerateMID(), "user.setstatus", options);
			}
			catch (JSONException e)
			{
			}
		}
	}
	@Override
	protected void onDestroy()
	{
		_Log("onDestroy");
		Active = false;
		_MainTimer.cancel();
		unregisterReceiver(_NetSateReceiver);
//    	Intent intent = new Intent(this, MessmeService.class);
//    	intent.putExtra(MessmeService.EXIT, true);
//        startService(intent);
        //unregisterReceiver(_BroadcastReceiver);
		super.onDestroy();
	}
	
	@Override
	protected void onNewIntent(Intent pIntent)
	{
    	_Log("onNewIntent");
		super.onNewIntent(pIntent);
		
		if (pIntent != null && pIntent.getExtras() != null && pIntent.getExtras().containsKey(Push.TYPE))
        {
        	switch (pIntent.getExtras().getInt(Push.TYPE, Push.TYPE_ALL))
			{
				case Push.TYPE_ALL:
					if (_Manager != null 
						&& _Manager.GetCurrentFragment() != null 
						&& _Manager.GetCurrentFragment() instanceof FragmentChats)
					{
					}
					else
					{
	    	        	_Manager.GoToChats();
					}
					break;
				case Push.TYPE_DIALOG:
					long userID = pIntent.getExtras().getLong(Push.ID);
					if (_Manager != null 
							&& _Manager.GetCurrentFragment() != null 
							&& _Manager.GetCurrentFragment() instanceof FragmentDialog 
							&& ((FragmentDialog) _Manager.GetCurrentFragment()).GetUserID() == userID)
					{
					}
					else
					{
	    	        	_Manager.GoToDialog(userID);
					}
					break;
				case Push.TYPE_CHAT:
					String chatID = pIntent.getExtras().getString(Push.ID);
					if (_Manager != null 
							&& _Manager.GetCurrentFragment() != null 
							&& _Manager.GetCurrentFragment() instanceof FragmentChat 
							&& ((FragmentChat) _Manager.GetCurrentFragment()).GetChat().GetID().equals(chatID))
					{
					}
					else
					{
	    	        	_Manager.GoToChat(_Chats.GetChat(chatID));
					}
					break;
			}
        }
	}

	
	@Override
	protected void onSaveInstanceState(Bundle pOutState)
	{
		_Manager.Save(pOutState);
	}
	
	@Override
	protected void onActivityResult(int pRequestCode, int pResultCode, Intent pData)
	{
		if (_Manager.GetCurrentFragment() != null)
		{
			_Manager.GetCurrentFragment().ActivityResult(pRequestCode, pResultCode, pData);
		}
		super.onActivityResult(pRequestCode, pResultCode, pData);
	}

	@Override
	public void onClick(View pView)
	{
		_Log("onClick");
		try
		{
			switch (pView.getId())
			{
				case R.id.llMenuChats:
					if (!_Manager.GetCurrentFragment().getClass().toString().equals(FragmentChats.class.toString()))
					{
						_Manager.GoToChats();
					}
					_DrawerLayout.closeDrawers();
					break;
				case R.id.llMenuContacts:
					if (!_Manager.GetCurrentFragment().getClass().toString().equals(FragmentContacts.class.toString()))
					{
						_Manager.GoToContacts();
					}
					_DrawerLayout.closeDrawers();
					break;
				case R.id.llMenuMap:
					if (!_Manager.GetCurrentFragment().getClass().toString().equals(FragmentMap.class.toString()))
					{
						_Manager.GoToMap();
					}
					_DrawerLayout.closeDrawers();
					break;
				case R.id.llMenuList:
					if (!_Manager.GetCurrentFragment().getClass().toString().equals(FragmentList.class.toString()))
					{
						_Manager.GoToList();
					}
					_DrawerLayout.closeDrawers();
					break;
				case R.id.llMenuFeed:
					if (!_Manager.GetCurrentFragment().getClass().toString().equals(FragmentFeed.class.toString()))
					{
						_Manager.GoToFeed();
					}
					_DrawerLayout.closeDrawers();
					break;
				case R.id.llMenuSettings:
					if (!_Manager.GetCurrentFragment().getClass().toString().equals(FragmentSettings.class.toString()))
					{
						_Manager.GoToSettings();
					}
					_DrawerLayout.closeDrawers();
					break;
				case R.id.rlMessage:
					_OnDialogClick(false);
					break;
				case R.id.btnMessage:
					_OnDialogClick(true);
					break;
			}
		}
		catch(Exception e)
		{
		}
	}	
	
	
	@Override
	public void onBackPressed()
	{
		_Log("onBackPressed");
		if (_rlMessage.getVisibility() == View.VISIBLE)
		{
			_OnDialogClick(false);
		}
		else
		{
			if (_Manager.OnBackPressed())
			{
				super.onBackPressed();
			}
		}
	}

	@Override
	public void onLocationChanged(Location pLocation)
	{
		_Log("onLocationChanged");
		boolean b = false;
		if (_LastLocation == null)
		{
			b = true;
		}
		else if (_LastLocation.distanceTo(pLocation) > 5)
		{
			b = true;
		}
		if (b)
		{
			_Log("New location: " + pLocation.getLatitude() + "/" + pLocation.getLongitude());
			_LastLocation = pLocation;

			if (_Manager.GetCurrentFragment() != null)
			{
				_Manager.GetCurrentFragment().LocationChanged(_LastLocation);
			}
			
			if (_Profile.GetMapStatus() != 0)
			{
				if (_Connection != null && _Connection.IsStrated())
				{
					try
					{
						JSONObject options = new JSONObject();
						options.put("lat", _LastLocation.getLatitude());
						options.put("lng", _LastLocation.getLongitude());
						_Connection.Send(Util.GenerateMID(), "user.setgeo", options);
					}
					catch (JSONException e)
					{
					}
				}
			}
		}
	}
	@Override
	public void onProviderDisabled(String provider)
	{
	}
	@Override
	public void onProviderEnabled(String provider)
	{
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
	}

	
	public void OpenDialog(int pId, String pHeader, String pText)
	{
		_DialogMessageId = pId;
		_tvMessageHeader.setText(pHeader);
		_tvMessageText.setText(pText);
		_DrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		_rlMessage.setVisibility(View.VISIBLE);
	}
	private void _OnDialogClick(boolean pValue)
	{
		_rlMessage.setVisibility(View.GONE);
		if (_Manager.GetCurrentFragment().getClass().toString().equals(FragmentChats.class.toString()) || 
				_Manager.GetCurrentFragment().getClass().toString().equals(FragmentContacts.class.toString()) ||
				_Manager.GetCurrentFragment().getClass().toString().equals(FragmentMap.class.toString()) ||
				_Manager.GetCurrentFragment().getClass().toString().equals(FragmentList.class.toString()) ||
				_Manager.GetCurrentFragment().getClass().toString().equals(FragmentFeed.class.toString()) ||
				_Manager.GetCurrentFragment().getClass().toString().equals(FragmentSettings.class.toString()))
		{
	        _DrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		}
		else
		{
			_DrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}
		if (_Manager.GetCurrentFragment() != null && _DialogMessageId != 0)
		{
			_Manager.GetCurrentFragment().DialogClicked(_DialogMessageId, pValue);
		}
		_DialogMessageId = 0;
	}
	
	
	public void MenuOpen()
	{
		_DrawerLayout.openDrawer(Gravity.LEFT);
	}
	public void MenuLock()
	{
		_DrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
	}
	public void MenuUnlock()
	{
        _DrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
	}
	public void MenuUpdate()
	{
		_tvName.setText(_Profile.GetName() + " " + _Profile.GetSurname());
		_tvLogin.setText(_Profile.GetLogin());
		_ivAvatar.setImageBitmap(_Profile.GetBigAvatarImage());		
	}
	public void MenuClear()
	{
		if (_Manager.GetCurrentFragment().getClass().toString().equals(FragmentChats.class.toString()))
		{
			_ivChats.setImageResource(R.drawable.selector_chats);
			_tvChats.setTextColor(getResources().getColorStateList(R.color.color_menu));
		}
		else if (_Manager.GetCurrentFragment().getClass().toString().equals(FragmentContacts.class.toString()))
		{
			_ivContacts.setImageResource(R.drawable.selector_contacts);
			_tvContacts.setTextColor(getResources().getColorStateList(R.color.color_menu));
		}
		else if (_Manager.GetCurrentFragment().getClass().toString().equals(FragmentMap.class.toString()))
		{
			_ivMap.setImageResource(R.drawable.selector_place);
			_tvMap.setTextColor(getResources().getColorStateList(R.color.color_menu));
		}
		else if (_Manager.GetCurrentFragment().getClass().toString().equals(FragmentList.class.toString()))
		{
			_ivList.setImageResource(R.drawable.selector_list);
			_tvList.setTextColor(getResources().getColorStateList(R.color.color_menu));
		}
		else if (_Manager.GetCurrentFragment().getClass().toString().equals(FragmentFeed.class.toString()))
		{
			_ivFeed.setImageResource(R.drawable.selector_feed);
			_tvFeed.setTextColor(getResources().getColorStateList(R.color.color_menu));
		}
		else if (_Manager.GetCurrentFragment().getClass().toString().equals(FragmentSettings.class.toString()))
		{
			_ivSettings.setImageResource(R.drawable.selector_settings);
			_tvSettings.setTextColor(getResources().getColorStateList(R.color.color_menu));
		}
	}
	public void MenuConfigure(MessmeFragment pFragment)
	{		
		_Manager.SetCurrentFragment(pFragment);

		if (pFragment.getClass().toString().equals(FragmentChats.class.toString()))
		{
	        _DrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			_ivChats.setImageResource(R.drawable.ic_chats_pressed);
			_tvChats.setTextColor(getResources().getColor(R.color.TextBlue));
		}
		else if (pFragment.getClass().toString().equals(FragmentContacts.class.toString()))
		{
	        _DrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			_ivContacts.setImageResource(R.drawable.ic_contacts_pressed);
			_tvContacts.setTextColor(getResources().getColor(R.color.TextBlue));
		}
		else if (pFragment.getClass().toString().equals(FragmentMap.class.toString()))
		{
	        _DrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			_ivMap.setImageResource(R.drawable.ic_place_pressed);
			_tvMap.setTextColor(getResources().getColor(R.color.TextBlue));
		}
		else if (pFragment.getClass().toString().equals(FragmentList.class.toString()))
		{
	        _DrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			_ivList.setImageResource(R.drawable.ic_list_pressed);
			_tvList.setTextColor(getResources().getColor(R.color.TextBlue));
		}
		else if (pFragment.getClass().toString().equals(FragmentFeed.class.toString()))
		{
	        _DrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			_ivFeed.setImageResource(R.drawable.ic_feed_pressed);
			_tvFeed.setTextColor(getResources().getColor(R.color.TextBlue));
		}
		else if (pFragment.getClass().toString().equals(FragmentSettings.class.toString()))
		{
	        _DrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			_ivSettings.setImageResource(R.drawable.ic_settings_pressed);
			_tvSettings.setTextColor(getResources().getColor(R.color.TextBlue));
		}
		else
		{
			_DrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}
	}
	
	
	public Countries GetCountries()
	{
		return _Countries;
	}
	public Location GetLocation()
	{
		return _LastLocation;
	}
	public SparseArray<ArrayList<City>> GetCities()
	{
		return _Cities;
	}
	public FragmentManager GetManager()
	{
		return _Manager;
	}
	public Profile GetProfile()
	{
		return _Profile;
	}
	public Dialogs GetDialogs()
	{
		return _Dialogs;
	}
	public Chats GetChats()
	{
		return _Chats;
	}
	public Users GetUsers()
	{
		return _Users;
	}
	public Contacts GetContacts()
	{
		return _Contacts;
	}
	public Deliveries GetDeliveries()
	{
		return _Deliveries;
	}
	public MessageServerConnection GetServerConnection()
	{
		return _Connection;
	}

	public void CreateServerConnection()
	{
		_Connection = new MessageServerConnection(this);
	}
	public void DestroyServerConnection()
	{
		_Connection = null;
	}

	public void InternetConnection()
	{
		if (_Connection != null)
		{
			_Connection.Reconnect(this);
		}
	}
	
	
	public boolean IsVisible()
	{
		return _ActivityVisibility;
	}

	private void _Log(String pMessage)
	{
		Log.d("Activity", pMessage);
	}
}