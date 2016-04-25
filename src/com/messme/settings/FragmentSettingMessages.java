package com.messme.settings;

import com.kyleduo.switchbutton.SwitchButton;
import com.messme.ActivityMain;
import com.messme.R;
import com.messme.view.MessmeFragment;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.ViewGroup;


public class FragmentSettingMessages extends MessmeFragment implements OnClickListener
{
	private SwitchButton _sbEnter;
	private TextView _tvSize;
	private SeekBar _sbSize;
	private TextView _tvTimer;
	private SeekBar _sbTimer;
	
	public FragmentSettingMessages(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		View view = pInflater.inflate(R.layout.setting_messages, pContainer, false);
		
		view.findViewById(R.id.ivSettingMessagesBack).setOnClickListener(this);
		
		view.findViewById(R.id.llSettingMessagesEnter).setOnClickListener(this);
		_sbEnter = (SwitchButton) view.findViewById(R.id.sbSettingMessagesEnter);
		_sbEnter.setChecked(__GetMainActivity().GetProfile().IsMessagesEnter());
		_sbEnter.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean pIsChecked)
			{
				__GetMainActivity().GetProfile().SetMessagesEnter(pIsChecked);
			}
		});
		
		_tvSize = (TextView) view.findViewById(R.id.tvSettingMessagesSize);
		_tvSize.setText(__GetMainActivity().getText(R.string.SettingMessagesSize) + Integer.toString(__GetMainActivity().GetProfile().GetMessagesSize()));
		_tvSize.setTextSize(__GetMainActivity().GetProfile().GetMessagesSize());
		_sbSize = (SeekBar) view.findViewById(R.id.sbSettingMessagesSize);
		_sbSize.setProgress(__GetMainActivity().GetProfile().GetMessagesSize() - 10);
		_sbSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int pProgress, boolean fromUser)
			{
				__GetMainActivity().GetProfile().SetMessagesSize(pProgress + 10);
				_tvSize.setText(__GetMainActivity().getText(R.string.SettingMessagesSize) + Integer.toString(__GetMainActivity().GetProfile().GetMessagesSize()));
				_tvSize.setTextSize(__GetMainActivity().GetProfile().GetMessagesSize());
			}
		});
		
		_tvTimer = (TextView) view.findViewById(R.id.tvSettingMessagesTimer);
		_tvTimer.setText(__GetMainActivity().getText(R.string.SettingMessagesTimer) + Integer.toString(__GetMainActivity().GetProfile().GetMessagesTimer()));
		_sbTimer = (SeekBar) view.findViewById(R.id.sbSettingMessagesTimer);
		_sbTimer.setProgress(__GetMainActivity().GetProfile().GetMessagesTimer() / 5 - 1);
		_sbTimer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int pProgress, boolean fromUser)
			{
				__GetMainActivity().GetProfile().SetMessagesTimer((pProgress + 1) * 5);
				_tvTimer.setText(__GetMainActivity().getText(R.string.SettingMessagesTimer) + Integer.toString(__GetMainActivity().GetProfile().GetMessagesTimer()));
			}
		});
		
		return view;
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivSettingMessagesBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.llSettingMessagesEnter:
				_sbEnter.setChecked(!_sbEnter.isChecked());
				break;
		}
	}
}