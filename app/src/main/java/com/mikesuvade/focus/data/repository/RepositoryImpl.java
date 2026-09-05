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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
        boolean isShortQuery = cleanQuery.length() < 3;

        // 🔥 ЕСЛИ ЗАПРОС КОРОТКИЙ (< 3 символов) — НЕ ИЩЕМ В ОПИСАНИИ
        String selection;
        String[] args;
        String orderBy;

        if (isShortQuery) {
            // Ищем ТОЛЬКО по st_marking и name (без full_name, location, kks)
            selection =
                    "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                            "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_NAME + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";

            args = new String[]{
                    "%" + cleanQuery + "%",
                    "%" + cleanQuery + "%"
            };

            orderBy =
                    "CASE WHEN REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + "), ' ', ''), '-', ''), '.', '') LIKE LOWER('%" + cleanQuery + "%') THEN 1 " +
                            "ELSE 2 END, " +
                            DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + " ASC";
        } else {
            // Полный поиск по всем полям
            selection =
                    "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                            "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_FULL_NAME + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                            "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_NAME + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                            "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_LOCATION + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                            "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_KKS + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";

            args = new String[]{
                    "%" + cleanQuery + "%",
                    "%" + cleanQuery + "%",
                    "%" + cleanQuery + "%",
                    "%" + cleanQuery + "%",
                    "%" + cleanQuery + "%"
            };

            orderBy =
                    "CASE WHEN REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + "), ' ', ''), '-', ''), '.', '') LIKE LOWER('%" + cleanQuery + "%') THEN 1 " +
                            "WHEN REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_FULL_NAME + "), ' ', ''), '-', ''), '.', '') LIKE LOWER('%" + cleanQuery + "%') THEN 2 " +
                            "WHEN REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_NAME + "), ' ', ''), '-', ''), '.', '') LIKE LOWER('%" + cleanQuery + "%') THEN 3 " +
                            "ELSE 4 END, " +
                            DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + " ASC";
        }

        Cursor cursor = db.query(
                DatabaseContract.SensorScheduleEntry.TABLE_NAME,
                null,
                selection,
                args,
                null, null,
                orderBy
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

        // 🔥 ИСПРАВЛЕНО: используем "id" вместо "_id"
        Cursor cursor = db.query(
                DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                null,
                "id = ?",  // ← было "_id = ?"
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
        boolean isShortQuery = cleanQuery.length() < 3;

        String selection;
        String[] args;

        if (isShortQuery) {
            // Короткий запрос — только name и position_name
            selection =
                    "LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_NAME + ") LIKE LOWER(?) OR " +
                            "LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + ") LIKE LOWER(?)";

            args = new String[]{
                    "%" + query + "%",
                    "%" + query + "%"
            };
        } else {
            // Полный поиск
            selection =
                    "LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_NAME + ") LIKE LOWER(?) OR " +
                            "LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + ") LIKE LOWER(?) OR " +
                            "LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_SETPOINT_VALUE + ") LIKE LOWER(?) OR " +
                            "LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_OPERATION + ") LIKE LOWER(?) OR " +
                            "LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_NOTES + ") LIKE LOWER(?)";

            args = new String[]{
                    "%" + query + "%",
                    "%" + query + "%",
                    "%" + query + "%",
                    "%" + query + "%",
                    "%" + query + "%"
            };
        }

        Cursor cursor = db.query(
                DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                null,
                selection,
                args,
                null, null,
                null // Без сортировки в SQL, сортируем в Java
        );

        while (cursor.moveToNext()) {
            setpoints.add(cursorToSetpoint(cursor));
        }
        cursor.close();

        // 🔥 СОРТИРОВКА В JAVA ПО РЕЛЕВАНТНОСТИ
        return sortSetpointsByRelevance(setpoints, query);
    }

    /**
     * Сортирует уставки по релевантности поисковому запросу
     */
    private List<Setpoint> sortSetpointsByRelevance(List<Setpoint> setpoints, String query) {
        String lowerQuery = query.toLowerCase();

        Collections.sort(setpoints, (a, b) -> {
            int scoreA = getSetpointRelevanceScore(a, lowerQuery);
            int scoreB = getSetpointRelevanceScore(b, lowerQuery);
            return Integer.compare(scoreA, scoreB);
        });

        return setpoints;
    }

    /**
     * Вычисляет оценку релевантности уставки
     * Чем меньше число — тем выше релевантность
     */
    private int getSetpointRelevanceScore(Setpoint setpoint, String query) {
        String name = setpoint.getName() != null ? setpoint.getName().toLowerCase() : "";
        String positionName = setpoint.getPositionName() != null ? setpoint.getPositionName().toLowerCase() : "";
        String setpointValue = setpoint.getSetpointValue() != null ? setpoint.getSetpointValue().toLowerCase() : "";
        String operation = setpoint.getOperation() != null ? setpoint.getOperation().toLowerCase() : "";
        String notes = setpoint.getNotes() != null ? setpoint.getNotes().toLowerCase() : "";

        // Полное совпадение в name — самый высокий приоритет
        if (name.equals(query)) return 1;
        if (positionName.equals(query)) return 2;
        if (setpointValue.equals(query)) return 3;
        if (operation.equals(query)) return 4;
        if (notes.equals(query)) return 5;

        // Частичное совпадение
        if (name.contains(query)) return 10;
        if (positionName.contains(query)) return 20;
        if (setpointValue.contains(query)) return 30;
        if (operation.contains(query)) return 40;
        if (notes.contains(query)) return 50;

        // Совпадение в начале слова
        if (name.startsWith(query)) return 11;
        if (positionName.startsWith(query)) return 21;

        return 100;
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

        // 🔥 ТОЛЬКО ТЕ КОЛОНКИ, КОТОРЫЕ ЕСТЬ В БД1
        sensor.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
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

        // ❌ НЕТ keynum, fa, is_deleted, original_id
        sensor.setKeynum(0);
        sensor.setFa(0);
        sensor.setIsDeleted(0);
        sensor.setOriginalId(0);

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

        // ❌ УБРАТЬ keynum и fa (их нет в БД1)
        // values.put(DatabaseContract.SensorScheduleEntry.COLUMN_KEYNUM, sensor.getKeynum());
        // values.put(DatabaseContract.SensorScheduleEntry.COLUMN_FA, sensor.getFa());

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
    @Override
    public Sensor getSensorById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.SensorScheduleEntry.TABLE_NAME,
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        Sensor sensor = null;
        if (cursor.moveToFirst()) {
            sensor = cursorToSensor(cursor);
        }
        cursor.close();
        return sensor;
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

        // 🔥 ТАКОЙ ЖЕ ПОИСК, КАК В БД1
        String selection =
                "REPLACE(REPLACE(LOWER(name), ' ', ''), '-', '') LIKE LOWER(?) OR " +
                        "REPLACE(REPLACE(LOWER(kks), ' ', ''), '-', '') LIKE LOWER(?) OR " +
                        "REPLACE(REPLACE(LOWER(isy), ' ', ''), '-', '') LIKE LOWER(?) OR " +
                        "LOWER(full_name) LIKE LOWER(?) OR " +
                        "LOWER(on_place) LIKE LOWER(?)";

        String[] args = new String[]{
                "%" + cleanQuery + "%",
                "%" + cleanQuery + "%",
                "%" + cleanQuery + "%",
                "%" + query + "%",
                "%" + query + "%"
        };

        Log.d("SEARCH_USER_VALVE", "=== searchUserGateValves ===");
        Log.d("SEARCH_USER_VALVE", "query = " + query);
        Log.d("SEARCH_USER_VALVE", "cleanQuery = " + cleanQuery);

        Cursor cursor = db.query(
                "user_gate_valves",
                null,
                selection,
                args,
                null, null,
                "name ASC"
        );

        while (cursor.moveToNext()) {
            GateValve valve = cursorToUserGateValve(cursor);
            valves.add(valve);
            Log.d("SEARCH_USER_VALVE", "  found: " + valve.getName() + " | " + valve.getIsy());
        }
        cursor.close();

        Log.d("SEARCH_USER_VALVE", "total found = " + valves.size());
        return valves;
    }

    @Override
    public long insertUserGateValve(GateValve valve) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userGateValveToContentValues(valve);

        Log.d("VALVE_DEBUG", "=== insertUserGateValve ===");
        Log.d("VALVE_DEBUG", "valve.getId() = " + valve.getId());
        Log.d("VALVE_DEBUG", "values = " + values.toString());

        long result = db.insert("user_gate_valves", null, values);

        Log.d("VALVE_DEBUG", "db.insert result = " + result);
        return result;
    }

    @Override
    public int updateUserGateValve(GateValve valve) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userGateValveToContentValues(valve);

        Log.d("REPO_UPDATE", "=== updateUserGateValve ===");
        Log.d("REPO_UPDATE", "valve.getId() = " + valve.getId());
        Log.d("REPO_UPDATE", "valve.getOriginalId() = " + valve.getOriginalId());
        Log.d("REPO_UPDATE", "values = " + values.toString());

        // ✅ Ищем по id (а не по original_id)
        int result = db.update(
                "user_gate_valves",
                values,
                "id = ?",
                new String[]{String.valueOf(valve.getId())}
        );

        Log.d("REPO_UPDATE", "update result = " + result + " rows affected");
        return result;
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
        Log.d("SEARCH_VALVE", "=== searchGateValvesWithUser START ===");
        Log.d("SEARCH_VALVE", "query = " + query);

        List<GateValve> result = new ArrayList<>();
        Set<Integer> seenIds = new HashSet<>();

        try {
            // 🔥 1. Ищем в БД2 (пользовательские)
            List<GateValve> userResults = searchUserGateValves(query);
            Log.d("SEARCH_VALVE", "userResults size = " + userResults.size());

            for (GateValve valve : userResults) {
                if (valve.getIsDeleted() != 1) {
                    // Добавляем блокировки из справочника
                    GateValve ref = getGateValveById(valve.getOriginalId());
                    if (ref != null) {
                        valve.setNameSpaceViewOpen(ref.getNameSpaceViewOpen());
                        valve.setNamespaceViewClose(ref.getNamespaceViewClose());
                        valve.setNamespaceViewPerifer(ref.getNamespaceViewPerifer());
                    }
                    result.add(valve);
                    seenIds.add(valve.getOriginalId());
                    Log.d("SEARCH_VALVE", "  added user: " + valve.getName());
                }
            }

            // 🔥 2. Ищем в БД1 (справочник)
            List<GateValve> refResults = searchGateValves(query);
            Log.d("SEARCH_VALVE", "refResults size = " + refResults.size());

            for (GateValve valve : refResults) {
                // Проверяем, не удалена ли эта задвижка в БД2
                GateValve deleted = getUserGateValveByOriginalId(valve.getId());
                if (deleted != null && deleted.getIsDeleted() == 1) {
                    Log.d("SEARCH_VALVE", "  skipping deleted: " + valve.getName());
                    continue;
                }

                if (!seenIds.contains(valve.getId())) {
                    result.add(valve);
                    seenIds.add(valve.getId());
                    Log.d("SEARCH_VALVE", "  added ref: " + valve.getName());
                }
            }

            Log.d("SEARCH_VALVE", "result size = " + result.size());
            Log.d("SEARCH_VALVE", "=== searchGateValvesWithUser END ===");
            return result;

        } catch (Exception e) {
            Log.e("SEARCH_VALVE", "Error searching valves with user", e);
            return new ArrayList<>();
        }
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

        // 🔥 ДОБАВИТЬ ЭТУ СТРОКУ
        valve.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));

        return valve;
    }
    private ContentValues userGateValveToContentValues(GateValve valve) {
        ContentValues values = new ContentValues();
        values.put("id", valve.getId());
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
        Log.d("REPO_UPDATE", "created_at = " + valve.getCreatedAt());  // ← ДОБАВИТЬ
        return values;
    }
    @Override
    public GateValve getUserGateValveById(int id) {
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_gate_valves",
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        GateValve valve = null;
        if (cursor.moveToFirst()) {
            valve = cursorToUserGateValve(cursor);
        }
        cursor.close();
        return valve;
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
    public List<Sensor> searchUserSensors(String query) {
        Log.d("SEARCH_USER", "=== searchUserSensors START ===");
        Log.d("SEARCH_USER", "query = " + query);

        List<Sensor> sensors = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();

        String cleanQuery = query.replaceAll("[\\s\\-.]", "");
        Log.d("SEARCH_USER", "cleanQuery = " + cleanQuery);

        String selection =
                "REPLACE(REPLACE(REPLACE(LOWER(st_marking), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                        "REPLACE(REPLACE(REPLACE(LOWER(full_name), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                        "REPLACE(REPLACE(REPLACE(LOWER(installation_location), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                        "REPLACE(REPLACE(REPLACE(LOWER(name), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                        "REPLACE(REPLACE(REPLACE(LOWER(kks), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";

        String[] args = new String[]{
                "%" + cleanQuery + "%",
                "%" + cleanQuery + "%",
                "%" + cleanQuery + "%",
                "%" + cleanQuery + "%",
                "%" + cleanQuery + "%"
        };

        Log.d("SEARCH_USER", "selection = " + selection);
        Log.d("SEARCH_USER", "args[0] = '" + args[0] + "'");

        Cursor cursor = db.query(
                "user_sensors",
                null,
                selection,
                args,
                null, null,
                "st_marking ASC"
        );

        Log.d("SEARCH_USER", "cursor count = " + cursor.getCount());

        while (cursor.moveToNext()) {
            Sensor sensor = cursorToUserSensor(cursor);
            sensors.add(sensor);
            Log.d("SEARCH_USER", "  found: id=" + sensor.getId() +
                    ", stMarking=" + sensor.getStMarkir() +
                    ", kks=" + sensor.getKks() +
                    ", originalId=" + sensor.getOriginalId() +
                    ", isDeleted=" + sensor.getIsDeleted());
        }
        cursor.close();

        Log.d("SEARCH_USER", "total found = " + sensors.size());
        Log.d("SEARCH_USER", "=== searchUserSensors END ===");
        return sensors;
    }
    @Override
    public long insertUserSensor(Sensor sensor) {
        Log.d("SENSOR_DEBUG", "=== insertUserSensor ===");
        Log.d("SENSOR_DEBUG", "sensor.getId() = " + sensor.getId());
        Log.d("SENSOR_DEBUG", "sensor.getKks() = " + sensor.getKks());
        Log.d("SENSOR_DEBUG", "sensor.getOriginalId() = " + sensor.getOriginalId());

        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userSensorToContentValues(sensor);

        Log.d("SENSOR_DEBUG", "values = " + values.toString());

        long result = db.insert("user_sensors", null, values);

        Log.d("SENSOR_DEBUG", "db.insert result = " + result);
        return result;
    }

    @Override
    public int updateUserSensor(Sensor sensor) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userSensorToContentValues(sensor);

        // 🔥 ИСПРАВЛЕНО: ищем по id, а не по original_kks!
        return db.update(
                "user_sensors",
                values,
                "id = ?",  // ← было "original_kks = ?"
                new String[]{String.valueOf(sensor.getId())}
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
    public List<Sensor> getAllSensorsWithUser() {
        List<Sensor> result = new ArrayList<>();
        Set<Integer> seenIds = new HashSet<>();  // ← ИСПОЛЬЗУЕМ ID

        List<Sensor> refs = getAllSensors();

        List<Sensor> users = new ArrayList<>();
        for (Sensor u : getAllUserSensors()) {
            if (u.getIsDeleted() != 1) {
                users.add(u);
            }
        }

        // 🔥 1. Добавляем пользовательские
        for (Sensor u : users) {
            result.add(u);
            int key = u.getOriginalId() > 0 ? u.getOriginalId() : u.getId();
            seenIds.add(key);
            Log.d("SENSOR_USER", "added user: " + u.getStMarkir() + " (id=" + u.getId() + ")");
        }

        // 🔥 2. Добавляем справочные, которых нет в пользовательских
        for (Sensor ref : refs) {
            Sensor deleted = getUserSensorByOriginalId(ref.getId());
            if (deleted != null && deleted.getIsDeleted() == 1) {
                Log.d("SENSOR_USER", "skipping deleted: " + ref.getStMarkir());
                continue;
            }

            if (!seenIds.contains(ref.getId())) {
                result.add(ref);
                seenIds.add(ref.getId());
                Log.d("SENSOR_USER", "added ref: " + ref.getStMarkir());
            }
        }

        return result;
    }

    @Override
    public List<Sensor> searchSensorsWithUser(String query) {
        Log.d("SEARCH_SENSOR", "=== searchSensorsWithUser START ===");
        Log.d("SEARCH_SENSOR", "query = " + query);

        List<Sensor> result = new ArrayList<>();
        Set<Integer> seenIds = new HashSet<>();

        try {
            // 🔥 1. Ищем в БД2 (пользовательские)
            Log.d("SEARCH_SENSOR", "Step 1: Searching in USER DB...");
            List<Sensor> userResults = searchUserSensors(query);
            Log.d("SEARCH_SENSOR", "userResults size = " + userResults.size());

            if (userResults.isEmpty()) {
                Log.d("SEARCH_SENSOR", "No results in USER DB");
            } else {
                for (Sensor sensor : userResults) {
                    Log.d("SEARCH_SENSOR", "  USER result: id=" + sensor.getId() +
                            ", stMarking=" + sensor.getStMarkir() +
                            ", kks=" + sensor.getKks() +
                            ", originalId=" + sensor.getOriginalId() +
                            ", isDeleted=" + sensor.getIsDeleted());
                    if (sensor.getIsDeleted() != 1) {
                        result.add(sensor);
                        int key = sensor.getOriginalId() > 0 ? sensor.getOriginalId() : sensor.getId();
                        seenIds.add(key);
                        Log.d("SEARCH_SENSOR", "  ✅ added user: " + sensor.getStMarkir());
                    } else {
                        Log.d("SEARCH_SENSOR", "  ⏭️ skipping deleted: " + sensor.getStMarkir());
                    }
                }
            }

            // 🔥 2. Ищем в БД1 (справочник)
            Log.d("SEARCH_SENSOR", "Step 2: Searching in REF DB...");
            List<Sensor> refResults = searchSensors(query);
            Log.d("SEARCH_SENSOR", "refResults size = " + refResults.size());

            if (refResults.isEmpty()) {
                Log.d("SEARCH_SENSOR", "No results in REF DB");
            } else {
                for (Sensor sensor : refResults) {
                    Log.d("SEARCH_SENSOR", "  REF result: id=" + sensor.getId() +
                            ", stMarking=" + sensor.getStMarkir() +
                            ", kks=" + sensor.getKks());

                    // Проверяем, не удалён ли этот датчик в БД2
                    Sensor deleted = getUserSensorByOriginalId(sensor.getId());
                    if (deleted != null && deleted.getIsDeleted() == 1) {
                        Log.d("SEARCH_SENSOR", "  ⏭️ skipping deleted ref: " + sensor.getStMarkir());
                        continue;
                    }

                    // Проверяем, нет ли уже пользовательской версии
                    if (!seenIds.contains(sensor.getId())) {
                        result.add(sensor);
                        seenIds.add(sensor.getId());
                        Log.d("SEARCH_SENSOR", "  ✅ added ref: " + sensor.getStMarkir());
                    } else {
                        Log.d("SEARCH_SENSOR", "  ⏭️ skipping duplicate ref: " + sensor.getStMarkir());
                    }
                }
            }

            Log.d("SEARCH_SENSOR", "FINAL result size = " + result.size());
            Log.d("SEARCH_SENSOR", "=== searchSensorsWithUser END ===");
            return result;

        } catch (Exception e) {
            Log.e("SEARCH_SENSOR", "Error in searchSensorsWithUser", e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    @Override
    public Sensor getUserSensorByOriginalId(int originalId) {
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_sensors",
                null,
                "original_id = ?",
                new String[]{String.valueOf(originalId)},
                null, null, null
        );

        Sensor sensor = null;
        if (cursor.moveToFirst()) {
            sensor = cursorToUserSensor(cursor);
        }
        cursor.close();
        return sensor;
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

        // 🔥 ТАКОЙ ЖЕ ПОИСК, КАК В БД1
        String selection =
                "REPLACE(REPLACE(REPLACE(LOWER(position_name), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                        "LOWER(name) LIKE LOWER(?) OR " +
                        "LOWER(setpoint_value) LIKE LOWER(?) OR " +
                        "LOWER(operation) LIKE LOWER(?) OR " +
                        "LOWER(location) LIKE LOWER(?)";

        String[] args = new String[]{
                "%" + cleanQuery + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%"
        };

        Log.d("SEARCH_USER_SETPOINT", "=== searchUserSetpoints ===");
        Log.d("SEARCH_USER_SETPOINT", "query = " + query);
        Log.d("SEARCH_USER_SETPOINT", "cleanQuery = " + cleanQuery);

        Cursor cursor = db.query(
                "user_setpoints",
                null,
                selection,
                args,
                null, null,
                "position_name ASC"
        );

        while (cursor.moveToNext()) {
            Setpoint setpoint = cursorToUserSetpoint(cursor);
            setpoints.add(setpoint);
            Log.d("SEARCH_USER_SETPOINT", "  found: " + setpoint.getPositionName());
        }
        cursor.close();

        Log.d("SEARCH_USER_SETPOINT", "total found = " + setpoints.size());
        return setpoints;
    }
    @Override
    public long insertUserSetpoint(Setpoint setpoint) {
        Log.d("REPO_INSERT", "=== insertUserSetpoint START ===");
        Log.d("REPO_INSERT", "setpoint = " + (setpoint != null ? "not null" : "NULL"));

        if (setpoint == null) {
            Log.e("REPO_INSERT", "setpoint is NULL!");
            return -1;
        }

        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userSetpointToContentValues(setpoint);

        Log.d("REPO_INSERT", "values = " + values.toString());

        long result = db.insert("user_setpoints", null, values);

        Log.d("REPO_INSERT", "insert result = " + result);
        Log.d("REPO_INSERT", "=== insertUserSetpoint END ===");
        return result;
    }

    @Override
    public int updateUserSetpoint(Setpoint setpoint) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = userSetpointToContentValues(setpoint);

        Log.d("REPO_UPDATE", "=== updateUserSetpoint ===");
        Log.d("REPO_UPDATE", "setpoint.getId() = " + setpoint.getId());
        Log.d("REPO_UPDATE", "setpoint.getOriginalId() = " + setpoint.getOriginalId());
        Log.d("REPO_UPDATE", "values = " + values.toString());

        // ✅ Ищем по id (а не по original_id)
        int result = db.update(
                "user_setpoints",
                values,
                "id = ?",
                new String[]{String.valueOf(setpoint.getId())}
        );

        Log.d("REPO_UPDATE", "update result = " + result + " rows affected");
        return result;
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
        Log.d("SEARCH_SETPOINT", "=== searchSetpointsWithUser START ===");
        Log.d("SEARCH_SETPOINT", "query = " + query);

        List<Setpoint> result = new ArrayList<>();
        Set<Integer> seenIds = new HashSet<>();

        try {
            // 🔥 1. Ищем в БД2 (пользовательские)
            List<Setpoint> userResults = searchUserSetpoints(query);
            Log.d("SEARCH_SETPOINT", "userResults size = " + userResults.size());

            for (Setpoint setpoint : userResults) {
                if (setpoint.getIsDeleted() != 1) {
                    result.add(setpoint);
                    int key = setpoint.getOriginalId() > 0 ? setpoint.getOriginalId() : setpoint.getId();
                    seenIds.add(key);
                    Log.d("SEARCH_SETPOINT", "  added user: " + setpoint.getPositionName());
                }
            }

            // 🔥 2. Ищем в БД1 (справочник)
            List<Setpoint> refResults = searchSetpoints(query);
            Log.d("SEARCH_SETPOINT", "refResults size = " + refResults.size());

            for (Setpoint setpoint : refResults) {
                // Проверяем, не удалена ли эта уставка в БД2
                Setpoint deleted = getUserSetpointByOriginalId(setpoint.getId());
                if (deleted != null && deleted.getIsDeleted() == 1) {
                    Log.d("SEARCH_SETPOINT", "  skipping deleted: " + setpoint.getPositionName());
                    continue;
                }

                // Проверяем, нет ли уже пользовательской версии
                if (!seenIds.contains(setpoint.getId())) {
                    result.add(setpoint);
                    seenIds.add(setpoint.getId());
                    Log.d("SEARCH_SETPOINT", "  added ref: " + setpoint.getPositionName());
                }
            }

            Log.d("SEARCH_SETPOINT", "result size = " + result.size());
            Log.d("SEARCH_SETPOINT", "=== searchSetpointsWithUser END ===");
            return result;

        } catch (Exception e) {
            Log.e("SEARCH_SETPOINT", "Error searching setpoints with user", e);
            return new ArrayList<>();
        }
    }

    // ==========================================
    // 🟢 ХЕЛПЕРЫ - USER SENSORS
    // ==========================================

    private Sensor cursorToUserSensor(Cursor cursor) {
        Sensor sensor = new Sensor();
        sensor.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        sensor.setOriginalId(cursor.getInt(cursor.getColumnIndexOrThrow("original_id")));  // ← ДОБАВИТЬ
        //sensor.setOriginalKks(cursor.getString(cursor.getColumnIndexOrThrow("original_kks")));
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
        sensor.setFilter(cursor.getString(cursor.getColumnIndexOrThrow("filter_value")));  // ← переименовали
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

        // 🔥 ДОБАВЛЯЕМ ID И ORIGINAL_ID
        values.put("id", sensor.getId());
        values.put("original_id", sensor.getOriginalId());

        //values.put("original_kks", sensor.getOriginalKks());
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
        values.put("filter_value", sensor.getFilter());
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
    @Override
    public void copySensorToUser(int originalId) {
        Sensor existing = getUserSensorByOriginalId(originalId);
        if (existing != null) return;

        Sensor ref = getSensorById(originalId);
        if (ref == null) return;

        // Генерируем отрицательный ID для копии
        int newId = generateNegativeIdForSensor();

        Sensor copy = new Sensor();
        copy.setId(newId);
        copy.setOriginalId(ref.getId());
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
        copy.setIsDeleted(0);
        copy.setIsEdited(1);
        copy.setCreatedAt(getCurrentDateTime());
        copy.setEditedAt(getCurrentDateTime());

        insertUserSensor(copy);
    }

    @Override
    public void markSensorAsDeleted(int originalId) {
        Sensor existing = getUserSensorByOriginalId(originalId);

        if (existing != null) {
            existing.setIsDeleted(1);
            existing.setEditedAt(getCurrentDateTime());
            updateUserSensor(existing);
        } else {
            Sensor ref = getSensorById(originalId);
            if (ref == null) return;

            // Создаём копию с пометкой is_deleted = 1
            int newId = generateNegativeIdForSensor();

            Sensor copy = new Sensor();
            copy.setId(newId);
            copy.setOriginalId(ref.getId());
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
            copy.setCreatedAt(getCurrentDateTime());
            copy.setEditedAt(getCurrentDateTime());

            insertUserSensor(copy);
        }
    }

    private int generateNegativeIdForSensor() {
        int minId = 0;
        List<Sensor> sensors = getAllUserSensors();
        for (Sensor s : sensors) {
            if (s.getId() < minId) minId = s.getId();
        }
        return minId - 1;
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
        Log.d("REPO_INSERT", "=== userSetpointToContentValues START ===");
        ContentValues values = new ContentValues();
        values.put("id", setpoint.getId());

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

        Log.d("REPO_INSERT", "values = " + values.toString());
        Log.d("REPO_INSERT", "=== userSetpointToContentValues END ===");
        return values;
    }
    // В RepositoryImpl.java добавьте:

// ==========================================
// 🟢 ПОЛУЧЕНИЕ ГРУПП ОБОРУДОВАНИЯ
// ==========================================

    @Override
    public List<String> getAllEquipmentGroups() {
        List<String> groups = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT DISTINCT " + DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP +
                " FROM " + DatabaseContract.SetpointScheduleEntry.TABLE_NAME +
                " WHERE " + DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP +
                " IS NOT NULL AND " + DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP + " != ''" +
                " ORDER BY " + DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP + " ASC";

        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            String group = cursor.getString(0);
            if (group != null && !group.isEmpty()) {
                groups.add(group);
            }
        }
        cursor.close();
        return groups;
    }

    @Override
    public List<String> getAllEquipmentGroupsWithUser() {
        Set<String> groupSet = new HashSet<>();

        // 1. Группы из БД1 (справочник)
        List<String> refGroups = getAllEquipmentGroups();
        for (String g : refGroups) {
            if (g != null && !g.isEmpty()) {
                groupSet.add(g.toUpperCase()); // 🔥 ВЕРХНИЙ РЕГИСТР
            }
        }

        // 2. Группы из БД2 (пользовательские уставки) - только активные (is_deleted != 1)
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_setpoints",
                new String[]{"equipment_group"},
                "equipment_group IS NOT NULL AND equipment_group != '' AND is_deleted != 1",
                null, null, null,
                "equipment_group ASC"
        );

        while (cursor.moveToNext()) {
            String group = cursor.getString(0);
            if (group != null && !group.isEmpty()) {
                // 🔥 ПРИВОДИМ К ВЕРХНЕМУ РЕГИСТРУ
                groupSet.add(group.toUpperCase());
            }
        }
        cursor.close();

        // Сортируем
        List<String> result = new ArrayList<>(groupSet);
        Collections.sort(result);
        return result;
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
    public void logUserSensors() {
        Log.d("DB_CHECK", "=== logUserSensors START ===");
        SQLiteDatabase db = userDbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                "user_sensors",
                null, null, null, null, null,
                "st_marking ASC"
        );

        Log.d("DB_CHECK", "user_sensors count = " + cursor.getCount());
        while (cursor.moveToNext()) {
            String stMarking = cursor.getString(cursor.getColumnIndexOrThrow("st_marking"));
            String kks = cursor.getString(cursor.getColumnIndexOrThrow("kks"));
            String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            int isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted"));
            Log.d("DB_CHECK", "  sensor: stMarking=" + stMarking +
                    ", kks=" + kks +
                    ", fullName=" + fullName +
                    ", isDeleted=" + isDeleted);
        }
        cursor.close();
        Log.d("DB_CHECK", "=== logUserSensors END ===");
    }
    @Override
    public List<Setpoint> searchSetpointsByGroupWithUser(String group) {
        Log.d("SEARCH_SETPOINT", "=== searchSetpointsByGroupWithUser ===");
        Log.d("SEARCH_SETPOINT", "group = " + group);

        List<Setpoint> result = new ArrayList<>();
        Set<Integer> seenIds = new HashSet<>();

        try {
            // 🔥 1. Ищем в БД2 (пользовательские) по группе
            List<Setpoint> userResults = searchUserSetpointsByGroup(group);
            Log.d("SEARCH_SETPOINT", "userResults size = " + userResults.size());

            for (Setpoint setpoint : userResults) {
                if (setpoint.getIsDeleted() != 1) {
                    result.add(setpoint);
                    int key = setpoint.getOriginalId() > 0 ? setpoint.getOriginalId() : setpoint.getId();
                    seenIds.add(key);
                    Log.d("SEARCH_SETPOINT", "  added user: " + setpoint.getPositionName() + " (group=" + setpoint.getEquipmentGroup() + ")");
                }
            }

            // 🔥 2. Ищем в БД1 (справочник) по группе
            List<Setpoint> refResults = searchSetpointsByGroup(group);
            Log.d("SEARCH_SETPOINT", "refResults size = " + refResults.size());

            for (Setpoint setpoint : refResults) {
                // Проверяем, не удалена ли эта уставка в БД2
                Setpoint deleted = getUserSetpointByOriginalId(setpoint.getId());
                if (deleted != null && deleted.getIsDeleted() == 1) {
                    Log.d("SEARCH_SETPOINT", "  skipping deleted: " + setpoint.getPositionName());
                    continue;
                }

                if (!seenIds.contains(setpoint.getId())) {
                    result.add(setpoint);
                    seenIds.add(setpoint.getId());
                    Log.d("SEARCH_SETPOINT", "  added ref: " + setpoint.getPositionName() + " (group=" + setpoint.getEquipmentGroup() + ")");
                }
            }

            Log.d("SEARCH_SETPOINT", "result size = " + result.size());
            return result;

        } catch (Exception e) {
            Log.e("SEARCH_SETPOINT", "Error searching setpoints by group", e);
            return new ArrayList<>();
        }
    }
    @Override
    public List<Setpoint> searchUserSetpointsByGroup(String group) {
        List<Setpoint> setpoints = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();

        // Убираем # из запроса для поиска
        String cleanGroup = group.replaceFirst("^#", "").trim();
        String searchPattern = "%" + cleanGroup + "%";

        Log.d("SEARCH_USER_SETPOINT", "=== searchUserSetpointsByGroup ===");
        Log.d("SEARCH_USER_SETPOINT", "cleanGroup = " + cleanGroup);
        Log.d("SEARCH_USER_SETPOINT", "searchPattern = " + searchPattern);

        // Ищем в equipment_group (без учёта регистра)
        String selection = "LOWER(equipment_group) LIKE LOWER(?)";
        String[] args = new String[]{searchPattern};

        Cursor cursor = db.query(
                "user_setpoints",
                null,
                selection,
                args,
                null, null,
                "position_name ASC"
        );

        while (cursor.moveToNext()) {
            Setpoint setpoint = cursorToUserSetpoint(cursor);
            setpoints.add(setpoint);
            Log.d("SEARCH_USER_SETPOINT", "  found: " + setpoint.getPositionName() + " (group=" + setpoint.getEquipmentGroup() + ")");
        }
        cursor.close();

        Log.d("SEARCH_USER_SETPOINT", "total found = " + setpoints.size());
        return setpoints;
    }

}