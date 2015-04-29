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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Criteria;
import android.content.ContextWrapper;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pooja.sfparksample.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import com.parkingapp.exception.ParkingAppException;
import com.parkingapp.parser.SFParkBean;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;


public class MainActivity extends ActionBarActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private  static GoogleMap mMap;
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 800;
    private String information;
    private String streetCleaningInformation;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setContentView(R.layout.activity_map);

        setUpMapIfNeeded();

        mMap.setMyLocationEnabled(true);
        mMap.getMyLocation();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // setup default location onMap load event
        Criteria criteria = new Criteria();

        // Use this for database connection
        // ContextWrapper contextWrapper = new ContextWrapper(getBaseContext());
        // DBConnectionHandler dbConnectionHandler=new DBConnectionHandler();
        // dbConnectionHandler.createDB(contextWrapper);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);


        String provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        double lat =  37.721897;
        double lng = -122.47820939999997;
        LatLng coordinate = new LatLng(lat, lng);
        CameraUpdate center=CameraUpdateFactory.newLatLng(new LatLng(37.721897,-122.47820939999997));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(12);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
    }


    @Override
    protected void onResume() {
        super.onResume();
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
                    mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(37.721897,-122.47820939999997) , 14.0f) );
                }
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
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
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Parking spots")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                    //.snippet("1: ABC \n" + "2: XYZ")

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
                /*
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Geographical Coordinates")
                        .snippet("LAT: " + latLng.latitude + " LNG: " + latLng.longitude + ));
                */

            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Geocoder geocoder = new Geocoder(getApplicationContext());
                geocoder.isPresent();
                String addressText;
                List<Address> matches = null;
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
                    String title = getString(R.string.street_cleaning_info);
                    setStreetCleaningInformation(addressText);
                    addMarker(latLng, title);
                }
            }
        });
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


        MenuInflater inflater= getMenuInflater();

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
        if(id==R.id.menu_2_choice_1){
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            return true;
        }
        //when user clicks layers-> Satellite, Map type will changed to satellite
        if(id==R.id.menu_2_choice_2){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            return true;
        }
        //when user clicks layers-> Terrain, Map type will changed to Terrain
        if(id==R.id.menu_2_choice_3){
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            return true;
        }
        if(id==R.id.menu_2_choice_4){
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            return true;
        }
        //dialog fragment will appear when user clicks on Clear Markers tab in action overflow
        if(id == R.id.action_clearMarkers){
            //calls a dialog box
            DialogFragment myFragment = new ClearMarkerDialog();
            	            myFragment.show(getFragmentManager(), "theDialog");
            return true;
        }
        // getLastKnownLocation wasn't giving me accurate coordinates.
        // The "new" way to get accurate coordinates is by using the FusedLocationApi.
        if(id == R.id.action_gps){
            if (null != mCurrentLocation) {
                CameraUpdate currentLocation = CameraUpdateFactory.newLatLng(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                mMap.animateCamera(currentLocation, 800, null);
            } else {
                Toast.makeText(getApplicationContext(), "Current loc is null", Toast.LENGTH_LONG).show();

            /*
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            CameraUpdate currentLocation = CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
            mMap.animateCamera(currentLocation, 800, null);
            Toast.makeText(getApplicationContext(),
                    "Current location\nLat: " + lat + "\nLng: " + lng,
                    Toast.LENGTH_LONG).show();
            */
            }
        }

        return super.onOptionsItemSelected(item);

    }


    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
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

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * This is an inner class used to created a dialog fragment when users
     * click the Clear Markers Tab in the action overflow
     */
    public static  class ClearMarkerDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder theDialog = new AlertDialog.Builder(getActivity());
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