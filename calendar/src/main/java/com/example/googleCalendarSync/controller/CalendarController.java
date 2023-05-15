package com.example.googleCalendarSync.controller;

import com.example.googleCalendarSync.request.CalendarEventModel;
import com.example.googleCalendarSync.service.CalendarEventService;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class CalendarController {
    @Autowired()
    private CalendarEventService calendarEventService;


    //--------------基本的新刪修查行事曆-----------------
    /**
     * 取得Google日曆中的下一個10個事件。
     * */
    @GetMapping("/getEvents")
    public ResponseEntity getEvents() throws Exception {
        Events result = calendarEventService.getUserEvents();
        ResponseEntity  resp = result == null? ResponseEntity.badRequest().body("can't find calendar list."): (ResponseEntity) ResponseEntity.ok(result);
        log.info("resp = {}",resp);
        return resp;
    }

    /**
     * 取得eventId
     * @param eventSummary - 行事曆上的事件名稱
     * */
    @GetMapping("/getEventId")
    public ResponseEntity getEventsId(@RequestParam String eventSummary ) throws Exception {
       String result = calendarEventService.getEventsId(eventSummary);
        ResponseEntity  resp = result == null? ResponseEntity.badRequest().body("can't find eventId."): (ResponseEntity) ResponseEntity.ok(result);
        log.info("resp = {}",resp);
        return resp;
    }

    /**
     * 新增行事曆一個新事件
     */
    @PostMapping("/addEvent")
    public ResponseEntity createEvent(@RequestBody(required = true) CalendarEventModel event) throws Exception {
        Event result = calendarEventService.createUserEvent(event);
        ResponseEntity  resp = result == null? ResponseEntity.badRequest().body("Failed to add calendar event."): (ResponseEntity) ResponseEntity.ok(result);
        log.info("resp = {}",resp);
        return resp;
    }
    // POST BODY sample
//    {
//        "summary":"TEST",
//            "location":"台北市",
//            "description":"測測試試",
//            "startDateTime":"2023-05-28T12:00:00+08:00",
//            "endDateTime":"2023-05-28T13:00:00+08:00"
//    }
    /**
     * 更新行事曆一個新事件
     * */
    @PutMapping("/updateEvent/{eventId}")
    public ResponseEntity updateEvent(@PathVariable String eventId, @RequestBody(required = true) CalendarEventModel event) throws Exception {
        Event response = calendarEventService.updateUserEvent(eventId, event);
        ResponseEntity  resp = response == null? ResponseEntity.badRequest().body("Failed to update calendar event."): (ResponseEntity) ResponseEntity.ok(response);
        log.info("resp = {}",resp);
        return resp;
    }
    /**
     * 刪除現有事件
     *
     * */
    @DeleteMapping("/deleteEvent/{eventId}")
    public ResponseEntity deleteEvent(@PathVariable String eventId) throws Exception {
        log.info("id={}",eventId);
        ResponseEntity result = calendarEventService.deleteUserEvent(eventId);
        log.info("resp = {}",result);
        return result;
    }



    //-------------基本的新刪修查行事曆-----------------


}
