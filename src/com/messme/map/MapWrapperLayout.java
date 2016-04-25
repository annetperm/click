package com.messme.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class MapWrapperLayout extends RelativeLayout 
{
    private GoogleMap _hMap;
    private Marker _hMarker;
    private View _hInfoWindow;    
    private int _BottomOffsetPixels;

    public MapWrapperLayout(Context pContext) 
    {
        super(pContext);
    }

    public MapWrapperLayout(Context pContext, AttributeSet pAttrs) 
    {
        super(pContext, pAttrs);
    }

    public MapWrapperLayout(Context pContext, AttributeSet pAttrs, int pDefStyle) 
    {
        super(pContext, pAttrs, pDefStyle);
    }


    public void Init(GoogleMap pMap, int pBottomOffsetPixels) 
    {
        _hMap = pMap;
        _BottomOffsetPixels = pBottomOffsetPixels;
    }


    public void SetMarkerWithInfoWindow(Marker pMarker, View pInfoWindow) 
    {
        _hMarker = pMarker;
        _hInfoWindow = pInfoWindow;
    }

    
    @SuppressLint("Recycle")
	@Override
    public boolean dispatchTouchEvent(MotionEvent pMotionEvent) 
    {
        boolean b = false;
        if (_hMarker != null && _hMarker.isInfoWindowShown() && _hMap != null && _hInfoWindow != null) 
        {
            Point point = _hMap.getProjection().toScreenLocation(_hMarker.getPosition());
            MotionEvent copyEv = MotionEvent.obtain(pMotionEvent);
            copyEv.offsetLocation(
                -point.x + (_hInfoWindow.getWidth() / 2), 
                -point.y + _hInfoWindow.getHeight() + _BottomOffsetPixels);
            b = _hInfoWindow.dispatchTouchEvent(copyEv);
        }
        return b || super.dispatchTouchEvent(pMotionEvent);
    }
}
