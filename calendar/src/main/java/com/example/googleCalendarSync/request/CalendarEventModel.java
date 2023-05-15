package com.example.googleCalendarSync.request;

import com.google.api.client.util.DateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalendarEventModel {

    private String id;
    /**
     *  事件的簡述或標題。
     * */
    private String summary;
    /**
     * 事件的詳細說明。
     * */
    private String description;
    /**
     * <p>事件開始時間</p>
     * 格式範例 : 2023-05-10T09:00:00+08:00
     * */
    private DateTime startDateTime;

    /**
     * <p>事件結束時間</p>
     * 格式範例 : 2023-05-10T09:00:00+08:00
     * */
    private DateTime endDateTime;
    /**
     * 地點
     * */
    private String location;
}
