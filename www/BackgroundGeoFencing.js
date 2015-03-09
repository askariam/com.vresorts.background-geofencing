var exec = require("cordova/exec");
module.exports = {
    getCurrentLocation: function(succuss, failure){
    	 exec(success || function(coordinates) {},
                 failure || function() {},
                 'BackgroundGeoFencing',
                 'getCurrentLocation',
                 []
          );
    },
    
    disablePlace: function(succuss, failure, placeUuid){
    	 exec(success || function() {},
                 failure || function() {},
                 'BackgroundGeoFencing',
                 'disablePlace',
                 [placeUuid]
            );
    },
    
    enablePlace: function(success, failture, placeUuid){
    	 exec(success || function() {},
                 failure || function() {},
                 'BackgroundGeoFencing',
                 'enablePlace',
                 [placeUuid]
            );
    },
    
    reconfigure: function(success, failture, tripplan){
    	 exec(success || function() {},
                 failure || function() {},
                 'BackgroundGeoFencing',
                 'reconfigure',
                 [tripplan]
            );
    },
    
    addPlace: function(success, failture, place){
    	 exec(success || function() {},
                 failure || function() {},
                 'BackgroundGeoFencing',
                 'addPlace',
                 [place]
            );
    },
    
    deletePlace: function(success, failture, placeUuid){
    	 exec(success || function() {},
                 failure || function() {},
                 'BackgroundGeoFencing',
                 'deletePlace',
                 [placeUuid]
            );
    },

    configure: function(success, failure, tripplan) {
   
        exec(success || function() {},
             failure || function() {},
             'BackgroundGeoFencing',
             'configure',
             [tripplan]
        );
    },
    
    start: function(success, failure) {
        exec(success || function() {},
             failure || function() {},
             'BackgroundGeoFencing',
             'start',
             []);
    },
    stop: function(success, failure) {
        exec(success || function() {},
            failure || function() {},
            'BackgroundGeoFencing',
            'stop',
            []);
    },
    
    mock: function(success, failure, coordinates){
   	 exec(success || function() {},
            failure || function() {},
            'BackgroundGeoFencing',
            'mock',
            [coordinates]
       );
   },
   mock_start: function(success, failure, coordinates){
   	 exec(success || function() {},
            failure || function() {},
            'BackgroundGeoFencing',
            'mock_start',
            []
       );
   },
   mock_stop: function(success, failure, coordinates){
   	 exec(success || function() {},
            failure || function() {},
            'BackgroundGeoFencing',
            'mock_stop',
            []
       );
   },
   
};
