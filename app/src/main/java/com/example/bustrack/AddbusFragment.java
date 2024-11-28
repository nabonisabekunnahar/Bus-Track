package com.example.bustrack;


import android.Manifest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.bustrack.Model.busModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;


public class AddbusFragment extends Fragment {

    private EditText edBusName,edBusTime,edBusNumber,edlocation;
    private AppCompatButton uploadbusbtn;

   //cardview as a selectphoto button
    private MaterialCardView selectPhoto;

    private Uri Imageuri;

    private ImageView busImageview;

    private Bitmap bitmap;

    private FirebaseStorage storage;
    private FirebaseFirestore firestore;

    private StorageReference mStorage;

    private FirebaseAuth firebaseAuth;

    private String photouri;

    private String currentUserId;

    private String docId;

    public AddbusFragment() {

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view= inflater.inflate(R.layout.fragment_addbus, container, false);

       edBusName=view.findViewById(R.id.busName);
       edBusTime=view.findViewById(R.id.busTime);
       edBusNumber=view.findViewById(R.id.busNumber);
      // edlocation=view.findViewById(R.id.buslocation);
       uploadbusbtn=view.findViewById(R.id.uploadbtn);


       selectPhoto=view.findViewById(R.id.selectorBus);
       busImageview=view.findViewById(R.id.busImage);

       //create instance
        firestore=FirebaseFirestore.getInstance();
        storage=FirebaseStorage.getInstance();
        mStorage=storage.getReference();

        //get current user id
        firebaseAuth=FirebaseAuth.getInstance();
        currentUserId=firebaseAuth.getCurrentUser().getUid();

       selectPhoto.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               checkstoragepermission();


           }
       });

       uploadbusbtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               uploadImage();
           }
       });
       return view;
    }

    private void checkstoragepermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }
            else {

                pickImagefromGallary();
            }
        }

        else {

            pickImagefromGallary();
        }
    }

    private void pickImagefromGallary() {
        Intent intent =new Intent();
        intent.setType("image/");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        launcher.launch(intent);
    }
    ActivityResultLauncher<Intent>launcher
           =registerForActivityResult(
                   new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {

                    Intent data = result.getData();
                    if (data != null && data.getData() != null)
                    {
                        Imageuri = data.getData();

                        //now need to convert my image into bitmap

                        try {
                            // Convert the image URI into a bitmap
                            bitmap = MediaStore.Images.Media.getBitmap(
                                    getActivity().getContentResolver(),
                                    Imageuri
                            );

                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (Imageuri != null)
                    {
                        busImageview.setImageBitmap(bitmap);
                    }
                }

            }
    );

    //upload image into firebase firestore

    private void uploadImage() {
        if (Imageuri != null) {
            final StorageReference myRef = mStorage.child("photo/" + System.currentTimeMillis() + "_" + Imageuri.getLastPathSegment());

            myRef.putFile(Imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d("UploadImage", "Image uploaded successfully.");

                    myRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            photouri = uri.toString();
                            Log.d("UploadImage", "Download URL retrieved successfully: " + photouri);
                            uploadbusinfo();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("UploadImage", "Failed to get download URL", e);
                            Toast.makeText(getActivity(), "Failed to get download URL", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("UploadImage", "Image upload failed", e);
                    Toast.makeText(getActivity(), "Image upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("UploadImage", "Image URI is null");
            Toast.makeText(getActivity(), "Please select an image first", Toast.LENGTH_SHORT).show();
        }
    }



    //now upload other information

    private void uploadbusinfo() {
        String name = edBusName.getText().toString().trim();
        String time = edBusTime.getText().toString().trim();
        String number = edBusNumber.getText().toString().trim();

        // Proper condition to check if any field is empty
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(time) || TextUtils.isEmpty(number)) {
            Toast.makeText(getContext(), "Fill all information", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference documentReference = firestore.collection("busInfo").document();

        busModel busmodel = new busModel(name, time, number, "", "", photouri, "", currentUserId);
        documentReference.set(busmodel, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    docId = documentReference.getId();
                    busmodel.setBusDocId(docId);
                    documentReference.set(busmodel, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Uploaded successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}