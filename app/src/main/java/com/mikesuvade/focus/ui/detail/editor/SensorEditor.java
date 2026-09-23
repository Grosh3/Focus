package com.mikesuvade.focus.ui.detail.editor;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.Sensor;
import com.mikesuvade.focus.domain.repository.IRepository;
import com.mikesuvade.focus.ui.detail.helper.EntityCopyUtils;

public class SensorEditor extends BaseEntityEditor {

    private Sensor currentSensor;
    private Sensor originalSensor;
    private int sensorId;

    private EditText etStMarking;
    private EditText etFullNameSensor;
    private EditText etLocationSensor;
    private EditText etSensorKks;
    private EditText etModelSensor;
    private EditText etModSensor;
    private EditText etAdditionalInfo;
    private EditText etMin;
    private EditText etMax;
    private EditText etUnitMeasure;
    private EditText etCva;
    private EditText etDampingTime;

    public SensorEditor(AppCompatActivity activity, IRepository repository) {
        super(activity, repository);
    }

    @Override
    public int getEntityType() { return TYPE_SENSOR; }

    @Override
    public void bindViews(View root) {
        bindCommonViews(root);

        etStMarking = root.findViewById(R.id.etStMarking);
        etFullNameSensor = root.findViewById(R.id.etFullNameSensor);
        etLocationSensor = root.findViewById(R.id.etLocationSensor);
        etSensorKks = root.findViewById(R.id.etSensorKks);
        etModelSensor = root.findViewById(R.id.etModelSensor);
        etModSensor = root.findViewById(R.id.etModSensor);
        etAdditionalInfo = root.findViewById(R.id.etAdditionalInfo);
        etMin = root.findViewById(R.id.etMin);
        etMax = root.findViewById(R.id.etMax);
        etUnitMeasure = root.findViewById(R.id.etUnitMeasure);
        etCva = root.findViewById(R.id.etCva);
        etDampingTime = root.findViewById(R.id.etDampingTime);

        View valveFields = root.findViewById(R.id.valveFields);
        View sensorFields = root.findViewById(R.id.sensorFields);
        View setpointFields = root.findViewById(R.id.setpointFields);
        if (valveFields != null) valveFields.setVisibility(View.GONE);
        if (sensorFields != null) sensorFields.setVisibility(View.VISIBLE);
        if (setpointFields != null) setpointFields.setVisibility(View.GONE);
    }

    @Override
    public void createEmpty() {
        currentSensor = new Sensor();
        originalSensor = new Sensor();
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

        sensorId = intent.getIntExtra(EXTRA_ID, 0);
        boolean isFromUserDb = sensorId < 0;

        new Thread(() -> {
            try {
                Sensor found = null;
                if (isFromUserDb) {
                    for (Sensor s : repository.getAllUserSensors()) {
                        if (s.getId() == sensorId && s.getIsDeleted() != 1) {
                            found = s;
                            break;
                        }
                    }
                } else {
                    found = repository.getSensorById(sensorId);
                }

                if (found == null) {
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, "Датчик не найден", Toast.LENGTH_SHORT).show();
                        activity.finish();
                    });
                    return;
                }

                currentSensor = found;
                originalSensor = EntityCopyUtils.copySensor(found);

                activity.runOnUiThread(() -> {
                    updateDeleteButtonVisibility();
                    displayData();
                });

            } catch (android.database.sqlite.SQLiteException e) {
                logAndToast("SQLite error loading sensor id=" + sensorId,
                        e, "Ошибка базы данных при загрузке датчика");
                activity.runOnUiThread(() -> activity.finish());
            } catch (RuntimeException e) {
                logAndToast("Runtime error loading sensor id=" + sensorId,
                        e, "Ошибка загрузки датчика");
                activity.runOnUiThread(() -> activity.finish());
            }
        }).start();
    }

    @Override
    public void displayData() {
        if (currentSensor == null) return;

        String stMarking = currentSensor.getStMarkir();
        activity.setTitle((stMarking != null && !stMarking.isEmpty() ? stMarking : "Датчик"));

        etStMarking.setText(currentSensor.getStMarkir());
        etFullNameSensor.setText(currentSensor.getFullName());
        etLocationSensor.setText(currentSensor.getLocation());
        etSensorKks.setText(currentSensor.getKks());
        etModelSensor.setText(currentSensor.getModelSensor());
        etModSensor.setText(currentSensor.getModSensor());
        etAdditionalInfo.setText(currentSensor.getAdditionalInfo());
        etMin.setText(String.valueOf(currentSensor.getMinVal()));
        etMax.setText(String.valueOf(currentSensor.getMaxVal()));
        etUnitMeasure.setText(currentSensor.getMeasureUnit());
        etCva.setText(currentSensor.getCva());
        etDampingTime.setText(currentSensor.getDampingTime());
    }

    @Override
    public String getScreenTitle() {
        if (currentSensor == null) return "Датчик";
        String st = currentSensor.getStMarkir();
        return (st != null && !st.isEmpty()) ? st : "Датчик";
    }

    @Override
    public void save() {
        if (isSaved) return;
        if (currentSensor == null) return;

        String stMarking = textOf(etStMarking);
        String fullName = textOf(etFullNameSensor);
        String location = textOf(etLocationSensor);
        String kks = textOf(etSensorKks);
        String modelSensor = textOf(etModelSensor);
        String modSensor = textOf(etModSensor);
        String additionalInfo = textOf(etAdditionalInfo);
        double min = parseDouble(textOf(etMin));
        double max = parseDouble(textOf(etMax));
        String unitMeasure = textOf(etUnitMeasure);
        String cva = textOf(etCva);
        String dampingTime = textOf(etDampingTime);

        boolean allFieldsEmpty = stMarking.isEmpty() && fullName.isEmpty()
                && location.isEmpty() && kks.isEmpty() && modelSensor.isEmpty()
                && modSensor.isEmpty() && additionalInfo.isEmpty()
                && min == 0 && max == 0 && unitMeasure.isEmpty()
                && cva.isEmpty() && dampingTime.isEmpty();

        if (isNew && allFieldsEmpty) {
            Toast.makeText(activity, "Заполните хотя бы одно поле", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNew && allFieldsEmpty) {
            showDeleteDialog("датчик",
                    currentSensor.getStMarkir() != null ? currentSensor.getStMarkir() : "без названия",
                    this::delete);
            return;
        }

        Sensor toSave = new Sensor();
        toSave.setStMarkir(stMarking);
        toSave.setFullName(fullName);
        toSave.setLocation(location);
        toSave.setKks(kks);
        toSave.setModelSensor(modelSensor);
        toSave.setModSensor(modSensor);
        toSave.setAdditionalInfo(additionalInfo);
        toSave.setMinVal(min);
        toSave.setMaxVal(max);
        toSave.setMeasureUnit(unitMeasure);
        toSave.setCva(cva);
        toSave.setDampingTime(dampingTime);

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

                    long id = repository.insertUserSensor(toSave);
                    if (id != -1) {
                        currentSensor = toSave;
                        onSaveSuccess("✅ Создано");
                    } else {
                        onSaveError("❌ Ошибка создания");
                    }
                } else {
                    boolean fromUser = currentSensor.getId() < 0;

                    if (fromUser) {
                        toSave.setId(currentSensor.getId());
                        toSave.setOriginalId(currentSensor.getOriginalId());
                        toSave.setIsDeleted(0);
                        toSave.setIsEdited(1);
                        toSave.setCustom(true);
                        toSave.setCreatedAt(currentSensor.getCreatedAt());
                        toSave.setEditedAt(now);

                        repository.updateUserSensor(toSave);
                        currentSensor = toSave;
                        onSaveSuccess("✅ Сохранено");

                    } else {
                        Sensor existing = repository.getUserSensorByOriginalId(currentSensor.getId());
                        if (existing != null) {
                            toSave.setId(existing.getId());
                            toSave.setOriginalId(currentSensor.getId());
                            toSave.setIsDeleted(0);
                            toSave.setIsEdited(1);
                            toSave.setCustom(true);
                            toSave.setCreatedAt(existing.getCreatedAt());
                            toSave.setEditedAt(now);

                            repository.updateUserSensor(toSave);
                            currentSensor = toSave;
                            onSaveSuccess("✅ Сохранено");
                        } else {
                            int newId = generateNegativeId();
                            toSave.setId(newId);
                            toSave.setOriginalId(currentSensor.getId());
                            toSave.setIsDeleted(0);
                            toSave.setIsEdited(1);
                            toSave.setCustom(true);
                            toSave.setCreatedAt(now);
                            toSave.setEditedAt(now);

                            long id = repository.insertUserSensor(toSave);
                            if (id != -1) {
                                currentSensor = toSave;
                                onSaveSuccess("✅ Скопировано");
                            } else {
                                onSaveError("❌ Ошибка копирования");
                            }
                        }
                    }
                }
            } catch (android.database.sqlite.SQLiteException e) {
                Log.e("SensorEditor", "SQLite error saving sensor", e);
                onSaveError("Ошибка БД при сохранении датчика");
            } catch (RuntimeException e) {
                Log.e("SensorEditor", "Runtime error saving sensor", e);
                onSaveError("Ошибка сохранения датчика");
            }
        }).start();
    }

    @Override
    public void delete() {
        isDeleting = true;
        new Thread(() -> {
            try {
                boolean fromUser = currentSensor.getId() < 0;
                if (fromUser && currentSensor.getOriginalId() > 0) {
                    repository.markSensorAsDeleted(currentSensor.getOriginalId());
                } else if (fromUser) {
                    repository.deleteUserSensor(currentSensor.getId());
                } else {
                    repository.markSensorAsDeleted(currentSensor.getId());
                }
                onSaveSuccess("Удалено");
            } catch (android.database.sqlite.SQLiteException e) {
                Log.e("SensorEditor", "SQLite error deleting sensor", e);
                onSaveError("Ошибка БД при удалении датчика");
            } catch (RuntimeException e) {
                Log.e("SensorEditor", "Runtime error deleting sensor", e);
                onSaveError("Ошибка удаления датчика");
            }
        }).start();
    }

    @Override
    public boolean hasChanges() {
        if (currentSensor == null || originalSensor == null) return false;
        return !eq(textOf(etStMarking), originalSensor.getStMarkir())
                || !eq(textOf(etFullNameSensor), originalSensor.getFullName())
                || !eq(textOf(etLocationSensor), originalSensor.getLocation())
                || !eq(textOf(etSensorKks), originalSensor.getKks())
                || !eq(textOf(etModelSensor), originalSensor.getModelSensor())
                || !eq(textOf(etModSensor), originalSensor.getModSensor())
                || !eq(textOf(etAdditionalInfo), originalSensor.getAdditionalInfo())
                || !eq(textOf(etMin), String.valueOf(originalSensor.getMinVal()))
                || !eq(textOf(etMax), String.valueOf(originalSensor.getMaxVal()))
                || !eq(textOf(etUnitMeasure), originalSensor.getMeasureUnit())
                || !eq(textOf(etCva), originalSensor.getCva())
                || !eq(textOf(etDampingTime), originalSensor.getDampingTime());
    }

    @Override
    public boolean isOverlayVisible() { return false; }

    @Override
    public void hideOverlay() {}

    @Override
    public void reloadFromDb() {
        if (currentSensor == null) return;
        int id = currentSensor.getId();
        if (id == 0) return;

        Intent i = new Intent();
        i.putExtra(EXTRA_ID, id);
        i.putExtra(EXTRA_IS_NEW, false);
        loadFromIntent(i);
    }

    @Override
    protected String getSensorDisplayName() {
        if (currentSensor == null) return "";
        return currentSensor.getStMarkir() != null ? currentSensor.getStMarkir() : "";
    }
}