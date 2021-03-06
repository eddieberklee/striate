package com.compscieddy.striate;

import android.content.Context;

import com.compscieddy.striate.SuggestionTextViewHolder.SuggestionTextClickListener;
import com.compscieddy.striate.databinding.SuggestionsItemBinding;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import static androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL;

public class SuggestionsViewHolder extends ViewHolder {

  private Context c;
  private com.compscieddy.striate.databinding.SuggestionsItemBinding binding;
  private SuggestionTextClickListener mSuggestionTextClickListener;

  public SuggestionsViewHolder(Context c, SuggestionsItemBinding binding, SuggestionTextClickListener suggestionTextClickListener) {
    super(binding.getRoot());
    this.c = c;
    this.binding = binding;
    mSuggestionTextClickListener = suggestionTextClickListener;
  }

  public void setSuggestionTexts(@StringRes int[] suggestionTexts) {
    binding.suggestionTextsRecyclerView.setLayoutManager(
        new StaggeredGridLayoutManager(2, VERTICAL));
    binding.suggestionTextsRecyclerView.setAdapter(
        new SuggestionTextsRecyclerAdapter(c, suggestionTexts, mSuggestionTextClickListener));
  }
}
