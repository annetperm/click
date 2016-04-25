package com.messme.chats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.messages.AbstractMessageContainer;
import com.messme.chats.messages.chat.Chat;
import com.messme.chats.messages.delivery.Delivery;
import com.messme.chats.messages.dialog.Dialog;
import com.messme.socket.ImageLoader;
import com.messme.user.User;

import android.content.Context;
import android.support.v4.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class AdapterChats extends BaseAdapter implements OnClickListener
{
    private final ActivityMain _hActivity;
	private final LayoutInflater _Inflater;
	private final ImageLoader _Loader;
	private final ArrayList<AbstractMessageContainer<?>> _Headers = new ArrayList<AbstractMessageContainer<?>>();
	private final int _Type;
	private String _Filter = "";

	
    public AdapterChats(ActivityMain pActivity, int pType) 
    {
    	_hActivity = pActivity;
		_Inflater = (LayoutInflater)_hActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_Loader = new ImageLoader(_hActivity);
		_Type = pType;
		_RefreshData();
    }

	@Override
	public int getCount()
	{
		return _Headers.size();
	}

	@Override
	public AbstractMessageContainer<?> getItem(int pPosition)
	{
		return _Headers.get(pPosition);
	}

	@Override
	public long getItemId(int pPosition)
	{
		return pPosition;
	}

	@Override
	public View getView(int pPosition, View pConventView, ViewGroup pParent)
	{
		if (pConventView == null)
		{
			pConventView = _Inflater.inflate(R.layout.chats_listitem_dialog, pParent, false);
		}

		ImageView ivAvatar = (ImageView) pConventView.findViewById(R.id.ivChatsListitemAvatar);
		TextView tvMain = (TextView) pConventView.findViewById(R.id.tvChatsListitemMain);
		TextView tvTime = (TextView) pConventView.findViewById(R.id.tvChatsListitemTime);
		TextView tvSub = (TextView) pConventView.findViewById(R.id.tvChatsListitemSubText);
		TextView tvCount = (TextView) pConventView.findViewById(R.id.tvChatsListitemCount);
		
		AbstractMessageContainer<?> container = _Headers.get(pPosition);

		ivAvatar.setOnClickListener(this);
		ivAvatar.setTag(container);
		if (container instanceof Dialog)
		{
			Dialog dialog = (Dialog) container;
			User user = _hActivity.GetUsers().GetUser(dialog.GetUserID());
			if (user == null)
			{
				ivAvatar.setImageResource(R.drawable.ic_load);
				
				tvMain.setText(R.string.Load);
			}
			else
			{
				if (user.HasImage())
				{
					ivAvatar.setImageBitmap(user.GetImage());
				}
				else
				{
					ivAvatar.setImageResource(R.drawable.ic_load);
					_Loader.Load(ivAvatar, user);
				}
				
				tvMain.setText(user.GetName(_hActivity));
			}
			
			if (dialog.GetUnReadedCount() > 0)
			{
				tvCount.setVisibility(View.VISIBLE);
				tvCount.setText(dialog.GetUnReadedText());
			}
			else if (dialog.IsUnReadFlag())
			{
				tvCount.setVisibility(View.VISIBLE);
				tvCount.setText("  ");
			}
			else
			{
				tvCount.setVisibility(View.GONE);
			}
		}
		else if (container instanceof Chat)
		{
			Chat chat = (Chat) container;
			if (chat.GetImage() == null)
			{
				ivAvatar.setImageResource(R.drawable.ic_chat_chat);
				_Loader.Load(ivAvatar, chat);
			}
			else
			{
				ivAvatar.setImageBitmap(chat.GetImage());
			}
			tvMain.setText(chat.GetName());
			
			if (chat.GetUnReadedCount() > 0)
			{
				tvCount.setVisibility(View.VISIBLE);
				tvCount.setText(chat.GetUnReadedText());
			}
			else if (chat.IsUnReadFlag())
			{
				tvCount.setVisibility(View.VISIBLE);
				tvCount.setText("  ");
			}
			else
			{
				tvCount.setVisibility(View.GONE);
			}
		}
		else if (container instanceof Delivery)
		{
			Delivery delivery = (Delivery) container;
			ivAvatar.setImageResource(R.drawable.ic_delivery_chat);
			tvMain.setText(delivery.GetName());
			tvCount.setVisibility(View.GONE);
		}
		
		if (container.GetLastMessageID().length() > 0)
		{
			tvTime.setVisibility(View.VISIBLE);
			tvTime.setText(container.GetLastMessageDateTime());
		}
		else
		{
			tvTime.setVisibility(View.GONE);
		}
		
		if (container instanceof Chat)
		{
			Chat chat = (Chat) container;
			if (chat.GetLastSenderID() != _hActivity.GetProfile().GetID())
			{
				if (chat.GetLastSenderID() == 0)
				{
					tvSub.setText("");
				}
				else
				{
					String userName;
					User user = _hActivity.GetUsers().GetUser(chat.GetLastSenderID());
					if (user == null)
					{
						userName = _hActivity.getString(R.string.Load);
					}
					else
					{
						userName = user.GetName(_hActivity);
					}
					
					tvSub.setText(chat.GetLastMessage(userName));
				}
			}
			else
			{
				tvSub.setText(chat.GetLastMessage(""));
			}
		}
		else
		{
			tvSub.setText(container.GetLastMessage(""));
		}
		
		return pConventView;
	}

	@Override
	public void onClick(View pView)
	{
		AbstractMessageContainer<?> container = (AbstractMessageContainer<?>) pView.getTag();
		switch (pView.getId())
		{
			case R.id.ivChatsListitemAvatar:
				if (container instanceof Dialog)
				{
					Dialog dialog = (Dialog) container;
					_hActivity.GetManager().GoToUser(dialog.GetUserID());
				}
				else if (container instanceof Delivery)
				{
					Delivery delivery = (Delivery) container;
					_hActivity.GetManager().GoToDelivery(delivery);
				}
				else if (container instanceof Chat)
				{
					Chat chat = (Chat) container;
					_hActivity.GetManager().GoToGroup(chat);
				}
				break;
		}
	}
	
	@Override
	public void notifyDataSetChanged()
	{
		_RefreshData();
		super.notifyDataSetChanged();
	}

	public void SetFilter(String pText)
	{
		_Filter = pText.toLowerCase().trim();
	}
	
	private void _RefreshData()
	{
		_Headers.clear();
		LongSparseArray<Dialog> dialogs = _hActivity.GetDialogs().GetDialogs();
		for (int i = 0; i < dialogs.size(); i++)
		{
			Dialog dialog = dialogs.valueAt(i);
			if (dialog.GetType() == _Type)
			{
				if (_Filter.length() == 0 || dialog.GetLastMessageText().toLowerCase().contains(_Filter))
				{
					_Headers.add(dialog);
				}
				else
				{
					User user = _hActivity.GetUsers().GetUser(dialog.GetUserID());
					if (user != null && user.GetName(_hActivity).toLowerCase().contains(_Filter))
					{
						_Headers.add(dialog);
					}
				}
			}
		}
		HashMap<String, Chat> chats = _hActivity.GetChats().GetChats();
		for (Chat chat: chats.values())
		{
			if (chat.GetType() == _Type)
			{
				if (_Filter.length() == 0 || chat.GetLastMessageText().toLowerCase().contains(_Filter) || chat.GetName().toLowerCase().contains(_Filter))
				{
					_Headers.add(chat);
				}
			}
		}
		HashMap<String, Delivery> deliveries = _hActivity.GetDeliveries().GetDeliveries();
		for (Delivery delivery: deliveries.values())
		{
			if (delivery.GetType() == _Type)
			{
				if (_Filter.length() == 0 || delivery.GetLastMessageText().toLowerCase().contains(_Filter) || delivery.GetName().toLowerCase().contains(_Filter))
				{
					_Headers.add(delivery);
				}
			}
		}
		
		Collections.sort(_Headers);
	}
}