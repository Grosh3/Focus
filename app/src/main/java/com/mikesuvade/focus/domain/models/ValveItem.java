package com.mikesuvade.focus.domain.models;

public class ValveItem {
    private int id;
    private int itemId;
    private String parentSessionId;
    private int gateValveId;

    // 🔥 НОВЫЕ ПОЛЯ ДЛЯ ХРАНЕНИЯ ДАННЫХ ЗАДВИЖКИ
    private String gateValveName;
    private String gateValveIsy;
    private String gateValveKks;
    private String gateValvePowerCabinet;
    private String gateValveLocationDescription;
    private String gateValveOnPlace;
    private String gateValveFullName;

    private int isAssembled;
    private int motorDisabled;
    private int boxRemoved;
    private int isChecked;
    private String checkedAt;
    private String operationTimestamp;
    private String createdAt;
    private String updatedAt;

    // ==========================================
    // ГЕТТЕРЫ И СЕТТЕРЫ
    // ==========================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getParentSessionId() {
        return parentSessionId;
    }

    public void setParentSessionId(String parentSessionId) {
        this.parentSessionId = parentSessionId;
    }

    public int getGateValveId() {
        return gateValveId;
    }

    public void setGateValveId(int gateValveId) {
        this.gateValveId = gateValveId;
    }

    // 🔥 НОВЫЕ ГЕТТЕРЫ И СЕТТЕРЫ
    public String getGateValveName() {
        return gateValveName;
    }

    public void setGateValveName(String gateValveName) {
        this.gateValveName = gateValveName;
    }

    public String getGateValveIsy() {
        return gateValveIsy;
    }

    public void setGateValveIsy(String gateValveIsy) {
        this.gateValveIsy = gateValveIsy;
    }

    public String getGateValveKks() {
        return gateValveKks;
    }

    public void setGateValveKks(String gateValveKks) {
        this.gateValveKks = gateValveKks;
    }

    public String getGateValvePowerCabinet() {
        return gateValvePowerCabinet;
    }

    public void setGateValvePowerCabinet(String gateValvePowerCabinet) {
        this.gateValvePowerCabinet = gateValvePowerCabinet;
    }

    public String getGateValveLocationDescription() {
        return gateValveLocationDescription;
    }

    public void setGateValveLocationDescription(String gateValveLocationDescription) {
        this.gateValveLocationDescription = gateValveLocationDescription;
    }

    public String getGateValveOnPlace() {
        return gateValveOnPlace;
    }

    public void setGateValveOnPlace(String gateValveOnPlace) {
        this.gateValveOnPlace = gateValveOnPlace;
    }

    public String getGateValveFullName() {
        return gateValveFullName;
    }

    public void setGateValveFullName(String gateValveFullName) {
        this.gateValveFullName = gateValveFullName;
    }

    public int getIsAssembled() {
        return isAssembled;
    }

    public void setIsAssembled(int isAssembled) {
        this.isAssembled = isAssembled;
    }

    public int getMotorDisabled() {
        return motorDisabled;
    }

    public void setMotorDisabled(int motorDisabled) {
        this.motorDisabled = motorDisabled;
    }

    public int getBoxRemoved() {
        return boxRemoved;
    }

    public void setBoxRemoved(int boxRemoved) {
        this.boxRemoved = boxRemoved;
    }

    public int getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(int isChecked) {
        this.isChecked = isChecked;
    }

    public String getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(String checkedAt) {
        this.checkedAt = checkedAt;
    }

    public String getOperationTimestamp() {
        return operationTimestamp;
    }

    public void setOperationTimestamp(String operationTimestamp) {
        this.operationTimestamp = operationTimestamp;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}