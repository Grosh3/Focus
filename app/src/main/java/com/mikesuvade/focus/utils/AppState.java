package com.mikesuvade.focus.utils;

import com.mikesuvade.focus.domain.models.ValveWorkSession;

public class AppState {
    private static AppState instance;

    private ValveWorkSession activeSession;
    private boolean hasUnsavedChanges = false;
    private String lastOpenedSessionName = null;
    private String lastOpenedSessionId = null;

    private AppState() {}

    public static synchronized AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }

    public ValveWorkSession getActiveSession() {
        return activeSession;
    }

    public void setActiveSession(ValveWorkSession session) {
        this.activeSession = session;
        if (session != null) {
            this.lastOpenedSessionName = session.getEquipmentDescription();
            this.lastOpenedSessionId = session.getSessionId();
        }
    }

    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    public void setHasUnsavedChanges(boolean hasUnsavedChanges) {
        this.hasUnsavedChanges = hasUnsavedChanges;
    }

    public String getLastOpenedSessionName() {
        return lastOpenedSessionName;
    }

    public String getLastOpenedSessionId() {
        return lastOpenedSessionId;
    }

    public void clearSession() {
        activeSession = null;
        hasUnsavedChanges = false;
        lastOpenedSessionName = null;
        lastOpenedSessionId = null;
    }

    public boolean hasActiveSession() {
        return activeSession != null || lastOpenedSessionId != null;
    }
}
