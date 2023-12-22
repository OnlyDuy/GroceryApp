package com.example.groceryapp.ui.mycarts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.tv.BroadcastInfoRequest;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.example.groceryapp.activities.PlaceOrderActivity;
import com.example.groceryapp.adapters.MyCartAdapter;
import com.example.groceryapp.models.MyCartModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class MyCartsFragment extends Fragment {

    FirebaseFirestore db;
    FirebaseAuth auth;

    RecyclerView recyclerView;
    MyCartAdapter cartAdapter;
    List<MyCartModel> cartModeList;

    TextView overTotalAmount;

    Button buyNow;
    int totalBill;
    ProgressBar progressBar;
    ConstraintLayout constraint1;
    ConstraintLayout constraint2;
    public MyCartsFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_my_carts, container, false);
//
//        // Đăng ký broadcast receiver
//        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
//                updateCartDisplayReceiver,
//                new IntentFilter("update_cart_display")
//        );

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        progressBar = root.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);

        constraint1 = root.findViewById(R.id.constraint1);
        constraint2 = root.findViewById(R.id.constraint2);

        overTotalAmount = root.findViewById(R.id.total_view_price);

        recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setVisibility(View.GONE);
        buyNow = root.findViewById(R.id.buy_now);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        cartModeList = new ArrayList<>();
        cartAdapter = new MyCartAdapter(getActivity(), cartModeList);
        recyclerView.setAdapter(cartAdapter);

        db.collection("CurrentUser").document(auth.getCurrentUser().getUid())
                .collection("AddToCart").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {

                                String documentId = documentSnapshot.getId();
                                // Lấy thông tin từ Collection có tên là AddToCart dể hiện lên màn hình
                                MyCartModel cartModel = documentSnapshot.toObject(MyCartModel.class);

                                cartModel.setDocumentId(documentId);

                                cartModeList.add(cartModel);
                                cartAdapter.notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                // Ẩn đi constraint1 và hiện constraint2
                                constraint1.setVisibility(View.GONE);
                                constraint2.setVisibility(View.VISIBLE);
                            }

                            calculateTotalAmount(cartModeList);
                        }
                    }
                });
        if (isConnected(requireContext())) {
            buyNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Xóa dữ liệu giỏ hàng trên Firestore
                    deleteCartDataFromFirestore();

                    Intent intent = new Intent(getContext(), PlaceOrderActivity.class);
                    intent.putExtra("itemList", (Serializable) cartModeList);
                    startActivity(intent);
                }
            });
        } else {
            // Nếu không có kết nối, ẩn ProgressBar và hiển thị thông báo Toast
            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(requireContext(), "Không có Internet, Vui lòng kết nối", Toast.LENGTH_LONG).show();
        }

        return root;
    }

    private void deleteCartDataFromFirestore() {
        for (MyCartModel cartModel : cartModeList) {
            // Xóa từng tài liệu trong giỏ hàng trên Firestore
            db.collection("CurrentUser")
                    .document(auth.getCurrentUser().getUid())
                    .collection("AddToCart")
                    .document(cartModel.getDocumentId())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Xóa thành công
                            cartAdapter.notifyDataSetChanged();
                            Log.d("MyCartsFragment", "DocumentSnapshot successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Xử lý khi xóa thất bại
                            Log.w("MyCartsFragment", "Error deleting document", e);
                        }
                    });
        }
    }

//
//    private BroadcastReceiver updateCartDisplayReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // Cập nhật giao diện người dùng theo cần thiết
//            // Hiển thị constraint1 và ẩn constraint2
//            Log.d("MyCartsFragment", "Broadcast received");
//            constraint1.setVisibility(View.GONE);
//            constraint2.setVisibility(View.VISIBLE);
//        }
//    };
//
//    @Override
//    public void onDestroyView() {
//        // Hủy đăng ký receiver để tránh rò rỉ bộ nhớ
//        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(updateCartDisplayReceiver);
//        super.onDestroyView();
//    }

    private void calculateTotalAmount(List<MyCartModel> cartModeList) {
        double totalAmount = 0.0;
        for (MyCartModel myCartModel: cartModeList) {
            totalAmount += myCartModel.getTotalPrice();
        }

        overTotalAmount.setText("total Amount: "+totalAmount);
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
}