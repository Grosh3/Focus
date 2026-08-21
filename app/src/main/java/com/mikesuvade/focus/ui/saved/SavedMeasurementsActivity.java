package com.mikesuvade.focus.ui.saved;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
    private RecyclerView rvSavedMeasurements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_measurements);

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
        rvSavedMeasurements = findViewById(R.id.rvSavedMeasurements);
        tvEmpty = findViewById(R.id.tvEmpty);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvSavedMeasurements.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SavedMeasurementsAdapter();
        adapter.setListener(new SavedMeasurementsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Measurement measurement) {
                // Можно открыть детальный просмотр или ничего не делать
                Toast.makeText(SavedMeasurementsActivity.this,
                        measurement.getDescription(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(Measurement measurement) {
                showItemOptionsDialog(measurement);
            }
        });
        rvSavedMeasurements.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getMeasurements().observe(this, measurements -> {
            if (measurements != null && !measurements.isEmpty()) {
                adapter.setItems(measurements);
                rvSavedMeasurements.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                rvSavedMeasurements.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    // ==========================================
    // ДИАЛОГИ
    // ==========================================

    private void showItemOptionsDialog(Measurement measurement) {
        String[] options = {
                getString(R.string.saved_measurements_options_rename),
                getString(R.string.saved_measurements_options_delete)
        };

        new AlertDialog.Builder(this)
                .setTitle(measurement.getDescription())
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
        String currentDesc = measurement.getDescription();
        input.setText(currentDesc != null ? currentDesc : "");
        input.setSelection(input.getText().length());
        builder.setView(input);

        builder.setPositiveButton(R.string.saved_measurements_rename_positive, (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                viewModel.renameMeasurement(measurement.getId(), newName);
                Toast.makeText(this, R.string.saved_measurements_renamed, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.saved_measurements_rename_negative, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showDeleteDialog(Measurement measurement) {
        String desc = measurement.getDescription();
        if (desc == null || desc.isEmpty()) {
            desc = measurement.getSensorType() + " " + measurement.getValue() + measurement.getUnit();
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.saved_measurements_delete_title)
                .setMessage(getString(R.string.saved_measurements_delete_message, desc))
                .setPositiveButton("Да", (dialog, which) -> {
                    viewModel.deleteMeasurement(measurement.getId());
                    Toast.makeText(this, R.string.saved_measurements_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Нет", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadMeasurements();
    }
}