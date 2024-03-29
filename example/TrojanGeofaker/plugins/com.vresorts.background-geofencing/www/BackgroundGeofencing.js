var exec = require("cordova/exec");
module.exports = {
    getCurrentLocation: function(succuss, failure){
    	 exec(success || function(coordinates) {},
                 failure || function() {},
                 'BackgroundGeofencing',
                 'getCurrentLocation',
                 []
          );
    },
    
    disablePlace: function(succuss, failure, placeUuid){
    	 exec(success || function() {},
                 failure || function() {},
                 'BackgroundGeofencing',
                 'disablePlace',
                 [placeUuid]
            );
    },
    
    enablePlace: function(success, failture, placeUuid){
    	 exec(success || function() {},
                 failure || function() {},
                 'BackgroundGeofencing',
                 'enablePlace',
                 [placeUuid]
            );
    },
    
    reconfigure: function(success, failture, tripplan){
    	 exec(success || function() {},
                 failure || function() {},
                 'BackgroundGeofencing',
                 'reconfigure',
                 [tripplan]
            );
    },
    
    addPlace: function(success, failture, place){
    	 exec(success || function() {},
                 failure || function() {},
                 'BackgroundGeofencing',
                 'addPlace',
                 [place]
            );
    },
    
    deletePlace: function(success, failture, placeUuid){
    	 exec(success || function() {},
                 failure || function() {},
                 'BackgroundGeofencing',
                 'deletePlace',
                 [placeUuid]
            );
    },

    configure: function(success, failure, tripplan) {
   
        exec(success || function() {},
             failure || function() {},
             'BackgroundGeofencing',
             'configure',
             [tripplan]
        );
    },
    
    start: function(success, failure) {
        exec(success || function() {},
             failure || function() {},
             'BackgroundGeofencing',
             'start',
             []);
    },
    stop: function(success, failure) {
        exec(success || function() {},
            failure || function() {},
            'BackgroundGeofencing',
            'stop',
            []);
    },
    
    mock: function(success, failure, coordinates){
   	 exec(success || function() {},
            failure || function() {},
            'BackgroundGeofencing',
            'mock',
            [coordinates]
       );
   },
   startMock: function(success, failure, coordinates){
   	 exec(success || function() {},
            failure || function() {},
            'BackgroundGeofencing',
            'startMock',
            []
       );
   },
   stopMock: function(success, failure, coordinates){
   	 exec(success || function() {},
            failure || function() {},
            'BackgroundGeofencing',
            'stopMock',
            []
       );
   },
   
};
