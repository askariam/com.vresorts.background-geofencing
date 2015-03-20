package com.vresorts.cordova.bgloc;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.vresorts.cordova.bgloc.Geotrigger.GeotriggerListener;
import com.vresorts.cordova.bgloc.beans.Place;
import com.vresorts.cordova.bgloc.beans.TripPlan;
import com.vresorts.cordova.bgloc.parser.PlaceParser;
import com.vresorts.cordova.bgloc.parser.PlaceUuidParser;
import com.vresorts.cordova.bgloc.parser.TripPlanParser;

@SuppressLint("NewApi")
public class BackgroundGeofencingPlugin extends CordovaPlugin {
	
	
    private static final String TAG = "BackgroundFencingPlugin";

    public static final String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_CONFIGURE = "configure";
    public static final String ACTION_RECONFIGURE = "reconfigure";
    public static final String ACTION_DISABLEPLACE = "disablePlace";
    public static final String ACTION_ENABLEPLACE = "enablePlace";
    public static final String ACTION_ADDPLACE = "addPlace";
    public static final String ACTION_DELETEPLACE = "deletePlace";
    public static final String ACTION_GETCURRENTLOCATION = "getCurrentLocation";
    
    //public static final String ACTION_SET_CONFIG = "setConfig";
    public static final String ACTION_MOCK = "mock";
    public static final String ACTION_MOCK_START = "startMock";
    public static final String ACTION_MOCK_STOP = "stopMock";
    
    private boolean isStarted = false;
    
    private TripPlan tripPlan;
    
    private Geofaker geoFaker;
    
    private Geotrigger geoTrigger;
    
    @TargetApi(16)
    private Notification buildForegroundNotification(Notification.Builder builder) {
        return builder.build();
    }

    @SuppressWarnings("deprecation")
    @TargetApi(15)
    private Notification buildForegroundNotificationCompat(Notification.Builder builder) {
        return builder.getNotification();
    }
    
    private GeotriggerListener geotriggerListener = new GeotriggerListener(){

		@Override
		public void onEnter(Place place, long time) {
			Activity activity = BackgroundGeofencingPlugin.this.cordova.getActivity();
			NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
			Intent main = new Intent(activity, BackgroundGeofencingPlugin.class);
            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, main,  PendingIntent.FLAG_UPDATE_CURRENT);

            
            Notification.Builder builder = new Notification.Builder(activity);
            builder.setContentTitle("welcome to"+place.getPlaceName());
            builder.setContentText("you get a coupon from" + place.getPlaceName());
            builder.setSmallIcon(android.R.drawable.ic_menu_mylocation);
            builder.setContentIntent(pendingIntent);
            Notification notification;
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                notification = buildForegroundNotification(builder);
            } else {
                notification = buildForegroundNotificationCompat(builder);
            }
            notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_NO_CLEAR;
            mNotificationManager.notify((int) System.currentTimeMillis(), notification);
            
			
			Log.v(TAG, "enter into place :" + place.getPlaceName() + "	at time:" + time);
		}

		@Override
		public void onExit(Place place, long time, long duration) {
			Activity activity = BackgroundGeofencingPlugin.this.cordova.getActivity();
			NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
			
			Intent main = new Intent(activity, BackgroundGeofencingPlugin.class);
            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, main,  PendingIntent.FLAG_UPDATE_CURRENT);

            
            Notification.Builder builder = new Notification.Builder(activity);
            builder.setContentTitle("happy day"+place.getPlaceName());
            builder.setContentText("you got a coupon from" + place.getPlaceName());
            builder.setSmallIcon(android.R.drawable.ic_menu_mylocation);
            builder.setContentIntent(pendingIntent);
            Notification notification;
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                notification = buildForegroundNotification(builder);
            } else {
                notification = buildForegroundNotificationCompat(builder);
            }
            notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_NO_CLEAR;
            mNotificationManager.notify((int) System.currentTimeMillis(), notification);
            
			Log.v(TAG, "exit place :" + place.getPlaceName() + "	at time:" + time + "	for:" + duration);
		}
    	
    };
    
    
    public boolean execute(String action, JSONArray data, final CallbackContext callbackContext) {
        
    	Activity activity = this.cordova.getActivity();
        Boolean result = false;

        if (ACTION_START.equalsIgnoreCase(action)) {
        	if(isStarted){
                callbackContext.error("Service has already been started");
        	}
        	else if (tripPlan == null) {
                callbackContext.error("Call configure before calling start");
            } else {
            	result = true;
                if(this.geoTrigger == null){
                	this.geoTrigger = new Geotrigger(this.cordova.getActivity(), tripPlan);
                	this.geoTrigger.setGeotriggerListener(geotriggerListener);
                }
                this.geoTrigger.start();
                this.isStarted = true;
                callbackContext.success();
            }
        } else if (ACTION_STOP.equalsIgnoreCase(action)) {
        	if(!isStarted){
            	callbackContext.error("The service hasn't started yet");
        	}
        	
            isStarted = false;
            if(this.geoTrigger != null){
            	this.geoTrigger.stop();
            }
            result = true;
            callbackContext.success();
        } else if (ACTION_CONFIGURE.equalsIgnoreCase(action)){
        	if(isStarted){
            	callbackContext.error("The service has started yet");
            }
            else{
            TripPlan tripPlanToConfigure = null;
            try {
        		TripPlanParser parser = new TripPlanParser(data.toString());
        		tripPlanToConfigure = parser.getTripplan();
        		
			} catch (Exception e) {
				e.printStackTrace();
				callbackContext.error("Errors occur when parsing tripplan data");
			}
            
            if(tripPlanToConfigure != null){
            tripPlan = tripPlanToConfigure;
            this.geoTrigger = new Geotrigger(this.cordova.getActivity(), tripPlan);
            this.geoTrigger.setGeotriggerListener(geotriggerListener);
            result = true;
            }
            
            }
            
        }
    	else if (ACTION_RECONFIGURE.equalsIgnoreCase(action)) {
            if(!isStarted){
            	callbackContext.error("The service is not started yet");
            }
            else{
            TripPlan tripPlanToReconfigure = null;
            try {
        		TripPlanParser parser = new TripPlanParser(data.toString());
        		tripPlanToReconfigure = parser.getTripplan();
        		
			} catch (Exception e) {
				e.printStackTrace();
				callbackContext.error("Errors occur when parsing tripplan data");
			}
            
            if(tripPlanToReconfigure != null && this.geoTrigger !=null){
            tripPlan = tripPlanToReconfigure;
            this.geoTrigger.reset(tripPlan);
            }
            result = true;
            
            }
            
        } 
        else if( ACTION_ADDPLACE.equalsIgnoreCase(action)){
        	if(tripPlan == null){
        		callbackContext.error("The service is not configured yet");
        	}
        	else if(!isStarted){
        		callbackContext.error("The service is not started yet");
        	}
        	else{
        		Place place= null;
        		 try {
             		PlaceParser parser = new PlaceParser(data.toString());
             		place = parser.getPlace();
     			} catch (Exception e) {
     				e.printStackTrace();
     			}
        		
        		 if(place == null){
      				callbackContext.error("Errors occur when parsing tripplan data");
        		 }
        		 else{
        			 String placeUuid = place.getUuid();
        			 Place existantPlace = tripPlan.getPlaceByUuid(placeUuid);
        			 if(existantPlace != null){
        				 callbackContext.error("place is already existing");
        			 }
        			 else{
        			 if(tripPlan != null && this.geoTrigger !=null){

            			 tripPlan.addPlace(place);
        		            this.geoTrigger.reset(tripPlan);
        		            }
        		            result = true;
        			 }
        		 }
        	}
        	
        }
        else if(ACTION_DELETEPLACE.equalsIgnoreCase(action)){
        	if(tripPlan == null){
        		callbackContext.error("The service is not configured yet");
        	}
        	else if(!isStarted){
        		callbackContext.error("The service is not started yet");
        	}
        	else{
        		String placeUuid= null;
        		 try {
             		PlaceUuidParser parser = new PlaceUuidParser(data.toString());
             		placeUuid = parser.getPlaceUuid();
     			} catch (Exception e) {
     				e.printStackTrace();
     			}
        		
        		 if(placeUuid == null){
      				callbackContext.error("Errors occur when parsing place uuid");
        		 }
        		 else{
        			 Place existantPlace = tripPlan.getPlaceByUuid(placeUuid);
        			 if(existantPlace == null){
        				 callbackContext.error("place is not existing");
        			 }
        			 else{
        				 if(tripPlan != null && this.geoTrigger !=null){
        					tripPlan.removePlace(placeUuid);
         		            this.geoTrigger.reset(tripPlan);
         		            }
         		            result = true;
        			
        			 }
        		 }
        	}
        }
        else if(ACTION_ENABLEPLACE.equalsIgnoreCase(action) || ACTION_DISABLEPLACE.equalsIgnoreCase(action)){
        	if(tripPlan == null){
        		callbackContext.error("The service is not configured yet");
        	}
        	else if(!isStarted){
        		callbackContext.error("The service is not started yet");
        	}
        	else{
        		String placeUuid= null;
        		 try {
             		PlaceUuidParser parser = new PlaceUuidParser(data.toString());
             		placeUuid = parser.getPlaceUuid();
     			} catch (Exception e) {
     				e.printStackTrace();
     			}
        		
        		 if(placeUuid == null){
      				callbackContext.error("Errors occur when parsing place uuid");
        		 }
        		 else{
        			 Place existantPlace = tripPlan.getPlaceByUuid(placeUuid);
        			 if(existantPlace == null){
        				 callbackContext.error("place is not existing");
        			 }
        			 else{
        			 if(tripPlan != null && this.geoTrigger !=null){
            			 existantPlace.setSubscribed(ACTION_ENABLEPLACE.equalsIgnoreCase(action) ? true : false);
     		            this.geoTrigger.reset(tripPlan);
     		            }
     		            result = true;
        			 }
        		 }
        	}
        }
        else if(ACTION_GETCURRENTLOCATION.equalsIgnoreCase(action)){
        	 final LocationManager locationManager = (LocationManager) this.cordova.getActivity().getSystemService(Context.LOCATION_SERVICE);
        	 Criteria criteria = new Criteria();
             criteria.setAltitudeRequired(false);
             criteria.setBearingRequired(false);
             criteria.setSpeedRequired(true);
             criteria.setCostAllowed(true);
             criteria.setAccuracy(Criteria.ACCURACY_FINE);
             criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
             criteria.setPowerRequirement(Criteria.POWER_HIGH);
             
        	 	String bestProvider = locationManager.getBestProvider(criteria, true);
                 if (bestProvider != LocationManager.PASSIVE_PROVIDER) {
                     locationManager.requestLocationUpdates(bestProvider, 0, 0, new LocationListener(){

						@Override
						public void onLocationChanged(Location location) {
							JSONObject object = new JSONObject();
							try {
								object.put("lat", location.getLatitude());
								object.put("lng", location.getLongitude());
							} catch (JSONException e) {
								e.printStackTrace();
							}
							callbackContext.success(object);
							locationManager.removeUpdates(this);
						}

						@Override
						public void onProviderDisabled(String arg0) {
							
						}

						@Override
						public void onProviderEnabled(String arg0) {
							
						}

						@Override
						public void onStatusChanged(String arg0, int arg1,
								Bundle arg2) {							
						}
                    	 
                     });
             }
        }
        else if(ACTION_MOCK_START.equalsIgnoreCase(action)){
        	if(this.geoFaker != null && this.geoFaker.isStarted()){
        		callbackContext.error("geofaker has started");
        	}
        	this.geoFaker = new Geofaker(this.cordova.getActivity());
        	this.geoFaker.start();
        }
        else if(ACTION_MOCK_STOP.equalsIgnoreCase(action)){
        	if(this.geoFaker != null && this.geoFaker.isStarted()){
        		this.geoFaker.stop();
        	}
        	else{
        		callbackContext.error("geofaker has already stopped");
        	}
        }
        else if(ACTION_MOCK.equalsIgnoreCase(action)){
        	Intent intent = new Intent();
        	intent.setAction(Geofaker.INTENT_MOCK_GPS_PROVIDER);
        	intent.putExtra(Geofaker.MOCKED_JSON_COORDINATES, data.toString());
        	activity.sendBroadcast(intent);
        	result = true;
        	callbackContext.success();
        }

        return result;
    }

    /**
     * Override method in CordovaPlugin.
     * Checks to see if it should turn off
     */
    public void onDestroy() {
//        Activity activity = this.cordova.getActivity();
//
//        if(isStarted) {
//            activity.stopService(updateServiceIntent);
//        }
    }
    
    /**
     * Function to get the user's current location
     * 
     * @return
     */
    
    
   
}
