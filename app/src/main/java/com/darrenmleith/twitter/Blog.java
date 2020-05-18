package com.darrenmleith.twitter;

public class Blog {

    //make sure these match what has been assigned in Firebase database
    private String description, imageURL, title;

    public Blog() {}

    public Blog(String description, String imageURL, String title) {
        this.description = description;
        this.imageURL = imageURL;
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
