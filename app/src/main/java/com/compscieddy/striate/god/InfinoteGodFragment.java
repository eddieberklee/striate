package com.compscieddy.striate.god;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.compscieddy.eddie_utils.etil.VibrationEtil;
import com.compscieddy.striate.R;
import com.compscieddy.striate.databinding.InfinoteGodFragmentBinding;
import com.compscieddy.striate.databinding.NoteItemBinding;
import com.compscieddy.striate.model.Note;
import com.compscieddy.striate.note.NoteHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class InfinoteGodFragment extends Fragment {

  List<NoteHolder> mHighlightedNoteHolders = new ArrayList<>();
  private InfinoteGodFragmentBinding binding;
  private Resources res;
  private FirebaseRecyclerAdapter mFirebaseAdapter;
  private Context c;

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

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = InfinoteGodFragmentBinding.inflate(inflater, container, false);
    c = binding.getRoot().getContext();
    res = c.getResources();

    initFirebaseRecyclerView();
    initNewNoteAutocomplete();

    return binding.getRoot();
  }

  private void initNewNoteAutocomplete() {
    binding.newNoteAutocomplete.setRawInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
        | InputType.TYPE_CLASS_TEXT
        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
    binding.newNoteAutocomplete.setImeOptions(EditorInfo.IME_ACTION_DONE);
    binding.newNoteAutocomplete.setOnEditorActionListener((v, actionId, event) -> {
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        VibrationEtil.vibrate(binding.newNoteAutocomplete);

        saveNewNote(binding.newNoteAutocomplete.getText().toString());

        binding.newNoteAutocomplete.setText("");

        return true;
      }
      return false;
    });
  }

  private void saveNewNote(String noteText) {
    Note newNote = new Note(noteText);
    newNote.saveNewEntryOnFirebaseRealtimeDatabase();
  }

  private void initFirebaseRecyclerView() {
    FirebaseRecyclerOptions<Note> options = new FirebaseRecyclerOptions.Builder<Note>()
        .setQuery(Note.getNoteQuery(), Note.class)
        .build();
    mFirebaseAdapter = new FirebaseRecyclerAdapter<Note, NoteHolder>(options) {
      @NonNull
      @Override
      public NoteHolder onCreateViewHolder(
          @NonNull ViewGroup parent, int viewType) {
        return new NoteHolder(
            NoteItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
      }

      @Override
      protected void onBindViewHolder(
          @NonNull NoteHolder holder,
          int position,
          @NonNull Note note) {
        holder.setNote(note);
      }
    };

    binding.notesRecyclerView.setLayoutManager(new LinearLayoutManager(
        c,
        RecyclerView.VERTICAL,
        true));
    binding.notesRecyclerView.setAdapter(mFirebaseAdapter);

    binding.notesRecyclerView.requestDisallowInterceptTouchEvent(true);

    binding.notesRecyclerView.setOnTouchListener(new View.OnTouchListener() {

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
    });
  }

  private int getRandomColor() {
    int[] colors = new int[] {
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

  private NoteHolder getNoteHolderForNoteView(View noteView) {
    return (NoteHolder) binding.notesRecyclerView.findContainingViewHolder(noteView);
  }

  private View getNoteViewForY(float y) {
    return binding.notesRecyclerView.findChildViewUnder(0, y);
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

  @Override
  public void onStart() {
    super.onStart();
    mFirebaseAdapter.startListening();
  }

  @Override
  public void onStop() {
    super.onStop();
    mFirebaseAdapter.stopListening();
  }
}
