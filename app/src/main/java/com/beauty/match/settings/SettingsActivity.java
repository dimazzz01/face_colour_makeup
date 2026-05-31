package com.beauty.match.settings;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.beauty.match.R;
import com.beauty.match.database.DatabaseHelper;
import com.beauty.match.utils.StorageUtils;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "BeautyMatchPrefs";
    
    private TextView tvPhotosPath, tvDatabasePath, tvExportPath, tvStats;
    private Switch switchFlash, switchAutoFocus, switchNormalize;
    private Button btnResetPaths, btnClearCache, btnExportDb;
    private LinearLayout menuPhotos, menuDatabase, menuExport;

    private SharedPreferences prefs;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        dbHelper = new DatabaseHelper(this);

        initViews();
        loadSettings();
        updatePathDisplay();
        updateStats();
        setupListeners();
        checkPermissions();
    }

    private void initViews() {
        tvPhotosPath = findViewById(R.id.tv_photos_path);
        tvDatabasePath = findViewById(R.id.tv_database_path);
        tvExportPath = findViewById(R.id.tv_export_path);
        tvStats = findViewById(R.id.tv_stats);
        
        switchFlash = findViewById(R.id.switch_flash);
        switchAutoFocus = findViewById(R.id.switch_autofocus);
        switchNormalize = findViewById(R.id.switch_normalize);
        
        btnResetPaths = findViewById(R.id.btn_reset_paths);
        btnClearCache = findViewById(R.id.btn_clear_cache);
        btnExportDb = findViewById(R.id.btn_export_db);
        
        menuPhotos = findViewById(R.id.menu_photos_folder);
        menuDatabase = findViewById(R.id.menu_database_folder);
        menuExport = findViewById(R.id.menu_export_folder);
    }

    private void loadSettings() {
        switchFlash.setChecked(prefs.getBoolean("flash_enabled", true));
        switchAutoFocus.setChecked(prefs.getBoolean("autofocus_enabled", true));
        switchNormalize.setChecked(prefs.getBoolean("normalize_enabled", true));
    }

    private void updatePathDisplay() {
        File photos = StorageUtils.getPhotosFolder(this);
        File database = StorageUtils.getDatabaseFolder(this);
        File export = StorageUtils.getExportFolder(this);
        
        tvPhotosPath.setText(photos.getAbsolutePath());
        tvDatabasePath.setText(database.getAbsolutePath());
        tvExportPath.setText(export.getAbsolutePath());
    }

    private void updateStats() {
        DatabaseHelper.DatabaseStats stats = dbHelper.getStats();
        String statsText = String.format(
            "Брендов: %d\nОттенков: %d\nКросскодов: %d\nПапка фото: %s\nПапка БД: %s",
            stats.brandsCount,
            stats.shadesCount,
            stats.crosscodesCount,
            StorageUtils.formatSize(StorageUtils.getFolderSize(StorageUtils.getPhotosFolder(this))),
            StorageUtils.formatSize(StorageUtils.getFolderSize(StorageUtils.getDatabaseFolder(this)))
        );
        tvStats.setText(statsText);
    }

    private void setupListeners() {
        switchFlash.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("flash_enabled", isChecked).apply();
            Toast.makeText(this, "Вспышка: " + (isChecked ? "вкл" : "выкл"), Toast.LENGTH_SHORT).show();
        });

        switchAutoFocus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("autofocus_enabled", isChecked).apply();
        });

        switchNormalize.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("normalize_enabled", isChecked).apply();
        });

        menuPhotos.setOnClickListener(v -> showFolderInfo("Папка фотографий", 
            StorageUtils.getPhotosFolder(this)));
        menuDatabase.setOnClickListener(v -> showFolderInfo("Папка базы данных", 
            StorageUtils.getDatabaseFolder(this)));
        menuExport.setOnClickListener(v -> showFolderInfo("Папка экспорта", 
            StorageUtils.getExportFolder(this)));

        btnResetPaths.setOnClickListener(v -> {
            new AlertDialog.Builder(this, R.style.Theme_BeautyMatch_Dialog)
                .setTitle("Сброс папок")
                .setMessage("Все папки будут созданы заново по умолчанию. Продолжить?")
                .setPositiveButton("Да", (dialog, which) -> {
                    StorageUtils.getPhotosFolder(this);
                    StorageUtils.getDatabaseFolder(this);
                    StorageUtils.getExportFolder(this);
                    StorageUtils.getSettingsFolder(this);
                    updatePathDisplay();
                    updateStats();
                    Toast.makeText(this, "Папки восстановлены", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
        });

        btnClearCache.setOnClickListener(v -> {
            new AlertDialog.Builder(this, R.style.Theme_BeautyMatch_Dialog)
                .setTitle("Очистка кэша")
                .setMessage("Удалить все временные файлы и фотографии?")
                .setPositiveButton("Очистить", (dialog, which) -> {
                    File photos = StorageUtils.getPhotosFolder(this);
                    File[] files = photos.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            file.delete();
                        }
                    }
                    updateStats();
                    Toast.makeText(this, "Кэш очищен", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
        });

        btnExportDb.setOnClickListener(v -> {
            File dbFile = getDatabasePath("beautymatch.db");
            File exportDir = StorageUtils.getExportFolder(this);
            File destFile = new File(exportDir, "beautymatch_backup_" + System.currentTimeMillis() + ".db");
            
            if (StorageUtils.copyFile(dbFile, destFile)) {
                Toast.makeText(this, "База экспортирована: " + destFile.getName(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Ошибка экспорта", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFolderInfo(String title, File folder) {
        long size = StorageUtils.getFolderSize(folder);
        int fileCount = folder.listFiles() != null ? folder.listFiles().length : 0;
        
        new AlertDialog.Builder(this, R.style.Theme_BeautyMatch_Dialog)
            .setTitle(title)
            .setMessage("Путь: " + folder.getAbsolutePath() + 
                       "\nРазмер: " + StorageUtils.formatSize(size) +
                       "\nФайлов: " + fileCount +
                       "\nДоступно для записи: " + (folder.canWrite() ? "Да" : "Нет"))
            .setPositiveButton("OK", null)
            .show();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            };
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    // Для Android 11+ нужен специальный запрос
                }
            }
            
            boolean needRequest = false;
            for (String perm : permissions) {
                if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                    needRequest = true;
                    break;
                }
            }
            
            if (needRequest) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Необходимо разрешение: " + permissions[i], Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
