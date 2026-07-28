package com.mikesuvade.focus.domain.models;

public class Setpoint {
    private int id;
    private String name;
    private String positionName;
    private String location;
    private String setpointValue;
    private String delayTime;
    private String operation;
    private String notes;
    private String equipmentGroup;

    // Для пользовательских таблиц
    private int originalId;
    private String editedAt;
    private boolean isCustom;

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSetpointValue() { return setpointValue; }
    public void setSetpointValue(String setpointValue) { this.setpointValue = setpointValue; }

    public String getDelayTime() { return delayTime; }
    public void setDelayTime(String delayTime) { this.delayTime = delayTime; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getEquipmentGroup() { return equipmentGroup; }
    public void setEquipmentGroup(String equipmentGroup) { this.equipmentGroup = equipmentGroup; }

    public int getOriginalId() { return originalId; }
    public void setOriginalId(int originalId) { this.originalId = originalId; }

    public String getEditedAt() { return editedAt; }
    public void setEditedAt(String editedAt) { this.editedAt = editedAt; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    @Override
    public String toString() {
        return name + " (" + positionName + ")";
    }
}