package com.mikesuvade.focus.ui.detail;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.mikesuvade.focus.domain.models.Sensor;
import com.mikesuvade.focus.domain.models.Setpoint;
import com.mikesuvade.focus.domain.repository.IRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    // ==========================================
    // 🏷️ КОНСТАНТЫ
    // ==========================================

    public static final String EXTRA_TYPE = "entity_type";
    public static final String EXTRA_ID = "entity_id";
    public static final String EXTRA_KKS = "entity_kks";
    public static final String EXTRA_IS_NEW = "is_new";

    public static final int TYPE_VALVE = 1;
    public static final int TYPE_SENSOR = 2;
    public static final int TYPE_SETPOINT = 3;

    // ==========================================
    // 📦 ПОЛЯ
    // ==========================================

    private IRepository repository;
    private int entityType;
    private boolean isNew = false;
    private boolean isSaved = false;
    private boolean isDeleting = false;

    // Объекты для разных типов
    private GateValve currentValve;
    private GateValve originalValve;
    private Sensor currentSensor;
    private Sensor originalSensor;
    private Setpoint currentSetpoint;
    private Setpoint originalSetpoint;

    private int valveId;
    private String sensorKks;
    private int setpointId;

    // UI элементы
    private View valveFields;
    private View sensorFields;
    private View setpointFields;

    // Общие поля
    private EditText etName;
    private EditText etKks;
    private EditText etLocation;

    // Поля задвижки
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

    // Поля датчика
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

    // Поля уставки
    private EditText etPositionName;
    private EditText etSetpointValue;
    private EditText etDelayTime;
    private EditText etOperation;
    private EditText etNotes;
    private EditText etEquipmentGroup;
    private EditText etLocationSetpoint;

    private boolean isExtraVisible = false;

    // ==========================================
    // 🚀 ЖИЗНЕННЫЙ ЦИКЛ
    // ==========================================

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
        // 📥 ЧТЕНИЕ ПАРАМЕТРОВ
        // ==========================================

        entityType = getIntent().getIntExtra(EXTRA_TYPE, TYPE_VALVE);
        isNew = getIntent().getBooleanExtra(EXTRA_IS_NEW, false);

        showFieldsForType();

        if (isNew) {
            // 🔥 НОВАЯ ЗАПИСЬ → ВСЕГДА В БД2
            getSupportActionBar().setTitle(getNewTitle());
            createEmptyEntity();
            displayData();
        } else {
            loadEntityData();
        }
    }

    // ==========================================
    // 🔧 ИНИЦИАЛИЗАЦИЯ
    // ==========================================

    private void initViews() {
        // Общие поля
        etName = findViewById(R.id.etName);
        etKks = findViewById(R.id.etKks);
        etLocation = findViewById(R.id.etLocation);

        // Контейнеры для разных типов
        valveFields = findViewById(R.id.valveFields);
        sensorFields = findViewById(R.id.sensorFields);
        setpointFields = findViewById(R.id.setpointFields);

        // Поля задвижки
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
        etLocationDescription = findViewById(R.id.etLocationDescription);
        extraFieldsContainer = findViewById(R.id.extraFieldsContainer);
        btnToggleExtra = findViewById(R.id.btnToggleExtra);

        // Поля датчика
        etStMarking = findViewById(R.id.etStMarking);
        etFullNameSensor = findViewById(R.id.etFullNameSensor);
        etLocationSensor = findViewById(R.id.etLocationSensor);
        etModelSensor = findViewById(R.id.etModelSensor);
        etModSensor = findViewById(R.id.etModSensor);
        etAdditionalInfo = findViewById(R.id.etAdditionalInfo);
        etMin = findViewById(R.id.etMin);
        etMax = findViewById(R.id.etMax);
        etUnitMeasure = findViewById(R.id.etUnitMeasure);
        etCva = findViewById(R.id.etCva);
        etDampingTime = findViewById(R.id.etDampingTime);

        // Поля уставки
        etPositionName = findViewById(R.id.etPositionName);
        etSetpointValue = findViewById(R.id.etSetpointValue);
        etDelayTime = findViewById(R.id.etDelayTime);
        etOperation = findViewById(R.id.etOperation);
        etNotes = findViewById(R.id.etNotes);
        etEquipmentGroup = findViewById(R.id.etEquipmentGroup);
        etLocationSetpoint = findViewById(R.id.etLocationSetpoint);
    }

    private void showFieldsForType() {
        valveFields.setVisibility(entityType == TYPE_VALVE ? View.VISIBLE : View.GONE);
        sensorFields.setVisibility(entityType == TYPE_SENSOR ? View.VISIBLE : View.GONE);
        setpointFields.setVisibility(entityType == TYPE_SETPOINT ? View.VISIBLE : View.GONE);

        // Общие поля переиспользуются
        etName.setVisibility(entityType == TYPE_VALVE ? View.VISIBLE : View.GONE);
        etKks.setVisibility(View.VISIBLE); // KKS есть у всех
        etLocation.setVisibility(entityType == TYPE_SENSOR || entityType == TYPE_SETPOINT ? View.VISIBLE : View.GONE);
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
                break;
        }
    }

    // ==========================================
    // 📥 ЗАГРУЗКА ДАННЫХ
    // ==========================================

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

    private void loadValveData() {
        valveId = getIntent().getIntExtra(EXTRA_ID, -1);
        if (valveId == -1) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Ошибка: ID не передан", Toast.LENGTH_SHORT).show();
                finish();
            });
            return;
        }

        // Сначала проверяем БД2
        GateValve userValve = repository.getUserGateValveByOriginalId(valveId);
        if (userValve != null && userValve.getIsDeleted() != 1) {
            currentValve = userValve;
            originalValve = copyValve(userValve);
        } else {
            GateValve refValve = repository.getGateValveById(valveId);
            if (refValve != null) {
                currentValve = refValve;
                originalValve = copyValve(refValve);
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Задвижка не найдена", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }
        }

        runOnUiThread(this::displayData);
    }

    private void loadSensorData() {
        sensorKks = getIntent().getStringExtra(EXTRA_KKS);
        if (sensorKks == null || sensorKks.isEmpty()) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Ошибка: KKS не передан", Toast.LENGTH_SHORT).show();
                finish();
            });
            return;
        }

        Sensor userSensor = repository.getUserSensorByOriginalKks(sensorKks);
        if (userSensor != null && userSensor.getIsDeleted() != 1) {
            currentSensor = userSensor;
            originalSensor = copySensor(userSensor);
        } else {
            Sensor refSensor = repository.getSensorByKks(sensorKks);
            if (refSensor != null) {
                currentSensor = refSensor;
                originalSensor = copySensor(refSensor);
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Датчик не найден", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }
        }

        runOnUiThread(this::displayData);
    }

    private void loadSetpointData() {
        setpointId = getIntent().getIntExtra(EXTRA_ID, -1);
        if (setpointId == -1) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Ошибка: ID не передан", Toast.LENGTH_SHORT).show();
                finish();
            });
            return;
        }

        Setpoint userSetpoint = repository.getUserSetpointByOriginalId(setpointId);
        if (userSetpoint != null && userSetpoint.getIsDeleted() != 1) {
            currentSetpoint = userSetpoint;
            originalSetpoint = copySetpoint(userSetpoint);
        } else {
            Setpoint refSetpoint = repository.getSetpointById(setpointId);
            if (refSetpoint != null) {
                currentSetpoint = refSetpoint;
                originalSetpoint = copySetpoint(refSetpoint);
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Уставка не найдена", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }
        }

        runOnUiThread(this::displayData);
    }

    // ==========================================
    // 📊 ОТОБРАЖЕНИЕ ДАННЫХ
    // ==========================================

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
        if (isy != null && !isy.isEmpty()) {
            getSupportActionBar().setTitle(isy + " - Задвижка");
        } else {
            getSupportActionBar().setTitle("Задвижка");
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

    private void displaySensorData() {
        if (currentSensor == null) return;

        String stMarking = currentSensor.getStMarkir();
        if (stMarking != null && !stMarking.isEmpty()) {
            getSupportActionBar().setTitle(stMarking + " - Датчик");
        } else {
            getSupportActionBar().setTitle("Датчик");
        }

        etStMarking.setText(currentSensor.getStMarkir());
        etFullNameSensor.setText(currentSensor.getFullName());
        etLocationSensor.setText(currentSensor.getLocation());
        etKks.setText(currentSensor.getKks());
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
        if (positionName != null && !positionName.isEmpty()) {
            getSupportActionBar().setTitle(positionName + " - Уставка");
        } else {
            getSupportActionBar().setTitle("Уставка");
        }

        etPositionName.setText(currentSetpoint.getPositionName());
        etName.setText(currentSetpoint.getName());
        etSetpointValue.setText(currentSetpoint.getSetpointValue());
        etDelayTime.setText(currentSetpoint.getDelayTime());
        etOperation.setText(currentSetpoint.getOperation());
        etNotes.setText(currentSetpoint.getNotes());
        etEquipmentGroup.setText(currentSetpoint.getEquipmentGroup());
        etLocationSetpoint.setText(currentSetpoint.getLocation());
    }

    // ==========================================
    // 🔧 СЛУШАТЕЛИ
    // ==========================================

    private void setupListeners() {
        btnToggleExtra.setOnClickListener(v -> {
            isExtraVisible = !isExtraVisible;
            extraFieldsContainer.setVisibility(isExtraVisible ? View.VISIBLE : View.GONE);
            String text = isExtraVisible
                    ? getString(R.string.hide_extra_fields)
                    : getString(R.string.toggle_extra_fields);
            ((android.widget.Button) btnToggleExtra).setText(text);
        });
    }

    // ==========================================
    // 💾 СОХРАНЕНИЕ
    // ==========================================

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

    // ==========================================
    // 💾 СОХРАНЕНИЕ ЗАДВИЖКИ
    // ==========================================

    private void saveValve() {
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

        boolean allFieldsEmpty = isy.isEmpty() && name.isEmpty() &&
                powerCabinet.isEmpty() && locationDescription.isEmpty() &&
                onPlace.isEmpty() && fullName.isEmpty() &&
                kks.isEmpty() && nameEng.isEmpty() && ap50.isEmpty() &&
                mark.isEmpty() && cdaCabinet.isEmpty() &&
                cdaCabinetPosition.isEmpty() && slot.isEmpty();

        // 🔥 НОВАЯ ЗАПИСЬ + ВСЕ ПОЛЯ ПУСТЫЕ → ОТМЕНА
        if (isNew && allFieldsEmpty) {
            Toast.makeText(this, "Заполните хотя бы одно поле", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 РЕДАКТИРОВАНИЕ + ВСЕ ПОЛЯ ПУСТЫЕ → УДАЛЯЕМ
        if (!isNew && allFieldsEmpty) {
            showDeleteDialog("задвижку",
                    currentValve.getName() != null ? currentValve.getName() : "без названия",
                    this::deleteValve);
            return;
        }

        // ✅ ЕСТЬ ХОТЯ БЫ ОДНО ПОЛЕ → СОХРАНЯЕМ
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

        if (isNew) {
            valveToSave.setOriginalId(0);
            valveToSave.setIsDeleted(0);
            valveToSave.setIsEdited(1);
            valveToSave.setCustom(true);
            valveToSave.setCreatedAt(currentDateTime);
            valveToSave.setEditedAtValve(currentDateTime);

            new Thread(() -> {
                try {
                    long id = repository.insertUserGateValve(valveToSave);
                    if (id > 0) {
                        isSaved = true;
                        runOnUiThread(() -> {
                            Toast.makeText(this, "✅ Создано", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "❌ Ошибка", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();
        } else {
            boolean isFromUserDb = currentValve.getOriginalId() > 0 || currentValve.getCreatedAt() != null;

            if (isFromUserDb) {
                valveToSave.setId(currentValve.getId());
                valveToSave.setOriginalId(currentValve.getOriginalId());
                valveToSave.setIsDeleted(0);
                valveToSave.setIsEdited(1);
                valveToSave.setCustom(true);
                valveToSave.setCreatedAt(currentValve.getCreatedAt());
                valveToSave.setEditedAtValve(currentDateTime);

                new Thread(() -> {
                    try {
                        repository.updateUserGateValve(valveToSave);
                        isSaved = true;
                        runOnUiThread(() -> {
                            Toast.makeText(this, "✅ Сохранено", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            } else {
                valveToSave.setOriginalId(currentValve.getId());
                valveToSave.setIsDeleted(0);
                valveToSave.setIsEdited(1);
                valveToSave.setCustom(true);
                valveToSave.setCreatedAt(currentDateTime);
                valveToSave.setEditedAtValve(currentDateTime);

                new Thread(() -> {
                    try {
                        GateValve existing = repository.getUserGateValveByOriginalId(currentValve.getId());
                        if (existing != null) {
                            valveToSave.setId(existing.getId());
                            repository.updateUserGateValve(valveToSave);
                        } else {
                            repository.insertUserGateValve(valveToSave);
                        }
                        isSaved = true;
                        runOnUiThread(() -> {
                            Toast.makeText(this, "✅ Скопировано", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            }
        }
    }

    private void saveNewValve(GateValve valve, String dateTime) {
        valve.setOriginalId(0);
        valve.setIsDeleted(0);
        valve.setIsEdited(1);
        valve.setCustom(true);
        valve.setCreatedAt(dateTime);
        valve.setEditedAtValve(dateTime);

        new Thread(() -> {
            try {
                long id = repository.insertUserGateValve(valve);
                if (id > 0) {
                    isSaved = true;
                    runOnUiThread(() -> {
                        Toast.makeText(this, "✅ Создано", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void saveExistingValve(GateValve valve, String dateTime) {
        // Проверяем, из какой БД загружена запись
        boolean isFromUserDb = currentValve.getOriginalId() > 0 ||
                currentValve.getCreatedAt() != null;

        if (isFromUserDb) {
            // Обновляем в БД2
            valve.setId(currentValve.getId());
            valve.setOriginalId(currentValve.getOriginalId());
            valve.setIsDeleted(0);
            valve.setIsEdited(1);
            valve.setCustom(true);
            valve.setCreatedAt(currentValve.getCreatedAt());
            valve.setEditedAtValve(dateTime);

            new Thread(() -> {
                try {
                    repository.updateUserGateValve(valve);
                    isSaved = true;
                    runOnUiThread(() -> {
                        Toast.makeText(this, "✅ Сохранено", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show());
                }
            }).start();
        } else {
            // Копируем в БД2
            valve.setOriginalId(currentValve.getId());
            valve.setIsDeleted(0);
            valve.setIsEdited(1);
            valve.setCustom(true);
            valve.setCreatedAt(dateTime);
            valve.setEditedAtValve(dateTime);

            new Thread(() -> {
                try {
                    GateValve existing = repository.getUserGateValveByOriginalId(currentValve.getId());
                    if (existing != null) {
                        valve.setId(existing.getId());
                        repository.updateUserGateValve(valve);
                    } else {
                        repository.insertUserGateValve(valve);
                    }
                    isSaved = true;
                    runOnUiThread(() -> {
                        Toast.makeText(this, "✅ Скопировано", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show());
                }
            }).start();
        }
    }

    private void deleteValve() {
        isDeleting = true;
        new Thread(() -> {
            try {
                boolean isFromUserDb = currentValve.getOriginalId() > 0 ||
                        currentValve.getCreatedAt() != null;
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

    // ==========================================
    // 💾 СОХРАНЕНИЕ ДАТЧИКА
    // ==========================================

    private void saveSensor() {
        if (currentSensor == null) return;

        String stMarking = etStMarking.getText().toString().trim();
        String fullName = etFullNameSensor.getText().toString().trim();
        String location = etLocationSensor.getText().toString().trim();
        String kks = etKks.getText().toString().trim();
        String modelSensor = etModelSensor.getText().toString().trim();
        String modSensor = etModSensor.getText().toString().trim();
        String additionalInfo = etAdditionalInfo.getText().toString().trim();
        double min = parseDouble(etMin.getText().toString().trim());
        double max = parseDouble(etMax.getText().toString().trim());
        String unitMeasure = etUnitMeasure.getText().toString().trim();
        String cva = etCva.getText().toString().trim();
        String dampingTime = etDampingTime.getText().toString().trim();

        boolean allFieldsEmpty = stMarking.isEmpty() && fullName.isEmpty() &&
                location.isEmpty() && kks.isEmpty() && modelSensor.isEmpty() &&
                modSensor.isEmpty() && additionalInfo.isEmpty() &&
                min == 0 && max == 0 && unitMeasure.isEmpty() &&
                cva.isEmpty() && dampingTime.isEmpty();

        // 🔥 НОВАЯ ЗАПИСЬ + ВСЕ ПОЛЯ ПУСТЫЕ → ОТМЕНА
        if (isNew && allFieldsEmpty) {
            Toast.makeText(this, "Заполните хотя бы одно поле", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 РЕДАКТИРОВАНИЕ + ВСЕ ПОЛЯ ПУСТЫЕ → УДАЛЯЕМ
        if (!isNew && allFieldsEmpty) {
            showDeleteDialog("датчик",
                    currentSensor.getStMarkir() != null ? currentSensor.getStMarkir() : "без названия",
                    this::deleteSensor);
            return;
        }

        // ✅ ЕСТЬ ХОТЯ БЫ ОДНО ПОЛЕ → СОХРАНЯЕМ
        Sensor sensorToSave = new Sensor();
        sensorToSave.setStMarkir(stMarking);
        sensorToSave.setFullName(fullName);
        sensorToSave.setLocation(location);

        if (kks.isEmpty()) {
            kks = generateUniqueKks();
        }
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

        if (isNew) {
            sensorToSave.setOriginalKks(kks);
            sensorToSave.setIsDeleted(0);
            sensorToSave.setIsEdited(1);
            sensorToSave.setCustom(true);
            sensorToSave.setCreatedAt(currentDateTime);
            sensorToSave.setEditedAt(currentDateTime);

            new Thread(() -> {
                try {
                    long id = repository.insertUserSensor(sensorToSave);
                    if (id > 0) {
                        isSaved = true;
                        runOnUiThread(() -> {
                            Toast.makeText(this, "✅ Создано", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "❌ Ошибка", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();
        } else {
            boolean isFromUserDb = currentSensor.getOriginalKks() != null && !currentSensor.getOriginalKks().isEmpty();

            if (isFromUserDb) {
                sensorToSave.setId(currentSensor.getId());
                sensorToSave.setOriginalKks(currentSensor.getOriginalKks());
                sensorToSave.setIsDeleted(0);
                sensorToSave.setIsEdited(1);
                sensorToSave.setCustom(true);
                sensorToSave.setCreatedAt(currentSensor.getCreatedAt());
                sensorToSave.setEditedAt(currentDateTime);

                new Thread(() -> {
                    try {
                        repository.updateUserSensor(sensorToSave);
                        isSaved = true;
                        runOnUiThread(() -> {
                            Toast.makeText(this, "✅ Сохранено", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            } else {
                sensorToSave.setOriginalKks(currentSensor.getKks());
                sensorToSave.setIsDeleted(0);
                sensorToSave.setIsEdited(1);
                sensorToSave.setCustom(true);
                sensorToSave.setCreatedAt(currentDateTime);
                sensorToSave.setEditedAt(currentDateTime);

                new Thread(() -> {
                    try {
                        Sensor existing = repository.getUserSensorByOriginalKks(currentSensor.getKks());
                        if (existing != null) {
                            sensorToSave.setId(existing.getId());
                            repository.updateUserSensor(sensorToSave);
                        } else {
                            repository.insertUserSensor(sensorToSave);
                        }
                        isSaved = true;
                        runOnUiThread(() -> {
                            Toast.makeText(this, "✅ Скопировано", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            }
        }
    }

    private void deleteSensor() {
        isDeleting = true;
        new Thread(() -> {
            try {
                boolean isFromUserDb = currentSensor.getOriginalKks() != null &&
                        !currentSensor.getOriginalKks().isEmpty();
                if (isFromUserDb && currentSensor.getOriginalKks() != null) {
                    repository.markSensorAsDeleted(currentSensor.getOriginalKks());
                } else if (isFromUserDb) {
                    repository.deleteUserSensor(currentSensor.getId());
                } else {
                    repository.markSensorAsDeleted(currentSensor.getKks());
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

    // ==========================================
    // 💾 СОХРАНЕНИЕ УСТАВКИ
    // ==========================================

    private void saveSetpoint() {
        if (currentSetpoint == null) return;

        String positionName = etPositionName.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String setpointValue = etSetpointValue.getText().toString().trim();
        String delayTime = etDelayTime.getText().toString().trim();
        String operation = etOperation.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String equipmentGroup = etEquipmentGroup.getText().toString().trim();
        String location = etLocationSetpoint.getText().toString().trim();

        boolean allFieldsEmpty = positionName.isEmpty() && name.isEmpty() &&
                setpointValue.isEmpty() && delayTime.isEmpty() &&
                operation.isEmpty() && notes.isEmpty() &&
                equipmentGroup.isEmpty() && location.isEmpty();

        // 🔥 НОВАЯ ЗАПИСЬ + ВСЕ ПОЛЯ ПУСТЫЕ → ОТМЕНА
        if (isNew && allFieldsEmpty) {
            Toast.makeText(this, "Заполните хотя бы одно поле", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 РЕДАКТИРОВАНИЕ + ВСЕ ПОЛЯ ПУСТЫЕ → УДАЛЯЕМ
        if (!isNew && allFieldsEmpty) {
            showDeleteDialog("уставку",
                    currentSetpoint.getPositionName() != null ? currentSetpoint.getPositionName() : "без названия",
                    this::deleteSetpoint);
            return;
        }

        // ✅ ЕСТЬ ХОТЯ БЫ ОДНО ПОЛЕ → СОХРАНЯЕМ
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

        if (isNew) {
            setpointToSave.setOriginalId(0);
            setpointToSave.setIsDeleted(0);
            setpointToSave.setIsEdited(1);
            setpointToSave.setCustom(true);
            setpointToSave.setCreatedAt(currentDateTime);
            setpointToSave.setEditedAt(currentDateTime);

            new Thread(() -> {
                try {
                    long id = repository.insertUserSetpoint(setpointToSave);
                    if (id > 0) {
                        isSaved = true;
                        runOnUiThread(() -> {
                            Toast.makeText(this, "✅ Создано", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "❌ Ошибка", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();
        } else {
            boolean isFromUserDb = currentSetpoint.getOriginalId() > 0 || currentSetpoint.getCreatedAt() != null;

            if (isFromUserDb) {
                setpointToSave.setId(currentSetpoint.getId());
                setpointToSave.setOriginalId(currentSetpoint.getOriginalId());
                setpointToSave.setIsDeleted(0);
                setpointToSave.setIsEdited(1);
                setpointToSave.setCustom(true);
                setpointToSave.setCreatedAt(currentSetpoint.getCreatedAt());
                setpointToSave.setEditedAt(currentDateTime);

                new Thread(() -> {
                    try {
                        repository.updateUserSetpoint(setpointToSave);
                        isSaved = true;
                        runOnUiThread(() -> {
                            Toast.makeText(this, "✅ Сохранено", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            } else {
                setpointToSave.setOriginalId(currentSetpoint.getId());
                setpointToSave.setIsDeleted(0);
                setpointToSave.setIsEdited(1);
                setpointToSave.setCustom(true);
                setpointToSave.setCreatedAt(currentDateTime);
                setpointToSave.setEditedAt(currentDateTime);

                new Thread(() -> {
                    try {
                        Setpoint existing = repository.getUserSetpointByOriginalId(currentSetpoint.getId());
                        if (existing != null) {
                            setpointToSave.setId(existing.getId());
                            repository.updateUserSetpoint(setpointToSave);
                        } else {
                            repository.insertUserSetpoint(setpointToSave);
                        }
                        isSaved = true;
                        runOnUiThread(() -> {
                            Toast.makeText(this, "✅ Скопировано", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            }
        }
    }

    private void deleteSetpoint() {
        isDeleting = true;
        new Thread(() -> {
            try {
                boolean isFromUserDb = currentSetpoint.getOriginalId() > 0 ||
                        currentSetpoint.getCreatedAt() != null;
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

    // ==========================================
    // 🧩 ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ==========================================

    private void showDeleteDialog(String entityName, String entityTitle, Runnable deleteAction) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
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

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
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

    private boolean hasSensorChanges() {
        if (currentSensor == null || originalSensor == null) return false;
        return !TextUtils.equals(etStMarking.getText().toString().trim(), originalSensor.getStMarkir()) ||
                !TextUtils.equals(etFullNameSensor.getText().toString().trim(), originalSensor.getFullName()) ||
                !TextUtils.equals(etLocationSensor.getText().toString().trim(), originalSensor.getLocation()) ||
                !TextUtils.equals(etKks.getText().toString().trim(), originalSensor.getKks()) ||
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
        return !TextUtils.equals(etPositionName.getText().toString().trim(), originalSetpoint.getPositionName()) ||
                !TextUtils.equals(etName.getText().toString().trim(), originalSetpoint.getName()) ||
                !TextUtils.equals(etSetpointValue.getText().toString().trim(), originalSetpoint.getSetpointValue()) ||
                !TextUtils.equals(etDelayTime.getText().toString().trim(), originalSetpoint.getDelayTime()) ||
                !TextUtils.equals(etOperation.getText().toString().trim(), originalSetpoint.getOperation()) ||
                !TextUtils.equals(etNotes.getText().toString().trim(), originalSetpoint.getNotes()) ||
                !TextUtils.equals(etEquipmentGroup.getText().toString().trim(), originalSetpoint.getEquipmentGroup()) ||
                !TextUtils.equals(etLocationSetpoint.getText().toString().trim(), originalSetpoint.getLocation());
    }

    // ==========================================
    // 🔧 UI ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ==========================================

    private void fixToolbarPadding(View toolbarView) {
        if (toolbarView == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(toolbarView, (view, windowInsets) -> {
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
    private String generateUniqueKks() {
        return "USR_" + System.currentTimeMillis();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}