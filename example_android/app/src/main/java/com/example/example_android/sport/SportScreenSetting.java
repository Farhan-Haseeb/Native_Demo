package com.example.example_android.sport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Remarks:
 * 1. Get basic info: ble reply (err_code, operate, min_data_num, max_data_num, min_screen_num, max_screen_num,
 * sport_num, sport_item->sport_type, data_num, ***data_item**)
 * 2. Get detail info: ble reply (err_code, operate, sport_num, *sport_item)
 * 3. Edit screen content: ble reply (err_code, operate)
 * */
public class SportScreenSetting implements Serializable {
    public final static int OPERATE_GET_BASE_INFO = 1;
    public final static int OPERATE_GET_DETAIL_INFO = 2;
    public final static int OPERATE_EDIT_SCREEN_CONTENT = 3;
    public int operate;//Operation type: 0x01 query basic info,0x02 query detail info,0x03 edit
    public int sport_num;//Used when the operation type is 0x02 query detail info; at most 2 sport types can fetch their config details at once, e.g. with 6 sport types send 3 times, fetching 2 types each time. Number of sport types fetched; max count: 255
    public List<ScreenSportItem> sport_item_s = new ArrayList<>(); //Sport items
    //Reply data
    public int version;                              // Version number
    public int err_code;                             //Error code: 0 succeeded, non-0 is an error code.
    public int min_data_num;                         //Min number of layout items displayed
    public int max_data_num;                         //Max number of layout items displayed
    public int min_screen_num;                       //Min number of screens displayed
    public int max_screen_num = 6;                       //Max number of screens displayed
    public int data_num;                             //Number of all data items fetched; returns 0 for the detail info query; max count: 30
    public List<ScreenSportItem> sport_item_reply_s = new ArrayList<>();
    public List<ScreenItemConf> screen_item_conf_t = new ArrayList<>();//Get screen info, used for the basic query; the detail query returns NULL

    public static class DataItem implements Serializable {
        public int data_type;           //Type of the current data item
        public int data_sub_type;       //Currently selected data item sub-item, normal range: 0~15; invalid value: 0XFF, meaning no sub-option is selected
        @Override
        public String toString() {
            return "DataItem{" +
                    "data_type=" + data_type +
                    ", data_sub_type=" + data_sub_type +
                    '}';
        }
    }
    public static class ScreenItem implements Serializable {
        public int data_item_count;    //Number of data items
        public List<DataItem> data_item_s = new ArrayList<>();//Data items

        @Override
        public String toString() {
            return "ScreenItem{" +
                    "data_item_count=" + data_item_count +
                    ", data_item_s=" + data_item_s +
                    '}';
        }
    }

    public static class ScreenItemConf implements Serializable {
        public int layout_type;            //Screen layout type
        public int style;                  //Style

        @Override
        public String toString() {
            return "ScreenItemConf{" +
                    "layout_type=" + layout_type +
                    ", style=" + style +
                    '}';
        }
    }

}
