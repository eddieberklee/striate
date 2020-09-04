package com.compscieddy.writeaday2.photo;

import android.content.Context;
import android.content.res.Resources;

import com.compscieddy.writeaday2.R;
import com.compscieddy.writeaday2.databinding.HashtagAllPhotosItemBinding;
import com.compscieddy.writeaday2.model.Entry;
import com.compscieddy.writeaday2.util.CrashUtil;

import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

public class HashtagAllPhotosHolder extends RecyclerView.ViewHolder {

  private final Context c;
  private final Resources res;
  private HashtagAllPhotosItemBinding binding;
  private String mHashtagName;
  private HashtagSpecificPhotosAdapter mHashtagSpecificPhotosAdapter;

  public HashtagAllPhotosHolder(HashtagAllPhotosItemBinding binding) {
    super(binding.getRoot());
    this.binding = binding;
    c = binding.getRoot().getContext();
    res = c.getResources();
  }

  public void setHashtagName(String hashtagName) {
    mHashtagName = hashtagName;
    binding.hashtagName.setText(mHashtagName);
    queryHashtagSpecificPhotos();
  }

  private void queryHashtagSpecificPhotos() {
    Entry.getEntryQueryForHashtag(mHashtagName)
        .addSnapshotListener((snapshots, error) -> {
          if (CrashUtil.didHandleFirestoreException(error)) return;

          if (snapshots != null) {
            List<Entry> entries = snapshots.toObjects(Entry.class);
            binding.numPhotosFoundForHashtag.setText(String.format(
                c.getString(R.string.num_photos_found_for_hashtag),
                entries.size()));
            initHashtagSpecificPhotos(entries);
          } else {
            Timber.d(
                "Snapshot was null while query for specific photos for hashtag %s",
                mHashtagName);
          }

        });
  }

  private void initHashtagSpecificPhotos(List<Entry> entries) {
    if (mHashtagSpecificPhotosAdapter == null) {
      mHashtagSpecificPhotosAdapter = new HashtagSpecificPhotosAdapter(entries);
    } else {
      mHashtagSpecificPhotosAdapter.updateEntries(entries);
    }
    binding.hashtagSpecificPhotosRecyclerView.setLayoutManager(new GridLayoutManager(c, 2));
    binding.hashtagSpecificPhotosRecyclerView.setAdapter(mHashtagSpecificPhotosAdapter);
  }
}
