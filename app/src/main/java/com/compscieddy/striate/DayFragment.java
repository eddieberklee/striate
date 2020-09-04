package com.compscieddy.striate;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.compscieddy.eddie_utils.etil.ColorEtil;
import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.eddie_utils.etil.VibrationEtil;
import com.compscieddy.eddie_utils.etil.ViewEtil;
import com.compscieddy.striate.databinding.DayFragmentBinding;
import com.compscieddy.striate.entry.EntryManager;
import com.compscieddy.striate.eventbus.TopPaddingForDayOfWeekWithBackgroundEvent;
import com.compscieddy.striate.god.DayOfWeekManager;
import com.compscieddy.striate.model.Entry;
import com.compscieddy.striate.photo.PhotoRequestHelper;
import com.compscieddy.striate.util.AuthenticationUtil;
import com.compscieddy.striate.util.DateUtil;
import com.compscieddy.striate.util.EntryColorHelper;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.ListenerRegistration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import timber.log.Timber;

public class DayFragment extends Fragment {

  public static final String TAG = DayFragment.class.getSimpleName();

  private DayFragmentBinding binding;
  private Context c;
  private String mYearMonthDay;
  private EntryManager mEntryManager;
  private ListenerRegistration mEntryQueryRegistration;
  private DayOfWeekManager mDayOfWeekManager;
  private PhotoRequestHelper mPhotoRequestHelper;
  private Resources res;
  private NativeAd mFacebookNativeAd;

  public DayFragment(String yearMonthDay) {
    super();
    mYearMonthDay = yearMonthDay;
  }

  public static DayFragment newInstance(DayOfWeekManager dayOfWeekManager, String yearMonthDay) {
    DayFragment dayFragment = new DayFragment(yearMonthDay);
    dayFragment.setDayOfWeekManager(dayOfWeekManager);
    return dayFragment;
  }

  private void setDayOfWeekManager(DayOfWeekManager dayOfWeekManager) {
    mDayOfWeekManager = dayOfWeekManager;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = DayFragmentBinding.inflate(inflater, container, false);
    c = binding.getRoot().getContext();
    res = c.getResources();

    mPhotoRequestHelper = new PhotoRequestHelper(
        getContext(),
        getChildFragmentManager(),
        mYearMonthDay);

    binding.dayFragmentRootView.post(() -> {
      // Need to post because EntryManager constructor will call getHeight() on scroll view
      int entryScrollViewHeight = binding.entryScrollView.getHeight();

      mEntryManager = new EntryManager(
          c,
          getChildFragmentManager(),
          mDayOfWeekManager,
          entryScrollViewHeight,
          binding.newTextEntryButton,
          binding.entryScrollViewContainer,
          binding.entryScrollView,
          DateUtil.getDayOfWeekIndexFromYearMonthDay(mYearMonthDay));

      initEntryScrollView();
    });

    initFacebookAd();

    initNewEntryButtonGradients();

    ViewEtil.expandTouchTarget(binding.newPhotoEntryButton, 10);
    ViewEtil.expandTouchTarget(binding.newTextEntryButton, 10);

    return binding.getRoot();
  }

  private void initFacebookAd() {
    int currDayOfWeekColor = EntryColorHelper.getDayOfWeekColor(c, mYearMonthDay);
    binding.facebookAdContainer.setBackgroundColor(currDayOfWeekColor);

    int darkDayOfWeekColor = ColorEtil.applySaturationValueMultiplier(
        currDayOfWeekColor,
        0.92f,
        1.05f);
    binding.adCallToActionButton.setBackgroundColor(darkDayOfWeekColor);
    binding.adCallToActionButton.setTextColor(Color.WHITE);
    binding.adCallToActionButtonContainer.setCornerRadius(Etil.dpToPx(8));

    int darkTextColor = ColorEtil.applySaturationValueMultiplier(currDayOfWeekColor, 1.1f, 0.4f);
    binding.adTitle.setTextColor(darkTextColor);
    binding.adSponsoredLabel.setTextColor(darkTextColor);

    // Instantiate a NativeAd object.
    // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
    // now, while you are testing and replace it later when you have signed up.
    // While you are using this temporary code you will only get test ads and if you release
    // your code like this to the Google Play your users will not receive ads (you will get a no
    // fill error).
    mFacebookNativeAd = new NativeAd(c, "YOUR_PLACEMENT_ID");

    mFacebookNativeAd.setAdListener(new NativeAdListener() {
      @Override
      public void onMediaDownloaded(Ad ad) {
        // Native ad finished downloading all assets
        Timber.e("Native ad finished downloading all assets.");
      }

      @Override
      public void onError(Ad ad, AdError adError) {
        // Native ad failed to load
        Timber.e("Native ad failed to load: " + adError.getErrorMessage());
      }

      @Override
      public void onAdLoaded(Ad ad) {
        // Native ad is loaded and ready to be displayed
        Timber.d(TAG, "Native ad is loaded and ready to be displayed!");
        // Race condition, load() called again before last ad was displayed
        if (ad == null || mFacebookNativeAd != ad) {
          return;
        }
        inflateAd((NativeAd) ad);
      }

      private void inflateAd(NativeAd ad) {
        ad.unregisterView();

        binding.adTitle.setText(ad.getAdvertiserName());
        binding.adBody.setText(ad.getAdBodyText());
        binding.adSocialContext.setText(ad.getAdSocialContext());
        binding.adCallToActionButton.setVisibility(ad.hasCallToAction() ? View.VISIBLE :
            View.INVISIBLE);
        binding.adCallToActionButton.setText(ad.getAdCallToAction());
        binding.adSponsoredLabel.setText(ad.getSponsoredTranslation());

        List<View> adClickableViews = new ArrayList<>();
        adClickableViews.add(binding.adTitle);
        adClickableViews.add(binding.adCallToActionButton);

        ad.registerViewForInteraction(
            binding.facebookAdContainer,
            binding.adMediaView,
            binding.adIcon,
            adClickableViews);

        binding.adCallToActionButton.setTextColor(ColorEtil.applySaturationValueMultiplier(
            currDayOfWeekColor,
            1.6f,
            0.5f));
      }

      @Override
      public void onAdClicked(Ad ad) {
        // Native ad clicked
        Timber.d(TAG, "Native ad clicked!");
      }

      @Override
      public void onLoggingImpression(Ad ad) {
        // Native ad impression
        Timber.d(TAG, "Native ad impression logged!");
      }
    });

    // Request an ad
    mFacebookNativeAd.loadAd();
  }

  @Override
  public void onStart() {
    super.onStart();
    EventBus.getDefault().register(DayFragment.this);
  }

  @Override
  public void onStop() {
    super.onStop();
    EventBus.getDefault().unregister(DayFragment.this);
  }

  private void initNewEntryButtonGradients() {
    int currDayOfWeekColor = EntryColorHelper.getDayOfWeekColor(c, mYearMonthDay);
    int nextDayOfWeekColor = EntryColorHelper.getNextDayOfWeekColor(c, mYearMonthDay);

    LayerDrawable layerDrawable =
        (LayerDrawable) res.getDrawable(R.drawable.new_entry_button_gradient_button).mutate();

    GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.getDrawable(0);
    gradientDrawable.setColors(new int[] {currDayOfWeekColor, nextDayOfWeekColor});

    GradientDrawable plainBackgroundDrawable = (GradientDrawable) layerDrawable.getDrawable(1);
    plainBackgroundDrawable.setColor(ColorEtil.getAttributeColor(c, R.attr.bgPrimary));

    binding.newEntryButtonsContainerWithBackground.setBackground(layerDrawable);
    binding.newEntryButtonsContainerWithBackground.setElevationShadowColor(nextDayOfWeekColor);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mEntryQueryRegistration.remove();
  }

  private void initEntryScrollView() {
    mEntryQueryRegistration = Entry
        .getEntryQuery(AuthenticationUtil.getUserEmail(), mYearMonthDay)
        .addSnapshotListener((snapshots, error) -> {
          if (error != null) {
            Timber.e("Error while listening for entry query changes");
            return;
          }
          if (snapshots == null) {
            Timber.e("Null snapshots found while listening for entry query changes");
            return;
          }
          for (DocumentChange documentChange : snapshots.getDocumentChanges()) {
            switch (documentChange.getType()) {
              case ADDED:
                Entry newEntry = documentChange.getDocument().toObject(Entry.class);
                mEntryManager.addEntry(newEntry);
                break;
              case MODIFIED:
                Entry modifiedEntry = documentChange.getDocument().toObject(Entry.class);
                mEntryManager.updateEntry(modifiedEntry);
                break;
              case REMOVED:
                Entry removedEntry = documentChange.getDocument().toObject(Entry.class);
                mEntryManager.removeEntry(removedEntry);
            }
          }
        });
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

  private void attachListeners() {
    binding.newTextEntryButton.setOnClickListener(v -> {
      ViewEtil.runTouchFeedbackAnimation(binding.newTextEntryButton, 0.7f);
      VibrationEtil.vibrate(binding.newTextEntryButton);

      Entry entry = new Entry(mYearMonthDay);
      StriateApplication.setSharedPreferencesString(
          SharedPrefKeys.PREF_NEW_ENTRY_ID,
          entry.getId());

      entry.saveOnFirestore(null);
    });
    binding.newPhotoEntryButton.setOnClickListener((v) -> {
      ViewEtil.runTouchFeedbackAnimation(binding.newPhotoEntryButton, 0.7f);
      VibrationEtil.vibrate(binding.newTextEntryButton);

      mPhotoRequestHelper.launchPhotoPicker(DayFragment.this);
    });
  }

  private void detachListeners() {
    binding.newTextEntryButton.setOnClickListener(null);
    binding.newPhotoEntryButton.setOnClickListener(null);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mPhotoRequestHelper.onActivityResult(requestCode, resultCode, data);
  }

  /**
   * Day of week container has a transparent white background. As that container grows, we need
   * to update the padding since these 2 elements are in a FrameLayout where they're stacked on
   * top of each other.
   */
  public void updateTopPaddingForDayOfWeekWithBackground(int height) {
    if (binding != null && binding.entryScrollViewContainer != null) {
      binding.entryScrollViewContainer.setPadding(
          binding.entryScrollViewContainer.getPaddingLeft(),
          height,
          binding.entryScrollViewContainer.getPaddingRight(),
          binding.entryScrollViewContainer.getPaddingBottom());
    }
  }

  @Subscribe
  public void onTopPaddingForDayOfWeekWithBackgroundEvent(TopPaddingForDayOfWeekWithBackgroundEvent topPaddingForDayOfWeekWithBackgroundEvent) {
    updateTopPaddingForDayOfWeekWithBackground(topPaddingForDayOfWeekWithBackgroundEvent.topPaddingForDayOfWeek);
  }
}
