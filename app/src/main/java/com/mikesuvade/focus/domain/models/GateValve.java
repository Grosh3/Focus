package com.mikesuvade.focus.domain.models;

public class GateValve {
    private int id;
    private String nameEng;
    private String kks;
    private String name;
    private String isy;
    private String powerCabinet;
    private String fullName;
    private String onPlace;
    private String ap50;
    private String mark;
    private String cdaCabinet;
    private String cdaCabinetPosition;
    private String slot;
    private byte[] nameSpaceViewOpen;
    private String descriptionBlockingOpen;
    private byte[] namespaceViewClose;
    private String descriptionBlockingClose;
    private byte[] namespaceViewPerifer;
    private String descriptionBlockingPerifer;

    // Для пользовательских таблиц
    private int originalId;
    private String editedAt;
    private boolean isCustom;

    // Конструкторы
    public GateValve() {}

    public GateValve(String name, String kks) {
        this.name = name;
        this.kks = kks;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNameEng() { return nameEng; }
    public void setNameEng(String nameEng) { this.nameEng = nameEng; }

    public String getKks() { return kks; }
    public void setKks(String kks) { this.kks = kks; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIsy() { return isy; }
    public void setIsy(String isy) { this.isy = isy; }

    public String getPowerCabinet() { return powerCabinet; }
    public void setPowerCabinet(String powerCabinet) { this.powerCabinet = powerCabinet; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getOnPlace() { return onPlace; }
    public void setOnPlace(String onPlace) { this.onPlace = onPlace; }

    public String getAp50() { return ap50; }
    public void setAp50(String ap50) { this.ap50 = ap50; }

    public String getMark() { return mark; }
    public void setMark(String mark) { this.mark = mark; }

    public String getCdaCabinet() { return cdaCabinet; }
    public void setCdaCabinet(String cdaCabinet) { this.cdaCabinet = cdaCabinet; }

    public String getCdaCabinetPosition() { return cdaCabinetPosition; }
    public void setCdaCabinetPosition(String cdaCabinetPosition) { this.cdaCabinetPosition = cdaCabinetPosition; }

    public String getSlot() { return slot; }
    public void setSlot(String slot) { this.slot = slot; }

    public byte[] getNameSpaceViewOpen() { return nameSpaceViewOpen; }
    public void setNameSpaceViewOpen(byte[] nameSpaceViewOpen) { this.nameSpaceViewOpen = nameSpaceViewOpen; }

    public String getDescriptionBlockingOpen() { return descriptionBlockingOpen; }
    public void setDescriptionBlockingOpen(String descriptionBlockingOpen) { this.descriptionBlockingOpen = descriptionBlockingOpen; }

    public byte[] getNamespaceViewClose() { return namespaceViewClose; }
    public void setNamespaceViewClose(byte[] namespaceViewClose) { this.namespaceViewClose = namespaceViewClose; }

    public String getDescriptionBlockingClose() { return descriptionBlockingClose; }
    public void setDescriptionBlockingClose(String descriptionBlockingClose) { this.descriptionBlockingClose = descriptionBlockingClose; }

    public byte[] getNamespaceViewPerifer() { return namespaceViewPerifer; }
    public void setNamespaceViewPerifer(byte[] namespaceViewPerifer) { this.namespaceViewPerifer = namespaceViewPerifer; }

    public String getDescriptionBlockingPerifer() { return descriptionBlockingPerifer; }
    public void setDescriptionBlockingPerifer(String descriptionBlockingPerifer) { this.descriptionBlockingPerifer = descriptionBlockingPerifer; }

    public int getOriginalId() { return originalId; }
    public void setOriginalId(int originalId) { this.originalId = originalId; }

    public String getEditedAt() { return editedAt; }
    public void setEditedAt(String editedAt) { this.editedAt = editedAt; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    @Override
    public String toString() {
        return name + " (" + kks + ")";
    }
}