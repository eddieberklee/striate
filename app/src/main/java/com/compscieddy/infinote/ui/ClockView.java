package com.compscieddy.infinote.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.compscieddy.infinote.R;
import com.compscieddy.eddie_utils.etil.ColorEtil;
import com.compscieddy.eddie_utils.etil.Etil;

import java.util.Calendar;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import timber.log.Timber;

import static android.graphics.Color.WHITE;
import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.Style.FILL;
import static android.graphics.Paint.Style.STROKE;

public class ClockView extends View {

  private static final boolean DEBUG = false;

  private Calendar mStartCalendar;
  private Calendar mEndCalendar;

  private Paint mWhiteCirclePaint;
  private Paint mHighlightAreaPaint;
  private Paint mHourLinePaint;
  private Paint mCurrentSecondsLinePaint;

  private Path mHighlightAreaPath;
  private Path mWhiteCirclePath;

  private RectF mWhiteCircleRect;

  private Context mContext;
  private int mShadowRadius;
  private boolean mShouldDrawSecondHand;
  private Calendar mCurrentSecondsCalendar;
  private int mShadowDy;

  public ClockView(Context context) {
    this(context, null);
  }

  public ClockView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    mContext = getContext();

    mWhiteCirclePaint = new Paint(ANTI_ALIAS_FLAG);
    mWhiteCirclePaint.setStyle(FILL);
    mWhiteCirclePaint.setColor(WHITE);

    mHighlightAreaPaint = new Paint(ANTI_ALIAS_FLAG);
    mHighlightAreaPaint.setStyle(FILL);

    int shadowColor = mContext.getResources().getColor(R.color.black_t20);
    mShadowRadius = Etil.dpToPx(5);
    mShadowDy = Etil.dpToPx(3);
    mWhiteCirclePaint.setShadowLayer(mShadowRadius, 0, mShadowDy, shadowColor);

    mHourLinePaint = new Paint(ANTI_ALIAS_FLAG);
    mHourLinePaint.setStyle(STROKE);
    mHourLinePaint.setStrokeWidth(Etil.dpToPx(2));
    mHourLinePaint.setStrokeCap(Cap.BUTT);

    mCurrentSecondsCalendar = Calendar.getInstance();

    mCurrentSecondsLinePaint = new Paint(ANTI_ALIAS_FLAG);
    mCurrentSecondsLinePaint.setStyle(STROKE);
    mCurrentSecondsLinePaint.setStrokeWidth(Etil.dpToPx(1));
    mCurrentSecondsLinePaint.setStrokeCap(Cap.ROUND);
    @ColorInt int red = mContext.getResources().getColor(R.color.pastel_red);
    mCurrentSecondsLinePaint.setColor(red);

    mHighlightAreaPath = new Path();
    mWhiteCirclePath = new Path();

    mWhiteCircleRect = new RectF();
  }

  public void setStartTimeMillis(long millis) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(millis);
    mStartCalendar = calendar;
  }

  public void setEndTimeMillis(long millis) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(millis);
    mEndCalendar = calendar;
  }

  public void setColor(@Px int color) {
    mHourLinePaint.setColor(color);
    mHighlightAreaPaint.setColor(ColorEtil.applyAlpha(color, 0.4f));
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mStartCalendar == null) {
      return;
    }

    if (mWhiteCircleRect.left == 0 && mWhiteCircleRect.top == 0 && mWhiteCircleRect.right == 0 && mWhiteCircleRect.bottom == 0) {
      // Insetting the white circle rect by the shadow radius on each side
      float shadowPadding = mShadowRadius + mShadowDy;
      mWhiteCircleRect.left = shadowPadding;
      mWhiteCircleRect.top = shadowPadding;
      mWhiteCircleRect.right = getWidth() - shadowPadding;
      mWhiteCircleRect.bottom = getHeight() - shadowPadding;
    }

    float hourAngle = getAndroidAngleFromHumanAngle(getHourAngleFromCalendar(mEndCalendar));

    mWhiteCirclePath.addOval(mWhiteCircleRect, Path.Direction.CW);

    float[] stopCoordinatesMinutes = getDistanceInPathCoordinates(mWhiteCirclePath, hourAngle / 360);
    float minutesX = stopCoordinatesMinutes[0];
    float minutesY = stopCoordinatesMinutes[1];

    canvas.drawCircle(getCenterX(), getCenterY(), getWhiteCircleRadius(), mWhiteCirclePaint);
    drawHighlightedRange(canvas);
    canvas.drawLine(getCenterX(), getCenterY(), minutesX, minutesY, mHourLinePaint);

    if (mShouldDrawSecondHand) {
      mCurrentSecondsCalendar.setTimeInMillis(System.currentTimeMillis());
      float currentSecondsHumanAngle = mCurrentSecondsCalendar.get(Calendar.SECOND) / 60f * 360f;
      float currentSecondsAndroidAngle = getAndroidAngleFromHumanAngle(currentSecondsHumanAngle);
      float currentSecondsAndroidFraction = currentSecondsAndroidAngle / 360f;
      float[] stopCoordinatesCurrentSeconds = getDistanceInPathCoordinates(mWhiteCirclePath, currentSecondsAndroidFraction);
      float currentSecondsX = stopCoordinatesCurrentSeconds[0];
      float currentSecondsY = stopCoordinatesCurrentSeconds[1];

      canvas.drawLine(getCenterX(), getCenterY(), currentSecondsX, currentSecondsY, mCurrentSecondsLinePaint);
    }
  }

  private float getCenterX() {
    return mWhiteCircleRect.left + getWhiteCircleRadius();
  }

  private float getCenterY() {
    return mWhiteCircleRect.top + getWhiteCircleRadius();
  }

  private float getWhiteCircleRadius() {
    return (mWhiteCircleRect.right - mWhiteCircleRect.left) / 2;
  }

  public void setShouldDrawCurrentSecondsLine(boolean shouldDrawCurrentSecondsLine) {
    mShouldDrawSecondHand = shouldDrawCurrentSecondsLine;
  }

  /**
   * Draws a highlighted range from the startCalendar time to the endCalendar time.
   * Highlighted range is capped at 12 hours.
   */
  private void drawHighlightedRange(Canvas canvas) {
    mHighlightAreaPath.reset();
    mHighlightAreaPath.setFillType(Path.FillType.EVEN_ODD);

    if (DEBUG) {
      Timber.d("drawing " + " mWhiteCircleRect.left: " + mWhiteCircleRect.left + " mWhiteCircleRect.right: " + mWhiteCircleRect.right + " mWhiteCircleRect.top: " + mWhiteCircleRect.top + " mWhiteCircleRect.bottom: " + mWhiteCircleRect.bottom);
      Timber.d("drawing " + " centerX: " + getCenterX() + " centerY: " + getCenterY());
    }

    float startAngle = getAndroidAngleFromHumanAngle(getHourAngleFromCalendar(mStartCalendar));
    float sweepAngle = getSweepAngle();

    mHighlightAreaPath.arcTo(mWhiteCircleRect, startAngle, sweepAngle);
    mHighlightAreaPath.lineTo(getCenterX(), getCenterY());
    mHighlightAreaPath.close();

    canvas.drawPath(mHighlightAreaPath, mHighlightAreaPaint);

    mHighlightAreaPath.reset();
  }

  /**
   * Subtracting the start angle with end angle didn't work out so well.
   * So here I'm getting the sweep angle by calculating the difference in time.
   * Then converting that into a fraction based on 12 hours in a clock face.
   * <p>
   * Note: capped at 360.
   */
  private float getSweepAngle() {
    int startSeconds = (int) (mStartCalendar.getTimeInMillis() / 1000);
    int endSeconds = (int) (mEndCalendar.getTimeInMillis() / 1000);

    int secondsInClockFace = 12 /* hours */ * 60 /* minutes in hour */ * 60 /* seconds in minute */;
    float sweepAngle = (float) (endSeconds - startSeconds) / secondsInClockFace * 360 /* angles in clockface */;

    if (DEBUG) {
      Timber.d("drawing " + " (endSeconds - startSeconds): " + (endSeconds - startSeconds));
      Timber.d("drawing " + " sweepAngle: " + sweepAngle);
    }

    if (sweepAngle > 360) {
      sweepAngle = 360;
    }

    return sweepAngle;
  }

  private float getAndroidAngleFromHumanAngle(float humanAngle) {
    return Etil.betterMod(humanAngle - 90, 360);
  }

  private float getHourAngleFromCalendar(Calendar calendar) {
    float oneHourAngle = 360f / 12;
    float hourAngle = calendar.get(Calendar.HOUR) * oneHourAngle;
    float minuteAngle = (calendar.get(Calendar.MINUTE) / 60f) * oneHourAngle;
    return hourAngle + minuteAngle;
  }

  /* If you're using this in an onDraw it may be a better idea to re-use the
    PathMeasure object (less memory allocations which = less GC operations)
    @return coordinates that is `fraction` distance into the path.
    @param fraction - percentage of length as a 0-1 float */
  private float[] getDistanceInPathCoordinates(Path path, float fraction) {
    return getDistanceInPathCoordinates(path, fraction, false);
  }

  /**
   * TODO: create alternate utility method that actually makes you pass in your own PathMeasure object
   */
  private float[] getDistanceInPathCoordinates(Path path, float distance, boolean isActualDistanceNotFraction) {
    PathMeasure pathMeasure = new PathMeasure(path, false);
    float[] pos = new float[2];
    if (isActualDistanceNotFraction) {
      pathMeasure.getPosTan(distance, pos, null);
    } else {
      pathMeasure.getPosTan(pathMeasure.getLength() * distance, pos, null);
    }
    return pos;
  }
}
