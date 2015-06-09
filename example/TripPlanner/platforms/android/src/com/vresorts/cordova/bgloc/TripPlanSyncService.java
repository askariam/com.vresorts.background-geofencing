package com.vresorts.cordova.bgloc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class TripPlanSyncService extends IntentService{
	
	static final String PLACE_ID_TO_UNSUBSRIBE = "place_id_to_unsubsribe";

	public TripPlanSyncService() {
		super("TripPlanSyncService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if(intent != null){
		String placeId = intent.getStringExtra(PLACE_ID_TO_UNSUBSRIBE);
		if(placeId != null){
			
			try {
				JSONObject params = new JSONObject();
				params.put("place_uuid", placeId);
			    
				HttpClient httpclient = new DefaultHttpClient();
				HttpPut httpPut = new HttpPut("http://xixixhalu-test.apigee.net/proxy/tripPlanner/disablePlaceSubscription");
				
				StringBuilder buffer = new StringBuilder();
				 String data = params.toString();
				 StringEntity se = new StringEntity(data);  
				   se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				   httpPut.setHeader("Accept", "application/json");
				   httpPut.setHeader("Content-type", "application/json");
				   httpPut.setEntity(se);
				HttpResponse response = httpclient.execute(httpPut);
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					
				    BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
				    try {
				    	String line = null;
				        while ((line = br.readLine()) != null){
				        	buffer.append(line);
				        }
				    } finally {
				        br.close();
				    }
				    
				  //buffer should be able to converted o json object for place. 
					//another strategy, maybe more stable strategry, is to send out notification from here, but it depends on real-life requirement.
					Log.v(Config.TAG, buffer.toString());
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		}
	}



}
