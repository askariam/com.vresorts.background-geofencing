var mobilePlugin = {
		platform: "android"
};

mobilePlugin.onUserAccountInfoChanged =  function (){
	//invoke android plugins By Kan
    var accountInformation = {
    		uuid:USER_UUID,
    		username:"",
    		account_type:"user"
    };

    if (window.AndroidClient) {
    	   AndroidClient.onUserLoggedInCallBack(JSON.stringify(accountInformation));
    	}
};

	mobilePlugin.onTripplanSwitched = function(tripPlanUUID){
		//invoke android plugins by Kan
        if (window.AndroidClient) {
     	   AndroidClient.onTripPlanSwitchedCallBack(tripPlanUUID);
     	}
        
	};
	
	mobilePlugin.onUserLogout = function(){
		if (window.AndroidClient) {
		 	   AndroidClient.onUserLoggedOutCallBack();
		 	}
	};
	
	mobilePlugin.onPlacesSubscriptionStatusChanged = function(placeUUID, is_subscribed){
	
	//invoke android plugins by Kan
    if (window.AndroidClient) {
 	   AndroidClient.onPlacesSubscriptionStatusChangedCallBack(placeUUID, is_subscribed);
    }
	};

window.mobilePlugin = mobilePlugin;