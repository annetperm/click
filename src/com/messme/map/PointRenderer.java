package com.messme.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.messme.R;
import com.messme.user.User;
import com.messme.util.ImageUtil;
import com.messme.util.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

public class PointRenderer extends DefaultClusterRenderer<User>
{
	private final Context _hContext;

	
	public PointRenderer(Context pContext, GoogleMap pMap, ClusterManager<User> pClusterManager)
	{
		super(pContext, pMap, pClusterManager);
		_hContext = pContext;
	}

    @Override
    protected void onBeforeClusterItemRendered(User pUser, MarkerOptions pMarkerOptions) 
    {
    	if (!pUser.HasImage())
    	{
    		pUser.LoadImage(_hContext, null);
    	}
    	pMarkerOptions.title(Long.toString(pUser.Id));
    	pMarkerOptions.position(new LatLng(pUser.Lat, pUser.Lng));
    	Bitmap background;
		if (pUser.IsMale())
		{
			background = BitmapFactory.decodeResource(_hContext.getResources(), R.drawable.map_marker_male);
		}
		else if (pUser.IsFemale())
		{
			background = BitmapFactory.decodeResource(_hContext.getResources(), R.drawable.map_marker_female);
		}
		else
		{
			background = BitmapFactory.decodeResource(_hContext.getResources(), R.drawable.map_marker_noinfo);
		}
		background = ImageUtil.ResizeImageWidth(background, Util.GetPixelsFromDp(_hContext, 40));
				
		Bitmap bmOverlay = Bitmap.createBitmap(background.getWidth(), background.getHeight(), background.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(background, new Matrix(), null);
        canvas.drawBitmap(ImageUtil.GetCircle(pUser.GetImage(), bmOverlay.getWidth() - 6), 3, 3, null);
        
    	pMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(bmOverlay));
    }

//    @Override
//    protected void onBeforeClusterRendered(Cluster<User> pCluster, MarkerOptions pMarkerOptions) 
//    {
////        // Draw multiple people.
////        // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
////        List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
////        int width = mDimension;
////        int height = mDimension;
////
////        for (Person p : cluster.getItems()) {
////            // Draw 4 at most.
////            if (profilePhotos.size() == 4) break;
////            Drawable drawable = getResources().getDrawable(p.profilePhoto);
////            drawable.setBounds(0, 0, width, height);
////            profilePhotos.add(drawable);
////        }
////        MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
////        multiDrawable.setBounds(0, 0, width, height);
////
////        mClusterImageView.setImageDrawable(multiDrawable);
////        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
////        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
//    }

    @SuppressWarnings("rawtypes")
	@Override
    protected boolean shouldRenderAsCluster(Cluster pCluster)
    {
        return pCluster.getSize() > 1;
    }
}
