package com.compscieddy.infinote.util;

import android.app.Activity;

import com.compscieddy.infinote.ActivityHelper;
import com.compscieddy.infinote.AuthenticationActivity;
import com.google.common.base.Preconditions;
import com.google.firebase.auth.FirebaseAuth;

public class AuthenticationUtil {

  public static boolean isLoggedOut() {
    return FirebaseAuth.getInstance().getCurrentUser() == null;
  }

  public static void handleLoggedOutUser(Activity activity) {
    ActivityHelper.launchActivityAndFinish(activity, AuthenticationActivity.class);
  }

  public static String getUserEmail() {
    return Preconditions.checkNotNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
  }

}
