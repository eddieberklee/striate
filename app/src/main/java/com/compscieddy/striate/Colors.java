package com.compscieddy.striate;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;

public class Colors {

  public static @ColorRes int[] colorResIds = new int[] {
      R.color.striate_red,
      R.color.striate_orange,
      R.color.striate_yellow,
      R.color.striate_green,
      R.color.striate_teal,
      R.color.striate_blue,
      R.color.striate_purple,
      R.color.striate_pink,
      R.color.striate_light_grey,
      R.color.striate_dark_grey,
  };

  public static @ColorInt int[] getColorInts(Context c) {
    @ColorInt int[] colorInts = new int[Colors.colorResIds.length];
    for (int i = 0; i < colorInts.length; i++) {
      colorInts[i] = c.getResources().getColor(Colors.colorResIds[i]);
    }
    return colorInts;
  }

  public static @ColorInt int getColorForPosition(Context c, int position) {
    return getColorInts(c)[position];
  }

}
