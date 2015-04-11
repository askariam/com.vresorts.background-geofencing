/*
  This file contains all the logic to get current locations from phone, etc.
  It used a Object Oriented Concept
  */
/*
    Get password and load the dialog
*/
var redeemPassword = "0";
var partnerUUID = "0";
var couponIMAGE = "0";

$(document).ready(function() {
                  $("#popup_password").popup();
                  });

function passwordClicked(uuid, place_name) {
    $("#div_password_name").empty();
    $("#div_password_info").empty();
    window.globalID.offeruuid = uuid;
    $("#div_password_name").append("<h2>"+place_name+"</h2>");
    $("#div_password_info").append("<input type='password' name='password' id='offer_password' value='' placeholder='password'>");
    $("#popup_password").popup("open");
    
    //Performance note: call on local data will be faster --> wating for improvement
    $.getJSON("http://xixixhalu-test.apigee.net/proxy/tripPlanner/getPlaces?trip_plan_uuid=" + window.globalID.tripPlanuuid, function(tripplan){
              $.each(tripplan.places, function(i, item){
                     var tmpID = item.uuid;
                     var tmpIsSub = item.is_subscribed;
                     if(item.offer_uuid == window.globalID.offeruuid && tmpIsSub == "true"){
                        togglePlaceSubscription(tmpID);
                }
                     
            })
    });
    
    $.getJSON("http://xixixhalu-test.apigee.net/proxy/tripPlanner/getOffer2?offer_uuid="+ window.globalID.offeruuid, function(offer){
              redeemPassword = offer.redeem_password;
              partnerUUID = offer.partner_uuid;
              couponIMAGE = offer.coupon.image_data;
              });
    
}


function passwordCheck(){
    var inputPassword = document.getElementById("offer_password").value;
    if( inputPassword == redeemPassword){
        getCouponClicked(window.globalID.offeruuid, partnerUUID, couponIMAGE);
    }
    else{
        alert("Wrong password, please try again");
    }
}

//Network stats variables initialization
//Put Imran's code here
var wifiGood=false;

//Handles to track the user 
var userTrackingButtonOn= true; //Check if user has kept tracking on
var sleepTime = 2000;   //sleep time
var currentVelocity=0, averageVelocity=0, weightedVelocity=0, ETAtime=0;    //Variables for user tracking
var accuracyRequired=80;    //in metres //change from 80 to 25 based on Doug's requirement

var storeNeighborhoodRadius=0.070;  //in kilo-metres
var storeGeofenceRadius=0.040; //in kilo-meters, just initialized

//general boolean values to indicate the location
var insideGeofence=false;
var insideNeighborhood=false;   

var prevMinStoreID; //to store the previous  store tracking ID
var isFirstTime=0;  //0 is first time; 1 is not first time
var distanceLeft;   //Calculate the distance left between current location and nearest store
var minDistStoreID; //Stores minimum distance store

//current location values:
var currentLocationLatitude = "undefined", currentLocationLongitude = "undefined", currentLocationAccuracy;

//Network stats variables initialization
//Put Imran's code here
var wifiGood=false;
var dataConnectionGood=true;

//One general JSON datastructure for a single store
var storeLocation = "undefined";  //Local Datastructure
var displayString;

/** Converts numeric degrees to radians */
function toRad(Value) {
    /** Converts numeric degrees to radians */
    return Value * Math.PI / 180;
}

//Calculate the distance for the current location that was just found!
function calculateSleepTimeETA(){
    //Calculate the distance between the current position and the store's location
    // Latitude/longitude spherical geodesy formulae & scripts (c) Chris Veness 2002-2011                   - www.movable-type.co.uk/scripts/latlong.html 
    // where R is earth���s radius (mean radius = 6,371km);
    // note that angles need to be in radians to pass to trig functions!
    if(storeLocation != "undefined" && currentLocationLatitude != "undefined" && currentLocationLongitude != "undefined")
    {
        if(insideGeofence==true)    //If not outside of current geofence..continue tracking the same geofence
        {
            //Calculate distance and usual stuff
            //Current Location
            var lat1=currentLocationLatitude;
            var lon1=currentLocationLongitude;
            //DESTINATION
            var lat2=storeLocation.places[minDistStoreID].latitude; 
            var lon2=storeLocation.places[minDistStoreID].longitude;

            var R = 6371; // km
            var dLat = toRad(lat2-lat1);
            var dLon = toRad(lon2-lon1);
            var lat1 = toRad(lat1);
            var lat2 = toRad(lat2);

            var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
            var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
            minDistance = R * c;
        }
        else    //Not inside of any geo-fence, so find the new nearest geo-fence
        {
            var minDistance=10000000000;
            for(var i=0;i<storeLocation.places.length;i++)
            {
                var obj=storeLocation.places[i];
                
                if(obj.is_subscribed === "true")
                {
                    //Compute the distance between current location and this obj's location
                    //Current Location
                    //console.log("iiiiiis_subscribed" + obj.is_subscribed);
                    //currentLocationLatitude = 34.020562;
                    //currentLocationLongitude = -118.2854;
                    var lat1=currentLocationLatitude;
                    var lon1=currentLocationLongitude;
                    
                    //DESTINATION
                    var lat2=obj.geofence.latitude;  
                    var lon2=obj.geofence.longitude;
                    console.log("lat2 : " + lat2);
                    console.log("lon2 : " + lon2);

                    var R = 6371; // km
                    var dLat = toRad(lat2-lat1);
                    var dLon = toRad(lon2-lon1);
                    var lat1 = toRad(lat1);
                    var lat2 = toRad(lat2);

                    var a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
                    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
                    var xDistanceLeft = R * c;
                    
                    if(xDistanceLeft<minDistance)   //Find the nearest store
                    {   
                        minDistance=xDistanceLeft;
                        minDistStoreID=i;
                        storeGeofenceRadius=(obj.geofence.radius/1000); //in kilo-meters
                    }
                }
            }
            if(isFirstTime==0)  //check if first time iterated or not, if not check if the store is still the same store, if not reset the global variables insideNeighborhood and insideGeofence as false
            {
                isFirstTime=1;
                prevMinStoreID=minDistStoreID;
            }
            else
            {
                if(prevMinStoreID!=minDistStoreID)
                {   
                    insideNeighborhood==false;
                    insideGeofence==false;
                }
            }         
        }

        distanceLeft = minDistance;
        displayString = "Store Name:"+storeLocation.places[minDistStoreID].place_name+"Distance: "+ distanceLeft +  "km";

    //Check if inside the geo-fence or neighborhood
    checkIfInside();
    //Log the position to Danny's fn location log
    //Put Danny's code here
    }      
}
//Check if the distance is less or equal to the geo-fence
function checkIfInside(){
    console.log("distanceLeft: " + distanceLeft);
    console.log("storeGeofenceRadius: " + storeGeofenceRadius);
    if(distanceLeft>storeNeighborhoodRadius && insideNeighborhood==false && insideGeofence==false)  //Trying to enter, outside Neighborhood
    {
        displayString+="Outside neighborhood";
        sleepMore();
    }
    else if(distanceLeft<=storeNeighborhoodRadius && distanceLeft>storeGeofenceRadius && insideNeighborhood==false && insideGeofence==false)    //Trying to enter, just entered inside Neighborhood but still outside store geofence (works even if tracking begun inside neighborhood)
    {
        insideNeighborhood=true;
        displayString+="Inside neighborhood";
        sleepLess();
    }
    else if(distanceLeft<=storeGeofenceRadius && insideGeofence==false && insideNeighborhood==true) //Trying to enter, just entered store's geofence
    {
        insideGeofence=true;
       
        displayString+="INSIDE GEOFENCE...COUPON DELIVERED!!";
        addNotification(storeLocation.places[minDistStoreID].offer_uuid,storeLocation.places[minDistStoreID].place_name)
        //Turn off subscription of this store
        storeLocation.places[minDistStoreID].is_subscribed=false;

        sleepOther();
    }
    else if(distanceLeft<=storeGeofenceRadius && insideGeofence==false && insideNeighborhood==false)    //Tracking begun inside store's geofence
    {
        insideGeofence=true;
        insideNeighborhood=true;

        displayString+="INSIDE GEOFENCE...COUPON DELIVERED!!";
        addNotification(storeLocation.places[minDistStoreID].offer_uuid,storeLocation.places[minDistStoreID].place_name)
        //Turn off subscription of this store
        storeLocation.places[minDistStoreID].is_subscribed=false;

        sleepOther();
    }
    else if(distanceLeft<=storeGeofenceRadius && insideGeofence==true && insideNeighborhood==true)  //trying to leave, still inside geo-fence
    {

        displayString+="Still inside Geofence";
        sleepOther();
    }
    else if(distanceLeft<=storeNeighborhoodRadius && distanceLeft>storeGeofenceRadius && insideNeighborhood==true && insideGeofence==true)  //trying to leave, still inside neighborhood but stepped outside store geofence
    {
        insideGeofence=false;

        displayString+="Exited stores Geofence..but still inside neighborhood";
        sleepLess();
    }
    else if(distanceLeft>storeNeighborhoodRadius && insideNeighborhood==true && insideGeofence==false)  //trying to leave, exited the neighborhood
    {
        insideNeighborhood=false;

        displayString+="Left Neighborhood! sleeping more again!";
        sleepMore();
    }
    else //Inside Neighborhood
    {
        if(insideGeofence==true)
        {
            insideGeofence=false;
            
            //displayString+="Exited stores Geofence..but still inside neighborhood<br/><img src='./img/bye.JPG'><br/>";
            displayString+="Exited stores Geofence..but still inside neighborhood";
            sleepLess();
        }
        else
        {
            displayString+="Inside Neighborhood";
            sleepLess();
        }
    }
    console.log("String: " + displayString);    
  
}
//sleep more (outside neighborhood) (50% of ETA)
function sleepMore()
{
    //get the current velocity and avg velocity in kmph from Danny!
    //Put Danny's code here
    currentVelocity=5;  //for walking
    averageVelocity=6;  //for walking

    //calculate the ETA (in seconds)
    weightedVelocity=(currentVelocity*2+averageVelocity)/3;
    var localDistanceLeft=distanceLeft-storeNeighborhoodRadius;
    ETAtime = localDistanceLeft/weightedVelocity; //in hours
    ETAtime*=3600;//in seconds

    displayString=displayString + 'Weighted Velocity: '+ weightedVelocity + ' kmph '+ 'Time to target: '+ ETAtime + ' s ' +'Sleep Time: '+ (ETAtime/2) +' s ';
    
    //determmine the sleep time
    //half of the ETAtime to the target (as want to check in between too)
    sleepTime=(ETAtime/2)*1000; //sleep time in milli seconds (50% of ETA)
    displayString+=" inside sleepMore ";
    
    //Max-Min sleeptime for sleepmore that may cause app to freeze
    if(sleepTime>45000)sleepTime=45000; //Max sleep time of 45 secs inside neighborhood
    if(sleepTime<25000)sleepTime=25000;   //don't want too many pings for location..so sleep 25 sec
 
    //Trajon Track plugin updating head
    setTimeout(getCurrentPosition,sleepTime);
    //Trajon Track plugin updating tail
}
//sleep less (inside neighborhood)
function sleepLess()
{
    //get the current velocity and avg velocity in kmph
    currentVelocity=5;
    averageVelocity=6;  

    //calculate the ETA (in seconds)
    weightedVelocity=(currentVelocity*2+averageVelocity)/3;
    var localDistanceLeft=distanceLeft-storeGeofenceRadius;
    ETAtime = localDistanceLeft/weightedVelocity; //in hours
    ETAtime*=3600;//in seconds

    displayString=displayString + 'Weighted Velocity: '+ weightedVelocity + ' kmph '+ 'Time to target: '+ ETAtime + ' s ' +'Sleep Time: '+ (ETAtime/5) +' s ';
    //element.innerHTML=displayString+element.innerHTML;
    //determmine the sleep time
    sleepTime=(ETAtime/5)*1000; //sleep time in milli seconds (20% of ETA)
    displayString+="inside sleepLess";

    if(sleepTime>20000)sleepTime=2000; //Max sleep time of 20 secs inside neighborhood
    if(sleepTime<10000)sleepTime=10000;   //don't want too many pings for location..so sleep 10 sec
    //Trajon Track plugin updating head
    setTimeout(getCurrentPosition,sleepTime);
    //Trajon Track plugin updating tail
}
function sleepOther()
{
    displayString=displayString + "Inside Geofence..so sleeping less";
    //element.innerHTML=displayString+element.innerHTML;
    
    //determmine the sleep time

    sleepTime=4000; //sleep 4 seconds only
    displayString+="inside sleepOther=5sec ";
    //Trajon Track plugin updating head
    setTimeout(getCurrentPosition,sleepTime);
    //Trajon Track plugin updating tail
}
//Trajon Track plugin updating head

function getCurrentPosition(){
    //Check the network stats Call Imrans code. And set wifiGood variable
    //Put Imran's code here
    if(wifiGood==false)
    {
        // Get the most accurate position updates available on the
        // device.
    	window.plugins.backgroundGeofencing.getCurrentLocation(onSuccess, onError);

    }
    else
    {
        // Just get a location, even if not accurate
    	window.plugins.backgroundGeofencing.getCurrentLocation(onSuccess,onError);
    }
}
function onSuccess(position) {
    currentLocationLatitude=position.coords.latitude;   //for testing, change this value
    //console.log(currentLocationLatitude);
    currentLocationLongitude=position.coords.longitude; //for testing, change this value
    // console.log(currentLocationLongitude);
    currentLocationAccuracy=position.coords.accuracy;
    
    if(currentLocationAccuracy>accuracyRequired)    //If accuracy is bad, get location again!
    {
        wifiGood=false;
        getCurrentPosition();

    }

    calculateSleepTimeETA();    //calculate distance, and check if inside the geo-fence, if not calculate sleep time and sleep again  
}

// onError Callback receives a PositionError object
//
function onError(error) {
    //Check network health, set wifiGood if needed
    //Put Imran's code here
    wifiGood=false;

    //Get location again
    getCurrentPosition();
}
//Trajon Track plugin updating tail

function getStoreLocation(){
    $.getJSON("http://xixixhalu-test.apigee.net/proxy/tripPlanner/getPlaces?trip_plan_uuid="+ window.globalID.tripPlanuuid, function(json){
        //alert("JSON Data: " + json.places[0].place_name);
        storeLocation = json;
        calculateSleepTimeETA(); 
    });
}
