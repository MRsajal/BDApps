//package com.example.bdapps;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.View;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.android.material.appbar.MaterialToolbar;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.textfield.TextInputEditText;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CreateGroup extends AppCompatActivity implements UserSearchAdapter.OnUserClickListener{
//
//    TextInputEditText etGroupName, etGroupDescription, etSearchUsers;
//    TextView tvSelectedCount;
//    RecyclerView rvSearchResults;
//    MaterialButton btnCreateGroup;
//    MaterialToolbar toolbar;
//    UserSearchAdapter userAdapter;
//    List<User> allUsers;
//    List<User> selectedUsers;
//    List<User> filteredUsers;
//    DatabaseHelper_login dbHelper;
//    Handler searchHandler;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_create_group);
//        initializeViews();
//        setupToolbar();
//        setupRecyclerView();
//        setupListeners();
//        loadUsers();
//    }
//
//    private void loadUsers() {
//        allUsers = dbHelper.getAllUsers();
//        String currentUsername=getCurrentUsername();
//        if(currentUsername!=null){
//            allUsers.removeIf(user->user.getUsername().equals(currentUsername));
//        }
//        filteredUsers.clear();
//        filteredUsers.addAll(allUsers);
//        userAdapter.notifyDataSetChanged();
//    }
//    private String getCurrentUsername(){
//        String username= getSharedPreferences("user_prefs",MODE_PRIVATE)
//                .getString("current_username",null);
//        if(username!=null){
//            return username;
//        }
//        Intent intent = getIntent();
//        if (intent != null) {
//            username = intent.getStringExtra("current_username");
//            if (username != null) {
//                return username;
//            }
//
//            username = intent.getStringExtra("username");
//            if (username != null) {
//                return username;
//            }
//        }
//        username = getSharedPreferences("UserPrefs", MODE_PRIVATE)
//                .getString("username", null);
//
//        if (username != null) {
//            return username;
//        }
//        username = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE)
//                .getString("logged_in_user", null);
//
//        return username;
//    }
//
//    private void setupListeners() {
//        etSearchUsers.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                searchHandler.removeCallbacksAndMessages(null);
//                searchHandler.postDelayed(() -> filterUsers(s.toString()), 300);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                createGroup();
//            }
//        });
//        etGroupName.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                validateCreateButton();
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//    }
//
//    private void createGroup() {
//        String groupName=etGroupName.getText().toString().trim();
//        String groupDescription=etGroupDescription.getText().toString().trim();
//        String currentUsername=getCurrentUsername();
//        if(groupName.isEmpty()){
//            etGroupName.setError("Group name is required");
//            etGroupName.requestFocus();
//            return;
//        }
//        if(selectedUsers.isEmpty()){
//            Toast.makeText(this, "Please select at least one user", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if(currentUsername==null){
//            Toast.makeText(this, "User session expired. Please login again", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        btnCreateGroup.setEnabled(false);
//        btnCreateGroup.setText("Creating...");
//
//        try {
//            User currentUser=dbHelper.getUserByUsername(currentUsername);
//            if(currentUser==null){
//                Toast.makeText(this, "User not found. Please login again", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            int createdBy=Integer.parseInt(currentUser.getUserId());
//            List<Integer> memberIds=new ArrayList<>();
//            for(User user:selectedUsers){
//                memberIds.add(Integer.parseInt(user.getUserId()));
//            }
//            long groupId= dbHelper.createGroup(groupName,groupDescription,createdBy,memberIds);
//            if(groupId!=-1){
//                Toast.makeText(this, "Group created successfully", Toast.LENGTH_SHORT).show();
//                Intent resultIntent=new Intent();
//                resultIntent.putExtra("group_created",true);
//                resultIntent.putExtra("group_name",groupName);
//                resultIntent.putExtra("group_id",groupId);
//                setResult(RESULT_OK,resultIntent);
//                finish();
//            }else{
//                Toast.makeText(this, "Failed to create group", Toast.LENGTH_SHORT).show();
//            }
//        }catch (Exception e){
//            Toast.makeText(this, "Error creating group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }finally {
//            btnCreateGroup.setEnabled(true);
//            btnCreateGroup.setText("Create Group");
//            validateCreateButton();
//        }
//    }
//
//    private void filterUsers(String query) {
//        filteredUsers.clear();
//
//        if (query.trim().isEmpty()) {
//            filteredUsers.addAll(allUsers);
//        } else {
//            String lowerQuery = query.toLowerCase().trim();
//            for (User user : allUsers) {
//                if (user.getUsername().toLowerCase().contains(lowerQuery) ||
//                        user.getUsername().toLowerCase().contains(lowerQuery) ||
//                        user.getEmail().toLowerCase().contains(lowerQuery)) {
//                    filteredUsers.add(user);
//                }
//            }
//        }
//
//        userAdapter.notifyDataSetChanged();
//    }
//
//    private void validateCreateButton() {
//        String groupName=etGroupName.getText().toString().trim();
//        boolean isValid=!groupName.isEmpty() && selectedUsers.size()>0;
//        btnCreateGroup.setEnabled(isValid);
//        btnCreateGroup.setAlpha(isValid?1.0f:0.5f);
//    }
//
//    private void setupRecyclerView() {
//        userAdapter=new UserSearchAdapter(filteredUsers,this);
//        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
//        rvSearchResults.setAdapter(userAdapter);
//    }
//
//    private void setupToolbar() {
//        setSupportActionBar(toolbar);
//        if(getSupportActionBar()!=null){
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setTitle("Create Group");
//        }
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//    }
//
//    private void initializeViews() {
//        etGroupName=findViewById(R.id.et_group_name);
//        etGroupDescription=findViewById(R.id.et_group_description);
//        etSearchUsers=findViewById(R.id.et_search_users);
//        tvSelectedCount=findViewById(R.id.tv_selected_count);
//        rvSearchResults=findViewById(R.id.rv_search_result);
//        btnCreateGroup=findViewById(R.id.btn_create_group);
//        toolbar=findViewById(R.id.toolbar);
//
//        allUsers=new ArrayList<>();
//        selectedUsers=new ArrayList<>();
//        filteredUsers=new ArrayList<>();
//
//        dbHelper=new DatabaseHelper_login(this);
//        searchHandler=new Handler();
//
//        updateSelectedCount();
//    }
//
//    private void updateSelectedCount() {
//        int count=selectedUsers.size();
//        if(count==0){
//            tvSelectedCount.setText("No users selected");
//            tvSelectedCount.setVisibility(View.GONE);
//        }else{
//            tvSelectedCount.setText(count+" user"+(count==1?"":"s")+" selected");
//            tvSelectedCount.setVisibility(View.VISIBLE);
//        }
//    }
//
//    @Override
//    public void onUserClick(User user, boolean isSelected) {
//        if(isSelected){
//            if(!selectedUsers.contains(user)){
//                selectedUsers.add(user);
//            }
//        }else{
//            selectedUsers.remove(user);
//        }
//        updateSelectedCount();
//        validateCreateButton();
//    }
//
//    private void debugCurrentUser() {
//        // Debug method to help identify the issue
//        android.util.Log.d("CreateGroup", "=== DEBUG USER SESSION ===");
//
//        // Check all possible SharedPreferences
//        android.content.SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
//        android.util.Log.d("CreateGroup", "user_prefs keys: " + userPrefs.getAll().keySet());
//
//        android.content.SharedPreferences defaultPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
//        android.util.Log.d("CreateGroup", "UserPrefs keys: " + defaultPrefs.getAll().keySet());
//
//        android.content.SharedPreferences packagePrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
//        android.util.Log.d("CreateGroup", "Package prefs keys: " + packagePrefs.getAll().keySet());
//
//        // Check Intent extras
//        Intent intent = getIntent();
//        if (intent != null && intent.getExtras() != null) {
//            android.util.Log.d("CreateGroup", "Intent extras keys: " + intent.getExtras().keySet());
//        }
//
//        android.util.Log.d("CreateGroup", "=== END DEBUG ===");
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if(searchHandler!=null){
//            searchHandler.removeCallbacksAndMessages(null);
//        }
//    }
//}

package com.example.bdapps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CreateGroup extends AppCompatActivity implements UserSearchAdapter.OnUserClickListener {

    TextInputEditText etGroupName, etGroupDescription, etSearchUsers;
    TextView tvSelectedCount;
    RecyclerView rvSearchResults;
    MaterialButton btnCreateGroup;
    MaterialToolbar toolbar;
    UserSearchAdapter userAdapter;
    List<User> allUsers;
    List<User> selectedUsers;
    List<User> filteredUsers;
    DatabaseHelper_login dbHelper;
    Handler searchHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadUsers();
    }

    private void initializeViews() {
                etGroupName=findViewById(R.id.et_group_name);
        etGroupDescription=findViewById(R.id.et_group_description);
        etSearchUsers=findViewById(R.id.et_search_users);
        tvSelectedCount=findViewById(R.id.tv_selected_count);
        rvSearchResults=findViewById(R.id.rv_search_result);
        btnCreateGroup=findViewById(R.id.btn_create_group);
//        toolbar=findViewById(R.id.toolbar);
        toolbar = findViewById(R.id.toolbar);

        // Initialize lists
        allUsers = new ArrayList<>();
        selectedUsers = new ArrayList<>();
        filteredUsers = new ArrayList<>();

        // Initialize database helpers
        dbHelper = new DatabaseHelper_login(this);

        // Initialize handler for search delay
        searchHandler = new Handler();

        updateSelectedCount();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create Group");
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        userAdapter = new UserSearchAdapter(filteredUsers, this);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(userAdapter);
    }

    private void setupListeners() {
        // Search functionality with debounce
        etSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacksAndMessages(null);
                searchHandler.postDelayed(() -> filterUsers(s.toString()), 300);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Create group button
        btnCreateGroup.setOnClickListener(v -> createGroup());

        // Group name validation
        etGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateCreateButton();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        // Load all users from database (excluding current user)
        allUsers = dbHelper.getAllUsers();

        // Remove current user from the list if needed
        String currentUsername = getCurrentUsername();
        if (currentUsername != null) {
            allUsers.removeIf(user -> user.getUsername().equals(currentUsername));
        }

        filteredUsers.clear();
        filteredUsers.addAll(allUsers);
        userAdapter.notifyDataSetChanged();
    }

    private String getCurrentUsername() {
        // Try to get current username from multiple sources

        // 1. Try from SharedPreferences
        String username = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("current_username", null);

        if (username != null) {
            return username;
        }

        // 2. Try from Intent extras
        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("current_username");
            if (username != null) {
                return username;
            }

            username = intent.getStringExtra("username");
            if (username != null) {
                return username;
            }
        }

        // 3. Try other common SharedPreferences keys
        username = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("username", null);

        if (username != null) {
            return username;
        }

        // 4. Try default SharedPreferences
        username = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE)
                .getString("logged_in_user", null);

        return username;
    }

    private void filterUsers(String query) {
        filteredUsers.clear();

        if (query.trim().isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (User user : allUsers) {
                if (user.getUsername().toLowerCase().contains(lowerQuery) ||
                        user.getUsername().toLowerCase().contains(lowerQuery) ||
                        user.getEmail().toLowerCase().contains(lowerQuery)) {
                    filteredUsers.add(user);
                }
            }
        }

        userAdapter.notifyDataSetChanged();
    }

    @Override
    public void onUserClick(User user, boolean isSelected) {
        if (isSelected) {
            if (!selectedUsers.contains(user)) {
                selectedUsers.add(user);
            }
        } else {
            selectedUsers.remove(user);
        }

        updateSelectedCount();
        validateCreateButton();
    }

    private void updateSelectedCount() {
        int count = selectedUsers.size();
        if (count == 0) {
            tvSelectedCount.setText("No users selected");
            tvSelectedCount.setVisibility(View.GONE);
        } else {
            tvSelectedCount.setText(count + " user" + (count == 1 ? "" : "s") + " selected");
            tvSelectedCount.setVisibility(View.VISIBLE);
        }
    }

    private void validateCreateButton() {
        String groupName = etGroupName.getText().toString().trim();
        boolean isValid = !groupName.isEmpty() && selectedUsers.size() > 0;

        btnCreateGroup.setEnabled(isValid);
        btnCreateGroup.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private void createGroup() {
        String groupName = etGroupName.getText().toString().trim();
        String groupDescription = etGroupDescription.getText().toString().trim();
        String currentUsername = getCurrentUsername();

        if (groupName.isEmpty()) {
            etGroupName.setError("Group name is required");
            etGroupName.requestFocus();
            return;
        }

        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "Please select at least one user", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUsername == null) {
            // Debug: Show what we're looking for
            debugCurrentUser();
            Toast.makeText(this, "User session expired. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent multiple clicks
        btnCreateGroup.setEnabled(false);
        btnCreateGroup.setText("Creating...");

        try {
            // Get current user info
            User currentUser = dbHelper.getUserByUsername(currentUsername);
            if (currentUser == null) {
                Toast.makeText(this, "User not found. Please login again.", Toast.LENGTH_SHORT).show();
                return;
            }

            int createdBy = Integer.parseInt(currentUser.getUserId());

            // Create list of member IDs
            List<Integer> memberIds = new ArrayList<>();
            for (User user : selectedUsers) {
                memberIds.add(Integer.parseInt(user.getUserId()));
            }

            // Create group in database
            long groupId = dbHelper.createGroup(groupName, groupDescription, createdBy, memberIds);

            if (groupId != -1) {
                Toast.makeText(this, "Group created successfully!", Toast.LENGTH_SHORT).show();

                // Return to previous activity with success result
                Intent resultIntent = new Intent();
                resultIntent.putExtra("group_created", true);
                resultIntent.putExtra("group_name", groupName);
                resultIntent.putExtra("group_id", groupId);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Failed to create group", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error creating group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            // Re-enable button
            btnCreateGroup.setEnabled(true);
            btnCreateGroup.setText("Create Group");
            validateCreateButton();
        }
    }

    private void debugCurrentUser() {
        // Debug method to help identify the issue
        android.util.Log.d("CreateGroup", "=== DEBUG USER SESSION ===");

        // Check all possible SharedPreferences
        android.content.SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        android.util.Log.d("CreateGroup", "user_prefs keys: " + userPrefs.getAll().keySet());

        android.content.SharedPreferences defaultPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        android.util.Log.d("CreateGroup", "UserPrefs keys: " + defaultPrefs.getAll().keySet());

        android.content.SharedPreferences packagePrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
        android.util.Log.d("CreateGroup", "Package prefs keys: " + packagePrefs.getAll().keySet());

        // Check Intent extras
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            android.util.Log.d("CreateGroup", "Intent extras keys: " + intent.getExtras().keySet());
        }

        android.util.Log.d("CreateGroup", "=== END DEBUG ===");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchHandler != null) {
            searchHandler.removeCallbacksAndMessages(null);
        }
    }
}