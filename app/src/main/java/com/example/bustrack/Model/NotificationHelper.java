package com.example.bustrack.Model;

import android.util.Log;
import com.google.auth.oauth2.GoogleCredentials;
import okhttp3.*;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

public class NotificationHelper {

    private static final String FCM_API_URL = "https://fcm.googleapis.com/v1/projects/YOUR_PROJECT_ID/messages:send";
    private static final String SERVICE_ACCOUNT_FILE_PATH = "C:/Users/Win-10/AndroidStudioProjects/BusTrack/app/bus01track-firebase-adminsdk-34993-426169b0a6.json";  // Path to your service account JSON file

    // Method to send FCM notification with custom title and body
    public void sendCustomFCMNotification(String recipientToken, String title, String body) {
        OkHttpClient client = new OkHttpClient();

        try {
            // Get OAuth 2.0 access token
            String accessToken = getAccessToken();

            // Create JSON payload for the FCM request
            JSONObject json = new JSONObject();
            JSONObject message = new JSONObject();
            JSONObject notification = new JSONObject();

            notification.put("title", title);
            notification.put("body", body);

            message.put("token", recipientToken);
            message.put("notification", notification);
            json.put("message", message);

            // Build request
            RequestBody requestBody = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(FCM_API_URL)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + accessToken)  // Use OAuth 2.0 token here
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Send the request
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("FCM", "Failed to send notification", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d("FCM", "Notification sent successfully: " + response.body().string());
                    } else {
                        Log.e("FCM", "Error sending notification: " + response.body().string());
                    }
                }
            });

        } catch (Exception e) {
            Log.e("FCM", "Error sending FCM notification", e);
        }
    }

    // Method to get OAuth 2.0 access token
    private String getAccessToken() throws IOException {
        FileInputStream serviceAccount = new FileInputStream(SERVICE_ACCOUNT_FILE_PATH);
        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(serviceAccount)
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}
