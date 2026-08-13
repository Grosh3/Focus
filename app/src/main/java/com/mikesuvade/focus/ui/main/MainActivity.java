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

import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.repository.IRepository;
import com.mikesuvade.focus.ui.detail.DetailActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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

    private void setupRecyclerView() {
        rvGateValves.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GateValveAdapter();

        rvGateValves.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard();
                }
            }
        });

        // Короткий тап - развернуть/свернуть
        adapter.setOnItemClickListener((valve, position) -> {
            toggleExpanded(position);
        });

        // Длинный тап - диалог подтверждения редактирования
        adapter.setOnItemLongClickListener(new GateValveAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(GateValve valve) {
                String displayName = valve.getIsy();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = valve.getName();
                }
                if (displayName == null || displayName.isEmpty()) {
                    displayName = "Без названия";
                }

                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
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
            }
        });

        // CheckBox - добавление/удаление из списка
        adapter.setOnCheckBoxClickListener((valve, position, isChecked) -> {
            if (isChecked) {
                if (!viewModel.isInCurrentList(valve)) {
                    viewModel.addToCurrentList(valve);
                    Toast.makeText(this, "Добавлено: " + valve.getName(), Toast.LENGTH_SHORT).show();
                }
            } else {
                viewModel.removeFromCurrentList(valve);
                Toast.makeText(this, "Удалено: " + valve.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        // Блокировки - показываем оверлей
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

        rvGateValves.setAdapter(adapter);
    }

    private void toggleExpanded(int position) {
        if (expandedPositions.contains(position)) {
            expandedPositions.remove(Integer.valueOf(position));
            adapter.setExpanded(position, false);
            return;
        }

        int previousExpanded = -1;
        if (!expandedPositions.isEmpty()) {
            previousExpanded = expandedPositions.get(0);
        }

        expandedPositions.clear();

        if (previousExpanded != -1 && previousExpanded != position) {
            adapter.setExpanded(previousExpanded, false);
        }

        expandedPositions.add(position);
        adapter.setExpanded(position, true);
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
        if (!isSearchActive) return;
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
        } else if (isSearchActive) {
            deactivateSearchState();
        } else {
            super.onBackPressed();
        }
    }

    // ==========================================
    // 🔍 ПОИСК И СЛУШАТЕЛИ ВВОДА
    // ==========================================

    private void setupSearch() {
        rvGateValves.setVisibility(View.GONE);
        tvEmptySearch.setVisibility(View.GONE);

        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !isSearchActive) {
                activateSearchState();
            }
        });

        etSearch.setOnClickListener(v -> {
            if (!isSearchActive) {
                activateSearchState();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    viewModel.search(query);
                } else {
                    rvGateValves.setVisibility(View.GONE);
                    tvEmptySearch.setVisibility(View.GONE);
                    if (query.isEmpty()) {
                        viewModel.search("");
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

    private void setupListeners() {
        btnOverlayBack.setOnClickListener(v -> hideOverlay());

        btnLists.setOnClickListener(v -> {
            Toast.makeText(this, "Открыть сохранённые списки", Toast.LENGTH_SHORT).show();
        });

        btnHelp.setOnClickListener(v -> {
            Toast.makeText(this, "Открыть справку", Toast.LENGTH_SHORT).show();
        });

        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Открыть настройки", Toast.LENGTH_SHORT).show();
        });

        btnSetpoints.setOnClickListener(v -> {
            Toast.makeText(this, "Открыть журнал уставок", Toast.LENGTH_SHORT).show();
        });

        btnSensors.setOnClickListener(v -> {
            Toast.makeText(this, "Открыть список датчиков", Toast.LENGTH_SHORT).show();
        });

        btnConverter.setOnClickListener(v -> {
            Toast.makeText(this, "Открыть замер температуры", Toast.LENGTH_SHORT).show();
        });
    }

    // ==========================================
    // 🗂️ НАБЛЮДАТЕЛИ (OBSERVERS)
    // ==========================================

    private void setupObservers() {
        viewModel.getGateValves().observe(this, valves -> {
            if (valves != null && !valves.isEmpty()) {
                adapter.setValves(valves);
                rvGateValves.setVisibility(View.VISIBLE);
                tvEmptySearch.setVisibility(View.GONE);
                expandedPositions.clear();
                syncAdapterSelection();
            } else {
                adapter.setValves(new ArrayList<>());
                rvGateValves.setVisibility(View.GONE);

                String query = etSearch.getText().toString().trim();
                if (query.length() >= 2) {
                    tvEmptySearch.setVisibility(View.VISIBLE);
                } else {
                    tvEmptySearch.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getIsRecording().observe(this, isRecording -> {
            if (isRecording) {
                int size = viewModel.getCurrentListSize();
                btnNewValve.setText(getString(R.string.to_list, size));
                btnNewValve.setOnClickListener(v -> {
                    Toast.makeText(this, "Открыть список (" + size + " шт.)", Toast.LENGTH_SHORT).show();
                });
            } else {
                btnNewValve.setText(R.string.new_valve);
                btnNewValve.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra(DetailActivity.EXTRA_IS_NEW, true);
                    detailResultLauncher.launch(intent);
                });
            }
            syncAdapterSelection();
        });

        viewModel.getCurrentListSizeLiveData().observe(this, size -> {
            if (viewModel.getIsRecording().getValue() != null && viewModel.getIsRecording().getValue()) {
                btnNewValve.setText(getString(R.string.to_list, size));
            }
            if (size == 0 && viewModel.getIsRecording().getValue() != null && viewModel.getIsRecording().getValue()) {
                viewModel.setRecording(false);
            }
            syncAdapterSelection();
        });

        viewModel.getListName().observe(this, name -> {
            // можно использовать для отображения имени
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
                for (int i = 0; i < currentValves.size(); i++) {
                    GateValve valve = currentValves.get(i);
                    if (viewModel.isInCurrentList(valve)) {
                        selectedPositions.add(i);
                    }
                }
            }
            adapter.setSelectedPositions(selectedPositions);
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

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshData();
    }
}