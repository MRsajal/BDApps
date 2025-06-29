package com.example.bdapps.ProfileComponent.Education;

public class Education {
    private int id;
    private Institution institution;
    private String major;
    private String degree;
    private String degree_display;
    private String series;
    private String start_date;
    private String end_date;
    private boolean is_current;
    private String description;

    // Constructor
    public Education() {}

    public Education(int id, Institution institution, String major, String degree,
                     String degree_display, String series, String start_date,
                     String end_date, boolean is_current, String description) {
        this.id = id;
        this.institution = institution;
        this.major = major;
        this.degree = degree;
        this.degree_display = degree_display;
        this.series = series;
        this.start_date = start_date;
        this.end_date = end_date;
        this.is_current = is_current;
        this.description = description;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getDegree_display() { return degree_display; }
    public void setDegree_display(String degree_display) { this.degree_display = degree_display; }

    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series; }

    public String getStart_date() { return start_date; }
    public void setStart_date(String start_date) { this.start_date = start_date; }

    public String getEnd_date() { return end_date; }
    public void setEnd_date(String end_date) { this.end_date = end_date; }

    public boolean isIs_current() { return is_current; }
    public void setIs_current(boolean is_current) { this.is_current = is_current; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Nested Institution class
    public static class Institution {
        private int id;
        private String name;
        private String location;
        private String website;
        private String students;

        // Constructor
        public Institution() {}

        public Institution(int id, String name, String location, String website, String students) {
            this.id = id;
            this.name = name;
            this.location = location;
            this.website = website;
            this.students = students;
        }

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getWebsite() { return website; }
        public void setWebsite(String website) { this.website = website; }

        public String getStudents() { return students; }
        public void setStudents(String students) { this.students = students; }
    }
}