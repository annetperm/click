package com.messme.socket;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.messme.ActivityMain;
import com.messme.profile.Profile;

import android.content.Context;
import android.os.Handler;
import android.widget.ImageView;


public class ImageLoader
{
	public class BitmapDisplayer implements Runnable 
	{
		private ImageView _hImageView;
		private iImageContainer _hContainer;
		
		public BitmapDisplayer(ImageView pImageView, iImageContainer pContainer) 
		{
			_hImageView = pImageView;
			_hContainer = pContainer;
		} 

		@Override
		public void run()
		{
			iImageContainer container = _ImageViews.get(_hImageView);
			if(container == null || container != _hContainer)
			{
				return;
			}
			
			_hImageView.setImageBitmap(_hContainer.GetImage());
		}
	}
	
	
	class Loader implements Runnable 
	{
		private ImageView _hImageView;
		private iImageContainer _hContainer;
		
		public Loader(ImageView pImageView, iImageContainer pContainer) 
		{
			_hImageView = pImageView;
			_hContainer = pContainer;
		}
		
		@Override
		public void run() 
		{
			_hContainer.LoadImage(_hContext, _hProfile);
			
			BitmapDisplayer bd = new BitmapDisplayer(_hImageView, _hContainer);
			_Handler.post(bd);
		}
	}
	
	
	private Map<ImageView, iImageContainer> _ImageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, iImageContainer>());
	private ExecutorService _Executor = Executors.newFixedThreadPool(5);
	private Handler _Handler = new Handler();
	private final Context _hContext;
	private final Profile _hProfile;
	
	
	public ImageLoader(Context pContext, Profile pProfile)
	{
		_hContext = pContext;
		_hProfile = pProfile;
	}
	public ImageLoader(ActivityMain pActivity)
	{
		_hContext = pActivity;
		_hProfile = pActivity.GetProfile();
	}

	
	public void Load(ImageView pImageView, iImageContainer pContainer)
	{
		_ImageViews.put(pImageView, pContainer);
		_Executor.submit(new Loader(pImageView, pContainer));
	}
}