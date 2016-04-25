package com.messme.settings;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.view.MessmeFragment;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;


public class FragmentSettings extends MessmeFragment implements OnClickListener
{
	private RelativeLayout _rlProgress = null;
	
	public FragmentSettings(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		View view = pInflater.inflate(R.layout.settings, pContainer, false);
		view.findViewById(R.id.ivSettingsMenu).setOnClickListener(this);
		view.findViewById(R.id.llSettings1).setOnClickListener(this);
		view.findViewById(R.id.llSettings2).setOnClickListener(this);
		view.findViewById(R.id.llSettings3).setOnClickListener(this);
		view.findViewById(R.id.llSettings4).setOnClickListener(this);
		view.findViewById(R.id.llSettings5).setOnClickListener(this);
		view.findViewById(R.id.llSettings6).setOnClickListener(this);
		view.findViewById(R.id.llSettings7).setOnClickListener(this);
		view.findViewById(R.id.llSettings8).setOnClickListener(this);
		view.findViewById(R.id.llSettings9).setOnClickListener(this);
		
		_rlProgress = (RelativeLayout) view.findViewById(R.id.rlSettingsProgress);
		_rlProgress.setVisibility(View.GONE);
		
		return view;
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivSettingsMenu:
				__GetMainActivity().MenuOpen();
				break;
			case R.id.llSettings1:
				__GetMainActivity().GetManager().GoToSettingSecurity();
				break;
			case R.id.llSettings2:
				__GetMainActivity().GetManager().GoToSettingInfo();
				break;
			case R.id.llSettings3:
				__GetMainActivity().GetManager().GoToSettingMap();
				break;
			case R.id.llSettings4:
				__GetMainActivity().GetManager().GoToProfile(false);
				break;
			case R.id.llSettings5:
				__GetMainActivity().GetManager().GoToSettingApplication();
				break;
			case R.id.llSettings6:
				__GetMainActivity().GetManager().GoToSettingNotification();
				break;
			case R.id.llSettings7:
				__GetMainActivity().GetManager().GoToSettingMessages();
				break;
			case R.id.llSettings8:
				__GetMainActivity().GetManager().GoToSettingFriends();
				break;
			case R.id.llSettings9:
				__GetMainActivity().OpenDialog(1, getActivity().getText(R.string.SettingsDialogHeader).toString(), "");
				break;
		}
	}
	
	@Override
	protected void __OnDialogClicked(int pMessageId, boolean pValue)
	{
		if (pMessageId == 1 && pValue)
		{
			_rlProgress.setVisibility(View.VISIBLE);
			__GetMainActivity().GetProfile().Clear(__GetMainActivity());
		}
	}
}