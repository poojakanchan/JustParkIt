package com.parkingapp.parser;

/**
 * Created by pooja on 5/11/2015.
 */
public class RatesBean {

    private String begTime;
    private String endTime;
    private String desc;
    private String rateQuantifier;
    private String rateRestriction;
    private double rate;

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getBegTime() {
        return begTime;
    }

    public void setBegTime(String begTime) {
        this.begTime = begTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRateQuantifier() {
        return rateQuantifier;
    }

    public void setRateQuantifier(String rateQuantifier) {
        this.rateQuantifier = rateQuantifier;
    }

    public String getRateRestriction() {
        return rateRestriction;
    }

    public void setRateRestriction(String rateRestriction) {
        this.rateRestriction = rateRestriction;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
