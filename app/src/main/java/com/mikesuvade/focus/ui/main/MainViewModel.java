package com.mikesuvade.focus.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.ValveItem;
import com.mikesuvade.focus.domain.models.ValveWorkSession;
import com.mikesuvade.focus.domain.repository.IRepository;
import com.mikesuvade.focus.utils.AppState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainViewModel extends ViewModel {

    private static final String TAG = "MainViewModel";
    private final IRepository repository;

    private final MutableLiveData<List<GateValve>> gateValves = new MutableLiveData<>();
    private List<GateValve> allGateValves = new ArrayList<>();

    // ==========================================
    // 🔥 currentList через LiveData
    // ==========================================
    private final MutableLiveData<List<GateValve>> currentListLive = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Boolean> isRecording = new MutableLiveData<>(false);
    private final MutableLiveData<String> listName = new MutableLiveData<>("НОВЫЙ 1");
    private final MutableLiveData<Integer> currentListSize = new MutableLiveData<>(0);
    private int nextListNumber = 1;

    public MainViewModel(IRepository repository) {
        this.repository = repository;
        updateNextListNumber();
        logDatabaseCount();
        logFirstRecords();
        gateValves.setValue(new ArrayList<>());
    }

    public LiveData<List<GateValve>> getGateValves() {
        return gateValves;
    }

    public LiveData<Boolean> getIsRecording() {
        return isRecording;
    }

    public LiveData<String> getListName() {
        return listName;
    }

    public LiveData<Integer> getCurrentListSizeLiveData() {
        return currentListSize;
    }

    public LiveData<List<GateValve>> getCurrentListLive() {
        return currentListLive;
    }

    public int getCurrentListSize() {
        List<GateValve> list = currentListLive.getValue();
        return list != null ? list.size() : 0;
    }

    public void updateCurrentListSize(int size) {
        Log.d("CURRENT_LIST", "updateCurrentListSize: size = " + size);
        // 🔥 ИСПРАВЛЕНО: используем postValue вместо setValue
        currentListSize.postValue(size);
    }

    // ==========================================
    // 🔧 ЗАГРУЗКА ВСЕХ ДАННЫХ
    // ==========================================
    public void loadAllGateValves() {
        Log.d("MAIN_DEBUG", "=== loadAllGateValves START ===");
        new Thread(() -> {
            try {
                allGateValves = repository.getAllGateValves();
                Log.d("MAIN_DEBUG", "allGateValves size = " + allGateValves.size());

                for (int i = 0; i < allGateValves.size(); i++) {
                    GateValve valve = allGateValves.get(i);
                    Log.d("MAIN_DEBUG", "  ALL [" + i + "] id=" + valve.getId() +
                            ", name=" + valve.getName() +
                            ", isy=" + valve.getIsy());
                }

                gateValves.postValue(allGateValves);
                Log.d("MAIN_DEBUG", "=== loadAllGateValves END ===");
            } catch (Exception e) {
                Log.e("MAIN_DEBUG", "Error loading valves", e);
            }
        }).start();
    }

    // ==========================================
    // 🔍 ПОИСК
    // ==========================================
    public void search(String query) {
        Log.d("MAIN_DEBUG", "=== search START ===");
        Log.d("MAIN_DEBUG", "query = " + query);

        if (query == null || query.trim().isEmpty()) {
            Log.d("MAIN_DEBUG", "query is empty, clearing list");
            gateValves.setValue(new ArrayList<>());
            return;
        }

        String trimmedQuery = query.trim();

        if (trimmedQuery.equalsIgnoreCase("#все")) {
            Log.d("MAIN_DEBUG", "loading all valves");
            loadAllGateValves();
            return;
        }

        if (trimmedQuery.length() < 2) {
            Log.d("MAIN_DEBUG", "query length < 2, clearing list");
            gateValves.setValue(new ArrayList<>());
            return;
        }

        new Thread(() -> {
            try {
                List<GateValve> results = repository.searchGateValves(trimmedQuery);
                Log.d("MAIN_DEBUG", "search results size = " + results.size());

                for (int i = 0; i < results.size(); i++) {
                    GateValve valve = results.get(i);
                    Log.d("MAIN_DEBUG", "  RESULT [" + i + "] id=" + valve.getId() +
                            ", name=" + valve.getName() +
                            ", isy=" + valve.getIsy());
                }

                gateValves.postValue(results);
                Log.d("MAIN_DEBUG", "=== search END ===");
            } catch (Exception e) {
                Log.e("MAIN_DEBUG", "Search error", e);
            }
        }).start();
    }

    // ==========================================
    // 📋 РАБОТА СО СПИСКОМ (через LiveData)
    // ==========================================
    public void addToCurrentList(GateValve valve) {
        Log.d("CURRENT_LIST", "addToCurrentList: id=" + valve.getId() + ", name=" + valve.getName());

        List<GateValve> list = currentListLive.getValue();
        if (list == null) list = new ArrayList<>();

        for (GateValve v : list) {
            if (v.getId() == valve.getId()) {
                Log.d("CURRENT_LIST", "Дубликат, пропускаем");
                return;
            }
        }

        list.add(valve);
        currentListLive.setValue(list);
        currentListSize.setValue(list.size());
        isRecording.setValue(true);
        Log.d("CURRENT_LIST", "currentList size = " + list.size());
    }

    public void removeFromCurrentList(GateValve valve) {
        List<GateValve> list = currentListLive.getValue();
        if (list == null) return;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == valve.getId()) {
                list.remove(i);
                currentListLive.setValue(list);
                currentListSize.setValue(list.size());
                if (list.isEmpty()) {
                    isRecording.setValue(false);
                }
                return;
            }
        }
    }

    public void clearCurrentList() {
        currentListLive.setValue(new ArrayList<>());
        currentListSize.setValue(0);
        isRecording.setValue(false);
    }

    public void setListName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            listName.setValue(name.trim());
        } else {
            listName.setValue("НОВЫЙ " + nextListNumber);
        }
    }

    public boolean isInCurrentList(GateValve valve) {
        List<GateValve> list = currentListLive.getValue();
        if (list == null) return false;

        for (GateValve v : list) {
            if (v.getId() == valve.getId()) {
                return true;
            }
        }
        return false;
    }

    public void setRecording(boolean recording) {
        isRecording.setValue(recording);
    }

    public void refreshData() {
        updateNextListNumber();
    }

    private void updateNextListNumber() {
        new Thread(() -> {
            try {
                int count = repository.getAllUserWorkSessions().size();  // 🔥 ИЗМЕНЕНО
                nextListNumber = count + 1;
                listName.postValue("НОВЫЙ " + nextListNumber);
            } catch (Exception e) {
                Log.e(TAG, "Error updating list number", e);
            }
        }).start();
    }

    private void logDatabaseCount() {
        new Thread(() -> {
            try {
                List<GateValve> all = repository.getAllGateValves();
                Log.d("DB_CHECK", "Количество задвижек в БД: " + (all != null ? all.size() : 0));
            } catch (Exception e) {
                Log.e("DB_CHECK", "Ошибка при проверке БД", e);
            }
        }).start();
    }

    private void logFirstRecords() {
        new Thread(() -> {
            try {
                List<GateValve> all = repository.getAllGateValves();
                if (all != null && !all.isEmpty()) {
                    Log.d("DB_CHECK", "=== ПЕРВЫЕ 10 ЗАПИСЕЙ ===");
                    for (int i = 0; i < Math.min(10, all.size()); i++) {
                        GateValve v = all.get(i);
                        Log.d("DB_CHECK", i + ": name=" + v.getName() + ", kks=" + v.getKks() + ", name_eng=" + v.getNameEng());
                    }
                }
            } catch (Exception e) {
                Log.e("DB_CHECK", "Ошибка", e);
            }
        }).start();
    }

    // ==========================================
    // 💾 СОХРАНЕНИЕ В ПОЛЬЗОВАТЕЛЬСКУЮ БД
    // ==========================================

    public void saveSessionToDb(ValveWorkSession session) {
        new Thread(() -> {
            try {
                // 🔥 ИСПОЛЬЗУЕМ ПОЛЬЗОВАТЕЛЬСКУЮ БД
                ValveWorkSession existing = repository.getUserWorkSessionById(session.getSessionId());
                if (existing != null) {
                    repository.updateUserWorkSession(session);
                } else {
                    repository.insertUserWorkSession(session);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void saveUserSessionToDb(ValveWorkSession session) {
        new Thread(() -> {
            try {
                // 🔥 ИСПОЛЬЗУЕМ ПОЛЬЗОВАТЕЛЬСКУЮ БД
                ValveWorkSession existing = repository.getUserWorkSessionById(session.getSessionId());
                if (existing != null) {
                    repository.updateUserWorkSession(session);
                } else {
                    repository.insertUserWorkSession(session);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void addToActiveSession(GateValve valve) {
        Log.d("SESSY", "=== addToActiveSession ВЫЗВАН ===");
        Log.d("SESSY", "valve.id = " + valve.getId());
        Log.d("SESSY", "valve.name = " + valve.getName());

        String sessionId = AppState.getInstance().getLastOpenedSessionId();
        Log.d("SESSY", "sessionId = " + sessionId);

        if (sessionId == null || sessionId.isEmpty()) {
            Log.d("SESSY", "ERROR: sessionId is NULL or EMPTY!");
            return;
        }

        new Thread(() -> {
            try {
                ValveItem item = new ValveItem();
                item.setParentSessionId(sessionId);
                item.setGateValveId(valve.getId());

                // 🔥 СОХРАНЯЕМ ВСЕ ДАННЫЕ ЗАДВИЖКИ
                item.setGateValveName(valve.getName() != null ? valve.getName() : "");
                item.setGateValveIsy(valve.getIsy() != null ? valve.getIsy() : "");
                item.setGateValveKks(valve.getKks() != null ? valve.getKks() : "");
                item.setGateValvePowerCabinet(valve.getPowerCabinet() != null ? valve.getPowerCabinet() : "");
                item.setGateValveLocationDescription(valve.getLocationDescription() != null ? valve.getLocationDescription() : "");
                item.setGateValveOnPlace(valve.getOnPlace() != null ? valve.getOnPlace() : "");
                item.setGateValveFullName(valve.getFullName() != null ? valve.getFullName() : "");

                item.setIsAssembled(1);
                item.setMotorDisabled(0);
                item.setBoxRemoved(0);
                item.setIsChecked(0);
                item.setCheckedAt(null);
                item.setOperationTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

                Log.d("SESSY", "Inserting user session item...");
                long result = repository.insertUserSessionItem(item);
                Log.d("SESSY", "insertUserSessionItem result = " + result);

                int newSize = repository.getUserSessionItemsBySession(sessionId).size();
                updateCurrentListSize(newSize);
                Log.d("SESSY", "Session items count = " + newSize);

            } catch (Exception e) {
                Log.e("SESSY", "Ошибка при добавлении", e);
            }
        }).start();
    }
}