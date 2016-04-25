package com.messme.chats.messages.dialog;

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


public class AdapterDialog extends AbstractMessageAdapter<DialogMessage>
{
	private class DialogViewHolder extends MessageViewHolder 
	{
		private ImageView _ivTimer = null;
		private ImageView _ivDelivery = null;
    	public DialogViewHolder(View pItemView)
		{
			super(pItemView);
			_ivTimer = (ImageView) pItemView.findViewById(R.id.ivDialogListitemTimer);
			_ivDelivery = (ImageView) pItemView.findViewById(R.id.ivDialogListitemDelivery);
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
    			case AbstractMessage.STATUS_RECEIVED:
    				__ivStatus.setImageResource(R.drawable.ic_message_received);
    				break;
    			case AbstractMessage.STATUS_READED:
    				__ivStatus.setImageResource(R.drawable.ic_message_readed);
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
//        	if (__hMessage.IsDeliveryMessage())
//        	{
//        		_ivDelivery.setVisibility(View.VISIBLE);
//        	}
//        	else
        	{
        		_ivDelivery.setVisibility(View.GONE);
        	}
    	}
	}
	
	
	public AdapterDialog(ActivityMain pActivity, AbstractMessageFragment<DialogMessage> pFragment, AbstractMessageContainer<DialogMessage> pMessageContainer)
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
         return __hMessages.get(pPosition).IsMy() ? TYPE_MY : TYPE_TOME;
    }
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup pParent, int pViewType)
	{
        switch (pViewType) 
        {
	        case TYPE_FOOTER: 
           	 	return new FooterViewHolder(LayoutInflater.from(pParent.getContext()).inflate(R.layout.messages_listitem_footer, pParent, false));
            case TYPE_MY: 
           	 	return new DialogViewHolder(LayoutInflater.from(pParent.getContext()).inflate(R.layout.dialog_listitem_my, pParent, false));
            case TYPE_TOME: 
           	 	return new DialogViewHolder(LayoutInflater.from(pParent.getContext()).inflate(R.layout.dialog_listitem_tome, pParent, false));
            default:
                return null;
        }
	}
	
	@Override
	public void OnBottomAdded(int pLastPosition)
	{
		__hFragment.OnBottomAdded(__hMessages.get(pLastPosition).IsMy());
	}

	@Override
	public int GetContainerType()
	{
		return 0;
	}

	@Override
	protected void __OnRefreshView(DialogMessage pMessage)
	{
		pMessage.StartSnapTimer(__hActivity);
	}
}