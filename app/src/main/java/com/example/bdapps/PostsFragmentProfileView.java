package com.example.bdapps;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class PostsFragmentProfileView extends Fragment {
    RecyclerView recyclerView;
    List<Post> postList;
    PostAdapter postAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post_profile_view, container, false);
        recyclerView=view.findViewById(R.id.post_recycler_view_profile);
        setupRecyclerView();

        return view;
    }
    private void setupRecyclerView(){
        postList=new ArrayList<>();
        postAdapter=new PostAdapter(postList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(postAdapter);
        addSamplePosts();
    }

    private void addSamplePosts() {
        postList.add(new Post("Welcome to our social media app! Share your thoughts and connect with others.", "Admin"));
        postList.add(new Post("Beautiful sunset today! 🌅", "User1"));
        postList.add(new Post("Welcome to our social media app! Share your thoughts and connect with others.", "Admin"));
        postList.add(new Post("Beautiful sunset today! 🌅", "User1"));
        postList.add(new Post("Welcome to our social media app! Share your thoughts and connect with others.", "Admin"));
        postList.add(new Post("Beautiful sunset today! 🌅", "User1"));
        postAdapter.notifyDataSetChanged();
    }
}