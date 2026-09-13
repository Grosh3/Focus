package com.mikesuvade.focus.ui.detail;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.Sensor;
import com.mikesuvade.focus.domain.models.Setpoint;
import com.mikesuvade.focus.domain.repository.IRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;    // ← ДОБАВИТЬ
import java.util.Date;
import java.util.HashSet;        // ← ДОБАВИТЬ
import java.util.List;
import java.util.Locale;
import java.util.Set;            // ← ДОБАВИТЬ
import android.text.Editable;           // для TextWatcher
import android.text.TextWatcher;        // для преобразования регистра
import java.util.Collections;           // для Collections.sort()
import java.util.HashSet;               // для HashSet
import java.util.Set;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "entity_type";
    public static final String EXTRA_ID = "entity_id";
    public static final String EXTRA_KKS = "entity_kks";
    public static final String EXTRA_IS_NEW = "is_new";
    public static final String EXTRA_IS_FROM_USER_DB = "is_from_user_db";

    public static final int TYPE_VALVE = 1;
    public static final int TYPE_SENSOR = 2;
    public static final int TYPE_SETPOINT = 3;

    private IRepository repository;
    private int entityType;
    private boolean isNew = false;
    private boolean isSaved = false;
    private boolean isDeleting = false;
    private boolean isFromUserDb = false;

    private GateValve currentValve;
    private GateValve originalValve;
    private Sensor currentSensor;
    private Sensor originalSensor;
    private Setpoint currentSetpoint;
    private Setpoint originalSetpoint;

    private int valveId;
    private String sensorKks;
    private int setpointId;

    private View valveFields;
    private View sensorFields;
    private View setpointFields;
    private View fieldsContainer;
    private View overlayGroups;

    private EditText etName;
    private EditText etKks;
    private EditText etLocation;

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
    private EditText etLocationDescription;
    private LinearLayout extraFieldsContainer;
    private View btnToggleExtra;

    private EditText etStMarking;
    private EditText etFullNameSensor;
    private EditText etLocationSensor;
    private EditText etModelSensor;
    private EditText etModSensor;
    private EditText etAdditionalInfo;
    private EditText etMin;
    private EditText etMax;
    private EditText etUnitMeasure;
    private EditText etCva;
    private EditText etDampingTime;

    private EditText etPositionName;
    private EditText etSetpointName;
    private EditText etSetpointValue;
    private EditText etDelayTime;
    private EditText etOperation;
    private EditText etNotes;
    private EditText etEquipmentGroup;
    private EditText etSetpointLocation;

    private boolean isExtraVisible = false;
    private EditText etValveName;
    private EditText etValveKks;
    private EditText etValveFullName;
    private Button btnDelete;

    // Поля датчика
    private EditText etSensorKks;
    private int sensorId;
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
        setupNativeKeyboardHandling();

        setupEquipmentGroupAutoComplete();
        fixToolbarPadding(toolbar);
        setStatusBarIconsDark(true);
        setupListeners();

      //  setupKeyboardAutoScroll();

        entityType = getIntent().getIntExtra(EXTRA_TYPE, TYPE_VALVE);
        isNew = getIntent().getBooleanExtra(EXTRA_IS_NEW, false);

        showFieldsForType();
        if (isNew) {
            btnDelete.setVisibility(View.GONE);
        } else {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
        }

        if (isNew) {
            getSupportActionBar().setTitle(getNewTitle());
            createEmptyEntity();
            displayData();
        } else {
            loadEntityData();
        }
    }

    private void initViews() {
        fieldsContainer = findViewById(R.id.fieldsContainer);
        overlayGroups = findViewById(R.id.overlayGroups);
        btnDelete = findViewById(R.id.btnDelete);

        // Задвижка
        etIsy = findViewById(R.id.etIsy);
        etValveName = findViewById(R.id.etValveName);
        etValveKks = findViewById(R.id.etValveKks);
        etPowerCabinet = findViewById(R.id.etPowerCabinet);
        etLocationDescription = findViewById(R.id.etLocationDescription);
        etOnPlace = findViewById(R.id.etOnPlace);
        etValveFullName = findViewById(R.id.etValveFullName);
        etNameEng = findViewById(R.id.etNameEng);
        etAp50 = findViewById(R.id.etAp50);
        etMark = findViewById(R.id.etMark);
        etCdaCabinet = findViewById(R.id.etCdaCabinet);
        etCdaCabinetPosition = findViewById(R.id.etCdaCabinetPosition);
        etSlot = findViewById(R.id.etSlot);
        extraFieldsContainer = findViewById(R.id.extraFieldsContainer);
        btnToggleExtra = findViewById(R.id.btnToggleExtra);

        // Датчик
        etStMarking = findViewById(R.id.etStMarking);
        etFullNameSensor = findViewById(R.id.etFullNameSensor);
        etModelSensor = findViewById(R.id.etModelSensor);
        etModSensor = findViewById(R.id.etModSensor);
        etAdditionalInfo = findViewById(R.id.etAdditionalInfo);
        etMin = findViewById(R.id.etMin);
        etMax = findViewById(R.id.etMax);
        etUnitMeasure = findViewById(R.id.etUnitMeasure);
        etCva = findViewById(R.id.etCva);
        etDampingTime = findViewById(R.id.etDampingTime);
        etSensorKks = findViewById(R.id.etSensorKks);
        etLocationSensor = findViewById(R.id.etLocationSensor);

        // Уставка
        etPositionName = findViewById(R.id.etPositionName);
        etSetpointName = findViewById(R.id.etSetpointName);
        etSetpointValue = findViewById(R.id.etSetpointValue);
        etDelayTime = findViewById(R.id.etDelayTime);
        etOperation = findViewById(R.id.etOperation);
        etSetpointLocation = findViewById(R.id.etSetpointLocation);
        etNotes = findViewById(R.id.etNotes);
        etEquipmentGroup = findViewById(R.id.etEquipmentGroup);

        valveFields = findViewById(R.id.valveFields);
        sensorFields = findViewById(R.id.sensorFields);
        setpointFields = findViewById(R.id.setpointFields);
    }

    private void setupEquipmentGroupAutoComplete() {
        etEquipmentGroup.setFocusable(false);
        etEquipmentGroup.setClickable(true);
        etEquipmentGroup.setOnClickListener(v -> showGroupsOverlay());
        Button btnClear = findViewById(R.id.btnClearGroupSelection);
        if (btnClear != null) {
            btnClear.setOnClickListener(v -> {
                etEquipmentGroup.setText("");  // пустая строка = «нет группы»
                hideGroupsOverlay();
            });
        }
    }

    private void showFieldsForType() {
        valveFields.setVisibility(entityType == TYPE_VALVE ? View.VISIBLE : View.GONE);
        sensorFields.setVisibility(entityType == TYPE_SENSOR ? View.VISIBLE : View.GONE);
        setpointFields.setVisibility(entityType == TYPE_SETPOINT ? View.VISIBLE : View.GONE);
    }

    private String getNewTitle() {
        switch (entityType) {
            case TYPE_VALVE: return "Новая задвижка";
            case TYPE_SENSOR: return "Новый датчик";
            case TYPE_SETPOINT: return "Новая уставка";
            default: return "Новая запись";
        }
    }

    private void createEmptyEntity() {
        switch (entityType) {
            case TYPE_VALVE:
                currentValve = new GateValve();
                originalValve = new GateValve();
                break;
            case TYPE_SENSOR:
                currentSensor = new Sensor();
                originalSensor = new Sensor();
                break;
            case TYPE_SETPOINT:
                currentSetpoint = new Setpoint();
                originalSetpoint = new Setpoint();
                currentSetpoint.setEquipmentGroup("");
                break;
        }
    }

    private void loadEntityData() {
        new Thread(() -> {
            try {
                switch (entityType) {
                    case TYPE_VALVE:
                        loadValveData();
                        break;
                    case TYPE_SENSOR:
                        loadSensorData();
                        break;
                    case TYPE_SETPOINT:
                        loadSetpointData();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void loadSensorData() {
        // 🔥 ТЕПЕРЬ ПО ID, А НЕ ПО KKS
        if (!getIntent().hasExtra(EXTRA_ID)) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Ошибка: ID не передан", Toast.LENGTH_SHORT).show();
                finish();
            });
            return;
        }

        sensorId = getIntent().getIntExtra(EXTRA_ID, 0);
        isFromUserDb = sensorId < 0;

        new Thread(() -> {
            try {
                if (isFromUserDb) {
                    // Ищем в БД2 по id
                    List<Sensor> userSensors = repository.getAllUserSensors();
                    Sensor found = null;
                    for (Sensor s : userSensors) {
                        if (s.getId() == sensorId && s.getIsDeleted() != 1) {
                            found = s;
                            break;
                        }
                    }
                    if (found != null) {
                        currentSensor = found;
                        originalSensor = copySensor(found);
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(DetailActivity.this, "Датчик не найден в пользовательской БД", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                        return;
                    }
                } else {
                    // Ищем в БД1 по id
                    Sensor refSensor = repository.getSensorById(sensorId);
                    if (refSensor != null) {
                        currentSensor = refSensor;
                        originalSensor = copySensor(refSensor);
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(DetailActivity.this, "Датчик не найден в справочнике", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                        return;
                    }
                }
                runOnUiThread(this::displaySensorData);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(DetailActivity.this, "Ошибка загрузки датчика", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void loadValveData() {
        if (!getIntent().hasExtra(EXTRA_ID)) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Ошибка: ID не передан", Toast.LENGTH_SHORT).show();
                finish();
            });
            return;
        }

        valveId = getIntent().getIntExtra(EXTRA_ID, 0);
        isFromUserDb = valveId < 0;

        new Thread(() -> {
            try {
                if (isFromUserDb) {
                    List<GateValve> userValves = repository.getAllUserGateValves();
                    GateValve found = null;
                    for (GateValve v : userValves) {
                        if (v.getId() == valveId && v.getIsDeleted() != 1) {
                            found = v;
                            break;
                        }
                    }
                    if (found != null) {
                        currentValve = found;
                        originalValve = copyValve(found);
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(DetailActivity.this, "Задвижка не найдена в пользовательской БД", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                        return;
                    }
                } else {
                    GateValve refValve = repository.getGateValveById(valveId);
                    if (refValve != null) {
                        currentValve = refValve;
                        originalValve = copyValve(refValve);
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(DetailActivity.this, "Задвижка не найдена в справочнике", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                        return;
                    }
                }
                runOnUiThread(this::displayValveData);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(DetailActivity.this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void loadSetpointData() {
        if (!getIntent().hasExtra(EXTRA_ID)) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Ошибка: ID не передан", Toast.LENGTH_SHORT).show();
                finish();
            });
            return;
        }

        setpointId = getIntent().getIntExtra(EXTRA_ID, 0);
        isFromUserDb = setpointId < 0;

        new Thread(() -> {
            try {
                if (isFromUserDb) {
                    List<Setpoint> userSetpoints = repository.getAllUserSetpoints();
                    Setpoint found = null;
                    for (Setpoint sp : userSetpoints) {
                        if (sp.getId() == setpointId && sp.getIsDeleted() != 1) {
                            found = sp;
                            break;
                        }
                    }
                    if (found != null) {
                        currentSetpoint = found;
                        originalSetpoint = copySetpoint(found);
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(DetailActivity.this, "Уставка не найдена в пользовательской БД", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                        return;
                    }
                } else {
                    Setpoint refSetpoint = repository.getSetpointById(setpointId);
                    if (refSetpoint != null) {
                        currentSetpoint = refSetpoint;
                        originalSetpoint = copySetpoint(refSetpoint);
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(DetailActivity.this, "Уставка не найдена в справочнике", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                        return;
                    }
                }
                runOnUiThread(this::displaySetpointData);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(DetailActivity.this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void displayData() {
        switch (entityType) {
            case TYPE_VALVE:
                displayValveData();
                break;
            case TYPE_SENSOR:
                displaySensorData();
                break;
            case TYPE_SETPOINT:
                displaySetpointData();
                break;
        }
    }

    private void displayValveData() {
        if (currentValve == null) return;

        String isy = currentValve.getIsy();
        getSupportActionBar().setTitle((isy != null && !isy.isEmpty() ? isy : "Задвижка"));

        etIsy.setText(currentValve.getIsy());
        etValveName.setText(currentValve.getName());           // ← было etName
        etValveKks.setText(currentValve.getKks());             // ← было etKks
        etPowerCabinet.setText(currentValve.getPowerCabinet());
        etLocationDescription.setText(currentValve.getLocationDescription());
        etOnPlace.setText(currentValve.getOnPlace());
        etValveFullName.setText(currentValve.getFullName());   // ← было etFullName
        etNameEng.setText(currentValve.getNameEng());
        etAp50.setText(currentValve.getAp50());
        etMark.setText(currentValve.getMark());
        etCdaCabinet.setText(currentValve.getCdaCabinet());
        etCdaCabinetPosition.setText(currentValve.getCdaCabinetPosition());
        etSlot.setText(currentValve.getSlot());
    }

    private void displaySensorData() {
        if (currentSensor == null) return;

        String stMarking = currentSensor.getStMarkir();
        getSupportActionBar().setTitle((stMarking != null && !stMarking.isEmpty() ? stMarking : "Датчик"));

        etStMarking.setText(currentSensor.getStMarkir());
        etFullNameSensor.setText(currentSensor.getFullName());
        etLocationSensor.setText(currentSensor.getLocation());
        etSensorKks.setText(currentSensor.getKks());  // ← ИСПРАВЛЕНО! (было etKks)
        etModelSensor.setText(currentSensor.getModelSensor());
        etModSensor.setText(currentSensor.getModSensor());
        etAdditionalInfo.setText(currentSensor.getAdditionalInfo());
        etMin.setText(String.valueOf(currentSensor.getMinVal()));
        etMax.setText(String.valueOf(currentSensor.getMaxVal()));
        etUnitMeasure.setText(currentSensor.getMeasureUnit());
        etCva.setText(currentSensor.getCva());
        etDampingTime.setText(currentSensor.getDampingTime());
    }

    private void displaySetpointData() {
        if (currentSetpoint == null) return;
        String positionName = currentSetpoint.getPositionName();
        getSupportActionBar().setTitle((positionName != null && !positionName.isEmpty() ? positionName : "Уставка"));
        etPositionName.setText(currentSetpoint.getPositionName());
        etSetpointName.setText(currentSetpoint.getName());
        etSetpointValue.setText(currentSetpoint.getSetpointValue());
        etDelayTime.setText(currentSetpoint.getDelayTime());
        etOperation.setText(currentSetpoint.getOperation());
        etNotes.setText(currentSetpoint.getNotes());
        etSetpointLocation.setText(currentSetpoint.getLocation());
        String group = currentSetpoint.getEquipmentGroup();
        if (group != null && !group.isEmpty()) {
            if (!group.startsWith("#")) group = "#" + group;
            // 🔥 ПРИВОДИМ К ВЕРХНЕМУ РЕГИСТРУ ПРИ ЗАГРУЗКЕ
            etEquipmentGroup.setText(group.toUpperCase());
        } else {
            etEquipmentGroup.setText("НЕТ ГРУППЫ");
        }
    }

    private void setupListeners() {
        btnToggleExtra.setOnClickListener(v -> {
            isExtraVisible = !isExtraVisible;
            extraFieldsContainer.setVisibility(isExtraVisible ? View.VISIBLE : View.GONE);
            String text = isExtraVisible
                    ? getString(R.string.hide_extra_fields)
                    : getString(R.string.toggle_extra_fields);
            ((Button) btnToggleExtra).setText(text);
        });
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
        switch (entityType) {
            case TYPE_VALVE:
                saveValve();
                break;
            case TYPE_SENSOR:
                saveSensor();
                break;
            case TYPE_SETPOINT:
                saveSetpoint();
                break;
        }
    }

    // ============================================================
    // ✅ ИСПРАВЛЕННЫЙ saveValve() — копирует и обновляет currentValve
    // ============================================================
    private void saveValve() {
        // 🔥 Защита от двойного клика
        if (isSaved) return;
        if (currentValve == null) return;

        String isy = etIsy.getText().toString().trim();
        String name = etValveName.getText().toString().trim();          // ← ИСПРАВЛЕНО
        String powerCabinet = etPowerCabinet.getText().toString().trim();
        String locationDescription = etLocationDescription.getText().toString().trim();
        String onPlace = etOnPlace.getText().toString().trim();
        String fullName = etValveFullName.getText().toString().trim(); // ← ИСПРАВЛЕНО
        String kks = etValveKks.getText().toString().trim();           // ← ИСПРАВЛЕНО
        String nameEng = etNameEng.getText().toString().trim();
        String ap50 = etAp50.getText().toString().trim();
        String mark = etMark.getText().toString().trim();
        String cdaCabinet = etCdaCabinet.getText().toString().trim();
        String cdaCabinetPosition = etCdaCabinetPosition.getText().toString().trim();
        String slot = etSlot.getText().toString().trim();

        boolean allFieldsEmpty = isy.isEmpty() && name.isEmpty() &&
                powerCabinet.isEmpty() && locationDescription.isEmpty() &&
                onPlace.isEmpty() && fullName.isEmpty() &&
                kks.isEmpty() && nameEng.isEmpty() && ap50.isEmpty() &&
                mark.isEmpty() && cdaCabinet.isEmpty() &&
                cdaCabinetPosition.isEmpty() && slot.isEmpty();

        if (isNew && allFieldsEmpty) {
            Toast.makeText(this, "Заполните хотя бы одно поле", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNew && allFieldsEmpty) {
            showDeleteDialog("задвижку",
                    currentValve.getName() != null ? currentValve.getName() : "без названия",
                    this::deleteValve);
            return;
        }

        GateValve valveToSave = new GateValve();
        valveToSave.setIsy(isy);
        valveToSave.setName(name);
        valveToSave.setPowerCabinet(powerCabinet);
        valveToSave.setLocationDescription(locationDescription);
        valveToSave.setOnPlace(onPlace);
        valveToSave.setFullName(fullName);
        valveToSave.setKks(kks.isEmpty() ? null : kks);
        valveToSave.setNameEng(nameEng);
        valveToSave.setAp50(ap50);
        valveToSave.setMark(mark);
        valveToSave.setCdaCabinet(cdaCabinet);
        valveToSave.setCdaCabinetPosition(cdaCabinetPosition);
        valveToSave.setSlot(slot);

        if (currentValve != null) {
            valveToSave.setNameSpaceViewOpen(currentValve.getNameSpaceViewOpen());
            valveToSave.setNamespaceViewClose(currentValve.getNamespaceViewClose());
            valveToSave.setNamespaceViewPerifer(currentValve.getNamespaceViewPerifer());
            valveToSave.setDescriptionBlockingOpen(currentValve.getDescriptionBlockingOpen());
            valveToSave.setDescriptionBlockingClose(currentValve.getDescriptionBlockingClose());
            valveToSave.setDescriptionBlockingPerifer(currentValve.getDescriptionBlockingPerifer());
        }

        String currentDateTime = getCurrentDateTime();

        // 🔥 БЛОКИРУЕМ ПОВТОРНЫЙ КЛИК
        isSaved = true;

        // 🔥 ВСЯ ЛОГИКА В ФОНОВОМ ПОТОКЕ
        new Thread(() -> {
            try {
                if (isNew) {
                    // ===== СОЗДАНИЕ НОВОЙ ЗАДВИЖКИ =====
                    int newId = generateNegativeId();
                    Log.d("VALVE_DEBUG", "Generated new ID: " + newId);

                    valveToSave.setId(newId);
                    valveToSave.setOriginalId(0);
                    valveToSave.setIsDeleted(0);
                    valveToSave.setIsEdited(1);
                    valveToSave.setCustom(true);
                    valveToSave.setCreatedAt(currentDateTime);
                    valveToSave.setEditedAtValve(currentDateTime);

                    long id = repository.insertUserGateValve(valveToSave);
                    Log.d("VALVE_DEBUG", "insertUserGateValve returned: " + id);

                    // 🔥 НЕ ПЕРЕЗАПИСЫВАЕМ ID! Оставляем отрицательный
                    if (id != -1) {
                        currentValve = valveToSave;
                        runOnUiThread(() -> {
                            Toast.makeText(this, "✅ Создано", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "❌ Ошибка создания", Toast.LENGTH_SHORT).show();
                            isSaved = false;
                        });
                    }

                } else {
                    // ===== РЕДАКТИРОВАНИЕ =====
                    boolean isFromUserDb = currentValve.getId() < 0;

                    if (isFromUserDb) {
                        // === РЕДАКТИРОВАНИЕ ЗАПИСИ ИЗ БД2 ===
                        Log.d("VALVE_DEBUG", "currentValve.getCreatedAt() = " + currentValve.getCreatedAt());
                        valveToSave.setId(currentValve.getId());
                        valveToSave.setOriginalId(currentValve.getOriginalId());
                        valveToSave.setIsDeleted(0);
                        valveToSave.setIsEdited(1);
                        valveToSave.setCustom(true);
                        valveToSave.setCreatedAt(currentValve.getCreatedAt());
                        valveToSave.setEditedAtValve(currentDateTime);

                        int rows = repository.updateUserGateValve(valveToSave);
                        Log.d("VALVE_DEBUG", "updateUserGateValve rows: " + rows);

                        if (rows > 0) {
                            currentValve = valveToSave;
                            runOnUiThread(() -> {
                                Toast.makeText(this, "✅ Сохранено", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            });
                        } else {
                            currentValve = valveToSave;
                            runOnUiThread(() -> {
                                Toast.makeText(this, "✅ Сохранено", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            });
                        }

                    } else {
                        // === КОПИРОВАНИЕ ИЗ БД1 В БД2 ===
                        GateValve existing = repository.getUserGateValveByOriginalId(currentValve.getId());

                        if (existing != null) {
                            // Обновляем существующую копию
                            valveToSave.setId(existing.getId());
                            valveToSave.setOriginalId(currentValve.getId());
                            valveToSave.setIsDeleted(0);
                            valveToSave.setIsEdited(1);
                            valveToSave.setCustom(true);
                            valveToSave.setCreatedAt(existing.getCreatedAt());
                            valveToSave.setEditedAtValve(currentDateTime);

                            int rows = repository.updateUserGateValve(valveToSave);
                            Log.d("VALVE_DEBUG", "updateUserGateValve (existing copy) rows: " + rows);

                            if (rows > 0) {
                                currentValve = valveToSave;
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "✅ Сохранено", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                });
                            } else {
                                currentValve = valveToSave;
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "✅ Сохранено", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                });
                            }
                        } else {
                            // Создаём новую копию
                            int newId = generateNegativeId();
                            Log.d("VALVE_DEBUG", "Generated new ID for copy: " + newId);

                            valveToSave.setId(newId);
                            valveToSave.setOriginalId(currentValve.getId());
                            valveToSave.setIsDeleted(0);
                            valveToSave.setIsEdited(1);
                            valveToSave.setCustom(true);
                            valveToSave.setCreatedAt(currentDateTime);
                            valveToSave.setEditedAtValve(currentDateTime);

                            long id = repository.insertUserGateValve(valveToSave);
                            Log.d("VALVE_DEBUG", "insertUserGateValve (copy) returned: " + id);

                            if (id != -1) {
                                currentValve = valveToSave;
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "✅ Скопировано", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                });
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "❌ Ошибка копирования", Toast.LENGTH_SHORT).show();
                                    isSaved = false;
                                });
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("VALVE_DEBUG", "Exception in saveValve", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    isSaved = false;
                });
            }
        }).start();
    }

    private void deleteValve() {
        isDeleting = true;
        new Thread(() -> {
            try {
                boolean isFromUserDb = currentValve.getId() < 0;
                if (isFromUserDb && currentValve.getOriginalId() > 0) {
                    repository.markGateValveAsDeleted(currentValve.getOriginalId());
                } else if (isFromUserDb) {
                    repository.deleteUserGateValve(currentValve.getId());
                } else {
                    repository.markGateValveAsDeleted(currentValve.getId());
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, "Удалено", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // ============================================================
    // ✅ ИСПРАВЛЕННЫЙ saveSensor()
    // ============================================================
    private void saveSensor() {
        // 🔥 Защита от двойного клика
        if (isSaved) return;
        if (currentSensor == null) return;

        String stMarking = etStMarking.getText().toString().trim();
        String fullName = etFullNameSensor.getText().toString().trim();
        String location = etLocationSensor.getText().toString().trim();
        String kks = etSensorKks.getText().toString().trim();  // ← ИСПРАВЛЕНО!
        String modelSensor = etModelSensor.getText().toString().trim();
        String modSensor = etModSensor.getText().toString().trim();
        String additionalInfo = etAdditionalInfo.getText().toString().trim();
        double min = parseDouble(etMin.getText().toString().trim());
        double max = parseDouble(etMax.getText().toString().trim());
        String unitMeasure = etUnitMeasure.getText().toString().trim();
        String cva = etCva.getText().toString().trim();
        String dampingTime = etDampingTime.getText().toString().trim();
        Log.d("SENSOR_DEBUG", "=== saveSensor fields ===");
        Log.d("SENSOR_DEBUG", "stMarking = '" + stMarking + "'");
        Log.d("SENSOR_DEBUG", "fullName = '" + fullName + "'");
        Log.d("SENSOR_DEBUG", "location = '" + location + "'");
        Log.d("SENSOR_DEBUG", "kks = '" + kks + "'");
        Log.d("SENSOR_DEBUG", "modelSensor = '" + modelSensor + "'");
        Log.d("SENSOR_DEBUG", "modSensor = '" + modSensor + "'");
        Log.d("SENSOR_DEBUG", "additionalInfo = '" + additionalInfo + "'");
        Log.d("SENSOR_DEBUG", "min = " + min);
        Log.d("SENSOR_DEBUG", "max = " + max);
        Log.d("SENSOR_DEBUG", "unitMeasure = '" + unitMeasure + "'");
        Log.d("SENSOR_DEBUG", "cva = '" + cva + "'");
        Log.d("SENSOR_DEBUG", "dampingTime = '" + dampingTime + "'");

        boolean allFieldsEmpty = stMarking.isEmpty() && fullName.isEmpty() &&
                location.isEmpty() && kks.isEmpty() && modelSensor.isEmpty() &&
                modSensor.isEmpty() && additionalInfo.isEmpty() &&
                min == 0 && max == 0 && unitMeasure.isEmpty() &&
                cva.isEmpty() && dampingTime.isEmpty();
        Log.d("SENSOR_DEBUG", "allFieldsEmpty = " + allFieldsEmpty);
        if (isNew && allFieldsEmpty) {
            Toast.makeText(this, "Заполните хотя бы одно поле", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNew && allFieldsEmpty) {
            showDeleteDialog("датчик",
                    currentSensor.getStMarkir() != null ? currentSensor.getStMarkir() : "без названия",
                    this::deleteSensor);
            return;
        }

        Sensor sensorToSave = new Sensor();
        sensorToSave.setStMarkir(stMarking);
        sensorToSave.setFullName(fullName);
        sensorToSave.setLocation(location);
        // 🔥 НЕ ГЕНЕРИРУЕМ ФАЛЬШИВЫЙ KKS
        sensorToSave.setKks(kks);
        sensorToSave.setModelSensor(modelSensor);
        sensorToSave.setModSensor(modSensor);
        sensorToSave.setAdditionalInfo(additionalInfo);
        sensorToSave.setMinVal(min);
        sensorToSave.setMaxVal(max);
        sensorToSave.setMeasureUnit(unitMeasure);
        sensorToSave.setCva(cva);
        sensorToSave.setDampingTime(dampingTime);

        String currentDateTime = getCurrentDateTime();

        // 🔥 БЛОКИРУЕМ ПОВТОРНЫЙ КЛИК
        isSaved = true;

        // 🔥 ВСЯ ЛОГИКА В ФОНОВОМ ПОТОКЕ
        new Thread(() -> {
            try {
                if (isNew) {
                    // ===== СОЗДАНИЕ НОВОГО ДАТЧИКА =====
                    int newId = generateNegativeId();
                    Log.d("SENSOR_DEBUG", "Generated new ID: " + newId);

                    sensorToSave.setId(newId);
                    sensorToSave.setOriginalId(0);
                    sensorToSave.setIsDeleted(0);
                    sensorToSave.setIsEdited(1);
                    sensorToSave.setCustom(true);
                    sensorToSave.setCreatedAt(currentDateTime);
                    sensorToSave.setEditedAt(currentDateTime);

                    // ❌ ПРОВЕРКА НА KKS УДАЛЕНА!

                    long id = repository.insertUserSensor(sensorToSave);
                    Log.d("SENSOR_DEBUG", "insertUserSensor returned: " + id);

                    if (id != -1) {
                        currentSensor = sensorToSave;
                        runOnUiThread(() -> {
                            Toast.makeText(this, "✅ Создано", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "❌ Ошибка создания", Toast.LENGTH_SHORT).show();
                            isSaved = false;
                        });
                    }

                } else {
                    // ===== РЕДАКТИРОВАНИЕ =====
                    boolean isFromUserDb = currentSensor.getId() < 0;

                    if (isFromUserDb) {
                        // === РЕДАКТИРОВАНИЕ ЗАПИСИ ИЗ БД2 ===
                        sensorToSave.setId(currentSensor.getId());
                        sensorToSave.setOriginalId(currentSensor.getOriginalId());
                        sensorToSave.setIsDeleted(0);
                        sensorToSave.setIsEdited(1);
                        sensorToSave.setCustom(true);
                        sensorToSave.setCreatedAt(currentSensor.getCreatedAt());
                        sensorToSave.setEditedAt(currentDateTime);

                        repository.updateUserSensor(sensorToSave);
                        currentSensor = sensorToSave;
                        runOnUiThread(() -> {
                            Toast.makeText(this, "✅ Сохранено", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });

                    } else {
                        // === КОПИРОВАНИЕ ИЗ БД1 В БД2 ===
                        Sensor existing = repository.getUserSensorByOriginalId(currentSensor.getId());

                        if (existing != null) {
                            // Обновляем существующую копию
                            sensorToSave.setId(existing.getId());
                            sensorToSave.setOriginalId(currentSensor.getId());
                            sensorToSave.setIsDeleted(0);
                            sensorToSave.setIsEdited(1);
                            sensorToSave.setCustom(true);
                            sensorToSave.setCreatedAt(existing.getCreatedAt());
                            sensorToSave.setEditedAt(currentDateTime);

                            repository.updateUserSensor(sensorToSave);
                            currentSensor = sensorToSave;
                            runOnUiThread(() -> {
                                Toast.makeText(this, "✅ Сохранено", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            });
                        } else {
                            // Создаём новую копию
                            int newId = generateNegativeId();
                            Log.d("SENSOR_DEBUG", "Generated new ID for copy: " + newId);

                            sensorToSave.setId(newId);
                            sensorToSave.setOriginalId(currentSensor.getId());
                            sensorToSave.setIsDeleted(0);
                            sensorToSave.setIsEdited(1);
                            sensorToSave.setCustom(true);
                            sensorToSave.setCreatedAt(currentDateTime);
                            sensorToSave.setEditedAt(currentDateTime);

                            // ❌ ПРОВЕРКА НА KKS УДАЛЕНА!

                            long id = repository.insertUserSensor(sensorToSave);
                            Log.d("SENSOR_DEBUG", "insertUserSensor (copy) returned: " + id);

                            if (id != -1) {
                                currentSensor = sensorToSave;
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "✅ Скопировано", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                });
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "❌ Ошибка копирования", Toast.LENGTH_SHORT).show();
                                    isSaved = false;
                                });
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("SENSOR_DEBUG", "Exception in saveSensor", e);
                runOnUiThread(() -> {
                    Toast.makeText(DetailActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    isSaved = false;
                });
            }
        }).start();
    }

    private void deleteSensor() {
        isDeleting = true;
        new Thread(() -> {
            try {
                boolean isFromUserDb = currentSensor.getId() < 0;

                if (isFromUserDb && currentSensor.getOriginalId() > 0) {
                    // ✅ ИСПРАВЛЕНО: используем originalId
                    repository.markSensorAsDeleted(currentSensor.getOriginalId());
                } else if (isFromUserDb) {
                    repository.deleteUserSensor(currentSensor.getId());
                } else {
                    // ✅ ИСПРАВЛЕНО: используем id (ID из БД1)
                    repository.markSensorAsDeleted(currentSensor.getId());
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, "Удалено", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // ============================================================
    // ✅ ИСПРАВЛЕННЫЙ saveSetpoint() — копирует и обновляет currentSetpoint
    // ============================================================
    private void saveSetpoint() {
        // 🔥 Защита от двойного клика
        if (isSaved) return;
        if (currentSetpoint == null) return;

        String positionName = etPositionName.getText().toString().trim();
        String name = etSetpointName.getText().toString().trim();
        String setpointValue = etSetpointValue.getText().toString().trim();
        String delayTime = etDelayTime.getText().toString().trim();
        String operation = etOperation.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String equipmentGroup = etEquipmentGroup.getText().toString().trim().toUpperCase(); // 🔥 ВЕРХНИЙ РЕГИСТР
        String location = etSetpointLocation.getText().toString().trim();
        if (equipmentGroup.equals("НЕТ ГРУППЫ")
                || equipmentGroup.equals("#НЕТ ГРУППЫ")
                || equipmentGroup.equals("🚫 НЕТ ГРУППЫ")) {
            equipmentGroup = "";
        }
        if (!equipmentGroup.isEmpty() && !equipmentGroup.startsWith("#")) {
            equipmentGroup = "#" + equipmentGroup;
        }

        boolean allFieldsEmpty = positionName.isEmpty() && name.isEmpty() &&
                setpointValue.isEmpty() && delayTime.isEmpty() &&
                operation.isEmpty() && notes.isEmpty() &&
                equipmentGroup.isEmpty() && location.isEmpty();

        if (isNew && allFieldsEmpty) {
            Toast.makeText(this, "Заполните хотя бы одно поле", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNew && allFieldsEmpty) {
            showDeleteDialog("уставку",
                    currentSetpoint.getPositionName() != null ? currentSetpoint.getPositionName() : "без названия",
                    this::deleteSetpoint);
            return;
        }

        Setpoint setpointToSave = new Setpoint();
        setpointToSave.setPositionName(positionName);
        setpointToSave.setName(name);
        setpointToSave.setSetpointValue(setpointValue);
        setpointToSave.setDelayTime(delayTime);
        setpointToSave.setOperation(operation);
        setpointToSave.setNotes(notes);
        setpointToSave.setEquipmentGroup(equipmentGroup);
        setpointToSave.setLocation(location);

        String currentDateTime = getCurrentDateTime();

        // 🔥 БЛОКИРУЕМ ПОВТОРНЫЙ КЛИК
        isSaved = true;

        // 🔥 ВСЯ ЛОГИКА В ФОНОВОМ ПОТОКЕ
        new Thread(() -> {
            try {
                if (isNew) {
                    // ===== СОЗДАНИЕ НОВОЙ УСТАВКИ =====
                    int newId = generateNegativeId();
                    Log.d("SETPOINT_DEBUG", "Generated new ID: " + newId);

                    setpointToSave.setId(newId);
                    setpointToSave.setOriginalId(0);
                    setpointToSave.setIsDeleted(0);
                    setpointToSave.setIsEdited(1);
                    setpointToSave.setCustom(true);
                    setpointToSave.setCreatedAt(currentDateTime);
                    setpointToSave.setEditedAt(currentDateTime);

                    long id = repository.insertUserSetpoint(setpointToSave);
                    Log.d("SETPOINT_DEBUG", "insertUserSetpoint returned: " + id);

                    // 🔥 НЕ ПЕРЕЗАПИСЫВАЕМ ID! Оставляем отрицательный
                    if (id != -1) {
                        currentSetpoint = setpointToSave;
                        runOnUiThread(() -> {
                            Toast.makeText(this, "✅ Создано", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "❌ Ошибка создания", Toast.LENGTH_SHORT).show();
                            isSaved = false;
                        });
                    }

                } else {
                    // ===== РЕДАКТИРОВАНИЕ =====
                    boolean isFromUserDb = currentSetpoint.getId() < 0;

                    if (isFromUserDb) {
                        // === РЕДАКТИРОВАНИЕ ЗАПИСИ ИЗ БД2 ===
                        setpointToSave.setId(currentSetpoint.getId());
                        setpointToSave.setOriginalId(currentSetpoint.getOriginalId());
                        setpointToSave.setIsDeleted(0);
                        setpointToSave.setIsEdited(1);
                        setpointToSave.setCustom(true);
                        setpointToSave.setCreatedAt(currentSetpoint.getCreatedAt());
                        setpointToSave.setEditedAt(currentDateTime);

                        int rows = repository.updateUserSetpoint(setpointToSave);
                        Log.d("SETPOINT_DEBUG", "updateUserSetpoint rows: " + rows);

                        if (rows > 0) {
                            currentSetpoint = setpointToSave;
                            runOnUiThread(() -> {
                                Toast.makeText(this, "✅ Сохранено", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            });
                        } else {
                            currentSetpoint = setpointToSave;
                            runOnUiThread(() -> {
                                Toast.makeText(this, "✅ Сохранено", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            });
                        }

                    } else {
                        // === КОПИРОВАНИЕ ИЗ БД1 В БД2 ===
                        Setpoint existing = repository.getUserSetpointByOriginalId(currentSetpoint.getId());

                        if (existing != null) {
                            // Обновляем существующую копию
                            setpointToSave.setId(existing.getId());
                            setpointToSave.setOriginalId(currentSetpoint.getId());
                            setpointToSave.setIsDeleted(0);
                            setpointToSave.setIsEdited(1);
                            setpointToSave.setCustom(true);
                            setpointToSave.setCreatedAt(existing.getCreatedAt());
                            setpointToSave.setEditedAt(currentDateTime);

                            int rows = repository.updateUserSetpoint(setpointToSave);
                            Log.d("SETPOINT_DEBUG", "updateUserSetpoint (existing copy) rows: " + rows);

                            if (rows > 0) {
                                currentSetpoint = setpointToSave;
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "✅ Сохранено", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                });
                            } else {
                                currentSetpoint = setpointToSave;
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "✅ Сохранено", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                });
                            }
                        } else {
                            // Создаём новую копию
                            int newId = generateNegativeId();
                            Log.d("SETPOINT_DEBUG", "Generated new ID for copy: " + newId);

                            setpointToSave.setId(newId);
                            setpointToSave.setOriginalId(currentSetpoint.getId());
                            setpointToSave.setIsDeleted(0);
                            setpointToSave.setIsEdited(1);
                            setpointToSave.setCustom(true);
                            setpointToSave.setCreatedAt(currentDateTime);
                            setpointToSave.setEditedAt(currentDateTime);

                            long id = repository.insertUserSetpoint(setpointToSave);
                            Log.d("SETPOINT_DEBUG", "insertUserSetpoint (copy) returned: " + id);

                            if (id != -1) {
                                currentSetpoint = setpointToSave;
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "✅ Скопировано", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                });
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "❌ Ошибка копирования", Toast.LENGTH_SHORT).show();
                                    isSaved = false;
                                });
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("SETPOINT_DEBUG", "Exception in saveSetpoint", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    isSaved = false;
                });
            }
        }).start();
    }

    private void deleteSetpoint() {
        isDeleting = true;
        new Thread(() -> {
            try {
                boolean isFromUserDb = currentSetpoint.getId() < 0;
                if (isFromUserDb && currentSetpoint.getOriginalId() > 0) {
                    repository.markSetpointAsDeleted(currentSetpoint.getOriginalId());
                } else if (isFromUserDb) {
                    repository.deleteUserSetpoint(currentSetpoint.getId());
                } else {
                    repository.markSetpointAsDeleted(currentSetpoint.getId());
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, "Удалено", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // ============================================================
    // 🧩 ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    private void showDeleteDialog(String entityName, String entityTitle, Runnable deleteAction) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить " + entityName + "?")
                .setMessage("Все поля очищены. Удалить " + entityName + " \"" + entityTitle + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteAction.run())
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .show();
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
        copy.setOriginalId(source.getOriginalId());
        copy.setIsDeleted(source.getIsDeleted());
        copy.setIsEdited(source.getIsEdited());
        copy.setEditedAtValve(source.getEditedAtValve());
        copy.setCreatedAt(source.getCreatedAt());
        return copy;
    }

    private Sensor copySensor(Sensor source) {
        if (source == null) return null;
        Sensor copy = new Sensor();
        copy.setId(source.getId());
        copy.setKeynum(source.getKeynum());
        copy.setFa(source.getFa());
        copy.setKks(source.getKks());
        copy.setStMarkir(source.getStMarkir());
        copy.setFullName(source.getFullName());
        copy.setName(source.getName());
        copy.setMedia(source.getMedia());
        copy.setUnits(source.getUnits());
        copy.setNominal(source.getNominal());
        copy.setVolMin(source.getVolMin());
        copy.setVolMax(source.getVolMax());
        copy.setSpeed(source.getSpeed());
        copy.setFaultPar(source.getFaultPar());
        copy.setInsteadF(source.getInsteadF());
        copy.setFilter(source.getFilter());
        copy.setModelSensor(source.getModelSensor());
        copy.setModSensor(source.getModSensor());
        copy.setAdditionalInfo(source.getAdditionalInfo());
        copy.setMinVal(source.getMinVal());
        copy.setMaxVal(source.getMaxVal());
        copy.setMeasureUnit(source.getMeasureUnit());
        copy.setLocation(source.getLocation());
        copy.setCva(source.getCva());
        copy.setDampingTime(source.getDampingTime());
        copy.setOriginalKks(source.getOriginalKks());
        copy.setIsDeleted(source.getIsDeleted());
        copy.setIsEdited(source.getIsEdited());
        copy.setEditedAt(source.getEditedAt());
        copy.setCreatedAt(source.getCreatedAt());
        return copy;
    }

    private Setpoint copySetpoint(Setpoint source) {
        if (source == null) return null;
        Setpoint copy = new Setpoint();
        copy.setId(source.getId());
        copy.setName(source.getName());
        copy.setPositionName(source.getPositionName());
        copy.setLocation(source.getLocation());
        copy.setSetpointValue(source.getSetpointValue());
        copy.setDelayTime(source.getDelayTime());
        copy.setOperation(source.getOperation());
        copy.setNotes(source.getNotes());
        copy.setEquipmentGroup(source.getEquipmentGroup());
        copy.setOriginalId(source.getOriginalId());
        copy.setIsDeleted(source.getIsDeleted());
        copy.setIsEdited(source.getIsEdited());
        copy.setEditedAt(source.getEditedAt());
        copy.setCreatedAt(source.getCreatedAt());
        return copy;
    }

    private double parseDouble(String value) {
        if (value == null || value.isEmpty()) return 0;
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ✅ ИСПРАВЛЕННЫЙ generateNewId() — всегда ищет свободный ID


    private void showGroupsOverlay() {
        hideKeyboard();
        fieldsContainer.setVisibility(View.GONE);
        overlayGroups.setVisibility(View.VISIBLE);

        // Клик на фон закрывает оверлей
        overlayGroups.setOnClickListener(v -> hideGroupsOverlay());

        // Загружаем группы в фоновом потоке
        new Thread(() -> {
            try {
                // Получаем группы из БД1 + БД2 (уже в верхнем регистре)
                List<String> groups = repository.getAllEquipmentGroupsWithUser();

                // Добавляем группы из ресурсов (уникальные)
                String[] resourceGroups = getResources().getStringArray(R.array.equipment_groups);
                Set<String> uniqueGroups = new HashSet<>(groups);
                for (String g : resourceGroups) {
                    if (g != null && !g.isEmpty()) {
                        // 🔥 ПРИВОДИМ К ВЕРХНЕМУ РЕГИСТРУ
                        uniqueGroups.add(g.toUpperCase());
                    }
                }

                List<String> allGroups = new ArrayList<>(uniqueGroups);
                Collections.sort(allGroups);

                // Добавляем пункт "Добавить группу" в начало
                allGroups.add(0, "➕ ДОБАВИТЬ ГРУППУ");

                runOnUiThread(() -> displayGroupsList(allGroups));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка загрузки групп", Toast.LENGTH_SHORT).show();
                    hideGroupsOverlay();
                });
            }
        }).start();
    }



    private void hideGroupsOverlay() {
        overlayGroups.setVisibility(View.GONE);
        fieldsContainer.setVisibility(View.VISIBLE);
    }



    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            view.clearFocus();
        }
    }

    // Лучший вариант - асинхронный



    private void displayGroupsList(List<String> groups) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                groups
        );

        ListView listView = new ListView(this);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        listView.setPadding(0, 0, 0, 0);
        listView.setCacheColorHint(android.graphics.Color.TRANSPARENT);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = groups.get(position);
            if (selected.equals("➕ ДОБАВИТЬ ГРУППУ")) {
                showAddGroupDialog();
            } else {
                etEquipmentGroup.setText(selected);
                hideGroupsOverlay();
            }
        });

        FrameLayout container = findViewById(R.id.rvGroups);
        container.removeAllViews();
        container.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        container.setPadding(0, 0, 0, 0);

        container.addView(listView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
    }


    private void showAddGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить группу");
        final EditText input = new EditText(this);
        input.setHint("Введите название группы");
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        // 🔥 ПРЕОБРАЗОВАНИЕ В ВЕРХНИЙ РЕГИСТР ПРИ ВВОДЕ
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (!text.equals(text.toUpperCase())) {
                    s.replace(0, s.length(), text.toUpperCase());
                }
            }
        });

        builder.setView(input);
        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String group = input.getText().toString().trim().toUpperCase(); // 🔥 ВЕРХНИЙ РЕГИСТР
            if (!group.isEmpty()) {
                if (!group.startsWith("#")) {
                    group = "#" + group;
                }
                etEquipmentGroup.setText(group);
                hideGroupsOverlay();
                Toast.makeText(this, "Группа добавлена: " + group, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    public void onOverlayClick(View v) {
        hideGroupsOverlay();
    }

    @Override
    public void onBackPressed() {
        if (isSaved || isDeleting) {
            super.onBackPressed();
            return;
        }
        if (overlayGroups != null && overlayGroups.getVisibility() == View.VISIBLE) {
            hideGroupsOverlay();
            return;
        }
        if (hasChanges()) {
            Toast.makeText(this, R.string.unsaved_changes, Toast.LENGTH_SHORT).show();
        }
        super.onBackPressed();
    }

    private boolean hasChanges() {
        switch (entityType) {
            case TYPE_VALVE:
                return hasValveChanges();
            case TYPE_SENSOR:
                return hasSensorChanges();
            case TYPE_SETPOINT:
                return hasSetpointChanges();
            default:
                return false;
        }
    }

    private boolean hasValveChanges() {
        if (currentValve == null || originalValve == null) return false;

        try {
            // 🔥 ИСПРАВЛЕНО: используем правильные переменные (etValveName, etValveKks, etValveFullName)
            return !TextUtils.equals(etIsy.getText().toString().trim(), originalValve.getIsy()) ||
                    !TextUtils.equals(etValveName.getText().toString().trim(), originalValve.getName()) ||
                    !TextUtils.equals(etPowerCabinet.getText().toString().trim(), originalValve.getPowerCabinet()) ||
                    !TextUtils.equals(etLocationDescription.getText().toString().trim(), originalValve.getLocationDescription()) ||
                    !TextUtils.equals(etOnPlace.getText().toString().trim(), originalValve.getOnPlace()) ||
                    !TextUtils.equals(etValveFullName.getText().toString().trim(), originalValve.getFullName()) ||
                    !TextUtils.equals(etValveKks.getText().toString().trim(), originalValve.getKks()) ||
                    !TextUtils.equals(etNameEng.getText().toString().trim(), originalValve.getNameEng()) ||
                    !TextUtils.equals(etAp50.getText().toString().trim(), originalValve.getAp50()) ||
                    !TextUtils.equals(etMark.getText().toString().trim(), originalValve.getMark()) ||
                    !TextUtils.equals(etCdaCabinet.getText().toString().trim(), originalValve.getCdaCabinet()) ||
                    !TextUtils.equals(etCdaCabinetPosition.getText().toString().trim(), originalValve.getCdaCabinetPosition()) ||
                    !TextUtils.equals(etSlot.getText().toString().trim(), originalValve.getSlot());
        } catch (NullPointerException e) {
            Log.e("DetailActivity", "hasValveChanges: NPE", e);
            return false;
        }
    }

    private boolean hasSensorChanges() {
        if (currentSensor == null || originalSensor == null) return false;
        return !TextUtils.equals(etStMarking.getText().toString().trim(), originalSensor.getStMarkir()) ||
                !TextUtils.equals(etFullNameSensor.getText().toString().trim(), originalSensor.getFullName()) ||
                !TextUtils.equals(etLocationSensor.getText().toString().trim(), originalSensor.getLocation()) ||
                !TextUtils.equals(etSensorKks.getText().toString().trim(), originalSensor.getKks()) ||  // ← было etKks, теперь etSensorKks
                !TextUtils.equals(etModelSensor.getText().toString().trim(), originalSensor.getModelSensor()) ||
                !TextUtils.equals(etModSensor.getText().toString().trim(), originalSensor.getModSensor()) ||
                !TextUtils.equals(etAdditionalInfo.getText().toString().trim(), originalSensor.getAdditionalInfo()) ||
                !TextUtils.equals(etMin.getText().toString().trim(), String.valueOf(originalSensor.getMinVal())) ||
                !TextUtils.equals(etMax.getText().toString().trim(), String.valueOf(originalSensor.getMaxVal())) ||
                !TextUtils.equals(etUnitMeasure.getText().toString().trim(), originalSensor.getMeasureUnit()) ||
                !TextUtils.equals(etCva.getText().toString().trim(), originalSensor.getCva()) ||
                !TextUtils.equals(etDampingTime.getText().toString().trim(), originalSensor.getDampingTime());
    }

    private boolean hasSetpointChanges() {
        if (currentSetpoint == null || originalSetpoint == null) return false;
        String currentGroup = etEquipmentGroup.getText().toString().trim().toUpperCase();
        if (currentGroup.equals("НЕТ ГРУППЫ") || currentGroup.equals("#НЕТ ГРУППЫ")) currentGroup = "";

        String originalGroup = originalSetpoint.getEquipmentGroup() != null
                ? originalSetpoint.getEquipmentGroup() : "";

        return !TextUtils.equals(etPositionName.getText().toString().trim(), originalSetpoint.getPositionName()) ||
                !TextUtils.equals(etSetpointName.getText().toString().trim(), originalSetpoint.getName()) ||
                !TextUtils.equals(etSetpointValue.getText().toString().trim(), originalSetpoint.getSetpointValue()) ||
                !TextUtils.equals(etDelayTime.getText().toString().trim(), originalSetpoint.getDelayTime()) ||
                !TextUtils.equals(etOperation.getText().toString().trim(), originalSetpoint.getOperation()) ||
                !TextUtils.equals(etNotes.getText().toString().trim(), originalSetpoint.getNotes()) ||
                !TextUtils.equals(etEquipmentGroup.getText().toString().trim(), originalSetpoint.getEquipmentGroup()) ||
                !TextUtils.equals(etSetpointLocation.getText().toString().trim(), originalSetpoint.getLocation());
    }

    private void fixToolbarPadding(View toolbarView) {
        if (toolbarView == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(toolbarView, (view, windowInsets) -> {
            int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            view.setPadding(view.getPaddingLeft(), statusBarHeight + view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
            ViewCompat.setOnApplyWindowInsetsListener(view, null);
            return windowInsets;
        });
    }

    private void setStatusBarIconsDark(boolean dark) {
        Window window = getWindow();
        if (window != null) {
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
            controller.setAppearanceLightStatusBars(dark);
        }
    }

    private void setupKeyboardAutoScroll() {
        ScrollView scrollView = findViewById(R.id.fieldsContainer);
        if (scrollView == null) return;
        View rootView = findViewById(android.R.id.content);
        final Handler handler = new Handler(Looper.getMainLooper());
        final View[] focusedView = {null};

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);
                int screenHeight = rootView.getHeight();
                int keypadHeight = screenHeight - rect.bottom;

                if (keypadHeight > 100) {
                    View currentFocus = getCurrentFocus();
                    if (currentFocus != null && currentFocus instanceof EditText) {
                        focusedView[0] = currentFocus;
                    }
                    if (focusedView[0] != null) {
                        handler.postDelayed(() -> {
                            scrollView.smoothScrollTo(0, focusedView[0].getBottom() + 50);
                        }, 300);
                    }
                }
            }
        });
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private String generateUniqueKks() {
        return "USR_" + System.currentTimeMillis();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSaved) {
            loadEntityData();
        }
    }
    private int generateNegativeId() {
        int hash = java.util.UUID.randomUUID().hashCode();
        if (hash == 0) return -1;
        return hash > 0 ? -hash : hash;
    }
    // В DetailActivity.java добавьте:

    private void setupNativeKeyboardHandling() {
        ScrollView scrollView = findViewById(R.id.fieldsContainer);
        if (scrollView == null) return;

        // clipToPadding=false жизненно необходим, чтобы контент мог прокручиваться ДО САМОГО НИЗА
        scrollView.setClipToPadding(false);

        // Подписываемся на инсеты ВСЕГО ЭКРАНА (window.getDecorView())
        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {
            // Узнаем, видима ли клавиатура прямо сейчас
            boolean isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());

            // Получаем чистую высоту клавиатуры
            int keyboardHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            // Получаем высоту нижней системной панели навигации (жесты/кнопки)
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            if (isKeyboardVisible && keyboardHeight > 0) {
                // Клавиатура открыта: выставляем нижний паддинг (высота клавы + запас)
                int paddingBottom = keyboardHeight + (int) (24 * getResources().getDisplayMetrics().density);
                scrollView.setPadding(
                        scrollView.getPaddingLeft(),
                        scrollView.getPaddingTop(),
                        scrollView.getPaddingRight(),
                        paddingBottom
                );

                // Мягко докручиваем скролл до активного EditText
                View currentFocus = getCurrentFocus();
                if (currentFocus instanceof EditText) {
                    final View focusedView = currentFocus;
                    scrollView.postDelayed(() -> {
                        if (focusedView.isFocused()) {
                            // Рассчитываем позицию и плавно скроллим
                            int scrollToY = focusedView.getBottom() + (int) (32 * getResources().getDisplayMetrics().density);
                            scrollView.smoothScrollTo(0, scrollToY);
                        }
                    }, 150); // Небольшая задержка, чтобы разметка успела адаптироваться
                }
            } else {
                // Клавиатура скрыта: возвращаем паддинг, равный высоте панели навигации (для Edge-to-Edge)
                scrollView.setPadding(
                        scrollView.getPaddingLeft(),
                        scrollView.getPaddingTop(),
                        scrollView.getPaddingRight(),
                        navBarHeight + (int) (16 * getResources().getDisplayMetrics().density)
                );
            }

            // ВАЖНО: Возвращаем insets дальше, чтобы fixToolbarPadding() тоже мог их прочитать
            return insets;
        });
    }
    private void showDeleteConfirmationDialog() {
        String entityName;
        String displayName;

        switch (entityType) {
            case TYPE_VALVE:
                entityName = "задвижку";
                displayName = currentValve != null && currentValve.getIsy() != null
                        ? currentValve.getIsy()
                        : (currentValve != null ? currentValve.getName() : "");
                break;
            case TYPE_SENSOR:
                entityName = "датчик";
                displayName = currentSensor != null && currentSensor.getStMarkir() != null
                        ? currentSensor.getStMarkir()
                        : "";
                break;
            case TYPE_SETPOINT:
                entityName = "уставку";
                displayName = currentSetpoint != null && currentSetpoint.getPositionName() != null
                        ? currentSetpoint.getPositionName()
                        : (currentSetpoint != null ? currentSetpoint.getName() : "");
                break;
            default:
                return;
        }

        if (displayName == null || displayName.isEmpty()) displayName = "без названия";

        new AlertDialog.Builder(this)
                .setTitle("Удалить " + entityName + "?")
                .setMessage("Удалить \"" + displayName + "\"?\nДействие нельзя отменить.")
                .setPositiveButton("Удалить", (d, w) -> performDelete())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void performDelete() {
        switch (entityType) {
            case TYPE_VALVE:    deleteValve();    break;
            case TYPE_SENSOR:   deleteSensor();   break;
            case TYPE_SETPOINT: deleteSetpoint(); break;
        }
    }
}