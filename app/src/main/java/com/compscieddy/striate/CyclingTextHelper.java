package com.compscieddy.striate;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

public class CyclingTextHelper {

  private final Handler mHandler;
  private Runnable mCycleRunnable;

  public CyclingTextHelper() {
    mHandler = new Handler(Looper.getMainLooper());
  }

  public void cycle(TextView textView, String[] stringsToCycle, int intervalMillis) {
    mCycleRunnable = () -> {
      getCycleRunnable(textView, stringsToCycle).run();
      mHandler.postDelayed(mCycleRunnable, intervalMillis);
    };
    mHandler.post(mCycleRunnable);
  }

  private Runnable getCycleRunnable(TextView textView, String[] stringsToCycle) {
    return () -> {
      int randomInt = (int) (Math.random() * (stringsToCycle.length - 1));
      String randomString = stringsToCycle[randomInt];
      textView.animate()
          .alpha(0)
          .withEndAction(() -> {
            textView.setText(randomString);
            textView.animate()
                .alpha(1);
          });
    };
  }

}
