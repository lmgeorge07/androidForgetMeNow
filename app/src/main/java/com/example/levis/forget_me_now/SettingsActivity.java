/*
Name: Levis George
Date: 12/1/2017
Course: Mobile Device Applications COP4656-01
File name: SettingsActivity.java
Purpose: Implements the controller for the settings page. Has 2 options: On/Off refill reminder and
On/Off notifications.
 */

package com.example.levis.forget_me_now;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

import java.util.Calendar;

public class SettingsActivity extends Activity {
    // Constant shared preference keys
    static final String prefFile = "SettingsPreferences";
    static final String reminderSetting = "ReminderSetting";
    static final String notificationSetting = "NotificationSetting";
    static final String reminderNum = "ReminderNum";

    // Class members
    private SharedPreferences preferences;
    private Switch rrSwitch, nSwitch;
    private ImageButton todayBtn, medBtn, setBtn;
    private TextView todayTV, medTV, setTV;
    private TextView msg1, msg2;
    private EditText remindWhen;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize views
        this.todayBtn = (ImageButton) findViewById(R.id.today_toolbar_btn);
        this.medBtn = (ImageButton) findViewById(R.id.medicine_btn);
        this.setBtn = (ImageButton) findViewById(R.id.settings_btn);
        this.todayTV = (TextView) findViewById(R.id.textViewTodayLbl);
        this.medTV = (TextView) findViewById(R.id.textViewMedLbl);
        this.setTV = (TextView) findViewById(R.id.textViewSettingLbl);

        this.msg1 = (TextView) findViewById(R.id.textViewSetMsg1);
        this.msg2 = (TextView) findViewById(R.id.textViewSetMsg2);
        this.remindWhen = (EditText) findViewById(R.id.editTextRefillNum);

        this.rrSwitch = (Switch) findViewById(R.id.switchRefillReminder);
        this.nSwitch = (Switch) findViewById(R.id.switchNotifications);

        // By default, the settings button on the toolbar is highlighted
        this.setBtn.setAlpha((float) 1);
        this.setTV.setTextColor(getResources().getColor(R.color.colorDarkGray));
        this.setTV.setTypeface(Typeface.DEFAULT_BOLD);

        // Get shared preferences & update in new activity
        this.preferences = getApplicationContext().getSharedPreferences(
                SettingsActivity.prefFile, MODE_PRIVATE);
        boolean isChecked = this.preferences.getBoolean(
                SettingsActivity.reminderSetting, false);
        boolean notSetting = this.preferences.getBoolean(
                SettingsActivity.notificationSetting, false);
        updateRRSetting(isChecked, notSetting);

        // Set listener on switches & input
        this.rrSwitch.setOnCheckedChangeListener(switchListener);
        this.nSwitch.setOnCheckedChangeListener(notListener);

        // Set listener on input for reminder quantity threshold
        this.remindWhen.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")) return;  // Check that there is a value

                // Update shared preferences with new settings
                SharedPreferences preferences = getApplicationContext().getSharedPreferences(
                        SettingsActivity.prefFile, MODE_PRIVATE);
                SharedPreferences.Editor prefEditor = preferences.edit();
                prefEditor.putString(SettingsActivity.reminderNum, s.toString());
                prefEditor.commit();  // or apply

                // Initiate reminder with the value entered
                AlarmManager alarmMgr;
                PendingIntent alarmIntent;

                alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
                intent.putExtra("purpose", "refill_reminder");
                intent.putExtra("refill_num", s.toString());

                // Alarm will occur at 12:00pm daily
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, 12);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                long timeInMillis = calendar.getTimeInMillis();
                if(timeInMillis-System.currentTimeMillis()<0){
                    //if its in past, add one day
                    timeInMillis += 86400000L;
                }

                // Setting the alarm
                alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 1,
                        intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis,
                        AlarmManager.INTERVAL_DAY, alarmIntent);
            }
        });
    }

    // Ran during activity creation: updates switches/inputs with shared pref stored settings
    public void updateRRSetting(boolean checkedVal, boolean notVal) {
        this.rrSwitch.setChecked(checkedVal);
        this.nSwitch.setChecked(notVal);
        if (checkedVal) {
            this.rrSwitch.setTextColor(getResources().getColor(R.color.colorGray));
            this.msg1.setVisibility(View.VISIBLE);
            this.remindWhen.setVisibility(View.VISIBLE);
            this.msg2.setVisibility(View.VISIBLE);
            this.remindWhen.setText(this.preferences.getString(
                    SettingsActivity.reminderNum, ""));
        } else {
            this.rrSwitch.setTextColor(getResources().getColor(R.color.colorLightishGray));
            this.msg1.setVisibility(View.INVISIBLE);
            this.remindWhen.setVisibility(View.INVISIBLE);
            this.msg2.setVisibility(View.INVISIBLE);
        }
        if (notVal) {
            this.nSwitch.setTextColor(getResources().getColor(R.color.colorGray));
        } else {
            this.nSwitch.setTextColor(getResources().getColor(R.color.colorLightishGray));
        }
    }

    // Initialize refill reminder switch listener
    CompoundButton.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            TextView msg1 = (TextView) findViewById(R.id.textViewSetMsg1);
            TextView msg2 = (TextView) findViewById(R.id.textViewSetMsg2);
            TextView remindWhen = (EditText) findViewById(R.id.editTextRefillNum);

            // Update shared preferences with new settings
            SharedPreferences preferences = getApplicationContext().getSharedPreferences(
                    SettingsActivity.prefFile, MODE_PRIVATE);
            SharedPreferences.Editor prefEditor = preferences.edit();
            prefEditor.putBoolean(SettingsActivity.reminderSetting, isChecked);
            prefEditor.commit();  // or apply
            if (isChecked) {
                buttonView.setTextColor(getResources().getColor(R.color.colorGray));
                msg1.setVisibility(View.VISIBLE);
                remindWhen.setVisibility(View.VISIBLE);
                msg2.setVisibility(View.VISIBLE);
            } else {
                buttonView.setTextColor(getResources().getColor(R.color.colorLightishGray));
                msg1.setVisibility(View.INVISIBLE);
                remindWhen.setVisibility(View.INVISIBLE);
                remindWhen.setText("");
                msg2.setVisibility(View.INVISIBLE);

                prefEditor.putString(SettingsActivity.reminderNum, "");
                prefEditor.commit();  // or apply

                // Cancel notification reminder
                // Unnecesary because I check in AlarmReceiver.java the value of reminderSetting
                // to decide if the notification will be sent
                /*NotificationManager notificationManager =
                        (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(AlarmReceiver.refillNotificationId);*/
            }
        }
    };

    // Initialize notification switch listener
    CompoundButton.OnCheckedChangeListener notListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // Update shared preferences with new settings
            SharedPreferences preferences = getApplicationContext().getSharedPreferences(
                    SettingsActivity.prefFile, MODE_PRIVATE);
            SharedPreferences.Editor prefEditor = preferences.edit();
            prefEditor.putBoolean(SettingsActivity.notificationSetting, isChecked);
            prefEditor.commit();  // or apply
            if (isChecked) {
                buttonView.setTextColor(getResources().getColor(R.color.colorGray));
            } else {
                buttonView.setTextColor(getResources().getColor(R.color.colorLightishGray));
            }
        }
    };

    /* Toolbar tab traversing */

    public void goToMedicineActivity(View view) {
        // Reset other colors
        this.todayBtn.setAlpha((float) 0.7);
        this.todayTV.setTextColor(getResources().getColor(R.color.colorGray));
        this.todayTV.setTypeface(Typeface.DEFAULT);
        this.setBtn.setAlpha((float) 0.7);
        this.setTV.setTextColor(getResources().getColor(R.color.colorGray));
        this.setTV.setTypeface(Typeface.DEFAULT);

        // Highlight clicked button
        this.medBtn.setAlpha((float) 1);
        this.medTV.setTextColor(getResources().getColor(R.color.colorDarkGray));
        this.medTV.setTypeface(Typeface.DEFAULT_BOLD);
        //getFragmentManager().beginTransaction().add(R.id.container, new MedFragment()).commit();

        // Go to medicine list screen
        startActivity(new Intent(this, MedActivity.class));
        finish();
    }

    public void goToToday(View view) {
        // Reset other colors
        this.medBtn.setAlpha((float) 0.7);
        this.medTV.setTextColor(getResources().getColor(R.color.colorGray));
        this.medTV.setTypeface(Typeface.DEFAULT);
        this.setBtn.setAlpha((float) 0.7);
        this.setTV.setTextColor(getResources().getColor(R.color.colorGray));
        this.setTV.setTypeface(Typeface.DEFAULT);

        // Highlight clicked button
        this.todayBtn.setAlpha((float) 1);
        this.todayTV.setTextColor(getResources().getColor(R.color.colorDarkGray));
        this.todayTV.setTypeface(Typeface.DEFAULT_BOLD);

        // Go to the main page
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    // Essentially does nothing in this context: should just replace with empty function
    public void goToSettings(View view) {}
}
