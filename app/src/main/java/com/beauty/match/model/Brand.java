package com.beauty.match.model;

public class Brand {
    private int id;
    private String name;
    private String country;
    private String category; // "luxury", "mass_market", "professional"
    private String website;

    public Brand(int id, String name, String country, String category, String website) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.category = category;
        this.website = website;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCountry() { return country; }
    public String getCategory() { return category; }
    public String getWebsite() { return website; }
}
