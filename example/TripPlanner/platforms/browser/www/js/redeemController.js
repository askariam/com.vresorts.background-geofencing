/*
 This file contains the functions that involve display coupon, coupon barcode generator, and push transaction (created by Ivy and modified by Pat)
 */

var myOfferID = "0";
var myPartnerID = "0";
var TimeStamp = "0";
var MTimeStamp = "0";
var MTimeStampString = "0";
var CouponCode = "0";
var respMsg = "";


function getCouponClicked(offerID, partnerID, couponImage){
    //$("#div_coupon_container").empty();
    $("#div_coupon_image").empty();
    $("#div_coupon_storecode").empty();
    $("#div_coupon_barcode").empty();
    
    // var for baecode generator
    TimeStamp = new Date().getTime();  // system time- how many minllion seconds
    MTimeStamp = parseInt(TimeStamp/(1000 * 60)); // how many minutes
    MTimeStampString = MTimeStamp.toString();
    myOfferID = offerID;
    myPartnerID = partnerID;
    CouponCode = MTimeStampString.concat(myOfferID.replace(/-/g,''));
    
    readOffer(offerID, function() {
              //display coupon
              $('#div_coupon_image').append('<img src="data:image/png;base64,' + couponImage + '" height="100%" width="100%" position="relative" />');
              $('#div_coupon_barcode').barcode(CouponCode, "code93",{barWidth:1, barHeight:50});
              $('#div_coupon_storecode').append('<h4>Store code: ' + window.globalID.couponEncode.substr(0,3)
                                                + window.globalID.couponEncode.slice(-3)+ '</h4>');

              //pushTransaction();
             
              },
              function(){
              $('#div_coupon_image').append('Unable to get coupon at this time.');

              });
    $.mobile.changePage("#page_coupon");

}


// pushing the transaction to server
function pushTransaction(){
    
    $.getJSON(window.globalURL + '/postTransaction?coupon_code='+ CouponCode +'&partner_uuid='+ myPartnerID + '&offer_uuid='+ myOfferID + '&user_uuid='+ localStorage.userUUID, function(resp) {
              
              if(resp.Success != undefined){
              respMsg = "success";
              }
              else {
              respMsg = "fail";
              }
                           });
}