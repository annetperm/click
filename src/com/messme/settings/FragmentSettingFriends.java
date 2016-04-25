package com.messme.settings;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.view.MessmeFragment;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;


public class FragmentSettingFriends extends MessmeFragment implements OnClickListener
{
	public FragmentSettingFriends(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		View view = pInflater.inflate(R.layout.setting_friends, pContainer, false);
		
		view.findViewById(R.id.ivSettingFriendsBack).setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivSettingFriendsBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
		}
	}
}