package com.vresorts.cordova.bgloc;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;

/*
 * integrate neiborhood algorithm, implement significant location change listener, fire geofencing notification only using GPS
 * 
 * utilize telephonemanager, and network location tracking.
 */
public class Geotracker extends Service implements LocationListener{
	private static final int DEFAULT_DESIRED_ACCURACY = 50;
	private Integer desiredAccuracy = 100;
	private Integer distanceFilter = 30;
//	private Integer scaledDistanceFilter;
	private Integer locationTimeout = 30;
	@Override
	public void onLocationChanged(Location arg0) {
		
	}
	@Override
	public void onProviderDisabled(String arg0) {
		
	}
	@Override
	public void onProviderEnabled(String arg0) {
		
	}
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	
}
