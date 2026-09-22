package com.mikesuvade.focus.data.repository;

import com.mikesuvade.focus.data.database.DatabaseHelper;
import com.mikesuvade.focus.data.database.UserDatabaseHelper;
import com.mikesuvade.focus.domain.models.ConverterPoint;
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.Measurement;
import com.mikesuvade.focus.domain.models.Sensor;
import com.mikesuvade.focus.domain.models.Setpoint;
import com.mikesuvade.focus.domain.models.ValveItem;
import com.mikesuvade.focus.domain.models.ValveWorkSession;
import com.mikesuvade.focus.domain.repository.IRepository;

import java.util.List;

public class RepositoryImpl implements IRepository {

    private final GateValveRepository valveRepo;
    private final SensorRepository sensorRepo;
    private final SetpointRepository setpointRepo;
    private final ConverterRepository converterRepo;
    private final SessionRepository sessionRepo;
    private final MeasurementRepository measurementRepo;
    private final GroupRepository groupRepo;

    public RepositoryImpl(DatabaseHelper dbHelper, UserDatabaseHelper userDbHelper) {
        CursorMapper mapper = new CursorMapper();
        this.valveRepo = new GateValveRepository(dbHelper, userDbHelper, mapper);
        this.sensorRepo = new SensorRepository(dbHelper, userDbHelper, mapper);
        this.setpointRepo = new SetpointRepository(dbHelper, userDbHelper, mapper);
        this.converterRepo = new ConverterRepository(dbHelper);
        this.sessionRepo = new SessionRepository(userDbHelper, mapper);
        this.measurementRepo = new MeasurementRepository(userDbHelper, mapper);
        this.groupRepo = new GroupRepository(dbHelper, userDbHelper);
    }

    // ==================== GATE VALVES ====================

    @Override public List<GateValve> getAllGateValves() { return valveRepo.getAll(); }
    @Override public GateValve getGateValveById(int id) { return valveRepo.getById(id); }
    @Override public List<GateValve> searchGateValves(String query) { return valveRepo.search(query); }
    @Override public long insertGateValve(GateValve valve) { return valveRepo.insert(valve); }
    @Override public int updateGateValve(GateValve valve) { return valveRepo.update(valve); }
    @Override public int deleteGateValve(int id) { return valveRepo.delete(id); }

    // ==================== SENSORS ====================

    @Override public List<Sensor> getAllSensors() { return sensorRepo.getAll(); }
    @Override public Sensor getSensorByKks(String kks) { return sensorRepo.getByKks(kks); }
    @Override public Sensor getSensorById(int id) { return sensorRepo.getById(id); }
    @Override public List<Sensor> searchSensors(String query) { return sensorRepo.search(query); }
    @Override public long insertSensor(Sensor sensor) { return sensorRepo.insert(sensor); }
    @Override public int updateSensor(Sensor sensor) { return sensorRepo.update(sensor); }
    @Override public int deleteSensor(String kks) { return sensorRepo.delete(kks); }

    // ==================== SETPOINTS ====================

    @Override public List<Setpoint> getAllSetpoints() { return setpointRepo.getAll(); }
    @Override public Setpoint getSetpointById(int id) { return setpointRepo.getById(id); }
    @Override public List<Setpoint> searchSetpoints(String query) { return setpointRepo.search(query); }
    @Override public List<Setpoint> searchSetpointsByGroup(String group) { return setpointRepo.searchByGroup(group); }
    @Override public long insertSetpoint(Setpoint setpoint) { return setpointRepo.insert(setpoint); }
    @Override public int updateSetpoint(Setpoint setpoint) { return setpointRepo.update(setpoint); }
    @Override public int deleteSetpoint(int id) { return setpointRepo.delete(id); }

    // ==================== CONVERTER ====================

    @Override public List<ConverterPoint> getConverterPoints(String tableName) { return converterRepo.getPoints(tableName); }
    @Override public double getTemperatureFromResistance(String tableName, double resistance) { return converterRepo.getTemperatureFromResistance(tableName, resistance); }
    @Override public double getSignalFromTemperature(String tableName, double temperature) { return converterRepo.getSignalFromTemperature(tableName, temperature); }
    @Override public List<String> getConverterTableNames() { return converterRepo.getTableNames(); }

    // ==================== USER GATE VALVES ====================

    @Override public List<GateValve> getAllUserGateValves() { return valveRepo.getAllUser(); }
    @Override public GateValve getUserGateValveByOriginalId(int originalId) { return valveRepo.getUserByOriginalId(originalId); }
    @Override public List<GateValve> searchUserGateValves(String query) { return valveRepo.searchUser(query); }
    @Override public long insertUserGateValve(GateValve valve) { return valveRepo.insertUser(valve); }
    @Override public int updateUserGateValve(GateValve valve) { return valveRepo.updateUser(valve); }
    @Override public int deleteUserGateValve(int id) { return valveRepo.deleteUser(id); }
    @Override public void copyGateValveToUser(int originalId) { valveRepo.copyToUser(originalId); }
    @Override public void markGateValveAsDeleted(int originalId) { valveRepo.markAsDeleted(originalId); }
    @Override public List<GateValve> getAllGateValvesWithUser() { return valveRepo.getAllWithUser(); }
    @Override public List<GateValve> searchGateValvesWithUser(String query) { return valveRepo.searchWithUser(query); }
    @Override public GateValve getUserGateValveById(int id) { return valveRepo.getUserById(id); }
    @Override public GateValve getAnyUserGateValveByOriginalId(int originalId) { return valveRepo.getAnyUserByOriginalId(originalId); }

    // ==================== USER SENSORS ====================

    @Override public List<Sensor> getAllUserSensors() { return sensorRepo.getAllUser(); }
    @Override public List<Sensor> searchUserSensors(String query) { return sensorRepo.searchUser(query); }
    @Override public long insertUserSensor(Sensor sensor) { return sensorRepo.insertUser(sensor); }
    @Override public int updateUserSensor(Sensor sensor) { return sensorRepo.updateUser(sensor); }
    @Override public int deleteUserSensor(int id) { return sensorRepo.deleteUser(id); }
    @Override public void copySensorToUser(int originalId) { sensorRepo.copyToUser(originalId); }
    @Override public void markSensorAsDeleted(int originalId) { sensorRepo.markAsDeleted(originalId); }
    @Override public List<Sensor> getAllSensorsWithUser() { return sensorRepo.getAllWithUser(); }
    @Override public List<Sensor> searchSensorsWithUser(String query) { return sensorRepo.searchWithUser(query); }
    @Override public Sensor getUserSensorByOriginalId(int originalId) { return sensorRepo.getUserByOriginalId(originalId); }
    @Override public Sensor getAnyUserSensorByOriginalId(int originalId) { return sensorRepo.getAnyUserByOriginalId(originalId); }

    // ==================== USER SETPOINTS ====================

    @Override public List<Setpoint> getAllUserSetpoints() { return setpointRepo.getAllUser(); }
    @Override public Setpoint getUserSetpointByOriginalId(int originalId) { return setpointRepo.getUserByOriginalId(originalId); }
    @Override public List<Setpoint> searchUserSetpoints(String query) { return setpointRepo.searchUser(query); }
    @Override public List<Setpoint> searchUserSetpointsByGroup(String group) { return setpointRepo.searchUserByGroup(group); }
    @Override public long insertUserSetpoint(Setpoint setpoint) { return setpointRepo.insertUser(setpoint); }
    @Override public int updateUserSetpoint(Setpoint setpoint) { return setpointRepo.updateUser(setpoint); }
    @Override public int deleteUserSetpoint(int id) { return setpointRepo.deleteUser(id); }
    @Override public void copySetpointToUser(int originalId) { setpointRepo.copyToUser(originalId); }
    @Override public void markSetpointAsDeleted(int originalId) { setpointRepo.markAsDeleted(originalId); }
    @Override public List<Setpoint> getAllSetpointsWithUser() { return setpointRepo.getAllWithUser(); }
    @Override public List<Setpoint> searchSetpointsWithUser(String query) { return setpointRepo.searchWithUser(query); }
    @Override public List<Setpoint> searchSetpointsByGroupWithUser(String group) { return setpointRepo.searchByGroupWithUser(group); }
    @Override public Setpoint getAnyUserSetpointByOriginalId(int originalId) { return setpointRepo.getAnyUserByOriginalId(originalId); }

    // ==================== GROUPS ====================

    @Override public List<String> getAllEquipmentGroups() { return groupRepo.getAll(); }
    @Override public List<String> getAllEquipmentGroupsWithUser() { return groupRepo.getAllWithUser(); }

    // ==================== WORK SESSIONS ====================

    @Override public long insertUserWorkSession(ValveWorkSession session) { return sessionRepo.insertSession(session); }
    @Override public List<ValveWorkSession> getAllUserWorkSessions() { return sessionRepo.getAllSessions(); }
    @Override public ValveWorkSession getUserWorkSessionById(String sessionId) { return sessionRepo.getSessionById(sessionId); }
    @Override public int updateUserWorkSession(ValveWorkSession session) { return sessionRepo.updateSession(session); }
    @Override public int deleteUserWorkSession(String sessionId) { return sessionRepo.deleteSession(sessionId); }
    @Override public int updateUserWorkSessionDate(String sessionId, String saveDate) { return sessionRepo.updateSessionDate(sessionId, saveDate); }
    @Override public int updateUserWorkSessionName(String sessionId, String newName) { return sessionRepo.updateSessionName(sessionId, newName); }

    // ==================== SESSION ITEMS ====================

    @Override public long insertUserSessionItem(ValveItem item) { return sessionRepo.insertItem(item); }
    @Override public List<ValveItem> getUserSessionItemsBySession(String sessionId) { return sessionRepo.getItemsBySession(sessionId); }
    @Override public int updateUserSessionItem(ValveItem item) { return sessionRepo.updateItem(item); }
    @Override public int deleteUserSessionItem(int itemId) { return sessionRepo.deleteItem(itemId); }
    @Override public int updateUserSessionItemStatus(int itemId, int isAssembled, int motorDisabled, int boxRemoved) { return sessionRepo.updateItemStatus(itemId, isAssembled, motorDisabled, boxRemoved); }
    @Override public int updateUserSessionItemChecked(int itemId, int isChecked, String checkedAt) { return sessionRepo.updateItemChecked(itemId, isChecked, checkedAt); }

    // ==================== MEASUREMENTS ====================

    @Override public long insertUserMeasurement(Measurement measurement) { return measurementRepo.insert(measurement); }
    @Override public List<Measurement> getAllUserMeasurements() { return measurementRepo.getAll(); }
    @Override public List<Measurement> getUserMeasurementsByDate(String date) { return measurementRepo.getByDate(date); }
    @Override public int updateUserMeasurementDescription(int id, String description) { return measurementRepo.updateDescription(id, description); }
    @Override public int deleteUserMeasurement(int id) { return measurementRepo.delete(id); }

    // ==================== СИНХРОНИЗАЦИЯ ====================

    @Override public void exportUserDatabase(String filePath) { /* TODO */ }
    @Override public void importUserDatabase(String filePath) { /* TODO */ }
}