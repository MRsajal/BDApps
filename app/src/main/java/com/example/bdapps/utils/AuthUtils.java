package com.example.bdapps.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthUtils {
    private static final String PREFS_NAME = "MyPrefs";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String USERNAME_KEY = "username";

    /**
     * Save access token and username to SharedPreferences
     */
    public static void saveAuthData(Context context, String accessToken, String username) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ACCESS_TOKEN_KEY, accessToken);
        editor.putString(USERNAME_KEY, username);
        editor.apply();
    }

    /**
     * Get access token from SharedPreferences
     */
    public static String getAccessToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(ACCESS_TOKEN_KEY, "");
    }

    /**
     * Get username from SharedPreferences
     */
    public static String getUsername(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(USERNAME_KEY, "");
    }

    /**
     * Check if user is authenticated (has access token)
     */
    public static boolean isAuthenticated(Context context) {
        return !getAccessToken(context).isEmpty();
    }

    /**
     * Clear all auth data (logout)
     */
    public static void clearAuthData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(ACCESS_TOKEN_KEY);
        editor.remove(USERNAME_KEY);
        editor.apply();
    }
}