package com.subzero.app.util;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.subzero.app.db.StorageManager;
import com.subzero.app.model.Subscription;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NotificationHelper {
    private static final String CHANNEL_ID = "subzero_renewal";
    private static final int NOTIFY_ID = 5001;

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "续费提醒", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("订阅续费到期提醒");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    public static void checkAndNotify(Context context) {
        StorageManager store = StorageManager.getInstance(context);
        int reminderDays = Integer.parseInt(store.getSetting("reminder_days"));
        List<Subscription> upcoming = store.getUpcomingRenewals(reminderDays);

        if (!upcoming.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Subscription s : upcoming) {
                sb.append(s.getName()).append(" (")
                        .append(formatCurrency(store, s.getMonthlyAmount()))
                        .append("/月)\n");
            }
            showNotification(context, "即将续费的订阅", sb.toString().trim());
        }
    }

    private static String formatCurrency(StorageManager store, double amount) {
        String currency = store.getSetting("currency");
        String symbol = currency.equals("USD") ? "$" : currency.equals("EUR") ? "€"
                : currency.equals("JPY") ? "¥" : "¥";
        return symbol + String.format(Locale.getDefault(), "%.0f", amount);
    }

    private static void showNotification(Context context, String title, String message) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        if (manager != null) manager.notify(NOTIFY_ID, builder.build());
    }
}
