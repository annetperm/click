package com.messme.countries;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.view.MessmeFragment;
import com.messme.view.TextWatcher;
import com.messme.view.TextWatcher.iOnTextChanged;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

public class FragmentCountries extends MessmeFragment implements OnClickListener, OnItemClickListener, iOnTextChanged
{
	private LinearLayout _llTitle = null;
	private LinearLayout _llSearch = null;
	private EditText _etSearch = null;
	private ListView _lv = null;
	
	
	private AdapterCountries _Adapter = null;
	private TextWatcher _SearchTextWatcher = null;

	
	public FragmentCountries(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
		_SearchTextWatcher = new TextWatcher(this, 1);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		View view = pInflater.inflate(R.layout.countries, pContainer, false);
		
		view.findViewById(R.id.ivCountriesBack).setOnClickListener(this);
		view.findViewById(R.id.ivCountriesSearchBack).setOnClickListener(this);
		view.findViewById(R.id.ivCountriesSearch).setOnClickListener(this);
		//view.findViewById(R.id.ivCountriesOk).setOnClickListener(this);
		view.findViewById(R.id.ivCountriesClose).setOnClickListener(this);
		
		_llTitle = (LinearLayout) view.findViewById(R.id.llCountriesTitle);
		_llSearch = (LinearLayout) view.findViewById(R.id.llCountriesSearch);
		
		_etSearch = (EditText) view.findViewById(R.id.etCountriesSearch);
		_etSearch.addTextChangedListener(_SearchTextWatcher);
		
		view.findViewById(R.id.llCountriesShadow).bringToFront();
		
		_lv = (ListView) view.findViewById(R.id.lvCountries);
		_lv.setOnItemClickListener(this);
		_Adapter = new AdapterCountries(__GetMainActivity(), Integer.parseInt(pStore.get(COUNTRY_ID)), Boolean.parseBoolean(pStore.get(COUNTRY_PHONE)));
		_lv.setAdapter(_Adapter);
		
		return view;
	}
	
	@Override
	protected void __OnResume()
	{
		_etSearch.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			    imm.hideSoftInputFromWindow(_etSearch.getWindowToken(), 0);
			}
		}, 50);
	}

	@Override
	protected void __OnDestroy()
	{
		_etSearch.removeTextChangedListener(_SearchTextWatcher);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pIndex, long arg3)
	{
		_Adapter.SetSelected(pIndex);
		_llTitle.setVisibility(View.VISIBLE);
		_llSearch.setVisibility(View.GONE);
		
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(_etSearch.getWindowToken(), 0);
	    
		SparseArray<String> store = new SparseArray<String>();
		store.put(COUNTRY_ID, Integer.toString(_Adapter.GetSelectedId()));
		__GetMainActivity().GetManager().GoToBackWithResult(store);
	}

	@Override
	public void onClick(View pView)
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(_etSearch.getWindowToken(), 0);
		switch (pView.getId())
		{
			case R.id.ivCountriesBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.ivCountriesSearch:
				_llTitle.setVisibility(View.GONE);
				_llSearch.setVisibility(View.VISIBLE);
				_etSearch.postDelayed(new Runnable() 
				{
					@Override
					public void run() {
						_etSearch.requestFocus();
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(_etSearch, 0);
					}
				}, 200);
				break;
//			case R.id.ivCountriesOk:
//				SparseArray<String> store = new SparseArray<String>();
//				store.put(COUNTRY_ID, Integer.toString(_Adapter.GetSelectedId()));
//				__GetMainActivity().GetManager().GoToBackWithResult(store);
//				break;
			case R.id.ivCountriesSearchBack:
			case R.id.ivCountriesClose:
				_llTitle.setVisibility(View.VISIBLE);
				_llSearch.setVisibility(View.GONE);
				if (pView.getId() == R.id.ivCountriesClose)
				{
					_etSearch.setText("");
				}
				break;
		}
	}

	@Override
	public void OnTextChanged(int pID, String pText)
	{
		_Adapter.SetFilter(pText);
	}
}
