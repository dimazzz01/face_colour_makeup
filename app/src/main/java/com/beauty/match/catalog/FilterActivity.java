package com.beauty.match.catalog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.beauty.match.R;

import java.util.ArrayList;

public class FilterActivity extends AppCompatActivity {

    private RadioGroup rgType, rgUndertone, rgCoverage, rgFinish;
    private CheckBox cbMac, cbLoreal, cbMaybelline, cbEstee, cbRevlon, cbNars;
    private CheckBox cbSpf, cbNoSilicone;
    private Button btnApply, btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        initViews();
        setupListeners();
    }

    private void initViews() {
        rgType = findViewById(R.id.rg_type);
        rgUndertone = findViewById(R.id.rg_undertone);
        rgCoverage = findViewById(R.id.rg_coverage);
        rgFinish = findViewById(R.id.rg_finish);

        cbMac = findViewById(R.id.cb_mac);
        cbLoreal = findViewById(R.id.cb_loreal);
        cbMaybelline = findViewById(R.id.cb_maybelline);
        cbEstee = findViewById(R.id.cb_estee);
        cbRevlon = findViewById(R.id.cb_revlon);
        cbNars = findViewById(R.id.cb_nars);

        cbSpf = findViewById(R.id.cb_spf);
        cbNoSilicone = findViewById(R.id.cb_no_silicone);

        btnApply = findViewById(R.id.btn_apply);
        btnReset = findViewById(R.id.btn_reset);
    }

    private void setupListeners() {
        btnApply.setOnClickListener(v -> applyFilters());
        btnReset.setOnClickListener(v -> resetFilters());
    }

    private void applyFilters() {
        Intent intent = new Intent(this, ProductListActivity.class);

        int typeId = rgType.getCheckedRadioButtonId();
        String type = "any";
        if (typeId == R.id.rb_foundation) type = "foundation";
        else if (typeId == R.id.rb_powder) type = "powder";
        else if (typeId == R.id.rb_concealer) type = "concealer";
        intent.putExtra("filter_type", type);

        int undertoneId = rgUndertone.getCheckedRadioButtonId();
        String undertone = "any";
        if (undertoneId == R.id.rb_warm) undertone = "warm";
        else if (undertoneId == R.id.rb_cool) undertone = "cool";
        else if (undertoneId == R.id.rb_neutral) undertone = "neutral";
        else if (undertoneId == R.id.rb_olive) undertone = "olive";
        intent.putExtra("filter_undertone", undertone);

        int coverageId = rgCoverage.getCheckedRadioButtonId();
        String coverage = "any";
        if (coverageId == R.id.rb_light) coverage = "light";
        else if (coverageId == R.id.rb_medium) coverage = "medium";
        else if (coverageId == R.id.rb_full) coverage = "full";
        intent.putExtra("filter_coverage", coverage);

        int finishId = rgFinish.getCheckedRadioButtonId();
        String finish = "any";
        if (finishId == R.id.rb_matte) finish = "matte";
        else if (finishId == R.id.rb_dewy) finish = "dewy";
        else if (finishId == R.id.rb_satin) finish = "satin";
        else if (finishId == R.id.rb_natural) finish = "natural";
        intent.putExtra("filter_finish", finish);

        ArrayList<Integer> brands = new ArrayList<>();
        if (cbMac.isChecked()) brands.add(1);
        if (cbLoreal.isChecked()) brands.add(2);
        if (cbMaybelline.isChecked()) brands.add(3);
        if (cbEstee.isChecked()) brands.add(4);
        if (cbRevlon.isChecked()) brands.add(5);
        if (cbNars.isChecked()) brands.add(6);
        intent.putIntegerArrayListExtra("filter_brands", brands);

        intent.putExtra("filter_has_spf", cbSpf.isChecked());
        intent.putExtra("filter_no_silicone", cbNoSilicone.isChecked());

        startActivity(intent);
    }

    private void resetFilters() {
        rgType.check(R.id.rb_foundation);
        rgUndertone.check(R.id.rb_neutral);
        rgCoverage.check(R.id.rb_medium);
        rgFinish.check(R.id.rb_natural);

        cbMac.setChecked(false);
        cbLoreal.setChecked(false);
        cbMaybelline.setChecked(false);
        cbEstee.setChecked(false);
        cbRevlon.setChecked(false);
        cbNars.setChecked(false);

        cbSpf.setChecked(false);
        cbNoSilicone.setChecked(false);

        Toast.makeText(this, "Фильтры сброшены", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
