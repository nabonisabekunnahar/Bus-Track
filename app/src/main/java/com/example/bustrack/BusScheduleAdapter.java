package com.example.bustrack;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bustrack.Model.BusSchedule;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusScheduleAdapter extends RecyclerView.Adapter<BusScheduleAdapter.BusScheduleViewHolder> {

    private final List<BusSchedule> busScheduleList;
    private final Context context;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final String currentUserId;

    public BusScheduleAdapter(Context context, List<BusSchedule> busScheduleList) {
        this.context = context;
        this.busScheduleList = busScheduleList;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    @NonNull
    @Override
    public BusScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bus_schedule, parent, false);
        return new BusScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BusScheduleViewHolder holder, int position) {
        BusSchedule schedule = busScheduleList.get(position);

        holder.tripNameTextView.setText(schedule.getTripName());
        holder.startTimeTextView.setText(schedule.getStartTimeFromCampus());
        holder.startSpotTextView.setText(schedule.getStartSpotAndTime());
        holder.remarksTextView.setText(schedule.getRemarks());

        holder.trackPermissionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                sendTrackingRequest(schedule);  // Send request when switch is turned on
            }
        });
    }

    @Override
    public int getItemCount() {
        return busScheduleList.size();
    }

    private void sendTrackingRequest(BusSchedule schedule) {
        if (currentUserId == null) {
            Log.e("Auth Error", "User is not authenticated.");
            return;
        }

        firestore.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userName = documentSnapshot.getString("userName");
                    String destination = schedule.getTripName();

                    // Create a tracking request map
                    Map<String, Object> trackingRequest = new HashMap<>();
                    trackingRequest.put("userId", currentUserId);
                    trackingRequest.put("userName", userName);
                    trackingRequest.put("destination", destination);
                    trackingRequest.put("requestStatus", "pending");
                    trackingRequest.put("timestamp", System.currentTimeMillis());
                    trackingRequest.put("latitude", null); // Placeholder for latitude
                    trackingRequest.put("longitude", null); // Placeholder for longitude

                    // Add the tracking request to Firestore
                    firestore.collection("tracking_requests").add(trackingRequest)
                            .addOnSuccessListener(docRef -> {
                                Log.d("TrackingRequest", "Request sent successfully with ID: " + docRef.getId());
                                Toast.makeText(context, "Tracking request sent.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Log.e("Firestore", "Error sending tracking request", e));
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching user data", e));

    }

    static class BusScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tripNameTextView, startTimeTextView, startSpotTextView, remarksTextView;
        SwitchMaterial trackPermissionSwitch;

        public BusScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tripNameTextView = itemView.findViewById(R.id.trip_name);
            startTimeTextView = itemView.findViewById(R.id.start_time);
            startSpotTextView = itemView.findViewById(R.id.start_spot);
            remarksTextView = itemView.findViewById(R.id.remarks);
            trackPermissionSwitch = itemView.findViewById(R.id.track_permission_switch);
        }
    }
}
