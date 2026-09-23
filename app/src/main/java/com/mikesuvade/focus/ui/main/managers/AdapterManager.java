package com.mikesuvade.focus.ui.main.managers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.ui.detail.DetailActivity;
import com.mikesuvade.focus.ui.main.GateValveAdapter;
import com.mikesuvade.focus.ui.main.SensorAdapter;
import com.mikesuvade.focus.ui.main.SetpointAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdapterManager {

    private static final String TAG = "AdapterManager";

    private final Context context;
    private final GateValveAdapter valveAdapter;
    private final SensorAdapter sensorAdapter;
    private final SetpointAdapter setpointAdapter;
    private final List<Integer> expandedPositions = new ArrayList<>();
    private final AdapterCallbacks callbacks;
    private ModeManager modeManager;

    public interface AdapterCallbacks {
        void showOverlay(String title, byte[] imageData, String description);
        void toggleExpanded(int position);
        void syncAdapterSelection();
        void updateButtonState();
        void showToast(String message);
        void launchValveDetailActivity(Intent intent);
        void launchSensorDetailActivity(Intent intent);
        void launchSetpointDetailActivity(Intent intent);
        void onCheckBoxChanged(GateValve valve, boolean isChecked);
        void hideKeyboardOnClick();
    }

    public AdapterManager(Context context, AdapterCallbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;
        this.valveAdapter = new GateValveAdapter();
        this.sensorAdapter = new SensorAdapter();
        this.setpointAdapter = new SetpointAdapter();

        setupValveAdapter();
        setupSensorAdapter();
        setupSetpointAdapter();
    }

    public void setModeManager(ModeManager modeManager) {
        this.modeManager = modeManager;
    }

    public GateValveAdapter getValveAdapter() {
        return valveAdapter;
    }

    public SensorAdapter getSensorAdapter() {
        return sensorAdapter;
    }

    public SetpointAdapter getSetpointAdapter() {
        return setpointAdapter;
    }

    private void setupValveAdapter() {
        valveAdapter.setOnItemClickListener((valve, position) -> {
            callbacks.hideKeyboardOnClick();

            callbacks.toggleExpanded(position);
        });

        valveAdapter.setOnItemLongClickListener(valve -> {

            String displayName = valve.getIsy();
            if (displayName == null || displayName.isEmpty()) {
                displayName = valve.getName();
            }
            if (displayName == null || displayName.isEmpty()) {
                displayName = "Без названия";
            }

            new AlertDialog.Builder(context)
                    .setTitle("Редактировать задвижку?")
                    .setMessage("Вы хотите отредактировать \"" + displayName + "\"?")
                    .setPositiveButton("Редактировать", (dialog, which) -> {
                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra(DetailActivity.EXTRA_TYPE, DetailActivity.TYPE_VALVE);
                        intent.putExtra(DetailActivity.EXTRA_ID, valve.getId());
                        boolean isFromUserDb = valve.getId() < 0;
                        intent.putExtra(DetailActivity.EXTRA_IS_FROM_USER_DB, isFromUserDb);
                        callbacks.launchValveDetailActivity(intent);
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            return true;
        });

        valveAdapter.setOnCheckBoxClickListener((valve, position, isChecked) -> {

            callbacks.onCheckBoxChanged(valve, isChecked);
            callbacks.syncAdapterSelection();
            callbacks.updateButtonState();
        });

        valveAdapter.setOnBlockingClickListener((valve, type) -> {
            // Страховка: если у user-задвижки не оказалось blob'ов (не подгрузились
            // в SearchManager, или запись пришла откуда-то ещё) — добираем из справочника.
            boolean missingBlobs =
                    valve.getNameSpaceViewOpen() == null
                            && valve.getNamespaceViewClose() == null
                            && valve.getNamespaceViewPerifer() == null;

            if (missingBlobs && valve.getOriginalId() > 0) {
                final GateValve finalValve = valve;
                new Thread(() -> {
                    try {
                        GateValve ref = ((com.mikesuvade.focus.MyApp) context.getApplicationContext())
                                .getRepository()
                                .getGateValveById(finalValve.getOriginalId());

                        if (ref != null) {
                            finalValve.setNameSpaceViewOpen(ref.getNameSpaceViewOpen());
                            finalValve.setNamespaceViewClose(ref.getNamespaceViewClose());
                            finalValve.setNamespaceViewPerifer(ref.getNamespaceViewPerifer());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Fallback blob load failed for originalId="
                                + finalValve.getOriginalId(), e);
                    }

                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(
                                () -> dispatchBlockingClick(finalValve, type));
                    } else {
                        dispatchBlockingClick(finalValve, type);
                    }
                }).start();
            } else {
                dispatchBlockingClick(valve, type);
            }
        });
    }

    private void setupSensorAdapter() {
        // 🔥 КОРОТКИЙ КЛИК - РАЗВОРАЧИВАЕМ КАРТОЧКУ (БАЯН)
        sensorAdapter.setOnItemClickListener((sensor, position) -> {
            callbacks.hideKeyboardOnClick();

            callbacks.toggleExpanded(position);
        });

        // 🔥 ДЛИННЫЙ КЛИК - ДИАЛОГ РЕДАКТИРОВАНИЯ
        sensorAdapter.setOnItemLongClickListener((sensor, position) -> {

            String displayName = sensor.getStMarkir();
            if (displayName == null || displayName.isEmpty()) {
                displayName = sensor.getFullName();
            }
            if (displayName == null || displayName.isEmpty()) {
                displayName = sensor.getKks();
            }
            if (displayName == null || displayName.isEmpty()) {
                displayName = "Без названия";
            }

            new AlertDialog.Builder(context)
                    .setTitle("Редактировать датчик?")
                    .setMessage("Вы хотите отредактировать \"" + displayName + "\"?")
                    .setPositiveButton("Редактировать", (dialog, which) -> {
                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra(DetailActivity.EXTRA_TYPE, DetailActivity.TYPE_SENSOR);
                        intent.putExtra(DetailActivity.EXTRA_ID, sensor.getId());
                        boolean isFromUserDb = sensor.getId() < 0;
                        intent.putExtra(DetailActivity.EXTRA_IS_FROM_USER_DB, isFromUserDb);
                        callbacks.launchSensorDetailActivity(intent);
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            return true;
        });
    }

    private void setupSetpointAdapter() {
        // 🔥 КОРОТКИЙ КЛИК - РАЗВОРАЧИВАЕМ КАРТОЧКУ (БАЯН)
        setpointAdapter.setOnItemClickListener((setpoint, position) -> {
            callbacks.hideKeyboardOnClick();

            callbacks.toggleExpanded(position);
        });

        // 🔥 ДЛИННЫЙ КЛИК - ДИАЛОГ РЕДАКТИРОВАНИЯ
        setpointAdapter.setOnItemLongClickListener((setpoint, position) -> {

            String displayName = setpoint.getPositionName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = setpoint.getName();
            }
            if (displayName == null || displayName.isEmpty()) {
                displayName = "Без названия";
            }

            new AlertDialog.Builder(context)
                    .setTitle("Редактировать уставку?")
                    .setMessage("Вы хотите отредактировать \"" + displayName + "\"?")
                    .setPositiveButton("Редактировать", (dialog, which) -> {
                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra(DetailActivity.EXTRA_TYPE, DetailActivity.TYPE_SETPOINT);
                        intent.putExtra(DetailActivity.EXTRA_ID, setpoint.getId());
                        boolean isFromUserDb = setpoint.getId() < 0;
                        intent.putExtra(DetailActivity.EXTRA_IS_FROM_USER_DB, isFromUserDb);
                        callbacks.launchSetpointDetailActivity(intent);
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            return true;
        });
    }

    public void syncAdapterSelection(List<Integer> selectedPositions) {
        valveAdapter.setSelectedPositions(selectedPositions);
    }

    public void clearExpanded() {
        valveAdapter.clearExpanded();
        sensorAdapter.clearExpanded();
        setpointAdapter.clearExpanded();
        expandedPositions.clear();
    }

    public void setExpanded(int position, boolean expanded) {
        if (modeManager == null) return;

        if (modeManager.isMainMode()) {
            valveAdapter.setExpanded(position, expanded);
        } else if (modeManager.isSensorsMode()) {
            sensorAdapter.setExpanded(position, expanded);
        } else if (modeManager.isSetpointsMode()) {
            setpointAdapter.setExpanded(position, expanded);
        }
    }

    public boolean isExpanded(int position) {
        return expandedPositions.contains(position);
    }

    public void addExpandedPosition(int position) {
        if (!expandedPositions.contains(position)) {
            expandedPositions.add(position);
        }
    }

    public void removeExpandedPosition(int position) {
        expandedPositions.remove(Integer.valueOf(position));
    }

    public void clearExpandedPositions() {
        expandedPositions.clear();
    }

    public List<Integer> getExpandedPositions() {
        return expandedPositions;
    }
    /**
     * Собирает заголовок/картинку/описание для оверлея блокировки и вызывает showOverlay.
     * Вынесено отдельно, чтобы можно было вызвать после асинхронной догрузки blob'ов.
     */
    private void dispatchBlockingClick(GateValve valve, String type) {
        String title = valve.getName() + " - ";
        byte[] imageData = null;
        String description = "";

        switch (type) {
            case "open":
                title += "БЛОКИРОВКИ \"ОТКРЫТИЕ\"";
                imageData = valve.getNameSpaceViewOpen();
                description = valve.getDescriptionBlockingOpen();
                break;
            case "close":
                title += "БЛОКИРОВКИ \"ЗАКРЫТИЕ\"";
                imageData = valve.getNamespaceViewClose();
                description = valve.getDescriptionBlockingClose();
                break;
            case "external":
                title += "ВНЕШНИЕ ЦЕПИ";
                imageData = valve.getNamespaceViewPerifer();
                description = valve.getDescriptionBlockingPerifer();
                break;
        }

        callbacks.showOverlay(title, imageData, description);
    }
}