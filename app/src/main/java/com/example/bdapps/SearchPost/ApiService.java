package com.example.bdapps.SearchPost;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ApiService {
    @GET("api/llm/semantic-search/")
    Call<List<SearchPost>> semanticSearch(
            @Header("Authorization") String token,
            @Query("query") String query,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

}
