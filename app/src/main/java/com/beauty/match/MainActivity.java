package com.beauty.match;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.beauty.match.camera.CameraActivity;
import com.beauty.match.catalog.FilterActivity;
import com.beauty.match.catalog.ProductListActivity;
import com.beauty.match.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout menuScan, menuCatalog, menuFilter, menuSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        animateMenuItems();
    }

    private void initViews() {
        menuScan = findViewById(R.id.menu_scan);
        menuCatalog = findViewById(R.id.menu_catalog);
        menuFilter = findViewById(R.id.menu_filter);
        menuSettings = findViewById(R.id.menu_settings);
    }

    private void setupListeners() {
        menuScan.setOnClickListener(v -> {
            animateClick(menuScan);
            startActivity(new Intent(MainActivity.this, CameraActivity.class));
        });

        menuCatalog.setOnClickListener(v -> {
            animateClick(menuCatalog);
            startActivity(new Intent(MainActivity.this, ProductListActivity.class));
        });

        menuFilter.setOnClickListener(v -> {
            animateClick(menuFilter);
            startActivity(new Intent(MainActivity.this, FilterActivity.class));
        });

        menuSettings.setOnClickListener(v -> {
            animateClick(menuSettings);
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });
    }

    private void animateMenuItems() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        LinearLayout[] items = {menuScan, menuCatalog, menuFilter, menuSettings};

        for (int i = 0; i < items.length; i++) {
            final int index = i;
            items[i].postDelayed(() -> {
                items[index].startAnimation(fadeIn);
                items[index].setVisibility(View.VISIBLE);
            }, i * 100);
        }
    }

    private void animateClick(View view) {
        view.animate()
                .scaleX(0.97f)
                .scaleY(0.97f)
                .setDuration(100)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this, R.style.Theme_BeautyMatch_Dialog)
                .setTitle("Выход из приложения")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Да", (dialog, which) -> finish())
                .setNegativeButton("Нет", null)
                .show();
    }
}
