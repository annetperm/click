package com.messme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkStateReceiver extends BroadcastReceiver
{	
	@Override
	public void onReceive(Context pContext, Intent intent)
	{
		try
		{
			if (pContext instanceof ActivityMain)
			{
				((ActivityMain)pContext).InternetConnection();
			}
		}
		catch (Exception e)
		{
			
		}
	}
}
