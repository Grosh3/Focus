package com.mikesuvade.focus.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mikesuvade.focus.data.database.UserDatabaseHelper;
import com.mikesuvade.focus.domain.models.ValveItem;
import com.mikesuvade.focus.domain.models.ValveWorkSession;

import java.util.ArrayList;
import java.util.List;

class SessionRepository {

    private final UserDatabaseHelper userDbHelper;
    private final CursorMapper mapper;

    SessionRepository(UserDatabaseHelper userDbHelper, CursorMapper mapper) {
        this.userDbHelper = userDbHelper;
        this.mapper = mapper;
    }

    // ==================== WORK SESSIONS ====================

    long insertSession(ValveWorkSession session) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = mapper.userWorkSessionToContentValues(session);
        return db.insert("user_work_sessions", null, values);
    }

    List<ValveWorkSession> getAllSessions() {
        List<ValveWorkSession> sessions = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_work_sessions", null, null, null, null, null, "save_date DESC");
        while (cursor.moveToNext()) sessions.add(mapper.cursorToUserWorkSession(cursor));
        cursor.close();
        return sessions;
    }

    ValveWorkSession getSessionById(String sessionId) {
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_work_sessions", null,
                "session_id = ?", new String[]{sessionId}, null, null, null);
        ValveWorkSession session = null;
        if (cursor.moveToFirst()) session = mapper.cursorToUserWorkSession(cursor);
        cursor.close();
        return session;
    }

    int updateSession(ValveWorkSession session) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = mapper.userWorkSessionToContentValues(session);
        return db.update("user_work_sessions", values, "session_id = ?",
                new String[]{session.getSessionId()});
    }

    int deleteSession(String sessionId) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        return db.delete("user_work_sessions", "session_id = ?", new String[]{sessionId});
    }

    int updateSessionDate(String sessionId, String saveDate) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("save_date", saveDate);
        return db.update("user_work_sessions", values, "session_id = ?", new String[]{sessionId});
    }

    int updateSessionName(String sessionId, String newName) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("equipment_description", newName);
        return db.update("user_work_sessions", values, "session_id = ?", new String[]{sessionId});
    }

    // ==================== SESSION ITEMS ====================

    long insertItem(ValveItem item) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = mapper.userSessionItemToContentValues(item);
        return db.insert("user_session_items", null, values);
    }

    List<ValveItem> getItemsBySession(String sessionId) {
        List<ValveItem> items = new ArrayList<>();
        SQLiteDatabase db = userDbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_session_items", null,
                "session_id = ?", new String[]{sessionId}, null, null, "id ASC");
        while (cursor.moveToNext()) items.add(mapper.cursorToUserSessionItem(cursor));
        cursor.close();
        return items;
    }

    int updateItem(ValveItem item) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = mapper.userSessionItemToContentValues(item);
        return db.update("user_session_items", values, "id = ?",
                new String[]{String.valueOf(item.getItemId())});
    }

    int deleteItem(int itemId) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        return db.delete("user_session_items", "id = ?", new String[]{String.valueOf(itemId)});
    }

    int updateItemStatus(int itemId, int isAssembled, int motorDisabled, int boxRemoved) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_assembled", isAssembled);
        values.put("motor_disabled", motorDisabled);
        values.put("box_removed", boxRemoved);
        values.put("operation_timestamp", RepositoryUtils.getCurrentDateTime());
        return db.update("user_session_items", values, "id = ?",
                new String[]{String.valueOf(itemId)});
    }

    int updateItemChecked(int itemId, int isChecked, String checkedAt) {
        SQLiteDatabase db = userDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_checked", isChecked);
        values.put("checked_at", checkedAt);
        values.put("operation_timestamp", RepositoryUtils.getCurrentDateTime());
        return db.update("user_session_items", values, "id = ?",
                new String[]{String.valueOf(itemId)});
    }
}