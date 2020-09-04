package com.compscieddy.striate.entry;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.striate.R;
import com.compscieddy.striate.databinding.EntryItemBinding;
import com.compscieddy.striate.god.DayOfWeekManager;
import com.compscieddy.striate.model.Entry;
import com.compscieddy.striate.util.EntryColorHelper;
import com.google.android.gms.common.internal.Asserts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class EntryManager {

  enum EntryType {
    TEXT,
    PHOTO,
  }

  private final Context c;
  private final Resources res;
  private final Handler mHandler;
  private final FragmentManager mFragmentManager;

  private Map<String, EntryHolder> mEntryHolderMap = new LinkedHashMap<>();
  private DayOfWeekManager mDayOfWeekManager;
  private View mNewEntryButton;
  private ViewGroup mEntryViewsContainer;
  private NestedScrollView mEntryScrollView;
  private int mEntryScrollViewHeight;
  private int mDayOfWeekIndex;
  private List<Entry> mEntries = new ArrayList<>();
  private int mNumEntriesLastColorCalculation;

  public EntryManager(
      Context c,
      FragmentManager fragmentManager,
      DayOfWeekManager dayOfWeekManager,
      int entryScrollViewHeight,
      View newEntryButton,
      ViewGroup entryViewsContainer,
      NestedScrollView entryScrollView,
      int dayOfWeekIndex) {
    this.c = c;
    res = c.getResources();
    mFragmentManager = fragmentManager;
    mDayOfWeekManager = dayOfWeekManager;
    mNewEntryButton = newEntryButton;
    mEntryViewsContainer = entryViewsContainer;
    mEntryScrollView = entryScrollView;
    mEntryScrollViewHeight = entryScrollViewHeight;
    mDayOfWeekIndex = dayOfWeekIndex;
    mHandler = new Handler(Looper.getMainLooper());
  }

  public static void updateEntryHeight(Entry entry, View entryView) {
    ViewGroup entryRowContainer = (ViewGroup) entryView.getParent();
    if (!entry.getIsPhoto() && entryRowContainer.getChildCount() == 1) {
      int entryHeight = getEntryHeight(0, 1, entryView.findViewById(R.id.entry_autocomplete_view));
      updateEntryHeight(entryRowContainer, entryHeight);
    }
  }

  public static void updateEntryHeight(ViewGroup entryRowContainer, int entryHeight) {
    ViewGroup.LayoutParams entryRowContainerParams = entryRowContainer.getLayoutParams();
    entryRowContainerParams.height = entryHeight;
    entryRowContainer.setLayoutParams(entryRowContainerParams);
  }

  private static int getEntryHeight(
      int numPhotos,
      int numTexts,
      @Nullable AutoCompleteTextView entryAutoCompleteTextView) {
    final int minEntryHeight = Etil.dpToPx(80);
    final int smallEntryHeight = Etil.dpToPx(120);
    final int mediumEntryHeight = Etil.dpToPx(180);
    final int largeEntryHeight = Etil.dpToPx(240);
    int entryHeight;

    if (numPhotos == 1 && numTexts == 1) {
      entryHeight = mediumEntryHeight;
    } else if (numTexts == 2) {
      entryHeight = mediumEntryHeight;
    } else if (numTexts == 1) {
      Asserts.checkNotNull(entryAutoCompleteTextView);
      int lineCount = entryAutoCompleteTextView.getLineCount();
      if (lineCount <= 2) {
        entryHeight = minEntryHeight;
      } else if (lineCount <= 4) {
        entryHeight = smallEntryHeight;
      } else if (lineCount <= 6) {
        entryHeight = mediumEntryHeight;
      } else { // lineCount is 7 or more
        entryHeight = largeEntryHeight;
      }
    } else if (numPhotos == 1) {
      entryHeight = largeEntryHeight;
    } else if (numPhotos == 2) {
      entryHeight = mediumEntryHeight;
    } else { // numPhotos == 3
      entryHeight = mediumEntryHeight;
    }
    return entryHeight;
  }

  public void addEntry(Entry entry) {
    mEntries.add(entry);

    EntryHolder newEntryHolder = createEntryHolder(mEntryScrollView);
    newEntryHolder.setEntry(mEntries, entry);

    mEntryHolderMap.put(entry.getId(), newEntryHolder);

    LinearLayout entryRowContainer = getPreviousOrNewRowContainer(mEntryViewsContainer, entry);
    LinearLayout sparklineRowContainer =
        getPreviousOrNewRowContainer(mDayOfWeekManager.getDayOfWeekSparklinesContainer(
            mDayOfWeekIndex), entry);

    View sparklineView = mDayOfWeekManager.generateNewSparklineView(entry);

    entryRowContainer.addView(newEntryHolder.itemView, getEntryViewParams(entry));
    sparklineRowContainer.addView(
        sparklineView,
        mDayOfWeekManager.getSparklineViewLayoutParams(entry));

    int remainingSpace = getRemainingSpace(entryRowContainer);
    entryRowContainer.setTag(remainingSpace - (int) getEntryViewLayoutParams(newEntryHolder).weight);

    updateEntryColorsWithAnimation();
    updateEntryHeightsBasedOnLineCount();
  }

  public void removeEntry(Entry removedEntry) {
    EntryHolder removedEntryHolder = mEntryHolderMap.get(removedEntry.getId());
    removedEntryHolder.clearFocus();

    int i;
    for (i = 0; i < mEntries.size(); i++) {
      Entry e = mEntries.get(i);
      if (TextUtils.equals(e.getId(), removedEntry.getId())) {
        break;
      }
    }
    mEntries.remove(i);

    mEntryHolderMap.remove(removedEntry.getId());

    View entryView = removedEntryHolder.itemView;

    ViewGroup rowContainer = (ViewGroup) entryView.getParent();
    boolean isOnlyEntryInContainer = rowContainer.getChildCount() == 1;
    if (!isOnlyEntryInContainer) {
      int viewWeight = entryView.getTag() == null ? 0 : (int) entryView.getTag();
      rowContainer.setTag((int) rowContainer.getTag() + viewWeight);
    }

    removeViewOrRowContainer(entryView, mEntryViewsContainer);
    removeViewOrRowContainer(mDayOfWeekManager.getSparklineView(
        removedEntry.getId(),
        mDayOfWeekIndex), mDayOfWeekManager.getDayOfWeekSparklinesContainer(mDayOfWeekIndex));

    updateEntryColorsWithAnimation();
    updateEntryHeightsBasedOnLineCount();

    removedEntry.deleteOnFirebase();
  }

  /**
   * Remove the view if there are other children in the row container.
   * Remove the row container itself if there is no other child.
   */
  private void removeViewOrRowContainer(View view, ViewGroup parentOfRowContainer) {
    ViewGroup rowContainer = (ViewGroup) view.getParent();
    boolean isOnlyEntryInContainer = rowContainer.getChildCount() == 1;

    if (isOnlyEntryInContainer) {
      // remove the row container
      parentOfRowContainer.removeView(rowContainer);
    } else {
      // remove the view
      rowContainer.removeView(view);
    }
  }

  private LinearLayout.LayoutParams getEntryViewLayoutParams(EntryHolder newEntryHolder) {
    return (LinearLayout.LayoutParams) newEntryHolder.itemView
        .getLayoutParams();
  }

  private int getRemainingSpace(LinearLayout entryRowContainer) {
    return entryRowContainer.getTag() == null ? 3 :
        (int) entryRowContainer.getTag();
  }

  /**
   * This can be used for both entry views as well as sparkline views, as long as you supply the
   * correct parent view of the previous or new row container.
   */
  private LinearLayout getPreviousOrNewRowContainer(
      ViewGroup parentOfRowContainer,
      Entry entry) {
    boolean shouldAddToPreviousContainer = doesPreviousContainerHaveSpace(entry);
    if (shouldAddToPreviousContainer) {
      return getLastRowContainer(parentOfRowContainer);
    } else {
      LinearLayout rowContainer = generateRowContainer();
      parentOfRowContainer.addView(rowContainer);
      return rowContainer;
    }
  }

  private LinearLayout.LayoutParams getEntryViewParams(Entry entry) {
    LinearLayout.LayoutParams itemViewParams =
        new LinearLayout.LayoutParams(0, MATCH_PARENT);

    final int margin = res.getDimensionPixelSize(R.dimen.entry_spacing);
    itemViewParams.setMargins(margin, margin, margin, margin);

    itemViewParams.weight = entry.getIsPhoto() ? 1 : 2;
    return itemViewParams;
  }

  /**
   * Note this is used for both entry as well as sparkline views.
   */
  private LinearLayout generateRowContainer() {
    LinearLayout layout = new LinearLayout(c);
    layout.setOrientation(LinearLayout.HORIZONTAL);
    layout.setClipToPadding(false);
    layout.setClipChildren(false);
    return layout;
  }

  private LinearLayout getLastRowContainer(ViewGroup parentOfRowContainer) {
    return (LinearLayout) parentOfRowContainer.getChildAt(parentOfRowContainer.getChildCount() - 1);
  }

  private boolean doesPreviousContainerHaveSpace(Entry entry) {
    EntryType entryType = entry.getIsPhoto() ? EntryType.PHOTO : EntryType.TEXT;
    if (mEntryViewsContainer.getChildCount() == 0) {
      return false;
    }

    ViewGroup previousRowContainer =
        (ViewGroup) mEntryViewsContainer.getChildAt(mEntryViewsContainer.getChildCount() - 1);

    if (previousRowContainer.getTag() == null) {
      return false;
    }

    int remainingSpace = (int) previousRowContainer.getTag();
    return (entryType == EntryType.TEXT && remainingSpace >= 2)
        || (entryType == EntryType.PHOTO && remainingSpace >= 1);
  }

  private void updateEntryColorsWithAnimation() {
    List<Entry> textOnlyEntries = new ArrayList<>();
    for (Entry e : mEntries) {
      if (!e.getIsPhoto()) {
        textOnlyEntries.add(e);
      }
    }

    boolean shouldSkipExpensiveFirestoreWrite =
        mNumEntriesLastColorCalculation == textOnlyEntries.size();
    mNumEntriesLastColorCalculation = textOnlyEntries.size();
    if (shouldSkipExpensiveFirestoreWrite) {
      return;
    }

    // Firestore writes are too expensive, don't update the colors if the gradient is very smooth
    // already
    if (textOnlyEntries.size() > 10) {
      return;
    }

    int i = 0;
    for (Entry textEntry : textOnlyEntries) {
      EntryHolder entryHolder = mEntryHolderMap.get(textEntry.getId());

      // Set background color
      View v = entryHolder.itemView;
      int entryColor = EntryColorHelper.getEntryColor(
          c,
          i,
          textOnlyEntries.size(),
          mDayOfWeekIndex);
      v.setBackgroundColor(entryColor);

      // Update text color
      entryHolder.updateTextAndButtonColors(entryColor);

      // Save on Firestore
      Entry entry = entryHolder.getEntry();
      entry.setColor(entryColor);
      entry.saveFieldOnFirestore(Entry.FIELD_COLOR, entry.getColor());

      i++;
    }
  }

  private EntryHolder createEntryHolder(NestedScrollView entryScrollView) {
    return new EntryHolder(
        mFragmentManager,
        entryScrollView,
        EntryItemBinding.inflate(
            LayoutInflater.from(c),
            null,
            false));
  }

  public void updateEntry(Entry modifiedEntry) {
    mDayOfWeekManager.updateSparklineColor(modifiedEntry, mDayOfWeekIndex);

    mEntryHolderMap.get(modifiedEntry.getId()).setEntry(mEntries, modifiedEntry);
  }

  /**
   * 3 and less lines: min height
   * 6 and less lines: medium height
   * 7+: max height
   */
  private void updateEntryHeightsBasedOnLineCount() {
    for (int i = 0; i < mEntryViewsContainer.getChildCount(); i++) {
      ViewGroup entryRowContainer = (ViewGroup) mEntryViewsContainer.getChildAt(i);

      if (entryRowContainer == mEntryViewsContainer.findViewById(R.id.facebook_ad_container)) {
        continue;
      }

      List<View> entryTextViews = new ArrayList<>();
      List<View> entryPhotoViews = new ArrayList<>();

      for (int j = 0; j < entryRowContainer.getChildCount(); j++) {
        View entryView = entryRowContainer.getChildAt(j);
        LinearLayout.LayoutParams entryParams =
            (LinearLayout.LayoutParams) entryView.getLayoutParams();

        if (entryParams.weight == 1) {
          entryPhotoViews.add(entryView);
        } else {
          entryTextViews.add(entryView);
        }
      }


      int numPhotos = entryPhotoViews.size();
      int numTexts = entryTextViews.size();

      if (numTexts == 1) {
        // TODO: could have weird bugs here since line count is dependent on exactly what the
        //  weight is. it could hop from one to the other if it's in a sweet spot.
        AutoCompleteTextView entryAutoCompleteTextView = entryTextViews
            .get(0)
            .findViewById(R.id.entry_autocomplete_view);
        entryAutoCompleteTextView.post(() -> {
          // We need to post since .getLineCount() only works after a layout pass, which the
          // .post() guarantees.
          int entryHeight = getEntryHeight(numPhotos, numTexts, entryAutoCompleteTextView);
          updateEntryHeight(entryRowContainer, entryHeight);
        });
      } else {
        int entryHeight = getEntryHeight(numPhotos, numTexts, null);
        updateEntryHeight(entryRowContainer, entryHeight);
      }
    }
  }
}
