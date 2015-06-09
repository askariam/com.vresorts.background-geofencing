package com.vresorts.cordova.bgloc.beans;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.location.Location;
public class Place{
	protected String uuid;
	protected String placeName;
	protected String address;
	protected String userUuid;
	protected String infoUrl;
	protected String tripPlanUuid;
	protected boolean isSubscribed;
	protected String shortDesc;
	protected String offerUuid;
	protected Geofence geofence;
	
	public boolean isLocatedIn(Location location) {
		double distance = this.calculateDistanceFromLatLonInKm(location.getLatitude(), location.getLongitude(), geofence.getLatitude(), geofence.getLongitude());
		if(distance < geofence.getRadius()){
			return true;
		}
		return false;
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
		
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getPlaceName() {
		return placeName;
	}
	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getUserUuid() {
		return userUuid;
	}
	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}
	public String getInfoUrl() {
		return infoUrl;
	}
	public void setInfoUrl(String infoUrl) {
		this.infoUrl = infoUrl;
	}
	public String getTripPlanUuid() {
		return tripPlanUuid;
	}
	public void setTripPlanUuid(String tripPlanUuid) {
		this.tripPlanUuid = tripPlanUuid;
	}
	public boolean isSubscribed() {
		return isSubscribed;
	}
	public void setSubscribed(boolean isSubscribed) {
		this.isSubscribed = isSubscribed;
	}
	public String getShortDesc() {
		return shortDesc;
	}
	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}
	public Geofence getGeofence() {
		return geofence;
	}
	public void setGeofence(Geofence geofence) {
		this.geofence = geofence;
		this.geofence.setGlobalUuid(this.geofence.getUuid()+"-"+this.userUuid);
	}
	
	public String getOfferUuid() {
		return offerUuid;
	}
	public void setOfferUuid(String offerUuid) {
		this.offerUuid = offerUuid;
	}
	
	public JSONObject toJSONObject(){
		JSONObject object = new JSONObject();
		try {
			object.put("uuid", this.uuid);
			object.put("address", this.address);
			object.put("place_name", this.placeName);
			object.put("infor_url", this.infoUrl);
			object.put("offer_uuid", this.offerUuid);
			object.put("is_subscribed", this.isSubscribed());
			object.put("short_desc", this.shortDesc);
			object.put("trip_plan_uuid", this.tripPlanUuid);
			object.put("user_uuid", this.userUuid);
			if(geofence != null){
			object.put("geofence", this.geofence.toJSONObject());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object;
	}
	
	long timeStamp;
	
	public void setTimeStamp(long timeStamp){
		this.timeStamp = timeStamp;
	}
	
	public void timeStamp(){
		this.timeStamp = System.currentTimeMillis();
	}
	
	public long getTimeStamp() {
		return timeStamp;
	}

}
