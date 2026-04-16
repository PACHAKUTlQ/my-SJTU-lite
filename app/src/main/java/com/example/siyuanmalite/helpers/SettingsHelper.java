package com.example.siyuanmalite.helpers;

import android.content.SharedPreferences;

public class SettingsHelper {
    public static Boolean useIndentity;
    public static Boolean usePay;
    public static Boolean hideInfo;
    public static String supplier;

    public static void ReadSettings(SharedPreferences preferences)
    {
        useIndentity = preferences.getBoolean("useIndentity", true);
        usePay = preferences.getBoolean("usePay", true);
        hideInfo = preferences.getBoolean("hideInfo", false);
        supplier = preferences.getString("supplier", "weixiao");
    }

    public static void WriteSettings(SharedPreferences preferences)
    {
        preferences.edit()
                .putBoolean("useIndentity", useIndentity)
                .putBoolean("usePay", usePay)
                .putBoolean("hideInfo", hideInfo)
                .putString("supplier", supplier)
                .commit();
    }
}
