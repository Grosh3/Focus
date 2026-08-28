package com.mikesuvade.focus;

import android.app.Application;
import android.util.Log;

import com.mikesuvade.focus.data.database.DatabaseHelper;
import com.mikesuvade.focus.data.database.UserDatabaseHelper;
import com.mikesuvade.focus.data.repository.RepositoryImpl;
import com.mikesuvade.focus.domain.repository.IRepository;

public class MyApp extends Application {

    private static final String TAG = "MyApp";
    private static MyApp instance;
    private IRepository repository;
    private UserDatabaseHelper userDatabaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userDatabaseHelper = UserDatabaseHelper.getInstance(this);

        repository = new RepositoryImpl(dbHelper, userDatabaseHelper);

        Log.d(TAG, "Application initialized successfully");
    }

    public static MyApp getInstance() {
        return instance;
    }

    public IRepository getRepository() {
        return repository;
    }

    public UserDatabaseHelper getUserDatabaseHelper() {
        return userDatabaseHelper;
    }
}