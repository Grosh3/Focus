package com.mikesuvade.focus.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.repository.IRepository;
import com.mikesuvade.focus.ui.detail.DetailActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // UI
    private RecyclerView rvGateValves;
    private EditText etSearch;
    private TextView tvListStatus;
    private TextView tvEmptySearch;
    private Button btnNewValve;
    private Button btnToList;
    private Button btnHelp;
    private Button btnLists;
    private Button btnSettings;
    private Button btnSetpoints;
    private Button btnSensors;
    private Button btnConverter;
    private View bottomButtons;

    // Adapter and ViewModel
    private GateValveAdapter adapter;
    private MainViewModel viewModel;

    // Состояние разворачивания (храним позиции)
    private final List<Integer> expandedPositions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupViewModel();
        setupRecyclerView();
        setupSearch();
        setupListeners();
        setupObservers();
    }

    private void initViews() {
        rvGateValves = findViewById(R.id.rvGateValves);
        etSearch = findViewById(R.id.etSearch);
        tvListStatus = findViewById(R.id.tvListStatus);
        tvEmptySearch = findViewById(R.id.tvEmptySearch);
        btnNewValve = findViewById(R.id.btnNewValve);
        btnToList = findViewById(R.id.btnToList);
        btnHelp = findViewById(R.id.btnHelp);
        btnLists = findViewById(R.id.btnLists);
        btnSettings = findViewById(R.id.btnSettings);
        btnSetpoints = findViewById(R.id.btnSetpoints);
        btnSensors = findViewById(R.id.btnSensors);
        btnConverter = findViewById(R.id.btnConverter);
        bottomButtons = findViewById(R.id.bottomButtons);
    }

    private void setupViewModel() {
        IRepository repository = ((MyApp) getApplication()).getRepository();

        viewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.Factory() {
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(Class<T> modelClass) {
                        return (T) new MainViewModel(repository);
                    }
                }
        ).get(MainViewModel.class);
    }

    private void setupRecyclerView() {
        // Настройка RecyclerView
        rvGateValves.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GateValveAdapter();

        // ==========================================
        // 🧹 СКРЫВАЕМ КЛАВИАТУРУ ПРИ ПРОКРУТКЕ
        // ==========================================
        rvGateValves.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard();
                }
            }
        });

        // Клик по элементу - развернуть/свернуть
        adapter.setOnItemClickListener((valve, position) -> {
            toggleExpanded(position);
        });

        // Длинный клик - редактировать
        adapter.setOnItemLongClickListener(valve -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("valve_id", valve.getId());
            startActivity(intent);
           // return true;
        });

        // Кнопка + добавить в список
        adapter.setOnAddToListClickListener(valve -> {
            if (viewModel.isInCurrentList(valve)) {
                Toast.makeText(this, "Уже в списке", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.addToCurrentList(valve);
            Toast.makeText(this, "Добавлено в список: " + valve.getName(), Toast.LENGTH_SHORT).show();
        });

        // Блокировки
        adapter.setOnBlockingClickListener((valve, type) -> {
            String message = "Блокировка: " + type + " для " + valve.getName();
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            // TODO: Открыть диалог с BLOB и описанием
        });

        rvGateValves.setAdapter(adapter);
    }

    private void toggleExpanded(int position) {
        if (expandedPositions.contains(position)) {
            expandedPositions.remove(Integer.valueOf(position));
            adapter.setExpanded(position, false);
        } else {
            expandedPositions.add(position);
            adapter.setExpanded(position, true);
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    viewModel.search(query);
                    bottomButtons.setVisibility(View.GONE);
                } else if (query.isEmpty()) {
                    viewModel.search("");
                    bottomButtons.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupListeners() {
        // Новая задвижка
        btnNewValve.setOnClickListener(v -> {
            Toast.makeText(this, "Открыть создание новой задвижки", Toast.LENGTH_SHORT).show();
            // TODO: Открыть DetailActivity с пустыми полями
        });

        // К списку
        btnToList.setOnClickListener(v -> {
            if (viewModel.getCurrentListSize() == 0) {
                Toast.makeText(this, "Список пуст", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: Открыть CurrentListActivity
            Toast.makeText(this, "Открыть список (" + viewModel.getCurrentListSize() + " шт.)", Toast.LENGTH_SHORT).show();
        });

        // Списки
        btnLists.setOnClickListener(v -> {
            Toast.makeText(this, "Открыть сохранённые списки", Toast.LENGTH_SHORT).show();
            // TODO: Открыть SavedListsActivity
        });

        // Справка
        btnHelp.setOnClickListener(v -> {
            Toast.makeText(this, "Открыть справку", Toast.LENGTH_SHORT).show();
            // TODO: Открыть HelpActivity
        });

        // Настройки
        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Открыть настройки", Toast.LENGTH_SHORT).show();
            // TODO: Открыть SettingsActivity
        });

        // Нижние кнопки
        btnSetpoints.setOnClickListener(v -> {
            Toast.makeText(this, "Открыть журнал уставок", Toast.LENGTH_SHORT).show();
            // TODO: Открыть SetpointActivity
        });

        btnSensors.setOnClickListener(v -> {
            Toast.makeText(this, "Открыть список датчиков", Toast.LENGTH_SHORT).show();
            // TODO: Открыть SensorActivity
        });

        btnConverter.setOnClickListener(v -> {
            Toast.makeText(this, "Открыть замер температуры", Toast.LENGTH_SHORT).show();
            // TODO: Открыть ConverterActivity
        });
    }

    // ==================== ЕДИНСТВЕННЫЙ МЕТОД setupObservers ====================
    private void setupObservers() {
        // Список задвижек + пустое состояние
        viewModel.getGateValves().observe(this, valves -> {
            if (valves != null && !valves.isEmpty()) {
                adapter.setValves(valves);
                rvGateValves.setVisibility(View.VISIBLE);
                tvEmptySearch.setVisibility(View.GONE);
                expandedPositions.clear();
            } else {
                adapter.setValves(new ArrayList<>());
                rvGateValves.setVisibility(View.GONE);
                tvEmptySearch.setVisibility(View.VISIBLE);
            }
        });

        // Статус записи
        viewModel.getIsRecording().observe(this, isRecording -> {
            if (isRecording) {
                btnNewValve.setVisibility(View.GONE);
                btnToList.setVisibility(View.VISIBLE);
                tvListStatus.setVisibility(View.VISIBLE);
            } else {
                btnNewValve.setVisibility(View.VISIBLE);
                btnToList.setVisibility(View.GONE);
                tvListStatus.setVisibility(View.GONE);
            }
        });

        // Имя списка
        viewModel.getListName().observe(this, name -> {
            if (name != null) {
                tvListStatus.setText(name);
            }
        });

        // Размер списка
        viewModel.getCurrentListSizeLiveData().observe(this, size -> {
            // Можно обновить баннер или что-то ещё
        });
    }

    // ==========================================
    // 🧹 СКРЫТИЕ КЛАВИАТУРЫ
    // ==========================================
    private void hideKeyboard() {
        if (etSearch != null && etSearch.getWindowToken() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем данные при возврате
        viewModel.refreshData();
    }
}