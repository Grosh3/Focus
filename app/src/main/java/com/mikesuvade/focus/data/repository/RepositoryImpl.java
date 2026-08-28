package com.mikesuvade.focus.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mikesuvade.focus.data.database.DatabaseContract;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RepositoryImpl implements IRepository {

    private static final String TAG = "RepositoryImpl";
    private final DatabaseHelper dbHelper;
    private final UserDatabaseHelper userDbHelper;

    public RepositoryImpl(DatabaseHelper dbHelper, UserDatabaseHelper userDbHelper) {
        this.dbHelper = dbHelper;
        this.userDbHelper = userDbHelper;
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

        Cursor cursor = db.query(
                DatabaseContract.GateValvesEntry.TABLE_NAME,
                null,
                selection,
                args,
                null, null,
                "LENGTH(" + DatabaseContract.GateValvesEntry.COLUMN_NAME + ") ASC, " +
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

    // ==================== SENSORS ====================

    @Override
    public List<Sensor> getAllSensors() {
        List<Sensor> sensors = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.SensorScheduleEntry.TABLE_NAME,
                null, null, null, null, null,
                DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + " ASC"
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

        String cleanQuery = query.replaceAll("[\\s\\-.]", "");

        String selection =
                "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_FULL_NAME + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_LOCATION + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_NAME + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_KKS + ") LIKE LOWER(?)";

        String[] args = new String[]{
                "%" + cleanQuery + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%"
        };

        String kksWithoutPrefix = query.replaceAll("^[0-9]{2,3}", "");
        if (!kksWithoutPrefix.equals(query) && !kksWithoutPrefix.isEmpty()) {
            selection += " OR LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_KKS + ") LIKE LOWER(?)";
            String[] newArgs = new String[args.length + 1];
            System.arraycopy(args, 0, newArgs, 0, args.length);
            newArgs[args.length] = "%" + kksWithoutPrefix + "%";
            args = newArgs;
        }

        Cursor cursor = db.query(
                DatabaseContract.SensorScheduleEntry.TABLE_NAME,
                null,
                selection,
                args,
                null, null,
                DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + " ASC"
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
                DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + " ASC"
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

        String cleanQuery = query.replaceAll("[\\s\\-.]", "");

        String selection =
                "(LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_NAME + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_SETPOINT_VALUE + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_OPERATION + ") LIKE LOWER(?) OR " +
                        "LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_LOCATION + ") LIKE LOWER(?)" +
                        ") OR " +
                        "(LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + ") LIKE LOWER(?))";

        String[] args = new String[]{
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + cleanQuery + "%"
        };

        Cursor cursor = db.query(
                DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                null,
                selection,
                args,
                null, null,
                DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + " ASC"
        );

        while (cursor.moveToNext()) {
            setpoints.add(cursorToSetpoint(cursor));
        }
        cursor.close();
        return setpoints;
    }

    @Override
    public List<Setpoint> searchSetpointsByGroup(String group) {
        List<Setpoint> setpoints = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String cleanGroup = group.replaceAll("\\s+", "");

        String selection =
                "REPLACE(LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP + "), ' ', '') LIKE LOWER(?)";

        String[] args = new String[]{
                "%" + cleanGroup + "%"
        };

        Cursor cursor = db.query(
                DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                null,
                selection,
                args,
                null, null,
                DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + " ASC"
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

    // ==================== HELPERS ====================

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
        valve.setEditedAtValve(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_EDITED_AT)));
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
        setpoint.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
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

    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД - GATE VALVES
    // ==========================================

    @Override
    public List<GateValve> getAllUserGateValves() {
        List<GateValve> valves = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_gate_valves",
                null, null, null, null, null,
                "name ASC"
        );

        while (cursor.moveToNext()) {
            valves.add(cursorToUserGateValve(cursor));
        }
        cursor.close();
        return valves;
    }

    @Override
    public GateValve getUserGateValveByOriginalId(int originalId) {
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_gate_valves",
                null,
                "original_id = ?",
                new String[]{String.valueOf(originalId)},
                null, null, null
        );

        GateValve valve = null;
        if (cursor.moveToFirst()) {
            valve = cursorToUserGateValve(cursor);
        }
        cursor.close();
        return valve;
    }

    @Override
    public List<GateValve> searchUserGateValves(String query) {
        List<GateValve> valves = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();

        String cleanQuery = query.replaceAll("[\\s-]", "");
        String prefixQuery = query + "%";

        String selection =
                "(LOWER(name) LIKE LOWER(?) OR " +
                        "LOWER(kks) LIKE LOWER(?) OR " +
                        "LOWER(isy) LIKE LOWER(?) OR " +
                        "LOWER(full_name) LIKE LOWER(?) OR " +
                        "LOWER(on_place) LIKE LOWER(?)" +
                        ") OR " +
                        "(LOWER(name) LIKE LOWER(?) OR " +
                        "LOWER(kks) LIKE LOWER(?) OR " +
                        "LOWER(isy) LIKE LOWER(?)" +
                        ")";

        String[] args = new String[]{
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + cleanQuery + "%",
                "%" + cleanQuery + "%",
                "%" + cleanQuery + "%"
        };

        Cursor cursor = db.query(
                "user_gate_valves",
                null,
                selection,
                args,
                null, null,
                "name ASC"
        );

        while (cursor.moveToNext()) {
            valves.add(cursorToUserGateValve(cursor));
        }
        cursor.close();
        return valves;
    }

    @Override
    public long insertUserGateValve(GateValve valve) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userGateValveToContentValues(valve);
        return db.insert("user_gate_valves", null, values);
    }

    @Override
    public int updateUserGateValve(GateValve valve) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userGateValveToContentValues(valve);
        return db.update(
                "user_gate_valves",
                values,
                "original_id = ?",
                new String[]{String.valueOf(valve.getOriginalId())}
        );
    }

    @Override
    public int deleteUserGateValve(int id) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        return db.delete(
                "user_gate_valves",
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    @Override
    public void copyGateValveToUser(int originalId) {
        GateValve existing = getUserGateValveByOriginalId(originalId);
        if (existing != null) return;

        GateValve ref = getGateValveById(originalId);
        if (ref == null) return;

        GateValve copy = new GateValve();
        copy.setOriginalId(ref.getId());
        copy.setNameEng(ref.getNameEng());
        copy.setKks(ref.getKks());
        copy.setName(ref.getName());
        copy.setIsy(ref.getIsy());
        copy.setPowerCabinet(ref.getPowerCabinet());
        copy.setFullName(ref.getFullName());
        copy.setOnPlace(ref.getOnPlace());
        copy.setAp50(ref.getAp50());
        copy.setMark(ref.getMark());
        copy.setCdaCabinet(ref.getCdaCabinet());
        copy.setCdaCabinetPosition(ref.getCdaCabinetPosition());
        copy.setSlot(ref.getSlot());
        copy.setDescriptionBlockingOpen(ref.getDescriptionBlockingOpen());
        copy.setDescriptionBlockingClose(ref.getDescriptionBlockingClose());
        copy.setDescriptionBlockingPerifer(ref.getDescriptionBlockingPerifer());
        copy.setLocationDescription(ref.getLocationDescription());
        copy.setIsEdited(1);
        copy.setEditedAt(getCurrentDateTime());
        copy.setCreatedAt(getCurrentDateTime());

        insertUserGateValve(copy);
    }

    @Override
    public void markGateValveAsDeleted(int originalId) {
        GateValve existing = getUserGateValveByOriginalId(originalId);

        if (existing != null) {
            existing.setIsDeleted(1);
            existing.setEditedAt(getCurrentDateTime());
            updateUserGateValve(existing);
        } else {
            GateValve ref = getGateValveById(originalId);
            if (ref == null) return;

            GateValve copy = new GateValve();
            copy.setOriginalId(ref.getId());
            copy.setIsDeleted(1);
            copy.setNameEng(ref.getNameEng());
            copy.setKks(ref.getKks());
            copy.setName(ref.getName());
            copy.setIsy(ref.getIsy());
            copy.setPowerCabinet(ref.getPowerCabinet());
            copy.setFullName(ref.getFullName());
            copy.setOnPlace(ref.getOnPlace());
            copy.setAp50(ref.getAp50());
            copy.setMark(ref.getMark());
            copy.setCdaCabinet(ref.getCdaCabinet());
            copy.setCdaCabinetPosition(ref.getCdaCabinetPosition());
            copy.setSlot(ref.getSlot());
            copy.setDescriptionBlockingOpen(ref.getDescriptionBlockingOpen());
            copy.setDescriptionBlockingClose(ref.getDescriptionBlockingClose());
            copy.setDescriptionBlockingPerifer(ref.getDescriptionBlockingPerifer());
            copy.setLocationDescription(ref.getLocationDescription());
            copy.setIsEdited(1);
            copy.setEditedAt(getCurrentDateTime());
            copy.setCreatedAt(getCurrentDateTime());

            insertUserGateValve(copy);
        }
    }

    @Override
    public List<GateValve> getAllGateValvesWithUser() {
        List<GateValve> result = new ArrayList<>();

        List<GateValve> refs = getAllGateValves();

        List<GateValve> users = new ArrayList<>();
        for (GateValve u : getAllUserGateValves()) {
            if (u.getIsDeleted() != 1) {
                users.add(u);
            }
        }

        Map<Integer, GateValve> userMap = new HashMap<>();
        for (GateValve u : users) {
            userMap.put(u.getOriginalId(), u);
        }

        for (GateValve ref : refs) {
            GateValve deleted = getUserGateValveByOriginalId(ref.getId());
            if (deleted != null && deleted.getIsDeleted() == 1) {
                continue;
            }

            if (userMap.containsKey(ref.getId())) {
                GateValve merged = userMap.get(ref.getId());
                merged.setNameSpaceViewOpen(ref.getNameSpaceViewOpen());
                merged.setNamespaceViewClose(ref.getNamespaceViewClose());
                merged.setNamespaceViewPerifer(ref.getNamespaceViewPerifer());
                result.add(merged);
            } else {
                result.add(ref);
            }
        }

        return result;
    }

    @Override
    public List<GateValve> searchGateValvesWithUser(String query) {
        List<GateValve> userResults = searchUserGateValves(query);
        List<GateValve> filteredUser = new ArrayList<>();
        for (GateValve u : userResults) {
            if (u.getIsDeleted() != 1) {
                filteredUser.add(u);
            }
        }

        if (!filteredUser.isEmpty()) {
            for (GateValve u : filteredUser) {
                GateValve ref = getGateValveById(u.getOriginalId());
                if (ref != null) {
                    u.setNameSpaceViewOpen(ref.getNameSpaceViewOpen());
                    u.setNamespaceViewClose(ref.getNamespaceViewClose());
                    u.setNamespaceViewPerifer(ref.getNamespaceViewPerifer());
                }
            }
            return filteredUser;
        }

        List<GateValve> refResults = searchGateValves(query);

        List<GateValve> result = new ArrayList<>();
        for (GateValve ref : refResults) {
            GateValve deleted = getUserGateValveByOriginalId(ref.getId());
            if (deleted == null || deleted.getIsDeleted() != 1) {
                result.add(ref);
            }
        }

        return result;
    }

    // ==========================================
    // 🟢 ХЕЛПЕРЫ - USER GATE VALVES
    // ==========================================

    private GateValve cursorToUserGateValve(Cursor cursor) {
        GateValve valve = new GateValve();
        valve.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        valve.setOriginalId(cursor.getInt(cursor.getColumnIndexOrThrow("original_id")));
        valve.setIsDeleted(cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")));
        valve.setNameEng(cursor.getString(cursor.getColumnIndexOrThrow("name_eng")));
        valve.setKks(cursor.getString(cursor.getColumnIndexOrThrow("kks")));
        valve.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));

        String isy = cursor.getString(cursor.getColumnIndexOrThrow("isy"));
        if (isy != null && isy.endsWith(".0")) {
            isy = isy.substring(0, isy.length() - 2);
        }
        valve.setIsy(isy);

        valve.setPowerCabinet(cursor.getString(cursor.getColumnIndexOrThrow("power_cabinet")));
        valve.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        valve.setOnPlace(cursor.getString(cursor.getColumnIndexOrThrow("on_place")));
        valve.setAp50(cursor.getString(cursor.getColumnIndexOrThrow("ap_50")));
        valve.setMark(cursor.getString(cursor.getColumnIndexOrThrow("mark")));
        valve.setCdaCabinet(cursor.getString(cursor.getColumnIndexOrThrow("cda_cabinet")));
        valve.setCdaCabinetPosition(cursor.getString(cursor.getColumnIndexOrThrow("cda_cabinet_position")));
        valve.setSlot(cursor.getString(cursor.getColumnIndexOrThrow("slot")));
        valve.setDescriptionBlockingOpen(cursor.getString(cursor.getColumnIndexOrThrow("description_blocking_open")));
        valve.setDescriptionBlockingClose(cursor.getString(cursor.getColumnIndexOrThrow("description_blocking_close")));
        valve.setDescriptionBlockingPerifer(cursor.getString(cursor.getColumnIndexOrThrow("description_blocking_perifer")));
        valve.setLocationDescription(cursor.getString(cursor.getColumnIndexOrThrow("location_description")));
        valve.setIsEdited(cursor.getInt(cursor.getColumnIndexOrThrow("is_edited")));
        valve.setEditedAtValve(cursor.getString(cursor.getColumnIndexOrThrow("edited_at")));
        return valve;
    }

    private ContentValues userGateValveToContentValues(GateValve valve) {
        ContentValues values = new ContentValues();
        values.put("original_id", valve.getOriginalId());
        values.put("is_deleted", valve.getIsDeleted());
        values.put("name_eng", valve.getNameEng());
        values.put("kks", valve.getKks());
        values.put("name", valve.getName());
        values.put("isy", valve.getIsy());
        values.put("power_cabinet", valve.getPowerCabinet());
        values.put("full_name", valve.getFullName());
        values.put("on_place", valve.getOnPlace());
        values.put("ap_50", valve.getAp50());
        values.put("mark", valve.getMark());
        values.put("cda_cabinet", valve.getCdaCabinet());
        values.put("cda_cabinet_position", valve.getCdaCabinetPosition());
        values.put("slot", valve.getSlot());
        values.put("description_blocking_open", valve.getDescriptionBlockingOpen());
        values.put("description_blocking_close", valve.getDescriptionBlockingClose());
        values.put("description_blocking_perifer", valve.getDescriptionBlockingPerifer());
        values.put("location_description", valve.getLocationDescription());
        values.put("is_edited", valve.getIsEdited());
        values.put("edited_at", valve.getEditedAtValve());
        values.put("created_at", valve.getCreatedAt());
        return values;
    }

    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД - SENSORS
    // ==========================================

    @Override
    public List<Sensor> getAllUserSensors() {
        List<Sensor> sensors = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_sensors",
                null, null, null, null, null,
                "st_marking ASC"
        );

        while (cursor.moveToNext()) {
            sensors.add(cursorToUserSensor(cursor));
        }
        cursor.close();
        return sensors;
    }

    @Override
    public Sensor getUserSensorByOriginalKks(String originalKks) {
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_sensors",
                null,
                "original_kks = ?",
                new String[]{originalKks},
                null, null, null
        );

        Sensor sensor = null;
        if (cursor.moveToFirst()) {
            sensor = cursorToUserSensor(cursor);
        }
        cursor.close();
        return sensor;
    }

    @Override
    public List<Sensor> searchUserSensors(String query) {
        List<Sensor> sensors = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();

        String cleanQuery = query.replaceAll("[\\s\\-.]", "");
        String kksWithoutPrefix = query.replaceAll("^[0-9]{2,3}", "");

        String selection =
                "(LOWER(st_marking) LIKE LOWER(?) OR " +
                        "LOWER(full_name) LIKE LOWER(?) OR " +
                        "LOWER(installation_location) LIKE LOWER(?) OR " +
                        "LOWER(name) LIKE LOWER(?) OR " +
                        "LOWER(kks) LIKE LOWER(?)" +
                        ") OR " +
                        "(LOWER(st_marking) LIKE LOWER(?))";

        String[] args = new String[]{
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + cleanQuery + "%"
        };

        if (!kksWithoutPrefix.equals(query) && !kksWithoutPrefix.isEmpty()) {
            selection += " OR LOWER(kks) LIKE LOWER(?)";
            String[] newArgs = new String[args.length + 1];
            System.arraycopy(args, 0, newArgs, 0, args.length);
            newArgs[args.length] = "%" + kksWithoutPrefix + "%";
            args = newArgs;
        }

        Cursor cursor = db.query(
                "user_sensors",
                null,
                selection,
                args,
                null, null,
                "st_marking ASC"
        );

        while (cursor.moveToNext()) {
            sensors.add(cursorToUserSensor(cursor));
        }
        cursor.close();
        return sensors;
    }

    @Override
    public long insertUserSensor(Sensor sensor) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userSensorToContentValues(sensor);
        return db.insert("user_sensors", null, values);
    }

    @Override
    public int updateUserSensor(Sensor sensor) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userSensorToContentValues(sensor);
        return db.update(
                "user_sensors",
                values,
                "original_kks = ?",
                new String[]{sensor.getOriginalKks()}
        );
    }

    @Override
    public int deleteUserSensor(int id) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        return db.delete(
                "user_sensors",
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    @Override
    public void copySensorToUser(String originalKks) {
        Sensor existing = getUserSensorByOriginalKks(originalKks);
        if (existing != null) return;

        Sensor ref = getSensorByKks(originalKks);
        if (ref == null) return;

        Sensor copy = new Sensor();
        copy.setOriginalKks(ref.getKks());
        copy.setKeynum(ref.getKeynum());
        copy.setFa(ref.getFa());
        copy.setKks(ref.getKks());
        copy.setStMarkir(ref.getStMarkir());
        copy.setFullName(ref.getFullName());
        copy.setName(ref.getName());
        copy.setMedia(ref.getMedia());
        copy.setUnits(ref.getUnits());
        copy.setNominal(ref.getNominal());
        copy.setVolMin(ref.getVolMin());
        copy.setVolMax(ref.getVolMax());
        copy.setSpeed(ref.getSpeed());
        copy.setFaultPar(ref.getFaultPar());
        copy.setInsteadF(ref.getInsteadF());
        copy.setFilter(ref.getFilter());
        copy.setModelSensor(ref.getModelSensor());
        copy.setModSensor(ref.getModSensor());
        copy.setAdditionalInfo(ref.getAdditionalInfo());
        copy.setMinVal(ref.getMinVal());
        copy.setMaxVal(ref.getMaxVal());
        copy.setMeasureUnit(ref.getMeasureUnit());
        copy.setLocation(ref.getLocation());
        copy.setCva(ref.getCva());
        copy.setDampingTime(ref.getDampingTime());
        copy.setIsEdited(1);
        copy.setEditedAt(getCurrentDateTime());
        copy.setCreatedAt(getCurrentDateTime());

        insertUserSensor(copy);
    }

    @Override
    public void markSensorAsDeleted(String originalKks) {
        Sensor existing = getUserSensorByOriginalKks(originalKks);

        if (existing != null) {
            existing.setIsDeleted(1);
            existing.setEditedAt(getCurrentDateTime());
            updateUserSensor(existing);
        } else {
            Sensor ref = getSensorByKks(originalKks);
            if (ref == null) return;

            Sensor copy = new Sensor();
            copy.setOriginalKks(ref.getKks());
            copy.setIsDeleted(1);
            copy.setKeynum(ref.getKeynum());
            copy.setFa(ref.getFa());
            copy.setKks(ref.getKks());
            copy.setStMarkir(ref.getStMarkir());
            copy.setFullName(ref.getFullName());
            copy.setName(ref.getName());
            copy.setMedia(ref.getMedia());
            copy.setUnits(ref.getUnits());
            copy.setNominal(ref.getNominal());
            copy.setVolMin(ref.getVolMin());
            copy.setVolMax(ref.getVolMax());
            copy.setSpeed(ref.getSpeed());
            copy.setFaultPar(ref.getFaultPar());
            copy.setInsteadF(ref.getInsteadF());
            copy.setFilter(ref.getFilter());
            copy.setModelSensor(ref.getModelSensor());
            copy.setModSensor(ref.getModSensor());
            copy.setAdditionalInfo(ref.getAdditionalInfo());
            copy.setMinVal(ref.getMinVal());
            copy.setMaxVal(ref.getMaxVal());
            copy.setMeasureUnit(ref.getMeasureUnit());
            copy.setLocation(ref.getLocation());
            copy.setCva(ref.getCva());
            copy.setDampingTime(ref.getDampingTime());
            copy.setIsEdited(1);
            copy.setEditedAt(getCurrentDateTime());
            copy.setCreatedAt(getCurrentDateTime());

            insertUserSensor(copy);
        }
    }

    @Override
    public List<Sensor> getAllSensorsWithUser() {
        List<Sensor> result = new ArrayList<>();

        List<Sensor> refs = getAllSensors();

        List<Sensor> users = new ArrayList<>();
        for (Sensor u : getAllUserSensors()) {
            if (u.getIsDeleted() != 1) {
                users.add(u);
            }
        }

        Map<String, Sensor> userMap = new HashMap<>();
        for (Sensor u : users) {
            userMap.put(u.getOriginalKks(), u);
        }

        for (Sensor ref : refs) {
            Sensor deleted = getUserSensorByOriginalKks(ref.getKks());
            if (deleted != null && deleted.getIsDeleted() == 1) {
                continue;
            }

            if (userMap.containsKey(ref.getKks())) {
                result.add(userMap.get(ref.getKks()));
            } else {
                result.add(ref);
            }
        }

        return result;
    }

    @Override
    public List<Sensor> searchSensorsWithUser(String query) {
        List<Sensor> userResults = searchUserSensors(query);
        List<Sensor> filteredUser = new ArrayList<>();
        for (Sensor u : userResults) {
            if (u.getIsDeleted() != 1) {
                filteredUser.add(u);
            }
        }

        if (!filteredUser.isEmpty()) {
            return filteredUser;
        }

        List<Sensor> refResults = searchSensors(query);

        List<Sensor> result = new ArrayList<>();
        for (Sensor ref : refResults) {
            Sensor deleted = getUserSensorByOriginalKks(ref.getKks());
            if (deleted == null || deleted.getIsDeleted() != 1) {
                result.add(ref);
            }
        }

        return result;
    }

    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД - SETPOINTS
    // ==========================================

    @Override
    public List<Setpoint> getAllUserSetpoints() {
        List<Setpoint> setpoints = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_setpoints",
                null, null, null, null, null,
                "position_name ASC"
        );

        while (cursor.moveToNext()) {
            setpoints.add(cursorToUserSetpoint(cursor));
        }
        cursor.close();
        return setpoints;
    }

    @Override
    public Setpoint getUserSetpointByOriginalId(int originalId) {
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_setpoints",
                null,
                "original_id = ?",
                new String[]{String.valueOf(originalId)},
                null, null, null
        );

        Setpoint setpoint = null;
        if (cursor.moveToFirst()) {
            setpoint = cursorToUserSetpoint(cursor);
        }
        cursor.close();
        return setpoint;
    }

    @Override
    public List<Setpoint> searchUserSetpoints(String query) {
        List<Setpoint> setpoints = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();

        String cleanQuery = query.replaceAll("[\\s\\-.]", "");

        String selection =
                "(LOWER(position_name) LIKE LOWER(?) OR " +
                        "LOWER(name) LIKE LOWER(?) OR " +
                        "LOWER(setpoint_value) LIKE LOWER(?) OR " +
                        "LOWER(operation) LIKE LOWER(?) OR " +
                        "LOWER(location) LIKE LOWER(?)" +
                        ") OR " +
                        "(LOWER(position_name) LIKE LOWER(?))";

        String[] args = new String[]{
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + cleanQuery + "%"
        };

        Cursor cursor = db.query(
                "user_setpoints",
                null,
                selection,
                args,
                null, null,
                "position_name ASC"
        );

        while (cursor.moveToNext()) {
            setpoints.add(cursorToUserSetpoint(cursor));
        }
        cursor.close();
        return setpoints;
    }

    @Override
    public long insertUserSetpoint(Setpoint setpoint) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userSetpointToContentValues(setpoint);
        return db.insert("user_setpoints", null, values);
    }

    @Override
    public int updateUserSetpoint(Setpoint setpoint) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userSetpointToContentValues(setpoint);
        return db.update(
                "user_setpoints",
                values,
                "original_id = ?",
                new String[]{String.valueOf(setpoint.getOriginalId())}
        );
    }

    @Override
    public int deleteUserSetpoint(int id) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        return db.delete(
                "user_setpoints",
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    @Override
    public void copySetpointToUser(int originalId) {
        Setpoint existing = getUserSetpointByOriginalId(originalId);
        if (existing != null) return;

        Setpoint ref = getSetpointById(originalId);
        if (ref == null) return;

        Setpoint copy = new Setpoint();
        copy.setOriginalId(ref.getId());
        copy.setName(ref.getName());
        copy.setPositionName(ref.getPositionName());
        copy.setLocation(ref.getLocation());
        copy.setSetpointValue(ref.getSetpointValue());
        copy.setDelayTime(ref.getDelayTime());
        copy.setOperation(ref.getOperation());
        copy.setNotes(ref.getNotes());
        copy.setEquipmentGroup(ref.getEquipmentGroup());
        copy.setIsEdited(1);
        copy.setEditedAt(getCurrentDateTime());
        copy.setCreatedAt(getCurrentDateTime());

        insertUserSetpoint(copy);
    }

    @Override
    public void markSetpointAsDeleted(int originalId) {
        Setpoint existing = getUserSetpointByOriginalId(originalId);

        if (existing != null) {
            existing.setIsDeleted(1);
            existing.setEditedAt(getCurrentDateTime());
            updateUserSetpoint(existing);
        } else {
            Setpoint ref = getSetpointById(originalId);
            if (ref == null) return;

            Setpoint copy = new Setpoint();
            copy.setOriginalId(ref.getId());
            copy.setIsDeleted(1);
            copy.setName(ref.getName());
            copy.setPositionName(ref.getPositionName());
            copy.setLocation(ref.getLocation());
            copy.setSetpointValue(ref.getSetpointValue());
            copy.setDelayTime(ref.getDelayTime());
            copy.setOperation(ref.getOperation());
            copy.setNotes(ref.getNotes());
            copy.setEquipmentGroup(ref.getEquipmentGroup());
            copy.setIsEdited(1);
            copy.setEditedAt(getCurrentDateTime());
            copy.setCreatedAt(getCurrentDateTime());

            insertUserSetpoint(copy);
        }
    }

    @Override
    public List<Setpoint> getAllSetpointsWithUser() {
        List<Setpoint> result = new ArrayList<>();

        List<Setpoint> refs = getAllSetpoints();

        List<Setpoint> users = new ArrayList<>();
        for (Setpoint u : getAllUserSetpoints()) {
            if (u.getIsDeleted() != 1) {
                users.add(u);
            }
        }

        Map<Integer, Setpoint> userMap = new HashMap<>();
        for (Setpoint u : users) {
            userMap.put(u.getOriginalId(), u);
        }

        for (Setpoint ref : refs) {
            Setpoint deleted = getUserSetpointByOriginalId(ref.getId());
            if (deleted != null && deleted.getIsDeleted() == 1) {
                continue;
            }

            if (userMap.containsKey(ref.getId())) {
                result.add(userMap.get(ref.getId()));
            } else {
                result.add(ref);
            }
        }

        return result;
    }

    @Override
    public List<Setpoint> searchSetpointsWithUser(String query) {
        List<Setpoint> userResults = searchUserSetpoints(query);
        List<Setpoint> filteredUser = new ArrayList<>();
        for (Setpoint u : userResults) {
            if (u.getIsDeleted() != 1) {
                filteredUser.add(u);
            }
        }

        if (!filteredUser.isEmpty()) {
            return filteredUser;
        }

        List<Setpoint> refResults = searchSetpoints(query);

        List<Setpoint> result = new ArrayList<>();
        for (Setpoint ref : refResults) {
            Setpoint deleted = getUserSetpointByOriginalId(ref.getId());
            if (deleted == null || deleted.getIsDeleted() != 1) {
                result.add(ref);
            }
        }

        return result;
    }

    // ==========================================
    // 🟢 ХЕЛПЕРЫ - USER SENSORS
    // ==========================================

    private Sensor cursorToUserSensor(Cursor cursor) {
        Sensor sensor = new Sensor();
        sensor.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        sensor.setOriginalKks(cursor.getString(cursor.getColumnIndexOrThrow("original_kks")));
        sensor.setIsDeleted(cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")));
        sensor.setKeynum(cursor.getInt(cursor.getColumnIndexOrThrow("keynum")));
        sensor.setFa(cursor.getInt(cursor.getColumnIndexOrThrow("fa")));
        sensor.setKks(cursor.getString(cursor.getColumnIndexOrThrow("kks")));
        sensor.setStMarkir(cursor.getString(cursor.getColumnIndexOrThrow("st_marking")));
        sensor.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        sensor.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        sensor.setMedia(cursor.getString(cursor.getColumnIndexOrThrow("media")));
        sensor.setUnits(cursor.getString(cursor.getColumnIndexOrThrow("units")));
        sensor.setNominal(cursor.getDouble(cursor.getColumnIndexOrThrow("nominal")));
        sensor.setVolMin(cursor.getDouble(cursor.getColumnIndexOrThrow("vol_min")));
        sensor.setVolMax(cursor.getDouble(cursor.getColumnIndexOrThrow("vol_max")));
        sensor.setSpeed(cursor.getString(cursor.getColumnIndexOrThrow("speed")));
        sensor.setFaultPar(cursor.getString(cursor.getColumnIndexOrThrow("fault_param")));
        sensor.setInsteadF(cursor.getString(cursor.getColumnIndexOrThrow("instead_f")));
        sensor.setFilter(cursor.getString(cursor.getColumnIndexOrThrow("filter")));
        sensor.setModelSensor(cursor.getString(cursor.getColumnIndexOrThrow("sensor_model")));
        sensor.setModSensor(cursor.getString(cursor.getColumnIndexOrThrow("sensor_mod")));
        sensor.setAdditionalInfo(cursor.getString(cursor.getColumnIndexOrThrow("additional_info")));
        sensor.setMinVal(cursor.getDouble(cursor.getColumnIndexOrThrow("min")));
        sensor.setMaxVal(cursor.getDouble(cursor.getColumnIndexOrThrow("max")));
        sensor.setMeasureUnit(cursor.getString(cursor.getColumnIndexOrThrow("unit_measure")));
        sensor.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("installation_location")));
        sensor.setCva(cursor.getString(cursor.getColumnIndexOrThrow("cva")));
        sensor.setDampingTime(cursor.getString(cursor.getColumnIndexOrThrow("damping_time")));
        sensor.setIsEdited(cursor.getInt(cursor.getColumnIndexOrThrow("is_edited")));
        sensor.setEditedAt(cursor.getString(cursor.getColumnIndexOrThrow("edited_at")));
        sensor.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return sensor;
    }

    private ContentValues userSensorToContentValues(Sensor sensor) {
        ContentValues values = new ContentValues();
        values.put("original_kks", sensor.getOriginalKks());
        values.put("is_deleted", sensor.getIsDeleted());
        values.put("keynum", sensor.getKeynum());
        values.put("fa", sensor.getFa());
        values.put("kks", sensor.getKks());
        values.put("st_marking", sensor.getStMarkir());
        values.put("full_name", sensor.getFullName());
        values.put("name", sensor.getName());
        values.put("media", sensor.getMedia());
        values.put("units", sensor.getUnits());
        values.put("nominal", sensor.getNominal());
        values.put("vol_min", sensor.getVolMin());
        values.put("vol_max", sensor.getVolMax());
        values.put("speed", sensor.getSpeed());
        values.put("fault_param", sensor.getFaultPar());
        values.put("instead_f", sensor.getInsteadF());
        values.put("filter", sensor.getFilter());
        values.put("sensor_model", sensor.getModelSensor());
        values.put("sensor_mod", sensor.getModSensor());
        values.put("additional_info", sensor.getAdditionalInfo());
        values.put("min", sensor.getMinVal());
        values.put("max", sensor.getMaxVal());
        values.put("unit_measure", sensor.getMeasureUnit());
        values.put("installation_location", sensor.getLocation());
        values.put("cva", sensor.getCva());
        values.put("damping_time", sensor.getDampingTime());
        values.put("is_edited", sensor.getIsEdited());
        values.put("edited_at", sensor.getEditedAt());
        values.put("created_at", sensor.getCreatedAt());
        return values;
    }

    // ==========================================
    // 🟢 ХЕЛПЕРЫ - USER SETPOINTS
    // ==========================================

    private Setpoint cursorToUserSetpoint(Cursor cursor) {
        Setpoint setpoint = new Setpoint();
        setpoint.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        setpoint.setOriginalId(cursor.getInt(cursor.getColumnIndexOrThrow("original_id")));
        setpoint.setIsDeleted(cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")));
        setpoint.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        setpoint.setPositionName(cursor.getString(cursor.getColumnIndexOrThrow("position_name")));
        setpoint.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("location")));
        setpoint.setSetpointValue(cursor.getString(cursor.getColumnIndexOrThrow("setpoint_value")));
        setpoint.setDelayTime(cursor.getString(cursor.getColumnIndexOrThrow("delay_time")));
        setpoint.setOperation(cursor.getString(cursor.getColumnIndexOrThrow("operation")));
        setpoint.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
        setpoint.setEquipmentGroup(cursor.getString(cursor.getColumnIndexOrThrow("equipment_group")));
        setpoint.setIsEdited(cursor.getInt(cursor.getColumnIndexOrThrow("is_edited")));
        setpoint.setEditedAt(cursor.getString(cursor.getColumnIndexOrThrow("edited_at")));
        setpoint.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return setpoint;
    }

    private ContentValues userSetpointToContentValues(Setpoint setpoint) {
        ContentValues values = new ContentValues();
        values.put("original_id", setpoint.getOriginalId());
        values.put("is_deleted", setpoint.getIsDeleted());
        values.put("name", setpoint.getName());
        values.put("position_name", setpoint.getPositionName());
        values.put("location", setpoint.getLocation());
        values.put("setpoint_value", setpoint.getSetpointValue());
        values.put("delay_time", setpoint.getDelayTime());
        values.put("operation", setpoint.getOperation());
        values.put("notes", setpoint.getNotes());
        values.put("equipment_group", setpoint.getEquipmentGroup());
        values.put("is_edited", setpoint.getIsEdited());
        values.put("edited_at", setpoint.getEditedAt());
        values.put("created_at", setpoint.getCreatedAt());
        return values;
    }

    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД - WORK SESSIONS
    // ==========================================

    @Override
    public long insertUserWorkSession(ValveWorkSession session) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userWorkSessionToContentValues(session);
        return db.insert("user_work_sessions", null, values);
    }

    @Override
    public List<ValveWorkSession> getAllUserWorkSessions() {
        List<ValveWorkSession> sessions = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_work_sessions",
                null, null, null, null, null,
                "save_date DESC"
        );

        while (cursor.moveToNext()) {
            sessions.add(cursorToUserWorkSession(cursor));
        }
        cursor.close();
        return sessions;
    }

    @Override
    public ValveWorkSession getUserWorkSessionById(String sessionId) {
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_work_sessions",
                null,
                "session_id = ?",
                new String[]{sessionId},
                null, null, null
        );

        ValveWorkSession session = null;
        if (cursor.moveToFirst()) {
            session = cursorToUserWorkSession(cursor);
        }
        cursor.close();
        return session;
    }

    @Override
    public int updateUserWorkSession(ValveWorkSession session) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userWorkSessionToContentValues(session);
        return db.update(
                "user_work_sessions",
                values,
                "session_id = ?",
                new String[]{session.getSessionId()}
        );
    }

    @Override
    public int deleteUserWorkSession(String sessionId) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        return db.delete(
                "user_work_sessions",
                "session_id = ?",
                new String[]{sessionId}
        );
    }

    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД - SESSION ITEMS
    // ==========================================

    @Override
    public long insertUserSessionItem(ValveItem item) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userSessionItemToContentValues(item);
        return db.insert("user_session_items", null, values);
    }

    @Override
    public List<ValveItem> getUserSessionItemsBySession(String sessionId) {
        List<ValveItem> items = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_session_items",
                null,
                "session_id = ?",
                new String[]{sessionId},
                null, null,
                "id ASC"
        );

        while (cursor.moveToNext()) {
            items.add(cursorToUserSessionItem(cursor));
        }
        cursor.close();
        return items;
    }

    @Override
    public int updateUserSessionItem(ValveItem item) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userSessionItemToContentValues(item);
        return db.update(
                "user_session_items",
                values,
                "id = ?",
                new String[]{String.valueOf(item.getItemId())}
        );
    }

    @Override
    public int deleteUserSessionItem(int itemId) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        return db.delete(
                "user_session_items",
                "id = ?",
                new String[]{String.valueOf(itemId)}
        );
    }

    // ==========================================
    // 🟢 ХЕЛПЕРЫ - USER WORK SESSIONS
    // ==========================================

    private ValveWorkSession cursorToUserWorkSession(Cursor cursor) {
        ValveWorkSession session = new ValveWorkSession();
        session.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        session.setSessionId(cursor.getString(cursor.getColumnIndexOrThrow("session_id")));
        session.setSaveDate(cursor.getString(cursor.getColumnIndexOrThrow("save_date")));
        session.setEquipmentDescription(cursor.getString(cursor.getColumnIndexOrThrow("equipment_description")));
        session.setIsSynced(cursor.getInt(cursor.getColumnIndexOrThrow("is_synced")));
        session.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return session;
    }

    private ContentValues userWorkSessionToContentValues(ValveWorkSession session) {
        ContentValues values = new ContentValues();
        values.put("session_id", session.getSessionId());
        values.put("save_date", session.getSaveDate());
        values.put("equipment_description", session.getEquipmentDescription());
        values.put("is_synced", session.getIsSynced());
        values.put("created_at", session.getCreatedAt());
        return values;
    }

    // ==========================================
    // 🟢 ХЕЛПЕРЫ - USER SESSION ITEMS
    // ==========================================

    private ValveItem cursorToUserSessionItem(Cursor cursor) {
        ValveItem item = new ValveItem();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        item.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        item.setParentSessionId(cursor.getString(cursor.getColumnIndexOrThrow("session_id")));
        item.setGateValveId(cursor.getInt(cursor.getColumnIndexOrThrow("gate_valve_id")));
        item.setIsAssembled(cursor.getInt(cursor.getColumnIndexOrThrow("is_assembled")));
        item.setMotorDisabled(cursor.getInt(cursor.getColumnIndexOrThrow("motor_disabled")));
        item.setBoxRemoved(cursor.getInt(cursor.getColumnIndexOrThrow("box_removed")));
        item.setIsChecked(cursor.getInt(cursor.getColumnIndexOrThrow("is_checked")));
        item.setCheckedAt(cursor.getString(cursor.getColumnIndexOrThrow("checked_at")));
        item.setOperationTimestamp(cursor.getString(cursor.getColumnIndexOrThrow("operation_timestamp")));
        return item;
    }

    private ContentValues userSessionItemToContentValues(ValveItem item) {
        ContentValues values = new ContentValues();
        values.put("session_id", item.getParentSessionId());
        values.put("gate_valve_id", item.getGateValveId());
        values.put("is_assembled", item.getIsAssembled());
        values.put("motor_disabled", item.getMotorDisabled());
        values.put("box_removed", item.getBoxRemoved());
        values.put("is_checked", item.getIsChecked());
        values.put("checked_at", item.getCheckedAt());
        values.put("operation_timestamp", item.getOperationTimestamp());
        return values;
    }

    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД - MEASUREMENTS
    // ==========================================

    @Override
    public long insertUserMeasurement(Measurement measurement) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userMeasurementToContentValues(measurement);
        return db.insert("user_measurements", null, values);
    }

    @Override
    public List<Measurement> getAllUserMeasurements() {
        List<Measurement> measurements = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_measurements",
                null, null, null, null, null,
                "measurement_date DESC"
        );

        while (cursor.moveToNext()) {
            measurements.add(cursorToUserMeasurement(cursor));
        }
        cursor.close();
        return measurements;
    }

    @Override
    public List<Measurement> getUserMeasurementsByDate(String date) {
        List<Measurement> measurements = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                "user_measurements",
                null,
                "measurement_date = ?",
                new String[]{date},
                null, null,
                "created_at DESC"
        );

        while (cursor.moveToNext()) {
            measurements.add(cursorToUserMeasurement(cursor));
        }
        cursor.close();
        return measurements;
    }

    @Override
    public int updateUserMeasurementDescription(int id, String description) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("description", description);
        return db.update(
                "user_measurements",
                values,
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    @Override
    public int deleteUserMeasurement(int id) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        return db.delete(
                "user_measurements",
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // ==========================================
    // 🟢 ХЕЛПЕРЫ - USER MEASUREMENTS
    // ==========================================

    private Measurement cursorToUserMeasurement(Cursor cursor) {
        Measurement measurement = new Measurement();
        measurement.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        measurement.setMeasurementDate(cursor.getString(cursor.getColumnIndexOrThrow("measurement_date")));
        measurement.setValue(cursor.getDouble(cursor.getColumnIndexOrThrow("value")));
        measurement.setInputValue(cursor.getDouble(cursor.getColumnIndexOrThrow("input_value")));
        measurement.setUnit(cursor.getString(cursor.getColumnIndexOrThrow("unit")));
        measurement.setTemperature(cursor.getDouble(cursor.getColumnIndexOrThrow("temperature")));
        measurement.setSensorType(cursor.getString(cursor.getColumnIndexOrThrow("sensor_type")));
        measurement.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        measurement.setColdJunctionMv(cursor.getDouble(cursor.getColumnIndexOrThrow("cold_junction_mv")));
        measurement.setLineResistance(cursor.getDouble(cursor.getColumnIndexOrThrow("line_resistance")));
        measurement.setIsSynced(cursor.getInt(cursor.getColumnIndexOrThrow("is_synced")));
        measurement.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return measurement;
    }

    private ContentValues userMeasurementToContentValues(Measurement measurement) {
        ContentValues values = new ContentValues();
        values.put("measurement_date", measurement.getMeasurementDate());
        values.put("value", measurement.getValue());
        values.put("input_value", measurement.getInputValue());
        values.put("unit", measurement.getUnit());
        values.put("temperature", measurement.getTemperature());
        values.put("sensor_type", measurement.getSensorType());
        values.put("description", measurement.getDescription());
        values.put("cold_junction_mv", measurement.getColdJunctionMv());
        values.put("line_resistance", measurement.getLineResistance());
        values.put("is_synced", measurement.getIsSynced());
        values.put("created_at", measurement.getCreatedAt());
        return values;
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // ==========================================
    // 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД - СИНХРОНИЗАЦИЯ
    // ==========================================

    @Override
    public void exportUserDatabase(String filePath) {
        // TODO: реализовать
    }

    @Override
    public void importUserDatabase(String filePath) {
        // TODO: реализовать
    }
    // ==========================================
// 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД - WORK SESSIONS (ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ)
// ==========================================

    @Override
    public int updateUserWorkSessionDate(String sessionId, String saveDate) {
        Log.d("DATEFRESH", "=== updateUserWorkSessionDate START ===");
        Log.d("DATEFRESH", "sessionId: " + sessionId);
        Log.d("DATEFRESH", "saveDate: " + saveDate);

        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("save_date", saveDate);

        int result = db.update(
                "user_work_sessions",
                values,
                "session_id = ?",
                new String[]{sessionId}
        );

        Log.d("DATEFRESH", "update result: " + result);
        Log.d("DATEFRESH", "=== updateUserWorkSessionDate END ===");
        return result;
    }

    @Override
    public int updateUserWorkSessionName(String sessionId, String newName) {
        Log.d("SESSY", "updateUserWorkSessionName: " + sessionId + " -> " + newName);

        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("equipment_description", newName);

        return db.update(
                "user_work_sessions",
                values,
                "session_id = ?",
                new String[]{sessionId}
        );
    }
// ==========================================
// 🟢 ПОЛЬЗОВАТЕЛЬСКАЯ БД - SESSION ITEMS (ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ)
// ==========================================

    @Override
    public int updateUserSessionItemStatus(int itemId, int isAssembled, int motorDisabled, int boxRemoved) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_assembled", isAssembled);
        values.put("motor_disabled", motorDisabled);
        values.put("box_removed", boxRemoved);
        values.put("operation_timestamp", getCurrentDateTime());

        return db.update(
                "user_session_items",
                values,
                "id = ?",
                new String[]{String.valueOf(itemId)}
        );
    }

    @Override
    public int updateUserSessionItemChecked(int itemId, int isChecked, String checkedAt) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_checked", isChecked);
        values.put("checked_at", checkedAt);
        values.put("operation_timestamp", getCurrentDateTime());

        return db.update(
                "user_session_items",
                values,
                "id = ?",
                new String[]{String.valueOf(itemId)}
        );
    }
}