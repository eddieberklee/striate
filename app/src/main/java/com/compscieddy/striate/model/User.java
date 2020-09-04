package com.compscieddy.striate.model;

import android.content.Context;
import android.content.Intent;

import com.compscieddy.striate.AuthenticationActivity;
import com.compscieddy.striate.util.AuthenticationUtil;
import com.compscieddy.striate.util.CrashUtil;
import com.google.common.base.Preconditions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

@SuppressWarnings("WeakerAccess")
public class User {

  public static final String USER_COLLECTION = "user";

  private String mEmail;
  private String mDisplayName;
  private String mPhotoUrl;

  public User() {}

  public User(FirebaseUser firebaseUser) {
    this(
        firebaseUser.getEmail(),
        firebaseUser.getDisplayName(),
        firebaseUser.getPhotoUrl() == null ? "" : firebaseUser.getPhotoUrl().toString());
  }

  public User(String email, String displayName, String photoUrl) {
    mEmail = email;
    mDisplayName = displayName;
    mPhotoUrl = photoUrl;
  }

  @Exclude
  public static DocumentReference getUserReference() {
    return FirebaseFirestore.getInstance().collection(USER_COLLECTION)
        .document(AuthenticationUtil.getUserEmail());
  }

  @Exclude
  public static Query getUsersQuery() {
    return FirebaseFirestore.getInstance().collection(USER_COLLECTION);
  }

  @Exclude
  public static String getEmail(Context context) {
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    String email = Preconditions.checkNotNull(firebaseAuth.getCurrentUser()).getEmail();
    if (email == null) {
      Intent intent = new Intent(context, AuthenticationActivity.class);
      context.startActivity(intent);
    }
    return email;
  }

  @Exclude
  public void saveUserToFirestore(final Runnable onSuccessRunnable) {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    db.collection(USER_COLLECTION).document(getEmail()).set(User.this, SetOptions.merge())
        .addOnSuccessListener(aVoid -> onSuccessRunnable.run())
        .addOnFailureListener(e -> CrashUtil.logAndShowToast("Failed to set the user in AuthenticationActivity e: " + e.toString()));
  }

  public String getEmail() {
    return mEmail;
  }
  public void setEmail(String email) {
    mEmail = email;
  }

  public String getDisplayName() {
    return mDisplayName;
  }
  public void setDisplayName(String displayName) {
    mDisplayName = displayName;
  }

  public String getPhotoUrl() {
    return mPhotoUrl;
  }
  public void setPhotoUrl(String photoUrl) {
    mPhotoUrl = photoUrl;
  }
}
