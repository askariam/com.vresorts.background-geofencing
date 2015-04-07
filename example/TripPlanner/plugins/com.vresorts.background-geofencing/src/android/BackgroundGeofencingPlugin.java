package com.vresorts.cordova.bgloc;

import java.util.HashMap;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.vresorts.cordova.bgloc.beans.Place;
import com.vresorts.cordova.bgloc.beans.TripPlan;
import com.vresorts.cordova.bgloc.parser.PlaceParser;
import com.vresorts.cordova.bgloc.parser.PlaceUuidParser;
import com.vresorts.cordova.bgloc.parser.TripPlanParser;

@SuppressLint("NewApi")
public class BackgroundGeofencingPlugin extends CordovaPlugin {

    public static final String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_CONFIGURE = "configure";
    public static final String ACTION_RECONFIGURE = "reconfigure";
    public static final String ACTION_DISABLEPLACE = "disablePlace";
    public static final String ACTION_ENABLEPLACE = "enablePlace";
    public static final String ACTION_ADDPLACE = "addPlace";
    public static final String ACTION_DELETEPLACE = "deletePlace";
    public static final String ACTION_GETCURRENTLOCATION = "getCurrentLocation";
    public static final String ACTION_SETONNOTIFICATIONCLICKEDCALLBACK = "setOnNotificationClickedCallback";
    
    //public static final String ACTION_SET_CONFIG = "setConfig";
    public static final String ACTION_MOCK = "mock";
    public static final String ACTION_MOCK_START = "startMock";
    public static final String ACTION_MOCK_STOP = "stopMock";
    
    private Geofaker geoFaker;
    
    private Geotrigger geotrigger;
    
    Activity activity;
    

    
    @Override
	protected void pluginInitialize() {
		super.pluginInitialize();
		this.activity = this.cordova.getActivity();
		geotrigger = new Geotrigger(activity);
	}
    
	@Override
	public void onPause(boolean multitasking) {
		super.onPause(multitasking);
		this.geotrigger.onPause();
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String offerData = null;
		if(intent != null && (offerData = intent.getStringExtra("com.vresorts.cordova.bgloc.NOTIFICATION_OFFER_DATA")) != null){
			this.performNotificationClicked(offerData);
		}
	}
	
	private Map<String, String> callbackIds = new HashMap<String, String>();
	
	private void performNotificationClicked(String offerData){
	    // These lines can be reused anywhere in your app to send data to the javascript
	    PluginResult result = new PluginResult(PluginResult.Status.OK, offerData);
	    result.setKeepCallback(true);//This is the important part that allows executing the callback more than once, change to false if you want the callbacks to stop firing  
	    this.webView.sendPluginResult(result, callbackIds.get(ACTION_SETONNOTIFICATIONCLICKEDCALLBACK)); 
	}

	public boolean execute(String action, JSONArray data, final CallbackContext callbackContext) {
        Boolean result = false;

        if (ACTION_START.equalsIgnoreCase(action)) {
        	if(geotrigger.isEnabled()){
                callbackContext.error("Service has already been started");
        	}
        	else if (!geotrigger.isConfigured()) {
                callbackContext.error("Call configure before calling start");
            } else {
            
                this.geotrigger.start();
                result = true;
                callbackContext.success("start succeed");
            }
        } else if (ACTION_STOP.equalsIgnoreCase(action)) {
        	if(!geotrigger.isEnabled()){
            	callbackContext.error("The service hasn't started yet");
        	}
        	else{
            this.geotrigger.stop();
            result = true;
            callbackContext.success("stop succeed");
        	}
        } else if (ACTION_CONFIGURE.equalsIgnoreCase(action)){
        	if(geotrigger.isEnabled()){
            	Log.v(Config.TAG, "has enabled, then disable all the previous geofences");
            	geotrigger.stop();
            }
           
            TripPlan tripPlanToConfigure = null;
            try {
        		TripPlanParser parser = new TripPlanParser(data.toString());
        		tripPlanToConfigure = parser.getTripplan();
        		
			} catch (Exception e) {
				e.printStackTrace();
				callbackContext.error("Errors occur when parsing tripplan data");
			}
            
            if(tripPlanToConfigure != null){
            this.geotrigger.configure(tripPlanToConfigure);
            result = true;
            callbackContext.success("configure succeed");
            }
        } else if (ACTION_RECONFIGURE.equalsIgnoreCase(action)){
           
            TripPlan tripPlanToConfigure = null;
            try {
        		TripPlanParser parser = new TripPlanParser(data.toString());
        		tripPlanToConfigure = parser.getTripplan();
        		
			} catch (Exception e) {
				e.printStackTrace();
				callbackContext.error("Errors occur when parsing tripplan data");
			}
            
            if(tripPlanToConfigure != null){
            this.geotrigger.reconfigure(tripPlanToConfigure);
            result = true;
            callbackContext.success("configure succeed");
            }
        }
        else if( ACTION_ADDPLACE.equalsIgnoreCase(action)){
        	if(!this.geotrigger.isConfigured()){
        		callbackContext.error("The service is not configured yet");
        	} else {
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
        			boolean isAdded = this.geotrigger.addPlace(place);
        			if(isAdded){
        				result = true;
        				callbackContext.success("adding place succeed");
        			}
        			else{
        				callbackContext.error("failed to add place");
        			}
        			 
        		 }
        	}
        	
        }
        else if(ACTION_DELETEPLACE.equalsIgnoreCase(action)){
        	if(!this.geotrigger.isConfigured()){
    		callbackContext.error("The service is not configured yet");
    	} else {
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
    			boolean isDeleted = this.geotrigger.deletePlace(placeUuid);
    			if(isDeleted){
    				result = true;
    				callbackContext.success("deleting place succeed");
    			}
    			else{
    				callbackContext.error("failed to delete place");
    			}
    			 
    		 }
    	}
        }
        else if(ACTION_ENABLEPLACE.equalsIgnoreCase(action)){
        	if(!this.geotrigger.isConfigured()){
        		callbackContext.error("The service is not configured yet");
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
        			 boolean isEnabled = this.geotrigger.enablePlace(placeUuid);
        			 if(isEnabled){
        			 result = true;
   		           callbackContext.success("enable place succeed");
        			 }
        			 else{
        				 callbackContext.error("failed to enable place");
        			 }
        			
        		 }
        	}
        }
        else if(ACTION_DISABLEPLACE.equalsIgnoreCase(action)){
        	if(!this.geotrigger.isConfigured()){
        		callbackContext.error("The service is not configured yet");
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
        			 boolean isEnabled = this.geotrigger.disablePlace(placeUuid);
        			 if(isEnabled){
        			 result = true;
   		           callbackContext.success("disable place succeed");
        			 }
        			 else{
        				 callbackContext.error("failed to disable place");
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
        else if(ACTION_SETONNOTIFICATIONCLICKEDCALLBACK.equalsIgnoreCase(action)){
        	String callbackId = callbackContext.getCallbackId(); 
        	this.callbackIds.put(ACTION_SETONNOTIFICATIONCLICKEDCALLBACK, callbackId);
            Log.d(Config.TAG, "notificaton callback is set");
            return true; 
        }
        else if(ACTION_MOCK_START.equalsIgnoreCase(action)){
        	if(this.geoFaker != null && this.geoFaker.isStarted()){
        		callbackContext.error("geofaker has started");
        	}
        	else{
        	this.geoFaker = new Geofaker(this.cordova.getActivity());
        	this.geoFaker.start();
        	result = true;
        	callbackContext.success("mocking started");
        	}
        }
        else if(ACTION_MOCK_STOP.equalsIgnoreCase(action)){
        	if(this.geoFaker != null && this.geoFaker.isStarted()){
        		this.geoFaker.stop();
        		result = true;
        		callbackContext.success("mocking stopped");
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
        	callbackContext.success("mocked position: "+data.toString());
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
