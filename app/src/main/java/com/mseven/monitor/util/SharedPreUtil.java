package com.mseven.monitor.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by mseven on 3/24/17.
 */

public class SharedPreUtil {


    private static SharedPreferences sConfigSp;
    public static final String SHARED_PREFERENCES_KEY = "com.mseven.monitor";
    private static Context sContext;

    private static final class Holder {
        private static final SharedPreUtil INSTANCE = new SharedPreUtil();
    }

    public static SharedPreUtil getInstance(Context context) {
        sContext = context.getApplicationContext();
        return Holder.INSTANCE;
    }

    public static SharedPreferences getSharedPreferences() {
        if (sConfigSp == null) {
            sConfigSp = sContext.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        }
        return sConfigSp;
    }


    public void putString(String key, String value) {
        getSharedPreferences().edit().putString(key, value).apply();
    }

    public String getString(String key, String def) {
        return getSharedPreferences().getString(key, def);
    }

    public void putBoolean(String key, boolean value) {
        getSharedPreferences().edit().putBoolean(key, value).apply();
    }

    public Boolean getBoolean(String key, boolean def) {
        return getSharedPreferences().getBoolean(key, def);
    }

}
