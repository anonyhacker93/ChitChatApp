package com.example.chitchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button update_setting_button;
    private EditText current_user_name, current_user_status;
    private CircleImageView setImageProfile;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference userProfileImageRef;
    private String currentId;
    private static final int galleryCode = 1;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("ProfileImages");

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            currentId = firebaseUser.getUid();

            initField();

            update_setting_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateUserProfile();
                }
            });

            retrieveUserData();

            setImageProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, galleryCode);
                }
            });
        }
    }

    private void initField() {
        update_setting_button = findViewById(R.id.update_setting_button);
        current_user_name = findViewById(R.id.set_user_name);
        current_user_status = findViewById(R.id.set_user_status);
        setImageProfile = findViewById(R.id.set_profile_image);
       Picasso.get().load(R.drawable.login_photo).into(setImageProfile);
        //setImageProfile.setImageDrawable(getDrawable(R.drawable.login_photo));
        loadingBar = new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galleryCode && resultCode == RESULT_OK && data != null) {

            Uri imgaeUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                loadingBar.setTitle("Set profile image");
                loadingBar.setMessage("PLease wait while we are uploading your profile picture...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                StorageReference profilePath = userProfileImageRef.child(currentId + ".jpg");
                profilePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(SettingsActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();
                        Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl();
                        if (downloadUri.isSuccessful()) {
                            String downloadURL = downloadUri.getResult().toString();
                            rootRef.child("Users").child(currentId).child("image").setValue(downloadURL).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SettingsActivity.this, "image save to database successfully...", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    } else {
                                        String message = task.getException().toString();
                                        Toast.makeText(SettingsActivity.this, "error " + message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(SettingsActivity.this, "Went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

        }
    }

    private void updateUserProfile() {
        String currentUserName = current_user_name.getText().toString();
        String status = current_user_status.getText().toString();

        if (TextUtils.isEmpty(currentUserName)) {
            Toast.makeText(this, "Please enter name...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(status)) {
            Toast.makeText(this, "Please enter status...", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("userId", currentId);
            profileMap.put("name", currentUserName);
            profileMap.put("status", status);

            rootRef.child("Users").child(currentId).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        sendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "profile updated", Toast.LENGTH_SHORT).show();
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "error " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void retrieveUserData() {
        rootRef.child("Users").child(currentId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))) {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveImage = dataSnapshot.child("image").getValue().toString();

                    current_user_name.setText(retrieveUserName);
                    current_user_status.setText(retrieveStatus);
                    Picasso.get().load(retrieveImage).into(setImageProfile, new Callback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(SettingsActivity.this, "No bug", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.d("ddd","error:"+e);
                            Toast.makeText(SettingsActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                        }
                    });

                } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();


                    current_user_name.setText(retrieveUserName);
                    current_user_status.setText(retrieveStatus);

                } else {
                    Toast.makeText(SettingsActivity.this, "Please set name and status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                String message = databaseError.getMessage().toString();
                Toast.makeText(SettingsActivity.this, "error " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
