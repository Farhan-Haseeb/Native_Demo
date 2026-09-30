package com.clj.fastble.logs;

/**
 * @author: zhouzj
 * @date: 2017/10/25 14:23
 */

public class LogTag {
    public static final String TAG_CMD = "IDO_CMD";
    /**
     * Bind, unbind
     */
    public static final String TAG_CMD_BIND_UNBIND = "[BIND_UNBIND] ";

    /**
     * Sync
     */
    public static final String TAG_CMD_SYNC = "[SYNC_DATA] ";

    /**
     * Get info
     */
    public static final String TAG_CMD_GET = "[GET_INFO] ";
    /**
     * Set parameters
     */
    public static final String TAG_CMD_SET = "[SET_PARA] ";

    /**
     * Operate parameters (add, delete, modify, query)
     */
    public static final String TAG_CMD_OPERATE = "[OPERATE_PARA] ";

    /**
     * Get parameters
     */
    public static final String TAG_CMD_GET_PARA = "[GET_PARA] ";

    /**
     * Data operations
     */
    public static final String TAG_DATA_OPERATOR = "IDO_DATA ";

    /**
     * SO library actions
     */
    public static final String TAG_SO_LIB_ACTION = "SO_LIB_ACTION ";

    /**
     * GPS
     */
    public static final String TAG_GPS = "IDO_GPS ";

    /**
     * Voice
     */
    public static final String TAG_VOICE = "IDO_VOICE";

    /**
     * Notification reminders
     */
    public static final String TAG_NOTICE = "IDO_NOTICE";

    /**
     * WATCH_PLATE
     */
    public static final String TAG_WATCH_PLATE = "[IDO_WATCH_PLATE] ";
    public static final String TAG_CMD_COMMON = "[GET_SET_COMMON] ";
    public static final String TAG_EXCHANGE_DATA = "[EXCHANGE_DATA]";

    /**
     * Send data
     */
    public static final String TAG_APP_SEND_DATA = "[APP_SEND_DATA] ";

    public static final String TAG_BT_CONNECT = "BT_CONNECT";
}
