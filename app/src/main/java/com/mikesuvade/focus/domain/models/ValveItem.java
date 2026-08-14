package com.mikesuvade.focus.domain.models;

public class ValveItem {
    private int itemId;
    private String parentSessionId;
    private String name;
    private String isy;
    private int hasMotor;           // 0 или 1

    // Статусы (назначаются мастером)
    private int isAssembled;        // 1=Собрать, 0=Разобрать
    private int motorDisabled;      // 1=двигатель отключён
    private int boxRemoved;         // 1=коробка КВ снята

    // Выполнение
    private int isChecked;          // 0=не выполнено, 1=выполнено
    private String checkedAt;       // дата выполнения

    // Служебное
    private String operationTimestamp;

    // Геттеры и сеттеры
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getParentSessionId() { return parentSessionId; }
    public void setParentSessionId(String parentSessionId) { this.parentSessionId = parentSessionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIsy() { return isy; }
    public void setIsy(String isy) { this.isy = isy; }

    public int getHasMotor() { return hasMotor; }
    public void setHasMotor(int hasMotor) { this.hasMotor = hasMotor; }

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
}