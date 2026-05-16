package com.example.travelmagazine.attributes;

public class feedback {
    private String id;
    private String id_user;
    private String id_excursion;
    private String text;
    private double estimation;
    private String datatime;
    private String photo;
    private boolean approved;
    public feedback(){}
    public feedback(String id_user, String id_excursion, String text, double estimation, String datatime, String photo){
        this.id_user=id_user;
        this.id_excursion=id_excursion;
        this.text=text;
        this.estimation=estimation;
        this.datatime=datatime;
        this.photo=photo;
        this.approved = false;
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

    public String getId_excursion() {
        return id_excursion;
    }

    public void setId_excursion(String id_excursion) {
        this.id_excursion = id_excursion;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getEstimation() {
        return estimation;
    }

    public void setEstimation(double estimation) {
        this.estimation = estimation;
    }

    public String getDatatime() {
        return datatime;
    }

    public void setDatatime(String datatime) {
        this.datatime = datatime;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
}
