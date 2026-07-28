package com.mikesuvade.focus.domain.models;

public class ValveItem {
    private int itemId;
    private String parentSessionId;
    private String name;
    private String nameEng;
    private String isy;
    private boolean hasMotor;
    private boolean isAssembled;
    private boolean isChecked;
    private String operationTimestamp;

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getParentSessionId() { return parentSessionId; }
    public void setParentSessionId(String parentSessionId) { this.parentSessionId = parentSessionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameEng() { return nameEng; }
    public void setNameEng(String nameEng) { this.nameEng = nameEng; }

    public String getIsy() { return isy; }
    public void setIsy(String isy) { this.isy = isy; }

    public boolean isHasMotor() { return hasMotor; }
    public void setHasMotor(boolean hasMotor) { this.hasMotor = hasMotor; }

    public boolean isAssembled() { return isAssembled; }
    public void setAssembled(boolean assembled) { isAssembled = assembled; }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }

    public String getOperationTimestamp() { return operationTimestamp; }
    public void setOperationTimestamp(String operationTimestamp) { this.operationTimestamp = operationTimestamp; }
}