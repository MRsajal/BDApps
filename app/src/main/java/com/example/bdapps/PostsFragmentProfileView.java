package com.example.bdapps;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.RequestQueue;

import java.util.List;

public class PostsFragmentProfileView extends Fragment {
    private String etUsername;
    private RecyclerView rvPosts;
    private ProgressBar progressBar;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private RequestQueue requestQueue;
    private static final String API_URL = "https://dormitorybackend.duckdns.org/api/posts";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_profile_view, container, false);
    }
}