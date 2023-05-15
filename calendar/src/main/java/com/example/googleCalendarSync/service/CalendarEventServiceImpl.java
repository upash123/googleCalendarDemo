package com.example.googleCalendarSync.service;

import com.example.googleCalendarSync.request.CalendarEventModel;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class CalendarEventServiceImpl implements CalendarEventService {

    @Value("${calendar.config.calendarId}")
    private String calendarId;
    @Value("${calendar.config.timeZone}")
    private String applicationTimeZone;

    /**
     * 使用服務帳戶憑證的GoogleCalendarService實例
     * */
    @Autowired
    private  GoogleCalendarService googleCalendarService;

    @Override
    public Event createUserEvent(CalendarEventModel req) throws Exception {

        return googleCalendarService.createEvent(req);
    }

    @Override
    public Events getUserEvents() throws Exception {

        return googleCalendarService.getEvents();
    }

    @Override
    public Event updateUserEvent(String eventId, CalendarEventModel event) throws Exception {
        return googleCalendarService.updateEvent(eventId,event);
    }

    @Override
    public ResponseEntity deleteUserEvent(String eventId) throws Exception {

           return googleCalendarService.deleteEvent(eventId);
    }

    @Override
    public String getEventsId(String eventSummary) throws Exception {
        return googleCalendarService.getEventsId(eventSummary);
    }


}
