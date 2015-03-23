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

public class Geotrigger {
	
	private final static String TAG = Geotrigger.class.getName();
	
	private TripPlan tripplan;
	
	private boolean isEnabled = false;
	
//	private GeotriggerListener geotriggerListener;
	
	private LocationManager locationManager;
	
	private Context context;
	
	private static final String STATIONARY_REGION_ACTION        = "com.tenforwardconsulting.cordova.bgloc.STATIONARY_REGION_ACTION";
	
//	
//	public GeotriggerListener getGeotriggerListener() {
//		return geotriggerListener;
//	}
//
//	public void setGeotriggerListener(GeotriggerListener geotriggerListener) {
//		this.geotriggerListener = geotriggerListener;
//	}

	public Geotrigger(Context context, TripPlan tripPlan){
		this(context);
		this.tripplan = tripPlan;
	}
	
	public Geotrigger(Context context){
		this.context = context;
			}
	
	public void start(){
	    this.locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
		
		if(this.tripplan != null){
		// Here be the execution of the stationary region monitor
		List<Place> places = this.tripplan.getPlaces();
		for(Place place : places){
			Intent intent = new Intent(STATIONARY_REGION_ACTION);
			intent.putExtra(GeofenceListener.INTENT_EXTRA_FIELD_PLACE, place.toJSONObject().toString());
			
			Geofence geofence = place.getGeofence();
			
			PendingIntent stationaryRegionPI = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			locationManager.addProximityAlert(
	                geofence.getLatitude(),
	                geofence.getLongitude(),
	                geofence.getRadius(),
	                (long)-1,
	                stationaryRegionPI
	        );
			
			place.setPendingIntnet(stationaryRegionPI);
		}
		
//		this.context.registerReceiver(stationaryRegionReceiver, new IntentFilter(STATIONARY_REGION_ACTION));

		
	}
		this.isEnabled = true;
	}
	
	public void stop(){
		if(this.locationManager != null){
			List<Place> places = this.tripplan.getPlaces();
			for(Place place : places){
				PendingIntent pendingIntent = place.getPendingIntent();
				if(pendingIntent != null){
					this.locationManager.removeProximityAlert(pendingIntent);
				}
			}
		}
		
//		this.context.unregisterReceiver(stationaryRegionReceiver);
		
		this.isEnabled = false;
	}
	
	public void reset(TripPlan tripplan){
		if(this.isEnabled){
			this.stop();
		}
		
		this.tripplan = tripplan;
		this.start();
		
	}
	
	public TripPlan getTripplan() {
		return tripplan;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

}
