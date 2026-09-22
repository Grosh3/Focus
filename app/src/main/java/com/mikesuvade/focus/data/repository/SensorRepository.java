package com.mikesuvade.focus.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mikesuvade.focus.data.database.DatabaseContract;
import com.mikesuvade.focus.data.database.DatabaseHelper;
import com.mikesuvade.focus.data.database.UserDatabaseHelper;
import com.mikesuvade.focus.domain.models.Sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class SensorRepository {

    private static final String TAG = "SensorRepo";

    private final DatabaseHelper dbHelper;
    private final UserDatabaseHelper userDbHelper;
    private final CursorMapper mapper;

    SensorRepository(DatabaseHelper dbHelper, UserDatabaseHelper userDbHelper, CursorMapper mapper) {
        this.dbHelper = dbHelper;
        this.userDbHelper = userDbHelper;
        this.mapper = mapper;
    }

    // ==================== СПРАВОЧНИК ====================

    List<Sensor> getAll() {
        List<Sensor> sensors = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.SensorScheduleEntry.TABLE_NAME,
                null, null, null, null, null,
                DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + " ASC"
        );
        while (cursor.moveToNext()) {
            sensors.add(mapper.cursorToSensor(cursor));
        }
        cursor.close();
        return sensors;
    }

    Sensor getByKks(String kks) {
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
            sensor = mapper.cursorToSensor(cursor);
        }
        cursor.close();
        return sensor;
    }

    Sensor getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.SensorScheduleEntry.TABLE_NAME,
                null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null
        );
        Sensor sensor = null;
        if (cursor.moveToFirst()) {
            sensor = mapper.cursorToSensor(cursor);
        }
        cursor.close();
        return sensor;
    }

    List<Sensor> search(String query) {
        List<Sensor> sensors = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String cleanQuery = query.replaceAll("[\\s\\-.]", "");
        boolean isShortQuery = cleanQuery.length() < 3;
        boolean isKksQuery = RepositoryUtils.containsLatin(cleanQuery);
        boolean isMarking = RepositoryUtils.isMarkingQuery(cleanQuery);

        String selection;
        List<String> argList = new ArrayList<>();

        if (isShortQuery) {
            selection = "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR "
                    + "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_NAME + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            argList.add("%" + cleanQuery + "%");
            argList.add("%" + cleanQuery + "%");
        } else if (isKksQuery) {
            String kksQuery = RepositoryUtils.buildKksQuery(cleanQuery);
            selection = "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_KKS + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR "
                    + "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_KKS + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            argList.add("%" + cleanQuery + "%");
            argList.add("%" + kksQuery + "%");
        } else if (isMarking) {
            selection = "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            argList.add("%" + cleanQuery + "%");
        } else {
            List<String> prefixes = RepositoryUtils.buildWordPrefixes(query);
            if (prefixes.isEmpty()) return sensors;

            int n = prefixes.size();
            String stMarkingCond = "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            String nameCond = RepositoryUtils.buildFieldCondition(DatabaseContract.SensorScheduleEntry.COLUMN_NAME, n);
            String fullNameCond = RepositoryUtils.buildFieldCondition(DatabaseContract.SensorScheduleEntry.COLUMN_FULL_NAME, n);
            String locationCond = RepositoryUtils.buildFieldCondition(DatabaseContract.SensorScheduleEntry.COLUMN_LOCATION, n);

            selection = "(" + stMarkingCond + " OR " + nameCond + " OR " + fullNameCond + " OR " + locationCond + ")";

            argList.add("%" + cleanQuery + "%");
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");
        }

        Cursor cursor = db.query(
                DatabaseContract.SensorScheduleEntry.TABLE_NAME,
                null, selection, argList.toArray(new String[0]),
                null, null, null);

        while (cursor.moveToNext()) {
            sensors.add(mapper.cursorToSensor(cursor));
        }
        cursor.close();

        return sortByRelevance(sensors, cleanQuery);
    }

    long insert(Sensor sensor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = mapper.sensorToContentValues(sensor);
        return db.insert(DatabaseContract.SensorScheduleEntry.TABLE_NAME, null, values);
    }

    int update(Sensor sensor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = mapper.sensorToContentValues(sensor);
        return db.update(
                DatabaseContract.SensorScheduleEntry.TABLE_NAME,
                values,
                DatabaseContract.SensorScheduleEntry.COLUMN_KKS + " = ?",
                new String[]{sensor.getKks()}
        );
    }

    int delete(String kks) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.SensorScheduleEntry.TABLE_NAME,
                DatabaseContract.SensorScheduleEntry.COLUMN_KKS + " = ?",
                new String[]{kks}
        );
    }

    // ==================== ПОЛЬЗОВАТЕЛЬСКАЯ ====================

    List<Sensor> getAllUser() {
        List<Sensor> sensors = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_sensors", null, null, null, null, null, "st_marking ASC");
        while (cursor.moveToNext()) {
            sensors.add(mapper.cursorToUserSensor(cursor));
        }
        cursor.close();
        return sensors;
    }

    List<Sensor> searchUser(String query) {
        List<Sensor> sensors = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();

        String cleanQuery = query.replaceAll("[\\s\\-.]", "");
        boolean isShortQuery = cleanQuery.length() < 3;
        boolean isKksQuery = RepositoryUtils.containsLatin(cleanQuery);
        boolean isMarking = RepositoryUtils.isMarkingQuery(cleanQuery);

        String selection;
        List<String> argList = new ArrayList<>();

        if (isShortQuery) {
            selection = "REPLACE(REPLACE(REPLACE(LOWER(st_marking), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR "
                    + "REPLACE(REPLACE(REPLACE(LOWER(name), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            argList.add("%" + cleanQuery + "%");
            argList.add("%" + cleanQuery + "%");
        } else if (isKksQuery) {
            String kksQuery = RepositoryUtils.buildKksQuery(cleanQuery);
            selection = "REPLACE(REPLACE(REPLACE(LOWER(kks), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR "
                    + "REPLACE(REPLACE(REPLACE(LOWER(kks), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            argList.add("%" + cleanQuery + "%");
            argList.add("%" + kksQuery + "%");
        } else if (isMarking) {
            selection = "REPLACE(REPLACE(REPLACE(LOWER(st_marking), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            argList.add("%" + cleanQuery + "%");
        } else {
            List<String> prefixes = RepositoryUtils.buildWordPrefixes(query);
            if (prefixes.isEmpty()) return sensors;

            int n = prefixes.size();
            String stMarkingCond = "REPLACE(REPLACE(REPLACE(LOWER(st_marking), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            String nameCond = RepositoryUtils.buildFieldCondition("name", n);
            String fullNameCond = RepositoryUtils.buildFieldCondition("full_name", n);
            String locationCond = RepositoryUtils.buildFieldCondition("installation_location", n);

            selection = "(" + stMarkingCond + " OR " + nameCond + " OR " + fullNameCond + " OR " + locationCond + ")";
            argList.add("%" + cleanQuery + "%");
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");
        }

        Cursor cursor = db.query("user_sensors", null, selection,
                argList.toArray(new String[0]), null, null, "st_marking ASC");

        while (cursor.moveToNext()) {
            sensors.add(mapper.cursorToUserSensor(cursor));
        }
        cursor.close();

        return sensors;
    }

    long insertUser(Sensor sensor) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = mapper.userSensorToContentValues(sensor);
        return db.insert("user_sensors", null, values);
    }

    int updateUser(Sensor sensor) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = mapper.userSensorToContentValues(sensor);
        return db.update("user_sensors", values, "id = ?",
                new String[]{String.valueOf(sensor.getId())});
    }

    int deleteUser(int id) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        return db.delete("user_sensors", "id = ?", new String[]{String.valueOf(id)});
    }

    Sensor getUserByOriginalId(int originalId) {
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_sensors", null,
                "original_id = ?", new String[]{String.valueOf(originalId)},
                null, null, null);
        Sensor sensor = null;
        if (cursor.moveToFirst()) {
            sensor = mapper.cursorToUserSensor(cursor);
        }
        cursor.close();
        return sensor;
    }

    Sensor getAnyUserByOriginalId(int originalId) {
        if (originalId <= 0) return null;
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_sensors", null,
                "original_id = ?", new String[]{String.valueOf(originalId)},
                null, null, "is_deleted ASC, edited_at DESC, id DESC LIMIT 1");
        Sensor sensor = null;
        if (cursor.moveToFirst()) {
            sensor = mapper.cursorToUserSensor(cursor);
        }
        cursor.close();
        return sensor;
    }

    void copyToUser(int originalId) {
        Sensor existing = getUserByOriginalId(originalId);
        if (existing != null) return;

        Sensor ref = getById(originalId);
        if (ref == null) return;

        int newId = generateNegativeId();

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
        copy.setCreatedAt(RepositoryUtils.getCurrentDateTime());
        copy.setEditedAt(RepositoryUtils.getCurrentDateTime());

        insertUser(copy);
    }

    void markAsDeleted(int originalId) {
        if (originalId <= 0) {
            Log.e(TAG, "markAsDeleted: invalid originalId=" + originalId);
            return;
        }

        Sensor anyExisting = getAnyUserByOriginalId(originalId);
        if (anyExisting != null) {
            if (anyExisting.getIsDeleted() == 1) return;
            anyExisting.setIsDeleted(1);
            anyExisting.setEditedAt(RepositoryUtils.getCurrentDateTime());
            updateUser(anyExisting);
            return;
        }

        Sensor ref = getById(originalId);
        if (ref == null) {
            Log.e(TAG, "markAsDeleted: ref not found, originalId=" + originalId);
            return;
        }

        Sensor copy = new Sensor();
        copy.setId(generateNegativeId());
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
        copy.setCreatedAt(RepositoryUtils.getCurrentDateTime());
        copy.setEditedAt(RepositoryUtils.getCurrentDateTime());

        insertUser(copy);
    }

    List<Sensor> getAllWithUser() {
        List<Sensor> result = new ArrayList<>();
        for (Sensor s : getAllUser()) {
            if (s.getIsDeleted() != 1) result.add(s);
        }
        Set<Integer> overriddenIds = getOverriddenIds();
        for (Sensor ref : getAll()) {
            if (overriddenIds.contains(ref.getId())) continue;
            result.add(ref);
        }
        return result;
    }

    List<Sensor> searchWithUser(String query) {
        List<Sensor> result = new ArrayList<>();
        for (Sensor s : searchUser(query)) {
            if (s.getIsDeleted() != 1) result.add(s);
        }
        Set<Integer> overriddenIds = getOverriddenIds();
        for (Sensor ref : search(query)) {
            if (overriddenIds.contains(ref.getId())) continue;
            result.add(ref);
        }
        return result;
    }

    private Set<Integer> getOverriddenIds() {
        Set<Integer> overriddenIds = new HashSet<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT original_id FROM user_sensors WHERE original_id > 0",
                null);
        while (c.moveToNext()) {
            overriddenIds.add(c.getInt(0));
        }
        c.close();
        return overriddenIds;
    }

    private int generateNegativeId() {
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

    private List<Sensor> sortByRelevance(List<Sensor> sensors, String query) {
        String lowerQuery = query.toLowerCase();
        String kksQuery = RepositoryUtils.buildKksQuery(lowerQuery);

        Collections.sort(sensors, (a, b) -> {
            int scoreA = RepositoryUtils.getSensorRelevanceScore(a, lowerQuery, kksQuery);
            int scoreB = RepositoryUtils.getSensorRelevanceScore(b, lowerQuery, kksQuery);
            if (scoreA != scoreB) return Integer.compare(scoreA, scoreB);

            String smA = a.getStMarkir() != null ? a.getStMarkir() : "";
            String smB = b.getStMarkir() != null ? b.getStMarkir() : "";
            int cmp = RepositoryUtils.naturalCompare(smA, smB);
            if (cmp != 0) return cmp;

            String nameA = a.getName() != null ? a.getName() : "";
            String nameB = b.getName() != null ? b.getName() : "";
            return RepositoryUtils.naturalCompare(nameA, nameB);
        });
        return sensors;
    }
}