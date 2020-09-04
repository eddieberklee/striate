package com.compscieddy.infinote;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.compscieddy.infinote.SuggestionTextViewHolder.SuggestionTextClickListener;
import com.compscieddy.infinote.databinding.SuggestionTextItemBinding;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

public class SuggestionTextsRecyclerAdapter extends RecyclerView.Adapter<SuggestionTextViewHolder> {

  private Context c;
  private SuggestionTextClickListener mSuggestionTextClickListener;
  private Resources res;

  private static @StringRes int[] suggestionTexts;

  public SuggestionTextsRecyclerAdapter(Context context, int[] suggestionTexts, SuggestionTextClickListener suggestionTextClickListener) {
    super();
    c = context;
    res = c.getResources();
    this.suggestionTexts = suggestionTexts;
    mSuggestionTextClickListener = suggestionTextClickListener;
  }

  @NonNull
  @Override
  public SuggestionTextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new SuggestionTextViewHolder(
        SuggestionTextItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false),
        mSuggestionTextClickListener);
  }

  @Override
  public void onBindViewHolder(@NonNull SuggestionTextViewHolder holder, int position) {
    holder.setSuggestionText(res.getString(suggestionTexts[position]));
  }

  @Override
  public int getItemCount() {
    return suggestionTexts.length;
  }
}
