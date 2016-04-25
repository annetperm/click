package com.messme.util;

import java.util.Random;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.messme.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;


public class Util
{	
	public static int GetPixelsFromDp(Context pContext, float pDp) 
	{
        final float scale = pContext.getResources().getDisplayMetrics().density;
        return (int)(pDp * scale + 0.5f);
    }
	
	public static int GetDpFromPixels(Context pContext, float pPixels) 
	{
        final float scale = pContext.getResources().getDisplayMetrics().density;
        return (int)(pPixels / scale + 0.5f);
    }
	
	public static String GenerateMID()
	{
	    final String alphabet = "0123456789abcdef";
	    Random r = new Random();
		String mid = "";
		for (int i = 0; i < 36; i++)
		{
			switch (i)
			{
				case 8:
				case 13:
				case 18:
				case 23:
					mid += '-';
					break;
				case 14:
					mid += '4';
					break;
				default:
					mid += alphabet.charAt(r.nextInt(alphabet.length()));
					break;
			}
		}
		return mid;
	}
	
	
	public static void CreateBreakPoint(String pMessage)
	{
		Log.d("MessmeBreakpoint", "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" + pMessage);
		try
		{
			throw new Exception();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static int GetConnectionType(Context pContext)
	{
		NetworkInfo info = ((ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (info != null && info.isConnected())
		{
			if (info.getType() == ConnectivityManager.TYPE_WIFI)
			{
				return 1;
			}
			else if (info.isRoaming())
			{
				return 3;
			}
			else
			{
				return 2;
			}
		}
		else
		{
			return 0;
		}
	}
	
	public static void GoToMap(Context pContext, double pLat, double pLng)
	{
		String label = pContext.getText(R.string.PlaceTitle).toString();
		String uriBegin = "geo:" + pLat + "," + pLng;
		String query = pLat + "," + pLng + "(" + label + ")";
		String encodedQuery = Uri.encode(query);
		String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
		Uri uri = Uri.parse(uriString);
		Intent intentPlace = new Intent(android.content.Intent.ACTION_VIEW, uri);
		pContext.startActivity(intentPlace);
	}

	public static String GetPhone(String pPhone, String pLocale)
	{
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		try
		{
			PhoneNumber swissNumberProto = phoneUtil.parse(pPhone, pLocale.toUpperCase());
			return phoneUtil.format(swissNumberProto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
		}
		catch (NumberParseException e)
		{
			return "";
		}
	}
	
	public static Typeface GetTypeface(Context pContext)
	{
		return Typeface.createFromAsset(pContext.getAssets(),"fonts/Kokila.ttf");
	}

	public static boolean IsGooglePlayServicesAvailable(Context pContext)
	{
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(pContext);
		if(status == ConnectionResult.SUCCESS) 
		{
		    return true;
		}
	    return false;
//		GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
//	    int result = googleAPI.isGooglePlayServicesAvailable(pContext);
//	    if(result == ConnectionResult.SUCCESS) 
//	    {
//		    return true;
//	    }
//        return false;
	}
}