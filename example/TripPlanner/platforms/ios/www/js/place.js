
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

//Trajon Track plugin updating head
function togglePlaceSubscription(placeUUID) {
	
    function togglePlaceSubscriptionSuccessCB(place) {
    	 var unsubscribeData = {};
         unsubscribeData.type = "place";
         unsubscribeData.uuid = placeUUID;


         if(place.is_subscribed =="true") {

        	 unsubscribeData.is_subscribed = "false";
         }
         else {
        	unsubscribeData.is_subscribed = "true";
         }

         
        function scb(place) {
        	 if(place.is_subscribed =="true") {
            	 window.plugins.backgroundGeofencing.enablePlace(function(){},function(){},{"place_uuid": placeUUID});
             }
             else {
            	 window.plugins.backgroundGeofencing.disablePlace(function(){},function(){},{"place_uuid": placeUUID});
             }
        }
        function ecb() {}
        
        modifyEntity(unsubscribeData, scb, ecb);
    }

    function togglePlaceSubscriptionErrorCB() {
        // Do nothing.
    }
   
    getEntity("place", placeUUID, PLACE_ATTRIBUTES, togglePlaceSubscriptionSuccessCB, togglePlaceSubscriptionErrorCB);
}
//Trajon Track plugin updating tail


