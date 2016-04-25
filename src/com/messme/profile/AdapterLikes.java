package com.messme.profile;

import java.util.ArrayList;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.socket.ImageLoader;
import com.messme.user.User;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class AdapterLikes extends BaseAdapter implements OnClickListener
{
	private final ActivityMain _hActivity;
	private final FragmentLikes _hFragment;
	private final LayoutInflater _Inflater;
	private final ImageLoader _Loader;
	private final ArrayList<User> _Items = new ArrayList<User>();
	
	
	public AdapterLikes(ActivityMain pActivity, FragmentLikes pFragment)
	{
		_hActivity = pActivity;
		_hFragment = pFragment;
		_Inflater = (LayoutInflater)_hActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_Loader = new ImageLoader(_hActivity);
	}


	public void AddUser(User pUser)
	{
		_Items.add(pUser);
	}
	
	@Override
	public int getCount()
	{
		return _Items.size();
	}

	@Override
	public User getItem(int pPosition)
	{
		return _Items.get(pPosition);
	}

	@Override
	public long getItemId(int pPosition)
	{
		return _Items.get(pPosition).Id;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public View getView(int pPosition, View pConvertView, ViewGroup pParent)
	{
		User user =_Items.get(pPosition);
		
		if (pConvertView == null)
		{
			pConvertView = _Inflater.inflate(R.layout.likes_listitem, pParent, false);
		}
		
		TextView tvName = (TextView) pConvertView.findViewById(R.id.tvLikesListitemName);
		ImageView ivAvatar = (ImageView) pConvertView.findViewById(R.id.ivLikesListitemAvatar);
		ivAvatar.setOnClickListener(this);
		ivAvatar.setTag(pPosition);
		TextView tvStatus = (TextView) pConvertView.findViewById(R.id.tvLikesListitemStatus);
		TextView tvLikes = (TextView) pConvertView.findViewById(R.id.tvLikesListitemLikes);
		tvLikes.setOnClickListener(this);
		tvLikes.setTag(pPosition);
		
		if (user == null)
		{
			tvName.setText(_hActivity.getString(R.string.Load));
			tvStatus.setText(_hActivity.getString(R.string.Load));
			tvLikes.setText(_hActivity.getString(R.string.Load));
			ivAvatar.setImageResource(R.drawable.ic_load);
		}
		else
		{
			tvName.setText(user.GetName(_hActivity));
			tvStatus.setText(user.GetStatus(_hActivity));

			SpannableString text = new SpannableString(Integer.toString(user.Likes) + "  ");
			if (user.Liked)
			{
				tvLikes.setTextColor(_hActivity.getResources().getColor(R.color.TextBlue));
				text.setSpan(new ImageSpan(BitmapFactory.decodeResource(_hActivity.getResources(), R.drawable.ic_like_pressed)), text.length() - 1, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			else
			{
				tvLikes.setTextColor(_hActivity.getResources().getColor(R.color.TextSub));
				text.setSpan(new ImageSpan(BitmapFactory.decodeResource(_hActivity.getResources(), R.drawable.ic_like)), text.length() - 1, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			tvLikes.setText(text);

			if (user.HasImage())
			{
				ivAvatar.setImageBitmap(user.GetImage());
			}
			else
			{
				ivAvatar.setImageResource(R.drawable.ic_load);
				_Loader.Load(ivAvatar, user);
			}
		}
		
		return pConvertView;
	}

	@Override
	public void onClick(View pView)
	{
		int position = (Integer) pView.getTag();
		User user = _Items.get(position);
		switch (pView.getId())
		{
			case R.id.ivLikesListitemAvatar:
				_hActivity.GetManager().GoToUser(user.Id);
				break;
			case R.id.tvLikesListitemLikes:
				_hFragment.SetLike(user);
				break;
		}
	}


	public User GetUser(long pUserID)
	{
		for (int i = 0; i < _Items.size(); i++)
		{
			if (_Items.get(i).Id == pUserID)
			{
				return _Items.get(i);
			}
		}
		return null;
	}
}