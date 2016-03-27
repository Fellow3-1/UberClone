package com.parse.uberclone;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class YourLocation extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    String provider;
    TextView infoTextView;
    Button requestUberButton;
    ArrayList<Marker> markers = new ArrayList<>();

    Boolean requestActive = false;
    String driverName;
    ParseGeoPoint driverLocation = new ParseGeoPoint(0,0);
    Handler handler = new Handler();


    public void performLogout(View view) {
        ParseUser.logOut();
        handler.removeCallbacksAndMessages(null);
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }

    public void requestUber(View view) {

        if(requestActive == false) {
            requestActive = true;
            Log.i("MyApp", "Uber Requested ");
            ParseObject request = new ParseObject("Requests");

            Location location = locationManager.getLastKnownLocation(provider);
            ParseGeoPoint userLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

            request.put("requesterUserName", ParseUser.getCurrentUser().getUsername());
            request.put("requesterLocation", userLocation);

            ParseACL parseACL = new ParseACL();
            parseACL.setPublicWriteAccess(true);
            parseACL.setPublicReadAccess(true);
            request.setACL(parseACL);


            request.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        infoTextView.setText("Finding Uber driver...");
                        requestUberButton.setText("Cancel your Uber");
                    } else {
                        Log.i("Myapp", "Save failed");
                        e.printStackTrace();
                    }
                }
            });
        }
        else  {
            requestActive = false;
            infoTextView.setText("Uber cancelled");
            requestUberButton.setText("Request Uber");

            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
            query.whereEqualTo("requesterUserName", ParseUser.getCurrentUser().getUsername());

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null) {
                        if(objects.size() > 0) {
                            for (ParseObject object : objects) {
                                object.deleteInBackground();
                            }
                        }
                        else {
                            Log.i("MyApp", "Query returned nothing");
                        }
                    }
                    else {
                        e.printStackTrace();
                        Log.i("MyApp", "Error occured while deleting");
                    }

                }
            });
        }

    }

    public void updateLocation(final Location location) {
        mMap.clear();



        //Updating user location on parse
        if(requestActive) {

            if(driverName != null) {
                ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
                userQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        if(e == null) {
                            if(objects.size() > 0) {
                                for (ParseObject driver : objects) {
                                    driverLocation = driver.getParseGeoPoint("location");
                                }
                            }
                            else {
                                Log.i("MyApp", "Query returned nothing");
                            }
                        }
                        else {
                            e.printStackTrace();
                            Log.i("MyApp", "Error occured while deleting");
                        }
                    }
                });


            }
            else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12f));
                mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Current Location"));
            }

            final ParseGeoPoint userLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
            query.whereEqualTo("requesterUserName", ParseUser.getCurrentUser().getUsername());

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null) {
                        if(objects.size() > 0) {
                            for (ParseObject object : objects) {
                                object.put("requesterLocation", userLocation);
                                object.saveInBackground();
                            }
                        }
                        else {
                            Log.i("MyApp", "Query returned nothing");
                        }
                    }
                    else {
                        e.printStackTrace();
                        Log.i("MyApp", "Error occured while deleting");
                    }

                }
            });
        }
        else {
            //Checking if user has active request
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");
            query.whereEqualTo("requesterUserName", ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null) {
                        if(objects.size() > 0) {
                            requestActive = true;
                            infoTextView.setText("Finding Uber driver...");
                            requestUberButton.setText("Cancel Uber");
                            for(ParseObject object : objects) {
                                infoTextView.setText("Your driver is on their way. Hold tight!");
                                if(object.get("driverUserName") != null) {
                                    driverName = object.getString("driverUserName");
                                    requestUberButton.setVisibility(View.INVISIBLE);
                                    Log.i("MyApp", "Driver chosen as " + driverName);
                                }
                            }
                        }
                        else {
                            Log.i("MyApp", "New Query returned nothing");
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12f));
                            mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Current Location"));
                        }
                    }
                    else {
                        e.printStackTrace();
                        Log.i("MyApp", "Error occured while deleting");
                    }
                }
            });
        }

        if(driverLocation.getLongitude() != 0 && driverLocation.getLatitude() != 0) {
            Log.i("MyApp", driverLocation.toString());
            Double distance = driverLocation.distanceInKilometersTo(new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
            infoTextView.setText("Your driver is " + String.format("%.2f", distance) + " kilometers away");

            //ADDING BOUNDS TO MARKERS TO VIEW ALL AT A COMFORTABLE ZOOM
            //*****************88

            markers.clear();
            markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude())).title("Your driver's current Location")));
            markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title("Your current Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();

            int padding = 100; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);

        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLocation(locationManager.getLastKnownLocation(provider));
            }
        }, 3000);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        infoTextView = (TextView) findViewById(R.id.infoTextView);
        requestUberButton = (Button) findViewById(R.id.requestUberButton);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        Log.i("MyApp", "Provider is " + provider);

        locationManager.requestLocationUpdates(provider, 400, 1, this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.i("MyApp", "Map is ready");

        Location location = locationManager.getLastKnownLocation(provider);

        if(location != null) {
            updateLocation(location);
        }
        else {
            Log.i("MyApp", "Location currently is null");
            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }



    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocation(location);
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
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }
}
