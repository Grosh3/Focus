package com.mikesuvade.focus.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mikesuvade.focus.data.database.DatabaseContract;
import com.mikesuvade.focus.data.database.DatabaseHelper;
import com.mikesuvade.focus.data.database.UserDatabaseHelper;
import com.mikesuvade.focus.domain.models.Setpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class SetpointRepository {

    private static final String TAG = "SetpointRepo";

    private final DatabaseHelper dbHelper;
    private final UserDatabaseHelper userDbHelper;
    private final CursorMapper mapper;

    SetpointRepository(DatabaseHelper dbHelper, UserDatabaseHelper userDbHelper, CursorMapper mapper) {
        this.dbHelper = dbHelper;
        this.userDbHelper = userDbHelper;
        this.mapper = mapper;
    }

    // ==================== СПРАВОЧНИК ====================

    List<Setpoint> getAll() {
        List<Setpoint> setpoints = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                null, null, null, null, null,
                DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + " ASC"
        );
        while (cursor.moveToNext()) {
            setpoints.add(mapper.cursorToSetpoint(cursor));
        }
        cursor.close();
        return setpoints;
    }

    Setpoint getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null
        );
        Setpoint setpoint = null;
        if (cursor.moveToFirst()) {
            setpoint = mapper.cursorToSetpoint(cursor);
        }
        cursor.close();
        return setpoint;
    }

    List<Setpoint> search(String query) {
        List<Setpoint> setpoints = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String trimmed = query.trim();

        if (trimmed.equalsIgnoreCase("#все")) {
            Cursor c = db.query(
                    DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                    null, null, null, null, null,
                    DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP + " ASC, "
                            + DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + " ASC");
            while (c.moveToNext()) setpoints.add(mapper.cursorToSetpoint(c));
            c.close();
            return setpoints;
        }

        if (trimmed.startsWith("#")) {
            String groupQuery = trimmed.toLowerCase();
            if (groupQuery.length() <= 1) return setpoints;
            Cursor c = db.query(
                    DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                    null,
                    "LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP + ") LIKE LOWER(?)",
                    new String[]{"%" + groupQuery + "%"},
                    null, null,
                    DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + " ASC");
            while (c.moveToNext()) setpoints.add(mapper.cursorToSetpoint(c));
            c.close();
            return setpoints;
        }

        String cleanQuery = query.replaceAll("[\\s\\-.]", "");
        boolean isShortQuery = cleanQuery.length() < 3;
        boolean isMarking = RepositoryUtils.isMarkingQuery(cleanQuery);

        String selection;
        List<String> argList = new ArrayList<>();

        if (isShortQuery) {
            selection = "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR "
                    + "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_NAME + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            argList.add("%" + cleanQuery + "%");
            argList.add("%" + cleanQuery + "%");
        } else if (isMarking) {
            selection = "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            argList.add("%" + cleanQuery + "%");
        } else {
            List<String> prefixes = RepositoryUtils.buildWordPrefixes(query);
            if (prefixes.isEmpty()) return setpoints;

            int n = prefixes.size();
            String posNameCond = "REPLACE(REPLACE(REPLACE(LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + "), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            String nameCond = RepositoryUtils.buildFieldCondition(DatabaseContract.SetpointScheduleEntry.COLUMN_NAME, n);
            String operationCond = RepositoryUtils.buildFieldCondition(DatabaseContract.SetpointScheduleEntry.COLUMN_OPERATION, n);
            String notesCond = RepositoryUtils.buildFieldCondition(DatabaseContract.SetpointScheduleEntry.COLUMN_NOTES, n);
            String locationCond = RepositoryUtils.buildFieldCondition(DatabaseContract.SetpointScheduleEntry.COLUMN_LOCATION, n);

            selection = "(" + posNameCond + " OR " + nameCond + " OR " + operationCond + " OR " + notesCond + " OR " + locationCond + ")";
            argList.add("%" + cleanQuery + "%");
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");
        }

        Cursor cursor = db.query(
                DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                null, selection, argList.toArray(new String[0]),
                null, null, null);

        while (cursor.moveToNext()) setpoints.add(mapper.cursorToSetpoint(cursor));
        cursor.close();

        return sortByRelevance(setpoints, query);
    }

    List<Setpoint> searchByGroup(String group) {
        List<Setpoint> setpoints = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String cleanGroup = group.replaceAll("\\s+", "");
        String selection = "REPLACE(LOWER(" + DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP + "), ' ', '') LIKE LOWER(?)";

        Cursor cursor = db.query(
                DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                null, selection, new String[]{"%" + cleanGroup + "%"},
                null, null,
                DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + " ASC");

        while (cursor.moveToNext()) setpoints.add(mapper.cursorToSetpoint(cursor));
        cursor.close();
        return setpoints;
    }

    long insert(Setpoint setpoint) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = mapper.setpointToContentValues(setpoint);
        return db.insert(DatabaseContract.SetpointScheduleEntry.TABLE_NAME, null, values);
    }

    int update(Setpoint setpoint) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = mapper.setpointToContentValues(setpoint);
        return db.update(
                DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                values,
                DatabaseContract.SetpointScheduleEntry._ID + " = ?",
                new String[]{String.valueOf(setpoint.getId())}
        );
    }

    int delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.SetpointScheduleEntry.TABLE_NAME,
                DatabaseContract.SetpointScheduleEntry._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // ==================== ПОЛЬЗОВАТЕЛЬСКАЯ ====================

    List<Setpoint> getAllUser() {
        List<Setpoint> setpoints = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_setpoints", null, null, null, null, null, "position_name ASC");
        while (cursor.moveToNext()) setpoints.add(mapper.cursorToUserSetpoint(cursor));
        cursor.close();
        return setpoints;
    }

    Setpoint getUserByOriginalId(int originalId) {
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_setpoints", null,
                "original_id = ?", new String[]{String.valueOf(originalId)},
                null, null, null);
        Setpoint setpoint = null;
        if (cursor.moveToFirst()) setpoint = mapper.cursorToUserSetpoint(cursor);
        cursor.close();
        return setpoint;
    }

    Setpoint getAnyUserByOriginalId(int originalId) {
        if (originalId <= 0) return null;
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_setpoints", null,
                "original_id = ?", new String[]{String.valueOf(originalId)},
                null, null, "is_deleted ASC, edited_at DESC, id DESC LIMIT 1");
        Setpoint setpoint = null;
        if (cursor.moveToFirst()) setpoint = mapper.cursorToUserSetpoint(cursor);
        cursor.close();
        return setpoint;
    }

    List<Setpoint> searchUser(String query) {
        List<Setpoint> setpoints = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();

        String trimmed = query.trim();

        if (trimmed.equalsIgnoreCase("#все")) {
            Cursor c = db.query("user_setpoints", null, null, null, null, null,
                    "equipment_group ASC, position_name ASC");
            while (c.moveToNext()) setpoints.add(mapper.cursorToUserSetpoint(c));
            c.close();
            return setpoints;
        }

        if (trimmed.startsWith("#")) {
            String groupQuery = trimmed.toLowerCase();
            if (groupQuery.length() <= 1) return setpoints;
            Cursor c = db.query("user_setpoints", null,
                    "LOWER(equipment_group) LIKE LOWER(?)",
                    new String[]{"%" + groupQuery + "%"},
                    null, null, "position_name ASC");
            while (c.moveToNext()) setpoints.add(mapper.cursorToUserSetpoint(c));
            c.close();
            return setpoints;
        }

        String cleanQuery = query.replaceAll("[\\s\\-.]", "");
        boolean isShortQuery = cleanQuery.length() < 3;
        boolean isMarking = RepositoryUtils.isMarkingQuery(cleanQuery);

        String selection;
        List<String> argList = new ArrayList<>();

        if (isShortQuery) {
            selection = "REPLACE(REPLACE(REPLACE(LOWER(position_name), ' ', ''), '-', ''), '.', '') LIKE LOWER(?) OR "
                    + "REPLACE(REPLACE(REPLACE(LOWER(name), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            argList.add("%" + cleanQuery + "%");
            argList.add("%" + cleanQuery + "%");
        } else if (isMarking) {
            selection = "REPLACE(REPLACE(REPLACE(LOWER(position_name), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            argList.add("%" + cleanQuery + "%");
        } else {
            List<String> prefixes = RepositoryUtils.buildWordPrefixes(query);
            if (prefixes.isEmpty()) return setpoints;

            int n = prefixes.size();
            String posNameCond = "REPLACE(REPLACE(REPLACE(LOWER(position_name), ' ', ''), '-', ''), '.', '') LIKE LOWER(?)";
            String nameCond = RepositoryUtils.buildFieldCondition("name", n);
            String operationCond = RepositoryUtils.buildFieldCondition("operation", n);
            String notesCond = RepositoryUtils.buildFieldCondition("notes", n);
            String locationCond = RepositoryUtils.buildFieldCondition("location", n);

            selection = "(" + posNameCond + " OR " + nameCond + " OR " + operationCond + " OR " + notesCond + " OR " + locationCond + ")";
            argList.add("%" + cleanQuery + "%");
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");
            for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");
        }

        Cursor cursor = db.query("user_setpoints", null, selection,
                argList.toArray(new String[0]), null, null, null);
        while (cursor.moveToNext()) setpoints.add(mapper.cursorToUserSetpoint(cursor));
        cursor.close();

        return sortByRelevance(setpoints, query);
    }

    List<Setpoint> searchUserByGroup(String group) {
        List<Setpoint> setpoints = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();

        String cleanGroup = group.replaceFirst("^#", "").trim();
        String pattern = "%" + cleanGroup + "%";

        Cursor cursor = db.query("user_setpoints", null,
                "LOWER(equipment_group) LIKE LOWER(?)",
                new String[]{pattern},
                null, null, "position_name ASC");

        while (cursor.moveToNext()) setpoints.add(mapper.cursorToUserSetpoint(cursor));
        cursor.close();
        return setpoints;
    }

    List<Setpoint> searchByGroupWithUser(String group) {
        List<Setpoint> result = new ArrayList<>();
        Set<Integer> seenIds = new HashSet<>();

        try {
            List<Setpoint> userResults = searchUserByGroup(group);
            for (Setpoint s : userResults) {
                if (s.getIsDeleted() != 1) {
                    result.add(s);
                    int key = s.getOriginalId() > 0 ? s.getOriginalId() : s.getId();
                    seenIds.add(key);
                }
            }

            List<Setpoint> refResults = searchByGroup(group);
            for (Setpoint s : refResults) {
                Setpoint deleted = getUserByOriginalId(s.getId());
                if (deleted != null && deleted.getIsDeleted() == 1) continue;
                if (!seenIds.contains(s.getId())) {
                    result.add(s);
                    seenIds.add(s.getId());
                }
            }
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error searchByGroupWithUser", e);
            return new ArrayList<>();
        }
    }

    long insertUser(Setpoint setpoint) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = mapper.userSetpointToContentValues(setpoint);
        return db.insert("user_setpoints", null, values);
    }

    int updateUser(Setpoint setpoint) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = mapper.userSetpointToContentValues(setpoint);
        return db.update("user_setpoints", values, "id = ?",
                new String[]{String.valueOf(setpoint.getId())});
    }

    int deleteUser(int id) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        return db.delete("user_setpoints", "id = ?", new String[]{String.valueOf(id)});
    }

    void copyToUser(int originalId) {
        Setpoint existing = getUserByOriginalId(originalId);
        if (existing != null) return;

        Setpoint ref = getById(originalId);
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
        copy.setEditedAt(RepositoryUtils.getCurrentDateTime());
        copy.setCreatedAt(RepositoryUtils.getCurrentDateTime());

        insertUser(copy);
    }

    void markAsDeleted(int originalId) {
        if (originalId <= 0) {
            Log.e(TAG, "markAsDeleted: invalid originalId=" + originalId);
            return;
        }

        Setpoint anyExisting = getAnyUserByOriginalId(originalId);
        if (anyExisting != null) {
            if (anyExisting.getIsDeleted() == 1) return;
            anyExisting.setIsDeleted(1);
            anyExisting.setEditedAt(RepositoryUtils.getCurrentDateTime());
            updateUser(anyExisting);
            return;
        }

        Setpoint ref = getById(originalId);
        if (ref == null) {
            Log.e(TAG, "markAsDeleted: ref not found, originalId=" + originalId);
            return;
        }

        Setpoint copy = new Setpoint();
        copy.setId(generateNegativeId());
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
        copy.setEditedAt(RepositoryUtils.getCurrentDateTime());
        copy.setCreatedAt(RepositoryUtils.getCurrentDateTime());

        insertUser(copy);
    }

    List<Setpoint> getAllWithUser() {
        List<Setpoint> result = new ArrayList<>();
        for (Setpoint sp : getAllUser()) {
            if (sp.getIsDeleted() != 1) result.add(sp);
        }
        Set<Integer> overriddenIds = getOverriddenIds();
        for (Setpoint ref : getAll()) {
            if (overriddenIds.contains(ref.getId())) continue;
            result.add(ref);
        }
        return result;
    }

    List<Setpoint> searchWithUser(String query) {
        List<Setpoint> result = new ArrayList<>();
        for (Setpoint sp : searchUser(query)) {
            if (sp.getIsDeleted() != 1) result.add(sp);
        }
        Set<Integer> overriddenIds = getOverriddenIds();
        for (Setpoint ref : search(query)) {
            if (overriddenIds.contains(ref.getId())) continue;
            result.add(ref);
        }
        return result;
    }

    private Set<Integer> getOverriddenIds() {
        Set<Integer> overriddenIds = new HashSet<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT original_id FROM user_setpoints WHERE original_id > 0",
                null);
        while (c.moveToNext()) overriddenIds.add(c.getInt(0));
        c.close();
        return overriddenIds;
    }

    private int generateNegativeId() {
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

    private List<Setpoint> sortByRelevance(List<Setpoint> setpoints, String query) {
        String lowerQuery = query.toLowerCase();

        Collections.sort(setpoints, (a, b) -> {
            int scoreA = RepositoryUtils.getSetpointRelevanceScore(a, lowerQuery);
            int scoreB = RepositoryUtils.getSetpointRelevanceScore(b, lowerQuery);
            if (scoreA != scoreB) return Integer.compare(scoreA, scoreB);

            String posA = a.getPositionName() != null ? a.getPositionName() : "";
            String posB = b.getPositionName() != null ? b.getPositionName() : "";
            int cmp = RepositoryUtils.naturalCompare(posA, posB);
            if (cmp != 0) return cmp;

            String nameA = a.getName() != null ? a.getName() : "";
            String nameB = b.getName() != null ? b.getName() : "";
            return RepositoryUtils.naturalCompare(nameA, nameB);
        });
        return setpoints;
    }
}