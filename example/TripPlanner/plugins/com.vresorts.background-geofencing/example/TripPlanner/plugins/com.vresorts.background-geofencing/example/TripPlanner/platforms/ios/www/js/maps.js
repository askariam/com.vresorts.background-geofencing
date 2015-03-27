
var MAPS = {
    "Big Island":"http://www.arcgis.com/home/webmap/templates/OnePane/basicviewer/embed.html?webmap=da209fcf6173471ab9f2233f0d645232&amp;gcsextent=-157.0939,18.9021,-154.1194,20.7879&amp;displayslider=true&amp;displayscalebar=true&amp;displaybasemaps=true",
    "Los Angeles":"http://www.arcgis.com/home/webmap/templates/OnePane/basicviewer/embed.html?webmap=3392a4d7debc4ca5a3b6e8df611fc636&amp;gcsextent=-118.3174,34.0013,-118.2271,34.0736&amp;displayslider=true&amp;displayscalebar=true&amp;displaybasemaps=true",
}

$(document).ready(function() {
    $("#page_display_trip_plan").bind("pagebeforeshow", function(event) {
//        loadMap();
        loadTripPlansToSelect();        
        loadPlaces(window.globalID.tripPlanuuid);
    });
});

$(document).ready(function() {
    $("#page_display_map").bind("pagebeforeshow", function(event) {
        loadMapNotLoggedIn();
    });
});

function loadMap() {
    $("#div_add_map_container").empty();
    $("#div_add_map_container_not_logged_in").empty();
    $("#div_add_map_container").append(
        "<iframe class='map-frame' frameborder='0' scrolling='no' marginheight='0' marginwidth='0' "
        + "src='"
        + MAPS[ACTIVE_MAP]
        + "'></iframe>"
    );
}

function loadMapNotLoggedIn() {
    $("#div_add_map_container_not_logged_in").empty();
    $("#div_add_map_container").empty();
    $("#div_add_map_container_not_logged_in").append(
        "<iframe class='map-frame' frameborder='0' scrolling='no' marginheight='0' marginwidth='0' "
        + "src='"
        + MAPS[ACTIVE_MAP]
        + "'></iframe>"
    );
}
