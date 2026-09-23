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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ListDetailViewModel extends ViewModel {

    private final IRepository repository;
    private boolean useUserDb = true;

    private final MutableLiveData<List<ValveItem>> leftList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ValveItem>> rightList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> listName = new MutableLiveData<>("Новый список");
    private final MutableLiveData<String> sessionIdLive = new MutableLiveData<>();
    private String currentSessionName = null;

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

    }

    public boolean isUseUserDb() {
        return useUserDb;
    }

    // ==========================================
    // ЗАГРУЗКА ДАННЫХ
    // ==========================================

    public void setListName(String name) {
        if (name != null && !name.isEmpty()) {
            listName.setValue(name);
        }
    }

    public void loadFromGateValves(List<GateValve> valves) {
        if (valves == null || valves.isEmpty()) {
            createEmptySession();
            return;
        }

        listName.setValue("Новый список");

        List<ValveItem> items = new ArrayList<>();
        for (GateValve valve : valves) {
            ValveItem item = new ValveItem();
            item.setGateValveId(valve.getId());

            String name = valve.getName();
            String isy = valve.getIsy();

            item.setGateValveName(name != null ? name : "");
            item.setGateValveIsy(isy != null ? isy : "");
            item.setGateValveKks(valve.getKks() != null ? valve.getKks() : "");
            item.setGateValvePowerCabinet(valve.getPowerCabinet() != null ? valve.getPowerCabinet() : "");
            item.setGateValveLocationDescription(valve.getLocationDescription() != null ? valve.getLocationDescription() : "");
            item.setGateValveOnPlace(valve.getOnPlace() != null ? valve.getOnPlace() : "");
            item.setGateValveFullName(valve.getFullName() != null ? valve.getFullName() : "");

            item.setIsAssembled(1);
            item.setMotorDisabled(0);
            item.setBoxRemoved(0);
            item.setIsChecked(0);
            items.add(item);
        }

        items = sortItems(items);
        leftList.setValue(items);
        rightList.setValue(new ArrayList<>());
    }
    public void createEmptySession() {

        leftList.setValue(new ArrayList<>());
        rightList.setValue(new ArrayList<>());
        sessionIdLive.setValue(null);
    }

    public void loadSession(String sessionId) {
        new Thread(() -> {
            try {
                ValveWorkSession session = repository.getUserWorkSessionById(sessionId);
                if (session != null) {
                    String name = session.getEquipmentDescription();
                    if (name != null && !name.isEmpty()) {
                        currentSessionName = name;
                        listName.postValue(name);
                    }
                }

                List<ValveItem> items = repository.getUserSessionItemsBySession(sessionId);

                processItems(items);
            } catch (Exception e) {
                Log.e("SESSY", "Error loading session", e);
            }
        }).start();
    }
    private void processItems(List<ValveItem> items) {


        if (items == null) {
            items = new ArrayList<>();
        }

        List<ValveItem> left = new ArrayList<>();
        List<ValveItem> right = new ArrayList<>();

        for (ValveItem item : items) {
            if (item.getIsAssembled() == 1) {
                left.add(item);

            } else {
                right.add(item);

            }
        }

        // 🔥 СОРТИРУЕМ
        left = sortItems(left);
        right = sortItems(right);



        leftList.postValue(left);
        rightList.postValue(right);
    }

    // ==========================================
    // ДЕЙСТВИЯ СО СПИСКОМ
    // ==========================================

    public void assembleAll() {

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

    }

    public void toggleMotor(ValveItem item) {

        ValveItem foundItem = findItemInLists(item.getGateValveId());
        if (foundItem != null) {
            // 🔥 ПЕРЕКЛЮЧАЕМ СОСТОЯНИЕ
            foundItem.setMotorDisabled(foundItem.getMotorDisabled() == 1 ? 0 : 1);
            refreshListsWithSort();
        }
    }

    public void toggleBox(ValveItem item) {

        ValveItem foundItem = findItemInLists(item.getGateValveId());
        if (foundItem != null) {
            // 🔥 ПЕРЕКЛЮЧАЕМ СОСТОЯНИЕ
            foundItem.setBoxRemoved(foundItem.getBoxRemoved() == 1 ? 0 : 1);
            refreshListsWithSort();
        }
    }

    public void toggleChecked(ValveItem item) {

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


        new Thread(() -> {
            try {
                String sessionId = sessionIdLive.getValue();
                if (sessionId == null) {
                    sessionId = "SESSION_" + System.currentTimeMillis();
                    sessionIdLive.postValue(sessionId);

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

                    if (existing != null) {

                        repository.updateUserWorkSession(session);
                    } else {

                        // 🔥 ИСПРАВЛЕНО: insertUserWorkSession
                        repository.insertUserWorkSession(session);
                    }

                    saveItemsToUserDb(sessionId);
                } else {

                    repository.insertUserWorkSession(session);
                    saveItemsToUserDb(sessionId);
                }


            } catch (Exception e) {
                Log.e("SESSY", "Error saving session", e);
            }
        }).start();
    }

    public void updateSession(String sessionId) {


        String newDate = getCurrentDateTime();


        new Thread(() -> {
            try {
                if (useUserDb) {

                    // 🔥 ИСПРАВЛЕНО: updateUserWorkSessionDate
                    int result = repository.updateUserWorkSessionDate(sessionId, newDate);

                    saveItemsToUserDb(sessionId);

                }

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



        for (ValveItem item : uniqueList) {
            item.setParentSessionId(sessionId);
            if (item.getItemId() > 0) {

                // 🔥 ИСПРАВЛЕНО: updateUserSessionItem
                repository.updateUserSessionItem(item);
            } else {

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
        // 🔥 1. СНАЧАЛА БЕРЕМ ИЗ ValveItem (это данные, сохраненные в БД2)
        if (item.getGateValveIsy() != null && !item.getGateValveIsy().isEmpty()) {

            return item.getGateValveIsy();
        }

        // 🔥 2. Если в ValveItem нет - пробуем из GateValve (объединенный источник)
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