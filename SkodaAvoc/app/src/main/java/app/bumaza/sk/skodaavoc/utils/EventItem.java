package app.bumaza.sk.skodaavoc.utils;

import java.util.List;

/**
 * Created by Budy on 7.4.18.
 */

public class EventItem {

    public String title;
    public List<String> attendances;

    public EventItem(String title, List<String> attendances) {
        this.title = title;
        this.attendances = attendances;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAttendances() {
        return attendances;
    }

    public void setAttendances(List<String> attendances) {
        this.attendances = attendances;
    }
}
