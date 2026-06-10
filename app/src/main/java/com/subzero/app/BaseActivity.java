package com.subzero.app;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.subzero.app.util.LocaleHelper;

/**
 * Base Activity that applies locale at context level.
 * All activities extend this to support real-time language switching.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** Switch language and recreate immediately — no restart needed */
    protected void switchLanguage(String lang) {
        LocaleHelper.setLanguage(this, lang);
        recreate();
    }
}
