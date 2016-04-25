package com.messme.chats.messages;

import java.io.File;
import java.util.Date;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.socket.ImageLoader;
import com.messme.util.DateUtil;
import com.messme.util.Util;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


public abstract class AbstractMessageAdapter<T extends AbstractMessage> extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	public final static int TYPE_FOOTER	= 0;
	public final static int TYPE_MY		= 1;
	public final static int TYPE_TOME	= 2;
	
	protected class FooterViewHolder extends RecyclerView.ViewHolder
	{
    	public FooterViewHolder(View pItemView)
		{
			super(pItemView);
		}
	}
	protected abstract class MessageViewHolder extends RecyclerView.ViewHolder implements OnClickListener, OnLongClickListener
	{
		protected T __hMessage;
		private TextView _tvHeader;
		private LinearLayout _llMain;
		private RelativeLayout _rl;
		private LinearLayout _llAttachments;
		private TextView _tvMessage;
		protected TextView __tvTime;
		private ImageView _ivPlace;
		private ImageView _ivTranslate;
		protected ImageView __ivStatus;
    	
    	protected MessageViewHolder(View pItemView)
		{
			super(pItemView);
			_tvHeader = (TextView) pItemView.findViewById(R.id.tvMessagesListitemHeader);
			_llMain = (LinearLayout) pItemView.findViewById(R.id.llMessagesListitem);
			_llMain.setOnLongClickListener(this);
			_rl = (RelativeLayout) pItemView.findViewById(R.id.rlMessagesListitem);
			_rl.setOnClickListener(this);
			_rl.setOnLongClickListener(this);
			_llAttachments = (LinearLayout) pItemView.findViewById(R.id.llMessagesListitemAttachments);
			_tvMessage = (TextView) pItemView.findViewById(R.id.tvMessagesListitemMessage);
			__tvTime = (TextView) pItemView.findViewById(R.id.tvMessagesListitemTime);
			_ivPlace = (ImageView) pItemView.findViewById(R.id.ivMessagesListitemPlace);
			_ivTranslate = (ImageView) pItemView.findViewById(R.id.ivMessagesListitemTranslate);
			__ivStatus = (ImageView) pItemView.findViewById(R.id.ivMessagesListitemStatus);
		}
    	public void SetMessage(T pMessage)
    	{
    		__hMessage = pMessage;
    	}
		public void SetHeader(T pPreviousMessage)
		{
	    	if (pPreviousMessage == null || !pPreviousMessage.GetDate().equals(__hMessage.GetDate()))
	    	{
	    		_tvHeader.setVisibility(View.VISIBLE);
	    		_tvHeader.setText(__hMessage.GetDate());
	    	}
	    	else
	    	{
	    		_tvHeader.setVisibility(View.GONE);
	    	}
		}
		public void SetFilter()
		{
			if (__hMessage.GetMessage().length() == 0)
			{
				_tvMessage.setVisibility(View.GONE);
				return;
			}
			_tvMessage.setVisibility(View.VISIBLE);
			String text;
			if (__hMessage.GetAttachmentsCount() == 0)
			{
				text = __hMessage.GetMessage() + __hActivity.getString(R.string.space);
			}
			else
			{
				text = __hMessage.GetMessage();
			}
			if (__hMessage.GetMessage().toLowerCase().contains(__Filter))
			{
				int index = __hMessage.GetMessage().toLowerCase().indexOf(__Filter);
				Spannable spannable = new SpannableString(text);
				spannable.setSpan(new ForegroundColorSpan(Color.YELLOW), index, index + __Filter.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				_tvMessage.setText(spannable);
			}
			else
			{
				_tvMessage.setText(text);
			}
			_tvMessage.setTextSize(__hActivity.GetProfile().GetMessagesSize());
		}
		public void SetAttachmentView()
		{
			if (__hMessage.GetAttachmentsCount() == 0)
			{
				_llAttachments.setVisibility(View.GONE);
				return;
			}
			_llAttachments.setVisibility(View.VISIBLE);
			_llAttachments.removeAllViews();
			
			for (int i = 0; i < __hMessage.GetAttachmentsCount(); i++)
			{
				Attachment attachment = __hMessage.GetAttachment(i);
				
				AttachmentView attachmentView = null;
				switch (attachment.GetType())
				{
					case Attachment.TYPE_IMAGE:
						attachmentView = new AttachmentImageView(_Inflater, __hMessage, attachment);
						break;
					case Attachment.TYPE_VIDEO:
						attachmentView = new AttachmentVideoView(_Inflater, __hMessage, attachment);
						break;
					case Attachment.TYPE_FILE:
						attachmentView = new AttachmentFileView(_Inflater, __hMessage, attachment);
						break;
					case Attachment.TYPE_AUDIO:
						attachmentView = new AttachmentAudioView(_Inflater, __hMessage, attachment);
						break;
					case Attachment.TYPE_PLACE:
						attachmentView = new AttachmentPlaceView(_Inflater, __hMessage, attachment);
						break;
					case Attachment.TYPE_CONTACT:
						attachmentView = new AttachmentContactView(_Inflater, __hMessage, attachment);
						break;
					case Attachment.TYPE_VOICE:
						attachmentView = new AttachmentVoiceView(_Inflater, __hMessage, attachment);
						break;
				}
				_llAttachments.addView(attachmentView.v);
				attachmentView.v.setOnLongClickListener(this);
				LinearLayout.LayoutParams lp = (LayoutParams) attachmentView.v.getLayoutParams();
				lp.bottomMargin = 10;
				attachmentView.v.setLayoutParams(lp);
						
				switch (__hMessage.GetStatus())
				{
					case AbstractMessage.STATUS_UPLOADING:
						attachmentView.SetProgress(__hMessage.GetProgress());
						break;
					case AbstractMessage.STATUS_NOTUPLOADED:
						attachmentView.SetError();
						break;
					case AbstractMessage.STATUS_NOTSENDED:
					case AbstractMessage.STATUS_SENDED_E:
					case AbstractMessage.STATUS_SENDED:
					case AbstractMessage.STATUS_RECEIVED:
					case AbstractMessage.STATUS_READED:
						switch (attachment.GetLoadStatus())
						{
							case Attachment.STATUS_INITIAL:
								__hMessage.DownloadingAttachments(__hActivity, false);
								attachmentView.SetProgress(attachment.GetProgress());
								break;
							case Attachment.STATUS_DOWNLOADING:
								attachmentView.SetProgress(attachment.GetProgress());
								break;
							case Attachment.STATUS_ERROR:
								attachmentView.SetError();
								break;
							case Attachment.STATUS_STOPTED:
								attachmentView.SetStop();
								break;
							case Attachment.STATUS_DOWNLOADED:
								attachmentView.SetComplite();
								break;
						}
						break;
				}
				
				attachmentView.SetValue();
			}
		}
    	public void SetStatusView()
    	{
        	if (__hMessage.IsGeo())
        	{
        		_ivPlace.setVisibility(View.VISIBLE);
        	}
        	else
        	{
        		_ivPlace.setVisibility(View.GONE);
        	}
        	if (__hMessage.IsTranslate())
        	{
        		_ivTranslate.setVisibility(View.VISIBLE);
        	}
        	else
        	{
        		_ivTranslate.setVisibility(View.GONE);
        	}
    	}
		@Override
		public void onClick(View pView)
		{
			if (__hMessage.IsGeo())
			{
				Util.GoToMap(__hActivity, __hMessage.GetLat(), __hMessage.GetLng());
			}
		}
		@Override
		public boolean onLongClick(View pView)
		{
			__hFragment.CallDialog(__hMessage, _llMain);
			return true;
		}
	}
	private abstract class AttachmentView implements OnClickListener
	{
		public View v;
		private RelativeLayout _rlContainer;
		private ProgressBar _pbStatus;
		private LinearLayout _llStatus;
		private ImageView _ivStatus;
		private TextView _tvStatus;
		protected T __hMessage;
		protected Attachment __hAttachment;
		public AttachmentView(LayoutInflater pInflater, int pID, T pMessage, Attachment pAttachment)
		{
			__hMessage = pMessage;
			__hAttachment = pAttachment;
			v = pInflater.inflate(pID, null, false);
			_rlContainer = (RelativeLayout) v.findViewById(R.id.rlMessagesListitem);
			_llStatus = (LinearLayout) v.findViewById(R.id.llMessagesListitemStatus);
			_pbStatus = (ProgressBar) v.findViewById(R.id.pbMessagesListitemStatus);
			_ivStatus = (ImageView) v.findViewById(R.id.ivMessagesListitemStatus);
			_tvStatus = (TextView) v.findViewById(R.id.tvMessagesListitemStatus);
			_rlContainer.setClickable(false);
			_rlContainer.setOnClickListener(this);
		}
		public void SetProgress(String pValue)
		{
			_rlContainer.setClickable(false);
			_llStatus.setVisibility(View.VISIBLE);
			_pbStatus.setVisibility(View.VISIBLE);
			_ivStatus.setVisibility(View.GONE);
			_tvStatus.setTextColor(__hActivity.getResources().getColor(R.color.TextWhite));
			_tvStatus.setText(pValue);
		}
		public void SetError()
		{
			_rlContainer.setClickable(true);
			_llStatus.setVisibility(View.VISIBLE);
			_pbStatus.setVisibility(View.GONE);
			_ivStatus.setVisibility(View.VISIBLE);
			_ivStatus.setImageResource(R.drawable.ic_error);
			_tvStatus.setText(__hActivity.getString(R.string.Error));
			_tvStatus.setTextColor(__hActivity.getResources().getColor(R.color.TextRed));
		}
		public void SetStop()
		{
			_rlContainer.setClickable(true);
			_llStatus.setVisibility(View.VISIBLE);
			_pbStatus.setVisibility(View.GONE);
			_ivStatus.setVisibility(View.VISIBLE);
			_ivStatus.setImageResource(R.drawable.ic_download);
			_tvStatus.setTextColor(__hActivity.getResources().getColor(R.color.TextWhite));
			_tvStatus.setText(__hAttachment.GetFileSizeText());
		}
		public void SetComplite()
		{
			_rlContainer.setClickable(true);
			_llStatus.setVisibility(View.GONE);
		}
		public void SetValue()
		{
		}
		@Override
		public void onClick(View v)
		{
			switch (__hMessage.GetStatus())
			{
				case AbstractMessage.STATUS_UPLOADING:
					break;
				case AbstractMessage.STATUS_NOTUPLOADED:
					__hMessage.Uploading(__hActivity);
					break;
				case AbstractMessage.STATUS_NOTSENDED:
					break;
				case AbstractMessage.STATUS_SENDED_E:
				case AbstractMessage.STATUS_SENDED:
				case AbstractMessage.STATUS_RECEIVED:
				case AbstractMessage.STATUS_READED:
					if (!__hMessage.DownloadingAttachments(__hActivity, true))
					{
						__OnClick();
					}
					break;
			}
		}
		protected abstract void __OnClick();
	}
	private class AttachmentImageView extends AttachmentView
	{
		ImageView ivMessage;
		public AttachmentImageView(LayoutInflater pInflater, T pMessage, Attachment pAttachment)
		{
			super(pInflater, R.layout.messages_listitem_image, pMessage, pAttachment);
			ivMessage = (ImageView) v.findViewById(R.id.ivMessagesListitemMessage);
			ivMessage.setClickable(false);
		}
		@Override
		public void SetValue()
		{
			super.SetValue();
			if (__hAttachment.HasLoadedImage())
			{
				ivMessage.setImageBitmap(__hAttachment.GetImage());
			}
			else
			{
				ivMessage.setImageResource(R.drawable.ic_load);
				_Loader.Load(ivMessage, __hAttachment);
			}
		}
		@Override
		protected void __OnClick()
		{
			__hAttachment.Open(__hActivity, true);
		}
	}
	private class AttachmentVideoView extends AttachmentView
	{
		ImageView ivMessage;
		//ImageView ivControl;
		public AttachmentVideoView(LayoutInflater pInflater, T pMessage, Attachment pAttachment)
		{
			super(pInflater, R.layout.messages_listitem_video, pMessage, pAttachment);
			ivMessage = (ImageView) v.findViewById(R.id.ivMessagesListitemMessage);
			ivMessage.setClickable(false);
			//ivControl = (ImageView) v.findViewById(R.id.ivMessagesListitemControl);
		}
		@Override
		public void SetProgress(String pValue)
		{
			super.SetProgress(pValue);
			//ivControl.setVisibility(View.GONE);
		}
		@Override
		public void SetError()
		{
			super.SetError();
			//ivControl.setVisibility(View.GONE);
		}
		@Override
		public void SetComplite()
		{
			super.SetComplite();
			//ivControl.setVisibility(View.VISIBLE);
		}
		@Override
		public void SetValue()
		{
			super.SetValue();
			if (__hAttachment.HasLoadedImage())
			{
				ivMessage.setImageBitmap(__hAttachment.GetImage());
			}
			else
			{
				ivMessage.setImageResource(R.drawable.ic_load);
				_Loader.Load(ivMessage, __hAttachment);
			}
		}
		@Override
		protected void __OnClick()
		{
			__hAttachment.Open(__hActivity, true);
		}
	}
	private class AttachmentFileView extends AttachmentView
	{
		ImageView ivMessage;
		TextView tvFileName;
		TextView tvFileSize;
		public AttachmentFileView(LayoutInflater pInflater, T pMessage, Attachment pAttachment)
		{
			super(pInflater, R.layout.messages_listitem_file, pMessage, pAttachment);
			ivMessage = (ImageView) v.findViewById(R.id.ivMessagesListitemMessage);
			tvFileName = (TextView) v.findViewById(R.id.tvMessagesListitemMessage);
			tvFileSize = (TextView) v.findViewById(R.id.tvMessagesListitemLength);
			ivMessage.setClickable(false);
			tvFileName.setClickable(false);
			tvFileSize.setClickable(false);
		}
		@Override
		public void SetValue()
		{
			super.SetValue();
    		tvFileName.setText(__hAttachment.GetTitle());
    		tvFileSize.setText(__hAttachment.GetFileSizeText());
		}
		@Override
		protected void __OnClick()
		{
			__hAttachment.Open(__hActivity, true);
		}
	}
	private class AttachmentAudioView extends AttachmentView
	{
		ImageView ivControl;
		TextView tvLength;
		public AttachmentAudioView(LayoutInflater pInflater, T pMessage, Attachment pAttachment)
		{
			super(pInflater, R.layout.messages_listitem_audio, pMessage, pAttachment);
			ivControl = (ImageView) v.findViewById(R.id.ivMessagesListitemControl);
			tvLength = (TextView) v.findViewById(R.id.tvMessagesListitemLength);
			ivControl.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					_PlayerControlClick(__hAttachment, __hMessage, ivControl);
				}
			});
		}
		@Override
		public void SetProgress(String pValue)
		{
			super.SetProgress(pValue);
			ivControl.setClickable(false);
			ivControl.setImageResource(R.drawable.ic_play);
			tvLength.setVisibility(View.INVISIBLE);
		}
		@Override
		public void SetError()
		{
			super.SetError();
			ivControl.setClickable(false);
			ivControl.setImageResource(R.drawable.ic_play);
			tvLength.setVisibility(View.INVISIBLE);
		}
		@Override
		public void SetComplite()
		{
			super.SetComplite();
			ivControl.setClickable(true);
			tvLength.setVisibility(View.VISIBLE);
			tvLength.setText(DateUtil.GetMinSec(new Date(__hAttachment.GetAudioLength())));
			if (_PlayingAttachmentID.equals(__hAttachment.GetID()))
			{
				ivControl.setImageResource(R.drawable.ic_pause);
			}
			else
			{
				ivControl.setImageResource(R.drawable.ic_play);
			}
		}
		@Override
		protected void __OnClick()
		{
		}
	}
	private class AttachmentPlaceView extends AttachmentView
	{
		ImageView ivMessage;
		public AttachmentPlaceView(LayoutInflater pInflater, T pMessage, Attachment pAttachment)
		{
			super(pInflater, R.layout.messages_listitem_place, pMessage, pAttachment);
			ivMessage = (ImageView) v.findViewById(R.id.ivMessagesListitemMessage);
			ivMessage.setClickable(false);
		}
		@Override
		public void SetComplite()
		{
			super.SetComplite();
			if (__hAttachment.HasLoadedImage())
			{
				ivMessage.setImageBitmap(__hAttachment.GetImage());
			}
			else
			{
				ivMessage.setImageResource(R.drawable.ic_load);
				_Loader.Load(ivMessage, __hAttachment);
			}
		}
		@Override
		protected void __OnClick()
		{
			__hAttachment.Open(__hActivity, true);
		}
	}
	private class AttachmentContactView extends AttachmentView
	{
		ImageView ivMessage;
		TextView tvMessage;
		public AttachmentContactView(LayoutInflater pInflater, T pMessage, Attachment pAttachment)
		{
			super(pInflater, R.layout.messages_listitem_contact, pMessage, pAttachment);
			ivMessage = (ImageView) v.findViewById(R.id.ivMessagesListitemMessage);
			ivMessage.setClickable(false);
			tvMessage = (TextView) v.findViewById(R.id.tvMessagesListitemMessage);
			tvMessage.setClickable(false);
		}
		@Override
		public void SetProgress(String pValue)
		{
			super.SetProgress(pValue);
			ivMessage.setImageResource(R.color.Background);
			tvMessage.setVisibility(View.INVISIBLE);
		}
		@Override
		public void SetError()
		{
			super.SetError();
			ivMessage.setImageResource(R.color.Background);
			tvMessage.setVisibility(View.INVISIBLE);
		}
		@Override
		public void SetComplite()
		{
			super.SetComplite();
			if (__hAttachment.HasLoadedImage())
			{
				ivMessage.setImageBitmap(__hAttachment.GetImage());
			}
			else
			{
				ivMessage.setImageResource(R.drawable.ic_load);
				_Loader.Load(ivMessage, __hAttachment);
			}
			tvMessage.setVisibility(View.VISIBLE);
			tvMessage.setText(__hAttachment.GetContactName());
		}
		@Override
		protected void __OnClick()
		{
			__hAttachment.Open(__hActivity, true);
		}
	}
	private class AttachmentVoiceView extends AttachmentView
	{
		ImageView ivControl;
		TextView tvLength;
		public AttachmentVoiceView(LayoutInflater pInflater, T pMessage, Attachment pAttachment)
		{
			super(pInflater, R.layout.messages_listitem_voice, pMessage, pAttachment);
			ivControl = (ImageView) v.findViewById(R.id.ivMessagesListitemControl);
			tvLength = (TextView) v.findViewById(R.id.tvMessagesListitemLength);
			ivControl.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					_PlayerControlClick(__hAttachment, __hMessage, ivControl);
				}
			});
		}
		@Override
		public void SetProgress(String pValue)
		{
			super.SetProgress(pValue);
			ivControl.setClickable(false);
			ivControl.setImageResource(R.drawable.ic_play);
			tvLength.setVisibility(View.INVISIBLE);
		}
		@Override
		public void SetError()
		{
			super.SetError();
			ivControl.setClickable(false);
			ivControl.setImageResource(R.drawable.ic_play);
			tvLength.setVisibility(View.INVISIBLE);
		}
		@Override
		public void SetComplite()
		{
			super.SetComplite();
			ivControl.setClickable(true);
			tvLength.setVisibility(View.VISIBLE);
			tvLength.setText(DateUtil.GetMinSec(new Date(__hAttachment.GetAudioLength())));
			if (_PlayingAttachmentID.equals(__hAttachment.GetID()))
			{
				ivControl.setImageResource(R.drawable.ic_pause);
			}
			else
			{
				ivControl.setImageResource(R.drawable.ic_play);
			}
		}
		@Override
		public void SetValue()
		{
			super.SetValue();
		}
		@Override
		protected void __OnClick()
		{
		}
	}
	

	protected final ActivityMain __hActivity;
	protected final AbstractMessageFragment<T> __hFragment;
	protected final AbstractMessageContainer<T> __hMessageContainer;
	protected final SortedList<T> __hMessages;
	private final ImageLoader _Loader;
	private final LayoutInflater _Inflater;
	
	protected String __Filter = "";

	private final MediaPlayer _MediaPlayer;
	private String _PlayingAttachmentID = "";
	private String _PlayingMessageID = "";
	
	public AbstractMessageAdapter(ActivityMain pActivity, AbstractMessageFragment<T> pFragment, AbstractMessageContainer<T> pMessageContainer)
	{
		__hActivity = pActivity;
		__hMessageContainer = pMessageContainer;
		__hMessages = __hMessageContainer.GetMessages();
		__hFragment = pFragment;
		_Loader = new ImageLoader(__hActivity);
		_Inflater = __hActivity.getLayoutInflater();
		
		_MediaPlayer = new MediaPlayer();
		_MediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer pMediaPlayer)
			{
				if (_MediaPlayer.isPlaying())
				{
					_MediaPlayer.stop();
				}
				
				String old = _PlayingMessageID;
				_PlayingMessageID = "";
				_PlayingAttachmentID = "";
				
				RefreshView(old);
			}
		});
	}
	
	
	public final String GetContainerID()
	{
		return __hMessageContainer.GetID();
	}	
	
	public final void SetFilter(String pText)
	{
		__Filter = pText.toLowerCase().trim();
		for (int i = __hMessages.size() - 1; i != -1 ; i--)
		{
			if (__hMessages.get(i).GetMessage().length() > 0)
			{
				notifyItemChanged(i);
			}
		}
	}
	public final int GetNextFilteredMessage(boolean pToUp, int pCurrentPosition)
	{
		if (pToUp)
		{
			for (int i = pCurrentPosition - 1; i != -1 ; i--)
			{
				if (__hMessages.get(i).GetMessage().length() > 0 && __hMessages.get(i).GetMessage().toLowerCase().contains(__Filter))
				{
					return i;
				}
			}
		}
		else
		{
			for (int i = pCurrentPosition + 1; i < __hMessages.size() ; i++)
			{
				if (__hMessages.get(i).GetMessage().length() > 0 && __hMessages.get(i).GetMessage().toLowerCase().contains(__Filter))
				{
					return i;
				}
			}
		}
		return pCurrentPosition;
	}
	
	@Override
	public final int getItemCount()
	{
		int count = __hMessages.size() + 1;
		return count;
	}	
	@Override
	public final void onBindViewHolder(ViewHolder pViewHolder, int pPosition)
	{
		if (pPosition == getItemCount() - 1)
		{
		}
		else
		{
			T prev = null;
			if (pPosition - 1 > -1)
			{
				prev = __hMessages.get(pPosition - 1);
			}
			T curr = __hMessages.get(pPosition);
			
			@SuppressWarnings("unchecked")
			MessageViewHolder holder = (MessageViewHolder) pViewHolder;
			holder.SetMessage(curr);
			holder.SetHeader(prev);
			holder.SetFilter();
			holder.SetAttachmentView();
			holder.SetStatusView();
		}
	}


	public final void RefreshView(String pMessageID)
	{
		for (int i = __hMessages.size() - 1; i != -1; i--)
		{
			if (__hMessages.get(i).GetID().equals(pMessageID))
			{
				__OnRefreshView(__hMessages.get(i));
				notifyItemChanged(i);
				break;
			}
		}
	}
	
	private void _PlayerControlClick(Attachment pAttachment, T pMessage, ImageView pControl)
	{
		if (pAttachment.GetID().equals(_PlayingAttachmentID))
		{
			if (_MediaPlayer.isPlaying())
			{
				_MediaPlayer.stop();
			}
			_PlayingMessageID = "";
			_PlayingAttachmentID = "";
			pControl.setImageResource(R.drawable.ic_play);
		}
		else
		{
			if (_PlayingMessageID.length() > 0)
			{
				String old = _PlayingMessageID;
				if (_MediaPlayer.isPlaying())
				{
					_MediaPlayer.stop();
				}
				_PlayingMessageID = "";
				_PlayingAttachmentID = "";
				RefreshView(old);
			}
			
            try
			{
            	_MediaPlayer.reset();
				if (pAttachment.GetRealFilePath().length() > 0 && new File(pAttachment.GetRealFilePath()).exists())
				{
					_MediaPlayer.setDataSource(pAttachment.GetRealFilePath());
				}
				else
				{
					_MediaPlayer.setDataSource(pAttachment.GetFilePath(__hActivity));
				}
				_MediaPlayer.prepare();
				_MediaPlayer.start();
				_PlayingMessageID = pMessage.GetID();
				_PlayingAttachmentID = pAttachment.GetID();
				pControl.setImageResource(R.drawable.ic_pause);
			}
			catch (Exception e)
			{
			}
		}
	}

	public abstract int GetContainerType();
	public abstract void OnBottomAdded(int pLastPosition);
	protected abstract void __OnRefreshView(T pMessage);
}