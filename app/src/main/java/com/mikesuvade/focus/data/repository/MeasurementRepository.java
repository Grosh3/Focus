package com.mikesuvade.focus.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mikesuvade.focus.data.database.UserDatabaseHelper;
import com.mikesuvade.focus.domain.models.Measurement;

import java.util.ArrayList;
import java.util.List;

class MeasurementRepository {

    private final UserDatabaseHelper userDbHelper;
    private final CursorMapper mapper;

    MeasurementRepository(UserDatabaseHelper userDbHelper, CursorMapper mapper) {
        this.userDbHelper = userDbHelper;
        this.mapper = mapper;
    }

    long insert(Measurement measurement) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = mapper.userMeasurementToContentValues(measurement);
        return db.insert("user_measurements", null, values);
    }

    List<Measurement> getAll() {
        List<Measurement> measurements = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_measurements", null, null, null, null, null,
                "created_at DESC, id DESC");
        while (cursor.moveToNext()) measurements.add(mapper.cursorToUserMeasurement(cursor));
        cursor.close();
        return measurements;
    }

    List<Measurement> getByDate(String date) {
        List<Measurement> measurements = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_measurements", null,
                "measurement_date = ?", new String[]{date}, null, null, "created_at DESC");
        while (cursor.moveToNext()) measurements.add(mapper.cursorToUserMeasurement(cursor));
        cursor.close();
        return measurements;
    }

    int updateDescription(int id, String description) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("description", description);
        return db.update("user_measurements", values, "id = ?",
                new String[]{String.valueOf(id)});
    }

    int delete(int id) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        return db.delete("user_measurements", "id = ?", new String[]{String.valueOf(id)});
    }
}