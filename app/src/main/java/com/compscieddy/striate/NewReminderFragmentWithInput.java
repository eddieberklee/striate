package com.compscieddy.striate;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.compscieddy.eddie_utils.etil.VibrationEtil;
import com.compscieddy.striate.databinding.NewReminderFragmentBinding;
import com.google.common.base.Preconditions;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.compscieddy.striate.Analytics.NEW_HABIT_SAVE_BUTTON;

public class NewReminderFragmentWithInput extends FloatingWithInputBaseFragment {

  public static final String TAG = NewReminderFragmentWithInput.class.getSimpleName();
  private Context c;
  private Resources res;
  private NewReminderFragmentBinding binding;

  public static NewReminderFragmentWithInput newInstance() {
    return new NewReminderFragmentWithInput();
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    binding = NewReminderFragmentBinding.inflate(inflater, container, false);
    /* ButterKnife does not appear to work, debugging was proving to be a waste of time. */
    c = Preconditions.checkNotNull(inflater.getContext());
    res = c.getResources();
    init();
    return binding.getRoot();
  }

  @Override
  public View getBlackBackground() {
    return binding.blackBackground;
  }

  @Override
  public View getMainDialogContainer() {
    return binding.mainDialogContainer;
  }

  @Override
  public View getKeyboardFocusView() {
    return null;
  }

  @Override
  public boolean shouldInterceptDismissAction() {
    return false;
  }

  @Override
  public Runnable interceptDismissActionWithThisRunnable() {
    return null;
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

  private void init() {
    initSubmitButton();
  }

  @SuppressWarnings("CodeBlock2Expr")
  private void attachListeners() {
    binding.newHabitSubmitButton.setOnClickListener(v -> {
      handleNewReminderSetButtonClick();
    });
  }

  private void handleNewReminderSetButtonClick() {
    // handle reminder being created

    dismissWithAnimation();

    VibrationEtil.vibrate(c, 2);

    Analytics.track(NEW_HABIT_SAVE_BUTTON);
  }

  private void detachListeners() {
    binding.newHabitSubmitButton.setOnClickListener(null);
    binding.blackBackground.setOnClickListener(null);
  }

  private void initSubmitButton() {
    setSubmitButtonBackground(res.getColor(R.color.black));
  }

  private void setSubmitButtonBackground(@ColorInt int color) {
    binding.newHabitSubmitButton.setBackgroundColor(color);
  }
}
