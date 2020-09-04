package com.compscieddy.writeaday2.photo;

import android.content.Context;
import android.content.res.Resources;

import com.bumptech.glide.Glide;
import com.compscieddy.writeaday2.databinding.HashtagSpecificPhotoItemBinding;
import com.compscieddy.writeaday2.model.Entry;

import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

public class HashtagSpecificPhotoHolder extends RecyclerView.ViewHolder {

  private final Context c;
  private final Resources res;
  private com.compscieddy.writeaday2.databinding.HashtagSpecificPhotoItemBinding binding;
  private Entry mEntry;

  public HashtagSpecificPhotoHolder(HashtagSpecificPhotoItemBinding binding) {
    super(binding.getRoot());
    this.binding = binding;
    c = binding.getRoot().getContext();
    res = c.getResources();
  }

  public void setEntry(Entry entry) {
    mEntry = entry;
    errorHandlingForNonPhotoEntries();

    Glide.with(c)
        .load(mEntry.getUriString())
        .into(binding.hashtagSpecificPhotoImageView);

    PhotoAnimationUtil.animateImagePulsing(entry, binding.hashtagSpecificPhotoImageView);
  }

  private void errorHandlingForNonPhotoEntries() {
    if (!mEntry.getIsPhoto()) {
      Timber.e(
          "All entries in HashtagSpecificPhotoHolder should be photos not text, but that's not so" +
              " with this entry with id %s",
          mEntry.getId());
    }
  }
}
