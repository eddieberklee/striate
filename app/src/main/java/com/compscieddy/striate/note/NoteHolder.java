package com.compscieddy.striate.note;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;

import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.striate.databinding.NoteItemBinding;
import com.compscieddy.striate.model.Hashtag;
import com.compscieddy.striate.model.Note;

import androidx.recyclerview.widget.RecyclerView;

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

    fetchHashtag();

    binding.noteText.setText(note.getNoteText());
  }

  private void fetchHashtag() {
    Hashtag.getHashtag(mNote.getHashtagId(), hashtag -> {
      mHashtag = hashtag;
      if (mHashtag != null) {
        initHashtag();
      }
    });
  }

  /**
   * Start from 0 height and grow it to full height.
   */
  private void initHashtag() {
    binding.hashtagText.setText(mHashtag.getHashtagText());

    ViewGroup.LayoutParams hashtagParams = binding.hashtagText.getLayoutParams();
    hashtagParams.height = 0;
    binding.hashtagText.setLayoutParams(hashtagParams);

    binding.hashtagText.setVisibility(View.VISIBLE);

    expandHashtagView();
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
}
