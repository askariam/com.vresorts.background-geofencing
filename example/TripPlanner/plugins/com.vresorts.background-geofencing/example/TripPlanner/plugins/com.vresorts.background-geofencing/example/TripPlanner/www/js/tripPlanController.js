
$(document).ready(function() {
    $("#popup_create_trip_plan").popup();
    $("#popup_delete_trip_plan").popup();
    // Stops a problem in this input where hitting the return key reloads the page.
    $("#input_create_trip_plan_name").keypress(function (e) {
        if (e.which == 13) {
            return false;
        }
    });
    $("#select_trip_plan").bind( "change", function(event, ui) {
        window.globalID.tripPlanuuid = $("#select_trip_plan").val();
        if(typeof(Storage)!=="undefined") {
            localStorage.activeTripPlanUUID = $("#select_trip_plan").val();
        }
        loadPlaces($("#select_trip_plan").val());
    });
});

function loadTripPlans() {
    // Nested function definition for the success callback that goes to readMultipleTripPlans().
    function loadTripPlansSuccessCB(tripPlanList) {
        $("#list_view_trip_plans").empty();

        tripPlanList.forEach( function(tripPlan) {
            // Makes a javascript Date object from Unix Epoch Time.
            var dateTime = new Date(tripPlan.created);

            $("#list_view_trip_plans").append(
                "<li>"
                + "Trip Plan Name: <i>" + tripPlan.trip_plan_name + "</i><br>"
                + "Created: <i>" + dateTime.toString() + "</i><br>"
                + "<div data-role='controlgroup' data-type='horizontal'>"
                + "<a href='#' data-role='button' data-icon='eye' data-mini='true'"
                + "onclick=\x22$.mobile.changePage(\x27#page_view_trip_places\x27); "
                + "loadTripPlaces(\x27" + tripPlan.uuid + "\x27);\x22>View As List</a>"
                + "<a href='#' data-role='button' data-icon='delete' data-mini='true'"
                + "onclick=\x22deleteTripPlanClickedUnconfirmed(\x27" + tripPlan.uuid + "\x27);\x22>Delete</a>"
                + "</div>"
		        + "</li>"
		    );

            $("#list_view_trip_plans").trigger("create");
            $("#list_view_trip_plans").listview("refresh");
        });
    }

    // Nested function definition for the error callback that goes to readMultipleTripPlans()
    function loadTripPlansErrorCB(tripPlanList) {
        // TODO: Deal with this error.
    }

    // Reads the trip plans
    readMultipleTripPlans("user_uuid", USER_UUID, loadTripPlansSuccessCB, loadTripPlansErrorCB);
}

function createTripPlanClickedUnconfirmed() {
    $("#popup_create_trip_plan").popup("open");
}

function createTripPlanClicked() {
    function createTripPlanClickedSuccessCB(uuid) {
        $("#popup_create_trip_plan").popup("close");
        $("#input_create_trip_plan_name").val("");
        window.globalID.tripPlanuuid = uuid;
        if(typeof(Storage)!=="undefined") {
            localStorage.activeTripPlanUUID = uuid;
        }
        $('#list_view_places').empty();
        $("#list_view_places").listview("refresh");
        loadTripPlansToSelect();
    }

    function createTripPlanClickedErrorCB() {
        $("#popup_create_trip_plan").popup("close");
        $.mobile.changePage("#page_create_trip_plan_error");
    }

    var tripPlanName = $("#input_create_trip_plan_name").val()

    createTripPlan(tripPlanName, createTripPlanClickedSuccessCB, createTripPlanClickedErrorCB);
}

function deleteTripPlanClickedUnconfirmed(uuid) {
    if(uuid !== "undefined") {
        $("#div_delete_trip_plan_confirm").empty();

        $("#div_delete_trip_plan_confirm").append(
            "<p>Are you sure you want to delete this trip plan?</p>"
            + "<button type='button' style='background-color:#EEEEEE; border-style:solid; border-color:#CCCCCC'; "
            + "onclick=\x22deleteTripPlanClicked(\x27" + uuid + "\x27);\x22>Yes, delete the trip plan.</button>"
            + "<button type='button' style='background-color:#EEEEEE; border-style:solid; border-color:#CCCCCC'; "
            + "onclick=\x22$('#popup_delete_trip_plan').popup('close');\x22>No, do not delete.</button>"
        );

        $("#div_delete_trip_plan_confirm").trigger("create");

        $("#popup_delete_trip_plan").popup("open");
    }
}

function deleteTripPlanClicked(uuid) {
    // Nested function definition for the success callback that goes to deleteTripPlan().
    function deleteTripPlanClickedSuccessCB() {
        window.globalID.tripPlanuuid = "undefined";
        if(typeof(Storage)!=="undefined") {
            localStorage.activeTripPlanUUID = "undefined";
        }
        loadTripPlansToSelect();
    }

    // Nested function definition for the error callback that goes to deleteTripPlan().
    function deleteTripPlanClickedErrorCB() {
        loadTripPlansToSelect();
    }

    deleteTripPlan(uuid, deleteTripPlanClickedSuccessCB, deleteTripPlanClickedErrorCB);

    $("#popup_delete_trip_plan").popup("close");
}

function loadTripPlansToSelect() {
    function loadTripPlansToSelectSuccessCB(tripPlanList) {

        if(tripPlanList.length < 1) {
            $("#select_trip_plan").empty();
            $("#select_trip_plan").selectmenu("refresh");
            $('#list_view_places').empty();
            $('#list_view_places').append('<li>You currently have no trip plans. Please create a new trip plan.</li>');
            $("#list_view_places").listview("refresh");
        }
        else {
            $("#select_trip_plan").empty();
            tripPlanList.forEach( function(tripPlan) {
                $("#select_trip_plan").append(
                    "<option value='"
                    + tripPlan.uuid
                    + "'>"
                    + tripPlan.trip_plan_name
                    + "</option>"
                ).selectmenu("refresh");
            });
            
            
            if(window.globalID.tripPlanuuid !== "undefined") {
                $("#select_trip_plan").val(window.globalID.tripPlanuuid).attr('selected', true).siblings('option').removeAttr('selected');
                $("#select_trip_plan").selectmenu("refresh");
            }
            else {
                if($("#select_trip_plan option").length > 0) {
                    $("#select_trip_plan").val($("#select_trip_plan option:first").val());
                    window.globalID.tripPlanuuid = $("#select_trip_plan").val();
                    if(typeof(Storage)!=="undefined") {
                        localStorage.activeTripPlanUUID = $("#select_trip_plan").val();
                    }
                }
                
                loadPlaces($("#select_trip_plan").val());
                //Trajon Track plugin  add reconfigure
            }
        }
    }

    function loadTripPlansToSelectErrorCB(tripPlanList) {
        // Do nothing.
    }

    readMultipleTripPlans("user_uuid", USER_UUID, loadTripPlansToSelectSuccessCB, loadTripPlansToSelectErrorCB);
}

function facebookShare() {
    var facebookURL = "https://www.facebook.com/sharer/sharer.php?u=";
    //var linkURL = "http://vresorts.com/futuretravel/tripPlanner/sharedTripPlan.html?trip_plan_uuid::";
    var linkURL = "http://underreact.com/tripPlanner/sharedTripPlan.html?trip_plan_uuid::";
    window.location.href = facebookURL + linkURL + window.globalID.tripPlanuuid;
}

