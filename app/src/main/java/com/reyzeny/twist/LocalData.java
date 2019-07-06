package com.reyzeny.twist;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalData {
    private static final String AUTH = "auth";
    private static final String USERID = "userId";
    private static final String USERNAME = "username";
    private static final String FIRSTNAME = "firstname";
    private static final String LASTNAME = "lastname";
    private static final String PREFERENCES_NAME = "com.twist.sharedpreferences";
    private static final int PRIVATE_MODE = 0;
    private static final String USER_SETUP = "user_setup";


    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, PRIVATE_MODE);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    public static boolean isUserAuthenticated(Context context){
        return getSharedPreferences(context).getBoolean(AUTH, false);
    }

    public static void setUserAuthentication(Context context, boolean auth) {
        getEditor(context).putBoolean(AUTH, auth).apply();
    }

    public static void setUserId(Context context, String phoneNumber) {
        getEditor(context).putString(USERID, phoneNumber).apply();
    }

    public static String getUserName(Context context) {
        return getSharedPreferences(context).getString(USERNAME, "");
    }

    public static void setUserName(Context context, String username) {
        getEditor(context).putString(USERNAME, username).apply();
    }

    public static String getFirstName(Context context) {
        return getSharedPreferences(context).getString(FIRSTNAME, "");
    }

    public static void setFirstName(Context context, String username) {
        getEditor(context).putString(FIRSTNAME, username).apply();
    }

    public static String getLastName(Context context) {
        return getSharedPreferences(context).getString(LASTNAME, "");
    }

    public static void setLastName(Context context, String username) {
        getEditor(context).putString(LASTNAME, username).apply();
    }

    public static String getUserId(Context context) {
        return getSharedPreferences(context).getString(USERID, "");
    }

    public static void setUserSetupDone(Context context, boolean setUpDone) {
        getEditor(context).putBoolean(USER_SETUP, setUpDone).apply();
    }

    public static boolean isUserSetupDone(Context context) {
        return getSharedPreferences(context).getBoolean(USER_SETUP, false);
    }

}
