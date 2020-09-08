package com.compscieddy.striate;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.compscieddy.eddie_utils.etil.KeyboardEtil;
import com.compscieddy.eddie_utils.etil.NotificationEtil;
import com.compscieddy.eddie_utils.etil.OnboardingEtil;
import com.compscieddy.eddie_utils.eui.ColorImageView;
import com.compscieddy.striate.databinding.MainActivityBinding;
import com.compscieddy.striate.god.GodPagerAdapter;
import com.smartlook.sdk.smartlook.Smartlook;

import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.compscieddy.striate.Analytics.MAIN_ACTIVITY_OPEN;
import static com.compscieddy.striate.god.GodPagerAdapter.DEFAULT_GOD_POSITION;
import static com.compscieddy.striate.god.GodPagerAdapter.INFINOTE_POSITION;
import static com.compscieddy.striate.god.GodPagerAdapter.SETTINGS_POSITION;

public class MainActivity extends BaseActivity {

  @BindView(R.id.god_view_pager) ViewPager mGodViewPager;

  @BindView(R.id.settings_god_fragment_icon) ColorImageView mSettingsGodFragmentIcon;
  @BindView(R.id.discover_god_fragment_icon) ColorImageView mInfinoteFragmentIcon;

  @BindView(R.id.settings_god_fragment_title) TextView mSettingsGodFragmentTitle;
  @BindView(R.id.discover_god_fragment_title) TextView mInfinoteGodFragmentTitle;

  @BindView(R.id.settings_navbar_container) View mSettingsNavbarContainer;
  @BindView(R.id.discover_navbar_container) View mDiscoverNavbarContainer;

  private OnPageChangeListener mGodPageChangeListener;
  private MainActivityBinding binding;

  public static void setGodFragmentButtonSelectedState(
      boolean isSelected,
      ColorImageView godFragmentIcon,
      TextView godFragmentTitle) {
    final float SELECTED_ALPHA = 1.0f;
    final float UNSELECTED_ALPHA = 0.3f;

    final float SELECTED_SCALE = 1.3f;
    final float UNSELECTED_SCALE = 0.9f;

    godFragmentIcon.animate()
        .setDuration(400)
        .alpha(isSelected ? SELECTED_ALPHA : UNSELECTED_ALPHA)
        .scaleX(isSelected ? SELECTED_SCALE : UNSELECTED_SCALE)
        .scaleY(isSelected ? SELECTED_SCALE : UNSELECTED_SCALE);
    godFragmentTitle.animate()
        .setDuration(400)
        .alpha(isSelected ? SELECTED_ALPHA : UNSELECTED_ALPHA);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = MainActivityBinding.inflate(getLayoutInflater(), null, false);
    setContentView(binding.getRoot());

    ButterKnife.bind(MainActivity.this);

    init();
    initNotification();

    Analytics.track(MAIN_ACTIVITY_OPEN);
  }

  private void initNotification() {
    // At 12pm remind people of a good afternoon to remind them to work on their habits.
    int nineAmHour = 9;
    int nineAmMinute = 0;
    NotificationEtil.scheduleDailyNotificationPreventingDuplicates(
        MainActivity.this,
        MainActivity.class,
        R.drawable.small_icon_colored,
        DailyNotificationPublisher.class,
        DailyNotificationPublisher.EXTRA_NOTIFICATION_ID,
        DailyNotificationPublisher.EXTRA_NOTIFICATION,
        "Daily Reminders",
        "Good morning! Pick a healthy habit like...",
        nineAmHour,
        nineAmMinute);
    // At 12pm remind people of a good afternoon to remind them to work on their habits.
    int twelvePmHour = 12;
    int twelvePmMinute = 0;
    NotificationEtil.scheduleDailyNotificationPreventingDuplicates(
        MainActivity.this,
        MainActivity.class,
        R.drawable.small_icon_colored,
        DailyNotificationPublisher.class,
        DailyNotificationPublisher.EXTRA_NOTIFICATION_ID,
        DailyNotificationPublisher.EXTRA_NOTIFICATION,
        "Daily Reminders",
        "Good afternoon! Pick a productive habit like...",
        twelvePmHour,
        twelvePmMinute);
    // At 7pm remind people of a good evening to remind them to work on their habits.
    int sevenPmHour = 19;
    int sevenPmMinute = 0;
    NotificationEtil.scheduleDailyNotificationPreventingDuplicates(
        MainActivity.this,
        MainActivity.class,
        R.drawable.small_icon_colored,
        DailyNotificationPublisher.class,
        DailyNotificationPublisher.EXTRA_NOTIFICATION_ID,
        DailyNotificationPublisher.EXTRA_NOTIFICATION,
        "Daily Reminders",
        "Good evening! Pick a night-time habit like...",
        sevenPmHour,
        sevenPmMinute);
    // At 10pm remind people of a good night to remind them to check off their habits.
    int twentyTwoHour = 22;
    int twentyTwoMinute = 0;
    NotificationEtil.scheduleDailyNotificationPreventingDuplicates(
        MainActivity.this,
        MainActivity.class,
        R.drawable.small_icon_colored,
        DailyNotificationPublisher.class,
        DailyNotificationPublisher.EXTRA_NOTIFICATION_ID,
        DailyNotificationPublisher.EXTRA_NOTIFICATION,
        "Daily Reminders",
        "Good night! Plan a healthy habit for tomorrow like...",
        twentyTwoHour,
        twentyTwoMinute);
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (!BuildConfig.DEBUG) {
      Smartlook.setupAndStartRecording("d173a7fe0f83e2e23da89f776c1c235d7e14d2b0", 10);
    }
    OnboardingEtil.showSequence(
        MainActivity.this,
        StriateApplication.getSharedPreferences(),
        "first_time_greeting",
        new int[] {
            R.string.onboarding_day_1_title,
            R.string.onboarding_day_2_title,
            R.string.onboarding_day_3_title,
            R.string.onboarding_day_4_title,
            R.string.onboarding_day_5_title,
            },
        new int[] {
            R.string.onboarding_day_1_description,
            R.string.onboarding_day_2_description,
            R.string.onboarding_day_3_description,
            R.string.onboarding_day_4_description,
            R.string.onboarding_day_5_description,
            });
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (!BuildConfig.DEBUG) {
      Smartlook.stopRecording();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    attachListeners();
  }

  @Override
  protected void onPause() {
    super.onPause();
    detachListeners();
  }

  private void init() {
    mGodViewPager.setAdapter(new GodPagerAdapter(MainActivity.this, getSupportFragmentManager()));
    mGodViewPager.setCurrentItem(INFINOTE_POSITION);
    setGodFragmentButtonsUnselected();
    new KeyboardEtil().addKeyboardStateListener(
        binding.getRoot(),
        new KeyboardEtil.KeyboardStateListener() {
          @Override
          public void onKeyboardChanged(boolean isKeyboardShowing) {
            binding.godFragmentsButtonRow.setVisibility(
                isKeyboardShowing ? View.GONE : View.VISIBLE);
          }
        });
  }

  private void attachListeners() {
    mSettingsNavbarContainer.setOnClickListener(view -> mGodViewPager.setCurrentItem(
        SETTINGS_POSITION));
    mDiscoverNavbarContainer.setOnClickListener(view -> mGodViewPager.setCurrentItem(
        INFINOTE_POSITION));

    mGodPageChangeListener = new OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      }

      @Override
      public void onPageSelected(int position) {
        setGodFragmentButtonsUnselected();

        // Highlight current god fragment, do nothing if on main screen
        if (position == GodPagerAdapter.SETTINGS_POSITION) {
          setGodFragmentButtonSelectedState(true, mSettingsGodFragmentIcon,
              mSettingsGodFragmentTitle);
        } else { // INFINOTE_POSITION
          setGodFragmentButtonSelectedState(true, mInfinoteFragmentIcon,
              mInfinoteGodFragmentTitle);
        }
      }

      @Override
      public void onPageScrollStateChanged(int state) {
      }
    };

    mGodViewPager.addOnPageChangeListener(mGodPageChangeListener);
    mGodPageChangeListener.onPageSelected(DEFAULT_GOD_POSITION);
  }

  private void detachListeners() {
    mSettingsNavbarContainer.setOnClickListener(null);
    mDiscoverNavbarContainer.setOnClickListener(null);

    mGodViewPager.removeOnPageChangeListener(mGodPageChangeListener);
  }

  private void setGodFragmentButtonsUnselected() {
    setGodFragmentButtonSelectedState(false, mSettingsGodFragmentIcon,
        mSettingsGodFragmentTitle);
    setGodFragmentButtonSelectedState(false, mInfinoteFragmentIcon,
        mInfinoteGodFragmentTitle);
  }
}
