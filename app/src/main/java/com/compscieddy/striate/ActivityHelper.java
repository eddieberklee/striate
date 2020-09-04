package com.compscieddy.striate;

import android.app.Activity;
import android.content.Intent;

public class ActivityHelper {

  public static void launchActivityAndFinish(Activity fromActivity, Class toActivity) {
    Intent intent = new Intent(fromActivity, toActivity);
    fromActivity.startActivity(intent);
    fromActivity.finish();
  }

}
