package com.compscieddy.striate;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

public class HashtagAutocompleteArrayAdapter extends ArrayAdapter<String> implements Filterable {

  private final Object mLock = new Object();
  private List<String> mOriginalExistingHashtagNames;
  private ArrayFilter mFilter;
  private List<String> mExistingHashtagNames;
  private List<Integer> mOriginalExistingHashtagColors;
  private int mRandomNewHashtagColor;
  private CharSequence mPrefix;

  /**
   * <p>An array filter constrains the content of the array adapter with
   * a prefix. Each item that does not start with the supplied prefix
   * is removed from the list.</p>
   */
  private class ArrayFilter extends Filter {

    @Override
    protected FilterResults performFiltering(CharSequence prefix) {
      mPrefix = prefix;
      final FilterResults results = new FilterResults();

      if (mOriginalExistingHashtagNames == null) {
        synchronized (mLock) {
          mOriginalExistingHashtagNames = new ArrayList<>(mExistingHashtagNames);
        }
      }

      if (prefix == null || prefix.length() == 0) {
        final ArrayList<String> list;
        synchronized (mLock) {
          list = new ArrayList<>(mOriginalExistingHashtagNames);
        }
        results.values = list;
        results.count = list.size();
      } else {
        final String prefixString = prefix.toString().toLowerCase();

        final ArrayList<String> values;
        synchronized (mLock) {
          values = new ArrayList<>(mOriginalExistingHashtagNames);
        }

        final int count = values.size();
        final ArrayList<String> newValues = new ArrayList<>();

        for (int i = 0; i < count; i++) {
          final String value = values.get(i);
          final String valueText = value.toString().toLowerCase();

          // First match against the whole, non-splitted value
          if (valueText.startsWith(prefixString)) {
            newValues.add(value);
          } else {
            final String[] words = valueText.split(" ");
            for (String word : words) {
              if (word.startsWith(prefixString)) {
                newValues.add(value);
                break;
              }
            }
          }
        }

        results.values = newValues;
        results.count = newValues.size();
      }

      return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
      //noinspection unchecked
      mExistingHashtagNames = (List<String>) results.values;
      if (results.count > 0) {
        notifyDataSetChanged();
      } else {
        notifyDataSetInvalidated();
      }
    }
  }

  public HashtagAutocompleteArrayAdapter(
      @NonNull Context context,
      int textViewResourceId,
      @NonNull List<String> originalExistingHashtagNames,
      @NonNull List<Integer> originalExistingHashtagColors,
      @ColorInt int randomNewHashtagColor) {
    super(context, textViewResourceId, originalExistingHashtagNames);
    mOriginalExistingHashtagNames = originalExistingHashtagNames;
    mExistingHashtagNames = new ArrayList<>(mOriginalExistingHashtagNames);
    mOriginalExistingHashtagColors = originalExistingHashtagColors;
    mRandomNewHashtagColor = randomNewHashtagColor;
  }

  @Override
  public Filter getFilter() {
    if (mFilter == null) {
      mFilter = new ArrayFilter();
    }
    return mFilter;
  }

  @Nullable
  @Override
  public String getItem(int position) {
    return mExistingHashtagNames.get(position);
  }

  @Override
  public int getCount() {
    return mExistingHashtagNames.size();
  }

  @NonNull
  @Override
  public View getView(
      int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    View view = LayoutInflater
        .from(parent.getContext())
        .inflate(R.layout.simple_thin_dropdown, parent, false);
    TextView textView = view.findViewById(android.R.id.text1);
    View hashtagColorDot = view.findViewById(R.id.hashtag_dot_color);

    CharSequence text = mExistingHashtagNames.get(position);

    Spannable highlighted = new SpannableString(text);
    ForegroundColorSpan highlightSpan = new ForegroundColorSpan(mRandomNewHashtagColor);

    int spanStart = text.toString().toLowerCase().indexOf(mPrefix.toString().toLowerCase());
    Timber.d("text %s prefix %s", text.toString().toLowerCase(), mPrefix.toString().toLowerCase());
    int spanEnd = spanStart + mPrefix.length();
    if (spanStart >= 0 && spanEnd <= highlighted.length()) {
      highlighted.setSpan(highlightSpan, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    textView.setText(highlighted);

    int hashtagIndex = mOriginalExistingHashtagNames.indexOf(text);
    if (hashtagIndex != -1 && mOriginalExistingHashtagColors.get(hashtagIndex) != -1) {
      hashtagColorDot.setBackgroundColor(mOriginalExistingHashtagColors.get(hashtagIndex));
    }

    return view;
  }
}
