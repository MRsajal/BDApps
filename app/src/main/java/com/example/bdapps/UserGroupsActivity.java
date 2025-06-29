package com.example.bdapps;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class UserGroupsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewGroups;
    private GroupsAdapter groupsAdapter;
    private DatabaseHelper_login dbHelper;
    private Button btnAction;
    List<Group> userGroups;
    int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_groups);

        recyclerViewGroups=findViewById(R.id.recyclerViewGroups);
        btnAction=findViewById(R.id.btnAction);

        dbHelper=new DatabaseHelper_login(this);

        String temp=getIntent().getStringExtra("current_username");
        User user=dbHelper.getUserByUsername(temp);
        if(user!=null){
            currentUserId=Integer.parseInt(user.getUserId());
        }
        else{
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
        }

        if(currentUserId==-1){
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setupRecyclerView();
        loadUserGroups();
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message="You have "+userGroups.size()+" groups!";
                Toast.makeText(UserGroupsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserGroups() {
        userGroups=dbHelper.getUserGroups(currentUserId);
        groupsAdapter.setGroups(userGroups);
        if(userGroups.isEmpty()){
            Toast.makeText(this, "You haven't joined any groups yet", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(this));
        groupsAdapter=new GroupsAdapter(this);
        recyclerViewGroups.setAdapter(groupsAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserGroups();
    }
}