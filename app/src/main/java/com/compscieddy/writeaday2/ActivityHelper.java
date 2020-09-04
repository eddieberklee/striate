package com.compscieddy.writeaday2;

import android.app.Activity;
import android.content.Intent;

public class ActivityHelper {

  public static void launchActivityAndFinish(Activity fromActivity, Class toActivity) {
    Intent intent = new Intent(fromActivity, toActivity);
    fromActivity.startActivity(intent);
    fromActivity.finish();
  }

}
