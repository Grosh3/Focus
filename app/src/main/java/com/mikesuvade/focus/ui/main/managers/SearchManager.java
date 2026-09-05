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

    public void searchValves(String query) {
        Log.d(TAG, "=== searchValves ===");
        Log.d(TAG, "query = " + query);

        new Thread(() -> {
            try {
                List<GateValve> results = repository.searchGateValvesWithUser(query);
                Log.d(TAG, "results size = " + results.size());

                new Handler(Looper.getMainLooper()).post(() -> {
                    valveAdapter.updateData(results, new ArrayList<>());
                    rvGateValves.setVisibility(results.isEmpty() ? View.GONE : View.VISIBLE);
                    tvEmptySearch.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error searching valves", e);
            }
        }).start();
    }

    public void searchSensors(String query) {
        Log.d("SEARCH_MANAGER", "=== searchSensors ===");
        Log.d("SEARCH_MANAGER", "query = '" + query + "'");

        new Thread(() -> {
            try {
                if (query.trim().equalsIgnoreCase("#все")) {
                    List<Sensor> all = repository.getAllSensorsWithUser();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        sensorAdapter.updateData(all);
                        tvEmptySearch.setVisibility(all.isEmpty() ? View.VISIBLE : View.GONE);
                    });
                    return;
                }

                Log.d("SEARCH_MANAGER", "calling repository.searchSensorsWithUser()");
                List<Sensor> results = repository.searchSensorsWithUser(query);
                Log.d("SEARCH_MANAGER", "results size = " + results.size());

                new Handler(Looper.getMainLooper()).post(() -> {
                    sensorAdapter.updateData(results);
                    tvEmptySearch.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (Exception e) {
                Log.e("SEARCH_MANAGER", "Error searching sensors", e);
            }
        }).start();
    }
    public void searchSetpoints(String query) {
        Log.d(TAG, "=== searchSetpoints ===");
        Log.d(TAG, "query = " + query);

        new Thread(() -> {
            try {
                // ✅ Проверяем, является ли запрос поиском по группе (начинается с #)
                if (query.trim().startsWith("#")) {
                    String groupQuery = query.trim();
                    Log.d(TAG, "Searching by equipment group: " + groupQuery);

                    // Ищем уставки по группе оборудования
                    List<Setpoint> results = repository.searchSetpointsByGroupWithUser(groupQuery);
                    Log.d(TAG, "results size = " + results.size());

                    new Handler(Looper.getMainLooper()).post(() -> {
                        setpointAdapter.updateData(results);
                        tvEmptySearch.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                    });
                    return;
                }

                // Обычный поиск по уставкам (без #)
                List<Setpoint> results = repository.searchSetpointsWithUser(query);
                Log.d(TAG, "results size = " + results.size());

                new Handler(Looper.getMainLooper()).post(() -> {
                    setpointAdapter.updateData(results);
                    tvEmptySearch.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error searching setpoints", e);
            }
        }).start();
    }

    public void loadAllValves() {
        Log.d(TAG, "=== loadAllValves ===");
        new Thread(() -> {
            try {
                List<GateValve> all = repository.getAllGateValvesWithUser();
                new Handler(Looper.getMainLooper()).post(() -> {
                    valveAdapter.updateData(all, new ArrayList<>());
                    rvGateValves.setVisibility(all.isEmpty() ? View.GONE : View.VISIBLE);
                    tvEmptySearch.setVisibility(all.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading all valves", e);
            }
        }).start();
    }

    public void loadAllSensors() {
        Log.d(TAG, "=== loadAllSensors ===");
        new Thread(() -> {
            try {
                List<Sensor> all = repository.getAllSensorsWithUser();
                new Handler(Looper.getMainLooper()).post(() -> {
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
}