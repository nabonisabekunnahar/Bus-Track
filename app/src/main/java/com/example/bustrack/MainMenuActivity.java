package com.example.bustrack;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.material.snackbar.Snackbar;

public class MainMenuActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ListenerRegistration acceptanceListener;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        FirebaseApp.initializeApp(this);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Transport");

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // Set up the ActionBarDrawerToggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation item clicks
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.nav_notifications) {
                openNotificationActivity();
            } else if(id == R.id.nav_logout) {
                logoutUser();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Set click listeners for schedule cards
        findViewById(R.id.card_morning).setOnClickListener(view -> openScheduleActivity("morning"));
        findViewById(R.id.card_noon).setOnClickListener(view -> openScheduleActivity("noon"));
        findViewById(R.id.card_afternoon).setOnClickListener(view -> openScheduleActivity("afternoon"));
        findViewById(R.id.card_night).setOnClickListener(view -> openScheduleActivity("night"));

        // Start listening for acceptance notifications
        listenForAcceptanceNotifications();
    }

    private void listenForAcceptanceNotifications() {
        // Get the currently authenticated user's UID
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        // Ensure userId is available
        if (userId == null) {
            Log.e("FirestoreListener", "User not authenticated");
            return;
        }

        // Log the current user ID for debugging
        Log.d("FirestoreListener", "Authenticated User ID: " + userId);

        // Query Firestore for tracking requests where the userId matches the current user and requestStatus is "accepted"
        acceptanceListener = firestore.collection("tracking_requests")
                .whereEqualTo("userId", userId)
                .whereEqualTo("requestStatus", "accepted")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error fetching acceptance notifications", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : snapshots) {
                            // Ensure the document has a destination field
                            if (document.contains("destination")) {
                                String acceptedDestination = document.getString("destination");

                                // Show Snackbar notification for accepted request
                                Snackbar.make(findViewById(android.R.id.content),
                                                "Your tracking request for " + acceptedDestination + " was accepted!",
                                                Snackbar.LENGTH_LONG)
                                        .setAction("Dismiss", v -> {})
                                        .setAction("Show in Map", v -> {
                                            // When "Show in Map" is clicked, pass the requestId or destination to a MapActivity
                                            String requestId = document.getId(); // Get the request ID
                                            Intent intent = new Intent(MainMenuActivity.this, MapActivity.class);
                                            intent.putExtra("requestId", requestId); // Pass the requestId to the map activity
                                            startActivity(intent);
                                        })
                                        .show();
                            }
                        }
                    }
                });
    }





    private void openScheduleActivity(String shift) {
        Intent intent = new Intent(MainMenuActivity.this, ScheduleActivity.class);
        intent.putExtra("shift", shift);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void openNotificationActivity() {
        Intent intent = new Intent(MainMenuActivity.this, NotificationActivity.class);
        startActivity(intent);
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (acceptanceListener != null) {
            acceptanceListener.remove();
        }
    }
}
