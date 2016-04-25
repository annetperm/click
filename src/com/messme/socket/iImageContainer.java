package com.messme.socket;

import com.messme.profile.Profile;

import android.content.Context;
import android.graphics.Bitmap;

public interface iImageContainer
{
	public void LoadImage(Context pContext, Profile pProfile);
	public Bitmap GetImage();
	public boolean HasLoadedImage();
}
