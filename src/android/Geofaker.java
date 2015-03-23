package com.vresorts.cordova.bgloc;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;


@SuppressLint("NewApi")
public class Geofaker {
	 private Context context;
	 
	 public static final String MOCKED_JSON_COORDINATES = "MOCKED_JSON_COORDINATES";
	    
	 public static final String INTENT_MOCK_GPS_PROVIDER = "MOCK_GPS_PROVIDER";
	    
	 private MockGpsProvider mockGpsProvider;
	 
	 private boolean isStarted = false;
	 
	 public Geofaker(Context context){
		 this.context = context;
	 }
	 private BroadcastReceiver mockGpsUpdatesReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				
				try {
					JSONArray coordinatesArray = new JSONArray(intent.getStringExtra(MOCKED_JSON_COORDINATES));
					for(int index = 0; index < coordinatesArray.length(); index++){
						JSONObject coordinates = coordinatesArray.getJSONObject(index);
					if(mockGpsProvider != null){
						mockGpsProvider.mock(coordinates.getDouble("latitude"), coordinates.getDouble("longitude"));
					}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				
			}
	    	
	    };

	public void start(){
		   	mockGpsProvider = new MockGpsProvider(this.context);
	        mockGpsProvider.setLocationListener(new LocationListener(){

				@Override
				public void onLocationChanged(Location location) {
					Log.v(INTENT_MOCK_GPS_PROVIDER, "mocked location:"+location.getLatitude()+location.getLongitude());
//					if(geotrigger == null){
//						return;
//					}
//					
//					if(geotrigger.isEnabled() || geotrigger.getGeotriggerListener() == null||geotrigger.getTripplan() == null){
//						return;
//					}
//					
//					List<Place> places = geotrigger.getTripplan().getPlaces();
//					
//					for(Place place : places){
//						boolean isLocatedIn = place.isLocatedIn(location);
//						if(isLocatedIn && !place.wasLocatedIn()) {
//							place.setLocatedIn(true);
//							geotrigger.getGeotriggerListener().onEnter(place, place.getTimeStampOnEnter());
//						}
//						else if(!isLocatedIn && place.wasLocatedIn()){
//							 long exitingTime = System.currentTimeMillis();
//				                long duration = exitingTime - place.getTimeStampOnEnter();
//							place.setLocatedIn(false);
//							geotrigger.getGeotriggerListener().onExit(place, exitingTime, duration);
//						}
//					}
				}

				@Override
				public void onProviderDisabled(String provider) {
					
				}

				@Override
				public void onProviderEnabled(String provider) {
					
				}

				@Override
				public void onStatusChanged(String provider, int status,
						Bundle extras) {
					
				}
	        	
	        });
	        mockGpsProvider.enable();
	        
	        context.registerReceiver(this.mockGpsUpdatesReceiver, new IntentFilter(INTENT_MOCK_GPS_PROVIDER));
	        
	        this.isStarted = true;
	}
	   
	   public void stop(){
		   if(this.mockGpsProvider != null){
			   this.mockGpsProvider.disable();
		   }
		   
		   context.unregisterReceiver(mockGpsUpdatesReceiver);
		   
		   this.isStarted = false;
	   }
	   
	   
	   
	   
	   public boolean isStarted() {
		return isStarted;
	}




	public class MockGpsProvider{
			public static final String LOG_TAG = "GpsMockProvider";
			public static final String GPS_MOCK_PROVIDER = "GpsMockProvider";
			
			
			private LocationListener locationListener = new LocationListener(){

				@Override
				public void onLocationChanged(Location arg0) {
					Log.v(LOG_TAG, "onLocationChanged"+arg0.toString());
				}

				@Override
				public void onProviderDisabled(String arg0) {
					Log.v(LOG_TAG, "onProviderDisabled"+arg0.toString());
				}

				@Override
				public void onProviderEnabled(String arg0) {
					Log.v(LOG_TAG, "onProviderEnabled"+arg0.toString());
				}

				@Override
				public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
					Log.v(LOG_TAG, "onProviderEnabled"+arg0.toString());
				}
				
			};
			
			
			public LocationListener getLocationListener() {
				return locationListener;
			}

			public void setLocationListener(LocationListener locationListener) {
				this.locationListener = locationListener;
			}

			private Context context;
			
			public MockGpsProvider(Context context) {
				this.context = context;
			}
			
			public void enable(){
				 /** Setup GPS. */
		        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		        if(!locationManager.isProviderEnabled(MockGpsProvider.GPS_MOCK_PROVIDER)) {
		        	
		        	// otherwise enable the mock GPS provider
		        	locationManager.addTestProvider(MockGpsProvider.GPS_MOCK_PROVIDER, false, false,
		        			false, false, true, true, true, 0, 5);
		        	locationManager.setTestProviderEnabled(MockGpsProvider.GPS_MOCK_PROVIDER, true);
		        }  
		        
		        if(locationManager.isProviderEnabled(MockGpsProvider.GPS_MOCK_PROVIDER)) {
		        	locationManager.requestLocationUpdates(MockGpsProvider.GPS_MOCK_PROVIDER, 0, 0, locationListener);
		        }

			}
			
	public void mock(double latitude, double longitude){
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		if(!locationManager.isProviderEnabled(MockGpsProvider.GPS_MOCK_PROVIDER)){
			return;
		}
				
				Location mockLocation = new Location(GPS_MOCK_PROVIDER);
				
				// translate to actual GPS location
				mockLocation.setBearing(50.0f);
				mockLocation.setAltitude(500);
				mockLocation.setSpeed(5f);
				mockLocation.setTime(new Date().getTime());
				mockLocation.setAccuracy(5);
				mockLocation.setLatitude(latitude);
				mockLocation.setLongitude(longitude);
				mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
				

				// show debug message in log
				Log.d(LOG_TAG, mockLocation.toString());

				// provide the new location
				locationManager.setTestProviderLocation(GPS_MOCK_PROVIDER, mockLocation);
				
			}
			
			public void disable(){
				// remove it from the location manager
		    	try {
		    		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		    		locationManager.removeTestProvider(MockGpsProvider.GPS_MOCK_PROVIDER);
		    	}
		    	catch (Exception e) {}
			}
		}
}
