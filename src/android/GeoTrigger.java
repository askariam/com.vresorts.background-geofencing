package com.vresorts.cordova.bgloc;

import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.util.Log;

import com.vresorts.cordova.bgloc.beans.Geofence;
import com.vresorts.cordova.bgloc.beans.Place;
import com.vresorts.cordova.bgloc.beans.TripPlan;

public class GeoTrigger {
	
	private final static String TAG = GeoTrigger.class.getName();
	
	private TripPlan tripplan;
	
	private boolean isEnabled = false;
	
	private GeotriggerListener geotriggerListener;
	
	private LocationManager locationManager;
	
	private Context context;
	
	private static final String STATIONARY_REGION_ACTION        = "com.tenforwardconsulting.cordova.bgloc.STATIONARY_REGION_ACTION";
	
	private static final String INTENT_EXTRA_FIELD_PLACE_UUID = "place_uuid";
	
	
	
	public GeotriggerListener getGeotriggerListener() {
		return geotriggerListener;
	}

	public void setGeotriggerListener(GeotriggerListener geotriggerListener) {
		this.geotriggerListener = geotriggerListener;
	}

	public GeoTrigger(Context context, TripPlan tripPlan){
		this(context);
		this.tripplan = tripPlan;
	}
	
	public GeoTrigger(Context context){
		this.context = context;
			}
	
	public void start(){
		this.context.registerReceiver(stationaryRegionReceiver, new IntentFilter(STATIONARY_REGION_ACTION));

		this.locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
		
		if(this.tripplan != null){
		// Here be the execution of the stationary region monitor
		List<Place> places = this.tripplan.getPlaces();
		for(Place place : places){
			Intent intent = new Intent(STATIONARY_REGION_ACTION);
			intent.putExtra(INTENT_EXTRA_FIELD_PLACE_UUID, place.getUuid());
			
			Geofence geofence = place.getGeofence();
			
			PendingIntent stationaryRegionPI = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			locationManager.addProximityAlert(
	                geofence.getLatitude(),
	                geofence.getLongitude(),
	                geofence.getRadius(),
	                (long)-1,
	                stationaryRegionPI
	        );
		}
	}
		this.isEnabled = true;
	}
	
	public void stop(){
		if(this.locationManager != null && this.locationManager != null){
			List<Place> places = this.tripplan.getPlaces();
			for(Place place : places){
				PendingIntent pendingIntent = place.getPendingIntent();
				if(pendingIntent != null){
					this.locationManager.removeProximityAlert(pendingIntent);
				}
			}
		}
		
		this.context.unregisterReceiver(stationaryRegionReceiver);
		
		this.isEnabled = false;
	}
	
	public void reset(TripPlan tripplan){
		if(this.isEnabled){
			this.stop();
		}
		
		this.tripplan = tripplan;
		this.start();
		
	}
	
	 /**
	    * Broadcast receiver which detects a user has exit his circular stationary-region determined by the greater of stationaryLocation.getAccuracy() OR stationaryRadius
	    */
	    private BroadcastReceiver stationaryRegionReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            Log.i(TAG, "stationaryRegionReceiver");
	            String key = LocationManager.KEY_PROXIMITY_ENTERING;

	            Boolean entering = intent.getBooleanExtra(key, false);
	            
	            String placeUuid = intent.getStringExtra(INTENT_EXTRA_FIELD_PLACE_UUID);
	            if(tripplan == null && geotriggerListener == null){
	            	return;
	            }
	            
	            Place place = tripplan.getPlaceByUuid(placeUuid);
	            
	            if (entering && !place.wasLocatedIn()) {
	                Log.d(TAG, "- ENTER");
	                place.setLocatedIn(true);
	                geotriggerListener.onEnter(place, place.getTimeStampOnEnter());
	            }
	            else if (!entering && place.wasLocatedIn()){
	                Log.d(TAG, "- EXIT");
	                long exitingTime = System.currentTimeMillis();
	                long duration = exitingTime - place.getTimeStampOnEnter();
	                place.setLocatedIn(false);
	                geotriggerListener.onExit(place, exitingTime, duration);
	            }
	        }
	    };
	
	public static interface GeotriggerListener{
		public void onEnter(Place place, long time);
		public void onExit(Place place, long time, long duration);
	}
	
	public TripPlan getTripplan() {
		return tripplan;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

}
