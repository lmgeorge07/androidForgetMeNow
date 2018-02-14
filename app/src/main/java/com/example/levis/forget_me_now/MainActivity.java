/*
Name: Levis George
Date: 12/1/2017
Course: Mobile Device Applications COP4656-01
File name: MainActivity.java
Purpose: Implements the controller for the main page ('Today' screen)
 */

package com.example.levis.forget_me_now;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

// The main 'Today' screen controller
public class MainActivity extends Activity {

    // Main class members
    static final String time_regex = "([1-9]|0[1-9]|1[0-2]):[0-5][0-9](AM|PM|am|pm)";
    static int scroll_pos_index = 0;

    private TextView msgMainTV;
    private ImageButton todayBtn, medBtn, setBtn;
    private TextView todayTV, medTV, setTV;
    private FrameLayout frameLayout;
    private DBHelper db;

    @Override
    protected void onDestroy() {
        this.db.close();  // Close database when activity is gone

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        /* Set today's date on the top-left calendar if we don't enter fragment */
        Calendar calendar = Calendar.getInstance();
        String week_day = (String) android.text.format.DateFormat.format(
                "EEEE", new Date());
        String month_name = (String) android.text.format.DateFormat.format(
                "MMMM", new Date());
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        TextView dayTV = (TextView) findViewById(R.id.textViewDay);
        TextView monthTV = (TextView) findViewById(R.id.textViewDate);

        dayTV.setText(week_day);
        monthTV.setText(String.format("%s %d", month_name, day));

        // Initialize views to use when  navigating with toolbar
        this.todayBtn = (ImageButton) findViewById(R.id.today_toolbar_btn);
        this.medBtn = (ImageButton) findViewById(R.id.medicine_btn);
        this.setBtn = (ImageButton) findViewById(R.id.settings_btn);
        this.todayTV = (TextView) findViewById(R.id.textViewTodayLbl);
        this.medTV = (TextView) findViewById(R.id.textViewMedLbl);
        this.setTV = (TextView) findViewById(R.id.textViewSettingLbl);
        this.msgMainTV = (TextView) findViewById(R.id.textViewMainMsg);

        // Default toolbar highlights
        this.todayBtn.setAlpha((float) 1);
        this.todayTV.setTextColor(getResources().getColor(R.color.colorDarkGray));
        this.todayTV.setTypeface(Typeface.DEFAULT_BOLD);

        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);

        // Initialize database helper object
        this.db = new DBHelper(this);

        if (savedInstanceState == null) {  // First time we create it
            if (this.db.getMeds().isEmpty()) {
                // Putting the "No meds" view on screen if there are no meds in the db
                LayoutInflater inflater = LayoutInflater.from(this);
                View rootView = inflater.inflate(R.layout.empty_today, null);
                this.frameLayout = (FrameLayout) findViewById(R.id.today_container);
                this.frameLayout.addView(rootView);
            } else {  // If we have meds, put the fragment up
                getFragmentManager().beginTransaction().add(
                        R.id.today_container, new TodayFragment()).commit();
            }
        }
    }

    // Nested fragment class to use with the main activity
    public static class TodayFragment extends Fragment {
        // Class members
        private DBHelper db;
        private View v;
        private TextView msgMainTV;

        @Override
        public void onDestroy() {
            db.close();  // Close local db helper when fragment is gone
            super.onDestroy();
        }

        // Fragment is visible and user interactable here
        @Override
        public void onResume() {
            // Update the checkbox and text based on whether the all the medications are taken
            ImageButton chkbox = (ImageButton) v.findViewById(R.id.checkbox_msg);

            if (this.db.isAllTaken()) {
                msgMainTV.setText(R.string.msg_all_meds_taken);
                chkbox.setContentDescription(getString(R.string.checked));
                chkbox.setImageResource(android.R.drawable.checkbox_on_background);
            } else {
                msgMainTV.setText(R.string.msg_leftover_meds);
                chkbox.setContentDescription(getString(R.string.unchecked));
                chkbox.setImageResource(android.R.drawable.checkbox_off_background);
            }
            super.onResume();
        }

        // Fragment created
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Remove any old views in the container to avoid overlap on the screen
            container.removeAllViews();

            View rootView = inflater.inflate(R.layout.activity_main, container, false);
            v = rootView;  // Update class member

            /* Set today's date on the top-left calendar */
            Calendar calendar = Calendar.getInstance();
            String week_day = (String) android.text.format.DateFormat.format(
                    "EEEE", new Date());
            String month_name = (String) android.text.format.DateFormat.format(
                    "MMMM", new Date());
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            TextView dayTV = (TextView) rootView.findViewById(R.id.textViewDay);
            TextView monthTV = (TextView) rootView.findViewById(R.id.textViewDate);
            msgMainTV = (TextView) rootView.findViewById(R.id.textViewMainMsg);

            dayTV.setText(week_day);
            monthTV.setText(String.format("%s %d", month_name, day));

            /* Set default toolbar color - today button highlighted */
            ImageButton todayBtn = (ImageButton) rootView.findViewById(R.id.today_toolbar_btn);
            TextView todayTV = (TextView)rootView.findViewById(R.id.textViewTodayLbl);
            todayBtn.setAlpha((float) 1);
            todayTV.setTextColor(getResources().getColor(R.color.colorDarkGray));
            todayTV.setTypeface(Typeface.DEFAULT_BOLD);

            db = new DBHelper(getActivity());  // Initialize new db handler

            // Make query to db to see if any med exist for today
            List<Medications> medObjectsList = db.getMeds();
            List<String> medList = new ArrayList<>();

            /* Load medication list of strings */
            DecimalFormat df = new DecimalFormat("0.##");
            String pillStr = "pill";
            for (Medications med: medObjectsList) {
                if (!med.getDoseTime1().equals("")) {
                    medList.add(String.format(
                            "%s\n%s\nTake %s %s", med.getDoseTime1(), med.getMedName(),
                            df.format(med.getMedDose()),
                            med.getMedDose().equals(1.00) ? pillStr : pillStr + 's'));
                }
                if (!med.getDoseTime2().equals("")) {
                    medList.add(String.format(
                            "%s\n%s\nTake %s %s", med.getDoseTime2(), med.getMedName(),
                            df.format(med.getMedDose()),
                            med.getMedDose().equals(1.00) ? pillStr : pillStr + 's'));
                }
                if (!med.getDoseTime3().equals("")) {
                    medList.add(String.format(
                            "%s\n%s\nTake %s %s", med.getDoseTime3(), med.getMedName(),
                            df.format(med.getMedDose()),
                            med.getMedDose().equals(1.00) ? pillStr : pillStr + 's'));
                }
            }

            /* Sorting list by time values */
            Collections.sort(medList, new Comparator<String>() {
                // Custom comparator meant to compare my weird time strings
                @Override
                public int compare(String o1, String o2) {
                    String temp1 = o1.substring(0, o1.indexOf('\n'));
                    String temp2 = o2.substring(0, o2.indexOf('\n'));
                    Integer hr1, min1, hr2, min2;

                    String shr1 = temp1.substring(0, temp1.indexOf(':'));
                    String shr2 = temp2.substring(0, temp2.indexOf(':'));

                    String smin1 = temp1.substring(temp1.indexOf(':') + 1);
                    String smin2 = temp2.substring(temp2.indexOf(':') + 1);

                    String tm1 = smin1.substring(2);
                    String tm2 = smin2.substring(2);

                    smin1 = smin1.substring(0, 2);
                    smin2 = smin2.substring(0, 2);

                    hr1 = Integer.parseInt(shr1);
                    hr2 = Integer.parseInt(shr2);
                    min1 = Integer.parseInt(smin1);
                    min2 = Integer.parseInt(smin2);

                    Integer milHour1 = tm1.toLowerCase().equals("am") ? hr1 % 12 :
                            hr1 == 12 ? hr1 : (hr1 + 12);

                    Integer milHour2 = tm2.toLowerCase().equals("am") ? hr2 % 12 :
                            hr2 == 12 ? hr2 : (hr2 + 12);

                    return milHour1 > milHour2 ? 1 : (milHour1 < milHour2 ? -1 :
                            (min1 > min2 ? 1 : (min1 < min2 ? -1 : 0)));
                }
            });


            /* Fill the adapter with the string list and populate the list view */
            final TodayAdapter medAdapter = new TodayAdapter(getActivity(),
                    R.layout.list_item, R.id.list_item_1_today_btn, medList);
            ListView listView = (ListView)  rootView.findViewById(R.id.list_view_today);
            listView.setAdapter(medAdapter);
            listView.setSelection(MainActivity.scroll_pos_index);  // Stay scrolled at current pos

            return rootView;
        }

        // Making a custom adapter for the list view on the 'Today' page
        public class TodayAdapter extends ArrayAdapter<String> {

            public TodayAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
                super(context, resource, textViewResourceId, objects);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                String time, name, dose;  // Store visible strings on list

                LayoutInflater inflater = LayoutInflater.from(getContext());
                View v = inflater.inflate(R.layout.list_item, parent, false);

                // Initialize views
                ImageButton b = (ImageButton) v.findViewById(R.id.list_item_1_today_btn);
                TextView tTv = (TextView) v.findViewById(R.id.today_btn_time_tv);
                TextView mTv = (TextView) v.findViewById(R.id.today_btn_med_tv);
                TextView dTv = (TextView) v.findViewById(R.id.today_btn_dose_tv);
                RelativeLayout rL = (RelativeLayout) v.findViewById(R.id.rel_checked_today);
                String strVal = getItem(position);

                // Get correct strings from the formatted text
                time = name = dose = strVal;
                time = time.substring(0, time.indexOf("\n"));
                name = name.substring(name.indexOf("\n") + 1);
                name = name.substring(0, name.indexOf("\n"));
                dose = dose.substring(dose.indexOf("\n") + 1);
                dose = dose.substring(dose.indexOf("\n") + 1);

                // Setting text view values
                tTv.setText(time);
                mTv.setText(name);
                dTv.setText(dose);

                // Highlight and check checkboxes of all taken medication list items
                if (db.isChecked(name, time)) {
                    b.setContentDescription("checked");
                    b.setImageResource(android.R.drawable.checkbox_on_background);
                    rL.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    mTv.setTextColor(getResources().getColor(R.color.colorWhite));
                    tTv.setTextColor(getResources().getColor(R.color.colorWhite));
                    dTv.setTextColor(getResources().getColor(R.color.colorWhite));

                    String timestamp = db.getMedTimeStamp(name, time);
                    dTv.setText(String.format("%s taken at %s", dose.substring(dose.indexOf(' ')+1),
                            timestamp));

                } else {
                    b.setContentDescription("unchecked");
                    b.setImageResource(android.R.drawable.checkbox_off_background);
                    rL.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    mTv.setTextColor(getResources().getColor(R.color.colorDarkGray));
                    tTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                    dTv.setTextColor(getResources().getColor(R.color.colorGray));
                }
                return v;
            }
        }
    }

    // User 'takes' a medication by clicking the corresponding checkbox
    public void takeMed(View view) {
        String time, name;

        //Initialize views
        ImageButton b = (ImageButton) view;  // Bind button
        TextView nameTV = (TextView) ((View) view.getParent()).findViewById(R.id.today_btn_med_tv);
        TextView timeTV = (TextView) ((View) view.getParent()).findViewById(R.id.today_btn_time_tv);
        TextView doseTV = (TextView) ((View) view.getParent()).findViewById(R.id.today_btn_dose_tv);
        RelativeLayout rL = (RelativeLayout) ((View) view.getParent()).
                findViewById(R.id.rel_checked_today);

        // Get string values to query database
        time = timeTV.getText().toString();
        name = nameTV.getText().toString();

        // If the medicine was already checked (taken), then uncheck it and 'untake' it
        if (this.db.isChecked(name, time)) {  // Uncheck the box and highlight the list item
            this.db.setChecked(name, time, 0);
            b.setContentDescription("unchecked");
            b.setImageResource(android.R.drawable.checkbox_off_background);
            rL.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            nameTV.setTextColor(getResources().getColor(R.color.colorDarkGray));
            timeTV.setTextColor(getResources().getColor(R.color.colorPrimary));
            doseTV.setTextColor(getResources().getColor(R.color.colorGray));
        } else {  // Check the box, and highlight the list item
            this.db.setChecked(name, time, 1);
            b.setContentDescription("checked");
            b.setImageResource(android.R.drawable.checkbox_on_background);
            rL.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            nameTV.setTextColor(getResources().getColor(R.color.colorWhite));
            timeTV.setTextColor(getResources().getColor(R.color.colorWhite));
            doseTV.setTextColor(getResources().getColor(R.color.colorWhite));
        }
        MainActivity.scroll_pos_index = ((ListView)((View) view.getParent()).getParent()).
                getPositionForView(view);

        // Update list by replacing fragment
        getFragmentManager().beginTransaction().replace(
                R.id.today_container, new TodayFragment()).commit();
    }

    // User hits checkbox that can unselect & select all meds with a single-click
    public void checkAllMeds(View view) {
        // Initialize views
        ImageButton chkbox = (ImageButton) view;
        ListView listView = (ListView)  findViewById(R.id.list_view_today);

        String medName, medTime;  // Db query strings
        boolean checked = chkbox.getContentDescription() == getString(R.string.checked);

        // For each item in the list, if I am checking the box, I check every unchecked item
        // If I am unchecking the box, I uncheck every checked item
        for (int i = 0; i < listView.getAdapter().getCount(); i++) {
            TextView medNameTV = (TextView) listView.getAdapter().getView(
                    i, null, listView).findViewById(R.id.today_btn_med_tv);
            TextView medTimeTV = (TextView) listView.getAdapter().getView(
                    i, null, listView).findViewById(R.id.today_btn_time_tv);
            medName = medNameTV.getText().toString();
            medTime = medTimeTV.getText().toString();

            // Update db with currect 'medicine taken' value
            if (checked && this.db.isChecked(medName, medTime)) {
                if (this.db.isChecked(medName, medTime)) {
                    this.db.setChecked(medName, medTime, 0);
                }
            } else {
                if (!this.db.isChecked(medName, medTime)) {
                    this.db.setChecked(medName, medTime, 1);
                }
            }
        }
        // Replace fragment to get updated list
        getFragmentManager().beginTransaction().replace(
                R.id.today_container, new TodayFragment()).commit();
    }


    public void expandCalendar(View view) {
        // Go to calendar
        startActivity(new Intent(this, CalendarActivity.class));
        finish();
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
    public void goToToday(View view) {}

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
