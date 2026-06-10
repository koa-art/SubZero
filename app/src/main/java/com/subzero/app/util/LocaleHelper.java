package com.subzero.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREF_NAME = "subzero_locale";
    private static final String KEY_LANG = "language";

    public static final String LANG_AUTO = "auto";
    public static final String LANG_ZH = "zh";
    public static final String LANG_EN = "en";

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANG, LANG_AUTO);
    }

    public static void setLanguage(Context context, String lang) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANG, lang).apply();
    }

    public static Context applyLanguage(Activity activity) {
        String lang = getLanguage(activity);
        Locale locale;
        if (LANG_ZH.equals(lang)) {
            locale = new Locale("zh", "CN");
        } else if (LANG_EN.equals(lang)) {
            locale = Locale.ENGLISH;
        } else {
            // Auto: use system default
            return activity;
        }

        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        return activity;
    }

    public static String getLanguageDisplayName(Context context) {
        String lang = getLanguage(context);
        switch (lang) {
            case LANG_ZH: return "中文";
            case LANG_EN: return "English";
            default: return "跟随系统";
        }
    }
}
