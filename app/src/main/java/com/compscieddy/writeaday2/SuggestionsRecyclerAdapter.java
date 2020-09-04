package com.compscieddy.writeaday2;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.compscieddy.writeaday2.SuggestionTextViewHolder.SuggestionTextClickListener;
import com.compscieddy.writeaday2.databinding.SuggestionsItemBinding;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings("WeakerAccess")
public class SuggestionsRecyclerAdapter extends RecyclerView.Adapter<SuggestionsViewHolder> {

  private Context c;
  private SuggestionTextClickListener mSuggestionTextClickListener;
  private Resources res;

  public @StringRes int[] suggestionCategories;

  public SuggestionsRecyclerAdapter(
      Context c,
      SuggestionTextClickListener suggestionTextClickListener) {
    super();
    this.c = c;
    mSuggestionTextClickListener = suggestionTextClickListener;
    res = this.c.getResources();
    suggestionCategories = new int[] {
        R.string.category_mental_health,
        R.string.category_physical_health,
        R.string.category_drawing,
        R.string.category_learning,
    };
  }

  @NonNull
  @Override
  public SuggestionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new SuggestionsViewHolder(
        c,
        SuggestionsItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false),
        mSuggestionTextClickListener);
  }

  @Override
  public void onBindViewHolder(@NonNull SuggestionsViewHolder holder, int position) {
    holder.setSuggestionTexts(getSuggestionTexts(suggestionCategories[position]));
  }

  private int[] getSuggestionTexts(int suggestionCategory) {
    @StringRes int[] suggestionTexts = new int[]{}; // initializing just to satisfy lint
    switch (suggestionCategory) {
      case R.string.category_mental_health:
        suggestionTexts = new int[] {
            R.string.mental_health_yoga,
            R.string.mental_health_meditate,
            R.string.mental_health_drink_tea,
            R.string.mental_health_podcast_with_tea,
            R.string.mental_health_message_friends_and_family,
        };
        break;
      case R.string.category_physical_health:
        suggestionTexts = new int[] {
            R.string.physical_health_take_a_walk,
            R.string.physical_health_workout,
            R.string.physical_health_cold_shower,
            R.string.physical_health_morning_jog,
            R.string.physical_health_pushups,
            R.string.physical_health_squats,
        };
        break;
      case R.string.category_drawing:
        suggestionTexts = new int[] {
            R.string.drawing_daily_drawing,
            R.string.drawing_5_minute_sketch,
            R.string.drawing_drawing_lesson_on_youtube,
            R.string.drawing_morning_sketch,
            R.string.drawing_daily_doodle,
        };
        break;
      case R.string.category_learning:
        suggestionTexts = new int[] {
            R.string.learning_youtube,
            R.string.learning_blog_post,
            R.string.learning_listen_to_podcast,
            R.string.learning_ted_video,
            R.string.learning_journal,
            R.string.learning_research_on_wikipedia,
        };
      default:
        break;
    }
    return suggestionTexts;
  }

  @Override
  public int getItemCount() {
    return suggestionCategories.length;
  }
}
