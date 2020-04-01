package com.example.chitchatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    private  String receiveUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiveUserId = getIntent().getExtras().get("userId").toString();
        Toast.makeText(this, "userId", Toast.LENGTH_SHORT).show();
    }
}
