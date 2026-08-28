package com.mikesuvade.focus.domain.models;

public class Measurement {
    private int id;
    private String measurementDate;
    private double value;               // для совместимости со старыми записями
    private double inputValue;          // введённое пользователем
    private String unit;
    private double temperature;
    private String sensorType;
    private String description;
    private String createdAt;
    private double coldJunctionMv;      // для термопар (мВ)
    private double lineResistance;      // для термосопротивлений (Ом)

    // ==========================================
    // ПОЛЬЗОВАТЕЛЬСКИЕ ПОЛЯ
    // ==========================================
    private int isSynced;               // 0 - не синхронизировано, 1 - синхронизировано

    // ==========================================
    // ГЕТТЕРЫ И СЕТТЕРЫ
    // ==========================================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMeasurementDate() { return measurementDate; }
    public void setMeasurementDate(String measurementDate) { this.measurementDate = measurementDate; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public double getInputValue() { return inputValue; }
    public void setInputValue(double inputValue) { this.inputValue = inputValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public String getSensorType() { return sensorType; }
    public void setSensorType(String sensorType) { this.sensorType = sensorType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public double getColdJunctionMv() { return coldJunctionMv; }
    public void setColdJunctionMv(double coldJunctionMv) { this.coldJunctionMv = coldJunctionMv; }

    public double getLineResistance() { return lineResistance; }
    public void setLineResistance(double lineResistance) { this.lineResistance = lineResistance; }

    // ==========================================
    // ПОЛЬЗОВАТЕЛЬСКИЕ ПОЛЯ - ГЕТТЕРЫ И СЕТТЕРЫ
    // ==========================================

    public int getIsSynced() { return isSynced; }
    public void setIsSynced(int isSynced) { this.isSynced = isSynced; }

    // ==========================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ==========================================

    @Override
    public String toString() {
        return "Measurement{" +
                "id=" + id +
                ", date='" + measurementDate + '\'' +
                ", value=" + value +
                ", unit='" + unit + '\'' +
                ", temperature=" + temperature +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Measurement that = (Measurement) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}