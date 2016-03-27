package com.parse.uberclone;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

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
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ViewRiderLocation extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Intent i;
    LatLng riderLocation;
    LatLng driverLocation;

    public void logout(View view) {
        ParseUser.logOut();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }

    public void acceptRequest(View view) {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
        query.whereEqualTo("requesterUserName", i.getStringExtra("riderName"));

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if( e == null) {
                    if(objects.size() > 0 ) {
                        for (ParseObject object : objects) {
                            object.put("driverUserName", ParseUser.getCurrentUser().getUsername());
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e != null) {
                                        e.printStackTrace();
                                    }
                                    else {
                                        //**************//
                                        //SWITCHING TO NAVIGATION VIEW VIA INTENTS
                                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                                Uri.parse("http://maps.google.com/maps?daddr=" + riderLocation.latitude + "," + riderLocation.longitude));
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                    }
                    else {
                        Log.i("MyApp", "Query returned 0 results");
                    }
                }
                else {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rider_location);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        i = getIntent();
        riderLocation = new LatLng(i.getDoubleExtra("riderLatitude", 0), i.getDoubleExtra("riderLongitude", 0));
        driverLocation = new LatLng(i.getDoubleExtra("driverLatitude", 0), i.getDoubleExtra("driverLongitude", 0));

        mMap = googleMap;
        mMap.clear();

        RelativeLayout mapLayout = (RelativeLayout)findViewById(R.id.map_layout);
        mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ArrayList<Marker> markers = new ArrayList<>();
                markers.add(mMap.addMarker(new MarkerOptions().position(riderLocation).title("Your fare's current Location")));
                markers.add(mMap.addMarker(new MarkerOptions().position(driverLocation)
                        .title("Your current Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));

                //ADDING BOUNDS TO MARKERS TO VIEW ALL AT A COMFORTABLE ZOOM
                //*****************88
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();

                int padding = 100; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);
            }
        });



    }
}
