/**
 * @Bo Wang
 */
var EVENT_ATTRIBUTES = [
     'uuid',
     'event_start_time',
     'event_end_time',
     'event_title',
     'trip_plan_uuid',   //place.trip_plan_uuid
     'place_uuid',        //place.uuid
     'user_uuid'          //localStorage.userUUID or USER_UUID
];

function TripCalendar(){
	
}

//Getter
TripCalendar.readMultipleEvents = function(attribute, value, successCB, errorCB) {
    getEntities("trip_events", attribute, value, EVENT_ATTRIBUTES, successCB, errorCB);
};
//#End of Setter

//Setter
TripCalendar.createEvent  = function(createEventObj, successCB, errorCB) {
    createEventObj.type = "trip_events";
    postEntity(createEventObj, successCB, errorCB);
};

TripCalendar.deleteEvent = function(uuid, successCB, errorCB) {
    deleteEntity("trip_events", uuid, successCB, errorCB);
};

TripCalendar.deleteEvents = function(trip_plan_uuid, successCB, errorCB) {
    deleteEntities("trip_events", "trip_plan_uuid", trip_plan_uuid, successCB, errorCB);
};

//Add other Setters here
//#End of Setter
