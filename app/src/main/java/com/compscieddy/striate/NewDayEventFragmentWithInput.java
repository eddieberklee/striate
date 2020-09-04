package com.compscieddy.striate;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import com.compscieddy.eddie_utils.etil.KeyboardEtil;
import com.compscieddy.eddie_utils.etil.VibrationEtil;
import com.google.common.base.Preconditions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

@SuppressWarnings("WeakerAccess")
public class NewDayEventFragmentWithInput extends FloatingWithInputBaseFragment {

  public static final String TAG = NewDayEventFragmentWithInput.class.getSimpleName();

  public static final String KEY_STARTED_AT_MILLIS = "started_at_millis";

  private Context mContext;

  private View mRootView;
  private View mBlackBackground;
  private View mMainDialogContainer;
  private AutoCompleteTextView mNewDayInputView;
  private long mStartedAtMillis;

  public static NewDayEventFragmentWithInput newInstance(long startedAtMillis) {
    Bundle args = new Bundle();
    args.putLong(KEY_STARTED_AT_MILLIS, startedAtMillis);
    NewDayEventFragmentWithInput fragment = new NewDayEventFragmentWithInput();
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    mRootView = inflater.inflate(R.layout.new_day_event_fragment, container, false);
    /* ButterKnife does not appear to work, debugging was proving to be a waste of time. */
    mContext = inflater.getContext();
    Bundle args = Preconditions.checkNotNull(getArguments());
    mStartedAtMillis = args.getLong(KEY_STARTED_AT_MILLIS);
    initViews();
    initNewDayInputView();
    focusAndShowKeyboard();
    return mRootView;
  }

  private void focusAndShowKeyboard() {
    mNewDayInputView.post(() -> {
      mNewDayInputView.requestFocus();
      Preconditions.checkNotNull(getContext());
      KeyboardEtil.showKeyboard(getContext());
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    attachListeners();
    Preconditions.checkNotNull(getActivity());
  }

  @Override
  public void onPause() {
    super.onPause();
    detachListeners();
    Preconditions.checkNotNull(getActivity());
  }

  @Override
  public View getBlackBackground() {
    return mBlackBackground;
  }

  @Override
  public View getMainDialogContainer() {
    return mMainDialogContainer;
  }

  @Override
  public View getKeyboardFocusView() {
    return mNewDayInputView;
  }

  @Override
  public boolean shouldInterceptDismissAction() {
    return mNewDayInputView.getText().length() != 0;
  }

  @Override
  public Runnable interceptDismissActionWithThisRunnable() {
    return () -> {
      AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog)
          .setMessage(R.string.confirmation_dialog_description)
          .setPositiveButton(R.string.confirmation_dialog_yes_button, (dialog, which) -> {
            // Dismiss the new day event fragment and lose any text that was written
            mNewDayInputView.setText("");
            dismissWithAnimation();
          })
          .setNegativeButton(R.string.confirmation_dialog_cancel_button, (dialog, which) -> {
            // Dismiss the confirmation dialog so the user can keep writing
            dialog.dismiss();
          });
      confirmDialogBuilder.create().show();
    };
  }

  private void initViews() {
    mBlackBackground = mRootView.findViewById(R.id.black_background);
    mMainDialogContainer = mRootView.findViewById(R.id.main_dialog_container);
    mNewDayInputView = mRootView.findViewById(R.id.new_day_edit_text);
  }

  private void initNewDayInputView() {
    /*
     * Very weird you need to set this in code vs xml in order to get the done button on a multiline edit text.
     * Source: https://stackoverflow.com/a/41022589
     */
    mNewDayInputView.setRawInputType(TYPE_CLASS_TEXT
        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
  }

  private void attachListeners() {
    mNewDayInputView.setOnEditorActionListener((v, actionId, event) -> {
      if (actionId == IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
        if (TextUtils.isEmpty(mNewDayInputView.getText())) {
          return true;
        }

        Preconditions.checkArgument("you need to call a method for the actual saving logic here".equals("this will error if you use without deleting this line"));

        dismissWithAnimation();
        VibrationEtil.vibrate(mContext, 2);

        // Getting this weird bug where pressing enter on the keyboard with emulator was creating
        // double entries.
        mNewDayInputView.setOnEditorActionListener(null);

        return true;
      }
      return false;
    });
  }

  private void detachListeners() {
    mNewDayInputView.setOnEditorActionListener(null);
  }
}
