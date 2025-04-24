package com.example.hayway;

public class SightPlace {
    public String name;
    public String description;
    public String photoUrl;
    public double latitude;
    public double longitude;

    public SightPlace() { }

    public SightPlace(String name, String description, String photoUrl, double latitude, double longitude) {
        this.name = name;
        this.description = description;
        this.photoUrl = photoUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

