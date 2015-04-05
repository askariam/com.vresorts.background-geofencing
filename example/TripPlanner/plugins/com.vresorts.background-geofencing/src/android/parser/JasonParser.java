package com.vresorts.cordova.bgloc.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.vresorts.cordova.bgloc.Config;


public class JasonParser {
	private List<Entity> entityList = new ArrayList<Entity>();
	
	public List<Entity> getEntities(){
		return entityList;
	}
	
	public JasonParser(JSONArray array) throws Exception{
		this(array.toString());
	}
	
	public JasonParser(JSONObject object) throws Exception{
		this(object.toString());
	}
	
	public JasonParser(String data) throws Exception{
		
	    	  Object json = new JSONTokener(data).nextValue();
	    	  if (json instanceof JSONObject){
	    	    //you have an object
					JSONObject object = (JSONObject) json;
					
					String error = object.optString(Config.ADSERVER_ERROR_TOKEN);
					
					if(!error.equals("")) {
						throw new Exception(error);
					} else{
						entityList.add(new Entity(object));
					}
	    	  }
	    	  else if (json instanceof JSONArray){
	    		  JSONArray array = (JSONArray) json;
	    		  for(int i=0; i<array.length(); i++){
	    			  entityList.add(new Entity(array.getJSONObject(i)));
	    		  }
	    	  }
	    	  else {
	    		  throw new Exception(data);
	    	  }
	}
	
}
