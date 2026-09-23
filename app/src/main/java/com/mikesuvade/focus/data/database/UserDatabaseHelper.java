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

public class UserDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "UserDatabaseHelper";
    private static final String DATABASE_NAME = "focus_user.db";
    private static final int DATABASE_VERSION = 1;
    private static final String ASSETS_PATH = DATABASE_NAME;

    private static UserDatabaseHelper instance;
    private final Context context;

    // ==========================================
    // ТАБЛИЦЫ ПОЛЬЗОВАТЕЛЬСКИХ ДАННЫХ
    // ==========================================

    private static final String CREATE_USER_GATE_VALVES =
            "CREATE TABLE IF NOT EXISTS user_gate_valves (" +
                    "id INTEGER PRIMARY KEY," +
                    "original_id INTEGER," +
                    "is_deleted INTEGER DEFAULT 0," +
                    "name_eng TEXT," +
                    "kks TEXT," +
                    "name TEXT," +
                    "isy TEXT," +
                    "power_cabinet TEXT," +
                    "full_name TEXT," +
                    "on_place TEXT," +
                    "ap_50 TEXT," +
                    "mark TEXT," +
                    "cda_cabinet TEXT," +
                    "cda_cabinet_position TEXT," +
                    "slot TEXT," +
                    "description_blocking_open TEXT," +
                    "description_blocking_close TEXT," +
                    "description_blocking_perifer TEXT," +
                    "location_description TEXT," +
                    "is_edited INTEGER DEFAULT 1," +
                    "edited_at TEXT," +
                    "created_at TEXT" +
                    ")";

    private static final String CREATE_USER_SENSORS =
            "CREATE TABLE IF NOT EXISTS user_sensors (" +
                    "id INTEGER PRIMARY KEY," +
                    "original_id INTEGER," +
                    "is_deleted INTEGER DEFAULT 0," +
                    "keynum INTEGER," +
                    "fa INTEGER," +
                    "kks TEXT," +
                    "st_marking TEXT," +
                    "full_name TEXT," +
                    "name TEXT," +
                    "media TEXT," +
                    "units TEXT," +
                    "nominal REAL," +
                    "vol_min REAL," +
                    "vol_max REAL," +
                    "speed TEXT," +
                    "fault_param TEXT," +
                    "instead_f TEXT," +
                    "filter_value TEXT," +
                    "sensor_model TEXT," +
                    "sensor_mod TEXT," +
                    "additional_info TEXT," +
                    "min REAL," +
                    "max REAL," +
                    "unit_measure TEXT," +
                    "installation_location TEXT," +
                    "cva TEXT," +
                    "damping_time TEXT," +
                    "is_edited INTEGER DEFAULT 1," +
                    "edited_at TEXT," +
                    "created_at TEXT" +
                    ")";

    private static final String CREATE_USER_SETPOINTS =
            "CREATE TABLE IF NOT EXISTS user_setpoints (" +
                    "id INTEGER PRIMARY KEY," +
                    "original_id INTEGER," +
                    "is_deleted INTEGER DEFAULT 0," +
                    "name TEXT," +
                    "position_name TEXT," +
                    "location TEXT," +
                    "setpoint_value TEXT," +
                    "delay_time TEXT," +
                    "operation TEXT," +
                    "notes TEXT," +
                    "equipment_group TEXT," +
                    "is_edited INTEGER DEFAULT 1," +
                    "edited_at TEXT," +
                    "created_at TEXT" +
                    ")";

    private static final String CREATE_USER_WORK_SESSIONS =
            "CREATE TABLE IF NOT EXISTS user_work_sessions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "session_id TEXT UNIQUE," +
                    "save_date TEXT," +
                    "equipment_description TEXT," +
                    "is_synced INTEGER DEFAULT 0," +
                    "created_at TEXT" +
                    ")";

    private static final String CREATE_USER_SESSION_ITEMS =
            "CREATE TABLE IF NOT EXISTS user_session_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "session_id TEXT," +
                    "gate_valve_id INTEGER," +
                    "is_assembled INTEGER DEFAULT 1," +
                    "motor_disabled INTEGER DEFAULT 0," +
                    "box_removed INTEGER DEFAULT 0," +
                    "is_checked INTEGER DEFAULT 0," +
                    "checked_at TEXT," +
                    "operation_timestamp TEXT," +
                    "FOREIGN KEY (session_id) REFERENCES user_work_sessions(session_id)" +
                    ")";

    private static final String CREATE_USER_MEASUREMENTS =
            "CREATE TABLE IF NOT EXISTS user_measurements (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "measurement_date TEXT," +
                    "value REAL," +
                    "input_value REAL," +
                    "unit TEXT," +
                    "temperature REAL," +
                    "sensor_type TEXT," +
                    "description TEXT," +
                    "cold_junction_mv REAL," +
                    "line_resistance REAL," +
                    "is_synced INTEGER DEFAULT 0," +
                    "created_at TEXT" +
                    ")";

    // ==========================================
    // КОНСТРУКТОР И SINGLETON
    // ==========================================

    private UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    public static synchronized UserDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new UserDatabaseHelper(context.getApplicationContext());
            // ВАЖНО: сразу проверяем и копируем, ДО любых getWritableDatabase()
            instance.ensureDatabaseReady();
        }
        return instance;
    }

    // ==========================================
    // КОПИРОВАНИЕ ИЗ ASSETS
    // ==========================================

    public boolean isDatabaseReady() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        return dbFile.exists() && dbFile.length() > 0;
    }

    public void ensureDatabaseReady() {
        if (isDatabaseReady()) {
            return;
        }

        try {
            copyDatabaseFromAssets();
            if (!isDatabaseReady()) {
                throw new IOException("Copied user DB file is empty or missing");
            }
        } catch (IOException e) {
            // Если в assets нет файла — onCreate создаст пустые таблицы.
            // Это позволяет собирать проект без заранее подготовленной focus_user.db.
            Log.w(TAG, "Failed to copy user DB from assets, falling back to onCreate()", e);
        }
    }

    private void copyDatabaseFromAssets() throws IOException {
        File outFile = context.getDatabasePath(DATABASE_NAME);
        File parent = outFile.getParentFile();
        if (parent != null) parent.mkdirs();

        try (InputStream inputStream = context.getAssets().open(ASSETS_PATH);
             OutputStream outputStream = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
        }
        // inputStream.close() и outputStream.close() вызовутся автоматически
    }

    // ==========================================
    // LIFECYCLE
    // ==========================================

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Срабатывает ТОЛЬКО если в assets не оказалось focus_user.db.
        // Если assets-файл есть — SQLiteOpenHelper.onCreate не вызовется,
        // потому что файл уже создан копированием.

        db.execSQL(CREATE_USER_GATE_VALVES);
        db.execSQL(CREATE_USER_SENSORS);
        db.execSQL(CREATE_USER_SETPOINTS);
        db.execSQL(CREATE_USER_WORK_SESSIONS);
        db.execSQL(CREATE_USER_SESSION_ITEMS);
        db.execSQL(CREATE_USER_MEASUREMENTS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


        // 🔥 МИГРАЦИИ — добавляются здесь, по одной на версию.
        // Пример:
        // if (oldVersion < 2) {
        //     db.execSQL("ALTER TABLE user_gate_valves ADD COLUMN photo BLOB");
        // }
        // if (oldVersion < 3) {
        //     db.execSQL("CREATE TABLE IF NOT EXISTS user_bearings (...)");
        // }
    }

    // ==========================================
    // ПУБЛИЧНЫЙ ХЕЛПЕР (если нужно пересоздать БД вручную)
    // ==========================================

    /**
     * Принудительно копирует focus_user.db из assets заново,
     * затирая текущую пользовательскую БД.
     *
     * Используйте осторожно — пользовательские данные будут потеряны.
     * Может пригодиться в экране «Сбросить к заводским настройкам».
     */

}