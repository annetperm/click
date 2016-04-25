package com.messme.chats.messages.chat;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.messme.ActivityMain;
import com.messme.R;
import com.messme.socket.ImageLoader;
import com.messme.user.Users;
import com.messme.util.DateUtil;
import com.messme.util.ImageUtil;
import com.messme.util.Util;
import com.messme.view.MessmeFragment;
import com.messme.view.TextWatcher;
import com.messme.view.TextWatcher.iOnTextChanged;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.ViewGroup;


public class FragmentGroup extends MessmeFragment implements OnClickListener, iOnTextChanged
{
	private static final int _RESULT_CAMERA = 3000;
	private static final int _RESULT_GALLERY = 3001;
	private static final int _RESULT_IMAGE = 3002;
	
	private static final int _THEME			= 100;
	private static final int _DESCRIPTION	= 101;
	private static final int _AVATAR 		= 102;
	private static final int _FIRST			= 103;
	
	
	private RelativeLayout _rlGroup = null;
	private ImageView _ivAvatar = null;
	private ImageView _ivPhoto = null;
	private ImageView _ivOpacity = null;
	private EditText _etTheme = null;
	private ImageView _ivSend = null;
	private LinearLayout _llAddUser = null;
	private ImageView _ivTheme = null;
	private EditText _etDescription = null;
	private ImageView _ivDescription = null;
	private TextView _tvDate = null;
	private LinearLayout _llTitle = null;
	private ObservableListView _lv = null;
	private RelativeLayout _rlDialogPhoto = null;
	private LinearLayout _llDialogPhotoDelete = null;
	private RelativeLayout _rlProgress = null;
	
	private RelativeLayout _rlDialogChanges = null;
	
	private AdapterGroup _Adapter = null;
	
	private TextWatcher _ThemeTextWatcher;
	private TextWatcher _DescriptionTextWatcher;
	
	private ImageLoader _Loader;

	private Chat _hChat;
	private String _Theme;
	private String _Description;
	private String _NewAvatar;	//null - если не каких изменений нет
	
	private boolean _Canceled = false;
	
	
	public FragmentGroup(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
		_ThemeTextWatcher = new TextWatcher(this, 1);
		_DescriptionTextWatcher = new TextWatcher(this, 2);
		_Loader = new ImageLoader(__GetMainActivity());
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		if (pStore.get(CHAT_ID) != null)
		{
			_hChat = __GetMainActivity().GetChats().GetChat(pStore.get(CHAT_ID));
		}
		else
		{
			_hChat = null;
		}
		_NewAvatar = pStore.get(_AVATAR);
		_Theme = pStore.get(_THEME) == null ? _hChat == null ? "" : _hChat.GetName() : pStore.get(_THEME);
		_Description = pStore.get(_DESCRIPTION) == null ? _hChat == null ? "" : _hChat.GetDescription() : pStore.get(_DESCRIPTION);
		ArrayList<Long> users = new ArrayList<Long>();
		if (pStore.get(COUNT) != null)
		{
			int count = Integer.parseInt(pStore.get(COUNT));
			pStore.remove(COUNT);
			for (int i = 0; i < count; i++)
			{
				long id = Long.parseLong(pStore.get(1000 + i));
				pStore.remove(1000 + i);
				users.add(id);
			}
		}
		else
		{
			if (_hChat != null)
			{
				users = _hChat.GetUsers();
			}
		}
		boolean first;
		if (pStore.get(_FIRST) == null)
		{
			first = true;
		}
		else
		{
			first = false;
		}
		
		
		View view = pInflater.inflate(R.layout.group, pContainer, false);
		
		_rlGroup = (RelativeLayout) view.findViewById(R.id.rlGroup);
		_rlGroup.bringToFront();
		
		_llTitle = (LinearLayout) view.findViewById(R.id.llGroupTitle);
		view.findViewById(R.id.ivGroupBack).setOnClickListener(this);
		_llTitle.bringToFront();

		_ivAvatar = (ImageView) view.findViewById(R.id.ivGroupAvatar);
		
		_ivOpacity = (ImageView) view.findViewById(R.id.ivGroupOpacity);
		_ivOpacity.setAlpha(0f);
		
		LayoutInflater ltInflater = getActivity().getLayoutInflater();
        View headerView = ltInflater.inflate(R.layout.group_header, null, false);
                
        _llAddUser = (LinearLayout) headerView.findViewById(R.id.llGroupAdd);
		_llAddUser.setOnClickListener(this);
        
		_ivSend = (ImageView) view.findViewById(R.id.ivGroupSend);
		_ivSend.setOnClickListener(this);
        
		_lv = (ObservableListView) view.findViewById(R.id.lvGroup);
		_lv.addHeaderView(headerView);
		_lv.setScrollViewCallbacks(new ObservableScrollViewCallbacks()
		{
			@Override
			public void onScrollChanged(int pScrollY, boolean firstScroll, boolean dragging)
			{
		        final float z = Util.GetPixelsFromDp(getActivity(), 200 - 56);
		        
				if (pScrollY > z)
				{
					pScrollY = (int) z;
				}
				if (pScrollY < 0)
				{
					pScrollY = 0;
				}
				
				float alpha = 1 - pScrollY / z;
				if (alpha < 0.3f)
				{
					_ivPhoto.setClickable(false);
					_ivPhoto.setOnClickListener(null);
				}
				else
				{
					_ivPhoto.setClickable(true);
					_ivPhoto.setOnClickListener(FragmentGroup.this);
				}
				_ivPhoto.setAlpha(alpha);
				_rlGroup.setTranslationY(-pScrollY);
				_llTitle.setTranslationY(pScrollY);
				_ivOpacity.setAlpha(ScrollUtils.getFloat((float) pScrollY / z, 0, 1));
			}
			@Override
			public void onUpOrCancelMotionEvent(ScrollState scrollState)
			{
			}
			@Override
			public void onDownMotionEvent()
			{
			}
		});

		_tvDate = (TextView) headerView.findViewById(R.id.tvGroupDateValue);
		
		_ivPhoto = (ImageView) view.findViewById(R.id.ivGroupPhoto);
		_ivPhoto.setOnClickListener(this);
		
		_etTheme = (EditText) headerView.findViewById(R.id.etGroupTheme);
		_etTheme.addTextChangedListener(_ThemeTextWatcher);
		_ivTheme = (ImageView) headerView.findViewById(R.id.ivGroupThemeEdit);
		
		_etDescription = (EditText) headerView.findViewById(R.id.etGroupDescription);
		_etDescription.addTextChangedListener(_DescriptionTextWatcher);
		_ivDescription = (ImageView) headerView.findViewById(R.id.ivGroupDescriptionEdit);
		
		_rlDialogPhoto = (RelativeLayout) view.findViewById(R.id.rlGroupDialogPhoto);
		_rlDialogPhoto.setVisibility(View.GONE);
		_rlDialogPhoto.setOnClickListener(this);
		_rlDialogPhoto.bringToFront();
		view.findViewById(R.id.llGroupDialogPhotoCamera).setOnClickListener(this);
		view.findViewById(R.id.llGroupDialogPhotoGallery).setOnClickListener(this);
		_llDialogPhotoDelete = (LinearLayout) view.findViewById(R.id.llGroupDialogPhotoDelete);
		_llDialogPhotoDelete.setOnClickListener(this);
		
		_rlProgress = (RelativeLayout) view.findViewById(R.id.rlGroupProgress);
		_rlProgress.bringToFront();
		_rlProgress.setVisibility(View.GONE);
		

		if (_NewAvatar == null)
		{
			if (_hChat == null)
			{
				_llDialogPhotoDelete.setVisibility(View.GONE);
				_ivAvatar.setImageResource(R.drawable.background_group);
			}
			else if (_hChat.GetAvatarUrl().equals(""))
			{
				_llDialogPhotoDelete.setVisibility(View.GONE);
				_ivAvatar.setImageResource(R.drawable.background_group);
			}
			else
			{
				_llDialogPhotoDelete.setVisibility(View.VISIBLE);
				if (_hChat.HasLoadedImage())
				{
					_ivAvatar.setImageBitmap(_hChat.GetImage());
				}
				else
				{
					_ivAvatar.setImageResource(R.drawable.background_group);
					_Loader.Load(_ivAvatar, _hChat);
				}
			}
		}
		else if (_NewAvatar.equals(""))
		{
			_llDialogPhotoDelete.setVisibility(View.GONE);
			_ivAvatar.setImageResource(R.drawable.background_group);
		}
		else
		{
			_llDialogPhotoDelete.setVisibility(View.VISIBLE);
			byte[] encodeByte = Base64.decode(_NewAvatar, Base64.DEFAULT);
			_ivAvatar.setImageBitmap(BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length));
		}
		
		_etTheme.setText(_Theme);
		_etDescription.setText(_Description);
		
		_rlDialogChanges = (RelativeLayout) view.findViewById(R.id.rlGroupChanges);
		_rlDialogChanges.setVisibility(View.GONE);
		_rlDialogChanges.setOnClickListener(this);
		_rlDialogChanges.bringToFront();
		view.findViewById(R.id.btnGroupChangesNo).setOnClickListener(this);
		view.findViewById(R.id.btnGroupChangesYes).setOnClickListener(this);
		
		if (_hChat == null)
		{
			_tvDate.setText(DateUtil.GetDateTime());

			_ivPhoto.setVisibility(View.VISIBLE);
			_etTheme.setEnabled(true);
			_ivTheme.setVisibility(View.VISIBLE);
			_etDescription.setEnabled(true);
			_ivDescription.setVisibility(View.VISIBLE);
			_llAddUser.setVisibility(View.VISIBLE);
			
			_Adapter = new AdapterGroup(__GetMainActivity(), true);
		}
		else//if (_hChat == null)
		{
			_tvDate.setText(_hChat.GetDateTime());
			
			if (first)
			{
				_ivPhoto.setVisibility(View.GONE);
				_etTheme.setEnabled(false);
				_ivTheme.setVisibility(View.GONE);
				_etDescription.setEnabled(false);
				_ivDescription.setVisibility(View.GONE);
				_llAddUser.setVisibility(View.GONE);
				
				_Adapter = new AdapterGroup(__GetMainActivity(), false);
			}
			else
			{
				if (_hChat.GetAdminID() == __GetMainActivity().GetProfile().GetID())
				{
					_ivPhoto.setVisibility(View.VISIBLE);
					_etTheme.setEnabled(true);
					_ivTheme.setVisibility(View.VISIBLE);
					_etDescription.setEnabled(true);
					_ivDescription.setVisibility(View.VISIBLE);
					_llAddUser.setVisibility(View.VISIBLE);
					
					_Adapter = new AdapterGroup(__GetMainActivity(), true);
				}
				else
				{					
					_ivPhoto.setVisibility(View.GONE);
					_etTheme.setEnabled(false);
					_ivTheme.setVisibility(View.GONE);
					_etDescription.setEnabled(false);
					_ivDescription.setVisibility(View.GONE);
					_llAddUser.setVisibility(View.GONE);
					
					_Adapter = new AdapterGroup(__GetMainActivity(), false);
				}
			}
		}//if (_hChat == null)	
		
		for (int i = 0; i < users.size(); i++)
		{
			_Adapter.AddUser(users.get(i));
		}
		_lv.setAdapter(_Adapter);
		
		if (_hChat != null)
		{
			if (first)
			{
				try
				{
					__SendToServer("groupchat.info", Chat.GetInfo(_hChat));
				}
				catch (JSONException e)
				{
				}
			}
			else
			{
				try
				{
					ArrayList<Long> empty = _hChat.GetEmptyUsers(__GetMainActivity().GetUsers());
					if (empty.size() > 0)
					{
						__SendToServer("user.list", Users.GetUsersOptions(__GetMainActivity(), empty));
					}
				}
				catch (JSONException e)
				{
				}
			}
		}
		
		return view;
	}
	
	@Override
	protected void __OnSave(SparseArray<String> pStore)
	{
		pStore.put(_FIRST, "not");
		pStore.put(_THEME, _Theme);
		pStore.put(_DESCRIPTION, _Description);
		pStore.put(_AVATAR, _NewAvatar);
		pStore.append(MessmeFragment.COUNT, Integer.toString(_Adapter.getCount()));
		for (int i = 0; i < _Adapter.getCount(); i++)
		{
			pStore.put(1000 + i, Long.toString(_Adapter.getItemId(i)));
		}
	}
	
	@Override
	protected void __OnDestroy()
	{
		_etTheme.removeTextChangedListener(_ThemeTextWatcher);
		_etDescription.removeTextChangedListener(_DescriptionTextWatcher);
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
				_NewAvatar = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
				
	            _ivAvatar.setImageBitmap(imageBitmap);
	            _llDialogPhotoDelete.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void OnTextChanged(int pID, String pText)
	{
		switch (pID)
		{
			case 1:
				_Theme = pText;
				break;
			case 2:
				_Description = pText;
				break;
		}
	}
	
	@Override
	public void onClick(View pView)
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(_etTheme.getWindowToken(), 0);
	    imm.hideSoftInputFromWindow(_etDescription.getWindowToken(), 0);
		switch (pView.getId())
		{
			case R.id.ivGroupBack:
				if (_CheckChanges())
				{
					_rlDialogChanges.setVisibility(View.VISIBLE);
					_rlDialogChanges.bringToFront();
				}
				else
				{
					__GetMainActivity().GetManager().GoToBack();
				}
				break;
			case R.id.ivGroupSend:
				_Update();
				break;
			case R.id.ivGroupPhoto:
				_rlDialogPhoto.setVisibility(View.VISIBLE);
				break;
			case R.id.rlGroupDialogPhoto:
				_rlDialogPhoto.setVisibility(View.GONE);
				break;
			case R.id.llGroupDialogPhotoCamera:
				_rlDialogPhoto.setVisibility(View.GONE);
				Intent intentCamera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intentCamera, _RESULT_CAMERA);
				break;
			case R.id.llGroupDialogPhotoGallery:
				_rlDialogPhoto.setVisibility(View.GONE);
				Intent intentGallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(Intent.createChooser(intentGallery, getActivity().getText(R.string.ProfileChoose)), _RESULT_GALLERY);
				break;
			case R.id.llGroupDialogPhotoDelete:
				_rlDialogPhoto.setVisibility(View.GONE);
				if (_hChat.GetAvatarUrl().equals(""))
				{
					_NewAvatar = null;
				}
				else
				{
					_NewAvatar = "";
				}
				_ivAvatar.setImageResource(R.drawable.background_group);
				_llDialogPhotoDelete.setVisibility(View.GONE);
				break;
			case R.id.llGroupAdd:
				if (__GetMainActivity().GetUsers().GetFriends().size() == 0)
				{
					__GetMainActivity().GetManager().GoToContacts();
				}
				else
				{
					__GetMainActivity().GetManager().GoToSelect(_Adapter.GetItems(), true);
				}
				break;
				
			case R.id.rlGroupChanges:
				_rlDialogChanges.setVisibility(View.GONE);
				break;
			case R.id.btnGroupChangesNo:
				_Canceled = true;
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.btnGroupChangesYes:
				_rlDialogChanges.setVisibility(View.GONE);
				_Update();
				break;
		}
	}

	@Override
	protected boolean __OnBackPressed()
	{
		if (_rlDialogPhoto.getVisibility() == View.VISIBLE)
		{
			_rlDialogPhoto.setVisibility(View.GONE);
			return false;
		}
		if (_Canceled)
		{
			return true;
		}
		if (_CheckChanges())
		{
			_rlDialogChanges.setVisibility(View.VISIBLE);
			return false;
		}
		return true;
	}
	
	@Override
	protected void __OnErrorSended(String pMessageId, String pAction, JSONObject pOptions)
	{
		_rlProgress.setVisibility(View.GONE);
		if (pAction.equals("groupchat.info"))
		{
			__GetMainActivity().OpenDialog(7, __GetMainActivity().getString(R.string.Dialog33Title), __GetMainActivity().getString(R.string.Dialog33Description));
		}
		if (pAction.equals("user.list"))
		{
			__GetMainActivity().OpenDialog(4, __GetMainActivity().getString(R.string.Dialog3Title), __GetMainActivity().getString(R.string.Dialog3Description));
		}
		else if (pAction.equals("groupchat.create"))
		{
			__GetMainActivity().OpenDialog(4, __GetMainActivity().getString(R.string.Dialog10Title), __GetMainActivity().getString(R.string.Dialog10Description));
		}
		else if (pAction.equals("groupchat.update"))
		{
			__GetMainActivity().OpenDialog(5, __GetMainActivity().getString(R.string.Dialog11Title), __GetMainActivity().getString(R.string.Dialog11Description));
		}
		else if (pAction.equals("user.list"))
		{
			__GetMainActivity().OpenDialog(4, __GetMainActivity().getString(R.string.Dialog3Title), __GetMainActivity().getString(R.string.Dialog3Description));
		}			
	}
	@Override
	protected void __OnMessageReceived(String pMessageId, String pAction, int pStatus, String pResult, JSONObject pOptions)
	{
		if (pAction.equals("groupchat.info"))
		{
			try
			{
				if (pStatus != 1000)
				{
					throw new JSONException("");
				}

				JSONObject result = new JSONObject(pResult);
				String oldAvatar = _hChat.GetAvatarUrl();
				_hChat.Update(result, __GetMainActivity().GetProfile().GetID());

				_etDescription.setText(_hChat.GetDescription());
				
				if (!oldAvatar.equals(_hChat.GetAvatarUrl()))
				{
					// новая автарка
					if (_hChat.GetAvatarUrl().length() == 0)
					{
						_llDialogPhotoDelete.setVisibility(View.GONE);
						_ivAvatar.setImageResource(R.drawable.background_group);
					}
					else
					{
						_llDialogPhotoDelete.setVisibility(View.VISIBLE);
						if (_hChat.HasLoadedImage())
						{
							_ivAvatar.setImageBitmap(_hChat.GetImage());
						}
						else
						{
							_ivAvatar.setImageResource(R.drawable.background_group);
							_Loader.Load(_ivAvatar, _hChat);
						}
					}
				}
				
				if (_hChat.GetAdminID() == __GetMainActivity().GetProfile().GetID())
				{
					_ivPhoto.setVisibility(View.VISIBLE);
					_etTheme.setEnabled(true);
					_ivTheme.setVisibility(View.VISIBLE);
					_etDescription.setEnabled(true);
					_ivDescription.setVisibility(View.VISIBLE);
					_llAddUser.setVisibility(View.VISIBLE);
					
					_Adapter = new AdapterGroup(__GetMainActivity(), true);
				}
				else
				{
					_Adapter = new AdapterGroup(__GetMainActivity(), false);
				}
				
				for (int i = 0; i < _hChat.GetUsers().size(); i++)
				{
					_Adapter.AddUser(_hChat.GetUsers().get(i));
				}
				
				ArrayList<Long> empty = _hChat.GetEmptyUsers(__GetMainActivity().GetUsers());
				if (empty.size() > 0)
				{
					__SendToServer("user.list", Users.GetUsersOptions(__GetMainActivity(), empty));
				}
				
				_lv.setAdapter(_Adapter);
			}
			catch (JSONException e)
			{
				__GetMainActivity().OpenDialog(3, __GetMainActivity().getString(R.string.Dialog34Title), __GetMainActivity().getString(R.string.Dialog34Description));
			}
		}
		else if (pAction.equals("user.list"))
		{
			try
			{
				if (pStatus != 1000)
				{
					throw new JSONException("");
				}
				// добавили новых пользователей
				JSONArray usersJSON = new JSONArray(pResult);
				__GetMainActivity().GetUsers().AddUsers(usersJSON, __GetMainActivity());
				_Adapter.notifyDataSetChanged();
			}
			catch (JSONException e)
			{
				__GetMainActivity().OpenDialog(6, __GetMainActivity().getString(R.string.Dialog7Title), __GetMainActivity().getString(R.string.Dialog7Description));
			}
		}
		else if (pAction.equals("groupchat.create"))
		{
			try
			{
				if (pStatus != 1000)
				{
					throw new JSONException("");
				}
				JSONObject result = new JSONObject(pResult);
				_hChat = __GetMainActivity().GetChats().GetChat(result.getString("id"));
				_hChat.Update(result, __GetMainActivity().GetProfile().GetID());
				
				__GetMainActivity().GetManager().GoToChat(_hChat, true, true);
			}
			catch (JSONException e)
			{
				_rlProgress.setVisibility(View.GONE);
				__GetMainActivity().OpenDialog(6, __GetMainActivity().getString(R.string.Dialog12Title), __GetMainActivity().getString(R.string.Dialog12Description));
			}
		}
		else if (pAction.equals("groupchat.deleteavatar"))
		{
			try
			{
				if (pStatus != 1000)
				{
					throw new JSONException("");
				}
				
				_hChat.DeleteAvatar();

				JSONObject options = new JSONObject();
				options.put("id", _hChat.GetID());
				options.put("name", _Theme);
				options.put("description", _Description);
				options.put("avatar", "");
				JSONArray userlist = new JSONArray();
				for (int i = 0; i < _Adapter.getCount(); i++)
				{
					userlist.put(_Adapter.getItemId(i));
				}
				options.put("userlist", userlist);
				__SendToServer("groupchat.update", options);
			}
			catch (JSONException e)
			{
				_rlProgress.setVisibility(View.GONE);
				__GetMainActivity().OpenDialog(5, __GetMainActivity().getString(R.string.Dialog13Title), __GetMainActivity().getString(R.string.Dialog13Description));
			}
		}
		else if (pAction.equals("groupchat.update"))
		{
			try
			{
				if (pStatus != 1000)
				{
					throw new JSONException("");
				}
				JSONObject result = new JSONObject(pResult);
				if (_NewAvatar == null)
				{
					// автар не был изменен но в ответ поле приходит пустым, заполняем его чтобы прошел нормально update
					result.put("avatar", _hChat.GetAvatarUrl());
				}
				_hChat.Update(result, __GetMainActivity().GetProfile().GetID());
				
				__GetMainActivity().GetManager().GoToChat(_hChat, true, true);
			}
			catch (JSONException e)
			{
				_rlProgress.setVisibility(View.GONE);
				__GetMainActivity().OpenDialog(5, __GetMainActivity().getString(R.string.Dialog13Title), __GetMainActivity().getString(R.string.Dialog13Description));
			}
		}
	}


	private boolean _CheckChanges()
	{
		if (_hChat == null)
		{
			return false;
		}
		else if (_Adapter.Contains(_hChat.GetUsers()) 
				&& _Theme.equals(_hChat.GetName())
				&& _Description.equals(_hChat.GetDescription())
				&& _NewAvatar == null)
		{
			return false;
		}
		return true;
	}
	private void _Update()
	{
		if (_Theme.length() == 0)
		{
			__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog8Title), __GetMainActivity().getString(R.string.Dialog8Description));
		}
		else if (_hChat == null)
		{
			if (_Adapter.getCount() < 2)
			{
				__GetMainActivity().OpenDialog(3, __GetMainActivity().getString(R.string.Dialog9Title), __GetMainActivity().getString(R.string.Dialog9Description));
			}
			else
			{
				try
				{
					JSONObject options = new JSONObject();
					options.put("name", _Theme);
					options.put("description", _Description);
					options.put("avatar", _NewAvatar == null ? "" : _NewAvatar);
					JSONArray userlist = new JSONArray();
					for (int i = 0; i < _Adapter.getCount(); i++)
					{
						userlist.put(_Adapter.getItemId(i));
					}
					options.put("userlist", userlist);
					__SendToServer("groupchat.create", options);
					_rlProgress.setVisibility(View.VISIBLE);
				}
				catch (JSONException e)
				{
				}
			}
		}
		else if (_Adapter.Contains(_hChat.GetUsers()) 
				&& _Theme.equals(_hChat.GetName())
				&& _Description.equals(_hChat.GetDescription())
				&& _NewAvatar == null)
		{
			// не изменилось или пользователь не админ группы
			__GetMainActivity().GetManager().GoToChat(_hChat, true, true);
		}
		else
		{
			if (_Adapter.getCount() < 2)
			{
				__GetMainActivity().OpenDialog(3, __GetMainActivity().getString(R.string.Dialog9Title), __GetMainActivity().getString(R.string.Dialog9Description));
			}
			else
			{
				if (_NewAvatar != null && _NewAvatar.equals(""))
				{
					try
					{
						JSONObject options = new JSONObject();
						options.put("id", _hChat.GetID());
						__SendToServer("groupchat.deleteavatar", options);
					}
					catch (JSONException e)
					{
					}
				}
				else
				{
					try
					{
						JSONObject options = new JSONObject();
						options.put("id", _hChat.GetID());
						options.put("name", _Theme);
						options.put("description", _Description);
						options.put("avatar", _NewAvatar == null ? "" : _NewAvatar);
						JSONArray userlist = new JSONArray();
						for (int i = 0; i < _Adapter.getCount(); i++)
						{
							userlist.put(_Adapter.getItemId(i));
						}
						options.put("userlist", userlist);
						__SendToServer("groupchat.update", options);
					}
					catch (JSONException e)
					{
					}
				}
				_rlProgress.setVisibility(View.VISIBLE);
			}
		}
	}
}