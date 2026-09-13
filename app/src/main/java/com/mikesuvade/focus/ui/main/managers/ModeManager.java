package com.mikesuvade.focus.ui.main.managers;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.R;
import com.mikesuvade.focus.ui.main.GateValveAdapter;
import com.mikesuvade.focus.ui.main.SensorAdapter;
import com.mikesuvade.focus.ui.main.SetpointAdapter;

public class ModeManager {

    private static final String TAG = "ModeManager";

    public static final int MODE_MAIN = 0;
    public static final int MODE_SENSORS = 1;
    public static final int MODE_SETPOINTS = 2;

    private int currentMode = MODE_MAIN;
    private final MainActivityCallback callback;

    private final RecyclerView rvGateValves;
    private final EditText etSearch;
    private final TextView tvEmptySearch;
    private final View bottomButtons;
    private final View btnHelp;
    private final View btnLists;
    private final View btnSettings;
    private final View btnNewValve;

    private final GateValveAdapter valveAdapter;
    private final SensorAdapter sensorAdapter;
    private final SetpointAdapter setpointAdapter;

    public interface MainActivityCallback {
        void activateSearchState();
        void deactivateSearchState();
        void showKeyboard();
        void updateSearchHint();
        void updateButtonState();

        void clearSearch();
        void setRvVisibility(boolean visible);
    }

    public ModeManager(MainActivityCallback callback,
                       RecyclerView rvGateValves,
                       EditText etSearch,
                       TextView tvEmptySearch,
                       View bottomButtons,
                       View btnHelp,
                       View btnLists,
                       View btnSettings,
                       View btnNewValve,
                       GateValveAdapter valveAdapter,
                       SensorAdapter sensorAdapter,
                       SetpointAdapter setpointAdapter) {
        this.callback = callback;
        this.rvGateValves = rvGateValves;
        this.etSearch = etSearch;
        this.tvEmptySearch = tvEmptySearch;
        this.bottomButtons = bottomButtons;
        this.btnHelp = btnHelp;
        this.btnLists = btnLists;
        this.btnSettings = btnSettings;
        this.btnNewValve = btnNewValve;
        this.valveAdapter = valveAdapter;
        this.sensorAdapter = sensorAdapter;
        this.setpointAdapter = setpointAdapter;
    }

    public int getCurrentMode() {
        return currentMode;
    }

    public void switchToMain() {
        Log.d(TAG, "=== switchToMain ===");
        currentMode = MODE_MAIN;

        updateTopButtonsVisibility(true);

        bottomButtons.setVisibility(View.VISIBLE);
        rvGateValves.setAdapter(valveAdapter);
        rvGateValves.setVisibility(View.GONE);

        etSearch.setText("");
        etSearch.clearFocus();

        callback.deactivateSearchState();
        callback.updateButtonState();
        callback.updateSearchHint();
        callback.updateButtonState();

        valveAdapter.updateData(new java.util.ArrayList<>(), new java.util.ArrayList<>());
    }

    public void switchToSensors() {
        Log.d(TAG, "=== switchToSensors ===");
        currentMode = MODE_SENSORS;

        updateTopButtonsVisibility(false);

        bottomButtons.setVisibility(View.GONE);
        rvGateValves.setAdapter(sensorAdapter);
        rvGateValves.setVisibility(View.VISIBLE);

        btnNewValve.setVisibility(View.GONE);

        sensorAdapter.updateData(new java.util.ArrayList<>());
        tvEmptySearch.setVisibility(View.GONE);

        etSearch.setText("");
        etSearch.requestFocus();

        callback.activateSearchState();
        callback.showKeyboard();
        callback.updateSearchHint();
        callback.updateButtonState(); // → вызывает MainActivity.updateButtonState() → updateTitle()
    }

    public void switchToSetpoints() {
        Log.d(TAG, "=== switchToSetpoints ===");
        currentMode = MODE_SETPOINTS;

        updateTopButtonsVisibility(false);

        bottomButtons.setVisibility(View.GONE);
        rvGateValves.setAdapter(setpointAdapter);
        rvGateValves.setVisibility(View.VISIBLE);

        btnNewValve.setVisibility(View.GONE);

        setpointAdapter.updateData(new java.util.ArrayList<>());
        tvEmptySearch.setVisibility(View.GONE);

        etSearch.setText("");
        etSearch.requestFocus();

        callback.activateSearchState();
        callback.showKeyboard();
        callback.updateSearchHint();
        callback.updateButtonState();
    }

    public boolean isMainMode() {
        return currentMode == MODE_MAIN;
    }

    public boolean isSensorsMode() {
        return currentMode == MODE_SENSORS;
    }

    public boolean isSetpointsMode() {
        return currentMode == MODE_SETPOINTS;
    }

    private void updateTopButtonsVisibility(boolean showExtraButtons) {
        if (showExtraButtons) {
            btnHelp.setVisibility(View.VISIBLE);
            btnLists.setVisibility(View.VISIBLE);
            btnSettings.setVisibility(View.VISIBLE);
        } else {
            btnHelp.setVisibility(View.VISIBLE);
            btnLists.setVisibility(View.GONE);
            btnSettings.setVisibility(View.GONE);
        }
    }
}