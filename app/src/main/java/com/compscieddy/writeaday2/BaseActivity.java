package com.compscieddy.writeaday2;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.compscieddy.writeaday2.util.AuthenticationUtil;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

@SuppressWarnings("DanglingJavadoc")
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

  private static final String PREF_SELECTED_THEME = "pref_selected_theme";
  private static final int THEME_DAY_MODE = 1;
  private static final int THEME_NIGHT_MODE = 2;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    initTheme();
    super.onCreate(savedInstanceState);
    if (AuthenticationUtil.isLoggedOut()) {
      AuthenticationUtil.handleLoggedOutUser(this);
      return;
    }
  }

  private void initTheme() {
    int selectedTheme = Writeaday2Application.getSharedPreferences()
        .getInt(PREF_SELECTED_THEME, THEME_DAY_MODE);
    if (selectedTheme == THEME_DAY_MODE) {
      setTheme(R.style.DayMode);
    } else { /** {@link THEME_NIGHT_MODE} */
      setTheme(R.style.NightMode);
    }
  }
}
