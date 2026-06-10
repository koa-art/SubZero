package com.subzero.app.util;

import android.content.Context;

/**
 * Provides localized display names for category keys and cycle keys.
 * Always reads from current resources, so language switch works immediately.
 */
public class DisplayHelper {

    public static String getCategoryName(Context ctx, String key) {
        if (key == null) return ctx.getString(R.string.cat_other);
        switch (key) {
            case "entertainment": return ctx.getString(R.string.cat_entertainment);
            case "productivity": return ctx.getString(R.string.cat_productivity);
            case "health": return ctx.getString(R.string.cat_health);
            case "shopping": return ctx.getString(R.string.cat_shopping);
            case "food": return ctx.getString(R.string.cat_food);
            case "education": return ctx.getString(R.string.cat_education);
            default: return ctx.getString(R.string.cat_other);
        }
    }

    public static String getCycleName(Context ctx, String key) {
        if (key == null) return ctx.getString(R.string.cycle_monthly);
        switch (key) {
            case "weekly": return ctx.getString(R.string.cycle_weekly);
            case "quarterly": return ctx.getString(R.string.cycle_quarterly);
            case "yearly": return ctx.getString(R.string.cycle_yearly);
            default: return ctx.getString(R.string.cycle_monthly);
        }
    }

    public static String[] getCategoryKeys() {
        return new String[]{"entertainment", "productivity", "health", "shopping", "food", "education", "other"};
    }

    public static String[] getCycleKeys() {
        return new String[]{"monthly", "yearly", "weekly", "quarterly"};
    }

    // Prevent instantiation
    private DisplayHelper() {}
}
