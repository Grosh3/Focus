package com.mikesuvade.focus.data.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "focus_data.db";
    private static final int DATABASE_VERSION = 1;
    private static final String ASSETS_PATH = DATABASE_NAME;

    private static DatabaseHelper instance;
    private final Context context;

    // ==========================================
    // ОЖИДАЕМАЯ СХЕМА (только справочные таблицы!)
    // ==========================================

    private static final Set<String> EXPECTED_GATE_VALVES_COLUMNS = new HashSet<>(Arrays.asList(
            "_id", "name_eng", "kks", "name", "isy", "power_cabinet",
            "full_name_of_the_position", "on_place", "ap_50", "mark",
            "cda_cabinet", "cda_cabinet_position", "slot",
            "name_space_view_open", "description_blocking_open",
            "namespace_view_close", "description_blocking_close",
            "namespace_view_perifer", "description_blocking_perifer",
            "location_description", "is_edited", "edited_at"
    ));

    private static final Set<String> EXPECTED_SENSORS_COLUMNS = new HashSet<>(Arrays.asList(
            "id", "kks", "st_marking", "full_name", "name", "media", "units",
            "nominal", "vol_min", "vol_max", "speed", "fault_param",
            "instead_f", "filter_value", "sensor_model", "sensor_mod",
            "additional_info", "min", "max", "unit_measure",
            "installation_location", "cva", "damping_time"
    ));

    private static final Set<String> EXPECTED_SETPOINTS_COLUMNS = new HashSet<>(Arrays.asList(
            "id", "name", "position_name", "location", "setpoint_value",
            "delay_time", "operation", "notes", "equipment_group"
    ));

    // ==========================================
    // SINGLETON
    // ==========================================

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
            instance.ensureDatabaseReady();
        }
        return instance;
    }

    // ==========================================
    // LIFECYCLE
    // ==========================================

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Ничего не делаем: БД копируется из assets в ensureDatabaseReady().

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // НЕ пересоздаём БД здесь. Всё делает ensureDatabaseReady() заранее,
        // до того как SQLiteOpenHelper вообще откроет файл.

    }

    public boolean isDatabaseReady() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        return dbFile.exists() && dbFile.length() > 0;
    }

    // ==========================================
    // ГОТОВНОСТЬ БД
    // ==========================================

    public void ensureDatabaseReady() {


        // 1. Файла нет — копируем.
        if (!isDatabaseReady()) {

            replaceDatabaseFromAssets("initial copy");

            return;
        }

        // 2. Файл есть — проверяем схему.
        if (!isSchemaUpToDate()) {

            replaceDatabaseFromAssets("schema update");

        }


    }

    /**
     * Удаляет текущий файл и копирует свежий из assets.
     * НЕ трогает SQLiteOpenHelper-соединение — вызывается до его первого открытия.
     */
    private void replaceDatabaseFromAssets(String reason) {
        try {
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            if (dbFile.exists()) {
                dbFile.delete();
            }

            // Удаляем shm/wal, если остались от предыдущей сессии
            File shm = new File(dbFile.getAbsolutePath() + "-shm");
            File wal = new File(dbFile.getAbsolutePath() + "-wal");
            if (shm.exists()) shm.delete();
            if (wal.exists()) wal.delete();

            copyDatabaseFromAssets();

            if (!isDatabaseReady()) {
                throw new IOException("Copied DB file is empty or missing");
            }

        } catch (IOException e) {
            Log.e(TAG, "❌ Failed to replace database (" + reason + ")", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    // ==========================================
    // ВАЛИДАЦИЯ СХЕМЫ
    // ==========================================

    private boolean isSchemaUpToDate() {
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(
                    context.getDatabasePath(DATABASE_NAME).getAbsolutePath(),
                    null,
                    SQLiteDatabase.OPEN_READONLY
            );

            boolean gateValvesOk = hasAllColumns(db, "gate_valves", EXPECTED_GATE_VALVES_COLUMNS);
            boolean sensorsOk = hasAllColumns(db, "sensor_schedule", EXPECTED_SENSORS_COLUMNS);
            boolean setpointsOk = hasAllColumns(db, "setpoint_schedule", EXPECTED_SETPOINTS_COLUMNS);

            return gateValvesOk && sensorsOk && setpointsOk;
        } catch (Exception e) {
            Log.e(TAG, "isSchemaUpToDate: failed to check schema", e);
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    private boolean hasAllColumns(SQLiteDatabase db, String tableName, Set<String> expected) {
        Set<String> actual = new HashSet<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            while (cursor.moveToNext()) {
                actual.add(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            }
        } catch (Exception e) {
            Log.e(TAG, "hasAllColumns: failed to read " + tableName, e);
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }

        if (actual.isEmpty()) {

            return false;
        }

        for (String col : expected) {
            if (!actual.contains(col)) {

                return false;
            }
        }
        return true;
    }

    // ==========================================
    // КОПИРОВАНИЕ
    // ==========================================

    private void copyDatabaseFromAssets() throws IOException {
        InputStream inputStream = context.getAssets().open(ASSETS_PATH);
        File outFile = context.getDatabasePath(DATABASE_NAME);
        File parent = outFile.getParentFile();
        if (parent != null) parent.mkdirs();

        OutputStream outputStream = new FileOutputStream(outFile);
        byte[] buffer = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }
}