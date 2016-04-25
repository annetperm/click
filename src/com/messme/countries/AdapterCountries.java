package com.messme.countries;

import java.util.ArrayList;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.countries.Countries.Country;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AdapterCountries extends BaseAdapter
{
	private int _SelectedID = -1;
	private final boolean _ShowPhone;
	private final ActivityMain _hContext;
	private final LayoutInflater _Inflater;
	private ArrayList<Country> _hCountries = null;
	private ArrayList<Country> _FilteredCountries = new ArrayList<Country>();
	
	public AdapterCountries(ActivityMain pActivity, int pId, boolean pShowPhone)
	{
		_hContext = pActivity;
		_ShowPhone = pShowPhone;
		_Inflater = (LayoutInflater)_hContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_hCountries = _hContext.GetCountries().GetValues();
		for (int i = 0; i < _hCountries.size(); i++)
		{
			_FilteredCountries.add(_hCountries.get(i));
		}
		_SelectedID = pId;
	}

	@Override
	public int getCount()
	{
		return _FilteredCountries.size();
	}

	@Override
	public Object getItem(int pPosition)
	{
		return _FilteredCountries.get(pPosition);
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
			pConvertView = _Inflater.inflate(R.layout.countries_listitem, pParent, false);
		}
		
		Country country = _FilteredCountries.get(pPosition);
		
		((TextView) pConvertView.findViewById(R.id.tvCountriesListitemName)).setText(country.Name);
		if (_ShowPhone)
		{
			((TextView) pConvertView.findViewById(R.id.tvCountriesListitemCode)).setText(country.GetCode());
		}
		else
		{
			pConvertView.findViewById(R.id.tvCountriesListitemCode).setVisibility(View.GONE);
		}
		
		if (_SelectedID == country.Id)
		{
			((ImageView) pConvertView.findViewById(R.id.ivCountriesListitem)).setImageResource(R.drawable.ic_select_pressed);
		}
		else
		{
			((ImageView) pConvertView.findViewById(R.id.ivCountriesListitem)).setImageResource(R.drawable.ic_select);
		}
		
		((ImageView) pConvertView.findViewById(R.id.ivCountriesListitemFlag)).setImageBitmap(country.Flag);
		
		return pConvertView;
	}
	
	public void SetFilter(String pMask)
	{
		_FilteredCountries.clear();
		pMask = pMask.toLowerCase();
		for (int i = 0; i < _hCountries.size(); i++)
		{
			if (_hCountries.get(i).NameLowerCase.contains(pMask))
			{
				_FilteredCountries.add(_hCountries.get(i));
			}
		}
		notifyDataSetChanged();
	}
	
	public void SetSelected(int pPosition)
	{
		_SelectedID = _FilteredCountries.get(pPosition).Id;
		notifyDataSetChanged();
	}

	public int GetSelectedId()
	{
		return _SelectedID;
	}
}
