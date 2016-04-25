package com.messme.chats;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.messages.AbstractMessageContainer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;


public class ArchiveListFragment extends Fragment implements OnItemClickListener, OnItemLongClickListener
{
	private final ActivityMain _hActivity;
	private final FragmentChats _hFragment;
	private ListView _lv;
	private LinearLayout _llEmpty;
	private AdapterChats _Adapter;
	
	public ArchiveListFragment(ActivityMain pActivity, FragmentChats pFragment)
	{
		_hActivity = pActivity;
		_hFragment = pFragment;
		_Adapter = new AdapterChats(_hActivity, AbstractMessageContainer.ARCHIVE);
	}
	
	@Override
	public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) 
	{
		View view = pInflater.inflate(R.layout.chats_archive, pContainer, false);
		
		_lv = (ListView) view.findViewById(R.id.lvArchive);
		_lv.setOnItemClickListener(this);
		_lv.setOnItemLongClickListener(this);
		_lv.setAdapter(_Adapter);
		
		_llEmpty = (LinearLayout) view.findViewById(R.id.llArchiveEmpty);
		
		Update();
		
	    return view;
	}

	@Override
	public void onItemClick(AdapterView<?> pParent, View pView, int pPosition, long pID)
	{
		_hFragment.Click(_Adapter.getItem(pPosition));
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> pParent, View pView, int pPosition, long pID)
	{
		_hFragment.LongClick(_Adapter.getItem(pPosition), AbstractMessageContainer.ARCHIVE);
		return true;
	}
	
	public void Update()
	{
		if (_Adapter != null)
		{
			_Adapter.notifyDataSetChanged();
			
			if (_llEmpty != null && _lv != null)
			{
				if (_Adapter.getCount() == 0)
				{
					_llEmpty.setVisibility(View.VISIBLE);
					_lv.setVisibility(View.GONE);
				}
				else
				{
					_llEmpty.setVisibility(View.GONE);
					_lv.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	public void SetFilter(String pText)
	{
		if (_Adapter != null)
		{
			_Adapter.SetFilter(pText);
			_Adapter.notifyDataSetChanged();
		}
	}
}