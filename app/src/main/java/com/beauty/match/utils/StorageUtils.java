package com.beauty.match.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class StorageUtils {

    private static final String TAG = "StorageUtils";
    
    // Имена папок по умолчанию
    public static final String FOLDER_IMAGES = "BeautyMatch/Photos";
    public static final String FOLDER_DATABASE = "BeautyMatch/Database";
    public static final String FOLDER_EXPORT = "BeautyMatch/Exports";
    public static final String FOLDER_SETTINGS = "BeautyMatch/Settings";

    /**
     * Получает или создаёт папку во внешнем хранилище (Documents)
     */
    public static File getAppFolder(Context context, String folderName) {
        File baseDir;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ — используем app-specific external storage
            baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        } else {
            // Android 9 и ниже — общее хранилище
            baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        }
        
        if (baseDir == null) {
            baseDir = context.getFilesDir();
        }
        
        File folder = new File(baseDir, folderName);
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            Log.d(TAG, "Folder created: " + created + " path: " + folder.getAbsolutePath());
        }
        return folder;
    }

    /**
     * Получает путь к папке для фотографий
     */
    public static File getPhotosFolder(Context context) {
        return getAppFolder(context, FOLDER_IMAGES);
    }

    /**
     * Получает путь к папке для базы данных
     */
    public static File getDatabaseFolder(Context context) {
        return getAppFolder(context, FOLDER_DATABASE);
    }

    /**
     * Получает путь к папке для экспорта
     */
    public static File getExportFolder(Context context) {
        return getAppFolder(context, FOLDER_EXPORT);
    }

    /**
     * Получает путь к папке для настроек
     */
    public static File getSettingsFolder(Context context) {
        return getAppFolder(context, FOLDER_SETTINGS);
    }

    /**
     * Проверяет доступность записи во внешнее хранилище
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Копирует файл
     */
    public static boolean copyFile(File src, File dst) {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Copy error", e);
            return false;
        } finally {
            try {
                if (inChannel != null) inChannel.close();
                if (outChannel != null) outChannel.close();
            } catch (IOException e) {
                Log.e(TAG, "Close error", e);
            }
        }
    }

    /**
     * Удаляет файл или папку рекурсивно
     */
    public static boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        return fileOrDirectory.delete();
    }

    /**
     * Получает размер папки в байтах
     */
    public static long getFolderSize(File folder) {
        long size = 0;
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    size += file.isDirectory() ? getFolderSize(file) : file.length();
                }
            }
        } else {
            size = folder.length();
        }
        return size;
    }

    /**
     * Форматирует байты в человекочитаемый вид
     */
    public static String formatSize(long size) {
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", size / Math.pow(1024, exp), unit);
    }
}
