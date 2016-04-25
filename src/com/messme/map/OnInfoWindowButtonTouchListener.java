package com.messme.map;

import android.graphics.LightingColorFilter;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;

import com.google.android.gms.maps.model.Marker;
import com.messme.R;
import com.messme.user.User;

public abstract class OnInfoWindowButtonTouchListener implements OnTouchListener 
{
    private final ImageButton _hAvatar;
    private final ImageButton _hLikes;
    private final ImageButton _hMessage;
    private Handler _Handler = new Handler();
    private Marker _hMarker;
    private User _hUser;
    private boolean _Pressed = false;

    
    private final Runnable ConfirmAvatarClickRunnable = new Runnable() 
    {
        public void run() 
        {
            if (_EndPress()) 
            {
                __OnAvatarClick(_hMarker, _hUser);
            }
        }
    };
    private final Runnable ConfirmLikesClickRunnable = new Runnable() 
    {
        public void run() 
        {
            if (_EndPress()) 
            {
            	__OnLikesClick(_hMarker, _hUser);
            }
        }
    };
    private final Runnable ConfirmMessageClickRunnable = new Runnable() 
    {
        public void run() 
        {
            if (_EndPress()) 
            {
                __OnMessageClick(_hMarker, _hUser);
            }
        }
    };

    
    public OnInfoWindowButtonTouchListener(ImageButton pAvatar, ImageButton pLikes, ImageButton pMessage) 
    {
    	_hAvatar = pAvatar;
    	_hLikes = pLikes;
    	_hMessage = pMessage;
    }

    
    public void SetMarker(Marker pMarker, User pUser) 
    {
    	_hMarker = pMarker;
    	_hUser = pUser;
    }

    
    @Override
    public boolean onTouch(View pView, MotionEvent pEvent) 
    {
//    	ImageButton ibtn = (ImageButton) pView;
//        if (0 <= pEvent.getX() && pEvent.getX() <= _hAvatar.getWidth() 
//        		&& 0 <= pEvent.getY() && pEvent.getY() <= _hAvatar.getHeight())
//        {
//        	ibtn = _hAvatar;
//        }
//        else if (_hMessage.getLeft() <= pEvent.getX() && pEvent.getX() <= _hMessage.getLeft() + _hMessage.getWidth() 
//        	&& _hMessage.getTop() <= pEvent.getY() && pEvent.getY() <= _hMessage.getTop() + _hMessage.getHeight())
//        {
//        	ibtn = _hMessage;
//        }
//        else if (0 <= pEvent.getX() && pEvent.getX() <= _hLikes.getWidth() && 0 <= pEvent.getY() && pEvent.getY() <= _hLikes.getHeight())
//        {
//        	ibtn = _hLikes;
//        }
        if (pView != null)
        {
            switch (pEvent.getActionMasked()) 
            {
                case MotionEvent.ACTION_DOWN:
                	if (pView == _hAvatar)
                	{
                    	_StartPress(_hAvatar);
                	}
                	else if (pView == _hLikes)
                	{
                    	_StartPress(_hLikes);
                	}
                	else if (pView == _hMessage)
                	{
                    	_StartPress(_hMessage);
                	}
                	else
                	{
                    	_StartPress(null);
                	}
                	break;
                case MotionEvent.ACTION_UP: 
                	if (pView == _hAvatar)
                	{
                    	_Handler.postDelayed(ConfirmAvatarClickRunnable, 150); 
                	}
                	else if (pView == _hLikes)
                	{
                    	_Handler.postDelayed(ConfirmLikesClickRunnable, 150); 
                	}
                	else if (pView == _hMessage)
                	{
                    	_Handler.postDelayed(ConfirmMessageClickRunnable, 150); 
                	}
                	break;
                case MotionEvent.ACTION_CANCEL: 
                	_EndPress(); 
    	            break;
                default: 
                	break;
            }
        }
        else
        {
        	_EndPress();
        }
        return false;
    }
    

    protected abstract void __OnAvatarClick(Marker pMarker, User pUser);
    protected abstract void __OnLikesClick(Marker pMarker, User pUser);
    protected abstract void __OnMessageClick(Marker pMarker, User pUser);

    
    private void _StartPress(ImageButton pButton)
    {
        if (!_Pressed) 
        {
        	_Pressed = true;
        	if (pButton == _hAvatar)
        	{
        		_hAvatar.setColorFilter(new LightingColorFilter(0x9999FF, 0x000099));
        	}
        	else if (pButton == _hLikes)
        	{
        		//_hLikes.setBackground(bgDrawablePressed);
        	}
        	else if (pButton == _hMessage)
        	{
        		_hMessage.setImageResource(R.drawable.ic_dialog);
        	}
            _Handler.removeCallbacks(ConfirmAvatarClickRunnable);
            _Handler.removeCallbacks(ConfirmLikesClickRunnable);
            _Handler.removeCallbacks(ConfirmMessageClickRunnable);
            if (_hMarker != null) 
            {
            	_hMarker.showInfoWindow();
            }
        }
    }
    
    private boolean _EndPress() 
    {
        if (_Pressed) 
        {
            _Pressed = false;
    		_hAvatar.setColorFilter(null);
    		//_hLikes.setBackground(bgDrawablePressed);
    		_hMessage.setImageResource(R.drawable.ic_dialog_pressed);
            _Handler.removeCallbacks(ConfirmAvatarClickRunnable);
            _Handler.removeCallbacks(ConfirmLikesClickRunnable);
            _Handler.removeCallbacks(ConfirmMessageClickRunnable);
            if (_hMarker != null) 
            {
            	_hMarker.showInfoWindow();
            }
            return true;
        }
        else
        {
            return false;
        }
    }
}