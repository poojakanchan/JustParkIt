package com.parkingapp.parser;

import java.util.List;

/**
 * Bean class to store parking locations returned by SFPark availability service.
 * Getters and setters are provided for each field in the class.
 * Created by pooja on 4/17/2015.
 */
public class SFParkBean {

    private String type;
    private String name;
    private String address;
    private String contactNumber;
    private List<OperationHoursBean> operationHours;
    private double latitude;
    private double longitude;
    private int occ;
    private int oper;
    private List<RatesBean> ratesBeanList;

    public List<RatesBean> getRatesBeanList() {
        return ratesBeanList;
    }

    public void setRatesBeanList(List<RatesBean> ratesBeanList) {
        this.ratesBeanList = ratesBeanList;
    }

    public int getOper() {
        return oper;
    }

    public void setOper(int oper) {
        this.oper = oper;
    }

    public int getOcc() {
        return occ;
    }

    public void setOcc(int occ) {
        this.occ = occ;
    }


    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name ;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<OperationHoursBean> getOperationHours() {
        return operationHours;
    }

    public void setOperationHours(List<OperationHoursBean> operationHours) {
        this.operationHours = operationHours;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }



}
