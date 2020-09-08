package com.compscieddy.striate.note;

import android.content.Context;
import android.content.res.Resources;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;

import com.compscieddy.eddie_utils.etil.ColorEtil;
import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.eddie_utils.etil.KeyboardEtil;
import com.compscieddy.eddie_utils.etil.VibrationEtil;
import com.compscieddy.eddie_utils.etil.ViewEtil;
import com.compscieddy.striate.R;
import com.compscieddy.striate.databinding.NoteItemBinding;
import com.compscieddy.striate.model.Hashtag;
import com.compscieddy.striate.model.HashtagDragSection;
import com.compscieddy.striate.model.Note;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

public class NoteHolder extends RecyclerView.ViewHolder {

  private final Context c;
  private final Resources res;
  private NoteItemBinding binding;

  private Note mNote;
  private Hashtag mHashtag;
  private List<Hashtag> mExistingHashtags = new ArrayList<>();

  public NoteHolder(NoteItemBinding binding) {
    super(binding.getRoot());
    c = binding.getRoot().getContext();
    this.binding = binding;
    res = c.getResources();
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
    binding.lineIndicator.setVisibility(View.GONE);
    binding.dotControl.setVisibility(View.VISIBLE);

    initDot();
  }

  private void fetchHashtag() {
    Hashtag.getHashtag(mNote.getHashtagId(), hashtag -> {
      mHashtag = hashtag;
      if (mHashtag != null) {
        initHashtag();
      }
    });
  }

  private void initDot() {
  }

  /**
   * Start from 0 height and grow it to full height.
   */
  private void initHashtag() {
    binding.hashtagText.setVisibility(View.VISIBLE);

    binding.hashtagText.setText(mHashtag.getHashtagName());
    binding.hashtagText.setSelection(mHashtag.getHashtagName().length());
    initHashtagTextColor(mNote.getHashtagColor());

    initHashtagLineAndHideDot(mNote.getHashtagColor());

    ViewGroup.LayoutParams hashtagParams = binding.hashtagText.getLayoutParams();
    hashtagParams.height = 0;
    binding.hashtagText.setLayoutParams(hashtagParams);

    expandHashtagView();
  }

  private void initHashtagTextColor(int hashtagColor) {
    binding.hashtagText.setTextColor(hashtagColor);
    binding.hashtagText.setHintTextColor(ColorEtil.applyAlpha(hashtagColor, 0.6f));
  }

  private void initHashtagLineAndHideDot(int color) {
    binding.lineIndicator.setVisibility(View.VISIBLE);
    binding.lineIndicator.setBackgroundColor(color);

    binding.dotControl.setVisibility(View.GONE);
  }

  private void expandHashtagView() {
    // todo: don't hardcode the height to grow to
    binding.hashtagText.setVisibility(View.VISIBLE);
    binding.hashtagText.getLayoutParams().height = 0;

    ViewEtil.animateViewHeight(
        binding.hashtagText,
        res.getDimensionPixelSize(R.dimen.expanded_hashtag_title_height),
        600);
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

  public void setNote(Note note) {
    mNote = note;

    initNoteAutoComplete();
    maybeFetchHashtag();

    binding.noteTextAutocomplete.setText(note.getNoteText());
  }

  /**
   * Init hashtag editor on first note.
   */
  public void initHashtagDragSectionEditor(List<Note> notes, int hashtagColor) {
    expandHashtagView();

    initHashtagTitleAutocomplete(notes, hashtagColor);
  }

  private void initHashtagTitleAutocomplete(final List<Note> notes, int hashtagColor) {
    initHashtagTextColor(hashtagColor);

    binding.hashtagText.setAdapter(new ArrayAdapter<String>(
        c,
        android.R.layout.simple_dropdown_item_1line,
        getHashtagNameFromHashtagList(mExistingHashtags)));
    binding.hashtagText.setRawInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
        | InputType.TYPE_CLASS_TEXT
        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
    binding.hashtagText.setImeOptions(EditorInfo.IME_ACTION_DONE);
    binding.hashtagText.setOnEditorActionListener((v, actionId, event) -> {
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        VibrationEtil.vibrate(binding.hashtagText);

        String newHashtagText = binding.hashtagText.getText().toString();

        HashtagDragSection hashtagDragSection = new HashtagDragSection(notes, hashtagColor);
        hashtagDragSection.saveOnFirebaseRealtimeDatabase();

        @Nullable Hashtag existingHashtag = findHashtagWithText(newHashtagText);
        boolean isNewHashtagUnique = existingHashtag == null;
        String newHashtagId;
        if (isNewHashtagUnique) {
          Hashtag hashtag = new Hashtag(newHashtagText);
          newHashtagId = hashtag.getId();
          hashtag.saveOnFirebaseRealtimeDatabase();
        } else {
          newHashtagId = existingHashtag.getId();
        }

        mNote.setHashtagId(newHashtagId);
        mNote.setHashtagName(newHashtagText);
        mNote.setHashtagColor(hashtagColor);
        mNote.saveOnFirebaseRealtimeDatabase();

        binding.hashtagText.clearFocus();
        KeyboardEtil.hideKeyboard(binding.hashtagText);

        return true;
      }
      return false;
    });
  }

  private @Nullable
  Hashtag findHashtagWithText(String newHashtagText) {
    // optimization: create a hashmap instead <HashtagText -> Hashtag>
    for (Hashtag hashtag : mExistingHashtags) {
      if (TextUtils.equals(hashtag.getHashtagName(), newHashtagText)) {
        return hashtag;
      }
    }
    return null;
  }

  private List<String> getHashtagNameFromHashtagList(List<Hashtag> existingHashtags) {
    List<String> hashtagNames = new ArrayList<>();
    for (Hashtag hashtag : existingHashtags) {
      hashtagNames.add(hashtag.getHashtagName());
    }
    return hashtagNames;
  }

  public List<Hashtag> getExistingHashtags() {
    return mExistingHashtags;
  }
}
