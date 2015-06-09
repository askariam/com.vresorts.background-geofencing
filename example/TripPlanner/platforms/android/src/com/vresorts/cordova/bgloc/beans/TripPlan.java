package com.vresorts.cordova.bgloc.beans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class TripPlan {
	private String uuid;
	private String tripPlanName;
	private String userUuid;
	private List<Place> places = new ArrayList<Place>();
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getTripPlanName() {
		return tripPlanName;
	}
	public void setTripPlanName(String tripPlanName) {
		this.tripPlanName = tripPlanName;
	}
	public String getUserUuid() {
		return userUuid;
	}
	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}
	public List<Place> getPlaces() {
		return places;
	}
	public void setPlaces(List<Place> places) {
		this.places = places;
	}
	
	public void addPlace(Place place){
		this.places.add(place);
	}
	
	public Place getPlaceByUuid(String uuid){
		for(Place place: this.places){
			if(place.getUuid().equals(uuid)) {
				return place;
			}
		}
		return null;
	}
	public Place removePlace(String uuid) {
		Iterator<Place> iterator = this.places.iterator();
		while(iterator.hasNext()){
			Place place = iterator.next();
			if(place.getUuid().equals(uuid)) {
				iterator.remove();
				return place;
			}
		}
		return null;
		
	}
	
	public JSONObject toJSONObject(){
		JSONObject object = new JSONObject();
		try {
			object.put("uuid", this.uuid);
			object.put("trip_plan_name", this.tripPlanName);
			object.put("user_uuid", this.userUuid);
			
			JSONArray placeJSONArray = new JSONArray();
			for(Place place : this.places){
				placeJSONArray.put(place.toJSONObject());
			}
			object.put("places", placeJSONArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object;
	}
	
}
