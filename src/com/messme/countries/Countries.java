package com.messme.countries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.messme.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Countries
{
	public class Country
	{
		public int Id = 0;
		public String Code = "";
		public String Name = "";
		public String NameLowerCase = "";
		public int ISO = 0;
		public Bitmap Flag = null;
		
		public Country(JSONObject pJSON) throws JSONException
		{
			Id = pJSON.getInt("countryid");
			Name = pJSON.getString("countryname");
			NameLowerCase = Name.toLowerCase();
			Code = Integer.toString(pJSON.getInt("phonecode"));
			ISO = pJSON.getInt("iso");
			
			int id = _hContext.getResources().getIdentifier("ic_flag_" + Integer.toString(ISO), "drawable", "com.messme");
			if (id == 0)
			{
				Flag = BitmapFactory.decodeResource(_hContext.getResources(), R.drawable.ic_flag_null);
			}
			else
			{
				Flag = BitmapFactory.decodeResource(_hContext.getResources(), id);
			}
		}
		
		public String GetCode()
		{
			return "+" + Code;
		}
	}
	
	private Context _hContext = null;
	private int _CurrentIndex = -1;
	private ArrayList<Country> _Countries = null;
	
	
	public Countries(Context pContext)
	{
		_hContext = pContext;
	}
	
	
	public ArrayList<Country> GetValues()
	{
		if (_Countries == null)
		{
			_Load();
		}
		return _Countries;
	}
	

	public void Refresh(JSONArray pCountries) throws JSONException
	{
		String currentCountryName = Locale.getDefault().getDisplayCountry();
		for (int i = 0; i < pCountries.length(); i++)
		{
			Country country = new Country(pCountries.getJSONObject(i));
			_Countries.add(country);
			if (country.Name.equals(currentCountryName))
			{
				_CurrentIndex = i;
			}
		}
		if (_CurrentIndex == -1)
		{
			_CurrentIndex = 0;
		}
	}


	public Country GetCurrent()
	{
		if (_Countries == null)
		{
			_Load();
		}
		return _Countries.get(_CurrentIndex);
	}


	public Country GetById(int pId)
	{
		if (_Countries == null)
		{
			_Load();
		}
		for (int i = 0; i < _Countries.size(); i++)
		{
			if (_Countries.get(i).Id == pId)
			{
				return _Countries.get(i);
			}
		}
		return null;
	}
	
	private void _Load()
	{
		if (Locale.getDefault().getCountry().equals("RU"))
		{
			_Countries = new ArrayList<Countries.Country>();
			
			String currentCountryName = Locale.getDefault().getDisplayCountry();
			try
			{
				InputStream inputStream = _hContext.getResources().openRawResource(R.raw.countries);
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
				String line = reader.readLine();
				JSONObject file = new JSONObject(line);
				JSONArray countries = file.getJSONArray("result");
				for (int i = 0; i < countries.length(); i++)
				{
					Country country = new Country(countries.getJSONObject(i));
					_Countries.add(country);
					if (country.Name.equals(currentCountryName))
					{
						_CurrentIndex = i;
					}
				}
				if (_CurrentIndex == -1)
				{
					_CurrentIndex = 0;
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
	}
}
