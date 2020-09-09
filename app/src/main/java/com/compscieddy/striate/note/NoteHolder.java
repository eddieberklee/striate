package com.compscieddy.striate.note;

import android.content.Context;
import android.content.res.Resources;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.compscieddy.eddie_utils.etil.ColorEtil;
import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.eddie_utils.etil.KeyboardEtil;
import com.compscieddy.eddie_utils.etil.VibrationEtil;
import com.compscieddy.eddie_utils.etil.ViewEtil;
import com.compscieddy.striate.HashtagAutocompleteArrayAdapter;
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

import static android.view.View.GONE;

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
    binding.hashtagNameAutocompleteView.setText("");
    binding.hashtagNameAutocompleteView.setVisibility(GONE);

    binding.lineIndicator.setVisibility(GONE);
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
    binding.hashtagNameAutocompleteView.setTextColor(hashtagColor);
    binding.hashtagNameAutocompleteView.setHintTextColor(ColorEtil.applyAlpha(hashtagColor, 0.6f));
  }

  private void initHashtagLineAndHideDot(int color) {
    binding.lineIndicator.setVisibility(View.VISIBLE);
    binding.lineIndicator.setBackgroundColor(color);

    binding.dotControl.setVisibility(GONE);
  }

  private void expandHashtagView() {
    // todo: don't hardcode the height to grow to
    binding.hashtagNameAutocompleteView.setVisibility(View.VISIBLE);
    binding.hashtagNameAutocompleteView.getLayoutParams().height = 0;

    ViewEtil.animateViewHeight(
        binding.hashtagNameAutocompleteView,
        res.getDimensionPixelSize(R.dimen.expanded_hashtag_title_height),
        100);
  }

  public void highlight(int color) {
    binding.dotControl.setVisibility(GONE);

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
    binding.lineIndicator.setVisibility(GONE);
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
    binding.hashtagNameAutocompleteView.setVisibility(GONE);
    binding.hashtagRightArrowButton.setVisibility(GONE);
    maybeFetchHashtag();
    initHashtag();

    binding.noteTextAutocomplete.setText(note.getNoteText());
  }

  /**
   * Init hashtag editor on first note.
   */
  public void initHashtagName(List<String> noteIds, int hashtagColor) {
    expandHashtagView();

    if (TextUtils.isEmpty(mNote.getHashtagName())) {
      // hashtag is being shown for new hashtag section, put focus on it
      binding.hashtagNameAutocompleteView.requestFocus();
      KeyboardEtil.showKeyboard(c);
    } else {
      binding.hashtagNameAutocompleteView.setText(mNote.getHashtagName());
    }

    binding.hashtagRightArrowButton.setVisibility(View.VISIBLE);
    binding.hashtagRightArrowButton.setCustomColor(ColorEtil.applyAlpha(hashtagColor, 0.6f));

    initHashtagNameAutocompleteTextView(noteIds, hashtagColor);
    initHashtagAutocompleteAdapter(noteIds, hashtagColor);
    binding.hashtagNameAutocompleteView.setOnFocusChangeListener((v, hasFocus) -> {
      // todo: hack to ensure we are always updating based on new hashtags we create
      initHashtagAutocompleteAdapter(noteIds, hashtagColor);
    });
  }

  private void initHashtagAutocompleteAdapter(List<String> noteIds, int hashtagColor) {
    binding.hashtagNameAutocompleteView.setAdapter(
        new HashtagAutocompleteArrayAdapter(
            c,
            R.layout.simple_thin_dropdown,
            getUniqueHashtagNamesFromHashtagList(mExistingHashtagsCallback.getExistingHashtags()),
            hashtagColor));
    binding.hashtagNameAutocompleteView.setOnItemClickListener((parent, view, position, id) -> {
      onHashtagActionDonePressed(noteIds, hashtagColor);
    });
  }

  private void initHashtagNameAutocompleteTextView(final List<String> noteIds, int hashtagColor) {
    initHashtagTextColor(hashtagColor);

    binding.hashtagNameAutocompleteView.setRawInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
        | InputType.TYPE_CLASS_TEXT
        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
    binding.hashtagNameAutocompleteView.setImeOptions(EditorInfo.IME_ACTION_DONE);
    binding.hashtagNameAutocompleteView.setOnEditorActionListener((v, actionId, event) -> {
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        if (TextUtils.isEmpty(binding.hashtagNameAutocompleteView.getText())) {
          removeHashtagSectionFromAllNotes(noteIds);
        } else {
          onHashtagActionDonePressed(noteIds, hashtagColor);
        }
        return true;
      }
      return false;
    });
  }

  private void removeHashtagSectionFromAllNotes(List<String> noteIds) {
    for (String noteId : noteIds) {
      Note.fetchNote(noteId, note -> {
        note.unsetHashtagInfo();
        note.saveOnFirebaseRealtimeDatabase();
      });
    }
  }

  private void onHashtagActionDonePressed(List<String> noteIds, int hashtagColor) {
    VibrationEtil.vibrate(binding.hashtagNameAutocompleteView);
    for (String noteId : noteIds) {
      updateNoteWithHashtagInfo(noteId, hashtagColor, noteIds);
    }
    clearFocusAndHideKeyboard();
  }

  private void clearFocusAndHideKeyboard() {
    binding.hashtagNameAutocompleteView.clearFocus();
    KeyboardEtil.hideKeyboard(binding.hashtagNameAutocompleteView);
  }

  private void updateNoteWithHashtagInfo(
      String noteId,
      int hashtagColor,
      List<String> noteIds) {
    String newHashtagText = binding.hashtagNameAutocompleteView.getText().toString();
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
