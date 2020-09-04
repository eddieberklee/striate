package com.compscieddy.writeaday2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.compscieddy.writeaday2.util.CrashUtil;

import static android.app.NotificationManager.IMPORTANCE_HIGH;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.os.Build.VERSION.SDK_INT;

/**
 * Daily notifications that are on the user/account-level as opposed to a per-habit basis.
 */
public class DailyNotificationPublisher extends BroadcastReceiver {

  public static final String EXTRA_NOTIFICATION = "notification";
  public static final String EXTRA_NOTIFICATION_ID = "notification_id";

  @Override
  public void onReceive(Context context, Intent intent) {
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    if (notificationManager == null) {
      CrashUtil.log("Notification manager was returned null");
      return;
    }

    Notification notification = intent.getParcelableExtra(EXTRA_NOTIFICATION);

    if (SDK_INT >= Build.VERSION_CODES.O) {
      String channelId = notification.getChannelId();
      // I don't see why you need a channel id and channel name if you just make your channel id plaintext and not a number
      NotificationChannel notificationChannel = new NotificationChannel(channelId, channelId, IMPORTANCE_HIGH);
      notificationChannel.setDescription("Daily reminders for building good habits.");
      notificationManager.createNotificationChannel(notificationChannel);
    }

    int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1);
    if (notificationId == 1 && BuildConfig.DEBUG) {
      CrashUtil.logAndShowToast(String.format("Notification id shouldn't be 1"));
    }

    notificationManager.notify(notificationId, notification);
  }
}
