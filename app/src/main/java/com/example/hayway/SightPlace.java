package com.example.hayway;

public class SightPlace {
    public String name;
    public String description;
    public String photoUrl;
    public double latitude;
    public double longitude;
    public boolean visited;  // new field to track visits

    // Default constructor required for Firebase
    public SightPlace() { }

    // Updated constructor including 'visited'
    public SightPlace(String name,
                      String description,
                      String photoUrl,
                      double latitude,
                      double longitude,
                      boolean visited) {
        this.name = name;
        this.description = description;
        this.photoUrl = photoUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.visited = visited;
    }

    // Getter & setter for the new field
    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}