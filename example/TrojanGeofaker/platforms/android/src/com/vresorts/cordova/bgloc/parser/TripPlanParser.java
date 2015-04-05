package com.vresorts.cordova.bgloc.parser;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.vresorts.cordova.bgloc.beans.Geofence;
import com.vresorts.cordova.bgloc.beans.Place;
import com.vresorts.cordova.bgloc.beans.TripPlan;

public class TripPlanParser extends JasonParser {

	public TripPlanParser(JSONArray array) throws Exception {
		super(array);
	}

	public TripPlanParser(String data) throws Exception {
		super(data);
	}

	public TripPlanParser(JSONObject object) throws Exception {
		super(object);
	}

	public TripPlan getTripplan() {
		TripPlan plan = null;
		for (Entity entity : this.getEntities()) {
			plan = new TripPlan();
			plan.setUuid(entity.getStringProperty("uuid"));
			plan.setTripPlanName(entity.getStringProperty("trip_plan_name"));
			plan.setUserUuid(entity.getStringProperty("user_uuid"));
			List<Entity> placeEntities = entity.getChildren("places");
			Place place = null;
			for (Entity placeEntity : placeEntities) {
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
