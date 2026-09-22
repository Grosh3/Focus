package com.mikesuvade.focus.data.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mikesuvade.focus.data.database.DatabaseContract;
import com.mikesuvade.focus.data.database.DatabaseHelper;
import com.mikesuvade.focus.data.database.UserDatabaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class GroupRepository {

    private final DatabaseHelper dbHelper;
    private final UserDatabaseHelper userDbHelper;

    GroupRepository(DatabaseHelper dbHelper, UserDatabaseHelper userDbHelper) {
        this.dbHelper = dbHelper;
        this.userDbHelper = userDbHelper;
    }

    List<String> getAll() {
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
            if (group != null && !group.isEmpty()) groups.add(group);
        }
        cursor.close();
        return groups;
    }

    List<String> getAllWithUser() {
        Set<String> groupSet = new HashSet<>();

        for (String g : getAll()) {
            if (g != null && !g.isEmpty()) groupSet.add(g.toUpperCase());
        }

        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_setpoints",
                new String[]{"equipment_group"},
                "equipment_group IS NOT NULL AND equipment_group != '' AND is_deleted != 1",
                null, null, null, "equipment_group ASC");

        while (cursor.moveToNext()) {
            String group = cursor.getString(0);
            if (group != null && !group.isEmpty()) groupSet.add(group.toUpperCase());
        }
        cursor.close();

        List<String> result = new ArrayList<>(groupSet);
        Collections.sort(result);
        return result;
    }
}