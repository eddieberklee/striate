package com.compscieddy.striate.god;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.compscieddy.eddie_utils.etil.VibrationEtil;
import com.compscieddy.striate.NoteCategorizer;
import com.compscieddy.striate.databinding.InfinoteGodFragmentBinding;
import com.compscieddy.striate.databinding.NoteItemBinding;
import com.compscieddy.striate.model.Note;
import com.compscieddy.striate.note.NoteHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class InfinoteGodFragment extends Fragment {

  private Context c;
  private Resources res;
  private InfinoteGodFragmentBinding binding;
  private FirebaseRecyclerAdapter mFirebaseAdapter;
  private NoteCategorizer mNoteCategorizer;

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

    mNoteCategorizer = new NoteCategorizer(binding);

    binding.notesRecyclerView.setOnTouchListener(mNoteCategorizer.getTouchListener());
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
