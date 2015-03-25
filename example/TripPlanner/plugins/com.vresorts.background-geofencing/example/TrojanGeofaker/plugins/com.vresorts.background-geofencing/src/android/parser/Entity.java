package com.vresorts.cordova.bgloc.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Entity {
	private JSONObject object;

	public JSONObject getJSONObject() {
		return object;
	}

	public void setJSONObject(JSONObject object) {
		this.object = object;
	}
	
	public Entity(JSONObject object){
		this.object = object;
	}
	
	public String getStringProperty(String propertyName){
		String propertyValue = null;
		
		propertyValue = object.optString(propertyName);
	
		
		return propertyValue;
		
	}
	
	public Boolean getBoolProperty(String propertyName){
		Boolean propertyValue = null;
		
		propertyValue = Boolean.parseBoolean(object.optString(propertyName));
		
		return propertyValue;
	}
	
	public float getFloatProperty(String propertyName){
		float propertyValue = Float.NaN;
		
		try{
		propertyValue = Float.parseFloat(object.optString(propertyName));
		}catch(Exception e){
			
		}
		
		return propertyValue;
	}
	
	public Entity getChild(String propertyName){
		Entity propertyValue = null;
		
		JSONObject objectChild = object.optJSONObject(propertyName);
		
		if(objectChild != null) {
		propertyValue = new Entity(objectChild);
		}
		
		return propertyValue;
	}
	
	public List<Entity> getChildren(String propertyName){
		List<Entity> propertyValues = new ArrayList<Entity>();
			 JSONArray array = object.optJSONArray(propertyName);
			 for(int i=0; i<array.length(); i++){
				 	
					JSONObject objectChild = array.optJSONObject(i);
					
					if(objectChild != null) {
					propertyValues.add(new Entity(objectChild));
					}
					
					
			 }
		
		
		return propertyValues;
	}
	
}
