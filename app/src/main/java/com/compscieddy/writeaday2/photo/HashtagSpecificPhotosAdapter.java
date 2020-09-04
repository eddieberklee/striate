package com.compscieddy.writeaday2.photo;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.compscieddy.writeaday2.databinding.HashtagSpecificPhotoItemBinding;
import com.compscieddy.writeaday2.model.Entry;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HashtagSpecificPhotosAdapter extends RecyclerView.Adapter<HashtagSpecificPhotoHolder> {

  private List<Entry> mEntries;

  public HashtagSpecificPhotosAdapter(List<Entry> entries) {
    super();
    mEntries = entries;
  }

  @NonNull
  @Override
  public HashtagSpecificPhotoHolder onCreateViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    return new HashtagSpecificPhotoHolder(
        HashtagSpecificPhotoItemBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false));
  }

  @Override
  public void onBindViewHolder(
      @NonNull HashtagSpecificPhotoHolder holder, int position) {
    holder.setEntry(mEntries.get(position));
  }

  @Override
  public int getItemCount() {
    return mEntries.size();
  }

  public void updateEntries(List<Entry> entries) {
    boolean hasEntriesChanged = mEntries.size() == entries.size()
        && !mEntries.containsAll(entries);
    if (hasEntriesChanged) {
      return;
    }

    mEntries = entries;
    notifyDataSetChanged();
  }
}
