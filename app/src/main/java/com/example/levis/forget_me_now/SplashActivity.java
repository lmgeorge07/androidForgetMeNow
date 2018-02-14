/*
Name: Levis George
Date: 12/1/2017
Course: Mobile Device Applications COP4656-01
File name: SplashActivity.java
Purpose: Loads the splash screen image and sets the refresh database alarm
 */

package com.example.levis.forget_me_now;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import java.util.Calendar;

// Initial short-lived screen while app loads
public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set alarm to reset med taken date
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("purpose", "refresh_extra");

        // Set the repeating alarm to start at approximately 12:00 a.m. and occur daiily
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);
        long timeInMillis = calendar.getTimeInMillis();
        if(timeInMillis-System.currentTimeMillis()<0){
            //if its in past, add one day
            timeInMillis += 86400000L;
        }

        alarmIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis,
                AlarmManager.INTERVAL_DAY, alarmIntent);

        // Begin 'Today' screen (the actual main page of the app)
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}

