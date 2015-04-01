//
//   This file contains logic related to displaying places in a trip plan
//
if(typeof(Storage)!=="undefined") {
    localStorage.placeUpdated = "false";
}
$(document).ready(function() {
    $("#popup_delete_place").popup();
});

function requestTripPlan(url, callback){
	var xmlhttp = new XMLHttpRequest();

	xmlhttp.onreadystatechange = function() {
	if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
		var myArr = JSON.parse(xmlhttp.responseText);
	    callback(myArr);
	    }
	}

	xmlhttp.open("GET", url, true);
	xmlhttp.send();
}

function loadPlaces(tripPlanUUID) {
	if(tripPlanUUID !== "undefined") {
    }
    else {
        $('#list_view_places').empty();
        $("#list_view_places").append("<li>No active trip plan selected. Select a trip plan from the box above.</li>");
        $("#list_view_places").listview("refresh"); 
        return;
    }
	
    window.globalID.tripPlanuuid = tripPlanUUID;
    // Nested function definition for the success callback that goes to readMultiplePlaces().
    
    
    function loadPlacesSuccessCB(placesList) {
    	this.curPlaceList = placesList;
//    	if(window.mobilePlugin) {
//    		mobilePlugin.onTripplanSwitched(tripPlanUUID);
//    	}
    
        $("#list_view_places").empty();

        placesList.forEach( function(place) {
            if(place.offer_uuid != "0") {
                var offerDetail = 
                    "<a href='#' data-role='button' data-mini='true' style='width:40px;' "
                    + "onclick=\x22offerDetailsClicked(\x27"
                    + place.offer_uuid 
                    + "\x27);\x22>Offer</a>";
                var switchHTML =
                    "<select onchange=\x22togglePlaceSubscription(\x27"
                    + place.uuid
                    + "\x27);\x22"
                    + "name='switch-" + place.uuid + "' "
                    + "id='switch-" + place.uuid + "' data-role='slider' data-mini='true'>"
                    + "<option value='off'>Off</option>"
                    + "<option value='on'>On</option></select>";
             
            //***Button for coupon and barcode testing
                var testButton =
                    "<a href='#' data-role='button' data-mini='true' style='width:40px;' "
                    + "onclick=\x22addNotification(\x27"
                    + place.offer_uuid
                    + "\x27,\x27"
                    + place.place_name
                    + "\x27);\x22>test</a>";
//                var subscribedCheckboxHTML =
//                    "<input onchange=\x22togglePlaceSubscription(\x27"
//                    + place.uuid
//                    + "\x27);\x22 type=\x22checkbox\x22 data-type='vertical'"
//                    + "data-mini='true' data-iconpos='left' name='checkbox-" + place.uuid + "' "
//                    + "id='checkbox-" + place.uuid + "'/>"
//                    + "<label data-inline='true' style='width:86px;heigth:36px;padding:8px;' for='checkbox-" 
//                    + place.uuid
//                    +"'><div class='ui-icon-heart ui-btn-icon-notext'></div></label>";
            }
            else {

                var offerDetail = "";
                var switchHTML = "";
                var testButton = "";
//                var subscribedCheckboxHTML = "";
            }

            $("#list_view_places").append(
                "<li><div>"
                + "<b>" + place.place_name + "</b>" + "<br>"
                + "<div class='word-break'><i>"
                + place.address + "</i></div>"
                + "<div class='word-break'>"
                + place.short_desc + "</div>"
                
                + "<div data-role='controlgroup' data-type='horizontal' class='ui-grid-a'>"
                + "<a href='" + place.info_url + "'target='_blank' data-role='button' data-mini='true'  style='width:40px;'>Info</a>"
                + offerDetail
                + testButton
                + "&nbsp;&nbsp;"
//                + subscribedCheckboxHTML
                + switchHTML
                
                + "</div>"

		        + "</li>"
		    );

            $("#list_view_places").listview("refresh");
            $("#list_view_places").trigger("create");
            if(place.is_subscribed == "true") {
                $("#switch-"+ place.uuid).val("on").slider("refresh");
//                $("#checkbox-"+ place.uuid).attr("checked", true).checkboxradio("refresh");
            }
        });
    }

//    readMultiplePlaces("trip_plan_uuid", tripPlanUUID, loadPlacesSuccessCB, loadPlacesErrorCB);
// 		read data from api.
    // for test
    //tripPlanUUID = 'a137a68a-be1f-11e4-a532-9192b501077c';
    requestTripPlan('http://xixixhalu-test.apigee.net/proxy/tripPlanner/getPlaces?trip_plan_uuid='+tripPlanUUID, function(tripplan){
    	loadPlacesSuccessCB(tripplan.places);
        // Nested function definition for the error callback that goes to readMultiplePlaces()
        function loadPlacesErrorCB() {
            // TODO: Deal with this error.
        }    
        function configureSuccessCB() {
            // 
        }        
        function configureErrorCB() {
            // 
        }     
        function startSuccessCB() {
            // 
        }        
        function startErrorCB() {
            // 
        } 
    	var bgGeo = window.plugins.backgroundGeofencing;
		//getCurrentPosition();
        //getStoreLocation();
        //Trajon Track plugin updating head
    	//var anonymousTripplan = {"uuid":"none", "trip_plan_name":"none", "user_uuid":"none", "places":placesList};
        bgGeo.configure(configureSuccessCB,configureErrorCB,tripplan);
        bgGeo.start(startSuccessCB,startErrorCB);
        //Trajon Track plugin updating tail
        
        
});
    
}

function deletePlaceClicked(placeUUID, tripPlanUUID) {
    // Nested function definition for the success callback that goes to deletePlace().
    function deletePlaceClickedSuccessCB() {
        loadPlaces(tripPlanUUID);
    }

    // Nested function definition for the error callback that goes to deletePlace().
    function deletePlaceClickedErrorCB() {
        loadPlaces(tripPlanUUID);
    }

    deletePlace(placeUUID, deletePlaceClickedSuccessCB, deletePlaceClickedErrorCB);

    $("#popup_delete_place").popup("close");
}

function deletePlaceClickedUnconfirmed(placeUUID, tripPlanUUID) {
    $("#div_delete_place_confirm").empty();

    $("#div_delete_place_confirm").append(
        "<p>Are you sure you want to delete this place?</p>"
        + "<button type='button' style='background-color:#EEEEEE; border-style:solid; border-color:#CCCCCC;' "
        + "onclick=\x22deletePlaceClicked(\x27" + placeUUID + "\x27, " + "\x27 " + tripPlanUUID + "\x27);\x22>Yes, delete the place.</button>"
        + "<button type='button' style='background-color:#EEEEEE; border-style:solid; border-color:#CCCCCC;' "
        + "onclick=\x22$('#popup_delete_place').popup('close');\x22>No, do not delete.</button>"
    );

    $("#div_delete_place_confirm").trigger("create");

    $("#popup_delete_place").popup("open");
}

function addPlace(placeData) {
    function addPlaceSuccessCB() {
        $("#div_added_message").empty();
        $("#div_added_message").append(
            placeData.place_name + " has been successfully added to your trip plan."
        );
        $.mobile.changePage("#page_add_place_success");
        PARAMS = {};
        IS_PARAMS = false;
        if(typeof(Storage)!=="undefined") {
            localStorage.placeUpdated = "true";
        }
    }

    function addPlaceErrorCB() {
        $.mobile.changePage("#page_add_place_error");
    }

    if(!IS_LOGGED_IN) {
        $.mobile.changePage("#page_add_place_not_logged_in");
    }
    else if(window.globalID.tripPlanuuid != "undefined") {
        placeData.trip_plan_uuid = window.globalID.tripPlanuuid;

        var matchName = placeData.place_name.replace(/\W/g, '')

        readMultipleOffers("match_name", matchName, function(offerList) {
            if(offerList.length > 0) {
                placeData.short_desc = offerList[0].short_desc;
                placeData.offer_uuid = offerList[0].uuid;
                createPlace(placeData, addPlaceSuccessCB, addPlaceErrorCB);
            }
            else {
                placeData.short_desc = "No offers available.";
                placeData.offer_uuid = "0";
                createPlace(placeData, addPlaceSuccessCB, addPlaceErrorCB);
            }
        },
        function() {
            $.mobile.changePage("#page_problem_with_place");
        });        
    }
    else {
        $.mobile.changePage("#page_no_active_trip_plan");
    }
}

