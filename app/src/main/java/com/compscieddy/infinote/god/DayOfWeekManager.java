package com.compscieddy.infinote.god;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.compscieddy.eddie_utils.etil.ColorEtil;
import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.eddie_utils.etil.KeyboardEtil;
import com.compscieddy.eddie_utils.etil.ViewEtil;
import com.compscieddy.infinote.Analytics;
import com.compscieddy.infinote.DaysPagerAdapter;
import com.compscieddy.infinote.R;
import com.compscieddy.infinote.databinding.MainGodFragmentBinding;
import com.compscieddy.infinote.model.Entry;
import com.compscieddy.infinote.util.DateUtil;
import com.compscieddy.infinote.util.EntryColorHelper;

import java.util.Calendar;

import androidx.annotation.ColorInt;
import androidx.viewpager.widget.ViewPager;
import carbon.widget.FrameLayout;
import timber.log.Timber;

public class DayOfWeekManager {

  private final Context c;
  private final Resources res;

  private final String[] mDayOfWeekInitials;
  @ColorInt private final int[] mDayOfWeekColors;

  private MainGodFragmentBinding binding;
  private ViewPager mDaysViewPager;
  private TextView[] mDayOfWeekTextViews = new TextView[DaysPagerAdapter.NUM_DAYS_IN_WEEK];
  private String mYearMonthDay;
  private int mTodayDayOfWeekIndex;

  private View.OnClickListener mDayOfWeekClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      int clickedDayOfWeekIndex = -1;
      switch (v.getId()) {
        case R.id.nav_mon_text:
          clickedDayOfWeekIndex = 0;
          break;
        case R.id.nav_tue_text:
          clickedDayOfWeekIndex = 1;
          break;
        case R.id.nav_wed_text:
          clickedDayOfWeekIndex = 2;
          break;
        case R.id.nav_thu_text:
          clickedDayOfWeekIndex = 3;
          break;
        case R.id.nav_fri_text:
          clickedDayOfWeekIndex = 4;
          break;
        case R.id.nav_sat_text:
          clickedDayOfWeekIndex = 5;
          break;
        case R.id.nav_sun_text:
          clickedDayOfWeekIndex = 6;
          break;
      }
      if (clickedDayOfWeekIndex != -1) {
        mDaysViewPager.setCurrentItem(clickedDayOfWeekIndex, true);
        KeyboardEtil.hideKeyboard(binding.getRoot());

        Analytics.track(Analytics.DAY_OF_WEEK_CLICKED);
      }
    }
  };
  private DaysPagerAdapter mDaysPagerAdapter;

  public DayOfWeekManager(MainGodFragmentBinding binding) {
    this.binding = binding;
    mDaysViewPager = binding.daysViewPager;

    c = binding.getRoot().getContext();
    res = c.getResources();

    mDayOfWeekInitials = res.getStringArray(R.array.day_of_week_initials);
    mDayOfWeekColors = EntryColorHelper.getWeekColors(c);
  }

  public void init(DaysPagerAdapter daysPagerAdapter) {
    mDaysPagerAdapter = daysPagerAdapter;

    int todayDayOfWeekIndex =
        DateUtil.getDayOfWeekIndexFromYearMonthDay(DateUtil.getTodayYearMonthDay());

    mDaysViewPager.setCurrentItem(todayDayOfWeekIndex);
    initDayOfWeek();
    initTodayDot();
    setTodayCircle(todayDayOfWeekIndex, 0);
  }

  private void initDayOfWeek() {
    resetNavTexts();

    for (int i = 0; i < 7; i++) {
      TextView dayOfWeekTextView = (TextView) ((ViewGroup) binding.dayOfWeekContainer
          .getChildAt(i))
          .getChildAt(0);

      dayOfWeekTextView.setTextColor(mDayOfWeekColors[i]);

      dayOfWeekTextView.setShadowLayer(
          Etil.dpToPx(2),
          0,
          Etil.dpToPx(2),
          ColorEtil.applyAlpha(mDayOfWeekColors[i], 0.8f));
    }

    if (mDaysPagerAdapter != null) { // if has been initialized
      for (int i = 0; i < binding.dayOfWeekSparklinesContainer.getChildCount(); i++) {
        View sparklineContainer = binding.dayOfWeekSparklinesContainer.getChildAt(i);
        String yearMonthDay = mDaysPagerAdapter.getYearMonthDayForPosition(i);
        sparklineContainer.setTag(yearMonthDay);
      }
    } else {
      Timber.e("initNavWeekView() was called before mDaysPagerAdapter was initialized!");
    }

    // click listeners for nav day texts
    for (int i = 0; i < binding.dayOfWeekContainer.getChildCount(); i++) {
      View v = binding.dayOfWeekContainer.getChildAt(i);
      v.setOnClickListener(mDayOfWeekClickListener);
    }

    binding.todayCircle.animate().alpha(1);
    initTodayDot();

    // also preload all entries for the "Al Entries" screen
  }

  public void setCurrentYearMonthDay(String yearMonthDay, boolean setCurrentItemForViewPager) {
    // go through all the other ones and reset the text
    resetNavTexts();

    Calendar selectedCalendar = DateUtil.getYearMonthDayCalendar(yearMonthDay);

    final int dayOfWeekIndex = DateUtil.getDayOfWeekIndexFromYearMonthDay(yearMonthDay);

    // set number for current day
    setNavWeekTextDayOfMonth(yearMonthDay);

    if (setCurrentItemForViewPager) {
      mDaysViewPager.setCurrentItem(dayOfWeekIndex);
      mYearMonthDay = mDaysPagerAdapter.getYearMonthDayForPosition(dayOfWeekIndex);
    }

    Timber.d("monday " + " dayOfWeekIndex: " + dayOfWeekIndex + " mTodayDayOfWeekIndex: " + mTodayDayOfWeekIndex);
    if (true) {
      return;
    }
    if (dayOfWeekIndex == mTodayDayOfWeekIndex) {
      binding.todayDotContainer.animate()
          .translationY(res.getDimensionPixelSize(R.dimen.nav_today_marker_extended_translation));
    } else {
      binding.todayDotContainer.animate()
          .translationY(res.getDimensionPixelSize(R.dimen.nav_today_marker_initial_translation));
    }
  }

  private void setNavWeekTextDayOfMonth(String yearMonthDay) {
    int dayOfWeekIndex = DateUtil.getDayOfWeekIndexFromYearMonthDay(yearMonthDay);

    Calendar selectedCalendar = DateUtil.getYearMonthDayCalendar(yearMonthDay);
    final int dayOfMonth = selectedCalendar.get(Calendar.DAY_OF_MONTH);

    final TextView navTextView = mDayOfWeekTextViews[dayOfWeekIndex];
    navTextView.animate().alpha(0).setDuration(100).withEndAction(() -> {
      navTextView.setText(String.valueOf(dayOfMonth));
      navTextView.animate()
          .alpha(1)
          .setDuration(100);
    });
  }

  private void resetNavTexts() {
    for (int i = 0; i < binding.dayOfWeekContainer.getChildCount(); i++) {
      TextView navDayText = (TextView) ((ViewGroup) binding.dayOfWeekContainer
          .getChildAt(i)).getChildAt(0);
      mDayOfWeekTextViews[i] = navDayText;

      navDayText.animate().cancel();

      navDayText.setAlpha(1);
      navDayText.setText(mDayOfWeekInitials[i]);
    }
  }

  private void initTodayDot() {
    binding.dayOfWeekContainer.post(() -> {
      Calendar calendar = DateUtil.getTodayCalendar();
      mTodayDayOfWeekIndex = DateUtil.normalizeDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));

      TextView dayOfWeekInitialView = mDayOfWeekTextViews[mTodayDayOfWeekIndex];

      int[] coordinates = ViewEtil.getViewCoordinatesInWindow(dayOfWeekInitialView);
      int middleOfDayOfWeek = coordinates[0]
          + dayOfWeekInitialView.getWidth() / 2
          - binding.todayDot.getWidth() / 2;

      binding.todayDot.animate()
          .translationX(middleOfDayOfWeek);

      ColorEtil.applyColorFilter(
          binding.todayDot.getBackground(),
          mDayOfWeekColors[mTodayDayOfWeekIndex]);
    });
  }

  public void setTodayCircle(int dayOfWeekIndex, float positionOffset) {
    float itemDistance =
        (res.getDimensionPixelSize(R.dimen.nav_day_width) + res.getDimensionPixelSize(
            R.dimen.nav_day_margin_right));
    float selectedTranslationX = dayOfWeekIndex * itemDistance;
    if (positionOffset != -1) selectedTranslationX += positionOffset * itemDistance;
    binding.todayCircle.setTranslationX(selectedTranslationX);

    int circleColor;

    boolean isSettledOnDayOfWeek = positionOffset == -1; // alternative is that it's in between
    // day of week initials

    int todayColor = mDayOfWeekColors[dayOfWeekIndex];
    int tomorrowColor = mDayOfWeekColors[(int) Etil.betterMod(
        dayOfWeekIndex + 1,
        mDayOfWeekColors.length)];

    if (isSettledOnDayOfWeek) {
      circleColor = todayColor;
    } else {
      circleColor = ColorEtil.getIntermediateColor(todayColor, tomorrowColor, positionOffset);
    }

    GradientDrawable gradientCircleDrawable = new GradientDrawable();
    gradientCircleDrawable.setShape(GradientDrawable.OVAL);
    gradientCircleDrawable.setStroke(Etil.dpToPx(2), circleColor);

    binding.todayCircle.setBackground(gradientCircleDrawable);

    resetNavTexts();

    String yearMonthDay = mDaysPagerAdapter.getYearMonthDayForPosition(dayOfWeekIndex);
    setNavWeekTextDayOfMonth(yearMonthDay);
  }

  public FrameLayout generateNewSparklineView(Entry entry) {
    FrameLayout sparklineView = new FrameLayout(c);
    setSparklineView(entry, sparklineView);
    sparklineView.setCornerRadius(Etil.dpToPx(999));
    sparklineView.setTag(entry.getId());
    return sparklineView;
  }

  // todo: use maps here so that we don't have to do for loops, these will add up, given how many
  //  ui components I have
  public void updateSparklineColor(Entry entry, int dayOfWeekIndex) {
    ViewGroup sparklineContainer = (ViewGroup) binding.dayOfWeekSparklinesContainer.getChildAt(
        dayOfWeekIndex);
    for (int i = 0; i < sparklineContainer.getChildCount(); i++) {
      ViewGroup sparklineRowContainer = (ViewGroup) sparklineContainer.getChildAt(i);
      for (int j = 0; j < sparklineRowContainer.getChildCount(); j++) {
        View sparklineView = sparklineRowContainer.getChildAt(j);
        if (TextUtils.equals(((String) sparklineView.getTag()), entry.getId())) {
          setSparklineView(entry, sparklineView);
          return;
        }
      }
    }
  }

  /**
   * Photo entries don't have an entry color.
   */
  private void setSparklineView(Entry entry, View sparklineView) {
    sparklineView.setBackgroundColor(entry.getIsPhoto() ? ColorEtil.getAttributeColor(
        c,
        R.attr.fgPrimary20) : entry.getColor());
  }

  public ViewGroup getDayOfWeekSparklinesContainer(int dayOfWeekIndex) {
    return (ViewGroup) binding.dayOfWeekSparklinesContainer.getChildAt(dayOfWeekIndex);
  }

  public LinearLayout.LayoutParams getSparklineViewLayoutParams(Entry entry) {
    LinearLayout.LayoutParams sparklineParams = new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        res.getDimensionPixelSize(R.dimen.sparkline_height));
    sparklineParams.bottomMargin = res.getDimensionPixelSize(R.dimen.sparkline_vertical_spacing);
    sparklineParams.rightMargin = Etil.dpToPx(1);

    // This should be reversed...but I don't know why this works.
    sparklineParams.weight = entry.getIsPhoto() ? 2 : 1;
    return sparklineParams;
  }

  // todo: keep track of all the sparkline views with a map just like we do with entries in 
  //  EntryManager.
  public View getSparklineView(String entryId, int dayOfWeekIndex) {
    ViewGroup sparklinesContainer = (ViewGroup) binding.dayOfWeekSparklinesContainer.getChildAt(
        dayOfWeekIndex);
    for (int i = 0; i < sparklinesContainer.getChildCount(); i++) {
      ViewGroup sparklineRowContainer = (ViewGroup) sparklinesContainer.getChildAt(i);
      for (int j = 0; j < sparklineRowContainer.getChildCount(); j++) {
        View sparklineView = sparklineRowContainer.getChildAt(j);
        if (TextUtils.equals(((String) sparklineView.getTag()), entryId)) {
          return sparklineView;
        }
      }
    }
    return null;
  }
}
