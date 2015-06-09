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
    $("#div_password_info").append("<input type='tel' pattern='[0-9]*' inputmode='numeric' style='-webkit-text-security: disc' name='password' id='offer_password' value='' placeholder='PASSWORD'>");
    $("#popup_password").popup("open");
    //Performance note: call on local data will be faster --> wating for improvement
    $.getJSON(window.globalURL + "/getPlaces?trip_plan_uuid=" + window.globalID.tripPlanuuid, function(tripplan){
              $.each(tripplan.places, function(i, item){
                     var tmpID = item.uuid;
                     var tmpIsSub = item.is_subscribed;
                     if(item.offer_uuid == window.globalID.offeruuid && tmpIsSub == "true"){
                        togglePlaceSubscription(tmpID);
                }
                     
            })
    });
    
    $.getJSON(window.globalURL + "/getOffer2?offer_uuid="+ window.globalID.offeruuid, function(offer){
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