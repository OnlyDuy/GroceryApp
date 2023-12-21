package com.example.groceryapp.ui.home;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.R;
import com.example.groceryapp.activities.ViewAllActivity;
import com.example.groceryapp.adapters.HomeAdapter;
import com.example.groceryapp.adapters.PopularAdapters;
import com.example.groceryapp.adapters.RecommendedAdapter;
import com.example.groceryapp.adapters.ViewAllAdapter;
import com.example.groceryapp.databinding.FragmentHomeBinding;
import com.example.groceryapp.models.HomeCategory;
import com.example.groceryapp.models.PopularModel;
import com.example.groceryapp.models.RecommendedModel;
import com.example.groceryapp.models.ViewAllModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeFragment extends Fragment {
    ScrollView scrollView;
    ProgressBar progressBar;
    private FragmentHomeBinding binding;
    RecyclerView popularRec, homeCatRec, recommendedRec;
    FirebaseFirestore db;

    // Popular Items
    List<PopularModel> popularModelList;
    PopularAdapters popularAdapters;

    // Search View
    EditText search_box;
    private List<ViewAllModel> viewAllModelList;
    private RecyclerView recyclerViewSearch;
    private ViewAllAdapter viewAllAdapter;

    private TextView viewAllExploreTextView;

    // Home Category
    List<HomeCategory> categoryList;
    HomeAdapter homeAdapter;

    // Recommended
    List<RecommendedModel> recommendedModelList;
    RecommendedAdapter recommendedAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();

        popularRec = root.findViewById(R.id.pop_rec);
        homeCatRec = root.findViewById(R.id.explore_rec);
        recommendedRec = root.findViewById(R.id.recommended_rec);
        scrollView = root.findViewById(R.id.scroll_view);
        progressBar = root.findViewById(R.id.progressbar);

        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);

        // Kiểm tra kết nối internet
        if (isConnected(requireContext())) {
            // Popular Items
            popularRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
            popularModelList = new ArrayList<>();
            popularAdapters = new PopularAdapters(getActivity(), popularModelList);
            popularRec.setAdapter(popularAdapters);
            db.collection("PopularProduct")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    PopularModel popularModel = document.toObject(PopularModel.class);
                                    popularModelList.add(popularModel);
                                    popularAdapters.notifyDataSetChanged();

                                    progressBar.setVisibility(View.GONE);
                                    scrollView.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Toast.makeText(getActivity(), "Error"+task.getException() , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            fetchDataFromFirebase();
        } else {
            // Nếu không có kết nối, ẩn ProgressBar và hiển thị thông báo Toast
            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(requireContext(), "Không có Internet, Vui lòng kết nối", Toast.LENGTH_LONG).show();
        }

            // Home Category
            homeCatRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
            categoryList = new ArrayList<>();
            homeAdapter = new HomeAdapter(getActivity(), categoryList);
            homeCatRec.setAdapter(homeAdapter);

            db.collection("HomeCategory")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    HomeCategory homeCategory = document.toObject(HomeCategory.class);
                                    categoryList.add(homeCategory);
                                    homeAdapter.notifyDataSetChanged();
                                }
                            } else {
                                Toast.makeText(getActivity(), "Error"+task.getException() , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            // Recommended
            recommendedRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
            recommendedModelList = new ArrayList<>();
            recommendedAdapter = new RecommendedAdapter(getActivity(), recommendedModelList);
            recommendedRec.setAdapter(recommendedAdapter);

            db.collection("Recommended")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    RecommendedModel recommendedModel = document.toObject(RecommendedModel.class);
                                    recommendedModelList.add(recommendedModel);
                                    recommendedAdapter.notifyDataSetChanged();
                                }
                            } else {
                                Toast.makeText(getActivity(), "Error"+task.getException() , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            // Search box
            recyclerViewSearch = root.findViewById(R.id.search_rec);
            search_box = root.findViewById(R.id.search_box);
            viewAllModelList = new ArrayList<>();
            viewAllAdapter = new ViewAllAdapter(getContext(),viewAllModelList);
            recyclerViewSearch.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerViewSearch.setAdapter(viewAllAdapter);
            recyclerViewSearch.setHasFixedSize(true);
            search_box.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.toString().isEmpty()) {
                        viewAllModelList.clear();
                        viewAllAdapter.notifyDataSetChanged();
                    } else {
                        searchProduct(s.toString());
                    }
                }

            });

        // Lấy tham chiếu đến TextView
        viewAllExploreTextView = root.findViewById(R.id.view_all_explore);

        // Gắn sự kiện click cho TextView
        viewAllExploreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển đến CategoryFragment khi TextView được click
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_categoryFragment);
            }
        });
        return root;
    }

    private void searchProduct(String type) {
        if (!type.isEmpty()) {
            db.collection("AllProducts").whereEqualTo("type", type).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                viewAllModelList.clear();
                                viewAllAdapter.notifyDataSetChanged();
                                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    ViewAllModel viewAllModel = doc.toObject(ViewAllModel.class);
                                    viewAllModelList.add(viewAllModel);
                                    viewAllAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
        }
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
