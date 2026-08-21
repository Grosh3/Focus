package com.mikesuvade.focus.ui.saved;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mikesuvade.focus.domain.models.ValveWorkSession;
import com.mikesuvade.focus.domain.repository.IRepository;

import java.util.List;

public class SavedListsViewModel extends ViewModel {

    private final IRepository repository;
    private final MutableLiveData<List<ValveWorkSession>> sessions = new MutableLiveData<>();

    public SavedListsViewModel(IRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<ValveWorkSession>> getSessions() {
        return sessions;
    }

    public void loadSessions() {
        new Thread(() -> {
            try {
                List<ValveWorkSession> result = repository.getAllWorkSessions();
                sessions.postValue(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void deleteSession(String sessionId) {
        new Thread(() -> {
            try {
                repository.deleteWorkSession(sessionId);
                loadSessions();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    public void updateSession(ValveWorkSession session) {
        new Thread(() -> {
            try {
                repository.updateWorkSession(session);
                loadSessions();  // обновляем список
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    public void renameSession(String sessionId, String newName) {
        new Thread(() -> {
            try {
                repository.updateWorkSessionName(sessionId, newName);
                loadSessions();  // обновляем список
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}