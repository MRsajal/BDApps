package com.example.bdapps.ProfileComponent.Education;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdapps.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class EducationAdapterEdit extends RecyclerView.Adapter<EducationAdapterEdit.ViewHolder> {

    private List<Education> educationList;
    private Context context;
    private OnEducationItemClickListener listener;

    // Interface for handling item clicks
    public interface OnEducationItemClickListener {
        void onEditClick(Education education, int position);
        void onItemClick(Education education, int position);
    }

    public EducationAdapterEdit(Context context) {
        this.context = context;
        this.educationList = new ArrayList<>();
    }

    public EducationAdapterEdit(Context context, List<Education> educationList) {
        this.context = context;
        this.educationList = educationList != null ? educationList : new ArrayList<>();
    }

    public void setOnEducationItemClickListener(OnEducationItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_institution_details_edit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Education education = educationList.get(position);
        holder.bind(education, position);
    }

    @Override
    public int getItemCount() {
        return educationList.size();
    }

    // Method to update the entire list
    public void updateEducationList(List<Education> newEducationList) {
        this.educationList.clear();
        if (newEducationList != null) {
            this.educationList.addAll(newEducationList);
        }
        notifyDataSetChanged();
    }

    // Method to add a single education item
    public void addEducation(Education education) {
        if (education != null) {
            educationList.add(education);
            notifyItemInserted(educationList.size() - 1);
        }
    }

    // Method to update a specific education item
    public void updateEducation(int position, Education education) {
        if (position >= 0 && position < educationList.size() && education != null) {
            educationList.set(position, education);
            notifyItemChanged(position);
        }
    }

    // Method to remove an education item
    public void removeEducation(int position) {
        if (position >= 0 && position < educationList.size()) {
            educationList.remove(position);
            notifyItemRemoved(position);
        }
    }

    // Method to get education at specific position
    public Education getEducationAt(int position) {
        if (position >= 0 && position < educationList.size()) {
            return educationList.get(position);
        }
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ShapeableImageView institutionLogo;
        private TextView institutionName;
        private TextView degreeProgram;
        private TextView duration;
        private ImageButton btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initializeViews();
            setupClickListeners();
        }

        private void initializeViews() {
            institutionLogo = itemView.findViewById(R.id.institution_logo_edit);
            institutionName = itemView.findViewById(R.id.institution_name_edit);
            degreeProgram = itemView.findViewById(R.id.degree_program_edit);
            duration = itemView.findViewById(R.id.duration_edit);
            btnEdit = itemView.findViewById(R.id.btnEditEducationEdit1);
        }

        private void setupClickListeners() {
            // Edit button click listener
            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditClick(educationList.get(position), position);
                }
            });

            // Item click listener
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(educationList.get(position), position);
                }
            });
        }

        public void bind(Education education, int position) {
            if (education == null) return;

            // Set institution name
            if (education.getInstitution() != null) {
                institutionName.setText(education.getInstitution().getName());
            } else {
                institutionName.setText("Unknown Institution");
            }

            // Set degree program - combine degree and major
            String degreeText = formatDegreeProgram(education);
            degreeProgram.setText(degreeText);

            // Set duration
            String durationText = formatDuration(education);
            duration.setText(durationText);

            // Set institution logo (you might want to load this from URL or use default)
            // For now, using default logo

            // You can implement image loading logic here if you have institution logos
            // Example with Glide or Picasso:
            // if (education.getInstitution() != null && education.getInstitution().getLogo() != null) {
            //     Glide.with(context)
            //         .load(education.getInstitution().getLogo())
            //         .placeholder(R.drawable.ruetlogo)
            //         .error(R.drawable.ruetlogo)
            //         .into(institutionLogo);
            // }
        }

        private String formatDegreeProgram(Education education) {
            StringBuilder sb = new StringBuilder();

            // Use degree_display if available, otherwise use degree
            String degree = education.getDegree_display();
            if (degree == null || degree.trim().isEmpty()) {
                degree = education.getDegree();
            }

            if (degree != null && !degree.trim().isEmpty()) {
                sb.append(degree);
            }

            // Add major if available and different from degree
            String major = education.getMajor();
            if (major != null && !major.trim().isEmpty()) {
                if (sb.length() > 0 && !major.equals(degree)) {
                    sb.append(" in ").append(major);
                } else if (sb.length() == 0) {
                    sb.append(major);
                }
            }

            // Fallback if nothing is available
            if (sb.length() == 0) {
                sb.append("Not specified");
            }

            return sb.toString();
        }

        private String formatDuration(Education education) {
            String startDate = education.getStart_date();
            String endDate = education.getEnd_date();
            boolean isCurrent = education.isIs_current();

            StringBuilder duration = new StringBuilder();

            // Format start date (extract year)
            if (startDate != null && !startDate.trim().isEmpty()) {
                String startYear = extractYear(startDate);
                duration.append(startYear);
            } else {
                duration.append("Unknown");
            }

            duration.append(" - ");

            // Format end date or "Present"
            if (isCurrent) {
                duration.append("Present");
            } else if (endDate != null && !endDate.trim().isEmpty()) {
                String endYear = extractYear(endDate);
                duration.append(endYear);
            } else {
                duration.append("Present");
            }

            return duration.toString();
        }

        private String extractYear(String dateString) {
            if (dateString == null || dateString.trim().isEmpty()) {
                return "Unknown";
            }

            // Assuming date format is "YYYY-MM-DD"
            String[] parts = dateString.split("-");
            if (parts.length > 0) {
                return parts[0]; // Return the year
            }

            return dateString; // Return as is if can't parse
        }
    }
}