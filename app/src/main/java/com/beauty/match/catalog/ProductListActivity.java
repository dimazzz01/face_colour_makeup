package com.beauty.match.catalog;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beauty.match.R;
import com.beauty.match.database.DatabaseHelper;
import com.beauty.match.model.Shade;
import com.beauty.match.utils.ColorUtils;
import com.beauty.match.utils.StorageUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private static final String TAG = "ProductListActivity";

    private RecyclerView recyclerView;
    private ShadeAdapter adapter;
    private DatabaseHelper dbHelper;
    private CardView headerResult;
    private TextView tvResultColor, tvResultInfo;
    private ImageButton btnFilters, btnExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        try {
            initViews();
            dbHelper = new DatabaseHelper(this);
            adapter = new ShadeAdapter();
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            adapter.setOnShadeClickListener(this::showShadeDetails);

            handleIntent(getIntent());
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Ошибка загрузки каталога: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_shades);
        headerResult = findViewById(R.id.header_result);
        tvResultColor = findViewById(R.id.tv_result_color);
        tvResultInfo = findViewById(R.id.tv_result_info);
        btnFilters = findViewById(R.id.btn_filters);
        btnExport = findViewById(R.id.btn_export);

        if (recyclerView == null || headerResult == null) {
            throw new IllegalStateException("Required views not found in layout");
        }

        btnFilters.setOnClickListener(v -> {
            Intent intent = new Intent(this, FilterActivity.class);
            startActivity(intent);
        });

        btnExport.setOnClickListener(v -> exportResults());
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;

        int skinColor = intent.getIntExtra("skin_color", -1);
        String undertone = intent.getStringExtra("undertone");

        if (skinColor != -1 && undertone != null) {
            headerResult.setVisibility(View.VISIBLE);
            tvResultColor.setText("Цвет: " + ColorUtils.toHexColor(skinColor).toUpperCase());
            tvResultInfo.setText("Подтон: " + undertone + " • Показаны ближайшие оттенки");

            List<Shade> matches = dbHelper.findShadesByColor(skinColor, 20, undertone);
            adapter.setShades(matches);

            if (matches.isEmpty()) {
                Toast.makeText(this, "Точных совпадений не найдено", Toast.LENGTH_LONG).show();
                adapter.setShades(dbHelper.getShadesByProductType("foundation"));
            }
        } else if (intent.hasExtra("filter_type") || intent.hasExtra("filter_undertone")) {
            applyFilterIntent(intent);
        } else {
            headerResult.setVisibility(View.GONE);
            List<Shade> allFoundations = dbHelper.getShadesByProductType("foundation");
            if (allFoundations == null) allFoundations = new ArrayList<>();
            adapter.setShades(allFoundations);
        }
    }

    private void applyFilterIntent(Intent intent) {
        String type = intent.getStringExtra("filter_type");
        String filterUndertone = intent.getStringExtra("filter_undertone");
        String coverage = intent.getStringExtra("filter_coverage");
        String finish = intent.getStringExtra("filter_finish");
        ArrayList<Integer> brands = intent.getIntegerArrayListExtra("filter_brands");
        boolean hasSpf = intent.getBooleanExtra("filter_has_spf", false);
        boolean noSilicone = intent.getBooleanExtra("filter_no_silicone", false);

        if ("any".equals(type)) type = null;
        if ("any".equals(filterUndertone)) filterUndertone = null;
        if ("any".equals(coverage)) coverage = null;
        if ("any".equals(finish)) finish = null;

        int[] brandIds = null;
        if (brands != null && !brands.isEmpty()) {
            brandIds = new int[brands.size()];
            for (int i = 0; i < brands.size(); i++) brandIds[i] = brands.get(i);
        }

        List<Shade> results = dbHelper.findShadesWithFilters(
                type, filterUndertone, coverage, finish, null, brandIds);

        if (hasSpf && results != null) {
            List<Shade> filtered = new ArrayList<>();
            for (Shade s : results) {
                if (s.hasSpf()) filtered.add(s);
            }
            results = filtered;
        }

        if (noSilicone && results != null) {
            List<Shade> filtered = new ArrayList<>();
            for (Shade s : results) {
                String ing = s.getIngredients();
                if (ing == null || !ing.toLowerCase().contains("silicone")) {
                    filtered.add(s);
                }
            }
            results = filtered;
        }

        headerResult.setVisibility(View.VISIBLE);
        tvResultColor.setText("Результаты фильтрации");
        tvResultInfo.setText(buildFilterDescription(type, filterUndertone, coverage, finish, brands, hasSpf));

        if (results == null || results.isEmpty()) {
            Toast.makeText(this, "По заданным фильтрам ничего не найдено", Toast.LENGTH_LONG).show();
        }
        adapter.setShades(results != null ? results : new ArrayList<>());
    }

    private String buildFilterDescription(String type, String undertone, String coverage,
                                          String finish, ArrayList<Integer> brands, boolean hasSpf) {
        StringBuilder sb = new StringBuilder();
        if (type != null) sb.append(capitalize(type)).append(" • ");
        if (undertone != null) sb.append(capitalize(undertone)).append(" • ");
        if (coverage != null) sb.append(capitalize(coverage)).append(" • ");
        if (finish != null) sb.append(capitalize(finish)).append(" • ");
        if (hasSpf) sb.append("SPF • ");
        if (sb.length() > 3) sb.setLength(sb.length() - 3);
        return sb.toString();
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private void showShadeDetails(Shade shade) {
        if (shade == null || dbHelper == null) return;

        List<Shade> crosscodes = dbHelper.getCrosscodes(shade.getBrandId(), shade.getShadeCode());

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_shade_detail, null);

        View vColor = dialogView.findViewById(R.id.dialog_shade_color);
        TextView tvTitle = dialogView.findViewById(R.id.dialog_title);
        TextView tvSubtitle = dialogView.findViewById(R.id.dialog_subtitle);
        TextView tvDetails = dialogView.findViewById(R.id.dialog_details);
        LinearLayout crossContainer = dialogView.findViewById(R.id.dialog_crosscodes_container);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(shade.getColorRgb());
        drawable.setStroke(3, Color.WHITE);
        vColor.setBackground(drawable);

        tvTitle.setText(shade.getShadeCode() + " — " + safe(shade.getShadeName()));
        tvSubtitle.setText(safe(shade.getBrandName()) + " • " + safe(shade.getProductLine()));

        String details = "Подтон: " + capitalize(safe(shade.getUndertone())) + "\n" +
                "Покрытие: " + capitalize(safe(shade.getCoverage())) + "\n" +
                "Финиш: " + capitalize(safe(shade.getFinish())) + "\n" +
                "Тип: " + capitalize(safe(shade.getProductType())) + "\n" +
                "Цвет: " + ColorUtils.toHexColor(shade.getColorRgb()).toUpperCase();
        tvDetails.setText(details);

        if (crosscodes != null && !crosscodes.isEmpty()) {
            for (Shade s : crosscodes) {
                View row = getLayoutInflater().inflate(R.layout.item_crosscode, crossContainer, false);

                View vCrossColor = row.findViewById(R.id.cross_color);
                TextView tvCrossText = row.findViewById(R.id.cross_text);

                GradientDrawable cd = new GradientDrawable();
                cd.setShape(GradientDrawable.OVAL);
                cd.setColor(s.getColorRgb());
                cd.setStroke(2, Color.LTGRAY);
                vCrossColor.setBackground(cd);

                tvCrossText.setText(s.getBrandName() + " " + s.getShadeCode() + " — " + s.getShadeName());

                crossContainer.addView(row);
            }
        } else {
            TextView tvNone = new TextView(this);
            tvNone.setText("Кросскоды для этого оттенка пока не добавлены.");
            tvNone.setTextColor(getResources().getColor(R.color.medium_gray));
            tvNone.setTextSize(13);
            crossContainer.addView(tvNone);
        }

        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_BeautyMatch_Dialog)
                .setView(dialogView)
                .setPositiveButton("OK", null)
                .show();
    }

    private void exportResults() {
        List<Shade> current = adapter.getShades();
        if (current == null || current.isEmpty()) {
            Toast.makeText(this, "Нет данных для экспорта", Toast.LENGTH_SHORT).show();
            return;
        }

        File exportDir = StorageUtils.getExportFolder(this);
        String fileName = "shades_export_" + System.currentTimeMillis() + ".csv";
        File file = new File(exportDir, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.append("Brand,Product Line,Code,Name,Undertone,Coverage,Finish,Type,Hex\n");
            for (Shade s : current) {
                writer.append(safe(s.getBrandName())).append(",")
                      .append(safe(s.getProductLine())).append(",")
                      .append(safe(s.getShadeCode())).append(",")
                      .append(safe(s.getShadeName())).append(",")
                      .append(safe(s.getUndertone())).append(",")
                      .append(safe(s.getCoverage())).append(",")
                      .append(safe(s.getFinish())).append(",")
                      .append(safe(s.getProductType())).append(",")
                      .append(ColorUtils.toHexColor(s.getColorRgb())).append("\n");
            }
            Toast.makeText(this, "Экспортировано: " + file.getName(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e(TAG, "Export error", e);
            Toast.makeText(this, "Ошибка экспорта", Toast.LENGTH_SHORT).show();
        }
    }

    private String safe(String value) {
        return value != null ? value : "-";
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
