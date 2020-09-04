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
import com.google.firebase.firestore.Exclude;

import androidx.annotation.NonNull;
import timber.log.Timber;

public class Hashtag {

  public interface HashtagCallback {
    public void onHashtag(Hashtag hashtag);
  }

  private String mId;
  private long mCreatedAtMillis;
  private String mHashtagText;

  public Hashtag() {
    // leaving empty for firebase
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
    ValueEventListener hashtagEventListener = new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Hashtag hashtag = snapshot.getValue(Hashtag.class);
        hashtagCallback.onHashtag(hashtag);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Timber.e("Hashtag was cancelled: %s", error.toString());
      }
    };
    getHashtagReference()
        .child(hashtagId)
        .addValueEventListener(hashtagEventListener);
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

  public String getHashtagText() {
    return mHashtagText;
  }

  public void setHashtagText(String hashtagText) {
    mHashtagText = hashtagText;
  }
}
