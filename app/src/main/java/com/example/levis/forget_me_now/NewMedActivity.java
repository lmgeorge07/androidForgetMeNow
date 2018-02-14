/*
Name: Levis George
Date: 12/1/2017
Course: Mobile Device Applications COP4656-01
File name: NewMedActivity.java
Purpose: Implements the controller for the New/Edit medication page
 */

package com.example.levis.forget_me_now;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NewMedActivity extends Activity {
    // Class members
    static int pendingId = 2;  // Id 0 - Daily clear taking pills and Id 1 - Refill reminder
    private DBHelper db;
    private EditText nameET, doseET, doseTimeET1, doseTimeET2, doseTimeET3, quantityET;
    private TextView add_med_err;
    private Button deleteMedBtn;
    private String purpose;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_med);

        // Initialize db helper
        this.db = new DBHelper(this);

        // Initialize inputs
        this.nameET = (EditText) findViewById(R.id.editTextName);
        this.doseET = (EditText) findViewById(R.id.editTextDose);
        this.doseTimeET1 = (EditText) findViewById(R.id.editTextDoseT1);
        this.doseTimeET2 = (EditText) findViewById(R.id.editTextDoseT2);
        this.doseTimeET3 = (EditText) findViewById(R.id.editTextDoseT3);
        this.quantityET = (EditText) findViewById(R.id.editTextQuantity);
        this.add_med_err = (TextView) findViewById(R.id.textViewError);
        this.deleteMedBtn = (Button) findViewById(R.id.buttonDeleteMed);

        // Find out why we arrived at this page: Is it New? or Edit?
        this.purpose = getIntent().getStringExtra("type");
        if (!purpose.equals("new")) {
            // Change header
            TextView headerTxt = (TextView) findViewById(R.id.textViewAddEditMed);
            headerTxt.setText(getText(R.string.edit_med));

            // Initialize inputs to db values if we are editing
            Medications med = this.db.getMed(this.purpose);
            this.nameET.setText(med.getMedName());
            this.doseET.setText(med.getMedDose().toString());
            this.doseTimeET1.setText(med.getDoseTime1());
            this.doseTimeET2.setText(med.getDoseTime2());
            this.doseTimeET3.setText(med.getDoseTime3());
            this.quantityET.setText(med.getMedQuantity().toString());
            this.deleteMedBtn.setVisibility(View.VISIBLE);
        }
    }

    // Clicking the top-left cancel button sends user back to medication list screen
    public void cancelNewMed(View view) {
        startActivity(new Intent(this, MedActivity.class));
        finish();  // Go back to list page
    }

    // User clicks the save button at the top-right to store values to the database
    public void saveNewMed(View view) {
        List<String> doseTimeList = new ArrayList<>();
        String n, d, dt1, dt2, dt3, q;

        // Getting strings from editText inputs
        n = this.nameET.getText().toString();
        d = this.doseET.getText().toString();
        dt1 = this.doseTimeET1.getText().toString();
        dt2 = this.doseTimeET2.getText().toString();
        dt3 = this.doseTimeET3.getText().toString();
        q = this.quantityET.getText().toString();
        String[] doseTimes = {dt1, dt2, dt3};

        // Verify that all required fields filled, and if so, add to database
        if (n.equals("") || d.equals("") || q.equals("") ||
                (dt1.equals("") && dt2.equals("") && dt3.equals(""))) {
            this.add_med_err.setText(getResources().getText(R.string.add_err));
        } else {
            // Save if new med
            try {  // Verify time format
                if ((!dt1.equals("") && !dt1.matches(MainActivity.time_regex)) ||
                        (!dt2.equals("") && !dt2.matches(MainActivity.time_regex)) ||
                        (!dt3.equals("") && !dt3.matches(MainActivity.time_regex))) {
                    throw new Exception();
                }
            } catch (Exception e) {
                this.add_med_err.setText(getResources().getText(R.string.add_err_format));
                return;
            }

            for (String dts : doseTimes) {  // Getting all valid dose times
                if (!dts.equals("")) {
                    doseTimeList.add(dts);
                }
            }

            // Why is the user on this page?
            // If there are here to add a new medication into the db, they add it and prep
            // notifications for each dosage time that exists (at least 1 required)
            if (this.purpose.equals("new")) {  // Add new medication
                this.db.addMed(new Medications(n, Double.parseDouble(d), dt1, dt2, dt3,
                        Double.parseDouble(q)));

                int n1, n2, n3;
                n1 = NotifyService.notificationId++;
                n2 = NotifyService.notificationId++;
                n3 = NotifyService.notificationId++;

                this.db.addNotification(n, String.format("%d", n1), String.format("%d", n2),
                        String.format("%d", n3));
            } else {  // Otherwise update the existing medication
                this.db.updateMed(new Medications(n, Double.parseDouble(d), dt1, dt2, dt3,
                        Double.parseDouble(q)));

                // Delete old notifications for this medication and adding new ones for the
                // new medication times
                List<Integer> noteIds = db.getNoteIds(n);

                // Deleting all notifications belonging to the deleted med
                NotificationManager notificationManager =
                        (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                for (Integer id: noteIds) {
                    notificationManager.cancel(id);
                }

                // Update to new notification ids
                int n1, n2, n3;
                n1 = NotifyService.notificationId++;
                n2 = NotifyService.notificationId++;
                n3 = NotifyService.notificationId++;

                this.db.updateNotification(n, String.format("%d", n1), String.format("%d", n2),
                        String.format("%d", n3), 0, 0, 0);
            }

            // Setting up repeating alarms (which will trigger notifications) for each
            // medication time
            for (String doseTStr: doseTimeList) {
                // Setting alarm
                Intent intent = new Intent(this, NotifyService.class);
                intent.putExtra("name", n);
                intent.putExtra("dose", d);
                intent.putExtra("time", doseTStr);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this, NewMedActivity.pendingId++, intent,
                        PendingIntent.FLAG_CANCEL_CURRENT);

                // Setting up calendar instance with military time to set the alarm
                int min =  Integer.parseInt(doseTStr.substring(doseTStr.indexOf(":")+1,
                        doseTStr.length()-2));
                int stdHour = Integer.parseInt(doseTStr.substring(0, doseTStr.indexOf(":")));
                int milHour = doseTStr.substring(doseTStr.length()-2).
                        toLowerCase().equals("am") ? stdHour % 12 : stdHour == 12 ? stdHour :
                        (stdHour + 12);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, milHour);
                calendar.set(Calendar.MINUTE, min);
                calendar.set(Calendar.SECOND, 0);

                // Set daily reminder --> run the Notify service at this calendar time daily
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY , pendingIntent);
            }

            // Go back to list view after adding/updating medications
            startActivity(new Intent(this, MedActivity.class));
            finish();
        }
    }

    // Only visible on the edit page
    // User clicks the delete button
    public void deleteMed(View view) {
        // Storing the name of the medication
        final String deleteKey = this.purpose;

        // Building alert dialog for user to confirm deicision to delete
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.delete_alert_title);
        adb.setMessage(R.string.delete_alert_msg);
        adb.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete med and finish activity
                DBHelper db = new DBHelper(getApplicationContext());
                db.deleteMed(deleteKey);

                // Get ids to cancel notifications and then remove the med from notification table
                List<Integer> noteIds = db.getNoteIds(deleteKey);

                // Deleting all notifications belonging to the deleted med
                NotificationManager notificationManager =
                        (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                for (Integer id: noteIds) {
                    notificationManager.cancel(id);
                }
                db.deleteNotifications(deleteKey);

                // Go back to list view
                startActivity(new Intent(getApplicationContext(), MedActivity.class));
                finish();
            }
        });
        adb.setNegativeButton(R.string.no, null);
        AlertDialog alertDialog = adb.create();
        alertDialog.show();  // Showing the dialog

        // Giving the buttons unique colors (Blue-YES, Red-NO)
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                getResources().getColor(R.color.colorPrimary));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                getResources().getColor(R.color.colorRed));
    }
}
