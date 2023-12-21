package com.example.groceryapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.groceryapp.R;
import com.example.groceryapp.activities.DetailedActivity;
import com.example.groceryapp.activities.NavCategoryDetailedActivity;
import com.example.groceryapp.models.NavCategoryDetailedModel;

import java.io.Serializable;
import java.util.List;

public class NavCategoryDetailedAdapter extends RecyclerView.Adapter<NavCategoryDetailedAdapter.ViewHolder> {

    Context context;
    List<NavCategoryDetailedModel> list;

    public NavCategoryDetailedAdapter(Context context, List<NavCategoryDetailedModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public NavCategoryDetailedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_category_detailed_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull NavCategoryDetailedAdapter.ViewHolder holder, int position) {
        Glide.with(context).load(list.get(position).getImg_url()).into(holder.imageView);
        holder.name.setText(list.get(position).getName());
        holder.price.setText(String.valueOf(list.get(position).getPrice()));

        holder.buy_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, NavCategoryDetailedActivity.class);
                intent.putExtra("detail", list.get(holder.getAdapterPosition()));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView name,price;
        Button buy_now;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.cat_nav_img);
            name = itemView.findViewById(R.id.nav_cat_name);
            price = itemView.findViewById(R.id.nav_cat_price);
            buy_now = itemView.findViewById(R.id.buy_now);
        }
    }
}
