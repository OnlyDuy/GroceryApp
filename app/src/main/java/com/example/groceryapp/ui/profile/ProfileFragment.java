package com.example.groceryapp.ui.profile;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.groceryapp.R;
import com.example.groceryapp.MainActivity;
import com.example.groceryapp.models.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    CircleImageView profileImg;
    EditText name, email, phone, address;

    Button updateBtn;

    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseDatabase database;

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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if (!isConnected(getContext())) {
            Toast.makeText(getContext(), "Không có Internet, Vui lòng kết nối", Toast.LENGTH_SHORT).show();
            View root = inflater.inflate(R.layout.fragment_profile, container, false);
            return root;
        } else {
            View root = inflater.inflate(R.layout.fragment_profile, container, false);

            auth = FirebaseAuth.getInstance();
            storage = FirebaseStorage.getInstance();
            database = FirebaseDatabase.getInstance();

            profileImg = root.findViewById(R.id.profile_img);
            name = root.findViewById(R.id.profile_name);
            email = root.findViewById(R.id.profile_email);
            phone = root.findViewById(R.id.profile_number);
            address = root.findViewById(R.id.profile_address);
            updateBtn = root.findViewById(R.id.update);

            database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            if (userModel != null) {
                                if (userModel.getProfileImg() != null) {
                                    Glide.with(requireContext()).load(userModel.getProfileImg()).into(profileImg);
                                }else {
                                    profileImg.setImageResource(R.drawable.profile);
                                }

                                name.setText(userModel.getName());
                                email.setText(userModel.getEmail());
                                phone.setText(userModel.getPhone());
                                address.setText(userModel.getAddress());
                            } else {
                                Log.e("ProfileFragment", "User data is null");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("ProfileFragment", "Error loading profile image: " + error.getMessage());
                        }
                    });

            profileImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT); // fetch files
                    intent.setType("image/*"); // fetch all types of images
                    startActivityForResult(intent, 33);
                }
            });

            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUserProfile();
                    // Refresh the header after updating the profile
                    ((MainActivity) requireActivity()).setNavHeader();
                }
            });

            return root;
        }
    }

    private void updateUserProfile() {
        String newName = name.getText().toString().trim();
        String newEmail = email.getText().toString().trim();
        String newPhone = phone.getText().toString().trim();
        String newAddress = address.getText().toString().trim();

        if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty() || newAddress.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userReference = database.getReference().child("Users").child(auth.getUid());

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("email", newEmail);
        updates.put("phone", newPhone);
        updates.put("address", newAddress);

        userReference.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ((MainActivity) requireActivity()).setNavHeader();
                Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null) {
            Uri profileImgUri = data.getData(); // get image uri

            final StorageReference reference = storage.getReference().child("profile_pictures")
                    .child(auth.getUid());

            reference.putFile(profileImgUri).addOnSuccessListener(taskSnapshot -> { // upload image to Firebase storage
                Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();

                reference.getDownloadUrl().addOnSuccessListener(uri -> { // get image url from Firebase storage
                    DatabaseReference userReference = database.getReference().child("Users").child(auth.getUid());
                    userReference.child("profileImg").setValue(uri.toString()) // save image url in Firebase database
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Profile Picture Updated", Toast.LENGTH_SHORT).show();
                                    // Load the updated user profile in the main activity
                                    ((MainActivity) requireActivity()).setNavHeader();
                                    // Load the updated user profile in the profile fragment
                                    loadUserProfile();
                                } else {
                                    Toast.makeText(getContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                                }
                            });
                });
            });
        }
    }


    private void loadUserProfile() {
        database.getReference().child("Users").child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        if (userModel != null) {
                            Glide.with(requireContext()).load(userModel.getProfileImg()).into(profileImg);
                            name.setText(userModel.getName());
                            email.setText(userModel.getEmail());
                            phone.setText(userModel.getPhone());
                            address.setText(userModel.getAddress());
                        } else {
                            Log.e("ProfileFragment", "User data is null");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ProfileFragment", "Error loading profile image: " + error.getMessage());
                    }
                });
    }

}