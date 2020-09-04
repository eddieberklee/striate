package com.compscieddy.striate.photo;

import android.animation.ValueAnimator;
import android.view.View;

import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.striate.model.Entry;

import static com.google.android.material.animation.AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR;

public class PhotoAnimationUtil {

  public static void animateImagePulsing(Entry entry, View imageView) {
    if (!entry.getIsPhoto()) {
      return;
    }

    final float startScale = 1f;
    final float endScale = 1.2f;

    ValueAnimator valueAnimator = ValueAnimator.ofFloat(startScale, endScale);
    valueAnimator.addUpdateListener(animator -> {
      float amount = (float) animator.getAnimatedValue();
      imageView.setScaleX(amount);
      imageView.setScaleY(amount);

      int maxTranslation = Etil.dpToPx(10);
      int amountTranslation = (int) (Etil.mapValue(
          amount,
          startScale,
          endScale,
          0,
          1) * maxTranslation);
      imageView.setTranslationX(amountTranslation);
    });

    valueAnimator.setInterpolator(FAST_OUT_LINEAR_IN_INTERPOLATOR);
    valueAnimator.setDuration(8000);
    valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
    valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
    valueAnimator.start();
  }

}
