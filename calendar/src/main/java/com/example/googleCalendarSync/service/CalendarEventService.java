package com.example.googleCalendarSync.service;

import com.example.googleCalendarSync.request.CalendarEventModel;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface  CalendarEventService {

    Event createUserEvent(CalendarEventModel req) throws Exception;
    /**
     * 使用OAuth 2.0用戶端ID憑證的GoogleCalendarService實例查詢事件
     */
    Events getUserEvents() throws Exception;
    /**
     * 更新事件
     */
    Event updateUserEvent(String eventId, CalendarEventModel event) throws Exception;

    /**
     * 刪除事件
     */
    ResponseEntity deleteUserEvent(String eventId) throws Exception;

    String getEventsId(String eventSummary) throws Exception;
}
