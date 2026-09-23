package com.mikesuvade.focus.ui.detail.editor;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.Setpoint;
import com.mikesuvade.focus.domain.repository.IRepository;
import com.mikesuvade.focus.ui.detail.groups.EquipmentGroupPicker;
import com.mikesuvade.focus.ui.detail.helper.EntityCopyUtils;

public class SetpointEditor extends BaseEntityEditor {

    private Setpoint currentSetpoint;
    private Setpoint originalSetpoint;
    private int setpointId;

    private EditText etPositionName;
    private EditText etSetpointName;
    private EditText etSetpointValue;
    private EditText etDelayTime;
    private EditText etOperation;
    private EditText etNotes;
    private EditText etEquipmentGroup;
    private EditText etSetpointLocation;

    private View overlayGroups;
    private EquipmentGroupPicker groupPicker;

    public SetpointEditor(AppCompatActivity activity, IRepository repository) {
        super(activity, repository);
    }

    @Override
    public int getEntityType() { return TYPE_SETPOINT; }

    @Override
    public void bindViews(View root) {
        bindCommonViews(root);

        etPositionName = root.findViewById(R.id.etPositionName);
        etSetpointName = root.findViewById(R.id.etSetpointName);
        etSetpointValue = root.findViewById(R.id.etSetpointValue);
        etDelayTime = root.findViewById(R.id.etDelayTime);
        etOperation = root.findViewById(R.id.etOperation);
        etNotes = root.findViewById(R.id.etNotes);
        etEquipmentGroup = root.findViewById(R.id.etEquipmentGroup);
        etSetpointLocation = root.findViewById(R.id.etSetpointLocation);

        overlayGroups = root.findViewById(R.id.overlayGroups);
        groupPicker = new EquipmentGroupPicker(activity, repository, etEquipmentGroup, overlayGroups, fieldsContainer);
        groupPicker.attach();

        View valveFields = root.findViewById(R.id.valveFields);
        View sensorFields = root.findViewById(R.id.sensorFields);
        View setpointFields = root.findViewById(R.id.setpointFields);
        if (valveFields != null) valveFields.setVisibility(View.GONE);
        if (sensorFields != null) sensorFields.setVisibility(View.GONE);
        if (setpointFields != null) setpointFields.setVisibility(View.VISIBLE);
    }

    @Override
    public void createEmpty() {
        currentSetpoint = new Setpoint();
        originalSetpoint = new Setpoint();
        currentSetpoint.setEquipmentGroup("");
    }

    @Override
    public void loadFromIntent(Intent intent) {
        isNew = intent.getBooleanExtra(EXTRA_IS_NEW, false);

        if (isNew) {
            createEmpty();
            updateDeleteButtonVisibility();
            displayData();
            return;
        }

        if (!intent.hasExtra(EXTRA_ID)) {
            Toast.makeText(activity, "Ошибка: ID не передан", Toast.LENGTH_SHORT).show();
            activity.finish();
            return;
        }

        setpointId = intent.getIntExtra(EXTRA_ID, 0);
        boolean isFromUserDb = setpointId < 0;

        new Thread(() -> {
            try {
                Setpoint found = null;
                if (isFromUserDb) {
                    for (Setpoint sp : repository.getAllUserSetpoints()) {
                        if (sp.getId() == setpointId && sp.getIsDeleted() != 1) {
                            found = sp;
                            break;
                        }
                    }
                } else {
                    found = repository.getSetpointById(setpointId);
                }

                if (found == null) {
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, "Уставка не найдена", Toast.LENGTH_SHORT).show();
                        activity.finish();
                    });
                    return;
                }

                currentSetpoint = found;
                originalSetpoint = EntityCopyUtils.copySetpoint(found);

                activity.runOnUiThread(() -> {
                    updateDeleteButtonVisibility();
                    displayData();
                });

            } catch (android.database.sqlite.SQLiteException e) {
                logAndToast("SQLite error loading setpoint id=" + setpointId,
                        e, "Ошибка базы данных при загрузке уставки");
                activity.runOnUiThread(() -> activity.finish());
            } catch (RuntimeException e) {
                logAndToast("Runtime error loading setpoint id=" + setpointId,
                        e, "Ошибка загрузки уставки");
                activity.runOnUiThread(() -> activity.finish());
            }
        }).start();
    }

    @Override
    public void displayData() {
        if (currentSetpoint == null) return;

        String positionName = currentSetpoint.getPositionName();
        activity.setTitle((positionName != null && !positionName.isEmpty() ? positionName : "Уставка"));

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
            etEquipmentGroup.setText(group.toUpperCase());
        } else {
            etEquipmentGroup.setText("НЕТ ГРУППЫ");
        }
    }

    @Override
    public String getScreenTitle() {
        if (currentSetpoint == null) return "Уставка";
        String pos = currentSetpoint.getPositionName();
        return (pos != null && !pos.isEmpty()) ? pos : "Уставка";
    }

    @Override
    public void save() {
        if (isSaved) return;
        if (currentSetpoint == null) return;

        String positionName = textOf(etPositionName);
        String name = textOf(etSetpointName);
        String setpointValue = textOf(etSetpointValue);
        String delayTime = textOf(etDelayTime);
        String operation = textOf(etOperation);
        String notes = textOf(etNotes);
        String equipmentGroup = textOf(etEquipmentGroup).toUpperCase();
        String location = textOf(etSetpointLocation);

        if (equipmentGroup.equals("НЕТ ГРУППЫ")
                || equipmentGroup.equals("#НЕТ ГРУППЫ")
                || equipmentGroup.equals("🚫 НЕТ ГРУППЫ")) {
            equipmentGroup = "";
        }
        if (!equipmentGroup.isEmpty() && !equipmentGroup.startsWith("#")) {
            equipmentGroup = "#" + equipmentGroup;
        }

        boolean allFieldsEmpty = positionName.isEmpty() && name.isEmpty()
                && setpointValue.isEmpty() && delayTime.isEmpty()
                && operation.isEmpty() && notes.isEmpty()
                && equipmentGroup.isEmpty() && location.isEmpty();

        if (isNew && allFieldsEmpty) {
            Toast.makeText(activity, "Заполните хотя бы одно поле", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNew && allFieldsEmpty) {
            showDeleteDialog("уставку",
                    currentSetpoint.getPositionName() != null ? currentSetpoint.getPositionName() : "без названия",
                    this::delete);
            return;
        }

        Setpoint toSave = new Setpoint();
        toSave.setPositionName(positionName);
        toSave.setName(name);
        toSave.setSetpointValue(setpointValue);
        toSave.setDelayTime(delayTime);
        toSave.setOperation(operation);
        toSave.setNotes(notes);
        toSave.setEquipmentGroup(equipmentGroup);
        toSave.setLocation(location);

        String now = getCurrentDateTime();
        isSaved = true;

        new Thread(() -> {
            try {
                if (isNew) {
                    int newId = generateNegativeId();
                    toSave.setId(newId);
                    toSave.setOriginalId(0);
                    toSave.setIsDeleted(0);
                    toSave.setIsEdited(1);
                    toSave.setCustom(true);
                    toSave.setCreatedAt(now);
                    toSave.setEditedAt(now);

                    long id = repository.insertUserSetpoint(toSave);
                    if (id != -1) {
                        currentSetpoint = toSave;
                        onSaveSuccess("✅ Создано");
                    } else {
                        onSaveError("❌ Ошибка создания");
                    }
                } else {
                    boolean fromUser = currentSetpoint.getId() < 0;

                    if (fromUser) {
                        toSave.setId(currentSetpoint.getId());
                        toSave.setOriginalId(currentSetpoint.getOriginalId());
                        toSave.setIsDeleted(0);
                        toSave.setIsEdited(1);
                        toSave.setCustom(true);
                        toSave.setCreatedAt(currentSetpoint.getCreatedAt());
                        toSave.setEditedAt(now);

                        repository.updateUserSetpoint(toSave);
                        currentSetpoint = toSave;
                        onSaveSuccess("✅ Сохранено");

                    } else {
                        Setpoint existing = repository.getUserSetpointByOriginalId(currentSetpoint.getId());
                        if (existing != null) {
                            toSave.setId(existing.getId());
                            toSave.setOriginalId(currentSetpoint.getId());
                            toSave.setIsDeleted(0);
                            toSave.setIsEdited(1);
                            toSave.setCustom(true);
                            toSave.setCreatedAt(existing.getCreatedAt());
                            toSave.setEditedAt(now);

                            repository.updateUserSetpoint(toSave);
                            currentSetpoint = toSave;
                            onSaveSuccess("✅ Сохранено");
                        } else {
                            int newId = generateNegativeId();
                            toSave.setId(newId);
                            toSave.setOriginalId(currentSetpoint.getId());
                            toSave.setIsDeleted(0);
                            toSave.setIsEdited(1);
                            toSave.setCustom(true);
                            toSave.setCreatedAt(now);
                            toSave.setEditedAt(now);

                            long id = repository.insertUserSetpoint(toSave);
                            if (id != -1) {
                                currentSetpoint = toSave;
                                onSaveSuccess("✅ Скопировано");
                            } else {
                                onSaveError("❌ Ошибка копирования");
                            }
                        }
                    }
                }
            } catch (android.database.sqlite.SQLiteException e) {
                Log.e("SetpointEditor", "SQLite error saving setpoint", e);
                onSaveError("Ошибка БД при сохранении уставки");
            } catch (RuntimeException e) {
                Log.e("SetpointEditor", "Runtime error saving setpoint", e);
                onSaveError("Ошибка сохранения уставки");
            }
        }).start();
    }

    @Override
    public void delete() {
        isDeleting = true;
        new Thread(() -> {
            try {
                boolean fromUser = currentSetpoint.getId() < 0;
                if (fromUser && currentSetpoint.getOriginalId() > 0) {
                    repository.markSetpointAsDeleted(currentSetpoint.getOriginalId());
                } else if (fromUser) {
                    repository.deleteUserSetpoint(currentSetpoint.getId());
                } else {
                    repository.markSetpointAsDeleted(currentSetpoint.getId());
                }
                onSaveSuccess("Удалено");
            } catch (android.database.sqlite.SQLiteException e) {
                Log.e("SetpointEditor", "SQLite error deleting setpoint", e);
                onSaveError("Ошибка БД при удалении уставки");
            } catch (RuntimeException e) {
                Log.e("SetpointEditor", "Runtime error deleting setpoint", e);
                onSaveError("Ошибка удаления уставки");
            }
        }).start();
    }

    @Override
    public boolean hasChanges() {
        if (currentSetpoint == null || originalSetpoint == null) return false;

        String currentGroup = textOf(etEquipmentGroup).toUpperCase();
        if (currentGroup.equals("НЕТ ГРУППЫ") || currentGroup.equals("#НЕТ ГРУППЫ")) currentGroup = "";

        String originalGroup = originalSetpoint.getEquipmentGroup() != null
                ? originalSetpoint.getEquipmentGroup() : "";

        return !eq(textOf(etPositionName), originalSetpoint.getPositionName())
                || !eq(textOf(etSetpointName), originalSetpoint.getName())
                || !eq(textOf(etSetpointValue), originalSetpoint.getSetpointValue())
                || !eq(textOf(etDelayTime), originalSetpoint.getDelayTime())
                || !eq(textOf(etOperation), originalSetpoint.getOperation())
                || !eq(textOf(etNotes), originalSetpoint.getNotes())
                || !currentGroup.equals(originalGroup)
                || !eq(textOf(etSetpointLocation), originalSetpoint.getLocation());
    }

    @Override
    public boolean isOverlayVisible() {
        return groupPicker != null && groupPicker.isVisible();
    }

    @Override
    public void hideOverlay() {
        if (groupPicker != null) groupPicker.hide();
    }

    @Override
    public void reloadFromDb() {
        if (currentSetpoint == null) return;
        int id = currentSetpoint.getId();
        if (id == 0) return;

        Intent i = new Intent();
        i.putExtra(EXTRA_ID, id);
        i.putExtra(EXTRA_IS_NEW, false);
        loadFromIntent(i);
    }

    @Override
    protected String getSetpointDisplayName() {
        if (currentSetpoint == null) return "";
        String pos = currentSetpoint.getPositionName();
        if (pos != null && !pos.isEmpty()) return pos;
        return currentSetpoint.getName() != null ? currentSetpoint.getName() : "";
    }
}