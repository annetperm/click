package com.messme.settings;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.view.MessmeFragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;


public class FragmentSettingApplication extends MessmeFragment implements OnClickListener
{
	private static final int _RESULT_GALLERY = 3001;
	
	private ImageView _ivTheme1 = null;
	private ImageView _ivTheme2 = null;
	private ImageView _ivTheme3 = null;
	private ImageView _ivTheme = null;
	
	private Bitmap _Theme = null;
	
	public FragmentSettingApplication(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		View view = pInflater.inflate(R.layout.setting_application, pContainer, false);
		
		view.findViewById(R.id.ivSettingApplicationBack).setOnClickListener(this);
		
		((CheckBox) view.findViewById(R.id.cbSettingApplicationMobilePhoto)).setChecked(__GetMainActivity().GetProfile().IsMPhoto());
		((CheckBox) view.findViewById(R.id.cbSettingApplicationMobileAudio)).setChecked(__GetMainActivity().GetProfile().IsMAudio());
		((CheckBox) view.findViewById(R.id.cbSettingApplicationMobileVideo)).setChecked(__GetMainActivity().GetProfile().IsMVideo());
		((CheckBox) view.findViewById(R.id.cbSettingApplicationMobileFile)).setChecked(__GetMainActivity().GetProfile().IsMFiles());
		
		((CheckBox) view.findViewById(R.id.cbSettingApplicationWifiPhoto)).setChecked(__GetMainActivity().GetProfile().IsWPhoto());
		((CheckBox) view.findViewById(R.id.cbSettingApplicationWifiAudio)).setChecked(__GetMainActivity().GetProfile().IsWAudio());
		((CheckBox) view.findViewById(R.id.cbSettingApplicationWifiVideo)).setChecked(__GetMainActivity().GetProfile().IsWVideo());
		((CheckBox) view.findViewById(R.id.cbSettingApplicationWifiFile)).setChecked(__GetMainActivity().GetProfile().IsWFiles());
		
		((CheckBox) view.findViewById(R.id.cbSettingApplicationRoamingPhoto)).setChecked(__GetMainActivity().GetProfile().IsRPhoto());
		((CheckBox) view.findViewById(R.id.cbSettingApplicationRoamingAudio)).setChecked(__GetMainActivity().GetProfile().IsRAudio());
		((CheckBox) view.findViewById(R.id.cbSettingApplicationRoamingVideo)).setChecked(__GetMainActivity().GetProfile().IsRVideo());
		((CheckBox) view.findViewById(R.id.cbSettingApplicationRoamingFile)).setChecked(__GetMainActivity().GetProfile().IsRFiles());
		
		_ivTheme1 = (ImageView) view.findViewById(R.id.ivSettingApplicationTheme1Check);
		_ivTheme2 = (ImageView) view.findViewById(R.id.ivSettingApplicationTheme2Check);
		_ivTheme3 = (ImageView) view.findViewById(R.id.ivSettingApplicationTheme3Check);
		_ivTheme = (ImageView) view.findViewById(R.id.ivSettingApplicationTheme3);
		switch (__GetMainActivity().GetProfile().GetThemeId())
		{
			case 0:
				_ivTheme1.setVisibility(View.VISIBLE);
				_ivTheme2.setVisibility(View.GONE);
				_ivTheme3.setVisibility(View.GONE);
				break;
			case 1:
				_ivTheme1.setVisibility(View.GONE);
				_ivTheme2.setVisibility(View.VISIBLE);
				_ivTheme3.setVisibility(View.GONE);
				break;
			case -1:
				_ivTheme1.setVisibility(View.GONE);
				_ivTheme2.setVisibility(View.GONE);
				_ivTheme3.setVisibility(View.VISIBLE);
				_ivTheme.setImageDrawable(__GetMainActivity().GetProfile().GetTheme());
				break;
		}
		
		view.findViewById(R.id.flSettingApplicationTheme1).setOnClickListener(this);
		view.findViewById(R.id.flSettingApplicationTheme2).setOnClickListener(this);
		view.findViewById(R.id.flSettingApplicationTheme3).setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivSettingApplicationBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.flSettingApplicationTheme1:
				_ivTheme1.setVisibility(View.VISIBLE);
				_ivTheme2.setVisibility(View.GONE);
				_ivTheme3.setVisibility(View.GONE);
				break;
			case R.id.flSettingApplicationTheme2:
				_ivTheme1.setVisibility(View.GONE);
				_ivTheme2.setVisibility(View.VISIBLE);
				_ivTheme3.setVisibility(View.GONE);
				break;
			case R.id.flSettingApplicationTheme3:
				Intent intentGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(Intent.createChooser(intentGallery, getActivity().getText(R.string.ProfileChoose)), _RESULT_GALLERY);
				break;
		}
	}
	
	@Override
	protected void __OnActivityResult(int pRequestCode, int pResultCode, Intent pData)
	{
		if (pResultCode == ActivityMain.RESULT_OK && pRequestCode == _RESULT_GALLERY && pData != null) 
		{
			Cursor cursor = __GetMainActivity().getContentResolver().query(pData.getData(), new String[]{MediaStore.Images.Media.DATA}, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		    cursor.moveToFirst();
		    String imagePath = cursor.getString(column_index);
			cursor.close();
            
            _Theme = BitmapFactory.decodeFile(imagePath);
            
			_ivTheme1.setVisibility(View.GONE);
			_ivTheme2.setVisibility(View.GONE);
			_ivTheme3.setVisibility(View.VISIBLE);
			_ivTheme.setImageBitmap(_Theme);
        }
	}
	
	@Override
	protected boolean __OnBackPressed()
	{
		__GetMainActivity().GetProfile().SetDownloadSettings(((CheckBox) getView().findViewById(R.id.cbSettingApplicationMobilePhoto)).isChecked()
			, ((CheckBox) getView().findViewById(R.id.cbSettingApplicationMobileAudio)).isChecked()
			, ((CheckBox) getView().findViewById(R.id.cbSettingApplicationMobileVideo)).isChecked()
			, ((CheckBox) getView().findViewById(R.id.cbSettingApplicationMobileFile)).isChecked()
			, ((CheckBox) getView().findViewById(R.id.cbSettingApplicationWifiPhoto)).isChecked()
			, ((CheckBox) getView().findViewById(R.id.cbSettingApplicationWifiAudio)).isChecked()
			, ((CheckBox) getView().findViewById(R.id.cbSettingApplicationWifiVideo)).isChecked()
			, ((CheckBox) getView().findViewById(R.id.cbSettingApplicationWifiFile)).isChecked()
			, ((CheckBox) getView().findViewById(R.id.cbSettingApplicationRoamingPhoto)).isChecked()
			, ((CheckBox) getView().findViewById(R.id.cbSettingApplicationRoamingAudio)).isChecked()
			, ((CheckBox) getView().findViewById(R.id.cbSettingApplicationRoamingVideo)).isChecked()
			, ((CheckBox) getView().findViewById(R.id.cbSettingApplicationRoamingFile)).isChecked());
		
		if (_ivTheme1.getVisibility() == View.VISIBLE)
		{
			__GetMainActivity().GetProfile().SetThemeId(0);
			__GetMainActivity().GetProfile().SetTheme(null);
		}
		else if (_ivTheme2.getVisibility() == View.VISIBLE)
		{
			__GetMainActivity().GetProfile().SetThemeId(1);
			__GetMainActivity().GetProfile().SetTheme(null);
		}
		else if (_ivTheme3.getVisibility() == View.VISIBLE)
		{
			if (_Theme == null)
			{
				__GetMainActivity().GetProfile().SetThemeId(0);
				__GetMainActivity().GetProfile().SetTheme(null);
			}
			else
			{
				__GetMainActivity().GetProfile().SetThemeId(-1);
				__GetMainActivity().GetProfile().SetTheme(_Theme);
			}
		}
			
		return super.__OnBackPressed();
	}
}