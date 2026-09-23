package com.mikesuvade.focus.utils;

import com.mikesuvade.focus.domain.models.ValveWorkSession;

import java.util.ArrayList;
import java.util.List;

public class AppState {
    private static AppState instance;

    private ValveWorkSession activeSession;
    private boolean hasUnsavedChanges = false;
    private String lastOpenedSessionName = null;
    private String lastOpenedSessionId = null;

    private List<Integer> unsavedListIds = new ArrayList<>();

    private AppState() {}

    public static synchronized AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }

    // ==========================================
    // SESSION
    // ==========================================

    public ValveWorkSession getActiveSession() {
        return activeSession;
    }

    public void setActiveSession(ValveWorkSession session) {
        this.activeSession = session;
        if (session != null) {
            this.lastOpenedSessionName = session.getEquipmentDescription();
            this.lastOpenedSessionId = session.getSessionId();
        } else {
            this.lastOpenedSessionName = null;
            this.lastOpenedSessionId = null;
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
        clearUnsavedListIds();
    }

    public boolean hasActiveSession() {
        return activeSession != null || lastOpenedSessionId != null;
    }

    // ==========================================
    // UNSAVED LIST (черновик)
    // ==========================================

    public List<Integer> getUnsavedListIds() {
        if (unsavedListIds == null) {
            unsavedListIds = new ArrayList<>();
        }
        return unsavedListIds;
    }

    public void setUnsavedListIds(List<Integer> ids) {
        this.unsavedListIds = ids != null ? ids : new ArrayList<>();
    }

    public void clearUnsavedListIds() {
        if (unsavedListIds != null) {
            unsavedListIds.clear();
        }
    }

    public void addUnsavedId(int id) {
        if (unsavedListIds == null) {
            unsavedListIds = new ArrayList<>();
        }
        if (!unsavedListIds.contains(id)) {
            unsavedListIds.add(id);
        }
    }

    public void removeUnsavedId(int id) {
        if (unsavedListIds != null) {
            unsavedListIds.remove(Integer.valueOf(id));
        }
    }

    public boolean hasUnsavedList() {
        return unsavedListIds != null && !unsavedListIds.isEmpty();
    }
}