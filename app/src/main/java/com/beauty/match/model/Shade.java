package com.beauty.match.model;

public class Shade {
    private int id;
    private int brandId;
    private String brandName;
    private String productLine;
    private String shadeCode;
    private String shadeName;
    private int colorRgb;
    private String undertone;
    private String skinType;
    private String coverage;
    private String finish;
    private String productType;
    private String ingredients;
    private boolean hasSpf;

    public Shade(int id, int brandId, String brandName, String productLine,
                 String shadeCode, String shadeName, int colorRgb,
                 String undertone, String skinType, String coverage,
                 String finish, String productType, String ingredients, boolean hasSpf) {
        this.id = id;
        this.brandId = brandId;
        this.brandName = brandName;
        this.productLine = productLine;
        this.shadeCode = shadeCode;
        this.shadeName = shadeName;
        this.colorRgb = colorRgb;
        this.undertone = undertone;
        this.skinType = skinType;
        this.coverage = coverage;
        this.finish = finish;
        this.productType = productType;
        this.ingredients = ingredients;
        this.hasSpf = hasSpf;
    }

    public int getId() { return id; }
    public int getBrandId() { return brandId; }
    public String getBrandName() { return brandName; }
    public String getProductLine() { return productLine; }
    public String getShadeCode() { return shadeCode; }
    public String getShadeName() { return shadeName; }
    public int getColorRgb() { return colorRgb; }
    public String getUndertone() { return undertone; }
    public String getSkinType() { return skinType; }
    public String getCoverage() { return coverage; }
    public String getFinish() { return finish; }
    public String getProductType() { return productType; }
    public String getIngredients() { return ingredients; }
    public boolean hasSpf() { return hasSpf; }
}
