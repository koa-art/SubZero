package com.subzero.app.util;

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
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_LANG, lang).apply();
    }

    /** Called from BaseActivity.attachBaseContext — applies locale at context level for immediate effect */
    public static Context wrapContext(Context context) {
        String lang = getLanguage(context);
        if (LANG_AUTO.equals(lang)) return context;

        Locale locale = LANG_ZH.equals(lang)
                ? new Locale("zh", "CN")
                : Locale.ENGLISH;

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
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
