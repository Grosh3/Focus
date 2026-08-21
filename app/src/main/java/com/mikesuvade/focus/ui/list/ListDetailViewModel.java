package com.mikesuvade.focus.ui.list;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.ValveItem;
import com.mikesuvade.focus.domain.models.ValveWorkSession;
import com.mikesuvade.focus.domain.repository.IRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ListDetailViewModel extends ViewModel {

    private static final String TAG = "ListDetailViewModel";
    private final IRepository repository;

    private final MutableLiveData<List<ValveItem>> leftList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ValveItem>> rightList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> totalCount = new MutableLiveData<>(0);
    private final MutableLiveData<String> listName = new MutableLiveData<>(null);

    private String sessionId;

    public ListDetailViewModel(IRepository repository) {
        this.repository = repository;
        Log.d("MYTITLE", "=== ListDetailViewModel CONSTRUCTOR ===");
        Log.d("MYTITLE", "listName initial value: " + listName.getValue());
    }

    // ==========================================
    // ГЕТТЕРЫ
    // ==========================================
    public LiveData<List<ValveItem>> getLeftList() { return leftList; }
    public LiveData<List<ValveItem>> getRightList() { return rightList; }
    public LiveData<Integer> getTotalCount() { return totalCount; }
    public LiveData<String> getListName() { return listName; }

    public void setListName(String name) {
        Log.d("MYTITLE", "=== setListName CALLED ===");
        Log.d("MYTITLE", "Setting listName to: " + name);
        listName.setValue(name);
    }

    public String getSessionId() { return sessionId; }

    // ==========================================
    // СОЗДАНИЕ ПУСТОЙ СЕССИИ
    // ==========================================
    public void createEmptySession() {
        Log.d("MYTITLE", "=== createEmptySession CALLED ===");
        leftList.setValue(new ArrayList<>());
        rightList.setValue(new ArrayList<>());
        updateCount();
        Log.d("MYTITLE", "Setting listName to: Новый список");
        listName.setValue("Новый список");
    }

    // ==========================================
    // ЗАГРУЗКА ИЗ GateValve (НОВЫЙ СПИСОК)
    // ==========================================
    public void loadFromGateValves(List<GateValve> valves) {
        Log.d("MYTITLE", "=== loadFromGateValves CALLED ===");
        Log.d("MYTITLE", "valves size: " + (valves != null ? valves.size() : 0));

        if (valves == null || valves.isEmpty()) {
            leftList.setValue(new ArrayList<>());
            rightList.setValue(new ArrayList<>());
            updateCount();
            Log.d("MYTITLE", "Setting listName to: Новый список (empty valves)");
            listName.setValue("Новый список");
            return;
        }

        List<ValveItem> leftItems = new ArrayList<>();
        for (GateValve valve : valves) {
            ValveItem item = new ValveItem();
            item.setGateValveId(valve.getId());
            item.setIsAssembled(1);
            item.setMotorDisabled(0);
            item.setBoxRemoved(0);
            item.setIsChecked(0);
            item.setCheckedAt(null);
            leftItems.add(item);
        }

        leftList.setValue(leftItems);
        rightList.setValue(new ArrayList<>());
        updateCount();

        Log.d("MYTITLE", "Setting listName to: Новый список (after loading)");
        listName.setValue("Новый список");
    }

    // ==========================================
    // ЗАГРУЗКА СЕССИИ ИЗ БД
    // ==========================================
    public void loadSession(String sessionId) {
        Log.d("MYTITLE", "=== loadSession CALLED ===");
        Log.d("MYTITLE", "sessionId: " + sessionId);

        // ✅ УСТАНАВЛИВАЕМ ВРЕМЕННОЕ ИМЯ
        listName.postValue("Загрузка...");

        new Thread(() -> {
            try {
                // 1. Загружаем элементы
                List<ValveItem> items = repository.getValveItemsBySession(sessionId);

                List<ValveItem> leftItems = new ArrayList<>();
                List<ValveItem> rightItems = new ArrayList<>();

                for (ValveItem item : items) {
                    if (item.getIsAssembled() == 1) {
                        leftItems.add(item);
                    } else {
                        rightItems.add(item);
                    }
                }

                leftList.postValue(leftItems);
                rightList.postValue(rightItems);
                updateCount();

                // ✅ 2. Загружаем сессию по ID (ОДИН ЗАПРОС!)
                ValveWorkSession session = repository.getWorkSessionById(sessionId);

                String finalName;
                if (session != null && session.getEquipmentDescription() != null
                        && !session.getEquipmentDescription().isEmpty()) {
                    finalName = session.getEquipmentDescription();
                    Log.d("MYTITLE", "Setting final list name: " + finalName);
                } else {
                    finalName = "Новый список";
                    Log.d("MYTITLE", "Setting final list name: " + finalName + " (default)");
                }

                // ✅ 3. Устанавливаем имя (гарантированно!)
                listName.postValue(finalName);

            } catch (Exception e) {
                Log.e("MYTITLE", "Error in loadSession", e);
                // ✅ ВСЕГДА УСТАНАВЛИВАЕМ ИМЯ, ДАЖЕ ПРИ ОШИБКЕ!
                listName.postValue("Новый список");
            }
        }).start();
    }
    // ==========================================
    // СОХРАНЕНИЕ В БД
    // ==========================================
    public void saveSession(String name) {
        if (name == null || name.trim().isEmpty()) {
            name = "Список " + new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
        }

        Log.d("MYTITLE", "=== saveSession CALLED ===");
        Log.d("MYTITLE", "Saving name: " + name);

        listName.setValue(name);

        sessionId = "SESSION_" + UUID.randomUUID().toString();

        ValveWorkSession session = new ValveWorkSession();
        session.setSessionId(sessionId);
        session.setSaveDate(getCurrentDateTime());
        session.setEquipmentDescription(name);
        repository.insertWorkSession(session);

        List<ValveItem> allItems = new ArrayList<>();
        allItems.addAll(leftList.getValue());
        allItems.addAll(rightList.getValue());

        for (ValveItem item : allItems) {
            item.setParentSessionId(sessionId);
            item.setOperationTimestamp(getCurrentDateTime());
            repository.insertValveItem(item);
        }

        Log.d(TAG, "saveSession: saved " + allItems.size() + " items with name: " + name);
    }

    // ==========================================
    // ОБНОВЛЕНИЕ СЕССИИ (ТОЛЬКО ДАТА, НЕ ИМЯ)
    // ==========================================
    public void updateSession(String sessionId) {
        Log.d("MYTITLE", "=== updateSession CALLED ===");
        Log.d("MYTITLE", "sessionId: " + sessionId);
        Log.d("MYTITLE", "Current listName: " + listName.getValue());

        new Thread(() -> {
            try {
                List<ValveItem> oldItems = repository.getValveItemsBySession(sessionId);
                for (ValveItem item : oldItems) {
                    repository.deleteValveItem(item.getItemId());
                }

                List<ValveItem> allItems = new ArrayList<>();
                allItems.addAll(leftList.getValue());
                allItems.addAll(rightList.getValue());

                for (ValveItem item : allItems) {
                    item.setParentSessionId(sessionId);
                    item.setOperationTimestamp(getCurrentDateTime());
                    repository.insertValveItem(item);
                }

                repository.updateWorkSessionDate(sessionId, getCurrentDateTime());

                Log.d(TAG, "updateSession: updated items and date for session: " + sessionId);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ==========================================
    // ОПЕРАЦИИ СО СПИСКАМИ — НЕ МЕНЯЮТ ИМЯ!
    // ==========================================

    public void moveItem(ValveItem item, boolean toRight) {
        int gateValveId = item.getGateValveId();
        List<ValveItem> left = new ArrayList<>(leftList.getValue());
        List<ValveItem> right = new ArrayList<>(rightList.getValue());

        int leftIndex = findItemIndexById(left, gateValveId);
        if (leftIndex != -1) {
            ValveItem found = left.get(leftIndex);
            left.remove(leftIndex);
            found.setIsAssembled(0);
            found.setMotorDisabled(0);
            found.setBoxRemoved(0);
            found.setIsChecked(0);
            found.setCheckedAt(null);
            right.add(found);
            leftList.setValue(left);
            rightList.setValue(right);
            updateCount();
            return;
        }

        int rightIndex = findItemIndexById(right, gateValveId);
        if (rightIndex != -1) {
            ValveItem found = right.get(rightIndex);
            right.remove(rightIndex);
            found.setIsAssembled(1);
            found.setMotorDisabled(0);
            found.setBoxRemoved(0);
            found.setIsChecked(0);
            found.setCheckedAt(null);
            left.add(found);
            leftList.setValue(left);
            rightList.setValue(right);
            updateCount();
        }
    }

    public void toggleMotor(ValveItem item) {
        int gateValveId = item.getGateValveId();
        List<ValveItem> left = new ArrayList<>(leftList.getValue());
        List<ValveItem> right = new ArrayList<>(rightList.getValue());

        int leftIndex = findItemIndexById(left, gateValveId);
        if (leftIndex != -1) {
            ValveItem found = left.get(leftIndex);
            found.setMotorDisabled(found.getMotorDisabled() == 1 ? 0 : 1);
            leftList.setValue(left);
            return;
        }

        int rightIndex = findItemIndexById(right, gateValveId);
        if (rightIndex != -1) {
            ValveItem found = right.get(rightIndex);
            found.setMotorDisabled(found.getMotorDisabled() == 1 ? 0 : 1);
            rightList.setValue(right);
        }
    }

    public void toggleBox(ValveItem item) {
        int gateValveId = item.getGateValveId();
        List<ValveItem> left = new ArrayList<>(leftList.getValue());
        List<ValveItem> right = new ArrayList<>(rightList.getValue());

        int leftIndex = findItemIndexById(left, gateValveId);
        if (leftIndex != -1) {
            ValveItem found = left.get(leftIndex);
            found.setBoxRemoved(found.getBoxRemoved() == 1 ? 0 : 1);
            leftList.setValue(left);
            return;
        }

        int rightIndex = findItemIndexById(right, gateValveId);
        if (rightIndex != -1) {
            ValveItem found = right.get(rightIndex);
            found.setBoxRemoved(found.getBoxRemoved() == 1 ? 0 : 1);
            rightList.setValue(right);
        }
    }

    public void toggleChecked(ValveItem item) {
        int gateValveId = item.getGateValveId();
        List<ValveItem> left = new ArrayList<>(leftList.getValue());
        List<ValveItem> right = new ArrayList<>(rightList.getValue());

        int leftIndex = findItemIndexById(left, gateValveId);
        if (leftIndex != -1) {
            ValveItem found = left.get(leftIndex);
            found.setIsChecked(found.getIsChecked() == 1 ? 0 : 1);
            found.setCheckedAt(found.getIsChecked() == 1 ? getCurrentDateTime() : null);
            left.remove(leftIndex);
            if (found.getIsChecked() == 1) {
                left.add(found);
            } else {
                left.add(0, found);
            }
            leftList.setValue(left);
            updateCount();
            return;
        }

        int rightIndex = findItemIndexById(right, gateValveId);
        if (rightIndex != -1) {
            ValveItem found = right.get(rightIndex);
            found.setIsChecked(found.getIsChecked() == 1 ? 0 : 1);
            found.setCheckedAt(found.getIsChecked() == 1 ? getCurrentDateTime() : null);
            right.remove(rightIndex);
            if (found.getIsChecked() == 1) {
                right.add(found);
            } else {
                right.add(0, found);
            }
            rightList.setValue(right);
            updateCount();
        }
    }

    public void assembleAll() {
        List<ValveItem> left = new ArrayList<>(leftList.getValue());
        List<ValveItem> right = new ArrayList<>(rightList.getValue());

        for (ValveItem item : right) {
            item.setIsAssembled(1);
            item.setMotorDisabled(0);
            item.setBoxRemoved(0);
            item.setIsChecked(0);
            item.setCheckedAt(null);
            left.add(item);
        }
        right.clear();

        leftList.setValue(left);
        rightList.setValue(right);
        updateCount();
    }

    public void disassembleAll() {
        List<ValveItem> left = new ArrayList<>(leftList.getValue());
        List<ValveItem> right = new ArrayList<>(rightList.getValue());

        for (ValveItem item : left) {
            item.setIsAssembled(0);
            item.setMotorDisabled(0);
            item.setBoxRemoved(0);
            item.setIsChecked(0);
            item.setCheckedAt(null);
            right.add(item);
        }
        left.clear();

        leftList.setValue(left);
        rightList.setValue(right);
        updateCount();
    }

    public void removeItem(ValveItem item) {
        int gateValveId = item.getGateValveId();
        List<ValveItem> left = new ArrayList<>(leftList.getValue());
        List<ValveItem> right = new ArrayList<>(rightList.getValue());

        int leftIndex = findItemIndexById(left, gateValveId);
        if (leftIndex != -1) {
            left.remove(leftIndex);
            leftList.setValue(left);
            updateCount();
            return;
        }

        int rightIndex = findItemIndexById(right, gateValveId);
        if (rightIndex != -1) {
            right.remove(rightIndex);
            rightList.setValue(right);
            updateCount();
        }
    }

    // ==========================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ==========================================

    private int findItemIndexById(List<ValveItem> list, int gateValveId) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getGateValveId() == gateValveId) {
                return i;
            }
        }
        return -1;
    }

    private void updateCount() {
        int leftSize = leftList.getValue() != null ? leftList.getValue().size() : 0;
        int rightSize = rightList.getValue() != null ? rightList.getValue().size() : 0;

        // ❌ БЫЛО (вызывает ошибку в фоновом потоке):
        // totalCount.setValue(leftSize + rightSize);

        // ✅ СТАЛО (работает в любом потоке):
        totalCount.postValue(leftSize + rightSize);
    }
    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public void getListCount(OnListCountCallback callback) {
        new Thread(() -> {
            try {
                int count = repository.getAllWorkSessions().size();
                if (callback != null) {
                    callback.onResult(count);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void loadFromValveItems(List<ValveItem> items) {
        if (items == null || items.isEmpty()) {
            leftList.setValue(new ArrayList<>());
            rightList.setValue(new ArrayList<>());
            updateCount();
            return;
        }
        leftList.setValue(items);
        rightList.setValue(new ArrayList<>());
        updateCount();
    }

    public interface OnListCountCallback {
        void onResult(int count);
    }
}