package com.mikesuvade.focus.domain.repository;

import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.Sensor;
import com.mikesuvade.focus.domain.models.Setpoint;
import com.mikesuvade.focus.domain.models.Measurement;
import com.mikesuvade.focus.domain.models.ValveWorkSession;
import com.mikesuvade.focus.domain.models.ValveItem;

import java.util.List;

public interface IRepository {

    // ==========================================
    // 📚 ОСНОВНАЯ БД (СПРАВОЧНИК) - GATE VALVES
    // ==========================================
    List<GateValve> getAllGateValves();
    GateValve getGateValveById(int id);
    List<GateValve> searchGateValves(String query);
    long insertGateValve(GateValve valve);
    int updateGateValve(GateValve valve);
    int deleteGateValve(int id);

    // ==========================================
    // 📚 ОСНОВНАЯ БД (СПРАВОЧНИК) - SENSORS
    // ==========================================
    List<Sensor> getAllSensors();
    Sensor getSensorByKks(String kks);
    Sensor getSensorById(int id);
    List<Sensor> searchSensors(String query);
    long insertSensor(Sensor sensor);
    int updateSensor(Sensor sensor);
    int deleteSensor(String kks);


    // ==========================================
    // 📚 ОСНОВНАЯ БД (СПРАВОЧНИК) - SETPOINTS
    // ==========================================
    List<Setpoint> getAllSetpoints();
    Setpoint getSetpointById(int id);
    List<Setpoint> searchSetpoints(String query);
    List<Setpoint> searchSetpointsByGroup(String group);  // ← ТОЛЬКО ЗДЕСЬ
    long insertSetpoint(Setpoint setpoint);
    int updateSetpoint(Setpoint setpoint);
    int deleteSetpoint(int id);

    // ==========================================
    // 📚 ОСНОВНАЯ БД (СПРАВОЧНИК) - CONVERTER
    // ==========================================
    List<com.mikesuvade.focus.domain.models.ConverterPoint> getConverterPoints(String tableName);
    double getTemperatureFromResistance(String tableName, double resistance);
    double getSignalFromTemperature(String tableName, double temperature);
    List<String> getConverterTableNames();

    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД - GATE VALVES
    // ==========================================
    List<GateValve> getAllUserGateValves();
    GateValve getUserGateValveByOriginalId(int originalId);
    List<GateValve> searchUserGateValves(String query);
    long insertUserGateValve(GateValve valve);
    int updateUserGateValve(GateValve valve);
    int deleteUserGateValve(int id);
    void copyGateValveToUser(int originalId);
    void markGateValveAsDeleted(int originalId);
    List<GateValve> getAllGateValvesWithUser();
    List<GateValve> searchGateValvesWithUser(String query);
    GateValve getUserGateValveById(int id);

    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД - SENSORS
    // ==========================================
    List<Sensor> getAllUserSensors();

    List<Sensor> searchUserSensors(String query);
    long insertUserSensor(Sensor sensor);
    int updateUserSensor(Sensor sensor);
    int deleteUserSensor(int id);
    void copySensorToUser(int originalId);       // ← ДОБАВИТЬ
    void markSensorAsDeleted(int originalId);

    List<Sensor> getAllSensorsWithUser();
    List<Sensor> searchSensorsWithUser(String query);
    Sensor getUserSensorByOriginalId(int originalId);

    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД - SETPOINTS
    // ==========================================
    List<Setpoint> getAllUserSetpoints();
    Setpoint getUserSetpointByOriginalId(int originalId);
    List<Setpoint> searchUserSetpoints(String query);
    List<Setpoint> searchUserSetpointsByGroup(String group);  // ← ТОЛЬКО ЗДЕСЬ
    long insertUserSetpoint(Setpoint setpoint);
    int updateUserSetpoint(Setpoint setpoint);
    int deleteUserSetpoint(int id);
    void copySetpointToUser(int originalId);
    void markSetpointAsDeleted(int originalId);
    List<Setpoint> getAllSetpointsWithUser();
    List<Setpoint> searchSetpointsWithUser(String query);
    List<Setpoint> searchSetpointsByGroupWithUser(String group);  // ← ТОЛЬКО ЗДЕСЬ
    // ==================== GROUPS ====================
    List<String> getAllEquipmentGroups();
    List<String> getAllEquipmentGroupsWithUser();

    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД - WORK SESSIONS
    // ==========================================
    long insertUserWorkSession(ValveWorkSession session);
    List<ValveWorkSession> getAllUserWorkSessions();
    ValveWorkSession getUserWorkSessionById(String sessionId);
    int updateUserWorkSession(ValveWorkSession session);
    int deleteUserWorkSession(String sessionId);
    int updateUserWorkSessionDate(String sessionId, String saveDate);
    int updateUserWorkSessionName(String sessionId, String newName);

    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД - SESSION ITEMS
    // ==========================================
    long insertUserSessionItem(ValveItem item);
    List<ValveItem> getUserSessionItemsBySession(String sessionId);
    int updateUserSessionItem(ValveItem item);
    int deleteUserSessionItem(int itemId);
    int updateUserSessionItemStatus(int itemId, int isAssembled, int motorDisabled, int boxRemoved);
    int updateUserSessionItemChecked(int itemId, int isChecked, String checkedAt);

    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД - MEASUREMENTS
    // ==========================================
    long insertUserMeasurement(Measurement measurement);
    List<Measurement> getAllUserMeasurements();
    List<Measurement> getUserMeasurementsByDate(String date);
    int updateUserMeasurementDescription(int id, String description);
    int deleteUserMeasurement(int id);

    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД - СИНХРОНИЗАЦИЯ
    // ==========================================
    void exportUserDatabase(String filePath);
    void importUserDatabase(String filePath);
    // ==========================================
    Setpoint getAnyUserSetpointByOriginalId(int originalId);
    Sensor getAnyUserSensorByOriginalId(int originalId);
    GateValve getAnyUserGateValveByOriginalId(int originalId);

}