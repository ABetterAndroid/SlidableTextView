package com.joe.slidable.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

/**
 * Created by qiaorongzhu on 2016/10/28.
 */

public class SlidableTextView extends TextView {

    private float canvasTranslation;
    float x1 = 0;
    float y1 = 0;
    private Rect leftRect;
    private Rect rightRect;
    private Paint paint;
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private float xVel;
    private ValueAnimator smoothAnim;
    private boolean slideRight = false;
    private boolean rightSlideEnabled = true;
    private boolean leftSlideEnabled = true;
    private boolean otherLeftSidable = true;
    private boolean otherRightSidable = true;
    private Bitmap icon;
    private int height;
    private boolean slided = false;
    private OnIconClickListener onIconClickListener;

    public SlidableTextView(Context context) {
        super(context);
        prepare(context);
    }

    public SlidableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepare(context);
    }

    private void prepare(Context context) {
        setLongClickable(true);
        initPaint();
        mMaximumVelocity = ViewConfiguration.get(context)
                .getScaledMaximumFlingVelocity();
        mVelocityTracker = VelocityTracker.obtain();
    }

    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#FA7F7F"));
        paint.setStyle(Paint.Style.FILL);
    }

    public SlidableTextView setIcon(Bitmap icon) {
        this.icon = icon;
        invalidate();
        return this;
    }

    public SlidableTextView setOnIconClickListener(OnIconClickListener onIconClickListener) {
        this.onIconClickListener = onIconClickListener;
        return this;
    }

    public SlidableTextView setRightSlideEnabled(boolean rightSlideEnabled) {
        this.rightSlideEnabled = rightSlideEnabled;
        return this;
    }

    public SlidableTextView setLeftSlideEnabled(boolean leftSlideEnabled) {
        this.leftSlideEnabled = leftSlideEnabled;
        return this;
    }

    private void resizeIcon() {
        Matrix matrix = new Matrix();
        float scale;
        matrix.setScale(scale = 1.3f * height / 3f / icon.getHeight(), scale);
        int width = icon.getWidth();
        int height = icon.getHeight();
        this.icon = Bitmap.createBitmap(icon, 0, 0, width, height, matrix, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        Log.i("canvasTranslation ", canvasTranslation + "");
        canvas.translate(canvasTranslation, 0);

        super.onDraw(canvas);
        canvas.drawRect(leftRect, paint);
        canvas.drawRect(rightRect, paint);

        if (icon != null) {
            canvas.drawBitmap(icon, leftRect.centerX() / 2 - icon.getWidth() / 2, leftRect.centerY() - icon.getHeight() / 2, paint);
            canvas.drawBitmap(icon, rightRect.centerX() - rightRect.width() / 4 - icon.getWidth() / 2, rightRect.centerY() - icon.getHeight() / 2, paint);
        }

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float deltaX;
        float x2;
        float y2;

        if (canvasTranslation > 0) {
            otherLeftSidable = false;
        } else if (canvasTranslation < 0) {
            otherRightSidable = false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();

                break;
            case MotionEvent.ACTION_MOVE:

                x2 = event.getX();
                y2 = event.getY();

                if (canvasTranslation == 0 && slided) {
                    x1 = x2;
                    y1 = y2;
                    slided = false;
                    return super.onTouchEvent(event);
                }

                if (canvasTranslation > 0 && x1 < canvasTranslation) {
                    x1 = x2;
                    y1 = y2;
                    return super.onTouchEvent(event);
                }

                if (canvasTranslation < 0 && x1 > (getMeasuredWidth() + canvasTranslation)) {
                    x1 = x2;
                    y1 = y2;
                    return super.onTouchEvent(event);
                }

                if (!rightSlideEnabled && canvasTranslation == 0 && x2 - x1 > 0) {
                    x1 = x2;
                    y1 = y2;
                    return super.onTouchEvent(event);
                }

                if (!leftSlideEnabled && canvasTranslation == 0 && x2 - x1 < 0) {
                    x1 = x2;
                    y1 = y2;
                    return super.onTouchEvent(event);
                }

                if (Math.abs(y2 - y1) > Math.abs(x2 - x1)) {
                    x1 = x2;
                    y1 = y2;
                    return super.onTouchEvent(event);
                }

                if (smoothAnim != null && smoothAnim.isRunning()) {
                    smoothAnim.cancel();
                }

                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1, mMaximumVelocity);
                xVel = mVelocityTracker.getXVelocity();

                deltaX = x2 - x1;
                slideRight = deltaX > 0;
                x1 = x2;
                y1 = y2;
                canvasTranslation += deltaX;
                slided = true;
                if (canvasTranslation > getMeasuredWidth() / 2) {
                    canvasTranslation = getMeasuredWidth() / 2;
                } else if (canvasTranslation < -getMeasuredWidth() / 2) {
                    canvasTranslation = -getMeasuredWidth() / 2;
                }

                canvasTranslation = (!rightSlideEnabled || !otherRightSidable) && canvasTranslation > 0 ? 0 : canvasTranslation;
                canvasTranslation = (!leftSlideEnabled || !otherLeftSidable) && canvasTranslation < 0 ? 0 : canvasTranslation;

                invalidate();
                return true;
            case MotionEvent.ACTION_UP:

                otherRightSidable = otherLeftSidable = true;

                float endValue = 0;
                long flingDuration = 200;
                if ((canvasTranslation > getMeasuredWidth() / 16 && slideRight) || canvasTranslation > 7 * getMeasuredWidth() / 16) {
                    flingDuration = (long) Math.abs((getMeasuredWidth() / 2 - canvasTranslation) / xVel);
                    flingDuration = flingDuration > 200 ? 200 : flingDuration;
                    endValue = getMeasuredWidth() / 2;
                } else if (canvasTranslation > 0 && canvasTranslation < 7 * getMeasuredWidth() / 16 && !slideRight) {
                    endValue = 0;
                } else if ((canvasTranslation < -getMeasuredWidth() / 16 && !slideRight) || canvasTranslation < -7 * getMeasuredWidth() / 16) {
                    flingDuration = (long) Math.abs((-getMeasuredWidth() / 2 - canvasTranslation) / xVel);
                    flingDuration = flingDuration > 200 ? 200 : flingDuration;
                    endValue = -getMeasuredWidth() / 2;
                } else if (canvasTranslation < 0 && canvasTranslation > -7 * getMeasuredWidth() / 16 && slideRight) {
                    endValue = 0;
                }

                if (onIconClickListener != null && ((canvasTranslation == getMeasuredWidth() / 2 && event.getX() < canvasTranslation) || (canvasTranslation == -getMeasuredWidth() / 2 && event.getX() > getMeasuredWidth() + canvasTranslation))) {
                    onIconClickListener.onIconClick();
                    endValue = 0;
                    flingDuration = 200;
                }

                smoothToEnd(canvasTranslation, endValue, flingDuration);
                break;
        }

        return super.onTouchEvent(event);
    }

    private void smoothToEnd(float startValue, float endValue, long duration) {
        smoothAnim = ValueAnimator.ofFloat(startValue, endValue);
        smoothAnim.setDuration(duration);
        smoothAnim.setInterpolator(new DecelerateInterpolator());
        smoothAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                canvasTranslation = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        smoothAnim.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.height = h;
        if (h > 0) {
            resizeIcon();
        }
        leftRect = new Rect(-w, 0, 0, h);
        rightRect = new Rect(w, 0, 2 * w, h);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public interface OnIconClickListener {
        void onIconClick();
    }

}
