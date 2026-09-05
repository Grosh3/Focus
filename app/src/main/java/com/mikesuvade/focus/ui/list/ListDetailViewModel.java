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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Collections;
import com.mikesuvade.focus.MyApp;

public class ListDetailViewModel extends ViewModel {

    private final IRepository repository;
    private boolean useUserDb = true;

    private final MutableLiveData<List<ValveItem>> leftList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ValveItem>> rightList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> listName = new MutableLiveData<>("Новый список");
    private final MutableLiveData<String> sessionIdLive = new MutableLiveData<>();

    public ListDetailViewModel(IRepository repository) {
        this.repository = repository;
    }

    // ==========================================
    // ГЕТТЕРЫ
    // ==========================================

    public LiveData<List<ValveItem>> getLeftList() {
        return leftList;
    }

    public LiveData<List<ValveItem>> getRightList() {
        return rightList;
    }

    public LiveData<String> getListName() {
        return listName;
    }

    public LiveData<String> getSessionIdLive() {
        return sessionIdLive;
    }

    public String getSessionId() {
        return sessionIdLive.getValue();
    }

    public void setSessionId(String sessionId) {
        sessionIdLive.setValue(sessionId);
    }

    public void setUseUserDb(boolean useUserDb) {
        this.useUserDb = useUserDb;
        Log.d("LIST_DEBUG", "setUseUserDb: " + useUserDb);
    }

    public boolean isUseUserDb() {
        return useUserDb;
    }

    // ==========================================
    // ЗАГРУЗКА ДАННЫХ
    // ==========================================

    public void setListName(String name) {
        listName.setValue(name);
    }

    public void loadFromGateValves(List<GateValve> valves) {
        Log.d("SESSY", "=== loadFromGateValves START ===");
        Log.d("SESSY", "valves size = " + (valves != null ? valves.size() : 0));

        if (valves == null || valves.isEmpty()) {
            Log.d("SESSY", "valves is empty, creating empty session");
            createEmptySession();
            return;
        }

        List<ValveItem> items = new ArrayList<>();
        for (GateValve valve : valves) {
            ValveItem item = new ValveItem();
            item.setGateValveId(valve.getId());
            item.setIsAssembled(1);
            item.setMotorDisabled(0);
            item.setBoxRemoved(0);
            item.setIsChecked(0);
            items.add(item);
            Log.d("SESSY", "  added gateValveId=" + valve.getId() + ", name=" + valve.getName());
        }

        // 🔥 СОРТИРУЕМ
        items = sortItems(items);

        sessionIdLive.setValue(null);
        leftList.setValue(items);
        rightList.setValue(new ArrayList<>());
        Log.d("SESSY", "=== loadFromGateValves END ===");
    }

    public void createEmptySession() {
        Log.d("SESSY", "createEmptySession");
        leftList.setValue(new ArrayList<>());
        rightList.setValue(new ArrayList<>());
        sessionIdLive.setValue(null);
    }

    public void loadSession(String sessionId) {
        Log.d("SESSY", "=== ListDetailViewModel.loadSession ===");
        Log.d("SESSY", "sessionId = " + sessionId);
        Log.d("SESSY", "useUserDb = " + useUserDb);

        new Thread(() -> {
            try {
                List<ValveItem> items;
                if (useUserDb) {
                    Log.d("SESSY", "Loading from USER DB");
                    // 🔥 ИСПРАВЛЕНО: getUserSessionItemsBySession
                    items = repository.getUserSessionItemsBySession(sessionId);
                } else {
                    Log.d("SESSY", "Loading from MAIN DB");
                    items = repository.getUserSessionItemsBySession(sessionId);
                }

                Log.d("SESSY", "Items loaded: " + (items != null ? items.size() : 0));

                if (items != null) {
                    for (int i = 0; i < items.size(); i++) {
                        ValveItem item = items.get(i);
                        Log.d("SESSY", "  ITEM[" + i + "] gateValveId=" + item.getGateValveId() +
                                ", assembled=" + item.getIsAssembled());
                    }
                }

                processItems(items);
            } catch (Exception e) {
                Log.e("SESSY", "Error loading session", e);
            }
        }).start();
    }

    private void processItems(List<ValveItem> items) {
        Log.d("SESSY", "=== processItems ===");
        Log.d("SESSY", "items size = " + (items != null ? items.size() : 0));

        if (items == null) {
            items = new ArrayList<>();
        }

        List<ValveItem> left = new ArrayList<>();
        List<ValveItem> right = new ArrayList<>();

        for (ValveItem item : items) {
            if (item.getIsAssembled() == 1) {
                left.add(item);
                Log.d("SESSY", "  LEFT: gateValveId=" + item.getGateValveId());
            } else {
                right.add(item);
                Log.d("SESSY", "  RIGHT: gateValveId=" + item.getGateValveId());
            }
        }

        // 🔥 СОРТИРУЕМ
        left = sortItems(left);
        right = sortItems(right);

        Log.d("SESSY", "left size = " + left.size() + ", right size = " + right.size());

        leftList.postValue(left);
        rightList.postValue(right);
    }

    // ==========================================
    // ДЕЙСТВИЯ СО СПИСКОМ
    // ==========================================

    public void assembleAll() {
        Log.d("LIST_DEBUG", "assembleAll");
        List<ValveItem> left = leftList.getValue();
        List<ValveItem> right = rightList.getValue();

        if (right == null) right = new ArrayList<>();
        if (left == null) left = new ArrayList<>();

        for (ValveItem item : right) {
            item.setIsAssembled(1);
        }
        left.addAll(right);
        right.clear();

        // 🔥 СОРТИРУЕМ
        left = sortItems(left);

        leftList.setValue(left);
        rightList.setValue(right);
    }

    public void disassembleAll() {
        Log.d("LIST_DEBUG", "disassembleAll");
        List<ValveItem> left = leftList.getValue();
        List<ValveItem> right = rightList.getValue();

        if (left == null) left = new ArrayList<>();
        if (right == null) right = new ArrayList<>();

        for (ValveItem item : left) {
            item.setIsAssembled(0);
        }
        right.addAll(left);
        left.clear();

        // 🔥 СОРТИРУЕМ
        right = sortItems(right);

        leftList.setValue(left);
        rightList.setValue(right);
    }

    public void moveItem(ValveItem item, boolean toLeft) {
        Log.d("LIST_DEBUG", "=== moveItem START ===");
        Log.d("LIST_DEBUG", "toLeft=" + toLeft + ", gateValveId=" + item.getGateValveId());

        List<ValveItem> left = leftList.getValue();
        List<ValveItem> right = rightList.getValue();

        if (left == null) left = new ArrayList<>();
        if (right == null) right = new ArrayList<>();

        ValveItem foundItem = null;
        int foundIndex = -1;

        if (toLeft) {
            for (int i = 0; i < right.size(); i++) {
                if (right.get(i).getGateValveId() == item.getGateValveId()) {
                    foundItem = right.get(i);
                    foundIndex = i;
                    break;
                }
            }
            if (foundItem != null) {
                right.remove(foundIndex);
                foundItem.setIsAssembled(1);
                foundItem.setIsChecked(0);
                foundItem.setCheckedAt(null);
                foundItem.setMotorDisabled(0);
                foundItem.setBoxRemoved(0);

                boolean existsInLeft = false;
                for (ValveItem existing : left) {
                    if (existing.getGateValveId() == foundItem.getGateValveId()) {
                        existsInLeft = true;
                        break;
                    }
                }
                if (!existsInLeft) {
                    left.add(foundItem);
                    Log.d("LIST_DEBUG", "Moved RIGHT -> LEFT: gateValveId=" + foundItem.getGateValveId());
                }
            } else {
                Log.e("LIST_DEBUG", "Item not found in RIGHT list!");
            }
        } else {
            for (int i = 0; i < left.size(); i++) {
                if (left.get(i).getGateValveId() == item.getGateValveId()) {
                    foundItem = left.get(i);
                    foundIndex = i;
                    break;
                }
            }
            if (foundItem != null) {
                left.remove(foundIndex);
                foundItem.setIsAssembled(0);
                foundItem.setIsChecked(0);
                foundItem.setCheckedAt(null);
                foundItem.setMotorDisabled(0);
                foundItem.setBoxRemoved(0);

                boolean existsInRight = false;
                for (ValveItem existing : right) {
                    if (existing.getGateValveId() == foundItem.getGateValveId()) {
                        existsInRight = true;
                        break;
                    }
                }
                if (!existsInRight) {
                    right.add(foundItem);
                    Log.d("LIST_DEBUG", "Moved LEFT -> RIGHT: gateValveId=" + foundItem.getGateValveId());
                }
            } else {
                Log.e("LIST_DEBUG", "Item not found in LEFT list!");
            }
        }

        // 🔥 СОРТИРУЕМ ОБА СПИСКА ПОСЛЕ ПЕРЕМЕЩЕНИЯ
        left = sortItems(left);
        right = sortItems(right);

        leftList.setValue(left);
        rightList.setValue(right);
        Log.d("LIST_DEBUG", "=== moveItem END ===");
    }

    public void toggleMotor(ValveItem item) {
        Log.d("LIST_DEBUG", "toggleMotor: gateValveId=" + item.getGateValveId());
        ValveItem foundItem = findItemInLists(item.getGateValveId());
        if (foundItem != null) {
            foundItem.setMotorDisabled(foundItem.getMotorDisabled() == 1 ? 0 : 1);
            refreshListsWithSort();
        }
    }

    public void toggleBox(ValveItem item) {
        Log.d("LIST_DEBUG", "toggleBox: gateValveId=" + item.getGateValveId());
        ValveItem foundItem = findItemInLists(item.getGateValveId());
        if (foundItem != null) {
            foundItem.setBoxRemoved(foundItem.getBoxRemoved() == 1 ? 0 : 1);
            refreshListsWithSort();  // ← ИСПРАВЛЕНО: refreshListsWithSort вместо refreshLists
        }
    }

    public void toggleChecked(ValveItem item) {
        Log.d("LIST_DEBUG", "toggleChecked: gateValveId=" + item.getGateValveId());
        ValveItem foundItem = findItemInLists(item.getGateValveId());
        if (foundItem != null) {
            foundItem.setIsChecked(foundItem.getIsChecked() == 1 ? 0 : 1);
            if (foundItem.getIsChecked() == 1) {
                foundItem.setCheckedAt(getCurrentDateTime());
            } else {
                foundItem.setCheckedAt(null);
            }
            refreshListsWithSort();  // ← ИСПРАВЛЕНО
        }
    }
    private void refreshListsWithSort() {
        List<ValveItem> left = leftList.getValue();
        List<ValveItem> right = rightList.getValue();

        if (left != null) {
            left = sortItems(left);
            leftList.setValue(left);
        }
        if (right != null) {
            right = sortItems(right);
            rightList.setValue(right);
        }
    }
    public void removeItem(ValveItem item) {
        Log.d("LIST_DEBUG", "removeItem: gateValveId=" + item.getGateValveId());
        ValveItem foundItem = findItemInLists(item.getGateValveId());
        if (foundItem != null) {
            List<ValveItem> left = leftList.getValue();
            List<ValveItem> right = rightList.getValue();
            if (left != null) left.remove(foundItem);
            if (right != null) right.remove(foundItem);
            leftList.setValue(left);
            rightList.setValue(right);
        }
    }

    private ValveItem findItemInLists(int gateValveId) {
        List<ValveItem> left = leftList.getValue();
        List<ValveItem> right = rightList.getValue();

        if (left != null) {
            for (ValveItem item : left) {
                if (item.getGateValveId() == gateValveId) {
                    return item;
                }
            }
        }
        if (right != null) {
            for (ValveItem item : right) {
                if (item.getGateValveId() == gateValveId) {
                    return item;
                }
            }
        }
        return null;
    }

    private void refreshLists() {
        List<ValveItem> left = leftList.getValue();
        List<ValveItem> right = rightList.getValue();

        if (left != null) {
            leftList.setValue(new ArrayList<>(left));
        }
        if (right != null) {
            rightList.setValue(new ArrayList<>(right));
        }
    }

    // ==========================================
    // СОХРАНЕНИЕ
    // ==========================================

    public void saveSession(String name) {
        Log.d("SESSY", "=== saveSession START ===");
        Log.d("SESSY", "name = " + name);
        Log.d("SESSY", "useUserDb = " + useUserDb);

        new Thread(() -> {
            try {
                String sessionId = sessionIdLive.getValue();
                if (sessionId == null) {
                    sessionId = "SESSION_" + System.currentTimeMillis();
                    sessionIdLive.postValue(sessionId);
                    Log.d("SESSY", "Generated new sessionId: " + sessionId);
                }

                // 🔥 ПРОВЕРЯЕМ, ЕСТЬ ЛИ УЖЕ ТАКАЯ СЕССИЯ
                ValveWorkSession existing = repository.getUserWorkSessionById(sessionId);

                ValveWorkSession session = new ValveWorkSession();
                session.setSessionId(sessionId);
                session.setEquipmentDescription(name);
                session.setSaveDate(getCurrentDateTime());
                session.setCreatedAt(getCurrentDateTime());
                session.setIsSynced(0);

                if (useUserDb) {
                    Log.d("SESSY", "Saving to USER DB");
                    if (existing != null) {
                        Log.d("SESSY", "Updating existing session");
                        repository.updateUserWorkSession(session);
                    } else {
                        Log.d("SESSY", "Inserting new session");
                        // 🔥 ИСПРАВЛЕНО: insertUserWorkSession
                        repository.insertUserWorkSession(session);
                    }

                    saveItemsToUserDb(sessionId);
                } else {
                    Log.d("SESSY", "Saving to MAIN DB");
                    repository.insertUserWorkSession(session);
                    saveItemsToUserDb(sessionId);
                }

                Log.d("SESSY", "Session saved successfully");
            } catch (Exception e) {
                Log.e("SESSY", "Error saving session", e);
            }
        }).start();
    }

    public void updateSession(String sessionId) {
        Log.d("DATEFRESH", "=== updateSession START ===");
        Log.d("DATEFRESH", "sessionId: " + sessionId);
        Log.d("DATEFRESH", "useUserDb: " + useUserDb);

        String newDate = getCurrentDateTime();
        Log.d("DATEFRESH", "newDate: " + newDate);

        new Thread(() -> {
            try {
                if (useUserDb) {
                    Log.d("DATEFRESH", "Updating date in USER DB...");
                    // 🔥 ИСПРАВЛЕНО: updateUserWorkSessionDate
                    int result = repository.updateUserWorkSessionDate(sessionId, newDate);
                    Log.d("DATEFRESH", "updateUserWorkSessionDate result: " + result);

                    Log.d("DATEFRESH", "Saving items to USER DB...");
                    saveItemsToUserDb(sessionId);
                    Log.d("DATEFRESH", "Items saved to USER DB");
                }
                Log.d("DATEFRESH", "=== updateSession END (success) ===");
            } catch (Exception e) {
                Log.e("DATEFRESH", "Error updating session", e);
            }
        }).start();
    }

    public void getListCount(OnListCountCallback callback) {
        new Thread(() -> {
            try {
                int count = repository.getAllUserWorkSessions().size();
                callback.onResult(count);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onResult(0);
            }
        }).start();
    }

    public interface OnListCountCallback {
        void onResult(int count);
    }

    // ==========================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ==========================================

    private void saveItemsToUserDb(String sessionId) {
        Log.d("SESSY", "=== saveItemsToUserDb ===");
        Log.d("SESSY", "sessionId = " + sessionId);

        List<ValveItem> leftItems = leftList.getValue();
        List<ValveItem> rightItems = rightList.getValue();
        List<ValveItem> allItems = new ArrayList<>();
        if (leftItems != null) allItems.addAll(leftItems);
        if (rightItems != null) allItems.addAll(rightItems);

        Map<Integer, ValveItem> uniqueItems = new HashMap<>();
        for (ValveItem item : allItems) {
            uniqueItems.put(item.getGateValveId(), item);
        }
        List<ValveItem> uniqueList = new ArrayList<>(uniqueItems.values());

        Log.d("SESSY", "Total unique items to save: " + uniqueList.size());

        for (ValveItem item : uniqueList) {
            item.setParentSessionId(sessionId);
            if (item.getItemId() > 0) {
                Log.d("SESSY", "Updating item: " + item.getItemId());
                // 🔥 ИСПРАВЛЕНО: updateUserSessionItem
                repository.updateUserSessionItem(item);
            } else {
                Log.d("SESSY", "Inserting new item");
                // 🔥 ИСПРАВЛЕНО: insertUserSessionItem
                repository.insertUserSessionItem(item);
            }
        }
    }
    // ==========================================
// 🔧 СОРТИРОВКА
// ==========================================

    /**
     * Сортирует список ValveItem по ИСУ (isy)
     * Отмеченные (isChecked = 1) всегда внизу
     * Внутри каждой группы сортировка по ИСУ
     */
    private List<ValveItem> sortItems(List<ValveItem> items) {
        if (items == null || items.isEmpty()) {
            return items;
        }

        List<ValveItem> unchecked = new ArrayList<>();
        List<ValveItem> checked = new ArrayList<>();

        for (ValveItem item : items) {
            if (item.getIsChecked() == 1) {
                checked.add(item);
            } else {
                unchecked.add(item);
            }
        }

        // 🔥 СОРТИРУЕМ ПО ИСУ (isy) - используем объединенный источник
        Collections.sort(unchecked, (a, b) -> {
            String isyA = getIsyForItemMerged(a);
            String isyB = getIsyForItemMerged(b);
            return compareIsy(isyA, isyB);
        });

        Collections.sort(checked, (a, b) -> {
            String isyA = getIsyForItemMerged(a);
            String isyB = getIsyForItemMerged(b);
            return compareIsy(isyA, isyB);
        });

        // Отмеченные внизу
        List<ValveItem> result = new ArrayList<>();
        result.addAll(unchecked);
        result.addAll(checked);
        return result;
    }

    /**
     * Получить ИСУ для элемента из объединенного источника
     */
    private String getIsyForItemMerged(ValveItem item) {
        GateValve valve = getGateValveMerged(item.getGateValveId());
        if (valve != null && valve.getIsy() != null && !valve.getIsy().isEmpty()) {
            return valve.getIsy();
        }
        return "ZZZZ";
    }

    /**
     * Получить ИСУ для элемента
     */
    /**
     * Получить ИСУ для элемента из объединенного источника
     */
    private String getIsyForItem(ValveItem item) {
        GateValve valve = getGateValveMerged(item.getGateValveId());
        if (valve != null && valve.getIsy() != null && !valve.getIsy().isEmpty()) {
            return valve.getIsy();
        }
        return "ZZZZ";
    }
    /**
     * Сравнение ИСУ как чисел
     * 134, 232, 435, 836
     */
    private int compareIsy(String isyA, String isyB) {
        // Если оба null или пустые — равны
        if ((isyA == null || isyA.isEmpty()) && (isyB == null || isyB.isEmpty())) return 0;
        if (isyA == null || isyA.isEmpty()) return 1;
        if (isyB == null || isyB.isEmpty()) return -1;

        try {
            // Пытаемся сравнить как числа
            int numA = Integer.parseInt(isyA.trim());
            int numB = Integer.parseInt(isyB.trim());
            return Integer.compare(numA, numB);
        } catch (NumberFormatException e) {
            // Если не числа — сравниваем как строки
            return isyA.compareToIgnoreCase(isyB);
        }
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
    private GateValve getGateValveMerged(int gateValveId) {
        // 🔥 1. Сначала пробуем найти в БД2 (пользовательские) по ID
        GateValve userValve = repository.getUserGateValveById(gateValveId);
        if (userValve != null && userValve.getIsDeleted() != 1) {
            return userValve;
        }

        // 🔥 2. Если не нашли по ID, пробуем найти по original_id
        List<GateValve> allUserValves = repository.getAllUserGateValves();
        for (GateValve uv : allUserValves) {
            if (uv.getOriginalId() == gateValveId && uv.getIsDeleted() != 1) {
                return uv;
            }
        }

        // 🔥 3. Если не нашли в БД2, ищем в БД1
        return repository.getGateValveById(gateValveId);
    }

}