package com.compscieddy.infinote;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;

public class Colors {

  public static @ColorRes int[] colorResIds = new int[] {
      R.color.infinote_red,
      R.color.infinote_orange,
      R.color.infinote_yellow,
      R.color.infinote_green,
      R.color.infinote_teal,
      R.color.infinote_blue,
      R.color.infinote_purple,
      R.color.infinote_pink,
      R.color.infinote_light_grey,
      R.color.infinote_dark_grey,
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
