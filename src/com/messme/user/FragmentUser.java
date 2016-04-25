package com.messme.user;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.messages.chat.Chat;
import com.messme.chats.messages.dialog.Dialog;
import com.messme.chats.messages.dialog.DialogMessage;
import com.messme.contacts.Contact;
import com.messme.socket.ImageLoader;
import com.messme.util.Util;
import com.messme.view.MessmeFragment;

import android.content.ContentUris;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.util.LongSparseArray;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class FragmentUser extends MessmeFragment implements OnClickListener, OnCheckedChangeListener
{
	private ImageView _ivAvatar = null;
	private ImageView _ivOpacity = null;
	private LinearLayout _llNameLogin = null;
	private LinearLayout _llLikes = null;
	private ImageView _ivLikes = null;
	private TextView _tvLikes = null;
	private TextView _tvDistance = null;
	private ObservableScrollView _sv = null;
	private SwitchButton _sbLock = null;
	private TextView _tvName = null;
	private TextView _tvStatus = null;
	private TextView _tvLogin = null;
	private LinearLayout _llPhone = null;
	private TextView _tvPhone = null;
	private TextView _tvSex = null;
	private TextView _tvAge = null;
	private LinearLayout _llUserPlace = null;
	private TextView _tvUserPlace = null;
	private TextView _tvUserDelete = null;
	private LinearLayout _llUserMedia = null;
	//private TextView _tvLock = null;
	private LinearLayout _llGroups = null;
	
	private ImageLoader _Loader = null;
	private User _hUser = null;
	
	
	public FragmentUser(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
		_Loader = new ImageLoader(__GetMainActivity());
	}

	
	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		long userID = Long.parseLong(pStore.get(USER_ID));
		_hUser = __GetMainActivity().GetUsers().GetUser(userID);
		
		View view = pInflater.inflate(R.layout.user, pContainer, false);
		
		view.findViewById(R.id.rlUser).bringToFront();
		
		view.findViewById(R.id.ivUserBack).setOnClickListener(this);
		if (__GetMainActivity().GetContacts().GetContact(userID) == null)
		{
			view.findViewById(R.id.ivUserContact).setVisibility(View.GONE);
		}
		else
		{
			view.findViewById(R.id.ivUserContact).setOnClickListener(this);
		}
		_llLikes = (LinearLayout) view.findViewById(R.id.llUserLikes);
		_llLikes.setOnClickListener(this);
		_ivLikes = (ImageView) view.findViewById(R.id.ivUserLikes);
		_tvLikes = (TextView) view.findViewById(R.id.tvUserLikes);

		_ivAvatar = (ImageView) view.findViewById(R.id.ivUserAvatar);
		_ivOpacity = (ImageView) view.findViewById(R.id.ivUserOpacity);
		_ivOpacity.setAlpha(0f);
		
		_llNameLogin = (LinearLayout) view.findViewById(R.id.llUser);
		_tvName = (TextView) view.findViewById(R.id.tvUserName);
		
		_tvStatus = (TextView) view.findViewById(R.id.tvUserStatus);
		_tvLogin = (TextView) view.findViewById(R.id.tvUserLogin);
		_llPhone = (LinearLayout) view.findViewById(R.id.llUserPhone);
		_tvPhone = (TextView) view.findViewById(R.id.tvUserPhone);
		_tvSex = (TextView) view.findViewById(R.id.tvUserSex);
		_tvAge = (TextView) view.findViewById(R.id.tvUserAge);
		
		view.findViewById(R.id.tvUserAllPhoto).setOnClickListener(this);
		
		_tvDistance = (TextView) view.findViewById(R.id.tvUserDistance);
		view.findViewById(R.id.tvUserToMap).setOnClickListener(this);
		
		_llUserPlace = (LinearLayout) view.findViewById(R.id.llUserPlace);
		_tvUserPlace = (TextView) view.findViewById(R.id.tvUserPlace);
		
		view.findViewById(R.id.llUserLock).setOnClickListener(this);
		//_tvLock = (TextView) view.findViewById(R.id.tvUserLock);
		_sbLock = (SwitchButton) view.findViewById(R.id.sbUserLock);
		_sbLock.setOnCheckedChangeListener(this);
		_llUserMedia = (LinearLayout) view.findViewById(R.id.llUserMedia);
		_llUserMedia.setOnClickListener(this);
		view.findViewById(R.id.llUserNotification).setOnClickListener(this);
		view.findViewById(R.id.llUserSettings).setOnClickListener(this);
		view.findViewById(R.id.llUserSharedGroups).setOnClickListener(this);
		view.findViewById(R.id.llUserMessages).setOnClickListener(this);
		
		view.findViewById(R.id.tvUserClear).setOnClickListener(this);
		_tvUserDelete = (TextView) view.findViewById(R.id.tvUserDelete);
		_tvUserDelete.setOnClickListener(this);
		
		_llGroups = (LinearLayout) view.findViewById(R.id.llUserSharedGroups);
		_llGroups.setOnClickListener(this);
		
		view.findViewById(R.id.ivUserSend).setOnClickListener(this);
		
		_sv = (ObservableScrollView) view.findViewById(R.id.svUser);
		ScrollUtils.addOnGlobalLayoutListener(_llNameLogin, new Runnable() 
		{
            @Override
            public void run() 
            {
				_OnScroll(_sv.getCurrentScrollY());
            }
        });
		_sv.setScrollViewCallbacks(new ObservableScrollViewCallbacks()
		{
			@Override
			public void onScrollChanged(int pScrollY, boolean firstScroll, boolean dragging)
			{
				_OnScroll(pScrollY);
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

		_Update();

		try
		{
			__SendToServer("user.info", User.GetOptions(__GetMainActivity(), userID));
		}
		catch (JSONException e)
		{
		}
		
		return view;
	}
	
	@Override
	protected void __OnDestroy()
	{
		_hUser = null;
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivUserBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.ivUserContact:
				Contact contact = __GetMainActivity().GetContacts().GetContact(_hUser.Id);
				if (contact != null)
				{
					Intent intent = new Intent(Intent.ACTION_EDIT);
					Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contact.ID)); 
					intent.setData(contactUri);
				    startActivity(intent);
				}
				break;
			case R.id.llUserLikes:
				try
				{
					JSONObject options = new JSONObject();
					options.put("id", _hUser.Id);
					options.put("value", _hUser.Liked ? 0 : 1);
					__SendToServer("user.like", options);
				}
				catch (JSONException e)
				{
				}
				break;
			case R.id.tvUserAllPhoto:
				//TODO переход к фото
				break;
			case R.id.tvUserToMap:
				__GetMainActivity().GetManager().GoToMap(_hUser);
				break;
			case R.id.llUserLock:
				_sbLock.setChecked(!_sbLock.isChecked());
				break;
			case R.id.llUserMedia:
				__GetMainActivity().GetManager().GoToMedia(_hUser);
				break;
			case R.id.llUserNotification:
				//TODO уведомления
				break;
			case R.id.llUserSettings:
				//TODO настройки
				break;
			case R.id.llUserSharedGroups:
				__GetMainActivity().GetManager().GoToSharedGroups(_hUser);
				break;
			case R.id.llUserMessages:
				//TODO эфиры
				break;
			case R.id.tvUserClear:
				if (__GetMainActivity().GetDialogs().Remove(_hUser.Id) == null)
				{
					__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog47Title), __GetMainActivity().getString(R.string.Dialog47Description));
				}
				else
				{
					__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog48Title), __GetMainActivity().getString(R.string.Dialog48Description));
				}
				break;
			case R.id.tvUserDelete:
				__GetMainActivity().OpenDialog(10, __GetMainActivity().getString(R.string.Dialog51Title), __GetMainActivity().getString(R.string.Dialog51Description));
				break;
			case R.id.ivUserSend:
				__GetMainActivity().GetManager().GoToDialog(_hUser.Id, false, false);
				break;
		}
	}
	@Override
	protected void __OnLocationChanged(Location pLocation)
	{
		if (pLocation == null)
		{
			_tvDistance.setVisibility(View.INVISIBLE);
			_tvDistance.setText("");
		}
		else
		{
			_tvDistance.setVisibility(View.VISIBLE);
			_tvDistance.setText(_hUser.GetDistance(getActivity(), pLocation.getLatitude(), pLocation.getLongitude()));
		}
	}
	@Override
	protected void __OnErrorSended(String pMessageId, String pAction, JSONObject pOptions)
	{
		if (pAction.equals("user.info"))
		{			
			__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog1Title), __GetMainActivity().getString(R.string.Dialog1Description));
		}
	}
	@Override
	protected void __OnMessageReceived(String pMessageId, String pAction, int pStatus, String pResult, JSONObject pOptions)
	{
		if (pAction.equals("user.like") && pStatus == 1000)
		{
			try
			{
				_hUser.Liked = pOptions.getInt("value") == 1;
				_hUser.Likes = Integer.parseInt(pResult);
				if (_hUser.Liked)
				{
					_tvLikes.setPressed(true);
					_ivLikes.setImageResource(R.drawable.ic_like_pressed);
	            }
	            else
	            {
	    			_tvLikes.setPressed(false);
					_ivLikes.setImageResource(R.drawable.ic_like);
				}
				_tvLikes.setText(Integer.toString(_hUser.Likes));
				__GetMainActivity().GetUsers().SaveChanges(_hUser, __GetMainActivity());
			}
			catch (JSONException e)
			{
			}
		}
		else if (pAction.equals("user.info") && pStatus == 1000)
		{
			try
			{
				JSONObject result = new JSONObject(pResult);
				_hUser = __GetMainActivity().GetUsers().AddUser(result, __GetMainActivity());
			}
			catch (JSONException e)
			{
			}
		}
	}
	
	@Override
	protected void __OnUsersChanged(LongSparseArray<User> pUsers)
	{
		if (pUsers.get(_hUser.Id) != null)
		{
			_Update();
		}
	}
	@Override
	protected void __OnContactsChanged()
	{
		_Update();
	}
	@Override
	protected void __OnDialogClicked(int pMessageId, boolean pValue)
	{
		if (pMessageId == 10 && pValue)
		{
			try
			{
				JSONObject options = new JSONObject();
				JSONArray userlist = new JSONArray();
				userlist.put(_hUser.Id);
				options.put("userlist", userlist);
				__SendToServer("user.removefriend", options);
				_hUser.IsFriend = false;
				_Update();
			}
			catch (JSONException e)
			{
			}
		}
	}
	
	private void _OnScroll(int pScrollY)
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
		
		_ivAvatar.setTranslationY(-pScrollY);
		_ivOpacity.setTranslationY(-pScrollY);
		_ivOpacity.setAlpha(ScrollUtils.getFloat((float) pScrollY / z, 0, 1));
        _llNameLogin.setTranslationX(pScrollY / 3);
		_llNameLogin.setTranslationY(-1 * pScrollY * 18 / 19);
        float scale = ((float)pScrollY * 1 / (-3f * z)) + 1f;
        //_llNameLogin.setPivotX(0);
        //_llNameLogin.setPivotY(0);
        if (_tvLogin.getVisibility() == View.VISIBLE)
        {
            _tvName.setTranslationY(pScrollY / 6);
        }
        else
        {
            _tvName.setTranslationY(pScrollY / 28);
        }
        _tvName.setTranslationX(-1 * pScrollY / 3);
        _tvName.setScaleX(scale);
        _tvName.setScaleY(scale);

        float alpha = 1f - (float) pScrollY / z;
        _tvLogin.setAlpha(ScrollUtils.getFloat(alpha, 0, 1));
        _llLikes.setAlpha(ScrollUtils.getFloat(alpha, 0, 1));
        //_tvLogin.setTranslationX(-1 * pScrollY / 3);
        _llLikes.setTranslationX(-1 * pScrollY / 3);
        if (alpha > 0.4f)
        {
        	_llLikes.setClickable(true);
        }
        else
        {
        	_llLikes.setClickable(false);
        }
	}
	
	private void _Update()
	{
		_tvLikes.setText(Integer.toString(_hUser.Likes));
		if (_hUser.Liked)
		{
			_tvLikes.setPressed(true);
			_ivLikes.setImageResource(R.drawable.ic_like_pressed);
		}
		else
		{
			_tvLikes.setPressed(false);
			_ivLikes.setImageResource(R.drawable.ic_like);
		}
		
		if (_hUser.HasImage())
		{
			if (_hUser.HasLoadedImage())
			{
				_ivAvatar.setImageBitmap(_hUser.GetImage());
			}
			else
			{
				_ivAvatar.setImageResource(R.drawable.drawable_background_blue);
			}
		}
		else
		{
			_ivAvatar.setImageResource(R.drawable.drawable_background_blue);
			_Loader.Load(_ivAvatar, _hUser);
		}
		
		if (_hUser.IsFriend)
		{
			_llPhone.setVisibility(View.VISIBLE);
			_tvPhone.setText(_hUser.GetPhone(__GetMainActivity().GetProfile().GetLocale()));
			_tvUserDelete.setVisibility(View.VISIBLE);
		}
		else
		{
			_llPhone.setVisibility(View.GONE);
			_tvUserDelete.setVisibility(View.GONE);
		}
		
		_tvName.setText(_hUser.GetName(__GetMainActivity()));
		_tvStatus.setText(_hUser.GetStatus(__GetMainActivity()));
		if (_tvName.getText().toString().equals(_hUser.Login))
		{
			_tvLogin.setVisibility(View.GONE);
		}
		else
		{
			_tvLogin.setVisibility(View.VISIBLE);
			_tvLogin.setText(_hUser.Login);
		}
		_tvSex.setText(_hUser.GetSex(getActivity()));
		_tvAge.setText(_hUser.GetAge(getActivity()));
		
		if (_hUser.HasGeoPosition())
		{
			switch (_hUser.GeoStatus)
			{
				case 0:
					_llUserPlace.setVisibility(View.GONE);
					break;
				case 1:
					// виден всем
				case 2:
					_llUserPlace.setVisibility(View.VISIBLE);
					
					if (_hUser.Country == 0)
					{
						_tvUserPlace.setText(R.string.UserNoPlace);
					}
					else
					{
						String country = __GetMainActivity().GetCountries().GetById(_hUser.Country).Name;
						if (_hUser.City == 0)
						{
							_tvUserPlace.setText(country);
						}
						else
						{
							_tvUserPlace.setText(country + ", " + _hUser.CityName);
						}
					}
					
					if (__GetMainActivity().GetLocation() == null)
					{
						_tvDistance.setVisibility(View.INVISIBLE);
						_tvDistance.setText("");
					}
					else
					{
						_tvDistance.setVisibility(View.VISIBLE);
						_tvDistance.setText(_hUser.GetDistance(getActivity(), __GetMainActivity().GetLocation().getLatitude(), __GetMainActivity().GetLocation().getLongitude()));
					}
					break;
				case 3:
					//TODO определить состоит ли пользователь в группе друга
					_llUserPlace.setVisibility(View.INVISIBLE);
					break;
			}
		}
		else
		{
			_llUserPlace.setVisibility(View.GONE);
		}
		
//		if (_hUser.IsLocked())
//		{
//			_tvLock.setText(R.string.UserLock);
//		}
//		else
//		{
//			_tvLock.setText(R.string.UserUnlock);
//		}
		_sbLock.setOnCheckedChangeListener(null);
		_sbLock.setChecked(_hUser.IsLocked());
		_sbLock.setOnCheckedChangeListener(this);
		
		Dialog dialog = __GetMainActivity().GetDialogs().GetDialog(_hUser.Id, false);
		if (dialog != null)
		{
			_llUserMedia.setVisibility(View.GONE);
			for (int i = 0; i < dialog.GetMessages().size(); i++)
			{
				DialogMessage message = dialog.GetMessages().get(i);
				if (message.GetAttachmentsCount() > 0)
				{
					_llUserMedia.setVisibility(View.VISIBLE);
					break;
				}
			}
		}
		else
		{
			_llUserMedia.setVisibility(View.GONE);
		}
		
		_llGroups.setVisibility(View.GONE);
		for (Chat chat: __GetMainActivity().GetChats().GetChats().values())
		{
			if (chat.IsContainUser(_hUser.Id))
			{
				_llGroups.setVisibility(View.VISIBLE);
				break;
			}
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean pIsChecked)
	{
		if (pIsChecked)
		{
//			_tvLock.setText(R.string.UserLock);
			_hUser.SetLocked(true, __GetMainActivity());
		}
		else
		{
//			_tvLock.setText(R.string.UserUnlock);
			_hUser.SetLocked(false, __GetMainActivity());
		}
	}
}