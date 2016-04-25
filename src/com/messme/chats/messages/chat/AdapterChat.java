package com.messme.chats.messages.chat;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.messages.AbstractMessage;
import com.messme.chats.messages.AbstractMessageAdapter;
import com.messme.chats.messages.AbstractMessageContainer;
import com.messme.chats.messages.AbstractMessageFragment;
import com.messme.socket.ImageLoader;
import com.messme.user.User;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public final class AdapterChat extends AbstractMessageAdapter<ChatMessage>
{
	private class ChatViewHolder extends MessageViewHolder 
	{
		private LinearLayout _llUser;
		private TextView _tvUser;
		private ImageView _ivUser;
		public ChatViewHolder(View pItemView)
		{
			super(pItemView);
			_llUser = (LinearLayout) pItemView.findViewById(R.id.llChatListitemName);
			_llUser.setOnClickListener(new LinearLayout.OnClickListener()
			{
				@Override
				public void onClick(View pView)
				{
					if (__hMessage.GetSenderID() != __hActivity.GetProfile().GetID())
					{
						__hActivity.GetManager().GoToUser(__hMessage.GetSenderID());
					}
				}
			});
			_ivUser = (ImageView) pItemView.findViewById(R.id.ivChatListitemAvatar);
			_tvUser = (TextView) pItemView.findViewById(R.id.tvChatListitemName);
		}
    	@Override
    	public void SetHeader(ChatMessage pPreviousMessage)
    	{
    		super.SetHeader(pPreviousMessage);
    		if (pPreviousMessage == null || pPreviousMessage.GetSenderID() != __hMessage.GetSenderID())
        	{
    			_llUser.setVisibility(View.VISIBLE);
    	    	if (__hMessage.GetSenderID() == __hActivity.GetProfile().GetID())
    	    	{
    				_tvUser.setText(__hActivity.getString(R.string.You));
    				_ivUser.setImageBitmap(__hActivity.GetProfile().GetSmallAvatarImage());
    	    	}
    	    	else
    	    	{
    	    		User user = __hActivity.GetUsers().GetUser(__hMessage.GetSenderID());
    	    		if (user == null)
    	    		{
    					_tvUser.setText(R.string.Load);
    					_ivUser.setImageResource(R.color.Background);
    	    		}
    	    		else
    	    		{
    					_tvUser.setText(user.GetName(__hActivity));
    					if (user.HasImage())
    					{
    						_ivUser.setImageBitmap(user.GetImage());
    					}
    					else
    					{
    						_ivUser.setImageResource(R.drawable.ic_load);
    						_Loader.Load(_ivUser, user);
    					}
    	    		}
    	    	}
        	}
    		else
    		{
    			_llUser.setVisibility(View.GONE);
    		}
    	}
    	@Override
    	public void SetStatusView()
    	{
    		super.SetStatusView();        		
        	switch (__hMessage.GetStatus())
    		{
    			case AbstractMessage.STATUS_NOTUPLOADED:
    			case AbstractMessage.STATUS_UPLOADING:
    			case AbstractMessage.STATUS_NOTSENDED:
    				__ivStatus.setImageResource(R.drawable.ic_message_sending);
    				break;
    			case AbstractMessage.STATUS_SENDED:
    			case AbstractMessage.STATUS_RECEIVED:
    			case AbstractMessage.STATUS_READED:
    				__ivStatus.setImageResource(R.drawable.ic_message_sended);
    				break;
    		}
    		__tvTime.setText(__hMessage.GetTime());
    	}
	}

	private final ImageLoader _Loader;

	public AdapterChat(ActivityMain pActivity, AbstractMessageFragment<ChatMessage> pFragment, AbstractMessageContainer<ChatMessage> pMessageContainer)
	{
		super(pActivity, pFragment, pMessageContainer);
		_Loader = new ImageLoader(pActivity);
	}
	

    @Override
    public int getItemViewType(int pPosition) 
    {
    	if (pPosition == getItemCount() - 1)
    	{
    		return TYPE_FOOTER;
    	}
         return __hMessages.get(pPosition).GetSenderID() == __hActivity.GetProfile().GetID() ? TYPE_MY : TYPE_TOME;
    }
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup pParent, int pViewType)
	{
        switch (pViewType) 
        {
	        case TYPE_FOOTER: 
	       	 	return new FooterViewHolder(LayoutInflater.from(pParent.getContext()).inflate(R.layout.messages_listitem_footer, pParent, false));
            case TYPE_MY: 
           	 	return new ChatViewHolder(LayoutInflater.from(pParent.getContext()).inflate(R.layout.chat_listitem_my, pParent, false));
            case TYPE_TOME: 
           	 	return new ChatViewHolder(LayoutInflater.from(pParent.getContext()).inflate(R.layout.chat_listitem_tome, pParent, false));
            default:
                return null;
        }
	}
    
	@Override
	public void OnBottomAdded(int pLastPosition)
	{
		__hFragment.OnBottomAdded(__hMessages.get(pLastPosition).GetSenderID() == __hActivity.GetProfile().GetID());
	}

//	@Override
//	protected void __OnGetItemCount()
//	{
//		ArrayList<ChatMessage> unreaded = new ArrayList<ChatMessage>();
//		for (int i = __hMessages.size() - 1; i != -1; i--)
//		{
//			ChatMessage message = __hMessages.get(i);
//			if (message.GetSenderID() != __hActivity.GetProfile().GetID() && message.GetStatus() != AbstractMessage.STATUS_READED)
//			{
//				unreaded.add(message);
//			}
//		}
//		
//		if (unreaded.size() > 0)
//		{
//			try
//			{
//				JSONObject options = new JSONObject();
//				options.put("id", __hMessageContainer.GetID());
//				options.put("status", __hActivity.IsVisible() ? 2 : 1);
//				JSONArray list = new JSONArray();
//				for (int i = 0; i < unreaded.size(); i++)
//				{
//					list.put(unreaded.get(i).GetID());
//				}
//				options.put("list", list);
//				__hActivity.GetServerConnection().Send(Util.GenerateMID(), "message.setstatus", options);
//			}
//			catch (JSONException e)
//			{
//			}
//		}
//	}
	@Override
	protected void __OnRefreshView(ChatMessage pMessage)
	{
	}

	@Override
	public int GetContainerType()
	{
		return 1;
	}
}