/*
Name: Levis George
Date: 12/1/2017
Course: Mobile Device Applications COP4656-01
File name: CalendarActivity.java
Purpose: Implements the controller for the calendar view. User can click on dates and see which
medications they ate that day.
 */

package com.example.levis.forget_me_now;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.List;


public class CalendarActivity extends Activity {
    private CalendarView calV;
    private TextView calMsg;
    private DBHelper db;

    // Toolbar members
    private ImageButton todayBtn, medBtn, setBtn;
    private TextView todayTV, medTV, setTV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_calendar);

        // Initialize views to use when  navigating with toolbar
        this.todayBtn = (ImageButton) findViewById(R.id.today_toolbar_btn);
        this.medBtn = (ImageButton) findViewById(R.id.medicine_btn);
        this.setBtn = (ImageButton) findViewById(R.id.settings_btn);
        this.todayTV = (TextView) findViewById(R.id.textViewTodayLbl);
        this.medTV = (TextView) findViewById(R.id.textViewMedLbl);
        this.setTV = (TextView) findViewById(R.id.textViewSettingLbl);

        // Initialize db helper & views
        this.db = new DBHelper(this);
        this.calV = (CalendarView) findViewById(R.id.calView);
        this.calMsg = (TextView) findViewById(R.id.textViewCalList);

        // Set todays currently taken meds
        List<String> medsTaken = this.db.getTakenMeds();

        if (!medsTaken.isEmpty()) {
            // Build String from list of Strings
            StringBuilder sb = new StringBuilder();
            for (String s : medsTaken) {
                sb.append(s).append("\n");
            }
            String sbS = sb.toString();
            sbS = sbS.substring(0, sbS.length()-1);

            // Set the text
            this.calMsg.setText(sbS);
            this.calMsg.setTextColor(getResources().getColor(R.color.colorPrimary));
        } else {
            // No meds on create
            calMsg.setTextColor(getResources().getColor(R.color.colorLightishGray));
            calMsg.setText(getText(R.string.noMedsCal));
        }

        // Listener for when calendar date changes
        this.calV.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NotNull
                    CalendarView view, int year, int month, int dayOfMonth) {
                TextView calMsg = (TextView) findViewById(R.id.textViewCalList);
                DBHelper db = new DBHelper(getApplicationContext());

                // If day is today, don't query db, just update with currently checked meds
                Calendar calendar = Calendar.getInstance();
                if ((year == calendar.get(Calendar.YEAR)) &&
                        (month == calendar.get(Calendar.MONTH)) &&
                        (dayOfMonth == calendar.get(Calendar.DAY_OF_MONTH))) {
                    calMsg.setTextColor(getResources().getColor(R.color.colorPrimary));

                    // Set todays currently taken meds
                    List<String> medsTaken = db.getTakenMeds();

                    if (!medsTaken.isEmpty()) {
                        // Build String from list of Strings
                        StringBuilder sb = new StringBuilder();
                        for (String s : medsTaken) {
                            sb.append(s).append("\n");
                        }
                        String sbS = sb.toString();
                        sbS = sbS.substring(0, sbS.length()-1);

                        // Set the text
                        calMsg.setText(sbS);
                    } else {
                        calMsg.setTextColor(getResources().getColor(R.color.colorLightishGray));
                        calMsg.setText(getText(R.string.noMedsCal));
                    }
                } else {
                    // Query calendar database for medications taken on that date
                    String meds = db.getMedsOnDate(year, month, dayOfMonth);
                    if (!meds.isEmpty()) {
                        calMsg.setText(meds);
                        calMsg.setTextColor(getResources().getColor(R.color.colorPrimary));
                    } else {
                        calMsg.setText(getText(R.string.noMedsCal));
                        calMsg.setTextColor(getResources().getColor(R.color.colorLightishGray));
                    }
                }
            }
        });
    }

    /* Toolbar navigation methods */
    // Going to Medicine list screen
    public void goToMedicineActivity(View view) {
        // Reset all other toolbar buttons
        this.todayBtn.setAlpha((float) 0.7);
        this.todayTV.setTextColor(getResources().getColor(R.color.colorGray));
        this.todayTV.setTypeface(Typeface.DEFAULT);
        this.setBtn.setAlpha((float) 0.7);
        this.setTV.setTextColor(getResources().getColor(R.color.colorGray));
        this.setTV.setTypeface(Typeface.DEFAULT);

        // 'Medicine' button highlighted
        this.medBtn.setAlpha((float) 1);
        this.medTV.setTextColor(getResources().getColor(R.color.colorDarkGray));
        this.medTV.setTypeface(Typeface.DEFAULT_BOLD);

        // Go to medicine list screen
        startActivity(new Intent(this, MedActivity.class));
        finish();
    }

    // Remaining on Today screen: should just replace with empty function
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

    // Navigating to the settings screen
    public void goToSettings(View view) {
        // Reset all other toolbar buttons
        this.medBtn.setAlpha((float) 0.7);
        this.medTV.setTextColor(getResources().getColor(R.color.colorGray));
        this.medTV.setTypeface(Typeface.DEFAULT);
        this.todayBtn.setAlpha((float) 0.7);
        this.todayTV.setTextColor(getResources().getColor(R.color.colorGray));
        this.todayTV.setTypeface(Typeface.DEFAULT);

        // Highlight 'Settings' Button
        this.setBtn.setAlpha((float) 1);
        this.setTV.setTextColor(getResources().getColor(R.color.colorDarkGray));
        this.setTV.setTypeface(Typeface.DEFAULT_BOLD);

        // Go to settings screen
        startActivity(new Intent(this, SettingsActivity.class));
        finish();
    }

}
