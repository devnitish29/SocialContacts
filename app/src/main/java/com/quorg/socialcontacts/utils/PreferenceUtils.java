package com.quorg.socialcontacts.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Nitish Singh on 14/4/17.
 */

public class PreferenceUtils {

    private static final String PREF_IS_USER_LOGGED_IN = "pref_is_user_logged_in";




    public static void setUserLoggedIn(Context context, boolean visible) {
        setBoolean(context, PREF_IS_USER_LOGGED_IN, visible);
    }

    public static boolean isUserLoggedIn(Context context) {
        return getBoolean(context, PREF_IS_USER_LOGGED_IN);
    }


    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(key, value).apply();
    }


    public static boolean getBoolean(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(key, false);
    }


    public static void setString(Context context, String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(key, value).apply();
    }

    public static String getString(Context context, String key, String defaultValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(key, defaultValue);
    }
}
