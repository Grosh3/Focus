package com.mikesuvade.focus.ui.list;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.ValveItem;
import com.mikesuvade.focus.domain.models.ValveWorkSession;
import com.mikesuvade.focus.utils.AppState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ListDetailActivity extends AppCompatActivity {

    private ListDetailViewModel viewModel;
    private ValveItemAdapter leftAdapter;
    private ValveItemAdapter rightAdapter;

    private TextView tvListTitle;
    private TextView tvCount;
    private TextView tvListNumber;

    private boolean isExistingSession = false;
    private String currentSessionId = null;
    private boolean hasChanges = false;

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

        // Проверяем Intent
        currentSessionId = getIntent().getStringExtra("session_id");
        boolean isNewSession = getIntent().getBooleanExtra("is_new_session", false);

        Log.d("MYTITLE", "=== onCreate ListDetailActivity ===");
        Log.d("MYTITLE", "currentSessionId: " + currentSessionId);
        Log.d("MYTITLE", "isNewSession: " + isNewSession);

        if (currentSessionId != null && !currentSessionId.isEmpty()) {
            // ✅ ЗАГРУЗКА СОХРАНЁННОЙ СЕССИИ
            isExistingSession = true;
            viewModel.loadSession(currentSessionId);
            showListNumber();

        } else if (isNewSession) {
            // ✅ СОЗДАНИЕ НОВОЙ ПУСТОЙ СЕССИИ
            isExistingSession = false;
            viewModel.createEmptySession();
            showListNumber();

        } else {
            // ✅ СОЗДАНИЕ НОВОГО СПИСКА ИЗ MAINACTIVITY
            isExistingSession = false;

            ArrayList<Integer> ids = getIntent().getIntegerArrayListExtra("valve_ids");
            ArrayList<String> names = getIntent().getStringArrayListExtra("valve_names");
            ArrayList<String> isys = getIntent().getStringArrayListExtra("valve_isys");

            Log.d("MYTITLE", "=== New list from MainActivity ===");
            Log.d("MYTITLE", "ids size: " + (ids != null ? ids.size() : 0));
            Log.d("MYTITLE", "names size: " + (names != null ? names.size() : 0));

            if (names != null && !names.isEmpty()) {
                List<GateValve> valves = new ArrayList<>();
                for (int i = 0; i < names.size(); i++) {
                    GateValve valve = new GateValve();
                    if (ids != null && i < ids.size()) {
                        valve.setId(ids.get(i));
                    }
                    valve.setName(names.get(i) != null ? names.get(i) : "");
                    valve.setIsy(i < isys.size() && isys.get(i) != null ? isys.get(i) : "");
                    valves.add(valve);
                    Log.d("MYTITLE", "  valve[" + i + "] id=" + valve.getId() + ", name=" + valve.getName());
                }

                // ✅ ВОТ ЭТИ СТРОКИ НУЖНО РАСКОММЕНТИРОВАТЬ!
                viewModel.setListName("Новый список");
                viewModel.loadFromGateValves(valves);

                showListNumber();
            } else {
                Toast.makeText(this, R.string.list_empty, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    private void initViews() {
        tvListTitle = findViewById(R.id.tvListTitle);
        Log.d("MYTITLE", "tvListTitle found: " + (tvListTitle != null));
      //  tvCount = findViewById(R.id.tvCount);
       // tvListNumber = findViewById(R.id.tvListNumber);

        findViewById(R.id.btnAssemble).setOnClickListener(v -> {
            hasChanges = true;
            viewModel.assembleAll();
        });

        findViewById(R.id.btnDisassemble).setOnClickListener(v -> {
            hasChanges = true;
            viewModel.disassembleAll();
        });
    }

    private void showListNumber() {
        viewModel.getListCount(listCount -> {
            if (listCount > 0) {
                tvListNumber.setVisibility(View.VISIBLE);
                tvListNumber.setText("Список №" + (listCount + 1));
            }
        });
    }
    private void setupRecyclerViews() {
        RecyclerView rvLeft = findViewById(R.id.rvLeft);
        rvLeft.setLayoutManager(new LinearLayoutManager(this));
        leftAdapter = new ValveItemAdapter(this);
        leftAdapter.setListener(new ValveItemAdapter.OnItemClickListener() {
            @Override
            public void onMoveClick(ValveItem item) {
                Log.d("LIST_DEBUG", "onMoveClick LEFT: item.gateValveId=" + item.getGateValveId());
                hasChanges = true;
                viewModel.moveItem(item, true);
            }

            @Override
            public void onMotorClick(ValveItem item) {
                Log.d("LIST_DEBUG", "onMotorClick LEFT: item.gateValveId=" + item.getGateValveId());
                hasChanges = true;
                viewModel.toggleMotor(item);
            }

            @Override
            public void onBoxClick(ValveItem item) {
                Log.d("LIST_DEBUG", "onBoxClick LEFT: item.gateValveId=" + item.getGateValveId());
                hasChanges = true;
                viewModel.toggleBox(item);
            }

            @Override
            public void onCheckedClick(ValveItem item, boolean isChecked) {
                Log.d("LIST_DEBUG", "=== onCheckedClick LEFT ===");
                Log.d("LIST_DEBUG", "item.gateValveId = " + item.getGateValveId());
                Log.d("LIST_DEBUG", "isChecked = " + isChecked);

                hasChanges = true;
                viewModel.toggleChecked(item);

                Log.d("LIST_DEBUG", "=== onCheckedClick LEFT END ===");
            }

            @Override
            public void onItemLongClick(ValveItem item) {
                hasChanges = true;
                showDeleteDialog(item);
            }

            // ✅ ДОБАВЛЕНО: КОРОТКИЙ ТАП ПО КАРТОЧКЕ
            @Override
            public void onItemClick(ValveItem item) {
                showValveInfoDialog(item);
            }
        });
        rvLeft.setAdapter(leftAdapter);

        RecyclerView rvRight = findViewById(R.id.rvRight);
        rvRight.setLayoutManager(new LinearLayoutManager(this));
        rightAdapter = new ValveItemAdapter(this);
        rightAdapter.setListener(new ValveItemAdapter.OnItemClickListener() {
            @Override
            public void onMoveClick(ValveItem item) {
                Log.d("LIST_DEBUG", "onMoveClick RIGHT: item.gateValveId=" + item.getGateValveId());
                hasChanges = true;
                viewModel.moveItem(item, false);
            }

            @Override
            public void onMotorClick(ValveItem item) {
                Log.d("LIST_DEBUG", "onMotorClick RIGHT: item.gateValveId=" + item.getGateValveId());
                hasChanges = true;
                viewModel.toggleMotor(item);
            }

            @Override
            public void onBoxClick(ValveItem item) {
                Log.d("LIST_DEBUG", "onBoxClick RIGHT: item.gateValveId=" + item.getGateValveId());
                hasChanges = true;
                viewModel.toggleBox(item);
            }

            @Override
            public void onCheckedClick(ValveItem item, boolean isChecked) {
                Log.d("LIST_DEBUG", "=== onCheckedClick RIGHT ===");
                Log.d("LIST_DEBUG", "item.gateValveId = " + item.getGateValveId());
                Log.d("LIST_DEBUG", "isChecked = " + isChecked);

                hasChanges = true;
                viewModel.toggleChecked(item);

                Log.d("LIST_DEBUG", "=== onCheckedClick RIGHT END ===");
            }

            @Override
            public void onItemLongClick(ValveItem item) {
                hasChanges = true;
                showDeleteDialog(item);
            }

            // ✅ ДОБАВЛЕНО: КОРОТКИЙ ТАП ПО КАРТОЧКЕ
            @Override
            public void onItemClick(ValveItem item) {
                showValveInfoDialog(item);
            }
        });
        rvRight.setAdapter(rightAdapter);
    }
    private void setupObservers() {
        viewModel.getLeftList().observe(this, items -> {
            Log.d("LIST_DEBUG", "=== OBSERVER: leftList changed ===");
            Log.d("LIST_DEBUG", "Left items size: " + (items != null ? items.size() : 0));
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    ValveItem item = items.get(i);
                    Log.d("LIST_DEBUG", "  LEFT [" + i + "] gateValveId=" + item.getGateValveId() +
                            ", assembled=" + item.getIsAssembled() +
                            ", checked=" + item.getIsChecked());
                }
            }
            leftAdapter.setItems(items);  // ← ЭТО ДОЛЖНО СРАБОТАТЬ
        });

        viewModel.getRightList().observe(this, items -> {
            Log.d("LIST_DEBUG", "=== OBSERVER: rightList changed ===");
            Log.d("LIST_DEBUG", "Right items size: " + (items != null ? items.size() : 0));
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    ValveItem item = items.get(i);
                    Log.d("LIST_DEBUG", "  RIGHT [" + i + "] gateValveId=" + item.getGateValveId() +
                            ", assembled=" + item.getIsAssembled() +
                            ", checked=" + item.getIsChecked());
                }
            }
            rightAdapter.setItems(items);
        });

     //   viewModel.getTotalCount().observe(this, count -> {
    //        tvCount.setText(String.valueOf(count));
     //   });

        viewModel.getListName().observe(this, name -> {
            Log.d("MYTITLE", "вызывается");
            if (name != null && !name.isEmpty()) {
                Log.d("MYTITLE", "устанавливается");
                tvListTitle.setText(name);
            }
        });
    }

    private void showDeleteDialog(ValveItem item) {
        GateValve valve = ((MyApp) getApplication()).getRepository().getGateValveById(item.getGateValveId());

        String name = valve != null && valve.getIsy() != null && !valve.getIsy().isEmpty()
                ? valve.getIsy()
                : (valve != null ? valve.getName() : "Неизвестная задвижка");

        new AlertDialog.Builder(this)
                .setTitle("Удалить из списка?")
                .setMessage("Удалить задвижку \"" + name + "\" из списка?")
                .setPositiveButton("Да", (dialog, which) -> {
                    hasChanges = true;
                    viewModel.removeItem(item);
                    Toast.makeText(this, "Удалено: " + name, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Нет", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onBackPressed() {
        if (isExistingSession) {
            if (hasChanges) {
                // ✅ ТОЛЬКО ОБНОВЛЯЕМ ЭЛЕМЕНТЫ И ДАТУ
                viewModel.updateSession(currentSessionId);
                AppState.getInstance().setHasUnsavedChanges(true);

                // ✅ НЕ СОЗДАЁМ НОВУЮ СЕССИЮ, НЕ ПЕРЕЗАПИСЫВАЕМ ИМЯ
                Intent data = new Intent();
                data.putExtra("data_changed", true);
                setResult(RESULT_OK, data);
            } else {
                setResult(RESULT_CANCELED);
            }
            super.onBackPressed();
        } else {
            showSaveDialog();
        }
    }

    private void showSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_save_title);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(getString(R.string.dialog_save_hint));
        builder.setView(input);

        builder.setPositiveButton(R.string.dialog_save_positive, (dialogInterface, whichButton) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                name = getString(R.string.default_list_name) + " " +
                        new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
            }
            viewModel.setListName(name);
            viewModel.saveSession(name);

            ValveWorkSession session = new ValveWorkSession();
            session.setSessionId(viewModel.getSessionId());
            session.setEquipmentDescription(name);
            session.setSaveDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            AppState.getInstance().setActiveSession(session);
            AppState.getInstance().setHasUnsavedChanges(false);

            Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });

        builder.setNegativeButton(R.string.dialog_save_negative, (dialogInterface, whichButton) -> {
            dialogInterface.cancel();

            // ✅ ПРИ ОТМЕНЕ — ОЧИЩАЕМ СЕССИЮ И ВОЗВРАЩАЕМСЯ
            AppState.getInstance().clearSession();
            setResult(RESULT_CANCELED);
            finish();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnDismissListener(dismissListener -> {
            if (!isFinishing()) {
                // ✅ ПРИ ЗАКРЫТИИ ДИАЛОГА (НАЖАТИЕ ВНЕ) — ОЧИЩАЕМ СЕССИЮ
                AppState.getInstance().clearSession();
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        alertDialog.show();
    }
    private void showValveInfoDialog(ValveItem item) {
        GateValve valve = ((MyApp) getApplication()).getRepository().getGateValveById(item.getGateValveId());
        if (valve == null) {
            Toast.makeText(this, "Задвижка не найдена", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder info = new StringBuilder();

        String isy = valve.getIsy();
        String name = valve.getName();

        // Сборка с дефисом
        String powerCabinet = valve.getPowerCabinet();
        String locationDescription = valve.getLocationDescription();
        if (powerCabinet != null && !powerCabinet.isEmpty()) {
            info.append("- ").append(powerCabinet);
            if (locationDescription != null && !locationDescription.isEmpty()) {
                info.append(" ").append(locationDescription);
            }
            info.append("\n");
        } else if (locationDescription != null && !locationDescription.isEmpty()) {
            info.append("- ").append(locationDescription).append("\n");
        }

        // Месторасположение с дефисом
        String onPlace = valve.getOnPlace();
        if (onPlace != null && !onPlace.isEmpty()) {
            info.append("- ").append(onPlace).append("\n");
        }

        // Если нет данных
        if (info.length() == 0) {
            info.append("Нет данных о задвижке");
        }

        // Заголовок: ИСУ + Название (жирным)
        String title = "";
        if (isy != null && !isy.isEmpty()) {
            title += isy + "    ";
        }
        if (name != null && !name.isEmpty()) {
            title += name;
        }
        if (title.isEmpty()) {
            title = "Задвижка";
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(info.toString().trim())
                .setPositiveButton("Закрыть", (dialog, which) -> dialog.dismiss())
                .show();
    }
}