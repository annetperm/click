package com.messme.profile;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.countries.Countries.Country;
import com.messme.socket.CommunicationSocket;
import com.messme.socket.MessageItem;
import com.messme.util.DateUtil;
import com.messme.util.ImageUtil;
import com.messme.util.Util;
import com.messme.view.MessmeFragment;
import com.messme.view.TextWatcher;
import com.messme.view.TextWatcher.iOnTextChanged;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class FragmentProfile extends MessmeFragment implements OnClickListener, iOnTextChanged, OnFocusChangeListener, CommunicationSocket.iSocketListener
{
	private static final int _RESULT_CAMERA = 3000;
	private static final int _RESULT_GALLERY = 3001;
	private static final int _RESULT_IMAGE = 3002;
	
	private static final int _IV_CHECK		= 100;
	private static final int _NAME 			= 101;
	private static final int _SURNAME 		= 102;
	private static final int _SEX 			= 103;
	private static final int _BIRTHDAY 		= 104;
	private static final int _AVATAR 		= 105;
	private static final int _LOGIN 		= 106;
	
	
	private ImageView _ivAvatar = null;
	private ImageView _ivOK = null;
	private ImageView _ivChangePhoto = null;
	
	private EditText _etLogin = null;
	private ImageView _ivCheck = null;
	
	private ImageView _ivInfo = null;
	private LinearLayout _llInfo = null;
	
	private EditText _etName = null;
	private ImageView _ivName = null;
	
	private EditText _etSurname = null;
	private ImageView _ivSurname = null;
	
	private TextView _tvSex = null;
	private ImageView _ivSex = null;
	
	private TextView _tvDate = null;
	private ImageView _ivDate = null;
	
	private ImageView _ivPlace = null;
	private LinearLayout _llPlace = null;

	private ImageView _ivCountryFlag = null;
	private TextView _tvCountry = null;
	private ImageView _ivCountry = null;
	
	private ImageView _ivCity = null;
	private TextView _tvCity = null;

	private RelativeLayout _rlDialogSex = null;
	private ImageView _ivDialogSexMale = null;
	private ImageView _ivDialogSexFemale = null;
	private ImageView _ivDialogSexNot = null;
	
	private RelativeLayout _rlDialogPhoto = null;
	private LinearLayout _llDialogPhotoDelete = null;
	
	private RelativeLayout _rlDialogChanges = null;
	
	private RelativeLayout _rlProgress = null;

	private TextWatcher _SearchTextWatcher;
	private TextWatcher _NameTextWatcher;
	private TextWatcher _SurnameTextWatcher;
	
	private boolean _IsNew;
	private boolean _Check;
	private String _Avatar;
	private String _Login;
	private String _Name;
	private String _Surname;
	private String _Sex;
	private String _BirthDay;
	private Country _Country;
	private String _CityName;
	private int _CityID;
	private int _CityCountry;
	
	private boolean _UserCreated = false;
	private boolean _Trying = true;
	private boolean _Canceled = false;
	
	private CommunicationSocket _Socket;	

	private boolean _CheckWork = false;
	
	private boolean _First = true;
	
	public FragmentProfile(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);		
		_SearchTextWatcher = new TextWatcher(this, 1);
		_NameTextWatcher = new TextWatcher(this, 2);
		_SurnameTextWatcher = new TextWatcher(this, 3);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		_CheckWork = false;
		_IsNew = pStore.get(NEW) == null ? false : Boolean.parseBoolean(pStore.get(NEW));
		_Check = pStore.get(_IV_CHECK) == null ? false : Boolean.parseBoolean(pStore.get(_IV_CHECK));
		_Avatar = pStore.get(_AVATAR) == null ? __GetMainActivity().GetProfile().GetAvatarBase64() : pStore.get(_AVATAR);
		_Login = pStore.get(_LOGIN) == null ? __GetMainActivity().GetProfile().GetLogin() : pStore.get(_LOGIN);
		_Name = pStore.get(_NAME) == null ? __GetMainActivity().GetProfile().GetName() : pStore.get(_NAME);
		_Surname = pStore.get(_SURNAME) == null ? __GetMainActivity().GetProfile().GetSurname() : pStore.get(_SURNAME);
		_Sex = pStore.get(_SEX) == null ? __GetMainActivity().GetProfile().GetSex() : pStore.get(_SEX);
		_BirthDay = pStore.get(_BIRTHDAY) == null ? __GetMainActivity().GetProfile().GetBirthday() : pStore.get(_BIRTHDAY);
		int countryID = pStore.get(COUNTRY_ID) == null ? __GetMainActivity().GetProfile().GetCountryId() : Integer.parseInt(pStore.get(COUNTRY_ID));
		if (countryID == 0)
		{
			if (_IsNew && _First)
			{
				_Country = __GetMainActivity().GetCountries().GetCurrent();
			}
			else
			{
				_Country = null;
			}
		}
		else
		{
			_Country = __GetMainActivity().GetCountries().GetById(countryID);
		}
		_CityID = pStore.get(CITY_ID) == null ? __GetMainActivity().GetProfile().GetCityId() : Integer.parseInt(pStore.get(CITY_ID));
		_CityName = pStore.get(CITY_NAME) == null ? __GetMainActivity().GetProfile().GetCityName() : pStore.get(CITY_NAME);
		_CityCountry = pStore.get(CITY_COUNTRY) == null ? __GetMainActivity().GetProfile().GetCityCountry() : Integer.parseInt(pStore.get(CITY_COUNTRY));
		
		View view = pInflater.inflate(R.layout.profile, pContainer, false);

		if (_IsNew)
		{
			_Socket = new CommunicationSocket(this);
			_Socket.Connect("User/?id=" + __GetMainActivity().GetProfile().GetID());
			view.findViewById(R.id.ivProfileBack).setVisibility(View.INVISIBLE);
		}
		else
		{
			if (__GetMainActivity().GetServerConnection() == null)
			{
				__GetMainActivity().CreateServerConnection();
			}
			view.findViewById(R.id.ivProfileBack).setOnClickListener(this);
		}

		_ivOK = (ImageView) view.findViewById(R.id.ivProfileOk);
		_ivOK.setOnClickListener(this);
		if (_IsNew)
		{
			if (_Check)
			{
				_ivOK.setVisibility(View.VISIBLE);
			}
			else
			{
				_ivOK.setVisibility(View.INVISIBLE);
			}
			view.findViewById(R.id.llProfileLikes).setVisibility(View.INVISIBLE);
		}
		else
		{
			_ivOK.setVisibility(View.VISIBLE);
			view.findViewById(R.id.llProfileLikes).setVisibility(View.VISIBLE);
			view.findViewById(R.id.llProfileLikes).setOnClickListener(this);
			((TextView) view.findViewById(R.id.tvProfileLikes)).setText(__GetMainActivity().GetProfile().GetLikes());
		}
		
		_ivChangePhoto = (ImageView) view.findViewById(R.id.ivProfileChangePhoto);
		_ivChangePhoto.setOnClickListener(this);
		
		view.findViewById(R.id.llProfileShadow).bringToFront();
		
		_ivAvatar = (ImageView) view.findViewById(R.id.ivProfileAfatar);
		if (_Avatar.length() != 0)
		{
			byte[] encodeByte = Base64.decode(_Avatar, Base64.DEFAULT);
			_ivAvatar.setImageBitmap(BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length));
		}
		else
		{
			_ivAvatar.setImageResource(R.drawable.drawable_background_blue);
		}
		
		TextView tvPhone = (TextView) view.findViewById(R.id.tvProfilePhone);
		String phone = Long.toString(__GetMainActivity().GetProfile().GetID());
		tvPhone.setText(Util.GetPhone(phone, __GetMainActivity().GetProfile().GetLocale()));

		_etLogin = (EditText) view.findViewById(R.id.etProfileLogin);
		_etLogin.setText(_Login);
		_etLogin.setOnFocusChangeListener(this);
		if (_IsNew)
		{
			_etLogin.addTextChangedListener(_SearchTextWatcher);
			_etLogin.setEnabled(true);
		}
		else
		{
			_etLogin.setEnabled(false);
		}
		_ivCheck = (ImageView) view.findViewById(R.id.ivProfileCheck);
		if (_IsNew)
		{
			if (_Check)
			{
				_ivCheck.setImageResource(R.drawable.ic_ok_green);
			}
			else
			{
				_ivCheck.setImageResource(R.drawable.ic_cancel_red);
			}
		}
		else
		{
			_ivCheck.setImageResource(R.drawable.ic_ok_green);
		}
		
		view.findViewById(R.id.llProfileInfoClick).setOnClickListener(this);
		_ivInfo = (ImageView) view.findViewById(R.id.ivProfileInfoClick);
		_llInfo = (LinearLayout) view.findViewById(R.id.llProfileInfo);
		_ivName = (ImageView) view.findViewById(R.id.ivProfileName);
		_etName = (EditText) view.findViewById(R.id.etProfileName);
		_etName.setOnFocusChangeListener(this);
		_etName.addTextChangedListener(_NameTextWatcher);
		_etName.setText(_Name);

		_ivSurname = (ImageView) view.findViewById(R.id.ivProfileSurname);
		_etSurname = (EditText) view.findViewById(R.id.etProfileSurname);
		_etSurname.setOnFocusChangeListener(this);
		_etSurname.addTextChangedListener(_SurnameTextWatcher);
		_etSurname.setText(_Surname);

		view.findViewById(R.id.llProfileSex).setOnClickListener(this);
		_ivSex = (ImageView) view.findViewById(R.id.ivProfileSexNext);
		_tvSex = (TextView) view.findViewById(R.id.tvProfileSex);

		view.findViewById(R.id.llProfileDate).setOnClickListener(this);
		_ivDate = (ImageView) view.findViewById(R.id.ivProfileDateNext);
		_tvDate = (TextView) view.findViewById(R.id.tvProfileDate);

		view.findViewById(R.id.llProfilePlaceClick).setOnClickListener(this);
		_ivPlace = (ImageView) view.findViewById(R.id.ivProfilePlaceClick);
		_llPlace = (LinearLayout) view.findViewById(R.id.llProfilePlace);
		view.findViewById(R.id.llProfileCountry).setOnClickListener(this);
		_ivCountryFlag = (ImageView) view.findViewById(R.id.ivProfileCountry);
		_tvCountry = (TextView) view.findViewById(R.id.tvProfileCountry);
		_ivCountry = (ImageView) view.findViewById(R.id.ivProfileCountryNext);
		if (countryID == 0)
		{
			if (_IsNew)
			{
				_ivCountryFlag.setImageBitmap(_Country.Flag);
				_tvCountry.setText(_Country.Name);
				_ivCountry.setOnClickListener(this);
				_ivCountry.setImageResource(R.drawable.selector_delete);
			}
			else
			{
				_ivCountryFlag.setImageResource(R.drawable.ic_flag_0);
				_tvCountry.setText(getActivity().getText(R.string.SelectCountry));
				_ivCountry.setImageResource(R.drawable.selector_next);
			}
		}
		else
		{
			_ivCountryFlag.setImageBitmap(_Country.Flag);
			_tvCountry.setText(_Country.Name);
			_ivCountry.setOnClickListener(this);
			_ivCountry.setImageResource(R.drawable.selector_delete);
		}
		
		view.findViewById(R.id.llProfileCity).setOnClickListener(this);
		_ivCity = (ImageView) view.findViewById(R.id.ivProfileCityNext);
		_tvCity = (TextView) view.findViewById(R.id.tvProfileCity);
		if (_CityID == 0)
		{
			_ClearCity();
		}
		else if (_CityCountry == countryID)
		{
			_tvCity.setText(_CityName);
			_ivCity.setOnClickListener(this);
			_ivCity.setImageResource(R.drawable.selector_delete);
		}
		else
		{
			_ClearCity();
		}

		
		_rlDialogSex = (RelativeLayout) view.findViewById(R.id.rlProfileDialogSex);
		_rlDialogSex.setVisibility(View.GONE);
		_rlDialogSex.setOnClickListener(this);
		view.findViewById(R.id.llProfileDialogSexMale).setOnClickListener(this);
		view.findViewById(R.id.llProfileDialogSexFemale).setOnClickListener(this);
		view.findViewById(R.id.llProfileDialogSexNot).setOnClickListener(this);
		_ivDialogSexMale = ((ImageView) view.findViewById(R.id.ivProfileDialogSexMale));
		_ivDialogSexFemale = ((ImageView) view.findViewById(R.id.ivProfileDialogSexFemale));
		_ivDialogSexNot = ((ImageView) view.findViewById(R.id.ivProfileDialogSexNot));

		_rlDialogPhoto = (RelativeLayout) view.findViewById(R.id.rlProfileDialogPhoto);
		_rlDialogPhoto.setVisibility(View.GONE);
		_rlDialogPhoto.setOnClickListener(this);
		view.findViewById(R.id.llProfileDialogPhotoCamera).setOnClickListener(this);
		view.findViewById(R.id.llProfileDialogPhotoGallery).setOnClickListener(this);
		_llDialogPhotoDelete = (LinearLayout) view.findViewById(R.id.llProfileDialogPhotoDelete);
		_llDialogPhotoDelete.setOnClickListener(this);
		if (_Avatar.length() != 0)
		{
			_llDialogPhotoDelete.setVisibility(View.VISIBLE);
		}
		else
		{
			_llDialogPhotoDelete.setVisibility(View.GONE);
		}
		
		_rlDialogChanges = (RelativeLayout) view.findViewById(R.id.rlProfileChanges);
		_rlDialogChanges.setVisibility(View.GONE);
		_rlDialogChanges.setOnClickListener(this);
		view.findViewById(R.id.btnProfileChangesNo).setOnClickListener(this);
		view.findViewById(R.id.btnProfileChangesYes).setOnClickListener(this);

		_rlProgress = (RelativeLayout) view.findViewById(R.id.rlProfileProgress);
		_rlProgress.setVisibility(View.GONE);
		
		_SetSex();
		_SetBirthday();
		
		return view;
	}
	
	@Override
	protected void __OnResume()
	{
		_CheckWork = true;
	}

	@Override
	protected void __OnSave(SparseArray<String> pStore)
	{
		pStore.put(_AVATAR, _Avatar);
		pStore.put(_LOGIN, _Login);
		pStore.put(_NAME, _Name);
		pStore.put(_SURNAME, _Surname);
		pStore.put(_SEX, _Sex);
		pStore.put(_BIRTHDAY, _BirthDay);
		pStore.put(COUNTRY_ID, _Country == null ? "0" : Integer.toString(_Country.Id));
		pStore.put(CITY_ID, Integer.toString(_CityID));
		pStore.put(CITY_NAME, _CityName);
		pStore.put(CITY_COUNTRY, Integer.toString(_CityCountry));
		pStore.put(_IV_CHECK, Boolean.toString(_Check));
	}
	
	@Override
	protected void __OnDestroy()
	{
		_etLogin.removeTextChangedListener(_SearchTextWatcher);
		_etName.removeTextChangedListener(_NameTextWatcher);
		_etSurname.removeTextChangedListener(_SurnameTextWatcher);
		if (_Socket != null)
		{
			_Socket.Disconnect();
			_Socket = null;
		}
	}

	@Override
	public void onFocusChange(View pView, boolean pHasFocus)
	{
		if (!pHasFocus)
		{
			((EditText) pView).setSelection(0);
		}
	}
	
	@Override
	public void onClick(View pView)
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(_etLogin.getWindowToken(), 0);
	    imm.hideSoftInputFromWindow(_etName.getWindowToken(), 0);
	    imm.hideSoftInputFromWindow(_etSurname.getWindowToken(), 0);
		switch (pView.getId())
		{
			case R.id.ivProfileBack:
				if (_CheckChanges())
				{
					_rlDialogChanges.setVisibility(View.VISIBLE);
				}
				else
				{
					__GetMainActivity().GetManager().GoToBack();
				}
				break;
			case R.id.llProfileLikes:
				__GetMainActivity().GetManager().GoToLikes();
				break;
			case R.id.ivProfileChangePhoto:
				_rlDialogPhoto.setVisibility(View.VISIBLE);
				break;
			case R.id.rlProfileDialogPhoto:
				_rlDialogPhoto.setVisibility(View.GONE);
				break;
			case R.id.llProfileDialogPhotoCamera:
				_rlDialogPhoto.setVisibility(View.GONE);
				Intent intentCamera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intentCamera, _RESULT_CAMERA);
				break;
			case R.id.llProfileDialogPhotoGallery:
				_rlDialogPhoto.setVisibility(View.GONE);
				Intent intentGallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(Intent.createChooser(intentGallery, getActivity().getText(R.string.ProfileChoose)), _RESULT_GALLERY);
				break;
			case R.id.llProfileDialogPhotoDelete:
				_rlDialogPhoto.setVisibility(View.GONE);
				_Avatar = "";
				_ivAvatar.setImageResource(R.drawable.drawable_background_blue);
				_llDialogPhotoDelete.setVisibility(View.GONE);
				break;
//			case R.id.ivChangePhoto:
//				try
//				{    
////		            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
////		            
////		            Intent galleryIntent = new Intent(Intent.ACTION_PICK).setType("image/*");
////		            
////		            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
////		            chooserIntent.putExtra(Intent.EXTRA_INTENT, galleryIntent);      
////		            chooserIntent.putExtra(Intent.EXTRA_TITLE, getActivity().getText(R.string.ProfileChoose));
////		            //if (_Avatar.length() != 0)
////		            {
////		            	Intent deleteIntent = new Intent(Intent.ACTION_DELETE);
////		            	deleteIntent.setAction("android.intent.action.DELETE");
////		            	deleteIntent.addCategory("android.intent.category.DEFAULT");
////		            	Uri packageName = Uri.parse("package:");
////		            	deleteIntent.setData(packageName);	
////		            	
////			            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[] { cameraIntent , deleteIntent});
//////			            chooserIntent.putExtra(Intent.EXTRA_INTENT, share ); 
////		            }
//////		            else
//////		            {
//////			            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[] { cameraIntent });
//////		            }
////		            startActivityForResult(chooserIntent, _RESULTCODE);
//		        }
//		        catch(Exception e)
//				{
//		        }
//				String[] menu = null;
//				if (_Avatar.length() == 0)
//				{
//					menu = new String[]{getActivity().getText(R.string.AvatarCamera).toString(), getActivity().getText(R.string.AvatarGallery).toString()};
//				}
//				else
//				{
//					menu = new String[]{getActivity().getText(R.string.AvatarCamera).toString(), getActivity().getText(R.string.AvatarGallery).toString(), getActivity().getText(R.string.AvatarDelete).toString()};
//				}
//				
//			    new AlertDialog.Builder(getActivity()).setItems(menu, new DialogInterface.OnClickListener() 
//			    {
//	                @Override
//	                public void onClick(DialogInterface pDialog, int pIndex) 
//	                {
//	                	switch (pIndex)
//						{
//							case 0:
//								Intent intentCamera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//								startActivityForResult(intentCamera, _RESULT_CAMERA);
//								break;
//							case 1:
//								Intent intentGallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//								startActivityForResult(Intent.createChooser(intentGallery, getActivity().getText(R.string.ProfileChoose)), _RESULT_GALLERY);
//								break;
//							case 2:
//								_Avatar = "";
//								_ivAvatar.setImageResource(R.color.BackgroundBlue);
//								break;
//						}
//	                }
//	            }).show();
//				break;
				
			case R.id.llProfileInfoClick:
				if (_llInfo.getVisibility() == View.VISIBLE)
				{
					_llInfo.setVisibility(View.GONE);
					_ivInfo.setImageResource(R.drawable.selector_down);
				}
				else
				{
					_llInfo.setVisibility(View.VISIBLE);
					_ivInfo.setImageResource(R.drawable.selector_up);
				}
				break;
				
			case R.id.ivProfileName:
				_etName.setText("");
				break;
				
			case R.id.ivProfileSurname:
				_etSurname.setText("");
				break;
				
			case R.id.rlProfileDialogSex:
				_rlDialogSex.setVisibility(View.GONE);
				break;
			case R.id.llProfileSex:
				_rlDialogSex.setVisibility(View.VISIBLE);
				break;
			case R.id.llProfileDialogSexMale:
				_Sex = "m";
				_SetSex();
				_rlDialogSex.setVisibility(View.GONE);
				break;
			case R.id.llProfileDialogSexFemale:
				_Sex = "f";
				_SetSex();
				_rlDialogSex.setVisibility(View.GONE);
				break;
			case R.id.llProfileDialogSexNot:
				_Sex = "";
				_SetSex();
				_rlDialogSex.setVisibility(View.GONE);
				break;
			case R.id.ivProfileSexNext:
				_Sex = "";
				_SetSex();
				break;

			case R.id.llProfileDate:
				Calendar cal = Calendar.getInstance();
				if (_BirthDay.length() > 0)
				{
			        cal.set(Calendar.YEAR, Integer.parseInt(_BirthDay.substring(6, 10)));
			        cal.set(Calendar.MONTH, Integer.parseInt(_BirthDay.substring(3, 5)) - 1);
			        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(_BirthDay.substring(0, 2)));
				}
				Dialog picker = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog, new DatePickerDialog.OnDateSetListener()
				{
					@Override
					public void onDateSet(DatePicker view, int pYear, int pMonthOfYear, int pDayOfMonth)
					{  
						Calendar cal = Calendar.getInstance();
				        cal.set(Calendar.YEAR, pYear);
				        cal.set(Calendar.MONTH, pMonthOfYear);
				        cal.set(Calendar.DAY_OF_MONTH, pDayOfMonth);
				        if (new Date().getTime() - cal.getTime().getTime() > 1000l * 60l * 60l * 24l * 365l * 14l)
				        {
							_BirthDay = DateUtil.GetDate(cal.getTime());
							_SetBirthday();
				        }
				        else
				        {
							__GetMainActivity().OpenDialog(2, __GetMainActivity().getString(R.string.Dialog52Title), __GetMainActivity().getString(R.string.Dialog52Description));
				        }
					}
				}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		        picker.setTitle(getActivity().getText(R.string.ProfileChooseBirthday));
		        picker.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		        picker.show();
				break;
			case R.id.ivProfileDateNext:
				_BirthDay = "";
				_SetBirthday();
				break;

			case R.id.llProfilePlaceClick:
				if (_llPlace.getVisibility() == View.VISIBLE)
				{
					_llPlace.setVisibility(View.GONE);
					_ivPlace.setImageResource(R.drawable.selector_down);
				}
				else
				{
					_llPlace.setVisibility(View.VISIBLE);
					_ivPlace.setImageResource(R.drawable.selector_up);
				}
				break;
			case R.id.llProfileCountry:
				__GetMainActivity().GetManager().GoToCountries(_Country == null ? -1 : _Country.Id, false);
				break;
			case R.id.ivProfileCountryNext:
				_Country = null;
				_ivCountryFlag.setImageResource(R.drawable.ic_flag_0);
				_tvCountry.setText(getActivity().getText(R.string.SelectCountry));
				_ivCountry.setImageResource(R.drawable.selector_next);

				_ClearCity();
				break;
				
			case R.id.llProfileCity:
				if (_Country != null)
				{
					__GetMainActivity().GetManager().GoToCities(_Country.Id, _CityID);
				}
				break;
			case R.id.ivProfileCityNext:
				_ClearCity();
				break;
				
			case R.id.rlProfileChanges:
				_rlDialogChanges.setVisibility(View.GONE);
				break;
			case R.id.btnProfileChangesNo:
				_Canceled = true;
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.btnProfileChangesYes:
				_rlDialogChanges.setVisibility(View.GONE);
				_rlProgress.setVisibility(View.VISIBLE);
				try
				{
					JSONObject options = new JSONObject();
					options.put("name", _Name);
					options.put("surname", _Surname);
					options.put("birthdate", _BirthDay);
					options.put("sex", _Sex);
					options.put("country", _Country == null ? 0 : _Country.Id);
					options.put("city", _CityID);
					options.put("avatar", _Avatar.equals(__GetMainActivity().GetProfile().GetAvatarBase64()) ? "" : _Avatar);
					__SendToServer("user.update", options);
				}
				catch (JSONException e)
				{
				}
				break;
				
			case R.id.ivProfileOk:
				_rlProgress.setVisibility(View.VISIBLE);
				try
				{
					if (_UserCreated)
					{
						__SendToServer("user.clearfriend", null);
					}
					else
					{
						if (_IsNew)
						{
							JSONObject options = new JSONObject();
							options.put("name", _Name);
							options.put("surname", _Surname);
							options.put("birthdate", _BirthDay);
							options.put("sex", _Sex);
							options.put("country", _Country == null ? 0 : _Country.Id);
							options.put("city", _CityID);
							options.put("avatar", _Avatar);
							options.put("login", _Login);
							_Socket.Send("user.create", options);
						}
						else
						{
							JSONObject options = new JSONObject();
							options.put("name", _Name);
							options.put("surname", _Surname);
							options.put("birthdate", _BirthDay);
							options.put("sex", _Sex);
							options.put("country", _Country == null ? 0 : _Country.Id);
							options.put("city", _CityID);
							options.put("avatar", _Avatar.equals(__GetMainActivity().GetProfile().GetAvatarBase64()) ? "" : _Avatar);
							__SendToServer("user.update", options);
						}
					}
				}
				catch (JSONException e)
				{
				}
				break;
		}
	}

	@Override
	protected boolean __OnBackPressed()
	{
		if (_rlDialogSex.getVisibility() == View.VISIBLE)
		{
			_rlDialogSex.setVisibility(View.GONE);
			return false;
		}
		if (_Canceled)
		{
			return true;
		}
		if (!_IsNew && _CheckChanges())
		{
			_rlDialogChanges.setVisibility(View.VISIBLE);
			return false;
		}
		return true;
	}
	
	@Override
	protected void __OnActivityResult(int pRequestCode, int pResultCode, Intent pData)
	{
		if (pResultCode == ActivityMain.RESULT_OK && (pRequestCode == _RESULT_CAMERA || pRequestCode == _RESULT_GALLERY))  
        {
			Intent cropIntent = new Intent("com.android.camera.action.CROP"); 
			cropIntent.setDataAndType(pData.getData(), "image/*");
			cropIntent.putExtra("crop", "true");
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			cropIntent.putExtra("outputX", 256);
			cropIntent.putExtra("outputY", 256);
			cropIntent.putExtra("return-data", true);
			startActivityForResult(cropIntent, _RESULT_IMAGE);
        }
		else if (pResultCode == ActivityMain.RESULT_OK && pRequestCode == _RESULT_IMAGE)
		{
			Bitmap imageBitmap = null;
			if (pData.hasExtra("data"))
			{
				Bundle extras = pData.getExtras();
	        	imageBitmap = (Bitmap) extras.get("data");
			}
			else
			{
				Uri selectedImageUri = pData == null ? null : pData.getData();
		        try
				{
					imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
				}
				catch (Exception e)
				{
				}
			}
			
			if (imageBitmap != null)
			{
				imageBitmap = ImageUtil.ResizeImage(imageBitmap);
            	
            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
            	imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
				_Avatar = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
				
	            _ivAvatar.setImageBitmap(imageBitmap);
			}
		}
	}

	@Override
	public void OnTextChanged(int pID, String pText)
	{
		switch (pID)
		{
			case 1:
				if (_CheckWork == false)
				{
					return;
				}
				_Login = pText;
				_ivCheck.setImageResource(R.drawable.ic_cancel_red);
				_ivOK.setVisibility(View.INVISIBLE);
				_Check = false;
				boolean b = true;
				if (pText.length() < 5)
				{
					b = false;
				}
				else
				{
					String all = "qwertyuiopasdfghjklzxcvbnm1234567890_";
					String lower = pText.toLowerCase();
					for (int i = 0; i < lower.length(); i++)
					{
						String ch = Character.toString(lower.charAt(i));
						if (!all.contains(ch))
						{
							b = false;
							break;
						}
					}
				}
				
				if (b)
				{
					try
					{
						JSONObject options = new JSONObject();
						options.put("login", pText);
						if (_Socket == null)
						{
							__SendToServer("user.checklogin", options);
						}
						else
						{
							_Socket.Send("user.checklogin", options);
						}
					}
					catch (JSONException e)
					{
					}
				}
				break;
				
			case 2:
				_Name = pText;
				if (pText.length() == 0)
				{
					_ivName.setImageResource(R.drawable.ic_edit);
					_ivName.setOnClickListener(null);
				}
				else
				{
					_ivName.setImageResource(R.drawable.selector_delete);
					_ivName.setOnClickListener(FragmentProfile.this);
				}
				break;
				
			case 3:
				_Surname = pText;
				if (pText.length() == 0)
				{
					_ivSurname.setImageResource(R.drawable.ic_edit);
					_ivSurname.setOnClickListener(null);
				}
				else
				{
					_ivSurname.setImageResource(R.drawable.selector_delete);
					_ivSurname.setOnClickListener(FragmentProfile.this);
				}
				break;
		}
	}

	@Override
	public void OnConnect()
	{
	}

	@Override
	public void OnMessageSendError(MessageItem pItem, boolean pTimeOut)
	{
		if (pItem.GetAction().equals("user.create"))
		{
			__GetMainActivity().OpenDialog(2, __GetMainActivity().getString(R.string.Dialog24Title), __GetMainActivity().getString(R.string.Dialog24Description));
		}
		_rlProgress.setVisibility(View.GONE);
	}
	@Override
	public void OnMessageSendSuccess(MessageItem pItem)
	{
	}
	@Override
	public void OnMessageReceive(String pMID, String pAction, int pStatus, String pResult, int pType, JSONObject pOptions)
	{
		if (pAction.equals("user.checklogin"))
		{
			try
			{
				if (pStatus == 1000 && pOptions.getString("login").equals(_Login))
				{
					_ivCheck.setImageResource(R.drawable.ic_ok_green);
					_ivOK.setVisibility(View.VISIBLE);
					_Check = true;
				}
				else
				{
					_ivCheck.setImageResource(R.drawable.ic_cancel_red);
					_Check = false;
				}
			}
			catch (JSONException e)
			{
				_ivCheck.setImageResource(R.drawable.ic_cancel_red);
				_Check = false;
			}
		}
		else if (pAction.equals("user.create"))
		{
			if (pStatus == 1000)
			{
				_UserCreated = true;
				// пользователь создан надо удалить его страые данные
				_Socket.Disconnect();
				_Socket = null;
				__GetMainActivity().CreateServerConnection();
			}
			else
			{
				_rlProgress.setVisibility(View.GONE);
				__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog26Title), __GetMainActivity().getString(R.string.Dialog26Description));
			}
		}
	}
	
	@Override
	protected void __OnConnected()
	{
		if (_IsNew && _Trying)
		{
			// дождались подключения теперь грохаем старых друзей
			__SendToServer("user.clearfriend", null);
		}
	}
	@Override
	protected void __OnErrorSended(String pMessageId, String pAction, JSONObject pOptions)
	{
		if (pAction.equals("user.clearfriend") || pAction.equals("user.cleanallhistory"))
		{
			// пользователь создан
			_Trying = false;
			__GetMainActivity().OpenDialog(2, __GetMainActivity().getString(R.string.Dialog22Title), __GetMainActivity().getString(R.string.Dialog22Description));
		}
		else if (pAction.equals("user.update") || pAction.equals("user.deleteavatar"))
		{
			__GetMainActivity().OpenDialog(2, __GetMainActivity().getString(R.string.Dialog23Title), __GetMainActivity().getString(R.string.Dialog23Description));
		}
		_rlProgress.setVisibility(View.GONE);
	}
	@Override
	protected void __OnMessageReceived(String pMessageId, String pAction, int pStatus, String pResult, JSONObject pOptions)
	{
		if (pAction.equals("user.checklogin"))
		{
			try
			{
				if (pStatus == 1000 && pOptions.getString("login").equals(_Login))
				{
					_ivCheck.setImageResource(R.drawable.ic_ok_green);
					_ivOK.setVisibility(View.VISIBLE);
					_Check = true;
				}
				else
				{
					_ivCheck.setImageResource(R.drawable.ic_cancel_red);
					_Check = false;
				}
			}
			catch (JSONException e)
			{
				_ivCheck.setImageResource(R.drawable.ic_cancel_red);
				_Check = false;
			}
		}
		else if (pAction.equals("user.update"))
		{
			if (pStatus == 1000)
			{
				_rlProgress.setVisibility(View.GONE);
				
				if (_Avatar.length() == 0 && __GetMainActivity().GetProfile().GetAvatarBase64().length() != 0)
				{
					// удалили аватар
					__SendToServer("user.deleteavatar", null);
				}
				else
				{
					__GetMainActivity().GetProfile().Update(__GetMainActivity()
						, _Login
						, _Name
						, _Surname
						, _Sex
						, _BirthDay.length() == 0 ? "" : _BirthDay
						, _Country == null ? 0 : _Country.Id
						, _CityID
						, _CityCountry
						, _CityID == 0 ? "" : _CityName
						, _Avatar);
				
					__GetMainActivity().GetManager().GoToBack();
				}
			}
			else
			{
				_rlProgress.setVisibility(View.GONE);
				__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog25Title), __GetMainActivity().getString(R.string.Dialog25Description));
			}
		}
		else if (pAction.equals("user.deleteavatar"))
		{
			_rlProgress.setVisibility(View.GONE);
			__GetMainActivity().GetProfile().Update(__GetMainActivity()
					, _Login
					, _etName.getText().toString()
					, _etSurname.getText().toString()
					, _Sex
					, _BirthDay.length() == 0 ? "" : _BirthDay
					, _Country == null ? 0 : _Country.Id
					, _CityID
					, _CityCountry
					, _CityID == 0 ? "" : _CityName
					, _Avatar);
			
			__GetMainActivity().GetManager().GoToBack();
		}
		else if (pAction.equals("user.clearfriend"))
		{
			if (pStatus == 1000)
			{
				__SendToServer("user.cleanallhistory", null);
			}
			else
			{
				_rlProgress.setVisibility(View.GONE);
				__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog27Title), __GetMainActivity().getString(R.string.Dialog27Description));
			}
		}
		else if (pAction.equals("user.cleanallhistory"))
		{
			if (pStatus == 1000)
			{
				__GetMainActivity().GetProfile().Initialized();
				
				__GetMainActivity().GetProfile().Update(__GetMainActivity()
						, _Login
						, _etName.getText().toString()
						, _etSurname.getText().toString()
						, _Sex
						, _BirthDay.length() == 0 ? "" : _BirthDay
						, _Country == null ? 0 : _Country.Id
						, _CityID
						, _CityCountry
						, _CityID == 0 ? "" : _CityName
						, _Avatar);
				
				__GetMainActivity().GetServerConnection().PushStartSequence();
				__GetMainActivity().GetManager().GoToChats();
			}
			else
			{
				_rlProgress.setVisibility(View.GONE);
				__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog27Title), __GetMainActivity().getString(R.string.Dialog27Description));
			}
		}
	}
	
	private void _SetSex()
	{
		if (_Sex.equals("m"))
		{
			_tvSex.setText(R.string.SexMale);
			_ivSex.setImageResource(R.drawable.selector_delete);
			_ivSex.setOnClickListener(this);
			_ivDialogSexMale.setImageResource(R.drawable.ic_select_pressed);
			_ivDialogSexFemale.setImageResource(R.drawable.ic_select);
			_ivDialogSexNot.setImageResource(R.drawable.ic_select);
		}
		else if (_Sex.equals("f"))
		{
			_tvSex.setText(R.string.SexFemale);
			_ivSex.setImageResource(R.drawable.selector_delete);
			_ivSex.setOnClickListener(this);
			_ivDialogSexMale.setImageResource(R.drawable.ic_select);
			_ivDialogSexFemale.setImageResource(R.drawable.ic_select_pressed);
			_ivDialogSexNot.setImageResource(R.drawable.ic_select);
		}
		else 
		{
			_tvSex.setText(R.string.SexNot);
			_ivSex.setImageResource(R.drawable.selector_next);
			_ivSex.setOnClickListener(null);
			_ivSex.setClickable(false);
			_ivDialogSexMale.setImageResource(R.drawable.ic_select);
			_ivDialogSexFemale.setImageResource(R.drawable.ic_select);
			_ivDialogSexNot.setImageResource(R.drawable.ic_select_pressed);
		}
	}
	private void _SetBirthday()
	{
		if (_BirthDay.length() == 0)
		{
			_ivDate.setImageResource(R.drawable.selector_next);
			_ivDate.setOnClickListener(null);
			_ivDate.setClickable(false);
			_tvDate.setText(getActivity().getText(R.string.DateNot).toString());
		}
		else
		{
			_ivDate.setImageResource(R.drawable.selector_delete);
			_ivDate.setOnClickListener(this);
			_tvDate.setText(_BirthDay);
		}
	}

	private void _ClearCity()
	{
		_CityID = 0;
		_CityName = "";
		_CityCountry = 0;
		_tvCity.setText(getActivity().getText(R.string.ProfileChoose).toString());
		_ivCity.setClickable(false);
		_ivCity.setOnClickListener(null);
		_ivCity.setImageResource(R.drawable.selector_next);
	}
	
	private boolean _CheckChanges()
	{
		if (!_Name.equals(__GetMainActivity().GetProfile().GetName()))
		{
			return true;
		}
		if (!_Surname.equals(__GetMainActivity().GetProfile().GetSurname()))
		{
			return true;
		}
		if (!_BirthDay.equals(__GetMainActivity().GetProfile().GetBirthday()))
		{
			return true;
		}
		if (!_Sex.equals(__GetMainActivity().GetProfile().GetSex()))
		{
			return true;
		}
		int countryID = _Country == null ? 0 : _Country.Id;
		if (__GetMainActivity().GetProfile().GetCountryId() != countryID)
		{
			return true;
		}
		if (__GetMainActivity().GetProfile().GetCityId() != _CityID)
		{
			return true;
		}
		if (!__GetMainActivity().GetProfile().GetAvatarBase64().equals(_Avatar))
		{
			return true;
		}
		return false;
	}
}