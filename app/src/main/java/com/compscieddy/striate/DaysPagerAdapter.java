package com.compscieddy.striate;

import android.content.Context;
import android.content.res.Resources;

import com.compscieddy.striate.god.DayOfWeekManager;
import com.compscieddy.striate.util.DateUtil;

import java.util.Calendar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class DaysPagerAdapter extends FragmentPagerAdapter {

  public static final int NUM_DAYS_IN_WEEK = 7;

  private final Context c;
  private final Calendar mTodayCalendar;
  private final Resources res;
  private DayOfWeekManager mDayOfWeekManager;

  public DaysPagerAdapter(Context context, FragmentManager fm, DayOfWeekManager dayOfWeekManager) {
    super(fm);
    c = context;
    res = c.getResources();
    mDayOfWeekManager = dayOfWeekManager;
    mTodayCalendar = DateUtil.getTodayCalendar();
  }

  @Override
  public int getCount() {
    return NUM_DAYS_IN_WEEK;
  }

  @Override
  public long getItemId(int position) {
    String yearMonthDay = getYearMonthDayForPosition(position);
    return Integer.parseInt(yearMonthDay);
  }

  @Override
  public Fragment getItem(int position) {
    String yearMonthDay = getYearMonthDayForPosition(position);
    return DayFragment.newInstance(mDayOfWeekManager, yearMonthDay);
  }

  public String getYearMonthDayForPosition(int position) {
    Calendar currentCalendar = mTodayCalendar;
    int currentDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK); // ranges from 1 to 7

    // 0-indexing is much easier to work with (monday = 0)
    currentDayOfWeek = DateUtil.normalizeDayOfWeek(currentDayOfWeek);

    currentCalendar.add(Calendar.DAY_OF_YEAR, -currentDayOfWeek); // start of current week is Monday
    currentCalendar.add(Calendar.DAY_OF_YEAR, position);

    return DateUtil.getYearMonthDayFromCalendar(currentCalendar);
  }

}
