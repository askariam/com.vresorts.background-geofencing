package com.vresorts.cordova.bgloc.parser;

import org.json.JSONArray;
import org.json.JSONObject;

import com.vresorts.cordova.bgloc.beans.Geofence;
import com.vresorts.cordova.bgloc.beans.Place;

public class PlaceParser extends JasonParser{

	public PlaceParser(JSONArray array) throws Exception {
		super(array);
	}

	public PlaceParser(String data) throws Exception {
		super(data);
	}

	public PlaceParser(JSONObject object) throws Exception {
		super(object);
	}
	
	public Place getPlace(){
		Place place = null;
		for (Entity placeEntity : this.getEntities()) {
			place = new Place();
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

			break;
		}
		
		return place;
	}

}
