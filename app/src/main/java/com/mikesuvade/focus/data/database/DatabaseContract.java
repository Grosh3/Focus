package com.mikesuvade.focus.data.database;

import android.provider.BaseColumns;

public final class DatabaseContract {

    private DatabaseContract() {}

    // ==================== GATE VALVES ====================
    public static class GateValvesEntry implements BaseColumns {
        public static final String TABLE_NAME = "gate_valves";
        public static final String COLUMN_NAME_ENG = "name_eng";
        public static final String COLUMN_KKS = "kks";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ISY = "isy";
        public static final String COLUMN_POWER_CABINET = "power_cabinet";
        public static final String COLUMN_FULL_NAME = "full_name_of_the_position";
        public static final String COLUMN_ON_PLACE = "on_place";
        public static final String COLUMN_AP_50 = "ap_50";
        public static final String COLUMN_MARK = "mark";
        public static final String COLUMN_CDA_CABINET = "cda_cabinet";
        public static final String COLUMN_CDA_CABINET_POSITION = "cda_cabinet_position";
        public static final String COLUMN_SLOT = "slot";
        public static final String COLUMN_NAME_SPACE_VIEW_OPEN = "name_space_view_open";
        public static final String COLUMN_DESCRIPTION_BLOCKING_OPEN = "description_blocking_open";
        public static final String COLUMN_NAMESPACE_VIEW_CLOSE = "namespace_view_close";
        public static final String COLUMN_DESCRIPTION_BLOCKING_CLOSE = "description_blocking_close";
        public static final String COLUMN_NAMESPACE_VIEW_PERIFER = "namespace_view_perifer";
        public static final String COLUMN_DESCRIPTION_BLOCKING_PERIFER = "description_blocking_perifer";
    }

    // ==================== GATE VALVES USER ====================
    public static class GateValvesUserEntry implements BaseColumns {
        public static final String TABLE_NAME = "gate_valves_user";
        public static final String COLUMN_NAME_ENG = "name_eng";
        public static final String COLUMN_KKS = "kks";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ISY = "isy";
        public static final String COLUMN_POWER_CABINET = "power_cabinet";
        public static final String COLUMN_FULL_NAME = "full_name_of_the_position";
        public static final String COLUMN_ON_PLACE = "on_place";
        public static final String COLUMN_AP_50 = "ap_50";
        public static final String COLUMN_MARK = "mark";
        public static final String COLUMN_CDA_CABINET = "cda_cabinet";
        public static final String COLUMN_CDA_CABINET_POSITION = "cda_cabinet_position";
        public static final String COLUMN_SLOT = "slot";
        public static final String COLUMN_NAME_SPACE_VIEW_OPEN = "name_space_view_open";
        public static final String COLUMN_DESCRIPTION_BLOCKING_OPEN = "description_blocking_open";
        public static final String COLUMN_NAMESPACE_VIEW_CLOSE = "namespace_view_close";
        public static final String COLUMN_DESCRIPTION_BLOCKING_CLOSE = "description_blocking_close";
        public static final String COLUMN_NAMESPACE_VIEW_PERIFER = "namespace_view_perifer";
        public static final String COLUMN_DESCRIPTION_BLOCKING_PERIFER = "description_blocking_perifer";
        public static final String COLUMN_ORIGINAL_ID = "original_id";
        public static final String COLUMN_EDITED_AT = "edited_at";
        public static final String COLUMN_IS_CUSTOM = "is_custom";
    }

    // ==================== SENSOR SCHEDULE ====================
    public static class SensorScheduleEntry implements BaseColumns {
        public static final String TABLE_NAME = "sensor_schedule";
        public static final String COLUMN_KEYNUM = "KEYNUM";
        public static final String COLUMN_FA = "FA";
        public static final String COLUMN_KKS = "KKS";
        public static final String COLUMN_ST_MARKIR = "ст_маркир";
        public static final String COLUMN_FULL_NAME = "полное_название";
        public static final String COLUMN_NAME = "NAME";
        public static final String COLUMN_MEDIA = "MEDIA";
        public static final String COLUMN_UNITS = "UNITS";
        public static final String COLUMN_NOMINAL = "NOMINAL";
        public static final String COLUMN_VOL_MIN = "VOL_MIN";
        public static final String COLUMN_VOL_MAX = "VOL_MAX";
        public static final String COLUMN_SPEED = "SPEED";
        public static final String COLUMN_FAULT_PAR = "FAULT_PAR";
        public static final String COLUMN_INSTEAD_F = "INSTEAD_F";
        public static final String COLUMN_FILTER = "FILTER";
        public static final String COLUMN_MODEL_SENSOR = "модель_датч";
        public static final String COLUMN_MOD_SENSOR = "мод_датчика";
        public static final String COLUMN_ADDITIONAL_INFO = "доп_сведения";
        public static final String COLUMN_MIN = "min";
        public static final String COLUMN_MAX = "max";
        public static final String COLUMN_MEASURE_UNIT = "ед_изм";
        public static final String COLUMN_LOCATION = "место_установки";
        public static final String COLUMN_CVA = "CVA";
        public static final String COLUMN_DAMPING_TIME = "вр_демпф";
    }

    // ==================== SENSOR SCHEDULE USER ====================
    public static class SensorScheduleUserEntry implements BaseColumns {
        public static final String TABLE_NAME = "sensor_schedule_user";
        public static final String COLUMN_ORIGINAL_KKS = "original_kks";
        public static final String COLUMN_KEYNUM = "keynum";
        public static final String COLUMN_FA = "fa";
        public static final String COLUMN_ST_MARKIR = "st_markir";
        public static final String COLUMN_FULL_NAME = "full_name";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_MEDIA = "media";
        public static final String COLUMN_UNITS = "units";
        public static final String COLUMN_NOMINAL = "nominal";
        public static final String COLUMN_VOL_MIN = "vol_min";
        public static final String COLUMN_VOL_MAX = "vol_max";
        public static final String COLUMN_SPEED = "speed";
        public static final String COLUMN_FAULT_PAR = "fault_par";
        public static final String COLUMN_INSTEAD_F = "instead_f";
        public static final String COLUMN_FILTER = "filter";
        public static final String COLUMN_MODEL_SENSOR = "model_sensor";
        public static final String COLUMN_MOD_SENSOR = "mod_sensor";
        public static final String COLUMN_ADDITIONAL_INFO = "additional_info";
        public static final String COLUMN_MIN_VAL = "min_val";
        public static final String COLUMN_MAX_VAL = "max_val";
        public static final String COLUMN_MEASURE_UNIT = "measure_unit";
        public static final String COLUMN_LOCATION = "location";
        public static final String COLUMN_CVA = "cva";
        public static final String COLUMN_DAMPING_TIME = "damping_time";
        public static final String COLUMN_EDITED_AT = "edited_at";
        public static final String COLUMN_IS_CUSTOM = "is_custom";
    }

    // ==================== SETPOINT SCHEDULE ====================
    public static class SetpointScheduleEntry implements BaseColumns {
        public static final String TABLE_NAME = "setpoint_schedule";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_POSITION_NAME = "position_name";
        public static final String COLUMN_LOCATION = "location";
        public static final String COLUMN_SETPOINT_VALUE = "setpoint_value";
        public static final String COLUMN_DELAY_TIME = "delay_time";
        public static final String COLUMN_OPERATION = "operation";
        public static final String COLUMN_NOTES = "notes";
        public static final String COLUMN_EQUIPMENT_GROUP = "equipment_group";
    }

    // ==================== SETPOINT SCHEDULE USER ====================
    public static class SetpointScheduleUserEntry implements BaseColumns {
        public static final String TABLE_NAME = "setpoint_schedule_user";
        public static final String COLUMN_ORIGINAL_ID = "original_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_POSITION_NAME = "position_name";
        public static final String COLUMN_LOCATION = "location";
        public static final String COLUMN_SETPOINT_VALUE = "setpoint_value";
        public static final String COLUMN_DELAY_TIME = "delay_time";
        public static final String COLUMN_OPERATION = "operation";
        public static final String COLUMN_NOTES = "notes";
        public static final String COLUMN_EQUIPMENT_GROUP = "equipment_group";
        public static final String COLUMN_EDITED_AT = "edited_at";
        public static final String COLUMN_IS_CUSTOM = "is_custom";
    }

    // ==================== CONVERTER TABLES (термометры/термопары) ====================
    public static class Gr21Entry implements BaseColumns {
        public static final String TABLE_NAME = "gr21";
        public static final String COLUMN_SIGNAL_VALUE = "signal_value";
        public static final String COLUMN_TEMPERATURE = "temperature";
    }

    public static class Gr23Entry implements BaseColumns {
        public static final String TABLE_NAME = "gr23";
        public static final String COLUMN_SIGNAL_VALUE = "signal_value";
        public static final String COLUMN_TEMPERATURE = "temperature";
    }

    public static class HaEntry implements BaseColumns {
        public static final String TABLE_NAME = "ha";
        public static final String COLUMN_SIGNAL_VALUE = "signal_value";
        public static final String COLUMN_TEMPERATURE = "temperature";
    }

    public static class HkEntry implements BaseColumns {
        public static final String TABLE_NAME = "hk";
        public static final String COLUMN_SIGNAL_VALUE = "signal_value";
        public static final String COLUMN_TEMPERATURE = "temperature";
    }

    public static class Tcp50pEntry implements BaseColumns {
        public static final String TABLE_NAME = "tcp50p";
        public static final String COLUMN_SIGNAL_VALUE = "signal_value";
        public static final String COLUMN_TEMPERATURE = "temperature";
    }

    public static class Tsm50mEntry implements BaseColumns {
        public static final String TABLE_NAME = "tsm50m";
        public static final String COLUMN_SIGNAL_VALUE = "signal_value";
        public static final String COLUMN_TEMPERATURE = "temperature";
    }

    // ==================== VALVE WORK SESSIONS ====================
    public static class ValveWorkSessionsEntry implements BaseColumns {
        public static final String TABLE_NAME = "valve_work_sessions";
        public static final String COLUMN_SESSION_ID = "session_id";
        public static final String COLUMN_SAVE_DATE = "save_date";
        public static final String COLUMN_EQUIPMENT_DESCRIPTION = "equipment_description";
    }

    // ==================== VALVE ITEMS ====================
    public static class ValveItemsEntry implements BaseColumns {
        public static final String TABLE_NAME = "valve_items";
        public static final String COLUMN_ITEM_ID = "item_id";
        public static final String COLUMN_PARENT_SESSION_ID = "parent_session_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_NAME_ENG = "name_eng";
        public static final String COLUMN_ISY = "isy";
        public static final String COLUMN_HAS_MOTOR = "has_motor";
        public static final String COLUMN_IS_ASSEMBLED = "is_assembled";
        public static final String COLUMN_IS_CHECKED = "is_checked";
        public static final String COLUMN_OPERATION_TIMESTAMP = "operation_timestamp";
    }

    // ==================== MEASUREMENTS ====================
    public static class MeasurementsEntry implements BaseColumns {
        public static final String TABLE_NAME = "measurements";
        public static final String COLUMN_MEASUREMENT_DATE = "measurement_date";
        public static final String COLUMN_VALUE = "value";
        public static final String COLUMN_UNIT = "unit";
        public static final String COLUMN_TEMPERATURE = "temperature";
        public static final String COLUMN_SENSOR_TYPE = "sensor_type";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_CREATED_AT = "created_at";
    }
}