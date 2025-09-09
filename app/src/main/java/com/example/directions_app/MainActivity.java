package com.example.directions_app;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.directions_app.adapter.AddressAdapter;
import com.example.directions_app.model.Address;
import com.example.directions_app.network.ApiService;
import java.util.List;
import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private EditText edtSearch;
    private RecyclerView recyclerView;
    private AddressAdapter adapter;
    private Handler handler = new Handler();
    private Runnable searchRunnable,resetIconRunnable;
    private ApiService apiService;
    private final String API_KEY = "pk.bd1ce34b80403277af968f7cf0c48cf3";
    private ImageView iconView;
    private ImageView btnClearText;

    private int searchIconResId = R.drawable.ic_search;
    private int loadingIconResId = R.drawable.ic_loading;
    private ObjectAnimator rotationAnimator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iconView = findViewById(R.id.icon);
        btnClearText= findViewById(R.id.btnClearText);
        edtSearch = findViewById(R.id.edtSearch);
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new AddressAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://us1.locationiq.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        iconView.post(new Runnable() {
            @Override
            public void run() {
                View parent = (View) iconView.getParent();
                if (parent != null) {
                    int parentHeight = parent.getHeight();
                    int newHeight = (int) (parentHeight * 0.05);
                    ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
                    layoutParams.height = newHeight;
                    iconView.setLayoutParams(layoutParams);
                    btnClearText.setLayoutParams(layoutParams);
                }
            }
        });

        rotationAnimator = ObjectAnimator.ofFloat(iconView, "rotation", 0f, 360f);
        rotationAnimator.setDuration(1000); // 1 giây cho một vòng xoay
        rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE); // Lặp lại vô hạn
        rotationAnimator.setInterpolator(new LinearInterpolator());

        resetIconRunnable = () -> {
            iconView.setImageResource(searchIconResId);
            if (rotationAnimator.isRunning()) {
                rotationAnimator.cancel();
            }
            iconView.setRotation(0f);
        };

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(resetIconRunnable);

                if (s.length() > 0) {
                    iconView.setImageResource(loadingIconResId);
                    if (!rotationAnimator.isRunning()) {
                        rotationAnimator.start();
                    }
                } else {
                    iconView.setImageResource(searchIconResId);
                    if (rotationAnimator.isRunning()) {
                        rotationAnimator.cancel();
                    }
                    iconView.setRotation(0f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Đây là phần sử lý lệnh gọi API
                handler.removeCallbacks(searchRunnable);
                searchRunnable = () -> {
                    String query = s.toString().trim();
                    if (query.isEmpty()) {
                        adapter.setData(null, "");
                        return;
                    }
                    searchAddress(query);
                };
                handler.postDelayed(searchRunnable, 1000);

                //Đây là phần sử lý ẩn hiện nút X
                if (s.length() > 0) {
                    btnClearText.setVisibility(View.VISIBLE);
                } else {
                    btnClearText.setVisibility(View.GONE);
                }

                if (s.length() > 0) {
                    handler.postDelayed(resetIconRunnable, 1150); // 1200ms = 1.2s
                }
            }
        });

        btnClearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSearch.setText("");
            }
        });


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(resetIconRunnable);
        if (rotationAnimator.isRunning()) {
            rotationAnimator.cancel();
        }
    }
    private void searchAddress(String query) {
        if (query.isEmpty()) {
            adapter.setData(null, "");
            return;
        }
        apiService.searchAddress(API_KEY, query, "json", "vn", 50)
                .enqueue(new retrofit2.Callback<List<Address>>() {
                    @Override
                    public void onResponse(Call<List<Address>> call, retrofit2.Response<List<Address>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setData(response.body(), query);
                        } else {
                            adapter.setData(null, "");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Address>> call, Throwable t) {
                        adapter.setData(null, "");
                    }
                });
    }

}
