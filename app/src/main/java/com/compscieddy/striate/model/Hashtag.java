package com.compscieddy.striate.model;

import com.compscieddy.striate.util.CrashUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Exclude;

import androidx.annotation.NonNull;
import timber.log.Timber;

public class Hashtag {

  public interface HashtagCallback {
    public void onHashtag(Hashtag hashtag);
  }

  private static final String FIELD_ID = "id";
  private static final String FIELD_CREATED_AT_MILLIS = "createdAtMillis";
  private static final String FIELD_HASHTAG_NAME = "hashtagName";
  private static final String FIELD_HASHTAG_COLOR = "hashtagColor";

  private String mId;
  private long mCreatedAtMillis;
  private String mHashtagName;
  private int mHashtagColor = -1;

  public Hashtag() {
    // leaving empty for firebase
  }

  public Hashtag(String hashtagName) {
    mId = getHashtagReference().push().getKey();
    mHashtagName = hashtagName;
    mCreatedAtMillis = System.currentTimeMillis();
  }

  @Exclude
  public static String generateNoteId() {
    return getHashtagReference()
        .push()
        .getKey();
  }

  @Exclude
  public static DatabaseReference getHashtagReference() {
    return FirebaseDatabase.getInstance()
        .getReference()
        .child("hashtags");
  }

  @Exclude
  public static Query getHashtagQuery() {
    return getHashtagReference()
        .orderByChild(Note.FIELD_CREATED_AT_MILLIS);
  }

  @Exclude
  public static void getHashtag(String hashtagId, HashtagCallback hashtagCallback) {
    getHashtagReference()
        .child(hashtagId)
        .addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            Hashtag hashtag = snapshot.getValue(Hashtag.class);
            hashtagCallback.onHashtag(hashtag);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {
            Timber.e("Hashtag was cancelled: %s", error.toString());
          }
        });
  }

  @Exclude
  public void saveOnFirebaseRealtimeDatabase() {
    getHashtagReference()
        .child(getId())
        .setValue(Hashtag.this);
  }

  @Exclude
  public void saveFieldOnFirestore(String field, Object value) {
    getHashtagReference()
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
    getHashtagReference()
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

  public String getId() {
    return mId;
  }

  public void setId(String id) {
    mId = id;
  }

  public long getCreatedAtMillis() {
    return mCreatedAtMillis;
  }

  public void setCreatedAtMillis(long createdAtMillis) {
    mCreatedAtMillis = createdAtMillis;
  }

  public String getHashtagName() {
    return mHashtagName;
  }

  public void setHashtagName(String hashtagName) {
    mHashtagName = hashtagName;
  }

  public int getHashtagColor() {
    return mHashtagColor;
  }

  public void setHashtagColor(int hashtagColor) {
    mHashtagColor = hashtagColor;
  }
}
