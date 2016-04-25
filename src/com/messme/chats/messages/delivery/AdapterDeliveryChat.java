package com.messme.chats.messages.delivery;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.messages.AbstractMessage;
import com.messme.chats.messages.AbstractMessageAdapter;
import com.messme.chats.messages.AbstractMessageContainer;
import com.messme.chats.messages.AbstractMessageFragment;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class AdapterDeliveryChat  extends AbstractMessageAdapter<DeliveryMessage>
{
	private class DeliveryViewHolder extends MessageViewHolder
	{
		private ImageView _ivTimer;
		public DeliveryViewHolder(View pItemView)
		{
			super(pItemView);
			_ivTimer = (ImageView) pItemView.findViewById(R.id.ivDeliveryListitemTimer);
		}
    	@Override
    	public void SetStatusView()
    	{
    		super.SetStatusView();
        	switch (__hMessage.GetStatus())
    		{
    			case AbstractMessage.STATUS_NOTUPLOADED:
    				__ivStatus.setImageResource(R.drawable.ic_message_sending);
    				break;
    			case AbstractMessage.STATUS_UPLOADING:
    				__ivStatus.setImageResource(R.drawable.ic_message_sending);
    				break;
    			case AbstractMessage.STATUS_NOTSENDED:
    				__ivStatus.setImageResource(R.drawable.ic_message_sending);
    				break;
    			case AbstractMessage.STATUS_SENDED_E:
    			case AbstractMessage.STATUS_SENDED:
    				__ivStatus.setImageResource(R.drawable.ic_message_sended);
    				break;
    			case AbstractMessage.STATUS_READED:
    				__ivStatus.setImageResource(R.drawable.ic_message_sended);
    				break;
    		}
        	if (__hMessage.IsSnap())
        	{
        		_ivTimer.setVisibility(View.VISIBLE);
        		if (__hMessage.GetStatus() == AbstractMessage.STATUS_READED)
        		{
            		__tvTime.setText(__hMessage.GetTickedSnapTimer());
        		}
        		else
        		{
            		__tvTime.setText(__hMessage.GetTime());
        		}
        	}
        	else
        	{        		
        		_ivTimer.setVisibility(View.GONE);
        		__tvTime.setText(__hMessage.GetTime());
        	}
    	}
	}
	
	
	public AdapterDeliveryChat(ActivityMain pActivity, AbstractMessageFragment<DeliveryMessage> pFragment, AbstractMessageContainer<DeliveryMessage> pMessageContainer)
	{
		super(pActivity, pFragment, pMessageContainer);
	}

	
    @Override
    public int getItemViewType(int pPosition) 
    {
    	if (pPosition == getItemCount() - 1)
    	{
    		return TYPE_FOOTER;
    	}
         return TYPE_MY;
    }
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup pParent, int pViewType)
	{
        switch (pViewType) 
        {
	        case TYPE_FOOTER: 
	       	 	return new FooterViewHolder(LayoutInflater.from(pParent.getContext()).inflate(R.layout.messages_listitem_footer, pParent, false));
            case TYPE_MY: 
           	 	return new DeliveryViewHolder(LayoutInflater.from(pParent.getContext()).inflate(R.layout.delivery_listitem_my, pParent, false));
            default:
                return null;
        }
	}

	@Override
	public void OnBottomAdded(int pLastPosition)
	{
		__hFragment.OnBottomAdded(true);
	}
	@Override
	protected void __OnRefreshView(DeliveryMessage pMessage)
	{
		pMessage.StartSnapTimer(__hActivity);
	}

	@Override
	public int GetContainerType()
	{
		return 2;
	}
}