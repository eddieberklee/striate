package com.compscieddy.infinote;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.compscieddy.infinote.util.CrashUtil;

import timber.log.Timber;

import static android.app.NotificationManager.IMPORTANCE_HIGH;
import static android.content.Context.NOTIFICATION_SERVICE;

public class HabitNotificationPublisher extends BroadcastReceiver {

  public static final String EXTRA_NOTIFICATION = "notification";
  public static final String EXTRA_NOTIFICATION_ID = "notification_id";
  public static final String EXTRA_NOTIFICATION_CHANNEL_ID = "notification_channel_name";
  public static final String EXTRA_NOTIFICATION_HABIT_NAME = "notification_habit_name";

  @Override
  public void onReceive(Context context, Intent intent) {
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    if (notificationManager == null) {
      CrashUtil.log("Notification manager was returned null");
      return;
    }

    Notification notification = intent.getParcelableExtra(EXTRA_NOTIFICATION);
    String habitName = intent.getStringExtra(EXTRA_NOTIFICATION_HABIT_NAME);
    int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1);
    if (notificationId == 1 && BuildConfig.DEBUG) {
      CrashUtil.logAndShowToast(String.format("Notification id shouldn't be 1 for habit: %s", habitName));
    }

    Timber.d("Received notification request for %s", habitName);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      String channelId = intent.getStringExtra(EXTRA_NOTIFICATION_CHANNEL_ID);
      // I don't see why you need a channel id and channel name if you just make your channel id plaintext and not a number
      NotificationChannel notificationChannel = new NotificationChannel(channelId, channelId, IMPORTANCE_HIGH);
      notificationChannel.setDescription("Reminders for your habits");
      notificationManager.createNotificationChannel(notificationChannel);
    }

    notificationManager.notify(notificationId, notification);
  }

}
