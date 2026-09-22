package com.mikesuvade.focus.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mikesuvade.focus.data.database.DatabaseContract;
import com.mikesuvade.focus.data.database.DatabaseHelper;
import com.mikesuvade.focus.data.database.UserDatabaseHelper;
import com.mikesuvade.focus.domain.models.GateValve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class GateValveRepository {

    private static final String TAG = "GateValveRepo";

    private final DatabaseHelper dbHelper;
    private final UserDatabaseHelper userDbHelper;
    private final CursorMapper mapper;

    GateValveRepository(DatabaseHelper dbHelper, UserDatabaseHelper userDbHelper, CursorMapper mapper) {
        this.dbHelper = dbHelper;
        this.userDbHelper = userDbHelper;
        this.mapper = mapper;
    }

    // ==================== СПРАВОЧНИК ====================

    List<GateValve> getAll() {
        List<GateValve> valves = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.GateValvesEntry.TABLE_NAME,
                null, null, null, null, null,
                DatabaseContract.GateValvesEntry.COLUMN_NAME + " ASC"
        );
        while (cursor.moveToNext()) {
            valves.add(mapper.cursorToGateValve(cursor));
        }
        cursor.close();
        return valves;
    }

    GateValve getById(int id) {
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
            valve = mapper.cursorToGateValve(cursor);
        }
        cursor.close();
        return valve;
    }

    List<GateValve> search(String query) {
        List<GateValve> valves = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String cleanQuery = query.replaceAll("[\\s\\-\\.\\u2013\\u2014]", "");
        boolean isShortQuery = cleanQuery.length() < 3;
        boolean isKksQuery = RepositoryUtils.containsLatin(cleanQuery);
        boolean hasDigit = cleanQuery.matches(".*[0-9].*");

        String isyClean = "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(isy), ' ', ''), '-', ''), '–', ''), '—', ''), '.', '')";
        String nameClean = "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(name), ' ', ''), '-', ''), '–', ''), '—', ''), '.', '')";
        String powerCabinetClean = "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(power_cabinet), ' ', ''), '-', ''), '–', ''), '—', ''), '.', '')";
        String kksClean = "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(kks), ' ', ''), '-', ''), '–', ''), '—', ''), '.', '')";

        if (isKksQuery) {
            String kksQuery = RepositoryUtils.buildKksQuery(cleanQuery);
            String selection = "(" + kksClean + " LIKE LOWER(?) OR " + kksClean + " LIKE LOWER(?))";
            Cursor cursor = db.query(
                    DatabaseContract.GateValvesEntry.TABLE_NAME, null, selection,
                    new String[]{"%" + cleanQuery + "%", "%" + kksQuery + "%"},
                    null, null, null);
            while (cursor.moveToNext()) valves.add(mapper.cursorToGateValve(cursor));
            cursor.close();
            return sortByRelevance(valves, query);
        }

        if (isShortQuery) {
            String selection = "(" + isyClean + " LIKE LOWER(?)"
                    + " OR " + nameClean + " LIKE LOWER(?)"
                    + " OR " + powerCabinetClean + " LIKE LOWER(?))";
            Cursor cursor = db.query(
                    DatabaseContract.GateValvesEntry.TABLE_NAME, null, selection,
                    new String[]{cleanQuery + "%", cleanQuery + "%", cleanQuery + "%"},
                    null, null, null);
            while (cursor.moveToNext()) valves.add(mapper.cursorToGateValve(cursor));
            cursor.close();
            return sortByRelevance(valves, query);
        }

        if (cleanQuery.length() <= 6) {
            if (hasDigit) {
                String selection = "(" + isyClean + " LIKE LOWER(?)"
                        + " OR " + nameClean + " LIKE LOWER(?)"
                        + " OR " + powerCabinetClean + " LIKE LOWER(?))";
                Cursor cursor = db.query(
                        DatabaseContract.GateValvesEntry.TABLE_NAME, null, selection,
                        new String[]{cleanQuery + "%", cleanQuery + "%", cleanQuery + "%"},
                        null, null, null);
                while (cursor.moveToNext()) valves.add(mapper.cursorToGateValve(cursor));
                cursor.close();

                if (!valves.isEmpty()) return sortByRelevance(valves, query);

            } else {
                String exactSelection = "(" + isyClean + " = LOWER(?)"
                        + " OR " + nameClean + " = LOWER(?)"
                        + " OR " + powerCabinetClean + " = LOWER(?))";
                Cursor cursor = db.query(
                        DatabaseContract.GateValvesEntry.TABLE_NAME, null, exactSelection,
                        new String[]{cleanQuery, cleanQuery, cleanQuery},
                        null, null, null);
                while (cursor.moveToNext()) valves.add(mapper.cursorToGateValve(cursor));
                cursor.close();

                if (!valves.isEmpty()) return sortByRelevance(valves, query);

                String prefixSelection = "(" + isyClean + " LIKE LOWER(?)"
                        + " OR " + nameClean + " LIKE LOWER(?)"
                        + " OR " + powerCabinetClean + " LIKE LOWER(?))";
                cursor = db.query(
                        DatabaseContract.GateValvesEntry.TABLE_NAME, null, prefixSelection,
                        new String[]{cleanQuery + "%", cleanQuery + "%", cleanQuery + "%"},
                        null, null, null);
                while (cursor.moveToNext()) valves.add(mapper.cursorToGateValve(cursor));
                cursor.close();

                if (!valves.isEmpty()) return sortByRelevance(valves, query);
            }
        }

        List<String> prefixes = RepositoryUtils.buildWordPrefixes(query);

        if (prefixes.isEmpty()) {
            String selection = "LOWER(" + DatabaseContract.GateValvesEntry.COLUMN_FULL_NAME + ") LIKE LOWER(?)";
            Cursor cursor = db.query(
                    DatabaseContract.GateValvesEntry.TABLE_NAME, null, selection,
                    new String[]{"%" + cleanQuery + "%"},
                    null, null, null);
            while (cursor.moveToNext()) valves.add(mapper.cursorToGateValve(cursor));
            cursor.close();
            return sortByRelevance(valves, query);
        }

        int n = prefixes.size();
        String fullNameCond = RepositoryUtils.buildFieldCondition(DatabaseContract.GateValvesEntry.COLUMN_FULL_NAME, n);

        List<String> argList = new ArrayList<>();
        for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");

        Cursor cursor = db.query(
                DatabaseContract.GateValvesEntry.TABLE_NAME, null,
                fullNameCond,
                argList.toArray(new String[0]),
                null, null, null);
        while (cursor.moveToNext()) valves.add(mapper.cursorToGateValve(cursor));
        cursor.close();

        return sortByRelevance(valves, query);
    }

    long insert(GateValve valve) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = mapper.gateValveToContentValues(valve);
        return db.insert(DatabaseContract.GateValvesEntry.TABLE_NAME, null, values);
    }

    int update(GateValve valve) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = mapper.gateValveToContentValues(valve);
        return db.update(
                DatabaseContract.GateValvesEntry.TABLE_NAME,
                values,
                DatabaseContract.GateValvesEntry._ID + " = ?",
                new String[]{String.valueOf(valve.getId())}
        );
    }

    int delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DatabaseContract.GateValvesEntry.TABLE_NAME,
                DatabaseContract.GateValvesEntry._ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // ==================== ПОЛЬЗОВАТЕЛЬСКАЯ ====================

    List<GateValve> getAllUser() {
        List<GateValve> valves = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_gate_valves", null, null, null, null, null, "name ASC");
        while (cursor.moveToNext()) {
            valves.add(mapper.cursorToUserGateValve(cursor));
        }
        cursor.close();
        return valves;
    }

    GateValve getUserByOriginalId(int originalId) {
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_gate_valves", null,
                "original_id = ?", new String[]{String.valueOf(originalId)},
                null, null, null);
        GateValve valve = null;
        if (cursor.moveToFirst()) {
            valve = mapper.cursorToUserGateValve(cursor);
        }
        cursor.close();
        return valve;
    }

    GateValve getUserById(int id) {
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_gate_valves", null,
                "id = ?", new String[]{String.valueOf(id)},
                null, null, null);
        GateValve valve = null;
        if (cursor.moveToFirst()) {
            valve = mapper.cursorToUserGateValve(cursor);
        }
        cursor.close();
        return valve;
    }

    GateValve getAnyUserByOriginalId(int originalId) {
        if (originalId <= 0) return null;
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_gate_valves", null,
                "original_id = ?", new String[]{String.valueOf(originalId)},
                null, null,
                "is_deleted ASC, edited_at DESC, id DESC LIMIT 1");
        GateValve valve = null;
        if (cursor.moveToFirst()) {
            valve = mapper.cursorToUserGateValve(cursor);
        }
        cursor.close();
        return valve;
    }

    List<GateValve> searchUser(String query) {
        List<GateValve> valves = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();

        String cleanQuery = query.replaceAll("[\\s\\-\\.\\u2013\\u2014]", "");
        boolean isShortQuery = cleanQuery.length() < 3;
        boolean isKksQuery = RepositoryUtils.containsLatin(cleanQuery);
        boolean hasDigit = cleanQuery.matches(".*[0-9].*");

        String isyClean = "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(isy), ' ', ''), '-', ''), '–', ''), '—', ''), '.', '')";
        String nameClean = "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(name), ' ', ''), '-', ''), '–', ''), '—', ''), '.', '')";
        String powerCabinetClean = "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(power_cabinet), ' ', ''), '-', ''), '–', ''), '—', ''), '.', '')";
        String kksClean = "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(kks), ' ', ''), '-', ''), '–', ''), '—', ''), '.', '')";

        if (isKksQuery) {
            String kksQuery = RepositoryUtils.buildKksQuery(cleanQuery);
            String selection = "(" + kksClean + " LIKE LOWER(?) OR " + kksClean + " LIKE LOWER(?))";
            Cursor cursor = db.query("user_gate_valves", null, selection,
                    new String[]{"%" + cleanQuery + "%", "%" + kksQuery + "%"},
                    null, null, null);
            while (cursor.moveToNext()) valves.add(mapper.cursorToUserGateValve(cursor));
            cursor.close();
            return sortByRelevance(valves, query);
        }

        if (isShortQuery) {
            String selection = "(" + isyClean + " LIKE LOWER(?)"
                    + " OR " + nameClean + " LIKE LOWER(?)"
                    + " OR " + powerCabinetClean + " LIKE LOWER(?))";
            Cursor cursor = db.query("user_gate_valves", null, selection,
                    new String[]{cleanQuery + "%", cleanQuery + "%", cleanQuery + "%"},
                    null, null, null);
            while (cursor.moveToNext()) valves.add(mapper.cursorToUserGateValve(cursor));
            cursor.close();
            return sortByRelevance(valves, query);
        }

        if (cleanQuery.length() <= 6) {
            if (hasDigit) {
                String selection = "(" + isyClean + " LIKE LOWER(?)"
                        + " OR " + nameClean + " LIKE LOWER(?)"
                        + " OR " + powerCabinetClean + " LIKE LOWER(?))";
                Cursor cursor = db.query("user_gate_valves", null, selection,
                        new String[]{cleanQuery + "%", cleanQuery + "%", cleanQuery + "%"},
                        null, null, null);
                while (cursor.moveToNext()) valves.add(mapper.cursorToUserGateValve(cursor));
                cursor.close();
                if (!valves.isEmpty()) return sortByRelevance(valves, query);
            } else {
                String exactSelection = "(" + isyClean + " = LOWER(?)"
                        + " OR " + nameClean + " = LOWER(?)"
                        + " OR " + powerCabinetClean + " = LOWER(?))";
                Cursor cursor = db.query("user_gate_valves", null, exactSelection,
                        new String[]{cleanQuery, cleanQuery, cleanQuery},
                        null, null, null);
                while (cursor.moveToNext()) valves.add(mapper.cursorToUserGateValve(cursor));
                cursor.close();
                if (!valves.isEmpty()) return sortByRelevance(valves, query);

                String prefixSelection = "(" + isyClean + " LIKE LOWER(?)"
                        + " OR " + nameClean + " LIKE LOWER(?)"
                        + " OR " + powerCabinetClean + " LIKE LOWER(?))";
                cursor = db.query("user_gate_valves", null, prefixSelection,
                        new String[]{cleanQuery + "%", cleanQuery + "%", cleanQuery + "%"},
                        null, null, null);
                while (cursor.moveToNext()) valves.add(mapper.cursorToUserGateValve(cursor));
                cursor.close();
                if (!valves.isEmpty()) return sortByRelevance(valves, query);
            }
        }

        List<String> prefixes = RepositoryUtils.buildWordPrefixes(query);
        if (prefixes.isEmpty()) {
            String selection = "LOWER(full_name) LIKE LOWER(?)";
            Cursor cursor = db.query("user_gate_valves", null, selection,
                    new String[]{"%" + cleanQuery + "%"}, null, null, null);
            while (cursor.moveToNext()) valves.add(mapper.cursorToUserGateValve(cursor));
            cursor.close();
            return sortByRelevance(valves, query);
        }

        int n = prefixes.size();
        String fullNameCond = RepositoryUtils.buildFieldCondition("full_name", n);
        List<String> argList = new ArrayList<>();
        for (int i = 0; i < n; i++) argList.add("%" + prefixes.get(i) + "%");

        Cursor cursor = db.query("user_gate_valves", null,
                fullNameCond, argList.toArray(new String[0]), null, null, null);
        while (cursor.moveToNext()) valves.add(mapper.cursorToUserGateValve(cursor));
        cursor.close();
        return sortByRelevance(valves, query);
    }

    long insertUser(GateValve valve) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = mapper.userGateValveToContentValues(valve);
        return db.insert("user_gate_valves", null, values);
    }

    int updateUser(GateValve valve) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = mapper.userGateValveToContentValues(valve);
        return db.update("user_gate_valves", values, "id = ?",
                new String[]{String.valueOf(valve.getId())});
    }

    int deleteUser(int id) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        return db.delete("user_gate_valves", "id = ?", new String[]{String.valueOf(id)});
    }

    void copyToUser(int originalId) {
        GateValve existing = getUserByOriginalId(originalId);
        if (existing != null) return;

        GateValve ref = getById(originalId);
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
        copy.setEditedAtValve(RepositoryUtils.getCurrentDateTime());
        copy.setCreatedAt(RepositoryUtils.getCurrentDateTime());

        insertUser(copy);
    }

    void markAsDeleted(int originalId) {
        if (originalId <= 0) {
            Log.e(TAG, "markAsDeleted: invalid originalId=" + originalId);
            return;
        }

        GateValve anyExisting = getAnyUserByOriginalId(originalId);
        if (anyExisting != null) {
            if (anyExisting.getIsDeleted() == 1) return;
            anyExisting.setIsDeleted(1);
            anyExisting.setEditedAtValve(RepositoryUtils.getCurrentDateTime());
            updateUser(anyExisting);
            return;
        }

        GateValve ref = getById(originalId);
        if (ref == null) {
            Log.e(TAG, "markAsDeleted: ref not found, originalId=" + originalId);
            return;
        }

        GateValve copy = new GateValve();
        copy.setId(generateNegativeId());
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
        copy.setEditedAtValve(RepositoryUtils.getCurrentDateTime());
        copy.setCreatedAt(RepositoryUtils.getCurrentDateTime());

        insertUser(copy);
    }

    List<GateValve> getAllWithUser() {
        List<GateValve> result = new ArrayList<>();

        for (GateValve v : getAllUser()) {
            if (v.getIsDeleted() != 1) result.add(v);
        }

        Set<Integer> overriddenIds = getOverriddenIds();
        for (GateValve ref : getAll()) {
            if (overriddenIds.contains(ref.getId())) continue;
            result.add(ref);
        }
        return result;
    }

    List<GateValve> searchWithUser(String query) {
        List<GateValve> result = new ArrayList<>();

        for (GateValve v : searchUser(query)) {
            if (v.getIsDeleted() != 1) result.add(v);
        }

        Set<Integer> overriddenIds = getOverriddenIds();
        for (GateValve ref : search(query)) {
            if (overriddenIds.contains(ref.getId())) continue;
            result.add(ref);
        }
        return result;
    }

    private Set<Integer> getOverriddenIds() {
        Set<Integer> overriddenIds = new HashSet<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT original_id FROM user_gate_valves WHERE original_id > 0",
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
        Cursor c = db.rawQuery("SELECT MIN(id) FROM user_gate_valves", null);
        if (c.moveToFirst() && !c.isNull(0)) {
            minId = c.getInt(0);
        }
        c.close();
        int newId = minId - 1;
        if (newId == 0) newId = -1;
        return newId;
    }

    private List<GateValve> sortByRelevance(List<GateValve> valves, String query) {
        String cleanQuery = query.replaceAll("[\\s\\-\\.\\u2013\\u2014]", "").toLowerCase();

        Collections.sort(valves, (a, b) -> {
            int scoreA = RepositoryUtils.getGateValveRelevanceScore(a, cleanQuery);
            int scoreB = RepositoryUtils.getGateValveRelevanceScore(b, cleanQuery);
            if (scoreA != scoreB) return Integer.compare(scoreA, scoreB);

            String isyA = a.getIsy() != null ? a.getIsy() : "";
            String isyB = b.getIsy() != null ? b.getIsy() : "";
            int cmp = RepositoryUtils.naturalCompare(isyA, isyB);
            if (cmp != 0) return cmp;

            String nameA = a.getName() != null ? a.getName() : "";
            String nameB = b.getName() != null ? b.getName() : "";
            return RepositoryUtils.naturalCompare(nameA, nameB);
        });
        return valves;
    }
}