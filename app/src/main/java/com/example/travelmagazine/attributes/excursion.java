package com.example.travelmagazine.attributes;

public class excursion {
    private String id;
    private String name;
    private double estimation;
    private String photo;
    private String description;
    private boolean isApproved;

    public excursion() {}

    // Конструктор с 5 параметрами
    public excursion(String name, double estimation, String photo, String description, boolean isApproved) {
        this.name = name;
        this.estimation = estimation;
        this.photo = photo;
        this.description = description;
        this.isApproved = isApproved;
    }

    // Конструктор с 4 параметрами (для AdminActivity)
    public excursion(String name, double estimation, String photo, String description) {
        this.name = name;
        this.estimation = estimation;
        this.photo = photo;
        this.description = description;
        this.isApproved = true;  // По умолчанию одобрено
    }

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getEstimation() {
        return estimation;
    }

    public void setEstimation(double estimation) {
        this.estimation = estimation;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }
}