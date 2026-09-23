package com.mikesuvade.focus.ui.detail;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.mikesuvade.focus.MyApp;
import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.repository.IRepository;
import com.mikesuvade.focus.ui.detail.editor.BaseEntityEditor;
import com.mikesuvade.focus.ui.detail.editor.SensorEditor;
import com.mikesuvade.focus.ui.detail.editor.SetpointEditor;
import com.mikesuvade.focus.ui.detail.editor.ValveEditor;
import com.mikesuvade.focus.ui.detail.helper.KeyboardInsetsHelper;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = BaseEntityEditor.EXTRA_TYPE;
    public static final String EXTRA_ID = BaseEntityEditor.EXTRA_ID;
    public static final String EXTRA_KKS = BaseEntityEditor.EXTRA_KKS;
    public static final String EXTRA_IS_NEW = BaseEntityEditor.EXTRA_IS_NEW;
    public static final String EXTRA_IS_FROM_USER_DB = BaseEntityEditor.EXTRA_IS_FROM_USER_DB;

    public static final int TYPE_VALVE = BaseEntityEditor.TYPE_VALVE;
    public static final int TYPE_SENSOR = BaseEntityEditor.TYPE_SENSOR;
    public static final int TYPE_SETPOINT = BaseEntityEditor.TYPE_SETPOINT;

    private BaseEntityEditor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        IRepository repository = ((MyApp) getApplication()).getRepository();
        int entityType = getIntent().getIntExtra(EXTRA_TYPE, TYPE_VALVE);

        switch (entityType) {
            case TYPE_VALVE:    editor = new ValveEditor(this, repository);    break;
            case TYPE_SENSOR:   editor = new SensorEditor(this, repository);   break;
            case TYPE_SETPOINT: editor = new SetpointEditor(this, repository); break;
            default:
                Toast.makeText(this, "Неизвестный тип сущности", Toast.LENGTH_SHORT).show();
                finish();
                return;
        }

        View root = findViewById(android.R.id.content);
        editor.bindViews(root);
        editor.loadFromIntent(getIntent());

        if (editor.getScreenTitle() != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(editor.getScreenTitle());
        }

        ScrollView fieldsContainer = findViewById(R.id.fieldsContainer);
        if (fieldsContainer != null) {
            KeyboardInsetsHelper.setup(fieldsContainer, getWindow());
        }

        fixToolbarPadding(toolbar);
        setStatusBarIconsDark(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            editor.save();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (editor == null) {
            super.onBackPressed();
            return;
        }

        if (editor.isSaved() || editor.isDeleting()) {
            super.onBackPressed();
            return;
        }

        if (editor.isOverlayVisible()) {
            editor.hideOverlay();
            return;
        }

        if (editor.hasChanges()) {
            Toast.makeText(this, R.string.unsaved_changes, Toast.LENGTH_SHORT).show();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (editor != null && editor.isSaved()) {
            editor.reloadFromDb();
        }
    }

    private void fixToolbarPadding(View toolbarView) {
        if (toolbarView == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(toolbarView, (view, windowInsets) -> {
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

    private void setStatusBarIconsDark(boolean dark) {
        Window window = getWindow();
        if (window != null) {
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
            controller.setAppearanceLightStatusBars(dark);
        }
    }
}