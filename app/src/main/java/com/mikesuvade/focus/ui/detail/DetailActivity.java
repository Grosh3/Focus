package com.mikesuvade.focus.ui.detail;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.repository.IRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_VALVE_ID = "valve_id";
    public static final String EXTRA_IS_NEW = "is_new";

    private IRepository repository;
    private GateValve currentValve;
    private GateValve originalValve;
    private int valveId;
    private boolean isNewValve = false;
    private boolean isSaved = false;
    private boolean isDeleting = false;

    // UI
    private TextView tvValveTitle;
    private EditText etName;
    private EditText etKks;
    private EditText etIsy;
    private EditText etPowerCabinet;
    private EditText etOnPlace;
    private EditText etFullName;
    private EditText etNameEng;
    private EditText etAp50;
    private EditText etMark;
    private EditText etCdaCabinet;
    private EditText etCdaCabinetPosition;
    private EditText etSlot;
    private LinearLayout extraFieldsContainer;
    private View btnToggleExtra;
    private TextView tvStatus;
    private EditText etLocationDescription;

    private boolean isExtraVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = ((MyApp) getApplication()).getRepository();
        initViews();
        fixToolbarPadding(toolbar);
        setStatusBarIconsDark(true);
        setupListeners();
        setupKeyboardAutoScroll();

        // ==========================================
        // 🔥 ПРОВЕРЯЕМ — НОВАЯ ИЛИ РЕДАКТИРОВАНИЕ
        // ==========================================
        isNewValve = getIntent().getBooleanExtra(EXTRA_IS_NEW, false);
        valveId = getIntent().getIntExtra(EXTRA_VALVE_ID, -1);

        if (isNewValve) {
            getSupportActionBar().setTitle("Новая задвижка");
            if (tvStatus != null) {
                tvStatus.setVisibility(View.GONE);
            }
            currentValve = new GateValve();
            originalValve = new GateValve();
            displayValveData();
        } else {
            if (valveId == -1) {
                Toast.makeText(this, R.string.valve_id_not_passed, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            loadValveData();
        }
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etKks = findViewById(R.id.etKks);
        etIsy = findViewById(R.id.etIsy);
        etPowerCabinet = findViewById(R.id.etPowerCabinet);
        etOnPlace = findViewById(R.id.etOnPlace);
        etFullName = findViewById(R.id.etFullName);
        etNameEng = findViewById(R.id.etNameEng);
        etAp50 = findViewById(R.id.etAp50);
        etMark = findViewById(R.id.etMark);
        etCdaCabinet = findViewById(R.id.etCdaCabinet);
        etCdaCabinetPosition = findViewById(R.id.etCdaCabinetPosition);
        etSlot = findViewById(R.id.etSlot);
        extraFieldsContainer = findViewById(R.id.extraFieldsContainer);
        btnToggleExtra = findViewById(R.id.btnToggleExtra);
        tvStatus = findViewById(R.id.tvStatus);
        etLocationDescription = findViewById(R.id.etLocationDescription);

    }

    private void setupListeners() {
        btnToggleExtra.setOnClickListener(v -> {
            isExtraVisible = !isExtraVisible;
            extraFieldsContainer.setVisibility(isExtraVisible ? View.VISIBLE : View.GONE);
            String text = isExtraVisible
                    ? getString(R.string.hide_extra_fields)
                    : getString(R.string.toggle_extra_fields);
            ((android.widget.Button) btnToggleExtra).setText(text);

            ScrollView scrollView = findViewById(R.id.editScrollView);
            if (scrollView != null) {
                scrollView.post(() -> {
                    scrollView.requestLayout();
                    btnToggleExtra.post(() -> {
                        scrollView.smoothScrollTo(0, btnToggleExtra.getBottom() + 50);
                    });
                });
            }
        });
    }

    private void loadValveData() {
        new Thread(() -> {
            try {
                GateValve valve = repository.getGateValveById(valveId);
                currentValve = valve;
                originalValve = copyValve(valve);

                runOnUiThread(() -> {
                    if (currentValve != null) {
                        displayValveData();
                    } else {
                        Toast.makeText(this, R.string.valve_not_found, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_loading_data, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void displayValveData() {
        if (currentValve == null) return;

        String isy = currentValve.getIsy();
        if (isy != null && !isy.isEmpty()) {
            getSupportActionBar().setTitle(isy + " - " + getString(R.string.detail_title));
        } else {
            getSupportActionBar().setTitle(R.string.detail_title);
        }


        etName.setText(currentValve.getName());
        etKks.setText(currentValve.getKks());
        etIsy.setText(currentValve.getIsy());
        etPowerCabinet.setText(currentValve.getPowerCabinet());
        etOnPlace.setText(currentValve.getOnPlace());
        etFullName.setText(currentValve.getFullName());
        etNameEng.setText(currentValve.getNameEng());
        etAp50.setText(currentValve.getAp50());
        etMark.setText(currentValve.getMark());
        etCdaCabinet.setText(currentValve.getCdaCabinet());
        etCdaCabinetPosition.setText(currentValve.getCdaCabinetPosition());
        etSlot.setText(currentValve.getSlot());
        etLocationDescription.setText(currentValve.getLocationDescription());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            saveChanges();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveChanges() {
        if (currentValve == null) return;

        String isy = etIsy.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String powerCabinet = etPowerCabinet.getText().toString().trim();
        String locationDescription = etLocationDescription.getText().toString().trim();
        String onPlace = etOnPlace.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String kks = etKks.getText().toString().trim();
        String nameEng = etNameEng.getText().toString().trim();
        String ap50 = etAp50.getText().toString().trim();
        String mark = etMark.getText().toString().trim();
        String cdaCabinet = etCdaCabinet.getText().toString().trim();
        String cdaCabinetPosition = etCdaCabinetPosition.getText().toString().trim();
        String slot = etSlot.getText().toString().trim();

        boolean allFieldsEmpty = isy.isEmpty() &&
                name.isEmpty() &&
                powerCabinet.isEmpty() &&
                locationDescription.isEmpty() &&
                onPlace.isEmpty() &&
                fullName.isEmpty() &&
                kks.isEmpty() &&
                nameEng.isEmpty() &&
                ap50.isEmpty() &&
                mark.isEmpty() &&
                cdaCabinet.isEmpty() &&
                cdaCabinetPosition.isEmpty() &&
                slot.isEmpty();

        if (allFieldsEmpty) {
            if (isNewValve) {
                Toast.makeText(this, "Создание отменено", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Удалить задвижку?")
                    .setMessage("Все поля очищены. Удалить задвижку \"" + currentValve.getName() + "\"?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        deleteValve();
                    })
                    .setNegativeButton("Отмена", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
            return;
        }

        GateValve valveToSave = new GateValve();
        valveToSave.setIsy(isy);
        valveToSave.setName(name);
        valveToSave.setPowerCabinet(powerCabinet);
        valveToSave.setLocationDescription(locationDescription);
        valveToSave.setOnPlace(onPlace);
        valveToSave.setFullName(fullName);
        valveToSave.setKks(kks);
        valveToSave.setNameEng(nameEng);
        valveToSave.setAp50(ap50);
        valveToSave.setMark(mark);
        valveToSave.setCdaCabinet(cdaCabinet);
        valveToSave.setCdaCabinetPosition(cdaCabinetPosition);
        valveToSave.setSlot(slot);

        if (!isNewValve && currentValve != null) {
            valveToSave.setNameSpaceViewOpen(currentValve.getNameSpaceViewOpen());
            valveToSave.setNamespaceViewClose(currentValve.getNamespaceViewClose());
            valveToSave.setNamespaceViewPerifer(currentValve.getNamespaceViewPerifer());
            valveToSave.setDescriptionBlockingOpen(currentValve.getDescriptionBlockingOpen());
            valveToSave.setDescriptionBlockingClose(currentValve.getDescriptionBlockingClose());
            valveToSave.setDescriptionBlockingPerifer(currentValve.getDescriptionBlockingPerifer());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());
        valveToSave.setEditedAtValve(currentDateTime);

        new Thread(() -> {
            try {
                if (isNewValve) {
                    valveToSave.setIsEdited(1);
                    long id = repository.insertGateValve(valveToSave);
                    if (id > 0) {
                        isSaved = true;
                        runOnUiThread(() -> {
                            Toast.makeText(this, "✅ Создано", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);  // ← ДОБАВЛЕНО
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "❌ Ошибка создания", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    valveToSave.setId(currentValve.getId());
                    valveToSave.setIsEdited(1);
                    repository.updateGateValve(valveToSave);
                    isSaved = true;
                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.saved_success, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);  // ← ДОБАВЛЕНО
                        finish();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.save_error, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void deleteValve() {
        isDeleting = true;
        new Thread(() -> {
            try {
                repository.deleteGateValve(currentValve.getId());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Удалено", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);  // ← ДОБАВИТЬ (чтобы обновить список после удаления)
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "❌ Ошибка удаления", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if (isSaved || isDeleting) {
            super.onBackPressed();
            return;
        }

        if (hasChanges()) {
            Toast.makeText(this, R.string.unsaved_changes, Toast.LENGTH_SHORT).show();
        }
        super.onBackPressed();
    }

    private boolean hasChanges() {
        if (currentValve == null || originalValve == null) return false;

        return !TextUtils.equals(etIsy.getText().toString().trim(), originalValve.getIsy()) ||
                !TextUtils.equals(etName.getText().toString().trim(), originalValve.getName()) ||
                !TextUtils.equals(etPowerCabinet.getText().toString().trim(), originalValve.getPowerCabinet()) ||
                !TextUtils.equals(etLocationDescription.getText().toString().trim(), originalValve.getLocationDescription()) ||
                !TextUtils.equals(etOnPlace.getText().toString().trim(), originalValve.getOnPlace()) ||
                !TextUtils.equals(etFullName.getText().toString().trim(), originalValve.getFullName()) ||
                !TextUtils.equals(etKks.getText().toString().trim(), originalValve.getKks()) ||
                !TextUtils.equals(etNameEng.getText().toString().trim(), originalValve.getNameEng()) ||
                !TextUtils.equals(etAp50.getText().toString().trim(), originalValve.getAp50()) ||
                !TextUtils.equals(etMark.getText().toString().trim(), originalValve.getMark()) ||
                !TextUtils.equals(etCdaCabinet.getText().toString().trim(), originalValve.getCdaCabinet()) ||
                !TextUtils.equals(etCdaCabinetPosition.getText().toString().trim(), originalValve.getCdaCabinetPosition()) ||
                !TextUtils.equals(etSlot.getText().toString().trim(), originalValve.getSlot());
    }

    private GateValve copyValve(GateValve source) {
        if (source == null) return null;
        GateValve copy = new GateValve();
        copy.setId(source.getId());
        copy.setName(source.getName());
        copy.setKks(source.getKks());
        copy.setIsy(source.getIsy());
        copy.setPowerCabinet(source.getPowerCabinet());
        copy.setOnPlace(source.getOnPlace());
        copy.setFullName(source.getFullName());
        copy.setNameEng(source.getNameEng());
        copy.setAp50(source.getAp50());
        copy.setMark(source.getMark());
        copy.setCdaCabinet(source.getCdaCabinet());
        copy.setCdaCabinetPosition(source.getCdaCabinetPosition());
        copy.setSlot(source.getSlot());
        copy.setNameSpaceViewOpen(source.getNameSpaceViewOpen());
        copy.setNamespaceViewClose(source.getNamespaceViewClose());
        copy.setNamespaceViewPerifer(source.getNamespaceViewPerifer());
        copy.setDescriptionBlockingOpen(source.getDescriptionBlockingOpen());
        copy.setDescriptionBlockingClose(source.getDescriptionBlockingClose());
        copy.setDescriptionBlockingPerifer(source.getDescriptionBlockingPerifer());
        copy.setLocationDescription(source.getLocationDescription());
        return copy;
    }

    private void fixToolbarPadding(View toolbarView) {
        if (toolbarView == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(toolbarView, (view, windowInsets) -> {
            int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int leftPadding = view.getPaddingLeft();
            int rightPadding = view.getPaddingRight();
            int bottomPadding = view.getPaddingBottom();
            int topPadding = view.getPaddingTop();

            view.setPadding(leftPadding, statusBarHeight + topPadding, rightPadding, bottomPadding);
            ViewCompat.setOnApplyWindowInsetsListener(view, null);
            return windowInsets;
        });
    }

    private void setStatusBarIconsDark(boolean dark) {
        Window window = getWindow();
        if (window != null) {
            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(window, window.getDecorView());
            controller.setAppearanceLightStatusBars(dark);
        }
    }

    private void setupKeyboardAutoScroll() {
        ScrollView scrollView = findViewById(R.id.editScrollView);
        if (scrollView == null) return;

        View rootView = findViewById(android.R.id.content);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int previousHeight = 0;

            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);

                int screenHeight = rootView.getHeight();
                int keypadHeight = screenHeight - rect.bottom;

                if (keypadHeight != previousHeight) {
                    previousHeight = keypadHeight;

                    float density = getResources().getDisplayMetrics().density;
                    int basePaddingPx = (int) (16 * density);

                    int bottomPadding = keypadHeight > 0 ? keypadHeight + basePaddingPx : basePaddingPx;

                    scrollView.setPadding(
                            scrollView.getPaddingLeft(),
                            scrollView.getPaddingTop(),
                            scrollView.getPaddingRight(),
                            bottomPadding
                    );

                    if (keypadHeight > 0) {
                        scrollView.post(() -> scrollView.smoothScrollTo(0, scrollView.getHeight()));
                    }
                }
            }
        });
    }
}