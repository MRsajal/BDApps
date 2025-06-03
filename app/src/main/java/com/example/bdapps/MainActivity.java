package com.example.bdapps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    ImageButton btnMenu,btnChat;
    TextView btnAddPost;
    LinearLayout menuProfile,menuPost,menuLogout;
    RecyclerView recyclerView;
    PostAdapter postAdapter;
    List<Post> postList;
    String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setupClickListeners();
        setupRecyclerView();
    }

    private void initView() {
        drawerLayout=findViewById(R.id.drawer_layout);
        btnMenu=findViewById(R.id.btn_menu);
        btnChat=findViewById(R.id.btn_chat);
        btnAddPost=findViewById(R.id.btn_add_post);
        recyclerView=findViewById(R.id.recycler_view);

        menuProfile=findViewById(R.id.menu_profile);
        menuPost=findViewById(R.id.menu_post);
        menuLogout=findViewById(R.id.menu_logout);
        currentUsername= getIntent().getStringExtra("current_username");
    }

    private void setupRecyclerView(){
        postList=new ArrayList<>();
        postAdapter=new PostAdapter(postList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);
        addSamplePosts();
    }

    private void addSamplePosts() {
        postList.add(new Post("Welcome to our social media app! Share your thoughts and connect with others.", "Admin"));
        postList.add(new Post("Beautiful sunset today! 🌅", "User1"));
        postAdapter.notifyDataSetChanged();
    }

    private void setupClickListeners(){
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else{
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, ChatBot.class);
                startActivity(intent);
            }
        });
        btnAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPostDialog();
            }
        });
        menuProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Profile selected", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        menuPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Post selected", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(MainActivity.this, CreateGroup.class);
                intent.putExtra("current_username",currentUsername);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        menuLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Login.class));
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
    }

    private void showAddPostDialog() {
        Dialog dialog=new Dialog(this);
        dialog.setContentView(R.layout.add_post);
        dialog.getWindow().setLayout(
                (int)(getResources().getDisplayMetrics().widthPixels*0.9),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        EditText etPostContent=dialog.findViewById(R.id.et_post_content);
        TextView tvCharCounter=dialog.findViewById(R.id.tv_char_counter);
        Button btnCancel=dialog.findViewById(R.id.btn_cancel);
        Button btnPost=dialog.findViewById(R.id.btn_post);

        etPostContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCharCounter.setText(s.length()+"/500");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postContent=etPostContent.getText().toString().trim();
                if(!postContent.isEmpty()){
                    Post newPost=new Post(postContent,"You");
                    postAdapter.addPost(newPost);

                    recyclerView.smoothScrollToPosition(0);
                    Toast.makeText(MainActivity.this, "Post shared successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                else{
                    Toast.makeText(MainActivity.this, "Please write something to post", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();

    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }
}