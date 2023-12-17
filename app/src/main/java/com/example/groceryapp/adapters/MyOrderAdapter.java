package com.example.groceryapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.R;
import com.example.groceryapp.models.MyOrderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.net.Authenticator;
import java.util.List;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.ViewHolder> {

    Context context;
    List<MyOrderModel> myOrderModelList;
    int totalPrice = 0;
    FirebaseFirestore firestore;
    FirebaseAuth auth;

    public MyOrderAdapter (Context context, List<MyOrderModel> myOrderModelList) {
        this.context = context;
        this.myOrderModelList = myOrderModelList;
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MyOrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyOrderAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.my_order_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyOrderAdapter.ViewHolder holder, int position) {
        holder.name.setText(myOrderModelList.get(position).getProductName());
        holder.price.setText(myOrderModelList.get(position).getProductPrice());
        holder.date.setText(myOrderModelList.get(position).getCurrentDate());
        holder.time.setText(myOrderModelList.get(position).getCurrentTime());
        holder.quantity.setText(Integer.toString(myOrderModelList.get(position).getTotalQuantity()));
        holder.totalPrice.setText(Integer.toString(myOrderModelList.get(position).getTotalPrice()));
        // Set màu sắc cho background của view
       holder.colorIndicator.setBackgroundColor(ContextCompat.getColor(context, myOrderModelList.get(position).getColorResource()));

    }

    @Override
    public int getItemCount() {
        return myOrderModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View colorIndicator;
        TextView name, price, date, time, quantity, totalPrice;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.product_name);
            price = itemView.findViewById(R.id.product_price);
            date = itemView.findViewById(R.id.current_date);
            time = itemView.findViewById(R.id.current_time);
            quantity = itemView.findViewById(R.id.total_quantity);
            totalPrice = itemView.findViewById(R.id.total_price);

            // Ánh xạ trường colorIndicator từ item_my_order.xml
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
        }
    }
}
