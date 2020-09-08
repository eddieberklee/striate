package com.compscieddy.striate;

import android.content.Context;
import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.View;

import com.compscieddy.eddie_utils.etil.VibrationEtil;
import com.compscieddy.striate.databinding.InfinoteGodFragmentBinding;
import com.compscieddy.striate.model.Note;
import com.compscieddy.striate.note.NoteHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.Nullable;
import timber.log.Timber;

public class NoteCategorizer {

  private InfinoteGodFragmentBinding binding;
  List<NoteHolder> mHighlightedNoteHolders = new ArrayList<>();

  View.OnTouchListener mTouchListener = new View.OnTouchListener() {

    private float mStartX = -1;
    private int mRandomColor;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
      Timber.d("categorizer action " + getActionString(event));

      @Nullable View noteView = getNoteViewForY(event.getY());
      if (noteView == null) {
        Timber.d("categorizer note view is null");
        return false;
      }

      // Hmm there is no ACTION_DOWN

      NoteHolder noteHolder = getNoteHolderForNoteView(noteView);

      if (mStartX == -1) {
        Timber.d("categorizer Resetting mStartX");
        mStartX = event.getX();
        mRandomColor = getRandomColor();
      }

      Timber.d("categorizer start x %s note text x %s", mStartX, noteHolder.getNoteTextViewX());

      if (isActionUp(event) || isActionCancel(event)) {
        mStartX = -1;
      }

      if (mStartX > noteHolder.getNoteTextViewX()) {
        Timber.d("categorizer returning false because x is too far right");
        return false;
      }

      if (isActionMove(event)) { // DOWN or MOVE
        noteHolder.highlight(mRandomColor);
        if (!mHighlightedNoteHolders.contains(noteHolder)) {
          VibrationEtil.vibrate(noteView);
          // todo: this contains might not work perfectly, may need to double-check based on note id
          mHighlightedNoteHolders.add(noteHolder);
        }
      } else if (isActionUp(event)) { // UP
        for (NoteHolder holder : mHighlightedNoteHolders) {
          holder.setHighlight();
        }

        Collections.sort(mHighlightedNoteHolders, new Comparator<NoteHolder>() {
          @Override public int compare(NoteHolder o1, NoteHolder o2) {
            return (int) (o1.getNote().getCreatedAtMillis() - o2.getNote().getCreatedAtMillis());
          }
        });

        NoteHolder firstNoteHolder = mHighlightedNoteHolders.get(0);
        firstNoteHolder.initHashtagDragSectionEditor(
            getNotesFromNoteHolders(mHighlightedNoteHolders),
            mRandomColor);

        mHighlightedNoteHolders.clear();
        return false;
      } else if (isActionCancel(event)) { // CANCEL
        cancelHighlightingAndClear();
        return false;
      }

      // false so we still allow parent views to handle their own touches
      return true;
    }

    private void cancelHighlightingAndClear() {
      for (NoteHolder holder : mHighlightedNoteHolders) {
        holder.cancelHighlight();
      }
      mHighlightedNoteHolders.clear();
    }
  };

  private List<Note> getNotesFromNoteHolders(List<NoteHolder> noteHolders) {
    List<Note> notes = new ArrayList<>();
    for (NoteHolder holder : mHighlightedNoteHolders) {
      notes.add(holder.getNote());
    }
    return notes;
  }

  private final Context c;
  private final Resources res;

  public NoteCategorizer(InfinoteGodFragmentBinding binding) {
    this.binding = binding;
    c = binding.getRoot().getContext();
    res = c.getResources();
  }

  public View.OnTouchListener getTouchListener() {
    return mTouchListener;
  }

  private NoteHolder getNoteHolderForNoteView(View noteView) {
    return (NoteHolder) binding.notesRecyclerView.findContainingViewHolder(noteView);
  }

  private View getNoteViewForY(float y) {
    return binding.notesRecyclerView.findChildViewUnder(0, y);
  }

  private int getRandomColor() {
    int[] colors = new int[]{
        R.color.striate_red,
        R.color.striate_orange,
        R.color.striate_yellow,
        R.color.striate_green,
        R.color.striate_teal,
        R.color.striate_blue,
        R.color.striate_purple,
        R.color.striate_dark_grey,
    };
    return res.getColor(colors[(int) (Math.random() * (colors.length - 1))]);
  }

  // todo: move to etils
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
      case MotionEvent.ACTION_CANCEL:
        actionString = "ACTION_CANCEL";
        break;
      default:
        actionString = "UNRECOG";
        break;
    }
    return actionString;
  }

  private static boolean isActionDown(MotionEvent event) {
    return event.getAction() == MotionEvent.ACTION_DOWN;
  }

  private static boolean isActionMove(MotionEvent event) {
    return event.getAction() == MotionEvent.ACTION_MOVE;
  }

  private static boolean isActionUp(MotionEvent event) {
    return event.getAction() == MotionEvent.ACTION_UP;
  }

  private static boolean isActionCancel(MotionEvent event) {
    return event.getAction() == MotionEvent.ACTION_CANCEL;
  }

}
