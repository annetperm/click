package com.messme.user;

import java.util.ArrayList;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.messages.Attachment;
import com.messme.chats.messages.dialog.Dialog;
import com.messme.chats.messages.dialog.DialogMessage;
import com.messme.socket.ImageLoader;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class AdapterMedia extends RecyclerView.Adapter<AdapterMedia.ItemViewHolder>
{
	protected class ItemViewHolder extends RecyclerView.ViewHolder
	{
		private ImageView _iv;
		
    	public ItemViewHolder(View pItemView)
		{
			super(pItemView);
			_iv  = (ImageView) pItemView.findViewById(R.id.ivMediaItem);
		}
    	
    	public void SetView(final Attachment pAttachment)
    	{
    		if (pAttachment.HasLoadedImage())
    		{
    			_iv.setImageBitmap(pAttachment.GetImage());
    		}
    		else
    		{
    			_iv.setImageResource(R.drawable.ic_load);
				_Loader.Load(_iv, pAttachment);
    		}
    		_iv.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					try
					{
						pAttachment.Open(_hActivity, false);
					}
					catch (Exception e)
					{
						_hActivity.OpenDialog(1, _hActivity.getString(R.string.Dialog49Title), _hActivity.getString(R.string.Dialog49Description));
					}
				}
			});
    	}
	}
	
	
	private final ActivityMain _hActivity;
	private final ImageLoader _Loader;
	private ArrayList<Attachment> _Items = new ArrayList<Attachment>();
	
	
	public AdapterMedia(ActivityMain pActivity, long pUserID)
	{
		_hActivity = pActivity;
		_Loader = new ImageLoader(pActivity);
		Dialog dialog = pActivity.GetDialogs().GetDialog(pUserID, false);
		if (dialog != null)
		{
			for (int i = 0; i < dialog.GetMessages().size(); i++)
			{
				DialogMessage message = dialog.GetMessages().get(i);
				if (message.GetAttachmentsCount() > 0)
				{
					for (int j = 0; j < message.GetAttachmentsCount(); j++)
					{
						_Items.add(message.GetAttachment(j));
					}
				}
			}
		}
	}
	

	@Override
	public int getItemCount()
	{
		return _Items.size();
	}

	@Override
	public void onBindViewHolder(ItemViewHolder pViewHolder, int pPosition)
	{
		pViewHolder.SetView(_Items.get(pPosition));
	}

	@Override
	public ItemViewHolder onCreateViewHolder(ViewGroup pParent, int pViewType)
	{
		return new ItemViewHolder(LayoutInflater.from(pParent.getContext()).inflate(R.layout.media_item, pParent, false));
	}
}