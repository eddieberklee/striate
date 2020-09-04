package com.compscieddy.striate.god;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.compscieddy.striate.databinding.InfinoteGodFragmentBinding;
import com.compscieddy.striate.databinding.NoteItemBinding;
import com.compscieddy.striate.model.Note;
import com.compscieddy.striate.note.NoteHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InfinoteGodFragment extends Fragment {

  private InfinoteGodFragmentBinding binding;
  private Resources res;
  private FirebaseRecyclerAdapter mFirebaseAdapter;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = InfinoteGodFragmentBinding.inflate(inflater, container, false);
    Context c = binding.getRoot().getContext();
    res = c.getResources();
    initFirebaseRecyclerView();
    return binding.getRoot();
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
