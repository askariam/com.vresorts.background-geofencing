

//Local notification for app running on foreground and background

function addNotification(offerid, storename){
                                                   
    var num = offerid.match(/\d/g); // replace all leading non-digits with nothing
    num = num.join("");
    var subnum = num.substring(0,9); // select first 9 digits as notification id because Android takes only Integer as id
    
    window.plugin.notification.local.add({
                                         id:         subnum, // is converted to a string
                                         title:      storename + ' has a new offer!',
                                         message:    'Click here to view the offer',
                                         json:       JSON.stringify({ offer: offerid, name: storename }),
                                         autoCancel: true // set to cancel notification after click automatically
                                         //badge:      1,
                                         //foreground: 'foreground',
                                         //background: 'background'
                                         });
    
    window.plugin.notification.local.onclick = function (subnum, state, json) {
        passwordClicked(JSON.parse(json).offer, JSON.parse(json).name);
   
    };
    

    
 
                
}


    