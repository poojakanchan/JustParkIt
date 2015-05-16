package com.parkingapp.exception;

/**
 * Customized exception class for the App.
 *  Created by pooja on 4/13/2015.
 */
public class ParkingAppException extends Exception{
//   Please note that every exception should be rethrown as an instance of this class.
//   It makes exception handling efficient!

    Exception exception;
    String message;

    /**
     * constructor of ParkingAppException class.
     * @param e exception to be thrown
     * @param message error message to be displayed
     */
    public ParkingAppException(Exception e, String message) {
        this.exception = e;
        this.message = message;
        System.out.println(toString());
    }

    /**
     * constructor of ParkingAppException class.
     * @param message error message to be displayed
     */
    public ParkingAppException(String message) {
        this.message = message;
        System.out.println(toString());
    }

    /**
     * overridden method to display exception trace and error message
     * @return string with appropriate exception message consisting of error message and exception trace.
     */
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