package com.mikesuvade.focus.ui.temperature;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.R;
import com.mikesuvade.focus.ui.saved.SavedMeasurementsActivity;

public class TemperatureActivity extends AppCompatActivity {

    private TemperatureViewModel viewModel;
    private TemperatureResultAdapter adapter;

    private EditText etValue;
    private TextView tvUnit;
    private CheckBox cbLineResistance;
    private EditText etLineResistance;
    private RecyclerView rvResults;

    private String mode; // "MV" или "OHM"
    private EditText etColdJunction;// ← добавить поле
    private View tilColdJunction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        // ✅ ПОЛУЧАЕМ РЕЖИМ ИЗ INTENT
        mode = getIntent().getStringExtra("MODE");
        if (mode == null) {
            mode = "OHM"; // значение по умолчанию
        }

        viewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new TemperatureViewModel(
                                ((MyApp) getApplication()).getRepository()
                        );
                    }
                }
        ).get(TemperatureViewModel.class);

        initViews();
        setupRecyclerView();
        setupListeners();
        setupObservers();
    }

    private void initViews() {
        etValue = findViewById(R.id.etValue);
        tvUnit = findViewById(R.id.tvUnit);
        cbLineResistance = findViewById(R.id.cbLineResistance);
        etLineResistance = findViewById(R.id.etLineResistance);
        rvResults = findViewById(R.id.rvResults);
        etColdJunction = findViewById(R.id.etColdJunction);
        tilColdJunction = findViewById(R.id.tilColdJunction);
        Log.d("TEMP_DEBUG", "etColdJunction found: " + (etColdJunction != null));// ← ДОБАВИТЬ!

        // ✅ НАСТРАИВАЕМ UI В ЗАВИСИМОСТИ ОТ РЕЖИМА
        if ("MV".equals(mode)) {
            tvUnit.setText("мВ");
            etValue.setHint("Введите значение в мВ");
            cbLineResistance.setVisibility(View.GONE);
            etLineResistance.setVisibility(View.GONE);
            tilColdJunction.setVisibility(View.VISIBLE);   // ← ПОКАЗЫВАЕМ холодный спай
            etColdJunction.setText("");
        } else {
            tvUnit.setText("Ом");
            etValue.setHint("Введите значение в Ом");
            cbLineResistance.setVisibility(View.VISIBLE);
            etLineResistance.setVisibility(View.VISIBLE);
            tilColdJunction.setVisibility(View.GONE);//← СКРЫВАЕМ холодный спай
            cbLineResistance.setChecked(true);
            etLineResistance.setEnabled(true);
            etLineResistance.setText("");
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnHelp).setOnClickListener(v -> {
            Toast.makeText(this, "Справка по замерам температуры", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnSavedMeasurements).setOnClickListener(v -> {
            Intent intent = new Intent(this, SavedMeasurementsActivity.class);
            startActivity(intent);
        });
    }
    private void setupRecyclerView() {
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TemperatureResultAdapter();
        rvResults.setAdapter(adapter);
    }

    private void setupListeners() {
        etValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculate();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etLineResistance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculate();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        cbLineResistance.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etLineResistance.setEnabled(isChecked);
            calculate();
        });
        etColdJunction.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TEMP_DEBUG", "etColdJunction text changed: '" + s.toString() + "'");
                calculate();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupObservers() {
        viewModel.getResults().observe(this, results -> {
            adapter.setItems(results);
        });
    }



    // В calculate():
    private void calculate() {
        String valueStr = etValue.getText().toString().trim();
        Log.d("TEMP_DEBUG", "=== calculate() START ===");
        Log.d("TEMP_DEBUG", "valueStr = '" + valueStr + "'");
        Log.d("TEMP_DEBUG", "mode = " + mode);

        if (valueStr.isEmpty()) {
            Log.d("TEMP_DEBUG", "valueStr is empty, clearing results");
            viewModel.clearResults();
            return;
        }

        double value = parseDouble(valueStr);
        Log.d("TEMP_DEBUG", "parsed value = " + value);

        if (value == 0 && !valueStr.equals("0") && !valueStr.equals("0.0")) {
            Log.d("TEMP_DEBUG", "parsing error, value=0 but string is not '0'");
            viewModel.clearResults();
            Toast.makeText(this, "Некорректный ввод", Toast.LENGTH_SHORT).show();
            return;
        }

        double coldJunctionTemp = -999;  // ← флаг "НЕ ЗАДАНО"
        double lineResistance = 0;

        if ("MV".equals(mode)) {
            String coldStr = etColdJunction.getText().toString().trim();
            Log.d("TEMP_DEBUG", "coldStr = '" + coldStr + "'");
            if (!coldStr.isEmpty()) {
                coldJunctionTemp = parseDouble(coldStr);
                Log.d("TEMP_DEBUG", "parsed coldJunctionTemp = " + coldJunctionTemp);
            } else {
                Log.d("TEMP_DEBUG", "coldStr is empty, using flag -999 (not set)");
            }
        }

        if ("OHM".equals(mode) && cbLineResistance.isChecked()) {
            String lineStr = etLineResistance.getText().toString().trim();
            Log.d("TEMP_DEBUG", "lineStr = '" + lineStr + "'");
            if (!lineStr.isEmpty()) {
                lineResistance = parseDouble(lineStr);
                Log.d("TEMP_DEBUG", "parsed lineResistance = " + lineResistance);
            }
        }

        Log.d("TEMP_DEBUG", "FINAL: value=" + value + ", mode=" + mode +
                ", coldJunctionTemp=" + coldJunctionTemp +
                ", lineResistance=" + lineResistance);

        viewModel.calculate(value, mode, coldJunctionTemp, lineResistance);
        Log.d("TEMP_DEBUG", "=== calculate() END ===");
    }
    private double parseDouble(String value) {
        if (value == null || value.isEmpty()) return 0;
        value = value.replace(',', '.');
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}