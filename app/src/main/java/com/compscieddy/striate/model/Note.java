package com.compscieddy.striate.model;

import android.text.TextUtils;

import com.compscieddy.striate.util.CrashUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.Exclude;

import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class Note {

  public static final String FIELD_ID = "id";
  public static final String FIELD_USER_EMAIL = "userEmail";
  public static final String FIELD_CREATED_AT_MILLIS = "createdAtMillis";
  public static final String FIELD_NOTE_TEXT = "noteText";
  public static final String FIELD_HASHTAG_ID = "hashtagId";

  private static final String NOTE_COLLECTION = "note";

  private String mId;
  private String mUserEmail;
  private long mCreatedAtMillis;
  private String mNoteText;
  private String mHashtagId;

  public Note() {
    // for firebase
  }

  @Exclude
  public static String generateNoteId() {
    return getNoteReference()
        .push()
        .getKey();
  }

  @Exclude
  public static DatabaseReference getNoteReference() {
    return FirebaseDatabase.getInstance()
        .getReference()
        .child("notes");
  }

  @Exclude
  public static Query getNoteQuery() {
    return getNoteReference()
        .orderByChild(Note.FIELD_CREATED_AT_MILLIS);
  }

  @Exclude
  public void saveOnFirestore() {
    saveOnFirestore(null);
  }

  @Exclude
  public void saveOnFirestore(@Nullable final Runnable onSuccessRunnable) {
    if (onSuccessRunnable != null) {
      onSuccessRunnable.run();
    }
//    CrashUtil.log("Failed to save entry with id: " + getId());
  }

  /**
   * This is useful in cases where the Entry class doesn't have all the updated fields. This
   * prevents saving to Firestore with weird values.
   */
  public void saveFieldOnFirestore(String field, Object value) {
    getNoteReference()
        .child(field)
        .setValue(value)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
          @Override
          public void onSuccess(Void aVoid) {
            // success
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            CrashUtil.log("Failed to save field " + field + " with value " + value);
          }
        });
  }

  @Exclude
  public void deleteOnFirebase() {
    getNoteReference()
        .child(getId())
        .removeValue()
        .addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
            if (CrashUtil.didHandleFirestoreException(task.getException())) {
              return;
            }
            if (task.isSuccessful()) {
              Timber.d("Successfully deleted entry id: %s", getId());
            }
          }
        });
  }

  /**
   * Ex: Aug 4 (if it's this year)
   * Aug 4, 2019 (if it's not this year)
   */
  @Exclude
  public String getReadableDateString() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(getCreatedAtMillis());
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(calendar.getDisplayName(
        Calendar.MONTH,
        Calendar.SHORT,
        Locale.getDefault()));
    stringBuilder.append(" ");
    stringBuilder.append(calendar.get(Calendar.DAY_OF_MONTH));

    Calendar todayCalendar = Calendar.getInstance();
    boolean isCurrentYear = todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR);
    if (!isCurrentYear) {
      stringBuilder.append(",");
      stringBuilder.append(calendar.get(Calendar.YEAR));
    }
    return stringBuilder.toString();
  }

  @Exclude
  public boolean hasHashtag() {
    return !TextUtils.isEmpty(getHashtagId());
  }

  /**
   * Getters and Setters
   */

  public String getId() {
    return mId;
  }

  public void setId(String id) {
    mId = id;
  }

  public String getUserEmail() {
    return mUserEmail;
  }

  public void setUserEmail(String userEmail) {
    mUserEmail = userEmail;
  }

  public long getCreatedAtMillis() {
    return mCreatedAtMillis;
  }

  public void setCreatedAtMillis(long createdAtMillis) {
    mCreatedAtMillis = createdAtMillis;
  }

  public String getNoteText() {
    return mNoteText;
  }

  public void setNoteText(String noteText) {
    mNoteText = noteText;
  }

  public String getHashtagId() {
    return mHashtagId;
  }

  public void setHashtagId(String hashtagId) {
    mHashtagId = hashtagId;
  }
}
