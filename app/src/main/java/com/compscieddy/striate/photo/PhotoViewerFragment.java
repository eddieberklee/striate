package com.compscieddy.striate.photo;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.eddie_utils.etil.ScreenEtil;
import com.compscieddy.eddie_utils.etil.SharingEtil;
import com.compscieddy.eddie_utils.etil.VibrationEtil;
import com.compscieddy.eddie_utils.etil.ViewEtil;
import com.compscieddy.eddie_utils.eui.FloatingBaseFragment;
import com.compscieddy.striate.R;
import com.compscieddy.striate.databinding.PhotoViewerFragmentBinding;
import com.compscieddy.striate.entry.EntryHolder;
import com.compscieddy.striate.model.Entry;
import com.compscieddy.striate.util.DateUtil;

import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class PhotoViewerFragment extends FloatingBaseFragment {

  public interface CurrentEntryCallback {
    Entry getCurrentEntry();
  }

  public static final String TAG = PhotoViewerFragment.class.getSimpleName();
  private Context c;
  private Resources res;
  private PhotoViewerFragmentBinding binding;
  private List<Entry> mEntries;
  private Entry mEntry_useCurrentPositionInstead;
  private int mCurrentPosition;
  private PhotoViewerTimelineManager mTimelineManager;
  private PhotoCaptionManager mPhotoCaptionManager;

  public static PhotoViewerFragment newInstance(List<Entry> photoEntries, Entry entry) {
    PhotoViewerFragment fragment = new PhotoViewerFragment();
    fragment.setEntries(photoEntries);
    fragment.setEntry(entry);
    return fragment;
  }

  private void setEntry(Entry entry) {
    mEntry_useCurrentPositionInstead = entry;
  }

  private void setEntries(List<Entry> entries) {
    mEntries = entries;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = PhotoViewerFragmentBinding.inflate(inflater, container, false);
    c = binding.getRoot().getContext();
    res = c.getResources();
    mCurrentPosition = getPosition(mEntries, mEntry_useCurrentPositionInstead);

    mTimelineManager = new PhotoViewerTimelineManager(c, mEntries, binding);
    mTimelineManager.initEntryTimeline();

    initTextOrPhotoEntry();

    initListeners();
    expandTouchTargets();

    enterMainContainerAnimation();

    return binding.getRoot();
  }

  private void expandTouchTargets() {
    ViewEtil.expandTouchTarget(binding.photoSaveButtonContainer, 10);
    ViewEtil.expandTouchTarget(binding.photoShareButtonContainer, 10);
    ViewEtil.expandTouchTarget(binding.photoDeleteButtonContainer, 10);
  }

  private void initTextOrPhotoEntry() {
    Entry entry = mEntries.get(mCurrentPosition);

    binding.photoViewerEntryTimelineScrollView.post(() -> mTimelineManager.updateCurrentEntry(entry));

    initCommonLayout();
    if (entry.getIsPhoto()) {
      initPhotoLayout();
    } else {
      initTextLayout();
    }
  }

  private void initCommonLayout() {
    Entry currentEntry = mEntries.get(mCurrentPosition);

    binding.photoImageView.setVisibility(currentEntry.getIsPhoto() ? VISIBLE
        : GONE);
    binding.entryTextViewContainer.setVisibility(currentEntry.getIsPhoto() ?
        GONE : VISIBLE);
    binding.photoCaption.setVisibility(currentEntry.getIsPhoto() ? VISIBLE : GONE);
    binding.hashtagAllPhotosRecyclerView.setVisibility(currentEntry.getIsPhoto() ? VISIBLE : GONE);

    View photoOrEntryView = currentEntry.getIsPhoto()
        ? binding.photoImageView
        : binding.entryTextViewContainer;
    ViewGroup.LayoutParams photoOrTextParams = photoOrEntryView.getLayoutParams();
    if (currentEntry.getIsPhoto()) {
      photoOrTextParams.height = (int) (ScreenEtil.getScreenHeight(c) * 0.65f);
    } else {
      // text looks better when the container is slightly shorter
      photoOrTextParams.height = (int) (ScreenEtil.getScreenHeight(c) * 0.6f);
    }
    photoOrEntryView.setLayoutParams(photoOrTextParams);
  }

  private void initTextLayout() {
    Entry currentEntry = mEntries.get(mCurrentPosition);

    binding.entryTextView.setText(currentEntry.getEntryText());
    binding.entryTextView.setTextColor(EntryHolder.getChameleonTextColor(
        currentEntry.getColor()));

    binding.entryTextViewContainer.setBackgroundColor(currentEntry.getColor());
  }

  private void initPhotoLayout() {
    Entry entry = mEntries.get(mCurrentPosition);

    Glide.with(c)
        .load(entry.getUriString())
        .placeholder(R.drawable.photo_placeholder)
        .into(binding.photoImageView);

    PhotoAnimationUtil.animateImagePulsing(entry, binding.photoImageView);

    mPhotoCaptionManager = new PhotoCaptionManager(binding, () -> mEntries.get(mCurrentPosition));
    mPhotoCaptionManager.initPhotoCaption();
  }

  private int getPosition(List<Entry> photoEntries, Entry entry) {
    for (int i = 0; i < photoEntries.size(); i++) {
      Entry e = photoEntries.get(i);
      if (TextUtils.equals(e.getId(), entry.getId())) {
        return i;
      }
    }
    return -1;
  }

  private void enterMainContainerAnimation() {
    binding.mainDialogContainer.setScaleX(0.1f);
    binding.mainDialogContainer.setScaleY(0.1f);

    binding.mainDialogContainer.animate()
        .scaleX(1)
        .scaleY(1);
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.setOnKeyListener((dialog1, keyCode, event) -> {
      if (keyCode == KeyEvent.KEYCODE_BACK) {
        exitMainContainerAnimation();
        return true;
      }
      return false;
    });
    return dialog;
  }

  private void exitMainContainerAnimation() {
    binding.mainDialogContainer.animate()
        .scaleX(0.1f)
        .scaleY(0.1f)
        .withEndAction(this::dismiss);
    binding.blackCurtainBackground.animate()
        .alpha(0);
  }

  // todo: split into photo and text listeners, also make attach/detach
  private void initListeners() {
    binding.prevButton.setOnClickListener((v) -> {
      if (mCurrentPosition - 1 >= 0) {
        mCurrentPosition -= 1;
        initTextOrPhotoEntry();
      }
      VibrationEtil.vibrate(binding.prevButton);
    });
    binding.nextButton.setOnClickListener((v) -> {
      if (mCurrentPosition + 1 <= mEntries.size() - 1) {
        mCurrentPosition += 1;
        initTextOrPhotoEntry();
      }
      VibrationEtil.vibrate(binding.nextButton);
    });
    binding.photoSaveButtonContainer.setOnClickListener((v) -> {
      VibrationEtil.vibrate(binding.photoSaveButtonContainer);

      Entry entry = mEntries.get(mCurrentPosition);
      String photoUriString = entry.getUriString();

      downloadPhoto(entry, photoUriString);
    });
    binding.photoShareButtonContainer.setOnClickListener((v) -> {
      VibrationEtil.vibrate(binding.photoShareButtonContainer);

      Entry entry = mEntries.get(mCurrentPosition);
      String dateString = entry.getReadableDateString();

      String sharedContent = entry.getIsPhoto() ? entry.getUriString() : entry.getEntryText();

      String photoLinkText =
          entry.getEntryTypeTextCapitalized(c) + " from " + dateString + ": " + sharedContent;
      SharingEtil.fireShareIntentForText(
          getActivity(),
          "Share Photo with Friend:",
          photoLinkText);
    });

    binding.photoDeleteButton.setOnClickListener(v -> {
      Etil.showToast(c, "Long-press to delete", Gravity.TOP | CENTER_HORIZONTAL);
    });
    binding.photoDeleteButton.setOnLongClickListener(v -> {
      VibrationEtil.vibrate(binding.photoDeleteButton);

      Entry entry = mEntries.get(mCurrentPosition);
      Etil.showToast(c, "Deleting " + entry.getEntryTypeText(c) + "...");

      String removedEntryId = mEntries.get(mCurrentPosition).getId();

      deleteFromFirebaseAndFireStorage();
      deleteFromPhotoViewer();
      mTimelineManager.removeTimelineMarkerViewAndDivider(removedEntryId);
      return true;
    });
  }

  private void deleteFromFirebaseAndFireStorage() {
    Entry entry = mEntries.get(mCurrentPosition);
    if (entry.getIsPhoto()) {
      entry.deleteOnFirebaseStorage();
    }
    entry.deleteOnFirebase();
  }

  private void deleteFromPhotoViewer() {
    if (mCurrentPosition != 0) {
      mCurrentPosition -= 1;
    }

    if (mEntries.size() == 0) {
      exitMainContainerAnimation();
    } else {
      initTextOrPhotoEntry();
    }
  }

  private void downloadPhoto(Entry photoEntry, String photoUriString) {
    Etil.showToast(c, "Downloading photo...");

    BasicImageDownloader imageDownloader =
        new BasicImageDownloader(new BasicImageDownloader.OnImageLoaderListener() {
          @Override
          public void onError(BasicImageDownloader.ImageError error) {
            Timber.e("Failed to save photo from uri: %s", photoUriString);
          }

          @Override
          public void onProgressChange(int percent) {
            Timber.d("Downloading photo currently at " + percent + "%");
          }

          @Override
          public void onComplete(Bitmap resultBitmap) {
            Calendar calendar = DateUtil.getYearMonthDayCalendar(photoEntry.getYearMonthDay());
            String photoFilename = String.valueOf(calendar.getTimeInMillis());

            Etil.showToast(c, "Photo saved successfully");

            // todo: a better method may exist here: https://stackoverflow.com/a/8722494
            // todo: putting into a folder called Writeaday would be nice

            MediaStore.Images.Media.insertImage(
                getContext().getContentResolver(),
                resultBitmap,
                photoFilename,
                "");
          }
        });
    imageDownloader.download(photoUriString, true);
  }

  @Override
  public View getBlackCurtainBackground() {
    return binding.blackCurtainBackground;
  }

  @Override
  public View getMainDialogContainer() {
    return binding.mainDialogContainer;
  }
}
