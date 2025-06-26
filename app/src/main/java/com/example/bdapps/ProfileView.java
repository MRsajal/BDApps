package com.example.bdapps;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProfileView extends AppCompatActivity {
    String username;
    TextView tv_username;
    ShapeableImageView profileImageView;
    ProfileImageHandler profileImageHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        tv_username=findViewById(R.id.tv_username);
        profileImageView=findViewById(R.id.profile_image);


        username=getIntent().getStringExtra("current_username");
        tv_username.setText(username);
        profileImageHandler=new ProfileImageHandler(this,profileImageView);

        ProfileViewTabsAdapter adapter = new ProfileViewTabsAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) tab.setText("About");
            else tab.setText("Posts");
        }).attach();
        refreshProfileImage();

    }
    public void refreshProfileImage(){
        if (profileImageHandler != null) {
            profileImageHandler.loadProfileImage();
        }
    }
}