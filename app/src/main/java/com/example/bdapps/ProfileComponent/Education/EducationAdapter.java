package com.example.bdapps.ProfileComponent.Education;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bdapps.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EducationAdapter extends RecyclerView.Adapter<EducationAdapter.ViewHolder> {

    private List<Education> educationList;
    private OnEducationItemClickListener listener;

    // Interface for handling click events
    public interface OnEducationItemClickListener {
        void onEditClick(Education education, int position);
    }

    // Constructor
    public EducationAdapter() {
        this.educationList = new ArrayList<>();
    }

    public EducationAdapter(List<Education> educationList) {
        this.educationList = educationList != null ? educationList : new ArrayList<>();
    }

    // Set click listener
    public void setOnEducationItemClickListener(OnEducationItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EducationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_instituition_details_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EducationAdapter.ViewHolder holder, int position) {
        Education education = educationList.get(position);
        holder.bind(education, position);
    }

    @Override
    public int getItemCount() {
        return educationList.size();
    }

    // Update the education list
    public void updateEducationList(List<Education> newEducationList) {
        this.educationList.clear();
        if (newEducationList != null) {
            this.educationList.addAll(newEducationList);
        }
        notifyDataSetChanged();
    }

    // Add single education item
    public void addEducation(Education education) {
        educationList.add(education);
        notifyItemInserted(educationList.size() - 1);
    }

    // Remove education item
    public void removeEducation(int position) {
        if (position >= 0 && position < educationList.size()) {
            educationList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ShapeableImageView institutionLogo;
        private TextView institutionName;
        private TextView degreeProgram;
        private TextView duration;
//        private ImageButton editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views based on your XML layout
//            institutionLogo = itemView.findViewById(R.id.institution_logo); // You may need to add this ID
            institutionName = itemView.findViewById(R.id.institution_name); // You may need to add this ID
            degreeProgram = itemView.findViewById(R.id.degree_program); // You may need to add this ID
            duration = itemView.findViewById(R.id.duration); // You may need to add this ID
//            editButton = itemView.findViewById(R.id.btnEditEducation1);

            // Set edit button click listener
//            editButton.setOnClickListener(v -> {
//                int position = getAdapterPosition();
//                if (position != RecyclerView.NO_POSITION && listener != null) {
//                    listener.onEditClick(educationList.get(position), position);
//                }
//            });
        }

        public void bind(Education education, int position) {
            if (education == null) return;

            // Set institution name
            if (education.getInstitution() != null && education.getInstitution().getName() != null) {
                institutionName.setText(education.getInstitution().getName());
            } else {
                institutionName.setText("Unknown Institution");
            }

            // Set degree program (combine degree_display and major)
            StringBuilder degreeText = new StringBuilder();
            if (education.getDegree_display() != null && !education.getDegree_display().isEmpty()) {
                degreeText.append(education.getDegree_display());
            } else if (education.getDegree() != null && !education.getDegree().isEmpty()) {
                degreeText.append(education.getDegree());
            }

            if (education.getMajor() != null && !education.getMajor().isEmpty()) {
                if (degreeText.length() > 0) {
                    degreeText.append(" in ");
                }
                degreeText.append(education.getMajor());
            }

            degreeProgram.setText(degreeText.length() > 0 ? degreeText.toString() : "Degree Information Not Available");

            // Set duration
            String durationText = formatDuration(education.getStart_date(), education.getEnd_date(), education.isIs_current());
            duration.setText(durationText);

            // Set institution logo (you can add logic to load specific logos based on institution)
            // For now, using a default logo
//            institutionLogo.setImageResource(R.drawable.ruetlogo); // You might want to make this dynamic
        }

        private String formatDuration(String startDate, String endDate, boolean isCurrent) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

                String formattedStartDate = "Unknown";
                String formattedEndDate = "Unknown";

                if (startDate != null && !startDate.isEmpty()) {
                    try {
                        Date start = inputFormat.parse(startDate);
                        if (start != null) {
                            formattedStartDate = outputFormat.format(start);
                        }
                    } catch (ParseException e) {
                        formattedStartDate = startDate.substring(0, Math.min(4, startDate.length()));
                    }
                }

                if (isCurrent) {
                    formattedEndDate = "Present";
                } else if (endDate != null && !endDate.isEmpty()) {
                    try {
                        Date end = inputFormat.parse(endDate);
                        if (end != null) {
                            formattedEndDate = outputFormat.format(end);
                        }
                    } catch (ParseException e) {
                        formattedEndDate = endDate.substring(0, Math.min(4, endDate.length()));
                    }
                }

                return formattedStartDate + " - " + formattedEndDate;

            } catch (Exception e) {
                return "Duration Not Available";
            }
        }
    }
}