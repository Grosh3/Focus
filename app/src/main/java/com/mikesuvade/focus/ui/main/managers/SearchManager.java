package com.mikesuvade.focus.ui.main.managers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.Sensor;
import com.mikesuvade.focus.domain.models.Setpoint;
import com.mikesuvade.focus.domain.repository.IRepository;
import com.mikesuvade.focus.ui.main.GateValveAdapter;
import com.mikesuvade.focus.ui.main.SensorAdapter;
import com.mikesuvade.focus.ui.main.SetpointAdapter;

import java.util.ArrayList;
import java.util.List;
import com.mikesuvade.focus.domain.models.ValveItem;
import com.mikesuvade.focus.utils.AppState;

public class SearchManager {

    private static final String TAG = "SearchManager";

    private final IRepository repository;
    private final GateValveAdapter valveAdapter;
    private final SensorAdapter sensorAdapter;
    private final SetpointAdapter setpointAdapter;
    private final TextView tvEmptySearch;
    private final RecyclerView rvGateValves;

    public SearchManager(IRepository repository,
                         GateValveAdapter valveAdapter,
                         SensorAdapter sensorAdapter,
                         SetpointAdapter setpointAdapter,
                         TextView tvEmptySearch,
                         RecyclerView rvGateValves) {
        this.repository = repository;
        this.valveAdapter = valveAdapter;
        this.sensorAdapter = sensorAdapter;
        this.setpointAdapter = setpointAdapter;
        this.tvEmptySearch = tvEmptySearch;
        this.rvGateValves = rvGateValves;
    }

    // 🔥 МЕТОД ДЛЯ ЗАДВИЖЕК
    public void searchValves(String query) {
        Log.d(TAG, "=== searchValves ===");
        Log.d(TAG, "query = " + query);

        new Thread(() -> {
            try {
                List<GateValve> results = repository.searchGateValvesWithUser(query);

                AppState appState = AppState.getInstance();
                List<Integer> selectedIds = new ArrayList<>();

                // 🔥 1. ID из активной сессии
                if (appState.hasActiveSession()) {
                    String sessionId = appState.getLastOpenedSessionId();
                    List<ValveItem> items = repository.getUserSessionItemsBySession(sessionId);
                    for (ValveItem item : items) {
                        selectedIds.add(item.getGateValveId());
                    }
                }

                // 🔥 2. ID из несохраненного списка
                selectedIds.addAll(appState.getUnsavedListIds());

                List<Integer> positions = new ArrayList<>();
                for (int i = 0; i < results.size(); i++) {
                    if (selectedIds.contains(results.get(i).getId())) {
                        positions.add(i);
                    }
                }

                final List<Integer> finalPositions = positions;
                new Handler(Looper.getMainLooper()).post(() -> {
                    valveAdapter.updateData(results, finalPositions);
                    rvGateValves.setVisibility(results.isEmpty() ? View.GONE : View.VISIBLE);
                    tvEmptySearch.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error searching valves", e);
            }
        }).start();
    }

    // 🔥 МЕТОД ДЛЯ ДАТЧИКОВ
    public void searchSensors(String query) {
        Log.d("SEARCH_MANAGER", "=== searchSensors ===");
        Log.d("SEARCH_MANAGER", "query = '" + query + "'");

        new Thread(() -> {
            try {
                if (query.trim().equalsIgnoreCase("#все")) {
                    List<Sensor> all = repository.getAllSensorsWithUser();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        sensorAdapter.setSearchQuery("");
                        sensorAdapter.updateData(all);
                        tvEmptySearch.setVisibility(all.isEmpty() ? View.VISIBLE : View.GONE);
                    });
                    return;
                }

                String cleanQueryForHighlight = cleanQueryForHighlight(query);

                Log.d("SEARCH_MANAGER", "calling repository.searchSensorsWithUser()");
                List<Sensor> results = repository.searchSensorsWithUser(query);
                Log.d("SEARCH_MANAGER", "results size = " + results.size());

                new Handler(Looper.getMainLooper()).post(() -> {
                    sensorAdapter.setSearchQuery(cleanQueryForHighlight);
                    sensorAdapter.updateData(results);
                    tvEmptySearch.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (Exception e) {
                Log.e("SEARCH_MANAGER", "Error searching sensors", e);
            }
        }).start();
    }

    // 🔥 МЕТОД ДЛЯ УСТАВОК
    public void searchSetpoints(String query) {
        Log.d(TAG, "=== searchSetpoints ===");
        Log.d(TAG, "query = " + query);

        new Thread(() -> {
            try {
                List<Setpoint> results = repository.searchSetpointsWithUser(query);
                new Handler(Looper.getMainLooper()).post(() -> {
                    setpointAdapter.updateData(results);
                    tvEmptySearch.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error searching setpoints", e);
            }
        }).start();
    }

    // 🔥 МЕТОДЫ ЗАГРУЗКИ ВСЕХ
    public void loadAllValves() {
        Log.d(TAG, "=== loadAllValves ===");
        new Thread(() -> {
            try {
                List<GateValve> all = repository.getAllGateValvesWithUser();

                AppState appState = AppState.getInstance();
                List<Integer> selectedIds = new ArrayList<>();

                if (appState.hasActiveSession()) {
                    String sessionId = appState.getLastOpenedSessionId();
                    List<ValveItem> items = repository.getUserSessionItemsBySession(sessionId);
                    for (ValveItem item : items) {
                        selectedIds.add(item.getGateValveId());
                    }
                }

                // 🔥 ДОБАВЛЯЕМ ID ИЗ НЕСОХРАНЕННОГО СПИСКА
                selectedIds.addAll(appState.getUnsavedListIds());

                new Handler(Looper.getMainLooper()).post(() -> {
                    List<Integer> positions = findPositions(all, selectedIds);
                    valveAdapter.updateData(all, positions);
                    rvGateValves.setVisibility(all.isEmpty() ? View.GONE : View.VISIBLE);
                    tvEmptySearch.setVisibility(all.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading all valves", e);
            }
        }).start();
    }

    // Вспомогательный метод для поиска позиций
    private List<Integer> findPositions(List<GateValve> all, List<Integer> selectedIds) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            if (selectedIds.contains(all.get(i).getId())) {
                positions.add(i);
            }
        }
        return positions;
    }

    public void loadAllSensors() {
        Log.d(TAG, "=== loadAllSensors ===");
        new Thread(() -> {
            try {
                List<Sensor> all = repository.getAllSensorsWithUser();
                new Handler(Looper.getMainLooper()).post(() -> {
                    sensorAdapter.setSearchQuery("");
                    sensorAdapter.updateData(all);
                    tvEmptySearch.setVisibility(all.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading all sensors", e);
            }
        }).start();
    }

    public void loadAllSetpoints() {
        Log.d(TAG, "=== loadAllSetpoints ===");
        new Thread(() -> {
            try {
                List<Setpoint> all = repository.getAllSetpointsWithUser();
                new Handler(Looper.getMainLooper()).post(() -> {
                    setpointAdapter.updateData(all);
                    tvEmptySearch.setVisibility(all.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading all setpoints", e);
            }
        }).start();
    }

    public void clearSearch() {
        valveAdapter.updateData(new ArrayList<>(), new ArrayList<>());
        sensorAdapter.updateData(new ArrayList<>());
        setpointAdapter.updateData(new ArrayList<>());
        tvEmptySearch.setVisibility(View.GONE);
        rvGateValves.setVisibility(View.VISIBLE);
    }

    /**
     * Очищает запрос от приставки для подсветки KKS.
     * Примеры:
     *   "50CVA22CT001"  → "CVA22CT001"
     *   "d80CVA22CT001" → "CVA22CT001"
     *   "CVA22CT001"    → "CVA22CT001"
     */
    private String cleanQueryForHighlight(String query) {
        if (query == null || query.isEmpty()) return "";
        String clean = query.trim().replaceAll("[\\s\\-.]", "");

        // 🔥 Отрезаем "^[A-Za-z][0-9]+" — например d80, D80, KKS123
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^[A-Za-z][0-9]+")
                .matcher(clean);
        if (m.find()) {
            String rest = clean.substring(m.end());
            if (rest.length() >= 3) return rest;
            return clean;
        }

        // Отрезаем "^[0-9]+" — например 50, 80
        String rest = clean.replaceFirst("^[0-9]+", "");
        if (!rest.isEmpty() && rest.length() >= 3) return rest;
        return clean;
    }

}