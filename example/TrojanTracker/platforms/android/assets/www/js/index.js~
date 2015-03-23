/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var ENV = (function() {
    
    var localStorage = window.localStorage;

    return {
        settings: {
            /**
            * state-mgmt
            */
            //enabled:    localStorage.getItem('enabled')     || 'true',
            //aggressive: localStorage.getItem('aggressive')  || 'false'
        },
        toggle: function(key) {
            var value       = localStorage.getItem(key)
                newValue    = ((new String(value)) == 'true') ? 'false' : 'true';

            localStorage.setItem(key, newValue);
            return newValue;
        }
    }
})()

var app = {
    /**
    * @property {google.maps.Map} map
    */
    map: undefined,
    /**
    * @property {google.maps.Marker} location The current location
    */
    location: undefined,
    /**
    * @property {google.map.PolyLine} path The list of background geolocations
    */
    path: undefined,
    /**
    * @property {Boolean} aggressiveEnabled
    */
    aggressiveEnabled: false,
    /**
    * @property {Array} locations List of rendered map markers of prev locations
    */
    locations: [],
    /**
    * @private
    */
    btnEnabled: undefined,
    btnPace: undefined,
    btnHome: undefined,
    btnReset: undefined,
    btnMock: undefined,
    
    isMocking: false,
    
    bgGeo: undefined,
    
    geofences: [],

    // Application Constructor  
    initialize: function() {
        this.bindEvents();
        google.maps.event.addDomListener(window, 'load', app.initializeMap);
       
    },
    
    requestTripPlan: function(url, callback){
    	var xmlhttp = new XMLHttpRequest();

    	xmlhttp.onreadystatechange = function() {
    	if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
    		var myArr = JSON.parse(xmlhttp.responseText);
    	    callback(myArr);
    	    }
    	}

    	xmlhttp.open("GET", url, true);
    	xmlhttp.send();
    },
    initializeMap: function() {
        var mapOptions = {
          center: { lat: -34.397, lng: 150.644},
          zoom: 8,
          zoomControl: false
        };

        var header = $('#header'),
            footer = $('#footer'),
            canvas = $('#map-canvas'),
            canvasHeight = window.innerHeight - header[0].clientHeight - footer[0].clientHeight;

        canvas.height(canvasHeight);
        canvas.width(window.clientWidth);

        var map = new google.maps.Map(canvas[0], mapOptions);
        
        google.maps.event.addListener(map, 'click', function(event) {
        	app.onMapClick(map,event);
        });
        
        app.map = map;
    },
    
    onMapClick: function(map, event){
    	var currentLocationDisplay = $('#current-location');
    	if(!app.isMocking){
    		return;
    	}
    	
        var coordinates = {latitude: event.latLng.k, longitude:event.latLng.D};
        
        currentLocationDisplay[0].innerHTML = "current locaton: lat="+coordinates.latitude+", lng="+coordinates.longitude;

    	app.setCurrentLocation(coordinates);
    	
        var bgGeo = window.plugins.backgroundGeofencing;
        bgGeo.mock(function(msg){
        	console.log(msg);
        	//window.confirm("successful mocking coordinates");
        }, function(msg){
        	console.log(msg);
        },  coordinates);
    },
    
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
        document.addEventListener('pause', this.onPause, false);
        document.addEventListener('resume', this.onResume, false);

        // Init UI buttons
        this.btnHome        = $('button#btn-home');
        this.btnReset       = $('button#btn-reset');
        //this.btnPace        = $('button#btn-pace');
        this.btnEnabled     = $('button#btn-enabled');
        //this.btnMock 		= $('button#btn-mock');
        
        
        this.btnHome.on('click', this.onClickHome);
        this.btnReset.on('click', this.onClickReset);
        //this.btnPace.on('click', this.onClickChangePace);
        this.btnEnabled.on('click', this.onClickEnabled);
        //this.btnMock.on('click', this.onClickMock);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        app.receivedEvent('deviceready');
        app.setupGeofences();
    },
    
    setupGeofences: function() {
    	 app.requestTripPlan('http://xixixhalu-test.apigee.net/proxy/tripPlanner/getPlaces?trip_plan_uuid=a137a68a-be1f-11e4-a532-9192b501077c', function(jsonArray){
    		 	var bgGeo = window.plugins.backgroundGeofencing;
    	        bgGeo.configure(function(msg){
    	        	app.setGeofences(jsonArray);
        	        app.onClickHome();
    	        	console.log(msg);}, 
    	        	function(msg){console.log(msg);}, jsonArray);
    	        
    	});
        
    },
    setMapFocus: function(location){
    	var map     = app.map,
        coords  = location.coords,
        ll      = new google.maps.LatLng(coords.latitude, coords.longitude),
        zoom    = map.getZoom();

    	map.setCenter(ll);
    	if (zoom < 8) {
        map.setZoom(8);
    	}
    },
    onClickHome: function() {
    	if(app.geofences != null && app.geofences.length > 0){
    		var geofences = app.geofences;
    		
    		var bounds = new google.maps.LatLngBounds();
    		for(var i in geofences){
        		bounds.extend(geofences[i].getCenter());
        		//end loop
    		}
    		app.map.fitBounds(bounds);
    	}
    	else{
        var fgGeo = window.navigator.geolocation;
        // Your app must execute AT LEAST ONE call for the current position via standard Cordova geolocation,
        //  in order to prompt the user for Location permission.
        fgGeo.getCurrentPosition(function(location) {
            var map     = app.map,
                coords  = location.coords,
                ll      = new google.maps.LatLng(coords.latitude, coords.longitude),
                zoom    = map.getZoom();

            map.setCenter(ll);
            if (zoom < 15) {
                map.setZoom(15);
            }
        });
    	}
    },
    onClickReset: function() {
      // Clear prev location markers.
      var locations = app.locations;
      for (var n=0,len=locations.length;n<len;n++) {
          locations[n].setMap(null);
      }
      app.locations = [];
      
      var geofences = app.geofences;
      for (var n=0,len=geofences.length;n<len;n++) {
          geofences[n].setMap(null);
      }
      app.goefences = [];

      // Clear Polyline.
      if(app.path){
      app.path.setMap(null);
      app.path = undefined;
      }
      
      if(app.location){
          app.location.setMap(null);
          app.location = undefined;
          }
      
//      if(app.btnMock.hasClass("btn-danger")){
//    	 app.onClickMock();
//      }
      
      if(app.btnEnabled.hasClass("btn-danger")){
    	 app.onClickEnabled();
      }
      
      app.setupGeofences();
      
    },
    
    onClickMock: function(value){
    var bgGeo  = window.plugins.backgroundGeofencing,
    btnMock  = app.btnMock,
    isEnabled   = btnMock.hasClass("btn-danger");
     
     if (isEnabled) {
         bgGeo.stopMock(function(msg){
        	 btnMock.removeClass('btn-danger');
             btnMock.addClass('btn-success');
             app.isMocking = false;
             console.log(msg);
         }, function(msg){
        	 console.log(msg);
         });
     } else {
    	 
         bgGeo.startMock(function(msg){
        	 btnMock.removeClass('btn-success');
             btnMock.addClass('btn-danger');
             app.isMocking = true;
             console.log(msg);
         }, function(msg){
        	  console.log(msg);
         });
     }
    },
    
    onClickEnabled: function(value) {
        var bgGeo       = window.plugins.backgroundGeofencing,
            btnEnabled  = app.btnEnabled,
            isEnabled   = btnEnabled.hasClass("btn-danger");
        
        if (isEnabled) {
             
             bgGeo.stop(function(msg){
            	 btnEnabled.removeClass('btn-danger');
            	 btnEnabled.addClass('btn-success');
                 btnEnabled[0].innerHTML = 'Start';
                 console.log(msg);
             }, function(msg){
            	 console.log(msg);
             });
        } else {
            bgGeo.start(function(msg){
                btnEnabled.removeClass('btn-success');
                btnEnabled.addClass('btn-danger');
                btnEnabled[0].innerHTML = 'Stop';
                console.log(msg);
            }, function(msg){
            	console.log(msg);
            });
        }
    },
    /**
    * Cordova foreground geolocation watch has no stop/start detection or scaled distance-filtering to conserve HTTP requests based upon speed.  
    * You can't leave Cordova's Geofencing running in background or it'll kill your battery.  This is the purpose of BackgroundGeofencing:  to intelligently 
    * determine start/stop of device.
    */
    onPause: function() {
        console.log('- onPause');
    },
    /**
    * Once in foreground, re-engage foreground geolocation watch with standard Cordova Geofencing api
    */
    onResume: function() {
        console.log('- onResume');
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
        console.log('Received Event: ' + id);
    },

    setGeofences: function(tripPlan) {
    	var places = tripPlan.places;
    	for(var i in places){
    		var geofence = places[i].geofence;
    		var circleOptions = {
    			      strokeColor: '#3366c',
    			      strokeOpacity: 0.8,
    			      strokeWeight: 2,
    			      fillColor: '#3366c',
    			      fillOpacity: 0.35,
    			      map: app.map,
    			      center: new google.maps.LatLng(geofence.latitude, geofence.longitude),
    			      radius: parseInt(geofence.radius)
    			    };
    		// Add the circle for this city to the map.
    		var circleObject = new google.maps.Circle(circleOptions);
    		google.maps.event.addListener(circleObject, 'click', function(event) {
            	app.onMapClick(app.map, event);
            });
    		app.geofences.push(circleObject);
    	}
    	
    },
    setCurrentLocation: function(location) {
        if (!app.location) {
            app.location = new google.maps.Marker({
                map: app.map,
                icon: {
                    path: google.maps.SymbolPath.CIRCLE,
                    scale: 3,
                    fillColor: 'blue',
                    strokeColor: 'blue',
                    strokeWeight: 5
                }
            });
            app.locationAccuracy = new google.maps.Circle({
                fillColor: '#3366cc',
                fillOpacity: 0.4,
                strokeOpacity: 0,
                map: app.map
            });
        }
        if (!app.path) {
            app.path = new google.maps.Polyline({
                map: app.map,
                strokeColor: '#3366cc',
                fillOpacity: 0.4
            });
        }
        var latlng = new google.maps.LatLng(location.latitude, location.longitude);
        
        if (app.previousLocation) {
            var prevLocation = app.previousLocation;
            // Drop a breadcrumb of where we've been.
            app.locations.push(new google.maps.Marker({
                icon: {
                    path: google.maps.SymbolPath.CIRCLE,
                    scale: 3,
                    fillColor: 'green',
                    strokeColor: 'green',
                    strokeWeight: 5
                },
                map: app.map,
                position: new google.maps.LatLng(prevLocation.latitude, prevLocation.longitude)
            }));
        }

        // Update our current position marker and accuracy bubble.
        app.location.setPosition(latlng);
        app.locationAccuracy.setCenter(latlng);
        app.locationAccuracy.setRadius(location.accuracy);

        // Add breadcrumb to current Polyline path.
        app.path.getPath().push(latlng);
        app.previousLocation = location;
    }
};

app.initialize();
