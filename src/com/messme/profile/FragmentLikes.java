package com.messme.profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.user.User;
import com.messme.view.MessmeFragment;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


public class FragmentLikes extends MessmeFragment implements OnClickListener
{
	private ListView _lv = null;
	
	private AdapterLikes _Adapter;
	
	
	public FragmentLikes(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	
	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		View view = pInflater.inflate(R.layout.likes, pContainer, false);
		
		view.findViewById(R.id.ivLikesBack).setOnClickListener(this);
		
		_lv = (ListView) view.findViewById(R.id.lvLikes);
		
		_Adapter = new AdapterLikes(__GetMainActivity(), this);
		_lv.setAdapter(_Adapter);
		_lv.setOnItemClickListener(new ListView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> pParent, View pView, int pPosition, long pId)
			{
				__GetMainActivity().GetManager().GoToDialog(pId, false, false);
			}
		});

		try
		{
			JSONObject options = new JSONObject();
			options.put("locale", __GetMainActivity().GetProfile().GetLocale());
			__SendToServer("user.likelist", options);
		}
		catch (JSONException e)
		{
		}
			
		return view;
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivLikesBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
		}
	}
	
	@Override
	protected void __OnMessageReceived(String pMessageId, String pAction, int pStatus, String pResult, JSONObject pOptions)
	{
		if (pAction.equals("user.likelist") && pStatus == 1000)
		{
			try
			{
				JSONArray users = new JSONArray(pResult);
				for (int i = 0; i < users.length(); i++)
				{
					User user = new User(users.getJSONObject(i));
					user = __GetMainActivity().GetUsers().AddUser(user, __GetMainActivity());
					_Adapter.AddUser(user);
				}
				__GetMainActivity().GetProfile().SetLikes(_Adapter.getCount());
				_Adapter.notifyDataSetChanged();
			}
			catch (JSONException e)
			{
			}
		}
		else if (pAction.equals("user.like") && pStatus == 1000)
		{
			try
			{
				long userID = pOptions.getLong("id");
				User user = _Adapter.GetUser(userID);
				if (user != null)
				{
					user.Liked = pOptions.getInt("value") == 1;
					user.Likes = Integer.parseInt(pResult);
					__GetMainActivity().GetUsers().SaveChanges(user, __GetMainActivity());
					_Adapter.notifyDataSetChanged();
				}
			}
			catch (JSONException e)
			{
			}
		}
	}


	public void SetLike(User pUser)
	{
		try
		{
			JSONObject options = new JSONObject();
			options.put("id", pUser.Id);
			options.put("value", pUser.Liked ? 0 : 1);
			__SendToServer("user.like", options);
		}
		catch (JSONException e)
		{
		}
	}
}