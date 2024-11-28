package com.example.bustrack.Model;

public class BusSchedule {

    private String tripName;
    private String startTimeFromCampus;
    private String startSpotAndTime;
    private String remarks;

    public BusSchedule(String tripName, String startTimeFromCampus, String startSpotAndTime, String remarks) {
        this.tripName = tripName;
        this.startTimeFromCampus = startTimeFromCampus;
        this.startSpotAndTime = startSpotAndTime;
        this.remarks = remarks;
    }

    public String getTripName() {
        return tripName;
    }

    public String getStartTimeFromCampus() {
        return startTimeFromCampus;
    }

    public String getStartSpotAndTime() {
        return startSpotAndTime;
    }

    public String getRemarks() {
        return remarks;
    }
}
