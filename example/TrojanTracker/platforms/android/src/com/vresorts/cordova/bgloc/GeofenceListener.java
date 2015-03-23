package com.vresorts.cordova.bgloc;

import com.vresorts.cordova.bgloc.beans.Place;
import com.vresorts.cordova.bgloc.parser.PlaceParser;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

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
					// TODO Auto-generated catch block
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
    	
    	@TargetApi(16)
        private Notification buildForegroundNotification(Notification.Builder builder) {
            return builder.build();
        }

        @SuppressWarnings("deprecation")
        @TargetApi(15)
        private Notification buildForegroundNotificationCompat(Notification.Builder builder) {
            return builder.getNotification();
        }

		@Override
		public void onEnter(Place place, long time) {
//			Activity activity = BackgroundGeofencingPlugin.this.cordova.getActivity();
			NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
			Intent main = new Intent(activity, BackgroundGeofencingPlugin.class);
            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, main,  PendingIntent.FLAG_UPDATE_CURRENT);

            
            Notification.Builder builder = new Notification.Builder(activity);
            builder.setContentTitle("welcome to"+place.getPlaceName());
            builder.setContentText("enter:" + place.getPlaceName());
            builder.setSmallIcon(android.R.drawable.ic_menu_mylocation);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);
            Notification notification;
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                notification = buildForegroundNotification(builder);
            } else {
                notification = buildForegroundNotificationCompat(builder);
            }
            notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_NO_CLEAR;
            mNotificationManager.notify((int) System.currentTimeMillis(), notification);
            
			
			Log.v(Config.TAG, "enter into place :" + place.getPlaceName() + "	at time:" + time);
		}

		@Override
		public void onExit(Place place, long time, long duration) {
//			Activity activity = BackgroundGeofencingPlugin.this.cordova.getActivity();
			NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
			
			Intent main = new Intent(activity, BackgroundGeofencingPlugin.class);
            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, main,  PendingIntent.FLAG_UPDATE_CURRENT);

            
            Notification.Builder builder = new Notification.Builder(activity);
            builder.setContentTitle("welfare:"+place.getPlaceName());
            builder.setContentText("exit:" + place.getPlaceName());
            builder.setSmallIcon(android.R.drawable.ic_menu_mylocation);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);
            Notification notification;
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                notification = buildForegroundNotification(builder);
            } else {
                notification = buildForegroundNotificationCompat(builder);
            }
            notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_NO_CLEAR;
            mNotificationManager.notify((int) System.currentTimeMillis(), notification);
            
			Log.v(Config.TAG, "exit place :" + place.getPlaceName() + "	at time:" + time + "	for:" + duration);
		}
    	
    };
    

}
