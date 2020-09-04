package com.compscieddy.striate.photo;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.compscieddy.striate.databinding.HashtagAllPhotosItemBinding;

import java.util.List;

import androidx.annotation.NonNull;
import carbon.widget.RecyclerView;

public class HashtagAllPhotosRecyclerAdapter extends RecyclerView.Adapter<HashtagAllPhotosHolder> {

  private List<String> mHashtagNames;

  public HashtagAllPhotosRecyclerAdapter(List<String> hashtagNames) {
    super();
    mHashtagNames = hashtagNames;
  }

  @NonNull
  @Override
  public HashtagAllPhotosHolder onCreateViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    return new HashtagAllPhotosHolder(HashtagAllPhotosItemBinding.inflate(
        LayoutInflater.from(parent.getContext()),
        parent,
        false));
  }

  @Override
  public void onBindViewHolder(@NonNull HashtagAllPhotosHolder holder, int position) {
    holder.setHashtagName(mHashtagNames.get(position));
  }

  @Override
  public int getItemCount() {
    return mHashtagNames.size();
  }

  public void updateHashtagNames(List<String> newHashtagNames) {
    boolean isSameNames = mHashtagNames.size() == newHashtagNames.size()
        && mHashtagNames.containsAll(newHashtagNames);
    if (!isSameNames) {
      mHashtagNames = newHashtagNames;
      notifyDataSetChanged();
    }
  }
}
