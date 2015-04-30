package com.parkingapp.database;

/**
 * Created by nayanakamath on 4/26/15.
 */

public class StreetCleaningDataBean {

    private String WeekDay;
    private String RightLeft;
    private String Corridor;
    private String FromHour;
    private String ToHour;
    private String Holidays;
    private String Week1OfMonth;
    private String Week2OfMonth;
    private String Week3OfMonth;
    private String Week4OfMonth;
    private String Week5OfMonth;
    private int LF_FADD;
    private int LF_TOADD;
    private int RT_TOADD;
    private int RT_FADD;
    private String STREETNAME;
    private int ZIP_CODE;
    private String NHOOD;


    public String getRightLeft() {
        return RightLeft;
    }

    public void setRightLeft(String rightLeft) {
        RightLeft = rightLeft;
    }

    public String getNHOOD() {
        return NHOOD;
    }

    public void setNHOOD(String NHOOD) {
        this.NHOOD = NHOOD;
    }

    public int getZIP_CODE() {
        return ZIP_CODE;
    }

    public void setZIP_CODE(int ZIP_CODE) {
        this.ZIP_CODE = ZIP_CODE;
    }

    public int getRT_FADD() {
        return RT_FADD;
    }

    public void setRT_FADD(int RT_FADD) {
        this.RT_FADD = RT_FADD;
    }

    public String getSTREETNAME() {
        return STREETNAME;
    }

    public void setSTREETNAME(String STREETNAME) {
        this.STREETNAME = STREETNAME;
    }

    public int getRT_TOADD() {
        return RT_TOADD;
    }

    public void setRT_TOADD(int RT_TOADD) {
        this.RT_TOADD = RT_TOADD;
    }

    public int getLF_TOADD() {
        return LF_TOADD;
    }

    public void setLF_TOADD(int LF_TOADD) {
        this.LF_TOADD = LF_TOADD;
    }

    public int getLF_FADD() {
        return LF_FADD;
    }

    public void setLF_FADD(int LF_FADD) {
        this.LF_FADD = LF_FADD;
    }

    public String getWeek5OfMonth() {
        return Week5OfMonth;
    }

    public void setWeek5OfMonth(String week5OfMonth) {
        Week5OfMonth = week5OfMonth;
    }

    public String getWeek4OfMonth() {
        return Week4OfMonth;
    }

    public void setWeek4OfMonth(String week4OfMonth) {
        Week4OfMonth = week4OfMonth;
    }

    public String getWeek3OfMonth() {
        return Week3OfMonth;
    }

    public void setWeek3OfMonth(String week3OfMonth) {
        Week3OfMonth = week3OfMonth;
    }

    public String getWeek1OfMonth() {
        return Week1OfMonth;
    }

    public void setWeek1OfMonth(String week1OfMonth) {
        Week1OfMonth = week1OfMonth;
    }

    public String getWeek2OfMonth() {
        return Week2OfMonth;
    }

    public void setWeek2OfMonth(String week2OfMonth) {
        Week2OfMonth = week2OfMonth;
    }

    public String getHolidays() {
        return Holidays;
    }

    public void setHolidays(String holidays) {
        Holidays = holidays;
    }

    public String getToHour() {
        return ToHour;
    }

    public void setToHour(String toHour) {
        ToHour = toHour;
    }

    public String getFromHour() {
        return FromHour;
    }

    public void setFromHour(String fromHour) {
        FromHour = fromHour;
    }

    public String getCorridor() {
        return Corridor;
    }

    public void setCorridor(String corridor) {
        Corridor = corridor;
    }

    public String getWeekDay() {
        return WeekDay;
    }

    public void setWeekDay(String weekDay) {
        WeekDay = weekDay;
    }


}