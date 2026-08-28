package com.mikesuvade.focus.domain.models;

public class ValveWorkSession {
    private String sessionId;
    private String saveDate;
    private String equipmentDescription;

    // ==========================================
    // ПОЛЬЗОВАТЕЛЬСКИЕ ПОЛЯ
    // ==========================================
    private int id;           // ID в пользовательской БД
    private int isSynced;     // 0 - не синхронизировано, 1 - синхронизировано
    private String createdAt; // Дата создания записи в пользовательской БД

    // ==========================================
    // ГЕТТЕРЫ И СЕТТЕРЫ
    // ==========================================

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getSaveDate() { return saveDate; }
    public void setSaveDate(String saveDate) { this.saveDate = saveDate; }

    public String getEquipmentDescription() { return equipmentDescription; }
    public void setEquipmentDescription(String equipmentDescription) { this.equipmentDescription = equipmentDescription; }

    // ==========================================
    // ПОЛЬЗОВАТЕЛЬСКИЕ ПОЛЯ - ГЕТТЕРЫ И СЕТТЕРЫ
    // ==========================================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIsSynced() { return isSynced; }
    public void setIsSynced(int isSynced) { this.isSynced = isSynced; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // ==========================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ==========================================

    @Override
    public String toString() {
        return (equipmentDescription != null ? equipmentDescription : "Без названия") +
                " (" + (saveDate != null ? saveDate : "нет даты") + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ValveWorkSession that = (ValveWorkSession) obj;
        return sessionId != null && sessionId.equals(that.sessionId);
    }

    @Override
    public int hashCode() {
        return sessionId != null ? sessionId.hashCode() : 0;
    }
}