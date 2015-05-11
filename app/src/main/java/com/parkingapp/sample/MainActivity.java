package com.parkingapp.sample;
/*
 * File history
 * 1. Raymond Thai
 * changes: added onlocationchangelistener to setupmaps method which changes map camera to user's current location
 *          changed information snippet to parking spot snippet
 *          added personal icons
 *          implemented pooja's fix to delete previous marker when new marker is selected
 *          implemented Clear Marker button so user can clear all markers on map
 *          Fixed radio buttons on layers and help menu in action overflow so that checked tab corresponds to current state
 *
 * 2. Pooja K
 * changes: Added a code to handle add to favorites and iew favorites part.
 *          Added a code to check whether street cleaning is currently going on or not and display message accordingly.
 *
 * 3. Pooja K
 *  changes: merged the codes of street cleaning and SFPark APIs.
 *           added a code to display markers for parking locations returned by SFParkAPI.
 */

//import android.app.AlertDialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.location.Address;
import android.content.ContextWrapper;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pooja.sfparksample.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parkingapp.connection.SFParkHandler;
import com.parkingapp.database.DBConnectionHandler;
import com.parkingapp.exception.ParkingAppException;
import com.parkingapp.parser.OperationHoursBean;
import com.parkingapp.parser.SFParkBean;
import com.parkingapp.database.StreetCleaningDataBean;
import com.parkingapp.utility.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

import android.util.Log;

public class MainActivity extends ActionBarActivity implements
        LocationListener, ConnectionCallbacks, OnConnectionFailedListener {

    private static final String TAG = "LocationActivity";
    private static GoogleMap mMap;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private String information;
    private String streetCleaningInformation;
    private GoogleApiClient mGoogleApiClient;
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    MarkerOptions marker;
    List<SFParkBean> SfParkBeanList = null;
    DBConnectionHandler dbConnectionHandler;
    String radius = "0.1";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkPlayServices()) {
            buildGoogleApiClient();
        }
        checkGPSStatus();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        SfParkBeanList = new ArrayList<SFParkBean>();
        setContentView(R.layout.activity_map);
        ContextWrapper contextWrapper = new ContextWrapper(getBaseContext());
        dbConnectionHandler = DBConnectionHandler.getDBHandler(contextWrapper);
        setUpMapIfNeeded();

        mMap.setMyLocationEnabled(true);
        mMap.getMyLocation();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // setup default location onMap load event

        double lat = 37.721897;
        double lng = -122.47820939999997;
        LatLng coordinate = new LatLng(lat, lng);
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(37.721897, -122.47820939999997));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

    }

  /* This method checks if the user has GPS and Network Services enabled.
  If the locations services aren't enabled, this method redirects the user to the phone's settings
   */

    private void checkGPSStatus() {
        LocationManager locationManager = null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if ( locationManager == null ) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex){}
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex){}
        if ( !gps_enabled && !network_enabled ){
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this,R.style.DialogTheme);


//Pops up a dialog box if location services are not enabled
            dialog.setTitle("GPS Not Enabled");
            dialog.setMessage("Please turn on GPS for proper functionality. \nGo to Settings -> Location -> Turn Location ON or press OK to take you there");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //this will navigate user to the device location settings screen
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            AlertDialog alert = dialog.create();
            alert.show();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Google Play services are not available", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
            setUpMapIfNeeded();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            private Location mLocation = null;

            @Override
            public void onMyLocationChange(Location myLocation) {

                if (mLocation == null) {
                    mLocation = myLocation;
                    mMap.clear();
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 16);
                    mMap.animateCamera(update);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.721897, -122.47820939999997), 14.0f));
                }
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                       @Override
                                       public void onMapClick(LatLng latLng) {
                                           // empty parking location list
                                           SfParkBeanList.clear();
                                           // retrieve and display street cleaning and parking information
                                           setStreetCleaningAndParkingInformation(latLng);
                                       }
                                   }
        );


        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                // send the latlng to updateMarkerPosition().
                updateMarkerPosition(marker.getPosition());
            }
        });
    }

    private void setStreetCleaningAndParkingInformation(LatLng latLng) {
       //note: parking API is called inside addMarker method.
        ContextWrapper contextWrapper = new ContextWrapper(getBaseContext());
        Geocoder geocoder = new Geocoder(getApplicationContext());
        geocoder.isPresent();
        String addressText;
        List<Address> matches = null;
        ArrayList<StreetCleaningDataBean> StreetCleanAddress = new ArrayList<>();
        try {
            matches = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean found = true;
        List<StreetCleaningDataBean> streetCleanAddress = new ArrayList<StreetCleaningDataBean>();

        String[] text = new String[8];
        String[] side = new String[8];
        StringBuilder rightFrom = new StringBuilder();
        StringBuilder rightTo = new StringBuilder();
        StringBuilder leftFrom = new StringBuilder();
        StringBuilder leftTo = new StringBuilder();

        text[0] = ("Street cleaning data not available");
        if (matches != null && matches.size() > 0) {
            Address address = matches.get(0);

            if (address.getSubThoroughfare() != null
                    && address.getThoroughfare() != null
                    && address.getPostalCode() != null) {

                String sub[] = address.getSubThoroughfare().split("-");
                int substreetParm = 0;
                String streetParm = null;
                int postalCode = 0;
                if (sub[0] != null) {
                    try {
                        substreetParm = Integer.valueOf(sub[0]);
                        streetParm = address.getThoroughfare().toUpperCase();
                        postalCode = Integer.valueOf(address.getPostalCode());
                        Log.d("substreetParm: ", address.getSubThoroughfare());
                        Log.d("substreetParm[0]: ", sub[0]);
                        Log.d("addressParm: ", streetParm);
                        Log.d("Pincode: ", address.getPostalCode());

                    } catch (NumberFormatException ne) {
                        found = false;
                    }

                    if (found) {
                        streetCleanAddress = dbConnectionHandler.getRequiredAddress(substreetParm, streetParm, postalCode);

                        if (streetCleanAddress != null && streetCleanAddress.size() > 0) {

                            int count = 1;
                            Calendar calendar = Calendar.getInstance();
                            int currDay = calendar.get(Calendar.DAY_OF_WEEK);
                            calendar.setMinimalDaysInFirstWeek(7);
                            int currWeek = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                            String currDayOfWeek = getDayOfWeek(currDay);

                            for (StreetCleaningDataBean bean : streetCleanAddress) {

                                StringBuilder sc = new StringBuilder();

                                if (bean.getRightLeft().equals("R")) {
                                    sc.append(String.valueOf(bean.getRT_FADD())).append("-")
                                            .append(String.valueOf(bean.getRT_TOADD())).append(" ")
                                            .append(bean.getSTREETNAME()).append("\n");
                                    side[count - 1] = ("R");
                                    rightFrom.append(String.valueOf(bean.getRT_FADD())).append(" ")
                                            .append(bean.getSTREETNAME()).append(" ")
                                            .append("San Francisco ")
                                            .append(bean.getZIP_CODE());
                                    rightTo.append(String.valueOf(bean.getRT_TOADD())).append(" ")
                                            .append(bean.getSTREETNAME()).append(" ")
                                            .append("San Francisco ")
                                            .append(bean.getZIP_CODE());
                                }
                                if (bean.getRightLeft().equals("L")) {
                                    sc.append(String.valueOf(bean.getLF_FADD())).append("-")
                                            .append(String.valueOf(bean.getLF_TOADD()))
                                            .append(" ").append(bean.getSTREETNAME()).append("\n");
                                    side[count - 1] = ("L");
                                    leftFrom.append(String.valueOf(bean.getLF_FADD())).append(" ")
                                            .append(bean.getSTREETNAME()).append(" ")
                                            .append("San Francisco ")
                                            .append(bean.getZIP_CODE());
                                    leftTo.append(String.valueOf(bean.getLF_TOADD())).append(" ")
                                            .append(bean.getSTREETNAME()).append(" ")
                                            .append("San Francisco ")
                                            .append(bean.getZIP_CODE());
                                }

                                sc.append(bean.getWeekDay() + " Weeks:");
                                List<Integer> weekList = new ArrayList<Integer>();

                                if (bean.getWeek1OfMonth().equals("Y")) {
                                    weekList.add(1);
                                    sc.append(" 1");
                                }
                                if (bean.getWeek2OfMonth().equals("Y")) {
                                    weekList.add(2);
                                    sc.append(" 2");
                                }
                                if (bean.getWeek3OfMonth().equals("Y")) {
                                    weekList.add(3);
                                    sc.append(" 3");
                                }
                                if (bean.getWeek4OfMonth().equals("Y")) {
                                    weekList.add(4);
                                    sc.append(" 4");
                                }
                                if (bean.getWeek5OfMonth().equals("Y")) {
                                    weekList.add(5);
                                    sc.append(" 5");
                                }
                                sc.append(" \n");


                                sc.append(bean.getFromHour() + "-" + bean.getToHour() + "\n");
                                if (bean.getHolidays().equals("N")) {
                                    sc.append("No cleaning on holidays \n");
                                }
                                // to check if street cleaning is going on currently
                                String currentInfo = "Street cleaning is not going on currently \n\n";
                                if (weekList.contains(currWeek) && currDayOfWeek != null && currDayOfWeek.equalsIgnoreCase(bean.getWeekDay())) {
                                    String fromString = calendar.get(Calendar.DATE) + ":" + (calendar.get(Calendar.MONTH) + 1) + ":" + calendar.get(Calendar.YEAR) + ":" + bean.getFromHour();
                                    String toString = calendar.get(Calendar.DATE) + ":" + (calendar.get(Calendar.MONTH) + 1) + ":" + calendar.get(Calendar.YEAR) + ":" + bean.getToHour();
                                    if (weekList.contains(currWeek) && currDayOfWeek != null && currDayOfWeek.equalsIgnoreCase("Mon")) {
                                        SimpleDateFormat parser = new SimpleDateFormat("dd:MM:yyyy:HH:mm");
                                        try {
                                            Date fromDate = parser.parse(fromString);
                                            Date toDate = parser.parse(toString);
                                            if (calendar.getTime().after(fromDate) && calendar.getTime().before(toDate)) {
                                                currentInfo = "Street cleaning is going on currently \n\n";
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                sc.append(currentInfo);

                                text[count - 1] = sc.toString();
                                Log.d("Data: ", text[count - 1]);
                                Log.d("Data: ", side[count - 1]);

                                count++;
                                if (count == 9) {
                                    break;
                                }
                            }

                        }
                    }
                }
                String title = getString(R.string.street_cleaning_info);
                Log.d("Right from  ", rightFrom.toString());
                Log.d("Right to  ", rightTo.toString());
                Log.d("Left from  ", leftFrom.toString());
                Log.d("Left to  ", leftTo.toString());
                // adds markers at the clicked location and parking locations
                addMarker(latLng, title, rightFrom.toString(), rightTo.toString(), leftFrom.toString(),
                        leftTo.toString(), text, side);
            }
        }
    }
    private List<Address> getGeoCoder(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext());
        geocoder.isPresent();
        List<Address> matches = null;
        try {
            matches = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matches;
    }

    private void drawLine(String rightFrom, String rightTo, String leftFrom, String leftTo) {
        Log.d("method:","Drawline method called");
        Geocoder geocoder = new Geocoder(getApplicationContext());
        geocoder.isPresent();
        try {
            List<Address> rightFromLatLngList = geocoder.getFromLocationName(rightFrom, 1);
            List<Address> rightToLatLngList = geocoder.getFromLocationName(rightTo, 1);
            List<Address> leftFromLatLngList = geocoder.getFromLocationName(leftFrom, 1);
            List<Address> leftToLatLngList = geocoder.getFromLocationName(leftTo, 1);

            if (rightFromLatLngList != null && leftToLatLngList.size() > 0
                    && rightToLatLngList != null && rightToLatLngList.size() > 0
                    && leftFromLatLngList != null && leftFromLatLngList.size() > 0
                    && leftToLatLngList != null && leftToLatLngList.size() > 0) {

                Address rightFromLatLng = rightFromLatLngList.get(0);
                Address rightToLatLng = rightToLatLngList.get(0);
                Address leftFromLatLng = leftFromLatLngList.get(0);
                Address leftToLatLng = leftToLatLngList.get(0);

                Log.d("Right from lat ", String.valueOf(rightFromLatLng.getLatitude()));
                Log.d("Right from long ", String.valueOf(rightFromLatLng.getLongitude()));
                Log.d("Right to lat ", String.valueOf(rightToLatLng.getLatitude()));
                Log.d("Right to long ", String.valueOf(rightToLatLng.getLongitude()));
                Log.d("Left from lat ", String.valueOf(leftFromLatLng.getLatitude()));
                Log.d("Left from long ", String.valueOf(leftFromLatLng.getLongitude()));
                Log.d("Left to lat ", String.valueOf(leftToLatLng.getLatitude()));
                Log.d("Left to long ", String.valueOf(leftToLatLng.getLongitude()));

                mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(rightFromLatLng.getLatitude(), rightFromLatLng.getLongitude()),
                                new LatLng(rightToLatLng.getLatitude(), rightToLatLng.getLongitude()))
                        .width(10)
                        .color(Color.MAGENTA));
                mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(leftFromLatLng.getLatitude(), leftFromLatLng.getLongitude()),
                                new LatLng(leftToLatLng.getLatitude(), leftToLatLng.getLongitude()))
                        .width(10)
                        .color(Color.BLUE));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void updateMarkerPosition(LatLng latLng) {

        //Log.d("DEMO=====>", latLng.toString());
        SFParkHandler sfParkHandler = new SFParkHandler();
        String latitude = String.valueOf(latLng.latitude);
        String longitude = String.valueOf(latLng.longitude);
        //String radius = "0.25";

        //List<SFParkBean> response = null;
        mMap.clear();

        try {
            SfParkBeanList = sfParkHandler.callAvailabilityService(latitude, longitude, radius);
        } catch (ParkingAppException e) {
            e.printStackTrace();
            Log.d("Caught Exception", e.getMessage());
        }

        if (SfParkBeanList != null) {
            StringBuilder sf = new StringBuilder();
            int count = 1;
            for (SFParkBean bean : SfParkBeanList) {

                sf.append(" " + count + " : " + bean.getName() + "\n");
                //Log.d("DEMO=====>", sf.toString());
                count++;

                // set the Marker options.
                marker = new MarkerOptions()
                        .position(new LatLng(bean.getLatitude(),bean.getLongitude()))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                        .draggable(true).visible(true).title("Spot " + count);
                mMap.addMarker(marker);
                Log.d("added marker at " , bean.getName() + "  " + bean.getLatitude() + "   " + bean.getLongitude());
                if (count == Constants.LIMIT_FOR_PARKING_DISPLAY + 1) {
                    break;
                }
            }
            // set the information using Setter.
            setInformation(sf.toString());

            if (count == 1) {
                sf.append("Parking not found");
                setInformation(sf.toString());
            }

            // set the Marker options.
            marker = new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .draggable(true);


            // add marker to Map
            mMap.addMarker(marker);
            marker.isDraggable();


        /*    // update the WindowAdapter in order to inflate the TextView with custom Text View Adapter
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    // Define a customView to attach it onClick of marker.
                    View customView = getLayoutInflater().inflate(R.layout.marker, null);
                    // inflate the customView layout with TextView.
                    TextView tvInformation = (TextView) customView.findViewById(R.id.information);
                    // get the information.
                    tvInformation.setText(getInformation());
                    return customView;
                }
            });
*/
        }
    }

    private void addMarker(LatLng latLng, final String title, String rightFrom, String rightTo, String leftFrom, String leftTo,
                           final String[] text, final String[] side) {

        // update the WindowAdapter in order to inflate the TextView with custom Text View Adapter
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Define a customView to attach it onClick of marker.
                View customView = getLayoutInflater().inflate(R.layout.marker, null);
                // inflate the customView layout with TextView.
                TextView tvInformation = (TextView) customView.findViewById(R.id.information);
                // get the information.
                tvInformation.setText("");
                int length = 0;
                if(marker.getTitle().equals(title)) {
                    for (int i = 0; i < 5; i++) {
                        if (text[i] != "" && text[i] != null) {
                            tvInformation.append(text[i]);
                            Spannable spannableText = (Spannable) tvInformation.getText();
                            if (side[i] == "R") {
                                spannableText.setSpan(new ForegroundColorSpan(Color.MAGENTA), length, length + text[i].length(), 0);
                                // tvInformation.setTextColor(getResources().getColor(R.color.right));
                            }
                            if (side[i] == "L") {
                                spannableText.setSpan(new ForegroundColorSpan(Color.BLUE), length, length + text[i].length(), 0);
                                // tvInformation.setTextColor(getResources().getColor(R.color.left));
                            }
                            length = length + text[i].length();
                        }
                    }
                    if(SfParkBeanList.size() ==0) {
                        tvInformation.append("\nParking data not available");
                    }
                } else {
                    tvInformation.setText(getLocationText(marker));
                }
                return customView;

            }
        });
        Log.d("Right from  ", rightFrom.toString());
        Log.d("Right to  ", rightTo.toString());
        Log.d("Left from  ", leftFrom.toString());
        Log.d("Left to  ", leftTo.toString());

        mMap.clear();
        drawLine(rightFrom.toString(), rightTo.toString(), leftFrom.toString(),
                leftTo.toString());
        // set the Marker options.
        setParkingLocations(latLng);

        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))).showInfoWindow();


    }

   private String getLocationText(Marker marker) {
       StringBuilder info = new StringBuilder();
       for(int i=0; i < SfParkBeanList.size() ; i++) {
           SFParkBean bean = SfParkBeanList.get(i);
           if (bean.getName().equals(marker.getTitle())) {
               marker.setTitle(bean.getName());
               info.append(bean.getName() + "\n");
               if (bean.getType() != null) {
                   info.append("Status: " + bean.getType() + "\n");
               }
               if (bean.getAddress() != null) {
                   info.append("Address: " + bean.getAddress() + "\n");
               }
               if (bean.getContactNumber() != null) {
                   info.append("Contact Number : " + bean.getContactNumber() + "\n");
               }
               if (bean.getOperationHours() != null) {
                   List<OperationHoursBean> opHours = bean.getOperationHours();
                   if (opHours.size() > 0) {
                       info.append("Operation Hours: \n");
                       for (OperationHoursBean opBean : opHours) {
                           if (opBean.getFromDay() != null) {
                               info.append(opBean.getFromDay());
                           }
                           if (opBean.getToDay() != null) {
                               info.append("-" + opBean.getToDay());
                           }
                           info.append(":");
                           if (opBean.getStartTime() != null) {
                               info.append(opBean.getStartTime());
                           }
                           if (opBean.getEndTime() != null) {
                               info.append("-" + opBean.getEndTime());
                           }
                           info.append("\n");
                       }
                   }
               }
           }
       }

       return info.toString();
   }

    private void setParkingLocations(LatLng latLng) {
        SFParkHandler sfParkHandler = new SFParkHandler();
        String latitude = String.valueOf(latLng.latitude);
        String longitude = String.valueOf(latLng.longitude);
        //String radius = "0.25";
        //mMap.clear();

        // List<SFParkBean> response = null;

        try {
            SfParkBeanList = sfParkHandler.callAvailabilityService(latitude, longitude, radius);
        } catch (ParkingAppException e) {

        }
        Log.d("current position ", latLng.latitude + " " + latLng.longitude);
        if (SfParkBeanList != null) {
            StringBuilder sf = new StringBuilder();
            int count = 1;
            for (SFParkBean bean : SfParkBeanList) {
                count++;
                Log.d("Marker added at ", bean.getName() + " " + bean.getLongitude() + " " + bean.getLatitude());
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(bean.getLongitude(), bean.getLatitude()))
                        .title(bean.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.parking_marker))
                        .draggable(true).visible(true));
                if (count == 9) {
                    break;
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        //when user clicks layers-> Normal, Map type will changed to normal
        if (id == R.id.menu_2_choice_1) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            Toast.makeText(getApplicationContext(), "Normal View", Toast.LENGTH_LONG).show();
            item.setChecked(true);
            return true;
        }
        //when user clicks layers-> Satellite, Map type will changed to satellite
        else if (id == R.id.menu_2_choice_2) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            Toast.makeText(getApplicationContext(), "Satellite View", Toast.LENGTH_LONG).show();
            item.setChecked(true);
            return true;
        }
        //when user clicks layers-> Terrain, Map type will changed to Terrain
        else if (id == R.id.menu_2_choice_3) {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            Toast.makeText(getApplicationContext(), "Terrain View", Toast.LENGTH_LONG).show();
            item.setChecked(true);
            return true;
        }
        //when user clicks layers-> Terrain, Map type will changed to Terrain
        else if (id == R.id.menu_2_choice_4) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            Toast.makeText(getApplicationContext(), "Hybrid View", Toast.LENGTH_LONG).show();
            item.setChecked(true);
            return true;
        }

        //when user clicks Settings-> Display parking within tenth of a mile, radius sent to SFPark will be 0.1 mi
        if (id == R.id.menu_5_choice_1) {
            radius = "0.1";
            Toast.makeText(getApplicationContext(), "Shall display parking within tenth of a mile", Toast.LENGTH_LONG).show();
            item.setChecked(true);
            return true;
        }
        //when user clicks Settings-> Display parking within quarter mile, radius sent to SFPark will be 0.25 mi
        else if (id == R.id.menu_5_choice_2) {
            radius = "0.25";
            Toast.makeText(getApplicationContext(), "Shall display parking within quarter of a mile", Toast.LENGTH_LONG).show();
            item.setChecked(true);
            return true;
        }
        //when user clicks Settings-> Display parking within half a mile, radius sent to SFPark will be 0.5 mi
        else if (id == R.id.menu_5_choice_3) {
            radius = "0.5";
            Toast.makeText(getApplicationContext(), "Shall display parking within half a mile", Toast.LENGTH_LONG).show();
            item.setChecked(true);
            return true;
        }
        //when user clicks Settings-> Display parking within a mile, radius sent to SFPark will be 1 mi
        else if (id == R.id.menu_5_choice_4) {
            radius = "1";
            Toast.makeText(getApplicationContext(), "Shall display parking within 1 mile", Toast.LENGTH_LONG).show();
            item.setChecked(true);
            return true;
        }

        //dialog fragment will appear when user clicks on Clear Markers tab in action overflow
        if (id == R.id.action_clearMarkers) {
            //calls a dialog box
            DialogFragment myFragment = new ClearMarkerDialog();
            myFragment.show(getFragmentManager(), "theDialog");
            SfParkBeanList.clear();
            return true;
        }
        // getLastKnownLocation wasn't giving me accurate coordinates.
        // The "new" way to get accurate coordinates is by using the FusedLocationApi.
        if (id == R.id.action_gps) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                CameraUpdate currentLocation = CameraUpdateFactory.newLatLng(
                        new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                mMap.animateCamera(currentLocation, 800, null);
            } else {
                Toast.makeText(getApplicationContext(),
                        "Current location not available", Toast.LENGTH_SHORT).show();
            }
        }
        //when user clicks Help-> Street Cleaning Help, help information for Street Cleaning Data will be displayed
        if (id == R.id.help_choice_1) {
            DialogFragment myFragment = new StreetCleaningHelp();
            myFragment.show(getFragmentManager(), "helpDialog_1");
            item.setChecked(true);
            return true;

        }
        //when user clicks Help-> Parking Information Help, help information for Parking Information Data will be displayed

        if (id == R.id.help_choice_2) {
            DialogFragment myFragment = new ParkingInfoHelp();
            myFragment.show(getFragmentManager(), "helpDialog_2");
            item.setChecked(true);
            return true;

        }

        if (id == R.id.help_choice_3) {
            DialogFragment myFragment = new AddToFavoritesHelp();
            myFragment.show(getFragmentManager(), "helpDialog_3");
            item.setChecked(true);
            return true;

        }
        //when user clicks Favorites button, the current parking info will be stored in database
        if (id == R.id.action_favorites) {

            try {
                if (SfParkBeanList != null && SfParkBeanList.size() != 0) {
                    dbConnectionHandler.insertParkingInfo(SfParkBeanList);
                    Toast.makeText(getApplicationContext(), "Current Parking info added to favorites", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to add current parking info to favorites", Toast.LENGTH_LONG).show();
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Failed to add current parking info to favorites", Toast.LENGTH_LONG).show();
            }
        }

        //when user clicks View Favorites, the favorite parking information will be displayed in a separate activity
        if (id == R.id.action_view_favorites) {
            Intent intent = new Intent(this, DisplayFavorite.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        // commenting this out for now. causes force close since we are still relying on the old LocationListener
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }


    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle arg0) {
        if(mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    /**
     * This is an inner class used to created a dialog fragment when users
     * click the Clear Markers Tab in the action overflow
     */
    public static class ClearMarkerDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder theDialog = new AlertDialog.Builder(getActivity(),R.style.DialogTheme);
            theDialog.setTitle("Clear Markers");
            theDialog.setMessage("Are you sure you would like to clear all markers on map?");
            // Markers will be cleared if user clicks YES and a toast will appear notifying
            //the the user
            theDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mMap.clear();
                    Toast.makeText(getActivity(), "Markers Cleared", Toast.LENGTH_SHORT).show();
                }
            });

            //if user clicks NO, a toast will appear telling user that the clear
            //function has been canceled
            theDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getActivity(), "Clear Canceled", Toast.LENGTH_SHORT).show();

                }
            });

            return theDialog.create();

        }
    }

    /**
     * This is an inner class used to created a dialog fragment when users
     * click the Street Cleaning Help item under the Help menu  in the action overflow
     */
    public static class StreetCleaningHelp extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder helpDialog_1 = new AlertDialog.Builder(getActivity(),R.style.DialogTheme);
            helpDialog_1.setTitle("Street Cleaning Help");
            helpDialog_1.setMessage("-Tap anywhere on the map to place Marker\n" +
                    "-Tap on the yellow marker to view Street Cleaning Information\n" +
                    "-The green line that appears on the map corresponds to right side of the street\n" +
                    "-The blue line that appears on the map corresponds to left side of the street");
            helpDialog_1.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            return helpDialog_1.create();
        }
    }

    /**
     * This is an inner class used to created a dialog fragment when users
     * click the Parking Information Help item under the Help menu  in the action overflow
     */
    public static class ParkingInfoHelp extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder helpDialog_2 = new AlertDialog.Builder(getActivity(),R.style.DialogTheme);
            helpDialog_2.setTitle("Parking Information Help");
            helpDialog_2.setMessage("-Tap anywhere on the map to place Marker\n" +
                    "-Tap on the markers with a P icon to view Parking Information");
            helpDialog_2.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            return helpDialog_2.create();
        }
    }
    /**
     * This is an inner class used to created a dialog fragment when users
     * click the Add to Favorites help item under the Help menu  in the action overflow
     */
    public static class AddToFavoritesHelp extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder helpDialog_3 = new AlertDialog.Builder(getActivity(),R.style.DialogTheme);
            helpDialog_3.setTitle("Add to Favorites Help");
            helpDialog_3.setMessage("-Long press anywhere on the map to place Marker\n" +
                    "-Tap on the blue marker to view if Parking Information is found\n" +
                    "-If Parking data is found and you would like to add to favorites,\n" +
                    "tap the star icon in the action bar and parking data will be saved.\n" +
                    "-To view your favorite parking spots go to View Favorites tab in the action overflow\n" +
                    "-To delete parking spots long press on the data and click Delete from favorites");
            helpDialog_3.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {


                }
            });

            return helpDialog_3.create();
        }
    }

    // getter and setter for Information. In order to access it globally.
    public void setInformation(String information) {
        this.information = information;
    }

    public String getInformation() {
        return information;
    }

    public void setStreetCleaningInformation(String streetCleaningInformation) {
        this.streetCleaningInformation = streetCleaningInformation;
    }

    public String getStreetCleaningInformation() {
        return streetCleaningInformation;
    }

    private String getDayOfWeek(int value) {
        String day = null;
        switch (value) {
            case 1:
                day = "Sun";
                break;
            case 2:
                day = "Mon";
                break;
            case 3:
                day = "Tues";
                break;
            case 4:
                day = "Wed";
                break;
            case 5:
                day = "Thurs";
                break;
            case 6:
                day = "Fri";
                break;
            case 7:
                day = "Sat";
                break;
        }
        return day;
    }


    private static class InflatingEntity extends HttpEntityWrapper {
        public InflatingEntity(HttpEntity wrapped) {
            super(wrapped);
        }

        @Override
        public InputStream getContent() throws IOException {
            return new GZIPInputStream(wrappedEntity.getContent());
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }


}