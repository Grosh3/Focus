package com.mikesuvade.focus.domain.models;

public class ValveItem {
    private int itemId;
    private String parentSessionId;
    private int gateValveId;

    private int isAssembled;
    private int motorDisabled;
    private int boxRemoved;
    private int isChecked;
    private String checkedAt;
    private String operationTimestamp;

    // ==========================================
    // ПОЛЬЗОВАТЕЛЬСКИЕ ПОЛЯ
    // ==========================================
    private int id;           // ID в пользовательской БД

    // ==========================================
    // ГЕТТЕРЫ И СЕТТЕРЫ
    // ==========================================

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getParentSessionId() { return parentSessionId; }
    public void setParentSessionId(String parentSessionId) { this.parentSessionId = parentSessionId; }

    public int getGateValveId() { return gateValveId; }
    public void setGateValveId(int gateValveId) { this.gateValveId = gateValveId; }

    public int getIsAssembled() { return isAssembled; }
    public void setIsAssembled(int isAssembled) { this.isAssembled = isAssembled; }

    public int getMotorDisabled() { return motorDisabled; }
    public void setMotorDisabled(int motorDisabled) { this.motorDisabled = motorDisabled; }

    public int getBoxRemoved() { return boxRemoved; }
    public void setBoxRemoved(int boxRemoved) { this.boxRemoved = boxRemoved; }

    public int getIsChecked() { return isChecked; }
    public void setIsChecked(int isChecked) { this.isChecked = isChecked; }

    public String getCheckedAt() { return checkedAt; }
    public void setCheckedAt(String checkedAt) { this.checkedAt = checkedAt; }

    public String getOperationTimestamp() { return operationTimestamp; }
    public void setOperationTimestamp(String operationTimestamp) { this.operationTimestamp = operationTimestamp; }

    // ==========================================
    // ПОЛЬЗОВАТЕЛЬСКИЕ ПОЛЯ - ГЕТТЕРЫ И СЕТТЕРЫ
    // ==========================================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // ==========================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ==========================================

    @Override
    public String toString() {
        return "ValveItem{" +
                "itemId=" + itemId +
                ", gateValveId=" + gateValveId +
                ", parentSessionId='" + parentSessionId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ValveItem that = (ValveItem) obj;
        return itemId == that.itemId;
    }

    @Override
    public int hashCode() {
        return itemId;
    }
}