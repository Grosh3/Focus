package com.mikesuvade.focus.ui.list;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.ValveItem;
import com.mikesuvade.focus.domain.models.ValveWorkSession;
import com.mikesuvade.focus.domain.repository.IRepository;
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
    private TextView tvListNumber;

    private boolean isExistingSession = false;
    private String currentSessionId = null;
    private boolean hasChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔥 ВКЛЮЧАЕМ EDGE-TO-EDGE
        androidx.activity.EdgeToEdge.enable(this);

        setContentView(R.layout.activity_list_detail);

        // 🔥 УСТАНАВЛИВАЕМ ТЁМНЫЕ ИКОНКИ СТАТУС-БАРА
        setStatusBarIconsDark(true);

        // 🔥 ДОБАВЛЯЕМ ОТСТУПЫ ДЛЯ СИСТЕМНЫХ БАРОВ
        fixTopPanelPadding();
        fixRecyclerViewBottomPadding();
        //fixActionButtonsBottomPadding();



        // ИНИЦИАЛИЗАЦИЯ ViewModel
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

        // ПОЛУЧАЕМ ДАННЫЕ ИЗ INTENT
        currentSessionId = getIntent().getStringExtra("session_id");
        boolean isNewSession = getIntent().getBooleanExtra("is_new_session", false);
        boolean useUserDb = getIntent().getBooleanExtra("use_user_db", true);

        viewModel.setUseUserDb(useUserDb);

        // ЗАГРУЗКА ДАННЫХ
        if (isNewSession) {
            isExistingSession = false;

            ArrayList<Integer> ids = getIntent().getIntegerArrayListExtra("valve_ids");
            ArrayList<String> names = getIntent().getStringArrayListExtra("valve_names");
            ArrayList<String> isys = getIntent().getStringArrayListExtra("valve_isys");



            if (names != null && !names.isEmpty()) {

                List<GateValve> valves = new ArrayList<>();
                for (int i = 0; i < names.size(); i++) {
                    GateValve valve = new GateValve();
                    if (ids != null && i < ids.size()) {
                        valve.setId(ids.get(i));
                    }
                    valve.setName(names.get(i) != null ? names.get(i) : "");
                    valve.setIsy(isys != null && i < isys.size() && isys.get(i) != null ? isys.get(i) : "");
                    valves.add(valve);
                }

                Log.d("SESSY", "valves.size=" + valves.size());
                Log.d("SESSY", "вызов viewModel.loadFromGateValves");

                viewModel.setListName("Новый список");
                viewModel.loadFromGateValves(valves);
                updateTitle("Новый список");
            } else {
                Log.w("SESSY", "names == null или empty — создаём пустую сессию");
                viewModel.createEmptySession();
            }
            showListNumber();

        } else if (currentSessionId != null && !currentSessionId.isEmpty()) {
            isExistingSession = true;

            Log.d("SESSY", "загрузка существующей сессии: " + currentSessionId);
            viewModel.loadSession(currentSessionId);
            updateTitle("Загрузка...");
            showListNumber();

        } else {
            Log.e("SESSY", "ERROR: no session_id and no isNewSession");
            Toast.makeText(this, "Ошибка загрузки списка", Toast.LENGTH_SHORT).show();
            finish();
        }


    }

    private void initViews() {


        tvListTitle = findViewById(R.id.tvListTitle);


        // 🔥 УСТАНАВЛИВАЕМ НАЗВАНИЕ ПО УМОЛЧАНИЮ
        if (tvListTitle != null) {
            tvListTitle.setText("Новый список");
        }

        View btnAssemble = findViewById(R.id.btnAssemble);
        if (btnAssemble != null) {
            btnAssemble.setOnClickListener(v -> {
                hasChanges = true;
                viewModel.assembleAll();
            });

        } else {
            Log.e("SESSY", "btnAssemble not found!");
        }

        View btnDisassemble = findViewById(R.id.btnDisassemble);
        if (btnDisassemble != null) {
            btnDisassemble.setOnClickListener(v -> {
                hasChanges = true;
                viewModel.disassembleAll();
            });

        } else {
            Log.e("SESSY", "btnDisassemble not found!");
        }


    }
    private void setupRecyclerViews() {


        RecyclerView rvLeft = findViewById(R.id.rvLeft);
        if (rvLeft != null) {
            rvLeft.setLayoutManager(new LinearLayoutManager(this));
            leftAdapter = new ValveItemAdapter(this);
            leftAdapter.setListener(new ValveItemAdapter.OnItemClickListener() {
                @Override
                public void onMoveClick(ValveItem item) {

                    hasChanges = true;
                    viewModel.moveItem(item, false);
                }

                @Override
                public void onMotorClick(ValveItem item) {

                    hasChanges = true;
                    viewModel.toggleMotor(item);
                }

                @Override
                public void onBoxClick(ValveItem item) {

                    hasChanges = true;
                    viewModel.toggleBox(item);
                }

                @Override
                public void onCheckedClick(ValveItem item, boolean isChecked) {

                    hasChanges = true;
                    viewModel.toggleChecked(item);

                }

                @Override
                public void onItemLongClick(ValveItem item) {
                    hasChanges = true;
                    showDeleteDialog(item);
                }

                @Override
                public void onItemClick(ValveItem item) {
                    showValveInfoDialog(item);
                }
            });
            rvLeft.setAdapter(leftAdapter);

        } else {
            Log.e("SESSY", "rvLeft not found!");
        }

        RecyclerView rvRight = findViewById(R.id.rvRight);
        if (rvRight != null) {
            rvRight.setLayoutManager(new LinearLayoutManager(this));
            rightAdapter = new ValveItemAdapter(this);
            rightAdapter.setListener(new ValveItemAdapter.OnItemClickListener() {
                @Override
                public void onMoveClick(ValveItem item) {

                    hasChanges = true;
                    viewModel.moveItem(item, true);
                }

                @Override
                public void onMotorClick(ValveItem item) {

                    hasChanges = true;
                    viewModel.toggleMotor(item);
                }

                @Override
                public void onBoxClick(ValveItem item) {

                    hasChanges = true;
                    viewModel.toggleBox(item);
                }

                @Override
                public void onCheckedClick(ValveItem item, boolean isChecked) {

                    hasChanges = true;
                    viewModel.toggleChecked(item);

                }

                @Override
                public void onItemLongClick(ValveItem item) {
                    hasChanges = true;
                    showDeleteDialog(item);
                }

                @Override
                public void onItemClick(ValveItem item) {
                    showValveInfoDialog(item);
                }
            });
            rvRight.setAdapter(rightAdapter);

        } else {
            Log.e("SESSY", "rvRight not found!");
        }


    }

    private void setupObservers() {


        viewModel.getLeftList().observe(this, items -> {

            if (items != null && leftAdapter != null) {
                leftAdapter.setItems(items);
            }
        });

        viewModel.getRightList().observe(this, items -> {

            if (items != null && rightAdapter != null) {
                rightAdapter.setItems(items);
            }
        });

        // 🔥 СЛУШАЕМ ИЗМЕНЕНИЕ НАЗВАНИЯ
        viewModel.getListName().observe(this, name -> {

            if (name != null && !name.isEmpty()) {
                updateTitle(name);
            }
        });


    }

    private void showListNumber() {

        viewModel.getListCount(listCount -> {
            runOnUiThread(() -> {
                if (tvListNumber != null && listCount > 0) {
                    tvListNumber.setVisibility(View.VISIBLE);
                    tvListNumber.setText("Список №" + (listCount + 1));

                }
            });
        });

    }

    private void showDeleteDialog(ValveItem item) {


        String name = item.getGateValveIsy();
        if (name == null || name.isEmpty()) {
            name = item.getGateValveName();
        }
        if (name == null || name.isEmpty()) {
            name = "Неизвестная задвижка";
        }



        final String finalName = name;

        new AlertDialog.Builder(this)
                .setTitle("Удалить из списка?")
                .setMessage("Удалить задвижку \"" + finalName + "\" из списка?")
                .setPositiveButton("Да", (dialog, which) -> {
                    hasChanges = true;
                    viewModel.removeItem(item);
                    Toast.makeText(this, "Удалено: " + finalName, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Нет", (dialog, which) -> dialog.dismiss())
                .show();
    }
    @Override
    public void onBackPressed() {


        if (isExistingSession) {

            if (hasChanges) {

                viewModel.updateSession(currentSessionId);
                AppState.getInstance().setHasUnsavedChanges(true);

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


            String sessionId = viewModel.getSessionId();


            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = "SESSION_" + System.currentTimeMillis();
                viewModel.setSessionId(sessionId);

            }

            // 🔥 УСТАНАВЛИВАЕМ ИМЯ В ViewModel И ОБНОВЛЯЕМ ЗАГОЛОВОК
            viewModel.setListName(name);
            updateTitle(name); // <-- ДОБАВИТЬ ЭТУ СТРОКУ
            viewModel.saveSession(name);

            ValveWorkSession session = new ValveWorkSession();
            session.setSessionId(sessionId);
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
            AppState.getInstance().clearSession();
            setResult(RESULT_CANCELED);
            finish();
        });

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnDismissListener(dismissListener -> {
            if (!isFinishing()) {

                AppState.getInstance().clearSession();
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        alertDialog.show();

    }

    private void showValveInfoDialog(ValveItem item) {
        // 🔥 ПОЛУЧАЕМ ЗАДВИЖКУ ИЗ ОБЪЕДИНЕННОГО ИСТОЧНИКА
        final GateValve valve = getGateValveMerged(item);

        if (valve == null) {
            Toast.makeText(this, "Задвижка не найдена", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder info = new StringBuilder();
        String title = "";

        // 🔥 1. ИСУ и название
        String isy = valve.getIsy();
        String name = valve.getName();

        if (isy != null && !isy.isEmpty()) {
            title += isy + "    ";
        }
        if (name != null && !name.isEmpty()) {
            title += name;
        }
        if (title.isEmpty()) {
            title = "Задвижка";
        }

        // 🔥 2. Шкаф и сборка
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

        // 🔥 3. Расположение
        String onPlace = valve.getOnPlace();
        if (onPlace != null && !onPlace.isEmpty()) {
            info.append("- ").append(onPlace).append("\n");
        }

        // 🔥 4. KKS
        String kks = valve.getKks();
        if (kks != null && !kks.isEmpty()) {
            info.append("- KKS: ").append(kks).append("\n");
        }

        // 🔥 5. Полное описание
        String fullName = valve.getFullName();
        if (fullName != null && !fullName.isEmpty()) {
            info.append("- ").append(fullName).append("\n");
        }

        if (info.length() == 0) {
            info.append("Нет данных о задвижке");
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(info.toString().trim())
                .setPositiveButton("Закрыть", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private GateValve getGateValveMerged(ValveItem item) {
        // 🔥 СНАЧАЛА ПРОВЕРЯЕМ ДАННЫЕ В ValveItem
        if (item.getGateValveName() != null && !item.getGateValveName().isEmpty()) {
            GateValve valve = new GateValve();
            valve.setId(item.getGateValveId());
            valve.setName(item.getGateValveName());
            valve.setIsy(item.getGateValveIsy());
            valve.setKks(item.getGateValveKks());
            valve.setPowerCabinet(item.getGateValvePowerCabinet());
            valve.setLocationDescription(item.getGateValveLocationDescription());
            valve.setOnPlace(item.getGateValveOnPlace());
            valve.setFullName(item.getGateValveFullName());
            return valve;
        }

        // Если в ValveItem нет данных - ищем в БД
        IRepository repository = ((MyApp) getApplication()).getRepository();

        // 1. Сначала пробуем найти в БД2 (пользовательские) по ID
        GateValve userValve = repository.getUserGateValveById(item.getGateValveId());
        if (userValve != null && userValve.getIsDeleted() != 1) {
            return userValve;
        }

        // 2. Если не нашли по ID, пробуем найти по original_id
        List<GateValve> allUserValves = repository.getAllUserGateValves();
        for (GateValve uv : allUserValves) {
            if (uv.getOriginalId() == item.getGateValveId() && uv.getIsDeleted() != 1) {
                return uv;
            }
        }

        // 3. Если не нашли в БД2, ищем в БД1 (справочник)
        return repository.getGateValveById(item.getGateValveId());
    }
    private void setStatusBarIconsDark(boolean dark) {
        Window window = getWindow();
        if (window != null) {
            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(window, window.getDecorView());
            controller.setAppearanceLightStatusBars(dark);
        }
    }
    private void fixTopPanelPadding() {
        View topPanel = findViewById(R.id.topPanel);
        if (topPanel == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(topPanel, (view, windowInsets) -> {
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
    private void fixRecyclerViewBottomPadding() {
        RecyclerView rvLeft = findViewById(R.id.rvLeft);
        RecyclerView rvRight = findViewById(R.id.rvRight);

        ViewCompat.setOnApplyWindowInsetsListener(rvLeft, (v, insets) -> {
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            int extraPadding = (int) (16 * getResources().getDisplayMetrics().density);

            rvLeft.setPadding(
                    rvLeft.getPaddingLeft(),
                    rvLeft.getPaddingTop(),
                    rvLeft.getPaddingRight(),
                    navBarHeight + extraPadding
            );

            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(rvRight, (v, insets) -> {
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            int extraPadding = (int) (16 * getResources().getDisplayMetrics().density);

            rvRight.setPadding(
                    rvRight.getPaddingLeft(),
                    rvRight.getPaddingTop(),
                    rvRight.getPaddingRight(),
                    navBarHeight + extraPadding
            );

            return insets;
        });
    }
    private void updateTitle(String title) {
        if (tvListTitle != null) {
            if (title != null && !title.isEmpty()) {
                tvListTitle.setVisibility(View.VISIBLE);
                tvListTitle.setText(title);
            } else {
                tvListTitle.setVisibility(View.VISIBLE);
                tvListTitle.setText("Новый список");
            }
        }
    }
}