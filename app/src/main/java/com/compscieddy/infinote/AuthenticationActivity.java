package com.compscieddy.infinote;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.TextView;

import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.infinote.model.User;
import com.compscieddy.infinote.util.CrashUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.common.base.Preconditions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.animation.PathInterpolatorCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.compscieddy.infinote.Analytics.AUTHENTICATION_BUTTON;
import static com.compscieddy.infinote.PreferenceConstants.FIRST_LOGIN_MILLIS;

@SuppressWarnings("deprecation")
public class AuthenticationActivity extends AppCompatActivity {

  public static final int REQUEST_CODE_SIGN_IN = 101;
  private static final int REQUEST_CODE_RESOLVE_CONNECTION = 102;
  private static final Interpolator FADE_OUT_TEXT_INTERPOLATOR = PathInterpolatorCompat.create(
      0.420f,
      0.000f,
      1.000f,
      1.000f);
  private static final Interpolator FADE_IN_TEXT_INTERPOLATOR = PathInterpolatorCompat.create(
      0.000f,
      0.000f,
      0.580f,
      1.000f);
  private final static int CYCLE_ANSWERS_DELAY = 3500; // every 4 seconds
  @BindView(R.id.sign_in_button) SignInButton mGoogleLoginButton;
  @BindView(R.id.loading_screen) View mLoadingScreen;
  @BindView(R.id.want_to_text) TextView mWantToText;
  private FirebaseAuth mAuth;
  private FirebaseAuth.AuthStateListener mAuthStateListener;
  private GoogleApiClient mGoogleClient;
  private String[] mWantToAnswers;
  private Handler mHandler;
  private int lastRandomIndex = -1;
  private Runnable mCycleWantAnswersRunnable = new Runnable() {
    @Override
    public void run() {
      int randomIndex;
      do {
        randomIndex = (int) Math.round(Math.random() * (mWantToAnswers.length - 1));
      } while (randomIndex == lastRandomIndex);
      lastRandomIndex = randomIndex;

      final String randomAnswer = mWantToAnswers[randomIndex];
      mWantToText.animate()
          .alpha(0)
          .setDuration(1500)
          .setInterpolator(FADE_OUT_TEXT_INTERPOLATOR)
          .withEndAction(() -> {
            mWantToText.setText(randomAnswer);
            mWantToText.animate()
                .alpha(1)
                .setDuration(1500)
                .setInterpolator(FADE_IN_TEXT_INTERPOLATOR);
          });

      // infinite loop here cycling through answers
      mHandler.postDelayed(mCycleWantAnswersRunnable, CYCLE_ANSWERS_DELAY);
    }
  };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.authentication_activity);
    ButterKnife.bind(AuthenticationActivity.this);
    Analytics.track(Analytics.AUTHENTICATION_SCREEN);
    mHandler = new Handler(Looper.getMainLooper());

    init();
    initRotatingAnswers();
    setListeners();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mAuth.addAuthStateListener(mAuthStateListener);
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mAuthStateListener != null) {
      mAuth.removeAuthStateListener(mAuthStateListener);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Timber.d("onActivityResult()");

    switch (requestCode) {
      case REQUEST_CODE_RESOLVE_CONNECTION:
        CrashUtil.log("Request Code REQUEST_CODE_RESOLVE_CONNECTION");
        if (resultCode == RESULT_OK) {
          mGoogleClient.connect();
        }
        break;
      case REQUEST_CODE_SIGN_IN:
        showLoadingScreen();

        GoogleSignInResult result =
            Preconditions.checkNotNull(Auth.GoogleSignInApi.getSignInResultFromIntent(
                data));
        Timber.d("REQUEST_CODE_SIGN_IN status: " + result.getStatus() + " status code: " + result
            .getStatus()
            .getStatusCode());
        if (result.isSuccess()) {
          Timber.d("Google sign-in was successful");
          // Google Sign In was successful, authenticate with Firebase
          GoogleSignInAccount account = Preconditions.checkNotNull(result.getSignInAccount());
          firebaseAuthWithGoogle(account);
        } else if (result
            .getStatus()
            .getStatusCode() == 7) { // found out this magic constant through debugging
          Etil.showToast(this, "Not detecting a network connection.");
        } else {
          CrashUtil.log("Found a non-successful google api sign-in case. Status: " + result.getStatus() + " status code: " + result
              .getStatus()
              .getStatusCode());
        }
        break;
    }
  }

  private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
    Timber.d("firebaseAuthWithGoogle:" + acct.getId() + " /// " + acct.getIdToken());

    AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

    FirebaseAuth auth = FirebaseAuth.getInstance();
    auth.signInWithCredential(credential)
        .addOnCompleteListener(this, task -> {
          Timber.d("signInWithCredential:onComplete:%s", task.isSuccessful());

          // If sign in fails, display a message to the user. If sign in succeeds
          // the auth state listener will be notified and logic to handle the
          // signed in user can be handled in the listener.
          if (!task.isSuccessful()) {
            Timber.d("signInWithCredential %s", task.getException());
            CrashUtil.logAndShowToast("Authentication Failed - Please Retry");

            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(AuthenticationActivity.this::finish, 3000);
          }
        });
  }

  private void init() {
    mAuth = FirebaseAuth.getInstance();
    mAuthStateListener = this::handleSignInOrSignOut;

    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.firebase_web_client_id))
        .requestEmail()
        .build();

    mGoogleClient = new GoogleApiClient.Builder(this)
        .enableAutoManage(this, getOnConnectionFailedListener())
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        .build();
  }

  private void initRotatingAnswers() {
    mWantToAnswers = getResources().getStringArray(R.array.if_you_want_to_answers);
    mHandler.post(mCycleWantAnswersRunnable);
  }

  @NotNull
  private GoogleApiClient.OnConnectionFailedListener getOnConnectionFailedListener() {
    return connectionResult -> {
      CrashUtil.log(String.format("onConnectionFailed() %s", connectionResult));
      if (connectionResult.hasResolution()) {
        try {
          connectionResult.startResolutionForResult(
              AuthenticationActivity.this,
              REQUEST_CODE_RESOLVE_CONNECTION);
        } catch (IntentSender.SendIntentException e) {
          CrashUtil.log("Google connection could not be established for Google API Client");
        }
      }
    };
  }

  private void setListeners() {
    mGoogleLoginButton.setOnClickListener(v -> {
      launchLogin();
      Analytics.track(AUTHENTICATION_BUTTON);
    });
  }

  private void handleSignInOrSignOut(@NonNull FirebaseAuth firebaseAuth) {
    FirebaseUser user = firebaseAuth.getCurrentUser();
    if (user != null) {
      // User is signed in
      Timber.d("onAuthStateChanged:signed_in: %s", user.getUid());
      loginSuccess();
    } else {
      // User is signed out
      Timber.d("onAuthStateChanged:signed_out");
    }
  }

  private void launchLogin() {
    Timber.d("auth launchLogin");
    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleClient);
    startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
  }

  private void loginSuccess() {
    hideLoadingScreen();

    FirebaseUser firebaseUser = Preconditions.checkNotNull(mAuth.getCurrentUser());
    String name = firebaseUser.getDisplayName();

    Timber.d("Login has been done successfully! For: %s", firebaseUser.getEmail());
    Etil.showToast(AuthenticationActivity.this, "Hi, " + name + ".");

    saveUserToFirestoreThenForwardToMainActivity();
    saveFirstTimeLoginMillis();

    ActivityHelper.launchActivityAndFinish(AuthenticationActivity.this, MainActivity.class);
  }

  private void saveUserToFirestoreThenForwardToMainActivity() {
    @Nullable FirebaseUser currentUser = mAuth.getCurrentUser();
    if (currentUser == null) {
      Etil.showToast(this, "Couldn't login. Close the app and try again.");
      return;
    }
    User user = new User(currentUser);
    user.saveUserToFirestore(() -> {
      boolean isAlreadySignedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
      if (isAlreadySignedIn) {
        Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
      }
    });
  }

  private void saveFirstTimeLoginMillis() {
    long firstLoginMillis = InfinoteApplication.getSharedPreferencesLong(FIRST_LOGIN_MILLIS);
    if (firstLoginMillis == -1L) {
      InfinoteApplication.setSharedPreferencesLong(
          FIRST_LOGIN_MILLIS,
          System.currentTimeMillis());
    }
  }

  private void showLoadingScreen() {
    mLoadingScreen.setVisibility(VISIBLE);
  }

  private void hideLoadingScreen() {
    mLoadingScreen.setVisibility(GONE);
  }

}
