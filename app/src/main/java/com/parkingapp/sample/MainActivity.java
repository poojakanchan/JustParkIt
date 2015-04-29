package com.parkingapp.sample;
/**
 * File history
 * 1. Raymond Thai
 * changes: added onlocationchangelistener to setupmaps method which changes map camera to user's current location
 *          changed information snippet to parking spot snippet
 *          added personal icons
 *          implemented pooja's fix to delete previous marker when new marker is selected
 *          implemented Clear Marker button so user can clear all markers on map
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Criteria;
import android.content.ContextWrapper;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.pooja.sfparksample.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parkingapp.connection.SFParkHandler;
import com.parkingapp.database.DBConnectionHandler;
import com.parkingapp.database.StreetCleaningDataBean;
import com.parkingapp.exception.ParkingAppException;
import com.parkingapp.parser.SFParkBean;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;


public class MainActivity extends FragmentActivity implements LocationListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 800;
    private String information;
    private String streetCleaningInformation;
    MarkerOptions marker;

    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_map);

        setUpMapIfNeeded();

        mMap.setMyLocationEnabled(true);
        mMap.getMyLocation();

        // setup default location onMap load event
        Criteria criteria = new Criteria();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);


        String provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        double lat =  37.773972;
        double lng = -122.431297;
        LatLng coordinate = new LatLng(lat, lng);
        CameraUpdate center=CameraUpdateFactory.newLatLng(new LatLng(37.773972,-122.431297));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(12);


        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            private Location mLocation = null;

            @Override
            public void onMyLocationChange(Location myLocation) {

                if (mLocation == null) {
                    mLocation = myLocation;
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())).title("You are here"));
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 16);
                    mMap.animateCamera(update);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.773972, -122.431297), 14.0f));
                }
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {


                // call the updateMarkerPosition every time the lat-lng is updated.
                updateMarkerPosition(latLng);



                /*
                SFParkHandler sfParkHandler = new SFParkHandler();
                String latitude = String.valueOf(latLng.latitude);
                String longitude = String.valueOf(latLng.longitude);
                String radius = "0.25";

                List<SFParkBean> response = null;

                try {
                    response = sfParkHandler.callAvailabilityService(latitude, longitude, radius);
                } catch (ParkingAppException e) {

                }
                if (response != null) {
                    StringBuilder sf = new StringBuilder();
                    int count = 1;
                    for (SFParkBean bean : response) {

                        sf.append(" " + count + " : " + bean.getName() + "\n");
                        count++;
                        if(count == 9){
                            break;
                        }
                    }
                    // set the information using Setter.
                    setInformation(sf.toString());

                    mMap.clear();

                    if (count == 1) {
                        sf.append("Parking not found");
                        setInformation(sf.toString());
                    }

                    // set the Marker options.
                    marker = new MarkerOptions()
                            .position(latLng)
                            .title("Parking spots")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .draggable(true);

                    // add marker to Map
                    mMap.addMarker(marker).showInfoWindow();
                    marker.isDraggable();


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
                            tvInformation.setText(getInformation());
                            return customView;
                        }
                    });
                }*/
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                // Use this for database connection
                ContextWrapper contextWrapper = new ContextWrapper(getBaseContext());
                DBConnectionHandler dbConnectionHandler=new DBConnectionHandler(contextWrapper);
                SQLiteDatabase sqLiteDatabase=dbConnectionHandler.getWritableDatabase();
                dbConnectionHandler.onCreate(sqLiteDatabase);
                //dbConnectionHandler.getRequiredAddress("11TH AVE",94116);


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
                if (matches != null && matches.size() > 0) {
                    Address address = matches.get(0);
                    addressText = String.format("%s,\n%s\n%s",
                            address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                            address.getLocality(), address.getPostalCode());
                    setStreetCleaningInformation(addressText);
                    String title = getString(R.string.street_cleaning_info);

                    int postalCode = Integer.valueOf(address.getPostalCode());
                    String streetParm = address.getThoroughfare().toUpperCase();
                    String sub[] = address.getSubThoroughfare().split("-");
                    int substreetParm = Integer.valueOf(sub[0]);

                    Log.d("substreetParm", address.getSubThoroughfare());
                    Log.d("substreetParm", sub[0]);
                    Log.d("addressParm: ", streetParm);
                    Log.d("Pincode:", address.getPostalCode());

                    StreetCleanAddress = dbConnectionHandler.getRequiredAddress(substreetParm, streetParm,postalCode);

                    if (StreetCleanAddress != null) {
                        StringBuilder sc = new StringBuilder();
                        int count = 1;
                        for (StreetCleaningDataBean bean : StreetCleanAddress) {

                            if (bean.getRightLeft().equals("R")) {
                                sc.append(String.valueOf(bean.getRT_FADD()) + "-" + String.valueOf(bean.getRT_TOADD()) + " " + bean.getSTREETNAME() + "\n");
                            }
                            if (bean.getRightLeft().equals("L")) {
                                sc.append(String.valueOf(bean.getLF_FADD()) + "-" + String.valueOf(bean.getLF_TOADD()) + " " + bean.getSTREETNAME() + "\n");
                            }

                            sc.append(bean.getWeekDay()+ " Weeks:" );
                            if (bean.getWeek1OfMonth().equals("Y")) {
                                sc.append(" 1");
                            }
                            if (bean.getWeek2OfMonth().equals("Y")) {
                                sc.append(" 2");
                            }
                            if (bean.getWeek3OfMonth().equals("Y")) {
                                sc.append(" 3");
                            }
                            if (bean.getWeek4OfMonth().equals("Y")) {
                                sc.append(" 4");
                            }
                            if (bean.getWeek5OfMonth().equals("Y")) {
                                sc.append(" 5");
                            }
                            sc.append(" \n");

                            sc.append(bean.getFromHour() + "-" + bean.getToHour() + "\n");
                            if (bean.getHolidays().equals("N")) {
                                sc.append("No cleaning on holidays \n");
                            }

                            count++;
                            if (count == 9) {
                                break;
                            }
                        }
                        // set the information using Setter.
                        setStreetCleaningInformation(sc.toString());

                        mMap.clear();

                        if (count == 1) {
                            sc.append("Street cleaning info not found");
                            setStreetCleaningInformation(sc.toString());
                        }
                     //String log = "Weekday: " + bean.getWeekDay() + ", STREETNAME: " + bean.getSTREETNAME() + ", ZipCode: " + bean.getZIP_CODE() ;

                     // Writing Retrieved data to log
                     Log.d("Data: ", sc.toString());
                    }
                    addMarker(latLng, title);
                }
            }
        });

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


    // update the position every time
    // marker is dragged or position is moved on Map.

    private void updateMarkerPosition(LatLng latLng) {

        //Log.d("DEMO=====>", latLng.toString());
        SFParkHandler sfParkHandler = new SFParkHandler();
        String latitude = String.valueOf(latLng.latitude);
        String longitude = String.valueOf(latLng.longitude);
        String radius = "0.25";

        List<SFParkBean> response = null;

        try {
            response = sfParkHandler.callAvailabilityService(latitude, longitude, radius);
        } catch (ParkingAppException e) {

        }
        if (response != null) {
            StringBuilder sf = new StringBuilder();
            int count = 1;
            for (SFParkBean bean : response) {

                sf.append(" " + count + " : " + bean.getName() + "\n");
                //Log.d("DEMO=====>", sf.toString());
                count++;
                if (count == 9) {
                    break;
                }
            }
            // set the information using Setter.
            setInformation(sf.toString());

            mMap.clear();


            if (count == 1) {
                sf.append("Parking not found");
                setInformation(sf.toString());
            }

            // set the Marker options.
            marker = new MarkerOptions()
                    .position(latLng)
//                    .title("Parking spots")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .draggable(true);

            // add marker to Map
            mMap.addMarker(marker).showInfoWindow();
            marker.isDraggable();


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
                    tvInformation.setText(getInformation());
                    return customView;
                }
            });

        }
    }

    private void addMarker(LatLng latLng, String title) {
        mMap.clear();
        // set the Marker options.
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

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
                tvInformation.setText(getStreetCleaningInformation());
                return customView;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * This method will clear markers on map when user clicks the clear Markers button on the screen
     * @param view
     */
    public void onClick_clearMarker(View view) {
        mMap.clear();
    }

    // getter and setter for Information. In order to access it globally.
    public void setInformation(String information) {
        this.information = information;
    }

    public String getInformation(){
        return information;
    }

    public String getStreetCleaningInformation() {
        return streetCleaningInformation;
    }

    public void setStreetCleaningInformation(String streetCleaningInformation) {
        this.streetCleaningInformation = streetCleaningInformation;
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