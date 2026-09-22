package com.mikesuvade.focus.ui.detail.editor;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.repository.IRepository;
import com.mikesuvade.focus.ui.detail.helper.EntityCopyUtils;

public class ValveEditor extends BaseEntityEditor {

    private GateValve currentValve;
    private GateValve originalValve;
    private int valveId;

    private EditText etIsy;
    private EditText etValveName;
    private EditText etValveKks;
    private EditText etPowerCabinet;
    private EditText etLocationDescription;
    private EditText etOnPlace;
    private EditText etValveFullName;

    public ValveEditor(AppCompatActivity activity, IRepository repository) {
        super(activity, repository);
    }

    @Override
    public int getEntityType() { return TYPE_VALVE; }

    @Override
    public void bindViews(View root) {
        bindCommonViews(root);

        etIsy = root.findViewById(R.id.etIsy);
        etValveName = root.findViewById(R.id.etValveName);
        etValveKks = root.findViewById(R.id.etValveKks);
        etPowerCabinet = root.findViewById(R.id.etPowerCabinet);
        etLocationDescription = root.findViewById(R.id.etLocationDescription);
        etOnPlace = root.findViewById(R.id.etOnPlace);
        etValveFullName = root.findViewById(R.id.etValveFullName);

        View valveFields = root.findViewById(R.id.valveFields);
        View sensorFields = root.findViewById(R.id.sensorFields);
        View setpointFields = root.findViewById(R.id.setpointFields);
        if (valveFields != null) valveFields.setVisibility(View.VISIBLE);
        if (sensorFields != null) sensorFields.setVisibility(View.GONE);
        if (setpointFields != null) setpointFields.setVisibility(View.GONE);
    }

    @Override
    public void createEmpty() {
        currentValve = new GateValve();
        originalValve = new GateValve();
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

        valveId = intent.getIntExtra(EXTRA_ID, 0);
        boolean isFromUserDb = valveId < 0;

        new Thread(() -> {
            try {
                GateValve found = null;
                if (isFromUserDb) {
                    for (GateValve v : repository.getAllUserGateValves()) {
                        if (v.getId() == valveId && v.getIsDeleted() != 1) {
                            found = v;
                            break;
                        }
                    }
                } else {
                    found = repository.getGateValveById(valveId);
                }

                if (found == null) {
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, "Задвижка не найдена", Toast.LENGTH_SHORT).show();
                        activity.finish();
                    });
                    return;
                }

                currentValve = found;
                originalValve = EntityCopyUtils.copyValve(found);

                activity.runOnUiThread(() -> {
                    updateDeleteButtonVisibility();
                    displayData();
                });

            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                    activity.finish();
                });
            }
        }).start();
    }

    @Override
    public void displayData() {
        if (currentValve == null) return;

        String isy = currentValve.getIsy();
        activity.setTitle((isy != null && !isy.isEmpty() ? isy : "Задвижка"));

        etIsy.setText(currentValve.getIsy());
        etValveName.setText(currentValve.getName());
        etValveKks.setText(currentValve.getKks());
        etPowerCabinet.setText(currentValve.getPowerCabinet());
        etLocationDescription.setText(currentValve.getLocationDescription());
        etOnPlace.setText(currentValve.getOnPlace());
        etValveFullName.setText(currentValve.getFullName());
    }

    @Override
    public String getScreenTitle() {
        if (currentValve == null) return "Задвижка";
        String isy = currentValve.getIsy();
        return (isy != null && !isy.isEmpty()) ? isy : "Задвижка";
    }

    @Override
    public void save() {
        if (isSaved) return;
        if (currentValve == null) return;

        String isy = textOf(etIsy);
        String name = textOf(etValveName);
        String powerCabinet = textOf(etPowerCabinet);
        String locationDescription = textOf(etLocationDescription);
        String onPlace = textOf(etOnPlace);
        String fullName = textOf(etValveFullName);
        String kks = textOf(etValveKks);

        boolean allFieldsEmpty = isy.isEmpty()
                && name.isEmpty()
                && powerCabinet.isEmpty()
                && locationDescription.isEmpty()
                && onPlace.isEmpty()
                && fullName.isEmpty()
                && kks.isEmpty();

        if (isNew && allFieldsEmpty) {
            Toast.makeText(activity, "Заполните хотя бы одно поле", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNew && allFieldsEmpty) {
            showDeleteDialog("задвижку",
                    currentValve.getName() != null ? currentValve.getName() : "без названия",
                    this::delete);
            return;
        }

        GateValve toSave = new GateValve();
        toSave.setIsy(isy);
        toSave.setName(name);
        toSave.setPowerCabinet(powerCabinet);
        toSave.setLocationDescription(locationDescription);
        toSave.setOnPlace(onPlace);
        toSave.setFullName(fullName);
        toSave.setKks(kks.isEmpty() ? null : kks);

        // ❌ Остальные поля (nameEng, ap50, mark, cdaCabinet, cdaCabinetPosition, slot)
        // НЕ заполняем — они обнуляются при сохранении. В БД1 они остаются как есть.

        if (currentValve != null) {
            toSave.setNameSpaceViewOpen(currentValve.getNameSpaceViewOpen());
            toSave.setNamespaceViewClose(currentValve.getNamespaceViewClose());
            toSave.setNamespaceViewPerifer(currentValve.getNamespaceViewPerifer());
            toSave.setDescriptionBlockingOpen(currentValve.getDescriptionBlockingOpen());
            toSave.setDescriptionBlockingClose(currentValve.getDescriptionBlockingClose());
            toSave.setDescriptionBlockingPerifer(currentValve.getDescriptionBlockingPerifer());
        }

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
                    toSave.setEditedAtValve(now);

                    long id = repository.insertUserGateValve(toSave);
                    if (id != -1) {
                        currentValve = toSave;
                        onSaveSuccess("✅ Создано");
                    } else {
                        onSaveError("❌ Ошибка создания");
                    }
                } else {
                    boolean fromUser = currentValve.getId() < 0;

                    if (fromUser) {
                        toSave.setId(currentValve.getId());
                        toSave.setOriginalId(currentValve.getOriginalId());
                        toSave.setIsDeleted(0);
                        toSave.setIsEdited(1);
                        toSave.setCustom(true);
                        toSave.setCreatedAt(currentValve.getCreatedAt());
                        toSave.setEditedAtValve(now);

                        repository.updateUserGateValve(toSave);
                        currentValve = toSave;
                        onSaveSuccess("✅ Сохранено");

                    } else {
                        GateValve existing = repository.getUserGateValveByOriginalId(currentValve.getId());
                        if (existing != null) {
                            toSave.setId(existing.getId());
                            toSave.setOriginalId(currentValve.getId());
                            toSave.setIsDeleted(0);
                            toSave.setIsEdited(1);
                            toSave.setCustom(true);
                            toSave.setCreatedAt(existing.getCreatedAt());
                            toSave.setEditedAtValve(now);

                            repository.updateUserGateValve(toSave);
                            currentValve = toSave;
                            onSaveSuccess("✅ Сохранено");
                        } else {
                            int newId = generateNegativeId();
                            toSave.setId(newId);
                            toSave.setOriginalId(currentValve.getId());
                            toSave.setIsDeleted(0);
                            toSave.setIsEdited(1);
                            toSave.setCustom(true);
                            toSave.setCreatedAt(now);
                            toSave.setEditedAtValve(now);

                            long id = repository.insertUserGateValve(toSave);
                            if (id != -1) {
                                currentValve = toSave;
                                onSaveSuccess("✅ Скопировано");
                            } else {
                                onSaveError("❌ Ошибка копирования");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                onSaveError("Ошибка: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void delete() {
        isDeleting = true;
        new Thread(() -> {
            try {
                boolean fromUser = currentValve.getId() < 0;
                if (fromUser && currentValve.getOriginalId() > 0) {
                    repository.markGateValveAsDeleted(currentValve.getOriginalId());
                } else if (fromUser) {
                    repository.deleteUserGateValve(currentValve.getId());
                } else {
                    repository.markGateValveAsDeleted(currentValve.getId());
                }
                onSaveSuccess("Удалено");
            } catch (Exception e) {
                onSaveError("Ошибка удаления");
            }
        }).start();
    }

    @Override
    public boolean hasChanges() {
        if (currentValve == null || originalValve == null) return false;
        return !eq(textOf(etIsy), originalValve.getIsy())
                || !eq(textOf(etValveName), originalValve.getName())
                || !eq(textOf(etPowerCabinet), originalValve.getPowerCabinet())
                || !eq(textOf(etLocationDescription), originalValve.getLocationDescription())
                || !eq(textOf(etOnPlace), originalValve.getOnPlace())
                || !eq(textOf(etValveFullName), originalValve.getFullName())
                || !eq(textOf(etValveKks), originalValve.getKks());
    }

    @Override
    public boolean isOverlayVisible() { return false; }

    @Override
    public void hideOverlay() {}

    @Override
    public void reloadFromDb() {
        if (currentValve == null) return;
        int id = currentValve.getId();
        if (id == 0) return;

        Intent i = new Intent();
        i.putExtra(EXTRA_ID, id);
        i.putExtra(EXTRA_IS_NEW, false);
        loadFromIntent(i);
    }

    @Override
    protected String getValveDisplayName() {
        if (currentValve == null) return "";
        String isy = currentValve.getIsy();
        if (isy != null && !isy.isEmpty()) return isy;
        return currentValve.getName() != null ? currentValve.getName() : "";
    }
}