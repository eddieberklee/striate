package com.compscieddy.writeaday2;

import com.compscieddy.writeaday2.databinding.SuggestionTextItemBinding;

import androidx.recyclerview.widget.RecyclerView;

public class SuggestionTextViewHolder extends RecyclerView.ViewHolder {

  public interface SuggestionTextClickListener {
    void onSuggestionTextClick(String suggestionText);
  }

  private SuggestionTextItemBinding binding;
  private SuggestionTextClickListener mSuggestionTextClickListener;

  public SuggestionTextViewHolder(
      SuggestionTextItemBinding binding,
      SuggestionTextClickListener suggestionTextClickListener) {
    super(binding.getRoot());
    this.binding = binding;
    mSuggestionTextClickListener = suggestionTextClickListener;
  }

  public void setSuggestionText(String text) {
    binding.suggestionTextTitle.setText(text);
    binding.rootView.setOnClickListener(v -> mSuggestionTextClickListener.onSuggestionTextClick(text));
  }
}
