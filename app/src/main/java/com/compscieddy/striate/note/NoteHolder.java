package com.compscieddy.striate.note;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.MotionEvent;
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

    binding.getRoot().setOnTouchListener((v, event) -> {
      String actionString = getActionString(event);
      Timber.d("onTouch v: " + v + " " + actionString);
      return true;
    });
  }

  private String getActionString(MotionEvent event) {
    String actionString;
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        actionString = "ACTION_DOWN";
        break;
      case MotionEvent.ACTION_UP:
        actionString = "ACTION_UP";
        break;
      case MotionEvent.ACTION_MOVE:
        actionString = "ACTION_MOVE";
        break;
      default:
        actionString = "UNRECOG";
        break;
    }
    return actionString;
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
}
