package com.messme.cities;

import org.json.JSONException;
import org.json.JSONObject;

public class City
{
	public int ID = 0; 				// идентификатор города
	public String Name = ""; 		// название города
	public int ContinentID = 0; 		// идентификатор континента
	public String ContinentName = ""; 	// название континента
	public int CountryID = 0; 			// идентификатор страны
	public String CountryName = ""; 	// название страны
	public int RegionID = 0; 			// идентификатор региона
	public String RegionName = ""; 		// название региона
	public int ISO = 0; 				// ISO код страны
	public double Lat = 0d; 			// координата широты города
	public double Lng = 0d; 			// координата долготы города
	
	public String NameLowerCase = ""; 		// название города
	
	
	public City(JSONObject pJSON) throws JSONException
	{
		ID = pJSON.getInt("cityid");
		Name = pJSON.getString("cityname");
		ContinentID = pJSON.getInt("continentid");
		ContinentName = pJSON.getString("continentname");
		CountryID = pJSON.getInt("countryid");
		CountryName = pJSON.getString("countryname");
		RegionID = pJSON.getInt("regionid");
		RegionName = pJSON.getString("regionname");
		ISO = pJSON.getInt("iso");
		Lat = pJSON.getDouble("lat");
		Lng = pJSON.getDouble("lng");
		
		NameLowerCase = Name.toLowerCase();
	}
}
