package com.messme.settings;

import com.kyleduo.switchbutton.SwitchButton;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.messme.ActivityMain;
import com.messme.R;
import com.messme.view.MessmeFragment;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class FragmentSettingNotification extends MessmeFragment implements OnClickListener
{
	private RelativeLayout _rlDialog;
	private ColorPicker _cp;
	
	private SwitchButton _sbMain;
	private LinearLayout _llLight;
	private ImageView _ivLight;
	private TextView _tvLight;
	private LinearLayout _llVibration;
	private TextView _tvVibration;
	private SwitchButton _sbVibration;
	
	public FragmentSettingNotification(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		View view = pInflater.inflate(R.layout.setting_notification, pContainer, false);
		
		view.findViewById(R.id.ivSettingNotificationBack).setOnClickListener(this);
		
		view.findViewById(R.id.llSettingNotificationMain).setOnClickListener(this);
		_sbMain = (SwitchButton) view.findViewById(R.id.sbSettingNotificationMain);
		
		_llLight = (LinearLayout) view.findViewById(R.id.llSettingNotificationLight);
		_llLight.setOnClickListener(this);
		_tvLight = (TextView) view.findViewById(R.id.tvSettingNotificationLight);
		_ivLight = (ImageView) view.findViewById(R.id.ivSettingNotificationLight);
		
		_llVibration = (LinearLayout) view.findViewById(R.id.llSettingNotificationVibration);
		_llVibration.setOnClickListener(this);
		_tvVibration = (TextView) view.findViewById(R.id.tvSettingNotificationVibration);
		_sbVibration = (SwitchButton) view.findViewById(R.id.sbSettingNotificationVibration);

		view.findViewById(R.id.btnSettingNotificationDialogCancel).setOnClickListener(this);
		view.findViewById(R.id.btnSettingNotificationDialogOK).setOnClickListener(this);
		_rlDialog = (RelativeLayout) view.findViewById(R.id.rlSettingNotificationDialog);
		_rlDialog.setOnClickListener(this);
		_rlDialog.setVisibility(View.GONE);
		
		_cp = (ColorPicker) view.findViewById(R.id.cpSettingNotificationDialog);
		
		_SetSettings();
		
		return view;
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivSettingNotificationBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.llSettingNotificationMain:
				__GetMainActivity().GetProfile().SetNotification(!__GetMainActivity().GetProfile().IsNotification());
				_SetSettings();
				break;
			case R.id.llSettingNotificationLight:
				_cp.setOldCenterColor(__GetMainActivity().GetProfile().GetNotificationColor());
				_rlDialog.setVisibility(View.VISIBLE);
				break;
			case R.id.llSettingNotificationVibration:
				__GetMainActivity().GetProfile().SetNotificationVibrate(!__GetMainActivity().GetProfile().IsNotificationVibrate());
				_SetSettings();
				break;
			case R.id.rlSettingNotificationDialog:
			case R.id.btnSettingNotificationDialogCancel:
				_rlDialog.setVisibility(View.GONE);
				break;
			case R.id.btnSettingNotificationDialogOK:
				_rlDialog.setVisibility(View.GONE);
				__GetMainActivity().GetProfile().SetNotificationColor(_cp.getColor());
				_SetSettings();
				break;
		}
	}

	private void _SetSettings()
	{
		if (__GetMainActivity().GetProfile().IsNotification())
		{
			_llLight.setEnabled(true);
			_tvLight.setTextColor(getResources().getColorStateList(R.color.color_btn_black));
			_llVibration.setEnabled(true);
			_tvVibration.setTextColor(getResources().getColorStateList(R.color.color_btn_black));
		}
		else
		{
			_llLight.setEnabled(false);
			_tvLight.setTextColor(getResources().getColor(R.color.TextSub));
			_llVibration.setEnabled(false);
			_tvVibration.setTextColor(getResources().getColor(R.color.TextSub));
		}
		
		_sbMain.setOnCheckedChangeListener(null);
		_sbMain.setChecked(__GetMainActivity().GetProfile().IsNotification());
		_sbMain.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				__GetMainActivity().GetProfile().SetNotification(!__GetMainActivity().GetProfile().IsNotification());
				_SetSettings();
			}
		});
		_ivLight.setColorFilter(__GetMainActivity().GetProfile().GetNotificationColor());
		_sbVibration.setOnCheckedChangeListener(null);
		_sbVibration.setChecked(__GetMainActivity().GetProfile().IsNotificationVibrate());
		_sbVibration.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				__GetMainActivity().GetProfile().SetNotificationVibrate(!__GetMainActivity().GetProfile().IsNotificationVibrate());
				_SetSettings();
			}
		});
	}
}