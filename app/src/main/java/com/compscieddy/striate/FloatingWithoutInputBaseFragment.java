package com.compscieddy.striate;

import android.os.Bundle;
import android.view.View;

import com.compscieddy.eddie_utils.etil.Etil;
import com.google.common.base.Preconditions;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public abstract class FloatingWithoutInputBaseFragment extends DialogFragment {

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, R.style.FloatingFullscreenDialogTheme);
  }

  @Override
  public void onStart() {
    super.onStart();
    Preconditions.checkNotNull(Preconditions.checkNotNull(
        getDialog()).getWindow()).setLayout(MATCH_PARENT, MATCH_PARENT);
    runEnteringAnimation();
  }

  @Override
  public void onResume() {
    super.onResume();
    attachListeners();
  }

  @Override
  public void onPause() {
    super.onPause();
    detachListeners();
  }

  /** Used for enter and exit animation of black background and dialog container. */
  public abstract View getBlackBackground();

  /** Used for enter and exit animation of black background and dialog container. */
  public abstract View getMainDialogContainer();

  private void attachListeners() {
    getBlackBackground().setOnClickListener(v -> dismissWithAnimation());
  }

  private void detachListeners() {
    getBlackBackground().setOnClickListener(null);
  }

  /**
   * TODO: this doesn't catch the back button edge case (maybe that's a feature not a bug)
   */
  private void dismissWithAnimation() {
    getBlackBackground().animate()
        .alpha(0)
        .setDuration(450)
        .withEndAction(FloatingWithoutInputBaseFragment.this::dismiss);
    getMainDialogContainer().animate()
        .alpha(0)
        .setDuration(370)
        .setInterpolator(new FastOutSlowInInterpolator())
        .translationY(Etil.dpToPx(10));
  }

  private void runEnteringAnimation() {
    getBlackBackground().setAlpha(0);
    getMainDialogContainer().setAlpha(0);
    getMainDialogContainer().setTranslationY(Etil.dpToPx(10));

    getBlackBackground().animate()
        .alpha(1)
        .setDuration(300);
    getMainDialogContainer().animate()
        .alpha(1)
        .translationY(0)
        .setDuration(220);
  }
}
