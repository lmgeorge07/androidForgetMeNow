/*
Name: Levis George
Date: 12/1/2017
Course: Mobile Device Applications COP4656-01
File name: Medications.java
Purpose: Model for this app. All medication data to be retrieved, viewed, updated etc.
 */

package com.example.levis.forget_me_now;


public class Medications {
    // Class members
    private String medName;
    private Double medDose;
    private String doseTime1, doseTime2, doseTime3;
    private Double medQuantity;

    // Parameterized constructor
    Medications(String n, Double d, String dt1, String dt2, String dt3, Double q) {
        this.medName = n;
        this.medDose = d;
        this.doseTime1 = dt1;
        this.doseTime2 = dt2;
        this.doseTime3 = dt3;
        this.medQuantity = q;
    }

    // Getters
    public String getDoseTime1() {
        return doseTime1;
    }

    public String getDoseTime2() {
        return doseTime2;
    }

    public String getDoseTime3() {
        return doseTime3;
    }

    public String getMedName() {
        return this.medName;
    }

    public Double getMedDose() {return this.medDose;}

    public Double getMedQuantity() {
        return this.medQuantity;
    }
}
