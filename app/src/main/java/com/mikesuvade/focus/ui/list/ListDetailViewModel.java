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

    // Два списка: левый (СОБРАТЬ) и правый (РАЗОБРАТЬ)
    private final MutableLiveData<List<ValveItem>> leftList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ValveItem>> rightList = new MutableLiveData<>(new ArrayList<>());

    // Счётчик
    private final MutableLiveData<Integer> totalCount = new MutableLiveData<>(0);

    // Имя списка
    private final MutableLiveData<String> listName = new MutableLiveData<>("Список работ");

    // Session ID для сохранения
    private String sessionId;

    public ListDetailViewModel(IRepository repository) {
        this.repository = repository;
    }

    // ==========================================
    // ЗАГРУЗКА СПИСКА ИЗ GateValve
    // ==========================================
    public void loadFromGateValves(List<GateValve> valves) {
        Log.d(TAG, "loadFromGateValves: " + (valves != null ? valves.size() : 0) + " valves");

        if (valves == null || valves.isEmpty()) {
            leftList.setValue(new ArrayList<>());
            rightList.setValue(new ArrayList<>());
            updateCount();
            return;
        }

        // По умолчанию все элементы в левом списке (СОБРАТЬ)
        List<ValveItem> leftItems = new ArrayList<>();
        for (GateValve valve : valves) {
            ValveItem item = new ValveItem();
            item.setName(valve.getName());
            item.setIsy(valve.getIsy());
            item.setHasMotor(1);
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
        Log.d(TAG, "loadFromGateValves: loaded " + leftItems.size() + " items to left list");
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

    public LiveData<Integer> getTotalCount() {
        return totalCount;
    }

    public LiveData<String> getListName() {
        return listName;
    }

    public void setListName(String name) {
        listName.setValue(name);
    }

    // ==========================================
    // ПЕРЕМЕЩЕНИЕ ЭЛЕМЕНТА
    // ==========================================
    public void moveItem(ValveItem item, boolean toRight) {
        Log.d(TAG, "moveItem: " + item.getName() + " toRight=" + toRight);

        List<ValveItem> left = new ArrayList<>(leftList.getValue());
        List<ValveItem> right = new ArrayList<>(rightList.getValue());

        if (toRight) {
            left.remove(item);
            item.setIsAssembled(0);
            item.setMotorDisabled(0);
            item.setBoxRemoved(0);
            item.setIsChecked(0);
            item.setCheckedAt(null);
            right.add(item);
        } else {
            right.remove(item);
            item.setIsAssembled(1);
            item.setMotorDisabled(0);
            item.setBoxRemoved(0);
            item.setIsChecked(0);
            item.setCheckedAt(null);
            left.add(item);
        }

        leftList.setValue(left);
        rightList.setValue(right);
        updateCount();
    }

    // ==========================================
    // ГЛОБАЛЬНЫЕ ДЕЙСТВИЯ
    // ==========================================
    public void assembleAll() {
        Log.d(TAG, "assembleAll: moving all from right to left");

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
        Log.d(TAG, "disassembleAll: moving all from left to right");

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

    // ==========================================
    // ДОПОЛНИТЕЛЬНЫЕ РАБОТЫ (исправлено — работают с ОБОИМИ списками!)
    // ==========================================
    public void toggleMotor(ValveItem item) {
        Log.d(TAG, "toggleMotor: " + item.getName() + " (current state: " + item.getMotorDisabled() + ")");

        List<ValveItem> left = new ArrayList<>(leftList.getValue());
        List<ValveItem> right = new ArrayList<>(rightList.getValue());

        // Проверяем в левом списке
        if (left.contains(item)) {
            int index = left.indexOf(item);
            ValveItem found = left.get(index);
            found.setMotorDisabled(found.getMotorDisabled() == 1 ? 0 : 1);
            leftList.setValue(left);
            Log.d(TAG, "toggleMotor: left list, new state: " + found.getMotorDisabled());
            return;
        }

        // Проверяем в правом списке
        if (right.contains(item)) {
            int index = right.indexOf(item);
            ValveItem found = right.get(index);
            found.setMotorDisabled(found.getMotorDisabled() == 1 ? 0 : 1);
            rightList.setValue(right);
            Log.d(TAG, "toggleMotor: right list, new state: " + found.getMotorDisabled());
        }
    }

    public void toggleBox(ValveItem item) {
        Log.d(TAG, "toggleBox: " + item.getName() + " (current state: " + item.getBoxRemoved() + ")");

        List<ValveItem> left = new ArrayList<>(leftList.getValue());
        List<ValveItem> right = new ArrayList<>(rightList.getValue());

        // Проверяем в левом списке
        if (left.contains(item)) {
            int index = left.indexOf(item);
            ValveItem found = left.get(index);
            found.setBoxRemoved(found.getBoxRemoved() == 1 ? 0 : 1);
            leftList.setValue(left);
            Log.d(TAG, "toggleBox: left list, new state: " + found.getBoxRemoved());
            return;
        }

        // Проверяем в правом списке
        if (right.contains(item)) {
            int index = right.indexOf(item);
            ValveItem found = right.get(index);
            found.setBoxRemoved(found.getBoxRemoved() == 1 ? 0 : 1);
            rightList.setValue(right);
            Log.d(TAG, "toggleBox: right list, new state: " + found.getBoxRemoved());
        }
    }

    // ==========================================
    // ЧЕКБОКС (ВЫПОЛНЕНО)
    // ==========================================
    public void toggleChecked(ValveItem item) {
        Log.d(TAG, "toggleChecked: " + item.getName() + " (current state: " + item.getIsChecked() + ")");

        List<ValveItem> left = new ArrayList<>(leftList.getValue());
        List<ValveItem> right = new ArrayList<>(rightList.getValue());

        if (left.contains(item)) {
            int index = left.indexOf(item);
            ValveItem found = left.get(index);
            found.setIsChecked(found.getIsChecked() == 1 ? 0 : 1);
            if (found.getIsChecked() == 1) {
                found.setCheckedAt(getCurrentDateTime());
            } else {
                found.setCheckedAt(null);
            }
            left.remove(index);
            if (found.getIsChecked() == 1) {
                left.add(found);
            } else {
                left.add(0, found);
            }
            leftList.setValue(left);
            Log.d(TAG, "toggleChecked: left list, new state: " + found.getIsChecked());

        } else if (right.contains(item)) {
            int index = right.indexOf(item);
            ValveItem found = right.get(index);
            found.setIsChecked(found.getIsChecked() == 1 ? 0 : 1);
            if (found.getIsChecked() == 1) {
                found.setCheckedAt(getCurrentDateTime());
            } else {
                found.setCheckedAt(null);
            }
            right.remove(index);
            if (found.getIsChecked() == 1) {
                right.add(found);
            } else {
                right.add(0, found);
            }
            rightList.setValue(right);
            Log.d(TAG, "toggleChecked: right list, new state: " + found.getIsChecked());
        }

        updateCount();
    }

    // ==========================================
    // СОХРАНЕНИЕ В БД
    // ==========================================
    public void saveSession(String name) {
        Log.d(TAG, "saveSession: " + name);

        if (name == null || name.trim().isEmpty()) {
            name = "Список " + new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
        }

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

        Log.d(TAG, "saveSession: saved " + allItems.size() + " items");
    }

    // ==========================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ==========================================
    private void updateCount() {
        int leftSize = leftList.getValue() != null ? leftList.getValue().size() : 0;
        int rightSize = rightList.getValue() != null ? rightList.getValue().size() : 0;
        totalCount.setValue(leftSize + rightSize);
        Log.d(TAG, "updateCount: left=" + leftSize + ", right=" + rightSize + ", total=" + (leftSize + rightSize));
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}