package com.mikesuvade.focus.domain.repository;

import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.Sensor;
import com.mikesuvade.focus.domain.models.Setpoint;
import com.mikesuvade.focus.domain.models.Measurement;

import java.util.List;

public interface IRepository {

    // ==================== GATE VALVES ====================
    List<GateValve> getAllGateValves();
    GateValve getGateValveById(int id);
    List<GateValve> searchGateValves(String query);
    long insertGateValve(GateValve valve);
    int updateGateValve(GateValve valve);
    int deleteGateValve(int id);

    // ==================== GATE VALVES USER (правки) ====================
    List<GateValve> getCustomGateValves();
    long insertCustomGateValve(GateValve valve);
    int updateCustomGateValve(GateValve valve);
    int deleteCustomGateValve(int id);
    GateValve getCustomGateValveByOriginalId(int originalId);

    // ==================== SENSORS ====================
    List<Sensor> getAllSensors();
    Sensor getSensorByKks(String kks);
    List<Sensor> searchSensors(String query);
    long insertSensor(Sensor sensor);
    int updateSensor(Sensor sensor);
    int deleteSensor(String kks);

    // ==================== SENSORS USER (правки) ====================
    List<Sensor> getCustomSensors();
    long insertCustomSensor(Sensor sensor);
    int updateCustomSensor(Sensor sensor);
    int deleteCustomSensor(int id);
    Sensor getCustomSensorByOriginalKks(String originalKks);

    // ==================== SETPOINTS ====================
    List<Setpoint> getAllSetpoints();
    Setpoint getSetpointById(int id);
    List<Setpoint> searchSetpoints(String query);
    long insertSetpoint(Setpoint setpoint);
    int updateSetpoint(Setpoint setpoint);
    int deleteSetpoint(int id);

    // ==================== SETPOINTS USER (правки) ====================
    List<Setpoint> getCustomSetpoints();
    long insertCustomSetpoint(Setpoint setpoint);
    int updateCustomSetpoint(Setpoint setpoint);
    int deleteCustomSetpoint(int id);
    Setpoint getCustomSetpointByOriginalId(int originalId);

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
}