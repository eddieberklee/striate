package com.compscieddy.striate.model;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import timber.log.Timber;

public class HashtagDragSection {

  private String mId;
  private List<Note> mNotes;
  private @ColorInt int mColor;


  public HashtagDragSection() {
    // leaving empty for firebase
  }

  public HashtagDragSection(List<Note> notes, @ColorInt int color) {
    mId = getHashtagDragSection().push().getKey();
    mNotes = notes;
    mColor = color;
  }

  public static DatabaseReference getHashtagDragSection() {
    return FirebaseDatabase.getInstance()
        .getReference()
        .child("hashtag_drag_sections");
  }

  public String getId() {
    return mId;
  }

  public void setId(String id) {
    mId = id;
  }

  public List<Note> getNotes() {
    return mNotes;
  }

  public void setNotes(List<Note> notes) {
    mNotes = notes;
  }

  public int getColor() {
    return mColor;
  }

  public void setColor(int color) {
    mColor = color;
  }

  public void saveOnFirebaseRealtimeDatabase() {
    getHashtagDragSection()
        .child(getId())
        .setValue(HashtagDragSection.this)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
          @Override public void onSuccess(Void aVoid) {
            Timber.d("Successfully saved HashtagDragSection with id %s", getId());
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override public void onFailure(@NonNull Exception e) {
            Timber.d("Failed to save HashtagDragSection with id %s", getId());
          }
        });
  }
}
