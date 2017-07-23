package com.example.souravmandal.isight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import java.util.Locale;

/**
 * Created by Sourav Mandal on 7/20/2017.
 */

public class FlowActivity extends AppCompatActivity {

    TextToSpeech ttobj;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.flow);

        ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    ttobj.setLanguage(Locale.UK);
                    ttobj.speak("Press the volume down button to find the nearest A T M near you.", TextToSpeech.QUEUE_FLUSH, null);

                    ttobj.playSilence(750, TextToSpeech.QUEUE_ADD, null);

                    ttobj.speak("If you are at the A T M location press volume up button to explore surroundings to see an A T M", TextToSpeech.QUEUE_ADD, null);

                }
            }
        });



    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {

            Intent intent = new Intent(this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);

        }
        if((keyCode == KeyEvent.KEYCODE_VOLUME_UP))
        {
            Intent intent = new Intent(this,CaptureActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);

        }

        return true;


    }




}
