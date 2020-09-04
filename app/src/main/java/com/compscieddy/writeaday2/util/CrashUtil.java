package com.compscieddy.writeaday2.util;

import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.writeaday2.BuildConfig;
import com.compscieddy.writeaday2.Writeaday2Application;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import timber.log.Timber;

public class CrashUtil {

  /**
   * Most basic logging to logcat and to Crashlytics.
   */
  public static void log(String errorMessage) {
    Timber.e(errorMessage);
    FirebaseCrashlytics.getInstance().log(errorMessage);
  }

  /**
   * Does everything {@link CrashUtil#log(String)} does, but also shows a toast to DEBUG users.
   */
  public static void logAndShowToast(String errorMessage) {
    log(errorMessage);
    if (BuildConfig.DEBUG) {
      Etil.showToast(Writeaday2Application.sApplicationContext, errorMessage);
    }
  }

  public static boolean didHandleFirestoreException(Exception e) {
    if (e != null) {
      CrashUtil.logAndShowToast(e.getMessage());
      e.printStackTrace();
      return true;
    }
    return false;
  }
}
