package com.example.groceryapp.ui.profile;

import android.content.Intent;
import android.net.Uri;
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
import com.example.groceryapp.models.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    CircleImageView profileImg;
    EditText name, email, phone, address;

    Button updateBtn;

    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseDatabase database;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

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
            }
        });

        return root;
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

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .child("name").setValue(newName);
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .child("email").setValue(newEmail);
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .child("phone").setValue(newPhone);
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .child("address").setValue(newAddress);

        Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data.getData() != null) {
            Uri profileImgUri = data.getData(); // get image uri
            profileImg.setImageURI(profileImgUri); // set image in profile image view

            final StorageReference reference = storage.getReference().child("profile_pictures")
                    .child(FirebaseAuth.getInstance().getUid());

            reference.putFile(profileImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { // upload image to firebase storage
                    Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();

                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) { // get image url from firebase storage
                            database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                                    .child("profileImg")
                                    .setValue(uri.toString()); // save image url in firebase database
                            Toast.makeText(getContext(), "Profile Picture Updated", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

    }
}