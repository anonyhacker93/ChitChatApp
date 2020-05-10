package com.example.chitchatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiveUserId,senderUserId, currentState;
    private CircleImageView userImageView;
    private TextView visitUserName, visitUserStatus;
    private Button sendRequestMessageButton, declineChatRequestButton;

    private DatabaseReference userRef,chatRequestRef,contactsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        mAuth = FirebaseAuth.getInstance().getInstance();

        receiveUserId = getIntent().getExtras().get("userId").toString();
        senderUserId =mAuth.getCurrentUser().getUid();

        userImageView = findViewById(R.id.visitProfileImage);
        visitUserName = findViewById(R.id.visitProfileName);
        visitUserStatus = findViewById(R.id.visitProfileStatus);
        sendRequestMessageButton = findViewById(R.id.send_message_request_button);
        declineChatRequestButton = findViewById(R.id.decline_message_request_button);
        currentState = "new";

        retrieveUserInfo();

    }

    private void retrieveUserInfo() {

        userRef.child(receiveUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userImageView);
                    visitUserName.setText(userName);
                    visitUserStatus.setText(userStatus);

                    manageChatRequest();

                } else {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    visitUserName.setText(userName);
                    visitUserStatus.setText(userStatus);

                    manageChatRequest();
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequest() {
        chatRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiveUserId)){
                    String requestType = dataSnapshot.child(receiveUserId).child("request_type").getValue().toString();

                    if(requestType.equals("sent")){
                        currentState = "request_sent";
                        sendRequestMessageButton.setText("Cancel Chat Request");
                    }else if(requestType.equals("received")){
                         currentState = "request_received";
                         sendRequestMessageButton.setText("Accept Chat Request");

                         declineChatRequestButton.setEnabled(true);
                         declineChatRequestButton.setVisibility(View.VISIBLE);

                         declineChatRequestButton.setOnClickListener(new View.OnClickListener() {
                             @Override
                             public void onClick(View v) {
                                 cancelChatRequest();
                             }
                         });
                    }
                }
                else {
                    contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiveUserId)){
                                currentState = "friends";
                                sendRequestMessageButton.setText("Remove this Contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(!senderUserId.equals(receiveUserId)){
            sendRequestMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendRequestMessageButton.setEnabled(false);

                    if(currentState.equals("new")){
                        sendChatRequest();
                    }
                    if(currentState.equals("request_sent")){
                        cancelChatRequest();
                    }
                    if(currentState.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if(currentState.equals("friends")){
                        removeSpecificContact();
                    }
                }
            });
        }else
            {
            sendRequestMessageButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContact() {
        contactsRef.child(senderUserId).child(receiveUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    contactsRef.child(receiveUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendRequestMessageButton.setEnabled(true);
                                currentState = "new";
                                sendRequestMessageButton.setText("Send Message");

                                declineChatRequestButton.setVisibility(View.INVISIBLE);
                                declineChatRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void AcceptChatRequest() {
        contactsRef.child(senderUserId).child(receiveUserId).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
               if(task.isSuccessful()){
                   contactsRef.child(receiveUserId).child(senderUserId).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           if(task.isSuccessful()){
                               chatRequestRef.child(senderUserId).child(receiveUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       if(task.isSuccessful()){
                                           chatRequestRef.child(receiveUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                               @Override
                                               public void onComplete(@NonNull Task<Void> task) {
                                                   if(task.isSuccessful()){
                                                       sendRequestMessageButton.setEnabled(true);
                                                       currentState = "friends";
                                                       sendRequestMessageButton.setText("Remove this Contact");

                                                       declineChatRequestButton.setVisibility(View.INVISIBLE);
                                                       declineChatRequestButton.setEnabled(false);

                                                   }
                                               }
                                           });
                                       }
                                   }
                               });
                           }
                       }
                   });
               }
            }
        });
    }

    private void cancelChatRequest() {
        chatRequestRef.child(senderUserId).child(receiveUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    chatRequestRef.child(receiveUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendRequestMessageButton.setEnabled(true);
                                currentState = "new";
                                sendRequestMessageButton.setText("Send Message");

                                declineChatRequestButton.setVisibility(View.INVISIBLE);
                                declineChatRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendChatRequest() {


        chatRequestRef.child(senderUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    chatRequestRef.child(receiveUserId).child(senderUserId).child("request_type").setValue(
                            "received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendRequestMessageButton.setEnabled(true);
                                currentState = "request_sent";
                                sendRequestMessageButton.setText("Cancel Chat Request");
                            }
                        }
                    });
                }
            }
        });
    }
}
