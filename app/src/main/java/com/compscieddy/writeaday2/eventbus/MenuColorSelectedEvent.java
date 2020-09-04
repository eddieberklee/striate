package com.compscieddy.writeaday2.eventbus;

@SuppressWarnings("WeakerAccess")
public class MenuColorSelectedEvent {

  public int selectedColor;

  public MenuColorSelectedEvent(int selectedColor) {
    this.selectedColor = selectedColor;
  }
}
