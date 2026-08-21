package com.mikesuvade.focus.domain.models;

public class TemperatureResult {
    private String sensorName;
    private String unit;
    private double temperature;
    private String tableName;

    // Для термосопротивлений (Ом)
    private double userValue;        // введённое значение
    private double correctedValue;   // значение минус сопротивление линии

    // Для термопар (мВ)
    private double coldJunctionTemp; // температура холодного спая
    private double coldJunctionMv;   // напряжение холодного спая
    private double totalMv;          // суммарное напряжение

    // ==========================================
    // ГЕТТЕРЫ И СЕТТЕРЫ
    // ==========================================

    public String getSensorName() { return sensorName; }
    public void setSensorName(String sensorName) { this.sensorName = sensorName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    // Для термосопротивлений
    public double getUserValue() { return userValue; }
    public void setUserValue(double userValue) { this.userValue = userValue; }

    public double getCorrectedValue() { return correctedValue; }
    public void setCorrectedValue(double correctedValue) { this.correctedValue = correctedValue; }

    // Для термопар
    public double getColdJunctionTemp() { return coldJunctionTemp; }
    public void setColdJunctionTemp(double coldJunctionTemp) { this.coldJunctionTemp = coldJunctionTemp; }

    public double getColdJunctionMv() { return coldJunctionMv; }
    public void setColdJunctionMv(double coldJunctionMv) { this.coldJunctionMv = coldJunctionMv; }

    public double getTotalMv() { return totalMv; }
    public void setTotalMv(double totalMv) { this.totalMv = totalMv; }
}