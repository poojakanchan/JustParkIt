package com.parkingapp.exception;

/**
 * Customized exception class for Parking App.
 *  Created by pooja on 4/13/2015.
 */
public class ParkingAppException extends Exception{
//   Please note that every exception should be rethrown as an instance of this class.
//   It makes exception handling efficient!

    Exception exception;
    String message;

    public ParkingAppException(Exception e, String message) {
        this.exception = e;
        this.message = message;
        System.out.println(toString());
    }

    public ParkingAppException(String message) {
        this.message = message;
        System.out.println(toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (message != null) {
            sb.append("Exception occurred: " + message);
        }
        sb.append(System.getProperty("line.separator"));
        if (exception != null) {
            sb.append(exception.getMessage());
            sb.append(exception.getStackTrace());
        }
        return sb.toString();
    }
}