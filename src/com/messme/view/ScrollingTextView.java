package com.messme.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;


public class ScrollingTextView extends TextView
{
	public ScrollingTextView(Context pContext, AttributeSet pAttrs, int pDefStyle) 
	{
		super(pContext, pAttrs, pDefStyle);
	}
	
	public ScrollingTextView(Context pContext, AttributeSet pAttrs) 
	{
		super(pContext, pAttrs);
	}
	
	public ScrollingTextView(Context pContext) 
	{
		super(pContext);
	}
	
	@Override
	protected void onFocusChanged(boolean pFocused, int pDirection, Rect pPreviouslyFocusedRect) 
	{
		if (pFocused) 
		{
			super.onFocusChanged(pFocused, pDirection, pPreviouslyFocusedRect);
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean pFocused) 
	{
		if (pFocused) 
		{
			super.onWindowFocusChanged(pFocused);
		}
	}
	
	@Override
	public boolean isFocused() 
	{
		return true;
	}
}