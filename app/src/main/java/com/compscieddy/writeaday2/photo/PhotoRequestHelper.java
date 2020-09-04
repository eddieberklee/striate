package com.compscieddy.writeaday2.photo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.compscieddy.writeaday2.model.Entry;
import com.compscieddy.writeaday2.util.CrashUtil;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import timber.log.Timber;

public class PhotoRequestHelper {

  private Context c;
  private FragmentManager mFragmentManager;
  private String mYearMonthDay;
  private @Nullable PhotoLoadingFragment mPhotoLoadingFragment;

  /**
   * Request codes should be instantiated in the activity/fragment that they're used to avoid
   * accidental conflicts in request codes being used by various helper classes.
   */
  public PhotoRequestHelper(Context c, FragmentManager fragmentManager, String yearMonthDay) {
    this.c = c;
    mFragmentManager = fragmentManager;
    mYearMonthDay = yearMonthDay;
  }

  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
      for (Image image : ImagePicker.getImages(data)) {
        upload(image);
      }
      showPhotoLoadingFragment();
    }
  }

  private void showPhotoLoadingFragment() {
    mPhotoLoadingFragment = PhotoLoadingFragment.newInstance(mYearMonthDay);
    mPhotoLoadingFragment.show(mFragmentManager, PhotoLoadingFragment.TAG);
  }

  private void upload(Image image) {
    final String newPhotoId = getNewPhotoId();

    final StorageReference newPhotoStorageRef = Entry.generateNewPhotoStorageReference(newPhotoId);
    UploadTask uploadTask = newPhotoStorageRef.putFile(image.getUri());
    uploadTask
        .addOnSuccessListener(taskSnapshot -> {
          if (mPhotoLoadingFragment != null && mPhotoLoadingFragment.isResumed()) {
            mPhotoLoadingFragment.dismiss();
          }
        })
        .addOnFailureListener(e -> CrashUtil.log(String.format(
            "Failed to upload image %s",
            image.getUri())))
        .addOnProgressListener(taskSnapshot -> {
          double progress =
              (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
          Timber.d("Image upload progress %s", progress);
          if (mPhotoLoadingFragment != null && mPhotoLoadingFragment.isResumed()) {
            mPhotoLoadingFragment.setProgress(progress);
          }
        });
    uploadTask
        .continueWithTask(task -> {
          if (!task.isSuccessful()) {
            throw task.getException();
          }
          return newPhotoStorageRef.getDownloadUrl();
        })
        .addOnCompleteListener(task -> {
          if (task.isSuccessful()) {
            Uri downloadUri = task.getResult();
            Timber.d("Photo upload path %s", task.getResult().getPath());
            Entry photoEntry = new Entry(
                true,
                newPhotoId,
                downloadUri.toString(),
                mYearMonthDay);
            photoEntry.saveOnFirestore();
          }
        });
  }

  private String getNewPhotoId() {
    try {
      TimeUnit.MILLISECONDS.sleep(1);
    } catch (InterruptedException e) {
      Timber.e("Sleeping to make sure we have a unique photo id");
    }
    return String.valueOf(System.currentTimeMillis());
  }

  private @Nullable
  Bitmap getBitmap(Image image) {
    try {
      return MediaStore.Images.Media.getBitmap(c.getContentResolver(), image.getUri());
    } catch (IOException e) {
      CrashUtil.log(String.format("Error while getting bitmap for path %s", image.getPath()));
      e.printStackTrace();
    }
    return null;
  }

  public void launchPhotoPicker(Activity activity) {
    ImagePicker.create(activity)
        .start();
  }

  public void launchPhotoPicker(Fragment fragment) {
    ImagePicker.create(fragment)
        .start();
  }
}
