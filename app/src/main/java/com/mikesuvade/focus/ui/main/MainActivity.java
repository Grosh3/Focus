package com.mikesuvade.focus.ui.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.google.android.material.textfield.TextInputLayout;
import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.ValveItem;
import com.mikesuvade.focus.domain.models.ValveWorkSession;
import com.mikesuvade.focus.domain.repository.IRepository;
import com.mikesuvade.focus.ui.detail.DetailActivity;
import com.mikesuvade.focus.ui.list.ListDetailActivity;
import com.mikesuvade.focus.ui.saved.SavedListsActivity;
import com.mikesuvade.focus.ui.saved.SavedMeasurementsActivity;
import com.mikesuvade.focus.ui.temperature.TemperatureActivity;
import com.mikesuvade.focus.utils.AppState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.mikesuvade.focus.domain.models.Sensor;
import com.mikesuvade.focus.domain.models.Setpoint;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int MODE_MAIN = 0;
    private static final int MODE_SENSORS = 1;
    private static final int MODE_SETPOINTS = 2;
    private int currentMode = MODE_MAIN;



    // UI элементы
    private ConstraintLayout rootLayout;
    private View topPanel;
    private ConstraintLayout searchContainer;
    private RecyclerView rvGateValves;
    private EditText etSearch;
    private TextView tvEmptySearch;
    private Button btnNewValve;

    private Button btnHelp;
    private Button btnLists;
    private Button btnSettings;
    private Button btnSetpoints;
    private Button btnSensors;
    private Button btnConverter;
    private View bottomButtons;

    // Оверлей
    private ConstraintLayout overlayDetail;
    private ImageButton btnOverlayBack;
    private TextView tvOverlayTitle;
    private ImageView ivOverlayImage;
    private TextView tvOverlayDescription;
    private ProgressBar progressOverlay;
    private com.google.android.material.card.MaterialCardView cardOverlayDescription;

    // Адаптер и ViewModel
    private GateValveAdapter adapter;
    private SensorAdapter sensorAdapter;
    private SetpointAdapter setpointAdapter;
    private MainViewModel viewModel;

    // Списки для хранения состояния раскрытия элементов
    private final List<Integer> expandedPositions = new ArrayList<>();
    private boolean isSearchActive = false;
    private int statusBarHeightPx = 0;
    private int navigationBarHeight = 0;

    // ==========================================
    // 🔥 ACTIVITY RESULT API
    // ==========================================
    private final ActivityResultLauncher<Intent> detailResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    refreshData();
                }
            }
    );

    private final ActivityResultLauncher<Intent> listDetailResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, "Список сохранён!", Toast.LENGTH_SHORT).show();
                } else {
                    if (!AppState.getInstance().hasActiveSession()) {
                        AppState.getInstance().clearSession();
                        viewModel.clearCurrentList();
                        Toast.makeText(this, "Список не сохранён", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Возврат к списку", Toast.LENGTH_SHORT).show();
                    }
                }
                refreshData();
                updateButtonState();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initViews();

        setupNavigationBarPadding();
        setupStatusBarPadding();
        fixTopPanelPadding();
        setStatusBarIconsDark(true);

        setupViewModel();
        setupRecyclerView();
        setupSearch();
        setupListeners();
        setupObservers();
        setupEmptySearchKeyboardPadding();
        updateSearchHint();
    }

    private void initViews() {
        rootLayout = findViewById(R.id.rootLayout);
        topPanel = findViewById(R.id.topPanel);
        searchContainer = findViewById(R.id.searchContainer);
        rvGateValves = findViewById(R.id.rvGateValves);
        etSearch = findViewById(R.id.etSearch);
        tvEmptySearch = findViewById(R.id.tvEmptySearch);
        btnNewValve = findViewById(R.id.btnNewValve);

        btnHelp = findViewById(R.id.btnHelp);
        btnLists = findViewById(R.id.btnLists);
        btnSettings = findViewById(R.id.btnSettings);
        btnSetpoints = findViewById(R.id.btnSetpoints);
        btnSensors = findViewById(R.id.btnSensors);
        btnConverter = findViewById(R.id.btnConverter);
        bottomButtons = findViewById(R.id.bottomButtons);

        overlayDetail = findViewById(R.id.overlayDetail);
        btnOverlayBack = findViewById(R.id.btnOverlayBack);
        tvOverlayTitle = findViewById(R.id.tvOverlayTitle);
        ivOverlayImage = findViewById(R.id.ivOverlayImage);
        tvOverlayDescription = findViewById(R.id.tvOverlayDescription);
        progressOverlay = findViewById(R.id.progressOverlay);
        cardOverlayDescription = findViewById(R.id.cardOverlayDescription);

        // 🔥 ИНИЦИАЛИЗАЦИЯ АДАПТЕРОВ
        sensorAdapter = new SensorAdapter();
        setpointAdapter = new SetpointAdapter();

        // ✅ ДОЛГИЙ ТАП ПО КНОПКЕ
        btnNewValve.setOnLongClickListener(v -> {
            if (AppState.getInstance().hasActiveSession()) {
                showCloseListDialog();
                return true;
            } else if (viewModel.getIsRecording().getValue() != null && viewModel.getIsRecording().getValue()) {
                showClearSelectionDialog();
                return true;
            }
            return false;
        });
    }
    private void setupViewModel() {
        IRepository repository = ((MyApp) getApplication()).getRepository();

        viewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new MainViewModel(repository);
                    }
                }
        ).get(MainViewModel.class);
    }

    // ==========================================
    // 📋 ДОЛГИЙ ТАП ПО КНОПКЕ
    // ==========================================

    private void showCloseListDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_close_list_title)
                .setPositiveButton(R.string.dialog_close_list_positive, (dialog, which) -> {
                    closeCurrentList();
                })
                .setNegativeButton(R.string.dialog_close_list_negative, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void closeCurrentList() {
        Log.d("MAIN_DEBUG", "=== closeCurrentList ===");

        AppState.getInstance().clearSession();
        viewModel.clearCurrentList();
        viewModel.setRecording(false);

        updateButtonState();
        syncAdapterSelection();

        Toast.makeText(this, R.string.toast_list_closed, Toast.LENGTH_SHORT).show();
    }

    private void showClearSelectionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_clear_selection_title)
                .setPositiveButton(R.string.dialog_clear_selection_positive, (dialog, which) -> {
                    clearSelection();
                })
                .setNegativeButton(R.string.dialog_clear_selection_negative, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void clearSelection() {
        Log.d("MAIN_DEBUG", "=== clearSelection ===");

        viewModel.clearCurrentList();
        viewModel.setRecording(false);

        updateButtonState();
        syncAdapterSelection();

        Toast.makeText(this, R.string.toast_selection_cleared, Toast.LENGTH_SHORT).show();
    }

    private void setupRecyclerView() {
        rvGateValves.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GateValveAdapter();
        sensorAdapter = new SensorAdapter();
        setpointAdapter = new SetpointAdapter();

        rvGateValves.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard();
                }
            }
        });

        // ==========================================
        // 1️⃣ АДАПТЕР ЗАДВИЖЕК
        // ==========================================
        adapter.setOnItemClickListener((valve, position) -> {
            toggleExpanded(position);
        });

        adapter.setOnItemLongClickListener(valve -> {
            String displayName = valve.getIsy();
            if (displayName == null || displayName.isEmpty()) {
                displayName = valve.getName();
            }
            if (displayName == null || displayName.isEmpty()) {
                displayName = "Без названия";
            }

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Редактировать задвижку?")
                    .setMessage("Вы хотите отредактировать \"" + displayName + "\"?")
                    .setPositiveButton("Редактировать", (dialog, which) -> {
                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                        intent.putExtra("valve_id", valve.getId());
                        detailResultLauncher.launch(intent);
                    })
                    .setNegativeButton("Отмена", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
            return true;
        });

        adapter.setOnCheckBoxClickListener((valve, position, isChecked) -> {
            if (isChecked) {
                if (viewModel.isInCurrentList(valve)) {
                    Toast.makeText(this, "Задвижка уже в списке", Toast.LENGTH_SHORT).show();
                    syncAdapterSelection();
                    updateButtonState();
                    return;
                }
                if (AppState.getInstance().hasActiveSession()) {
                    viewModel.addToActiveSession(valve);
                    viewModel.addToCurrentList(valve);
                } else {
                    viewModel.addToCurrentList(valve);
                }
                Toast.makeText(this, "Добавлено: " + valve.getName(), Toast.LENGTH_SHORT).show();
            } else {
                viewModel.removeFromCurrentList(valve);
                Toast.makeText(this, "Удалено: " + valve.getName(), Toast.LENGTH_SHORT).show();
            }
            syncAdapterSelection();
            updateButtonState();
        });

        adapter.setOnBlockingClickListener((valve, type) -> {
            String title = valve.getName() + " - ";
            byte[] imageData = null;
            String description = "";

            switch (type) {
                case "open":
                    title += "БЛОКИРОВКИ \"ОТКРЫТИЕ\"";
                    imageData = valve.getNameSpaceViewOpen();
                    description = valve.getDescriptionBlockingOpen();
                    break;
                case "close":
                    title += "БЛОКИРОВКИ \"ЗАКРЫТИЕ\"";
                    imageData = valve.getNamespaceViewClose();
                    description = valve.getDescriptionBlockingClose();
                    break;
                case "external":
                    title += "ВНЕШНИЕ ЦЕПИ";
                    imageData = valve.getNamespaceViewPerifer();
                    description = valve.getDescriptionBlockingPerifer();
                    break;
            }

            showOverlay(title, imageData, description);
        });

        // ==========================================
        // 2️⃣ АДАПТЕР ДАТЧИКОВ (НОВЫЙ)
        // ==========================================
        sensorAdapter.setOnItemClickListener((sensor, position) -> {
            Log.d("EXPAND_SENSOR", "onItemClick: position=" + position + ", name=" + sensor.getFullName());
            toggleExpanded(position);
        });

        sensorAdapter.setOnItemLongClickListener((sensor, position) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Редактировать датчик?")
                    .setMessage("Вы хотите отредактировать \"" + sensor.getStMarkir() + "\"?")
                    .setPositiveButton("Редактировать", (dialog, which) -> {
                        Toast.makeText(this, "Редактирование датчика", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Удалить", (dialog, which) -> {
                        Toast.makeText(this, "Удаление датчика", Toast.LENGTH_SHORT).show();
                    })
                    .setNeutralButton("Отмена", null)
                    .show();
            return true;
        });

        // ==========================================
        // 3️⃣ АДАПТЕР УСТАВОК (НОВЫЙ)
        // ==========================================
        setpointAdapter.setOnItemClickListener((setpoint, position) -> {
            Log.d("EXPAND_SETPOINT", "onItemClick: position=" + position + ", name=" + setpoint.getName());
            toggleExpanded(position);
        });

        setpointAdapter.setOnItemLongClickListener((setpoint, position) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Редактировать уставку?")
                    .setMessage("Вы хотите отредактировать \"" + setpoint.getPositionName() + "\"?")
                    .setPositiveButton("Редактировать", (dialog, which) -> {
                        Toast.makeText(this, "Редактирование уставки", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Удалить", (dialog, which) -> {
                        Toast.makeText(this, "Удаление уставки", Toast.LENGTH_SHORT).show();
                    })
                    .setNeutralButton("Отмена", null)
                    .show();
            return true;
        });

        // По умолчанию устанавливаем адаптер задвижек
        rvGateValves.setAdapter(adapter);
    }

    private void toggleExpanded(int position) {
        Log.d("EXPAND_1", "=== toggleExpanded START ===");
        Log.d("EXPAND", "position = " + position);
        Log.d("EXPAND", "currentMode = " + currentMode);
        Log.d("EXPAND", "expandedPositions = " + expandedPositions);

        if (expandedPositions.contains(position)) {
            Log.d("EXPAND", "position already expanded, collapsing");
            expandedPositions.remove(Integer.valueOf(position));
            if (currentMode == MODE_SENSORS) {
                Log.d("EXPAND_SENSOR", "collapsing sensor at position " + position);
                sensorAdapter.setExpanded(position, false);
            } else if (currentMode == MODE_SETPOINTS) {
                Log.d("EXPAND_SETPOINT", "collapsing setpoint at position " + position);
                setpointAdapter.setExpanded(position, false);
            } else {
                Log.d("EXPAND_VALVE", "collapsing valve at position " + position);
                adapter.setExpanded(position, false);
            }
            return;
        }

        int previousExpanded = -1;
        if (!expandedPositions.isEmpty()) {
            previousExpanded = expandedPositions.get(0);
            Log.d("EXPAND", "previousExpanded = " + previousExpanded);
        }

        expandedPositions.clear();
        Log.d("EXPAND", "cleared expandedPositions");

        if (previousExpanded != -1 && previousExpanded != position) {
            if (currentMode == MODE_SENSORS) {
                Log.d("EXPAND_SENSOR", "collapsing previous sensor at " + previousExpanded);
                sensorAdapter.setExpanded(previousExpanded, false);
            } else if (currentMode == MODE_SETPOINTS) {
                Log.d("EXPAND_SETPOINT", "collapsing previous setpoint at " + previousExpanded);
                setpointAdapter.setExpanded(previousExpanded, false);
            } else {
                Log.d("EXPAND_VALVE", "collapsing previous valve at " + previousExpanded);
                adapter.setExpanded(previousExpanded, false);
            }
        }

        expandedPositions.add(position);
        Log.d("EXPAND", "added position " + position + " to expandedPositions");

        if (currentMode == MODE_SENSORS) {
            Log.d("EXPAND_SENSOR", "expanding sensor at position " + position);
            sensorAdapter.setExpanded(position, true);
        } else if (currentMode == MODE_SETPOINTS) {
            Log.d("EXPAND_SETPOINT", "expanding setpoint at position " + position);
            setpointAdapter.setExpanded(position, true);
        } else {
            Log.d("EXPAND_VALVE", "expanding valve at position " + position);
            adapter.setExpanded(position, true);
        }
        Log.d("EXPAND", "=== toggleExpanded END ===");
    }

    // ==========================================
    // 🔧 ОВЕРЛЕЙ ДЛЯ ПРОСМОТРА СХЕМЫ
    // ==========================================
    private void showOverlay(String title, byte[] imageData, String description) {
        if (imageData == null || imageData.length == 0) {
            Toast.makeText(this, "Изображение отсутствует", Toast.LENGTH_SHORT).show();
            return;
        }

        hideKeyboard();

        tvOverlayTitle.setText(title);
        tvOverlayDescription.setText(description != null && !description.isEmpty()
                ? description
                : "Описание отсутствует");

        progressOverlay.setVisibility(View.VISIBLE);
        ivOverlayImage.setImageDrawable(null);
        overlayDetail.setVisibility(View.VISIBLE);

        if (cardOverlayDescription != null) {
            float density = getResources().getDisplayMetrics().density;
            int safeNavBarHeight = (navigationBarHeight > 0) ? navigationBarHeight : (int) (80 * density);
            int baseMarginPx = (int) (24 * density);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cardOverlayDescription.getLayoutParams();
            params.bottomMargin = safeNavBarHeight + baseMarginPx;
            cardOverlayDescription.setLayoutParams(params);
        }

        new Thread(() -> {
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                runOnUiThread(() -> {
                    progressOverlay.setVisibility(View.GONE);
                    if (bitmap != null) {
                        ivOverlayImage.setImageBitmap(bitmap);
                    } else {
                        ivOverlayImage.setImageResource(R.drawable.ic_image_placeholder);
                        Toast.makeText(MainActivity.this, "Не удалось загрузить изображение", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Ошибка декодирования схемы блокировки: ", e);
                runOnUiThread(() -> {
                    progressOverlay.setVisibility(View.GONE);
                    ivOverlayImage.setImageResource(R.drawable.ic_image_placeholder);
                    Toast.makeText(MainActivity.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void hideOverlay() {
        overlayDetail.setVisibility(View.GONE);
        ivOverlayImage.setImageBitmap(null);
        ivOverlayImage.setImageDrawable(null);
        progressOverlay.setVisibility(View.GONE);
    }

    // ==========================================
    // 🔧 МЕТОДЫ УПРАВЛЕНИЯ АНИМАЦИЕЙ
    // ==========================================

    private void activateSearchState() {
        isSearchActive = true;

        TransitionManager.beginDelayedTransition(rootLayout);

        bottomButtons.setVisibility(View.GONE);
        rvGateValves.setVisibility(View.VISIBLE);
        rvGateValves.smoothScrollToPosition(0);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(rootLayout);

        constraintSet.clear(R.id.searchContainer, ConstraintSet.BOTTOM);
        int topMargin = (int) (4 * getResources().getDisplayMetrics().density);
        constraintSet.connect(R.id.searchContainer, ConstraintSet.TOP, R.id.topPanel, ConstraintSet.BOTTOM, topMargin);
        constraintSet.setVerticalBias(R.id.searchContainer, 0.0f);

        constraintSet.applyTo(rootLayout);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void deactivateSearchState() {
        // Убираем проверку if (!isSearchActive) return;
        // Чтобы метод всегда выполнялся
        isSearchActive = false;

        TransitionManager.beginDelayedTransition(rootLayout);

        bottomButtons.setVisibility(View.VISIBLE);
        rvGateValves.setVisibility(View.GONE);
        tvEmptySearch.setVisibility(View.GONE);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(rootLayout);

        constraintSet.clear(R.id.searchContainer, ConstraintSet.TOP);
        constraintSet.clear(R.id.searchContainer, ConstraintSet.BOTTOM);
        constraintSet.connect(R.id.searchContainer, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
        constraintSet.connect(R.id.searchContainer, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
        constraintSet.setVerticalBias(R.id.searchContainer, 0.5f);

        constraintSet.applyTo(rootLayout);

        etSearch.setText("");
        etSearch.clearFocus();
        hideKeyboard();
    }

    @Override
    public void onBackPressed() {
        if (overlayDetail.getVisibility() == View.VISIBLE) {
            hideOverlay();
        } else if (currentMode != MODE_MAIN) {
            showMainMode();
            // Принудительно обновляем layout
            rootLayout.requestLayout();
        } else if (isSearchActive) {
            deactivateSearchState();
            rootLayout.requestLayout();
        } else {
            super.onBackPressed();
        }
    }
    // 🔍 ПОИСК И СЛУШАТЕЛИ ВВОДА
// ==========================================

    private void setupSearch() {
        rvGateValves.setVisibility(View.GONE);
        tvEmptySearch.setVisibility(View.GONE);

        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !isSearchActive && currentMode == MODE_MAIN) {
                activateSearchState();
            }
        });

        etSearch.setOnClickListener(v -> {
            if (!isSearchActive && currentMode == MODE_MAIN) {
                activateSearchState();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                if (currentMode == MODE_MAIN) {
                    // Главный режим - поиск задвижек
                    if (query.length() >= 2 || query.equalsIgnoreCase("#все")) {
                        viewModel.search(query);
                    } else {
                        rvGateValves.setVisibility(View.GONE);
                        tvEmptySearch.setVisibility(View.GONE);
                        if (query.isEmpty()) {
                            viewModel.search("");
                        }
                    }
                } else if (currentMode == MODE_SENSORS) {
                    // Режим датчиков
                    if (query.length() >= 2 || query.equalsIgnoreCase("#все")) {
                        searchSensors(query);
                    } else {
                        // 🔥 ПРИ ПУСТОМ ЗАПРОСЕ - ОЧИЩАЕМ СПИСОК
                        sensorAdapter.updateData(new ArrayList<>());
                        rvGateValves.setVisibility(View.VISIBLE);
                        tvEmptySearch.setVisibility(View.GONE);
                    }
                } else if (currentMode == MODE_SETPOINTS) {
                    // Режим уставок
                    if (query.length() >= 2 || query.equalsIgnoreCase("#все")) {
                        searchSetpoints(query);
                    } else {
                        // 🔥 ПРИ ПУСТОМ ЗАПРОСЕ - ОЧИЩАЕМ СПИСОК
                        setpointAdapter.updateData(new ArrayList<>());
                        rvGateValves.setVisibility(View.VISIBLE);
                        tvEmptySearch.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // ==========================================
    // 🔧 НАСТРОЙКА tvEmptySearch ПРИ КЛАВИАТУРЕ
    // ==========================================

    private void setupEmptySearchKeyboardPadding() {
        View rootView = findViewById(android.R.id.content);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);

                int screenHeight = rootView.getHeight();
                int keypadHeight = screenHeight - rect.bottom;

                if (tvEmptySearch != null) {
                    float density = getResources().getDisplayMetrics().density;
                    int basePaddingPx = (int) (16 * density);
                    int bottomPadding = keypadHeight > 0 ? keypadHeight + basePaddingPx : basePaddingPx;

                    tvEmptySearch.setPadding(
                            tvEmptySearch.getPaddingLeft(),
                            tvEmptySearch.getPaddingTop(),
                            tvEmptySearch.getPaddingRight(),
                            bottomPadding
                    );
                }
            }
        });
    }

    // ==========================================
    // 📋 СЛУШАТЕЛИ
    // ==========================================

    private void setupListeners() {
        btnOverlayBack.setOnClickListener(v -> hideOverlay());

        btnLists.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SavedListsActivity.class);
            startActivity(intent);
        });

        btnHelp.setOnClickListener(v -> {
            Toast.makeText(this, "Открыть справку", Toast.LENGTH_SHORT).show();
        });

        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Открыть настройки", Toast.LENGTH_SHORT).show();
        });

        btnSetpoints.setOnClickListener(v -> {
            // Переключаемся в режим уставок
            showSetpointsMode();
        });

        btnSensors.setOnClickListener(v -> {
            // Переключаемся в режим датчиков
            showSensorsMode();
        });
        // ✅ ЗАМЕР ТЕМПЕРАТУРЫ → ДИАЛОГ ВЫБОРА
        btnConverter.setOnClickListener(v -> {
            showMeasurementTypeDialog();
        });
    }

    // ==========================================
    // 📋 ДИАЛОГ ВЫБОРА РЕЖИМА ЗАМЕРА
    // ==========================================

    private void showMeasurementTypeDialog() {
        String[] options = {"мВ (Термопары)", "Ом (Термосопротивления)"};

        new AlertDialog.Builder(this)
                .setTitle("Выберите тип замера")
                .setItems(options, (dialog, which) -> {
                    String mode = (which == 0) ? "MV" : "OHM";
                    Intent intent = new Intent(MainActivity.this, TemperatureActivity.class);
                    intent.putExtra("MODE", mode);
                    startActivity(intent);
                })
                .show();
    }

    // ==========================================
    // 🗂️ НАБЛЮДАТЕЛИ (OBSERVERS)
    // ==========================================

    private void setupObservers() {
        viewModel.getGateValves().observe(this, valves -> {
            Log.d("MAIN_DEBUG", "=== getGateValves observer ===");
            Log.d("MAIN_DEBUG", "valves size = " + (valves != null ? valves.size() : 0));

            if (valves != null && !valves.isEmpty()) {
                List<Integer> selectedPositions = new ArrayList<>();
                List<GateValve> currentList = viewModel.getCurrentListLive().getValue();
                if (currentList == null) currentList = new ArrayList<>();

                for (int i = 0; i < valves.size(); i++) {
                    GateValve valve = valves.get(i);
                    for (GateValve v : currentList) {
                        if (v.getId() == valve.getId()) {
                            selectedPositions.add(i);
                            break;
                        }
                    }
                }

                adapter.updateData(valves, selectedPositions);
                rvGateValves.setVisibility(View.VISIBLE);
                tvEmptySearch.setVisibility(View.GONE);
                expandedPositions.clear();
            } else {
                adapter.updateData(new ArrayList<>(), new ArrayList<>());
                rvGateValves.setVisibility(View.GONE);
                String query = etSearch.getText().toString().trim();
                tvEmptySearch.setVisibility(query.length() >= 2 ? View.VISIBLE : View.GONE);
            }
            Log.d("MAIN_DEBUG", "=== getGateValves observer END ===");
        });
    }

    // ==========================================
    // ✅ СИНХРОНИЗАЦИЯ ВЫДЕЛЕНИЯ
    // ==========================================

    private void syncAdapterSelection() {
        rvGateValves.post(() -> {
            List<Integer> selectedPositions = new ArrayList<>();
            List<GateValve> currentValves = adapter.getValves();

            if (currentValves != null) {
                List<GateValve> currentList = viewModel.getCurrentListLive().getValue();
                if (currentList == null) currentList = new ArrayList<>();

                for (int i = 0; i < currentValves.size(); i++) {
                    GateValve valve = currentValves.get(i);
                    for (GateValve v : currentList) {
                        if (v.getId() == valve.getId()) {
                            selectedPositions.add(i);
                            break;
                        }
                    }
                }
            }
            adapter.updateData(adapter.getValves(), selectedPositions);
        });
    }

    private void hideKeyboard() {
        if (etSearch != null && etSearch.getWindowToken() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
        }
    }

    // ==========================================
    // 🔧 НАСТРОЙКА ОТСТУПОВ
    // ==========================================

    private void setupStatusBarPadding() {
        if (btnOverlayBack == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(btnOverlayBack, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            MainActivity.this.statusBarHeightPx = statusBarHeight;

            float density = v.getResources().getDisplayMetrics().density;
            int baseMarginPx = (int) (16 * density);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) v.getLayoutParams();
            params.topMargin = statusBarHeight + baseMarginPx;
            v.setLayoutParams(params);

            ViewCompat.setOnApplyWindowInsetsListener(v, null);
            return insets;
        });
    }

    private void setupNavigationBarPadding() {
        if (rootLayout == null || cardOverlayDescription == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            MainActivity.this.navigationBarHeight = navBarHeight;

            float density = v.getResources().getDisplayMetrics().density;
            int basePaddingPx = (int) (16 * density);
            int bottomPadding = navBarHeight > 0 ? navBarHeight + basePaddingPx : basePaddingPx;

            cardOverlayDescription.setPadding(
                    cardOverlayDescription.getPaddingLeft(),
                    cardOverlayDescription.getPaddingTop(),
                    cardOverlayDescription.getPaddingRight(),
                    bottomPadding
            );

            return insets;
        });
    }

    private void setStatusBarIconsDark(boolean dark) {
        Window window = getWindow();
        if (window != null) {
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
            controller.setAppearanceLightStatusBars(dark);
        }
    }

    private void fixTopPanelPadding() {
        if (topPanel == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(topPanel, (view, windowInsets) -> {
            int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            statusBarHeightPx = statusBarHeight;

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

    // ==========================================
    // 🔄 ОБНОВЛЕНИЕ ДАННЫХ
    // ==========================================

    private void refreshData() {
        String query = etSearch.getText().toString().trim();
        if (query.length() >= 2) {
            viewModel.search(query);
        } else {
            viewModel.search("");
        }
    }

    private void updateButtonState() {
        AppState appState = AppState.getInstance();

        if (appState.hasActiveSession()) {
            String name = appState.getLastOpenedSessionName();
            int size = viewModel.getCurrentListSize();
            if (name != null && !name.isEmpty()) {
                btnNewValve.setText(name + " (" + size + ")");
            } else {
                btnNewValve.setText("Список (" + size + ")");
            }
            btnNewValve.setOnClickListener(v -> openSession());
            return;
        }

        if (viewModel.getIsRecording().getValue() != null && viewModel.getIsRecording().getValue()) {
            int size = viewModel.getCurrentListSize();
            btnNewValve.setText(getString(R.string.to_list, size));
            btnNewValve.setOnClickListener(v -> openNewList());
            return;
        }

        btnNewValve.setText(R.string.new_valve);
        btnNewValve.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_IS_NEW, true);
            detailResultLauncher.launch(intent);
        });
    }
    // ==========================================
// 🔥 ПЕРЕКЛЮЧЕНИЕ РЕЖИМОВ
// ==========================================

    private void showMainMode() {
        currentMode = MODE_MAIN;
        Log.d(TAG, "=== showMainMode ===");

        // Показываем все кнопки
        updateTopButtonsVisibility(true);

        bottomButtons.setVisibility(View.VISIBLE);
        rvGateValves.setAdapter(adapter);
        rvGateValves.setVisibility(View.GONE);

        etSearch.setText("");
        etSearch.clearFocus();
        isSearchActive = false;

        deactivateSearchState();

        btnNewValve.setText(R.string.new_valve);
        btnNewValve.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_IS_NEW, true);
            detailResultLauncher.launch(intent);
        });

        updateSearchHint();
        refreshData();
        updateButtonState();
    }
    private void showSensorsMode() {
        currentMode = MODE_SENSORS;
        Log.d(TAG, "=== showSensorsMode ===");

        // Скрываем Списки и Настройки
        updateTopButtonsVisibility(false);

        bottomButtons.setVisibility(View.GONE);
        rvGateValves.setAdapter(sensorAdapter);
        rvGateValves.setVisibility(View.VISIBLE);

        isSearchActive = true;
        activateSearchState();

        btnNewValve.setText("НОВАЯ ПОЗИЦИЯ");
        btnNewValve.setOnClickListener(v -> {
            Toast.makeText(this, "Создание новой позиции датчика", Toast.LENGTH_SHORT).show();
        });

        sensorAdapter.updateData(new ArrayList<>());
        tvEmptySearch.setVisibility(View.GONE);

        etSearch.setText("");
        etSearch.requestFocus();
        showKeyboard();
        updateSearchHint();
    }

    private void showSetpointsMode() {
        currentMode = MODE_SETPOINTS;
        Log.d(TAG, "=== showSetpointsMode ===");

        // Скрываем Списки и Настройки
        updateTopButtonsVisibility(false);

        bottomButtons.setVisibility(View.GONE);
        rvGateValves.setAdapter(setpointAdapter);
        rvGateValves.setVisibility(View.VISIBLE);

        isSearchActive = true;
        activateSearchState();

        btnNewValve.setText("НОВАЯ УСТАВКА");
        btnNewValve.setOnClickListener(v -> {
            Toast.makeText(this, "Создание новой уставки", Toast.LENGTH_SHORT).show();
        });

        setpointAdapter.updateData(new ArrayList<>());
        tvEmptySearch.setVisibility(View.GONE);

        etSearch.setText("");
        etSearch.requestFocus();
        showKeyboard();
        updateSearchHint();
    }
    private void updateTopButtonsVisibility(boolean showExtraButtons) {
        if (showExtraButtons) {
            // Режим 1 - показываем все кнопки
            btnHelp.setVisibility(View.VISIBLE);
            btnLists.setVisibility(View.VISIBLE);
            btnSettings.setVisibility(View.VISIBLE);
        } else {
            // Режимы 2 и 3 - скрываем Списки и Настройки, оставляем только Справку
            btnHelp.setVisibility(View.VISIBLE);
            btnLists.setVisibility(View.GONE);
            btnSettings.setVisibility(View.GONE);
        }
    }
    // ==========================================
// 🔧 ЗАГРУЗКА ДАННЫХ
// ==========================================

    private void loadAllSensors() {
        new Thread(() -> {
            try {
                IRepository repository = ((MyApp) getApplication()).getRepository();
                List<Sensor> sensors = repository.getAllSensors();
                runOnUiThread(() -> {
                    sensorAdapter.updateData(sensors);
                    tvEmptySearch.setVisibility(sensors.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadAllSetpoints() {
        Log.d("POINT", "=== loadAllSetpoints START ===");
        new Thread(() -> {
            try {
                IRepository repository = ((MyApp) getApplication()).getRepository();
                List<Setpoint> setpoints = repository.getAllSetpoints();
                Log.d("POINT", "all setpoints size = " + setpoints.size());

                for (int i = 0; i < setpoints.size(); i++) {
                    Setpoint sp = setpoints.get(i);
                    Log.d("POINT", "  ALL [" + i + "] position=" + sp.getPositionName() +
                            ", name=" + sp.getName());
                }

                runOnUiThread(() -> {
                    setpointAdapter.updateData(setpoints);
                    tvEmptySearch.setVisibility(setpoints.isEmpty() ? View.VISIBLE : View.GONE);
                });
                Log.d("POINT", "=== loadAllSetpoints END ===");
            } catch (Exception e) {
                Log.e("POINT", "Error loading setpoints", e);
                e.printStackTrace();
            }
        }).start();
    }

    private void searchSensors(String query) {
        Log.d(TAG, "searchSensors: query = " + query);
        new Thread(() -> {
            try {
                IRepository repository = ((MyApp) getApplication()).getRepository();

                // Проверяем команду #ВСЕ
                if (query.trim().equalsIgnoreCase("#все")) {
                    List<Sensor> all = repository.getAllSensors();
                    runOnUiThread(() -> {
                        sensorAdapter.updateData(all);
                        tvEmptySearch.setVisibility(all.isEmpty() ? View.VISIBLE : View.GONE);
                    });
                    return;
                }

                List<Sensor> results = repository.searchSensors(query);
                Log.d(TAG, "searchSensors: results size = " + results.size());
                runOnUiThread(() -> {
                    sensorAdapter.updateData(results);
                    tvEmptySearch.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (Exception e) {
                Log.e(TAG, "searchSensors error", e);
                e.printStackTrace();
            }
        }).start();
    }
    private void searchSetpoints(String query) {
        Log.d("POINT", "=== searchSetpoints START ===");
        Log.d("POINT", "query = " + query);

        new Thread(() -> {
            try {
                IRepository repository = ((MyApp) getApplication()).getRepository();

                // Проверяем команду #ВСЕ
                if (query.trim().equalsIgnoreCase("#все")) {
                    Log.d("POINT", "command #все detected, loading all setpoints");
                    List<Setpoint> all = repository.getAllSetpoints();
                    Log.d("POINT", "all setpoints size = " + all.size());
                    runOnUiThread(() -> {
                        setpointAdapter.updateData(all);
                        tvEmptySearch.setVisibility(all.isEmpty() ? View.VISIBLE : View.GONE);
                    });
                    return;
                }

                // 🔥 Проверяем, начинается ли запрос с #
                if (query.trim().startsWith("#")) {
                    String groupQuery = query.trim().substring(1).replaceAll("\\s+", "");
                    Log.d("POINT", "search by equipment_group: " + groupQuery);
                    List<Setpoint> results = repository.searchSetpointsByGroup(groupQuery);
                    runOnUiThread(() -> {
                        setpointAdapter.updateData(results);
                        tvEmptySearch.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                    });
                    return;
                }

                // Обычный поиск
                List<Setpoint> results = repository.searchSetpoints(query);
                Log.d("POINT", "search results size = " + results.size());

                runOnUiThread(() -> {
                    setpointAdapter.updateData(results);
                    tvEmptySearch.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                });
                Log.d("POINT", "=== searchSetpoints END ===");
            } catch (Exception e) {
                Log.e("POINT", "Search error", e);
                e.printStackTrace();
            }
        }).start();
    }
    private void createNewSession() {
        Toast.makeText(this, "createNewSession START", Toast.LENGTH_SHORT).show();

        String name = "Новый список";
        ValveWorkSession session = new ValveWorkSession();
        session.setSessionId("SESSION_" + System.currentTimeMillis());
        session.setEquipmentDescription(name);
        session.setSaveDate(getCurrentDateTime());

        AppState.getInstance().setActiveSession(session);
        AppState.getInstance().setHasUnsavedChanges(true);

        Intent intent = new Intent(MainActivity.this, ListDetailActivity.class);
        intent.putExtra("is_new_session", true);
        startActivity(intent);

        Toast.makeText(this, "createNewSession END", Toast.LENGTH_SHORT).show();
    }

    private void openSession() {
        String sessionId = AppState.getInstance().getLastOpenedSessionId();
        if (sessionId != null && !sessionId.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, ListDetailActivity.class);
            intent.putExtra("session_id", sessionId);
            listDetailResultLauncher.launch(intent);
        } else {
            Toast.makeText(this, "Сессия не найдена", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSaveChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Сохранить изменения?")
                .setMessage("Вы редактируете список. Сохранить изменения перед выходом?")
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    saveCurrentSession();
                    openSavedLists();
                })
                .setNegativeButton("Отмена", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void saveCurrentSession() {
        ValveWorkSession session = AppState.getInstance().getActiveSession();
        if (session != null) {
            viewModel.saveSessionToDb(session);
            AppState.getInstance().setHasUnsavedChanges(false);
            Toast.makeText(this, "Список сохранён", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSavedLists() {
        Intent intent = new Intent(MainActivity.this, SavedListsActivity.class);
        startActivity(intent);
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private void restoreLastSession() {
        Log.d("DUPLICATE", "=== restoreLastSession START ===");
        AppState appState = AppState.getInstance();
        Log.d("DUPLICATE", "hasActiveSession = " + appState.hasActiveSession());

        if (appState.hasActiveSession()) {
            Log.d("DUPLICATE", "sessionId = " + appState.getLastOpenedSessionId());
            new Thread(() -> {
                try {
                    List<ValveItem> items = ((MyApp) getApplication())
                            .getRepository()
                            .getValveItemsBySession(appState.getLastOpenedSessionId());

                    Log.d("DUPLICATE", "items size = " + items.size());

                    runOnUiThread(() -> {
                        viewModel.clearCurrentList();
                        for (ValveItem item : items) {
                            Log.d("RESTORE", "item: gateValveId=" + item.getGateValveId());

                            GateValve valve = ((MyApp) getApplication())
                                    .getRepository()
                                    .getGateValveById(item.getGateValveId());

                            Log.d("RESTORE", "valve found: " + (valve != null ? valve.getName() : "null"));

                            if (valve != null) {
                                valve.setId(item.getGateValveId());
                                viewModel.addToCurrentList(valve);
                            }
                        }
                        viewModel.updateCurrentListSize(items.size());
                        updateButtonState();
                        Log.d("DUPLICATE", "currentList size after restore = " + viewModel.getCurrentListSize());
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            Log.d("DUPLICATE", "Нет активной сессии");
        }
        Log.d("DUPLICATE", "=== restoreLastSession END ===");
    }

    private void openNewList() {
        List<GateValve> currentList = viewModel.getCurrentListLive().getValue();
        if (currentList == null || currentList.isEmpty()) {
            Toast.makeText(this, R.string.list_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> isys = new ArrayList<>();

        for (GateValve valve : currentList) {
            ids.add(valve.getId());
            names.add(valve.getName() != null ? valve.getName() : "");
            isys.add(valve.getIsy() != null ? valve.getIsy() : "");
        }

        Intent intent = new Intent(MainActivity.this, ListDetailActivity.class);
        intent.putIntegerArrayListExtra("valve_ids", ids);
        intent.putStringArrayListExtra("valve_names", names);
        intent.putStringArrayListExtra("valve_isys", isys);

        Log.d("MYTITLE", "=== openNewList ===");
        Log.d("MYTITLE", "ids size: " + ids.size());
        Log.d("MYTITLE", "names size: " + names.size());

        listDetailResultLauncher.launch(intent);
    }
    private void showKeyboard() {
        if (etSearch != null) {
            etSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }
    private void updateSearchHint() {
        TextInputLayout searchLayout = findViewById(R.id.searchLayout);
        if (searchLayout != null) {
            switch (currentMode) {
                case MODE_MAIN:
                    searchLayout.setHint(R.string.search_hint);
                    break;
                case MODE_SENSORS:
                    searchLayout.setHint(R.string.search_hint_sensors);
                    break;
                case MODE_SETPOINTS:
                    searchLayout.setHint(R.string.search_hint_setpoints);
                    break;
                default:
                    searchLayout.setHint(R.string.search_hint);
                    break;
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MAIN_DEBUG", "=== onResume START ===");

        if (!AppState.getInstance().hasActiveSession()) {
            viewModel.clearCurrentList();
            Log.d("MAIN_DEBUG", "No active session, cleared current list");
        } else {
            restoreLastSession();
        }

        viewModel.refreshData();
        updateButtonState();
        Log.d("MAIN_DEBUG", "=== onResume END ===");
    }
}