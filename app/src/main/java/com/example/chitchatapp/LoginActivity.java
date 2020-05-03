package com.example.chitchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button loginButton, phoneLoginButton;
    private EditText userEmial, userPassword;
    private TextView forgotPasswordLink, needNewAccountLink;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        initField();

        needNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserRegisterActivity();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allowUserToLogin();
            }
        });

        phoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent phoneLoginIntent = new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });
    }

    public void initField() {
        loginButton = findViewById(R.id.login_button);
        phoneLoginButton = findViewById(R.id.phone_login_button);
        userEmial = findViewById(R.id.email_login);
        userPassword = findViewById(R.id.login_password);
        forgotPasswordLink = findViewById(R.id.forgot_password);
        needNewAccountLink = findViewById(R.id.need_an_account);
        loadingBar = new ProgressDialog(this);
    }

    private void allowUserToLogin() {

        String emial = userEmial.getText().toString();
        String password = userPassword.getText().toString();
        if (TextUtils.isEmpty(emial)) {
            Toast.makeText(this, "Please enter emial...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(emial, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        sendUserToMainActivity();
                        Toast.makeText(LoginActivity.this, "Sign In Successfully", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(LoginActivity.this, "error" + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    public void sendUserRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }

}
