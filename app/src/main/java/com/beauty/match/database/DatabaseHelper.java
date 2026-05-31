package com.beauty.match.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.beauty.match.model.Brand;
import com.beauty.match.model.Shade;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "beautymatch.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_BRANDS = "brands";
    private static final String COL_BRAND_ID = "id";
    private static final String COL_BRAND_NAME = "name";
    private static final String COL_BRAND_COUNTRY = "country";
    private static final String COL_BRAND_CATEGORY = "category";
    private static final String COL_BRAND_WEBSITE = "website";

    private static final String TABLE_SHADES = "shades";
    private static final String COL_SHADE_ID = "id";
    private static final String COL_SHADE_BRAND_ID = "brand_id";
    private static final String COL_SHADE_BRAND_NAME = "brand_name";
    private static final String COL_SHADE_PRODUCT_LINE = "product_line";
    private static final String COL_SHADE_CODE = "shade_code";
    private static final String COL_SHADE_NAME = "shade_name";
    private static final String COL_SHADE_COLOR = "color_rgb";
    private static final String COL_SHADE_UNDERTONE = "undertone";
    private static final String COL_SHADE_SKIN_TYPE = "skin_type";
    private static final String COL_SHADE_COVERAGE = "coverage";
    private static final String COL_SHADE_FINISH = "finish";
    private static final String COL_SHADE_PRODUCT_TYPE = "product_type";
    private static final String COL_SHADE_INGREDIENTS = "ingredients";
    private static final String COL_SHADE_HAS_SPF = "has_spf";

    private static final String TABLE_CROSSCODES = "crosscodes";
    private static final String COL_CC_ID = "id";
    private static final String COL_CC_BRAND1_ID = "brand1_id";
    private static final String COL_CC_SHADE1_CODE = "shade1_code";
    private static final String COL_CC_BRAND2_ID = "brand2_id";
    private static final String COL_CC_SHADE2_CODE = "shade2_code";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createBrands = "CREATE TABLE " + TABLE_BRANDS + " (" +
                COL_BRAND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BRAND_NAME + " TEXT NOT NULL, " +
                COL_BRAND_COUNTRY + " TEXT, " +
                COL_BRAND_CATEGORY + " TEXT, " +
                COL_BRAND_WEBSITE + " TEXT)";
        db.execSQL(createBrands);

        String createShades = "CREATE TABLE " + TABLE_SHADES + " (" +
                COL_SHADE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SHADE_BRAND_ID + " INTEGER, " +
                COL_SHADE_BRAND_NAME + " TEXT NOT NULL, " +
                COL_SHADE_PRODUCT_LINE + " TEXT, " +
                COL_SHADE_CODE + " TEXT NOT NULL, " +
                COL_SHADE_NAME + " TEXT, " +
                COL_SHADE_COLOR + " INTEGER, " +
                COL_SHADE_UNDERTONE + " TEXT, " +
                COL_SHADE_SKIN_TYPE + " TEXT, " +
                COL_SHADE_COVERAGE + " TEXT, " +
                COL_SHADE_FINISH + " TEXT, " +
                COL_SHADE_PRODUCT_TYPE + " TEXT, " +
                COL_SHADE_INGREDIENTS + " TEXT, " +
                COL_SHADE_HAS_SPF + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + COL_SHADE_BRAND_ID + ") REFERENCES " + TABLE_BRANDS + "(" + COL_BRAND_ID + "))";
        db.execSQL(createShades);

        String createCrosscodes = "CREATE TABLE " + TABLE_CROSSCODES + " (" +
                COL_CC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CC_BRAND1_ID + " INTEGER, " +
                COL_CC_SHADE1_CODE + " TEXT, " +
                COL_CC_BRAND2_ID + " INTEGER, " +
                COL_CC_SHADE2_CODE + " TEXT)";
        db.execSQL(createCrosscodes);

        db.execSQL("CREATE INDEX idx_shades_brand ON " + TABLE_SHADES + "(" + COL_SHADE_BRAND_ID + ")");
        db.execSQL("CREATE INDEX idx_shades_color ON " + TABLE_SHADES + "(" + COL_SHADE_COLOR + ")");
        db.execSQL("CREATE INDEX idx_shades_undertone ON " + TABLE_SHADES + "(" + COL_SHADE_UNDERTONE + ")");
        db.execSQL("CREATE INDEX idx_crosscodes ON " + TABLE_CROSSCODES + "(" + COL_CC_BRAND1_ID + "," + COL_CC_SHADE1_CODE + ")");

        insertInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CROSSCODES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHADES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BRANDS);
        onCreate(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        insertBrand(db, 1, "MAC Cosmetics", "USA", "professional", "https://www.maccosmetics.com");
        insertBrand(db, 2, "L'Oréal Paris", "France", "mass_market", "https://www.lorealparisusa.com");
        insertBrand(db, 3, "Maybelline New York", "USA", "mass_market", "https://www.maybelline.com");
        insertBrand(db, 4, "Estée Lauder", "USA", "luxury", "https://www.esteelauder.com");
        insertBrand(db, 5, "Revlon", "USA", "mass_market", "https://www.revlon.com");
        insertBrand(db, 6, "NARS", "France", "luxury", "https://www.narscosmetics.com");

        // MAC Studio Fix Fluid SPF 15 (hasSpf=1)
        insertShade(db, 1, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NC10", "Porcelain", 0xFFF5E6D8, "neutral", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 2, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NC15", "Ivory", 0xFFF0DDD0, "neutral", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 3, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NC20", "Fair", 0xFFE8D5C4, "neutral", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 4, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NC25", "Light", 0xFFE0CDB8, "neutral", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 5, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NC30", "Light Medium", 0xFFD4C0A8, "neutral", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 6, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NC35", "Medium", 0xFFC9B498, "neutral", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 7, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NC40", "Medium Tan", 0xFFC0A888, "neutral", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 8, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NC42", "Tan", 0xFFB89C78, "neutral", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 9, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NC45", "Rich Tan", 0xFFAD9068, "neutral", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 10, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NC50", "Deep", 0xFFA08060, "neutral", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 11, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NC55", "Deep Dark", 0xFF8B7050, "neutral", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 12, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NW10", "Porcelain Rose", 0xFFF5E0E0, "cool", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 13, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NW13", "Ivory Rose", 0xFFF0D5D5, "cool", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 14, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NW15", "Fair Rose", 0xFFE8C8C8, "cool", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 15, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NW20", "Light Rose", 0xFFE0B8B8, "cool", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 16, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NW25", "Medium Rose", 0xFFD4A8A8, "cool", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 17, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NW30", "Rose Beige", 0xFFC89898, "cool", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 18, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NW35", "Golden Beige", 0xFFBE8888, "cool", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 19, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NW40", "Caramel", 0xFFB07878, "cool", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 20, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NW43", "Amber", 0xFFA86868, "cool", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 21, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NW45", "Hazelnut", 0xFF9C5858, "cool", "all", "medium", "natural", "foundation", "", 1);
        insertShade(db, 22, 1, "MAC Cosmetics", "Studio Fix Fluid SPF 15", "NW50", "Cocoa", 0xFF8B4848, "cool", "all", "medium", "natural", "foundation", "", 1);

        // MAC Powder (hasSpf=0)
        insertShade(db, 23, 1, "MAC Cosmetics", "Studio Fix Soft Focus", "Light", "Light", 0xFFF0DDD0, "neutral", "all", "light", "matte", "powder", "", 0);
        insertShade(db, 24, 1, "MAC Cosmetics", "Studio Fix Soft Focus", "Light Plus", "Light Plus", 0xFFE8D5C4, "neutral", "all", "light", "matte", "powder", "", 0);
        insertShade(db, 25, 1, "MAC Cosmetics", "Studio Fix Soft Focus", "Medium", "Medium", 0xFFD4C0A8, "neutral", "all", "medium", "matte", "powder", "", 0);
        insertShade(db, 26, 1, "MAC Cosmetics", "Studio Fix Soft Focus", "Medium Plus", "Medium Plus", 0xFFC9B498, "neutral", "all", "medium", "matte", "powder", "", 0);
        insertShade(db, 27, 1, "MAC Cosmetics", "Studio Fix Soft Focus", "Medium Dark", "Medium Dark", 0xFFC0A888, "neutral", "all", "medium", "matte", "powder", "", 0);
        insertShade(db, 28, 1, "MAC Cosmetics", "Studio Fix Soft Focus", "Dark", "Dark", 0xFFA08060, "neutral", "all", "medium", "matte", "powder", "", 0);
        insertShade(db, 29, 1, "MAC Cosmetics", "Studio Fix Soft Focus", "Deep Dark", "Deep Dark", 0xFF8B7050, "neutral", "all", "medium", "matte", "powder", "", 0);

        // L'Oréal True Match
        insertShade(db, 30, 2, "L'Oréal Paris", "True Match Super-Blendable", "W1", "Porcelain", 0xFFF5E6D8, "warm", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 31, 2, "L'Oréal Paris", "True Match Super-Blendable", "W2", "Soft Ivory", 0xFFF0DDD0, "warm", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 32, 2, "L'Oréal Paris", "True Match Super-Blendable", "W3", "Nude Beige", 0xFFE8D5C4, "warm", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 33, 2, "L'Oréal Paris", "True Match Super-Blendable", "W4", "Natural Beige", 0xFFE0CDB8, "warm", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 34, 2, "L'Oréal Paris", "True Match Super-Blendable", "W5", "Sand Beige", 0xFFD4C0A8, "warm", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 35, 2, "L'Oréal Paris", "True Match Super-Blendable", "W6", "Sun Beige", 0xFFC9B498, "warm", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 36, 2, "L'Oréal Paris", "True Match Super-Blendable", "W7", "Classic Tan", 0xFFC0A888, "warm", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 37, 2, "L'Oréal Paris", "True Match Super-Blendable", "W8", "Caramel Beige", 0xFFB89C78, "warm", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 38, 2, "L'Oréal Paris", "True Match Super-Blendable", "W9", "Deep Golden", 0xFFAD9068, "warm", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 39, 2, "L'Oréal Paris", "True Match Super-Blendable", "W10", "Deep Cool", 0xFFA08060, "warm", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 40, 2, "L'Oréal Paris", "True Match Super-Blendable", "C1", "Alabaster", 0xFFF5E0E0, "cool", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 41, 2, "L'Oréal Paris", "True Match Super-Blendable", "C2", "Rose Ivory", 0xFFF0D5D5, "cool", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 42, 2, "L'Oréal Paris", "True Match Super-Blendable", "C3", "Creamy Natural", 0xFFE8C8C8, "cool", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 43, 2, "L'Oréal Paris", "True Match Super-Blendable", "C4", "Shell Beige", 0xFFE0B8B8, "cool", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 44, 2, "L'Oréal Paris", "True Match Super-Blendable", "C5", "Rose Beige", 0xFFD4A8A8, "cool", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 45, 2, "L'Oréal Paris", "True Match Super-Blendable", "C6", "Soft Sable", 0xFFC89898, "cool", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 46, 2, "L'Oréal Paris", "True Match Super-Blendable", "C7", "Nut Brown", 0xFFBE8888, "cool", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 47, 2, "L'Oréal Paris", "True Match Super-Blendable", "C8", "Cocoa", 0xFFB07878, "cool", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 48, 2, "L'Oréal Paris", "True Match Super-Blendable", "C9", "Deep Cool", 0xFFA86868, "cool", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 49, 2, "L'Oréal Paris", "True Match Super-Blendable", "N1", "Soft Ivory", 0xFFF2E2D8, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 50, 2, "L'Oréal Paris", "True Match Super-Blendable", "N2", "Classic Ivory", 0xFFEBD8CC, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 51, 2, "L'Oréal Paris", "True Match Super-Blendable", "N3", "Natural Buff", 0xFFE2CEC0, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 52, 2, "L'Oréal Paris", "True Match Super-Blendable", "N4", "True Beige", 0xFFD8C4B4, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 53, 2, "L'Oréal Paris", "True Match Super-Blendable", "N5", "Soft Tan", 0xFFCEB8A8, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 54, 2, "L'Oréal Paris", "True Match Super-Blendable", "N6", "Honey Beige", 0xFFC4AC98, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 55, 2, "L'Oréal Paris", "True Match Super-Blendable", "N7", "Classic Tan", 0xFFB8A088, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 56, 2, "L'Oréal Paris", "True Match Super-Blendable", "N8", "Cappuccino", 0xFFAC9478, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 57, 2, "L'Oréal Paris", "True Match Super-Blendable", "N9", "Deep Neutral", 0xFFA08868, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 58, 2, "L'Oréal Paris", "True Match Mineral", "Fair", "Fair", 0xFFF0DDD0, "neutral", "all", "light", "matte", "powder", "", 0);
        insertShade(db, 59, 2, "L'Oréal Paris", "True Match Mineral", "Light", "Light", 0xFFE8D5C4, "neutral", "all", "light", "matte", "powder", "", 0);
        insertShade(db, 60, 2, "L'Oréal Paris", "True Match Mineral", "Medium", "Medium", 0xFFD4C0A8, "neutral", "all", "medium", "matte", "powder", "", 0);
        insertShade(db, 61, 2, "L'Oréal Paris", "True Match Mineral", "Natural Beige", "Natural Beige", 0xFFC9B498, "neutral", "all", "medium", "matte", "powder", "", 0);
        insertShade(db, 62, 2, "L'Oréal Paris", "True Match Mineral", "Tan", "Tan", 0xFFC0A888, "neutral", "all", "medium", "matte", "powder", "", 0);
        insertShade(db, 63, 2, "L'Oréal Paris", "True Match Mineral", "Deep", "Deep", 0xFFA08060, "neutral", "all", "medium", "matte", "powder", "", 0);

        // Maybelline Fit Me
        insertShade(db, 64, 3, "Maybelline New York", "Fit Me Matte + Poreless", "102", "Fair Porcelain", 0xFFF5E6D8, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 65, 3, "Maybelline New York", "Fit Me Matte + Poreless", "110", "Porcelain", 0xFFF0DDD0, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 66, 3, "Maybelline New York", "Fit Me Matte + Poreless", "112", "Natural Ivory", 0xFFE8D5C4, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 67, 3, "Maybelline New York", "Fit Me Matte + Poreless", "115", "Ivory", 0xFFE0CDB8, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 68, 3, "Maybelline New York", "Fit Me Matte + Poreless", "118", "Light Beige", 0xFFD4C0A8, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 69, 3, "Maybelline New York", "Fit Me Matte + Poreless", "120", "Classic Ivory", 0xFFC9B498, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 70, 3, "Maybelline New York", "Fit Me Matte + Poreless", "122", "Creamy Beige", 0xFFC0A888, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 71, 3, "Maybelline New York", "Fit Me Matte + Poreless", "124", "Soft Sand", 0xFFB89C78, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 72, 3, "Maybelline New York", "Fit Me Matte + Poreless", "128", "Warm Nude", 0xFFAD9068, "warm", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 73, 3, "Maybelline New York", "Fit Me Matte + Poreless", "130", "Buff Beige", 0xFFA08060, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 74, 3, "Maybelline New York", "Fit Me Matte + Poreless", "220", "Natural Beige", 0xFF9C7A58, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 75, 3, "Maybelline New York", "Fit Me Matte + Poreless", "228", "Soft Tan", 0xFF8B7050, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 76, 3, "Maybelline New York", "Fit Me Matte + Poreless", "230", "Natural Buff", 0xFF7A6040, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 77, 3, "Maybelline New York", "Fit Me Matte + Poreless", "235", "Pure Beige", 0xFF6E5438, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 78, 3, "Maybelline New York", "Fit Me Matte + Poreless", "238", "Rich Tan", 0xFF604830, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 79, 3, "Maybelline New York", "Fit Me Matte + Poreless", "240", "Golden Beige", 0xFF543C28, "warm", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 80, 3, "Maybelline New York", "Fit Me Matte + Poreless", "310", "Sun Beige", 0xFF483420, "warm", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 81, 3, "Maybelline New York", "Fit Me Matte + Poreless", "312", "Golden", 0xFF3C2C18, "warm", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 82, 3, "Maybelline New York", "Fit Me Matte + Poreless", "315", "Soft Honey", 0xFF302410, "warm", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 83, 3, "Maybelline New York", "Fit Me Matte + Poreless", "322", "Honey", 0xFF241C0C, "warm", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 84, 3, "Maybelline New York", "Fit Me Matte + Poreless", "330", "Toffee", 0xFF1C1408, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 85, 3, "Maybelline New York", "Fit Me Matte + Poreless", "332", "Coconut", 0xFF140E04, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 86, 3, "Maybelline New York", "Fit Me Matte + Poreless", "335", "Classic Tan", 0xFF0C0800, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 87, 3, "Maybelline New York", "Fit Me Matte + Poreless", "338", "Spicy Brown", 0xFF080400, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 88, 3, "Maybelline New York", "Fit Me Matte + Poreless", "340", "Cappuccino", 0xFF040200, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 89, 3, "Maybelline New York", "Fit Me Matte + Poreless", "355", "Coconut", 0xFF020100, "neutral", "oily", "medium", "matte", "foundation", "", 0);
        insertShade(db, 90, 3, "Maybelline New York", "Fit Me Powder", "Fair", "Fair", 0xFFF0DDD0, "neutral", "all", "light", "matte", "powder", "", 0);
        insertShade(db, 91, 3, "Maybelline New York", "Fit Me Powder", "Light", "Light", 0xFFE8D5C4, "neutral", "all", "light", "matte", "powder", "", 0);
        insertShade(db, 92, 3, "Maybelline New York", "Fit Me Powder", "Medium", "Medium", 0xFFD4C0A8, "neutral", "all", "medium", "matte", "powder", "", 0);
        insertShade(db, 93, 3, "Maybelline New York", "Fit Me Powder", "Natural Beige", "Natural Beige", 0xFFC9B498, "neutral", "all", "medium", "matte", "powder", "", 0);
        insertShade(db, 94, 3, "Maybelline New York", "Fit Me Powder", "Tan", "Tan", 0xFFC0A888, "neutral", "all", "medium", "matte", "powder", "", 0);
        insertShade(db, 95, 3, "Maybelline New York", "Fit Me Powder", "Deep", "Deep", 0xFFA08060, "neutral", "all", "medium", "matte", "powder", "", 0);

        // Estée Lauder Double Wear
        insertShade(db, 96, 4, "Estée Lauder", "Double Wear Stay-in-Place", "1C0", "Shell", 0xFFF5E0E0, "cool", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 97, 4, "Estée Lauder", "Double Wear Stay-in-Place", "1C1", "Cool Bone", 0xFFF0D5D5, "cool", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 98, 4, "Estée Lauder", "Double Wear Stay-in-Place", "1N0", "Porcelain", 0xFFF5E6D8, "neutral", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 99, 4, "Estée Lauder", "Double Wear Stay-in-Place", "1N1", "Ivory Nude", 0xFFF0DDD0, "neutral", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 100, 4, "Estée Lauder", "Double Wear Stay-in-Place", "1N2", "Ecru", 0xFFE8D5C4, "neutral", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 101, 4, "Estée Lauder", "Double Wear Stay-in-Place", "1W1", "Bone", 0xFFF2E2D8, "warm", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 102, 4, "Estée Lauder", "Double Wear Stay-in-Place", "1W2", "Sand", 0xFFEBD8CC, "warm", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 103, 4, "Estée Lauder", "Double Wear Stay-in-Place", "2C0", "Cool Vanilla", 0xFFE8C8C8, "cool", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 104, 4, "Estée Lauder", "Double Wear Stay-in-Place", "2C1", "Pure Beige", 0xFFE0B8B8, "cool", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 105, 4, "Estée Lauder", "Double Wear Stay-in-Place", "2N1", "Desert Beige", 0xFFE2CEC0, "neutral", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 106, 4, "Estée Lauder", "Double Wear Stay-in-Place", "2N2", "Buff", 0xFFD8C4B4, "neutral", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 107, 4, "Estée Lauder", "Double Wear Stay-in-Place", "2W0", "Warm Vanilla", 0xFFD4C0A8, "warm", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 108, 4, "Estée Lauder", "Double Wear Stay-in-Place", "2W1", "Dawn", 0xFFCEB8A8, "warm", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 109, 4, "Estée Lauder", "Double Wear Stay-in-Place", "2W2", "Rattan", 0xFFC4AC98, "warm", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 110, 4, "Estée Lauder", "Double Wear Stay-in-Place", "3C0", "Cool Cream", 0xFFD4A8A8, "cool", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 111, 4, "Estée Lauder", "Double Wear Stay-in-Place", "3C1", "Cool Bone", 0xFFC89898, "cool", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 112, 4, "Estée Lauder", "Double Wear Stay-in-Place", "3N1", "Ivory Beige", 0xFFB8A088, "neutral", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 113, 4, "Estée Lauder", "Double Wear Stay-in-Place", "3N2", "Wheat", 0xFFAC9478, "neutral", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 114, 4, "Estée Lauder", "Double Wear Stay-in-Place", "3W0", "Warm Crème", 0xFFB89C78, "warm", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 115, 4, "Estée Lauder", "Double Wear Stay-in-Place", "3W1", "Tawny", 0xFFAD9068, "warm", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 116, 4, "Estée Lauder", "Double Wear Stay-in-Place", "4C1", "Outdoor Beige", 0xFFBE8888, "cool", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 117, 4, "Estée Lauder", "Double Wear Stay-in-Place", "4C2", "Spiced Sand", 0xFFB07878, "cool", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 118, 4, "Estée Lauder", "Double Wear Stay-in-Place", "4C3", "Soft Tan", 0xFFA86868, "cool", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 119, 4, "Estée Lauder", "Double Wear Stay-in-Place", "4N1", "Shell Beige", 0xFFA08868, "neutral", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 120, 4, "Estée Lauder", "Double Wear Stay-in-Place", "4N2", "Spiced Sand", 0xFF9C7A58, "neutral", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 121, 4, "Estée Lauder", "Double Wear Stay-in-Place", "4W1", "Honey Bronze", 0xFF8B7050, "warm", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 122, 4, "Estée Lauder", "Double Wear Stay-in-Place", "4W2", "Toasty Toffee", 0xFF7A6040, "warm", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 123, 4, "Estée Lauder", "Double Wear Stay-in-Place", "4W3", "Cocoa", 0xFF6E5438, "warm", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 124, 4, "Estée Lauder", "Double Wear Stay-in-Place", "5C1", "Rich Cocoa", 0xFF604830, "cool", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 125, 4, "Estée Lauder", "Double Wear Stay-in-Place", "5C2", "Clove", 0xFF543C28, "cool", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 126, 4, "Estée Lauder", "Double Wear Stay-in-Place", "5N1", "Rich Ginger", 0xFF483420, "neutral", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 127, 4, "Estée Lauder", "Double Wear Stay-in-Place", "5N2", "Amber Honey", 0xFF3C2C18, "neutral", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 128, 4, "Estée Lauder", "Double Wear Stay-in-Place", "5W1", "Brandy", 0xFF302410, "warm", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 129, 4, "Estée Lauder", "Double Wear Stay-in-Place", "5W2", "Rich Caramel", 0xFF241C0C, "warm", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 130, 4, "Estée Lauder", "Double Wear Stay-in-Place", "6C1", "Rich Mahogany", 0xFF1C1408, "cool", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 131, 4, "Estée Lauder", "Double Wear Stay-in-Place", "6C2", "Rich Earth", 0xFF140E04, "cool", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 132, 4, "Estée Lauder", "Double Wear Stay-in-Place", "6N1", "Truffle", 0xFF0C0800, "neutral", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 133, 4, "Estée Lauder", "Double Wear Stay-in-Place", "6N2", "Toffee", 0xFF080400, "neutral", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 134, 4, "Estée Lauder", "Double Wear Stay-in-Place", "6W1", "Sandbar", 0xFF040200, "warm", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 135, 4, "Estée Lauder", "Double Wear Stay-in-Place", "6W2", "Cocoa", 0xFF020100, "warm", "all", "full", "matte", "foundation", "", 0);
        insertShade(db, 136, 4, "Estée Lauder", "Perfecting Loose Powder", "Light", "Light", 0xFFF0DDD0, "neutral", "all", "light", "natural", "powder", "", 0);
        insertShade(db, 137, 4, "Estée Lauder", "Perfecting Loose Powder", "Light Medium", "Light Medium", 0xFFE8D5C4, "neutral", "all", "light", "natural", "powder", "", 0);
        insertShade(db, 138, 4, "Estée Lauder", "Perfecting Loose Powder", "Medium", "Medium", 0xFFD4C0A8, "neutral", "all", "medium", "natural", "powder", "", 0);
        insertShade(db, 139, 4, "Estée Lauder", "Perfecting Loose Powder", "Medium Deep", "Medium Deep", 0xFFC9B498, "neutral", "all", "medium", "natural", "powder", "", 0);
        insertShade(db, 140, 4, "Estée Lauder", "Perfecting Loose Powder", "Deep", "Deep", 0xFFC0A888, "neutral", "all", "medium", "natural", "powder", "", 0);

        // Revlon ColorStay
        insertShade(db, 141, 5, "Revlon", "ColorStay 24H", "110", "Ivory", 0xFFF5E6D8, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 142, 5, "Revlon", "ColorStay 24H", "150", "Buff", 0xFFF0DDD0, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 143, 5, "Revlon", "ColorStay 24H", "180", "Sand Beige", 0xFFE8D5C4, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 144, 5, "Revlon", "ColorStay 24H", "200", "Nude", 0xFFE0CDB8, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 145, 5, "Revlon", "ColorStay 24H", "220", "Natural Beige", 0xFFD4C0A8, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 146, 5, "Revlon", "ColorStay 24H", "240", "Medium Beige", 0xFFC9B498, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 147, 5, "Revlon", "ColorStay 24H", "250", "Fresh Beige", 0xFFC0A888, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 148, 5, "Revlon", "ColorStay 24H", "300", "Golden Beige", 0xFFB89C78, "warm", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 149, 5, "Revlon", "ColorStay 24H", "310", "Warm Golden", 0xFFAD9068, "warm", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 150, 5, "Revlon", "ColorStay 24H", "320", "True Beige", 0xFFA08060, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 151, 5, "Revlon", "ColorStay 24H", "330", "Natural Tan", 0xFF9C7A58, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 152, 5, "Revlon", "ColorStay 24H", "340", "Early Tan", 0xFF8B7050, "warm", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 153, 5, "Revlon", "ColorStay 24H", "350", "Rich Tan", 0xFF7A6040, "warm", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 154, 5, "Revlon", "ColorStay 24H", "360", "Golden Caramel", 0xFF6E5438, "warm", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 155, 5, "Revlon", "ColorStay 24H", "370", "Toast", 0xFF604830, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 156, 5, "Revlon", "ColorStay 24H", "380", "Rich Maple", 0xFF543C28, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 157, 5, "Revlon", "ColorStay 24H", "390", "Mahogany", 0xFF483420, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 158, 5, "Revlon", "ColorStay 24H", "400", "Caramel", 0xFF3C2C18, "warm", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 159, 5, "Revlon", "ColorStay 24H", "410", "Cappuccino", 0xFF302410, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 160, 5, "Revlon", "ColorStay 24H", "420", "Mocha", 0xFF241C0C, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 161, 5, "Revlon", "ColorStay 24H", "430", "Hazelnut", 0xFF1C1408, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 162, 5, "Revlon", "ColorStay 24H", "440", "Cocoa", 0xFF140E04, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 163, 5, "Revlon", "ColorStay 24H", "450", "Espresso", 0xFF0C0800, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 164, 5, "Revlon", "ColorStay 24H", "460", "Rich Espresso", 0xFF080400, "neutral", "combination", "full", "natural", "foundation", "", 0);
        insertShade(db, 165, 5, "Revlon", "ColorStay Pressed Powder", "Fair", "Fair", 0xFFF0DDD0, "neutral", "all", "light", "matte", "powder", "", 0);
        insertShade(db, 166, 5, "Revlon", "ColorStay Pressed Powder", "Light", "Light", 0xFFE8D5C4, "neutral", "all", "light", "matte", "powder", "", 0);
        insertShade(db, 167, 5, "Revlon", "ColorStay Pressed Powder", "Light Medium", "Light Medium", 0xFFD4C0A8, "neutral", "all", "medium", "matte", "powder", "", 0);
        insertShade(db, 168, 5, "Revlon", "ColorStay Pressed Powder", "Medium", "Medium", 0xFFC9B498, "neutral", "all", "medium", "matte", "powder", "", 0);
        insertShade(db, 169, 5, "Revlon", "ColorStay Pressed Powder", "Medium Deep", "Medium Deep", 0xFFC0A888, "neutral", "all", "medium", "matte", "powder", "", 0);
        insertShade(db, 170, 5, "Revlon", "ColorStay Pressed Powder", "Deep", "Deep", 0xFFA08060, "neutral", "all", "medium", "matte", "powder", "", 0);

        // NARS
        insertShade(db, 171, 6, "NARS", "Light Reflecting Foundation", "Siberia", "Siberia", 0xFFF5E6D8, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 172, 6, "NARS", "Light Reflecting Foundation", "Mont Blanc", "Mont Blanc", 0xFFF0DDD0, "cool", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 173, 6, "NARS", "Light Reflecting Foundation", "Deauville", "Deauville", 0xFFE8D5C4, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 174, 6, "NARS", "Light Reflecting Foundation", "Fiji", "Fiji", 0xFFE0CDB8, "warm", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 175, 6, "NARS", "Light Reflecting Foundation", "Punjab", "Punjab", 0xFFD4C0A8, "warm", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 176, 6, "NARS", "Light Reflecting Foundation", "Santa Fe", "Santa Fe", 0xFFC9B498, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 177, 6, "NARS", "Light Reflecting Foundation", "Stromboli", "Stromboli", 0xFFC0A888, "warm", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 178, 6, "NARS", "Light Reflecting Foundation", "Barcelona", "Barcelona", 0xFFB89C78, "warm", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 179, 6, "NARS", "Light Reflecting Foundation", "Macao", "Macao", 0xFFAD9068, "warm", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 180, 6, "NARS", "Light Reflecting Foundation", "New Guinea", "New Guinea", 0xFFA08060, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 181, 6, "NARS", "Light Reflecting Foundation", "Tahoe", "Tahoe", 0xFF9C7A58, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 182, 6, "NARS", "Light Reflecting Foundation", "Cadiz", "Cadiz", 0xFF8B7050, "warm", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 183, 6, "NARS", "Light Reflecting Foundation", "Dark1", "Dark1", 0xFF7A6040, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 184, 6, "NARS", "Light Reflecting Foundation", "Dark2", "Dark2", 0xFF6E5438, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 185, 6, "NARS", "Light Reflecting Foundation", "Dark3", "Dark3", 0xFF604830, "neutral", "all", "medium", "natural", "foundation", "", 0);
        insertShade(db, 186, 6, "NARS", "Soft Velvet Loose Powder", "Eden", "Eden", 0xFFF0DDD0, "neutral", "all", "light", "matte", "powder", "", 0);
        insertShade(db, 187, 6, "NARS", "Soft Velvet Loose Powder", "Flesh", "Flesh", 0xFFE8D5C4, "neutral", "all", "light", "matte", "powder", "", 0);
        insertShade(db, 188, 6, "NARS", "Soft Velvet Loose Powder", "Mountain", "Mountain", 0xFFD4C0A8, "neutral", "all", "medium", "matte", "powder", "", 0);
        insertShade(db, 189, 6, "NARS", "Soft Velvet Loose Powder", "Beach", "Beach", 0xFFC9B498, "neutral", "all", "medium", "matte", "powder", "", 0);
        insertShade(db, 190, 6, "NARS", "Soft Velvet Loose Powder", "Desert", "Desert", 0xFFC0A888, "neutral", "all", "medium", "matte", "powder", "", 0);

        // Crosscodes
        insertCrosscode(db, 1, 1, "NC15", 2, "W1");
        insertCrosscode(db, 2, 1, "NC20", 2, "W2");
        insertCrosscode(db, 3, 1, "NC25", 2, "W3");
        insertCrosscode(db, 4, 1, "NC30", 2, "W4");
        insertCrosscode(db, 5, 1, "NC35", 2, "W5");
        insertCrosscode(db, 6, 1, "NC40", 2, "W6");
        insertCrosscode(db, 7, 1, "NC42", 2, "W7");
        insertCrosscode(db, 8, 1, "NC45", 2, "W8");
        insertCrosscode(db, 9, 1, "NC50", 2, "W9");
        insertCrosscode(db, 10, 1, "NC55", 2, "W10");
        insertCrosscode(db, 11, 1, "NW15", 2, "C1");
        insertCrosscode(db, 12, 1, "NW20", 2, "C2");
        insertCrosscode(db, 13, 1, "NW25", 2, "C3");
        insertCrosscode(db, 14, 1, "NW30", 2, "C4");
        insertCrosscode(db, 15, 1, "NW35", 2, "C5");
        insertCrosscode(db, 16, 1, "NW40", 2, "C6");
        insertCrosscode(db, 17, 1, "NW43", 2, "C7");
        insertCrosscode(db, 18, 1, "NW45", 2, "C8");
        insertCrosscode(db, 19, 1, "NW50", 2, "C9");
        insertCrosscode(db, 20, 1, "NC20", 3, "110");
        insertCrosscode(db, 21, 1, "NC25", 3, "112");
        insertCrosscode(db, 22, 1, "NC30", 3, "118");
        insertCrosscode(db, 23, 1, "NC35", 3, "120");
        insertCrosscode(db, 24, 1, "NC40", 3, "122");
        insertCrosscode(db, 25, 1, "NC42", 3, "124");
        insertCrosscode(db, 26, 1, "NC45", 3, "128");
        insertCrosscode(db, 27, 1, "NC50", 3, "130");
        insertCrosscode(db, 28, 1, "NC15", 4, "1N1");
        insertCrosscode(db, 29, 1, "NC20", 4, "1N2");
        insertCrosscode(db, 30, 1, "NC25", 4, "2N1");
        insertCrosscode(db, 31, 1, "NC30", 4, "2N2");
        insertCrosscode(db, 32, 1, "NC35", 4, "3N1");
        insertCrosscode(db, 33, 1, "NC40", 4, "3N2");
        insertCrosscode(db, 34, 1, "NC42", 4, "4N1");
        insertCrosscode(db, 35, 1, "NC45", 4, "4N2");
        insertCrosscode(db, 36, 1, "NC50", 4, "5N1");
        insertCrosscode(db, 37, 1, "NC55", 4, "5N2");
        insertCrosscode(db, 38, 2, "W1", 3, "102");
        insertCrosscode(db, 39, 2, "W2", 3, "110");
        insertCrosscode(db, 40, 2, "W3", 3, "112");
        insertCrosscode(db, 41, 2, "W4", 3, "115");
        insertCrosscode(db, 42, 2, "W5", 3, "118");
        insertCrosscode(db, 43, 2, "W6", 3, "120");
        insertCrosscode(db, 44, 2, "W7", 3, "122");
        insertCrosscode(db, 45, 2, "W8", 3, "124");
        insertCrosscode(db, 46, 2, "W9", 3, "128");
        insertCrosscode(db, 47, 2, "W10", 3, "130");
        insertCrosscode(db, 48, 2, "W1", 5, "110");
        insertCrosscode(db, 49, 2, "W2", 5, "150");
        insertCrosscode(db, 50, 2, "W3", 5, "180");
        insertCrosscode(db, 51, 2, "W4", 5, "200");
        insertCrosscode(db, 52, 2, "W5", 5, "220");
        insertCrosscode(db, 53, 2, "W6", 5, "240");
        insertCrosscode(db, 54, 2, "W7", 5, "250");
        insertCrosscode(db, 55, 2, "W8", 5, "300");
        insertCrosscode(db, 56, 2, "W9", 5, "320");
        insertCrosscode(db, 57, 2, "W10", 5, "340");
        insertCrosscode(db, 58, 4, "1N1", 5, "110");
        insertCrosscode(db, 59, 4, "1N2", 5, "150");
        insertCrosscode(db, 60, 4, "2N1", 5, "180");
        insertCrosscode(db, 61, 4, "2N2", 5, "200");
        insertCrosscode(db, 62, 4, "3N1", 5, "220");
        insertCrosscode(db, 63, 4, "3N2", 5, "240");
        insertCrosscode(db, 64, 4, "4N1", 5, "300");
        insertCrosscode(db, 65, 4, "4N2", 5, "320");
        insertCrosscode(db, 66, 4, "5N1", 5, "340");
        insertCrosscode(db, 67, 4, "5N2", 5, "370");

        Log.d(TAG, "Initial data inserted successfully");
    }

    private void insertBrand(SQLiteDatabase db, int id, String name, String country, String category, String website) {
        ContentValues values = new ContentValues();
        values.put(COL_BRAND_ID, id);
        values.put(COL_BRAND_NAME, name);
        values.put(COL_BRAND_COUNTRY, country);
        values.put(COL_BRAND_CATEGORY, category);
        values.put(COL_BRAND_WEBSITE, website);
        db.insert(TABLE_BRANDS, null, values);
    }

    private void insertShade(SQLiteDatabase db, int id, int brandId, String brandName,
                             String productLine, String shadeCode, String shadeName,
                             int colorRgb, String undertone, String skinType,
                             String coverage, String finish, String productType) {
        insertShade(db, id, brandId, brandName, productLine, shadeCode, shadeName,
                    colorRgb, undertone, skinType, coverage, finish, productType, "", 0);
    }

    private void insertShade(SQLiteDatabase db, int id, int brandId, String brandName,
                             String productLine, String shadeCode, String shadeName,
                             int colorRgb, String undertone, String skinType,
                             String coverage, String finish, String productType,
                             String ingredients, int hasSpf) {
        ContentValues values = new ContentValues();
        values.put(COL_SHADE_ID, id);
        values.put(COL_SHADE_BRAND_ID, brandId);
        values.put(COL_SHADE_BRAND_NAME, brandName);
        values.put(COL_SHADE_PRODUCT_LINE, productLine);
        values.put(COL_SHADE_CODE, shadeCode);
        values.put(COL_SHADE_NAME, shadeName);
        values.put(COL_SHADE_COLOR, colorRgb);
        values.put(COL_SHADE_UNDERTONE, undertone);
        values.put(COL_SHADE_SKIN_TYPE, skinType);
        values.put(COL_SHADE_COVERAGE, coverage);
        values.put(COL_SHADE_FINISH, finish);
        values.put(COL_SHADE_PRODUCT_TYPE, productType);
        values.put(COL_SHADE_INGREDIENTS, ingredients);
        values.put(COL_SHADE_HAS_SPF, hasSpf);
        db.insert(TABLE_SHADES, null, values);
    }

    private void insertCrosscode(SQLiteDatabase db, int id, int brand1Id, String shade1Code,
                                  int brand2Id, String shade2Code) {
        ContentValues values = new ContentValues();
        values.put(COL_CC_ID, id);
        values.put(COL_CC_BRAND1_ID, brand1Id);
        values.put(COL_CC_SHADE1_CODE, shade1Code);
        values.put(COL_CC_BRAND2_ID, brand2Id);
        values.put(COL_CC_SHADE2_CODE, shade2Code);
        db.insert(TABLE_CROSSCODES, null, values);
    }

    public List<Brand> getAllBrands() {
        List<Brand> brands = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BRANDS, null, null, null, null, null, COL_BRAND_NAME);
        
        if (cursor.moveToFirst()) {
            do {
                brands.add(new Brand(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_BRAND_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_BRAND_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_BRAND_COUNTRY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_BRAND_CATEGORY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_BRAND_WEBSITE))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return brands;
    }

    public List<Shade> getShadesByBrand(int brandId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SHADES, null, COL_SHADE_BRAND_ID + "=?",
                new String[]{String.valueOf(brandId)}, null, null, COL_SHADE_CODE);
        return cursorToShadeList(cursor);
    }

    public List<Shade> getShadesByProductType(String productType) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SHADES, null, COL_SHADE_PRODUCT_TYPE + "=?",
                new String[]{productType}, null, null, COL_SHADE_BRAND_NAME + "," + COL_SHADE_CODE);
        return cursorToShadeList(cursor);
    }

    public List<Shade> findShadesByColor(int targetColor, int maxResults, String undertone) {
        List<Shade> allShades = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selection = null;
        String[] selectionArgs = null;
        if (undertone != null && !undertone.equals("any")) {
            selection = COL_SHADE_UNDERTONE + "=?";
            selectionArgs = new String[]{undertone};
        }
        
        Cursor cursor = db.query(TABLE_SHADES, null, selection, selectionArgs, null, null, null);
        allShades = cursorToShadeList(cursor);
        
        allShades.sort((s1, s2) -> {
            double d1 = com.beauty.match.utils.ColorUtils.colorDistance(targetColor, s1.getColorRgb());
            double d2 = com.beauty.match.utils.ColorUtils.colorDistance(targetColor, s2.getColorRgb());
            return Double.compare(d1, d2);
        });
        
        if (allShades.size() > maxResults) {
            return allShades.subList(0, maxResults);
        }
        return allShades;
    }

    public List<Shade> findShadesWithFilters(String productType, String undertone,
                                              String coverage, String finish,
                                              String skinType, int[] brandIds) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder selection = new StringBuilder("1=1");
        List<String> args = new ArrayList<>();
        
        if (productType != null) {
            selection.append(" AND ").append(COL_SHADE_PRODUCT_TYPE).append("=?");
            args.add(productType);
        }
        if (undertone != null && !undertone.equals("any")) {
            selection.append(" AND ").append(COL_SHADE_UNDERTONE).append("=?");
            args.add(undertone);
        }
        if (coverage != null) {
            selection.append(" AND ").append(COL_SHADE_COVERAGE).append("=?");
            args.add(coverage);
        }
        if (finish != null) {
            selection.append(" AND ").append(COL_SHADE_FINISH).append("=?");
            args.add(finish);
        }
        if (skinType != null) {
            selection.append(" AND ").append(COL_SHADE_SKIN_TYPE).append("=?");
            args.add(skinType);
        }
        if (brandIds != null && brandIds.length > 0) {
            selection.append(" AND ").append(COL_SHADE_BRAND_ID).append(" IN (");
            for (int i = 0; i < brandIds.length; i++) {
                selection.append(brandIds[i]);
                if (i < brandIds.length - 1) selection.append(",");
            }
            selection.append(")");
        }
        
        Cursor cursor = db.query(TABLE_SHADES, null, selection.toString(),
                args.toArray(new String[0]), null, null,
                COL_SHADE_BRAND_NAME + "," + COL_SHADE_CODE);
        return cursorToShadeList(cursor);
    }

    public List<Shade> getCrosscodes(int brandId, String shadeCode) {
        List<Shade> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT s.* FROM " + TABLE_SHADES + " s " +
                "INNER JOIN " + TABLE_CROSSCODES + " c " +
                "ON s." + COL_SHADE_BRAND_ID + " = c." + COL_CC_BRAND2_ID +
                " AND s." + COL_SHADE_CODE + " = c." + COL_CC_SHADE2_CODE +
                " WHERE c." + COL_CC_BRAND1_ID + "=? AND c." + COL_CC_SHADE1_CODE + "=?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(brandId), shadeCode});
        return cursorToShadeList(cursor);
    }

    private List<Shade> cursorToShadeList(Cursor cursor) {
        List<Shade> shades = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                shades.add(new Shade(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_SHADE_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_SHADE_BRAND_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_SHADE_BRAND_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_SHADE_PRODUCT_LINE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_SHADE_CODE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_SHADE_NAME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_SHADE_COLOR)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_SHADE_UNDERTONE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_SHADE_SKIN_TYPE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_SHADE_COVERAGE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_SHADE_FINISH)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_SHADE_PRODUCT_TYPE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_SHADE_INGREDIENTS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_SHADE_HAS_SPF)) == 1
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return shades;
    }

    public DatabaseStats getStats() {
        SQLiteDatabase db = this.getReadableDatabase();
        DatabaseStats stats = new DatabaseStats();
        
        Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BRANDS, null);
        if (c1.moveToFirst()) stats.brandsCount = c1.getInt(0);
        c1.close();
        
        Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SHADES, null);
        if (c2.moveToFirst()) stats.shadesCount = c2.getInt(0);
        c2.close();
        
        Cursor c3 = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CROSSCODES, null);
        if (c3.moveToFirst()) stats.crosscodesCount = c3.getInt(0);
        c3.close();
        
        return stats;
    }

    public static class DatabaseStats {
        public int brandsCount;
        public int shadesCount;
        public int crosscodesCount;
    }
}
