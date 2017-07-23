package com.example.souravmandal.isight;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;

public class SplashActivity extends AppCompatActivity {
    Thread splashTread;
    TextToSpeech ttobj;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_splash);
        imageView = (ImageView)findViewById(R.id.imageView2);


        ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    ttobj.setLanguage(Locale.UK);
                    ttobj.speak("Welcome to ISight", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });



        //timer to pause time
            splashTread = new Thread() {
                @Override
                public void run() {
                    try {
                        int waited = 0;
                        // Splash screen pause time
                        while (waited < 3500) {
                            sleep(100);
                            waited += 100;
                        }
                        Intent intent = new Intent(SplashActivity.this,
                                FlowActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        SplashActivity.this.finish();
                    } catch (InterruptedException e) {
                        // do nothing
                    } finally {
                       SplashActivity.this.finish();
                    }

                }
            };
            splashTread.start();


        TextView isight = (TextView)findViewById(R.id.textView);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "opensansregular.ttf");
        isight.setTypeface(custom_font);
        isight.setTextColor(Color.parseColor("#696969"));




    }

    public void onPause(){
        if(ttobj !=null){
            ttobj.stop();
            ttobj.shutdown();
        }
        super.onPause();
    }

}
