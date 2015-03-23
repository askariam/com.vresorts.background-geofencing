package com.vresorts.cordova.bgloc.beans;

public class Geofence {
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
	
}
