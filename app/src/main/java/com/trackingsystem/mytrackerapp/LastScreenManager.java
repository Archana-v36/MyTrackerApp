
package com.trackingsystem.mytrackerapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

public class LastScreenManager {

    private static final String PREF_NAME = "LastScreenPrefs";
    private static final String KEY_LAST_SCREEN = "lastScreen";


    // SAVE LAST SCREEN
    public static void save(Activity activity, String screenName) {

        SharedPreferences prefs =
                activity.getSharedPreferences(
                        PREF_NAME,
                        Activity.MODE_PRIVATE
                );

        prefs.edit()
                .putString(KEY_LAST_SCREEN, screenName)
                .apply();
    }


    // RESTORE LAST SCREEN (call ONLY in MainActivity & LoginActivity)
    public static boolean restoreIfExists(Activity activity) {

        SharedPreferences prefs =
                activity.getSharedPreferences(
                        PREF_NAME,
                        Activity.MODE_PRIVATE
                );

        String lastScreen =
                prefs.getString(KEY_LAST_SCREEN, null);

        if (lastScreen == null)
            return false;


        // prevent reopening same screen again
        if (activity.getClass()
                .getName()
                .equals(lastScreen))
            return false;


        try {

            Class<?> clazz =
                    Class.forName(lastScreen);

            Intent intent =
                    new Intent(activity, clazz);

            // IMPORTANT FIX
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            activity.startActivity(intent);

            activity.finish();

            return true;

        }
        catch (Exception e) {

            return false;
        }
    }


    // CLEAR LAST SCREEN (use when logout added later)
    public static void clear(Activity activity) {

        SharedPreferences prefs =
                activity.getSharedPreferences(
                        PREF_NAME,
                        Activity.MODE_PRIVATE
                );

        prefs.edit().clear().apply();
    }
}