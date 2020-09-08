package com.compscieddy.striate.note;

import android.content.Context;
import android.content.res.Resources;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;

import com.compscieddy.eddie_utils.etil.ColorEtil;
import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.eddie_utils.etil.KeyboardEtil;
import com.compscieddy.eddie_utils.etil.VibrationEtil;
import com.compscieddy.eddie_utils.etil.ViewEtil;
import com.compscieddy.striate.R;
import com.compscieddy.striate.databinding.NoteItemBinding;
import com.compscieddy.striate.god.InfinoteGodFragment;
import com.compscieddy.striate.model.Hashtag;
import com.compscieddy.striate.model.Note;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

public class NoteHolder extends RecyclerView.ViewHolder {

  private final Context c;
  private final Resources res;
  private NoteItemBinding binding;

  private Note mNote;
  private InfinoteGodFragment.ExistingHashtagsCallback mExistingHashtagsCallback;
  private Hashtag mHashtag;

  public NoteHolder(NoteItemBinding binding) {
    super(binding.getRoot());
    c = binding.getRoot().getContext();
    this.binding = binding;
    res = c.getResources();
  }

  /**
   * todo: turn into util helper method
   */
  public static List<String> getNoteIdsForNotes(List<Note> notes) {
    List<String> noteIds = new ArrayList<>();
    for (Note note : notes) {
      noteIds.add(note.getId());
    }
    return noteIds;
  }

  private void initNoteAutoComplete() {
    binding.noteTextAutocomplete.setRawInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
        | InputType.TYPE_CLASS_TEXT
        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
    binding.noteTextAutocomplete.setImeOptions(EditorInfo.IME_ACTION_DONE);
    binding.noteTextAutocomplete.setOnEditorActionListener((v, actionId, event) -> {
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        VibrationEtil.vibrate(binding.noteTextAutocomplete);

        String updatedNoteText = binding.noteTextAutocomplete.getText().toString();

        if (TextUtils.isEmpty(updatedNoteText)) {
          mNote.deleteOnFirebase();
          return true;
        }

        boolean hasNoteTextChanged = !TextUtils.equals(updatedNoteText, mNote.getNoteText());
        if (hasNoteTextChanged) {
          mNote.setNoteText(updatedNoteText);
          mNote.saveFieldOnFirebaseRealtimeDatabase(Note.FIELD_NOTE_TEXT, updatedNoteText);
        }

        binding.noteTextAutocomplete.clearFocus();
        KeyboardEtil.hideKeyboard(binding.noteTextAutocomplete);

        return true;
      }
      return false;
    });
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
    binding.hashtagName.setText("");
    binding.hashtagName.setVisibility(View.GONE);

    binding.lineIndicator.setVisibility(View.GONE);
    binding.dotControl.setVisibility(View.VISIBLE);

    initDot();
  }

  private void fetchHashtag() {
    Hashtag.getHashtag(mNote.getHashtagId(), hashtag -> {
      mHashtag = hashtag;
      if (mHashtag != null) {
      }
    });
  }

  private void initDot() {
  }

  /**
   * Start from 0 height and grow it to full height.
   */
  private void initHashtag() {
    if (TextUtils.isEmpty(mNote.getHashtagName())) {
      return;
    }

    boolean isFirstInSection = mNote.getHashtagSectionNoteIds() != null
        && mNote.getHashtagSectionNoteIds().size() > 0
        && TextUtils.equals(mNote.getHashtagSectionNoteIds().get(0), mNote.getId());
    boolean shouldShowHashtagName = isFirstInSection;

    // always show the line if available
    initHashtagLineAndHideDot(mNote.getHashtagColor());

    // only show the name if the first in hashtag section
    if (shouldShowHashtagName) {
      initHashtagName(mNote.getHashtagSectionNoteIds(), mNote.getHashtagColor());
    }
  }

  private void initHashtagTextColor(int hashtagColor) {
    binding.hashtagName.setTextColor(hashtagColor);
    binding.hashtagName.setHintTextColor(ColorEtil.applyAlpha(hashtagColor, 0.6f));
  }

  private void initHashtagLineAndHideDot(int color) {
    binding.lineIndicator.setVisibility(View.VISIBLE);
    binding.lineIndicator.setBackgroundColor(color);

    binding.dotControl.setVisibility(View.GONE);
  }

  private void expandHashtagView() {
    // todo: don't hardcode the height to grow to
    binding.hashtagName.setVisibility(View.VISIBLE);
    binding.hashtagName.getLayoutParams().height = 0;

    ViewEtil.animateViewHeight(
        binding.hashtagName,
        res.getDimensionPixelSize(R.dimen.expanded_hashtag_title_height),
        100);
  }

  public void highlight(int color) {
    binding.dotControl.setVisibility(View.GONE);

    initHashtagLineAndHideDot(color);

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

  public Note getNote() {
    return mNote;
  }

  public void setNote(Note note, InfinoteGodFragment.ExistingHashtagsCallback hashtagCallback) {
    mNote = note;
    mExistingHashtagsCallback = hashtagCallback;

    initNoteAutoComplete();
    maybeFetchHashtag();
    initHashtag();

    binding.noteTextAutocomplete.setText(note.getNoteText());
  }

  /**
   * Init hashtag editor on first note.
   */
  public void initHashtagName(List<String> noteIds, int hashtagColor) {
    expandHashtagView();

    if (!TextUtils.isEmpty(mNote.getHashtagName()))
      binding.hashtagName.setText(mNote.getHashtagName());
    initHashtagNameAutocomplete(noteIds, hashtagColor);

    binding.hashtagName.requestFocus();
    KeyboardEtil.showKeyboard(c);

    binding.hashtagName.setAdapter(new ArrayAdapter<>(
        c,
        R.layout.simple_thin_dropdown,
        getUniqueHashtagNamesFromHashtagList(mExistingHashtagsCallback.getExistingHashtags())));
  }

  private void initHashtagNameAutocomplete(final List<String> noteIds, int hashtagColor) {
    initHashtagTextColor(hashtagColor);

    binding.hashtagName.setRawInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
        | InputType.TYPE_CLASS_TEXT
        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
    binding.hashtagName.setImeOptions(EditorInfo.IME_ACTION_DONE);
    binding.hashtagName.setOnEditorActionListener((v, actionId, event) -> {
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        VibrationEtil.vibrate(binding.hashtagName);
        for (String noteId : noteIds) {
          updateNoteWithHashtagInfo(noteId, hashtagColor, noteIds);
        }
        clearFocusAndHideKeyboard();
        return true;
      }
      return false;
    });
  }

  private void clearFocusAndHideKeyboard() {
    binding.hashtagName.clearFocus();
    KeyboardEtil.hideKeyboard(binding.hashtagName);
  }

  private void updateNoteWithHashtagInfo(
      String noteId,
      int hashtagColor,
      List<String> noteIds) {
    String newHashtagText = binding.hashtagName.getText().toString();
    String hashtagId = getExistingOrCreateNewHashtag(newHashtagText);

    Note.fetchNote(noteId, note -> {
      note.setHashtagId(hashtagId);
      note.setHashtagName(newHashtagText);
      note.setHashtagColor(hashtagColor);
      note.setHashtagSectionNoteIds(noteIds);
      note.saveOnFirebaseRealtimeDatabase();
    });
  }

  private String getExistingOrCreateNewHashtag(String newHashtagText) {
    @Nullable Hashtag existingHashtag = findHashtagWithText(newHashtagText);

    if (existingHashtag != null) {
      return existingHashtag.getId();
    } else {
      Hashtag hashtag = new Hashtag(newHashtagText);
      hashtag.saveOnFirebaseRealtimeDatabase();
      return hashtag.getId();
    }
  }

  private @Nullable
  Hashtag findHashtagWithText(String newHashtagText) {
    // optimization: create a hashmap instead <HashtagText -> Hashtag>
    for (Hashtag hashtag : mExistingHashtagsCallback.getExistingHashtags()) {
      if (TextUtils.equals(hashtag.getHashtagName(), newHashtagText)) {
        return hashtag;
      }
    }
    return null;
  }

  private List<String> getUniqueHashtagNamesFromHashtagList(List<Hashtag> existingHashtags) {
    Set<String> hashtagNames = new HashSet<>();
    for (Hashtag hashtag : existingHashtags) {
      if (hashtag == null || TextUtils.isEmpty(hashtag.getHashtagName())) {
        continue;
      }
      hashtagNames.add(hashtag.getHashtagName());
    }

    return new ArrayList<>(hashtagNames);
  }

  public boolean isPartOfExistingHashtagSection() {
    return !TextUtils.isEmpty(mNote.getHashtagId());
  }

  public void restoreHighlight() {
    initHashtagLineAndHideDot(mNote.getHashtagColor());
  }
}
