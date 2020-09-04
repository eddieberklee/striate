package com.compscieddy.striate;

import com.compscieddy.eddie_utils.etil.ColorEtil;

import androidx.annotation.ColorInt;

@SuppressWarnings("WeakerAccess")
public class ConfettiEvent {

  public final @ColorInt int confettiColor;
  public float x;
  public float y;

  public ConfettiEvent(@ColorInt int confettiColor, float x, float y) {
    this.confettiColor = confettiColor;
    this.x = x;
    this.y = y;
  }

  public @ColorInt int getConfettiColor() {
    return confettiColor;
  }

  public @ColorInt int getLightConfettiColor() {
    return ColorEtil.applyValueMultiplier(confettiColor, 1.1f);
  }

  public @ColorInt int getDarkConfettiColor() {
    return ColorEtil.applyValueMultiplier(confettiColor, 0.9f);
  }

}
