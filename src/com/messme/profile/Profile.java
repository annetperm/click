package com.messme.profile;

import java.io.File;
import java.util.Locale;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.data.DB;
import com.messme.user.User;
import com.messme.util.ImageUtil;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Base64;


public class Profile
{
	public final static String PREFERENCE_ID 			= "ID";
	public final static String PREFERENCE_INIT 			= "Init";
	public final static String PREFERENCE_UUID			= "UUID";
	public final static String PREFERENCE_LOGIN			= "Login";
	public final static String PREFERENCE_NAME			= "Name";
	public final static String PREFERENCE_SURNAME		= "Surname";
	public final static String PREFERENCE_SEX			= "Sex";
	public final static String PREFERENCE_BIRTHDAY		= "BirthDay";
	public final static String PREFERENCE_COUNTRY		= "Country";
	public final static String PREFERENCE_CITY			= "City";
	public final static String PREFERENCE_CITYNAME		= "CityName";
	public final static String PREFERENCE_CITYCOUNTRY	= "CityCountry";
	public final static String PREFERENCE_AVATAR_BASE64	= "AvatarBase64";
	public final static String PREFERENCE_LIKES 		= "Likes";
	
	public final static String PREFERENCE_DELIVERY 		= "Delivery";
	
	public final static String PREFERENCE_MPHOTO 		= "MPhoto";
	public final static String PREFERENCE_MAUDIO 		= "MAudio";
	public final static String PREFERENCE_MVIDEO 		= "MVideo";
	public final static String PREFERENCE_MFILES 		= "MFiles";
	public final static String PREFERENCE_WPHOTO 		= "WPhoto";
	public final static String PREFERENCE_WAUDIO 		= "WAudio";
	public final static String PREFERENCE_WVIDEO 		= "WVideo";
	public final static String PREFERENCE_WFILES 		= "WFiles";
	public final static String PREFERENCE_RPHOTO 		= "RPhoto";
	public final static String PREFERENCE_RAUDIO 		= "RAudio";
	public final static String PREFERENCE_RVIDEO 		= "RVideo";
	public final static String PREFERENCE_RFILES 		= "RFiles";
	
	public final static String PREFERENCE_THEME 		= "Theme";
	public final static String PREFERENCE_THEME_DATA	= "ThemeData";
	
	public final static String PREFERENCE_NOTIFICATION			= "Notification";
	public final static String PREFERENCE_NOTIFICATION_COLOR_R	= "NotColorR";
	public final static String PREFERENCE_NOTIFICATION_COLOR_G	= "NotColorG";
	public final static String PREFERENCE_NOTIFICATION_COLOR_B	= "NotColorB";
	public final static String PREFERENCE_NOTIFICATION_VIBRATE	= "NotVibrate";

	public final static String PREFERENCE_MESSAGES_ENTER	= "Enter";
	public final static String PREFERENCE_MESSAGES_SIZE		= "Size";
	public final static String PREFERENCE_MESSAGES_TIMER	= "Timer";
	
	public final static String PREFERENCE_MAP_STATUS 		= "Status";

	public final static String PREFERENCE_SECURITY_PIN	 	= "Pin";

	public final static String PREFERENCE_KEYBOARD_HEIGHT	 	= "KeyboardHeight";
	
	
	private Context _hContext;

	private String _UUID;
	private boolean _Initial;
	
	private long _ID;
	private String _Login;
	private String _Name;
	private String _Surname;
	private String _Sex;
	private String _Birthday;
	private int _Country;
	private int _City;
	private String _CityName;
	private int _CityCountry;
	private String _AvatarBase64;
	private int _Likes;
	
	private Bitmap _AvatarSmall = null;
	private Bitmap _AvatarBig = null;


	private int _Delivery;
	
	private boolean _MPhoto;
	private boolean _MAudio;
	private boolean _MVideo;
	private boolean _MFiles;
	private boolean _WPhoto;
	private boolean _WAudio;
	private boolean _WVideo;
	private boolean _WFiles;
	private boolean _RPhoto;
	private boolean _RAudio;
	private boolean _RVideo;
	private boolean _RFiles;
	
	private int _Theme;
	private Bitmap _ThemeData;

	private boolean _Notification;
	private int _NotificationColor;
	private boolean _NotificationVibrate;

	private boolean _MessagesEnter;
	private int _MessagesSize;
	private int _MessagesTimer;
	
	private int _MapStatus;
	
	private String _SecurityPin;


	private int _KeyboardHeight;
	
	private File _FolderAttachment = null;
	
	
	public Profile(Context pContext)
	{
		_hContext = pContext;
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
	    _UUID = preferences.getString(PREFERENCE_UUID, "");
	    
		_ID = preferences.getLong(PREFERENCE_ID, 0);
		_Login = preferences.getString(PREFERENCE_LOGIN, "");
		_Name = preferences.getString(PREFERENCE_NAME, "");
		_Surname = preferences.getString(PREFERENCE_SURNAME, "");
		_Sex = preferences.getString(PREFERENCE_SEX, "");
		_Birthday = preferences.getString(PREFERENCE_BIRTHDAY, "");
		_Country = preferences.getInt(PREFERENCE_COUNTRY, 0);
		_City = preferences.getInt(PREFERENCE_CITY, 0);
		_CityName = preferences.getString(PREFERENCE_CITYNAME, "");
		_CityCountry = preferences.getInt(PREFERENCE_CITYCOUNTRY, 0);
		_AvatarBase64 = preferences.getString(PREFERENCE_AVATAR_BASE64, "");
		_Likes = preferences.getInt(PREFERENCE_LIKES, 0);
		_Initial = preferences.getBoolean(PREFERENCE_INIT, false);
		
		
		_Delivery = preferences.getInt(PREFERENCE_DELIVERY, 0);
		
		_MPhoto = preferences.getBoolean(PREFERENCE_MPHOTO, true);
		_MAudio = preferences.getBoolean(PREFERENCE_MAUDIO, true);
		_MVideo = preferences.getBoolean(PREFERENCE_MVIDEO, true);
		_MFiles = preferences.getBoolean(PREFERENCE_MFILES, true);
		_WPhoto = preferences.getBoolean(PREFERENCE_WPHOTO, true);
		_WAudio = preferences.getBoolean(PREFERENCE_WAUDIO, true);
		_WVideo = preferences.getBoolean(PREFERENCE_WVIDEO, true);
		_WFiles = preferences.getBoolean(PREFERENCE_WFILES, true);
		_RPhoto = preferences.getBoolean(PREFERENCE_RPHOTO, false);
		_RAudio = preferences.getBoolean(PREFERENCE_RAUDIO, false);
		_RVideo = preferences.getBoolean(PREFERENCE_RVIDEO, false);
		_RFiles = preferences.getBoolean(PREFERENCE_RFILES, false);
		
		_Theme = preferences.getInt(PREFERENCE_THEME, 0);
		if (_Theme == -1)
		{
			String image = preferences.getString(PREFERENCE_THEME_DATA, "");
			byte[] encodeByte = Base64.decode(image, Base64.DEFAULT);
			_ThemeData = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
		}
		else
		{
			_ThemeData = null;
		}
		
		_Notification = preferences.getBoolean(PREFERENCE_NOTIFICATION, true);
		int r = preferences.getInt(PREFERENCE_NOTIFICATION_COLOR_R, 255);
		int g = preferences.getInt(PREFERENCE_NOTIFICATION_COLOR_G, 255);
		int b = preferences.getInt(PREFERENCE_NOTIFICATION_COLOR_B, 255);
		_NotificationColor = Color.rgb(r, g, b);
		_NotificationVibrate = preferences.getBoolean(PREFERENCE_NOTIFICATION_VIBRATE, true);
		
		_MessagesEnter = preferences.getBoolean(PREFERENCE_MESSAGES_ENTER, false);
		_MessagesSize = preferences.getInt(PREFERENCE_MESSAGES_SIZE, 12);
		_MessagesTimer = preferences.getInt(PREFERENCE_MESSAGES_TIMER, 15);
		
		_MapStatus = preferences.getInt(PREFERENCE_MAP_STATUS, 1);
		
		_KeyboardHeight = preferences.getInt(PREFERENCE_KEYBOARD_HEIGHT, 200);
		
		try 
		{
			SharedPreferences preferencesSec = ((Activity) _hContext).getPreferences(Context.MODE_PRIVATE);
			_SecurityPin = preferencesSec.getString(PREFERENCE_SECURITY_PIN, "");
		}
		catch (Exception e)
		{
			_SecurityPin = "";
		}
	}


	public String GetUUID()
	{
		return _UUID;
	}
	public long GetID()
	{
		return _ID;
	}
	public String GetLogin()
	{
		return _Login;
	}
	public String GetName()
	{
		return _Name;
	}
	public String GetSurname()
	{
		return _Surname;
	}
	public String GetAvatarBase64()
	{
		return _AvatarBase64;
	}
	public int GetCountryId()
	{
		return _Country;
	}
	public int GetCityId()
	{
		return _City;
	}
	public String GetSex()
	{
		return _Sex;
	}
	public String GetBirthday()
	{
		return _Birthday;
	}
	public int GetCityCountry()
	{
		return _CityCountry;
	}
	public String GetCityName()
	{
		return _CityName;
	}
	public String GetLikes()
	{
		return Integer.toString(_Likes);
	}
	public void SetLikes(int pLikes)
	{
		_Likes = pLikes;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.putInt(PREFERENCE_LIKES, _Likes);
		ed.commit();
	}
	
	public String GetHeader()
	{
		String result = _Name + " " + _Surname;
		result = result.trim();
		if (result.length() == 0)
		{
			return _Login;
		}
		return result;
	}


	public void SetID(long pID)
	{
		_ID = pID;
	}
	public void SetUUID(String pUUID)
	{
		_UUID = pUUID;
	}
	
	public void SetIDandUUID()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.putLong(PREFERENCE_ID, _ID);
		ed.putString(PREFERENCE_UUID, _UUID);
		ed.commit();
	}
	
	public void SetRecovery(ActivityMain pContext, User pUser)
	{
		_ID = pUser.Id;
		_Login = pUser.Login;
		_Name = pUser.Name;
		_Surname = pUser.Surname;
		_Sex = pUser.Sex;
		_Birthday = pUser.BirthDay;
		_Country = pUser.Country;
		_City = pUser.City;
		_CityName = pUser.CityName;
		_CityCountry = pUser.Country;
		_Likes = pUser.Likes;
		_Initial = true;
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.putLong(PREFERENCE_ID, _ID);
		ed.putString(PREFERENCE_UUID, _UUID);
		ed.putString(PREFERENCE_LOGIN, _Login);
		ed.putString(PREFERENCE_NAME, _Name);
		ed.putString(PREFERENCE_SURNAME, _Surname);
		ed.putString(PREFERENCE_SEX, _Sex);
		ed.putString(PREFERENCE_BIRTHDAY, _Birthday);
		ed.putInt(PREFERENCE_COUNTRY, _Country);
		ed.putInt(PREFERENCE_CITY, _City);
		ed.putString(PREFERENCE_CITYNAME, _CityName);
		ed.putInt(PREFERENCE_CITYCOUNTRY, _CityCountry);
		ed.putInt(PREFERENCE_LIKES, _Likes);
		if (pUser.HasLoadedImage())
		{
			_AvatarBase64 = ImageUtil.GetBase64(pUser.GetImage());
			_AvatarSmall = Bitmap.createBitmap(pUser.GetImage());
			_AvatarBig = Bitmap.createBitmap(pUser.GetImage());
		}
		else
		{
			_AvatarBase64 = "";
			_AvatarSmall = null;
			_AvatarBig = null;
		}
		ed.putString(PREFERENCE_AVATAR_BASE64, _AvatarBase64);
		ed.putBoolean(PREFERENCE_INIT, _Initial);
		ed.commit();
		
		pContext.MenuUpdate();
	}


	public void Update(ActivityMain pContext, String pLogin, String pName, String pSurname, String pSex, String pBirthDay, int pCountry, int pCity, int pCityCountry, String pCityName, String pAvatar)
	{
		_Login = pLogin;
		_Name = pName;
		_Surname = pSurname;
		_Sex = pSex;
		_Birthday = pBirthDay;
		_Country = pCountry;
		_City = pCity;
		_CityCountry = pCityCountry;
		_CityName = pCityName;
		_AvatarBase64 = pAvatar;
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.putString(PREFERENCE_LOGIN, _Login);
		ed.putString(PREFERENCE_NAME, _Name);
		ed.putString(PREFERENCE_SURNAME, _Surname);
		ed.putString(PREFERENCE_SEX, _Sex);
		ed.putString(PREFERENCE_BIRTHDAY, _Birthday);
		ed.putInt(PREFERENCE_COUNTRY, _Country);
		ed.putInt(PREFERENCE_CITY, _City);
		ed.putString(PREFERENCE_CITYNAME, _CityName);
		ed.putInt(PREFERENCE_CITYCOUNTRY, _CityCountry);
		ed.putString(PREFERENCE_AVATAR_BASE64, _AvatarBase64);
		ed.commit();

		_AvatarSmall = null;
		_AvatarBig = null;
		
		pContext.MenuUpdate();
	}


	public Bitmap GetSmallAvatarImage()
	{
		if (_AvatarSmall == null)
		{
			if (_AvatarBase64.length() > 0)
			{
				byte[] encodeByte = Base64.decode(_AvatarBase64, Base64.DEFAULT);
				_AvatarSmall = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
			}
			else
			{
				_AvatarSmall = User.CreateAvatar(_Login, _Name);
			}
		}
		return _AvatarSmall;
	}
	public Bitmap GetBigAvatarImage()
	{
		if (_AvatarBig == null)
		{
			if (_AvatarBase64.length() > 0)
			{
				byte[] encodeByte = Base64.decode(_AvatarBase64, Base64.DEFAULT);
				_AvatarBig = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
			}
			else
			{
				_AvatarBig = BitmapFactory.decodeResource(_hContext.getResources(), R.drawable.background_blue);
			}
		}
		return _AvatarBig;
	}


	public boolean IsInitial()
	{
		return _Initial;
	}
	public void Initialized()
	{
		_Initial = true;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.putBoolean(PREFERENCE_INIT, true);
		ed.commit();
	}
	
	
	public String GetDeliveryIndex()
	{
		_Delivery++;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.putInt(PREFERENCE_DELIVERY, _Delivery);
		ed.commit();
		return _hContext.getText(R.string.Delivery).toString() + " " + Integer.toString(_Delivery);
	}


	
	public boolean IsMPhoto()
	{
		return _MPhoto;
	}
	public boolean IsMAudio()
	{
		return _MAudio;
	}
	public boolean IsMVideo()
	{
		return _MVideo;
	}
	public boolean IsMFiles()
	{
		return _MFiles;
	}
	
	public boolean IsWPhoto()
	{
		return _WPhoto;
	}
	public boolean IsWAudio()
	{
		return _WAudio;
	}
	public boolean IsWVideo()
	{
		return _WVideo;
	}
	public boolean IsWFiles()
	{
		return _WFiles;
	}
	
	public boolean IsRPhoto()
	{
		return _RPhoto;
	}
	public boolean IsRAudio()
	{
		return _RAudio;
	}
	public boolean IsRVideo()
	{
		return _RVideo;
	}
	public boolean IsRFiles()
	{
		return _RFiles;
	}
	
	public void SetDownloadSettings(boolean pMPhoto
			, boolean pMAudio
			, boolean pMVideo
			, boolean pMFiles
			, boolean pWPhoto
			, boolean pWAudio
			, boolean pWVideo
			, boolean pWFiles
			, boolean pRPhoto
			, boolean pRAudio
			, boolean pRVideo
			, boolean pRFiles)
	{
		_MPhoto = pMPhoto;
		_MAudio = pMAudio;
		_MVideo = pMVideo;
		_MFiles = pMFiles;
		_WPhoto = pWPhoto;
		_WAudio = pWAudio;
		_WVideo = pWVideo;
		_WFiles = pWFiles;
		_RPhoto = pRPhoto;
		_RAudio = pRAudio;
		_RVideo = pRVideo;
		_RFiles = pRFiles;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.putBoolean(PREFERENCE_MPHOTO, _MPhoto);
		ed.putBoolean(PREFERENCE_MAUDIO, _MAudio);
		ed.putBoolean(PREFERENCE_MVIDEO, _MVideo);
		ed.putBoolean(PREFERENCE_MFILES, _MFiles);
		ed.putBoolean(PREFERENCE_WPHOTO, _WPhoto);
		ed.putBoolean(PREFERENCE_WAUDIO, _WAudio);
		ed.putBoolean(PREFERENCE_WVIDEO, _WVideo);
		ed.putBoolean(PREFERENCE_WFILES, _WFiles);
		ed.putBoolean(PREFERENCE_RPHOTO, _RPhoto);
		ed.putBoolean(PREFERENCE_RAUDIO, _RAudio);
		ed.putBoolean(PREFERENCE_RVIDEO, _RVideo);
		ed.putBoolean(PREFERENCE_RFILES, _RFiles);
		ed.commit();
	}


	public int GetThemeId()
	{
		return _Theme;
	}
	public Drawable GetTheme()
	{
		switch (_Theme)
		{
			case 0:
				return _hContext.getResources().getDrawable(R.drawable.drawable_background_chats);
			case 1:
				return _hContext.getResources().getDrawable(R.drawable.drawable_background_chats2);
			case -1:
				return new BitmapDrawable(_hContext.getResources(), _ThemeData);
		}
		return null;
	}
	public void SetThemeId(int pID)
	{
		_Theme = pID;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.putInt(PREFERENCE_THEME, _Theme);
		ed.commit();
	}
	public void SetTheme(Bitmap pBitmap)
	{
		_ThemeData = pBitmap;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		if (_ThemeData == null)
		{
			ed.putString(PREFERENCE_THEME_DATA, "");
		}
		else
		{
			ed.putString(PREFERENCE_THEME_DATA, ImageUtil.GetBase64(pBitmap));
		}
		ed.commit();
	}

	
	public boolean IsNotification()
	{
		return _Notification;
	}
	public void SetNotification(boolean pNotification)
	{
		_Notification = pNotification;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.putBoolean(PREFERENCE_NOTIFICATION, _Notification);
		ed.commit();
	}
	public int GetNotificationColor()
	{
		return _NotificationColor;
	}
	public void SetNotificationColor(int pColor)
	{
		_NotificationColor = pColor;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.putInt(PREFERENCE_NOTIFICATION_COLOR_R, Color.red(_NotificationColor));
		ed.putInt(PREFERENCE_NOTIFICATION_COLOR_G, Color.green(_NotificationColor));
		ed.putInt(PREFERENCE_NOTIFICATION_COLOR_B, Color.blue(_NotificationColor));
		ed.commit();
	}
	public boolean IsNotificationVibrate()
	{
		return _NotificationVibrate;
	}
	public void SetNotificationVibrate(boolean pNotificationVibrate)
	{
		_NotificationVibrate = pNotificationVibrate;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.putBoolean(PREFERENCE_NOTIFICATION_VIBRATE, _NotificationVibrate);
		ed.commit();
	}



	public boolean IsMessagesEnter()
	{
		return _MessagesEnter;
	}
	public void SetMessagesEnter(boolean pMessagesEnter)
	{
		_MessagesEnter = pMessagesEnter;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.putBoolean(PREFERENCE_MESSAGES_ENTER, _MessagesEnter);
		ed.commit();
	}
	public int GetMessagesSize()
	{
		return _MessagesSize;
	}
	public void SetMessagesSize(int pMessagesSize)
	{
		_MessagesSize = pMessagesSize;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.putInt(PREFERENCE_MESSAGES_SIZE, _MessagesSize);
		ed.commit();
	}
	public int GetMessagesTimer()
	{
		return _MessagesTimer;
	}
	public void SetMessagesTimer(int pMessagesTimer)
	{
		_MessagesTimer = pMessagesTimer;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.putInt(PREFERENCE_MESSAGES_TIMER, _MessagesTimer);
		ed.commit();
	}


	
	public int GetMapStatus()
	{
		return _MapStatus;
	}
	public void SetMapStatus(int pMapStatus)
	{
		_MapStatus = pMapStatus;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.putInt(PREFERENCE_MAP_STATUS, _MapStatus);
		ed.commit();
	}
	
	
	public boolean IsSecurityPinInitiated()
	{
		if (_SecurityPin.equals(""))
		{
			return false;
		}
		return true;
	}
	public boolean CheckSecurityPin(String pText)
	{
		if (_SecurityPin.equals("") && pText.equals("0000"))
		{
			return true;
		}
		else if (_SecurityPin.equals(pText))
		{
			return true;
		}
		return false;
	}
	public void SetSecurityPin(ActivityMain pActivity, String pNewPin)
	{
		_SecurityPin = pNewPin;
		SharedPreferences preferencesSec = pActivity.getPreferences(Context.MODE_PRIVATE);
		Editor ed = preferencesSec.edit();
		ed.putString(PREFERENCE_SECURITY_PIN, _SecurityPin);
		ed.commit();
	}
	
	
	
	public File GetFolderAttachment()
	{
		if (_FolderAttachment == null)
		{
	        File folder = new File(Environment.getExternalStorageDirectory() + "/Click");
	        if (!folder.exists()) 
	        {
	            folder.mkdir();
	        }
	        _FolderAttachment = new File(Environment.getExternalStorageDirectory() + "/Click/Attacment");
	        if (!_FolderAttachment.exists()) 
	        {
	        	_FolderAttachment.mkdir();
	        }
		}
		return _FolderAttachment;
	}
	
	
	
	public int GetKeyboardHeight()
	{
		return _KeyboardHeight;
	}
	public void SetKeyboardHeight(int pHeight)
	{
		_KeyboardHeight = pHeight;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.putInt(PREFERENCE_KEYBOARD_HEIGHT, _KeyboardHeight);
		ed.commit();
	}
	
	
	
	public String GetLocale()
	{
		return Locale.getDefault().getLanguage();
	}


	
	public void Clear(ActivityMain pActivity)
	{
		pActivity.MenuLock();
		
		_ID = 0;
		_UUID = "";
		_Login = "";
		_Name = "";
		_Surname = "";
		_Sex = "";
		_Birthday = "";
		_Country = 0;
		_City = 0;
		_CityCountry = 0;
		_CityName = "";
		_AvatarBase64 = "";
		_Likes = 0;
		_Initial = false;

		_Delivery = 0;
		
		_MPhoto = true;
		_MAudio = true;
		_MVideo = true;
		_MFiles = true;
		_WPhoto = true;
		_WAudio = true;
		_WVideo = true;
		_WFiles = true;
		_RPhoto = false;
		_RAudio = false;
		_RVideo = false;
		_RFiles = false;
		
		_Theme = 0;
		_ThemeData = null;
		
		_Notification = true;
		_NotificationColor = Color.WHITE;
		_NotificationVibrate = true;

		_MessagesEnter = false;
		_MessagesSize = 12;
		_MessagesTimer = 15;
		
		_MapStatus = 1;
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_hContext);
		Editor ed = preferences.edit();
		ed.clear();
		ed.commit();
		
		_SecurityPin = "";
		SharedPreferences preferencesSec = pActivity.getPreferences(Context.MODE_PRIVATE);
		ed = preferencesSec.edit();
		ed.clear();
		ed.commit();
		
		DB db = new DB(_hContext);
		SQLiteDatabase writer = db.getWritableDatabase();
		pActivity.GetServerConnection().Disconnect(writer);
		pActivity.DestroyServerConnection();
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
		}
		pActivity.GetContacts().NotSynchronized();			
		pActivity.GetDialogs().Clear(writer);		
		pActivity.GetChats().Clear(writer);	
		pActivity.GetDeliveries().Clear(writer);
		pActivity.GetUsers().Clear(writer);
		
		writer.delete("attachments", null, null);
		writer.delete("pairs", null, null);
		
		File files = GetFolderAttachment();
		if (files.listFiles() != null)
		{
			for (File file : files.listFiles()) 
			{
				file.delete();
	        }
		}
		
		_FolderAttachment = null;
		writer.close();
		db.close();
		
		pActivity.GetManager().GoToStart();
	}
}