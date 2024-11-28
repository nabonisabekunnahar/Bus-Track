package com.example.bustrack;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bustrack.Model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity";
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Initialize Firebase Auth and get current user ID
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firestore and notification list
        notificationList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        adapter = new NotificationAdapter(this, notificationList);
        recyclerView.setAdapter(adapter);

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
        } else {
            // If permissions already granted, fetch notifications
            fetchNotifications();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch notifications
                fetchNotifications();
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Location permission is required to accept requests.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchNotifications() {
        firestore.collection("tracking_requests")
                .whereEqualTo("requestStatus", "pending")  // Only show pending requests
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "Error fetching notifications", e);
                            Toast.makeText(NotificationActivity.this, "Error fetching notifications", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        notificationList.clear();

                        if (snapshots != null) {
                            for (QueryDocumentSnapshot document : snapshots) {
                                String userId = document.getString("userId");
                                if (!currentUserId.equals(userId)) {  // Exclude notifications from self
                                    String userName = document.getString("userName");
                                    String destination = document.getString("destination");
                                    String requestId = document.getId();
                                    Notification notification = new Notification(userId, userName, destination, requestId, false);
                                    notificationList.add(notification);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "No notifications found.");
                            Toast.makeText(NotificationActivity.this, "No new notifications.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
