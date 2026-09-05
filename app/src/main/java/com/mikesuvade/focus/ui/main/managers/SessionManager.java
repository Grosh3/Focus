package com.mikesuvade.focus.ui.main.managers;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.mikesuvade.focus.R;
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.ValveItem;
import com.mikesuvade.focus.domain.models.ValveWorkSession;
import com.mikesuvade.focus.domain.repository.IRepository;
import com.mikesuvade.focus.ui.list.ListDetailActivity;
import com.mikesuvade.focus.utils.AppState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SessionManager {

    private static final String TAG = "SessionManager";

    private final IRepository repository;
    private final Button btnNewValve;
    private final MainViewModelCallback viewModelCallback;
    private final SessionListener listener;

    public interface MainViewModelCallback {
        void clearCurrentList();
        int getCurrentListSize();
        List<GateValve> getCurrentList();
        void updateCurrentListSize(int size);
        void addToCurrentList(GateValve valve);
        void removeFromCurrentList(GateValve valve);
        void setRecording(boolean recording);
        boolean isInCurrentList(GateValve valve);
        void saveUserSessionToDb(ValveWorkSession session);
    }

    public interface SessionListener {
        void syncAdapterSelection();
        void updateButtonState();
        void refreshData();
        void launchListDetailActivity(Intent intent);
        void updateTitle(boolean showTitle);
    }

    public SessionManager(IRepository repository,
                          Button btnNewValve,
                          MainViewModelCallback viewModelCallback,
                          SessionListener listener) {
        this.repository = repository;
        this.btnNewValve = btnNewValve;
        this.viewModelCallback = viewModelCallback;
        this.listener = listener;
    }

    public void restoreLastSession() {
        Log.d(TAG, "=== restoreLastSession START ===");
        AppState appState = AppState.getInstance();

        if (appState.hasActiveSession()) {
            String sessionId = appState.getLastOpenedSessionId();
            Log.d(TAG, "sessionId = " + sessionId);

            new Thread(() -> {
                try {
                    List<ValveItem> items = repository.getUserSessionItemsBySession(sessionId);
                    Log.d(TAG, "items size = " + items.size());

                    List<GateValve> loadedValves = new ArrayList<>();
                    for (ValveItem item : items) {
                        GateValve valve = repository.getGateValveById(item.getGateValveId());
                        if (valve != null) {
                            loadedValves.add(valve);
                        }
                    }

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        viewModelCallback.clearCurrentList();
                        for (GateValve valve : loadedValves) {
                            viewModelCallback.addToCurrentList(valve);
                        }
                        viewModelCallback.updateCurrentListSize(items.size());
                        listener.syncAdapterSelection();
                        listener.updateButtonState();
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error restoring session", e);
                }
            }).start();
        } else {
            Log.d(TAG, "No active session");
            listener.syncAdapterSelection();
            listener.updateButtonState();
        }
    }

    public void openSession() {
        Log.d(TAG, "=== openSession START ===");
        String sessionId = AppState.getInstance().getLastOpenedSessionId();

        if (sessionId != null && !sessionId.isEmpty()) {
            Intent intent = new Intent(btnNewValve.getContext(), ListDetailActivity.class);
            intent.putExtra("session_id", sessionId);
            intent.putExtra("use_user_db", true);
            listener.launchListDetailActivity(intent);
        } else {
            Toast.makeText(btnNewValve.getContext(), "Сессия не найдена", Toast.LENGTH_SHORT).show();
        }
    }

    public void openNewList() {
        Log.d(TAG, "=== openNewList START ===");

        List<GateValve> currentList = viewModelCallback.getCurrentList();

        if (currentList == null || currentList.isEmpty()) {
            Toast.makeText(btnNewValve.getContext(), "Список пуст", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> isys = new ArrayList<>();

        for (GateValve valve : currentList) {
            ids.add(valve.getId());
            names.add(valve.getName() != null ? valve.getName() : "");
            isys.add(valve.getIsy() != null ? valve.getIsy() : "");
        }

        String sessionId = "SESSION_" + System.currentTimeMillis();

        ValveWorkSession session = new ValveWorkSession();
        session.setSessionId(sessionId);
        session.setEquipmentDescription("Новый список");
        session.setSaveDate(getCurrentDateTime());
        session.setCreatedAt(getCurrentDateTime());
        session.setIsSynced(0);

        AppState.getInstance().setActiveSession(session);
        AppState.getInstance().setHasUnsavedChanges(true);

        Intent intent = new Intent(btnNewValve.getContext(), ListDetailActivity.class);
        intent.putExtra("is_new_session", true);
        intent.putExtra("session_id", sessionId);
        intent.putExtra("use_user_db", true);
        intent.putIntegerArrayListExtra("valve_ids", ids);
        intent.putStringArrayListExtra("valve_names", names);
        intent.putStringArrayListExtra("valve_isys", isys);

        listener.launchListDetailActivity(intent);
    }

    public void showCloseListDialog() {
        new AlertDialog.Builder(btnNewValve.getContext())
                .setTitle(R.string.dialog_close_list_title)
                .setPositiveButton(R.string.dialog_close_list_positive, (dialog, which) -> closeCurrentList())
                .setNegativeButton(R.string.dialog_close_list_negative, (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void closeCurrentList() {
        AppState.getInstance().clearSession();
        viewModelCallback.clearCurrentList();
        viewModelCallback.setRecording(false);
        listener.updateButtonState();
        listener.syncAdapterSelection();
        Toast.makeText(btnNewValve.getContext(), R.string.toast_list_closed, Toast.LENGTH_SHORT).show();
    }

    public void updateButtonState() {
        Log.d(TAG, "=== updateButtonState START ===");
        AppState appState = AppState.getInstance();

        if (appState.hasActiveSession()) {
            btnNewValve.setVisibility(View.VISIBLE);
            String name = appState.getLastOpenedSessionName();
            int size = viewModelCallback.getCurrentListSize();
            btnNewValve.setText(name + " (" + size + ")");
            btnNewValve.setOnClickListener(v -> openSession());
            listener.updateTitle(false); // ← скрываем заголовок
            return;
        }

        if (viewModelCallback.getCurrentListSize() > 0) {
            btnNewValve.setVisibility(View.VISIBLE);
            int size = viewModelCallback.getCurrentListSize();
            btnNewValve.setText("К СПИСКУ (" + size + ")");
            btnNewValve.setOnClickListener(v -> openNewList());
            listener.updateTitle(false); // ← скрываем заголовок
            return;
        }

        btnNewValve.setVisibility(View.GONE);
        listener.updateTitle(true); // ← показываем заголовок
    }
    public void showClearSelectionDialog() {
        new AlertDialog.Builder(btnNewValve.getContext())
                .setTitle(R.string.dialog_clear_selection_title)
                .setPositiveButton(R.string.dialog_clear_selection_positive, (dialog, which) -> clearSelection())
                .setNegativeButton(R.string.dialog_clear_selection_negative, (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void clearSelection() {
        viewModelCallback.clearCurrentList();
        viewModelCallback.setRecording(false);
        listener.updateButtonState();
        listener.syncAdapterSelection();
        Toast.makeText(btnNewValve.getContext(), R.string.toast_selection_cleared, Toast.LENGTH_SHORT).show();
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

}