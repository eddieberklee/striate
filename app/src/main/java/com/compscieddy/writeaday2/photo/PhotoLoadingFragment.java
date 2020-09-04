package com.compscieddy.writeaday2.photo;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.compscieddy.eddie_utils.eui.FloatingBaseFragment;
import com.compscieddy.writeaday2.CyclingTextHelper;
import com.compscieddy.writeaday2.R;
import com.compscieddy.writeaday2.databinding.PhotoLoadingFragmentBinding;
import com.compscieddy.writeaday2.util.EntryColorHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PhotoLoadingFragment extends FloatingBaseFragment {

  public static final String TAG = PhotoLoadingFragment.class.getSimpleName();

  private PhotoLoadingFragmentBinding binding;
  private String mYearMonthDay;
  private Resources res;
  private Context c;

  public static PhotoLoadingFragment newInstance(String yearMonthDay) {
    PhotoLoadingFragment fragment = new PhotoLoadingFragment();
    fragment.setYearMonthDay(yearMonthDay);
    return fragment;
  }

  private void setYearMonthDay(String yearMonthDay) {
    mYearMonthDay = yearMonthDay;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = PhotoLoadingFragmentBinding.inflate(
        inflater,
        container,
        false);
    c = binding.getRoot().getContext();
    res = c.getResources();

    initLoadingSpinnerColor();
    initLoadingPhotoHints();
    return binding.getRoot();
  }

  private void initLoadingPhotoHints() {
    new CyclingTextHelper().cycle(
        binding.photoLoadingHintText,
        new String[] {
            res.getString(R.string.photo_loading_hint_1),
            res.getString(R.string.photo_loading_hint_2),
            res.getString(R.string.photo_loading_hint_3),
            },
        5000);
  }

  private void initLoadingSpinnerColor() {
    binding.loadingSpinner.setColor(EntryColorHelper.getDayOfWeekColor(
        getContext(),
        mYearMonthDay));
  }

  @Override
  public View getBlackCurtainBackground() {
    return binding.blackCurtainBackground;
  }

  @Override
  public View getMainDialogContainer() {
    return binding.mainDialogContainer;
  }

  public void setProgress(double progress) {
    binding.loadingSpinner.animate()
        .scaleX((float) (1 + 7 * (progress / 100)))
        .scaleY((float) (1 + 7 * (progress / 100)));
  }
}
