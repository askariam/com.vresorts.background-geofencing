package com.vresorts.cordova.bgloc.beans;


import org.json.JSONException;
import org.json.JSONObject;


public class Geofence{
	private float latitude;
	private float longitude;
	private float radius;
	private String uuid;
	private String offerUuid;
	private String globalUuid; //unique id for all the triggers
	
	public Geofence(){
		
	}
	
	public Geofence(Geofence geofence){
		if(geofence == null){
			return;
		}
		this.latitude = geofence.latitude;
		this.longitude = geofence.longitude;
		this.radius = geofence.radius;
		this.uuid = geofence.offerUuid;
	}
	
	public String getGlobalUuid() {
		return globalUuid;
	}
	public void setGlobalUuid(String globalUuid) {
		this.globalUuid = globalUuid;
	}
	
	public String getOfferUuid() {
		return offerUuid;
	}
	public void setOfferUuid(String offerUuid) {
		this.offerUuid = offerUuid;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public float getLatitude() {
		return latitude;
	}
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	public float getLongitude() {
		return longitude;
	}
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}
	public float getRadius() {
		return radius;
	}
	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	public JSONObject toJSONObject(){
		JSONObject object = new JSONObject();
		try {
			object.put("latitude", this.latitude);
			object.put("longitude", this.longitude);
			object.put("radius", this.radius);
			object.put("uuid", this.uuid);
			object.put("offer_uuid", this.offerUuid);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object;
	}
	
	
}
