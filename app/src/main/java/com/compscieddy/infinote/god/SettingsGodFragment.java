package com.compscieddy.infinote.god;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.compscieddy.infinote.ActivityHelper;
import com.compscieddy.infinote.AuthenticationActivity;
import com.compscieddy.infinote.databinding.SettingsGodFragmentBinding;
import com.google.common.base.Preconditions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.instabug.bug.BugReporting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsGodFragment extends Fragment {

  private SettingsGodFragmentBinding binding;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    binding = SettingsGodFragmentBinding.inflate(inflater, container, false);
    initUserDetails();
    return binding.getRoot();
  }

  @Override
  public void onResume() {
    super.onResume();
    attachListeners();
  }

  @Override
  public void onPause() {
    super.onPause();
    detachListeners();
  }

  private void initUserDetails() {
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    Preconditions.checkNotNull(currentUser);

    binding.userName.setText(String.format("Hey, %s.", currentUser.getDisplayName()));

    Uri photoUri = currentUser.getPhotoUrl();
    Glide
        .with(SettingsGodFragment.this)
        .load(photoUri)
        .centerCrop()
        .into(binding.userProfilePicture);
  }

  private void attachListeners() {
    binding.logoutButton.setOnClickListener(view -> {
      FirebaseAuth.getInstance().signOut();
      ActivityHelper.launchActivityAndFinish(getActivity(), AuthenticationActivity.class);
    });
    binding.giveFeedbackRoundedFakeComposer.setOnClickListener(v -> BugReporting.show(BugReporting.ReportType.FEEDBACK));
    binding.reportAProblem.setOnClickListener(v -> BugReporting.show(BugReporting.ReportType.BUG));
    binding.reviewTheApp.setOnClickListener(v -> {
      final String appPackageName = Preconditions.checkNotNull(getActivity()).getPackageName();
      // getPackageName() from Context or Activity object
      try {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("market://details?id" +
            "=%s", appPackageName))));
      } catch (android.content.ActivityNotFoundException anfe) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://play.google" +
            ".com/store/apps/details?id=%s", appPackageName))));
      }
    });
  }

  private void detachListeners() {
    binding.logoutButton.setOnClickListener(null);
    binding.giveFeedbackRoundedFakeComposer.setOnClickListener(null);
    binding.reportAProblem.setOnClickListener(null);
    binding.reviewTheApp.setOnClickListener(null);
  }
}
