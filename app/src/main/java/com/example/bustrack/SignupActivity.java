package com.example.bustrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bustrack.Model.userModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.FirebaseApp;

public class SignupActivity extends AppCompatActivity {

    private EditText edName, edEmail, edNumber, edPassword;
    private Button signUpButton;
    private TextView loginRedirectText;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize FirebaseApp
        FirebaseApp.initializeApp(this);  // Initialize Firebase

        edName = findViewById(R.id.signup_name);
        edEmail = findViewById(R.id.signup_email);
        edPassword = findViewById(R.id.signup_password);
        edNumber = findViewById(R.id.signup_phone);
        signUpButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loginRedirectText.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        signUpButton.setOnClickListener(v -> SignUpUser());
    }

    private void SignUpUser() {
        String name = edName.getText().toString().trim();
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString().trim();
        String number = edNumber.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(number)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                            userId = firebaseAuth.getCurrentUser().getUid();
                            saveUserToFirestore(name, email, number, password, userId);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void saveUserToFirestore(String name, String email, String number, String password, String userId) {
        // Retrieve FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String fcmToken = task.getResult();

                        // Create user model with all details including FCM token
                        userModel model = new userModel(name, email, number, password, userId, fcmToken);

                        // Save the user model directly to Firestore
                        DocumentReference userInfo = firestore.collection("Users").document(userId);
                        userInfo.set(model, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(SignupActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignupActivity.this, MainMenuActivity.class);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    // Log error to see what went wrong
                                    Toast.makeText(SignupActivity.this, "Error saving user to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("Firestore Error", "Error saving user to Firestore: ", e);
                                });
                    } else {
                        Toast.makeText(SignupActivity.this, "Failed to retrieve FCM token", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
