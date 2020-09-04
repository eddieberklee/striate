package com.compscieddy.writeaday2;

import com.amplitude.api.Amplitude;
import com.google.firebase.analytics.FirebaseAnalytics;

import timber.log.Timber;

@SuppressWarnings("WeakerAccess")
public class Analytics {

  public static final String AUTHENTICATION_SCREEN = "authentication_screen";
  public static final String AUTHENTICATION_BUTTON = "authentication_button";
  public static final String MAIN_ACTIVITY_OPEN = "main_activity_open";
  public static final String NEW_HABIT_DIALOG_OPEN = "new_habit_dialog_open";
  public static final String NEW_HABIT_SAVE_BUTTON = "new_habit_save_button";
  public static final String NEW_HABIT_REMINDER = "new_habit_reminder";
  public static final String DAY_OF_WEEK_CLICKED = "day_of_week_clicked";

  public static void track(String event) {
    Timber.d("Logging event: " + event);
    FirebaseAnalytics firebaseAnalytics =
        FirebaseAnalytics.getInstance(Writeaday2Application.sApplicationContext);
    firebaseAnalytics.logEvent(event, null);
    Amplitude.getInstance().logEvent(event);
  }

}
