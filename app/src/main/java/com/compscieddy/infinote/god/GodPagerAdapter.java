package com.compscieddy.infinote.god;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class GodPagerAdapter extends FragmentPagerAdapter {

  public static final int SETTINGS_POSITION = 0;
  public static final int HOME_POSITION = 1;
  public static final int DISCOVER_POSITION = 2;

  public static final int DEFAULT_GOD_POSITION = HOME_POSITION;

  private final Context mContext;

  public GodPagerAdapter(Context context, FragmentManager fragmentManager) {
    super(fragmentManager);
    mContext = context;
  }

  @NonNull
  @Override
  public Fragment getItem(int position) {
    switch (position) {
      case SETTINGS_POSITION:
        return new SettingsGodFragment();
      case HOME_POSITION:
        return new MainGodFragment();
      case DISCOVER_POSITION:
      default:
        return new DiscoverGodFragment();
    }
  }

  @Override
  public int getCount() {
    return 3;
  }
}
