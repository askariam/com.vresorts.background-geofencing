package com.vresorts.cordova.bgloc;

import java.util.Date;
import java.util.List;

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

import com.vresorts.cordova.bgloc.beans.Place;

@SuppressLint("NewApi")
public class GeoFaker {
	 private Context context;
	 
	 public static final String MOCKED_JSON_COORDINATES = "MOCKED_JSON_COORDINATES";
	    
	 public static final String INTENT_MOCK_GPS_PROVIDER = "MOCK_GPS_PROVIDER";
	    
	 private MockGpsProvider mockGpsProvider;
	 
	 private GeoTrigger geotrigger;
	 
	 private boolean isStarted = false;
	 
	 public GeoFaker(Context context){
		 this.context = context;
	 }
	 private BroadcastReceiver mockGpsUpdatesReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				android.os.Debug.waitForDebugger();
				try {
					JSONArray coordinatesArray = new JSONArray(intent.getStringExtra(MOCKED_JSON_COORDINATES));
					for(int index = 0; index < coordinatesArray.length(); index++){
						JSONObject coordinates = coordinatesArray.getJSONObject(index);
					if(mockGpsProvider != null){
						mockGpsProvider.mock(coordinates.getDouble("lat"), coordinates.getDouble("lng"));
					}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				
			}
	    	
	    };
	    
	    
	    
	    
	   public GeoTrigger getGeotrigger() {
		return geotrigger;
	}

	public void setGeotrigger(GeoTrigger geotrigger) {
		this.geotrigger = geotrigger;
	}

	public void start(){
		   	mockGpsProvider = new MockGpsProvider(this.context);
	        mockGpsProvider.setLocationListener(new LocationListener(){

				@Override
				public void onLocationChanged(Location location) {
					android.os.Debug.waitForDebugger();
					if(geotrigger == null){
						return;
					}
					
					if(geotrigger.isEnabled() || geotrigger.getGeotriggerListener() == null||geotrigger.getTripplan() == null){
						return;
					}
					
					List<Place> places = geotrigger.getTripplan().getPlaces();
					
					for(Place place : places){
						boolean isLocatedIn = place.isLocatedIn(location);
						if(isLocatedIn && !place.wasLocatedIn()) {
							place.setLocatedIn(true);
							geotrigger.getGeotriggerListener().onEnter(place, place.getTimeStampOnEnter());
						}
						else if(!isLocatedIn && place.wasLocatedIn()){
							 long exitingTime = System.currentTimeMillis();
				                long duration = exitingTime - place.getTimeStampOnEnter();
							place.setLocatedIn(false);
							geotrigger.getGeotriggerListener().onExit(place, exitingTime, duration);
						}
					}
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
	        this.mockGpsProvider.enable();
	        
	        context.registerReceiver(this.mockGpsUpdatesReceiver, new IntentFilter(INTENT_MOCK_GPS_PROVIDER));
	        
	        this.isStarted = true;
	}
	   
	   public void stop(){
		   if(this.mockGpsProvider != null){
			   this.mockGpsProvider.destroy();
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
			
			private Location templateLocation;
			
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
//		        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){ 
//		        	// use real GPS provider if enabled on the device
//		            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//		        }
//		        else 
		        if(!locationManager.isProviderEnabled(MockGpsProvider.GPS_MOCK_PROVIDER)) {
		        	// otherwise enable the mock GPS provider
		        	locationManager.addTestProvider(MockGpsProvider.GPS_MOCK_PROVIDER, false, false,
		        			false, false, true, true, true, 0, 5);
		        	locationManager.setTestProviderEnabled(MockGpsProvider.GPS_MOCK_PROVIDER, true);
		        }  
		        
		        if(locationManager.isProviderEnabled(MockGpsProvider.GPS_MOCK_PROVIDER)) {
		        	locationManager.requestLocationUpdates(MockGpsProvider.GPS_MOCK_PROVIDER, 0, 0, locationListener);
		        	Location location = this.getLastBestLocation(locationManager);
		        	templateLocation = new Location(location.getProvider());
		        	templateLocation.setAltitude(location.getAltitude());
		        	templateLocation.setLatitude(location.getLatitude());
		        	templateLocation.setLongitude(location.getLongitude());
		        	templateLocation.setBearing(location.getBearing());
		        	templateLocation.setSpeed(location.getSpeed());
		        	templateLocation.setTime(location.getTime());
		        	templateLocation.setAccuracy(location.getAccuracy());
		        	templateLocation.setElapsedRealtimeNanos(location.getElapsedRealtimeNanos());
		        }

			}
			
			/**
			 * @return the last know best location
			 */
			private Location getLastBestLocation(LocationManager mLocationManager) {
			    Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			    Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			    long GPSLocationTime = 0;
			    if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

			    long NetLocationTime = 0;

			    if (null != locationNet) {
			        NetLocationTime = locationNet.getTime();
			    }

			    if ( 0 < GPSLocationTime - NetLocationTime ) {
			        return locationGPS;
			    }
			    else {
			        return locationNet;
			    }
			}
			
			public void mock(double latitude, double longitude){
				 LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

				if(!locationManager.isProviderEnabled(MockGpsProvider.GPS_MOCK_PROVIDER)&&this.templateLocation == null){
					return;
				}
				
				Location mockLocation = new Location(GPS_MOCK_PROVIDER);
				
				// translate to actual GPS location
				mockLocation.setBearing(templateLocation.getBearing());
				mockLocation.setAltitude(templateLocation.getAltitude());
				mockLocation.setSpeed(templateLocation.getSpeed());
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
			
			public void destroy(){
				// remove it from the location manager
		    	try {
		    		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		    		locationManager.removeTestProvider(MockGpsProvider.GPS_MOCK_PROVIDER);
		    	}
		    	catch (Exception e) {}
			}
		}
}
