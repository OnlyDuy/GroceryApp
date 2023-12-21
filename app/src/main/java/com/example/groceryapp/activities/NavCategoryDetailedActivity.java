package com.example.groceryapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.groceryapp.R;
import com.example.groceryapp.models.NavCategoryDetailedModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class NavCategoryDetailedActivity extends AppCompatActivity {
    TextView quantity;
    int totalQuantity = 1;
    int totalPrice = 0;
    ImageView detailedImg;
    TextView price, rating, description;
    Button addToCart;
    ImageView addItem, removeItem;
    Toolbar toolbar;

    FirebaseFirestore firestore;
    FirebaseAuth auth;

    NavCategoryDetailedModel model;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Bắt sự kiện click cho nút back
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Quay lại màn hình trước đó
            }
        });

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        final Object object = getIntent().getSerializableExtra("detail");
        if (object instanceof NavCategoryDetailedModel) {
            model = (NavCategoryDetailedModel) object;
        }

        quantity = findViewById(R.id.quantity);

        detailedImg = findViewById(R.id.detailed_img);
        addItem = findViewById(R.id.add_item);
        removeItem = findViewById(R.id.remove_item);

        price = findViewById(R.id.detailed_price);
        rating = findViewById(R.id.detailed_rating);
        description = findViewById(R.id.detailed_des);

        if (model != null) {
            Glide.with(getApplicationContext()).load(model.getImg_url()).into(detailedImg);
            rating.setText(model.getRating());
            description.setText(model.getDescription());
            price.setText("Price: $"+model.getPrice()+"/kg");

            if (model.getType().equals("egg")) {
                price.setText("Price: $"+model.getPrice()+"/dozen");
            }

            if (model.getType().equals("milk")) {
                price.setText("Price: $"+model.getPrice()+"/litre");
            }
        }

        addToCart = findViewById(R.id.add_to_cart);
        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addedToCart();
            }
        });
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (totalQuantity < 10) {
                    totalQuantity++;
                    quantity.setText(String.valueOf(totalQuantity));
                    totalPrice = Integer.valueOf(model.getPrice()) * totalQuantity;
                    if (model.getType().equals("egg")) {
                        price.setText("Price: $" + totalPrice + "/dozen");
                    } else if (model.getType().equals("milk")) {
                        price.setText("Price: $" + totalPrice + "/litre");
                    } else {
                        price.setText("Price: $" + totalPrice + "/kg");
                    }
                }
            }
        });
        removeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (totalQuantity > 0) {
                    totalQuantity--;
                    quantity.setText(String.valueOf(totalQuantity));
                    totalPrice = Integer.valueOf(model.getPrice()) * totalQuantity;
                    if (model.getType().equals("egg")) {
                        price.setText("Price: $" + totalPrice + "/dozen");
                    } else if (model.getType().equals("milk")) {
                        price.setText("Price: $" + totalPrice + "/litre");
                    } else {
                        price.setText("Price: $" + totalPrice + "/kg");
                    }
                }
            }
        });
    }

    private void addedToCart() {
        String saveCurrentDate, saveCurrentTime;
        Calendar calForDate = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calForDate.getTime());

        final HashMap<String,Object> cartMap = new HashMap<>();

        cartMap.put("productName", model.getName());
        cartMap.put("productPrice", price.getText().toString());
        cartMap.put("currentDate", saveCurrentDate);
        cartMap.put("currentTime", saveCurrentTime);
        cartMap.put("totalQuantity", totalQuantity);
        cartMap.put("totalPrice", totalPrice);

        firestore.collection("CurrentUser").document(auth.getCurrentUser().getUid())
                .collection("AddToCart").add(cartMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        Toast.makeText(NavCategoryDetailedActivity.this, "Added To A Cart", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
        // Gửi broadcast
//        Intent intent = new Intent("update_cart_display");
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//        Log.d("MyCartsFragment", "Broadcast sent");
    }
}