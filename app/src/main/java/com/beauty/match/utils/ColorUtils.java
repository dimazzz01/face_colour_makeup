package com.beauty.match.utils;

import android.graphics.Color;

public class ColorUtils {

    /**
     * Вычисляет евклидово расстояние между двумя цветами в RGB
     */
    public static double colorDistance(int color1, int color2) {
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);
        
        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);
        
        return Math.sqrt(
            Math.pow(r1 - r2, 2) +
            Math.pow(g1 - g2, 2) +
            Math.pow(b1 - b2, 2)
        );
    }

    /**
     * Преобразует RGB в HSV и возвращает тон (Hue) — полезно для определения подтона кожи
     */
    public static float[] rgbToHsv(int color) {
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
        return hsv;
    }

    /**
     * Определяет подтон кожи: тёплый, холодный, нейтральный
     * на основе соотношения красного и жёлтого
     */
    public static String detectUndertone(int skinColor) {
        int r = Color.red(skinColor);
        int g = Color.green(skinColor);
        int b = Color.blue(skinColor);
        
        // Тёплый: больше жёлтого/оранжевого (R и G высокие, B низкий)
        // Холодный: больше розового/синего (R и B высокие, G средний)
        // Нейтральный: всё в балансе
        
        float warmth = (r + g) / 2.0f - b;
        
        if (warmth > 40) {
            return "warm";      // Тёплый (золотистый, персиковый)
        } else if (warmth < -10) {
            return "cool";      // Холодный (розовый, оливковый)
        } else {
            return "neutral";   // Нейтральный
        }
    }

    /**
     * Нормализация цвета: осветление/затемнение до стандартного диапазона
     * Убирает влияние яркости освещения
     */
    public static int normalizeSkinColor(int color, float targetLuminance) {
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
        
        // Нормализуем яркость (Value), сохраняя оттенок и насыщенность
        hsv[2] = targetLuminance;
        
        return Color.HSVToColor(hsv);
    }

    /**
     * Получает средний цвет из массива пикселей
     */
    public static int averageColor(int[] pixels) {
        long r = 0, g = 0, b = 0;
        for (int pixel : pixels) {
            r += Color.red(pixel);
            g += Color.green(pixel);
            b += Color.blue(pixel);
        }
        int count = pixels.length;
        return Color.rgb((int)(r/count), (int)(g/count), (int)(b/count));
    }

    /**
     * Конвертирует HEX строку в цвет
     */
    public static int parseHexColor(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        return Color.parseColor("#" + hex);
    }

    /**
     * Конвертирует цвет в HEX строку
     */
    public static String toHexColor(int color) {
        return String.format("#%02X%02X%02X", 
            Color.red(color), Color.green(color), Color.blue(color));
    }
}
