package com.example.googleCalendarSync.service;

import com.example.googleCalendarSync.request.CalendarEventModel;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class GoogleCalendarService {

    /**
     * <p>JSON 轉換工廠。</p>
     * 用來解析 Google API 回傳的 JSON 格式資料。
     */
    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    /**
     * Google Calendar API 的範圍，用來取得驗證授權。
     */
    private final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    /**
     * <p> 創建HTTP傳輸對象 :此物件是用來處理 HTTP 請求的。</p>
     * 在建立 Calendar 物件時，需要傳入 httpTransport 參數，這樣才能透過 Google Calendar API 進行日曆事件的同步。
     */
    private final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    @Value("${calendar.config.applicationName}")
    private String applicationName;
    @Value("${calendar.config.calendarId}")
    private String calendarId;
    @Value("${calendar.config.timeZone}")
    private String applicationTimeZone;
    /**
     * 取得服務帳戶憑證
     * */
    @Value("${path.client}")
    private String CREDENTIALS_FILE_PATH;

    public GoogleCalendarService( ) throws GeneralSecurityException, IOException
    { }

    /**
     * <h2>使用憑證、HTTP 通訊協定和 JSON 工廠建立 Calendar 服務</h2>
     * Initialize Calendar service with valid OAuth credentials
     * @param httpTransport
     * @param jsonFactory
     * @return
     */
    private Calendar buildGoogleCalServise(HttpTransport httpTransport, JsonFactory jsonFactory) throws Exception {

        // 建立憑證
        GoogleCredential client = GoogleCredential.fromStream(new FileInputStream(CREDENTIALS_FILE_PATH))
                .createScoped(CalendarScopes.all());

        return new Calendar.Builder(httpTransport, jsonFactory, client)
                .setApplicationName(applicationName).build();
    }

    /**
     *  獲取事件列表
     * */
    public Events getEvents() throws Exception {

        // 調用API獲取事件列表
        Events events = buildGoogleCalServise(httpTransport,JSON_FACTORY).events()
                .list(calendarId)
                .setMaxResults(10)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setPageToken(null)
                .execute();
        // 返回事件列表
        return events;
    }

    /**
     * 認證用戶端並返回憑據
     * */
    public Credential authorize() throws IOException, GeneralSecurityException {

        // 讀取憑證檔案
        ClassPathResource resource = new ClassPathResource(CREDENTIALS_FILE_PATH);
        // 載入OAuth 2.0用戶端ID取得的JSON檔案憑據文件 。
        InputStream in = resource.getInputStream();
        // 加載客戶端密鑰
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // 創建GoogleAuthorizationCodeFlow對象，授權流程
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .build();


        // 執行授權流程
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    /**
     * 新增事件
     * */
    public Event createEvent(CalendarEventModel request) throws Exception {
        log.info("request = {}" , request);
        // 設置事件開始時間和結束時間
        DateTime startDateTime = request.getStartDateTime();
        DateTime endDateTime = request.getEndDateTime();

        Event event = new Event().setSummary(request.getSummary()).setDescription(request.getDescription()).setLocation(request.getLocation());
        //調整開始時間之時區
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(applicationTimeZone);
         event.setStart(start);
         log.info("eventStartTime = {}",event.getStart());
        // 設定事件的結束時間
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(applicationTimeZone);
                event.setEnd(end);
        log.info("eventEndTime = {}",event.getEnd());
        //設定重複時間
//        String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=2"};
//        event.setRecurrence(Arrays.asList(recurrence));
        // 設定事件的提醒方式
        // 待測試
        EventReminder[] reminderOverrides = new EventReminder[]{
                new EventReminder().setMethod("email").setMinutes(24 * 60),
                new EventReminder().setMethod("popup").setMinutes(10),
        };
        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        // 創建事件
        event.setReminders(reminders);
        // CALL GOOGLE API
        event = buildGoogleCalServise(httpTransport,JSON_FACTORY).events().insert(calendarId, event).execute();
        log.info("日曆新增事件 = {}",event.getHtmlLink());
        log.info("日曆新增事件 = {}",event);

        /*
         * <h2>關於primary</h2>
         * "primary"`是指要將事件添加到哪個日曆中。在Google Calendar中，每個用戶都可以創建多個日曆，並將事件添加到這些日曆中。
         * 當您使用Google Calendar API時，需要指定要將事件添加到哪個日曆中。
         *  我們將事件添加到名為 "primary" 的日曆中。這是因為在Google Calendar中，每個用戶都有一個名為 "primary" 的日曆，這是默認的日曆。
         *  如果您沒有指定要將事件添加到哪個日曆中，則默認會將事件添加到 "primary"日曆中。
         * 希望將事件添加到其他日曆中，則需要將"primary"替換為該日曆的ID。您可以使用CalendarList API來獲取用戶的日曆列表，然後從中選擇要添加事件的日曆。
         * */

        // 調用API創建事件
        return event;
    }
    /**
     * 更新事件
     * */
    public Event updateEvent(String eventId,CalendarEventModel request) throws Exception {

        Calendar service = buildGoogleCalServise(httpTransport,JSON_FACTORY);
        //利用calendarId找出要更新的日曆

        //取得calendarId
        Event event = service.events().get(calendarId, eventId).execute();

        log.info("利用 calendarId = {} ，primary = {} 找出的calendar日曆明細 = {}",calendarId,service.calendars().get(calendarId),event);
        //-----塞入要更新的內容 START--------------
        event.setLocation(request.getLocation());
        event.setSummary(request.getSummary());
        event.setDescription(request.getDescription());
        EventDateTime start = new EventDateTime()
                .setDateTime(request.getStartDateTime())
                .setTimeZone(applicationTimeZone);
        event.setStart(start);
        log.info("eventStartTime = {}",event.getStart());
        EventDateTime end = new EventDateTime()
                .setDateTime(request.getEndDateTime())
                .setTimeZone(applicationTimeZone);
        event.setEnd(end);
        log.info("eventEndTime = {}",event.getEnd());
        event.setDescription(request.getDescription());
        //-----塞入要更新的內容 END--------------
        // 更新事件
        Event updatedEvent = service.events().update(calendarId, event.getId(), event).execute();
        log.info("更新後內容 = {}",updatedEvent);
        return updatedEvent;
    }

    /**
     * 刪除事件
     * @param eventId
     * */
    public ResponseEntity deleteEvent(String eventId) throws Exception {
        // 調用API刪除事件
        Calendar service = buildGoogleCalServise(httpTransport,JSON_FACTORY);
        Boolean result = false;
        String message ="error";
        String errorContent ="";
        result =  isEventExists(eventId);
        if(result){
            try {
                service.events().delete(calendarId, eventId).execute();
                return ResponseEntity.ok("刪除eventId ="+eventId +"資料成功");
            } catch (GoogleJsonResponseException e) {
                Gson gson = new Gson();
                errorContent = e.getContent();
                JsonObject json = JsonParser.parseString(errorContent).getAsJsonObject();
                message = json.get("message").getAsString();
                log.info("message = {}",message);
                // 處理錯誤情況
                log.error("Error Content: {} " + errorContent);
                return  ResponseEntity.status(200).body("刪除失敗原因 :"+message);
            }
        }
        return  ResponseEntity.status(200).body("刪除失敗原因 :"+message);
    }
    /**
     * <p>確認eventId是否存在</p>
     * @param eventId
     * */
    private Boolean isEventExists(String eventId) throws Exception {
        Calendar service = buildGoogleCalServise(httpTransport,JSON_FACTORY);
        try {
            Event event = service.events().get(calendarId, eventId).execute();
            log.info("{} :event ={} ",eventId,event);
            return (event != null);
        } catch (IOException e) {
            // 處理錯誤情況
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 取得事件 ID。
     * */
    public String getEventsId(String eventSummary) throws Exception {
        Calendar service = buildGoogleCalServise(httpTransport,JSON_FACTORY);
    // 取得日曆中的事件列表
        Events events = service.events().list(calendarId).execute();
        // 遍歷事件列表
        for (Event event : events.getItems()) {
            // 比對事件標題
            if (eventSummary.equals(event.getSummary())) {
                // 返回事件的 eventId
                log.info("id = {}",event.getId());
                return event.getId();
            }
        }
        return null;
    }
}