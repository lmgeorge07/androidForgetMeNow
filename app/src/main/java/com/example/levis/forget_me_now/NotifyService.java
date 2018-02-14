/*
Name: Levis George
Date: 12/1/2017
Course: Mobile Device Applications COP4656-01
File name: NotifyService.java
Purpose: Implements the receiver for medication timing notification broadcasts
 */

package com.example.levis.forget_me_now;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;

import java.util.Map;

public class NotifyService extends BroadcastReceiver {
    static String notificationTag = "Levis_George_FMN";  //  Not necessary, but additional info
    static int notificationId = 1;  // Will be grabbed and incremented for several pending intents
    private String name, dose, time;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check if notifications are allowed
        SharedPreferences preferences = context.getSharedPreferences(SettingsActivity.prefFile,
                Context.MODE_PRIVATE);
        if (!preferences.getBoolean(SettingsActivity.notificationSetting, false))
            return;

        // Get intent data
        this.name = intent.getStringExtra("name");
        this.dose = intent.getStringExtra("dose");
        this.time = intent.getStringExtra("time");

        //
        DBHelper db = new DBHelper(context);
        Map<String, String> idTimeMap = db.getIDTimeMap(this.name);

        // Medication notifications will have sound
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Setting up intent, ad building the notification
        NotificationManager mNM = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, NewMedActivity.pendingId++, intent1, 0);

        Notification notification = new Notification.Builder(context)
                .setContentTitle(this.time)
                .setContentText(String.format("%s %s.",
                        context.getText(R.string.nt_txt), this.name))
                .setSmallIcon(R.mipmap.fmn_app_icon_round)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        // Sending the notification
        mNM.notify(NotifyService.notificationTag, Integer.parseInt(idTimeMap.get(this.time)),
                notification);
    }
}
