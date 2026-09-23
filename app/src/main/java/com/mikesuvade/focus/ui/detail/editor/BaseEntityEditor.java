package com.mikesuvade.focus.ui.detail.editor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.repository.IRepository;
import com.mikesuvade.focus.ui.detail.helper.DateTimeUtils;

public abstract class BaseEntityEditor {

    public static final String EXTRA_TYPE = "entity_type";
    public static final String EXTRA_ID = "entity_id";
    public static final String EXTRA_KKS = "entity_kks";
    public static final String EXTRA_IS_NEW = "is_new";
    public static final String EXTRA_IS_FROM_USER_DB = "is_from_user_db";

    public static final int TYPE_VALVE = 1;
    public static final int TYPE_SENSOR = 2;
    public static final int TYPE_SETPOINT = 3;

    protected final AppCompatActivity activity;
    protected final IRepository repository;

    protected boolean isNew = false;
    protected boolean isSaved = false;
    protected boolean isDeleting = false;

    protected View fieldsContainer;
    protected Button btnDelete;

    public BaseEntityEditor(AppCompatActivity activity, IRepository repository) {
        this.activity = activity;
        this.repository = repository;
    }

    // ============ АБСТРАКТНЫЕ МЕТОДЫ ============

    public abstract void bindViews(View root);
    public abstract void createEmpty();
    public abstract void loadFromIntent(Intent intent);
    public abstract void displayData();
    public abstract void save();
    public abstract void delete();
    public abstract boolean hasChanges();
    public abstract String getScreenTitle();
    public abstract int getEntityType();
    public abstract boolean isOverlayVisible();
    public abstract void hideOverlay();
    public abstract void reloadFromDb();

    // ============ ОБЩАЯ ЛОГИКА ============

    public boolean isSaved() { return isSaved; }
    public boolean isDeleting() { return isDeleting; }

    protected void bindCommonViews(View root) {
        fieldsContainer = root.findViewById(R.id.fieldsContainer);
        btnDelete = root.findViewById(R.id.btnDelete);

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
        }
    }

    protected void updateDeleteButtonVisibility() {
        if (btnDelete == null) return;
        btnDelete.setVisibility(isNew ? View.GONE : View.VISIBLE);
    }

    protected void showDeleteConfirmationDialog() {
        String entityName;
        String displayName;

        switch (getEntityType()) {
            case TYPE_VALVE:
                entityName = "задвижку";
                displayName = getValveDisplayName();
                break;
            case TYPE_SENSOR:
                entityName = "датчик";
                displayName = getSensorDisplayName();
                break;
            case TYPE_SETPOINT:
                entityName = "уставку";
                displayName = getSetpointDisplayName();
                break;
            default:
                return;
        }

        if (displayName == null || displayName.isEmpty()) displayName = "без названия";
        final String finalName = displayName;

        new AlertDialog.Builder(activity)
                .setTitle("Удалить " + entityName + "?")
                .setMessage("Удалить \"" + finalName + "\"?\nДействие нельзя отменить.")
                .setPositiveButton("Удалить", (d, w) -> delete())
                .setNegativeButton("Отмена", null)
                .show();
    }

    protected void showDeleteDialog(String entityName, String entityTitle, Runnable deleteAction) {
        new AlertDialog.Builder(activity)
                .setTitle("Удалить " + entityName + "?")
                .setMessage("Все поля очищены. Удалить " + entityName + " \"" + entityTitle + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteAction.run())
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .show();
    }

    protected void onSaveSuccess(String message) {
        activity.runOnUiThread(() -> {
            if (activity.isFinishing() || activity.isDestroyed()) return;
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            activity.setResult(AppCompatActivity.RESULT_OK);
            activity.finish();
        });
    }

    protected void onSaveError(String message) {
        isSaved = false;
        activity.runOnUiThread(() -> {
            if (activity.isFinishing() || activity.isDestroyed()) return;
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Единая точка логирования + показа сообщения об ошибке.
     * Использовать во всех catch-блоках редакторов.
     */
    protected void logAndToast(String logMessage, Throwable cause, String userMessage) {
        Log.e(getClass().getSimpleName(), logMessage, cause);
        activity.runOnUiThread(() -> {
            if (activity.isFinishing() || activity.isDestroyed()) return;
            Toast.makeText(activity, userMessage, Toast.LENGTH_LONG).show();
        });
    }

    protected void hideKeyboard() {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            view.clearFocus();
        }
    }

    protected String getCurrentDateTime() {
        return DateTimeUtils.now();
    }

    protected double parseDouble(String value) {
        if (value == null || value.isEmpty()) return 0;
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    protected int generateNegativeId() {
        int hash = java.util.UUID.randomUUID().hashCode();
        if (hash == 0) return -1;
        return hash > 0 ? -hash : hash;
    }

    protected String textOf(android.widget.EditText et) {
        if (et == null) return "";
        return et.getText().toString().trim();
    }

    protected boolean eq(String a, String b) {
        return TextUtils.equals(a, b);
    }

    // ============ ЗАГЛУШКИ ДЛЯ ИМЁН (переопределяются при необходимости) ============

    protected String getValveDisplayName() { return ""; }
    protected String getSensorDisplayName() { return ""; }
    protected String getSetpointDisplayName() { return ""; }
}