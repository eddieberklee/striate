package com.compscieddy.striate.util;

import android.content.Context;
import android.content.res.TypedArray;

import com.compscieddy.eddie_utils.etil.ColorEtil;
import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.striate.R;

import androidx.annotation.ColorInt;

public class EntryColorHelper {

  public static @ColorInt
  int getDayOfWeekColor(Context c, String yearMonthDay) {
    int dayOfWeekIndex = DateUtil.getDayOfWeekIndexFromYearMonthDay(yearMonthDay);
    return getWeekColors(c)[dayOfWeekIndex];
  }

  public static @ColorInt
  int getNextDayOfWeekColor(Context c, String yearMonthDay) {
    int dayOfWeekIndex = DateUtil.getDayOfWeekIndexFromYearMonthDay(yearMonthDay);
    int nextDayOfWeekIndex = (int) Etil.betterMod(dayOfWeekIndex + 1, 7);
    return getWeekColors(c)[nextDayOfWeekIndex];
  }

  public static @ColorInt
  int[] getWeekColors(Context c) {
    TypedArray weekColors = c.getResources().obtainTypedArray(R.array.day_of_week_colors);
    int[] colors = new int[weekColors.length()];
    for (int i = 0; i < weekColors.length(); i++) {
      colors[i] = weekColors.getColor(i, -1);
    }
    weekColors.recycle();
    return colors;
  }

  public static int getEntryColor(Context c, int position, int total, int dayOfWeekIndex) {
    int[] weekColors = EntryColorHelper.getWeekColors(c);

    float colorFraction = 0;
    if (position > 0) {
      colorFraction = (float) position / (total - 1);
    }

    int startColor = weekColors[dayOfWeekIndex];
    int endColor = weekColors[dayOfWeekIndex + 1];

    return ColorEtil.getIntermediateColor(startColor, endColor, colorFraction);
  }

}
