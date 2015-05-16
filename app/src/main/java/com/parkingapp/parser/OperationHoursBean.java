package com.parkingapp.parser;

/**
 * Bean class to store operation hours of paring location returned by SFParkAPi availability service.
 * The getters and setters are provided for each field.
 * Created by pooja on 4/17/2015.
 */
public class OperationHoursBean {

    private String fromDay;
    private String toDay;
    private String startTime;
    private String endTime;

    public String getFromDay() {
        return fromDay;
    }

    public void setFromDay(String fromDay) {
        this.fromDay = fromDay;
    }

    public String getToDay() {
        return toDay;
    }

    public void setToDay(String toDay) {
        this.toDay = toDay;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

}
