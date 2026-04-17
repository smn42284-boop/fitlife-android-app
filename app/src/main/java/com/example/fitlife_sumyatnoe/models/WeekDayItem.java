package com.example.fitlife_sumyatnoe.models;

import java.util.Calendar;

public class WeekDayItem {
    public String dayName;
    public String date;
    public String fullDate;
    public Calendar calendar;

    public WeekDayItem(String dayName, String date, String fullDate, Calendar calendar) {
        this.dayName = dayName;
        this.date = date;
        this.fullDate = fullDate;
        this.calendar = calendar;
    }
}