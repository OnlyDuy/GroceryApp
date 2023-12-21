package com.example.groceryapp.ui.myorders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.groceryapp.R;
import com.example.groceryapp.adapters.MyCartAdapter;
import com.example.groceryapp.adapters.MyOrderAdapter;
import com.example.groceryapp.models.MyCartModel;
import com.example.groceryapp.models.MyOrderModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MyOrdersFragment extends Fragment {
    FirebaseFirestore db;
    FirebaseAuth auth;

    RecyclerView recyclerView;
    MyOrderAdapter orderAdapter;
    List<MyOrderModel> myOrderModelList;

    TextView overTotalAmount;

    int totalBill;
    ProgressBar progressBar;
    ConstraintLayout constraint1;
    ConstraintLayout constraint2;
    public MyOrdersFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_my_orders, container, false);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        progressBar = root.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);

        constraint1 = root.findViewById(R.id.constraint1);
        constraint2 = root.findViewById(R.id.constraint2);

        overTotalAmount = root.findViewById(R.id.total_view_price);

        recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setVisibility(View.GONE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        myOrderModelList = new ArrayList<>();
        orderAdapter = new MyOrderAdapter(getActivity(), myOrderModelList);
        recyclerView.setAdapter(orderAdapter);

        db.collection("CurrentUser").document(auth.getCurrentUser().getUid())
                .collection("MyOrder").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().isEmpty()) {
                            // Hiển thị constaint1 nếu danh sách rỗng
                            constraint1.setVisibility(View.VISIBLE);
                            constraint2.setVisibility(View.GONE);
                        } else if (task.isSuccessful()) {
                            // Hiển thị constaint2 nếu có dữ liệu
                            constraint1.setVisibility(View.GONE);
                            constraint2.setVisibility(View.VISIBLE);

                            for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {

                                String documentId = documentSnapshot.getId();

                                MyOrderModel orderModel = documentSnapshot.toObject(MyOrderModel.class);

                                orderModel.setDocumentId(documentId);

                                myOrderModelList.add(orderModel);
                            }
                            Collections.sort(myOrderModelList, new Comparator<MyOrderModel>() {
                                @Override
                                public int compare(MyOrderModel order1, MyOrderModel order2) {
                                    int dateComparison = order2.getCurrentDate().compareTo(order1.getCurrentDate());

                                    // Nếu CurrentDate giống nhau, so sánh theo currentTime
                                    if (dateComparison == 0) {
                                        // Nếu CurrentDate giống nhau, so sánh theo currentTime
                                        return order2.getCurrentTime().compareTo(order1.getCurrentTime());
                                    }
                                    return dateComparison;
                                }
                            });
                            orderAdapter.notifyDataSetChanged();
                            calculateTotalAmount(myOrderModelList);
                        }
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
        return root;
    }

    private void calculateTotalAmount(List<MyOrderModel> myOrderModelList) {
        double totalAmount = 0.0;
        for (MyOrderModel myOrderModel: myOrderModelList) {
            totalAmount += myOrderModel.getTotalPrice();
        }

        overTotalAmount.setText("total Amount: "+totalAmount);
    }
}