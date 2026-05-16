package com.example.travelmagazine.attributes;

public class appeal {
    private String id;


    private String id_user;
    private String name;
    private String photo;
    private String description;
    public appeal(){}
    public appeal(String id_user, String name,String photo, String description){
        this.id_user=id_user;
        this.name=name;
        this.photo=photo;
        this.description=description;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
