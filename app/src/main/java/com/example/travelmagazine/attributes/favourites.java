package com.example.travelmagazine.attributes;

public class favourites {
    private String id_user;
    private String id_excursion;
    public favourites(){}
    public favourites(String id_user, String id_excursion){
        this.id_user = id_user;
        this.id_excursion = id_excursion;
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
}
