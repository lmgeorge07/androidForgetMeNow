/*
Name: Levis George
Date: 12/1/2017
Course: Mobile Device Applications COP4656-01
File name: DBHelper.java
Purpose: Implements the database helper and CRUD functions for utilizing SQLite in this app
 */

package com.example.levis.forget_me_now;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;  // Increment to upgrade db

    // Database Name
    private static final String DATABASE_NAME = "medicationDB";

    // Meds main table name
    private static final String TABLE_MEDS = "medications";

    // Meds notification/reminder table name
    private static final String TABLE_NOTIFICATIONS = "notifications";

    // Meds calendar date table name
    private static final String TABLE_CALENDAR = "calendar";

    // Meds Table Columns names
    private static final String KEY_NAME = "name";
    private static final String KEY_DOSE = "dose";
    private static final String KEY_DOSET1 = "doseT1";
    private static final String KEY_DOSET2 = "doseT2";
    private static final String KEY_DOSET3 = "doseT3";
    private static final String KEY_QUANTITY = "quantity";

    // Notification columns
    private static final String KEY_N_NAME = "name";
    private static final String KEY_N_ID1 = "noteID1";
    private static final String KEY_N_ID2 = "noteID2";
    private static final String KEY_N_ID3 = "noteID3";
    private static final String KEY_N_TAKEN1 = "taken1";
    private static final String KEY_N_TAKEN2 = "taken2";
    private static final String KEY_N_TAKEN3 = "taken3";

    private static final String KEY_N_TAKEN_TIME1 = "taken_time1";
    private static final String KEY_N_TAKEN_TIME2 = "taken_time2";
    private static final String KEY_N_TAKEN_TIME3 = "taken_time3";

    // Calendar columns
    private static final String KEY_CAL_DATE = "date";
    private static final String KEY_CAL_MEDS_TAKEN = "medsTaken";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create main med table
        String CREATE_MED_TABLE = "CREATE TABLE " + TABLE_MEDS + "("
                + KEY_NAME + " TEXT PRIMARY KEY," + KEY_DOSE + " REAL," +
                KEY_DOSET1 + " TEXT," + KEY_DOSET2 + " TEXT," + KEY_DOSET3 + " TEXT,"
                + KEY_QUANTITY + " REAL" + ")";
        db.execSQL(CREATE_MED_TABLE);

        // Create notification table
        String CREATE_NOTE_TABLE = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                + KEY_NAME + " TEXT PRIMARY KEY," + KEY_N_ID1 + " TEXT," +
                KEY_N_ID2 + " TEXT," + KEY_N_ID3 + " TEXT," + KEY_N_TAKEN1 + " INTEGER,"
                + KEY_N_TAKEN2 + " INTEGER," + KEY_N_TAKEN3 + " INTEGER, " + KEY_N_TAKEN_TIME1 +
                " TEXT," + KEY_N_TAKEN_TIME2 + " TEXT," + KEY_N_TAKEN_TIME3 + " TEXT" + ")";
        db.execSQL(CREATE_NOTE_TABLE);

        // Create calendar table
        String CREATE_CAL_TABLE = "CREATE TABLE " + TABLE_CALENDAR + "("
                + KEY_CAL_DATE+ " TEXT PRIMARY KEY," + KEY_CAL_MEDS_TAKEN + " TEXT" + ")";
        db.execSQL(CREATE_CAL_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALENDAR);

        // Create tables again
        onCreate(db);
    }

    /********************** CRUD(Create, Read, Update, Delete) Operations *************************/

    // Adding new medication
    public void addMed(Medications med) {
        SQLiteDatabase db = this.getWritableDatabase();

        String INSERT_QUERY = "INSERT INTO " + TABLE_MEDS + " VALUES('" +
                med.getMedName() + "', '" + med.getMedDose() + "', '" + med.getDoseTime1() +
                "', '" + med.getDoseTime2() + "', '" + med.getDoseTime3() + "', '" +
                med.getMedQuantity() + "')";
        db.execSQL(INSERT_QUERY);
        db.close(); // Close database connection
    }

    // Deleting a medication
    public void deleteMed(String medName) {
        SQLiteDatabase db = this.getWritableDatabase();

        String DELETE_QUERY = "DELETE FROM " + TABLE_MEDS + " WHERE " + KEY_NAME + " = " +
                "'" + medName + "'";

        db.execSQL(DELETE_QUERY);

        String DELETE_QUERY1 = "DELETE FROM " + TABLE_NOTIFICATIONS + " WHERE " + KEY_N_NAME + " = " +
                "'" + medName + "'";
        db.execSQL(DELETE_QUERY1);

        db.close();  // Close db connection
    }

    // Updating a medication
    public void updateMed(Medications med) {
        SQLiteDatabase db = this.getWritableDatabase();

        String UPDATE_QUERY = "UPDATE " + TABLE_MEDS + " SET "
                + KEY_DOSE + " = " + med.getMedDose() + ", "
                + KEY_DOSET1 + " = '" + med.getDoseTime1() + "', "
                + KEY_DOSET2 + " = '" + med.getDoseTime2() + "', "
                + KEY_DOSET3 + " = '" + med.getDoseTime3() + "', "
                + KEY_QUANTITY + " = " + med.getMedQuantity()
                + " WHERE " + KEY_NAME + " = '" + med.getMedName() + "'";

        db.execSQL(UPDATE_QUERY);  // Exec query
        db.close();  // Close
    }

    // Get full medication information from database for 1 medication
    public Medications getMed(String key_name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Medications med = null;
        String RETRIEVE_QUERY = "SELECT * FROM " + TABLE_MEDS + " WHERE " + KEY_NAME + " = " +
                "'" + key_name + "'";

        Cursor cursor = db.rawQuery(RETRIEVE_QUERY, null);

        if (cursor.moveToFirst()) {
            med = new Medications(cursor.getString(0),
                    cursor.getDouble(1),
                    cursor.getString(2), cursor.getString(3),
                    cursor.getString(4),
                    cursor.getDouble(5));
        }

        cursor.close();  // Close cursor
        return med;  // med object

    }

    // Retrieving list of all medications in the database
    List<Medications> getMeds() {
        List<Medications> medList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Format query based on key
        String GET_QUERY = "SELECT * FROM " + TABLE_MEDS;

        Cursor cursor = db.rawQuery(GET_QUERY, null);

        if (cursor.moveToFirst()) {
           while (!cursor.isAfterLast()) {
               Medications med = new Medications(cursor.getString(0),
                       Double.parseDouble(cursor.getString(1)),
                       cursor.getString(2), cursor.getString(3),
                       cursor.getString(4),
                       Double.parseDouble(cursor.getString(5)));
               medList.add(med);
               cursor.moveToNext();
           }
        }

        cursor.close();  // Close cursor
        return medList;  // Return contact
    }

    // Get all valid dose times for a specific medication
    public List<String> getDoseTimes(String key_name) {
        List<String> times = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Format query based on key
        String GET_QUERY = "SELECT " + KEY_DOSET1 + ", " + KEY_DOSET2 + ", " + KEY_DOSET3 + " FROM " +
                TABLE_MEDS + " WHERE " + KEY_NAME + " = " + "'" + key_name + "'";

        Cursor cursor = db.rawQuery(GET_QUERY, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                if (!cursor.getString(0).equals("")) {
                    times.add(cursor.getString(0));
                }
                if (!cursor.getString(1).equals("")) {
                    times.add(cursor.getString(1));
                }
                if (!cursor.getString(2).equals("")) {
                    times.add(cursor.getString(2));
                }
                cursor.moveToNext();
            }
        }
        cursor.close();  // Close cursor
        return times;
    }
    // Get all valid and invalid (empty string) dose times for a medication
    public List<String> getDoseTimesExtra(String key_name) {
        List<String> times = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Format query based on key
        String GET_QUERY = "SELECT " + KEY_DOSET1 + ", " + KEY_DOSET2 + ", " + KEY_DOSET3 + " FROM " +
                TABLE_MEDS + " WHERE " + KEY_NAME + " = " + "'" + key_name + "'";

        Cursor cursor = db.rawQuery(GET_QUERY, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                times.add(cursor.getString(0));
                times.add(cursor.getString(1));
                times.add(cursor.getString(2));
                cursor.moveToNext();
            }
        }
        cursor.close();  // Close cursor
        return times;
    }

    // Get the dosage for a single medication
    public Double getMedDose(String key_name) {
        Double d = 0.0;
        SQLiteDatabase db = this.getReadableDatabase();

        // Format query based on key
        String GET_QUERY = "SELECT " + KEY_DOSE + " FROM " +
                TABLE_MEDS + " WHERE " + KEY_NAME + " = " + "'" + key_name + "'";

        Cursor cursor = db.rawQuery(GET_QUERY, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                d = cursor.getDouble(0);
                cursor.moveToNext();
            }
        }
        cursor.close();  // Close cursor
        return d;
    }

    // Get the remaining quantity of a medication
    public Double getMedQuantity(String key_name) {
        Double q = 0.0;
        SQLiteDatabase db = this.getReadableDatabase();

        // Format query based on key
        String GET_QUERY = "SELECT " + KEY_QUANTITY + " FROM " +
                TABLE_MEDS + " WHERE " + KEY_NAME + " = " + "'" + key_name + "'";

        Cursor cursor = db.rawQuery(GET_QUERY, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                q = cursor.getDouble(0);
                cursor.moveToNext();
            }
        }
        cursor.close();  // Close cursor
        return q;
    }

    // Return list of all medications with a quantity less than or equal to passed in arg
    public List<String> getMedsWithQuantityLessThan(Double bound) {
        List<String> meds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Format query based on key
        String GET_QUERY = "SELECT " + KEY_NAME + " FROM " +
                TABLE_MEDS + " WHERE " + KEY_QUANTITY + " <= " + bound;

        Cursor cursor = db.rawQuery(GET_QUERY, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                meds.add(cursor.getString(0));
                cursor.moveToNext();
            }
        }
        cursor.close();  // Close cursor

        return meds;
    }

    /* Notification table methods */

    // Delete notification row for a medication
    public void deleteNotifications(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        String DELETE_QUERY = "DELETE FROM " + TABLE_NOTIFICATIONS + " WHERE " + KEY_NAME + " = " +
                "'" + name + "'";
        db.execSQL(DELETE_QUERY);
        db.close();  // Close db connection
    }

    // Get all valid (active) notification ids for a medication
    public List<Integer> getNoteIds(String key_name) {
        List<Integer> ids = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Format query based on key
        String GET_QUERY = "SELECT " + KEY_N_ID1 + ", " + KEY_N_ID2 + ", " + KEY_N_ID3 + " FROM " +
                TABLE_NOTIFICATIONS + " WHERE " + KEY_NAME + " = " + "'" + key_name + "'";

        Cursor cursor = db.rawQuery(GET_QUERY, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                if (!cursor.getString(0).equals("")) {
                    ids.add(Integer.parseInt(cursor.getString(0)));
                }
                if (!cursor.getString(1).equals("")) {
                    ids.add(Integer.parseInt(cursor.getString(1)));
                }
                if (!cursor.getString(2).equals("")) {
                    ids.add(Integer.parseInt(cursor.getString(2)));
                }
                cursor.moveToNext();
            }
        }
        cursor.close();  // Close cursor
        return ids;  // Return ids
    }

    // Add a new notification row
    public void addNotification(String name, String note1, String note2, String note3) {
        SQLiteDatabase db = this.getWritableDatabase();
        String INSERT_QUERY = "INSERT INTO " + TABLE_NOTIFICATIONS + " VALUES('" +
                name + "', '" + note1+ "', '" + note2 +
                "', '" + note3 + "', " + 0 + ", " + 0 + ", " + 0 + ", '', " + "'', " + "''" + ")";
        db.execSQL(INSERT_QUERY);
        db.close(); // Close database connection
    }

    // Reset all medicine taken values back to 0
    public void clearTakenAll() {
        SQLiteDatabase db = this.getWritableDatabase();

        String UPDATE_QUERY = "UPDATE " + TABLE_NOTIFICATIONS + " SET "
                + KEY_N_TAKEN1 + " = " + 0 + ", "
                + KEY_N_TAKEN2 + " = " + 0 + ", "
                + KEY_N_TAKEN3 + " = " + 0 + ", "
                + KEY_N_TAKEN_TIME1 + " = " + "'', "
                + KEY_N_TAKEN_TIME2 + " = " + "'', "
                + KEY_N_TAKEN_TIME3 + " = " + "''";

        db.execSQL(UPDATE_QUERY);  // Exec query
        db.close();  // Close
    }

    // Update a notification row (ids, and med taken or not)
    public void updateNotification(String n, String n1, String n2, String n3,
                                   int t1, int t2, int t3) {
        SQLiteDatabase db = this.getWritableDatabase();
        String UPDATE_QUERY = "UPDATE " + TABLE_NOTIFICATIONS + " SET "
                + KEY_NAME + " = '" + n + "', "
                + KEY_N_ID1 + " = '" + n1 + "', "
                + KEY_N_ID2 + " = '" + n2 + "', "
                + KEY_N_ID3 + " = '" + n3 + "', "
                + KEY_N_TAKEN1 + " = " + t1 + ", "
                + KEY_N_TAKEN2 + " = " + t2 + ", "
                + KEY_N_TAKEN3 + " = " + t3
                + " WHERE " + KEY_NAME + " = '" + n + "'";
        db.execSQL(UPDATE_QUERY);  // Exec query
        db.close();  // Close
    }

    // Return map of med dosage times to notification ids for sending notfications
    public Map<String, String> getIDTimeMap(String key_name) {
        Map<String, String> myMap = new HashMap<>();

        // First get id list
        List<Integer> ids = getNoteIds(key_name);

        // Then get times
        List<String> times = getDoseTimes(key_name);

        for (String time : times) {
            myMap.put(time, ids.get(times.indexOf(time)).toString());
        }
        return myMap;
    }

    // Set med taken cells to the passed in value for a specific medication & time
    public void setChecked(String n, String t, Integer val) {
        // Also updates the quantity value
        List<String> doseTimes = getDoseTimesExtra(n);
        int doseIndex = doseTimes.indexOf(t);
        String taken_key = KEY_N_TAKEN1;
        String taken_time_key = KEY_N_TAKEN_TIME1;
        switch (doseIndex) {
            case 0:
                taken_key = KEY_N_TAKEN1;
                taken_time_key = KEY_N_TAKEN_TIME1;
                break;
            case 1:
                taken_key = KEY_N_TAKEN2;
                taken_time_key = KEY_N_TAKEN_TIME2;
                break;
            case 2:
                taken_key = KEY_N_TAKEN3;
                taken_time_key = KEY_N_TAKEN_TIME3;
                break;
            default:
                break;
        }

        // Get new quantity value after the dose is taken
        SQLiteDatabase dbRead = this.getReadableDatabase();
        Double q = getMedQuantity(n);
        Double d = getMedDose(n);
        Double sum = ((val == 1) ? q - d : q + d);

        // Get time value
        Calendar calendar = Calendar.getInstance();
        Integer hr = calendar.get(Calendar.HOUR_OF_DAY);  // Mil hour -> std 12 hour clock hour
        Integer stdHr = (calendar.get(Calendar.AM_PM) == 0) ? (hr == 0 ? 12 : hr) :
                (hr == 12 ? hr : hr % 12);
        Integer min = calendar.get(Calendar.MINUTE);
        String am_pm = (calendar.get(Calendar.AM_PM) == 0) ? "AM" : "PM";
        String calTime = String.format("%s:%s%s", stdHr.toString(), (min < 10) ? "0" +
                min.toString() : min.toString(), am_pm);

        SQLiteDatabase db = this.getWritableDatabase();
        String UPDATE_QUERY = "UPDATE " + TABLE_NOTIFICATIONS + " SET "
                + taken_key + " = " + val + ", " + taken_time_key + " = '" + calTime + "'" +
                " WHERE " + KEY_NAME + " = '" + n + "'";
        db.execSQL(UPDATE_QUERY);  // Exec query

        String UPDATE_Q_QUERY = "UPDATE " + TABLE_MEDS + " SET "
                + KEY_QUANTITY + " = " + sum +
                " WHERE " + KEY_NAME + " = '" + n + "'";
        db.execSQL(UPDATE_Q_QUERY);  // Exec query

        db.close();  // Close
    }

    // Get timestamp for a taken med/dose
    public String getMedTimeStamp(String n, String t) {
        String timestamp = "";
        List<String> doseTimes = getDoseTimesExtra(n);
        int doseIndex = doseTimes.indexOf(t);
        String taken_key = KEY_N_TAKEN1;
        String taken_time_key = KEY_N_TAKEN_TIME1;
        switch (doseIndex) {
            case 0:
                taken_key = KEY_N_TAKEN1;
                taken_time_key = KEY_N_TAKEN_TIME1;
                break;
            case 1:
                taken_key = KEY_N_TAKEN2;
                taken_time_key = KEY_N_TAKEN_TIME2;
                break;
            case 2:
                taken_key = KEY_N_TAKEN3;
                taken_time_key = KEY_N_TAKEN_TIME3;
                break;
            default:
                break;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        String GET_QUERY = "SELECT " + taken_time_key + " FROM " + TABLE_NOTIFICATIONS + " WHERE " +
                KEY_N_NAME + " = '" + n + "'";
        Cursor cursor = db.rawQuery(GET_QUERY, null);

        if (cursor.moveToFirst()) {
            timestamp = cursor.getString(0);
        }

        cursor.close();
        return timestamp;
    }

    // Returns meds that are taken
    public List<String> getTakenMeds() {
        SQLiteDatabase db = this.getReadableDatabase();

        List<Medications> medList = getMeds();
        List<String> medNames = new ArrayList<>();
        List<String> medsTaken = new ArrayList<>();
        for (Medications m : medList) {
            medNames.add(m.getMedName());
        }

        for (String key_n : medNames) {
            // Format query based on key
            String GET_QUERY = "SELECT " + KEY_N_TAKEN1 + ", " + KEY_N_TAKEN2 + ", " + KEY_N_TAKEN3
                    + ", " + KEY_N_TAKEN_TIME1 + ", " + KEY_N_TAKEN_TIME2 + ", " + KEY_N_TAKEN_TIME3
                    + " FROM " + TABLE_NOTIFICATIONS + " WHERE " +
                    KEY_N_NAME + " = '" + key_n + "'";

            // Get all doseTimes to filter by which taken indexes are valid
            List<String> dosetimes = this.getDoseTimesExtra(key_n);

            Cursor cursor = db.rawQuery(GET_QUERY, null);

            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    // If taken and there is a dose time, add the string + time
                    if ((Integer.parseInt(cursor.getString(0)) == 1 ) &&
                            !dosetimes.get(0).isEmpty()) {
                        medsTaken.add(String.format("%s %s", cursor.getString(3), key_n));
                    }
                    if ((Integer.parseInt(cursor.getString(1)) == 1) &&
                            !dosetimes.get(1).isEmpty())  {
                        medsTaken.add(String.format("%s %s", cursor.getString(4), key_n));
                    }
                    if ((Integer.parseInt(cursor.getString(2)) == 1) &&
                            !dosetimes.get(2).isEmpty()) {
                        medsTaken.add(String.format("%s %s", cursor.getString(5), key_n));
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();  // Close cursor
        }
        // Sorting list by time values before returning
        Collections.sort(medsTaken, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String temp1 = o1.substring(0, o1.indexOf(' '));
                String temp2 = o2.substring(0, o2.indexOf(' '));
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

        return medsTaken;
    }

    // Return whether all medications have been taken for the day or not
    public boolean isAllTaken() {
        SQLiteDatabase db = this.getReadableDatabase();

        List<Medications> medList = getMeds();
        List<String> medNames = new ArrayList<>();
        for (Medications m : medList) {
            medNames.add(m.getMedName());
        }

        for (String key_n : medNames) {
            // Format query based on key
            String GET_QUERY = "SELECT " + KEY_N_TAKEN1 + ", " + KEY_N_TAKEN2 + ", " + KEY_N_TAKEN3
                    + " FROM " + TABLE_NOTIFICATIONS + " WHERE " +
                    KEY_N_NAME + " = '" + key_n + "'";

            // Get all doseTimes to filter by which taken indexes are valid
            List<String> dosetimes = this.getDoseTimesExtra(key_n);

            Cursor cursor = db.rawQuery(GET_QUERY, null);

            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    if ((Integer.parseInt(cursor.getString(0)) != 1) &&
                            !dosetimes.get(0).isEmpty()) {
                        return false;
                    }
                    if ((Integer.parseInt(cursor.getString(1)) != 1) &&
                            !dosetimes.get(1).isEmpty())  {
                        return false;
                    }
                    if ((Integer.parseInt(cursor.getString(2)) != 1) &&
                            !dosetimes.get(2).isEmpty()) {
                        return false;
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();  // Close cursor
        }
        return true;
    }

    // Return whether 1 single medication/time combination has been taken
    public boolean isChecked(String n, String t) {
        List<String> doseTimes = getDoseTimesExtra(n);
        int doseIndex = doseTimes.indexOf(t);
        String taken_key = KEY_N_TAKEN1;
        switch (doseIndex) {
            case 0:
                taken_key = KEY_N_TAKEN1;
                break;
            case 1:
                taken_key = KEY_N_TAKEN2;
                break;
            case 2:
                taken_key = KEY_N_TAKEN3;
                break;
            default:
                break;
        }

        SQLiteDatabase db = this.getReadableDatabase();

        // Format query based on key
        String GET_QUERY = "SELECT " + taken_key + " FROM " + TABLE_NOTIFICATIONS + " WHERE "
                + KEY_NAME + " = '" + n + "'";

        Cursor cursor = db.rawQuery(GET_QUERY, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Integer takenInt = Integer.parseInt(cursor.getString(0));
                if (Integer.parseInt(cursor.getString(0)) != 1) {
                    return false;
                }
                cursor.moveToNext();
            }
        }
        cursor.close();  // Close cursor
        return true;
    }

    /* Calendar specific helper functions */
    public void addMedDate(Integer y, Integer m, Integer d, String meds) {
        SQLiteDatabase db = getWritableDatabase();
        String date = String.format("%s/%s/%s", m.toString(), d.toString(), y.toString());

        String INSERT_QUERY = "INSERT INTO " + TABLE_CALENDAR + " VALUES('" +
                date + "', '" + meds + "'" + ")";
        db.execSQL(INSERT_QUERY);

        db.close();  // Close db after writing
    }


    public String getMedsOnDate(Integer y, Integer m, Integer d) {
        String date = String.format("%s/%s/%s", m.toString(), d.toString(), y.toString());
        String meds = "";

        SQLiteDatabase db = getReadableDatabase();

        String GET_QUERY = "SELECT " + KEY_CAL_MEDS_TAKEN + " FROM " + TABLE_CALENDAR + " WHERE " +
                KEY_CAL_DATE + " = '" + date + "'";

        Cursor cursor = db.rawQuery(GET_QUERY, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                meds = cursor.getString(0);
                cursor.moveToNext();
            }
        }
        cursor.close();  // Close cursor
        return meds;
    }
}
