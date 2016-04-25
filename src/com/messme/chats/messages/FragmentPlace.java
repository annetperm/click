package com.messme.chats.messages;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.messme.ActivityMain;
import com.messme.R;
import com.messme.view.MessmeFragment;

import android.app.FragmentTransaction;
import android.location.Location;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class FragmentPlace extends MessmeFragment implements OnClickListener
{
	private MapFragment _MapFragment = null;
	private GoogleMap _Map = null;
	
	public FragmentPlace(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		View view = pInflater.inflate(R.layout.place, pContainer, false);

		view.findViewById(R.id.ivPlaceBack).setOnClickListener(this);
		
		view.findViewById(R.id.llPlaceShadow1).bringToFront();
		view.findViewById(R.id.llPlaceShadow2).bringToFront();
		
		_MapFragment = (MapFragment)((ActivityMain)getActivity()).getFragmentManager().findFragmentById(R.id.mapPlace);
		_Map = _MapFragment.getMap();
		_Map.setMyLocationEnabled(true);
		_Map.getUiSettings().setZoomControlsEnabled(false);
		Location location = __GetMainActivity().GetLocation();
		CameraPosition cameraPosition = null;
		if (location == null)
		{
			cameraPosition = new CameraPosition(new LatLng(0, 0), 15, 0, 0);
		}
		else
		{
			cameraPosition = new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()), 15, 0, 0);
		}
		_Map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		
		view.findViewById(R.id.llPlace).setOnClickListener(this);
		return view;
	}
	
	@Override
	protected void __OnDestroy()
	{
		FragmentTransaction ft = ((ActivityMain)getActivity()).getFragmentManager().beginTransaction();
		ft.remove(_MapFragment);
		ft.commit();
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivPlaceBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.llPlace:
				SparseArray<String> result = new SparseArray<String>();
				result.put(PLACE_LAT, Double.toString(_Map.getCameraPosition().target.latitude));
				result.put(PLACE_LNG, Double.toString(_Map.getCameraPosition().target.longitude));
				__GetMainActivity().GetManager().GoToBackWithResult(result);
				break;
		}
	}
}