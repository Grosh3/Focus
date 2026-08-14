package com.mikesuvade.focus.ui.list;

import android.os.Bundle;
import android.text.InputType;
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
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.ValveItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ListDetailActivity extends AppCompatActivity {

    private ListDetailViewModel viewModel;
    private ValveItemAdapter leftAdapter;
    private ValveItemAdapter rightAdapter;

    private TextView tvListTitle;
    private TextView tvCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_detail);

        // Инициализация ViewModel
        viewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new ListDetailViewModel(
                                ((MyApp) getApplication()).getRepository()
                        );
                    }
                }
        ).get(ListDetailViewModel.class);

        initViews();
        setupRecyclerViews();
        setupObservers();
        setupListeners();

        // Загружаем данные из Intent
        ArrayList<GateValve> valves = null;
        try {
            valves = (ArrayList<GateValve>) getIntent().getSerializableExtra("valve_list");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (valves != null && !valves.isEmpty()) {
            viewModel.loadFromGateValves(valves);
        } else {
            Toast.makeText(this, R.string.list_empty, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvListTitle = findViewById(R.id.tvListTitle);
        tvCount = findViewById(R.id.tvCount);

        tvListTitle.setText(getString(R.string.list_title));

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.btnAssemble).setOnClickListener(v -> viewModel.assembleAll());
        findViewById(R.id.btnDisassemble).setOnClickListener(v -> viewModel.disassembleAll());
    }

    private void setupRecyclerViews() {
        // Левый список (СОБРАТЬ)
        RecyclerView rvLeft = findViewById(R.id.rvLeft);
        rvLeft.setLayoutManager(new LinearLayoutManager(this));
        leftAdapter = new ValveItemAdapter(this);
        leftAdapter.setListener(new ValveItemAdapter.OnItemClickListener() {
            @Override
            public void onMoveClick(ValveItem item) {
                viewModel.moveItem(item, true);
            }

            @Override
            public void onMotorClick(ValveItem item) {
                viewModel.toggleMotor(item);
            }

            @Override
            public void onBoxClick(ValveItem item) {
                viewModel.toggleBox(item);
            }

            @Override
            public void onCheckedClick(ValveItem item) {
                viewModel.toggleChecked(item);
            }
        });
        rvLeft.setAdapter(leftAdapter);

        // Правый список (РАЗОБРАТЬ)
        RecyclerView rvRight = findViewById(R.id.rvRight);
        rvRight.setLayoutManager(new LinearLayoutManager(this));
        rightAdapter = new ValveItemAdapter(this);
        rightAdapter.setListener(new ValveItemAdapter.OnItemClickListener() {
            @Override
            public void onMoveClick(ValveItem item) {
                viewModel.moveItem(item, false);
            }

            @Override
            public void onMotorClick(ValveItem item) {
                viewModel.toggleMotor(item);
            }

            @Override
            public void onBoxClick(ValveItem item) {
                viewModel.toggleBox(item);
            }

            @Override
            public void onCheckedClick(ValveItem item) {
                viewModel.toggleChecked(item);
            }
        });
        rvRight.setAdapter(rightAdapter);
    }

    private void setupObservers() {
        viewModel.getLeftList().observe(this, items -> {
            leftAdapter.setItems(items);
        });

        viewModel.getRightList().observe(this, items -> {
            rightAdapter.setItems(items);
        });

        viewModel.getTotalCount().observe(this, count -> {
            tvCount.setText(String.valueOf(count));
        });

        viewModel.getListName().observe(this, name -> {
            tvListTitle.setText(name);
        });
    }

    private void setupListeners() {
        // Дополнительные слушатели
    }

    private void showSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_save_title);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(getString(R.string.dialog_save_hint));
        builder.setView(input);

        builder.setPositiveButton(R.string.dialog_save_positive, (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                name = getString(R.string.default_list_name) + " " +
                        new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
            }
            viewModel.setListName(name);
            viewModel.saveSession(name);
            Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });

        builder.setNegativeButton(R.string.dialog_save_negative, (dialog, which) -> {
            dialog.cancel();
        });

        builder.show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_exit_title)
                .setMessage(R.string.dialog_exit_message)
                .setPositiveButton(R.string.dialog_exit_positive, (dialog, which) -> {
                    setResult(RESULT_CANCELED);
                    ListDetailActivity.super.onBackPressed();
                })
                .setNegativeButton(R.string.dialog_exit_negative, (dialog, which) -> dialog.dismiss())
                .show();
    }
}