package com.messme.user;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.view.MessmeFragment;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.ViewGroup;

public class FragmentSharedGroups extends MessmeFragment implements OnClickListener
{
	private AdapterSharedGroups _Adapter = null;
	
	public FragmentSharedGroups(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		long userID = Long.parseLong(pStore.get(USER_ID));
		
		View view = pInflater.inflate(R.layout.sharedgroups, pContainer, false);
		
		view.findViewById(R.id.ivSharedGroupsBack).setOnClickListener(this);
		
		ListView lv = (ListView) view.findViewById(R.id.lvSharedGroups);
		_Adapter = new AdapterSharedGroups(__GetMainActivity(), userID);
		lv.setAdapter(_Adapter);
		lv.setOnItemClickListener(new ListView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pPosition, long id)
			{
				__GetMainActivity().GetManager().GoToChat(_Adapter.getItem(pPosition), false, false);
			}
		});
		
		return view;
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivSharedGroupsBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
		}
	}
}