package com.skyblue.recyclerviewloadmoremethod1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.skyblue.recyclerviewloadmoremethod1.databinding.ActivityMainBinding;
import com.skyblue.recyclerviewloadmoremethod1.retrofit.APIClient;
import com.skyblue.recyclerviewloadmoremethod1.retrofit.APIInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private final String TAG = "load_more";
    private final Context context = this;
    private RecyclerViewAdapter recyclerViewAdapter;
    private List<Post> rowsArrayList;
    boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        rowsArrayList = new ArrayList<>();

        initRecyclerView();
        loadList();
        initScrollListener();
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setItemViewCacheSize(20);
        recyclerViewAdapter = new RecyclerViewAdapter(rowsArrayList);
        binding.recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void loadList() {

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call=apiInterface.getCommonPosts("1");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    String postList = "";

                    try {
                        assert response.body() != null;
                        postList = response.body().string();
                        JSONArray jsonArray = new JSONArray(postList);

                        for (int i = 0; i<jsonArray.length(); i++) {
                            Post post = new Post();
                            JSONObject object = jsonArray.getJSONObject(i);
                            post.setUser_name(object.getString("user_name"));
                            post.setPost_id(object.getString("post_id"));
                            rowsArrayList.add(post);
                        }

                        recyclerViewAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                       // throw new RuntimeException(e);
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initScrollListener() {
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == rowsArrayList.size() - 1) {
                        //bottom of list!
                        loadMore();
                        Log.e("loadmore_", "isLoading = true");
                        isLoading = true;
                    }
                }
            }
        });
    }

    private void loadMore() {
        // add load more progressbar
        rowsArrayList.add(null);
        recyclerViewAdapter.notifyItemInserted(rowsArrayList.size() - 1);

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call=apiInterface.getCommonPosts("1");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    String postList = "";

                    try {
                        assert response.body() != null;
                        postList = response.body().string();
                        JSONArray jsonArray = new JSONArray(postList);

                        // remove progress bar
                        rowsArrayList.remove(rowsArrayList.size() - 1);
                        int scrollPosition = rowsArrayList.size();
                        recyclerViewAdapter.notifyItemRemoved(scrollPosition);

                        for (int i = 0; i<jsonArray.length(); i++) {
                            Post post = new Post();
                            JSONObject object = jsonArray.getJSONObject(i);
                            post.setUser_name(object.getString("user_name"));
                            post.setPost_id(object.getString("post_id"));
                            rowsArrayList.add(post);
                        }

                        int arrayListSize = rowsArrayList.size();
                        Log.e(TAG, "Load more : arrayListSize " + arrayListSize);

                        recyclerViewAdapter.notifyDataSetChanged();
                        isLoading = false;
                    } catch (JSONException e) {
                        // throw new RuntimeException(e);
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}