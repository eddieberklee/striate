package com.compscieddy.infinote.color_picker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import com.compscieddy.infinote.Colors;
import com.compscieddy.infinote.R;
import com.compscieddy.infinote.color_picker.ColorPickerRecyclerAdapter.ColorPickerCallBack;
import com.compscieddy.infinote.databinding.ColorPickerItemBinding;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings("WeakerAccess")
public class ColorPickerViewHolder extends RecyclerView.ViewHolder {

  @BindView(R.id.color_circle) View mColorCircle;
  @BindView(R.id.color_circle_button) View mColorCircleButton;

  private final ColorPickerCallBack mColorPickerCallback;
  private @ColorInt int mColor;
  private final Context c;
  private int mRandomlySelectedPosition;
  private com.compscieddy.infinote.databinding.ColorPickerItemBinding binding;
  private int mPosition;
  private final Resources res;

  public ColorPickerViewHolder(
      ColorPickerCallBack colorPickerCallback,
      int randomlySelectedPosition,
      ColorPickerItemBinding binding) {
    super(binding.getRoot());
    this.binding = binding;
    c = itemView.getContext();
    res = c.getResources();

    mColorPickerCallback = colorPickerCallback;
    mRandomlySelectedPosition = randomlySelectedPosition;
    ButterKnife.bind(ColorPickerViewHolder.this, itemView);
    init();
  }

  public void requestFocusOnColorItem() {
    mColorCircleButton.requestFocus();
  }

  public @ColorInt int getColor() {
    return mColor;
  }

  void setPosition(int position) {
    mPosition = position;
    mColor = Colors.getColorForPosition(c, position);
    initHabitRecord();
    if (mPosition == mRandomlySelectedPosition) {
      mColorCircleButton.requestFocus();
    }
  }

  private void init() {
    mColorCircleButton.setOnFocusChangeListener((view, isFocused) -> {
      if (isFocused) {
        mColorPickerCallback.onColorSelected(mColor);
      }
    });
  }

  private void initHabitRecord() {
    GradientDrawable gradientDrawable = (GradientDrawable) c.getResources().getDrawable(R.drawable.color_picker_circle);
    gradientDrawable.setColor(mColor);
    mColorCircle.setBackground(gradientDrawable);
  }
}
