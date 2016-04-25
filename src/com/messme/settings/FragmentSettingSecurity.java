package com.messme.settings;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.view.MessmeFragment;
import com.messme.view.TextWatcher;
import com.messme.view.TextWatcher.iOnTextChanged;

import android.content.Context;
import android.text.method.PasswordTransformationMethod;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class FragmentSettingSecurity extends MessmeFragment implements OnClickListener, iOnTextChanged
{
	private LinearLayout _llPinEmpty;
	private EditText _etPinEmpty;
	private ImageView _ivPinEmpty;

	private LinearLayout _llPin;
	private EditText _etPinOld;
	private EditText _etPinNew;
	private EditText _etPinSubmit;
	
	private TextWatcher _OldTextWatcher = null;
	private TextWatcher _NewTextWatcher = null;
	private TextWatcher _SubmitTextWatcher = null;
	
	private String _NewPin = "";
	
	public FragmentSettingSecurity(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
		_OldTextWatcher = new TextWatcher(this, 1);
		_NewTextWatcher = new TextWatcher(this, 2);
		_SubmitTextWatcher = new TextWatcher(this, 3);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		View view = pInflater.inflate(R.layout.setting_security, pContainer, false);
		
		view.findViewById(R.id.ivSettingSecurityBack).setOnClickListener(this);
		
		view.findViewById(R.id.llSettingSecurityShadow).bringToFront();
		
		_llPinEmpty = (LinearLayout) view.findViewById(R.id.llSettingSecurityPinEmpty);
		_etPinEmpty = (EditText) view.findViewById(R.id.etSettingSecurityPinEmpty);
		view.findViewById(R.id.ivSettingSecurityPinEmpty).setOnClickListener(this);
		view.findViewById(R.id.btnSettingSecurityPinEmpty).setOnClickListener(this);

		_llPin = (LinearLayout) view.findViewById(R.id.llSettingSecurityPinChange);
		_ivPinEmpty = (ImageView) view.findViewById(R.id.btnSettingSecurityPinReset);
		_ivPinEmpty.setOnClickListener(this);
		_etPinOld = (EditText) view.findViewById(R.id.etSettingSecurityOld);
		_etPinOld.addTextChangedListener(_OldTextWatcher);
		_etPinNew = (EditText) view.findViewById(R.id.etSettingSecurityNew);
		_etPinNew.addTextChangedListener(_NewTextWatcher);
		_etPinNew.setEnabled(false);
		_etPinSubmit = (EditText) view.findViewById(R.id.etSettingSecuritySubmit);
		_etPinSubmit.addTextChangedListener(_SubmitTextWatcher);
		_etPinSubmit.setEnabled(false);
		
		if (__GetMainActivity().GetProfile().IsSecurityPinInitiated())
		{
			_llPinEmpty.setVisibility(View.GONE);
			_llPin.setVisibility(View.VISIBLE);
		}
		else
		{
			_llPinEmpty.setVisibility(View.VISIBLE);
			_llPin.setVisibility(View.GONE);
		}
		
		return view;
	}
	
	@Override
	protected void __OnDestroy()
	{
		_etPinOld.removeTextChangedListener(_OldTextWatcher);
		_etPinNew.removeTextChangedListener(_NewTextWatcher);
		_etPinSubmit.removeTextChangedListener(_SubmitTextWatcher);
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivSettingSecurityBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.ivSettingSecurityPinEmpty:
				if (_etPinEmpty.getTransformationMethod() instanceof PasswordTransformationMethod)
				{
					_ivPinEmpty.setImageResource(R.drawable.ic_eye_close);
					_etPinEmpty.setTransformationMethod(null);
				}
				else
				{
					_ivPinEmpty.setImageResource(R.drawable.ic_eye_open);
					_etPinEmpty.setTransformationMethod(new PasswordTransformationMethod());
				}
				break;
			case R.id.btnSettingSecurityPinEmpty:
				break;
			case R.id.btnSettingSecurityPinReset:
				break;
		}
	}

	@Override
	public void OnTextChanged(int pID, String pText)
	{
		switch (pID)
		{
			case 1:
				if (__GetMainActivity().GetProfile().CheckSecurityPin(pText))
				{
					_etPinNew.setEnabled(true);
					_etPinSubmit.setEnabled(true);
					if (_etPinNew.requestFocus())
					{
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(_etPinNew, InputMethodManager.SHOW_IMPLICIT);
					}
					_etPinOld.setEnabled(false);
				}
				break;
			case 2:
				_NewPin = pText;
				break;
			case 3:
				if (pText.length() >= 4 && _NewPin.equals(pText))
				{
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				    imm.hideSoftInputFromWindow(_etPinSubmit.getWindowToken(), 0);
					_etPinOld.setEnabled(true);
					_etPinOld.setText("");
					_etPinNew.setEnabled(false);
					_etPinNew.setText("");
					_etPinSubmit.setEnabled(false);
					_etPinSubmit.setText("");
					__GetMainActivity().GetProfile().SetSecurityPin(__GetMainActivity(), pText);
					__GetMainActivity().OpenDialog(1, __GetMainActivity().getText(R.string.SettingSecurityTitle).toString(), __GetMainActivity().getText(R.string.SettingSecurityMessageDescription).toString());
				}
				break;
		}
	}
}