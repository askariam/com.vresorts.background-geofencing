package com.vresorts.cordova.bgloc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;

import com.vresorts.cordova.bgloc.beans.Geofence;
import com.vresorts.cordova.bgloc.beans.Place;
import com.vresorts.cordova.bgloc.beans.TripPlan;
import com.vresorts.cordova.bgloc.parser.TripPlanParser;

public class Geotrigger {
	
	private TripPlan tripplan;
	
	private boolean isEnabled = false;
	
//	private GeotriggerListener geotriggerListener;
	
	private LocationManager locationManager;
	
	private Context context;
	
	private static final String GEOTRIGGER_ACTION  = "com.vresorts.cordova.bgloc.STATIONARY_REGION_ACTION_TRIGGERED";
	private static final String GEOTRIGGER_CATEGORY = "com.vresorts.cordova.bgloc.STATIONARY_REGION";
	
	
	private static final String GEOTRIGGER_DATA_HOST = "bgloc.cordova.vresorts.com";
	private static final String GEOTRIGGER_DATA_SCHEME = "geotrigger";
	
	private static final String CONFIG_FILE = "com.vresorts.cordova.bgloc.CONFIG_FILE";
	
//	
//	public GeotriggerListener getGeotriggerListener() {
//		return geotriggerListener;
//	}
//
//	public void setGeotriggerListener(GeotriggerListener geotriggerListener) {
//		this.geotriggerListener = geotriggerListener;
//	}
	
	
	private static class GeotriggerConfig extends JSONObject{
		static final String FIELD_TRIP_PLAN = "trip_plan";
		static final String FIELD_IS_ENABLED = "is_enabled";
		
		public GeotriggerConfig(){
			
		}
		
		public GeotriggerConfig(String data) throws JSONException {
			super(data);
		}
		public boolean isEnabled() {
			try {
				return this.getBoolean(FIELD_IS_ENABLED);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		public void setEnabled(boolean isEnabled) {
			try {
				this.put(FIELD_IS_ENABLED, isEnabled);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public TripPlan getTripPlan() {
			try {
				JSONObject tripPlanJSONObject = this.getJSONObject(FIELD_TRIP_PLAN);

				TripPlanParser parser = new TripPlanParser(tripPlanJSONObject);
				
				return parser.getTripplan();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
			
		}
		public void setTripPlan(TripPlan tripPlan) {
			try {
				this.put(FIELD_TRIP_PLAN, tripPlan.toJSONObject());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		
		
		
	}
	
	public void writeToInternalStorage(GeotriggerConfig config)
	{

	Log.v(Config.TAG, "writeToInternalStorage");
	try{
	// String endOfLine = System.getProperty("line.separator");
	File file = new File(context.getFilesDir(), CONFIG_FILE);
	
	if(file.exists()){
	file.delete();
	}

	file.createNewFile();

	// MODE_PRIVATE will create the file (or replace a file of the same name) and make it private to your application. Other modes available are: MODE_APPEND, MODE_WORLD_READABLE, and MODE_WORLD_WRITEABLE.
	FileOutputStream fos = new FileOutputStream(file, false);


	fos.write(config.toString().getBytes());


	Log.v(Config.TAG, "writeFileToInternalStorage complete.. "+config.toString());
	// writer.write(userName);

	fos.close();
	}
	catch(Exception e)
	{
	Log.v(Config.TAG, "Error: " + e.getMessage());
	}
	}


	public GeotriggerConfig readFromInternalStorage()
	{

	Log.v(Config.TAG, "read from internal storage");
	GeotriggerConfig config = null;
	try{
	File file = context.getFileStreamPath(CONFIG_FILE);
	if(file.exists() == true)
	{
	Log.v(Config.TAG, "readFileFromInternalStorage File found..."); 

	FileInputStream fis = context.openFileInput(file.getName()); 
	StringBuilder buffer = new StringBuilder(); 
	int ch;
	while( (ch = fis.read()) != -1){
	buffer.append((char)ch);
	}

	Log.v(Config.TAG, "readFileFromInternalStorage complete.. " + buffer.toString());
	fis.close();
	
	config = new GeotriggerConfig(buffer.toString());

	}
	}
	catch(Exception e)
	{
	Log.v(Config.TAG, "Error: " + e.getMessage());
	}
	return config;
	}
	
	
	public void onPause(){
		GeotriggerConfig config = new GeotriggerConfig();
		config.setEnabled(isEnabled);
		config.setTripPlan(tripplan);
		this.writeToInternalStorage(config);
	}
	
	public Geotrigger(Context context){
		this.context = context;
	    this.locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
		GeotriggerConfig config = this.readFromInternalStorage();
		if(config != null){
			this.tripplan = config.getTripPlan();
			this.isEnabled = config.isEnabled();
		}
	}
	
	public void start(){
		if(this.isEnabled){
			Log.v(Config.TAG, "has started");
			return;
		}
		
		if(this.tripplan != null){
		// Here be the execution of the stationary region monitor
		List<Place> places = this.tripplan.getPlaces();
		for(Place place : places){
			if(place.isSubscribed()){
				registerPlace(place);
			
		}
		}
		
	}
		this.isEnabled = true;
	}
	
	public void stop(){
		if(!this.isEnabled){
			Log.v(Config.TAG, "has not started");
			return;
		}
		
			if(tripplan != null){
			List<Place> places = this.tripplan.getPlaces();
			for(Place place : places){
				if(place.isSubscribed()){
					this.unregisterPlace(place);
				}
			}
			}
		
		this.isEnabled = false;
	}
	
	private PendingIntent getPendingIntentByPlace(Place place, boolean toCreate){
		if(place == null){
			return null;
		}
		
		Intent intent = new Intent(GEOTRIGGER_ACTION);
		intent.addCategory(GEOTRIGGER_CATEGORY);
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(GEOTRIGGER_DATA_SCHEME);
		builder.authority(GEOTRIGGER_DATA_HOST);
		builder.appendQueryParameter("placeUuid", place.getUuid());
		intent.setData(builder.build());
		
		intent.putExtra(GeofenceListener.INTENT_EXTRA_FIELD_PLACE, place.toJSONObject().toString());
		
		int flag = PendingIntent.FLAG_UPDATE_CURRENT;
		
		if(!toCreate){
			flag = PendingIntent.FLAG_NO_CREATE;
		}
		
		PendingIntent stationaryRegionPI = PendingIntent.getBroadcast(context, 0, intent, flag);
		
		return stationaryRegionPI;
		
	}
	
	public void reconfigure(TripPlan tripplan){
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


	public boolean isConfigured() {
		return this.tripplan == null ? false : true;
	}


	public void configure(TripPlan tripPlanToConfigure) {
		this.tripplan = tripPlanToConfigure;
	}


	public boolean addPlace(Place place) {
		if(place != null && this.tripplan != null){
			if(tripplan.getPlaceByUuid(place.getUuid()) != null){
				return false;
			}
			else{
				if(place.isSubscribed()){
				this.registerPlace(place);
				}
				tripplan.addPlace(place);
			}
		}
		return false;
	}
	
	private void registerPlace(Place place){
		Geofence geofence = place.getGeofence();
		PendingIntent stationaryRegionPI = this.getPendingIntentByPlace(place, true);
		
		
		locationManager.addProximityAlert(
                geofence.getLatitude(),
                geofence.getLongitude(),
                geofence.getRadius(),
                (long)-1,
                stationaryRegionPI
        );
	}
	
	private void unregisterPlace(Place place){
		PendingIntent pendingIntent = this.getPendingIntentByPlace(place, false);
		if(pendingIntent != null){
			this.locationManager.removeProximityAlert(pendingIntent);
			Log.v(Config.TAG, place.getPlaceName()+" removed");
		}
	}


	public boolean deletePlace(String placeUuid) {
		if(placeUuid != null && this.tripplan != null){
			Place place = null;
			if((place = tripplan.removePlace(placeUuid)) == null){
				return false;
			}
			else{
				if(place.isSubscribed()){
				this.registerPlace(place);
				}
				return true;
			}
		}
		return false;
	}


	public boolean enablePlace(String placeUuid) {
		if(placeUuid != null && this.tripplan != null){
		 Place place = tripplan.getPlaceByUuid(placeUuid);
		 if(place == null){
			 return false;
		 }
		 else{
			if(!place.isSubscribed()){
			place.setSubscribed(true);
			this.registerPlace(place);
			}
			return true;
	          
		 }
		}
		return false;
	}


	public boolean disablePlace(String placeUuid) {
		if(placeUuid != null && this.tripplan != null){
			 Place place = tripplan.getPlaceByUuid(placeUuid);
			 if(place == null){
				 return false;
			 }
			 else{
				if(place.isSubscribed()){
				place.setSubscribed(false);
				this.unregisterPlace(place);
				}
				return true;
		          
			 }
			}
			return false;
	}

}
