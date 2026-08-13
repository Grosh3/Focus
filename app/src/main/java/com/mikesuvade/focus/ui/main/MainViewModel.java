package com.mikesuvade.focus.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {

    private static final String TAG = "MainViewModel";
    private final IRepository repository;

    private final MutableLiveData<List<GateValve>> gateValves = new MutableLiveData<>();
    private List<GateValve> allGateValves = new ArrayList<>();
    private final List<GateValve> currentList = new ArrayList<>();
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

    public List<GateValve> getCurrentList() {
        return new ArrayList<>(currentList);
    }

    public int getCurrentListSize() {
        return currentList.size();
    }

    // ==========================================
    // 🔧 ЗАГРУЗКА ВСЕХ ДАННЫХ
    // ==========================================
    public void loadAllGateValves() {
        new Thread(() -> {
            try {
                allGateValves = repository.getAllGateValves();
                gateValves.postValue(allGateValves);
                Log.d(TAG, "Загружено " + allGateValves.size() + " задвижек");
            } catch (Exception e) {
                Log.e(TAG, "Error loading valves", e);
            }
        }).start();
    }

    // ==========================================
    // 🔍 ПОИСК
    // ==========================================
    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            gateValves.setValue(new ArrayList<>());
            return;
        }

        String trimmedQuery = query.trim();

        // Команда #все
        if (trimmedQuery.equalsIgnoreCase("#все")) {
            loadAllGateValves();
            return;
        }

        // Обычный поиск (>= 2 символов)
        if (trimmedQuery.length() < 2) {
            gateValves.setValue(new ArrayList<>());
            return;
        }

        new Thread(() -> {
            try {
                List<GateValve> results = repository.searchGateValves(trimmedQuery);
                gateValves.postValue(results);
                Log.d(TAG, "Найдено " + results.size() + " задвижек");
            } catch (Exception e) {
                Log.e(TAG, "Search error", e);
            }
        }).start();
    }

    // ==========================================
    // 📋 РАБОТА СО СПИСКОМ
    // ==========================================
    public void addToCurrentList(GateValve valve) {
        for (GateValve v : currentList) {
            if (v.getId() == valve.getId()) {
                return;
            }
        }
        currentList.add(valve);
        currentListSize.setValue(currentList.size());
        isRecording.setValue(true);
    }

    public void removeFromCurrentList(int position) {
        if (position >= 0 && position < currentList.size()) {
            currentList.remove(position);
            currentListSize.setValue(currentList.size());
            if (currentList.isEmpty()) {
                isRecording.setValue(false);
            }
        }
    }

    public void removeFromCurrentList(GateValve valve) {
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).getId() == valve.getId()) {
                currentList.remove(i);
                currentListSize.setValue(currentList.size());
                if (currentList.isEmpty()) {
                    isRecording.setValue(false);
                }
                return;
            }
        }
    }

    public void clearCurrentList() {
        currentList.clear();
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
        for (GateValve v : currentList) {
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
                int count = repository.getAllWorkSessions().size();
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
}