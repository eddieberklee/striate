package com.compscieddy.striate;

import android.content.Context;
import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.View;

import com.compscieddy.eddie_utils.etil.VibrationEtil;
import com.compscieddy.striate.databinding.InfinoteGodFragmentBinding;
import com.compscieddy.striate.note.NoteHolder;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class NoteCategorizer {

  private InfinoteGodFragmentBinding binding;
  List<NoteHolder> mHighlightedNoteHolders = new ArrayList<>();

  View.OnTouchListener mTouchListener = new View.OnTouchListener() {

    private int mRandomColor;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
      @Nullable View noteView = getNoteViewForY(event.getY());
      if (noteView == null) {
        return false;
      }

      NoteHolder noteHolder = getNoteHolderForNoteView(noteView);

      if (event.getX() > noteHolder.getNoteTextViewX()) {
        cancelHighlightingAndClear();
        return false;
      }

      if (isActionDown(event) || isActionMove(event)) {
        if (isActionDown(event)) {
          mRandomColor = getRandomColor();
        }
        noteHolder.highlight(mRandomColor);
        if (!mHighlightedNoteHolders.contains(noteHolder)) {
          VibrationEtil.vibrate(noteView);
          // todo: this contains might not work perfectly
          mHighlightedNoteHolders.add(noteHolder);
        }
      } else if (isActionUp(event)) {
        for (NoteHolder holder : mHighlightedNoteHolders) {
          holder.setHighlight();
        }
        mHighlightedNoteHolders.clear();

      } else if (isActionCancel(event)) {
        cancelHighlightingAndClear();
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
