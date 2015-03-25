//
//$(document).ready(function() {
//    $("#popup_offer_details").popup();
//});

function offerDetailsClicked(uuid) {
    
    $("#div_view_offer_detail").empty();
    $("#div_view_offer_title").empty();
    $("#div_view_offer_img").empty();
    
    // Nested function definition for the success callback that goes to readOffer().
    $.getJSON("http://xixixhalu-test.apigee.net/proxy/tripPlanner/getOffer?offer_uuid="+ uuid, function(offer){

              $("#div_view_offer_title").append("<center>" + offer.store_name + "</center>");
              
              $("#div_view_offer_img").append(
                                              "<center><img width='40%' height='40%' src='data:image/jpeg;base64,"+ offer.coupon.image_data+"'/></center>");
              
              $("#div_view_offer_detail").append(
                                                 "<table style='padding:0px 0px'>"
                                                 + "<col width='22px' height='15px'><col height='15px'>"
                                                 + "<tr>"
                                                 + "<td><a class='ui-btn ui-corner-all ui-icon-location ui-btn-icon-notext' style='padding:0;border:none;background:none;'></a></td>"
                                                 + "<td rowspan='2'>" + offer.store_address + "</td>"
                                                 + "</tr>"
                                                 + "<tr><td></td></tr>"
                                                 + "<tr>"
                                                 + "<td><a class='ui-btn ui-corner-all ui-icon-phone ui-btn-icon-notext' style='padding:0;border:none;background:none;'></a></td>"
                                                 + "<td rowspan='2'>" + offer.store_phone + "</td>"
                                                 + "</tr>"
                                                 + "<tr><td></td></tr>"
                                                 + "<tr>"
                                                 + "<td><a class='ui-btn ui-corner-all ui-icon-info ui-btn-icon-notext' style='padding:0;border:none;background:none;'></a></td>"
                                                 + "<td rowspan='2'>" + offer.long_desc + "</td>"
                                                 + "</tr>"
                                                 + "<tr><td></td></tr>"
                                                 + "<tr>"
                                                 + "<td></td>"
                                                 + "<td>" + offer.terms + "</td>"
                                                 + "</tr>"
                                                 + "<tr>"
                                                 + "<td><a class='ui-btn ui-corner-all ui-icon-clock ui-btn-icon-notext' style='padding:0;border:none;background:none;'></a></td>"
                                                 + "<td>" + offer.start_date +" - " + offer.end_date +"</td>"
                                                 + "</tr>"
                                                 + "</table>"
                                                 + "<br>"
                                                 );
            
                $.mobile.changePage("#page_view_offer_details");
              
              });

}

