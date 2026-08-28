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
    private TextView tvListNumber;

    private boolean isExistingSession = false;
    private String currentSessionId = null;
    private boolean hasChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_detail);

        Log.d("SESSY", "=== ListDetailActivity.onCreate START ===");

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
        Log.d("SESSY", "viewModel created");

        initViews();
        Log.d("SESSY", "initViews completed");

        setupRecyclerViews();
        Log.d("SESSY", "setupRecyclerViews completed");

        setupObservers();
        Log.d("SESSY", "setupObservers completed");

        // ПОЛУЧАЕМ ДАННЫЕ ИЗ INTENT
        currentSessionId = getIntent().getStringExtra("session_id");
        boolean isNewSession = getIntent().getBooleanExtra("is_new_session", false);
        boolean useUserDb = getIntent().getBooleanExtra("use_user_db", true);

        Log.d("SESSY", "currentSessionId: " + currentSessionId);
        Log.d("SESSY", "isNewSession: " + isNewSession);
        Log.d("SESSY", "useUserDb: " + useUserDb);

        viewModel.setUseUserDb(useUserDb);

        // ЗАГРУЗКА ДАННЫХ
        if (isNewSession) {
            isExistingSession = false;
            Log.d("SESSY", "isExistingSession = FALSE (new session)");

            ArrayList<Integer> ids = getIntent().getIntegerArrayListExtra("valve_ids");
            ArrayList<String> names = getIntent().getStringArrayListExtra("valve_names");
            ArrayList<String> isys = getIntent().getStringArrayListExtra("valve_isys");

            Log.d("SESSY", "ids = " + (ids != null ? ids.size() : "NULL"));
            Log.d("SESSY", "names = " + (names != null ? names.size() : "NULL"));
            Log.d("SESSY", "isys = " + (isys != null ? isys.size() : "NULL"));

            if (names != null && !names.isEmpty()) {
                Log.d("SESSY", "Loading " + names.size() + " valves from MainActivity");
                List<GateValve> valves = new ArrayList<>();
                for (int i = 0; i < names.size(); i++) {
                    GateValve valve = new GateValve();
                    if (ids != null && i < ids.size()) {
                        valve.setId(ids.get(i));
                    }
                    valve.setName(names.get(i) != null ? names.get(i) : "");
                    valve.setIsy(i < isys.size() && isys.get(i) != null ? isys.get(i) : "");
                    valves.add(valve);
                    Log.d("SESSY", "  valve[" + i + "] id=" + valve.getId() + ", name=" + valve.getName());
                }
                viewModel.setListName("Новый список");
                viewModel.loadFromGateValves(valves);
            } else {
                Log.d("SESSY", "names is NULL or EMPTY - creating empty session");
                viewModel.createEmptySession();
            }
            showListNumber();

        } else if (currentSessionId != null && !currentSessionId.isEmpty()) {
            isExistingSession = true;
            Log.d("SESSY", "isExistingSession = TRUE (loading existing session)");
            viewModel.loadSession(currentSessionId);
            showListNumber();

        } else {
            Log.e("SESSY", "ERROR: no session_id and no isNewSession");
            Toast.makeText(this, "Ошибка загрузки списка", Toast.LENGTH_SHORT).show();
            finish();
        }

        Log.d("SESSY", "=== ListDetailActivity.onCreate END ===");
    }

    private void initViews() {
        Log.d("SESSY", "=== initViews START ===");

        tvListTitle = findViewById(R.id.tvListTitle);
        Log.d("SESSY", "tvListTitle = " + (tvListTitle != null ? "found" : "NULL"));

        tvListNumber = findViewById(R.id.tvListNumber);
        Log.d("SESSY", "tvListNumber = " + (tvListNumber != null ? "found" : "NULL"));

        View btnAssemble = findViewById(R.id.btnAssemble);
        if (btnAssemble != null) {
            btnAssemble.setOnClickListener(v -> {
                hasChanges = true;
                viewModel.assembleAll();
            });
            Log.d("SESSY", "btnAssemble configured");
        } else {
            Log.e("SESSY", "btnAssemble not found!");
        }

        View btnDisassemble = findViewById(R.id.btnDisassemble);
        if (btnDisassemble != null) {
            btnDisassemble.setOnClickListener(v -> {
                hasChanges = true;
                viewModel.disassembleAll();
            });
            Log.d("SESSY", "btnDisassemble configured");
        } else {
            Log.e("SESSY", "btnDisassemble not found!");
        }

        Log.d("SESSY", "=== initViews END ===");
    }

    private void setupRecyclerViews() {
        Log.d("SESSY", "=== setupRecyclerViews START ===");

        RecyclerView rvLeft = findViewById(R.id.rvLeft);
        if (rvLeft != null) {
            rvLeft.setLayoutManager(new LinearLayoutManager(this));
            leftAdapter = new ValveItemAdapter(this);
            leftAdapter.setListener(new ValveItemAdapter.OnItemClickListener() {
                @Override
                public void onMoveClick(ValveItem item) {
                    Log.d("LIST_DEBUG", "onMoveClick LEFT: item.gateValveId=" + item.getGateValveId());
                    hasChanges = true;
                    // 🔥 ИСПРАВЛЕНО: из левого → в правый (toLeft = false)
                    viewModel.moveItem(item, false);
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

                @Override
                public void onItemClick(ValveItem item) {
                    showValveInfoDialog(item);
                }
            });
            rvLeft.setAdapter(leftAdapter);
            Log.d("SESSY", "rvLeft configured");
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
                    Log.d("LIST_DEBUG", "onMoveClick RIGHT: item.gateValveId=" + item.getGateValveId());
                    hasChanges = true;
                    // 🔥 ИСПРАВЛЕНО: из правого → в левый (toLeft = true)
                    viewModel.moveItem(item, true);
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

                @Override
                public void onItemClick(ValveItem item) {
                    showValveInfoDialog(item);
                }
            });
            rvRight.setAdapter(rightAdapter);
            Log.d("SESSY", "rvRight configured");
        } else {
            Log.e("SESSY", "rvRight not found!");
        }

        Log.d("SESSY", "=== setupRecyclerViews END ===");
    }

    private void setupObservers() {
        Log.d("SESSY", "=== setupObservers START ===");

        viewModel.getLeftList().observe(this, items -> {
            Log.d("SESSY", "=== OBSERVER: leftList changed ===");
            Log.d("SESSY", "Left items size: " + (items != null ? items.size() : 0));
            if (items != null && leftAdapter != null) {
                leftAdapter.setItems(items);
            }
        });

        viewModel.getRightList().observe(this, items -> {
            Log.d("SESSY", "=== OBSERVER: rightList changed ===");
            Log.d("SESSY", "Right items size: " + (items != null ? items.size() : 0));
            if (items != null && rightAdapter != null) {
                rightAdapter.setItems(items);
            }
        });

        Log.d("SESSY", "=== setupObservers END ===");
    }

    private void showListNumber() {
        Log.d("SESSY", "=== showListNumber START ===");
        viewModel.getListCount(listCount -> {
            runOnUiThread(() -> {
                if (tvListNumber != null && listCount > 0) {
                    tvListNumber.setVisibility(View.VISIBLE);
                    tvListNumber.setText("Список №" + (listCount + 1));
                    Log.d("SESSY", "List number set to: " + (listCount + 1));
                }
            });
        });
        Log.d("SESSY", "=== showListNumber END ===");
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
        Log.d("DATEFRESH", "=== onBackPressed START ===");
        Log.d("DATEFRESH", "isExistingSession = " + isExistingSession);
        Log.d("DATEFRESH", "hasChanges = " + hasChanges);

        if (isExistingSession) {
            Log.d("DATEFRESH", "Existing session branch");
            if (hasChanges) {
                Log.d("DATEFRESH", "Has changes - updating session");
                viewModel.updateSession(currentSessionId);
                AppState.getInstance().setHasUnsavedChanges(true);

                Intent data = new Intent();
                data.putExtra("data_changed", true);
                setResult(RESULT_OK, data);
                Log.d("DATEFRESH", "Session update triggered");
            } else {
                Log.d("DATEFRESH", "No changes - cancel");
                setResult(RESULT_CANCELED);
            }
            super.onBackPressed();
        } else {
            Log.d("DATEFRESH", "New session - showing save dialog");
            showSaveDialog();
        }
        Log.d("DATEFRESH", "=== onBackPressed END ===");
    }

    private void showSaveDialog() {
        Log.d("SESSY", "=== showSaveDialog START ===");
        Log.d("SESSY", "isFinishing = " + isFinishing());

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
            Log.d("SESSY", "Save dialog: name = " + name);

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
            Log.d("SESSY", "Save dialog: CANCEL");
            dialogInterface.cancel();
            AppState.getInstance().clearSession();
            setResult(RESULT_CANCELED);
            finish();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnDismissListener(dismissListener -> {
            if (!isFinishing()) {
                Log.d("SESSY", "Save dialog: DISMISSED");
                AppState.getInstance().clearSession();
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        alertDialog.show();
        Log.d("SESSY", "=== showSaveDialog END ===");
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

        String onPlace = valve.getOnPlace();
        if (onPlace != null && !onPlace.isEmpty()) {
            info.append("- ").append(onPlace).append("\n");
        }

        if (info.length() == 0) {
            info.append("Нет данных о задвижке");
        }

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