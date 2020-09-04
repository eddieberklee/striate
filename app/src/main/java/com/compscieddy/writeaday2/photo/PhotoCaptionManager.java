package com.compscieddy.writeaday2.photo;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.compscieddy.eddie_utils.etil.KeyboardEtil;
import com.compscieddy.eddie_utils.etil.VibrationEtil;
import com.compscieddy.writeaday2.databinding.PhotoViewerFragmentBinding;
import com.compscieddy.writeaday2.hashtag.HashtagHelper;
import com.compscieddy.writeaday2.model.Entry;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

public class PhotoCaptionManager {

  private final Context c;
  private final Resources res;
  private Handler mHandler;
  private PhotoViewerFragmentBinding binding;
  private PhotoViewerFragment.CurrentEntryCallback mCurrentEntryCallback;
  private boolean mIsPrefilledTextChange;
  private HashtagAllPhotosRecyclerAdapter mHashtagAllPhotosRecyclerAdapter;

  private Runnable mSaveToFirestoreRunnable = () -> {
    savePhotoCaptionToFirebase();
  };

  TextView.OnEditorActionListener mOnEditorActionListener = (v, actionId, event) -> {
    if (actionId == EditorInfo.IME_ACTION_DONE) {
      VibrationEtil.vibrate(binding.photoCaption);

      mHandler.removeCallbacks(mSaveToFirestoreRunnable);
      if (TextUtils.isEmpty(binding.photoCaption.getText())) {
        mCurrentEntryCallback.getCurrentEntry().deleteOnFirebase();
      } else {
        savePhotoCaptionToFirebase();
      }

      binding.photoCaption.clearFocus();
      KeyboardEtil.hideKeyboard(binding.photoCaption);
      return true;
    }
    return false;
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
      // The entry text will not be new in the case of text being pre-filled
      boolean isEntryTextNew = !TextUtils.equals(
          s,
          mCurrentEntryCallback.getCurrentEntry().getEntryText());

      if (isEntryTextNew && !mIsPrefilledTextChange) {
        Timber.d("Entry text is new, so post delay saving in 5s");
        mHandler.removeCallbacks(mSaveToFirestoreRunnable);
        // Save the entry text every 3 seconds since the last text change
        mHandler.postDelayed(mSaveToFirestoreRunnable, 3000);
      }
    }
  };

  public PhotoCaptionManager(
      PhotoViewerFragmentBinding binding,
      PhotoViewerFragment.CurrentEntryCallback currentEntryCallback) {
    this.binding = binding;
    c = binding.getRoot().getContext();
    res = c.getResources();
    mCurrentEntryCallback = currentEntryCallback;
    mHandler = new Handler(Looper.getMainLooper());
  }

  public void initPhotoCaption() {
    Entry entry = mCurrentEntryCallback.getCurrentEntry();

    prefillPhotoCaptionText(entry);
    updatePhotoHashtagsSection(HashtagHelper.parseHashtags(entry.getPhotoCaptionText()));
    initPhotoCaptionImeOptions();
    initPhotoCaptionListeners();
  }

  private void prefillPhotoCaptionText(Entry entry) {
    mIsPrefilledTextChange = true;
    binding.photoCaption.setText(entry.getPhotoCaptionText());
    mIsPrefilledTextChange = false;
  }

  private void initPhotoCaptionListeners() {
    binding.photoCaption.addTextChangedListener(mTextWatcher);
    binding.photoCaption.setOnEditorActionListener(mOnEditorActionListener);
  }

  private void savePhotoCaptionToFirebase() {
    Entry entry = mCurrentEntryCallback.getCurrentEntry();
    String photoCaptionText = binding.photoCaption.getText().toString();
    List<String> hashtagNames = HashtagHelper.parseHashtags(photoCaptionText);

    entry.setPhotoCaptionText(photoCaptionText);
    entry.setHashtags(hashtagNames);

    updatePhotoHashtagsSection(hashtagNames);

    entry.saveOnFirestore();
  }

  private void updatePhotoHashtagsSection(List<String> hashtagNames) {
    if (mHashtagAllPhotosRecyclerAdapter == null) {
      mHashtagAllPhotosRecyclerAdapter = new HashtagAllPhotosRecyclerAdapter(hashtagNames);
    } else {
      mHashtagAllPhotosRecyclerAdapter.updateHashtagNames(hashtagNames);
    }
    binding.hashtagAllPhotosRecyclerView.setLayoutManager(new LinearLayoutManager(
        c,
        RecyclerView.VERTICAL,
        false));
    binding.hashtagAllPhotosRecyclerView.setAdapter(mHashtagAllPhotosRecyclerAdapter);
  }

  private void initPhotoCaptionImeOptions() {
    binding.photoCaption.setRawInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
        | InputType.TYPE_CLASS_TEXT
        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
    binding.photoCaption.setImeOptions(EditorInfo.IME_ACTION_DONE);
  }
}
