package com.mikesuvade.focus.data.database;

import android.content.ContentValues;
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
    private static final String ASSETS_PATH = DATABASE_NAME;

    private static DatabaseHelper instance;
    private final Context context;

    // Конструктор: только инициализация SQLiteOpenHelper, без обращений к БД
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
            // ВАЖНО: сразу проверяем и копируем, ДО любых getWritableDatabase()
            instance.ensureDatabaseReady();
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // НИЧЕГО не делаем: таблицы и данные уже есть в файле из assets
        Log.d(TAG, "onCreate: skipping — DB is pre-populated from assets");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Для pre-populated DB правильнее заменить файл целиком
        Log.w(TAG, "onUpgrade: replacing DB file from assets (v" + oldVersion + " -> " + newVersion + ")");
        try {
            copyDatabaseFromAssets();
        } catch (IOException e) {
            Log.e(TAG, "Failed to upgrade DB", e);
            throw new RuntimeException(e);
        }
    }

    public boolean isDatabaseReady() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        return dbFile.exists() && dbFile.length() > 0;
    }

    public void ensureDatabaseReady() {
        Log.d(TAG, "=== ensureDatabaseReady START ===");
        if (isDatabaseReady()) {
            Log.d(TAG, "✅ Database already exists and has size > 0");
            return;
        }

        Log.d(TAG, "📂 Copying database from assets...");
        try {
            copyDatabaseFromAssets();
            if (!isDatabaseReady()) {
                throw new IOException("Copied DB file is empty or missing");
            }
            Log.d(TAG, "✅ Database copied successfully, size: " + context.getDatabasePath(DATABASE_NAME).length());
        } catch (IOException e) {
            Log.e(TAG, "❌ Failed to copy database", e);
            // Не создавай пустую БД: лучше упасть с ошибкой, чем иметь пустую базу
            throw new RuntimeException("Database initialization failed", e);
        }
        Log.d(TAG, "=== ensureDatabaseReady END ===");
    }

    private void copyDatabaseFromAssets() throws IOException {
        InputStream inputStream = context.getAssets().open(ASSETS_PATH);
        File outFile = context.getDatabasePath(DATABASE_NAME);
        outFile.getParentFile().mkdirs();

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
