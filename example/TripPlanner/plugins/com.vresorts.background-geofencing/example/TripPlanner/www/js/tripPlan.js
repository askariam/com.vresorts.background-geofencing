
var TRIP_PLAN_ATTRIBUTES = [
    "uuid",
    "created",
    "trip_plan_name",
    "user_uuid"
];

function createTripPlan(tripPlanName, successCB, errorCB) {
    var createTripPlanObj = {};
    createTripPlanObj.type = "trip_plan";
    createTripPlanObj.trip_plan_name = tripPlanName;
    createTripPlanObj.user_uuid = USER_UUID;
    postEntity(createTripPlanObj, successCB, errorCB);
}

function readTripPlan(uuid, successCB, errorCB) {
    getEntity("trip_plan", uuid, TRIP_PLAN_ATTRIBUTES, successCB, errorCB);
}

function readMultipleTripPlans(attribute, value, successCB, errorCB) {
    getEntities("trip_plan", attribute, value, TRIP_PLAN_ATTRIBUTES, successCB, errorCB);
}

function deleteTripPlan(uuid, successCB, errorCB) {
    function deleteTripPlanSuccessCB() {
        deleteAllPlacesBelongingToTripPlan(uuid, function() {
            //TripCalendar.deleteEvents(uuid, successCB, errorCB);
            successCB();
        },
        function() {
            errorCB();
        });        
    }

    function deleteTripPlanErrorCB() {
        errorCB();
    }

    deleteEntity("trip_plan", uuid, deleteTripPlanSuccessCB, deleteTripPlanErrorCB);
}

function deleteAllTripPlansBelongingToUser(userUUID, successCB, errorCB) {
    deleteEntities("trip_plan", "user_uuid", userUUID, successCB, errorCB);
}

