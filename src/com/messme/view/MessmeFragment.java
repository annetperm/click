package com.messme.view;

import java.util.ArrayList;

import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.chats.messages.AbstractMessageContainer;
import com.messme.socket.MessageItem;
import com.messme.user.User;
import com.messme.util.Util;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public abstract class MessmeFragment extends Fragment
{	
	public static final int CODE 			= 2;	// телефонный код страны
	public static final int PHONE 			= 3;	// телефон без кода
	public static final int PROMO 			= 4;	// промо код

	public static final int NEW				= 15;
	
	public static final int USER_ID			= 8;	
	public static final int CITY_ID			= 5;
	public static final int COUNTRY_ID		= 1;
	public static final int CITY_NAME		= 6;
	public static final int CITY_COUNTRY	= 7;
	
	public static final int CHAT_ID	 		= 51;	
	
	public static final int DELIVERY_ID		= 61;	
	
	public static final int PLACE_LAT		= 31;
	public static final int PLACE_LNG		= 32;
	
	public static final int CONTACT_NAME	= 33; 
	
	public static final int COUNT 			= 40;
	
	public static final int TYPE 			= 70;
	
	public static final int MESSAGE_ID 		= 71;
	public static final int CONTAINER_ID 	= 72;
	public static final int CONTAINER_TYPE 	= 73;
	
	public static final int UNLOCKED		= 74;
	public static final int COUNTRY_PHONE	= 75;
	
	
	private final ActivityMain _hActivity;
	private boolean _IsActive = false;
	private ArrayList<String> _MessagesId = new ArrayList<String>();
	private SparseArray<String> _Store = null;
	
	
	
	public MessmeFragment(ActivityMain pActivity, SparseArray<String> pStore)
	{
		_hActivity = pActivity;
		// не будет null никогда
		_Store = pStore;
	}
	
	
	@Override
	public final void onCreate(Bundle savedInstanceState)
	{
		__Log("Create fragment " + this.getClass().getName());
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public final View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle savedInstanceState)
	{
		__Log("CreateView fragment " + this.getClass().getName());
		View view = __OnCreateView(pInflater, pContainer, _Store);
		__GetMainActivity().MenuConfigure(this);
		_IsActive = true;
		return view;
	}
	protected abstract View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore);
	
	@Override
	public final void onStart() //монитор включился или развенуто или запустили
	{
		__Log("Start fragment " + this.getClass().getName());
		super.onStart();
	}
	
	@Override
	public final void onResume()
	{
		__Log("Resume fragment " + this.getClass().getName());
		super.onResume();
		__OnResume();
	}
	protected void __OnResume()
	{
	}
	
	@Override
	public final void onPause()
	{
		__Log("Pause fragment " + this.getClass().getName());
		super.onPause();
	}
	
	@Override
	public final void onStop()
	{
		__Log("Stop fragment " + this.getClass().getName());
		super.onStop();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		__Log("Save fragment " + this.getClass().getName());
		_IsActive = false;
		__OnSave(_Store);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public final void onDestroy()
	{
		__Log("Destroy fragment " + this.getClass().getName());
		_IsActive = false;
		__OnSave(_Store);
		__OnDestroy();
		super.onDestroy();
	}
	protected void __OnDestroy()
	{
	}
	
	
	public final void DialogClicked(int pMessageId, boolean pValue)
	{
		__Log("DialogClicked");
		if (_IsActive)
		{
			__OnDialogClicked(pMessageId, pValue);
		}
	}
	protected void __OnDialogClicked(int pMessageId, boolean pValue)
	{
	}


	public final void Connected()
	{
		__Log("Connected");
		if (_IsActive)
		{
			__OnConnected();
		}
	}
	protected void __OnConnected()
	{
	}
	

	public final boolean BackPressed()
	{
		__Log("BackPressed");
		if (_IsActive)
		{
			return __OnBackPressed();
		}
		else
		{
			return true;
		}
	}
	protected boolean __OnBackPressed()
	{
		return true;
	}
	

	public final void LocationChanged(Location pLocation)
	{
		__Log("LocationChanged");
		if (_IsActive)
		{
			__OnLocationChanged(pLocation);
		}
	}
	protected void __OnLocationChanged(Location pLocation)
	{
	}
	

	public final void ContactsChanged()
	{
		__Log("ContactsChanged");
		if (_IsActive)
		{
			__OnContactsChanged();
		}
	}
	protected void __OnContactsChanged()
	{
	}


	public final void UsersChanged(LongSparseArray<User> pUsers)
	{
		__Log("UsersChanged");
		if (_IsActive)
		{
			__OnUsersChanged(pUsers);
		}
	}
	protected void __OnUsersChanged(LongSparseArray<User> pUsers)
	{
	}


	public final void HeadersChanged(AbstractMessageContainer<?>... pChangedContainers)
	{
		__Log("HeadersChanged");
		if (_IsActive)
		{
			ArrayList<AbstractMessageContainer<?>> containers = new ArrayList<AbstractMessageContainer<?>>();
			for (int i = 0; i < pChangedContainers.length; i++)
			{
				containers.add(pChangedContainers[i]);
			}
			__OnHeadersChanged(containers);
		}
	}
	public final void HeadersChanged(ArrayList<AbstractMessageContainer<?>> pChangedContainers)
	{
		__Log("HeadersChanged");
		if (_IsActive)
		{
			__OnHeadersChanged(pChangedContainers);
		}
	}
	protected void __OnHeadersChanged(ArrayList<AbstractMessageContainer<?>> pChangedContainers)
	{
	}

	
	public final void TimerTicked()
	{
		__Log("TimerTicked");
		if (_IsActive)
		{
			__OnTimerTicked();
		}
	}
	protected void __OnTimerTicked()
	{
	}


	@Override
	public final void onActivityResult(int pRequestCode, int pResultCode, Intent pData)
	{
		super.onActivityResult(pRequestCode, pResultCode, pData);
		__Log("onActivityResult");
		if (_IsActive)
		{
			__OnActivityResult(pRequestCode, pResultCode, pData);
		}
	}
	public void ActivityResult(int pRequestCode, int pResultCode, Intent pData)
	{
		__Log("ActivityResult");
		if (_IsActive)
		{
			__OnActivityResult(pRequestCode, pResultCode, pData);
		}
	}
	protected void __OnActivityResult(int pRequestCode, int pResultCode, Intent pData)
	{
	}
	
	
	public final void MessageReceived(String pMessageId, String pAction, int pStatus, String pResult, int pType, JSONObject pOptions)
	{
		__Log("MessageReceived - Action: " + pAction);
		if (_IsActive)
		{
			switch (pType)
			{
				case MessageItem.MESSAGE_TYPE_SEND:
					// Send
					int index = -1;
					for (int i = 0; i < _MessagesId.size(); i++)
					{
						if (_MessagesId.get(i).equals(pMessageId))
						{
							index = i;
							break;
						}
					}
					if (index != -1)
					{
						_MessagesId.remove(index);
						__OnMessageReceived(pMessageId, pAction, pStatus, pResult, pOptions);
					}
					break;
				case MessageItem.MESSAGE_TYPE_MESSAGE:
					__OnMessageReceived(pMessageId, pAction, pStatus, pResult, pOptions);
					break;
			}
		}
	}
	protected void __OnMessageReceived(String pMessageId, String pAction, int pStatus, String pResult, JSONObject pOptions)
	{
	}
	
	public final void ErrorSended(String pMessageId, String pAction, JSONObject pOptions)
	{
		__Log("ErrorSended - Action: " + pAction);
		if (_IsActive)
		{
			int index = -1;
			for (int i = 0; i < _MessagesId.size(); i++)
			{
				if (_MessagesId.get(i).equals(pMessageId))
				{
					index = i;
					break;
				}
			}
			if (index != -1)
			{
				_MessagesId.remove(index);
				__OnErrorSended(pMessageId, pAction, pOptions);
			}
		}
	}
	protected void __OnErrorSended(String pMessageId, String pAction, JSONObject pOptions)
	{
	}

	
	public final void Add(SparseArray<String> pStore)
	{
		for (int i = 0; i < pStore.size(); i++)
		{
			_Store.put(pStore.keyAt(i), pStore.valueAt(i));
		}
	}
	
	public final SparseArray<String> GetStore()
	{
		return _Store;
	}

	public final void Save(Bundle pBundle, int pIndex)
	{
		if (_IsActive)
		{
			__OnSave(_Store);
		}
		pBundle.putString(Integer.toString(pIndex), this.getClass().toString());
		String key = Integer.toString(pIndex);
		pBundle.putInt(key + "s", _Store.size());
		__Log("Save: fragment - " + this.getClass().toString() + ", size - " + _Store.size());
		for (int i = 0; i < _Store.size(); i++)
		{
			pBundle.putInt(key + "k" + Integer.toString(i), _Store.keyAt(i));
			pBundle.putString(key + "v" + Integer.toString(i), _Store.valueAt(i));
			__Log("Save: key - " + _Store.keyAt(i) + ", value - " + _Store.valueAt(i));
		}
	}
	protected void __OnSave(SparseArray<String> pStore)
	{
	}

	protected final String __SendToServer(String pAction, JSONObject pOptions)
	{
		String mid = Util.GenerateMID();
		__GetMainActivity().GetServerConnection().Send(mid, pAction, pOptions);
		_MessagesId.add(mid);
		return mid;
	}
	protected final ActivityMain __GetMainActivity()
	{
		return _hActivity;
	}
	protected final void __Log(String pMessage)
	{
		Log.i(this.getClass().getName().replace("com.messme.", ""), pMessage);
	}
}