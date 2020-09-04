package com.compscieddy.writeaday2;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;

public class Colors {

  public static @ColorRes int[] colorResIds = new int[] {
      R.color.writeaday2_red,
      R.color.writeaday2_orange,
      R.color.writeaday2_yellow,
      R.color.writeaday2_green,
      R.color.writeaday2_teal,
      R.color.writeaday2_blue,
      R.color.writeaday2_purple,
      R.color.writeaday2_pink,
      R.color.writeaday2_light_grey,
      R.color.writeaday2_dark_grey,
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
