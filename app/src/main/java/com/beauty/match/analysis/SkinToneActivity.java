package com.beauty.match.analysis;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.beauty.match.R;
import com.beauty.match.catalog.ProductListActivity;
import com.beauty.match.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;

public class SkinToneActivity extends AppCompatActivity {

    private static final String TAG = "SkinToneActivity";
    private static final int MAX_POINTS = 3;

    private ImageView ivPhoto;
    private FrameLayout pointsContainer;
    private LinearLayout pointsListContainer;
    private CardView cardPointsControl, cardResult, bottomEditPanel;
    private View vResultColor, editColorPreview;
    private TextView tvHexColor, tvUndertone, tvUndertoneDesc;
    private TextView tvPointsCount, tvPhotoHint, tvEditTitle, tvEditHex, tvEditArea;
    private Button btnAnalyze, btnFindProducts, btnRetake;
    private Button btnForehead, btnLeftCheek, btnRightCheek, btnNose, btnChin;
    private ImageButton btnHelp, btnCloseEdit;
    private Button btnDeletePoint, btnMovePoint;

    private Bitmap originalBitmap;
    private String photoPath;
    private List<SelectedPoint> selectedPoints = new ArrayList<>();
    private int selectedPointIndex = -1;
    private boolean isMoveMode = false;

    private static final float[][] PRESET_AREAS = {
        {0.50f, 0.22f},
        {0.28f, 0.48f},
        {0.72f, 0.48f},
        {0.50f, 0.42f},
        {0.50f, 0.72f}
    };
    private static final String[] AREA_NAMES = {"Лоб", "Левая щека", "Правая щека", "Нос", "Подбородок"};

    private int analyzedColor = -1;
    private String analyzedUndertone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_tone);

        photoPath = getIntent().getStringExtra("photo_path");
        if (photoPath == null) {
            Toast.makeText(this, "Фото не найдено", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadPhoto();
        setupListeners();
    }

    private void initViews() {
        ivPhoto = findViewById(R.id.iv_photo);
        pointsContainer = findViewById(R.id.points_container);
        pointsListContainer = findViewById(R.id.points_list_container);
        cardPointsControl = findViewById(R.id.card_points_control);
        cardResult = findViewById(R.id.card_result);
        bottomEditPanel = findViewById(R.id.bottom_edit_panel);
        vResultColor = findViewById(R.id.v_result_color);
        editColorPreview = findViewById(R.id.edit_color_preview);
        tvHexColor = findViewById(R.id.tv_hex_color);
        tvUndertone = findViewById(R.id.tv_undertone);
        tvUndertoneDesc = findViewById(R.id.tv_undertone_desc);
        tvPointsCount = findViewById(R.id.tv_points_count);
        tvPhotoHint = findViewById(R.id.tv_photo_hint);
        tvEditTitle = findViewById(R.id.tv_edit_title);
        tvEditHex = findViewById(R.id.tv_edit_hex);
        tvEditArea = findViewById(R.id.tv_edit_area);
        btnAnalyze = findViewById(R.id.btn_analyze);
        btnFindProducts = findViewById(R.id.btn_find_products);
        btnRetake = findViewById(R.id.btn_retake);
        btnHelp = findViewById(R.id.btn_help);
        btnCloseEdit = findViewById(R.id.btn_close_edit);
        btnDeletePoint = findViewById(R.id.btn_delete_point);
        btnMovePoint = findViewById(R.id.btn_move_point);
        btnForehead = findViewById(R.id.btn_area_forehead);
        btnLeftCheek = findViewById(R.id.btn_area_left_cheek);
        btnRightCheek = findViewById(R.id.btn_area_right_cheek);
        btnNose = findViewById(R.id.btn_area_nose);
        btnChin = findViewById(R.id.btn_area_chin);
    }

    private void loadPhoto() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        originalBitmap = BitmapFactory.decodeFile(photoPath, options);

        if (originalBitmap == null) {
            Toast.makeText(this, "Не удалось загрузить фото", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ivPhoto.setImageBitmap(originalBitmap);
    }

    private void setupListeners() {
        ivPhoto.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (isMoveMode && selectedPointIndex >= 0) {
                    moveSelectedPoint(event.getX(), event.getY());
                    return true;
                }

                if (selectedPoints.size() >= MAX_POINTS) {
                    Toast.makeText(this, "Максимум " + MAX_POINTS + " точки", Toast.LENGTH_SHORT).show();
                    return true;
                }

                float[] touchPoint = new float[]{event.getX(), event.getY()};
                Matrix matrix = new Matrix();
                ivPhoto.getImageMatrix().invert(matrix);
                matrix.mapPoints(touchPoint);

                float bitmapX = touchPoint[0];
                float bitmapY = touchPoint[1];

                if (bitmapX >= 0 && bitmapX < originalBitmap.getWidth() &&
                    bitmapY >= 0 && bitmapY < originalBitmap.getHeight()) {

                    float relX = bitmapX / originalBitmap.getWidth();
                    float relY = bitmapY / originalBitmap.getHeight();
                    int color = getAverageColorAround((int) bitmapX, (int) bitmapY);

                    addPoint(relX, relY, color, "Точка " + (selectedPoints.size() + 1));
                    updatePointsDisplay();
                    animateCardAppear(cardPointsControl);
                }
                return true;
            }
            return false;
        });

        btnForehead.setOnClickListener(v -> addPresetPoint(0));
        btnLeftCheek.setOnClickListener(v -> addPresetPoint(1));
        btnRightCheek.setOnClickListener(v -> addPresetPoint(2));
        btnNose.setOnClickListener(v -> addPresetPoint(3));
        btnChin.setOnClickListener(v -> addPresetPoint(4));

        btnAnalyze.setOnClickListener(v -> performAnalysis());
        btnFindProducts.setOnClickListener(v -> goToProducts());
        btnRetake.setOnClickListener(v -> finish());

        btnHelp.setOnClickListener(v -> showHelpDialog());

        btnCloseEdit.setOnClickListener(v -> closeEditPanel());
        btnDeletePoint.setOnClickListener(v -> deleteSelectedPoint());
        btnMovePoint.setOnClickListener(v -> startMoveMode());
    }

    private void addPresetPoint(int areaIndex) {
        if (selectedPoints.size() >= MAX_POINTS) {
            Toast.makeText(this, "Сначала удалите лишнюю точку", Toast.LENGTH_SHORT).show();
            return;
        }

        float relX = PRESET_AREAS[areaIndex][0];
        float relY = PRESET_AREAS[areaIndex][1];
        int x = (int) (relX * originalBitmap.getWidth());
        int y = (int) (relY * originalBitmap.getHeight());
        int color = getAverageColorAround(x, y);

        addPoint(relX, relY, color, AREA_NAMES[areaIndex]);
        updatePointsDisplay();
        animateCardAppear(cardPointsControl);
    }

    private int getAverageColorAround(int centerX, int centerY) {
        int radius = 10;
        int startX = Math.max(0, centerX - radius);
        int endX = Math.min(originalBitmap.getWidth(), centerX + radius);
        int startY = Math.max(0, centerY - radius);
        int endY = Math.min(originalBitmap.getHeight(), centerY + radius);

        long r = 0, g = 0, b = 0;
        int count = 0;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int pixel = originalBitmap.getPixel(x, y);
                r += Color.red(pixel);
                g += Color.green(pixel);
                b += Color.blue(pixel);
                count++;
            }
        }

        if (count == 0) return originalBitmap.getPixel(centerX, centerY);
        return Color.rgb((int) (r / count), (int) (g / count), (int) (b / count));
    }

    private void addPoint(float relX, float relY, int color, String name) {
        SelectedPoint point = new SelectedPoint(relX, relY, color, name);
        selectedPoints.add(point);
        refreshPhotoMarkers();
        tvPhotoHint.setVisibility(View.GONE);
    }

    private void refreshPhotoMarkers() {
        pointsContainer.removeAllViews();

        for (int i = 0; i < selectedPoints.size(); i++) {
            SelectedPoint point = selectedPoints.get(i);
            View marker = createMarker(point, i);
            pointsContainer.addView(marker);
        }

        tvPointsCount.setText(selectedPoints.size() + "/" + MAX_POINTS);
    }

    private View createMarker(SelectedPoint point, int index) {
        View marker = new View(this);
        int size = getResources().getDimensionPixelSize(R.dimen.marker_size);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);

        params.leftMargin = (int) (point.relX * pointsContainer.getWidth()) - size / 2;
        params.topMargin = (int) (point.relY * pointsContainer.getHeight()) - size / 2;

        marker.setLayoutParams(params);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(point.color);
        drawable.setStroke(3, Color.WHITE);

        if (index == selectedPointIndex) {
            drawable.setStroke(4, getResources().getColor(R.color.pastel_mint));
            marker.setElevation(8f);
        }

        marker.setBackground(drawable);
        marker.setElevation(6f);

        marker.setOnClickListener(v -> openEditPanel(index));

        return marker;
    }

    private void updatePointsDisplay() {
        pointsListContainer.removeAllViews();

        for (int i = 0; i < selectedPoints.size(); i++) {
            SelectedPoint point = selectedPoints.get(i);
            View row = getLayoutInflater().inflate(R.layout.item_point, pointsListContainer, false);

            View vColor = row.findViewById(R.id.point_color);
            TextView tvName = row.findViewById(R.id.point_name);
            TextView tvHex = row.findViewById(R.id.point_hex);
            ImageView ivSelected = row.findViewById(R.id.point_selected_indicator);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(point.color);
            drawable.setStroke(2, Color.LTGRAY);
            vColor.setBackground(drawable);

            tvName.setText(point.name);
            tvHex.setText(ColorUtils.toHexColor(point.color).toUpperCase());

            if (i == selectedPointIndex) {
                ivSelected.setVisibility(View.VISIBLE);
                row.setBackgroundColor(getResources().getColor(R.color.pastel_mint_light));
            }

            final int idx = i;
            row.setOnClickListener(v -> openEditPanel(idx));

            pointsListContainer.addView(row);
        }

        cardPointsControl.setVisibility(selectedPoints.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void openEditPanel(int index) {
        selectedPointIndex = index;
        SelectedPoint point = selectedPoints.get(index);

        tvEditTitle.setText(point.name);
        tvEditHex.setText(ColorUtils.toHexColor(point.color).toUpperCase());
        tvEditArea.setText("Координаты: " + String.format("%.0f%%", point.relX * 100) + ", " +
                String.format("%.0f%%", point.relY * 100));

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(point.color);
        drawable.setStroke(3, Color.WHITE);
        editColorPreview.setBackground(drawable);

        refreshPhotoMarkers();
        updatePointsDisplay();

        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        bottomEditPanel.startAnimation(slideUp);
        bottomEditPanel.setVisibility(View.VISIBLE);

        isMoveMode = false;
        btnMovePoint.setText("Переместить");
    }

    private void closeEditPanel() {
        bottomEditPanel.setVisibility(View.GONE);
        selectedPointIndex = -1;
        isMoveMode = false;
        refreshPhotoMarkers();
        updatePointsDisplay();
    }

    private void deleteSelectedPoint() {
        if (selectedPointIndex < 0 || selectedPointIndex >= selectedPoints.size()) return;

        selectedPoints.remove(selectedPointIndex);
        closeEditPanel();

        if (selectedPoints.isEmpty()) {
            tvPhotoHint.setVisibility(View.VISIBLE);
            cardResult.setVisibility(View.GONE);
            btnFindProducts.setVisibility(View.GONE);
        }

        Toast.makeText(this, "Точка удалена", Toast.LENGTH_SHORT).show();
    }

    private void startMoveMode() {
        isMoveMode = true;
        btnMovePoint.setText("Нажмите на фото...");
        Toast.makeText(this, "Нажмите на новое место на фото", Toast.LENGTH_SHORT).show();
    }

    private void moveSelectedPoint(float touchX, float touchY) {
        if (selectedPointIndex < 0) return;

        float[] touchPoint = new float[]{touchX, touchY};
        Matrix matrix = new Matrix();
        ivPhoto.getImageMatrix().invert(matrix);
        matrix.mapPoints(touchPoint);

        float bitmapX = touchPoint[0];
        float bitmapY = touchPoint[1];

        if (bitmapX < 0 || bitmapX >= originalBitmap.getWidth() ||
            bitmapY < 0 || bitmapY >= originalBitmap.getHeight()) {
            return;
        }

        SelectedPoint point = selectedPoints.get(selectedPointIndex);
        point.relX = bitmapX / originalBitmap.getWidth();
        point.relY = bitmapY / originalBitmap.getHeight();
        point.color = getAverageColorAround((int) bitmapX, (int) bitmapY);

        isMoveMode = false;
        btnMovePoint.setText("Переместить");

        openEditPanel(selectedPointIndex);
        Toast.makeText(this, "Точка перемещена", Toast.LENGTH_SHORT).show();
    }

    private void performAnalysis() {
        if (selectedPoints.isEmpty()) {
            Toast.makeText(this, "Выберите хотя бы одну точку", Toast.LENGTH_SHORT).show();
            return;
        }

        long r = 0, g = 0, b = 0;
        for (SelectedPoint p : selectedPoints) {
            r += Color.red(p.color);
            g += Color.green(p.color);
            b += Color.blue(p.color);
        }
        int avgColor = Color.rgb((int) (r / selectedPoints.size()),
                (int) (g / selectedPoints.size()),
                (int) (b / selectedPoints.size()));

        boolean normalize = getSharedPreferences("BeautyMatchPrefs", MODE_PRIVATE)
                .getBoolean("normalize_enabled", true);

        if (normalize) {
            analyzedColor = ColorUtils.normalizeSkinColor(avgColor, 0.75f);
        } else {
            analyzedColor = avgColor;
        }

        analyzedUndertone = ColorUtils.detectUndertone(analyzedColor);

        cardResult.setVisibility(View.VISIBLE);
        btnFindProducts.setVisibility(View.VISIBLE);

        animateCardAppear(cardResult);

        GradientDrawable resultDrawable = new GradientDrawable();
        resultDrawable.setShape(GradientDrawable.OVAL);
        resultDrawable.setColor(analyzedColor);
        vResultColor.setBackground(resultDrawable);

        tvHexColor.setText(ColorUtils.toHexColor(analyzedColor).toUpperCase());

        String undertoneText, undertoneDesc;
        switch (analyzedUndertone) {
            case "warm":
                undertoneText = "Тёплый (Warm)";
                undertoneDesc = "Золотистый/персиковый подтон. Рекомендуются оттенки с индексами W, NC, Golden, Honey.";
                break;
            case "cool":
                undertoneText = "Холодный (Cool)";
                undertoneDesc = "Розовый/оливковый подтон. Рекомендуются оттенки с индексами C, NW, Rose, Pink.";
                break;
            default:
                undertoneText = "Нейтральный (Neutral)";
                undertoneDesc = "Сбалансированный подтон. Подходят оттенки N, а также многие W и C.";
                break;
        }

        tvUndertone.setText(undertoneText);
        tvUndertoneDesc.setText(undertoneDesc);

        cardResult.post(() -> {
            cardResult.requestFocus();
            cardResult.getParent().requestChildFocus(cardResult, cardResult);
        });
    }

    private void goToProducts() {
        if (analyzedColor == -1) {
            Toast.makeText(this, "Сначала выполните анализ", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ProductListActivity.class);
        intent.putExtra("skin_color", analyzedColor);
        intent.putExtra("undertone", analyzedUndertone);
        intent.putExtra("photo_path", photoPath);
        startActivity(intent);
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(this, R.style.Theme_BeautyMatch_Dialog)
                .setTitle("Как анализировать тон кожи")
                .setMessage("1. Нажмите на чистую область кожи на фото\n" +
                        "2. Добавьте до 3 точек для точности\n" +
                        "3. Используйте быстрые кнопки для типичных зон\n" +
                        "4. Нажмите на точку в списке, чтобы изменить или удалить\n" +
                        "5. Нажмите «Анализировать» для получения результата")
                .setPositiveButton("Понятно", null)
                .show();
    }

    private void animateCardAppear(View view) {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        view.startAnimation(fadeIn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }
    }

    @Override
    public void onBackPressed() {
        if (bottomEditPanel.getVisibility() == View.VISIBLE) {
            closeEditPanel();
            return;
        }
        finish();
    }

    private static class SelectedPoint {
        float relX, relY;
        int color;
        String name;

        SelectedPoint(float relX, float relY, int color, String name) {
            this.relX = relX;
            this.relY = relY;
            this.color = color;
            this.name = name;
        }
    }
}
