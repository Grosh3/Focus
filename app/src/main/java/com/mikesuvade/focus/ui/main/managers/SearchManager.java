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
import com.mikesuvade.focus.domain.models.ValveItem;
import com.mikesuvade.focus.domain.repository.IRepository;
import com.mikesuvade.focus.ui.main.GateValveAdapter;
import com.mikesuvade.focus.ui.main.SensorAdapter;
import com.mikesuvade.focus.ui.main.SetpointAdapter;
import com.mikesuvade.focus.utils.AppState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchManager {

    private static final String TAG = "SearchManager";
    private static final long DEBOUNCE_MS = 250L;

    public interface OnSearchErrorListener {
        void onSearchError(String userMessage, Throwable cause);
    }

    private final IRepository repository;
    private final GateValveAdapter valveAdapter;
    private final SensorAdapter sensorAdapter;
    private final SetpointAdapter setpointAdapter;
    private final TextView tvEmptySearch;
    private final RecyclerView rvGateValves;

    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    private OnSearchErrorListener errorListener;

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

    public void setOnSearchErrorListener(OnSearchErrorListener listener) {
        this.errorListener = listener;
    }

    private void notifyError(String userMessage, Throwable cause) {
        if (errorListener != null) {
            new Handler(Looper.getMainLooper()).post(
                    () -> errorListener.onSearchError(userMessage, cause));
        }
    }

    // ==========================================
    // 🔥 ОБЩИЙ ДЕБАУНС
    // ==========================================

    private void scheduleSearch(Runnable task) {
        if (pendingSearch != null) {
            debounceHandler.removeCallbacks(pendingSearch);
        }
        pendingSearch = task;
        debounceHandler.postDelayed(task, DEBOUNCE_MS);
    }

    // ==========================================
    // 🔍 ЗАДВИЖКИ
    // ==========================================

    public void searchValves(String query) {
        final String finalQuery = query;

        scheduleSearch(() -> {
            final int requestId = requestCounter.incrementAndGet();

            new Thread(() -> {
                try {
                    List<GateValve> results = repository.searchGateValvesWithUser(finalQuery);

                    fillBlockingBlobsFromReference(results);

                    AppState appState = AppState.getInstance();
                    List<Integer> selectedIds = new ArrayList<>();

                    if (appState.hasActiveSession()) {
                        String sessionId = appState.getLastOpenedSessionId();
                        List<ValveItem> items = repository.getUserSessionItemsBySession(sessionId);
                        for (ValveItem item : items) {
                            selectedIds.add(item.getGateValveId());
                        }
                    }

                    selectedIds.addAll(appState.getUnsavedListIds());

                    List<Integer> positions = new ArrayList<>();
                    for (int i = 0; i < results.size(); i++) {
                        if (selectedIds.contains(results.get(i).getId())) {
                            positions.add(i);
                        }
                    }

                    if (requestId != requestCounter.get()) {
                        return;
                    }

                    final List<GateValve> finalResults = results;
                    final List<Integer> finalPositions = positions;

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (requestId != requestCounter.get()) return;

                        valveAdapter.updateData(finalResults, finalPositions);
                        rvGateValves.setVisibility(finalResults.isEmpty() ? View.GONE : View.VISIBLE);
                        tvEmptySearch.setVisibility(finalResults.isEmpty() ? View.VISIBLE : View.GONE);
                    });

                } catch (android.database.sqlite.SQLiteException e) {
                    Log.e(TAG, "SQLite error in searchValves", e);
                    notifyError("Ошибка базы данных. Возможно, структура устарела.", e);
                } catch (RuntimeException e) {
                    Log.e(TAG, "Runtime error in searchValves", e);
                    notifyError("Ошибка поиска. Попробуйте ещё раз.", e);
                }
            }).start();
        });
    }

    public void loadAllValves() {
        scheduleSearch(() -> {
            final int requestId = requestCounter.incrementAndGet();

            new Thread(() -> {
                try {
                    List<GateValve> all = repository.getAllGateValvesWithUser();

                    fillBlockingBlobsFromReference(all);

                    AppState appState = AppState.getInstance();
                    List<Integer> selectedIds = new ArrayList<>();

                    if (appState.hasActiveSession()) {
                        String sessionId = appState.getLastOpenedSessionId();
                        List<ValveItem> items = repository.getUserSessionItemsBySession(sessionId);
                        for (ValveItem item : items) {
                            selectedIds.add(item.getGateValveId());
                        }
                    }

                    selectedIds.addAll(appState.getUnsavedListIds());

                    if (requestId != requestCounter.get()) {
                        return;
                    }

                    final List<GateValve> finalAll = all;

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (requestId != requestCounter.get()) return;

                        List<Integer> positions = findPositions(finalAll, selectedIds);
                        valveAdapter.updateData(finalAll, positions);
                        rvGateValves.setVisibility(finalAll.isEmpty() ? View.GONE : View.VISIBLE);
                        tvEmptySearch.setVisibility(finalAll.isEmpty() ? View.VISIBLE : View.GONE);
                    });

                } catch (android.database.sqlite.SQLiteException e) {
                    Log.e(TAG, "SQLite error in loadAllValves", e);
                    notifyError("Ошибка базы данных. Возможно, структура устарела.", e);
                } catch (RuntimeException e) {
                    Log.e(TAG, "Runtime error in loadAllValves", e);
                    notifyError("Ошибка загрузки данных.", e);
                }
            }).start();
        });
    }

    // ==========================================
    // 🔍 ДАТЧИКИ
    // ==========================================

    public void searchSensors(String query) {
        final String finalQuery = query;

        scheduleSearch(() -> {
            final int requestId = requestCounter.incrementAndGet();

            new Thread(() -> {
                try {
                    String cleanQueryForHighlight = cleanQueryForHighlight(finalQuery);
                    List<Sensor> results = repository.searchSensorsWithUser(finalQuery);

                    if (requestId != requestCounter.get()) {
                        return;
                    }

                    final List<Sensor> finalResults = results;
                    final String finalHighlight = cleanQueryForHighlight;

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (requestId != requestCounter.get()) return;

                        sensorAdapter.setSearchQuery(finalHighlight);
                        sensorAdapter.updateData(finalResults);
                        tvEmptySearch.setVisibility(finalResults.isEmpty() ? View.VISIBLE : View.GONE);
                    });

                } catch (android.database.sqlite.SQLiteException e) {
                    Log.e(TAG, "SQLite error in searchSensors", e);
                    notifyError("Ошибка базы данных. Возможно, структура устарела.", e);
                } catch (RuntimeException e) {
                    Log.e(TAG, "Runtime error in searchSensors", e);
                    notifyError("Ошибка поиска датчиков.", e);
                }
            }).start();
        });
    }

    public void loadAllSensors() {
        scheduleSearch(() -> {
            final int requestId = requestCounter.incrementAndGet();

            new Thread(() -> {
                try {
                    List<Sensor> all = repository.getAllSensorsWithUser();

                    if (requestId != requestCounter.get()) {
                        return;
                    }

                    final List<Sensor> finalAll = all;

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (requestId != requestCounter.get()) return;

                        sensorAdapter.setSearchQuery("");
                        sensorAdapter.updateData(finalAll);
                        tvEmptySearch.setVisibility(finalAll.isEmpty() ? View.VISIBLE : View.GONE);
                    });

                } catch (android.database.sqlite.SQLiteException e) {
                    Log.e(TAG, "SQLite error in loadAllSensors", e);
                    notifyError("Ошибка базы данных. Возможно, структура устарела.", e);
                } catch (RuntimeException e) {
                    Log.e(TAG, "Runtime error in loadAllSensors", e);
                    notifyError("Ошибка загрузки датчиков.", e);
                }
            }).start();
        });
    }

    // ==========================================
    // 🔍 УСТАВКИ
    // ==========================================

    public void searchSetpoints(String query) {
        final String finalQuery = query;

        scheduleSearch(() -> {
            final int requestId = requestCounter.incrementAndGet();

            new Thread(() -> {
                try {
                    List<Setpoint> results = repository.searchSetpointsWithUser(finalQuery);

                    if (requestId != requestCounter.get()) {
                        return;
                    }

                    final List<Setpoint> finalResults = results;

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (requestId != requestCounter.get()) return;

                        setpointAdapter.updateData(finalResults);
                        tvEmptySearch.setVisibility(finalResults.isEmpty() ? View.VISIBLE : View.GONE);
                    });

                } catch (android.database.sqlite.SQLiteException e) {
                    Log.e(TAG, "SQLite error in searchSetpoints", e);
                    notifyError("Ошибка базы данных. Возможно, структура устарела.", e);
                } catch (RuntimeException e) {
                    Log.e(TAG, "Runtime error in searchSetpoints", e);
                    notifyError("Ошибка поиска уставок.", e);
                }
            }).start();
        });
    }

    public void loadAllSetpoints() {
        scheduleSearch(() -> {
            final int requestId = requestCounter.incrementAndGet();

            new Thread(() -> {
                try {
                    List<Setpoint> all = repository.getAllSetpointsWithUser();

                    if (requestId != requestCounter.get()) {
                        return;
                    }

                    final List<Setpoint> finalAll = all;

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (requestId != requestCounter.get()) return;

                        setpointAdapter.updateData(finalAll);
                        tvEmptySearch.setVisibility(finalAll.isEmpty() ? View.VISIBLE : View.GONE);
                    });

                } catch (android.database.sqlite.SQLiteException e) {
                    Log.e(TAG, "SQLite error in loadAllSetpoints", e);
                    notifyError("Ошибка базы данных. Возможно, структура устарела.", e);
                } catch (RuntimeException e) {
                    Log.e(TAG, "Runtime error in loadAllSetpoints", e);
                    notifyError("Ошибка загрузки уставок.", e);
                }
            }).start();
        });
    }

    // ==========================================
    // 🧹 ОЧИСТКА
    // ==========================================

    public void clearSearch() {
        if (pendingSearch != null) {
            debounceHandler.removeCallbacks(pendingSearch);
            pendingSearch = null;
        }
        requestCounter.incrementAndGet();

        valveAdapter.updateData(new ArrayList<>(), new ArrayList<>());
        sensorAdapter.updateData(new ArrayList<>());
        setpointAdapter.updateData(new ArrayList<>());
        tvEmptySearch.setVisibility(View.GONE);
        rvGateValves.setVisibility(View.VISIBLE);
    }

    // ==========================================
    // 🧩 ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ==========================================

    private List<Integer> findPositions(List<GateValve> all, List<Integer> selectedIds) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            if (selectedIds.contains(all.get(i).getId())) {
                positions.add(i);
            }
        }
        return positions;
    }

    private void fillBlockingBlobsFromReference(List<GateValve> valves) {
        if (valves == null || valves.isEmpty()) return;

        for (GateValve v : valves) {
            if (v.getId() >= 0) continue;
            if (v.getOriginalId() <= 0) continue;

            boolean alreadyFilled =
                    v.getNameSpaceViewOpen() != null
                            && v.getNamespaceViewClose() != null
                            && v.getNamespaceViewPerifer() != null;

            if (alreadyFilled) continue;

            try {
                GateValve ref = repository.getGateValveById(v.getOriginalId());
                if (ref == null) continue;

                v.setNameSpaceViewOpen(ref.getNameSpaceViewOpen());
                v.setNamespaceViewClose(ref.getNamespaceViewClose());
                v.setNamespaceViewPerifer(ref.getNamespaceViewPerifer());
            } catch (RuntimeException e) {
                Log.e(TAG, "fillBlockingBlobsFromReference failed for id=" + v.getId()
                        + ", originalId=" + v.getOriginalId(), e);
            }
        }
    }

    private String cleanQueryForHighlight(String query) {
        if (query == null || query.isEmpty()) return "";
        String clean = query.trim().replaceAll("[\\s\\-.]", "");

        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^[A-Za-z][0-9]+")
                .matcher(clean);
        if (m.find()) {
            String rest = clean.substring(m.end());
            if (rest.length() >= 3) return rest;
            return clean;
        }

        String rest = clean.replaceFirst("^[0-9]+", "");
        if (!rest.isEmpty() && rest.length() >= 3) return rest;
        return clean;
    }
}