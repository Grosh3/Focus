package com.mikesuvade.focus.ui.detail;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.repository.IRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_VALVE_ID = "valve_id";

    private IRepository repository;
    private GateValve currentValve;
    private GateValve originalValve;
    private int valveId;

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

    private boolean isExtraVisible = false;
    private boolean isSaved = false;
    private EditText etLocationDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Редактирование задвижки");
        }

        repository = ((MyApp) getApplication()).getRepository();
        initViews();
        setupListeners();

        valveId = getIntent().getIntExtra(EXTRA_VALVE_ID, -1);
        if (valveId == -1) {
            Toast.makeText(this, "Ошибка: ID задвижки не передан", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadValveData();
    }

    private void initViews() {
       // tvValveTitle = findViewById(R.id.tvValveTitle);
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
      //  etLocationDescription = findViewById(R.id.etLocationDescription);
        etLocationDescription = findViewById(R.id.etLocationDescription);
        tvValveTitle = findViewById(R.id.tvValveTitle);
    }

    private void setupListeners() {
        btnToggleExtra.setOnClickListener(v -> {
            isExtraVisible = !isExtraVisible;
            extraFieldsContainer.setVisibility(isExtraVisible ? View.VISIBLE : View.GONE);
            String text = isExtraVisible ? "▼ Скрыть дополнительные поля" : "▶ Дополнительные поля";
            ((android.widget.Button) btnToggleExtra).setText(text);
        });
    }

    private void loadValveData() {
        new Thread(() -> {
            try {
                // Загружаем из основной таблицы (user-таблиц больше нет)
                GateValve valve = repository.getGateValveById(valveId);
                currentValve = valve;
                originalValve = copyValve(valve);

                runOnUiThread(() -> {
                    if (currentValve != null) {
                        displayValveData();
                    } else {
                        Toast.makeText(this, "Задвижка не найдена", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void displayValveData() {
        if (currentValve == null) return;

        tvValveTitle.setText(currentValve.getName() != null ? currentValve.getName() : "Без названия");
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

        // Собираем данные с полей
        GateValve updatedValve = new GateValve();
        updatedValve.setId(currentValve.getId());

        // ==========================================
        // 1️⃣ Основные поля (в порядке отображения)
        // ==========================================
        updatedValve.setIsy(etIsy.getText().toString().trim());
        updatedValve.setName(etName.getText().toString().trim());
        updatedValve.setPowerCabinet(etPowerCabinet.getText().toString().trim());
        updatedValve.setLocationDescription(etLocationDescription.getText().toString().trim());
        updatedValve.setOnPlace(etOnPlace.getText().toString().trim());
        updatedValve.setFullName(etFullName.getText().toString().trim());

        // ==========================================
        // 2️⃣ Дополнительные поля (включая KKS)
        // ==========================================
        updatedValve.setKks(etKks.getText().toString().trim());
        updatedValve.setNameEng(etNameEng.getText().toString().trim());
        updatedValve.setAp50(etAp50.getText().toString().trim());
        updatedValve.setMark(etMark.getText().toString().trim());
        updatedValve.setCdaCabinet(etCdaCabinet.getText().toString().trim());
        updatedValve.setCdaCabinetPosition(etCdaCabinetPosition.getText().toString().trim());
        updatedValve.setSlot(etSlot.getText().toString().trim());

        // Копируем BLOB-поля из оригинальной задвижки
        updatedValve.setNameSpaceViewOpen(currentValve.getNameSpaceViewOpen());
        updatedValve.setNamespaceViewClose(currentValve.getNamespaceViewClose());
        updatedValve.setNamespaceViewPerifer(currentValve.getNamespaceViewPerifer());
        updatedValve.setDescriptionBlockingOpen(currentValve.getDescriptionBlockingOpen());
        updatedValve.setDescriptionBlockingClose(currentValve.getDescriptionBlockingClose());
        updatedValve.setDescriptionBlockingPerifer(currentValve.getDescriptionBlockingPerifer());

        // Устанавливаем флаг редактирования и дату
        updatedValve.setIsEdited(1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        updatedValve.setEditedAtValve(sdf.format(new Date()));

        // Сохраняем напрямую в основную таблицу
        new Thread(() -> {
            try {
                repository.updateGateValve(updatedValve);
                isSaved = true;
                runOnUiThread(() -> {
                    Toast.makeText(this, "✅ Сохранено", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "❌ Ошибка сохранения", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if (isSaved) {
            super.onBackPressed();
            return;
        }

        // Проверяем, были ли изменения
        if (hasChanges()) {
            Toast.makeText(this, "❌ Изменения не сохранены", Toast.LENGTH_SHORT).show();
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
}