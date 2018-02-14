/*
Name: Levis George
Date: 12/1/2017
Course: Mobile Device Applications COP4656-01
File name: AlarmReceiver.java
Purpose: Implements the receiver of the the broadcast/receiver. Receives 2 signals:
Resetting the taken meds daily, and sending the notification for a refill reminder daily
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

import java.util.Calendar;
import java.util.List;

// Receive alarm broadcast
public class AlarmReceiver extends BroadcastReceiver {
    // Set once and stored for future use
    static int refillNotificationId = NotifyService.notificationId++;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getStringExtra("purpose").equals("refresh_extra")) {
            // Clear medicine taken cells of db every day
            DBHelper db = new DBHelper(context);

            // Also add all taken meds to the calendar table
            List<String> takenMeds = db.getTakenMeds();

            if (takenMeds.isEmpty()) return;  // Leave if no meds

            // Build String from list of Strings
            StringBuilder sb = new StringBuilder();
            for (String s : takenMeds) {
                sb.append(s).append("\n");
            }
            String sbS = sb.toString();
            sbS = sbS.substring(0, sbS.length()-1);

            // Get today's calendar date and add it to db
            Calendar calendar = Calendar.getInstance();

            // Then we need to pass in yesterday because this alarm occurs at 12am
            long lastDay = calendar.getTimeInMillis() - (24 * 60 * 60 * 1000);
            calendar.setTimeInMillis(lastDay);

            db.addMedDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH), sbS);

            db.clearTakenAll();  // Clear taken meds

        } else if (intent.getStringExtra("purpose").equals("refill_reminder")) {
            // Check if notifications are allowed
            SharedPreferences preferences = context.getSharedPreferences(SettingsActivity.prefFile,
                    Context.MODE_PRIVATE);
            if (!preferences.getBoolean(SettingsActivity.reminderSetting, false))
                return;
            if (!preferences.getBoolean(SettingsActivity.notificationSetting, false))
                return;

            // Build string for which pills require refilling after db query
            Double refill_num = Double.parseDouble(intent.getStringExtra("refill_num"));

            DBHelper db = new DBHelper(context);
            String refillMsg = "It is time to refill the following medications:";
            StringBuilder medsRefill = new StringBuilder();
            String medRefill;

            List<String> meds = db.getMedsWithQuantityLessThan(refill_num);

            if (meds.isEmpty()) return;  // no notification if no pills are low

            for (String m : meds) {
                medsRefill.append(m).append(", ");
            }
            medRefill = medsRefill.substring(0, medsRefill.length()-2);

            // Notification will have sound
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // Build the intent attach it to the notification and send the notification to the user
            NotificationManager mNM = (NotificationManager) context.getSystemService(
                    Context.NOTIFICATION_SERVICE);

            Intent intent1 = new Intent(context, MedActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, NewMedActivity.pendingId++, intent1, PendingIntent.FLAG_CANCEL_CURRENT);

            Notification notification = new Notification.Builder(context)
                    .setContentTitle(refillMsg)
                    .setContentText(medRefill)
                    .setSmallIcon(R.mipmap.fmn_app_icon_round)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
            mNM.notify(NotifyService.notificationTag, AlarmReceiver.refillNotificationId,
                    notification);
        }
    }
}
