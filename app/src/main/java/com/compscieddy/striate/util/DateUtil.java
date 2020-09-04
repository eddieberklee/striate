package com.compscieddy.striate.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.text.style.RelativeSizeSpan;

import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.eddie_utils.eui.FontCache;
import com.compscieddy.striate.R;
import com.compscieddy.striate.StriateApplication;
import com.google.common.base.Preconditions;
import com.google.firebase.firestore.Exclude;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;

import static java.util.Calendar.AM_PM;
import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.SHORT;

public class DateUtil {

  public static class ThinTypefaceSpan extends MetricAffectingSpan {

    private Typeface mThinTypeface;

    @Override
    public void updateDrawState(TextPaint textPaint) {
      mThinTypeface = FontCache.get(
          StriateApplication.sApplicationContext,
          FontCache.MANROPE_REGULAR);
      textPaint.setTypeface(mThinTypeface);
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint textPaint) {
      textPaint.setTypeface(mThinTypeface);
    }
  }

  public static String getTimeAgoString(Context c, long millis) {
    Resources res = c.getResources();

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(millis);

    int minutesAgo = Math.abs(DateUtil.getMinutesAgo(millis, System.currentTimeMillis()));
    int hoursAgo = Math.abs(DateUtil.getHoursAgo(millis, System.currentTimeMillis()));
    int daysAgo = Math.abs(DateUtil.getDaysAgo(millis, System.currentTimeMillis()));
    int weeksAgo = Math.abs(DateUtil.getWeeksAgo(millis, System.currentTimeMillis()));
    int monthsAgo = weeksAgo / 4; // not completely accurate but roughly good enough

    if (minutesAgo < 60) {
      return res.getQuantityString(R.plurals.minutes_ago, minutesAgo, minutesAgo);
    } else if (hoursAgo < 24) {
      return res.getQuantityString(R.plurals.hours_ago, hoursAgo, hoursAgo);
    } else if (daysAgo < 7) {
      return res.getQuantityString(R.plurals.days_ago, daysAgo, daysAgo);
    } else if (weeksAgo < 4) {
      return res.getQuantityString(R.plurals.weeks_ago, weeksAgo, weeksAgo);
    } else if (monthsAgo < 3) {
      return res.getQuantityString(R.plurals.months_ago, monthsAgo, monthsAgo);
    } else {
      StringBuilder builder = new StringBuilder();

      String month = calendar.getDisplayName(
          Calendar.MONTH,
          Calendar.LONG,
          Locale.getDefault());
      builder.append(month);
      builder.append(" ");

      builder.append(calendar.get(Calendar.DAY_OF_MONTH));

      int todayYear = Calendar.getInstance().get(Calendar.YEAR);
      int entryYear = calendar.get(Calendar.YEAR);
      boolean isDifferentYear = entryYear != todayYear;
      if (isDifferentYear) {
        builder.append(", ");
        builder.append(entryYear);
      }
      return builder.toString();
    }
  }

  public static int getMinutesAgo(long pastMillis, long currentMillis) {
    return Math.round((currentMillis - pastMillis) / 1000f / 60);
  }

  public static int getHoursAgo(long pastMillis, long currentMillis) {
    return Math.round((currentMillis - pastMillis) / 1000f / 60 / 60);
  }

  public static int getDaysAgo(long pastMillis, long currentMillis) {
    return Math.round((currentMillis - pastMillis) / 1000f / 60 / 60 / 24);
  }

  public static int getWeeksAgo(long pastMillis, long currentMillis) {
    return Math.round((currentMillis - pastMillis) / 1000f / 60 / 60 / 24 / 7);
  }

  @Exclude
  public static String getExactTimeStringWithoutAmPm(long millis) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(millis);

    int hour = calendar.get(HOUR);
    int minute = calendar.get(Calendar.MINUTE);

    StringBuilder builder = new StringBuilder();
    builder.append(hour == 0 ? "12" : hour);
    builder.append(":");
    if (minute < 10) {
      builder.append("0");
    }
    builder.append(minute);

    return builder.toString();
  }

  public static String getExactTimeStringWithAmPm(long millis) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(millis);

    StringBuilder builder = new StringBuilder();
    builder.append(getExactTimeStringWithoutAmPm(millis));
    builder.append(" ");
    builder.append(calendar.getDisplayName(
        AM_PM,
        SHORT,
        Locale.getDefault()).toLowerCase());
    return builder.toString();
  }

  public static String getExactTimeStringWithShortAmPm(long millis) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(millis);

    char firstLetterOfAmPm = calendar.getDisplayName(
        AM_PM,
        SHORT,
        Locale.getDefault()).toLowerCase().charAt(0);

    StringBuilder builder = new StringBuilder();
    builder.append(getExactTimeStringWithoutAmPm(millis));
    builder.append(firstLetterOfAmPm);
    return builder.toString();
  }

  public static CharSequence getTimeWithAmPmString(long timeMillis) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timeMillis);
    SpannableStringBuilder builder = new SpannableStringBuilder();

    String hour = String.valueOf(calendar.get(HOUR));
    builder.append(hour.equals("0") ? "12" : hour);
    builder.append(":");

    String minuteString = String.valueOf(calendar.get(MINUTE));
    builder.append(minuteString.length() == 1 ? "0" + minuteString : minuteString);
    builder.append(" ");

    builder.append(String.valueOf(calendar.getDisplayName(AM_PM, SHORT, Locale.getDefault())));
    return builder;
  }

  /**
   * Ex: 3:38 PM (with the PM in a different typeface and smaller)
   */
  public static SpannableStringBuilder getTimeWithAmPmAndStylizedString(long timeMillis) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timeMillis);
    SpannableStringBuilder builder = new SpannableStringBuilder();

    builder.append(String.valueOf(calendar.get(HOUR)));
    builder.append(":");

    String minuteString = String.valueOf(calendar.get(MINUTE));
    builder.append(minuteString.length() == 1 ? "0" + minuteString : minuteString);
    builder.append(" ");

    int startIndexThinSmallFont = builder.length();
    builder.append(String.valueOf(calendar.getDisplayName(AM_PM, SHORT, Locale.getDefault())));
    int endIndexThinSmallFont = builder.length();

    ThinTypefaceSpan thinTypefaceSpan = new ThinTypefaceSpan();
    RelativeSizeSpan smallSizeSpan = new RelativeSizeSpan(0.65f);

    builder.setSpan(
        thinTypefaceSpan,
        startIndexThinSmallFont,
        endIndexThinSmallFont,
        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    builder.setSpan(
        smallSizeSpan,
        startIndexThinSmallFont,
        endIndexThinSmallFont,
        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    return builder;
  }

  /**
   * YYYYMMDD Format is zero padded on the months and days
   * <p>
   * Ex: 20200328 for March 28, 2020
   */
  public static String getYearMonthDayFromCalendar(Calendar calendar) {
    StringBuilder builder = new StringBuilder();
    builder.append(calendar.get(Calendar.YEAR));

    String month = String.valueOf(calendar.get(Calendar.MONTH));
    builder.append(month.length() == 1 ? "0" + month : month);

    String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    builder.append(day.length() == 1 ? "0" + day : day);

    return builder.toString();
  }

  /**
   * YYYYMMDD Format is zero padded on the months and days
   * <p>
   * Ex: 20200328 for March 28, 2020
   */
  public static Calendar getYearMonthDayCalendar(String yearMonthDay) {
    Calendar calendar = Calendar.getInstance();

    int year = Integer.parseInt(yearMonthDay.substring(0, 4));
    int month = Integer.parseInt(yearMonthDay.substring(4, 6));
    int day = Integer.parseInt(yearMonthDay.substring(6, 8));

    calendar.set(year, month, day);
    return calendar;
  }

  /**
   * Returns a duration string with the only unit of time being hours.
   * <p>
   * Ex: 1.2 hr, 3.7 hr
   */
  public static CharSequence getDurationStringHourFractionOnly(long durationMillis) {
    float hours = (float) durationMillis / (1000f * 60 * 60);
    int minutes = (int) durationMillis / (1000 * 60) % 60;

    StringBuilder stringBuilder = new StringBuilder();

    if (hours < 1) {
      stringBuilder.append(new DecimalFormat("#.#").format(minutes));
      stringBuilder.append(" min");
    } else {
      stringBuilder.append(new DecimalFormat("#.#").format(hours));
      stringBuilder.append(" hr");
    }

    return stringBuilder;
  }

  public static String getDurationStringHourFractionOnlyWithSeconds(long durationMillis) {
    int seconds = (int) durationMillis / 1000 % 60;
    return getDurationStringHourFractionOnly(durationMillis) + " " + seconds + " s";
  }

  public static String getShortHandDurationWithoutSecondsString(long durationMillis) {
    int seconds = (int) durationMillis / 1000 % 60;
    int minutes = (int) durationMillis / (1000 * 60) % 60;
    int hours = (int) durationMillis / (1000 * 60 * 60) % 60;

    StringBuilder stringBuilder = new StringBuilder();
    if (hours > 0) {
      stringBuilder.append(hours);
      stringBuilder.append(" h");
    }
    if (minutes > 0) {
      stringBuilder.append("\n");
      stringBuilder.append(minutes);
      stringBuilder.append(" m");
    }

    // Only show the seconds if we have no hours and no minutes to show
    if (hours == 0 && minutes == 0) {
      stringBuilder.append("\n");
      stringBuilder.append(seconds);
      stringBuilder.append(" s");
    }
    return stringBuilder.toString();
  }

  public static String getCurrentYearMonthDay() {
    Calendar calendar = Calendar.getInstance();
    return getYearMonthDayFromCalendar(calendar);
  }

  public static Calendar getTodayCalendar() {
    Calendar todayCalendar = Calendar.getInstance();
    return todayCalendar;
  }

  public static String getDayOfWeekChar(Context c, String yearMonthDay) {
    int dayOfWeekIndex = getDayOfWeekIndexFromYearMonthDay(yearMonthDay);

    TypedArray weekColors = c.getResources().obtainTypedArray(R.array.day_of_week_initials);
    String[] dayOfWeekInitials = new String[weekColors.length()];
    for (int i = 0; i < weekColors.length(); i++) {
      dayOfWeekInitials[i] = weekColors.getString(i);
    }
    weekColors.recycle();
    return dayOfWeekInitials[dayOfWeekIndex];
  }

  public static int getDayOfWeekIndexFromYearMonthDay(String yearMonthDay) {
    Calendar calendar = DateUtil.getYearMonthDayCalendar(yearMonthDay);
    return (int) Etil.betterMod(calendar.get(Calendar.DAY_OF_WEEK) - 2, 7);
  }

  public static String getTodayYearMonthDay() {
    Calendar calendar = Calendar.getInstance();
    return getYearMonthDayFromCalendar(calendar);
  }

  /**
   * Is @param yearMonthDay1 before @param yearMonthDay2
   */
  public static boolean isBefore(String yearMonthDay1, String yearMonthDay2) {
    Calendar calendar1 = DateUtil.getYearMonthDayCalendar(yearMonthDay1);
    Calendar calendar2 = DateUtil.getYearMonthDayCalendar(yearMonthDay2);
    return calendar1.getTimeInMillis() - calendar2.getTimeInMillis() < 0;
  }

  /**
   * position = 0 is today, position = 1 is yesterday, etc.
   */
  public static String getYearMonthDayFromAdapterPosition(int position) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(DAY_OF_YEAR, -position);
    return DateUtil.getYearMonthDayFromCalendar(calendar);
  }

  public static int[] getReminderHourOfDayMinuteFromString(String hourOfDayMinuteString) {
    int[] hourOfDayMinute = new int[2];
    hourOfDayMinute[0] = Integer.parseInt(hourOfDayMinuteString.substring(0, 2));
    hourOfDayMinute[1] = Integer.parseInt(hourOfDayMinuteString.substring(2, 4));
    return hourOfDayMinute;
  }

  public static String getReminderHourOfDayMinuteString(int hourOfDay, int minute) {
    StringBuilder b = new StringBuilder();
    if (hourOfDay < 10) {
      b.append("0");
    }
    b.append(hourOfDay);

    if (minute < 10) {
      b.append("0");
    }
    b.append(minute);
    return b.toString();
  }

  public static String getReminderText(String hourOfDayMinuteString) {
    int[] hourOfDayMinute = getReminderHourOfDayMinuteFromString(hourOfDayMinuteString);
    int hourOfDay = hourOfDayMinute[0];
    int minute = hourOfDayMinute[1];

    Calendar calendar = Calendar.getInstance();
    calendar.set(HOUR_OF_DAY, hourOfDay);
    calendar.set(MINUTE, minute);

    StringBuilder b = new StringBuilder();
    int hour = calendar.get(HOUR);
    b.append(hour == 0 ? "12" : hour);

    if (minute > 0) {
      b.append(":");
      if (minute < 10) {
        b.append("0");
      }
      b.append(minute);
    }

    String amPmString = Preconditions.checkNotNull(calendar.getDisplayName(
        AM_PM,
        SHORT,
        Locale.getDefault()));
    b.append(" ");
    b.append(amPmString.toLowerCase());
    return b.toString();
  }

  /**
   * param: dayOfWeek 1-7
   * to: -2 mod NUM_DAYS
   * so it goes from monday -> sunday.
   * 0-indexed
   */
  public static int normalizeDayOfWeek(int dayOfWeek) {
    final int NUM_DAYS = 7;
    return (int) Etil.betterMod(dayOfWeek - 2, NUM_DAYS);
  }
}
