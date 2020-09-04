package com.compscieddy.striate;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.eddie_utils.etil.KeyboardEtil;
import com.google.common.base.Preconditions;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public abstract class FloatingWithInputBaseFragment extends DialogFragment {

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

  /** Used for showing/hiding keyboard. */
  public abstract @Nullable View getKeyboardFocusView();

  public abstract boolean shouldInterceptDismissAction();

  public abstract Runnable interceptDismissActionWithThisRunnable();

  private void attachListeners() {
    getBlackBackground().setOnClickListener(v -> dismissWithAnimation(true));
  }

  private void detachListeners() {
    getBlackBackground().setOnClickListener(null);
  }

  void dismissWithAnimation() {
    dismissWithAnimation(false);
  }

  /**
   * TODO: this doesn't catch the back button edge case (maybe that's a feature not a bug)
   */
  private void dismissWithAnimation(boolean isBlackBackgroundDismiss) {
    if (isBlackBackgroundDismiss && shouldInterceptDismissAction()) {
      interceptDismissActionWithThisRunnable().run();
      return;
    }

    if (getKeyboardFocusView() != null) {
      new Handler(Looper.getMainLooper()).postDelayed(() -> KeyboardEtil.hideKeyboard(getKeyboardFocusView()), 350);
    }

    getBlackBackground().animate()
        .alpha(0)
        .setDuration(700)
        .withEndAction(FloatingWithInputBaseFragment.this::dismiss);
    getMainDialogContainer().animate()
        .alpha(0)
        .setDuration(600)
        .setInterpolator(new FastOutSlowInInterpolator())
        .translationY(Etil.dpToPx(-30));
  }

  private void runEnteringAnimation() {
    getBlackBackground().setAlpha(0);
    getMainDialogContainer().setAlpha(0);

    getBlackBackground().animate()
        .alpha(1)
        .setDuration(500);
    getMainDialogContainer().animate()
        .alpha(1)
        .translationY(0)
        .setDuration(400);
  }
}
