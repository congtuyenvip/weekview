package com.tuyennc.weekview;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Le Cong Tuyen on 8/19/2015.
 */
public class AppLogger {
    private final static String APP_LOG_TAG = "HomesPro";

    public static void logDebug(String TAG, String msg) {
        if (isDebug())
            Log.d(TAG, msg);
    }

    public static void logDebug(String msg) {
        if (isDebug())
            Log.d(APP_LOG_TAG, msg);
    }

    public static void logVerbose(String msg) {
        if (isDebug())
            Log.v(APP_LOG_TAG, msg);
    }

    public static void logInfo(String msg) {
        if (isDebug())
            Log.i(APP_LOG_TAG, msg);
    }

    public static void logInfo(String TAG, String msg) {
        if (isDebug())
            Log.i(TAG, msg);
    }

    public static void logWarn(String msg) {
        if (isDebug())
            Log.w(APP_LOG_TAG, msg);
    }

    public static void logExcep(Exception ex) {
        if (isDebug()) {
            Log.e(APP_LOG_TAG, ex.getMessage() + " - " + ex.getMessage());
//            ex.printStackTrace();
        }
    }

    public static void logExcep(String ex) {
        if (isDebug())
            Log.e(APP_LOG_TAG, ex);
    }

    public static void debugIntent(Intent intent) {
        if (isDebug()) {
            Log.v(APP_LOG_TAG, "action: " + intent.getAction());
            Log.v(APP_LOG_TAG, "component: " + intent.getComponent());
            Bundle extras = intent.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Log.v(APP_LOG_TAG, "key [" + key + "]: " + extras.get(key));
                }
            } else {
                Log.v(APP_LOG_TAG, "no extras");
            }
        }
    }

    public static boolean isDebug() {
        return true;
    }
}
