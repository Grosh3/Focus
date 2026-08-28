package com.mikesuvade.focus.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.R;
import com.mikesuvade.focus.ui.detail.DetailActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "current_theme";

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;

    private SharedPreferences prefs;

    private final ActivityResultLauncher<String> exportLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/octet-stream"),
            this::performExport
    );

    private final ActivityResultLauncher<String[]> importLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            this::performImport
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Настройки");
        }

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        initThemeSection();
        initAdminSection();
        initBackupSection();
    }

    private void initThemeSection() {
        RadioGroup rgTheme = findViewById(R.id.rgTheme);
        int savedTheme = prefs.getInt(KEY_THEME, THEME_SYSTEM);

        if (savedTheme == THEME_LIGHT) rgTheme.check(R.id.rbThemeLight);
        else if (savedTheme == THEME_DARK) rgTheme.check(R.id.rbThemeDark);
        else rgTheme.check(R.id.rbThemeSystem);

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int targetTheme = THEME_SYSTEM;
            int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

            if (checkedId == R.id.rbThemeLight) {
                targetTheme = THEME_LIGHT;
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.rbThemeDark) {
                targetTheme = THEME_DARK;
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            }

            prefs.edit().putInt(KEY_THEME, targetTheme).apply();
            AppCompatDelegate.setDefaultNightMode(mode);
        });
    }

    private void initAdminSection() {
        Button btnCreateValve = findViewById(R.id.btnAdminCreateValve);
        Button btnCreateSensor = findViewById(R.id.btnAdminCreateSensor);
        Button btnCreateSetpoint = findViewById(R.id.btnAdminCreateSetpoint);

        btnCreateValve.setOnClickListener(v -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_IS_NEW, true);
            startActivity(intent);
        });

        btnCreateSensor.setOnClickListener(v -> {
            Toast.makeText(this, "Форма создания датчика", Toast.LENGTH_SHORT).show();
            // TODO: открыть активити для создания датчика
        });

        btnCreateSetpoint.setOnClickListener(v -> {
            Toast.makeText(this, "Форма создания уставки", Toast.LENGTH_SHORT).show();
            // TODO: открыть активити для создания уставки
        });
    }

    private void initBackupSection() {
        Button btnExport = findViewById(R.id.btnExportDb);
        Button btnImport = findViewById(R.id.btnImportDb);

        btnExport.setOnClickListener(v -> {
            String defaultFileName = "focus_backup_" + System.currentTimeMillis() + ".db";
            exportLauncher.launch(defaultFileName);
        });

        btnImport.setOnClickListener(v -> {
            importLauncher.launch(new String[]{"application/octet-stream", "*/*"});
        });
    }

    private void performExport(Uri targetUri) {
        if (targetUri == null) return;

        new Thread(() -> {
            try {
                File currentDb = getDatabasePath("focus_data.db");

                if (!currentDb.exists()) {
                    runOnUiThread(() -> Toast.makeText(this, "База данных не найдена", Toast.LENGTH_SHORT).show());
                    return;
                }

                try (InputStream in = new FileInputStream(currentDb);
                     OutputStream out = getContentResolver().openOutputStream(targetUri)) {

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }

                runOnUiThread(() -> Toast.makeText(this, "Экспорт успешно завершен!", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                Log.e(TAG, "Ошибка экспорта БД", e);
                runOnUiThread(() -> Toast.makeText(this, "Ошибка экспорта", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void performImport(Uri sourceUri) {
        if (sourceUri == null) return;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Внимание")
                .setMessage("Импорт резервной копии полностью заменит текущие данные приложения. Продолжить?")
                .setPositiveButton("Импортировать", (dialog, which) -> executeDbReplacement(sourceUri))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void executeDbReplacement(Uri sourceUri) {
        new Thread(() -> {
            try {
                File targetDb = getDatabasePath("focus_data.db");
                targetDb.getParentFile().mkdirs();

                try (InputStream in = getContentResolver().openInputStream(sourceUri);
                     OutputStream out = new FileOutputStream(targetDb)) {

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "База данных успешно восстановлена! Перезапустите приложение.", Toast.LENGTH_LONG).show();
                    finishAffinity();
                });
            } catch (Exception e) {
                Log.e(TAG, "Ошибка импорта БД", e);
                runOnUiThread(() -> Toast.makeText(this, "Ошибка при накатывании бэкапа", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}