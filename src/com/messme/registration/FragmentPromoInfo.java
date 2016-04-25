package com.messme.registration;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.view.MessmeFragment;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

public class FragmentPromoInfo extends MessmeFragment implements OnClickListener
{
	public FragmentPromoInfo(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		View view = pInflater.inflate(R.layout.promo_info, pContainer, false);
		view.findViewById(R.id.ivPromoInfoBack).setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivPromoInfoBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
		}
	}
}