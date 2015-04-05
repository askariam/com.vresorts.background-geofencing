package com.vresorts.cordova.bgloc;

import com.vresorts.cordova.bgloc.beans.Place;
import com.vresorts.cordova.bgloc.parser.PlaceParser;
import com.vresorts.geotrigger.CordovaApp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("NewApi")
public class GeofenceListener extends BroadcastReceiver{
	
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
	            
	            GeotriggerListenerClass geotriggerListener = new GeotriggerListenerClass(context);

	            if (entering) {
	                Log.d(Config.TAG, "- ENTER");
	                place.timeStamp();
	                geotriggerListener.onEnter(place, place.getTimeStamp());
	            }
	            else if (!entering){
	                Log.d(Config.TAG, "- EXIT");
	                long enterTime = place.getTimeStamp();
	                place.timeStamp();
	                long duration = place.getTimeStamp() - enterTime;
	                geotriggerListener.onExit(place, place.getTimeStamp(), duration);
	            }
	            
		
	}
	
	
    
	public static interface GeotriggerListener{
		public void onEnter(Place place, long time);
		public void onExit(Place place, long time, long duration);
	}
    
    private static class GeotriggerListenerClass implements GeotriggerListener{
    	Context activity;
    	GeotriggerListenerClass(Context context){
    		activity = context;
    	}

		@Override
		public void onEnter(Place place, long time) {
//			Activity activity = BackgroundGeofencingPlugin.this.cordova.getActivity();
			NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
			Intent main = new Intent(activity, CordovaApp.class);
			main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, main,  PendingIntent.FLAG_UPDATE_CURRENT);

            
            Notification.Builder builder = new Notification.Builder(activity);
            builder.setContentTitle("welcome to"+place.getPlaceName());
            builder.setContentText("enter:" + place.getPlaceName());
            builder.setSmallIcon(android.R.drawable.ic_menu_mylocation);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            
            mNotificationManager.notify((int) System.currentTimeMillis(), builder.build());
            
			
			Log.v(Config.TAG, "enter into place :" + place.getPlaceName() + "	at time:" + time);
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
