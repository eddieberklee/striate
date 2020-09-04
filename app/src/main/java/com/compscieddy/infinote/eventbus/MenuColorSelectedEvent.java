package com.compscieddy.infinote.eventbus;

@SuppressWarnings("WeakerAccess")
public class MenuColorSelectedEvent {

  public int selectedColor;

  public MenuColorSelectedEvent(int selectedColor) {
    this.selectedColor = selectedColor;
  }
}
