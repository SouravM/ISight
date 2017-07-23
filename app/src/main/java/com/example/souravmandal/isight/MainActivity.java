package com.example.souravmandal.isight;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Sourav Mandal on 7/16/2017.
 */


public class MainActivity extends AppCompatActivity {
    TextToSpeech ttobj;
    JSONArray service;
    Double Latitude = 53.280533;
    Double Longitude = -3.804763;
    String lat;
    String longi;
    int state = 0;
    int upstate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    ttobj.setLanguage(Locale.UK);
                    ttobj.speak("Press the volume down button to search A T ems near you.", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (state == 0) {
            if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                ttobj.speak("Searching", TextToSpeech.QUEUE_FLUSH, null);

                //call the api
                BarclaysRestClient.get("atms", null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // If the response is JSONObject instead of expected JSONArray

                        ttobj.speak("Churning out nearest A T M for you.", TextToSpeech.QUEUE_FLUSH, null);


                        try {
                            JSONArray arr = response.getJSONArray("data");
                            JSONObject obj = arr.getJSONObject(0);
                            JSONObject geo = obj.getJSONObject("GeographicLocation");
                            lat = geo.getString("Latitude");
                            longi = geo.getString("Longitude");
                            JSONObject addobj = obj.getJSONObject("Address");
                            String streetname = addobj.getString("StreetName");
                            String buildingnumber = addobj.getString("BuildingNumberOrName");

                             service = obj.getJSONArray("ATMServices");

                           Log.e("streetname",streetname);
                            Log.e("buildingnumber",buildingnumber);

                            Log.e("coord", lat + "||" + longi);

                            //calculate distance
                            Location mallLoc = new Location("");
                            mallLoc.setLatitude(Double.parseDouble(lat));
                            mallLoc.setLongitude(Double.parseDouble(longi));

                            Location userLoc = new Location("");
                            userLoc.setLatitude(Latitude);
                            userLoc.setLongitude(Longitude);

                            float distance = mallLoc.distanceTo(userLoc);
                            Log.e("Dist", String.format(Locale.getDefault(), "%.2f", distance)
                                    + " m Away");

                            String data = "Found nearest A T M near you at" + buildingnumber + " " + streetname + "which is" + Float.toString(distance) + "metres away";
                            ttobj.speak(data, TextToSpeech.QUEUE_FLUSH, null);

                            ttobj.speak("To check the services about the ATM press the volume down button", TextToSpeech.QUEUE_ADD, null);
                            state = 1;

                        } catch (Exception e) {
                            Log.e("Exception", "ex");
                        }
                    }


                });


            }
            return true;
        }

        else if (state == 1)
        {

            try {
                for (int i = 0; i < service.length(); i++) {
                    if (i == 0)

                    { // Use for the first splited text to flush on audio stream
                        ttobj.speak(service.getString(i),TextToSpeech.QUEUE_FLUSH, null);
                        //Log.e("ee",service.getString(i));
                    }

                    else

                    { // add the new test on previous then play the TTS

                        ttobj.speak(service.getString(i), TextToSpeech.QUEUE_ADD,null);

                    }

                    ttobj.playSilence(750, TextToSpeech.QUEUE_ADD, null);


                }

                //ttobj.speak(bc,TextToSpeech.QUEUE_FLUSH,null);
            }
            catch(Exception e)
            {

            }
            ttobj.speak("To navigate to the A T M   press volume up button , To book an uber press volumne down button",TextToSpeech.QUEUE_ADD,null);
            state = 2;
            return true;
        }
        else
        {

            //navigate
            if((keyCode == KeyEvent.KEYCODE_VOLUME_UP))
            {
                //ttobj.speak("Navigating",TextToSpeech.QUEUE_FLUSH,null);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=12.938347,77.699084"));
                startActivity(intent);
                this.finishAffinity();
            }
            return true;
        }
    }




    public void onPause(){
        if(ttobj !=null){
            ttobj.stop();
            ttobj.shutdown();
        }
        super.onPause();
    }
}
