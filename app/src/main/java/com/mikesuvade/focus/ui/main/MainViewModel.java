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

    // Список задвижек для отображения (результат поиска)
    private final MutableLiveData<List<GateValve>> gateValves = new MutableLiveData<>();

    // Все задвижки (для сброса поиска)
    private List<GateValve> allGateValves = new ArrayList<>();

    // Текущий список задвижек (временный, в памяти)
    private final List<GateValve> currentList = new ArrayList<>();

    // Состояние записи
    private final MutableLiveData<Boolean> isRecording = new MutableLiveData<>(false);

    // Имя списка
    private final MutableLiveData<String> listName = new MutableLiveData<>("НОВЫЙ 1");

    // Размер текущего списка
    private final MutableLiveData<Integer> currentListSize = new MutableLiveData<>(0);

    // Следующий номер для "НОВЫЙ X"
    private int nextListNumber = 1;

    public MainViewModel(IRepository repository) {
        this.repository = repository;
        loadAllGateValves();
        updateNextListNumber();
        logDatabaseCount();
        logFirstRecords();
    }

    // ==================== GETTERS ====================

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

    // ==================== ЗАГРУЗКА ДАННЫХ ====================

    private void loadAllGateValves() {
        new Thread(() -> {
            try {
                // Сначала ищем в gate_valves_user
                List<GateValve> userValves = repository.getCustomGateValves();
                if (userValves != null && !userValves.isEmpty()) {
                    allGateValves = userValves;
                } else {

                    // Если нет, берём из gate_valves
                    allGateValves = repository.getAllGateValves();
                }
                gateValves.postValue(allGateValves);
            } catch (Exception e) {
                Log.e(TAG, "Error loading valves", e);
            }
        }).start();
    }

    private void updateNextListNumber() {
        new Thread(() -> {
            try {
                // Считаем количество сохранённых списков
                int count = repository.getAllWorkSessions().size();
                nextListNumber = count + 1;
                listName.postValue("НОВЫЙ " + nextListNumber);
            } catch (Exception e) {
                Log.e(TAG, "Error updating list number", e);
            }
        }).start();
    }

    // ==================== ПОИСК ====================
    public void search(String query) {
        if (query == null || query.trim().length() < 2) {  // ← МЕНЯЕМ 3 НА 2
            // Меньше 2 символов - показываем все
            gateValves.setValue(allGateValves);
            return;
        }

        new Thread(() -> {
            try {
                List<GateValve> results = repository.searchGateValves(query.trim());
                gateValves.postValue(results);
            } catch (Exception e) {
                Log.e(TAG, "Search error", e);
            }
        }).start();
    }


    // ==================== РАБОТА СО СПИСКОМ ====================

    public void addToCurrentList(GateValve valve) {
        // Проверяем, есть ли уже в списке
        for (GateValve v : currentList) {
            if (v.getId() == valve.getId()) {
                return; // уже есть
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

    // ==================== ПРОВЕРКИ ====================

    public boolean isInCurrentList(GateValve valve) {
        for (GateValve v : currentList) {
            if (v.getId() == valve.getId()) {
                return true;
            }
        }
        return false;
    }

    // ==================== ОБНОВЛЕНИЕ ====================

    public void refreshData() {
        loadAllGateValves();
        updateNextListNumber();
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