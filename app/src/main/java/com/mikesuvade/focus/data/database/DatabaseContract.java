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
        public static final String COLUMN_LOCATION_DESCRIPTION = "location_description";
        public static final String COLUMN_IS_EDITED = "is_edited";
        public static final String COLUMN_EDITED_AT = "edited_at";
    }

    // ==================== SENSOR SCHEDULE ====================
    public static class SensorScheduleEntry implements BaseColumns {
        public static final String TABLE_NAME = "sensor_schedule";
        public static final String COLUMN_KEYNUM = "keynum";
        public static final String COLUMN_FA = "fa";
        public static final String COLUMN_KKS = "kks";
        public static final String COLUMN_ST_MARKIR = "st_marking";      // ст.маркир
        public static final String COLUMN_FULL_NAME = "full_name";       // полное название
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_MEDIA = "media";
        public static final String COLUMN_UNITS = "units";
        public static final String COLUMN_NOMINAL = "nominal";
        public static final String COLUMN_VOL_MIN = "vol_min";
        public static final String COLUMN_VOL_MAX = "vol_max";
        public static final String COLUMN_SPEED = "speed";
        public static final String COLUMN_FAULT_PAR = "fault_param";
        public static final String COLUMN_INSTEAD_F = "instead_f";
        public static final String COLUMN_FILTER = "filter";
        public static final String COLUMN_MODEL_SENSOR = "sensor_model";  // модель датчика
        public static final String COLUMN_MOD_SENSOR = "sensor_mod";      // мод датчика
        public static final String COLUMN_ADDITIONAL_INFO = "additional_info"; // доп сведения
        public static final String COLUMN_MIN = "min";
        public static final String COLUMN_MAX = "max";
        public static final String COLUMN_MEASURE_UNIT = "unit_measure";  // ед.изм
        public static final String COLUMN_LOCATION = "installation_location"; // место установки
        public static final String COLUMN_CVA = "cva";
        public static final String COLUMN_DAMPING_TIME = "damping_time";  // вр.демпф
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

    // ==================== CONVERTER TABLES ====================
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
        public static final String COLUMN_GATE_VALVE_ID = "gate_valve_id";
        public static final String COLUMN_IS_ASSEMBLED = "is_assembled";
        public static final String COLUMN_MOTOR_DISABLED = "motor_disabled";
        public static final String COLUMN_BOX_REMOVED = "box_removed";
        public static final String COLUMN_IS_CHECKED = "is_checked";
        public static final String COLUMN_CHECKED_AT = "checked_at";
        public static final String COLUMN_OPERATION_TIMESTAMP = "operation_timestamp";
    }

    // ==================== MEASUREMENTS ====================
    public static class MeasurementsEntry implements BaseColumns {
        public static final String TABLE_NAME = "measurements";
        public static final String COLUMN_MEASUREMENT_DATE = "measurement_date";
        public static final String COLUMN_VALUE = "value";
        public static final String COLUMN_INPUT_VALUE = "input_value";
        public static final String COLUMN_UNIT = "unit";
        public static final String COLUMN_TEMPERATURE = "temperature";
        public static final String COLUMN_SENSOR_TYPE = "sensor_type";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_COLD_JUNCTION_MV = "cold_junction_mv";
        public static final String COLUMN_LINE_RESISTANCE = "line_resistance";
    }
    
}