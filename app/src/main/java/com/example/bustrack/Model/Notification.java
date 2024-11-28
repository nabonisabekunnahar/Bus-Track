package com.example.bustrack.Model;

public class Notification {
    private String userId;
    private String userName;
    private String destination;
    private String requestId;
    private boolean isAccepted;

    public Notification() {}

    public Notification(String userId, String userName, String destination, String requestId, boolean isAccepted) {
        this.userId = userId;
        this.userName = userName;
        this.destination = destination;
        this.requestId = requestId;
        this.isAccepted = isAccepted;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }
}
