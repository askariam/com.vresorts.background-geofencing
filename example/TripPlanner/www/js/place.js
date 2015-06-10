
var PLACE_ATTRIBUTES = [
    "uuid",
    "created",
    "place_name",
    "address",
    "user_uuid",
    "info_url",
    "trip_plan_uuid",
    "is_subscribed",
    "short_desc",
    "offer_uuid",
    "geofence"
];

function createPlace(createPlaceObj, successCB, errorCB) {
    createPlaceObj.type = "place";
    createPlaceObj.user_uuid = USER_UUID;
    createPlaceObj.is_subscribed = "false";
    postEntity(createPlaceObj, successCB, errorCB);
}

function readPlace(uuid, successCB, errorCB) {
    getEntity("place", uuid, PLACE_ATTRIBUTES, successCB, errorCB);
}

function readMultiplePlaces(attribute, value, successCB, errorCB) {
    getEntities("place", attribute, value, PLACE_ATTRIBUTES, successCB, errorCB);
}

function deletePlace(uuid, successCB, errorCB) {
    deleteEntity("place", uuid, successCB, errorCB);
}

function deleteAllPlacesBelongingToTripPlan(tripPlanUUID, successCB, errorCB) {
    deleteEntities("place", "trip_plan_uuid", tripPlanUUID, successCB, errorCB);
}

function togglePlaceSubscription(placeUUID) {
	
    function togglePlaceSubscriptionSuccessCB(place) {
    	 var unsubscribeData = {};
         unsubscribeData.type = "place";
         unsubscribeData.place_uuid = placeUUID;

         if(place.is_subscribed =="true") {
            $.ajax({
                type: "PUT", 
                dataType: "json",
                url: window.globalURL + "/disablePlaceSubscription",
                data: unsubscribeData,
                success: function(data){
                    console.log("backgroundGeofencing disablePlace!!");
                    window.plugins.backgroundGeofencing.disablePlace(function(){},function(){},{"place_uuid": placeUUID});
                },
                failure: function(errMsg) {}
            });
         }
         else {
            $.ajax({
                type: "PUT", 
                dataType: "json",
                url: window.globalURL + "/enablePlaceSubscription",
                data: unsubscribeData,
                success: function(data){                    
                    console.log("backgroundGeofencing enablePlace!!");
                    window.plugins.backgroundGeofencing.enablePlace(function(){},function(){},{"place_uuid": placeUUID});
                },
                failure: function(errMsg) {}
            });
         }
    }

    function togglePlaceSubscriptionErrorCB() {
        // Do nothing.
    }
   
    //getEntity("place", placeUUID, PLACE_ATTRIBUTES, togglePlaceSubscriptionSuccessCB, togglePlaceSubscriptionErrorCB);
    $.getJSON(window.globalURL + "/getPlace?place_uuid=" + placeUUID, function(place){
        togglePlaceSubscriptionSuccessCB(place);
    });
}


