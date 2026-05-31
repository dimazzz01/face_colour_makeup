package com.beauty.match.model;

public class Product {
    private int id;
    private int brandId;
    private String name;
    private String type;        // "foundation", "powder", "concealer"
    private String description;
    private double price;
    private String currency;
    private String ingredients;
    private boolean hasSpf;
    private int spfValue;

    public Product(int id, int brandId, String name, String type,
                   String description, double price, String currency,
                   String ingredients, boolean hasSpf, int spfValue) {
        this.id = id;
        this.brandId = brandId;
        this.name = name;
        this.type = type;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.ingredients = ingredients;
        this.hasSpf = hasSpf;
        this.spfValue = spfValue;
    }

    public int getId() { return id; }
    public int getBrandId() { return brandId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCurrency() { return currency; }
    public String getIngredients() { return ingredients; }
    public boolean hasSpf() { return hasSpf; }
    public int getSpfValue() { return spfValue; }
}
