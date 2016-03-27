package com.parse.uberclone;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class DriverOrRider extends AppCompatActivity{

    Switch riderOrDriverSwitch;

    public void getStarted(View view) {
        String riderOrDriver = "rider";
        if(riderOrDriverSwitch.isChecked()) {
            riderOrDriver = "driver";
        }

        ParseUser.getCurrentUser().put("riderOrDriver", riderOrDriver);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    Log.i("Myapp", "Must go to maps acitivty now");
                    redirectUser();
                }
                else {
                    e.printStackTrace();
                }
            }
        });
    }

    public void redirectUser() {
        if(ParseUser.getCurrentUser().get("riderOrDriver").equals("rider")) {
            Intent i = new Intent(getApplicationContext(), YourLocation.class);
            startActivity(i);
        }
        else {
            Intent i = new Intent(getApplicationContext(), ViewRequests.class);
            startActivity(i);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_or_rider);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        //hiding action bar
        getSupportActionBar().hide();

        if(ParseUser.getCurrentUser().get("riderOrDriver") != null) {
            redirectUser();
        }

        riderOrDriverSwitch = (Switch) findViewById(R.id.driverOrRiderSwitch);


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
}
