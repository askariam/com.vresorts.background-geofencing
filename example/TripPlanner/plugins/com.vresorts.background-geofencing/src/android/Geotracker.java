package com.vresorts.cordova.bgloc;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.vresorts.cordova.bgloc.beans.Geofence;
import com.vresorts.cordova.bgloc.beans.Place;
import com.vresorts.cordova.bgloc.beans.TripPlan;
import com.vresorts.cordova.bgloc.parser.Entity;
import com.vresorts.cordova.bgloc.parser.JasonParser;
import com.vresorts.cordova.bgloc.parser.PlaceParser;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import static android.telephony.PhoneStateListener.*;
import android.telephony.CellLocation;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.location.Location;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import static java.lang.Math.*;

@SuppressLint("NewApi")
public class Geotracker extends Service implements LocationListener {
    private static final String TAG = "Geotracker";
    private static final String STATIONARY_REGION_ACTION        = "com.kqi.cordova.bgloc.STATIONARY_REGION_ACTION";
    private static final String STATIONARY_ALARM_ACTION         = "com.kqi.cordova.bgloc.STATIONARY_ALARM_ACTION";
    private static final String SINGLE_LOCATION_UPDATE_ACTION   = "com.kqi.cordova.bgloc.SINGLE_LOCATION_UPDATE_ACTION";
    private static final String STATIONARY_LOCATION_MONITOR_ACTION = "com.kqi.cordova.bgloc.STATIONARY_LOCATION_MONITOR_ACTION";
    private static final long STATIONARY_TIMEOUT                                = 5 * 1000 * 60;    // 5 minutes.
    private static final long STATIONARY_LOCATION_POLLING_INTERVAL_LAZY         = 3 * 1000 * 60;    // 3 minutes.
    private static final long STATIONARY_LOCATION_POLLING_INTERVAL_AGGRESSIVE   = 1 * 1000 * 60;    // 1 minute.
    private static final Integer MAX_STATIONARY_ACQUISITION_ATTEMPTS = 5;
    private static final Integer MAX_SPEED_ACQUISITION_ATTEMPTS = 3;

    private PowerManager.WakeLock wakeLock;
    private Location lastLocation;
    
    private float stationaryRadius;
    private Location stationaryLocation;
    private PendingIntent stationaryAlarmPI;
    private PendingIntent stationaryLocationPollingPI;
    private long stationaryLocationPollingInterval;
    private PendingIntent stationaryRegionPI;
    private PendingIntent singleUpdatePI;

    private Boolean isMoving = false;
    private Boolean isAcquiringStationaryLocation = false;
    private Boolean isAcquiringSpeed = false;
    private Integer locationAcquisitionAttempts = 0;

    private Integer desiredAccuracy = 100;
    private Integer distanceFilter = 30;
    private Integer scaledDistanceFilter;
    private Integer locationTimeout = 30;
    private Boolean isDebugging;
    private Integer neighborhoodRadius = 1000;
    
    private ToneGenerator toneGenerator;

    private Criteria criteria;

    private LocationManager locationManager;
    private AlarmManager alarmManager;
    public static TelephonyManager telephonyManager = null;
    
    private boolean isInNeighborhood = false;
    
    private RegisteredTripPlan tripplan;
    
    private static final class RegisteredTripPlanParser extends JasonParser{

    	public RegisteredTripPlanParser(String data) throws Exception {
    		super(data);
    	}

    	public RegisteredTripPlan getRegisteredTripplan() {
    		RegisteredTripPlan plan = null;
    		for (Entity entity : this.getEntities()) {
    			plan = new RegisteredTripPlan();
    			plan.setUuid(entity.getStringProperty("uuid"));
    			plan.setTripPlanName(entity.getStringProperty("trip_plan_name"));
    			plan.setUserUuid(entity.getStringProperty("user_uuid"));
    			List<Entity> placeEntities = entity.getChildren("places");
    			RegisteredPlace place = null;
    			for (Entity placeEntity : placeEntities) {
    				place = new RegisteredPlace();
    				place.setUuid(placeEntity.getStringProperty("uuid"));
    				place.setAddress(placeEntity.getStringProperty("address"));
    				place.setPlaceName(placeEntity.getStringProperty("place_name"));
    				place.setInfoUrl(placeEntity.getStringProperty("infor_url"));
    				place.setOfferUuid(placeEntity.getStringProperty("offer_uuid"));
    				place.setSubscribed(placeEntity
    						.getBoolProperty("is_subscribed"));
    				place.setShortDesc(placeEntity.getStringProperty("short_desc"));
    				place.setTripPlanUuid(placeEntity
    						.getStringProperty("trip_plan_uuid"));
    				place.setUserUuid(placeEntity.getStringProperty("user_uuid"));

    				plan.addPlace(place);

    				Entity geoEntity = placeEntity.getChild("geofence");

    				if (geoEntity != null) {
    					Geofence geofence = new Geofence();
    					geofence.setLatitude(geoEntity.getFloatProperty("latitude"));
    					geofence.setLongitude(geoEntity
    							.getFloatProperty("longitude"));
    					geofence.setRadius(geoEntity.getFloatProperty("radius"));
    					geofence.setUuid(geoEntity.getStringProperty("uuid"));
    					geofence.setOfferUuid(geoEntity
    							.getStringProperty("offer_uuid"));
    					place.setGeofence(geofence);

    				}

    			}
    			break;
    		}
    		return plan;
    		

    	}
    	
    }
    
    private static final class RegisteredTripPlan extends TripPlan{

        private Hashtable <String, RegisteredPlace> registeredPlaces = new Hashtable<String, RegisteredPlace>();

        public Collection<RegisteredPlace> getRegisteredPlaces(){
        	return registeredPlaces.values();
        }
        
        public RegisteredPlace removePlace(RegisteredPlace place){
        	return this.registeredPlaces.remove(place.getUuid());
        }
        
        public void addPlace(RegisteredPlace place){
        	this.registeredPlaces.put(place.getUuid(), place);
        	
        }
    }
    
    private static final class RegisteredPlace extends Place{
    	private boolean wasLocatedIn = false;
        
    	public boolean wasLocatedIn() {
			return wasLocatedIn;
		}

		private void setLocated(boolean isLocated){
    		wasLocatedIn = isLocated;
    	}
		
		private double distanceTo(Location location){
			return this.calculateDistanceFromLatLonInKm(location.getLatitude(), location.getLongitude(), geofence.getLatitude(), geofence.getLongitude());
		}
		
		private double getRadious(){
			return geofence.getRadius();
		}
    	
    	
    	private double calculateDistanceFromLatLonInKm(double lat1,double lon1,double lat2,double lon2) {
    		  double R = 6371; // Radius of the earth in km
    		  double dLat = deg2rad(lat2-lat1);  // deg2rad below
    		  double dLon = deg2rad(lon2-lon1); 
    		  double a = 
    		    Math.sin(dLat/2) * Math.sin(dLat/2) +
    		    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * 
    		    Math.sin(dLon/2) * Math.sin(dLon/2)
    		    ; 
    		  double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
    		  double d = R * c * 1000; // Distance in km
    		  return d;
    		}

    		private double deg2rad(double deg) {
    		  return deg * (Math.PI/180);
    		}
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        Log.i(TAG, "OnBind" + intent);
        return null;
    }
    
    

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "OnCreate");

        locationManager         = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        alarmManager            = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        toneGenerator           = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        telephonyManager        = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        // Stop-detection PI
        stationaryAlarmPI   = PendingIntent.getBroadcast(this, 0, new Intent(STATIONARY_ALARM_ACTION), 0);
        registerReceiver(stationaryAlarmReceiver, new IntentFilter(STATIONARY_ALARM_ACTION));

        // Stationary region PI
        stationaryRegionPI  = PendingIntent.getBroadcast(this, 0, new Intent(STATIONARY_REGION_ACTION), PendingIntent.FLAG_CANCEL_CURRENT);
        registerReceiver(stationaryRegionReceiver, new IntentFilter(STATIONARY_REGION_ACTION));

        // Stationary location monitor PI
        stationaryLocationPollingPI = PendingIntent.getBroadcast(this, 0, new Intent(STATIONARY_LOCATION_MONITOR_ACTION), 0);
        registerReceiver(stationaryLocationMonitorReceiver, new IntentFilter(STATIONARY_LOCATION_MONITOR_ACTION));

        // One-shot PI (TODO currently unused)
        singleUpdatePI = PendingIntent.getBroadcast(this, 0, new Intent(SINGLE_LOCATION_UPDATE_ACTION), PendingIntent.FLAG_CANCEL_CURRENT);
        registerReceiver(singleUpdateReceiver, new IntentFilter(SINGLE_LOCATION_UPDATE_ACTION));

        ////
        // DISABLED
        // Listen to Cell-tower switches (NOTE does not operate while suspended)
        telephonyManager.listen(phoneStateListener, LISTEN_CELL_LOCATION);
        //

        PowerManager pm         = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

        wakeLock.acquire();

        // Location criteria
        criteria = new Criteria();
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);
    }
    

    public static final String GEOTRACKER_FIELD_COMMAND = "field_command";
    public static final String GEOTRACKER_FIELD_DATA_TRIPPLAN = "field_tripplan";
    public static final String GEOTRACKER_COMMOND_CONFIGURE_TRIPPLAN = "command_configure_tripplan";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        if (intent != null) {
        		String command = intent.getStringExtra(GEOTRACKER_FIELD_COMMAND);
        		if(command != null && command.equals(GEOTRACKER_COMMOND_CONFIGURE_TRIPPLAN)){
        			String tripplanData = intent.getStringExtra(GEOTRACKER_FIELD_DATA_TRIPPLAN);
        			if(tripplanData != null){
        				RegisteredTripPlan tripPlanToConfigure = null;
        				try {
							RegisteredTripPlanParser parser = new RegisteredTripPlanParser(tripplanData);
							tripPlanToConfigure = parser.getRegisteredTripplan();
						} catch (Exception e) {
							e.printStackTrace();
						}
        				
        				if(tripPlanToConfigure != null){
        					this.tripplan = tripPlanToConfigure;
        				}
        			}
    				
        		}
        }
        
        this.setPace(false);

        //We want this service to continue running until it is explicitly stopped
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public boolean stopService(Intent intent) {
        Log.i(TAG, "- Received stop: " + intent);
        cleanUp();
        if (isDebugging) {
            Toast.makeText(this, "Background location tracking stopped", Toast.LENGTH_SHORT).show();
        }
        return super.stopService(intent);
    }

    /**
     *
     * @param value set true to engage "aggressive", battery-consuming tracking, false for stationary-region tracking
     */
    private void setPace(Boolean value) {
        Log.i(TAG, "setPace: " + value);

        Boolean wasMoving   = isMoving;
        isMoving            = value;
        isAcquiringStationaryLocation = false;
        isAcquiringSpeed    = false;
        stationaryLocation  = null;

        locationManager.removeUpdates(this);

        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setHorizontalAccuracy(translateDesiredAccuracy(desiredAccuracy));
        criteria.setPowerRequirement(Criteria.POWER_HIGH);

        if (isMoving) {
            // setPace can be called while moving, after distanceFilter has been recalculated.  We don't want to re-acquire velocity in this case.
            if (!wasMoving) {
                isAcquiringSpeed = true;
            }
        } else {
            isAcquiringStationaryLocation = true;
        }

        // Temporarily turn on super-aggressive geolocation on all providers when acquiring velocity or stationary location.
        if (isAcquiringSpeed || isAcquiringStationaryLocation) {
            locationAcquisitionAttempts = 0;
            // Turn on each provider aggressively for a short period of time
            List<String> matchingProviders = locationManager.getAllProviders();
            for (String provider: matchingProviders) {
                if (provider != LocationManager.PASSIVE_PROVIDER) {
                    locationManager.requestLocationUpdates(provider, 0, 0, this);
                }
            }
        } else {
            locationManager.requestLocationUpdates(locationManager.getBestProvider(criteria, true), locationTimeout*1000, scaledDistanceFilter, this);
        }
    }

    /**
    * Translates a number representing desired accuracy of GeoLocation system from set [0, 10, 100, 1000].
    * 0:  most aggressive, most accurate, worst battery drain
    * 1000:  least aggressive, least accurate, best for battery.
    */
    private Integer translateDesiredAccuracy(Integer accuracy) {
        switch (accuracy) {
            case 1000:
                accuracy = Criteria.ACCURACY_LOW;
                break;
            case 100:
                accuracy = Criteria.ACCURACY_MEDIUM;
                break;
            case 10:
                accuracy = Criteria.ACCURACY_HIGH;
                break;
            case 0:
                accuracy = Criteria.ACCURACY_HIGH;
                break;
            default:
                accuracy = Criteria.ACCURACY_MEDIUM;
        }
        return accuracy;
    }

    /**
     * Returns the most accurate and timely previously detected location.
     * Where the last result is beyond the specified maximum distance or
     * latency a one-off location update is returned via the {@link LocationListener}
     * specified in {@link setChangedLocationListener}.
     * @param minDistance Minimum distance before we require a location update.
     * @param minTime Minimum time required between location updates.
     * @return The most accurate and / or timely previously detected location.
     */
    public Location getLastBestLocation() {
        int minDistance = (int) stationaryRadius;
        long minTime    = System.currentTimeMillis() - (locationTimeout * 1000);

        Log.i(TAG, "- fetching last best location " + minDistance + "," + minTime);
        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;

        // Iterate through all the providers on the system, keeping
        // note of the most accurate result within the acceptable time limit.
        // If no result is found within maxTime, return the newest Location.
        List<String> matchingProviders = locationManager.getAllProviders();
        for (String provider: matchingProviders) {
            Log.d(TAG, "- provider: " + provider);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                Log.d(TAG, " location: " + location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy() + "," + location.getSpeed() + "m/s");
                float accuracy = location.getAccuracy();
                long time = location.getTime();
                Log.d(TAG, "time>minTime: " + (time > minTime) + ", accuracy<bestAccuracy: " + (accuracy < bestAccuracy));
                if ((time > minTime && accuracy < bestAccuracy)) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                }
            }
        }
        return bestResult;
    }

    public void onLocationChanged(Location location) {
        Log.d(TAG, "- onLocationChanged: " + location.getLatitude() + "," + location.getLongitude() + ", accuracy: " + location.getAccuracy() + ", isMoving: " + isMoving + ", speed: " + location.getSpeed());

        if (!isMoving && !isAcquiringStationaryLocation && stationaryLocation==null) {
            // Perhaps our GPS signal was interupted, re-acquire a stationaryLocation now.
            setPace(false);
        }

        if (isDebugging) {
            Toast.makeText(this, "mv:"+isMoving+",acy:"+location.getAccuracy()+",v:"+location.getSpeed()+",df:"+scaledDistanceFilter, Toast.LENGTH_LONG).show();
        }
        if (isAcquiringStationaryLocation) {
            if (stationaryLocation == null || stationaryLocation.getAccuracy() > location.getAccuracy()) {
                stationaryLocation = location;
            }
            if (++locationAcquisitionAttempts == MAX_STATIONARY_ACQUISITION_ATTEMPTS) {
                isAcquiringStationaryLocation = false;
                startMonitoringStationaryRegion(stationaryLocation);
                if (isDebugging) {
                    startTone("long_beep");
                }
            } else {
                // Unacceptable stationary-location: bail-out and wait for another.
                if (isDebugging) {
                    startTone("beep");
                }
                return;
            }
        } else if (isAcquiringSpeed) {
            if (++locationAcquisitionAttempts == MAX_SPEED_ACQUISITION_ATTEMPTS) {
                // Got enough samples, assume we're confident in reported speed now.  Play "woohoo" sound.
                if (isDebugging) {
                    startTone("doodly_doo");
                }
                isAcquiringSpeed = false;
                scaledDistanceFilter = calculateDistanceFilter(location.getSpeed());
                setPace(true);
            } else {
                if (isDebugging) {
                    startTone("beep");
                }
                return;
            }
        } else if (isMoving) {
            if (isDebugging) {
                startTone("beep");
            }
            // Only reset stationaryAlarm when accurate speed is detected, prevents spurious locations from resetting when stopped.
            if ( (location.getSpeed() >= 1) && (location.getAccuracy() <= stationaryRadius) && !isInNeighborhood ) {
                resetStationaryAlarm();
            }
            // Calculate latest distanceFilter, if it changed by 5 m/s, we'll reconfigure our pace.
            Integer newDistanceFilter = calculateDistanceFilter(location.getSpeed());
            if (newDistanceFilter != scaledDistanceFilter.intValue()) {
                Log.i(TAG, "- updated distanceFilter, new: " + newDistanceFilter + ", old: " + scaledDistanceFilter);
                scaledDistanceFilter = newDistanceFilter;
                setPace(true);
            }
            if (location.distanceTo(lastLocation) < distanceFilter) {
                return;
            }
            
            onMovingUpdate(location);
            
        } else if (stationaryLocation != null) {
            return;
        }
        // Go ahead and cache, push to server
        lastLocation = location;
        
    }
    
    private void onMovingUpdate(Location location){
    	if(this.tripplan == null){
    		return;
    	}
    	isInNeighborhood = false;
    	Collection<RegisteredPlace> placeList = tripplan.getRegisteredPlaces();
    	for(RegisteredPlace place : placeList){
    		if(place.isSubscribed()){
    		double distance = place.distanceTo(location);
    		if(distance < place.getRadious() + location.getAccuracy()){
    			if(!place.wasLocatedIn()){
    				onEnter(place);
    			}
				place.setLocated(true);
    			isInNeighborhood = true;
    		}
    		else{
    			if(distance < this.neighborhoodRadius + location.getAccuracy()){
    			isInNeighborhood = true;
    			}
    			if(place.wasLocatedIn){
    				onExit(place);
    			}
    			place.setLocated(false);
    		}
    		}
    	}
    }
    
    private Intent getIntentByPlace(Place place){
    	Intent intent = new Intent(Geotrigger.GEOTRIGGER_ACTION);
		intent.addCategory(Geotrigger.GEOTRIGGER_CATEGORY);
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(Geotrigger.GEOTRIGGER_DATA_SCHEME);
		builder.authority(Geotrigger.GEOTRIGGER_DATA_HOST);
		builder.appendQueryParameter("placeUuid", place.getUuid());
		intent.setData(builder.build());
		
		intent.putExtra(Geotrigger.INTENT_EXTRA_FIELD_PLACE, place.toJSONObject().toString());
		
		return intent;
    }
    
    private void onEnter(Place place){
    	Intent intent = this.getIntentByPlace(place);
		intent.putExtra(LocationManager.KEY_PROXIMITY_ENTERING, true);
		this.sendBroadcast(intent);
		
    }
    
    private void onExit(Place place){
    	Intent intent = this.getIntentByPlace(place);
		intent.putExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
		this.sendBroadcast(intent);
    }

    /**
     * Plays debug sound
     * @param name
     */
    private void startTone(String name) {
        int tone = 0;
        int duration = 1000;

        if (name.equals("beep")) {
            tone = ToneGenerator.TONE_PROP_BEEP;
        } else if (name.equals("beep_beep_beep")) {
            tone = ToneGenerator.TONE_CDMA_CONFIRM;
        } else if (name.equals("long_beep")) {
            tone = ToneGenerator.TONE_CDMA_ABBR_ALERT;
        } else if (name.equals("doodly_doo")) {
            tone = ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE;
        } else if (name.equals("chirp_chirp_chirp")) {
            tone = ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD;
        } else if (name.equals("dialtone")) {
            tone = ToneGenerator.TONE_SUP_RINGTONE;
        }
        toneGenerator.startTone(tone, duration);
    }

    public void resetStationaryAlarm() {
        alarmManager.cancel(stationaryAlarmPI);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + STATIONARY_TIMEOUT, stationaryAlarmPI); // Millisec * Second * Minute
    }

    private Integer calculateDistanceFilter(Float speed) {
        Double newDistanceFilter = (double) distanceFilter;
        if (speed < 100) {
            float roundedDistanceFilter = (round(speed / 5) * 5);
            newDistanceFilter = pow(roundedDistanceFilter, 2) + (double) distanceFilter;
        }
        return (newDistanceFilter.intValue() < 1000) ? newDistanceFilter.intValue() : 1000;
    }

    private void startMonitoringStationaryRegion(Location location) {
        locationManager.removeUpdates(this);
        stationaryLocation = location;

        Log.i(TAG, "- startMonitoringStationaryRegion (" + location.getLatitude() + "," + location.getLongitude() + "), accuracy:" + location.getAccuracy());

        // Here be the execution of the stationary region monitor
        locationManager.addProximityAlert(
                location.getLatitude(),
                location.getLongitude(),
                (location.getAccuracy() < stationaryRadius) ? stationaryRadius : location.getAccuracy(),
                (long)-1,
                stationaryRegionPI
        );

        startPollingStationaryLocation(STATIONARY_LOCATION_POLLING_INTERVAL_LAZY);
    }

    public void startPollingStationaryLocation(long interval) {
        // proximity-alerts don't seem to work while suspended in latest Android 4.42 (works in 4.03).  Have to use AlarmManager to sample
        //  location at regular intervals with a one-shot.
        stationaryLocationPollingInterval = interval;
        alarmManager.cancel(stationaryLocationPollingPI);
        long start = System.currentTimeMillis() + (60 * 1000);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, start, interval, stationaryLocationPollingPI);
    }

    public void onPollStationaryLocation(Location location) {
        if (isMoving) {
            return;
        }
        if (isDebugging) {
            startTone("beep");
        }
    float distance = abs(location.distanceTo(stationaryLocation) - stationaryLocation.getAccuracy() - location.getAccuracy());

        if (isDebugging) {
            Toast.makeText(this, "Stationary exit in " + (stationaryRadius-distance) + "m", Toast.LENGTH_LONG).show();
        }

        // TODO http://www.cse.buffalo.edu/~demirbas/publications/proximity.pdf
        // determine if we're almost out of stationary-distance and increase monitoring-rate.
        Log.i(TAG, "- distance from stationary location: " + distance);
        if (distance > stationaryRadius) {
            onExitStationaryRegion(location);
        } else if (distance > 0) {
            startPollingStationaryLocation(STATIONARY_LOCATION_POLLING_INTERVAL_AGGRESSIVE);
        } else if (stationaryLocationPollingInterval != STATIONARY_LOCATION_POLLING_INTERVAL_LAZY) {
            startPollingStationaryLocation(STATIONARY_LOCATION_POLLING_INTERVAL_LAZY);
        }
    }
    /**
    * User has exit his stationary region!  Initiate aggressive geolocation!
    */
    public void onExitStationaryRegion(Location location) {
        // Filter-out spurious region-exits:  must have at least a little speed to move out of stationary-region
        if (isDebugging) {
            startTone("beep_beep_beep");
        }
        // Cancel the periodic stationary location monitor alarm.
        alarmManager.cancel(stationaryLocationPollingPI);

        // Kill the current region-monitor we just walked out of.
        locationManager.removeProximityAlert(stationaryRegionPI);

        // Engage aggressive tracking.
        this.setPace(true);
    }

    /**
    * TODO Experimental cell-tower change system; something like ios significant changes.
    */
    public void onCellLocationChange(CellLocation cellLocation) {
        Log.i(TAG, "- onCellLocationChange" + cellLocation.toString());
        if (isDebugging) {
            Toast.makeText(this, "Cellular location change", Toast.LENGTH_LONG).show();
            startTone("chirp_chirp_chirp");
        }
        if (!isMoving && stationaryLocation != null) {
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            locationManager.requestSingleUpdate(criteria, singleUpdatePI);
        }
    }

    /**
    * Broadcast receiver for receiving a single-update from LocationManager.
    */
    private BroadcastReceiver singleUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = LocationManager.KEY_LOCATION_CHANGED;
            Location location = (Location)intent.getExtras().get(key);
            if (location != null) {
                Log.d(TAG, "- singleUpdateReciever" + location.toString());
                onPollStationaryLocation(location);
            }
        }
    };

    /**
    * Broadcast receiver which detcts a user has stopped for a long enough time to be determined as STOPPED
    */
    private BroadcastReceiver stationaryAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.i(TAG, "- stationaryAlarm fired");
            setPace(false);
        }
    };
    /**
     * Broadcast receiver to handle stationaryMonitor alarm, fired at low frequency while monitoring stationary-region.
     * This is required because latest Android proximity-alerts don't seem to operate while suspended.  Regularly polling
     * the location seems to trigger the proximity-alerts while suspended.
     */
     private BroadcastReceiver stationaryLocationMonitorReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent)
         {
             Log.i(TAG, "- stationaryLocationMonitorReceiver fired");
             if (isDebugging) {
                 startTone("dialtone");
             }
             criteria.setAccuracy(Criteria.ACCURACY_FINE);
             criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
             criteria.setPowerRequirement(Criteria.POWER_HIGH);
             locationManager.requestSingleUpdate(criteria, singleUpdatePI);
         }
     };
    /**
    * Broadcast receiver which detects a user has exit his circular stationary-region determined by the greater of stationaryLocation.getAccuracy() OR stationaryRadius
    */
    private BroadcastReceiver stationaryRegionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "stationaryRegionReceiver");
            String key = LocationManager.KEY_PROXIMITY_ENTERING;

            Boolean entering = intent.getBooleanExtra(key, false);
            if (entering) {
                Log.d(TAG, "- ENTER");
                if (isMoving) {
                    setPace(false);
                }
            }
            else {
                Log.d(TAG, "- EXIT");
                // There MUST be a valid, recent location if this event-handler was called.
                Location location = getLastBestLocation();
                if (location != null) {
                    onExitStationaryRegion(location);
                }
            }
        }
    };
    /**
    * TODO Experimental, hoping to implement some sort of "significant changes" system here like ios based upon cell-tower changes.
    */
    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCellLocationChanged(CellLocation location)
        {
            onCellLocationChange(location);
        }
    };

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
        Log.d(TAG, "- onProviderDisabled: " + provider);
    }
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
        Log.d(TAG, "- onProviderEnabled: " + provider);
    }
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
        Log.d(TAG, "- onStatusChanged: " + provider + ", status: " + status);
    }
    
    @Override
    public void onDestroy() {
        Log.w(TAG, "------------------------------------------ Destroyed Location update Service");
        cleanUp();
        super.onDestroy();
    }
    private void cleanUp() {
    	this.tripplan = null;
        locationManager.removeUpdates(this);
        alarmManager.cancel(stationaryAlarmPI);
        alarmManager.cancel(stationaryLocationPollingPI);
        toneGenerator.release();

        unregisterReceiver(stationaryAlarmReceiver);
        unregisterReceiver(singleUpdateReceiver);
        unregisterReceiver(stationaryRegionReceiver);
        unregisterReceiver(stationaryLocationMonitorReceiver);

        if (stationaryLocation != null && !isMoving) {
            try {
                locationManager.removeProximityAlert(stationaryRegionPI);
            } catch (Throwable e) {
                Log.w(TAG, "- Something bad happened while removing proximity-alert");
            }
        }
        stopForeground(true);
        wakeLock.release();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        this.stopSelf();
        super.onTaskRemoved(rootIntent);
    }
}
