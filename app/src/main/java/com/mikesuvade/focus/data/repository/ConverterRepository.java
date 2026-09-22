package com.mikesuvade.focus.data.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mikesuvade.focus.data.database.DatabaseContract;
import com.mikesuvade.focus.data.database.DatabaseHelper;
import com.mikesuvade.focus.domain.models.ConverterPoint;

import java.util.ArrayList;
import java.util.List;

class ConverterRepository {

    private final DatabaseHelper dbHelper;

    ConverterRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    List<ConverterPoint> getPoints(String tableName) {
        List<ConverterPoint> points = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                tableName, null, null, null, null, null,
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

    double getTemperatureFromResistance(String tableName, double resistance) {
        List<ConverterPoint> points = getPoints(tableName);
        if (points.isEmpty()) return 0;

        ConverterPoint lower = null;
        ConverterPoint upper = null;

        for (ConverterPoint point : points) {
            if (point.getSignalValue() <= resistance) lower = point;
            if (point.getSignalValue() >= resistance && upper == null) upper = point;
        }

        if (lower == null && upper != null) return upper.getTemperature();
        if (lower != null && upper == null) return lower.getTemperature();
        if (lower == null && upper == null) return 0;
        if (lower.getSignalValue() == upper.getSignalValue()) return lower.getTemperature();

        double ratio = (resistance - lower.getSignalValue()) / (upper.getSignalValue() - lower.getSignalValue());
        return lower.getTemperature() + ratio * (upper.getTemperature() - lower.getTemperature());
    }

    double getSignalFromTemperature(String tableName, double temperature) {
        List<ConverterPoint> points = getPoints(tableName);
        if (points.isEmpty()) return 0;

        ConverterPoint lower = null;
        ConverterPoint upper = null;

        for (ConverterPoint point : points) {
            if (point.getTemperature() <= temperature) lower = point;
            if (point.getTemperature() >= temperature && upper == null) upper = point;
        }

        if (lower == null && upper != null) return upper.getSignalValue();
        if (lower != null && upper == null) return lower.getSignalValue();
        if (lower == null && upper == null) return 0;
        if (lower.getTemperature() == upper.getTemperature()) return lower.getSignalValue();

        double ratio = (temperature - lower.getTemperature()) / (upper.getTemperature() - lower.getTemperature());
        return lower.getSignalValue() + ratio * (upper.getSignalValue() - lower.getSignalValue());
    }

    List<String> getTableNames() {
        List<String> tables = new ArrayList<>();
        tables.add("gr21");
        tables.add("gr23");
        tables.add("ha");
        tables.add("hk");
        tables.add("tcp50p");
        tables.add("tsm50m");
        return tables;
    }
}