package com.chaoliu.ipc.setting;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;

public class IpcUtils {

    public static void putInt(Context context, String key, int value) {
        Settings.System.putInt( context.getContentResolver(), key, value );
    }

    public static void putLong(Context context, String key, long value) {
        Settings.System.putLong( context.getContentResolver(), key, value );
    }

    public static void putString(Context context, String key, String value) {
        Settings.System.putString( context.getContentResolver(), key, value );
    }

    public static void putFloat(Context context, String key, float value) {
        Settings.System.putFloat( context.getContentResolver(), key, value );
    }

    public static int getInt(Context context, String key) throws Settings.SettingNotFoundException {
        return Settings.System.getInt( context.getContentResolver(), key );
    }

    public static int getInt(Context context, String key, int def) {
        return Settings.System.getInt( context.getContentResolver(), key, def );
    }

    public static long getLong(Context context, String key) throws Settings.SettingNotFoundException {
        return Settings.System.getLong( context.getContentResolver(), key );
    }

    public static long getLong(Context context, String key, long def) {
        return Settings.System.getLong( context.getContentResolver(), key, def );
    }

    public static String getString(Context context, String key) {
        return Settings.System.getString( context.getContentResolver(), key );
    }

    public static float getFloat(Context context, String key) throws Settings.SettingNotFoundException {
        return Settings.System.getFloat( context.getContentResolver(), key );
    }

    public static float getFloat(Context context, String key, float def) {
        return Settings.System.getFloat( context.getContentResolver(), key, def );
    }
}
