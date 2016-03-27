package com.parse.uberclone;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class StarterApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    // Enable Local Datastore.
    Parse.enableLocalDatastore(this);

    // Add your initialization code here
    Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
            .applicationId("uberdofjh197fudhiu297f9dsuf97")
            .clientKey(null)
            .server("http://uber6919.herokuapp.com/parse/")
    .build()
    );

//      ParseUser.enableAutomaticUser();

//      ParseObject gameScore = new ParseObject("GameScore");
//      gameScore.put("score", 1337);
//      gameScore.put("playerName", "Sean Plott");
//      gameScore.put("cheatMode", false);
//      gameScore.saveInBackground(new SaveCallback() {
//          public void done(ParseException e) {
//              if (e == null) {
//                  Log.i("Parse", "Save Succeeded");
//              } else {
//                  Log.i("Parse", "Save Failed");
//              }
//          }
//      });
    ParseACL defaultACL = new ParseACL();
    // Optionally enable public read access.
    // defaultACL.setPublicReadAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);
  }
}
