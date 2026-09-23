package com.mikesuvade.focus.ui.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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
import com.mikesuvade.focus.domain.models.ValveWorkSession;
import com.mikesuvade.focus.domain.repository.IRepository;
import com.mikesuvade.focus.ui.main.managers.AdapterManager;
import com.mikesuvade.focus.ui.main.managers.ModeManager;
import com.mikesuvade.focus.ui.main.managers.SearchManager;
import com.mikesuvade.focus.ui.main.managers.SessionManager;
import com.mikesuvade.focus.ui.saved.SavedListsActivity;
import com.mikesuvade.focus.ui.settings.SettingsActivity;
import com.mikesuvade.focus.ui.temperature.TemperatureActivity;
import com.mikesuvade.focus.utils.AppState;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        ModeManager.MainActivityCallback,
        SessionManager.SessionListener,
        AdapterManager.AdapterCallbacks,
        SessionManager.MainViewModelCallback {

    private static final String TAG = "MainActivity";

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

    private ConstraintLayout overlayDetail;
    private ImageButton btnOverlayBack;
    private TextView tvOverlayTitle;
    private ImageView ivOverlayImage;
    private TextView tvOverlayDescription;
    private ProgressBar progressOverlay;
    private com.google.android.material.card.MaterialCardView cardOverlayDescription;

    // Менеджеры
    private ModeManager modeManager;
    private SearchManager searchManager;
    private SessionManager sessionManager;
    private AdapterManager adapterManager;
    private MainViewModel viewModel;

    // Состояние
    private boolean isSearchActive = false;
    private int statusBarHeightPx = 0;
    private int navigationBarHeight = 0;
    private TextView tvTitle;

    // ActivityResultLauncher
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

    // 🔥 ЛАУНЧЕР ДЛЯ ДАТЧИКОВ
    private final ActivityResultLauncher<Intent> sensorDetailResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {

                    refreshData();
                }
            }
    );

    // 🔥 ЛАУНЧЕР ДЛЯ УСТАВОК
    private final ActivityResultLauncher<Intent> setpointDetailResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {

                    refreshData();
                }
            }
    );

    public ActivityResultLauncher<Intent> getSensorDetailResultLauncher() {
        return sensorDetailResultLauncher;
    }

    public ActivityResultLauncher<Intent> getSetpointDetailResultLauncher() {
        return setpointDetailResultLauncher;
    }

    // ==========================================
    // 🚀 ЖИЗНЕННЫЙ ЦИКЛ
    // ==========================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 🌟 Инициализируем заставку перед созданием Activity
        androidx.core.splashscreen.SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initViews();
        setupNavigationBarPadding();
        setupStatusBarPadding();
        fixTopPanelPadding();
        setStatusBarIconsDark(true);

        setupViewModel();
        setupManagers();
        setupRecyclerView();
        fixRecyclerViewBottomPadding();
        setupSearch();
        setupHideKeyboardOnTouch();
        setupListeners();

        restoreLastSession();
        updateButtonState();

        updateTitleForMode();

    }

    @Override
    protected void onResume() {
        super.onResume();


        AppState appState = AppState.getInstance();


        if (modeManager != null && modeManager.isMainMode()) {
            if (appState.hasActiveSession()) {
                sessionManager.restoreLastSession();
            } else if (appState.hasUnsavedList()) {
                sessionManager.restoreUnsavedList();
            } else {
                viewModel.clearCurrentList();
            }

            rvGateValves.postDelayed(() -> {
                sessionManager.syncAdapterSelection();
                updateButtonState();
            }, 100);
        }


    }

    // ==========================================
    // 🔧 ИНИЦИАЛИЗАЦИЯ
    // ==========================================

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
        tvTitle = findViewById(R.id.tvTitle);

        btnNewValve.setVisibility(View.GONE);

        btnNewValve.setOnLongClickListener(v -> {
            if (AppState.getInstance().hasActiveSession()) {
                sessionManager.showCloseListDialog();
                return true;
            } else if (viewModel.getIsRecording().getValue() != null && viewModel.getIsRecording().getValue()) {
                sessionManager.showClearSelectionDialog();
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
                        if (modelClass.isAssignableFrom(MainViewModel.class)) {
                            return (T) new MainViewModel(repository);
                        }
                        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass);
                    }
                }
        ).get(MainViewModel.class);
    }

    private void setupManagers() {
        IRepository repository = ((MyApp) getApplication()).getRepository();

        adapterManager = new AdapterManager(this, this);

        modeManager = new ModeManager(
                this,
                rvGateValves,
                etSearch,
                tvEmptySearch,
                bottomButtons,
                btnHelp,
                btnLists,
                btnSettings,
                btnNewValve,
                adapterManager.getValveAdapter(),
                adapterManager.getSensorAdapter(),
                adapterManager.getSetpointAdapter()
        );
        adapterManager.setModeManager(modeManager);

        searchManager = new SearchManager(
                repository,
                adapterManager.getValveAdapter(),
                adapterManager.getSensorAdapter(),
                adapterManager.getSetpointAdapter(),
                tvEmptySearch,
                rvGateValves
        );

        searchManager.setOnSearchErrorListener((userMessage, cause) -> {
            if (isFinishing() || isDestroyed()) return;

            // Скрываем RecyclerView и показываем сообщение в tvEmptySearch
            rvGateValves.setVisibility(View.GONE);
            tvEmptySearch.setVisibility(View.VISIBLE);

            String display = "⚠️ " + userMessage
                    + "\n\nЕсли проблема повторяется — проверьте базу данных.";
            tvEmptySearch.setText(display);

            Toast.makeText(MainActivity.this, userMessage, Toast.LENGTH_LONG).show();
        });

        sessionManager = new SessionManager(
                repository,
                btnNewValve,
                this,
                this
        );
    }

    private void setupRecyclerView() {
        rvGateValves.setLayoutManager(new LinearLayoutManager(this));
        rvGateValves.setAdapter(adapterManager.getValveAdapter());

        rvGateValves.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard();
                }
            }
        });
    }

    private void setupSearch() {
        rvGateValves.setVisibility(View.GONE);
        tvEmptySearch.setVisibility(View.GONE);

        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !isSearchActive && modeManager.isMainMode()) {
                activateSearchState();
                adapterManager.getValveAdapter().updateData(new ArrayList<>(), new ArrayList<>());
                rvGateValves.setVisibility(View.VISIBLE);
                tvEmptySearch.setVisibility(View.GONE);
            }
        });

        etSearch.setOnClickListener(v -> {
            if (!isSearchActive && modeManager.isMainMode()) {
                activateSearchState();
                adapterManager.getValveAdapter().updateData(new ArrayList<>(), new ArrayList<>());
                rvGateValves.setVisibility(View.VISIBLE);
                tvEmptySearch.setVisibility(View.GONE);
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();


                if (modeManager.isMainMode()) {
                    if (query.isEmpty()) {
                        adapterManager.getValveAdapter().updateData(new ArrayList<>(), new ArrayList<>());
                        rvGateValves.setVisibility(View.VISIBLE);
                        tvEmptySearch.setVisibility(View.GONE);
                    } else if (query.equalsIgnoreCase("#все")) {
                        searchManager.loadAllValves();
                    } else if (query.length() >= 2) {
                        searchManager.searchValves(query);
                    } else {
                        adapterManager.getValveAdapter().updateData(new ArrayList<>(), new ArrayList<>());
                        rvGateValves.setVisibility(View.VISIBLE);
                        tvEmptySearch.setVisibility(View.GONE);
                    }
                } else if (modeManager.isSensorsMode()) {
                    if (query.isEmpty()) {
                        adapterManager.getSensorAdapter().updateData(new ArrayList<>());
                        rvGateValves.setVisibility(View.VISIBLE);
                        tvEmptySearch.setVisibility(View.GONE);
                    } else if (query.equalsIgnoreCase("#все")) {
                        searchManager.loadAllSensors();
                    } else if (query.length() >= 2) {
                        searchManager.searchSensors(query);
                    } else {
                        adapterManager.getSensorAdapter().updateData(new ArrayList<>());
                        rvGateValves.setVisibility(View.VISIBLE);
                        tvEmptySearch.setVisibility(View.GONE);
                    }
                } else if (modeManager.isSetpointsMode()) {
                    if (query.isEmpty()) {
                        adapterManager.getSetpointAdapter().updateData(new ArrayList<>());
                        rvGateValves.setVisibility(View.VISIBLE);
                        tvEmptySearch.setVisibility(View.GONE);
                    } else if (query.equalsIgnoreCase("#все")) {
                        searchManager.loadAllSetpoints();
                    } else if (query.length() >= 2) {
                        searchManager.searchSetpoints(query);
                    } else {
                        adapterManager.getSetpointAdapter().updateData(new ArrayList<>());
                        rvGateValves.setVisibility(View.VISIBLE);
                        tvEmptySearch.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupListeners() {
        btnOverlayBack.setOnClickListener(v -> hideOverlay());

        btnSensors.setOnClickListener(v -> {

            modeManager.switchToSensors();
        });

        btnSetpoints.setOnClickListener(v -> {

            modeManager.switchToSetpoints();
        });

        btnLists.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SavedListsActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        btnConverter.setOnClickListener(v -> showMeasurementTypeDialog());

        btnHelp.setOnClickListener(v -> {
            Toast.makeText(this, "Справка", Toast.LENGTH_SHORT).show();
        });
    }

    // ==========================================
    // 🔧 CALLBACKS — ModeManager
    // ==========================================

    @Override
    public void activateSearchState() {
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

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Override
    public void deactivateSearchState() {
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
    public void showKeyboard() {
        if (etSearch != null) {
            etSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    @Override
    public void updateSearchHint() {
        TextInputLayout searchLayout = findViewById(R.id.searchLayout);
        if (searchLayout != null) {
            if (modeManager.isMainMode()) {
                searchLayout.setHint(R.string.search_hint);
            } else if (modeManager.isSensorsMode()) {
                searchLayout.setHint(R.string.search_hint_sensors);
            } else if (modeManager.isSetpointsMode()) {
                searchLayout.setHint(R.string.search_hint_setpoints);
            }
        }
    }

    @Override
    public void updateButtonState() {
        if (modeManager != null && modeManager.isMainMode()) {
            sessionManager.updateButtonState();
        } else {
            btnNewValve.setVisibility(View.GONE);
            updateTitle(true);
        }
    }


    @Override
    public void clearSearch() {
        searchManager.clearSearch();
    }

    @Override
    public void setRvVisibility(boolean visible) {
        rvGateValves.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    // ==========================================
    // 🔧 CALLBACKS — SessionManager.SessionListener
    // ==========================================

    @Override
    public void syncAdapterSelection() {
        rvGateValves.post(() -> {
            List<Integer> selectedPositions = new ArrayList<>();
            List<GateValve> currentValves = adapterManager.getValveAdapter().getValves();

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
            adapterManager.syncAdapterSelection(selectedPositions);
        });
    }

    @Override
    public void refreshData() {
        // Ничего не грузим автоматически.
        // Обновляем список только если пользователь в активном поиске.
        if (!isSearchActive) {
            return;
        }

        String query = etSearch.getText().toString().trim();

        if (modeManager.isMainMode()) {
            if (query.length() >= 2) {
                searchManager.searchValves(query);
            }
            // Пустой запрос — не трогаем адаптер.
            // Пользователь либо уже вышел из поиска, либо ещё ничего не ввёл.
        } else if (modeManager.isSensorsMode()) {
            if (query.length() >= 2) {
                searchManager.searchSensors(query);
            }
        } else if (modeManager.isSetpointsMode()) {
            if (query.length() >= 2) {
                searchManager.searchSetpoints(query);
            }
        }
    }
    @Override
    public void launchListDetailActivity(Intent intent) {
        listDetailResultLauncher.launch(intent);
    }

    // ==========================================
    // 🔧 CALLBACKS — AdapterManager.AdapterCallbacks
    // ==========================================

    @Override
    public void showOverlay(String title, byte[] imageData, String description) {
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

    @Override
    public void toggleExpanded(int position) {


        List<Integer> expandedPositions = adapterManager.getExpandedPositions();

        if (expandedPositions.contains(position)) {
            expandedPositions.remove(Integer.valueOf(position));
            adapterManager.setExpanded(position, false);
            return;
        }

        int previousExpanded = -1;
        if (!expandedPositions.isEmpty()) {
            previousExpanded = expandedPositions.get(0);
        }

        expandedPositions.clear();

        if (previousExpanded != -1 && previousExpanded != position) {
            adapterManager.setExpanded(previousExpanded, false);
        }

        expandedPositions.add(position);
        adapterManager.setExpanded(position, true);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void launchValveDetailActivity(Intent intent) {

        detailResultLauncher.launch(intent);
    }

    @Override
    public void launchSensorDetailActivity(Intent intent) {

        sensorDetailResultLauncher.launch(intent);
    }

    @Override
    public void launchSetpointDetailActivity(Intent intent) {

        setpointDetailResultLauncher.launch(intent);
    }

    @Override
    public void onCheckBoxChanged(GateValve valve, boolean isChecked) {


        if (isChecked) {
            if (viewModel.isInCurrentList(valve)) {
                Toast.makeText(this, "Задвижка уже в списке", Toast.LENGTH_SHORT).show();
                return;
            }
            if (AppState.getInstance().hasActiveSession()) {
                viewModel.addToActiveSession(valve);
            } else {
                // 🔥 СОХРАНЯЕМ ID В НЕСОХРАНЕННЫЙ СПИСОК
                AppState.getInstance().addUnsavedId(valve.getId());
            }
            viewModel.addToCurrentList(valve);
            Toast.makeText(this, "Добавлено: " + valve.getName(), Toast.LENGTH_SHORT).show();
        } else {
            viewModel.removeFromCurrentList(valve);
            // 🔥 УДАЛЯЕМ ID ИЗ НЕСОХРАНЕННОГО СПИСКА
            AppState.getInstance().removeUnsavedId(valve.getId());
            Toast.makeText(this, "Удалено: " + valve.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    // ==========================================
    // 🔧 CALLBACKS — SessionManager.MainViewModelCallback
    // ==========================================

    @Override
    public void clearCurrentList() {
        viewModel.clearCurrentList();
      // 🔥 ДОБАВИТЬ
    }

    @Override
    public int getCurrentListSize() {
        return viewModel.getCurrentListSize();
    }

    @Override
    public List<GateValve> getCurrentList() {
        return viewModel.getCurrentListLive().getValue();
    }

    @Override
    public void updateCurrentListSize(int size) {
        viewModel.updateCurrentListSize(size);
    }

    @Override
    public void addToCurrentList(GateValve valve) {
        viewModel.addToCurrentList(valve);
    }

    @Override
    public void removeFromCurrentList(GateValve valve) {
        viewModel.removeFromCurrentList(valve);
    }

    @Override
    public void setRecording(boolean recording) {
        viewModel.setRecording(recording);
    }

    @Override
    public boolean isInCurrentList(GateValve valve) {
        return viewModel.isInCurrentList(valve);
    }

    @Override
    public void saveUserSessionToDb(ValveWorkSession session) {
        viewModel.saveUserSessionToDb(session);
    }

    // ==========================================
    // 🗂️ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ==========================================

    private void showMeasurementTypeDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_measurement_type, null);

        TextView tvMv  = dialogView.findViewById(R.id.tvMv);
        TextView tvOhm = dialogView.findViewById(R.id.tvOhm);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        tvMv.setOnClickListener(v -> {
            dialog.dismiss();                            // 🔥 ЗАКРЫВАЕМ ДИАЛОГ
            Intent intent = new Intent(MainActivity.this, TemperatureActivity.class);
            intent.putExtra("MODE", "MV");
            startActivity(intent);
        });

        tvOhm.setOnClickListener(v -> {
            dialog.dismiss();                            // 🔥 ЗАКРЫВАЕМ ДИАЛОГ
            Intent intent = new Intent(MainActivity.this, TemperatureActivity.class);
            intent.putExtra("MODE", "OHM");
            startActivity(intent);
        });

        dialog.show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;

        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } else {
            imm.hideSoftInputFromWindow(
                    getWindow().getDecorView().getWindowToken(), 0);
        }
    }
    private void restoreLastSession() {
        sessionManager.restoreLastSession();
    }

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

    @Override
    public void onBackPressed() {
        if (overlayDetail.getVisibility() == View.VISIBLE) {
            hideOverlay();
        } else if (!modeManager.isMainMode()) {
            modeManager.switchToMain();
            rootLayout.requestLayout();
        } else if (isSearchActive) {
            deactivateSearchState();
            rootLayout.requestLayout();
        } else {
            super.onBackPressed();
        }
    }


    private void fixRecyclerViewBottomPadding() {
        RecyclerView recyclerView = rvGateValves; // Ваш RecyclerView
        if (recyclerView == null) return;

        // Разрешаем скролл под паддинг (карточки уходят под кнопки, но последняя приподнимается)
        recyclerView.setClipToPadding(false);

        // Слушаем высоту системной панели навигации
        ViewCompat.setOnApplyWindowInsetsListener(recyclerView, (v, insets) -> {
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Запас 16dp для красоты
            int extraPadding = (int) (16 * getResources().getDisplayMetrics().density);

            recyclerView.setPadding(
                    recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getPaddingRight(),
                    navBarHeight + extraPadding
            );

            return insets;
        });
    }
    @Override
    public void updateTitle(boolean showTitle) {
        if (tvTitle == null) return;

        if (showTitle) {
            tvTitle.setVisibility(View.VISIBLE);

            // 🔥 ОПРЕДЕЛЯЕМ ЗАГОЛОВОК В ЗАВИСИМОСТИ ОТ РЕЖИМА
            if (modeManager.isMainMode()) {
                tvTitle.setText("Арматура");
            } else if (modeManager.isSensorsMode()) {
                tvTitle.setText("Датчики");
            } else if (modeManager.isSetpointsMode()) {
                tvTitle.setText("Уставки");
            } else {
                tvTitle.setText("Поиск");
            }
        } else {
            tvTitle.setVisibility(View.GONE);
        }
    }
    private void updateTitleForMode() {
        if (tvTitle == null) return;

        if (btnNewValve.getVisibility() == View.VISIBLE) {
            tvTitle.setVisibility(View.GONE);
        } else {
            tvTitle.setVisibility(View.VISIBLE);

            if (modeManager.isMainMode()) {
                tvTitle.setText(getString(R.string.title_armatura));
            } else if (modeManager.isSensorsMode()) {
                tvTitle.setText(getString(R.string.title_sensors));
            } else if (modeManager.isSetpointsMode()) {
                tvTitle.setText(getString(R.string.title_setpoints));
            } else {
                tvTitle.setText(getString(R.string.title_search));
            }
        }
    }
    private void setupHideKeyboardOnTouch() {
        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                View currentFocus = getCurrentFocus();
                if (currentFocus != null && currentFocus instanceof EditText) {
                    hideKeyboard();
                    currentFocus.clearFocus();
                }
            }
            return false;
        });
    }
    @Override
    public void hideKeyboardOnClick() {
        hideKeyboard(); // Вызываем существующий приватный метод
    }
    @Override
    public void syncAdapterSelectionWithIds(List<Integer> currentIds) {


        // Получаем текущие задвижки из адаптера
        List<GateValve> allValves = adapterManager.getValveAdapter().getValves();


        List<Integer> selectedPositions = new ArrayList<>();
        for (int i = 0; i < allValves.size(); i++) {
            GateValve valve = allValves.get(i);
            if (currentIds.contains(valve.getId())) {
                selectedPositions.add(i);

            }
        }


        adapterManager.syncAdapterSelection(selectedPositions);
    }
}