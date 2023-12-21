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
        holder.rating.setText(list.get(position).getRating());
        holder.description.setText(list.get(position).getDescription());
        holder.price.setText(list.get(position).getPrice()+"/kg");

        if (list.get(position).getType().equals("egg")) {
            holder.price.setText(list.get(position).getPrice()+"/dozen");
        }
        if (list.get(position).getType().equals("milk")) {
            holder.price.setText(list.get(position).getPrice()+"/litre");
        }
        if (list.get(position).getType().equals("drink")) {
            holder.price.setText(Integer.toString(list.get(position).getPrice()));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
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
        TextView name,price,description,rating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.nav_cat_img);
            name = itemView.findViewById(R.id.nav_cat_name);
            description = itemView.findViewById(R.id.nav_cat_description);
            rating = itemView.findViewById(R.id.nav_cat_rating);
            price = itemView.findViewById(R.id.nav_cat_price);
        }
    }
}
