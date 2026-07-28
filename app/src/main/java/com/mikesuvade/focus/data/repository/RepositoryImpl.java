package com.mikesuvade.focus.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mikesuvade.focus.data.database.DatabaseContract;
import com.mikesuvade.focus.data.database.DatabaseHelper;
import com.mikesuvade.focus.domain.models.ConverterPoint;
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.Measurement;
import com.mikesuvade.focus.domain.models.Sensor;
import com.mikesuvade.focus.domain.models.Setpoint;
import com.mikesuvade.focus.domain.models.ValveItem;
import com.mikesuvade.focus.domain.models.ValveWorkSession;
import com.mikesuvade.focus.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.List;

public class RepositoryImpl implements IRepository {

    private static final String TAG = "RepositoryImpl";
    private final DatabaseHelper dbHelper;

    public RepositoryImpl(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ==================== GATE VALVES ====================

    @Override
    public List<GateValve> getAllGateValves() {
        List<GateValve> valves = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.GateValvesEntry.TABLE_NAME,
                null, null, null, null, null,
                DatabaseContract.GateValvesEntry.COLUMN_NAME + " ASC"
        );

        while (cursor.moveToNext()) {
            valves.add(cursorToGateValve(cursor));
        }
        cursor.close();
        return valves;
    }

    @Override
    public GateValve getGateValveById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.GateValvesEntry.TABLE_NAME,
                null,
                DatabaseContract.GateValvesEntry._ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        GateValve valve = null;
        if (cursor.moveToFirst()) {
            valve = cursorToGateValve(cursor);
        }
        cursor.close();
        return valve;
    }

    @Override
    public List<GateValve> searchGateValves(String query) {
        List<GateValve> valves = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.GateValvesEntry.COLUMN_NAME + " LIKE ? OR " +
                DatabaseContract.GateValvesEntry.COLUMN_KKS + " LIKE ? OR " +
                DatabaseContract.GateValvesEntry.COLUMN_NAME_ENG + " LIKE ?";

        String[] args = new String[]{
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%"
        };

        Cursor cursor = db.query(
                DatabaseContract.GateValvesEntry.TABLE_NAME,
                null,
                selection,
                args,
                null, null,
                DatabaseContract.GateValvesEntry.COLUMN_NAME + " ASC"
        );

        while (cursor.moveToNext()) {
            valves.add(cursorToGateValve(cursor));
        }
        cursor.close();
        return valves;
    }

    @Override
    public long insertGateValve(GateValve valve) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = gateValveToContentValues(valve);
        return db.insert(DatabaseContract.GateValvesEntry.TABLE_NAME, null, values);
    }

    @Override
    public int updateGateValve(GateValve valve) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = gateValveToContentValues(valve);
        return db.update(
                DatabaseContract.GateValvesEntry.TABLE_NAME,
                values,
                DatabaseContract.GateValvesEntry._ID + " = ?",
                new String[]{String.valueOf(valve.getId())}
        );
    }

    @Override
    public int deleteGateValve(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.GateValvesEntry.TABLE_NAME,
                DatabaseContract.GateValvesEntry._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // ==================== GATE VALVES USER ====================

    @Override
    public List<GateValve> getCustomGateValves() {
        List<GateValve> valves = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.GateValvesUserEntry.TABLE_NAME,
                null, null, null, null, null,
                DatabaseContract.GateValvesUserEntry.COLUMN_EDITED_AT + " DESC"
        );

        while (cursor.moveToNext()) {
            valves.add(cursorToGateValveUser(cursor));
        }
        cursor.close();
        return valves;
    }

    @Override
    public long insertCustomGateValve(GateValve valve) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = gateValveUserToContentValues(valve);
        return db.insert(DatabaseContract.GateValvesUserEntry.TABLE_NAME, null, values);
    }

    @Override
    public int updateCustomGateValve(GateValve valve) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = gateValveUserToContentValues(valve);
        return db.update(
                DatabaseContract.GateValvesUserEntry.TABLE_NAME,
                values,
                DatabaseContract.GateValvesUserEntry._ID + " = ?",
                new String[]{String.valueOf(valve.getId())}
        );
    }

    @Override
    public int deleteCustomGateValve(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.GateValvesUserEntry.TABLE_NAME,
                DatabaseContract.GateValvesUserEntry._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    @Override
    public GateValve getCustomGateValveByOriginalId(int originalId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.GateValvesUserEntry.TABLE_NAME,
                null,
                DatabaseContract.GateValvesUserEntry.COLUMN_ORIGINAL_ID + " = ?",
                new String[]{String.valueOf(originalId)},
                null, null, null
        );

        GateValve valve = null;
        if (cursor.moveToFirst()) {
            valve = cursorToGateValveUser(cursor);
        }
        cursor.close();
        return valve;
    }

    // ==================== SENSORS ====================

    @Override
    public List<Sensor> getAllSensors() {
        List<Sensor> sensors = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.SensorScheduleEntry.TABLE_NAME,
                null, null, null, null, null,
                DatabaseContract.SensorScheduleEntry.COLUMN_NAME + " ASC"
        );

        while (cursor.moveToNext()) {
            sensors.add(cursorToSensor(cursor));
        }
        cursor.close();
        return sensors;
    }

    @Override
    public Sensor getSensorByKks(String kks) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.SensorScheduleEntry.TABLE_NAME,
                null,
                DatabaseContract.SensorScheduleEntry.COLUMN_KKS + " = ?",
                new String[]{kks},
                null, null, null
        );

        Sensor sensor = null;
        if (cursor.moveToFirst()) {
            sensor = cursorToSensor(cursor);
        }
        cursor.close();
        return sensor;
    }

    @Override
    public List<Sensor> searchSensors(String query) {
        List<Sensor> sensors = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.SensorScheduleEntry.COLUMN_NAME + " LIKE ? OR " +
                DatabaseContract.SensorScheduleEntry.COLUMN_KKS + " LIKE ? OR " +
                DatabaseContract.SensorScheduleEntry.COLUMN_FULL_NAME + " LIKE ?";

        String[] args = new String[]{
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%"
        };

        Cursor cursor = db.query(
                DatabaseContract.SensorScheduleEntry.TABLE_NAME,
                null,
                selection,
                args,
                null, null,
                DatabaseContract.SensorScheduleEntry.COLUMN_NAME + " ASC"
        );

        while (cursor.moveToNext()) {
            sensors.add(cursorToSensor(cursor));
        }
        cursor.close();
        return sensors;
    }

    @Override
    public long insertSensor(Sensor sensor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = sensorToContentValues(sensor);
        return db.insert(DatabaseContract.SensorScheduleEntry.TABLE_NAME, null, values);
    }

    @Override
    public int updateSensor(Sensor sensor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = sensorToContentValues(sensor);
        return db.update(
                DatabaseContract.SensorScheduleEntry.TABLE_NAME,
                values,
                DatabaseContract.SensorScheduleEntry.COLUMN_KKS + " = ?",
                new String[]{sensor.getKks()}
        );
    }

    @Override
    public int deleteSensor(String kks) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.SensorScheduleEntry.TABLE_NAME,
                DatabaseContract.SensorScheduleEntry.COLUMN_KKS + " = ?",
                new String[]{kks}
        );
    }

    // ==================== SENSORS USER ====================

    @Override
    public List<Sensor> getCustomSensors() {
        List<Sensor> sensors = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.SensorScheduleUserEntry.TABLE_NAME,
                null, null, null, null, null,
                DatabaseContract.SensorScheduleUserEntry.COLUMN_EDITED_AT + " DESC"
        );

        while (cursor.moveToNext()) {
            sensors.add(cursorToSensorUser(cursor));
        }
        cursor.close();
        return sensors;
    }

    @Override
    public long insertCustomSensor(Sensor sensor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = sensorUserToContentValues(sensor);
        return db.insert(DatabaseContract.SensorScheduleUserEntry.TABLE_NAME, null, values);
    }

    @Override
    public int updateCustomSensor(Sensor sensor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = sensorUserToContentValues(sensor);
        return db.update(
                DatabaseContract.SensorScheduleUserEntry.TABLE_NAME,
                values,
                DatabaseContract.SensorScheduleUserEntry._ID + " = ?",
                new String[]{String.valueOf(sensor.getId())}
        );
    }

    @Override
    public int deleteCustomSensor(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.SensorScheduleUserEntry.TABLE_NAME,
                DatabaseContract.SensorScheduleUserEntry._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    @Override
    public Sensor getCustomSensorByOriginalKks(String originalKks) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.SensorScheduleUserEntry.TABLE_NAME,
                null,
                DatabaseContract.SensorScheduleUserEntry.COLUMN_ORIGINAL_KKS + " = ?",
                new String[]{originalKks},
                null, null, null
        );

        Sensor sensor = null;
        if (cursor.moveToFirst()) {
            sensor = cursorToSensorUser(cursor);
        }
        cursor.close();
        return sensor;
    }

    // ==================== SETPOINTS ====================

    @Override
    public List<Setpoint> getAllSetpoints() {
        List<Setpoint> setpoints = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                null, null, null, null, null,
                DatabaseContract.SetpointScheduleEntry.COLUMN_NAME + " ASC"
        );

        while (cursor.moveToNext()) {
            setpoints.add(cursorToSetpoint(cursor));
        }
        cursor.close();
        return setpoints;
    }

    @Override
    public Setpoint getSetpointById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                null,
                DatabaseContract.SetpointScheduleEntry._ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        Setpoint setpoint = null;
        if (cursor.moveToFirst()) {
            setpoint = cursorToSetpoint(cursor);
        }
        cursor.close();
        return setpoint;
    }

    @Override
    public List<Setpoint> searchSetpoints(String query) {
        List<Setpoint> setpoints = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.SetpointScheduleEntry.COLUMN_NAME + " LIKE ? OR " +
                DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + " LIKE ? OR " +
                DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP + " LIKE ?";

        String[] args = new String[]{
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%"
        };

        Cursor cursor = db.query(
                DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                null,
                selection,
                args,
                null, null,
                DatabaseContract.SetpointScheduleEntry.COLUMN_NAME + " ASC"
        );

        while (cursor.moveToNext()) {
            setpoints.add(cursorToSetpoint(cursor));
        }
        cursor.close();
        return setpoints;
    }

    @Override
    public long insertSetpoint(Setpoint setpoint) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = setpointToContentValues(setpoint);
        return db.insert(DatabaseContract.SetpointScheduleEntry.TABLE_NAME, null, values);
    }

    @Override
    public int updateSetpoint(Setpoint setpoint) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = setpointToContentValues(setpoint);
        return db.update(
                DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                values,
                DatabaseContract.SetpointScheduleEntry._ID + " = ?",
                new String[]{String.valueOf(setpoint.getId())}
        );
    }

    @Override
    public int deleteSetpoint(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                DatabaseContract.SetpointScheduleEntry._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // ==================== SETPOINTS USER ====================

    @Override
    public List<Setpoint> getCustomSetpoints() {
        List<Setpoint> setpoints = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.SetpointScheduleUserEntry.TABLE_NAME,
                null, null, null, null, null,
                DatabaseContract.SetpointScheduleUserEntry.COLUMN_EDITED_AT + " DESC"
        );

        while (cursor.moveToNext()) {
            setpoints.add(cursorToSetpointUser(cursor));
        }
        cursor.close();
        return setpoints;
    }

    @Override
    public long insertCustomSetpoint(Setpoint setpoint) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = setpointUserToContentValues(setpoint);
        return db.insert(DatabaseContract.SetpointScheduleUserEntry.TABLE_NAME, null, values);
    }

    @Override
    public int updateCustomSetpoint(Setpoint setpoint) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = setpointUserToContentValues(setpoint);
        return db.update(
                DatabaseContract.SetpointScheduleUserEntry.TABLE_NAME,
                values,
                DatabaseContract.SetpointScheduleUserEntry._ID + " = ?",
                new String[]{String.valueOf(setpoint.getId())}
        );
    }

    @Override
    public int deleteCustomSetpoint(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.SetpointScheduleUserEntry.TABLE_NAME,
                DatabaseContract.SetpointScheduleUserEntry._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    @Override
    public Setpoint getCustomSetpointByOriginalId(int originalId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.SetpointScheduleUserEntry.TABLE_NAME,
                null,
                DatabaseContract.SetpointScheduleUserEntry.COLUMN_ORIGINAL_ID + " = ?",
                new String[]{String.valueOf(originalId)},
                null, null, null
        );

        Setpoint setpoint = null;
        if (cursor.moveToFirst()) {
            setpoint = cursorToSetpointUser(cursor);
        }
        cursor.close();
        return setpoint;
    }

    // ==================== CONVERTER ====================

    @Override
    public List<ConverterPoint> getConverterPoints(String tableName) {
        List<ConverterPoint> points = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                tableName,
                null, null, null, null, null,
                DatabaseContract.Gr21Entry.COLUMN_SIGNAL_VALUE + " ASC"
        );

        while (cursor.moveToNext()) {
            ConverterPoint point = new ConverterPoint();
            point.setSignalValue(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Gr21Entry.COLUMN_SIGNAL_VALUE)));
            point.setTemperature(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Gr21Entry.COLUMN_TEMPERATURE)));
            points.add(point);
        }
        cursor.close();
        return points;
    }

    @Override
    public double getTemperatureFromResistance(String tableName, double resistance) {
        List<ConverterPoint> points = getConverterPoints(tableName);
        if (points.isEmpty()) return 0;

        // Находим две точки для интерполяции
        ConverterPoint lower = null;
        ConverterPoint upper = null;

        for (ConverterPoint point : points) {
            if (point.getSignalValue() <= resistance) {
                lower = point;
            }
            if (point.getSignalValue() >= resistance && upper == null) {
                upper = point;
            }
        }

        if (lower == null && upper != null) {
            return upper.getTemperature(); // Экстраполяция вниз
        }
        if (lower != null && upper == null) {
            return lower.getTemperature(); // Экстраполяция вверх
        }
        if (lower == null && upper == null) {
            return 0;
        }

        // Линейная интерполяция
        if (lower.getSignalValue() == upper.getSignalValue()) {
            return lower.getTemperature();
        }

        double ratio = (resistance - lower.getSignalValue()) / (upper.getSignalValue() - lower.getSignalValue());
        return lower.getTemperature() + ratio * (upper.getTemperature() - lower.getTemperature());
    }

    @Override
    public List<String> getConverterTableNames() {
        List<String> tables = new ArrayList<>();
        tables.add("gr21");
        tables.add("gr23");
        tables.add("ha");
        tables.add("hk");
        tables.add("tcp50p");
        tables.add("tsm50m");
        return tables;
    }

    // ==================== MEASUREMENTS ====================

    @Override
    public long insertMeasurement(Measurement measurement) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = measurementToContentValues(measurement);
        return db.insert(DatabaseContract.MeasurementsEntry.TABLE_NAME, null, values);
    }

    @Override
    public List<Measurement> getAllMeasurements() {
        List<Measurement> measurements = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.MeasurementsEntry.TABLE_NAME,
                null, null, null, null, null,
                DatabaseContract.MeasurementsEntry.COLUMN_MEASUREMENT_DATE + " DESC"
        );

        while (cursor.moveToNext()) {
            measurements.add(cursorToMeasurement(cursor));
        }
        cursor.close();
        return measurements;
    }

    @Override
    public List<Measurement> getMeasurementsByDate(String date) {
        List<Measurement> measurements = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.MeasurementsEntry.TABLE_NAME,
                null,
                DatabaseContract.MeasurementsEntry.COLUMN_MEASUREMENT_DATE + " = ?",
                new String[]{date},
                null, null,
                DatabaseContract.MeasurementsEntry.COLUMN_CREATED_AT + " DESC"
        );

        while (cursor.moveToNext()) {
            measurements.add(cursorToMeasurement(cursor));
        }
        cursor.close();
        return measurements;
    }

    @Override
    public int deleteMeasurement(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.MeasurementsEntry.TABLE_NAME,
                DatabaseContract.MeasurementsEntry._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // ==================== VALVE WORK SESSIONS ====================

    @Override
    public long insertWorkSession(ValveWorkSession session) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = workSessionToContentValues(session);
        return db.insert(DatabaseContract.ValveWorkSessionsEntry.TABLE_NAME, null, values);
    }

    @Override
    public List<ValveWorkSession> getAllWorkSessions() {
        List<ValveWorkSession> sessions = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.ValveWorkSessionsEntry.TABLE_NAME,
                null, null, null, null, null,
                DatabaseContract.ValveWorkSessionsEntry.COLUMN_SAVE_DATE + " DESC"
        );

        while (cursor.moveToNext()) {
            sessions.add(cursorToWorkSession(cursor));
        }
        cursor.close();
        return sessions;
    }

    @Override
    public int deleteWorkSession(String sessionId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.ValveWorkSessionsEntry.TABLE_NAME,
                DatabaseContract.ValveWorkSessionsEntry.COLUMN_SESSION_ID + " = ?",
                new String[]{sessionId}
        );
    }

    // ==================== VALVE ITEMS ====================

    @Override
    public long insertValveItem(ValveItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = valveItemToContentValues(item);
        return db.insert(DatabaseContract.ValveItemsEntry.TABLE_NAME, null, values);
    }

    @Override
    public List<ValveItem> getValveItemsBySession(String sessionId) {
        List<ValveItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.ValveItemsEntry.TABLE_NAME,
                null,
                DatabaseContract.ValveItemsEntry.COLUMN_PARENT_SESSION_ID + " = ?",
                new String[]{sessionId},
                null, null,
                DatabaseContract.ValveItemsEntry.COLUMN_NAME + " ASC"
        );

        while (cursor.moveToNext()) {
            items.add(cursorToValveItem(cursor));
        }
        cursor.close();
        return items;
    }

    @Override
    public int updateValveItem(ValveItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = valveItemToContentValues(item);
        return db.update(
                DatabaseContract.ValveItemsEntry.TABLE_NAME,
                values,
                DatabaseContract.ValveItemsEntry.COLUMN_ITEM_ID + " = ?",
                new String[]{String.valueOf(item.getItemId())}
        );
    }

    @Override
    public int deleteValveItem(int itemId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.ValveItemsEntry.TABLE_NAME,
                DatabaseContract.ValveItemsEntry.COLUMN_ITEM_ID + " = ?",
                new String[]{String.valueOf(itemId)}
        );
    }

    // ==================== HELPERS: Cursor to Object ====================

    private GateValve cursorToGateValve(Cursor cursor) {
        GateValve valve = new GateValve();
        valve.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry._ID)));
        valve.setNameEng(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_NAME_ENG)));
        valve.setKks(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_KKS)));
        valve.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_NAME)));
        valve.setIsy(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_ISY)));
        valve.setPowerCabinet(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_POWER_CABINET)));
        valve.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_FULL_NAME)));
        valve.setOnPlace(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_ON_PLACE)));
        valve.setAp50(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_AP_50)));
        valve.setMark(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_MARK)));
        valve.setCdaCabinet(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_CDA_CABINET)));
        valve.setCdaCabinetPosition(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_CDA_CABINET_POSITION)));
        valve.setSlot(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_SLOT)));
        valve.setNameSpaceViewOpen(cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_NAME_SPACE_VIEW_OPEN)));
        valve.setDescriptionBlockingOpen(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_DESCRIPTION_BLOCKING_OPEN)));
        valve.setNamespaceViewClose(cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_NAMESPACE_VIEW_CLOSE)));
        valve.setDescriptionBlockingClose(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_DESCRIPTION_BLOCKING_CLOSE)));
        valve.setNamespaceViewPerifer(cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_NAMESPACE_VIEW_PERIFER)));
        valve.setDescriptionBlockingPerifer(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_DESCRIPTION_BLOCKING_PERIFER)));
        return valve;
    }

    private GateValve cursorToGateValveUser(Cursor cursor) {
        GateValve valve = new GateValve();
        valve.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry._ID)));
        valve.setNameEng(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_NAME_ENG)));
        valve.setKks(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_KKS)));
        valve.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_NAME)));
        valve.setIsy(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_ISY)));
        valve.setPowerCabinet(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_POWER_CABINET)));
        valve.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_FULL_NAME)));
        valve.setOnPlace(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_ON_PLACE)));
        valve.setAp50(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_AP_50)));
        valve.setMark(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_MARK)));
        valve.setCdaCabinet(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_CDA_CABINET)));
        valve.setCdaCabinetPosition(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_CDA_CABINET_POSITION)));
        valve.setSlot(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_SLOT)));
        valve.setNameSpaceViewOpen(cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_NAME_SPACE_VIEW_OPEN)));
        valve.setDescriptionBlockingOpen(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_DESCRIPTION_BLOCKING_OPEN)));
        valve.setNamespaceViewClose(cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_NAMESPACE_VIEW_CLOSE)));
        valve.setDescriptionBlockingClose(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_DESCRIPTION_BLOCKING_CLOSE)));
        valve.setNamespaceViewPerifer(cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_NAMESPACE_VIEW_PERIFER)));
        valve.setDescriptionBlockingPerifer(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_DESCRIPTION_BLOCKING_PERIFER)));
        valve.setOriginalId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_ORIGINAL_ID)));
        valve.setEditedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_EDITED_AT)));
        valve.setCustom(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesUserEntry.COLUMN_IS_CUSTOM)) == 1);
        return valve;
    }
    private Sensor cursorToSensorUser(Cursor cursor) {
        Sensor sensor = new Sensor();
        sensor.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry._ID)));
        sensor.setOriginalKks(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_ORIGINAL_KKS)));
        sensor.setKeynum(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_KEYNUM)));
        sensor.setFa(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_FA)));
        sensor.setStMarkir(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_ST_MARKIR)));
        sensor.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_FULL_NAME)));
        sensor.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_NAME)));
        sensor.setMedia(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_MEDIA)));
        sensor.setUnits(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_UNITS)));
        sensor.setNominal(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_NOMINAL)));
        sensor.setVolMin(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_VOL_MIN)));
        sensor.setVolMax(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_VOL_MAX)));
        sensor.setSpeed(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_SPEED)));
        sensor.setFaultPar(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_FAULT_PAR)));
        sensor.setInsteadF(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_INSTEAD_F)));
        sensor.setFilter(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_FILTER)));
        sensor.setModelSensor(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_MODEL_SENSOR)));
        sensor.setModSensor(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_MOD_SENSOR)));
        sensor.setAdditionalInfo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_ADDITIONAL_INFO)));
        sensor.setMinVal(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_MIN_VAL)));
        sensor.setMaxVal(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_MAX_VAL)));
        sensor.setMeasureUnit(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_MEASURE_UNIT)));
        sensor.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_LOCATION)));
        sensor.setCva(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_CVA)));
        sensor.setDampingTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_DAMPING_TIME)));
        sensor.setEditedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_EDITED_AT)));
        sensor.setCustom(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleUserEntry.COLUMN_IS_CUSTOM)) == 1);
        return sensor;
    }

    private Setpoint cursorToSetpointUser(Cursor cursor) {
        Setpoint setpoint = new Setpoint();
        setpoint.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleUserEntry._ID)));
        setpoint.setOriginalId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleUserEntry.COLUMN_ORIGINAL_ID)));
        setpoint.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleUserEntry.COLUMN_NAME)));
        setpoint.setPositionName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleUserEntry.COLUMN_POSITION_NAME)));
        setpoint.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleUserEntry.COLUMN_LOCATION)));
        setpoint.setSetpointValue(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleUserEntry.COLUMN_SETPOINT_VALUE)));
        setpoint.setDelayTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleUserEntry.COLUMN_DELAY_TIME)));
        setpoint.setOperation(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleUserEntry.COLUMN_OPERATION)));
        setpoint.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleUserEntry.COLUMN_NOTES)));
        setpoint.setEquipmentGroup(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleUserEntry.COLUMN_EQUIPMENT_GROUP)));
        setpoint.setEditedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleUserEntry.COLUMN_EDITED_AT)));
        setpoint.setCustom(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleUserEntry.COLUMN_IS_CUSTOM)) == 1);
        return setpoint;
    }

    private Measurement cursorToMeasurement(Cursor cursor) {
        Measurement measurement = new Measurement();
        measurement.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.MeasurementsEntry._ID)));
        measurement.setMeasurementDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MeasurementsEntry.COLUMN_MEASUREMENT_DATE)));
        measurement.setValue(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.MeasurementsEntry.COLUMN_VALUE)));
        measurement.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MeasurementsEntry.COLUMN_UNIT)));
        measurement.setTemperature(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.MeasurementsEntry.COLUMN_TEMPERATURE)));
        measurement.setSensorType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MeasurementsEntry.COLUMN_SENSOR_TYPE)));
        measurement.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MeasurementsEntry.COLUMN_DESCRIPTION)));
        measurement.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MeasurementsEntry.COLUMN_CREATED_AT)));
        return measurement;
    }

    private ValveWorkSession cursorToWorkSession(Cursor cursor) {
        ValveWorkSession session = new ValveWorkSession();
        session.setSessionId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ValveWorkSessionsEntry.COLUMN_SESSION_ID)));
        session.setSaveDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ValveWorkSessionsEntry.COLUMN_SAVE_DATE)));
        session.setEquipmentDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ValveWorkSessionsEntry.COLUMN_EQUIPMENT_DESCRIPTION)));
        return session;
    }

    private ValveItem cursorToValveItem(Cursor cursor) {
        ValveItem item = new ValveItem();
        item.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_ITEM_ID)));
        item.setParentSessionId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_PARENT_SESSION_ID)));
        item.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_NAME)));
        item.setNameEng(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_NAME_ENG)));
        item.setIsy(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_ISY)));
        item.setHasMotor(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_HAS_MOTOR)) == 1);
        item.setAssembled(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_IS_ASSEMBLED)) == 1);
        item.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_IS_CHECKED)) == 1);
        item.setOperationTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_OPERATION_TIMESTAMP)));
        return item;
    }

    private Sensor cursorToSensor(Cursor cursor) {
        Sensor sensor = new Sensor();
        sensor.setKeynum(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_KEYNUM)));
        sensor.setFa(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_FA)));
        sensor.setKks(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_KKS)));
        sensor.setStMarkir(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR)));
        sensor.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_FULL_NAME)));
        sensor.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_NAME)));
        sensor.setMedia(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_MEDIA)));
        sensor.setUnits(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_UNITS)));
        sensor.setNominal(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_NOMINAL)));
        sensor.setVolMin(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_VOL_MIN)));
        sensor.setVolMax(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_VOL_MAX)));
        sensor.setSpeed(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_SPEED)));
        sensor.setFaultPar(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_FAULT_PAR)));
        sensor.setInsteadF(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_INSTEAD_F)));
        sensor.setFilter(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_FILTER)));
        sensor.setModelSensor(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_MODEL_SENSOR)));
        sensor.setModSensor(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_MOD_SENSOR)));
        sensor.setAdditionalInfo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_ADDITIONAL_INFO)));
        sensor.setMinVal(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_MIN)));
        sensor.setMaxVal(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_MAX)));
        sensor.setMeasureUnit(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_MEASURE_UNIT)));
        sensor.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_LOCATION)));
        sensor.setCva(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_CVA)));
        sensor.setDampingTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_DAMPING_TIME)));
        return sensor;
    }

    // ==================== HELPERS: Object to ContentValues ====================

    private ContentValues gateValveToContentValues(GateValve valve) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.GateValvesEntry.COLUMN_NAME_ENG, valve.getNameEng());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_KKS, valve.getKks());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_NAME, valve.getName());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_ISY, valve.getIsy());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_POWER_CABINET, valve.getPowerCabinet());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_FULL_NAME, valve.getFullName());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_ON_PLACE, valve.getOnPlace());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_AP_50, valve.getAp50());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_MARK, valve.getMark());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_CDA_CABINET, valve.getCdaCabinet());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_CDA_CABINET_POSITION, valve.getCdaCabinetPosition());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_SLOT, valve.getSlot());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_NAME_SPACE_VIEW_OPEN, valve.getNameSpaceViewOpen());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_DESCRIPTION_BLOCKING_OPEN, valve.getDescriptionBlockingOpen());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_NAMESPACE_VIEW_CLOSE, valve.getNamespaceViewClose());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_DESCRIPTION_BLOCKING_CLOSE, valve.getDescriptionBlockingClose());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_NAMESPACE_VIEW_PERIFER, valve.getNamespaceViewPerifer());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_DESCRIPTION_BLOCKING_PERIFER, valve.getDescriptionBlockingPerifer());
        return values;
    }

    private ContentValues gateValveUserToContentValues(GateValve valve) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_NAME_ENG, valve.getNameEng());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_KKS, valve.getKks());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_NAME, valve.getName());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_ISY, valve.getIsy());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_POWER_CABINET, valve.getPowerCabinet());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_FULL_NAME, valve.getFullName());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_ON_PLACE, valve.getOnPlace());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_AP_50, valve.getAp50());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_MARK, valve.getMark());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_CDA_CABINET, valve.getCdaCabinet());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_CDA_CABINET_POSITION, valve.getCdaCabinetPosition());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_SLOT, valve.getSlot());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_NAME_SPACE_VIEW_OPEN, valve.getNameSpaceViewOpen());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_DESCRIPTION_BLOCKING_OPEN, valve.getDescriptionBlockingOpen());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_NAMESPACE_VIEW_CLOSE, valve.getNamespaceViewClose());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_DESCRIPTION_BLOCKING_CLOSE, valve.getDescriptionBlockingClose());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_NAMESPACE_VIEW_PERIFER, valve.getNamespaceViewPerifer());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_DESCRIPTION_BLOCKING_PERIFER, valve.getDescriptionBlockingPerifer());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_ORIGINAL_ID, valve.getOriginalId());
        values.put(DatabaseContract.GateValvesUserEntry.COLUMN_IS_CUSTOM, valve.isCustom() ? 1 : 0);
        return values;
    }

    private ContentValues sensorToContentValues(Sensor sensor) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_KEYNUM, sensor.getKeynum());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_FA, sensor.getFa());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_KKS, sensor.getKks());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR, sensor.getStMarkir());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_FULL_NAME, sensor.getFullName());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_NAME, sensor.getName());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_MEDIA, sensor.getMedia());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_UNITS, sensor.getUnits());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_NOMINAL, sensor.getNominal());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_VOL_MIN, sensor.getVolMin());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_VOL_MAX, sensor.getVolMax());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_SPEED, sensor.getSpeed());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_FAULT_PAR, sensor.getFaultPar());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_INSTEAD_F, sensor.getInsteadF());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_FILTER, sensor.getFilter());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_MODEL_SENSOR, sensor.getModelSensor());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_MOD_SENSOR, sensor.getModSensor());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_ADDITIONAL_INFO, sensor.getAdditionalInfo());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_MIN, sensor.getMinVal());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_MAX, sensor.getMaxVal());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_MEASURE_UNIT, sensor.getMeasureUnit());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_LOCATION, sensor.getLocation());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_CVA, sensor.getCva());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_DAMPING_TIME, sensor.getDampingTime());
        return values;
    }

    private ContentValues sensorUserToContentValues(Sensor sensor) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_ORIGINAL_KKS, sensor.getOriginalKks());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_KEYNUM, sensor.getKeynum());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_FA, sensor.getFa());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_ST_MARKIR, sensor.getStMarkir());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_FULL_NAME, sensor.getFullName());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_NAME, sensor.getName());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_MEDIA, sensor.getMedia());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_UNITS, sensor.getUnits());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_NOMINAL, sensor.getNominal());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_VOL_MIN, sensor.getVolMin());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_VOL_MAX, sensor.getVolMax());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_SPEED, sensor.getSpeed());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_FAULT_PAR, sensor.getFaultPar());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_INSTEAD_F, sensor.getInsteadF());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_FILTER, sensor.getFilter());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_MODEL_SENSOR, sensor.getModelSensor());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_MOD_SENSOR, sensor.getModSensor());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_ADDITIONAL_INFO, sensor.getAdditionalInfo());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_MIN_VAL, sensor.getMinVal());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_MAX_VAL, sensor.getMaxVal());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_MEASURE_UNIT, sensor.getMeasureUnit());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_LOCATION, sensor.getLocation());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_CVA, sensor.getCva());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_DAMPING_TIME, sensor.getDampingTime());
        values.put(DatabaseContract.SensorScheduleUserEntry.COLUMN_IS_CUSTOM, sensor.isCustom() ? 1 : 0);
        return values;
    }

    private ContentValues setpointToContentValues(Setpoint setpoint) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SetpointScheduleEntry.COLUMN_NAME, setpoint.getName());
        values.put(DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME, setpoint.getPositionName());
        values.put(DatabaseContract.SetpointScheduleEntry.COLUMN_LOCATION, setpoint.getLocation());
        values.put(DatabaseContract.SetpointScheduleEntry.COLUMN_SETPOINT_VALUE, setpoint.getSetpointValue());
        values.put(DatabaseContract.SetpointScheduleEntry.COLUMN_DELAY_TIME, setpoint.getDelayTime());
        values.put(DatabaseContract.SetpointScheduleEntry.COLUMN_OPERATION, setpoint.getOperation());
        values.put(DatabaseContract.SetpointScheduleEntry.COLUMN_NOTES, setpoint.getNotes());
        values.put(DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP, setpoint.getEquipmentGroup());
        return values;
    }

    private ContentValues setpointUserToContentValues(Setpoint setpoint) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SetpointScheduleUserEntry.COLUMN_ORIGINAL_ID, setpoint.getOriginalId());
        values.put(DatabaseContract.SetpointScheduleUserEntry.COLUMN_NAME, setpoint.getName());
        values.put(DatabaseContract.SetpointScheduleUserEntry.COLUMN_POSITION_NAME, setpoint.getPositionName());
        values.put(DatabaseContract.SetpointScheduleUserEntry.COLUMN_LOCATION, setpoint.getLocation());
        values.put(DatabaseContract.SetpointScheduleUserEntry.COLUMN_SETPOINT_VALUE, setpoint.getSetpointValue());
        values.put(DatabaseContract.SetpointScheduleUserEntry.COLUMN_DELAY_TIME, setpoint.getDelayTime());
        values.put(DatabaseContract.SetpointScheduleUserEntry.COLUMN_OPERATION, setpoint.getOperation());
        values.put(DatabaseContract.SetpointScheduleUserEntry.COLUMN_NOTES, setpoint.getNotes());
        values.put(DatabaseContract.SetpointScheduleUserEntry.COLUMN_EQUIPMENT_GROUP, setpoint.getEquipmentGroup());
        values.put(DatabaseContract.SetpointScheduleUserEntry.COLUMN_IS_CUSTOM, setpoint.isCustom() ? 1 : 0);
        return values;
    }

    private ContentValues measurementToContentValues(Measurement measurement) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.MeasurementsEntry.COLUMN_MEASUREMENT_DATE, measurement.getMeasurementDate());
        values.put(DatabaseContract.MeasurementsEntry.COLUMN_VALUE, measurement.getValue());
        values.put(DatabaseContract.MeasurementsEntry.COLUMN_UNIT, measurement.getUnit());
        values.put(DatabaseContract.MeasurementsEntry.COLUMN_TEMPERATURE, measurement.getTemperature());
        values.put(DatabaseContract.MeasurementsEntry.COLUMN_SENSOR_TYPE, measurement.getSensorType());
        values.put(DatabaseContract.MeasurementsEntry.COLUMN_DESCRIPTION, measurement.getDescription());
        return values;
    }

    private ContentValues workSessionToContentValues(ValveWorkSession session) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ValveWorkSessionsEntry.COLUMN_SESSION_ID, session.getSessionId());
        values.put(DatabaseContract.ValveWorkSessionsEntry.COLUMN_SAVE_DATE, session.getSaveDate());
        values.put(DatabaseContract.ValveWorkSessionsEntry.COLUMN_EQUIPMENT_DESCRIPTION, session.getEquipmentDescription());
        return values;
    }

    private ContentValues valveItemToContentValues(ValveItem item) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_PARENT_SESSION_ID, item.getParentSessionId());
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_NAME, item.getName());
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_NAME_ENG, item.getNameEng());
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_ISY, item.getIsy());
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_HAS_MOTOR, item.isHasMotor() ? 1 : 0);
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_IS_ASSEMBLED, item.isAssembled() ? 1 : 0);
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_IS_CHECKED, item.isChecked() ? 1 : 0);
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_OPERATION_TIMESTAMP, item.getOperationTimestamp());
        return values;
    }

    // ==================== HELPERS: Cursor to Object (недостающие) ====================

    private Setpoint cursorToSetpoint(Cursor cursor) {
        Setpoint setpoint = new Setpoint();
        setpoint.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry._ID)));
        setpoint.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry.COLUMN_NAME)));
        setpoint.setPositionName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME)));
        setpoint.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry.COLUMN_LOCATION)));
        setpoint.setSetpointValue(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry.COLUMN_SETPOINT_VALUE)));
        setpoint.setDelayTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry.COLUMN_DELAY_TIME)));
        setpoint.setOperation(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry.COLUMN_OPERATION)));
        setpoint.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry.COLUMN_NOTES)));
        setpoint.setEquipmentGroup(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP)));
        return setpoint;
    }

}