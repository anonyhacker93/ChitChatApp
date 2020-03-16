package com.example.chitchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private Button update_setting_button;
    private EditText current_user_name, current_user_status;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String currentId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        currentId = mAuth.getCurrentUser().getUid();

        initField();

        update_setting_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserProfile();
            }
        });

        retrieveUserData();
    }

    private void initField() {
        update_setting_button = findViewById(R.id.update_setting_button);
        current_user_name = findViewById(R.id.set_user_name);
        current_user_status = findViewById(R.id.set_user_status);
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
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                    current_user_name.setText(retrieveUserName);
                    current_user_status.setText(retrieveStatus);

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
