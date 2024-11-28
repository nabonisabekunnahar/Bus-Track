package com.example.bustrack.Model;

import com.google.firebase.firestore.GeoPoint;

public class LocationData {
    private String requestId;
    private GeoPoint location;

    // Required empty constructor for Firestore deserialization
    public LocationData() {}

    public LocationData(String requestId, GeoPoint location) {
        this.requestId = requestId;
        this.location = location;
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }
}
