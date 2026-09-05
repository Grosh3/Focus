package com.mikesuvade.focus;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.mikesuvade.focus.data.database.DatabaseHelper;
import com.mikesuvade.focus.data.database.UserDatabaseHelper;
import com.mikesuvade.focus.data.repository.RepositoryImpl;
import com.mikesuvade.focus.domain.repository.IRepository;
import com.mikesuvade.focus.ui.settings.SettingsActivity;

public class MyApp extends Application {

    private static final String TAG = "MY_APP";
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "current_theme";

    private static MyApp instance;
    private IRepository repository;
    private UserDatabaseHelper userDatabaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "=== MyApp.onCreate START ===");

        try {
            instance = this;
            applySavedTheme();

            Log.d(TAG, "About to init DatabaseHelper");
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

            Log.d(TAG, "About to init UserDatabaseHelper");
            userDatabaseHelper = UserDatabaseHelper.getInstance(this);

            Log.d(TAG, "About to create Repository");
            repository = new RepositoryImpl(dbHelper, userDatabaseHelper);

            Log.d(TAG, "Application initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "ERROR in onCreate", e);
            e.printStackTrace();
        }
    }

    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int savedTheme = prefs.getInt(KEY_THEME, SettingsActivity.THEME_SYSTEM);

        int mode;
        switch (savedTheme) {
            case SettingsActivity.THEME_LIGHT:
                mode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case SettingsActivity.THEME_DARK:
                mode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }

        AppCompatDelegate.setDefaultNightMode(mode);
        Log.d(TAG, "Applied theme: " + savedTheme);
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