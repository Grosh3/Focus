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
    private static final java.util.Set<String> STOP_WORDS = new java.util.HashSet<>(java.util.Arrays.asList(
            "в", "во", "на", "и", "с", "со", "к", "ко", "от", "по", "за", "из",
            "до", "у", "о", "об", "для", "при", "над", "под", "без", "через",
            "не", "а", "но", "или", "№"
    ));
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

        String cleanQuery = query.replaceAll("[\\s\\-\\.\\u2013\\u2014]", "");
        boolean isShortQuery = cleanQuery.length() < 3;
        boolean isKksQuery = containsLatin(cleanQuery);
        boolean hasDigit = cleanQuery.matches(".*[0-9].*");

        String isyClean =
                "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(isy), ' ', ''), '-', ''), '–', ''), '—', ''), '.', '')";
        String nameClean =
                "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(name), ' ', ''), '-', ''), '–', ''), '—', ''), '.', '')";
        String powerCabinetClean =
                "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(power_cabinet), ' ', ''), '-', ''), '–', ''), '—', ''), '.', '')";
        String kksClean =
                "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(kks), ' ', ''), '-', ''), '–', ''), '—', ''), '.', '')";

        // === KKS ===
        if (isKksQuery) {
            String kksQuery = buildKksQuery(cleanQuery);
            String selection = "(" + kksClean + " LIKE LOWER(?) OR " + kksClean + " LIKE LOWER(?))";
            Cursor cursor = db.query(
                    DatabaseContract.GateValvesEntry.TABLE_NAME, null, selection,
                    new String[]{"%" + cleanQuery + "%", "%" + kksQuery + "%"},
                    null, null, null);
            while (cursor.moveToNext()) valves.add(cursorToGateValve(cursor));
            cursor.close();
            return valves;
        }

        // === КОРОТКИЙ ===
        if (isShortQuery) {
            String selection = "(" + isyClean + " LIKE LOWER(?)"
                    + " OR " + nameClean + " LIKE LOWER(?)"
                    + " OR " + powerCabinetClean + " LIKE LOWER(?))";
            Cursor cursor = db.query(
                    DatabaseContract.GateValvesEntry.TABLE_NAME, null, selection,
                    new String[]{cleanQuery + "%", cleanQuery + "%", cleanQuery + "%"},
                    null, null, null);
            while (cursor.moveToNext()) valves.add(cursorToGateValve(cursor));
            cursor.close();
            return valves;
        }

        // === КОДОВЫЙ (<=6) ===
        if (cleanQuery.length() <= 6) {
            if (hasDigit) {
                // Есть цифры — префикс
                String selection = "(" + isyClean + " LIKE LOWER(?)"
                        + " OR " + nameClean + " LIKE LOWER(?)"
                        + " OR " + powerCabinetClean + " LIKE LOWER(?))";
                Cursor cursor = db.query(
                        DatabaseContract.GateValvesEntry.TABLE_NAME, null, selection,
                        new String[]{cleanQuery + "%", cleanQuery + "%", cleanQuery + "%"},
                        null, null, null);
                while (cursor.moveToNext()) valves.add(cursorToGateValve(cursor));
                cursor.close();

                if (!valves.isEmpty()) return valves;

            } else {
                // Без цифр — сначала точное, потом префикс
                String exactSelection = "(" + isyClean + " = LOWER(?)"
                        + " OR " + nameClean + " = LOWER(?)"
                        + " OR " + powerCabinetClean + " = LOWER(?))";
                Cursor cursor = db.query(
                        DatabaseContract.GateValvesEntry.TABLE_NAME, null, exactSelection,
                        new String[]{cleanQuery, cleanQuery, cleanQuery},
                        null, null, null);
                while (cursor.moveToNext()) valves.add(cursorToGateValve(cursor));
                cursor.close();

                if (!valves.isEmpty()) return valves;

                String prefixSelection = "(" + isyClean + " LIKE LOWER(?)"
                        + " OR " + nameClean + " LIKE LOWER(?)"
                        + " OR " + powerCabinetClean + " LIKE LOWER(?))";
                cursor = db.query(
                        DatabaseContract.GateValvesEntry.TABLE_NAME, null, prefixSelection,
                        new String[]{cleanQuery + "%", cleanQuery + "%", cleanQuery + "%"},
                        null, null, null);
                while (cursor.moveToNext()) valves.add(cursorToGateValve(cursor));
                cursor.close();

                if (!valves.isEmpty()) return valves;
            }
            // Если пусто — фразовый fallback
        }

        // === ФРАЗОВЫЙ (только full_name) ===
        List<String> prefixes = buildWordPrefixes(query);

        if (prefixes.isEmpty()) {
            String selection = "LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_FULL_NAME + ") LIKE LOWER(?)";
            Cursor cursor = db.query(
                    DatabaseContract.GateValvesEntry.TABLE_NAME, null, selection,
                    new String[]{"%" + cleanQuery + "%"},
                    null, null, null);
            while (cursor.moveToNext()) valves.add(cursorToGateValve(cursor));
            cursor.close();
            return valves;
        }

        int n = prefixes.size();
        String fullNameCond = buildFieldCondition(DatabaseContract.GateValvesEntry.COLUMN_FULL_NAME, n);

        List<String> argList = new ArrayList<>();
        for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");

        Cursor cursor = db.query(
                DatabaseContract.GateValvesEntry.TABLE_NAME, null,
                fullNameCond,
                argList.toArray(new String[0]),
                null, null, null);
        while (cursor.moveToNext()) valves.add(cursorToGateValve(cursor));
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
    @Override
    public GateValve getAnyUserGateValveByOriginalId(int originalId) {
        if (originalId <= 0) return null;
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_gate_valves",
                null,
                "original_id = ?",
                new String[]{String.valueOf(originalId)},
                null, null,
                "is_deleted ASC, edited_at DESC, id DESC LIMIT 1"
        );
        GateValve valve = null;
        if (cursor.moveToFirst()) {
            valve = cursorToUserGateValve(cursor);
        }
        cursor.close();
        return valve;
    }

    private int generateNegativeIdForValve() {
        int minId = 0;
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MIN(id) FROM user_gate_valves", null);
        if (c.moveToFirst() && !c.isNull(0)) {
            minId = c.getInt(0);
        }
        c.close();
        int newId = minId - 1;
        if (newId == 0) newId = -1;
        return newId;
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
        boolean isKksQuery = containsLatin(cleanQuery);
        boolean isMarking = isMarkingQuery(cleanQuery);

        String selection;
        List<String> argList = new ArrayList<>();

        if (isShortQuery) {
            // Короткий запрос — st_marking + name по cleanQuery
            selection =
                    "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                            "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_NAME + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";

            argList.add("%" + cleanQuery + "%");
            argList.add("%" + cleanQuery + "%");

        } else if (isKksQuery) {
            // KKS-запрос — только по kks
            String kksQuery = buildKksQuery(cleanQuery);

            selection =
                    "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_KKS + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                            "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_KKS + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";

            argList.add("%" + cleanQuery + "%");
            argList.add("%" + kksQuery + "%");

        } else if (isMarking) {
            // Режим маркировки — только st_marking по cleanQuery
            selection =
                    "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";

            argList.add("%" + cleanQuery + "%");

        } else {
            // Фразовый режим: st_marking по cleanQuery, остальные — префиксно
            List<String> prefixes = buildWordPrefixes(query);
            if (prefixes.isEmpty()) {
                return sensors;
            }

            int n = prefixes.size();

            String stMarkingCond =
                    "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            String nameCond = buildFieldCondition(DatabaseContract.SensorScheduleEntry.COLUMN_NAME, n);
            String fullNameCond = buildFieldCondition(DatabaseContract.SensorScheduleEntry.COLUMN_FULL_NAME, n);
            String locationCond = buildFieldCondition(DatabaseContract.SensorScheduleEntry.COLUMN_LOCATION, n);

            selection = "(" + stMarkingCond + " OR " + nameCond + " OR " + fullNameCond + " OR " + locationCond + ")";

            argList.add("%" + cleanQuery + "%");   // st_marking
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");  // name
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");  // full_name
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");  // location
        }

        Cursor cursor = db.query(
                DatabaseContract.SensorScheduleEntry.TABLE_NAME,
                null,
                selection,
                argList.toArray(new String[0]),
                null, null,
                null
        );

        while (cursor.moveToNext()) {
            sensors.add(cursorToSensor(cursor));
        }
        cursor.close();

        return sortSensorsByRelevance(sensors, cleanQuery);
    }



    private int getSensorRelevanceScore(Sensor sensor, String query, String kksQuery) {
        String stMarking = sensor.getStMarkir() != null ? sensor.getStMarkir().toLowerCase() : "";
        String fullName = sensor.getFullName() != null ? sensor.getFullName().toLowerCase() : "";
        String name = sensor.getName() != null ? sensor.getName().toLowerCase() : "";
        String kks = sensor.getKks() != null ? sensor.getKks().toLowerCase() : "";

        // Полное совпадение
        if (stMarking.equals(query)) return 1;
        if (fullName.equals(query)) return 2;
        if (name.equals(query)) return 3;
        if (kks.equals(kksQuery)) return 4;
        if (kks.equals(query)) return 5;

        // Частичное совпадение
        if (stMarking.contains(query)) return 10;
        if (fullName.contains(query)) return 20;
        if (name.contains(query)) return 30;
        if (kks.contains(kksQuery)) return 40;
        if (kks.contains(query)) return 50;

        // Совпадение в начале
        if (stMarking.startsWith(query)) return 11;
        if (fullName.startsWith(query)) return 21;
        if (kks.startsWith(kksQuery)) return 41;

        return 100;
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
    @Override
    public Sensor getAnyUserSensorByOriginalId(int originalId) {
        if (originalId <= 0) return null;
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_sensors",
                null,
                "original_id = ?",
                new String[]{String.valueOf(originalId)},
                null, null,
                "is_deleted ASC, edited_at DESC, id DESC LIMIT 1"
        );
        Sensor sensor = null;
        if (cursor.moveToFirst()) {
            sensor = cursorToUserSensor(cursor);
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

        String trimmed = query.trim();

        // Ветка 1: #все
        if (trimmed.equalsIgnoreCase("#все")) {
            Cursor c = db.query(
                    DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                    null, null, null, null, null,
                    DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP + " ASC, " +
                            DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + " ASC"
            );
            while (c.moveToNext()) {
                setpoints.add(cursorToSetpoint(c));
            }
            c.close();
            return setpoints;
        }

        // Ветка 2: #группа
        if (trimmed.startsWith("#")) {
            String groupQuery = trimmed.toLowerCase();
            if (groupQuery.length() <= 1) {
                return setpoints;
            }
            Cursor c = db.query(
                    DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                    null,
                    "LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP + ") LIKE LOWER(?)",
                    new String[]{"%" + groupQuery + "%"},
                    null, null,
                    DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + " ASC"
            );
            while (c.moveToNext()) {
                setpoints.add(cursorToSetpoint(c));
            }
            c.close();
            return setpoints;
        }

        // Ветка 3-6: обычный поиск
        String cleanQuery = query.replaceAll("[\\s\\-.]", "");
        boolean isShortQuery = cleanQuery.length() < 3;
        boolean isMarking = isMarkingQuery(cleanQuery);

        String selection;
        List<String> argList = new ArrayList<>();

        if (isShortQuery) {
            // Короткий — position_name + name по cleanQuery
            selection =
                    "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                            "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_NAME + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";

            argList.add("%" + cleanQuery + "%");
            argList.add("%" + cleanQuery + "%");

        } else if (isMarking) {
            // Маркировка — только position_name по cleanQuery
            selection =
                    "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";

            argList.add("%" + cleanQuery + "%");

        } else {
            // Фраза: position_name по cleanQuery + префикс по name/operation/notes/location
            List<String> prefixes = buildWordPrefixes(query);
            if (prefixes.isEmpty()) {
                return setpoints;
            }

            int n = prefixes.size();

            String posNameCond =
                    "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            String nameCond = buildFieldCondition(DatabaseContract.SetpointScheduleEntry.COLUMN_NAME, n);
            String operationCond = buildFieldCondition(DatabaseContract.SetpointScheduleEntry.COLUMN_OPERATION, n);
            String notesCond = buildFieldCondition(DatabaseContract.SetpointScheduleEntry.COLUMN_NOTES, n);
            String locationCond = buildFieldCondition(DatabaseContract.SetpointScheduleEntry.COLUMN_LOCATION, n);

            selection = "(" + posNameCond + " OR " + nameCond + " OR " + operationCond
                    + " OR " + notesCond + " OR " + locationCond + ")";

            argList.add("%" + cleanQuery + "%");   // position_name
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");  // name
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");  // operation
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");  // notes
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");  // location
        }

        Cursor cursor = db.query(
                DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                null,
                selection,
                argList.toArray(new String[0]),
                null, null,
                null
        );

        while (cursor.moveToNext()) {
            setpoints.add(cursorToSetpoint(cursor));
        }
        cursor.close();

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

    /**
     * Разбивает поисковый запрос на префиксы слов.
     * Служебные слова и слишком короткие токены игнорируются.
     * Для слов длиной 5-6 — отрезается 1 символ, 7+ — 2 символа.
     */
    private List<String> buildWordPrefixes(String query) {
        List<String> prefixes = new ArrayList<>();
        if (query == null) return prefixes;

        String lower = query.toLowerCase().trim();
        if (lower.isEmpty()) return prefixes;

        String[] words = lower.split("\\s+");
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (STOP_WORDS.contains(word)) continue;
            if (word.length() <= 2) continue;

            String prefix = word;
            if (word.length() >= 7) {
                prefix = word.substring(0, word.length() - 2);
            } else if (word.length() >= 5) {
                prefix = word.substring(0, word.length() - 1);
            }
            if (!prefix.isEmpty()) {
                prefixes.add(prefix);
            }
        }
        return prefixes;
    }

    /**
     * Собирает SQL-условие для одного поля:
     * (LOWER(field) LIKE LOWER(?) AND LOWER(field) LIKE LOWER(?) ...)
     */
    private String buildFieldCondition(String field, int prefixCount) {
        if (prefixCount <= 0) return "";
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < prefixCount; i++) {
            if (i > 0) sb.append(" AND ");
            sb.append("LOWER(").append(field).append(") LIKE LOWER(?)");
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Вычисляет kksQuery — запрос с отрезанным префиксом.
     * ^[A-Za-z][0-9]+ → отрезается; ^[0-9]+ → отрезается.
     * Остаток должен быть >= 3 символов.
     */
    private String buildKksQuery(String cleanQuery) {
        String kksQuery = cleanQuery;

        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^[A-Za-z][0-9]+")
                .matcher(kksQuery);
        if (m.find()) {
            String rest = kksQuery.substring(m.end());
            if (rest.length() >= 3) {
                return rest;
            }
            return kksQuery;
        }

        String rest = kksQuery.replaceFirst("^[0-9]+", "");
        if (!rest.isEmpty() && rest.length() >= 3) {
            return rest;
        }
        return kksQuery;
    }

    /**
     * Проверяет, содержит ли строка латиницу.
     */
    private boolean containsLatin(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                return true;
            }
        }
        return false;
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

        String cleanQuery = query.replaceAll("[\\s\\-\\.\\u2013\\u2014]", "");
        boolean isShortQuery = cleanQuery.length() < 3;
        boolean isKksQuery = containsLatin(cleanQuery);
        boolean hasDigit = cleanQuery.matches(".*[0-9].*");

        String isyClean =
                "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(isy), ' ', ''), '-', ''), '–', ''), '—', ''), '.', '')";
        String nameClean =
                "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(name), ' ', ''), '-', ''), '–', ''), '—', ''), '.', '')";
        String powerCabinetClean =
                "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(power_cabinet), ' ', ''), '-', ''), '–', ''), '—', ''), '.', '')";
        String kksClean =
                "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(kks), ' ', ''), '-', ''), '–', ''), '—', ''), '.', '')";

        // === KKS ===
        if (isKksQuery) {
            String kksQuery = buildKksQuery(cleanQuery);
            String selection = "(" + kksClean + " LIKE LOWER(?) OR " + kksClean + " LIKE LOWER(?))";
            Cursor cursor = db.query(
                    "user_gate_valves", null, selection,
                    new String[]{"%" + cleanQuery + "%", "%" + kksQuery + "%"},
                    null, null, null);
            while (cursor.moveToNext()) valves.add(cursorToUserGateValve(cursor));
            cursor.close();
            return valves;
        }

        // === КОРОТКИЙ ===
        if (isShortQuery) {
            String selection = "(" + isyClean + " LIKE LOWER(?)"
                    + " OR " + nameClean + " LIKE LOWER(?)"
                    + " OR " + powerCabinetClean + " LIKE LOWER(?))";
            Cursor cursor = db.query(
                    "user_gate_valves", null, selection,
                    new String[]{cleanQuery + "%", cleanQuery + "%", cleanQuery + "%"},
                    null, null, null);
            while (cursor.moveToNext()) valves.add(cursorToUserGateValve(cursor));
            cursor.close();
            return valves;
        }

        // === КОДОВЫЙ (<=6) ===
        if (cleanQuery.length() <= 6) {
            if (hasDigit) {
                String selection = "(" + isyClean + " LIKE LOWER(?)"
                        + " OR " + nameClean + " LIKE LOWER(?)"
                        + " OR " + powerCabinetClean + " LIKE LOWER(?))";
                Cursor cursor = db.query(
                        "user_gate_valves", null, selection,
                        new String[]{cleanQuery + "%", cleanQuery + "%", cleanQuery + "%"},
                        null, null, null);
                while (cursor.moveToNext()) valves.add(cursorToUserGateValve(cursor));
                cursor.close();

                if (!valves.isEmpty()) return valves;

            } else {
                String exactSelection = "(" + isyClean + " = LOWER(?)"
                        + " OR " + nameClean + " = LOWER(?)"
                        + " OR " + powerCabinetClean + " = LOWER(?))";
                Cursor cursor = db.query(
                        "user_gate_valves", null, exactSelection,
                        new String[]{cleanQuery, cleanQuery, cleanQuery},
                        null, null, null);
                while (cursor.moveToNext()) valves.add(cursorToUserGateValve(cursor));
                cursor.close();

                if (!valves.isEmpty()) return valves;

                String prefixSelection = "(" + isyClean + " LIKE LOWER(?)"
                        + " OR " + nameClean + " LIKE LOWER(?)"
                        + " OR " + powerCabinetClean + " LIKE LOWER(?))";
                cursor = db.query(
                        "user_gate_valves", null, prefixSelection,
                        new String[]{cleanQuery + "%", cleanQuery + "%", cleanQuery + "%"},
                        null, null, null);
                while (cursor.moveToNext()) valves.add(cursorToUserGateValve(cursor));
                cursor.close();

                if (!valves.isEmpty()) return valves;
            }
        }

        // === ФРАЗОВЫЙ (только full_name) ===
        List<String> prefixes = buildWordPrefixes(query);

        if (prefixes.isEmpty()) {
            String selection = "LOWER(full_name) LIKE LOWER(?)";
            Cursor cursor = db.query(
                    "user_gate_valves", null, selection,
                    new String[]{"%" + cleanQuery + "%"},
                    null, null, null);
            while (cursor.moveToNext()) valves.add(cursorToUserGateValve(cursor));
            cursor.close();
            return valves;
        }

        int n = prefixes.size();
        String fullNameCond = buildFieldCondition("full_name", n);

        List<String> argList = new ArrayList<>();
        for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");

        Cursor cursor = db.query(
                "user_gate_valves", null,
                fullNameCond,
                argList.toArray(new String[0]),
                null, null, null);
        while (cursor.moveToNext()) valves.add(cursorToUserGateValve(cursor));
        cursor.close();

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
        if (originalId <= 0) {
            Log.e(TAG, "markGateValveAsDeleted: invalid originalId=" + originalId);
            return;
        }

        GateValve anyExisting = getAnyUserGateValveByOriginalId(originalId);
        if (anyExisting != null) {
            if (anyExisting.getIsDeleted() == 1) {
                Log.d(TAG, "markGateValveAsDeleted: already deleted, id=" + anyExisting.getId());
                return;
            }
            anyExisting.setIsDeleted(1);
            anyExisting.setEditedAtValve(getCurrentDateTime());
            updateUserGateValve(anyExisting);
            Log.d(TAG, "markGateValveAsDeleted: marked id=" + anyExisting.getId()
                    + " (original_id=" + originalId + ") as deleted");
            return;
        }

        GateValve ref = getGateValveById(originalId);
        if (ref == null) {
            Log.e(TAG, "markGateValveAsDeleted: ref not found, originalId=" + originalId);
            return;
        }

        GateValve copy = new GateValve();
        copy.setId(generateNegativeIdForValve());
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
        copy.setEditedAtValve(getCurrentDateTime());
        copy.setCreatedAt(getCurrentDateTime());

        long id = insertUserGateValve(copy);
        Log.d(TAG, "markGateValveAsDeleted: inserted new tombstone id=" + id
                + " for original_id=" + originalId);
    }

    @Override
    public List<GateValve> getAllGateValvesWithUser() {
        List<GateValve> result = new ArrayList<>();

        for (GateValve v : getAllUserGateValves()) {
            if (v.getIsDeleted() != 1) {
                result.add(v);
            }
        }

        Set<Integer> overriddenIds = new HashSet<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT original_id FROM user_gate_valves WHERE original_id > 0",
                null);
        while (c.moveToNext()) {
            overriddenIds.add(c.getInt(0));
        }
        c.close();

        for (GateValve ref : getAllGateValves()) {
            if (overriddenIds.contains(ref.getId())) continue;
            result.add(ref);
        }

        return result;
    }

    @Override
    public List<GateValve> searchGateValvesWithUser(String query) {
        List<GateValve> result = new ArrayList<>();

        for (GateValve v : searchUserGateValves(query)) {
            if (v.getIsDeleted() != 1) {
                result.add(v);
            }
        }

        Set<Integer> overriddenIds = new HashSet<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT original_id FROM user_gate_valves WHERE original_id > 0",
                null);
        while (c.moveToNext()) {
            overriddenIds.add(c.getInt(0));
        }
        c.close();

        for (GateValve ref : searchGateValves(query)) {
            if (overriddenIds.contains(ref.getId())) continue;
            result.add(ref);
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

    private List<GateValve> sortGateValvesByRelevance(List<GateValve> valves, String query) {
        String cleanQuery = query.replaceAll("[\\s\\-\\.\\u2013\\u2014]", "").toLowerCase();
        if (cleanQuery.isEmpty()) return valves;

        Collections.sort(valves, (a, b) -> {
            int scoreA = getValveRelevanceScore(a, cleanQuery);
            int scoreB = getValveRelevanceScore(b, cleanQuery);
            if (scoreA != scoreB) return Integer.compare(scoreA, scoreB);

            // Дополнительно: короче isy — выше
            int lenA = a.getIsy() != null ? a.getIsy().length() : Integer.MAX_VALUE;
            int lenB = b.getIsy() != null ? b.getIsy().length() : Integer.MAX_VALUE;
            return Integer.compare(lenA, lenB);
        });

        return valves;
    }

    private int getValveRelevanceScore(GateValve valve, String cleanQuery) {
        String isy = valve.getIsy() != null
                ? valve.getIsy().toLowerCase().replaceAll("[\\s\\-\\.\\u2013\\u2014]", "")
                : "";
        String name = valve.getName() != null
                ? valve.getName().toLowerCase().replaceAll("[\\s\\-\\.\\u2013\\u2014]", "")
                : "";
        String fullName = valve.getFullName() != null ? valve.getFullName().toLowerCase() : "";
        String powerCabinet = valve.getPowerCabinet() != null ? valve.getPowerCabinet().toLowerCase() : "";
        String locationDesc = valve.getLocationDescription() != null ? valve.getLocationDescription().toLowerCase() : "";
        String onPlace = valve.getOnPlace() != null ? valve.getOnPlace().toLowerCase() : "";

        // 1. Точное совпадение isy / name
        if (isy.equals(cleanQuery)) return 1;
        if (name.equals(cleanQuery)) return 2;

        // 2. Префикс isy / name
        if (isy.startsWith(cleanQuery)) return 10;
        if (name.startsWith(cleanQuery)) return 11;

        // 3. Подстрока isy / name
        if (isy.contains(cleanQuery)) return 20;
        if (name.contains(cleanQuery)) return 21;

        // 4. Другие поля
        if (fullName.contains(cleanQuery)) return 30;
        if (powerCabinet.contains(cleanQuery)) return 31;
        if (locationDesc.contains(cleanQuery)) return 32;
        if (onPlace.contains(cleanQuery)) return 33;

        return 100;
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
        boolean isShortQuery = cleanQuery.length() < 3;
        boolean isKksQuery = containsLatin(cleanQuery);
        boolean isMarking = isMarkingQuery(cleanQuery);

        String selection;
        List<String> argList = new ArrayList<>();

        if (isShortQuery) {
            selection =
                    "REPLACE(REPLACE(REPLACE(LOWER(st_marking), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                            "REPLACE(REPLACE(REPLACE(LOWER(name), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";

            argList.add("%" + cleanQuery + "%");
            argList.add("%" + cleanQuery + "%");

        } else if (isKksQuery) {
            String kksQuery = buildKksQuery(cleanQuery);

            selection =
                    "REPLACE(REPLACE(REPLACE(LOWER(kks), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                            "REPLACE(REPLACE(REPLACE(LOWER(kks), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";

            argList.add("%" + cleanQuery + "%");
            argList.add("%" + kksQuery + "%");

        } else if (isMarking) {
            selection =
                    "REPLACE(REPLACE(REPLACE(LOWER(st_marking), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";

            argList.add("%" + cleanQuery + "%");

        } else {
            List<String> prefixes = buildWordPrefixes(query);
            if (prefixes.isEmpty()) {
                return sensors;
            }

            int n = prefixes.size();

            String stMarkingCond =
                    "REPLACE(REPLACE(REPLACE(LOWER(st_marking), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            String nameCond = buildFieldCondition("name", n);
            String fullNameCond = buildFieldCondition("full_name", n);
            String locationCond = buildFieldCondition("installation_location", n);

            selection = "(" + stMarkingCond + " OR " + nameCond + " OR " + fullNameCond + " OR " + locationCond + ")";

            argList.add("%" + cleanQuery + "%");   // st_marking
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");  // name
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");  // full_name
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");  // location
        }

        Cursor cursor = db.query(
                "user_sensors",
                null,
                selection,
                argList.toArray(new String[0]),
                null, null,
                "st_marking ASC"
        );

        Log.d("SEARCH_USER", "cursor count = " + cursor.getCount());

        while (cursor.moveToNext()) {
            Sensor sensor = cursorToUserSensor(cursor);
            sensors.add(sensor);
        }
        cursor.close();

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
    public List<Sensor> searchSensorsWithUser(String query) {
        List<Sensor> result = new ArrayList<>();

        // 1. Живые дочки по запросу
        for (Sensor s : searchUserSensors(query)) {
            if (s.getIsDeleted() != 1) {
                result.add(s);
            }
        }

        // 2. Множество original_id из ВСЕХ дочек (включая удалённые)
        Set<Integer> overriddenIds = new HashSet<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT original_id FROM user_sensors WHERE original_id > 0",
                null);
        while (c.moveToNext()) {
            overriddenIds.add(c.getInt(0));
        }
        c.close();

        // 3. Справочные по запросу, минус перекрытые
        for (Sensor ref : searchSensors(query)) {
            if (overriddenIds.contains(ref.getId())) continue;
            result.add(ref);
        }

        return result;
    }
    @Override
    public List<Sensor> getAllSensorsWithUser() {
        List<Sensor> result = new ArrayList<>();

        // 1. Все живые дочки
        for (Sensor s : getAllUserSensors()) {
            if (s.getIsDeleted() != 1) {
                result.add(s);
            }
        }

        // 2. Множество original_id из всех дочек
        Set<Integer> overriddenIds = new HashSet<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT original_id FROM user_sensors WHERE original_id > 0",
                null);
        while (c.moveToNext()) {
            overriddenIds.add(c.getInt(0));
        }
        c.close();

        // 3. Все справочные, минус перекрытые
        for (Sensor ref : getAllSensors()) {
            if (overriddenIds.contains(ref.getId())) continue;
            result.add(ref);
        }

        return result;
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

        String trimmed = query.trim();

        // #все
        if (trimmed.equalsIgnoreCase("#все")) {
            Cursor c = db.query(
                    "user_setpoints",
                    null, null, null, null, null,
                    "equipment_group ASC, position_name ASC"
            );
            while (c.moveToNext()) {
                setpoints.add(cursorToUserSetpoint(c));
            }
            c.close();
            return setpoints;
        }

        // #группа
        if (trimmed.startsWith("#")) {
            String groupQuery = trimmed.toLowerCase();
            if (groupQuery.length() <= 1) {
                return setpoints;
            }
            Cursor c = db.query(
                    "user_setpoints",
                    null,
                    "LOWER(equipment_group) LIKE LOWER(?)",
                    new String[]{"%" + groupQuery + "%"},
                    null, null,
                    "position_name ASC"
            );
            while (c.moveToNext()) {
                setpoints.add(cursorToUserSetpoint(c));
            }
            c.close();
            return setpoints;
        }

        // Обычный поиск
        String cleanQuery = query.replaceAll("[\\s\\-.]", "");
        boolean isShortQuery = cleanQuery.length() < 3;
        boolean isMarking = isMarkingQuery(cleanQuery);

        String selection;
        List<String> argList = new ArrayList<>();

        if (isShortQuery) {
            selection =
                    "REPLACE(REPLACE(REPLACE(LOWER(position_name), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR " +
                            "REPLACE(REPLACE(REPLACE(LOWER(name), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";

            argList.add("%" + cleanQuery + "%");
            argList.add("%" + cleanQuery + "%");

        } else if (isMarking) {
            selection =
                    "REPLACE(REPLACE(REPLACE(LOWER(position_name), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";

            argList.add("%" + cleanQuery + "%");

        } else {
            List<String> prefixes = buildWordPrefixes(query);
            if (prefixes.isEmpty()) {
                return setpoints;
            }

            int n = prefixes.size();

            String posNameCond =
                    "REPLACE(REPLACE(REPLACE(LOWER(position_name), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            String nameCond = buildFieldCondition("name", n);
            String operationCond = buildFieldCondition("operation", n);
            String notesCond = buildFieldCondition("notes", n);
            String locationCond = buildFieldCondition("location", n);

            selection = "(" + posNameCond + " OR " + nameCond + " OR " + operationCond
                    + " OR " + notesCond + " OR " + locationCond + ")";

            argList.add("%" + cleanQuery + "%");   // position_name
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");  // name
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");  // operation
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");  // notes
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");  // location
        }

        Cursor cursor = db.query(
                "user_setpoints",
                null,
                selection,
                argList.toArray(new String[0]),
                null, null,
                null
        );

        while (cursor.moveToNext()) {
            setpoints.add(cursorToUserSetpoint(cursor));
        }
        cursor.close();

        return sortSetpointsByRelevance(setpoints, query);
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
        if (originalId <= 0) {
            Log.e(TAG, "markSetpointAsDeleted: invalid originalId=" + originalId);
            return;
        }

        // 1. Проверяем ЛЮБУЮ дочку (включая удалённую) — чтобы не плодить дубли
        Setpoint anyExisting = getAnyUserSetpointByOriginalId(originalId);
        if (anyExisting != null) {
            if (anyExisting.getIsDeleted() == 1) {
                // Уже удалена — ничего не делаем, чтобы не затирать edited_at
                Log.d(TAG, "markSetpointAsDeleted: already deleted, id=" + anyExisting.getId());
                return;
            }
            // Живая дочка — помечаем удалённой
            anyExisting.setIsDeleted(1);
            anyExisting.setEditedAt(getCurrentDateTime());
            updateUserSetpoint(anyExisting);
            Log.d(TAG, "markSetpointAsDeleted: marked id=" + anyExisting.getId()
                    + " (original_id=" + originalId + ") as deleted");
            return;
        }

        // 2. Дочки нет — берём мать из БД1
        Setpoint ref = getSetpointById(originalId);
        if (ref == null) {
            Log.e(TAG, "markSetpointAsDeleted: ref not found, originalId=" + originalId);
            return;
        }

        // 3. Создаём дочку с is_deleted = 1
        Setpoint copy = new Setpoint();
        copy.setId(generateNegativeIdForSetpoint());   // 🔥 обязательно отрицательный id
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

        long id = insertUserSetpoint(copy);
        Log.d(TAG, "markSetpointAsDeleted: inserted new tombstone id=" + id
                + " for original_id=" + originalId);
    }

    @Override
    public List<Setpoint> getAllSetpointsWithUser() {
        List<Setpoint> result = new ArrayList<>();

        // 1. Все живые дочки
        for (Setpoint sp : getAllUserSetpoints()) {
            if (sp.getIsDeleted() != 1) {
                result.add(sp);
            }
        }

        // 2. Множество original_id из всех дочек
        Set<Integer> overriddenIds = new HashSet<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT original_id FROM user_setpoints WHERE original_id > 0",
                null);
        while (c.moveToNext()) {
            overriddenIds.add(c.getInt(0));
        }
        c.close();

        // 3. Все справочные, минус перекрытые
        for (Setpoint ref : getAllSetpoints()) {
            if (overriddenIds.contains(ref.getId())) continue;
            result.add(ref);
        }

        return result;
    }

    @Override
    public List<Setpoint> searchSetpointsWithUser(String query) {
        List<Setpoint> result = new ArrayList<>();

        // Шаг 1. Дочек по запросу — показать (живых)
        for (Setpoint sp : searchUserSetpoints(query)) {
            if (sp.getIsDeleted() != 1) {
                result.add(sp);
            }
        }

        // Шаг 2. Множество original_id из ВСЕХ дочек (и удалённых тоже)
        Set<Integer> overriddenIds = new HashSet<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT original_id FROM user_setpoints WHERE original_id > 0",
                null);
        while (c.moveToNext()) {
            overriddenIds.add(c.getInt(0));
        }
        c.close();

        // Шаг 3. Справочные по запросу, минус те, у кого есть дочка
        for (Setpoint ref : searchSetpoints(query)) {
            if (overriddenIds.contains(ref.getId())) continue;
            result.add(ref);
        }

        return result;
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
        if (originalId <= 0) {
            Log.e(TAG, "markSensorAsDeleted: invalid originalId=" + originalId);
            return;
        }

        Sensor anyExisting = getAnyUserSensorByOriginalId(originalId);
        if (anyExisting != null) {
            if (anyExisting.getIsDeleted() == 1) {
                Log.d(TAG, "markSensorAsDeleted: already deleted, id=" + anyExisting.getId());
                return;
            }
            anyExisting.setIsDeleted(1);
            anyExisting.setEditedAt(getCurrentDateTime());
            updateUserSensor(anyExisting);
            Log.d(TAG, "markSensorAsDeleted: marked id=" + anyExisting.getId()
                    + " (original_id=" + originalId + ") as deleted");
            return;
        }

        Sensor ref = getSensorById(originalId);
        if (ref == null) {
            Log.e(TAG, "markSensorAsDeleted: ref not found, originalId=" + originalId);
            return;
        }

        Sensor copy = new Sensor();
        copy.setId(generateNegativeIdForSensor());
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

        long id = insertUserSensor(copy);
        Log.d(TAG, "markSensorAsDeleted: inserted new tombstone id=" + id
                + " for original_id=" + originalId);
    }

    private int generateNegativeIdForSensor() {
        int minId = 0;
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MIN(id) FROM user_sensors", null);
        if (c.moveToFirst() && !c.isNull(0)) {
            minId = c.getInt(0);
        }
        c.close();
        int newId = minId - 1;
        if (newId == 0) newId = -1;
        return newId;
    }

    private List<Sensor> sortSensorsByRelevance(List<Sensor> sensors, String query) {
        String lowerQuery = query.toLowerCase();
        String kksQuery = buildKksQuery(lowerQuery);

        Collections.sort(sensors, (a, b) -> {
            int scoreA = getSensorRelevanceScore(a, lowerQuery, kksQuery);
            int scoreB = getSensorRelevanceScore(b, lowerQuery, kksQuery);
            return Integer.compare(scoreA, scoreB);
        });

        return sensors;
    }
    /**
     * Проверяет, является ли запрос "маркировкой":
     * начинается с кириллических букв, за которыми идут цифры,
     * и общая длина >= 4. Пример: КИ505а, Мп291, ПТНп13.
     */
    /**
     * Проверяет, является ли запрос "маркировкой":
     * начинается с кириллических букв, за которыми идут цифры,
     * и общая длина >= 3. Пример: КИ5, КИ505а, Мп291, ПТНп13.
     */
    private boolean isMarkingQuery(String cleanQuery) {
        if (cleanQuery == null) return false;
        if (cleanQuery.length() < 3) return false;
        return cleanQuery.matches("^[А-Яа-яЁё]+[0-9].*");
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
    @Override
    public Setpoint getAnyUserSetpointByOriginalId(int originalId) {
        if (originalId <= 0) return null;
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "user_setpoints",
                null,
                "original_id = ?",
                new String[]{String.valueOf(originalId)},
                null, null,
                "is_deleted ASC, edited_at DESC, id DESC LIMIT 1"
        );
        Setpoint setpoint = null;
        if (cursor.moveToFirst()) {
            setpoint = cursorToUserSetpoint(cursor);
        }
        cursor.close();
        return setpoint;
    }
    private int generateNegativeIdForSetpoint() {
        int minId = 0;
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MIN(id) FROM user_setpoints", null);
        if (c.moveToFirst() && !c.isNull(0)) {
            minId = c.getInt(0);
        }
        c.close();
        int newId = minId - 1;
        if (newId == 0) newId = -1;
        return newId;
    }
}

