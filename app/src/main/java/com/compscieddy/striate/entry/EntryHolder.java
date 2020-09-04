package com.compscieddy.striate.entry;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.bumptech.glide.Glide;
import com.compscieddy.eddie_utils.etil.ColorEtil;
import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.eddie_utils.etil.KeyboardEtil;
import com.compscieddy.eddie_utils.etil.VibrationEtil;
import com.compscieddy.striate.R;
import com.compscieddy.striate.SharedPrefKeys;
import com.compscieddy.striate.StriateApplication;
import com.compscieddy.striate.databinding.EntryItemBinding;
import com.compscieddy.striate.model.Entry;
import com.compscieddy.striate.photo.PhotoAnimationUtil;
import com.compscieddy.striate.photo.PhotoViewerFragment;

import java.util.List;

import androidx.annotation.ColorInt;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class EntryHolder extends RecyclerView.ViewHolder {

  private final Context c;
  private final Resources res;
  private final Handler mHandler;
  private FragmentManager mFragmentManager;

  private Entry mEntry;
  private List<Entry> mEntries;
  private EntryItemBinding binding;

  private NestedScrollView mEntryScrollView;
  private boolean mIsPrefilledTextChange;

  private Runnable mSaveToFirestoreRunnable = () -> {
    saveEntryToFirebase();
  };

  private TextWatcher mTextWatcher = new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
      // When we save an entry, we clear focus. This hides the cursor but doesn't actually hide
      // the keyboard. If a user types in this state, they can type but they see no cursor.
      // Requesting focus, will bring back that cursor while the user is typing.
      binding.entryAutocompleteView.requestFocus();

      if (hasEntryTextChanged(s.toString()) && !mIsPrefilledTextChange) {
        Timber.d("Entry text is new, so post delay saving in 5s");
        mHandler.removeCallbacks(mSaveToFirestoreRunnable);
        // Save the entry text every 3 seconds since the last text change
        mHandler.postDelayed(mSaveToFirestoreRunnable, 3000);
      }
    }
  };

  public EntryHolder(
      FragmentManager fragmentManager,
      NestedScrollView entryScrollView,
      EntryItemBinding binding) {
    super(binding.getRoot());
    mFragmentManager = fragmentManager;
    mEntryScrollView = entryScrollView;
    this.binding = binding;
    c = binding.getRoot().getContext();
    res = c.getResources();
    mHandler = new Handler(Looper.getMainLooper());

    initListeners();
    initImeOptions();
  }

  private static boolean isNewEntry_canOnlyCallOnce(Entry e) {
    String newEntryId =
        StriateApplication.getSharedPreferencesString(SharedPrefKeys.PREF_NEW_ENTRY_ID);
    boolean isNewEntry = TextUtils.equals(newEntryId, e.getId());

    if (isNewEntry) {
      // If we've detected a new entry, we want to focus on it and show the keyboard. We only
      // want to do this once, so we reset the value.
      StriateApplication.setSharedPreferencesString(SharedPrefKeys.PREF_NEW_ENTRY_ID, "");
    }

    return isNewEntry;
  }

  public static @ColorInt
  int getChameleonTextColor(@ColorInt int entryColor) {
    return ColorEtil.applySaturationValueMultiplier(
        entryColor,
        4.3f,
        0.6f);
  }

  private boolean hasEntryTextChanged(String s) {
    return !TextUtils.equals(s, mEntry.getEntryText());
  }

  private void initImeOptions() {
    binding.entryAutocompleteView.setRawInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
        | InputType.TYPE_CLASS_TEXT
        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
    binding.entryAutocompleteView.setImeOptions(EditorInfo.IME_ACTION_DONE);
  }

  private void initListeners() {
    binding.entryAutocompleteView.addTextChangedListener(mTextWatcher);
    binding.entryAutocompleteView.setOnEditorActionListener((v, actionId, event) -> {
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        VibrationEtil.vibrate(binding.entryAutocompleteView);

        mHandler.removeCallbacks(mSaveToFirestoreRunnable);
        if (TextUtils.isEmpty(binding.entryAutocompleteView.getText())) {
          mEntry.deleteOnFirebase();
        } else {
          saveEntryToFirebase();
        }

        EntryManager.updateEntryHeight(getEntry(), itemView);

        binding.entryAutocompleteView.clearFocus();
        KeyboardEtil.hideKeyboard(binding.entryAutocompleteView);
        return true;
      }
      return false;
    });
    binding.entryAutocompleteView.setOnFocusChangeListener((v, hasFocus) -> {
      binding
          .entryActionIconsContainer
          .setVisibility(hasFocus ? VISIBLE : GONE);
    });
    binding.entryDeleteButton.setOnClickListener(v -> {
      Etil.showToast(c, res.getString(R.string.instructions_long_press_to_delete), Gravity.CENTER);
    });
    binding.entryDeleteButton.setOnLongClickListener(v -> {
      VibrationEtil.vibrate(binding.entryDeleteButton);

      mEntry.deleteOnFirebase();
      return true;
    });
    binding.entryPhotoImageView.setOnClickListener(v -> {
      PhotoViewerFragment photoViewerFragment = PhotoViewerFragment.newInstance(
          mEntries,
          mEntry);
      photoViewerFragment.show(mFragmentManager, PhotoViewerFragment.TAG);
    });
    binding.entryPhotoImageView.setOnLongClickListener(v -> {
      // todo: show entry action container to be able to delete photos
      return false;
    });
    binding.entryAutocompleteView.postDelayed(() -> {
      boolean isThereEnoughTextToScroll =
          binding.entryAutocompleteView.getLineCount() > 1
              && binding.entryAutocompleteView.getLineCount()
              * binding.entryAutocompleteView.getLineHeight() > binding.entryAutocompleteView.getHeight();
      if (isThereEnoughTextToScroll) {
        ViewGroup.LayoutParams autocompleteParams = binding.entryAutocompleteView.getLayoutParams();
        autocompleteParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        binding.entryAutocompleteView.setLayoutParams(autocompleteParams);

        ViewGroup autocompleteContainerParent =
            (ViewGroup) binding.entryAutocompleteViewContainer.getParent();
        autocompleteContainerParent.removeView(binding.entryAutocompleteViewContainer);

        NestedScrollView nestedScrollView = new NestedScrollView(c);
        nestedScrollView.addView(binding.entryAutocompleteViewContainer);
        autocompleteContainerParent.addView(
            nestedScrollView,
            new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
      }
    }, 800);
  }

  public Entry getEntry() {
    return mEntry;
  }

  public void setEntry(List<Entry> entries, Entry entry) {
    mEntries = entries;

    boolean isEntryDifferent = mEntry == null || !TextUtils.equals(mEntry.getId(), mEntry.getId());
    // check if instance variable value shows that we're trying to update the same entry, before
    // initializing
    mEntry = entry;

    binding.getRoot().setBackgroundColor(mEntry.getColor());

    initCommonLayout();
    if (mEntry.getIsPhoto()) {
      initEntryPhoto();
      hideEntryText();
      return;
    } else { // is a text entry
      hideEntryPhoto();
      updateTextAndButtonColors(mEntry.getColor());
      if (isEntryDifferent) {
        prefillTextAndCursorLocation();
      }
      if (isNewEntry_canOnlyCallOnce(mEntry)) {
        scrollAndShowKeyboardForNewEntry();
      }
    }
  }

  private void initCommonLayout() {
    binding.entryPhotoCaptionTextContainer.setVisibility(mEntry.getIsPhoto() ? VISIBLE : GONE);
  }

  private void hideEntryPhoto() {
    binding.entryPhotoImageView.setVisibility(GONE);
  }

  private void hideEntryText() {
    binding.entryAutocompleteView.setVisibility(GONE);
  }

  private void initEntryPhoto() {
    binding.entryPhotoImageView.postDelayed(() -> {
      // lol I need this .post() to allow the layout to get the correct size to layout (weights
      // are used).

      Glide.with(c)
          .load(mEntry.getUriString())
          .placeholder(R.drawable.photo_placeholder)
          .into(binding.entryPhotoImageView);

      PhotoAnimationUtil.animateImagePulsing(mEntry, binding.entryPhotoImageView);
    }, 200);

    binding.entryPhotoCaptionText.setText(mEntry.getPhotoCaptionText());
  }

  public void updateTextAndButtonColors(@ColorInt int entryColor) {
// For calibrating each day's color with careful precision (not actually necessary imo):
//    int dayOfWeekIndex = DateUtil.getDayOfWeekIndexFromYearMonthDay(mEntry.getYearMonthDay());
//
//    float saturationMultiplier = 1;
//    float valueMultiplier = 1;
//    switch (dayOfWeekIndex) {
//      case 0: // starts with monday
//        saturationMultiplier = 4.3f;
//        valueMultiplier = 0.6f;
//        break;
//      case 1:
//        saturationMultiplier = 4.4f;
//        valueMultiplier = 0.6f;
//        break;
//      case 2:
//        saturationMultiplier = 4.6f;
//        valueMultiplier = 0.6f;
//        break;
//      case 3:
//        saturationMultiplier = 4.3f;
//        valueMultiplier = 0.6f;
//        break;
//      case 4:
//        saturationMultiplier = 4.3f;
//        valueMultiplier = 0.6f;
//        break;
//      case 5:
//        saturationMultiplier = 4.3f;
//        valueMultiplier = 0.6f;
//        break;
//      case 6:
//        saturationMultiplier = 4.3f;
//        valueMultiplier = 0.6f;
//        break;
//    }

    int chameleonColor = getChameleonTextColor(entryColor);
    binding.entryAutocompleteView.setTextColor(chameleonColor);

    binding.entryAutocompleteView.setHintTextColor(ColorEtil.applyAlpha(chameleonColor, 0.4f));

    binding.entryActionMoodButton.setCustomColor(chameleonColor);
    binding.entryActionJournalPromptsButton.setCustomColor(chameleonColor);
    binding.entryActionHashtagsButton.setCustomColor(chameleonColor);
    binding.entryDeleteButton.setCustomColor(chameleonColor);
  }

  private void prefillTextAndCursorLocation() {
    mIsPrefilledTextChange = true;
    binding.entryAutocompleteView.setText(mEntry.getEntryText());
    mIsPrefilledTextChange = false;

    binding.entryAutocompleteView.postDelayed(() -> {
      binding.entryAutocompleteView.scrollTo(0, 0);
    }, 500);

    binding.entryAutocompleteView.setSelection(mEntry.getEntryText().length());
  }

  private void scrollAndShowKeyboardForNewEntry() {
    scrollToNewEntry();
    showKeyboardNewEntryAfterDelay();
  }

  private void scrollToNewEntry() {
    // lol i hate this but we need a delay so that the new entry can be laid out, and then we
    // scroll to the end
    mEntryScrollView.postDelayed(() -> {
      mEntryScrollView.smoothScrollTo(0, 99999);
    }, 400);
  }

  private void showKeyboardNewEntryAfterDelay() {
    mHandler.postDelayed(() -> {
      binding.entryAutocompleteView.requestFocus();
      KeyboardEtil.showKeyboard(c);
    }, 300);
  }

  private void saveEntryToFirebase() {
    String entryText = binding.entryAutocompleteView.getText().toString();
    if (hasEntryTextChanged(entryText)) {
      Timber.d("Saving entry (" + entryText + ")");
      mEntry.setEntryText(entryText);
      mEntry.saveOnFirestore();
    }

    clearFocus();
  }

  public void clearFocus() {
    binding.entryAutocompleteView.clearFocus();
  }
}
