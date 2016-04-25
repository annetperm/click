package com.messme.list;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.messages.dialog.Dialog;
import com.messme.socket.ImageLoader;
import com.messme.user.User;

import android.content.Context;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class AdapterList extends BaseAdapter implements OnClickListener
{
    private ActivityMain _hActivity;
	private LayoutInflater _Inflater;
	private ImageLoader _Loader;
	private LongSparseArray<User> _hUsers = null;
	
	public AdapterList(ActivityMain pActivity, LongSparseArray<User> pUsers) 
    {
        _hActivity = pActivity;
		_Inflater = (LayoutInflater)_hActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_hUsers = pUsers;
		_Loader = new ImageLoader(_hActivity);
	}

	@Override
	public int getCount()
	{
		return _hUsers.size();
	}

	@Override
	public Object getItem(int pPosition)
	{
		return _hUsers.valueAt(pPosition);
	}

	@Override
	public long getItemId(int pPosition)
	{
		return _hUsers.valueAt(pPosition).Id;
	}

	@Override
	public View getView(int pPosition, View pConvertView, ViewGroup pParent)
	{
		if (pConvertView == null)
		{
			pConvertView = _Inflater.inflate(R.layout.list_listitem, pParent, false);
			pConvertView.findViewById(R.id.ivListListitemAvatar).setOnClickListener(this);
			pConvertView.findViewById(R.id.ivListListitemMessage).setOnClickListener(this);
			pConvertView.findViewById(R.id.tvListListitemDistance).setOnClickListener(this);
			pConvertView.findViewById(R.id.tvListListitemMap).setOnClickListener(this);
			pConvertView.findViewById(R.id.tvListListitemName).setOnClickListener(this);
			pConvertView.findViewById(R.id.tvListListitemMessage).setOnClickListener(this);
		}
		
		ImageView avatar = (ImageView) pConvertView.findViewById(R.id.ivListListitemAvatar);
		TextView distance = (TextView) pConvertView.findViewById(R.id.tvListListitemDistance);
		TextView map = (TextView) pConvertView.findViewById(R.id.tvListListitemMap);
		TextView name = (TextView) pConvertView.findViewById(R.id.tvListListitemName);
		TextView message = (TextView) pConvertView.findViewById(R.id.tvListListitemMessage);
		
		User user = _hUsers.get(pPosition);
		
		avatar.setTag(user.Id);
		((ImageView) pConvertView.findViewById(R.id.ivListListitemMessage)).setTag(user.Id);
		distance.setTag(user.Id);
		map.setTag(user.Id);
		name.setTag(user.Id);
		message.setTag(user.Id);
		
		if (user.HasImage())
		{
			avatar.setImageBitmap(user.GetImage());
		}
		else
		{
			avatar.setImageResource(R.drawable.ic_load);
			_Loader.Load(avatar, user);
		}
		
		name.setText(user.GetName(_hActivity));
		
		if (user.GeoStatus == 0 || user.GeoStatus == 2 && !user.IsFriend)
		{
			map.setText(_hActivity.getText(R.string.ListListitemHidden));
			map.setTextColor(_hActivity.getResources().getColor(R.color.TextSub));
		}
		else
		{
			map.setText(_hActivity.getText(R.string.ListListitemToMap));
			map.setTextColor(_hActivity.getResources().getColor(R.color.TextBlue));
		}
		
		if (_hActivity.GetLocation() == null)
		{
			distance.setText("");
		}
		else
		{
			distance.setText(user.GetDistance(_hActivity, _hActivity.GetLocation().getLatitude(), _hActivity.GetLocation().getLongitude()));
		}
		
		Dialog dialog = _hActivity.GetDialogs().GetDialog(user.Id, false);
		if (dialog == null)
		{
			message.setText("");
		}
		else
		{
			message.setText(dialog.GetLastMessage(""));
		}
		
		return pConvertView;
	}

	@Override
	public void onClick(View pView)
	{
		long id = (Long) pView.getTag();
		User user = _hUsers.get(id);
		if (user != null)
		{
			user = _hActivity.GetUsers().AddUser(user, _hActivity);
			switch (pView.getId())
			{
				case R.id.ivListListitemAvatar:
					_hActivity.GetManager().GoToUser(user.Id);
					break;
				case R.id.ivListListitemMessage:
					_hActivity.GetManager().GoToDialog(user.Id, false, false);
					break;
				case R.id.tvListListitemDistance:
				case R.id.tvListListitemMap:
					if (user.GeoStatus == 1 || user.GeoStatus == 2 && user.IsFriend)
					{
						_hActivity.GetManager().GoToMap(user);
					}
					break;
			}
		}
	}
}