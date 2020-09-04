package com.compscieddy.infinote.god;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.eddie_utils.etil.KeyboardEtil;
import com.compscieddy.infinote.ConfettiEvent;
import com.compscieddy.infinote.DaysPagerAdapter;
import com.compscieddy.infinote.databinding.MainGodFragmentBinding;
import com.compscieddy.infinote.eventbus.TopPaddingForDayOfWeekWithBackgroundEvent;
import com.google.common.base.Preconditions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class MainGodFragment extends Fragment {

  public static final int DEFAULT_MAX_DAYS = 10;

  private Context c;
  private Resources res;
  private MainGodFragmentBinding binding;
  private DayOfWeekManager mDayOfWeekManager;
  private DaysPagerAdapter mDaysPagerAdapter;
  private View.OnLayoutChangeListener mDayOfWeekContainerWithBackgroundLayoutListener = (
      v, left, top,
      right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
    int height = bottom - top;
    height -= binding.dayOfWeekContainerWithTransparentBackground.getPaddingBottom();
    height -= Etil.dpToPx(3);
    EventBus.getDefault().post(new TopPaddingForDayOfWeekWithBackgroundEvent(height));
  };

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = MainGodFragmentBinding.inflate(inflater, container, false);
    c = Preconditions.checkNotNull(getContext());
    res = c.getResources();

    // todo: untangle the dependencies between DayOfWeekManager, DayPagerAdapter, and DayViewPager
    mDayOfWeekManager = new DayOfWeekManager(binding);
    initDayViewPager();
    mDayOfWeekManager.init(mDaysPagerAdapter);

    return binding.getRoot();
  }

  private void initDayViewPager() {
    mDaysPagerAdapter = new DaysPagerAdapter(
        c,
        getChildFragmentManager(),
        mDayOfWeekManager);
    binding.daysViewPager.setAdapter(mDaysPagerAdapter);
    binding.daysViewPager.setOffscreenPageLimit(DaysPagerAdapter.NUM_DAYS_IN_WEEK);
    binding.daysViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        int dayOfWeekIndex = position;
        mDayOfWeekManager.setTodayCircle(dayOfWeekIndex, positionOffset);

        KeyboardEtil.hideKeyboard(binding.daysViewPager);
      }

      @Override
      public void onPageSelected(int position) {
        String newYearMonthDay = mDaysPagerAdapter.getYearMonthDayForPosition(position);
        mDayOfWeekManager.setCurrentYearMonthDay(newYearMonthDay, false);
      }

      @Override
      public void onPageScrollStateChanged(int state) {
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

  @SuppressWarnings("unused")
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onConfettiEvent(ConfettiEvent confettiEvent) {
    binding.confettiView.build()
        .addShapes(Shape.CIRCLE)
        .addColors(confettiEvent.getLightConfettiColor(), confettiEvent.getConfettiColor(),
            confettiEvent.getDarkConfettiColor())
        .setDirection(0, 359)
        .setSpeed(5f, 7f)
        .setFadeOutEnabled(true)
        .setTimeToLive(300L)
        .addSizes(new Size(5, 2.1f))
        .setPosition(confettiEvent.x, confettiEvent.x, confettiEvent.y, confettiEvent.y)
        .streamFor(1400, 60L);
  }

  private void attachListeners() {
    binding.dayOfWeekContainerWithTransparentBackground.postDelayed(() -> {
      binding.dayOfWeekContainerWithTransparentBackground.addOnLayoutChangeListener(
          mDayOfWeekContainerWithBackgroundLayoutListener);
    }, 400);
  }

  private void detachListeners() {
    binding.dayOfWeekContainerWithTransparentBackground.removeOnLayoutChangeListener(
        mDayOfWeekContainerWithBackgroundLayoutListener);
  }

}
