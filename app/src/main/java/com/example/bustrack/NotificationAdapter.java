package com.example.bustrack;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bustrack.Model.Notification;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final List<Notification> notificationList;
    private final Context context;
    private final FirebaseFirestore firestore;

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.senderTextView.setText(notification.getUserName());
        holder.destinationTextView.setText(notification.getDestination());

        holder.acceptButton.setOnClickListener(v -> {
            updateRequestStatus(notification, "accepted", position);
        });

        holder.denyButton.setOnClickListener(v -> {
            updateRequestStatus(notification, "denied", position);
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    private void updateRequestStatus(Notification notification, String status, int position) {
        // Check if status is "accepted" to update latitude and longitude
        if ("accepted".equals(status)) {
            // Fetch the current user's location
            getUserLocation((latitude, longitude) -> {
                // Update Firestore with the new status and location
                firestore.collection("tracking_requests").document(notification.getRequestId())
                        .update(
                                "requestStatus", status,
                                "latitude", latitude,
                                "longitude", longitude
                        )
                        .addOnSuccessListener(aVoid -> {
                            notificationList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Request " + status, Toast.LENGTH_SHORT).show();

                            // Send an acceptance notification to the requester
                            sendAcceptanceNotification(notification);
                        })
                        .addOnFailureListener(e -> Log.e("Firestore", "Error updating request status", e));
            });
        }
    }

    private void getUserLocation(LocationCallback callback) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the last known location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        callback.onLocationReceived(latitude, longitude);
                    } else {
                        Toast.makeText(context, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("Location", "Error fetching location", e));
    }

    interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);
    }

    private void sendAcceptanceNotification(Notification notification) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("type", "acceptance");
        notificationData.put("message", "Your tracking request has been accepted.");
        notificationData.put("requestId", notification.getRequestId());
        notificationData.put("timestamp", System.currentTimeMillis());

        firestore.collection("Users").document(notification.getUserId())  // Use getUserId() directly here
                .collection("notifications")
                .add(notificationData)
                .addOnSuccessListener(docRef -> Log.d("Firestore", "Acceptance notification sent"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error sending acceptance notification", e));
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView, destinationTextView;
        Button acceptButton, denyButton;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.sender_name);
            destinationTextView = itemView.findViewById(R.id.destination);
            acceptButton = itemView.findViewById(R.id.accept_button);
            denyButton = itemView.findViewById(R.id.deny_button);
        }
    }
}
