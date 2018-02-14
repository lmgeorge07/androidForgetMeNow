/*
Name: Levis George
Date: 12/1/2017
Course: Mobile Device Applications COP4656-01
File name: MedActivity.java
Purpose: Implements the controller for the medication list/new/edit screen
*/

package com.example.levis.forget_me_now;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MedActivity extends Activity {

    // Class members
    private ImageButton todayBtn, medBtn, setBtn;
    private TextView todayTV, medTV, setTV;
    private FrameLayout frameLayout;
    private Button addMedBtn;
    private DBHelper db;

    // Comes here after fragment fills in list on create
    @Override
    protected void onResume() {
        super.onResume();
        // Initialize views
        this.todayBtn = (ImageButton) findViewById(R.id.today_toolbar_btn);
        this.medBtn = (ImageButton) findViewById(R.id.medicine_btn);
        this.setBtn = (ImageButton) findViewById(R.id.settings_btn);
        this.todayTV = (TextView) findViewById(R.id.textViewTodayLbl);
        this.medTV = (TextView) findViewById(R.id.textViewMedLbl);
        this.setTV = (TextView) findViewById(R.id.textViewSettingLbl);
        this.addMedBtn = (Button) findViewById(R.id.buttonAdd);

        // Default highlighted tab for medicine page
        this.medBtn.setAlpha((float) 1);
        this.medTV.setTextColor(getResources().getColor(R.color.colorDarkGray));
        this.medTV.setTypeface(Typeface.DEFAULT_BOLD);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_med);

        this.db = new DBHelper(this);  // Initialize db helper

        if (savedInstanceState == null) {  // First time we create it
            // If we have no meds in the db, we give user a msg
            if (this.db.getMeds().isEmpty()) {
                // Initialize views here b/c not inflating fragment
                /*this.todayBtn = (ImageButton) findViewById(R.id.today_toolbar_btn);
                this.medBtn = (ImageButton) findViewById(R.id.medicine_btn);
                this.setBtn = (ImageButton) findViewById(R.id.settings_btn);
                this.todayTV = (TextView) findViewById(R.id.textViewTodayLbl);
                this.medTV = (TextView) findViewById(R.id.textViewMedLbl);
                this.setTV = (TextView) findViewById(R.id.textViewSettingLbl);
                this.addMedBtn = (Button) findViewById(R.id.buttonAdd);*/

                LayoutInflater inflater = LayoutInflater.from(this);
                View rootView = inflater.inflate(R.layout.empty_list, null);
                this.frameLayout = (FrameLayout) findViewById(R.id.med_container);
                this.frameLayout.addView(rootView);
            } else {  // We inflate fragment with list view of all medications otherwise
                getFragmentManager().beginTransaction().add(R.id.med_container, new MedFragment()).
                        commit();
            }
        }
    }

    // Nested fragment
    public static class MedFragment extends Fragment {
        private DBHelper db;  // DB object

        public MedFragment() {}

        @Override
        public void onDestroy() {
            db.close();  // Close the database
            super.onDestroy();
        }

        // User can interact with fragment
        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Make query to db to see if any med exist for today
            db = new DBHelper(getActivity());
            List<Medications> medObjectsList = db.getMeds();

            // Make button text list
            List<String> medList = new ArrayList<>();
            String pill = "pill";
            DecimalFormat df = new DecimalFormat("0.##");
            for (Medications med: medObjectsList) {
                medList.add(String.format("%s\n%s %s remaining", med.getMedName(),
                        df.format(med.getMedQuantity()),
                        med.getMedQuantity().equals(1.00) ? pill : pill + 's'));
            }

            /* Sorting list by quantity remaining (lowest -> highest) values */
            Collections.sort(medList, new Comparator<String>() {
                // Custom comparator meant to compare my med quantities
                @Override
                public int compare(String o1, String o2) {
                    String temp1 = o1.substring(o1.indexOf('\n') + 1);
                    String temp2 = o2.substring(o2.indexOf('\n') + 1);
                    temp1 = temp1.substring(0, temp1.indexOf(' '));
                    temp2 = temp2.substring(0, temp2.indexOf(' '));

                    Double t1 = Double.parseDouble(temp1);
                    Double t2 = Double.parseDouble(temp2);

                    return t1 > t2 ? 1 : (t1 < t2 ? -1 : 0);
                }
            });

            // Adapter of all texts to populate listss
            final MedAdapter medAdapter = new MedAdapter(getActivity(),
                    R.layout.populated_list, R.id.list_view_med_editable_btn, medList);

            View rootView = inflater.inflate(R.layout.activity_med, container, false);
            ListView listView = (ListView)  rootView.findViewById(R.id.list_view_meds);

            // Populating list view
            listView.setAdapter(medAdapter);
            return rootView;
        }

        // Making a custom adapter for the list view on the 'Med List' page
        public class MedAdapter extends ArrayAdapter<String> {

            public MedAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
                super(context, resource, textViewResourceId, objects);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                String name, quant;  // Store visible strings on list

                LayoutInflater inflater = LayoutInflater.from(getContext());
                View v = inflater.inflate(R.layout.populated_list, parent, false);

                // Initialize views
                TextView nTv = (TextView) v.findViewById(R.id.list_view_med_editable_btn);
                TextView qTv = (TextView) v.findViewById(R.id.list_view_med_quant_editable_btn);
                RelativeLayout rL = (RelativeLayout) v.findViewById(R.id.rel_med_list);
                String strVal = getItem(position);

                // Get correct strings from the formatted text
                name = quant = strVal;
                name = name.substring(0, name.indexOf("\n"));
                quant = quant.substring(quant.indexOf('\n')+1);

                // Setting text view values
                nTv.setText(name);
                qTv.setText(quant);

                // If the user has a quantity refill reminder, we notify them with a msg when
                // the fragment is loaded and visible
                SharedPreferences preferences = getActivity().getSharedPreferences(
                        SettingsActivity.prefFile, MODE_PRIVATE);
                if (preferences.getBoolean(SettingsActivity.reminderSetting, false)) {
                    if (!preferences.getString(SettingsActivity.reminderNum, "").
                            isEmpty()) {
                        if (Double.parseDouble(quant.substring(0, quant.indexOf(' '))) <=
                                Double.parseDouble(preferences.getString(
                                        SettingsActivity.reminderNum, "0.0"))) {
                            nTv.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            qTv.setTextColor(getResources().getColor(R.color.colorDarkGray));
                            qTv.setText(String.format("%s. It is time for a refill", quant));
                        }
                    }
                }
                return v;
            }
        }
    }

    // User clicks on button to add medicine
    public void addMedicine(View view) {
        this.addMedBtn.setTypeface(Typeface.DEFAULT_BOLD);  // Highlight button
        Intent intent = new Intent(this, NewMedActivity.class);
        intent.putExtra("type", "new");  // Pass in purpose
        startActivity(intent);  // Go to new page activity
        finish();
    }

    // User clicks on an existing medicine in the list
    public void editMed(View view) {
        RelativeLayout b = (RelativeLayout) view;  // Bind the layout

        // Grab children
        TextView nameTV = b.findViewById(R.id.list_view_med_editable_btn);
        TextView quantTV = b.findViewById(R.id.list_view_med_quant_editable_btn);

        // Show button click by changing color
        nameTV.setTextColor(getResources().getColor(R.color.colorWhite));
        quantTV.setTextColor(getResources().getColor(R.color.colorWhite));
        b.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        // Setup intent and pass data to new activity screen
        Intent intent = new Intent(this, NewMedActivity.class);
        intent.putExtra("type", nameTV.getText().toString());
        startActivity(intent);
        finish();
    }

    /* Toolbar navigation */

    // Can be empty function. Just staying on this page
    public void goToMedicineActivity(View view) { }

    // Open up main activity
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

        // Bring up today/main page
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    // Go to settings tab
    public void goToSettings(View view) {
        // Reset other colors
        this.medBtn.setAlpha((float) 0.7);
        this.medTV.setTextColor(getResources().getColor(R.color.colorGray));
        this.medTV.setTypeface(Typeface.DEFAULT);
        this.todayBtn.setAlpha((float) 0.7);
        this.todayTV.setTextColor(getResources().getColor(R.color.colorGray));
        this.todayTV.setTypeface(Typeface.DEFAULT);

        // Highlight clicked button
        this.setBtn.setAlpha((float) 1);
        this.setTV.setTextColor(getResources().getColor(R.color.colorDarkGray));
        this.setTV.setTypeface(Typeface.DEFAULT_BOLD);

        startActivity(new Intent(this, SettingsActivity.class));
        finish();
    }
}
