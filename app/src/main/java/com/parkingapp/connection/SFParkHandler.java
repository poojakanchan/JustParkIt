package com.parkingapp.connection;

import com.parkingapp.exception.ParkingAppException;
import com.parkingapp.parser.SFParkBean;
import com.parkingapp.utility.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pooja on 4/13/2015.
 *  Class to handle SF PARK APIs
 */
public class SFParkHandler extends RESTConnectionHandler{

    /**
     * calls SF API availability service to check  available parking spots within the given radius of the given location.
     * @param latitude latitude of the search location
     * @param longitude longitude of the search location
     * @param radius radius for the parking spot search
     * @return response of availability service API
     * @throws ParkingAppException
     */
    public List<SFParkBean> callAvailabilityService(String latitude, String longitude, String radius) throws ParkingAppException{

        List<String> parameters = new ArrayList<String>();
        parameters.add("lat=" + latitude);
        parameters.add("long=" + longitude);
        parameters.add("radius=" + radius);
        parameters.add("uom=mile");
        parameters.add("method=availability");
        parameters.add("response=xml");
        parameters.add("pricing=yes");

        String url = generateURL(Constants.SF_PARK_URI + Constants.SF_PARK_AVAILABILITY_SERVICE, parameters);
        return connect(url);
        //StringBuilder response =  restHandler.connect(url);
        //return response;
    }

}
