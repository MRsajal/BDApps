package com.example.bdapps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {
    private List<Group> groups;
    private Context context;
    public GroupsAdapter(Context context){
        this.context=context;
        this.groups=new ArrayList<>();
    }
    public void setGroups(List<Group> groups){
        this.groups=groups;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupsAdapter.GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group,parent,false);
       return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupsAdapter.GroupViewHolder holder, int position) {
        Group group=groups.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {
        private TextView tvGroupName;
        private TextView tvGroupDescription;
        private TextView tvCreatedAt;
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName=itemView.findViewById(R.id.tvGroupName);
            tvGroupDescription=itemView.findViewById(R.id.tvGroupDescription);
            tvCreatedAt=itemView.findViewById(R.id.tvCreatedAt);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position=getAdapterPosition();
                    if(position!=RecyclerView.NO_POSITION){
                        Group clickedGroup=groups.get(position);
                        String message="Clicked on: "+clickedGroup.getGroupName();
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        public void bind(Group group){
            tvGroupName.setText(group.getGroupName());
            String description=group.getDescription();
            if(description!=null && !description.trim().isEmpty()){
                tvGroupDescription.setText(description);
                tvGroupDescription.setVisibility(View.VISIBLE);
            }else {
                tvGroupDescription.setVisibility(View.GONE);
            }

            if(group.getCreatedAt()>0){
                SimpleDateFormat sdf=new SimpleDateFormat("MMM dd, YYYY", Locale.getDefault());
                String createdDate=sdf.format(new Date(group.getCreatedAt()));
                tvCreatedAt.setText("Created: "+createdDate);
                tvCreatedAt.setVisibility(View.VISIBLE);
            }
            else{
                tvCreatedAt.setVisibility(View.GONE);
            }
        }
    }
}
