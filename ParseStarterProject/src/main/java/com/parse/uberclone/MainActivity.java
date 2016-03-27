/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.uberclone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.parse.uberclone.DriverOrRider;
import com.parse.uberclone.R;

import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener{

    EditText usernameField;
    EditText passwordField;
    TextView changeSignUpMode;
    Button signUpButton;
    ImageView logo;
    RelativeLayout relativeLayout;

    Boolean signUpModeActive;


    public void goToSelectionPage() {
        Intent i = new Intent(getApplicationContext(), DriverOrRider.class);
        startActivity(i);
    }

    public void signUpOrLogin(View view) {

        if(signUpModeActive) {
            ParseUser user = new ParseUser();
            user.setUsername(String.valueOf(usernameField.getText()));
            user.setPassword(String.valueOf(passwordField.getText()));

            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        goToSelectionPage();

                    } else {
                        Toast.makeText(getApplicationContext(), e.getMessage().substring(e.getMessage().indexOf(" ")), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            ParseUser.logInInBackground(String.valueOf(usernameField.getText()), String.valueOf(passwordField.getText()), new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(user != null) {
                        Toast.makeText(getApplicationContext(), "Logged in successfully!", Toast.LENGTH_SHORT).show();
                        goToSelectionPage();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), e.getMessage().substring(e.getMessage().indexOf(" ")), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override //ONCLICKLISTENER
    public void onClick(View v) {
        if(v.getId() == R.id.changeSignUpMode) {

            if(signUpModeActive) {
                signUpModeActive = false;
                changeSignUpMode.setText("Sign up");
                signUpButton.setText("Log in");
            }
            else {
                signUpModeActive = true;
                changeSignUpMode.setText("Log in");
                signUpButton.setText("Sign up");
            }

        }

        if(v.getId() == R.id.relativeLayout || v.getId() == R.id.logo) {
            //Dismiss keyboard on this click
            InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0); //0 is for no custom flags
        }
    }

    //ONKEY LISTENER
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(v.getId() == R.id.password) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                signUpOrLogin(v);
            }
        }
        else if(v.getId() == R.id.username) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                usernameField.clearFocus();
                passwordField.requestFocus();
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        ParseUser.logOut();

        if(ParseUser.getCurrentUser() != null) {
            Log.i("MyApp", "User exists already");
            goToSelectionPage();
        }

        signUpModeActive = true;
        usernameField = (EditText) findViewById(R.id.username);
        passwordField = (EditText) findViewById(R.id.password);
        logo = (ImageView) findViewById(R.id.logo);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);

        //Done button for keyboard instead of enter
        usernameField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordField.setImeOptions(EditorInfo.IME_ACTION_DONE);

        changeSignUpMode = (TextView) findViewById(R.id.changeSignUpMode);
        signUpButton = (Button) findViewById(R.id.signUpButton);

        changeSignUpMode.setOnClickListener(this);

        usernameField.setOnKeyListener(this);
        passwordField.setOnKeyListener(this);

        logo.setOnClickListener(this);
        relativeLayout.setOnClickListener(this);


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
