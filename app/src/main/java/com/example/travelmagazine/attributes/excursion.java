package com.example.travelmagazine.attributes;

public class excursion {
    private String id;
    private String name;
    private double estimation;
    private String photo;
    private String description;
    private boolean approved;
    private String city;

    public excursion() {}

    public excursion(String name, double estimation, String photo, String description, boolean approved) {
        this.name = name;
        this.estimation = estimation;
        this.photo = photo;
        this.description = description;
        this.approved = approved;
    }

    public excursion(String name, double estimation, String photo, String description, boolean approved, String city) {
        this.name = name;
        this.estimation = estimation;
        this.photo = photo;
        this.description = description;
        this.approved = approved;
        this.city = city;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getEstimation() { return estimation; }
    public void setEstimation(double estimation) { this.estimation = estimation; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}