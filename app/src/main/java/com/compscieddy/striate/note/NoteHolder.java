package com.compscieddy.striate.note;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.striate.databinding.NoteItemBinding;
import com.compscieddy.striate.model.Hashtag;
import com.compscieddy.striate.model.Note;

import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

public class NoteHolder extends RecyclerView.ViewHolder {

  private final Context c;
  private final Resources res;
  private NoteItemBinding binding;

  private Note mNote;
  private Hashtag mHashtag;

  public NoteHolder(NoteItemBinding binding) {
    super(binding.getRoot());
    c = binding.getRoot().getContext();
    this.binding = binding;
    res = c.getResources();
  }

  public void setNote(Note note) {
    mNote = note;

    maybeFetchHashtag();

    binding.noteTextAutocomplete.setText(note.getNoteText());
  }

  private void maybeFetchHashtag() {
    boolean doesHashtagExist = !TextUtils.isEmpty(mNote.getHashtagId());
    if (doesHashtagExist) {
      fetchHashtag();
    } else {
      // hide hashtag-related views
      initNoHashtag();
    }
  }

  private void initNoHashtag() {
    binding.lineIndicator.setVisibility(View.GONE);
    binding.dotControl.setVisibility(View.VISIBLE);

    initDot();
  }

  private void fetchHashtag() {
    Hashtag.getHashtag(mNote.getHashtagId(), hashtag -> {
      mHashtag = hashtag;
      if (mHashtag != null) {
        initYesHashtag();
      }
    });
  }

  private void initYesHashtag() {
    initHashtag();
    initLine();
  }

  private void initDot() {
  }

  /**
   * Start from 0 height and grow it to full height.
   */
  private void initHashtag() {
    binding.lineIndicator.setVisibility(View.VISIBLE);
    binding.hashtagText.setVisibility(View.VISIBLE);

    binding.hashtagText.setText(mHashtag.getHashtagText());

    ViewGroup.LayoutParams hashtagParams = binding.hashtagText.getLayoutParams();
    hashtagParams.height = 0;
    binding.hashtagText.setLayoutParams(hashtagParams);

    expandHashtagView();
  }

  private void initLine() {

  }

  private void expandHashtagView() {
    // todo: don't hardcode the height to grow to
    ValueAnimator heightAnimator = ValueAnimator.ofInt(new int[] {0, Etil.dpToPx(20)});
    heightAnimator.addUpdateListener((animation) -> {
      Integer animatedHeight = (Integer) animation.getAnimatedValue();
      ViewGroup.LayoutParams layoutParams = binding.hashtagText.getLayoutParams();
      layoutParams.height = animatedHeight;
      binding.hashtagText.setLayoutParams(layoutParams);
    });

    heightAnimator.setDuration(500);
    heightAnimator.start();
  }

  public void highlight(int color) {
    binding.lineIndicator.setVisibility(View.VISIBLE);
    binding.dotControl.setVisibility(View.GONE);

    binding.lineIndicator.setBackgroundColor(color);

    indentViews();
  }

  private void indentViews() {
    binding.mainNoteContainer.setTranslationX(Etil.dpToPx(50));
    binding.noteTextAutocomplete.setTranslationX(Etil.dpToPx(-5));
  }

  private void resetIndentViews() {
    binding.mainNoteContainer.setTranslationX(0);
    binding.noteTextAutocomplete.setTranslationX(0);
  }

  public void setHighlight() {
    Timber.d("notes setHighlight()");
    resetIndentViews();
  }

  public void cancelHighlight() {
    hideHashtagLineIndicatorShowDot();
    resetIndentViews();
  }

  private void hideHashtagLineIndicatorShowDot() {
    binding.lineIndicator.setVisibility(View.GONE);
    binding.dotControl.setVisibility(View.VISIBLE);
  }

  public float getNoteTextViewX() {
    return binding.noteTextAutocomplete.getLeft();
  }
}
