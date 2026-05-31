package com.beauty.match.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * Поворачивает Bitmap на заданный угол
     */
    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * Зеркально отражает Bitmap по горизонтали (для фронтальной камеры)
     */
    public static Bitmap flipBitmapHorizontal(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * Сохраняет Bitmap в файл с качеством JPEG 95%
     */
    public static boolean saveBitmap(Bitmap bitmap, File file) {
        FileOutputStream out = null;
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
            out.flush();
            Log.d(TAG, "Bitmap saved: " + file.getAbsolutePath() + ", size: " + file.length());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to save bitmap", e);
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "Close error", e);
                }
            }
        }
    }
}
