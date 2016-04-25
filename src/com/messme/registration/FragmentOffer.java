package com.messme.registration;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.view.MessmeFragment;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;


public class FragmentOffer extends MessmeFragment implements OnClickListener
{
	public FragmentOffer(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		View view = pInflater.inflate(R.layout.offer, pContainer, false);
		view.findViewById(R.id.ivOfferBack).setOnClickListener(this);
		view.findViewById(R.id.btnOfferCancel).setOnClickListener(this);
		view.findViewById(R.id.btnOfferOk).setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivOfferBack:
			case R.id.btnOfferCancel:
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.btnOfferOk:
				__GetMainActivity().GetManager().GoToLogin();
				break;
		}
	}
}