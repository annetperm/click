package com.messme.settings;

import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.view.MessmeFragment;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;


public class FragmentSettingMap extends MessmeFragment implements OnClickListener, OnCheckedChangeListener
{
	public FragmentSettingMap(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		View view = pInflater.inflate(R.layout.setting_map, pContainer, false);
		
		view.findViewById(R.id.ivSettingMapBack).setOnClickListener(this);
		
		switch (__GetMainActivity().GetProfile().GetMapStatus())
		{
			case 1:
				((RadioButton) view.findViewById(R.id.rbSettingMapStatus1)).setChecked(true);
				break;
			case 2:
				((RadioButton) view.findViewById(R.id.rbSettingMapStatus2)).setChecked(true);
				break;
			case 0:
				((RadioButton) view.findViewById(R.id.rbSettingMapStatus3)).setChecked(true);
				break;
		}
		
		((RadioButton) view.findViewById(R.id.rbSettingMapStatus1)).setOnCheckedChangeListener(this);
		((RadioButton) view.findViewById(R.id.rbSettingMapStatus2)).setOnCheckedChangeListener(this);
		((RadioButton) view.findViewById(R.id.rbSettingMapStatus3)).setOnCheckedChangeListener(this);
		
		return view;
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivSettingMapBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton pButtonView, boolean pIsChecked)
	{
		if (pIsChecked)
		{
			int status = 0;
			switch (pButtonView.getId())
			{
				case R.id.rbSettingMapStatus1:
					status = 1;
					break;
				case R.id.rbSettingMapStatus2:
					status = 2;
					break;
				case R.id.rbSettingMapStatus3:
					status = 0;
					break;
			}
			try
			{
				JSONObject options = new JSONObject();
				options.put("status", status);
				__SendToServer("user.setgeostatus", options);
			}
			catch (JSONException e)
			{
			}
		}
	}
	
	@Override
	protected void __OnMessageReceived(String pMessageId, String pAction, int pStatus, String pResult, JSONObject pOptions)
	{
		if (pAction.equals("user.setgeostatus") && pStatus == 1000)
		{
			try
			{
				int status = pOptions.getInt("status");
				__GetMainActivity().GetProfile().SetMapStatus(status);
			}
			catch (JSONException e)
			{
			}
		}
	}
}