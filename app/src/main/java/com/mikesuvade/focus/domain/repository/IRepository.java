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
    List<Setpoint> searchSetpointsByGroup(String group);
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
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД (focus_user.db) - GATE VALVES
    // ==========================================

    /**
     * Получить все пользовательские задвижки (редактированные и удаленные)
     */
    List<GateValve> getAllUserGateValves();

    /**
     * Получить пользовательскую задвижку по original_id
     */
    GateValve getUserGateValveByOriginalId(int originalId);

    /**
     * Поиск по пользовательским задвижкам
     */
    List<GateValve> searchUserGateValves(String query);

    /**
     * Добавить задвижку в пользовательскую БД
     */
    long insertUserGateValve(GateValve valve);

    /**
     * Обновить задвижку в пользовательской БД
     */
    int updateUserGateValve(GateValve valve);

    /**
     * Физически удалить задвижку из пользовательской БД
     */
    int deleteUserGateValve(int id);

    /**
     * Скопировать задвижку из справочника в пользовательскую БД
     * @param originalId ID из справочника
     */
    void copyGateValveToUser(int originalId);

    /**
     * Пометить задвижку как удаленную (is_deleted = 1)
     * Если задвижки нет в пользовательской БД - копирует из справочника
     */
    void markGateValveAsDeleted(int originalId);

    /**
     * Получить все задвижки (объединение справочника + пользовательские)
     * Пользовательские заменяют справочные по original_id
     */
    List<GateValve> getAllGateValvesWithUser();

    /**
     * Поиск задвижек (сначала пользовательская БД, потом справочник)
     * Если есть в пользовательской - показываем ее
     */
    List<GateValve> searchGateValvesWithUser(String query);


    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД (focus_user.db) - SENSORS
    // ==========================================

    /**
     * Получить все пользовательские датчики (редактированные и удаленные)
     */
    List<Sensor> getAllUserSensors();

    /**
     * Получить пользовательский датчик по original_kks
     */
    Sensor getUserSensorByOriginalKks(String originalKks);

    /**
     * Поиск по пользовательским датчикам
     */
    List<Sensor> searchUserSensors(String query);

    /**
     * Добавить датчик в пользовательскую БД
     */
    long insertUserSensor(Sensor sensor);

    /**
     * Обновить датчик в пользовательской БД
     */
    int updateUserSensor(Sensor sensor);

    /**
     * Физически удалить датчик из пользовательской БД
     */
    int deleteUserSensor(int id);

    /**
     * Скопировать датчик из справочника в пользовательскую БД
     * @param originalKks KKS из справочника
     */
    void copySensorToUser(String originalKks);

    /**
     * Пометить датчик как удаленный (is_deleted = 1)
     * Если датчика нет в пользовательской БД - копирует из справочника
     */
    void markSensorAsDeleted(String originalKks);

    /**
     * Получить все датчики (объединение справочника + пользовательские)
     */
    List<Sensor> getAllSensorsWithUser();

    /**
     * Поиск датчиков (сначала пользовательская БД, потом справочник)
     */
    List<Sensor> searchSensorsWithUser(String query);


    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД (focus_user.db) - SETPOINTS
    // ==========================================

    /**
     * Получить все пользовательские уставки (редактированные и удаленные)
     */
    List<Setpoint> getAllUserSetpoints();

    /**
     * Получить пользовательскую уставку по original_id
     */
    Setpoint getUserSetpointByOriginalId(int originalId);

    /**
     * Поиск по пользовательским уставкам
     */
    List<Setpoint> searchUserSetpoints(String query);

    /**
     * Добавить уставку в пользовательскую БД
     */
    long insertUserSetpoint(Setpoint setpoint);

    /**
     * Обновить уставку в пользовательской БД
     */
    int updateUserSetpoint(Setpoint setpoint);

    /**
     * Физически удалить уставку из пользовательской БД
     */
    int deleteUserSetpoint(int id);

    /**
     * Скопировать уставку из справочника в пользовательскую БД
     * @param originalId ID из справочника
     */
    void copySetpointToUser(int originalId);

    /**
     * Пометить уставку как удаленную (is_deleted = 1)
     * Если уставки нет в пользовательской БД - копирует из справочника
     */
    void markSetpointAsDeleted(int originalId);

    /**
     * Получить все уставки (объединение справочника + пользовательские)
     */
    List<Setpoint> getAllSetpointsWithUser();

    /**
     * Поиск уставок (сначала пользовательская БД, потом справочник)
     */
    List<Setpoint> searchSetpointsWithUser(String query);


    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД (focus_user.db) - WORK SESSIONS
    // ==========================================

    /**
     * Сохранить сессию в пользовательскую БД
     */
    long insertUserWorkSession(ValveWorkSession session);

    /**
     * Получить все пользовательские сессии
     */
    List<ValveWorkSession> getAllUserWorkSessions();

    /**
     * Получить сессию по session_id
     */
    ValveWorkSession getUserWorkSessionById(String sessionId);

    /**
     * Обновить сессию в пользовательской БД
     */
    int updateUserWorkSession(ValveWorkSession session);

    /**
     * Удалить сессию из пользовательской БД
     */
    int deleteUserWorkSession(String sessionId);

    /**
     * Обновить дату сессии в пользовательской БД
     */
    int updateUserWorkSessionDate(String sessionId, String saveDate);

    /**
     * Обновить имя сессии в пользовательской БД
     */
    int updateUserWorkSessionName(String sessionId, String newName);


    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД (focus_user.db) - SESSION ITEMS
    // ==========================================

    /**
     * Сохранить элемент сессии (задвижку в сессии)
     */
    long insertUserSessionItem(ValveItem item);

    /**
     * Получить все элементы сессии
     */
    List<ValveItem> getUserSessionItemsBySession(String sessionId);

    /**
     * Обновить элемент сессии
     */
    int updateUserSessionItem(ValveItem item);

    /**
     * Удалить элемент сессии
     */
    int deleteUserSessionItem(int itemId);

    /**
     * Обновить статус элемента сессии (собрано/разобрано/двигатель/ККВ)
     */
    int updateUserSessionItemStatus(int itemId, int isAssembled, int motorDisabled, int boxRemoved);

    /**
     * Обновить статус выполнения элемента сессии
     */
    int updateUserSessionItemChecked(int itemId, int isChecked, String checkedAt);


    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД (focus_user.db) - MEASUREMENTS
    // ==========================================

    /**
     * Сохранить замер в пользовательскую БД
     */
    long insertUserMeasurement(Measurement measurement);

    /**
     * Получить все пользовательские замеры
     */
    List<Measurement> getAllUserMeasurements();

    /**
     * Получить замеры по дате
     */
    List<Measurement> getUserMeasurementsByDate(String date);

    /**
     * Обновить описание замера
     */
    int updateUserMeasurementDescription(int id, String description);

    /**
     * Удалить замер
     */
    int deleteUserMeasurement(int id);


    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД (focus_user.db) - СИНХРОНИЗАЦИЯ
    // ==========================================

    /**
     * Экспортировать пользовательскую БД в файл
     */
    void exportUserDatabase(String filePath);

    /**
     * Импортировать пользовательскую БД из файла
     */
    void importUserDatabase(String filePath);
}