package com.example.bustrack.Model;

public class busModel {

    private String busName;
    private String busTime;
    private String busNumber;
    private String busLatitude;
    private String busLongitude;
    private String busImage;
    private String busDocId;
    private String currentUserId;

    public busModel() {
    }

    public busModel(String busName, String busTime, String busNumber,
                    String busLatitude, String busLongitude, String busImage, String busDocId, String currentUserId) {
        this.busName = busName;
        this.busTime = busTime;
        this.busNumber = busNumber;
        this.busLatitude = busLatitude;
        this.busLongitude = busLongitude;
        this.busImage = busImage;
        this.busDocId = busDocId;
        this.currentUserId = currentUserId;
    }

    public String getBusName() {
        return busName;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public String getBusTime() {
        return busTime;
    }

    public void setBusTime(String busTime) {
        this.busTime = busTime;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public String getBusLatitude() {
        return busLatitude;
    }

    public void setBusLatitude(String busLatitude) {
        this.busLatitude = busLatitude;
    }

    public String getBusLongitude() {
        return busLongitude;
    }

    public void setBusLongitude(String busLongitude) {
        this.busLongitude = busLongitude;
    }

    public String getBusImage() {
        return busImage;
    }

    public void setBusImage(String busImage) {
        this.busImage = busImage;
    }

    public String getBusDocId() {
        return busDocId;
    }

    public void setBusDocId(String busDocId) {
        this.busDocId = busDocId;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }
}
