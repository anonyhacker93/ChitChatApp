package com.example.chitchatapp;

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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {


    private Button sendVerifyCodeButton, verifyButton;
    private EditText inputPhoneNumber, inputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callback;
    private FirebaseAuth mAuth;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();
        initField();
        sendVerifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phoneNumber = inputPhoneNumber.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please enter phone number", Toast.LENGTH_SHORT).show();
                } else {

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callback);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        callback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(PhoneLoginActivity.this, "Invalid phone number, Please enter correct phone number with your country code", Toast.LENGTH_SHORT).show();

                inputPhoneNumber.setVisibility(View.VISIBLE);
                sendVerifyCodeButton.setVisibility(View.VISIBLE);

                inputVerificationCode.setVisibility(View.INVISIBLE);
                verifyButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {

                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(PhoneLoginActivity.this, "code has been sent", Toast.LENGTH_SHORT).show();
                inputPhoneNumber.setVisibility(View.INVISIBLE);
                sendVerifyCodeButton.setVisibility(View.INVISIBLE);

                inputVerificationCode.setVisibility(View.VISIBLE);
                verifyButton.setVisibility(View.VISIBLE);
            }
        };
    }

    private void initField() {
        sendVerifyCodeButton = findViewById(R.id.send_ver_code_button);
        verifyButton = findViewById(R.id.verify_button);
        inputPhoneNumber = findViewById(R.id.phone_login_input);
        inputVerificationCode = findViewById(R.id.input_verification_code);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                        } else {

                        }
                    }
                });
    }
}
