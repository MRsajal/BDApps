package com.example.bdapps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.OnUserLeaveHintProvider;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {
    private List<User> userList;
    private List<User> selectedUsers;
    private OnUserClickListener listener;

    public interface OnUserClickListener{
        void onUserClick(User user,boolean isSelected);
    }

    public UserSearchAdapter(List<User> users, OnUserClickListener listener) {
        this.userList = users;
        this.listener = listener;
        this.selectedUsers = new ArrayList<>();
    }
    @NonNull
    @Override
    public UserSearchAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_search,parent,false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserSearchAdapter.UserViewHolder holder, int position) {
        User user=userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
    public void updateUsers(List<User>users){
        this.userList.clear();
        this.userList.addAll(users);
        notifyDataSetChanged();
    }
    public List<User> getSelectedUsers(){
        return new ArrayList<>(selectedUsers);
    }
    public void clearSelection(){
        selectedUsers.clear();
        notifyDataSetChanged();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUsername;
        private TextView tvDisplayName;
        private CheckBox cbSelect;
        private CardView cardView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername=itemView.findViewById(R.id.tv_username);
            tvDisplayName=itemView.findViewById(R.id.tv_display_name);
            cbSelect=itemView.findViewById(R.id.cb_select);
            cardView=itemView.findViewById(R.id.card_view);
        }
        public void bind(User user){
            tvUsername.setText("@"+user.getUsername());
            tvDisplayName.setText(user.getDisplayName());

           boolean isSelected=selectedUsers.contains(user);
           cbSelect.setChecked(isSelected);
           cardView.setCardBackgroundColor(isSelected?
                   ContextCompat.getColor(itemView.getContext(),R.color.top_nov):
                   ContextCompat.getColor(itemView.getContext(),R.color.background_color));

           itemView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   toggleSelection(user);
               }
           });
           cbSelect.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   toggleSelection(user);
               }
           });
        }
        private void toggleSelection(User user) {
            boolean isCurrentlySelected=selectedUsers.contains(user);
            if(isCurrentlySelected){
                selectedUsers.remove(user);
            }else{
                selectedUsers.add(user);
            }
            cbSelect.setChecked(!isCurrentlySelected);
            cardView.setCardBackgroundColor(!isCurrentlySelected?
                    ContextCompat.getColor(itemView.getContext(),R.color.top_nov):
                    ContextCompat.getColor(itemView.getContext(),R.color.background_color));

            if (listener != null) {
                listener.onUserClick(user, !isCurrentlySelected);
            }
        }
    }


}
