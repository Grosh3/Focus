package com.mikesuvade.focus;

import android.app.Application;
import android.util.Log;

import com.mikesuvade.focus.data.database.DatabaseHelper;
import com.mikesuvade.focus.data.repository.RepositoryImpl;
import com.mikesuvade.focus.domain.repository.IRepository;

public class MyApp extends Application {

    private static final String TAG = "MyApp";
    private static MyApp instance;
    private IRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Инициализируем DatabaseHelper и копируем БД из assets
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        dbHelper.ensureDatabaseReady();

        // Создаём Repository (внедряем зависимости)
        repository = new RepositoryImpl(dbHelper);

        Log.d(TAG, "Application initialized successfully");
    }

    public static MyApp getInstance() {
        return instance;
    }

    public IRepository getRepository() {
        return repository;
    }
}