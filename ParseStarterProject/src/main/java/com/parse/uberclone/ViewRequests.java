package com.parse.uberclone;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ViewRequests extends AppCompatActivity implements LocationListener {

    ListView listView;
    ArrayList<String> listViewContent;
    ArrayList<String> usernames;
    ArrayAdapter arrayAdapter;
    LocationManager locationManager;
    String provider;
    Location location;

    public void updateLocation() {
        final ParseGeoPoint userLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

        ParseUser.getCurrentUser().put("location", userLocation);
        ParseUser.getCurrentUser().saveInBackground();

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
//        query.whereDoesNotExist("driverUsername");
        query.whereNear("requesterLocation", userLocation);
        query.setLimit(100);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null) {
                    if(objects.size() > 0) {
                        listViewContent.clear();
                        usernames.clear();
                        for (ParseObject object : objects) {
                            if(object.get("driverUserName") == null) {
                                usernames.add(object.getString("requesterUserName"));
                                Double distance = userLocation.distanceInKilometersTo((ParseGeoPoint) object.get("requesterLocation"));
                                listViewContent.add(object.get("requesterUserName").toString() + " - " + String.format("%.2f", distance)
                                         + " kilometers");
                            }
                        }
                        arrayAdapter.notifyDataSetChanged();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        listView = (ListView) findViewById(R.id.listView);
        listViewContent = new ArrayList<String>();
        usernames = new ArrayList<>();
        listViewContent.add("Finding nearby requests...");
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listViewContent);
        listView.setAdapter(arrayAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        Log.i("MyApp", "Provider is " + provider);

        locationManager.requestLocationUpdates(provider, 400, 1, this);
        location = locationManager.getLastKnownLocation(provider);

        if(location != null) {
            Log.i("MyApp", "Driver location not null");
            updateLocation();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Need to pass the user location along which actually could be changing
                //Better to query the object?
                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
                query.whereEqualTo("requesterUserName", usernames.get(position));
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        Intent i = new Intent(getApplicationContext(), ViewRiderLocation.class);
                        i.putExtra("riderName", objects.get(0).getString("requesterUserName"));
                        i.putExtra("riderLatitude", objects.get(0).getParseGeoPoint("requesterLocation").getLatitude());
                        i.putExtra("riderLongitude", objects.get(0).getParseGeoPoint("requesterLocation").getLongitude());
                        i.putExtra("driverLatitude", location.getLatitude());
                        i.putExtra("driverLongitude", location.getLongitude());
                        startActivity(i);
                    }
                });

            }
        });

    }
// Akshayaa is the best. I love her sooooooo much.
    @Override
    public void onLocationChanged(Location location) {
        updateLocation();
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
//        locationManager.removeUpdates(this);
    }
}
