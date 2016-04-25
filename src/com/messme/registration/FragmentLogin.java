package com.messme.registration;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.countries.Countries.Country;
import com.messme.view.MessmeFragment;
import com.messme.view.TextWatcher;
import com.messme.view.TextWatcher.iOnTextChanged;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class FragmentLogin extends MessmeFragment implements OnClickListener, iOnTextChanged
{	
	private EditText _etPhone = null;
	private EditText _etPromo = null;
	private RelativeLayout _rlDialog = null;
	private TextView _tvCode = null;
	private TextView _tvPhone = null;

	private TextWatcher _PhoneTextWatcher;
	private TextWatcher _PromoTextWatcher;
	
	private Country _Country;
	private String _Phone;
	private String _Promo;
	
	
	public FragmentLogin(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
		_PhoneTextWatcher = new TextWatcher(this, 1);
		_PromoTextWatcher = new TextWatcher(this, 2);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{		
		if (pStore.get(COUNTRY_ID) == null)
		{
			_Country = __GetMainActivity().GetCountries().GetCurrent();
		}
		else
		{
			int id = Integer.parseInt(pStore.get(COUNTRY_ID));
			_Country = __GetMainActivity().GetCountries().GetById(id);
		}
		_Phone = pStore.get(PHONE) == null ? "" : pStore.get(PHONE);
		_Promo = pStore.get(PROMO) == null ? "" : pStore.get(PROMO);
		
		View view = pInflater.inflate(R.layout.login, pContainer, false);
		view.findViewById(R.id.ivLoginBack).setOnClickListener(this);
		view.findViewById(R.id.llLoginCountry).setOnClickListener(this);
		view.findViewById(R.id.ivLoginInfo).setOnClickListener(this);
		view.findViewById(R.id.btnLoginNext).setOnClickListener(this);
		view.findViewById(R.id.btnLoginDialogChange).setOnClickListener(this);
		view.findViewById(R.id.btnLoginDialogOK).setOnClickListener(this);
		
		((TextView) view.findViewById(R.id.tvLoginCountry)).setText(_Country.Name);
		((TextView) view.findViewById(R.id.tvLoginCountryCode)).setText(_Country.GetCode());
		((ImageView) view.findViewById(R.id.ivLoginFlag)).setImageBitmap(_Country.Flag);
		
		_tvCode = (TextView) view.findViewById(R.id.tvLoginPhoneCode);
		_tvCode.setText(_Country.GetCode());
		
		_etPhone = (EditText) view.findViewById(R.id.etLoginPhone);
		_etPhone.setText(_Phone);
		_etPhone.addTextChangedListener(_PhoneTextWatcher);
		
		_etPromo = (EditText) view.findViewById(R.id.etLoginPromo);
		_etPromo.setText(_Promo);
		_etPromo.addTextChangedListener(_PromoTextWatcher);
		
		_rlDialog = (RelativeLayout) view.findViewById(R.id.rlLoginDialog);
		_rlDialog.setVisibility(View.GONE);
		
		_tvPhone = (TextView) view.findViewById(R.id.tvLoginDialogPhone);

		return view;
	}
	
	@Override
	protected void __OnResume()
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(_etPhone.getWindowToken(), 0);
	    imm.hideSoftInputFromWindow(_etPromo.getWindowToken(), 0);
	}

	@Override
	protected void __OnSave(SparseArray<String> pStore)
	{
		pStore.put(PHONE, _Phone);
		pStore.put(PROMO, _Promo);
	}
	
	@Override
	protected void __OnDestroy()
	{
		_etPhone.removeTextChangedListener(_PhoneTextWatcher);
		_etPromo.removeTextChangedListener(_PromoTextWatcher);
	}
	
	@Override
	protected boolean __OnBackPressed()
	{
		if (_rlDialog.getVisibility() == View.VISIBLE)
		{
			_rlDialog.setVisibility(View.GONE);
			return false;
		}
		return true;
	}

	@Override
	public void onClick(View pView)
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(_etPhone.getWindowToken(), 0);
	    imm.hideSoftInputFromWindow(_etPromo.getWindowToken(), 0);
		switch (pView.getId())
		{
			case R.id.ivLoginBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.llLoginCountry:
				__GetMainActivity().GetManager().GoToCountries(_Country.Id, true);
				break;
			case R.id.ivLoginInfo:
				__GetMainActivity().GetManager().GoToPromoInfo();
				break;
			case R.id.btnLoginNext:
				if (_etPhone.getText().toString().length() == 0)
				{
					__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog18Title), __GetMainActivity().getString(R.string.Dialog18Description));
				}
				else
				{
					_rlDialog.setVisibility(View.VISIBLE);
					_tvPhone.setText("(" + _tvCode.getText().toString() + ") " + _etPhone.getText().toString());
				}
				break;
			case R.id.btnLoginDialogChange:
				_rlDialog.setVisibility(View.GONE);
				break;
			case R.id.btnLoginDialogOK:
				_rlDialog.setVisibility(View.GONE);
				__GetMainActivity().GetProfile().SetID(Long.parseLong(_Country.Code + _etPhone.getText().toString()));
				__GetMainActivity().GetManager().GoToSMS(_Country.Code, _etPhone.getText().toString(), _etPromo.getText().toString());
				break;
		}
	}

	@Override
	public void OnTextChanged(int pID, String pText)
	{
		switch (pID)
		{
			case 1:
				_Phone = pText;
				break;
			case 2:
				_Promo = pText;
				break;
		}
	}
}