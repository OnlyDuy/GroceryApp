package com.example.groceryapp.ui.category;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.R;
import com.example.groceryapp.adapters.NavCategoryAdapter;
import com.example.groceryapp.adapters.PopularAdapters;
import com.example.groceryapp.databinding.FragmentCategoryBinding;
import com.example.groceryapp.models.NavCategoryModel;
import com.example.groceryapp.models.PopularModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {

    FirebaseFirestore db;
    RecyclerView recyclerView;
    List<NavCategoryModel> categoryModelList;
    NavCategoryAdapter navCategoryAdapter;

    ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_category, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = root.findViewById(R.id.cat_rec);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        categoryModelList = new ArrayList<>();
        navCategoryAdapter = new NavCategoryAdapter(getActivity(), categoryModelList);
        recyclerView.setAdapter(navCategoryAdapter);

        progressBar = root.findViewById(R.id.progressbar);

        progressBar.setVisibility(View.VISIBLE);

        // Kiểm tra kết nối Internet
        if (isConnected(requireContext())) {
            db.collection("NavCategory")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    NavCategoryModel navCategoryModel = document.toObject(NavCategoryModel.class);
                                    categoryModelList.add(navCategoryModel);
                                    navCategoryAdapter.notifyDataSetChanged();
                                }
                            } else {
                                Toast.makeText(getActivity(), "Error"+task.getException() , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            // Nếu không có kết nối, ẩn ProgressBar và hiển thị thông báo Toast
            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(requireContext(), "Không có Internet, Vui lòng kết nối", Toast.LENGTH_LONG).show();
        }

        return root;
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

    private void fetchDataFromFirebase() {
        // Thực hiện tác vụ lấy dữ liệu từ Firebase
        // ...
        // Khi hoàn thành, ẩn ProgressBar
        progressBar.setVisibility(View.GONE);
    }
}
