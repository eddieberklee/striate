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
import com.compscieddy.striate.HashtagDragSectionManager;
import com.compscieddy.striate.NoteCategorizer;
import com.compscieddy.striate.databinding.InfinoteGodFragmentBinding;
import com.compscieddy.striate.databinding.NoteItemBinding;
import com.compscieddy.striate.model.Hashtag;
import com.compscieddy.striate.model.HashtagDragSection;
import com.compscieddy.striate.model.Note;
import com.compscieddy.striate.note.NoteHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

public class InfinoteGodFragment extends Fragment {

  private FirebaseRecyclerAdapter mNoteFirebaseAdapter = new FirebaseRecyclerAdapter<Note, NoteHolder>(
      new FirebaseRecyclerOptions.Builder<Note>()
          .setQuery(Note.getNoteQuery(), Note.class)
          .build()) {
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

  private Context c;
  private Resources res;
  private InfinoteGodFragmentBinding binding;
  private NoteCategorizer mNoteCategorizer;
  private HashtagDragSectionManager mHashtagDragSectionManager;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = InfinoteGodFragmentBinding.inflate(inflater, container, false);
    c = binding.getRoot().getContext();
    res = c.getResources();

    initFirebaseRecyclerView();
    initHashtagDragSections();
    initNewNoteAutocomplete();
    initExistingHashtags();

    return binding.getRoot();
  }

  private void initExistingHashtags() {
    binding.notesRecyclerView.postDelayed(() -> {
      // This post delayed is a hack to make sure we have valid view holders if this hashtag query
      // returns more quickly than the note view holders are created and laid out.
      Hashtag.getHashtagQuery()
          .addChildEventListener(new ChildEventListener() {
            @Override public void onChildAdded(
                @NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
              Hashtag hashtagAdded = snapshot.getValue(Hashtag.class);
              for (int i = 0; i < mNoteFirebaseAdapter.getItemCount(); i++) {
                NoteHolder noteHolder = (NoteHolder) binding.notesRecyclerView.findViewHolderForAdapterPosition(
                    i);
                if (noteHolder == null) {
                  Timber.e("Non-breaking but existing hashtags may not all appear");
                  continue;
                }
                noteHolder.getExistingHashtags().add(hashtagAdded);
              }
            }

            @Override public void onChildChanged(
                @NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
              Timber.d("Hashtag query child changed");
            }

            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {
              Hashtag hashtagRemoved = snapshot.getValue(Hashtag.class);
              for (int i = 0; i < mNoteFirebaseAdapter.getItemCount(); i++) {
                NoteHolder noteHolder = (NoteHolder) mNoteFirebaseAdapter.getItem(i);
                noteHolder.getExistingHashtags().remove(hashtagRemoved.getHashtagName());
              }
            }

            @Override public void onChildMoved(
                @NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
              Timber.d("Hashtag query child moved");
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
              Timber.d("Hashtag query cancelled");
            }
          });
    }, 500);
  }

  /**
   * These determine the colored lines on the side of the notes and the hashtag categorization title.
   */
  private void initHashtagDragSections() {
    mHashtagDragSectionManager = new HashtagDragSectionManager(mNoteFirebaseAdapter);
    HashtagDragSection.getHashtagDragSection()
        .addChildEventListener(new ChildEventListener() {
          @Override public void onChildAdded(
              @NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            mHashtagDragSectionManager.addHashtagDragSection(snapshot.getValue(HashtagDragSection.class));
          }

          @Override public void onChildChanged(
              @NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

          }

          @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {

          }

          @Override public void onChildMoved(
              @NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

          }

          @Override public void onCancelled(@NonNull DatabaseError error) {

          }
        });
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
    binding.notesRecyclerView.setLayoutManager(new LinearLayoutManager(
        c,
        RecyclerView.VERTICAL,
        true));
    binding.notesRecyclerView.setAdapter(mNoteFirebaseAdapter);

//    binding.notesRecyclerView.requestDisallowInterceptTouchEvent(true);

    mNoteCategorizer = new NoteCategorizer(binding);

    binding.notesRecyclerView.setOnTouchListener(mNoteCategorizer.getTouchListener());
  }

  @Override
  public void onStart() {
    super.onStart();
    mNoteFirebaseAdapter.startListening();
  }

  @Override
  public void onStop() {
    super.onStop();
    mNoteFirebaseAdapter.stopListening();
  }
}
