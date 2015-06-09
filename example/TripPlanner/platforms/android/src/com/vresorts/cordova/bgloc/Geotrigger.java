package com.vresorts.cordova.bgloc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.vresorts.cordova.bgloc.beans.Geofence;
import com.vresorts.cordova.bgloc.beans.Place;
import com.vresorts.cordova.bgloc.beans.TripPlan;
import com.vresorts.cordova.bgloc.parser.PlaceParser;
import com.vresorts.cordova.bgloc.parser.TripPlanParser;
import com.vresorts.tripplan.CordovaApp;
import com.vresorts.tripplan.R;

@SuppressLint({ "NewApi", "Instantiatable" })
public class Geotrigger extends BroadcastReceiver{
	
	private static class TripPlanManager{
		
		private class GeotriggerConfig extends JSONObject{
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
		
		private Context mContext;
		
		private TripPlan tripplan;
		
		private boolean isEnabled;
		
		public TripPlan getTripplan() {
			return tripplan;
		}
		public void setTripplan(TripPlan tripplan) {
			this.tripplan = tripplan;
		}
		public boolean isEnabled() {
			return isEnabled;
		}
		public void setEnabled(boolean isEnabled) {
			this.isEnabled = isEnabled;
		}
		TripPlanManager(Context context){
			this.mContext = context;
			GeotriggerConfig config = this.readFromInternalStorage();
			if(config != null){
				this.tripplan = config.getTripPlan();
				this.isEnabled = config.isEnabled();
			}
		}
		private void writeToInternalStorage(GeotriggerConfig config)
		{

		Log.v(Config.TAG, "writeToInternalStorage");
		try{
		// String endOfLine = System.getProperty("line.separator");
		File file = new File(mContext.getFilesDir(), CONFIG_FILE);
		
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


		private GeotriggerConfig readFromInternalStorage()
		{

		Log.v(Config.TAG, "read from internal storage");
		GeotriggerConfig config = null;
		try{
		File file = mContext.getFileStreamPath(CONFIG_FILE);
		if(file.exists() == true)
		{
		Log.v(Config.TAG, "readFileFromInternalStorage File found..."); 

		FileInputStream fis = mContext.openFileInput(file.getName()); 
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
		

		public void save(){
			GeotriggerConfig config = new GeotriggerConfig();
			config.setEnabled(isEnabled);
			config.setTripPlan(tripplan);
			writeToInternalStorage(config);
		}
	}
	
	private boolean isEnabled = false;
	
//	private GeotriggerListener geotriggerListener;
	
	private LocationManager locationManager;
	
	private Context context;
	
	private TripPlan tripplan;
	
	private TripPlanManager tripplanManager;
	
	private static final String INTENT_ACTION_GEOTRIGGER  = "com.vresorts.cordova.bgloc.STATIONARY_REGION_ACTION_TRIGGERED";
	private static final String INTENT_CATEGORY_GEOTRIGGER = "com.vresorts.cordova.bgloc.STATIONARY_REGION";
	
	
	private static final String INTENT_HOST_GEOTRIGGER = "bgloc.cordova.vresorts.com";
	private static final String INTENT_DATA_SCHEME_GEOTRIGGER = "geotrigger";
	
	private static final String CONFIG_FILE = "com.vresorts.cordova.bgloc.CONFIG_FILE";
	
//	
//	public GeotriggerListener getGeotriggerListener() {
//		return geotriggerListener;
//	}
//
//	public void setGeotriggerListener(GeotriggerListener geotriggerListener) {
//		this.geotriggerListener = geotriggerListener;
//	}
	
	
	public void onPause(){
		this.tripplanManager.setEnabled(isEnabled);
		this.tripplanManager.setTripplan(tripplan);
		this.tripplanManager.save();
	}
	
	public Geotrigger(Context context){
		this.context = context;
	    this.locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
		this.tripplanManager = new TripPlanManager(context);
		this.isEnabled = tripplanManager.isEnabled();
		this.tripplan = tripplanManager.getTripplan();
	}
	
	public Geotrigger(){
		
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
		
		Intent intent = new Intent(INTENT_ACTION_GEOTRIGGER);
		intent.addCategory(INTENT_CATEGORY_GEOTRIGGER);
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(INTENT_DATA_SCHEME_GEOTRIGGER);
		builder.authority(INTENT_HOST_GEOTRIGGER);
		builder.appendQueryParameter("placeUuid", place.getUuid());
		intent.setData(builder.build());
		
		intent.putExtra(INTENT_EXTRA_FIELD_PLACE, place.toJSONObject().toString());
		
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
		if(geofence!= null){
		PendingIntent stationaryRegionPI = this.getPendingIntentByPlace(place, true);
		locationManager.addProximityAlert(
                geofence.getLatitude(),
                geofence.getLongitude(),
                geofence.getRadius(),
                (long)-1,
                stationaryRegionPI
        );
		}
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
				this.unregisterPlace(place);
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
	
public static final String INTENT_EXTRA_FIELD_PLACE = "place";
	
	

	@Override
	public void onReceive(Context context, Intent intent) {
		    Log.i(Config.TAG, "stationaryRegionReceiver");
	            String key = LocationManager.KEY_PROXIMITY_ENTERING;

	            Boolean entering = intent.getBooleanExtra(key, false);
	            
	            String placeData = intent.getStringExtra(INTENT_EXTRA_FIELD_PLACE);
	            if(placeData == null){
	            	return;
	            }
	            
	            Place place = null;
				try {
					PlaceParser placeParser = new PlaceParser(placeData);

		             place = placeParser.getPlace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if(place == null){
					Log.v(Config.TAG, "place is null");
					return;
				}
	            
	            IGeotriggerListener geotriggerListener = new GeotriggerListener(context);

	            if (entering) {
	                Log.d(Config.TAG, "- ENTER");
	                place.timeStamp();
	                geotriggerListener.onEnter(place, place.getTimeStamp());

	    			// to just enable the notifications once, delete the place from tripplan.
	                Geotrigger geotrigger = new Geotrigger(context);
	                if(geotrigger != null){
	                boolean isDeleted = geotrigger.deletePlace(place.getUuid());
	                if(isDeleted){
	                	geotrigger.onPause();
	                }
	                }
	            }
	            else if (!entering){
	                Log.d(Config.TAG, "- EXIT");
	                long enterTime = place.getTimeStamp();
	                place.timeStamp();
	                long duration = place.getTimeStamp() - enterTime;
	                geotriggerListener.onExit(place, place.getTimeStamp(), duration);
	            }
	            
		
	}
	
	
    
	public static interface IGeotriggerListener{
		public void onEnter(Place place, long time);
		public void onExit(Place place, long time, long duration);
	}
	
    
    public static class GeotriggerListener implements IGeotriggerListener{
    	
        public static final String INTENT_EXTRA_KEY_NOTIFICATION_OFFER_DATA = "com.vresorts.cordova.bgloc.NOTIFICATION_OFFER_DATA";
    	private static final String INTENT_ACTION_OFFER_NOTIFICATION  = "com.vresorts.cordova.bgloc.OFFER_NOTIFICATION_RECEIVED";
//    	private static final String INTENT_CATEGORY_OFFER_NOTIFICATION = "com.vresorts.cordova.bgloc.OFFER_NOTIFICATION";
    	
    	
    	private static final String INTENT_HOST_OFFER_NOTIFICATION = "bgloc.cordova.vresorts.com";
    	private static final String INTENT_DATA_SCHEME_OFFER_NOTIFICATION = "offerNotification";
    	
    	Context activity;
    	GeotriggerListener(Context context){
    		activity = context;
    	}

		@Override
		public void onEnter(Place place, long time) {
//			Activity activity = BackgroundGeofencingPlugin.this.cordova.getActivity();
			NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
			JSONObject offerData = new JSONObject();
			try {
				offerData.put("offerUuid", place.getOfferUuid());
				offerData.put("placeName", place.getPlaceName());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			Intent intent = new Intent(activity, CordovaApp.class);
//			intent.addCategory(INTENT_CATEGORY_OFFER_NOTIFICATION);
			Uri.Builder uriBuilder = new Uri.Builder();
			uriBuilder.scheme(INTENT_DATA_SCHEME_OFFER_NOTIFICATION);
			uriBuilder.authority(INTENT_HOST_OFFER_NOTIFICATION);
			uriBuilder.appendQueryParameter("placeUuid", place.getUuid());
			intent.setData(uriBuilder.build());
			intent.putExtra(INTENT_EXTRA_KEY_NOTIFICATION_OFFER_DATA, offerData.toString());
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
            PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent,  PendingIntent.FLAG_CANCEL_CURRENT);

            Notification.Builder builder = new Notification.Builder(activity);
            builder.setContentTitle(place.getPlaceName()+"has a new offer");
            builder.setContentText("Click here to view the offer");
            builder.setSmallIcon(R.drawable.icon);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            
            mNotificationManager.notify((int) System.currentTimeMillis(), builder.build());
            
            
			Log.v(Config.TAG, "enter into place :" + place.getPlaceName() + "	at time:" + time);
		
		
			//unregister place in apigee
			Intent tripPlanSyncIntent = new Intent(activity, TripPlanSyncService.class);
			tripPlanSyncIntent.putExtra(TripPlanSyncService.PLACE_ID_TO_UNSUBSRIBE, place.getUuid());
			activity.startService(tripPlanSyncIntent);
		}

		@Override
		public void onExit(Place place, long time, long duration) {
//			Activity activity = BackgroundGeofencingPlugin.this.cordova.getActivity();
//			NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
//			
//			Intent main = new Intent(activity, CordovaApp.class);
//            main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            
//            PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, main,  PendingIntent.FLAG_UPDATE_CURRENT);
//            Notification.Builder builder = new Notification.Builder(activity);
//            builder.setContentTitle("farewell:"+place.getPlaceName());
//            builder.setContentText("exit:" + place.getPlaceName());
//            builder.setSmallIcon(android.R.drawable.ic_menu_mylocation);
//            builder.setContentIntent(pendingIntent);
//            builder.setAutoCancel(true);
//            
//            mNotificationManager.notify((int) System.currentTimeMillis(), builder.build());
			
			Toast.makeText(activity, "exit place:" + place.getPlaceName(), Toast.LENGTH_LONG);
            
			Log.v(Config.TAG, "exit place :" + place.getPlaceName() + "	at time:" + time + "	for:" + duration);
		}
    	
    };
    


}
