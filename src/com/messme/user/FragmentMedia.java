package com.messme.user;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.view.MessmeFragment;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;


public class FragmentMedia extends MessmeFragment implements OnClickListener
{
	private TextView _tvCount = null;
	private RecyclerView _rv = null;	
	
	private AdapterMedia _Adapter = null;
	
	
	public FragmentMedia(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		long userID = Long.parseLong(pStore.get(USER_ID));
		
		View view = pInflater.inflate(R.layout.media, pContainer, false);
		
		view.findViewById(R.id.ivMediaBack).setOnClickListener(this);
		
		_tvCount = (TextView) view.findViewById(R.id.tvMediaCount);

		_Adapter = new AdapterMedia(__GetMainActivity(), userID);
				
		_rv = (RecyclerView) view.findViewById(R.id.rvMedia);
		_rv.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
		_rv.setAdapter(_Adapter);
		
		int count = _Adapter.getItemCount();
		String countText;
		if (count == 0)
		{
			countText = __GetMainActivity().getString(R.string.MediaCount567890, count);
		}
		else if (count == 1)
		{
			countText = __GetMainActivity().getString(R.string.MediaCount1, count);
		}
		else if (count <= 4)
		{
			countText = __GetMainActivity().getString(R.string.MediaCount234, count);
		}
		else if (count <= 20)
		{
			countText = __GetMainActivity().getString(R.string.MediaCount567890, count);
		}
		else if (count % 100 == 11)
		{
			countText = __GetMainActivity().getString(R.string.MediaCount567890, count);
		}
		else if (count % 10 == 1)
		{
			countText = __GetMainActivity().getString(R.string.MediaCount1, count);
		}
		else if (count % 10 <= 4)
		{
			countText = __GetMainActivity().getString(R.string.MediaCount234, count);
		}
		else
		{
			countText = __GetMainActivity().getString(R.string.MediaCount567890, count);
		}
		_tvCount.setText(countText);
		
		return view;
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivMediaBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
		}
	}
}