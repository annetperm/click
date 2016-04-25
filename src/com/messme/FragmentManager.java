package com.messme;

import java.util.ArrayList;

import com.messme.chats.FragmentChats;
import com.messme.cities.FragmentCities;
import com.messme.contacts.FragmentContacts;
import com.messme.countries.FragmentCountries;
import com.messme.feed.FragmentFeed;
import com.messme.list.FragmentList;
import com.messme.map.FragmentMap;
import com.messme.profile.FragmentLikes;
import com.messme.profile.FragmentProfile;
import com.messme.chats.messages.AbstractMessage;
import com.messme.chats.messages.FragmentContact;
import com.messme.chats.messages.FragmentPlace;
import com.messme.chats.messages.chat.Chat;
import com.messme.chats.messages.chat.FragmentChat;
import com.messme.chats.messages.chat.FragmentGroup;
import com.messme.chats.messages.delivery.Delivery;
import com.messme.chats.messages.delivery.FragmentDelivery;
import com.messme.chats.messages.delivery.FragmentDeliveryChat;
import com.messme.chats.messages.dialog.FragmentDialog;
import com.messme.chats.messages.resend.FragmentResend;
import com.messme.chats.select.FragmentSelect;
import com.messme.registration.FragmentLogin;
import com.messme.registration.FragmentOffer;
import com.messme.registration.FragmentPromoInfo;
import com.messme.registration.FragmentRestore;
import com.messme.registration.FragmentSMS;
import com.messme.registration.FragmentStart;
import com.messme.registration.FragmentWelcome;
import com.messme.settings.FragmentSettingApplication;
import com.messme.settings.FragmentSettingFriends;
import com.messme.settings.FragmentSettingInfo;
import com.messme.settings.FragmentSettingMap;
import com.messme.settings.FragmentSettingMessages;
import com.messme.settings.FragmentSettingNotification;
import com.messme.settings.FragmentSettingSecurity;
import com.messme.settings.FragmentSettings;
import com.messme.user.FragmentMedia;
import com.messme.user.FragmentSharedGroups;
import com.messme.user.FragmentUser;
import com.messme.user.User;
import com.messme.util.Util;
import com.messme.view.MessmeFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;


public class FragmentManager
{
	private MessmeFragment _hCurrent = null;
	private ArrayList<MessmeFragment> _History = new ArrayList<MessmeFragment>();
	private ActivityMain _hActivity = null;
	
	
	public FragmentManager(ActivityMain pActivity)
	{
		_hActivity = pActivity;
        _hCurrent = null;
		_History.clear();
	}


	public boolean OnBackPressed()
	{
		if (_hCurrent != null)
		{
			if (_hCurrent.BackPressed())
			{
				if (_History.size() == 1)
				{
					return true;
				}
				else
				{
					_Log("Go back - history size: " + _History.size());
					_History.remove(_History.size() - 1);
					_GoTo(_History.get(_History.size() - 1), false);
				}
			}
		}
		return false;
	}
	

	public void GoToBack()
	{
		_hActivity.onBackPressed();
	}
	public void GoToBackWithResult(SparseArray<String> pStore)
	{
		_Log("Go back - history size: " + _History.size());
		_History.remove(_History.size() - 1);
		_History.get(_History.size() - 1).Add(pStore);
		_GoTo(_History.get(_History.size() - 1), false);
	}
	
	
	public void GoToStart()
	{
		_History.clear();
		_GoTo(new FragmentStart(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToOffer()
	{
    	_GoTo(new FragmentOffer(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToLogin()
	{
		_GoTo(new FragmentLogin(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToPromoInfo()
	{
    	_GoTo(new FragmentPromoInfo(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToSMS(String pCode, String pPhone, String pPromo)
	{
		SparseArray<String> store = new SparseArray<String>();
		store.append(MessmeFragment.CODE, pCode);
		store.append(MessmeFragment.PHONE, pPhone);
		store.append(MessmeFragment.PROMO, pPromo);
    	_GoTo(new FragmentSMS(_hActivity, store), true);
	}
	public void GoToWelcome()
	{
		_GoTo(new FragmentWelcome(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToRestore(String pCode, String pPhone)
	{
		_History.remove(_History.size() - 1);
		SparseArray<String> store = new SparseArray<String>();
		store.append(MessmeFragment.CODE, pCode);
		store.append(MessmeFragment.PHONE, pPhone);
    	_GoTo(new FragmentRestore(_hActivity, store), true);
	}
	public void GoToProfile()
	{
		_History.clear();
		SparseArray<String> store = new SparseArray<String>();
		store.append(MessmeFragment.NEW, Boolean.toString(true));
    	_GoTo(new FragmentProfile(_hActivity, store), true);
	}
	public void GoToProfile(boolean pFirst) //востановили
	{
		if (pFirst)
		{
			_History.clear();
			_History.add(new FragmentChats(_hActivity, new SparseArray<String>()));
		}

		SparseArray<String> store = new SparseArray<String>();
		store.append(MessmeFragment.NEW, Boolean.toString(false));
		
    	_GoTo(new FragmentProfile(_hActivity, store), true);
	}
	
	public void GoToCountries(int pCountryId, boolean pShowCode)
	{
		SparseArray<String> store = new SparseArray<String>();
		store.append(MessmeFragment.COUNTRY_ID, Integer.toString(pCountryId));
		store.append(MessmeFragment.COUNTRY_PHONE, Boolean.toString(pShowCode));
    	_GoTo(new FragmentCountries(_hActivity, store), true);
	}
	public void GoToCities(int pCountryId, int pCityId)
	{
		SparseArray<String> store = new SparseArray<String>();
		store.append(MessmeFragment.COUNTRY_ID, Integer.toString(pCountryId));
		store.append(MessmeFragment.CITY_ID, Integer.toString(pCityId));
    	_GoTo(new FragmentCities(_hActivity, store), true);
	}
	
	public void GoToChats()
	{
		_History.clear();
		_GoTo(new FragmentChats(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToContacts()
	{
		_History.clear();
		_History.add(new FragmentChats(_hActivity, new SparseArray<String>()));
		_GoTo(new FragmentContacts(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToMap(User pUser)
	{
		if (Util.IsGooglePlayServicesAvailable(_hActivity))
		{
			SparseArray<String> store = new SparseArray<String>();
			store.put(MessmeFragment.USER_ID, Long.toString(pUser.Id));
			_GoTo(new FragmentMap(_hActivity, store), true);
		}
	}
	public void GoToMap()
	{
		if (Util.IsGooglePlayServicesAvailable(_hActivity))
		{
			_History.clear();
			_History.add(new FragmentChats(_hActivity, new SparseArray<String>()));
			_GoTo(new FragmentMap(_hActivity, new SparseArray<String>()), true);
		}
	}
	public void GoToList()
	{
		_History.clear();
		_History.add(new FragmentChats(_hActivity, new SparseArray<String>()));
		_GoTo(new FragmentList(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToFeed()
	{
		_History.clear();
		_History.add(new FragmentChats(_hActivity, new SparseArray<String>()));
		_GoTo(new FragmentFeed(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToSettings()
	{
		_History.clear();
		_History.add(new FragmentChats(_hActivity, new SparseArray<String>()));
		_GoTo(new FragmentSettings(_hActivity, new SparseArray<String>()), true);
	}

	public void GoToSelect()
	{
		SparseArray<String> store = new SparseArray<String>();
		store.put(MessmeFragment.TYPE, Integer.toString(0));
    	_GoTo(new FragmentSelect(_hActivity, store), true);
	}
	public void GoToSelect(ArrayList<Long> pUsers, boolean pForChat)
	{
		SparseArray<String> store = new SparseArray<String>();
		if (pForChat)
		{
			store.put(MessmeFragment.TYPE, Integer.toString(1));
		}
		else
		{
			store.put(MessmeFragment.TYPE, Integer.toString(2));
		}
		store.append(MessmeFragment.COUNT, Integer.toString(pUsers.size()));
		for (int i = 0; i < pUsers.size(); i++)
		{
			store.put(1000 + i, Long.toString(pUsers.get(i)));
		}
    	_GoTo(new FragmentSelect(_hActivity, store), true);
	}

	public void GoToDialog(long pUserID)
	{
		_History.clear();
		_History.add(new FragmentChats(_hActivity, new SparseArray<String>()));
		SparseArray<String> store = new SparseArray<String>();
		store.put(MessmeFragment.UNLOCKED, Boolean.toString(false));
		store.put(MessmeFragment.USER_ID, Long.toString(pUserID));
		_GoTo(new FragmentDialog(_hActivity, store), true);
	}
	public void GoToDialog(long pUserID, boolean pUnlocked, boolean pNew)
	{
		if (pNew)
		{
			_History.remove(_History.size() - 1);
		}
		SparseArray<String> store = new SparseArray<String>();
		store.put(MessmeFragment.UNLOCKED, Boolean.toString(pUnlocked));
		store.put(MessmeFragment.USER_ID, Long.toString(pUserID));
		_GoTo(new FragmentDialog(_hActivity, store), true);
	}
	public void GoToDialog(long pUserID, boolean pUnlocked, String pContainerID, int pContainerType, String pMessageID)
	{
		_History.remove(_History.size() - 1);
		SparseArray<String> store = new SparseArray<String>();
		store.put(MessmeFragment.UNLOCKED, Boolean.toString(pUnlocked));
		store.put(MessmeFragment.USER_ID, Long.toString(pUserID));
		store.put(MessmeFragment.CONTAINER_ID, pContainerID);
		store.put(MessmeFragment.CONTAINER_TYPE, Integer.toString(pContainerType));
		store.put(MessmeFragment.MESSAGE_ID, pMessageID);
		_GoTo(new FragmentDialog(_hActivity, store), true);
	}

	public void GoToUser(long pUserID)
	{
		SparseArray<String> store = new SparseArray<String>();
		store.put(MessmeFragment.USER_ID, Long.toString(pUserID));
		_GoTo(new FragmentUser(_hActivity, store), true);
	}

	public void GoToPlace()
	{
		_GoTo(new FragmentPlace(_hActivity, new SparseArray<String>()), true);
	}

	public void GoToContact(String pFullFilename)
	{
		SparseArray<String> store = new SparseArray<String>();
		store.append(MessmeFragment.CONTACT_NAME, pFullFilename);
		_GoTo(new FragmentContact(_hActivity, store), true);
	}

	public void GoToGroup()
	{
		_GoTo(new FragmentGroup(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToGroup(Chat pChat)
	{
		SparseArray<String> store = new SparseArray<String>();
		store.put(MessmeFragment.CHAT_ID, pChat.GetID());
		_GoTo(new FragmentGroup(_hActivity, store), true);
	}

	public void GoToChat(Chat pChat)
	{
		_History.clear();
		_History.add(new FragmentChats(_hActivity, new SparseArray<String>()));
		SparseArray<String> store = new SparseArray<String>();
		store.put(MessmeFragment.UNLOCKED, Boolean.toString(false));
		store.put(MessmeFragment.CHAT_ID, pChat.GetID());
		_GoTo(new FragmentChat(_hActivity, store), true);
	}
	public void GoToChat(Chat pChat, boolean pUnlocked, boolean pNew)
	{
		if (pNew)
		{
			_History.remove(_History.size() - 1);
		}
		SparseArray<String> store = new SparseArray<String>();
		store.put(MessmeFragment.UNLOCKED, Boolean.toString(pUnlocked));
		store.put(MessmeFragment.CHAT_ID, pChat.GetID());
		_GoTo(new FragmentChat(_hActivity, store), true);
	}
	public void GoToChat(Chat pChat, boolean pUnlocked, String pContainerID, int pContainerType, String pMessageID)
	{
		_History.remove(_History.size() - 1);
		SparseArray<String> store = new SparseArray<String>();
		store.put(MessmeFragment.UNLOCKED, Boolean.toString(pUnlocked));
		store.put(MessmeFragment.CHAT_ID, pChat.GetID());
		store.put(MessmeFragment.CONTAINER_ID, pContainerID);
		store.put(MessmeFragment.CONTAINER_TYPE, Integer.toString(pContainerType));
		store.put(MessmeFragment.MESSAGE_ID, pMessageID);
		_GoTo(new FragmentChat(_hActivity, store), true);
	}

	public void GoToDelivery()
	{
		_GoTo(new FragmentDelivery(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToDelivery(Delivery pDelivery)
	{
		SparseArray<String> store = new SparseArray<String>();
		store.put(MessmeFragment.DELIVERY_ID, pDelivery.GetID());
		_GoTo(new FragmentDelivery(_hActivity, store), true);
	}
	public void GoToDeliveryChat(Delivery pDelivery, boolean pUnlocked, boolean pNew)
	{
		if (pNew)
		{
			_History.remove(_History.size() - 1);
		}
		SparseArray<String> store = new SparseArray<String>();
		store.put(MessmeFragment.UNLOCKED, Boolean.toString(pUnlocked));
		store.put(MessmeFragment.DELIVERY_ID, pDelivery.GetID());
		_GoTo(new FragmentDeliveryChat(_hActivity, store), true);
	}

	public void GoToSettingSecurity()
	{
		_GoTo(new FragmentSettingSecurity(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToSettingInfo()
	{
		_GoTo(new FragmentSettingInfo(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToSettingMap()
	{
		_GoTo(new FragmentSettingMap(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToSettingApplication()
	{
		_GoTo(new FragmentSettingApplication(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToSettingNotification()
	{
		_GoTo(new FragmentSettingNotification(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToSettingMessages()
	{
		_GoTo(new FragmentSettingMessages(_hActivity, new SparseArray<String>()), true);
	}
	public void GoToSettingFriends()
	{
		_GoTo(new FragmentSettingFriends(_hActivity, new SparseArray<String>()), true);
	}
	
	public void GoToResend(String pContainerID, int pContainerType, AbstractMessage pMessage)
	{
		SparseArray<String> store = new SparseArray<String>();
		store.put(MessmeFragment.CONTAINER_ID, pContainerID);
		store.put(MessmeFragment.CONTAINER_TYPE, Integer.toString(pContainerType));
		store.put(MessmeFragment.MESSAGE_ID, pMessage.GetID());
		_GoTo(new FragmentResend(_hActivity, store), true);
	}

	public void GoToLikes()
	{
		_GoTo(new FragmentLikes(_hActivity, new SparseArray<String>()), true);
	}

	public void GoToMedia(User pUser)
	{
		SparseArray<String> store = new SparseArray<String>();
		store.put(MessmeFragment.USER_ID, Long.toString(pUser.Id));
		_GoTo(new FragmentMedia(_hActivity, store), true);
	}


	public void GoToSharedGroups(User pUser)
	{
		SparseArray<String> store = new SparseArray<String>();
		store.put(MessmeFragment.USER_ID, Long.toString(pUser.Id));
		_GoTo(new FragmentSharedGroups(_hActivity, store), true);
	}


	public void Save(Bundle pOutState)
	{
		pOutState.putInt("size", _History.size());
		_Log("Save: size - " + _History.size());
		for (int i = 0; i < _History.size(); i++)
		{
			_History.get(i).Save(pOutState, i);
		}
	}
	public void Restore(Bundle pSavedInstanceState)
	{
    	//востановлено
    	int size = pSavedInstanceState.getInt("size");
		_Log("Restore: size - " + size);
    	for (int i = 0; i < size; i++)
    	{
    		String key = Integer.toString(i);
    		String fragmentName = pSavedInstanceState.getString(key);
    		int storeSize = pSavedInstanceState.getInt(key + "s");
    		_Log("Restore: fragment - " + fragmentName + ", size - " + storeSize);
    		SparseArray<String> store = new SparseArray<String>();
    		for (int j = 0; j < storeSize; j++)
    		{
    			int storeKey = pSavedInstanceState.getInt(key + "k" + Integer.toString(j));
    			String storeValue = pSavedInstanceState.getString(key + "v" + Integer.toString(j));
    			store.append(storeKey, storeValue);
    			_Log("Restore: key - " + storeKey + ", value - " + storeValue);
    		}
    		MessmeFragment fragment = null;
    		if (FragmentStart.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentStart(_hActivity, store);
    		}
    		else if (FragmentOffer.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentOffer(_hActivity, store);
    		}
    		else if (FragmentLogin.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentLogin(_hActivity, store);
    		}
    		else if (FragmentPromoInfo.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentPromoInfo(_hActivity, store);
    		}
    		else if (FragmentSMS.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentSMS(_hActivity, store);
    		}
    		else if (FragmentRestore.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentRestore(_hActivity, store);
    		}
    		else if (FragmentWelcome.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentWelcome(_hActivity, store);
    		}
    		else if (FragmentProfile.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentProfile(_hActivity, store);
    		}
    		else if (FragmentChats.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentChats(_hActivity, store);
    		}
    		else if (FragmentContacts.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentContacts(_hActivity, store);
    		}
    		else if (FragmentList.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentList(_hActivity, store);
    		}
    		else if (FragmentMap.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentMap(_hActivity, store);
    		}
    		else if (FragmentFeed.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentFeed(_hActivity, store);
    		}
    		else if (FragmentSettings.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentSettings(_hActivity, store);
    		}
    		else if (FragmentUser.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentUser(_hActivity, store);
    		}
    		else if (FragmentCountries.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentCountries(_hActivity, store);
    		}
    		else if (FragmentCities.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentCities(_hActivity, store);
    		}
    		else if (FragmentSelect.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentSelect(_hActivity, store);
    		}
    		else if (FragmentDialog.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentDialog(_hActivity, store);
    		}
    		else if (FragmentPlace.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentPlace(_hActivity, store);
    		}
    		else if (FragmentContact.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentContact(_hActivity, store);
    		}
    		else if (FragmentGroup.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentGroup(_hActivity, store);
    		}
    		else if (FragmentChat.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentChat(_hActivity, store);
    		}
    		else if (FragmentDelivery.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentDelivery(_hActivity, store);
    		}
    		else if (FragmentDeliveryChat.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentDeliveryChat(_hActivity, store);
    		}
    		else if (FragmentSettingSecurity.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentSettingSecurity(_hActivity, store);
    		}
    		else if (FragmentSettingInfo.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentSettingInfo(_hActivity, store);
    		}
    		else if (FragmentSettingMap.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentSettingMap(_hActivity, store);
    		}
    		else if (FragmentSettingApplication.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentSettingApplication(_hActivity, store);
    		}
    		else if (FragmentSettingNotification.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentSettingNotification(_hActivity, store);
    		}
    		else if (FragmentSettingMessages.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentSettingMessages(_hActivity, store);
    		}
    		else if (FragmentSettingFriends.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentSettingFriends(_hActivity, store);
    		}
    		else if (FragmentResend.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentResend(_hActivity, store);
    		}
    		else if (FragmentLikes.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentLikes(_hActivity, store);
    		}
    		else if (FragmentMedia.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentMedia(_hActivity, store);
    		}
    		else if (FragmentSharedGroups.class.toString().equals(fragmentName))
    		{
    			fragment = new FragmentSharedGroups(_hActivity, store);
    		}
    		//XXX востановление фрагментогв
    		else
    		{
    			Util.CreateBreakPoint(fragmentName);
    			Toast.makeText(_hActivity, "MESSME: " + fragmentName, Toast.LENGTH_LONG).show();
    		}
    		_History.add(fragment);
    	}
		_GoTo(_History.get(size - 1), false);
	}


	public void SetCurrentFragment(MessmeFragment pFragment)
	{
		_hCurrent = pFragment;
	}
	public MessmeFragment GetCurrentFragment()
	{
		return _hCurrent;
	}
	
	
	private void _GoTo(MessmeFragment pFragment, boolean pForward)
	{
		if (pForward)
		{
			_History.add(pFragment);
		}
		
    	FragmentTransaction fragmentTransaction = _hActivity.getSupportFragmentManager().beginTransaction();
		if (_hCurrent == null)
		{
			fragmentTransaction.add(R.id.flMain, pFragment);
			//fragmentTransaction.addToBackStack("");
		}
		else
		{
//			fragmentTransaction.hide(_hCurrent);
//			fragmentTransaction.add(R.id.flMain, pFragment);
//			fragmentTransaction.addToBackStack("");
			//fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_in_left);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		    fragmentTransaction.replace(R.id.flMain, pFragment);
		    //fragmentTransaction.addToBackStack(null);
			
		    _hActivity.MenuClear();
		}
		
		_hCurrent = null;
		try
		{
			fragmentTransaction.commit();
		}
		catch (Exception e)
		{
		}
	}
	
	private void _Log(String pMessage)
	{
		Log.d("FragmentManager", pMessage);
	}
}