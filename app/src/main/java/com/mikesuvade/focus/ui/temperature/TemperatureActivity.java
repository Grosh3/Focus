package com.mikesuvade.focus.ui.temperature;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.Measurement;
import com.mikesuvade.focus.domain.models.TemperatureResult;
import com.mikesuvade.focus.ui.saved.SavedMeasurementsActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TemperatureActivity extends AppCompatActivity {

    private TemperatureViewModel viewModel;
    private TemperatureResultAdapter adapter;

    private EditText etValue;
    private TextView tvUnit;
    private CheckBox cbLineResistance;
    private EditText etLineResistance;
    private RecyclerView rvResults;

    private String mode; // "MV" или "OHM"
    private EditText etColdJunction;
    private View tilColdJunction;
    private TextView tvColdJunctionUnit;
    private View rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        mode = getIntent().getStringExtra("MODE");
        if (mode == null) {
            mode = "OHM";
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
        setupHideKeyboardOnTouch();
    }

    private void initViews() {
        etValue = findViewById(R.id.etValue);
        tvUnit = findViewById(R.id.tvUnit);
        cbLineResistance = findViewById(R.id.cbLineResistance);
        etLineResistance = findViewById(R.id.etLineResistance);
        rvResults = findViewById(R.id.rvResults);
        etColdJunction = findViewById(R.id.etColdJunction);
        tilColdJunction = findViewById(R.id.tilColdJunction);
        tvColdJunctionUnit = findViewById(R.id.tvColdJunctionUnit);
        rootLayout = findViewById(R.id.rootLayout);

        Log.d("TEMP_DEBUG", "etColdJunction found: " + (etColdJunction != null));

        if ("MV".equals(mode)) {
            tvUnit.setText("мВ");
            etValue.setHint("Введите значение в мВ");
            cbLineResistance.setVisibility(View.GONE);
            etLineResistance.setVisibility(View.GONE);
            tilColdJunction.setVisibility(View.VISIBLE);
            tvColdJunctionUnit.setVisibility(View.VISIBLE);
            etColdJunction.setHint("Т холодного спая");
            etColdJunction.setText("");
        } else {
            tvUnit.setText("Ом");
            etValue.setHint("Введите значение в Ом");
            cbLineResistance.setVisibility(View.VISIBLE);
            etLineResistance.setVisibility(View.VISIBLE);
            tilColdJunction.setVisibility(View.GONE);
            tvColdJunctionUnit.setVisibility(View.GONE);
            cbLineResistance.setChecked(true);
            etLineResistance.setEnabled(true);
            etLineResistance.setText("");
            etLineResistance.setHint("Напр. 0.15");
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

    private void setupHideKeyboardOnTouch() {
        if (rootLayout != null) {
            rootLayout.setOnTouchListener((v, event) -> {
                hideKeyboard();
                return false;
            });
        }
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                view.clearFocus();
            }
        }
    }

    private void setupRecyclerView() {
        rvResults.setLayoutManager(new LinearLayoutManager(this));

        // ✅ СКРЫВАЕМ КЛАВИАТУРУ ПРИ СКРОЛЛЕ
        rvResults.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard();
                }
            }
        });

        adapter = new TemperatureResultAdapter();
        adapter.setOnSaveClickListener(this::showSaveDialog);
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

        if (valueStr.length() > 0 && value == 0 && !valueStr.matches("0(\\.0*)?")) {
            Log.d("TEMP_DEBUG", "parsing error, invalid number format");
            viewModel.clearResults();
            Toast.makeText(this, "Некорректный ввод", Toast.LENGTH_SHORT).show();
            return;
        }

        double coldJunctionTemp = -999;
        double lineResistance = 0;

        if ("MV".equals(mode)) {
            String coldStr = etColdJunction.getText().toString().trim();
            Log.d("TEMP_DEBUG", "coldStr = '" + coldStr + "'");
            if (!coldStr.isEmpty()) {
                coldJunctionTemp = parseDouble(coldStr);
                Log.d("TEMP_DEBUG", "parsed coldJunctionTemp = " + coldJunctionTemp);
            } else {
                Log.d("TEMP_DEBUG", "coldStr is empty, using flag -999");
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

    // ==========================================
    // 💾 СОХРАНЕНИЕ ЗАМЕРА
    // ==========================================

    private void showSaveDialog(TemperatureResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Сохранить замер");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);

        final EditText inputDescription = new EditText(this);
        inputDescription.setHint("Описание (необязательно)");
        inputDescription.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(inputDescription);

        TextView info = new TextView(this);
        String infoText = result.getSensorName() + "   " +
                result.getUserValue() + " " + result.getUnit() + " → " +
                String.format("%.2f", result.getTemperature()) + " °C";
        info.setText(infoText);
        info.setPadding(0, 24, 0, 0);
        info.setTextSize(14);
        layout.addView(info);

        builder.setView(layout);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String description = inputDescription.getText().toString().trim();
            if (description.isEmpty()) {
                description = result.getSensorName() + " " + result.getUserValue() + result.getUnit();
            }
            saveMeasurement(result, description);
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void saveMeasurement(TemperatureResult result, String description) {
        Measurement measurement = new Measurement();
        measurement.setSensorType(result.getSensorName());
        measurement.setInputValue(result.getUserValue());
        measurement.setValue(result.getUserValue());  // для совместимости
        measurement.setUnit(result.getUnit());

        double roundedTemperature = Math.round(result.getTemperature() * 100.0) / 100.0;
        measurement.setTemperature(roundedTemperature);
        measurement.setDescription(description);

        // ✅ ДЛЯ ТЕРМОПАР
        if ("мВ".equals(result.getUnit())) {
            measurement.setColdJunctionMv(result.getColdJunctionMv());
            measurement.setLineResistance(0);
        } else {
            // ✅ ДЛЯ ТЕРМОСОПРОТИВЛЕНИЙ
            double lineResistance = result.getUserValue() - result.getCorrectedValue();
            measurement.setColdJunctionMv(0);
            measurement.setLineResistance(lineResistance > 0 ? lineResistance : 0);
        }

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        measurement.setMeasurementDate(currentDate);
        measurement.setCreatedAt(currentDateTime);

        new Thread(() -> {
            try {
                long id = ((MyApp) getApplication()).getRepository().insertMeasurement(measurement);
                runOnUiThread(() -> {
                    if (id > 0) {
                        Toast.makeText(this, "Замер сохранён!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("TEMP_DEBUG", "ERROR saving measurement", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

}