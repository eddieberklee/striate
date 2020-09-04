package com.compscieddy.striate.photo;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.compscieddy.eddie_utils.etil.Etil;
import com.compscieddy.eddie_utils.eui.FontCache;
import com.compscieddy.eddie_utils.eui.FontTextView;
import com.compscieddy.striate.R;
import com.compscieddy.striate.databinding.PhotoViewerFragmentBinding;
import com.compscieddy.striate.model.Entry;
import com.compscieddy.striate.util.DateUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import carbon.widget.FrameLayout;

import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class PhotoViewerTimelineManager {

  private final Resources res;
  private List<Entry> mEntries;
  private com.compscieddy.striate.databinding.PhotoViewerFragmentBinding binding;
  private Context c;

  private Map<String, View> mTimelineMarkerViewMap = new LinkedHashMap<>();

  public PhotoViewerTimelineManager(
      Context c,
      List<Entry> entries,
      PhotoViewerFragmentBinding binding) {
    this.c = c;
    res = c.getResources();
    mEntries = entries;
    this.binding = binding;
  }

  private static View generateTimelineMarkerView(Context c, Entry e) {
    LinearLayout markerView = new LinearLayout(c);
    markerView.setOrientation(LinearLayout.VERTICAL);
    markerView.setGravity(CENTER_HORIZONTAL);

    markerView.addView(
        generateExactTimeView(c, e),
        generateExactTimeLayoutParams());
    markerView.addView(
        generateCircleLayoutForPhotoOrTextEntry(c, e),
        generateCircleLayoutParams(c));
    markerView.addView(
        generateTimeAgoView(c, e),
        generateTimeAgoLayoutParams());

    return markerView;
  }

  private static View generateExactTimeView(Context c, Entry e) {
    Resources res = c.getResources();
    FontTextView exactTimeView = new FontTextView(c);
    exactTimeView.setText(DateUtil.getExactTimeStringWithShortAmPm(e.getCreatedAtMillis()));
    exactTimeView.setTextSize(14);
    exactTimeView.setTextColor(res.getColor(R.color.white_t80));
    exactTimeView.setTypeface(FontCache.get(c, FontCache.JOST_REGULAR));
    return exactTimeView;
  }

  private static LinearLayout.LayoutParams generateExactTimeLayoutParams() {
    LinearLayout.LayoutParams exactTimeParams = new LinearLayout.LayoutParams(
        WRAP_CONTENT,
        WRAP_CONTENT);
    exactTimeParams.gravity = CENTER_HORIZONTAL;
    exactTimeParams.bottomMargin = Etil.dpToPx(3);
    return exactTimeParams;
  }

  private static View generateTimeAgoView(Context c, Entry e) {
    Resources res = c.getResources();
    FontTextView timeAgoView = new FontTextView(c);
    timeAgoView.setText(DateUtil.getTimeAgoString(c, e.getCreatedAtMillis()));
    timeAgoView.setTextSize(13);
    timeAgoView.setTextColor(res.getColor(R.color.white_t60));
    timeAgoView.setTypeface(FontCache.get(c, FontCache.JOST_LIGHT));

    // saving space by hiding time ago
    timeAgoView.setVisibility(View.GONE);

    return timeAgoView;
  }

  private static LinearLayout.LayoutParams generateTimeAgoLayoutParams() {
    LinearLayout.LayoutParams timeAgoParams = new LinearLayout.LayoutParams(
        WRAP_CONTENT,
        WRAP_CONTENT);
    timeAgoParams.gravity = CENTER_HORIZONTAL;
    timeAgoParams.topMargin = Etil.dpToPx(3);
    return timeAgoParams;
  }

  private static LinearLayout.LayoutParams generateCircleLayoutParams(Context c) {
    Resources res = c.getResources();
    return new LinearLayout.LayoutParams(
        res.getDimensionPixelSize(R.dimen.photo_viewer_timeline_circle_size),
        res.getDimensionPixelSize(R.dimen.photo_viewer_timeline_circle_size));
  }

  private static View generateCircleLayoutForPhotoOrTextEntry(Context c, Entry e) {
    FrameLayout circleLayout = new FrameLayout(c);
    circleLayout.setCornerRadius(Etil.dpToPx(7));

    if (e.getIsPhoto()) {
      ImageView photoEntryView = new ImageView(c);
      Glide.with(c)
          .load(e.getUriString())
          .into(photoEntryView);

      photoEntryView.setScaleType(ImageView.ScaleType.CENTER_CROP);

      circleLayout.addView(photoEntryView);
    } else { // text entry
      FontTextView textEntryView = new FontTextView(c);
      textEntryView.setBackgroundColor(e.getColor());
      textEntryView.setText(e.getEntryText());

      textEntryView.setTextSize(10);
      textEntryView.setLetterSpacing(-0.05f);
      textEntryView.setLineSpacing(0, 0.75f);

      textEntryView.setPadding(Etil.dpToPx(2), 0, 0, 0);

      MarginLayoutParams textEntryParams = new MarginLayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT);
      circleLayout.addView(textEntryView, textEntryParams);
    }
    return circleLayout;
  }

  public void initEntryTimeline() {
    for (int i = 0; i < mEntries.size(); i++) {
      Entry e = mEntries.get(i);
      View timelineMarkerView = generateTimelineMarkerView(c, e);
      mTimelineMarkerViewMap.put(e.getId(), timelineMarkerView);

      MarginLayoutParams markerParams = new MarginLayoutParams(
          WRAP_CONTENT,
          WRAP_CONTENT);
      markerParams.rightMargin = Etil.dpToPx(6);
      binding.photoViewerEntryTimelineContainer.addView(timelineMarkerView, markerParams);
    }
  }

  public void updateCurrentEntry(Entry currentEntry) {
    for (String entryId : mTimelineMarkerViewMap.keySet()) {
      View markerView = mTimelineMarkerViewMap.get(entryId);

      boolean isCurrentEntry = TextUtils.equals(entryId, currentEntry.getId());
      markerView.setAlpha(isCurrentEntry ? 1.0f : 0.3f);

      boolean isRightOfContainer =
          markerView.getRight() > binding.photoViewerEntryTimelineScrollView.getScrollX() + binding.photoViewerEntryTimelineScrollView
              .getWidth();
      boolean isLeftOfContainer =
          markerView.getLeft() < binding.photoViewerEntryTimelineScrollView.getScrollX();

      if (isCurrentEntry) {
        if (isLeftOfContainer) {
          binding.photoViewerEntryTimelineScrollView.scrollTo(markerView.getLeft(), 0);
        } else if (isRightOfContainer) {
          binding.photoViewerEntryTimelineScrollView.scrollTo(markerView.getRight(), 0);
        }
      }
    }
  }

  public void removeTimelineMarkerViewAndDivider(String removedEntryId) {
    View removedMarkerView = mTimelineMarkerViewMap.get(removedEntryId);
    ViewGroup markerParent = (ViewGroup) removedMarkerView.getParent();

    // Remove the marker view (entry preview layout)
    markerParent.removeView(removedMarkerView);

    if (markerParent.getChildCount() == 0) {
      // if we're deleting the last marker view, then we will have no divider line
      return;
    }

  }
}
