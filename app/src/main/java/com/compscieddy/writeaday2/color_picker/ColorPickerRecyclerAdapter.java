package com.compscieddy.writeaday2.color_picker;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.compscieddy.writeaday2.Colors;
import com.compscieddy.writeaday2.databinding.ColorPickerItemBinding;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * This is the adapter for showing the list of colors for users to select in the new habit dialog.
 */

public class ColorPickerRecyclerAdapter extends RecyclerView.Adapter<ColorPickerViewHolder> {

  public static final int COLOR_VIEW_HOLDER = 0;

  private ColorPickerCallBack mColorPickerCallback;
  private final int mRandomSelection;

  public ColorPickerRecyclerAdapter(ColorPickerCallBack callback) {
    mColorPickerCallback = callback;
    mRandomSelection = (int) (Math.random() * Colors.colorResIds.length - 1);
  }

  @NonNull
  @Override
  public ColorPickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ColorPickerViewHolder(
        mColorPickerCallback,
        mRandomSelection,
        ColorPickerItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ColorPickerViewHolder holder, int position) {
    holder.setPosition(position);
  }

  @Override
  public int getItemCount() {
    return Colors.colorResIds.length;
  }

  public interface ColorPickerCallBack {
    void onColorSelected(int color);
  }

  @Override
  public int getItemViewType(int position) {
    return COLOR_VIEW_HOLDER;
  }
}
