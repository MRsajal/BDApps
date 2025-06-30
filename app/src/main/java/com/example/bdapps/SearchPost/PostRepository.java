package com.example.bdapps.SearchPost;

import com.example.bdapps.Post;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostRepository {
    private ApiService apiService;

    public PostRepository() {
        this.apiService = NetworkClient.getApiService();
    }

    public void searchPosts(String token,String query, int page, int limit, SearchCallback callback) {
        Call<List<SearchPost>> call = apiService.semanticSearch(token,query, page, limit);

        call.enqueue(new Callback<List<SearchPost>>() {
            @Override
            public void onResponse(Call<List<SearchPost>> call, Response<List<SearchPost>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Convert SearchPost to Post
                    List<Post> posts = new ArrayList<>();
                    for (SearchPost searchPost : response.body()) {
                        posts.add(searchPost.toPost());
                    }
                    callback.onSuccess(posts);
                } else {
                    callback.onError("Search failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<SearchPost>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public interface SearchCallback {
        void onSuccess(List<Post> posts);
        void onError(String error);
    }
}
