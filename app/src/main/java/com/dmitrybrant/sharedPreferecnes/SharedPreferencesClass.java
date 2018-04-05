package com.dmitrybrant.sharedPreferecnes;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by dharamveer on 5/4/18.
 */

public class SharedPreferencesClass {


    static SharedPreferences sharedPreferences;

    static Context context;
    private static String session_key;

    public SharedPreferencesClass(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("3dKey", Context.MODE_PRIVATE);

    }

    public static String getSession_key() {
//        session_key = sharedPreferences.getString("session_key",session_key);

        return session_key;
    }

    public static void setSession_key(String session_key) {
        session_key = session_key;
        sharedPreferences.edit().putString("session_key",session_key).commit();

    }
}
