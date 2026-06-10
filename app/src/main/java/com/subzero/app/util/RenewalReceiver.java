package com.subzero.app.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RenewalReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper.checkAndNotify(context);
    }
}
