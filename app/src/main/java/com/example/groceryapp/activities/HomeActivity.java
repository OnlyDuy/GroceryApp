package com.example.groceryapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.google.firebase.auth.FirebaseAuth;

import com.example.groceryapp.MainActivity;

public class HomeActivity extends AppCompatActivity {

    ProgressBar progressBar;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance(); // get instance of firebase auth

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        if(auth.getCurrentUser() != null){
            progressBar.setVisibility(View.VISIBLE);
            Log.e("TAG", "onCreate: " + auth.getCurrentUser().getEmail());
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            Toast.makeText(this, "please wait you are already logged in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void login(View view){
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
    }

    public void registration(View view){
        startActivity(new Intent(HomeActivity.this, RegistrationActivity.class));
    }
}