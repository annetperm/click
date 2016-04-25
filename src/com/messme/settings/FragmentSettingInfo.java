package com.messme.settings;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.view.MessmeFragment;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;


public class FragmentSettingInfo extends MessmeFragment implements OnClickListener
{
	public FragmentSettingInfo(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		View view = pInflater.inflate(R.layout.setting_info, pContainer, false);
		
		view.findViewById(R.id.ivSettingInfoBack).setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivSettingInfoBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
		}
	}
}