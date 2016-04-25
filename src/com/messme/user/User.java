package com.messme.user;

import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.messme.ActivityMain;
import com.messme.R;
import com.messme.contacts.Contact;
import com.messme.contacts.Contacts;
import com.messme.data.DB;
import com.messme.profile.Profile;
import com.messme.socket.iImageContainer;
import com.messme.util.DateUtil;
import com.messme.util.ImageUtil;
import com.messme.util.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;


public class User implements ClusterItem, iImageContainer
{
	public static final int NO_IMAGE= 0;
	public static final int FROM_WEB= 1;
	public static final int FROM_DB= 2;
	public static final int NO_URL= 3;
	public static final int BAD_URL= 4;
	
	
	private boolean _Synchronized;
	
	public long Id = 0;
	public String Login = "";
	public String Name = "";
	public String Surname = "";
	public String Sex = "";
	private int _Age = 0;
	public int Country = 0;
	public int City = 0;
	public String CityName = "";
	public int Likes = 0;
	public boolean Liked = false;
	private int _Status = 0;
	public int GeoStatus = 0;
	public double Lat = 0;
	public double Lng = 0;
	public boolean IsFriend = false;
	public String BirthDay = "";
	public String Avatar = "";
	private Date _StatusDate;
	public boolean _Locked = false;
	
	private Location _Location = null;
	private Bitmap _Image = null;
	private int _ImageStatus = NO_IMAGE;;
	
	public User(JSONObject pUser) throws JSONException
	{
		super();
		_Synchronized = true;
		
		Id = pUser.getLong("id");
		Login = pUser.getString("login");
		Name = pUser.getString("name");
		Surname = pUser.getString("surname");
		Sex = pUser.getString("sex");
		_Age = pUser.getInt("age");
		Country = pUser.getInt("country");
		City = pUser.getInt("city");
		CityName = pUser.getString("cityname");
		if (pUser.getString("birthdate").length() == 0)
		{
			BirthDay = "";
		}
		else
		{
			BirthDay = pUser.getString("birthdate").substring(8, 10)
					+ "." + pUser.getString("birthdate").substring(5, 7)
					+ "." + pUser.getString("birthdate").substring(0, 4);
			if (BirthDay.equals("01.01.0001"))
			{
				BirthDay = "";
			}
		}
		Likes = pUser.getInt("likes");
		Liked = pUser.getBoolean("isliked");
		_Status = pUser.getInt("status");
		GeoStatus = pUser.getInt("geostatus");
		Lat = pUser.getDouble("lat");
		Lng = pUser.getDouble("lng");
		IsFriend = pUser.getBoolean("isfriend");
		Avatar = pUser.getString("avatar");
		try 
		{  
			_StatusDate = DateUtil.DATE_TIME.parse(pUser.getString("statusdate"));  //2015-10-01T08:10:53
		} 
		catch (ParseException e) 
		{  
			_StatusDate = null;
		}
		_Locked = false;
	}
	public User(Cursor pCursor)
	{
		super();
		_Synchronized = false;
		
		Id = pCursor.getLong(0);
		Login = pCursor.getString(1);
		Name = pCursor.getString(2);
		Surname = pCursor.getString(3);
		Sex = pCursor.getString(4);
		_Age = pCursor.getInt(5);
		Country = pCursor.getInt(6);
		City = pCursor.getInt(7);
		CityName = pCursor.getString(8);
		BirthDay = pCursor.getString(9);
		Likes = pCursor.getInt(10);
		Liked = pCursor.getInt(11) == 0 ? false : true;
		_Status = pCursor.getInt(12);
		GeoStatus = pCursor.getInt(13);
		Lat = pCursor.getDouble(14);
		Lng = pCursor.getDouble(15);
		Avatar = pCursor.getString(16);
		IsFriend = pCursor.getInt(17) == 0 ? false : true;
		try 
		{  
			_StatusDate = DateUtil.DATE_TIME.parse(pCursor.getString(18));  //2015-10-01T08:10:53
		} 
		catch (ParseException e) 
		{  
			_StatusDate = null;
		}
		_Locked = pCursor.getInt(19) == 0 ? false : true;
	}
	
	
	public String GetName(ActivityMain pActivity)
	{
		if (IsFriend)
		{
			Contact contact = pActivity.GetContacts().GetContact(Id);
			if (contact == null)
			{
				String temp = Name + " " + Surname;
				if (temp.trim().length() == 0)
				{
					return Login;
				}
				else
				{
					return temp;
				}
			}
			else
			{
				return contact.Name;
			}
		}
		else
		{
			if (Name.trim().length() == 0)
			{
				return Login;
			}
			else
			{
				return Name;
			}
		}
	}
	
	public String GetAge(Context pContext)
	{
		if (_Age == -1)
		{
			return pContext.getText(R.string.AgeNot).toString();
		}
		String result;
		int age = _Age % 100;
		if (age >= 5 && age <= 20)
		{
			result =pContext.getText(R.string.Age3).toString();
		}
		else
		{
			age = age % 10;
			if (age == 1)
			{
				result = pContext.getText(R.string.Age1).toString();
			}
			else if (age >= 2 && age <= 4)
			{
				result = pContext.getText(R.string.Age2).toString();
			}
			else
			{
				result = pContext.getText(R.string.Age3).toString();
			}
		}
		return _Age + " " + result;
	}
	
	public String GetSex(Context pContext)
	{
		if (Sex.equals("m"))
		{
			return pContext.getText(R.string.SexMale).toString();
		}
		else if (Sex.equals("f"))
		{
			return pContext.getText(R.string.SexFemale).toString();
		}
		else
		{
			return pContext.getText(R.string.SexNot).toString();
		}
	}
	
	public String GetDistance(Context pContext, double pLat, double pLng)
	{
		if (_Location == null)
		{
			_Location = new Location("dummyprovider");
			_Location.setLatitude(Lat);
			_Location.setLongitude(Lng);
		}
		
		Location locat = new Location("dummyprovider");
		locat.setLatitude(pLat);
		locat.setLongitude(pLng);
		double distance = _Location.distanceTo(locat);
		if (distance < 50)
	    {
			return pContext.getText(R.string.DistanceNear).toString();
	    }
	    if (distance < 100)
	    {
	        return "<100m";
	    }
	    if (distance < 150)
	    {
	        return "<150m";
	    }
	    if (distance < 200)
	    {
	        return "<200m";
	    }
	    return (int)distance + "m";
	}
	
	public Spannable GetStatus(Context pContext)
	{
		Spannable result;
		if (_Synchronized)
		{
			if (IsOnline())
			{
				result = new SpannableString(pContext.getText(R.string.StatusOnline).toString());
				result.setSpan(new ForegroundColorSpan(Color.GREEN), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			else
			{
				if (_StatusDate == null)
				{
					result = new SpannableString(pContext.getText(R.string.StatusOffline).toString());
				}
				Calendar now = Calendar.getInstance();
				Calendar statusDate = Calendar.getInstance();
				statusDate.setTime(_StatusDate);
				statusDate.add(Calendar.HOUR, TimeZone.getDefault().getRawOffset() / 1000 / 60 / 60);
				if (now.get(Calendar.YEAR) == statusDate.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == statusDate.get(Calendar.DAY_OF_YEAR))
				{
					if (Sex.equals("m"))
					{
						result = new SpannableString(pContext.getString(R.string.StatusOfflineTime1, DateUtil.GetTime(_StatusDate)));
					}
					else if (Sex.equals("f"))
					{
						result = new SpannableString(pContext.getString(R.string.StatusOfflineTime2, DateUtil.GetTime(_StatusDate)));
					}
					else
					{
						result = new SpannableString(pContext.getString(R.string.StatusOfflineTime3, DateUtil.GetTime(_StatusDate)));
					}
				}
				else
				{
					if (Sex.equals("m"))
					{
						result = new SpannableString(pContext.getString(R.string.StatusOfflineTime4, DateUtil.GetDate(_StatusDate), DateUtil.GetTime(_StatusDate)));
					}
					else if (Sex.equals("f"))
					{
						result = new SpannableString(pContext.getString(R.string.StatusOfflineTime5, DateUtil.GetDate(_StatusDate), DateUtil.GetTime(_StatusDate)));
					}
					else
					{
						result = new SpannableString(pContext.getString(R.string.StatusOfflineTime6, DateUtil.GetDate(_StatusDate), DateUtil.GetTime(_StatusDate)));
					}
				}
				result.setSpan(new ForegroundColorSpan(Color.GRAY), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		else
		{
			result = new SpannableString(pContext.getText(R.string.UserStatusNoData).toString());
			//result.setSpan(new ForegroundColorSpan(Color.GRAY), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return result;
	}

	public boolean IsMale()
	{
		return Sex.equals("m");
	}
	public boolean IsFemale()
	{
		return Sex.equals("f");
	}

	public String GetPhone(String pLocale)
	{
		String phone = Long.toString(Id);
		return Util.GetPhone(phone, pLocale);
	}
	
	public String GetFullName()
	{
		return Name + " " + Surname;
	}
	
	public boolean IsOnline()
	{
		return _Status == 1;
	}
	
	public boolean IsShowableInMap()
	{
		return GeoStatus == 1 || IsFriend && GeoStatus == 2;
	}
	
	public boolean HasImage()
	{
		return _ImageStatus == NO_IMAGE ? false : true;
	}
	
	public boolean HasGeoPosition()
	{
		if (Lat + Lng < 0.0001)
		{
			return false;
		}
		return true;
	}
	
	public boolean IsLocked()
	{
		return _Locked;
	}
	
	
	public void SetLocked(boolean pLocked, Context pContext)
	{
		_Locked = pLocked;
		DB db = new DB(pContext);
		SQLiteDatabase writer = db.getWritableDatabase();
		writer.update("users", GetCV(false), "id = ?", new String[]{Long.toString(Id)});
		writer.close();
		db.close();
	}
	
	
	public ContentValues GetCV(boolean pNew)
	{
		ContentValues cv = new ContentValues();
		if (pNew)
		{
			cv.put("id", Id);
		}
	    cv.put("login", Login);
	    cv.put("name", Name);
	    cv.put("surname", Surname);
	    cv.put("sex", Sex);
	    cv.put("age", _Age);
	    cv.put("country", Country);
	    cv.put("city", City);
	    cv.put("cityname", CityName);
	    cv.put("birthdate", BirthDay);
	    cv.put("likes", Likes);
	    cv.put("liked", Liked ? 1 : 0);  
	    cv.put("status", _Status);
	    cv.put("geostatus", GeoStatus);
	    cv.put("lat", Lat);
	    cv.put("lng", Lng);      	
	    cv.put("url", Avatar);	
	    cv.put("isfriend", IsFriend ? 1 : 0); 
	    cv.put("statusdate", _StatusDate == null ? "" : DateUtil.DATE_TIME.format(_StatusDate)); 
	    cv.put("locked", _Locked ? 1 : 0); 
	    return cv;
	}
	
	
	public boolean Update(User pUser) 
	{
		boolean b = false;
		if (!Login.equals(pUser.Login))
		{
			Login = pUser.Login;
			b = true;
		}
		if (!Name.equals(pUser.Name))
		{
			Name = pUser.Name;
			b = true;
		}
		if (!Surname.equals(pUser.Surname))
		{
			Surname = pUser.Surname;
			b = true;
		}
		if (!Sex.equals(pUser.Sex))
		{
			Sex = pUser.Sex;
			b = true;
		}
		if (_Age != pUser._Age)
		{
			_Age = pUser._Age;
			b = true;
		}
		if (Country != pUser.Country)
		{
			Country = pUser.Country;
			b = true;
		}
		if (City != pUser.City)
		{
			City = pUser.City;
			b = true;
		}
		if (!CityName.equals(pUser.CityName))
		{
			CityName = pUser.CityName;
			b = true;
		}
		if (!BirthDay.equals(pUser.BirthDay))
		{
			BirthDay = pUser.BirthDay;
			b = true;
		}
		if (Likes != pUser.Likes)
		{
			Likes = pUser.Likes;
			b = true;
		}
		if (Liked != pUser.Liked)
		{
			Liked = pUser.Liked;
			b = true;
		}
		if (_Status != pUser._Status)
		{
			_Status = pUser._Status;
			b = true;
		}
		if (GeoStatus != pUser.GeoStatus)
		{
			GeoStatus = pUser.GeoStatus;
			b = true;
		}
		if (Lat != pUser.Lat)
		{
			Lat = pUser.Lat;
			b = true;
		}
		if (Lng != pUser.Lng)
		{
			Lng = pUser.Lng;
			b = true;
		}
		if (_StatusDate == null && pUser._StatusDate == null)
		{
			// не изменилось
		}
		if (_StatusDate == null && pUser._StatusDate != null)
		{
			_StatusDate = new Date(pUser._StatusDate.getTime());
			b = true;
		}
		else if (_StatusDate != null && pUser._StatusDate == null)
		{
			_StatusDate = null;
			b = true;
		}
		else if (!_StatusDate.equals(pUser._StatusDate))
		{
			_StatusDate.setTime(pUser._StatusDate.getTime());
			b = true;
		}
		if (!Avatar.equals(pUser.Avatar))
		{
			Avatar = pUser.Avatar;
			_ImageStatus = pUser._ImageStatus;
			//__ClearImage();
			if (pUser._Image == null)
			{
				_Image = null;
			}
			else
			{
				_Image = Bitmap.createBitmap(pUser._Image);
			}
			b = true;
		}
		if (IsFriend != pUser.IsFriend)
		{
			IsFriend = pUser.IsFriend;
			b = true;
		}
		if (_Synchronized != pUser._Synchronized)
		{
			_Synchronized = pUser._Synchronized;
			b = true;
		}
		// Locked не обновляется
		return b;
	}
	
	public static JSONObject GetOptions(ActivityMain pActivity, long pUserID) throws JSONException
	{
		JSONObject options = new JSONObject();
		options.put("userid", pUserID);
		options.put("locale", pActivity.GetProfile().GetLocale());
		return options;
	}
	
	@Override
	public LatLng getPosition()
	{
		return new LatLng(Lat, Lng);
	}
	
	@Override
	public void LoadImage(Context pContext, Profile pProfile)
	{
		if (Avatar.length() == 0)
		{
			_ImageStatus = NO_URL;
			String name = Contacts.GetContactName(pContext, Long.toString(Id));
			if (name == null || name.length() == 0)
			{
				if (Name.length() > 0)
				{
					name = Name;
				}
				else
				{
					name = Login;
				}
			}
			_Image = CreateAvatar(Login, name);
		}
		else
		{
			_Image = ImageUtil.LoadImageFromDB(pContext, Avatar);
			if (_Image == null)
			{
				try 
				{			
					_Image = BitmapFactory.decodeStream((InputStream)new URL("https://files.messme.me:8102/avatar/" + Avatar).getContent());
					_ImageStatus = FROM_WEB;
					ImageUtil.SaveImageToDB(pContext, _Image, Avatar);
				} 
				catch(Throwable e) 
				{
					String name = Contacts.GetContactName(pContext, Long.toString(Id));
					if (name == null || name.length() == 0)
					{
						if (Name.length() > 0)
						{
							name = Name;
						}
						else
						{
							name = Login;
						}
					}
					_Image = CreateAvatar(Login, name);
					_ImageStatus = BAD_URL;
				}
			}
			else
			{
				_ImageStatus = FROM_DB;
			}
		}
	}
	@Override
	public Bitmap GetImage()
	{
		return _Image;
	}
	@Override
	public boolean HasLoadedImage()
	{
		return _ImageStatus == FROM_DB || _ImageStatus == FROM_WEB ? true : false;
	}
	
	public static String GetName(Context pContext, User pUser)
	{
		if (pUser.IsFriend)
		{
			String name = Contacts.GetContactName(pContext, Long.toString(pUser.Id));
			if (name == null || name.length() == 0)
			{
				String temp = pUser.Name + " " + pUser.Surname;
				if (temp.trim().length() == 0)
				{
					return pUser.Login;
				}
				else
				{
					return temp;
				}
			}
			else
			{
				return name;
			}
		}
		else
		{
			if (pUser.Name.trim().length() == 0)
			{
				return pUser.Login;
			}
			else
			{
				return pUser.Name;
			}
		}
	}

	public static Bitmap CreateAvatar(String pLogin, String pName)
	{
		String firstChar = pLogin.substring(0, 1).toUpperCase();
		if (pName.length() > 0)
		{
			firstChar = pName.substring(0, 1).toUpperCase();
		}
		String lastChar = pLogin.substring(pLogin.length() - 1).toUpperCase();
		Paint paint = new Paint();
	    paint.setTextSize(120);
	    paint.setColor(Color.WHITE);
	    paint.setTextAlign(Paint.Align.CENTER);
	    Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap);
	    int[] rgb = null;
		switch (lastChar.charAt(0))
		{
			case 'A':
				rgb = new int[]{230, 54, 180};
				break;
			case 'B':
				rgb = new int[]{250, 55, 122};
				break;
			case 'C':
				rgb = new int[]{255, 112, 102};
				break;
			case 'D':
				rgb = new int[]{255, 123, 82};
				break;
			case 'E':
				rgb = new int[]{254, 200, 7};
				break;
			case 'F':
				rgb = new int[]{96, 219, 100};
				break;
			case 'G':
				rgb = new int[]{194, 35, 137};
				break;
			case 'H':
				rgb = new int[]{232, 30, 99};
				break;
			case 'I':
				rgb = new int[]{243, 67, 54};
				break;
			case 'J':
				rgb = new int[]{254, 87, 34};
				break;
			case 'K':
				rgb = new int[]{254, 151, 0};
				break;
			case 'L':
				rgb = new int[]{76, 174, 80};
				break;
			case 'M':
				rgb = new int[]{124, 21, 104};
				break;
			case 'N':
				rgb = new int[]{135, 14, 79};
				break;
			case 'O':
				rgb = new int[]{182, 28, 28};
				break;
			case 'P':
				rgb = new int[]{190, 54, 12};
				break;
			case 'Q':
				rgb = new int[]{229, 81, 0};
				break;
			case 'R':
				rgb = new int[]{27, 94, 32};
				break;
			case 'S':
				rgb = new int[]{0, 181, 164};
				break;
			case 'T':
				rgb = new int[]{0, 197, 222};
				break;
			case 'U':
				rgb = new int[]{78, 99, 219};
				break;
			case 'V':
				rgb = new int[]{144, 76, 228};
				break;
			case 'W':
				rgb = new int[]{210, 53, 237};
				break;
			case 'X':
				rgb = new int[]{121, 157, 173};
				break;
			case 'Y':
				rgb = new int[]{176, 124, 105};
				break;
			case 'Z':
				rgb = new int[]{0, 149, 135};
				break;
			case '0':
				rgb = new int[]{0, 172, 194};
				break;
			case '1':
				rgb = new int[]{63, 81, 180};
				break;
			case '2':
				rgb = new int[]{109, 60, 178};
				break;
			case '3':
				rgb = new int[]{155, 39, 175};
				break;
			case '4':
				rgb = new int[]{96, 125, 138};
				break;
			case '5':
				rgb = new int[]{121, 85, 72};
				break;
			case '6':
				rgb = new int[]{0, 77, 64};
				break;
			case '7':
				rgb = new int[]{0, 96, 100};
				break;
			case '8':
				rgb = new int[]{21, 31, 124};
				break;
			case '9':
				rgb = new int[]{70, 32, 127};
				break;
			default:
				rgb = new int[]{113, 28, 128};
				break;
		}
	    canvas.drawColor(Color.rgb(rgb[0], rgb[1], rgb[2]));
	    canvas.drawText(firstChar, 128, 128 + 120 / 3, paint);
	    return bitmap;
	}
}