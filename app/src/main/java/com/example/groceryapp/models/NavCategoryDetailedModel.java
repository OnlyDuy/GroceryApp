package com.example.groceryapp.models;

import java.io.Serializable;

public class NavCategoryDetailedModel implements Serializable {
    String name;
    String type;
    String img_url;
    String price;
    String description;
    String rating;

    public NavCategoryDetailedModel() {
    }

    public NavCategoryDetailedModel(String name, String type, String img_url, String price, String description, String rating) {
        this.name = name;
        this.type = type;
        this.img_url = img_url;
        this.price = price;
        this.description = description;
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
