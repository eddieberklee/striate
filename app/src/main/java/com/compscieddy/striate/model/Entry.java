package com.compscieddy.striate.model;

import android.content.Context;

import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.striate.R;
import com.compscieddy.striate.StriateApplication;
import com.compscieddy.striate.util.AuthenticationUtil;
import com.compscieddy.striate.util.CrashUtil;
import com.compscieddy.striate.util.DateUtil;
import com.compscieddy.striate.util.FirestoreUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

import static com.compscieddy.striate.model.User.USER_COLLECTION;
import static com.google.firebase.firestore.Query.Direction.ASCENDING;
import static com.google.firebase.firestore.Query.Direction.DESCENDING;

@SuppressWarnings("ALL")
public class Entry {

  public static final String FIELD_ID = "id";
  public static final String FIELD_USER_EMAIL = "userEmail";
  public static final String FIELD_CREATED_AT_MILLIS = "createdAtMillis";
  public static final String FIELD_YEAR_MONTH_DAY = "yearMonthDay";
  public static final String FIELD_IS_NEW_ENTRY = "isNewEntry";
  public static final String FIELD_COLOR = "color";
  public static final String FIELD_URI_STRING = "uriString";
  public static final String FIELD_IS_PHOTO = "isPhoto";
  public static final String FIELD_PHOTO_CAPTION_TEXT = "photoCaptionText";
  public static final String FIELD_HASHTAGS = "hashtags";

  // Firebase Storage folder name for photos
  private static final String PHOTO_FOLDER = "images";

  private static final String ENTRY_COLLECTION = "entry";

  private String mId;
  private String mUserEmail;
  private long mCreatedAtMillis;
  private String mEntryText;
  private String mYearMonthDay;
  private boolean mIsNewEntry;
  private @ColorInt int mColor;
  private String mUriString;
  private boolean mIsPhoto;
  private String mPhotoId;
  private String mPhotoCaptionText;
  private List<String> mHashtags;

  public Entry() {
    // for firebase
  }

  public Entry(String yearMonthDay) {
    this(false, "", "", yearMonthDay);
  }

  public Entry(boolean isPhoto, String photoId, String uriString, String yearMonthDay) {
    mIsPhoto = isPhoto;
    mPhotoId = photoId;
    mEntryText = "";
    mUriString = uriString;
    mYearMonthDay = yearMonthDay;
    mId = FirestoreUtil.generateId(getEntrySubcollection());
    mUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    mCreatedAtMillis = System.currentTimeMillis();
    mIsNewEntry = false;
  }

  @Exclude
  public static CollectionReference getEntrySubcollection() {
    return getEntrySubcollection(AuthenticationUtil.getUserEmail());
  }

  @Exclude
  public static CollectionReference getEntrySubcollection(String userEmail) {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    return db
        .collection(USER_COLLECTION)
        .document(userEmail)
        .collection(ENTRY_COLLECTION);
  }

  @Exclude
  public static Query getEntryQuery(String userEmail, String yearMonthDay) {
    return getEntrySubcollection(userEmail)
        .whereEqualTo(FIELD_USER_EMAIL, userEmail)
        .whereEqualTo(FIELD_YEAR_MONTH_DAY, yearMonthDay)
        .orderBy(FIELD_CREATED_AT_MILLIS, ASCENDING);
  }

  @Exclude
  public static StorageReference generateNewPhotoStorageReference(String id) {
    return FirebaseStorage.getInstance()
        .getReference(AuthenticationUtil.getUserEmail()
            + "/" + PHOTO_FOLDER + "/" + id);
  }

  @Exclude
  public static Query getEntryQueryForHashtag(String hashtagName) {
    return getEntrySubcollection(AuthenticationUtil.getUserEmail())
        .whereEqualTo(FIELD_USER_EMAIL, AuthenticationUtil.getUserEmail())
        .whereArrayContains(FIELD_HASHTAGS, hashtagName)
        .orderBy(FIELD_CREATED_AT_MILLIS, DESCENDING);
  }

  @Exclude
  public String getEntryTypeText(Context c) {
    return c
        .getResources()
        .getString(getIsPhoto() ? R.string.entry_type_photo : R.string.entry_type_text);
  }

  @Exclude
  public String getEntryTypeTextCapitalized(Context c) {
    String entryTypeText = getEntryTypeText(c);
    return entryTypeText.substring(0, 1).toUpperCase() + entryTypeText.substring(1);
  }

  @Exclude
  public void saveOnFirestore() {
    saveOnFirestore(null);
  }

  @Exclude
  public void saveOnFirestore(@Nullable final Runnable onSuccessRunnable) {
    getEntrySubcollection()
        .document(getId())
        .set(Entry.this, SetOptions.merge())
        .addOnSuccessListener(aVoid -> {
          if (onSuccessRunnable != null) {
            onSuccessRunnable.run();
          }
        })
        .addOnFailureListener(e -> {
          CrashUtil.log("Failed to save entry with id: " + getId());
        });
  }

  /**
   * This is useful in cases where the Entry class doesn't have all the updated fields. This
   * prevents saving to Firestore with weird values.
   */
  public void saveFieldOnFirestore(String field, Object value) {
    getEntrySubcollection()
        .document(getId())
        .update(field, value)
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
    getEntrySubcollection(AuthenticationUtil.getUserEmail()).document(getId())
        .delete()
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

  @Exclude
  public StorageReference getPhotoStorageReference() {
    return FirebaseStorage.getInstance()
        .getReference(AuthenticationUtil.getUserEmail()
            + "/" + PHOTO_FOLDER + "/" + getPhotoId());
  }

  /**
   * Ex: Aug 4 (if it's this year)
   * Aug 4, 2019 (if it's not this year)
   */
  @Exclude
  public String getReadableDateString() {
    Calendar calendar = DateUtil.getYearMonthDayCalendar(getYearMonthDay());
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

  public String getEntryText() {
    return mEntryText;
  }

  public void setEntryText(String entryText) {
    mEntryText = entryText;
  }

  public String getYearMonthDay() {
    return mYearMonthDay;
  }

  public void setYearMonthDay(String yearMonthDay) {
    mYearMonthDay = yearMonthDay;
  }

  public boolean getIsNewEntry() {
    return mIsNewEntry;
  }

  public void setIsNewEntry(boolean newEntry) {
    mIsNewEntry = newEntry;
  }

  public int getColor() {
    return mColor;
  }

  public void setColor(int color) {
    mColor = color;
  }

  public String getUriString() {
    return mUriString;
  }

  public void setUriString(String uriString) {
    mUriString = uriString;
  }

  public boolean getIsPhoto() {
    return mIsPhoto;
  }

  public void setIsPhoto(boolean photo) {
    mIsPhoto = photo;
  }

  public String getPhotoId() {
    return mPhotoId;
  }

  public void setPhotoId(String photoId) {
    mPhotoId = photoId;
  }

  @Exclude
  public void deleteOnFirebaseStorage() {
    getPhotoStorageReference().delete()
        .addOnSuccessListener(new OnSuccessListener<Void>() {
          @Override
          public void onSuccess(Void aVoid) {
            Etil.showToast(
                StriateApplication.sApplicationContext,
                "Photo successfully deleted.");
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            Timber.e(
                "Photo could not be deleted. id is %s. path is %s",
                getId(),
                getPhotoStorageReference().getPath());
          }
        });
  }

  public String getPhotoCaptionText() {
    return mPhotoCaptionText;
  }

  public void setPhotoCaptionText(String photoCaptionText) {
    mPhotoCaptionText = photoCaptionText;
  }

  public List<String> getHashtags() {
    return mHashtags;
  }

  public void setHashtags(List<String> hashtags) {
    mHashtags = hashtags;
  }
}
