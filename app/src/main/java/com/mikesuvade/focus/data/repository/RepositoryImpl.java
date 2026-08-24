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
import java.text.SimpleDateFormat;      // ← ДОБАВИТЬ
import java.util.ArrayList;             // ← ДОБАВИТЬ
import java.util.Date;                  // ← ДОБАВИТЬ
import java.util.List;                  // ← ДОБАВИТЬ
import java.util.Locale;

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

        String cleanQuery = query.replaceAll("[\\s-]", "");
        String prefixQuery = query + "%";

        String selection =
                "(LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_NAME + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_KKS + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_ISY + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_POWER_CABINET + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_FULL_NAME + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_ON_PLACE + ") LIKE LOWER(?)" +
                        ") OR " +
                        "(LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_NAME + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_KKS + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_ISY + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_POWER_CABINET + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_FULL_NAME + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_ON_PLACE + ") LIKE LOWER(?)" +
                        ") OR " +
                        "(LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_NAME + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_KKS + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_ISY + ") LIKE LOWER(?)" +
                        ")";

        String[] args = new String[]{
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + cleanQuery + "%",
                "%" + cleanQuery + "%",
                "%" + cleanQuery + "%",
                "%" + cleanQuery + "%",
                "%" + cleanQuery + "%",
                "%" + cleanQuery + "%",
                prefixQuery,
                prefixQuery,
                prefixQuery
        };

        Log.d("SEARCH", "Запрос: " + query + " | clean: " + cleanQuery + " | prefix: " + prefixQuery);

        Cursor cursor = db.query(
                DatabaseContract.GateValvesEntry.TABLE_NAME,
                null,
                selection,
                args,
                null, null,
                "LENGTH(" + DatabaseContract.GateValvesEntry.COLUMN_NAME + ") ASC, " +
                        DatabaseContract.GateValvesEntry.COLUMN_NAME + " ASC"
        );

        Log.d("SEARCH", "Количество найденных записей: " + cursor.getCount());

        while (cursor.moveToNext()) {
            valves.add(cursorToGateValve(cursor));
        }
        cursor.close();

        Log.d("SEARCH", "Возвращено объектов: " + valves.size());
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
            return upper.getTemperature();
        }
        if (lower != null && upper == null) {
            return lower.getTemperature();
        }
        if (lower == null && upper == null) {
            return 0;
        }

        if (lower.getSignalValue() == upper.getSignalValue()) {
            return lower.getTemperature();
        }

        double ratio = (resistance - lower.getSignalValue()) / (upper.getSignalValue() - lower.getSignalValue());
        return lower.getTemperature() + ratio * (upper.getTemperature() - lower.getTemperature());
    }
    @Override
    public double getSignalFromTemperature(String tableName, double temperature) {
        List<ConverterPoint> points = getConverterPoints(tableName);
        if (points.isEmpty()) return 0;

        ConverterPoint lower = null;
        ConverterPoint upper = null;

        for (ConverterPoint point : points) {
            if (point.getTemperature() <= temperature) {
                lower = point;
            }
            if (point.getTemperature() >= temperature && upper == null) {
                upper = point;
            }
        }

        if (lower == null && upper != null) return upper.getSignalValue();
        if (lower != null && upper == null) return lower.getSignalValue();
        if (lower == null && upper == null) return 0;

        if (lower.getTemperature() == upper.getTemperature()) {
            return lower.getSignalValue();
        }

        double ratio = (temperature - lower.getTemperature()) / (upper.getTemperature() - lower.getTemperature());
        return lower.getSignalValue() + ratio * (upper.getSignalValue() - lower.getSignalValue());
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
        Log.d("TEMP_DEBUG", "=== insertMeasurement() START ===");
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = measurementToContentValues(measurement);

            Log.d("TEMP_DEBUG", "ContentValues: " + values.toString());

            long result = db.insert(DatabaseContract.MeasurementsEntry.TABLE_NAME, null, values);
            Log.d("TEMP_DEBUG", "insert result: " + result);

            if (result == -1) {
                Log.e("TEMP_DEBUG", "Insert failed! Check table structure and column names.");
            }
            return result;
        } catch (Exception e) {
            Log.e("TEMP_DEBUG", "ERROR in insertMeasurement", e);
            throw e;
        }
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
            ValveWorkSession session = cursorToWorkSession(cursor);
            Log.d("MYTITLE", "Loaded session: id=" + session.getSessionId() + ", name=" + session.getEquipmentDescription());
            sessions.add(session);
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
                DatabaseContract.ValveItemsEntry.COLUMN_ITEM_ID + " ASC"
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

        String isy = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_ISY));
        if (isy != null && isy.endsWith(".0")) {
            isy = isy.substring(0, isy.length() - 2);
        }
        valve.setIsy(isy);

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
        valve.setLocationDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_LOCATION_DESCRIPTION)));
        valve.setIsEdited(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_IS_EDITED)));
        valve.setEditedAtValve(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_EDITED_AT)));  // ← ТОЛЬКО ОДНА СТРОКА!
        return valve;
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

    // В RepositoryImpl.java

    private ValveItem cursorToValveItem(Cursor cursor) {
        ValveItem item = new ValveItem();
        item.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_ITEM_ID)));
        item.setParentSessionId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_PARENT_SESSION_ID)));
        item.setGateValveId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_GATE_VALVE_ID)));
        item.setIsAssembled(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_IS_ASSEMBLED)));
        item.setMotorDisabled(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_MOTOR_DISABLED)));
        item.setBoxRemoved(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_BOX_REMOVED)));
        item.setIsChecked(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_IS_CHECKED)));
        item.setCheckedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_CHECKED_AT)));
        item.setOperationTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ValveItemsEntry.COLUMN_OPERATION_TIMESTAMP)));
        return item;
    }


// В RepositoryImpl.java - УДАЛЯЕМ ДУБЛИКАТ

    // Оставляем ОДИН метод valveItemToContentValues (правильный):
    private ContentValues valveItemToContentValues(ValveItem item) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_PARENT_SESSION_ID, item.getParentSessionId());
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_GATE_VALVE_ID, item.getGateValveId());
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_IS_ASSEMBLED, item.getIsAssembled());
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_MOTOR_DISABLED, item.getMotorDisabled());
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_BOX_REMOVED, item.getBoxRemoved());
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_IS_CHECKED, item.getIsChecked());
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_CHECKED_AT, item.getCheckedAt());
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_OPERATION_TIMESTAMP, item.getOperationTimestamp());
        return values;
    }
    // ==================== HELPERS: Object to ContentValues ====================

    private ContentValues gateValveToContentValues(GateValve valve) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.GateValvesEntry.COLUMN_NAME_ENG, valve.getNameEng());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_KKS, valve.getKks());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_NAME, valve.getName());

        String isy = valve.getIsy();
        if (isy != null && isy.endsWith(".0")) {
            isy = isy.substring(0, isy.length() - 2);
        }
        values.put(DatabaseContract.GateValvesEntry.COLUMN_ISY, isy);

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
        values.put(DatabaseContract.GateValvesEntry.COLUMN_LOCATION_DESCRIPTION, valve.getLocationDescription());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_IS_EDITED, valve.getIsEdited());

        // ==========================================
        // 🔧 ТОЛЬКО ОДНА СТРОКА — ПРАВИЛЬНАЯ!
        // ==========================================
        values.put(DatabaseContract.GateValvesEntry.COLUMN_EDITED_AT, valve.getEditedAtValve());

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

    private ContentValues measurementToContentValues(Measurement measurement) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.MeasurementsEntry.COLUMN_MEASUREMENT_DATE, measurement.getMeasurementDate());
        values.put(DatabaseContract.MeasurementsEntry.COLUMN_VALUE, measurement.getValue());
        values.put(DatabaseContract.MeasurementsEntry.COLUMN_UNIT, measurement.getUnit());
        values.put(DatabaseContract.MeasurementsEntry.COLUMN_TEMPERATURE, measurement.getTemperature());
        values.put(DatabaseContract.MeasurementsEntry.COLUMN_SENSOR_TYPE, measurement.getSensorType());
        values.put(DatabaseContract.MeasurementsEntry.COLUMN_DESCRIPTION, measurement.getDescription());

        // ✅ ДОБАВЛЯЕМ created_at!
        values.put(DatabaseContract.MeasurementsEntry.COLUMN_CREATED_AT, measurement.getCreatedAt());

        return values;
    }

    private ContentValues workSessionToContentValues(ValveWorkSession session) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ValveWorkSessionsEntry.COLUMN_SESSION_ID, session.getSessionId());
        values.put(DatabaseContract.ValveWorkSessionsEntry.COLUMN_SAVE_DATE, session.getSaveDate());
        values.put(DatabaseContract.ValveWorkSessionsEntry.COLUMN_EQUIPMENT_DESCRIPTION, session.getEquipmentDescription());
        return values;
    }



    @Override
    public int updateValveItemStatus(int itemId, int isAssembled, int motorDisabled, int boxRemoved) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_IS_ASSEMBLED, isAssembled);
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_MOTOR_DISABLED, motorDisabled);
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_BOX_REMOVED, boxRemoved);
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_OPERATION_TIMESTAMP,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        return db.update(
                DatabaseContract.ValveItemsEntry.TABLE_NAME,
                values,
                DatabaseContract.ValveItemsEntry.COLUMN_ITEM_ID + " = ?",
                new String[]{String.valueOf(itemId)}
        );
    }

    @Override
    public int updateValveItemChecked(int itemId, int isChecked, String checkedAt) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_IS_CHECKED, isChecked);
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_CHECKED_AT, checkedAt);
        values.put(DatabaseContract.ValveItemsEntry.COLUMN_OPERATION_TIMESTAMP,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        return db.update(
                DatabaseContract.ValveItemsEntry.TABLE_NAME,
                values,
                DatabaseContract.ValveItemsEntry.COLUMN_ITEM_ID + " = ?",
                new String[]{String.valueOf(itemId)}
        );
    }
    // В RepositoryImpl.java

    @Override
    public int updateWorkSession(ValveWorkSession session) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ValveWorkSessionsEntry.COLUMN_SAVE_DATE, session.getSaveDate());
        values.put(DatabaseContract.ValveWorkSessionsEntry.COLUMN_EQUIPMENT_DESCRIPTION, session.getEquipmentDescription());

        return db.update(
                DatabaseContract.ValveWorkSessionsEntry.TABLE_NAME,
                values,
                DatabaseContract.ValveWorkSessionsEntry.COLUMN_SESSION_ID + " = ?",
                new String[]{session.getSessionId()}
        );
    }
    @Override
    public int updateWorkSessionDate(String sessionId, String saveDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ValveWorkSessionsEntry.COLUMN_SAVE_DATE, saveDate);
        // ❌ НЕ ТРОГАЕМ COLUMN_EQUIPMENT_DESCRIPTION!

        return db.update(
                DatabaseContract.ValveWorkSessionsEntry.TABLE_NAME,
                values,
                DatabaseContract.ValveWorkSessionsEntry.COLUMN_SESSION_ID + " = ?",
                new String[]{sessionId}
        );
    }
    @Override
    public ValveWorkSession getWorkSessionById(String sessionId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.ValveWorkSessionsEntry.TABLE_NAME,
                null,
                DatabaseContract.ValveWorkSessionsEntry.COLUMN_SESSION_ID + " = ?",
                new String[]{sessionId},
                null, null, null
        );

        ValveWorkSession session = null;
        if (cursor.moveToFirst()) {
            session = cursorToWorkSession(cursor);
            Log.d("MYTITLE", "getWorkSessionById: found session, name=" + session.getEquipmentDescription());
        } else {
            Log.d("MYTITLE", "getWorkSessionById: session NOT found for id=" + sessionId);
        }
        cursor.close();
        return session;
    }
    @Override
    public int updateWorkSessionName(String sessionId, String newName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ValveWorkSessionsEntry.COLUMN_EQUIPMENT_DESCRIPTION, newName);
        // ❌ НЕ ТРОГАЕМ COLUMN_SAVE_DATE!

        return db.update(
                DatabaseContract.ValveWorkSessionsEntry.TABLE_NAME,
                values,
                DatabaseContract.ValveWorkSessionsEntry.COLUMN_SESSION_ID + " = ?",
                new String[]{sessionId}
        );
    }
    @Override
    public int updateMeasurementDescription(int id, String description) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.MeasurementsEntry.COLUMN_DESCRIPTION, description);

        return db.update(
                DatabaseContract.MeasurementsEntry.TABLE_NAME,
                values,
                DatabaseContract.MeasurementsEntry._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

}