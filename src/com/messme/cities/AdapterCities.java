package com.messme.cities;

import java.util.ArrayList;

import com.messme.ActivityMain;
import com.messme.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AdapterCities extends BaseAdapter
{
	private City _hSelected = null;
	private ActivityMain _hActivity;
	private LayoutInflater _Inflater;
	private ArrayList<City> _hCities = null;
	private ArrayList<City> _FilteredCities = new ArrayList<City>();
	
	public AdapterCities(ActivityMain pActivity, int pId, ArrayList<City> pCities, String pStartFilter)
	{
		_hActivity = pActivity;
		_Inflater = (LayoutInflater)_hActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_hCities = pCities;
		for (int i = 0; i < _hCities.size(); i++)
		{
			_FilteredCities.add(_hCities.get(i));
			if (_hCities.get(i).ID == pId)
			{
				_hSelected = _hCities.get(i);
			}
		}
		if (pStartFilter.length() != 0)
		{
			SetFilter(pStartFilter);
		}
	}

	@Override
	public int getCount()
	{
		return _FilteredCities.size();
	}

	@Override
	public Object getItem(int pPosition)
	{
		return _FilteredCities.get(pPosition);
	}

	@Override
	public long getItemId(int pPosition)
	{
		return pPosition;
	}

	@Override
	public View getView(int pPosition, View pConvertView, ViewGroup pParent)
	{
		if (pConvertView == null)
		{
			pConvertView = _Inflater.inflate(R.layout.cities_listitem, pParent, false);
		}
		
		City city = _FilteredCities.get(pPosition);
		
		((TextView) pConvertView.findViewById(R.id.tvCitiesListitemName)).setText(city.Name);
		((TextView) pConvertView.findViewById(R.id.tvCitiesListitemCode)).setText(city.RegionName);
		
		if (_hSelected == null)
		{
			((ImageView) pConvertView.findViewById(R.id.ivCitiesListitem)).setImageResource(R.drawable.ic_select);
		}
		else
		{
			if (_hSelected.ID == city.ID)
			{
				((ImageView) pConvertView.findViewById(R.id.ivCitiesListitem)).setImageResource(R.drawable.ic_select_pressed);
			}
			else
			{
				((ImageView) pConvertView.findViewById(R.id.ivCitiesListitem)).setImageResource(R.drawable.ic_select);
			}
		}
		
		return pConvertView;
	}
	
	public void SetFilter(String pMask)
	{
		_FilteredCities.clear();
		pMask = pMask.toLowerCase();
		for (int i = 0; i < _hCities.size(); i++)
		{
			if (_hCities.get(i).NameLowerCase.contains(pMask))
			{
				_FilteredCities.add(_hCities.get(i));
			}
		}
		notifyDataSetChanged();
	}
	
	public void SetSelected(int pPosition)
	{
		_hSelected = _FilteredCities.get(pPosition);
		notifyDataSetChanged();
	}

	public City GetSelected()
	{
		return _hSelected;
	}
}
