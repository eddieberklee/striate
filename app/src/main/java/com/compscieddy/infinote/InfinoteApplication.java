package com.compscieddy.infinote;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.amplitude.api.Amplitude;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.instabug.library.Instabug;
import com.instabug.library.invocation.InstabugInvocationEvent;
import com.instabug.library.visualusersteps.State;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class InfinoteApplication extends Application {

  public static InfinoteApplication sApplicationContext;

  public static SharedPreferences getSharedPreferences() {
    return sApplicationContext.getSharedPreferences("infinote", Context.MODE_PRIVATE);
  }

  public static String getSharedPreferencesString(String key) {
    return getSharedPreferences().getString(key, "");
  }

  public static boolean getSharedPreferencesBoolean(String key) {
    return getSharedPreferences().getBoolean(key, false);
  }

  public static void setSharedPreferencesBoolean(String key, boolean booleanValue) {
    SharedPreferences.Editor editor = getSharedPreferences().edit();
    editor.putBoolean(key, booleanValue);
    editor.apply();
  }

  public static int getSharedPreferencesInt(String key) {
    return getSharedPreferences().getInt(key, -1);
  }

  public static void setSharedPreferencesInt(String key, int intValue) {
    SharedPreferences.Editor editor = getSharedPreferences().edit();
    editor.putInt(key, intValue);
    editor.apply();
  }

  public static long getSharedPreferencesLong(String key) {
    return getSharedPreferences().getLong(key, -1L);
  }

  public static void setSharedPreferencesLong(String key, long longValue) {
    SharedPreferences.Editor editor = getSharedPreferences().edit();
    editor.putLong(key, longValue);
    editor.apply();
  }

  public static void setSharedPreferencesString(String key, String stringValue) {
    SharedPreferences.Editor editor = getSharedPreferences().edit();
    editor.putString(key, stringValue);
    editor.apply();
  }

  @Override
  public void onCreate() {
    super.onCreate();

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree() {
        @Override
        protected void log(int priority, String tag, @NotNull String message, Throwable t) {
          super.log(priority, "Timber " + tag, message, t);
        }
      });
    }

    sApplicationContext = InfinoteApplication.this;

    initEmojis();
    initInstabug();
    initAmplitude();
    initFacebookAds();
  }

  private void initFacebookAds() {
    AudienceNetworkAds.initialize(this);
    AdSettings.addTestDevice("f2392b05-9d10-4562-9369-0d5ddff9aee3");
  }

  private void initAmplitude() {
    Amplitude
        .getInstance()
        .initialize(this, "21613220870bd7db5277118041de3160")
        .enableForegroundTracking(sApplicationContext);
  }

  private void initInstabug() {
    new Instabug.Builder(this, "1bd81a87ad20a9fe7dae26915649576f")
        .setInvocationEvents(InstabugInvocationEvent.SHAKE)
        .setReproStepsState(State.ENABLED)
        .build();
  }

  private void initEmojis() {
    EmojiManager.install(new IosEmojiProvider());
  }
}
