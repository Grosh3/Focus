package com.mikesuvade.focus.domain.repository;

import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.Sensor;
import com.mikesuvade.focus.domain.models.Setpoint;
import com.mikesuvade.focus.domain.models.Measurement;
import com.mikesuvade.focus.domain.models.ValveWorkSession;

import java.util.List;

public interface IRepository {

    // ==================== GATE VALVES ====================
    List<GateValve> getAllGateValves();
    GateValve getGateValveById(int id);
    List<GateValve> searchGateValves(String query);
    long insertGateValve(GateValve valve);
    int updateGateValve(GateValve valve);
    int deleteGateValve(int id);
    // ==================== SENSORS ====================
    List<Sensor> getAllSensors();
    Sensor getSensorByKks(String kks);
    List<Sensor> searchSensors(String query);
    long insertSensor(Sensor sensor);
    int updateSensor(Sensor sensor);
    int deleteSensor(String kks);

    // ==================== SETPOINTS ====================
    List<Setpoint> getAllSetpoints();
    Setpoint getSetpointById(int id);
    List<Setpoint> searchSetpoints(String query);
    List<Setpoint> searchSetpointsByGroup(String group);
    long insertSetpoint(Setpoint setpoint);
    int updateSetpoint(Setpoint setpoint);
    int deleteSetpoint(int id);

    // ==================== CONVERTER (термометры/термопары) ====================
    List<com.mikesuvade.focus.domain.models.ConverterPoint> getConverterPoints(String tableName);
    double getTemperatureFromResistance(String tableName, double resistance);
    List<String> getConverterTableNames();

    // ==================== MEASUREMENTS ====================
    long insertMeasurement(Measurement measurement);
    List<Measurement> getAllMeasurements();
    List<Measurement> getMeasurementsByDate(String date);
    int deleteMeasurement(int id);

    // ==================== VALVE WORK SESSIONS ====================
    long insertWorkSession(com.mikesuvade.focus.domain.models.ValveWorkSession session);
    List<com.mikesuvade.focus.domain.models.ValveWorkSession> getAllWorkSessions();
    int deleteWorkSession(String sessionId);

    // ==================== VALVE ITEMS ====================
    long insertValveItem(com.mikesuvade.focus.domain.models.ValveItem item);
    List<com.mikesuvade.focus.domain.models.ValveItem> getValveItemsBySession(String sessionId);
    int updateValveItem(com.mikesuvade.focus.domain.models.ValveItem item);
    int deleteValveItem(int itemId);
    int updateValveItemStatus(int itemId, int isAssembled, int motorDisabled, int boxRemoved);
    int updateValveItemChecked(int itemId, int isChecked, String checkedAt);
    // В IRepository.java

    int updateWorkSession(ValveWorkSession session);
    int updateWorkSessionDate(String sessionId, String saveDate);
    ValveWorkSession getWorkSessionById(String sessionId);
    int updateWorkSessionName(String sessionId, String newName);
    int updateMeasurementDescription(int id, String description);
    // Добавь этот метод в интерфейс
    double getSignalFromTemperature(String tableName, double temperature);
}