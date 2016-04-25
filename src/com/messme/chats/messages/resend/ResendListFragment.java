package com.messme.chats.messages.resend;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.messages.chat.Chat;
import com.messme.chats.messages.dialog.Dialog;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


public class ResendListFragment extends Fragment implements OnItemClickListener
{
	private final ActivityMain _hActivity;
	private final int _Type;
	private final String _ContainerID;
	private final int _ContainerType;
	private final String _MessageID;
	private AdapterResendChats _AdapterChats = null;
	
	public ResendListFragment(ActivityMain pActivity, int pType, String pContainerID, int pContainerType, String pMessageID)
	{
		_hActivity = pActivity;
		_Type = pType;
		_ContainerID = pContainerID;
		_ContainerType = pContainerType;
		_MessageID = pMessageID;
	}
	
	@Override
	public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) 
	{
		View view = pInflater.inflate(R.layout.resend_fragment, pContainer, false);
		ListView lv = (ListView) view.findViewById(R.id.lvResendFragment);
		lv.setOnItemClickListener(this);
		if (_Type == 1)
		{
			lv.setAdapter(new AdapterResendContacts(_hActivity));
		}
		else
		{
			_AdapterChats = new AdapterResendChats(_hActivity, _ContainerID, _ContainerType, true);
			lv.setAdapter(_AdapterChats);
		}
	    return view;
	}

	@Override
	public void onItemClick(AdapterView<?> pParent, View pView, int pPosition, long pID)
	{
		if (_Type == 1)
		{
			_hActivity.GetManager().GoToDialog(pID, false, _ContainerID, _ContainerType, _MessageID);
		}
		else
		{
			if (_AdapterChats.getItem(pPosition) instanceof Dialog)
			{
				Dialog dialog = (Dialog) _AdapterChats.getItem(pPosition);
				_hActivity.GetManager().GoToDialog(dialog.GetUserID(), false, _ContainerID, _ContainerType, _MessageID);
			}
			else
			{
				Chat chat = (Chat) _AdapterChats.getItem(pPosition);
				_hActivity.GetManager().GoToChat(chat, false, _ContainerID, _ContainerType, _MessageID);
			}
		}
	}

	public void Update()
	{
		if (_Type == 2)
		{
			_AdapterChats.notifyDataSetChanged();
		}
	}
}