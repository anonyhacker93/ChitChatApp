package com.example.chitchatapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar myToolbar;
    private ScrollView myScrolView;
    private EditText userMessageInput;
    private ImageButton sendMessageButton;
    private TextView displayTextMessage;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef, groupNameRef, groupMessageKeyRef;

    private String groupName, currentUserId, currentUserName, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        groupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(this, groupName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName);
        groupMessageKeyRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        currentUserId = mAuth.getCurrentUser().getUid();

        initField();

        getUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInfoToDatabase();
                userMessageInput.setText("");

                myScrolView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists()) {
                    displayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    displayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initField() {
        myToolbar = findViewById(R.id.group_chat_app_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle(groupName);

        myScrolView = findViewById(R.id.group_chat_scroll_view);
        displayTextMessage = findViewById(R.id.group_chat_text_display);
        userMessageInput = findViewById(R.id.userInputMessage);
        sendMessageButton = findViewById(R.id.send_message_button);
    }

    private void getUserInfo() {
        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                String message = databaseError.getMessage();
                Toast.makeText(GroupChatActivity.this, "error " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserInfoToDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = groupNameRef.push().getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please enter your message...", Toast.LENGTH_SHORT).show();
        } else {
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy");
            currentDate = dateFormat.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = timeFormat.format(calendarTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);
            groupMessageKeyRef = groupNameRef.child(messageKey);

            HashMap<String, Object> groupMessageInfoMap = new HashMap<>();
            groupMessageInfoMap.put("name", currentUserName);
            groupMessageInfoMap.put("message",message);
            groupMessageInfoMap.put("date", currentDate);
            groupMessageInfoMap.put("time", currentTime);

            groupMessageKeyRef.updateChildren(groupMessageInfoMap);
        }
    }

    private void displayMessage(DataSnapshot dataSnapshot) {

        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()) {

            String chatDate = ((DataSnapshot) iterator.next()).getValue().toString();
            String chatMessage = ((DataSnapshot) iterator.next()).getValue().toString();
            String chatName = ((DataSnapshot) iterator.next()).getValue().toString();
            String chatTime = ((DataSnapshot) iterator.next()).getValue().toString();
            displayTextMessage.append(chatName + " :\n" + chatMessage + " :\n" + chatTime + " :\n" + chatDate + " \n\n\n");

            myScrolView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
