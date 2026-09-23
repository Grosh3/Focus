package com.mikesuvade.focus.ui.saved;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mikesuvade.focus.domain.models.ValveWorkSession;
import com.mikesuvade.focus.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.List;

public class SavedListsViewModel extends ViewModel {

    private final IRepository repository;
    private final MutableLiveData<List<ValveWorkSession>> sessions = new MutableLiveData<>(new ArrayList<>());

    public SavedListsViewModel(IRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<ValveWorkSession>> getSessions() {
        return sessions;
    }

    public void loadSessions() {

        new Thread(() -> {
            try {
                // 🔥 ТОЛЬКО ПОЛЬЗОВАТЕЛЬСКАЯ БД
                List<ValveWorkSession> items = repository.getAllUserWorkSessions();

                if (items != null && !items.isEmpty()) {
                    for (int i = 0; i < items.size(); i++) {
                        ValveWorkSession s = items.get(i);

                    }
                }
                sessions.postValue(items);

            } catch (Exception e) {
                Log.e("SESSY", "Error loading sessions", e);

            }
        }).start();
    }

    public void deleteSession(String sessionId) {
        new Thread(() -> {
            try {
                repository.deleteUserWorkSession(sessionId);
                loadSessions();
            } catch (android.database.sqlite.SQLiteException e) {
                // ignore
            } catch (RuntimeException e) {
                // ignore
            }
        }).start();
    }

    public void renameSession(String sessionId, String newName) {
        new Thread(() -> {
            try {
                ValveWorkSession session = repository.getUserWorkSessionById(sessionId);
                if (session != null) {
                    session.setEquipmentDescription(newName);
                    repository.updateUserWorkSession(session);
                }
            } catch (android.database.sqlite.SQLiteException e) {
                // ignore
            } catch (RuntimeException e) {
                // ignore
            }
            loadSessions();   // ← вынесено, вызывается всегда
        }).start();
    }
}