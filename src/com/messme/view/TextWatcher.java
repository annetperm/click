package com.messme.view;

import android.text.Editable;


public class TextWatcher implements android.text.TextWatcher
{
	public interface iOnTextChanged
	{
		void OnTextChanged(int pID, String pText);
	}
	
	private final iOnTextChanged _Listener;
	private final int _ID;
	
	
	public TextWatcher(iOnTextChanged pListener, int pID)
	{
		_Listener = pListener;
		_ID = pID;
	}
	
	
	@Override
	public void afterTextChanged(Editable pEditable)
	{
		_Listener.OnTextChanged(_ID, pEditable.toString());
	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{
	}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
	}
}