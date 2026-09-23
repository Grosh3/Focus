package com.mikesuvade.focus.ui.saved;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.Measurement;

public class SavedMeasurementsActivity extends AppCompatActivity {

    private SavedMeasurementsViewModel viewModel;
    private SavedMeasurementsAdapter adapter;
    private TextView tvEmpty;
    private RecyclerView rvMeasurements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_measurements);

        // 🔥 Системные бары + отступ от статус-бара
        setStatusBarAndNavigationIconsDark(true);
        fixTopPanelPadding();
        fixRecyclerViewBottomPadding();
        viewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new SavedMeasurementsViewModel(
                                ((MyApp) getApplication()).getRepository()
                        );
                    }
                }
        ).get(SavedMeasurementsViewModel.class);

        initViews();
        setupRecyclerView();
        setupObservers();

        viewModel.loadMeasurements();
    }

    private void initViews() {
        rvMeasurements = findViewById(R.id.rvSavedMeasurements);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void setupRecyclerView() {
        rvMeasurements.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SavedMeasurementsAdapter();
        adapter.setListener(new SavedMeasurementsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Measurement measurement) {
                showMeasurementInfoDialog(measurement);
            }

            @Override
            public void onItemLongClick(Measurement measurement) {
                showItemOptionsDialog(measurement);
            }
        });
        rvMeasurements.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getMeasurements().observe(this, measurements -> {
            if (measurements != null && !measurements.isEmpty()) {
                adapter.setItems(measurements);
                rvMeasurements.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                rvMeasurements.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showMeasurementInfoDialog(Measurement measurement) {
        StringBuilder info = new StringBuilder();
        info.append("Дата: ").append(measurement.getMeasurementDate()).append("\n");
        info.append("Температура: ").append(measurement.getTemperature()).append(" °C\n");
        info.append("Тип датчика: ").append(measurement.getSensorType()).append("\n");

        if (measurement.getSensorType() != null && measurement.getSensorType().equals("MV")) {
            info.append("Значение: ").append(measurement.getInputValue()).append(" мВ\n");
            info.append("Холодный спай: ").append(measurement.getColdJunctionMv()).append(" мВ\n");
        } else {
            info.append("Значение: ").append(measurement.getInputValue()).append(" Ом\n");
            info.append("Сопротивление линии: ").append(measurement.getLineResistance()).append(" Ом\n");
        }

        if (measurement.getDescription() != null && !measurement.getDescription().isEmpty()) {
            info.append("Описание: ").append(measurement.getDescription());
        }

        new AlertDialog.Builder(this)
                .setTitle("Информация о замере")
                .setMessage(info.toString().trim())
                .setPositiveButton("Закрыть", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showItemOptionsDialog(Measurement measurement) {
        String[] options = {"Переименовать", "Удалить"};

        new AlertDialog.Builder(this)
                .setTitle(measurement.getDescription() != null ? measurement.getDescription() : "Замер")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showRenameDialog(measurement);
                    } else {
                        showDeleteDialog(measurement);
                    }
                })
                .show();
    }

    private void showRenameDialog(Measurement measurement) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.saved_measurements_rename_title);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(measurement.getDescription() != null ? measurement.getDescription() : "");
        input.setSelection(input.getText().length());
        builder.setView(input);

        builder.setPositiveButton(R.string.saved_measurements_rename_positive, (dialog, which) -> {
            String newDescription = input.getText().toString().trim();
            viewModel.renameMeasurement(measurement.getId(), newDescription);
            Toast.makeText(this, R.string.saved_measurements_renamed, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton(R.string.saved_measurements_rename_negative, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showDeleteDialog(Measurement measurement) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.saved_measurements_delete_title)
                .setMessage(getString(R.string.saved_measurements_delete_message,
                        measurement.getDescription() != null ? measurement.getDescription() : "Замер"))
                .setPositiveButton("Да", (dialog, which) -> {
                    viewModel.deleteMeasurement(measurement.getId());
                    Toast.makeText(this, R.string.saved_measurements_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Нет", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void fixTopPanelPadding() {
        View topPanel = findViewById(R.id.topPanel);
        if (topPanel == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(topPanel, (view, windowInsets) -> {
            int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

            view.setPadding(
                    view.getPaddingLeft(),
                    statusBarHeight + view.getPaddingTop(),
                    view.getPaddingRight(),
                    view.getPaddingBottom()
            );

            ViewCompat.setOnApplyWindowInsetsListener(view, null);
            return windowInsets;
        });
    }

    private void setStatusBarAndNavigationIconsDark(boolean dark) {
        Window window = getWindow();
        if (window != null) {
            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(window, window.getDecorView());
            controller.setAppearanceLightStatusBars(dark);
            controller.setAppearanceLightNavigationBars(dark);
        }
    }
    private void fixRecyclerViewBottomPadding() {
        RecyclerView rv = findViewById(R.id.rvSavedMeasurements);
        if (rv == null) return;

        rv.setClipToPadding(false);

        ViewCompat.setOnApplyWindowInsetsListener(rv, (v, insets) -> {
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            int extraPadding = (int) (16 * getResources().getDisplayMetrics().density);

            rv.setPadding(
                    rv.getPaddingLeft(),
                    rv.getPaddingTop(),
                    rv.getPaddingRight(),
                    navBarHeight + extraPadding
            );

            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new android.os.Handler().postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) {
                viewModel.loadMeasurements();
            }
        }, 300);
    }
}