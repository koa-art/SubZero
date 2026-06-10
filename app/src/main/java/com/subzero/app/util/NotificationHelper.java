package com.subzero.app.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.subzero.app.R;
import com.subzero.app.db.StorageManager;
import com.subzero.app.model.Subscription;

import java.util.List;

public class NotificationHelper {
    private static final String CHANNEL_ID = "subzero_renewal";
    private static final int NOTIFY_ID = 5001;

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.notification_channel_desc));
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
                sb.append(s.getName())
                        .append(" (").append(formatCurrency(store, s.getMonthlyAmount()))
                        .append("/mo)\n");
            }
            showNotification(context,
                    context.getString(R.string.notification_title),
                    sb.toString().trim());
        }
    }

    private static String formatCurrency(StorageManager store, double amount) {
        String currency = store.getSetting("currency");
        String symbol = "¥";
        if (currency.equals("USD")) symbol = "$";
        else if (currency.equals("EUR")) symbol = "€";
        else if (currency.equals("JPY")) symbol = "¥";
        return symbol + String.format(java.util.Locale.getDefault(), "%.0f", amount);
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
