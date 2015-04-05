//Bowen: Waiting for test
var calendar_timeFormat = "d MMMM, yyyy, h:mm tt";
var clickedEvent;

$(document).ready(function() {
    $('#popup_create_event').popup();
    $('#popup_event_detail').popup();
    
    $('#input_create_event_startdate').pickadate({
        today: 'Today',
        clear: 'Clear',
        close: 'Close',
        weekdaysShort: ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'],
        showMonthsShort: true
    });
    $('#input_create_event_enddate').pickadate({
        today: 'Today',
        clear: 'Clear',
        close: 'Close',
        weekdaysShort: ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'],
        showMonthsShort: true
    });
    $('#input_create_event_starttime').pickatime({
        clear: 'Clear',
        klass: {
            selected: '',
            highlighted: '',
            now: ''
        }
    });
    $('#input_create_event_endtime').pickatime({
        clear: 'Clear',
        klass: {
            selected: '',
            highlighted: '',
            now: ''
        }
    });
    
    $('#button_cancel_create_event').on("click", function() {
        $('#popup_create_event').popup("close");
    });
    
    $('#button_close_event_detail').on("click", function() {
        $('#popup_event_detail').popup("close");
    });
    
    $('#button_delete_event').on("click", function(){
        var r = confirm("Are you sure to delete this event?");
        if (r == true) {
            TripCalendarController.deleteEvent(clickedEvent);
        }
    });
    
    $('#button_reserve_event').on("click", function() {
        alert("This feature is under development.");
    });
});

function TripCalendarController() {
      
}

TripCalendarController.scheduleClicked = function(place)
{   
    $('#input_create_event_title').val(place.place_name);
    $('#input_create_event_startdate').val('');
    $('#input_create_event_enddate').val('');
    $('#input_create_event_starttime').val('');
    $('#input_create_event_endtime').val('');
    $('#button_create_event').unbind("click");
    $('#button_create_event').on("click", function() {
        if($('#input_create_event_title').val() == ''
           || $('#input_create_event_startdate').val() == ''
           || $('#input_create_event_starttime').val() == ''
           || $('#input_create_event_enddate').val() == ''
           || $('#input_create_event_endtime').val() == '')
        {
            alert('All fields are mandatory');
            return;
        };

        var event = {
            'event_start_time': $('#input_create_event_startdate').val() + ', ' + $('#input_create_event_starttime').val(),
            'event_end_time': $('#input_create_event_enddate').val() + ', ' + $('#input_create_event_endtime').val(),
            'event_title': $('#input_create_event_title').val(),
            'trip_plan_uuid': place.trip_plan_uuid,
            'place_uuid': place.uuid,
            'user_uuid': USER_UUID
        }
        
        if(Date.compare(new Date(event.event_start_time), new Date(event.event_end_time)) == 1)
        {
            alert('Wrong Input.');
            return;
        }
        
        $('#popup_create_event').popup("close");
        $('#button_create_event').unbind("click");
        TripCalendarController.createEvent(event);
    });
    
    $("#popup_create_event").popup("open");
};

TripCalendarController.getTripEvents = function() {
    TripCalendar.readMultipleEvents("trip_plan_uuid", window.globalID.tripPlanuuid, function(eventSet) {
        TripCalendarController.displayEvents(eventSet);
    });
};

TripCalendarController.displayEvents = function(eventSet){
    var events = new Array;
    for(var i = 0; i < eventSet.length; i++)
    {
        events.push({'id': eventSet[i].uuid, 
                     'start': new Date(eventSet[i].event_start_time),
                    'end': new Date(eventSet[i].event_end_time), 
                    'title': eventSet[i].event_title});
    }
    $('#div_calendar_container').fullCalendar( 'destroy' );
    $('#div_calendar_container').fullCalendar({
        header: {
            left: 'today',
            center: 'prev title next',
            right: 'month,basicWeek,basicDay'
        },
        dayClick: function(date, jsEvent, view) {
            $('#div_calendar_container').fullCalendar( 'gotoDate', date );
            $('#div_calendar_container').fullCalendar( 'changeView', 'agendaDay' );
        },
        eventClick: function(calEvent, jsEvent, view) {
            $('#span_event_detail_title').html(calEvent.title);
            $('#span_event_detail_startdate').html(new Date(calEvent.start).toString(calendar_timeFormat));
            $('#span_event_detail_enddate').html(new Date(calEvent.end).toString(calendar_timeFormat));
            clickedEvent = calEvent;
            $('#popup_event_detail').popup("open");
        },
        defaultView: 'basicDay',
        weekMode: 'variable',
        timeFormat: 'h(:mm)T ',
        defaultDate: Date.today(),
        editable: false,
        allDaySlot: false,
        events: events
    });
    
    if(this.getParaValue('agendaDay') == 'true')
    {
        var date = unescape(this.getParaValue('date'));
        $('#div_calendar_container').fullCalendar( 'gotoDate', date );
        $('#div_calendar_container').fullCalendar( 'changeView', 'agendaDay' );
    }
};

TripCalendarController.createEvent = function(event) {
    TripCalendar.createEvent(event, function(){
        window.location.href = '#page_display_trip_calendar?agendaDay=true&date='+new Date(event.event_start_time);
        alert('Success'); 
    }, function(){
        alert('Failed');                    
    });
};

TripCalendarController.deleteEvent = function(event) {
    TripCalendar.deleteEvent(event.id, function(){
        $('#div_calendar_container').fullCalendar( 'removeEvents', event.id);
            $('#popup_event_detail').popup("close");
            alert('Success');
    }, function(){
        alert('Failed');                  
    });
};

TripCalendarController.getParaValue = function(paraName)
{ 
    var url = document.location.href; 
    var arrStr = url.substring(url.indexOf("?")+1).split("&"); 
    //return arrStr; 
    for(var i =0;i<arrStr.length;i++) 
    { 
        var loc = arrStr[i].indexOf(paraName+"="); 
        if(loc != -1) 
        { 
            return arrStr[i].replace(paraName+"=","").replace("?","");
        } 
    } 
    return ""; 
};
