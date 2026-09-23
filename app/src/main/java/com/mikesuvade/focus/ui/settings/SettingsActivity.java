package com.mikesuvade.focus.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.mikesuvade.focus.R;
import com.mikesuvade.focus.ui.detail.DetailActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private final ActivityResultLauncher<Intent> createResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, "✅ Запись создана", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 🔥 ФИКС СТАТУС-БАРА
        fixTopPanelPadding();
        setStatusBarAndNavigationIconsDark(true);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        initThemeSection();
        initAdminSection();
        initBackupSection();
    }

    // ========================================== //
    // 🔧 ФИКС СТАТУС-БАРА
    // ========================================== //

    private void fixTopPanelPadding() {
        View topPanel = findViewById(R.id.topPanel);
        if (topPanel == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(topPanel, (view, windowInsets) -> {
            int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

            view.setPadding(
                    view.getPaddingLeft(),
                    statusBarHeight + view.getPaddingTop(),
                    view.getPaddingRight(),
                    view.getPaddingBottom()
            );

            ViewCompat.setOnApplyWindowInsetsListener(view, null);
            return windowInsets;
        });
    }

    private void setStatusBarAndNavigationIconsDark(boolean dark) {
        Window window = getWindow();
        if (window != null) {
            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(window, window.getDecorView());
            controller.setAppearanceLightStatusBars(dark);
            controller.setAppearanceLightNavigationBars(dark);
        }
    }

    // ========================================== //
    // 📋 ИНИЦИАЛИЗАЦИЯ
    // ========================================== //

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
            intent.putExtra(DetailActivity.EXTRA_TYPE, DetailActivity.TYPE_VALVE);
            intent.putExtra(DetailActivity.EXTRA_IS_NEW, true);
            createResultLauncher.launch(intent);
        });

        btnCreateSensor.setOnClickListener(v -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_TYPE, DetailActivity.TYPE_SENSOR);
            intent.putExtra(DetailActivity.EXTRA_IS_NEW, true);
            createResultLauncher.launch(intent);
        });

        btnCreateSetpoint.setOnClickListener(v -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_TYPE, DetailActivity.TYPE_SETPOINT);
            intent.putExtra(DetailActivity.EXTRA_IS_NEW, true);
            createResultLauncher.launch(intent);
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

    // ========================================== //
    // 📤 ЭКСПОРТ / ИМПОРТ
    // ========================================== //

    private void performExport(Uri targetUri) {
        if (targetUri == null) return;

        new Thread(() -> {
            try {
                File currentDb = getDatabasePath("focus_user.db");

                if (!currentDb.exists()) {
                    runOnUiThread(() -> Toast.makeText(this, "Пользовательская БД не найдена", Toast.LENGTH_SHORT).show());
                    return;
                }

                try (InputStream in = new FileInputStream(currentDb);
                     OutputStream out = getContentResolver().openOutputStream(targetUri)) {

                    if (out == null) {
                        throw new IOException("openOutputStream returned null");
                    }

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }

                runOnUiThread(() -> Toast.makeText(this, "✅ Экспорт успешно завершен!", Toast.LENGTH_LONG).show());
            } catch (IOException e) {
                Log.e(TAG, "IO error exporting DB", e);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(this, "Ошибка экспорта: доступ к файлу", Toast.LENGTH_LONG).show();
                });
            } catch (RuntimeException e) {
                Log.e(TAG, "Runtime error exporting DB", e);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(this, "Ошибка экспорта БД", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void performImport(Uri sourceUri) {
        if (sourceUri == null) return;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Внимание")
                .setMessage("Импорт резервной копии полностью заменит текущие пользовательские данные. Продолжить?")
                .setPositiveButton("Импортировать", (dialog, which) -> executeDbReplacement(sourceUri))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void executeDbReplacement(Uri sourceUri) {
        new Thread(() -> {
            try {
                File targetDb = getDatabasePath("focus_user.db");
                File parent = targetDb.getParentFile();
                if (parent != null) parent.mkdirs();

                try (InputStream in = getContentResolver().openInputStream(sourceUri);
                     OutputStream out = new FileOutputStream(targetDb)) {

                    if (in == null) {
                        throw new IOException("openInputStream returned null");
                    }

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "✅ База данных успешно восстановлена! Перезапустите приложение.", Toast.LENGTH_LONG).show();
                    finishAffinity();
                });
            } catch (IOException e) {
                Log.e(TAG, "IO error importing DB", e);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(this, "Ошибка импорта: доступ к файлу", Toast.LENGTH_LONG).show();
                });
            } catch (RuntimeException e) {
                Log.e(TAG, "Runtime error importing DB", e);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(this, "Ошибка импорта БД", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}