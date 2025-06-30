package com.example.bdapps.SearchPost;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.bdapps.Post;
import com.example.bdapps.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivityPost extends AppCompatActivity implements PostAdapter.OnPostClickListener {
    private Button searchToggleButton;
    private LinearLayout searchLayout;
    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private ApiService apiService;
    private boolean isSearchVisible = true;
    String accessToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_post);
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        initViews();
        setupRecyclerView();
        setupClickListeners();

        apiService = NetworkClient.getApiService();
    }
    private void initViews() {

        searchLayout = findViewById(R.id.searchLayout);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        recyclerView = findViewById(R.id.recyclerView);

        // Initially hide search layout
        searchLayout.setVisibility(View.VISIBLE);
    }
    private void setupRecyclerView() {
        postList = new ArrayList<>();
        postAdapter = new PostAdapter((Context) this, postList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);
    }
    private void setupClickListeners() {


        // Perform search
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                performSemanticSearch(query);
            } else {
                Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void performSemanticSearch(String query) {
        // Show loading state
        searchButton.setEnabled(false);
        searchButton.setText("Searching...");

        Call<List<SearchPost>> call = apiService.semanticSearch(
                "Bearer " + accessToken, // 👈 include Bearer prefix
                query,
                1,
                20
        );

        call.enqueue(new Callback<List<SearchPost>>() {
            @Override
            public void onResponse(Call<List<SearchPost>> call, Response<List<SearchPost>> response) {
                searchButton.setEnabled(true);
                searchButton.setText("Search");

                if (response.isSuccessful() && response.body() != null) {
                    postList.clear();
                    // Convert SearchPost to Post
                    for (SearchPost searchPost : response.body()) {
                        postList.add(searchPost.toPost());
                    }
                    postAdapter.notifyDataSetChanged();

                    if (postList.isEmpty()) {
                        Toast.makeText(SearchActivityPost.this, "No results found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SearchActivityPost.this, "Search failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SearchPost>> call, Throwable t) {
                searchButton.setEnabled(true);
                searchButton.setText("Search");
                Toast.makeText(SearchActivityPost.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onPostClick(Post post) {
        // Open post detail activity
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("post_id", post.getId());
        intent.putExtra("post_content", post.getContent());
        intent.putExtra("post_username", post.getUsername());
        intent.putExtra("post_time", post.getTimeAgo());
        startActivity(intent);
    }
}