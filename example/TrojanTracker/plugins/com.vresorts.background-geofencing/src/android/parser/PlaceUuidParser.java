package com.vresorts.cordova.bgloc.parser;

import org.json.JSONArray;
import org.json.JSONObject;

public class PlaceUuidParser extends JasonParser{
	public PlaceUuidParser(JSONArray array) throws Exception {
		super(array);
	}

	public PlaceUuidParser(String data) throws Exception {
		super(data);
	}

	public PlaceUuidParser(JSONObject object) throws Exception {
		super(object);
	}
	
	public String getPlaceUuid(){
		String placeUuid = null;
		for (Entity entity : this.getEntities()) {
		
			placeUuid = entity.getStringProperty("place_uuid");
			break;
		}
		
		return placeUuid;
	}

}
