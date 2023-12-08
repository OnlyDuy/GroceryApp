package com.example.groceryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.groceryapp.MainActivity;
import com.example.groceryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    Button signIn;
    TextView signUp;
    EditText email, password;
    FirebaseAuth auth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        signIn = findViewById(R.id.login_btn);
        email = findViewById(R.id.email_login);
        password = findViewById(R.id.password_login);
        signUp = findViewById(R.id.sign_up);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        // Check if the user is already logged in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish(); // Close the LoginActivity to prevent going back to it with the back button
        }
    }

    private void loginUser() {
        String userEmail = email.getText().toString();
        String userPassword = password.getText().toString();

        if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(this, "Email is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(userPassword)) {
            Toast.makeText(this, "Password is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userPassword.length() < 6) {
            Toast.makeText(this, "Password must be greater than 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Login user
        auth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish(); // Close the LoginActivity to prevent going back to it with the back button
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
