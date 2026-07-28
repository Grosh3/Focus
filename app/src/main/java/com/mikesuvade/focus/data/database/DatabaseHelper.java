package com.mikesuvade.focus.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "focus_data.db";
    private static final int DATABASE_VERSION = 1;
    private static final String ASSETS_PATH = "databases/" + DATABASE_NAME;

    private static DatabaseHelper instance;
    private final Context context;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
        // Включаем WAL для многопоточности - через PRAGMA
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Включаем WAL при открытии
        db.execSQL("PRAGMA journal_mode=WAL;");
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: Creating database from scratch");
        createTables(db);
        createIndexes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: Upgrading database from " + oldVersion + " to " + newVersion);
        dropTables(db);
        onCreate(db);
    }

    private void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.GateValvesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.GateValvesUserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.SensorScheduleEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.SensorScheduleUserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.SetpointScheduleEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.SetpointScheduleUserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Gr21Entry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Gr23Entry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.HaEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.HkEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Tcp50pEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Tsm50mEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.ValveWorkSessionsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.ValveItemsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.MeasurementsEntry.TABLE_NAME);
    }

    public boolean isDatabaseReady() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        return dbFile.exists() && dbFile.length() > 0;
    }

    public void ensureDatabaseReady() {
        if (isDatabaseReady()) {
            Log.d(TAG, "Database already exists");
            return;
        }

        Log.d(TAG, "Copying database from assets...");
        try {
            copyDatabaseFromAssets();
            Log.d(TAG, "Database copied successfully");
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy database", e);
            createEmptyDatabase();
        }
    }

    private void copyDatabaseFromAssets() throws IOException {
        InputStream inputStream = context.getAssets().open(ASSETS_PATH);
        String outFileName = context.getDatabasePath(DATABASE_NAME).getPath();
        OutputStream outputStream = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    private void createEmptyDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        createTables(db);
        createIndexes(db);
        db.close();
    }

    // ==================== CREATE TABLES ====================

    private void createTables(SQLiteDatabase db) {
        // gate_valves
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContract.GateValvesEntry.TABLE_NAME + " (" +
                DatabaseContract.GateValvesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.GateValvesEntry.COLUMN_NAME_ENG + " TEXT, " +
                DatabaseContract.GateValvesEntry.COLUMN_KKS + " TEXT, " +
                DatabaseContract.GateValvesEntry.COLUMN_NAME + " TEXT, " +
                DatabaseContract.GateValvesEntry.COLUMN_ISY + " TEXT, " +
                DatabaseContract.GateValvesEntry.COLUMN_POWER_CABINET + " TEXT, " +
                DatabaseContract.GateValvesEntry.COLUMN_FULL_NAME + " TEXT, " +
                DatabaseContract.GateValvesEntry.COLUMN_ON_PLACE + " TEXT, " +
                DatabaseContract.GateValvesEntry.COLUMN_AP_50 + " TEXT, " +
                DatabaseContract.GateValvesEntry.COLUMN_MARK + " TEXT, " +
                DatabaseContract.GateValvesEntry.COLUMN_CDA_CABINET + " TEXT, " +
                DatabaseContract.GateValvesEntry.COLUMN_CDA_CABINET_POSITION + " TEXT, " +
                DatabaseContract.GateValvesEntry.COLUMN_SLOT + " TEXT, " +
                DatabaseContract.GateValvesEntry.COLUMN_NAME_SPACE_VIEW_OPEN + " BLOB, " +
                DatabaseContract.GateValvesEntry.COLUMN_DESCRIPTION_BLOCKING_OPEN + " TEXT, " +
                DatabaseContract.GateValvesEntry.COLUMN_NAMESPACE_VIEW_CLOSE + " BLOB, " +
                DatabaseContract.GateValvesEntry.COLUMN_DESCRIPTION_BLOCKING_CLOSE + " TEXT, " +
                DatabaseContract.GateValvesEntry.COLUMN_NAMESPACE_VIEW_PERIFER + " BLOB, " +
                DatabaseContract.GateValvesEntry.COLUMN_DESCRIPTION_BLOCKING_PERIFER + " TEXT" +
                ")");

        // gate_valves_user
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContract.GateValvesUserEntry.TABLE_NAME + " (" +
                DatabaseContract.GateValvesUserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_NAME_ENG + " TEXT, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_KKS + " TEXT, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_NAME + " TEXT, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_ISY + " TEXT, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_POWER_CABINET + " TEXT, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_FULL_NAME + " TEXT, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_ON_PLACE + " TEXT, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_AP_50 + " TEXT, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_MARK + " TEXT, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_CDA_CABINET + " TEXT, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_CDA_CABINET_POSITION + " TEXT, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_SLOT + " TEXT, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_NAME_SPACE_VIEW_OPEN + " BLOB, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_DESCRIPTION_BLOCKING_OPEN + " TEXT, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_NAMESPACE_VIEW_CLOSE + " BLOB, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_DESCRIPTION_BLOCKING_CLOSE + " TEXT, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_NAMESPACE_VIEW_PERIFER + " BLOB, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_DESCRIPTION_BLOCKING_PERIFER + " TEXT, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_ORIGINAL_ID + " INTEGER, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_EDITED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                DatabaseContract.GateValvesUserEntry.COLUMN_IS_CUSTOM + " INTEGER DEFAULT 1" +
                ")");

        // sensor_schedule
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContract.SensorScheduleEntry.TABLE_NAME + " (" +
                DatabaseContract.SensorScheduleEntry.COLUMN_KEYNUM + " INTEGER, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_FA + " INTEGER, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_KKS + " TEXT PRIMARY KEY, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR + " TEXT, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_FULL_NAME + " TEXT, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_NAME + " TEXT, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_MEDIA + " TEXT, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_UNITS + " TEXT, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_NOMINAL + " REAL, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_VOL_MIN + " REAL, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_VOL_MAX + " REAL, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_SPEED + " TEXT, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_FAULT_PAR + " TEXT, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_INSTEAD_F + " TEXT, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_FILTER + " TEXT, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_MODEL_SENSOR + " TEXT, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_MOD_SENSOR + " TEXT, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_ADDITIONAL_INFO + " TEXT, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_MIN + " REAL, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_MAX + " REAL, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_MEASURE_UNIT + " TEXT, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_LOCATION + " TEXT, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_CVA + " TEXT, " +
                DatabaseContract.SensorScheduleEntry.COLUMN_DAMPING_TIME + " TEXT" +
                ")");

        // sensor_schedule_user
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContract.SensorScheduleUserEntry.TABLE_NAME + " (" +
                DatabaseContract.SensorScheduleUserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_ORIGINAL_KKS + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_KEYNUM + " INTEGER, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_FA + " INTEGER, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_ST_MARKIR + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_FULL_NAME + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_NAME + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_MEDIA + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_UNITS + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_NOMINAL + " REAL, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_VOL_MIN + " REAL, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_VOL_MAX + " REAL, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_SPEED + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_FAULT_PAR + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_INSTEAD_F + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_FILTER + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_MODEL_SENSOR + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_MOD_SENSOR + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_ADDITIONAL_INFO + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_MIN_VAL + " REAL, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_MAX_VAL + " REAL, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_MEASURE_UNIT + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_LOCATION + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_CVA + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_DAMPING_TIME + " TEXT, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_EDITED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                DatabaseContract.SensorScheduleUserEntry.COLUMN_IS_CUSTOM + " INTEGER DEFAULT 1" +
                ")");

        // setpoint_schedule
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContract.SetpointScheduleEntry.TABLE_NAME + " (" +
                DatabaseContract.SetpointScheduleEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.SetpointScheduleEntry.COLUMN_NAME + " TEXT, " +
                DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + " TEXT, " +
                DatabaseContract.SetpointScheduleEntry.COLUMN_LOCATION + " TEXT, " +
                DatabaseContract.SetpointScheduleEntry.COLUMN_SETPOINT_VALUE + " TEXT, " +
                DatabaseContract.SetpointScheduleEntry.COLUMN_DELAY_TIME + " TEXT, " +
                DatabaseContract.SetpointScheduleEntry.COLUMN_OPERATION + " TEXT, " +
                DatabaseContract.SetpointScheduleEntry.COLUMN_NOTES + " TEXT, " +
                DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP + " TEXT" +
                ")");

        // setpoint_schedule_user
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContract.SetpointScheduleUserEntry.TABLE_NAME + " (" +
                DatabaseContract.SetpointScheduleUserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.SetpointScheduleUserEntry.COLUMN_ORIGINAL_ID + " INTEGER, " +
                DatabaseContract.SetpointScheduleUserEntry.COLUMN_NAME + " TEXT, " +
                DatabaseContract.SetpointScheduleUserEntry.COLUMN_POSITION_NAME + " TEXT, " +
                DatabaseContract.SetpointScheduleUserEntry.COLUMN_LOCATION + " TEXT, " +
                DatabaseContract.SetpointScheduleUserEntry.COLUMN_SETPOINT_VALUE + " TEXT, " +
                DatabaseContract.SetpointScheduleUserEntry.COLUMN_DELAY_TIME + " TEXT, " +
                DatabaseContract.SetpointScheduleUserEntry.COLUMN_OPERATION + " TEXT, " +
                DatabaseContract.SetpointScheduleUserEntry.COLUMN_NOTES + " TEXT, " +
                DatabaseContract.SetpointScheduleUserEntry.COLUMN_EQUIPMENT_GROUP + " TEXT, " +
                DatabaseContract.SetpointScheduleUserEntry.COLUMN_EDITED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                DatabaseContract.SetpointScheduleUserEntry.COLUMN_IS_CUSTOM + " INTEGER DEFAULT 1" +
                ")");

        // gr21
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContract.Gr21Entry.TABLE_NAME + " (" +
                DatabaseContract.Gr21Entry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.Gr21Entry.COLUMN_SIGNAL_VALUE + " REAL NOT NULL, " +
                DatabaseContract.Gr21Entry.COLUMN_TEMPERATURE + " REAL NOT NULL" +
                ")");

        // gr23
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContract.Gr23Entry.TABLE_NAME + " (" +
                DatabaseContract.Gr23Entry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.Gr23Entry.COLUMN_SIGNAL_VALUE + " REAL NOT NULL, " +
                DatabaseContract.Gr23Entry.COLUMN_TEMPERATURE + " REAL NOT NULL" +
                ")");

        // ha
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContract.HaEntry.TABLE_NAME + " (" +
                DatabaseContract.HaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.HaEntry.COLUMN_SIGNAL_VALUE + " REAL NOT NULL, " +
                DatabaseContract.HaEntry.COLUMN_TEMPERATURE + " REAL NOT NULL" +
                ")");

        // hk
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContract.HkEntry.TABLE_NAME + " (" +
                DatabaseContract.HkEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.HkEntry.COLUMN_SIGNAL_VALUE + " REAL NOT NULL, " +
                DatabaseContract.HkEntry.COLUMN_TEMPERATURE + " REAL NOT NULL" +
                ")");

        // tcp50p
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContract.Tcp50pEntry.TABLE_NAME + " (" +
                DatabaseContract.Tcp50pEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.Tcp50pEntry.COLUMN_SIGNAL_VALUE + " REAL NOT NULL, " +
                DatabaseContract.Tcp50pEntry.COLUMN_TEMPERATURE + " REAL NOT NULL" +
                ")");

        // tsm50m
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContract.Tsm50mEntry.TABLE_NAME + " (" +
                DatabaseContract.Tsm50mEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.Tsm50mEntry.COLUMN_SIGNAL_VALUE + " REAL NOT NULL, " +
                DatabaseContract.Tsm50mEntry.COLUMN_TEMPERATURE + " REAL NOT NULL" +
                ")");

        // valve_work_sessions
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContract.ValveWorkSessionsEntry.TABLE_NAME + " (" +
                DatabaseContract.ValveWorkSessionsEntry.COLUMN_SESSION_ID + " TEXT PRIMARY KEY, " +
                DatabaseContract.ValveWorkSessionsEntry.COLUMN_SAVE_DATE + " TEXT NOT NULL, " +
                DatabaseContract.ValveWorkSessionsEntry.COLUMN_EQUIPMENT_DESCRIPTION + " TEXT NOT NULL" +
                ")");

        // valve_items
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContract.ValveItemsEntry.TABLE_NAME + " (" +
                DatabaseContract.ValveItemsEntry.COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.ValveItemsEntry.COLUMN_PARENT_SESSION_ID + " TEXT NOT NULL, " +
                DatabaseContract.ValveItemsEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                DatabaseContract.ValveItemsEntry.COLUMN_NAME_ENG + " TEXT, " +
                DatabaseContract.ValveItemsEntry.COLUMN_ISY + " TEXT, " +
                DatabaseContract.ValveItemsEntry.COLUMN_HAS_MOTOR + " INTEGER DEFAULT 0, " +
                DatabaseContract.ValveItemsEntry.COLUMN_IS_ASSEMBLED + " INTEGER DEFAULT 0, " +
                DatabaseContract.ValveItemsEntry.COLUMN_IS_CHECKED + " INTEGER DEFAULT 0, " +
                DatabaseContract.ValveItemsEntry.COLUMN_OPERATION_TIMESTAMP + " TEXT, " +
                "FOREIGN KEY (" + DatabaseContract.ValveItemsEntry.COLUMN_PARENT_SESSION_ID + ") REFERENCES " +
                DatabaseContract.ValveWorkSessionsEntry.TABLE_NAME + "(" + DatabaseContract.ValveWorkSessionsEntry.COLUMN_SESSION_ID + ") ON DELETE CASCADE" +
                ")");

        // measurements
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseContract.MeasurementsEntry.TABLE_NAME + " (" +
                DatabaseContract.MeasurementsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.MeasurementsEntry.COLUMN_MEASUREMENT_DATE + " TEXT NOT NULL, " +
                DatabaseContract.MeasurementsEntry.COLUMN_VALUE + " REAL NOT NULL, " +
                DatabaseContract.MeasurementsEntry.COLUMN_UNIT + " TEXT NOT NULL, " +
                DatabaseContract.MeasurementsEntry.COLUMN_TEMPERATURE + " REAL NOT NULL, " +
                DatabaseContract.MeasurementsEntry.COLUMN_SENSOR_TYPE + " TEXT NOT NULL, " +
                DatabaseContract.MeasurementsEntry.COLUMN_DESCRIPTION + " TEXT, " +
                DatabaseContract.MeasurementsEntry.COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");
    }

    // ==================== CREATE INDEXES ====================

    private void createIndexes(SQLiteDatabase db) {
        // gr21
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_gr21_signal ON " + DatabaseContract.Gr21Entry.TABLE_NAME + "(" + DatabaseContract.Gr21Entry.COLUMN_SIGNAL_VALUE + ")");

        // gr23
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_gr23_signal ON " + DatabaseContract.Gr23Entry.TABLE_NAME + "(" + DatabaseContract.Gr23Entry.COLUMN_SIGNAL_VALUE + ")");

        // ha
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ha_signal ON " + DatabaseContract.HaEntry.TABLE_NAME + "(" + DatabaseContract.HaEntry.COLUMN_SIGNAL_VALUE + ")");

        // hk
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_hk_signal ON " + DatabaseContract.HkEntry.TABLE_NAME + "(" + DatabaseContract.HkEntry.COLUMN_SIGNAL_VALUE + ")");

        // tcp50p
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_tcp50p_signal ON " + DatabaseContract.Tcp50pEntry.TABLE_NAME + "(" + DatabaseContract.Tcp50pEntry.COLUMN_SIGNAL_VALUE + ")");

        // tsm50m
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_tsm50m_signal ON " + DatabaseContract.Tsm50mEntry.TABLE_NAME + "(" + DatabaseContract.Tsm50mEntry.COLUMN_SIGNAL_VALUE + ")");

        // setpoint_schedule
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_setpoint_group ON " + DatabaseContract.SetpointScheduleEntry.TABLE_NAME + "(" + DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_setpoint_name ON " + DatabaseContract.SetpointScheduleEntry.TABLE_NAME + "(" + DatabaseContract.SetpointScheduleEntry.COLUMN_NAME + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_setpoint_position ON " + DatabaseContract.SetpointScheduleEntry.TABLE_NAME + "(" + DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME + ")");

        // measurements
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_measurements_date ON " + DatabaseContract.MeasurementsEntry.TABLE_NAME + "(" + DatabaseContract.MeasurementsEntry.COLUMN_MEASUREMENT_DATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_measurements_sensor_type ON " + DatabaseContract.MeasurementsEntry.TABLE_NAME + "(" + DatabaseContract.MeasurementsEntry.COLUMN_SENSOR_TYPE + ")");
    }
}