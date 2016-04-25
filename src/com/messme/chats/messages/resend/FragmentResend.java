package com.messme.chats.messages.resend;

import java.util.ArrayList;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.messages.AbstractMessageContainer;
import com.messme.view.MessmeFragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;


public class FragmentResend extends MessmeFragment implements OnClickListener
{
	private class ResendFragmentPagerAdapter extends android.support.v13.app.FragmentStatePagerAdapter 
	{
		public ResendFragmentPagerAdapter(FragmentManager pFragmentManager) 
		{
			super(pFragmentManager);
		}
		@Override
		public Fragment getItem(int pPosition) 
		{
			if (pPosition == 0)
			{
				return new ResendListFragment(__GetMainActivity(), 1, _ContainerID, _ContainerType, _MessageID);
			}
			else if (pPosition == 1)
			{
				return new ResendListFragment(__GetMainActivity(), 2, _ContainerID, _ContainerType, _MessageID);
			}
			else //if (pPosition == 2)
			{
				return new SecretListFragment(__GetMainActivity(), _ContainerID, _ContainerType, _MessageID);
			}
		}
		@Override
		public int getCount() 
		{
			return 3;
		}
		@Override
		public CharSequence getPageTitle(int pPosition)
		{
			if (pPosition == 0)
			{
				return __GetMainActivity().getText(R.string.ResendContacts);
			}
			else if (pPosition == 1)
			{
				return __GetMainActivity().getText(R.string.ResendChats);
			}
			else
			{
				return __GetMainActivity().getText(R.string.ResendSecret);
			}
		}
		@Override
		public int getItemPosition(Object object) 
		{
		    return POSITION_NONE;
		}
	}
	
	
	private ViewPager _ViewPager = null; 
	private ResendFragmentPagerAdapter _PagerAdapter = null;
	private String _ContainerID;
	private int _ContainerType;
	private String _MessageID;
	
	
	public FragmentResend(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
		_ContainerID = pStore.get(CONTAINER_ID);
		_ContainerType = Integer.parseInt(pStore.get(CONTAINER_TYPE));
		_MessageID = pStore.get(MESSAGE_ID);
	}

	
	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		View view = pInflater.inflate(R.layout.resend, pContainer, false);
		
		view.findViewById(R.id.ivResendBack).setOnClickListener(this);
		
		_ViewPager = (ViewPager) view.findViewById(R.id.vpResend);
		_PagerAdapter = new ResendFragmentPagerAdapter(__GetMainActivity().getFragmentManager());
		_ViewPager.setAdapter(_PagerAdapter);
	    
		return view;
	}
	
	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivResendBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
		}
	}
	
	@Override
	protected void __OnHeadersChanged(ArrayList<AbstractMessageContainer<?>> pChangedContainers)
	{
		if (_PagerAdapter != null)
		{
			_PagerAdapter.notifyDataSetChanged();
		}
	}
}