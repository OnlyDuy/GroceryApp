package com.example.groceryapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.groceryapp.activities.HomeActivity;
import com.example.groceryapp.activities.LoginActivity;
import com.example.groceryapp.ui.home.HomeFragment;

import com.example.groceryapp.ui.profile.ProfileFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.groceryapp.models.UserModel;
import com.example.groceryapp.databinding.ActivityMainBinding;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ScheduledFuture;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    FirebaseDatabase database;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (isConnected(this)) {
//            loadHomeFragment();
        } else {
            Toast.makeText(getApplicationContext(), "Không có Internet, Vui lòng kết nối", Toast.LENGTH_LONG).show();
        }

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile , R.id.nav_category, R.id.nav_offers,
                    R.id.nav_new_products , R.id.nav_my_orders, R.id.nav_my_carts)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Set the item click listener for the logout item
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                // Handle logout click
                logout();
                return true;
            }else if (item.getItemId() == R.id.nav_profile) {
                // Handle profile click using NavController
                navController.navigate(R.id.nav_profile);
                drawer.closeDrawers();
                return true;
            }else if (item.getItemId() == R.id.nav_home) {
                // Handle profile click using NavController
                navController.navigate(R.id.nav_home);
                drawer.closeDrawers();
                return true;
            }else if (item.getItemId() == R.id.nav_category) {
                // Handle profile click using NavController
                navController.navigate(R.id.nav_category);
                drawer.closeDrawers();
                return true;
            }else if (item.getItemId() == R.id.nav_offers) {
                // Handle profile click using NavController
                navController.navigate(R.id.nav_offers);
                drawer.closeDrawers();
                return true;
            }else if (item.getItemId() == R.id.nav_new_products) {
                // Handle profile click using NavController
                navController.navigate(R.id.nav_new_products);
                drawer.closeDrawers();
                return true;
            }else if (item.getItemId() == R.id.nav_my_orders) {
                // Handle profile click using NavController
                navController.navigate(R.id.nav_my_orders);
                drawer.closeDrawers();
                return true;
            }
            else if (item.getItemId() == R.id.nav_my_carts) {
                // Handle profile click using NavController
                navController.navigate(R.id.nav_my_carts);
                drawer.closeDrawers();
                return true;
            }

            // Add other cases as needed
            return false;
        });

        // Initialize FirebaseDatabase instance
        database = FirebaseDatabase.getInstance();

        // Set the navigation header data
        setNavHeader();

    }

    public void setNavHeader() {
        NavigationView navigationView = binding.navView; // Assuming "binding" is your ViewBinding instance

        // Get the header view
        View headerView = navigationView.getHeaderView(0);

        // Find the views within the header view by their IDs
        ImageView headerImageView = headerView.findViewById(R.id.imageView);
        TextView headerTitleTextView = headerView.findViewById(R.id.textView2);

        // Update the content of the views with Firebase authentication data
        DatabaseReference userReference = database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid());
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);
                if (userModel != null) {
                    Glide.with(getApplicationContext()).load(userModel.getProfileImg()).into(headerImageView);
                    headerTitleTextView.setText(userModel.getName());  // Corrected line
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error if needed
            }
        });
    }




    private void logout() {
        auth = FirebaseAuth.getInstance();
        auth.signOut();
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }


    private boolean isConnected (Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
            }
        } else {
            // For older Android versions, you can use the deprecated method
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}