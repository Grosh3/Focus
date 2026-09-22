package com.mikesuvade.focus.data.repository;

import android.content.ContentValues;
import android.database.Cursor;

import com.mikesuvade.focus.data.database.DatabaseContract;
import com.mikesuvade.focus.domain.models.GateValve;
import com.mikesuvade.focus.domain.models.Measurement;
import com.mikesuvade.focus.domain.models.Sensor;
import com.mikesuvade.focus.domain.models.Setpoint;
import com.mikesuvade.focus.domain.models.ValveItem;
import com.mikesuvade.focus.domain.models.ValveWorkSession;

final class CursorMapper {

    CursorMapper() {}

    // ==================== GATE VALVES ====================

    GateValve cursorToGateValve(Cursor cursor) {
        GateValve valve = new GateValve();
        valve.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry._ID)));
        valve.setNameEng(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_NAME_ENG)));
        valve.setKks(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_KKS)));
        valve.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_NAME)));

        String isy = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_ISY));
        if (isy != null && isy.endsWith(".0")) {
            isy = isy.substring(0, isy.length() - 2);
        }
        valve.setIsy(isy);

        valve.setPowerCabinet(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_POWER_CABINET)));
        valve.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_FULL_NAME)));
        valve.setOnPlace(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_ON_PLACE)));
        valve.setAp50(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_AP_50)));
        valve.setMark(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_MARK)));
        valve.setCdaCabinet(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_CDA_CABINET)));
        valve.setCdaCabinetPosition(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_CDA_CABINET_POSITION)));
        valve.setSlot(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_SLOT)));
        valve.setNameSpaceViewOpen(cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_NAME_SPACE_VIEW_OPEN)));
        valve.setDescriptionBlockingOpen(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_DESCRIPTION_BLOCKING_OPEN)));
        valve.setNamespaceViewClose(cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_NAMESPACE_VIEW_CLOSE)));
        valve.setDescriptionBlockingClose(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_DESCRIPTION_BLOCKING_CLOSE)));
        valve.setNamespaceViewPerifer(cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_NAMESPACE_VIEW_PERIFER)));
        valve.setDescriptionBlockingPerifer(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_DESCRIPTION_BLOCKING_PERIFER)));
        valve.setLocationDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_LOCATION_DESCRIPTION)));
        valve.setIsEdited(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_IS_EDITED)));
        valve.setEditedAtValve(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GateValvesEntry.COLUMN_EDITED_AT)));
        return valve;
    }

    GateValve cursorToUserGateValve(Cursor cursor) {
        GateValve valve = new GateValve();
        valve.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        valve.setOriginalId(cursor.getInt(cursor.getColumnIndexOrThrow("original_id")));
        valve.setIsDeleted(cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")));
        valve.setNameEng(cursor.getString(cursor.getColumnIndexOrThrow("name_eng")));
        valve.setKks(cursor.getString(cursor.getColumnIndexOrThrow("kks")));
        valve.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));

        String isy = cursor.getString(cursor.getColumnIndexOrThrow("isy"));
        if (isy != null && isy.endsWith(".0")) {
            isy = isy.substring(0, isy.length() - 2);
        }
        valve.setIsy(isy);

        valve.setPowerCabinet(cursor.getString(cursor.getColumnIndexOrThrow("power_cabinet")));
        valve.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        valve.setOnPlace(cursor.getString(cursor.getColumnIndexOrThrow("on_place")));
        valve.setAp50(cursor.getString(cursor.getColumnIndexOrThrow("ap_50")));
        valve.setMark(cursor.getString(cursor.getColumnIndexOrThrow("mark")));
        valve.setCdaCabinet(cursor.getString(cursor.getColumnIndexOrThrow("cda_cabinet")));
        valve.setCdaCabinetPosition(cursor.getString(cursor.getColumnIndexOrThrow("cda_cabinet_position")));
        valve.setSlot(cursor.getString(cursor.getColumnIndexOrThrow("slot")));
        valve.setDescriptionBlockingOpen(cursor.getString(cursor.getColumnIndexOrThrow("description_blocking_open")));
        valve.setDescriptionBlockingClose(cursor.getString(cursor.getColumnIndexOrThrow("description_blocking_close")));
        valve.setDescriptionBlockingPerifer(cursor.getString(cursor.getColumnIndexOrThrow("description_blocking_perifer")));
        valve.setLocationDescription(cursor.getString(cursor.getColumnIndexOrThrow("location_description")));
        valve.setIsEdited(cursor.getInt(cursor.getColumnIndexOrThrow("is_edited")));
        valve.setEditedAtValve(cursor.getString(cursor.getColumnIndexOrThrow("edited_at")));
        valve.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return valve;
    }

    ContentValues gateValveToContentValues(GateValve valve) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.GateValvesEntry.COLUMN_NAME_ENG, valve.getNameEng());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_KKS, valve.getKks());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_NAME, valve.getName());

        String isy = valve.getIsy();
        if (isy != null && isy.endsWith(".0")) {
            isy = isy.substring(0, isy.length() - 2);
        }
        values.put(DatabaseContract.GateValvesEntry.COLUMN_ISY, isy);

        values.put(DatabaseContract.GateValvesEntry.COLUMN_POWER_CABINET, valve.getPowerCabinet());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_FULL_NAME, valve.getFullName());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_ON_PLACE, valve.getOnPlace());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_AP_50, valve.getAp50());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_MARK, valve.getMark());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_CDA_CABINET, valve.getCdaCabinet());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_CDA_CABINET_POSITION, valve.getCdaCabinetPosition());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_SLOT, valve.getSlot());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_NAME_SPACE_VIEW_OPEN, valve.getNameSpaceViewOpen());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_DESCRIPTION_BLOCKING_OPEN, valve.getDescriptionBlockingOpen());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_NAMESPACE_VIEW_CLOSE, valve.getNamespaceViewClose());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_DESCRIPTION_BLOCKING_CLOSE, valve.getDescriptionBlockingClose());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_NAMESPACE_VIEW_PERIFER, valve.getNamespaceViewPerifer());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_DESCRIPTION_BLOCKING_PERIFER, valve.getDescriptionBlockingPerifer());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_LOCATION_DESCRIPTION, valve.getLocationDescription());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_IS_EDITED, valve.getIsEdited());
        values.put(DatabaseContract.GateValvesEntry.COLUMN_EDITED_AT, valve.getEditedAtValve());
        return values;
    }

    ContentValues userGateValveToContentValues(GateValve valve) {
        ContentValues values = new ContentValues();
        values.put("id", valve.getId());
        values.put("original_id", valve.getOriginalId());
        values.put("is_deleted", valve.getIsDeleted());
        values.put("name_eng", valve.getNameEng());
        values.put("kks", valve.getKks());
        values.put("name", valve.getName());
        values.put("isy", valve.getIsy());
        values.put("power_cabinet", valve.getPowerCabinet());
        values.put("full_name", valve.getFullName());
        values.put("on_place", valve.getOnPlace());
        values.put("ap_50", valve.getAp50());
        values.put("mark", valve.getMark());
        values.put("cda_cabinet", valve.getCdaCabinet());
        values.put("cda_cabinet_position", valve.getCdaCabinetPosition());
        values.put("slot", valve.getSlot());
        values.put("description_blocking_open", valve.getDescriptionBlockingOpen());
        values.put("description_blocking_close", valve.getDescriptionBlockingClose());
        values.put("description_blocking_perifer", valve.getDescriptionBlockingPerifer());
        values.put("location_description", valve.getLocationDescription());
        values.put("is_edited", valve.getIsEdited());
        values.put("edited_at", valve.getEditedAtValve());
        values.put("created_at", valve.getCreatedAt());
        return values;
    }

    // ==================== SENSORS ====================

    Sensor cursorToSensor(Cursor cursor) {
        Sensor sensor = new Sensor();
        sensor.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        sensor.setKks(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_KKS)));
        sensor.setStMarkir(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR)));
        sensor.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_FULL_NAME)));
        sensor.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_NAME)));
        sensor.setMedia(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_MEDIA)));
        sensor.setUnits(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_UNITS)));
        sensor.setNominal(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_NOMINAL)));
        sensor.setVolMin(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_VOL_MIN)));
        sensor.setVolMax(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_VOL_MAX)));
        sensor.setSpeed(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_SPEED)));
        sensor.setFaultPar(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_FAULT_PAR)));
        sensor.setInsteadF(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_INSTEAD_F)));
        sensor.setFilter(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_FILTER)));
        sensor.setModelSensor(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_MODEL_SENSOR)));
        sensor.setModSensor(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_MOD_SENSOR)));
        sensor.setAdditionalInfo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_ADDITIONAL_INFO)));
        sensor.setMinVal(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_MIN)));
        sensor.setMaxVal(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_MAX)));
        sensor.setMeasureUnit(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_MEASURE_UNIT)));
        sensor.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_LOCATION)));
        sensor.setCva(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_CVA)));
        sensor.setDampingTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SensorScheduleEntry.COLUMN_DAMPING_TIME)));

        sensor.setKeynum(0);
        sensor.setFa(0);
        sensor.setIsDeleted(0);
        sensor.setOriginalId(0);
        return sensor;
    }

    Sensor cursorToUserSensor(Cursor cursor) {
        Sensor sensor = new Sensor();
        sensor.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        sensor.setOriginalId(cursor.getInt(cursor.getColumnIndexOrThrow("original_id")));
        sensor.setIsDeleted(cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")));
        sensor.setKeynum(cursor.getInt(cursor.getColumnIndexOrThrow("keynum")));
        sensor.setFa(cursor.getInt(cursor.getColumnIndexOrThrow("fa")));
        sensor.setKks(cursor.getString(cursor.getColumnIndexOrThrow("kks")));
        sensor.setStMarkir(cursor.getString(cursor.getColumnIndexOrThrow("st_marking")));
        sensor.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        sensor.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        sensor.setMedia(cursor.getString(cursor.getColumnIndexOrThrow("media")));
        sensor.setUnits(cursor.getString(cursor.getColumnIndexOrThrow("units")));
        sensor.setNominal(cursor.getDouble(cursor.getColumnIndexOrThrow("nominal")));
        sensor.setVolMin(cursor.getDouble(cursor.getColumnIndexOrThrow("vol_min")));
        sensor.setVolMax(cursor.getDouble(cursor.getColumnIndexOrThrow("vol_max")));
        sensor.setSpeed(cursor.getString(cursor.getColumnIndexOrThrow("speed")));
        sensor.setFaultPar(cursor.getString(cursor.getColumnIndexOrThrow("fault_param")));
        sensor.setInsteadF(cursor.getString(cursor.getColumnIndexOrThrow("instead_f")));
        sensor.setFilter(cursor.getString(cursor.getColumnIndexOrThrow("filter_value")));
        sensor.setModelSensor(cursor.getString(cursor.getColumnIndexOrThrow("sensor_model")));
        sensor.setModSensor(cursor.getString(cursor.getColumnIndexOrThrow("sensor_mod")));
        sensor.setAdditionalInfo(cursor.getString(cursor.getColumnIndexOrThrow("additional_info")));
        sensor.setMinVal(cursor.getDouble(cursor.getColumnIndexOrThrow("min")));
        sensor.setMaxVal(cursor.getDouble(cursor.getColumnIndexOrThrow("max")));
        sensor.setMeasureUnit(cursor.getString(cursor.getColumnIndexOrThrow("unit_measure")));
        sensor.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("installation_location")));
        sensor.setCva(cursor.getString(cursor.getColumnIndexOrThrow("cva")));
        sensor.setDampingTime(cursor.getString(cursor.getColumnIndexOrThrow("damping_time")));
        sensor.setIsEdited(cursor.getInt(cursor.getColumnIndexOrThrow("is_edited")));
        sensor.setEditedAt(cursor.getString(cursor.getColumnIndexOrThrow("edited_at")));
        sensor.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return sensor;
    }

    ContentValues sensorToContentValues(Sensor sensor) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_KKS, sensor.getKks());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_ST_MARKIR, sensor.getStMarkir());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_FULL_NAME, sensor.getFullName());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_NAME, sensor.getName());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_MEDIA, sensor.getMedia());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_UNITS, sensor.getUnits());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_NOMINAL, sensor.getNominal());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_VOL_MIN, sensor.getVolMin());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_VOL_MAX, sensor.getVolMax());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_SPEED, sensor.getSpeed());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_FAULT_PAR, sensor.getFaultPar());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_INSTEAD_F, sensor.getInsteadF());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_FILTER, sensor.getFilter());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_MODEL_SENSOR, sensor.getModelSensor());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_MOD_SENSOR, sensor.getModSensor());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_ADDITIONAL_INFO, sensor.getAdditionalInfo());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_MIN, sensor.getMinVal());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_MAX, sensor.getMaxVal());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_MEASURE_UNIT, sensor.getMeasureUnit());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_LOCATION, sensor.getLocation());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_CVA, sensor.getCva());
        values.put(DatabaseContract.SensorScheduleEntry.COLUMN_DAMPING_TIME, sensor.getDampingTime());
        return values;
    }

    ContentValues userSensorToContentValues(Sensor sensor) {
        ContentValues values = new ContentValues();
        values.put("id", sensor.getId());
        values.put("original_id", sensor.getOriginalId());
        values.put("is_deleted", sensor.getIsDeleted());
        values.put("keynum", sensor.getKeynum());
        values.put("fa", sensor.getFa());
        values.put("kks", sensor.getKks());
        values.put("st_marking", sensor.getStMarkir());
        values.put("full_name", sensor.getFullName());
        values.put("name", sensor.getName());
        values.put("media", sensor.getMedia());
        values.put("units", sensor.getUnits());
        values.put("nominal", sensor.getNominal());
        values.put("vol_min", sensor.getVolMin());
        values.put("vol_max", sensor.getVolMax());
        values.put("speed", sensor.getSpeed());
        values.put("fault_param", sensor.getFaultPar());
        values.put("instead_f", sensor.getInsteadF());
        values.put("filter_value", sensor.getFilter());
        values.put("sensor_model", sensor.getModelSensor());
        values.put("sensor_mod", sensor.getModSensor());
        values.put("additional_info", sensor.getAdditionalInfo());
        values.put("min", sensor.getMinVal());
        values.put("max", sensor.getMaxVal());
        values.put("unit_measure", sensor.getMeasureUnit());
        values.put("installation_location", sensor.getLocation());
        values.put("cva", sensor.getCva());
        values.put("damping_time", sensor.getDampingTime());
        values.put("is_edited", sensor.getIsEdited());
        values.put("edited_at", sensor.getEditedAt());
        values.put("created_at", sensor.getCreatedAt());
        return values;
    }

    // ==================== SETPOINTS ====================

    Setpoint cursorToSetpoint(Cursor cursor) {
        Setpoint setpoint = new Setpoint();
        setpoint.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        setpoint.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry.COLUMN_NAME)));
        setpoint.setPositionName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME)));
        setpoint.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry.COLUMN_LOCATION)));
        setpoint.setSetpointValue(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry.COLUMN_SETPOINT_VALUE)));
        setpoint.setDelayTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry.COLUMN_DELAY_TIME)));
        setpoint.setOperation(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry.COLUMN_OPERATION)));
        setpoint.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry.COLUMN_NOTES)));
        setpoint.setEquipmentGroup(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP)));
        return setpoint;
    }

    Setpoint cursorToUserSetpoint(Cursor cursor) {
        Setpoint setpoint = new Setpoint();
        setpoint.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        setpoint.setOriginalId(cursor.getInt(cursor.getColumnIndexOrThrow("original_id")));
        setpoint.setIsDeleted(cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")));
        setpoint.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        setpoint.setPositionName(cursor.getString(cursor.getColumnIndexOrThrow("position_name")));
        setpoint.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("location")));
        setpoint.setSetpointValue(cursor.getString(cursor.getColumnIndexOrThrow("setpoint_value")));
        setpoint.setDelayTime(cursor.getString(cursor.getColumnIndexOrThrow("delay_time")));
        setpoint.setOperation(cursor.getString(cursor.getColumnIndexOrThrow("operation")));
        setpoint.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
        setpoint.setEquipmentGroup(cursor.getString(cursor.getColumnIndexOrThrow("equipment_group")));
        setpoint.setIsEdited(cursor.getInt(cursor.getColumnIndexOrThrow("is_edited")));
        setpoint.setEditedAt(cursor.getString(cursor.getColumnIndexOrThrow("edited_at")));
        setpoint.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return setpoint;
    }

    ContentValues setpointToContentValues(Setpoint setpoint) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SetpointScheduleEntry.COLUMN_NAME, setpoint.getName());
        values.put(DatabaseContract.SetpointScheduleEntry.COLUMN_POSITION_NAME, setpoint.getPositionName());
        values.put(DatabaseContract.SetpointScheduleEntry.COLUMN_LOCATION, setpoint.getLocation());
        values.put(DatabaseContract.SetpointScheduleEntry.COLUMN_SETPOINT_VALUE, setpoint.getSetpointValue());
        values.put(DatabaseContract.SetpointScheduleEntry.COLUMN_DELAY_TIME, setpoint.getDelayTime());
        values.put(DatabaseContract.SetpointScheduleEntry.COLUMN_OPERATION, setpoint.getOperation());
        values.put(DatabaseContract.SetpointScheduleEntry.COLUMN_NOTES, setpoint.getNotes());
        values.put(DatabaseContract.SetpointScheduleEntry.COLUMN_EQUIPMENT_GROUP, setpoint.getEquipmentGroup());
        return values;
    }

    ContentValues userSetpointToContentValues(Setpoint setpoint) {
        ContentValues values = new ContentValues();
        values.put("id", setpoint.getId());
        values.put("original_id", setpoint.getOriginalId());
        values.put("is_deleted", setpoint.getIsDeleted());
        values.put("name", setpoint.getName());
        values.put("position_name", setpoint.getPositionName());
        values.put("location", setpoint.getLocation());
        values.put("setpoint_value", setpoint.getSetpointValue());
        values.put("delay_time", setpoint.getDelayTime());
        values.put("operation", setpoint.getOperation());
        values.put("notes", setpoint.getNotes());
        values.put("equipment_group", setpoint.getEquipmentGroup());
        values.put("is_edited", setpoint.getIsEdited());
        values.put("edited_at", setpoint.getEditedAt());
        values.put("created_at", setpoint.getCreatedAt());
        return values;
    }

    // ==================== WORK SESSIONS ====================

    ValveWorkSession cursorToUserWorkSession(Cursor cursor) {
        ValveWorkSession session = new ValveWorkSession();
        session.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        session.setSessionId(cursor.getString(cursor.getColumnIndexOrThrow("session_id")));
        session.setSaveDate(cursor.getString(cursor.getColumnIndexOrThrow("save_date")));
        session.setEquipmentDescription(cursor.getString(cursor.getColumnIndexOrThrow("equipment_description")));
        session.setIsSynced(cursor.getInt(cursor.getColumnIndexOrThrow("is_synced")));
        session.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return session;
    }

    ContentValues userWorkSessionToContentValues(ValveWorkSession session) {
        ContentValues values = new ContentValues();
        values.put("session_id", session.getSessionId());
        values.put("save_date", session.getSaveDate());
        values.put("equipment_description", session.getEquipmentDescription());
        values.put("is_synced", session.getIsSynced());
        values.put("created_at", session.getCreatedAt());
        return values;
    }

    ValveItem cursorToUserSessionItem(Cursor cursor) {
        ValveItem item = new ValveItem();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        item.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        item.setParentSessionId(cursor.getString(cursor.getColumnIndexOrThrow("session_id")));
        item.setGateValveId(cursor.getInt(cursor.getColumnIndexOrThrow("gate_valve_id")));
        item.setIsAssembled(cursor.getInt(cursor.getColumnIndexOrThrow("is_assembled")));
        item.setMotorDisabled(cursor.getInt(cursor.getColumnIndexOrThrow("motor_disabled")));
        item.setBoxRemoved(cursor.getInt(cursor.getColumnIndexOrThrow("box_removed")));
        item.setIsChecked(cursor.getInt(cursor.getColumnIndexOrThrow("is_checked")));
        item.setCheckedAt(cursor.getString(cursor.getColumnIndexOrThrow("checked_at")));
        item.setOperationTimestamp(cursor.getString(cursor.getColumnIndexOrThrow("operation_timestamp")));
        return item;
    }

    ContentValues userSessionItemToContentValues(ValveItem item) {
        ContentValues values = new ContentValues();
        values.put("session_id", item.getParentSessionId());
        values.put("gate_valve_id", item.getGateValveId());
        values.put("is_assembled", item.getIsAssembled());
        values.put("motor_disabled", item.getMotorDisabled());
        values.put("box_removed", item.getBoxRemoved());
        values.put("is_checked", item.getIsChecked());
        values.put("checked_at", item.getCheckedAt());
        values.put("operation_timestamp", item.getOperationTimestamp());
        return values;
    }

    // ==================== MEASUREMENTS ====================

    Measurement cursorToUserMeasurement(Cursor cursor) {
        Measurement measurement = new Measurement();
        measurement.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        measurement.setMeasurementDate(cursor.getString(cursor.getColumnIndexOrThrow("measurement_date")));
        measurement.setValue(cursor.getDouble(cursor.getColumnIndexOrThrow("value")));
        measurement.setInputValue(cursor.getDouble(cursor.getColumnIndexOrThrow("input_value")));
        measurement.setUnit(cursor.getString(cursor.getColumnIndexOrThrow("unit")));
        measurement.setTemperature(cursor.getDouble(cursor.getColumnIndexOrThrow("temperature")));
        measurement.setSensorType(cursor.getString(cursor.getColumnIndexOrThrow("sensor_type")));
        measurement.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        measurement.setColdJunctionMv(cursor.getDouble(cursor.getColumnIndexOrThrow("cold_junction_mv")));
        measurement.setLineResistance(cursor.getDouble(cursor.getColumnIndexOrThrow("line_resistance")));
        measurement.setIsSynced(cursor.getInt(cursor.getColumnIndexOrThrow("is_synced")));
        measurement.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return measurement;
    }

    ContentValues userMeasurementToContentValues(Measurement measurement) {
        ContentValues values = new ContentValues();
        values.put("measurement_date", measurement.getMeasurementDate());
        values.put("value", measurement.getValue());
        values.put("input_value", measurement.getInputValue());
        values.put("unit", measurement.getUnit());
        values.put("temperature", measurement.getTemperature());
        values.put("sensor_type", measurement.getSensorType());
        values.put("description", measurement.getDescription());
        values.put("cold_junction_mv", measurement.getColdJunctionMv());
        values.put("line_resistance", measurement.getLineResistance());
        values.put("is_synced", measurement.getIsSynced());
        values.put("created_at", measurement.getCreatedAt());
        return values;
    }
}