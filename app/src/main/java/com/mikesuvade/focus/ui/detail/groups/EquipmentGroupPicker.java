package com.mikesuvade.focus.ui.detail.groups;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EquipmentGroupPicker {

    private static final String ITEM_ADD = "➕ ДОБАВИТЬ ГРУППУ";

    private final Context context;
    private final IRepository repository;
    private final EditText etEquipmentGroup;
    private final View overlayGroups;
    private final View fieldsContainer;

    public EquipmentGroupPicker(Context context,
                                IRepository repository,
                                EditText etEquipmentGroup,
                                View overlayGroups,
                                View fieldsContainer) {
        this.context = context;
        this.repository = repository;
        this.etEquipmentGroup = etEquipmentGroup;
        this.overlayGroups = overlayGroups;
        this.fieldsContainer = fieldsContainer;
    }

    public void attach() {
        etEquipmentGroup.setFocusable(false);
        etEquipmentGroup.setClickable(true);
        etEquipmentGroup.setOnClickListener(v -> show());

        View btnClear = overlayGroups.findViewById(R.id.btnClearGroupSelection);
        if (btnClear != null) {
            btnClear.setOnClickListener(v -> {
                etEquipmentGroup.setText("");
                hide();
            });
        }
    }

    public void show() {
        hideKeyboard();
        fieldsContainer.setVisibility(View.GONE);
        overlayGroups.setVisibility(View.VISIBLE);
        overlayGroups.setOnClickListener(v -> hide());

        new Thread(() -> {
            try {
                List<String> groups = repository.getAllEquipmentGroupsWithUser();

                String[] resourceGroups = context.getResources().getStringArray(R.array.equipment_groups);
                Set<String> uniqueGroups = new HashSet<>(groups);
                for (String g : resourceGroups) {
                    if (g != null && !g.isEmpty()) {
                        uniqueGroups.add(g.toUpperCase());
                    }
                }

                List<String> allGroups = new ArrayList<>(uniqueGroups);
                Collections.sort(allGroups);
                allGroups.add(0, ITEM_ADD);

                android.os.Handler main = new android.os.Handler(context.getMainLooper());
                main.post(() -> displayGroupsList(allGroups));

            } catch (android.database.sqlite.SQLiteException e) {
                Log.e("EquipmentGroupPicker", "SQLite error loading groups", e);
                android.os.Handler main = new android.os.Handler(context.getMainLooper());
                main.post(() -> {
                    Toast.makeText(context, "Ошибка БД при загрузке групп", Toast.LENGTH_LONG).show();
                    hide();
                });
            } catch (RuntimeException e) {
                Log.e("EquipmentGroupPicker", "Runtime error loading groups", e);
                android.os.Handler main = new android.os.Handler(context.getMainLooper());
                main.post(() -> {
                    Toast.makeText(context, "Ошибка загрузки групп", Toast.LENGTH_LONG).show();
                    hide();
                });
            }
        }).start();
    }

    public void hide() {
        overlayGroups.setVisibility(View.GONE);
        fieldsContainer.setVisibility(View.VISIBLE);
    }

    public boolean isVisible() {
        return overlayGroups != null && overlayGroups.getVisibility() == View.VISIBLE;
    }

    private void displayGroupsList(List<String> groups) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_list_item_1,
                groups
        );

        ListView listView = new ListView(context);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        listView.setPadding(0, 0, 0, 0);
        listView.setCacheColorHint(android.graphics.Color.TRANSPARENT);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = groups.get(position);
            if (ITEM_ADD.equals(selected)) {
                showAddGroupDialog();
            } else {
                etEquipmentGroup.setText(selected);
                hide();
            }
        });

        FrameLayout container = overlayGroups.findViewById(R.id.rvGroups);
        container.removeAllViews();
        container.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        container.setPadding(0, 0, 0, 0);

        container.addView(listView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
    }

    private void showAddGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Добавить группу");

        final EditText input = new EditText(context);
        input.setHint("Введите название группы");
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

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
            String group = input.getText().toString().trim().toUpperCase();
            if (!group.isEmpty()) {
                if (!group.startsWith("#")) {
                    group = "#" + group;
                }
                etEquipmentGroup.setText(group);
                hide();
                Toast.makeText(context, "Группа добавлена: " + group, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void hideKeyboard() {
        View view = overlayGroups.getRootView().findFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            view.clearFocus();
        }
    }
}