package com.joe.slidable.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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

    private float canvasTranslate;
    float x1 = 0;
    private Rect leftRect;
    private Rect rightRect;
    private Paint paint;
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private float xVel;
    private ValueAnimator smoothAnim;
    private boolean slideRight = false;

    public SlidableTextView(Context context) {
        super(context);
    }

    public SlidableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
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

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        Log.i("canvasTranslate ", canvasTranslate + "");
        canvas.translate(canvasTranslate, 0);

        super.onDraw(canvas);
        canvas.drawRect(leftRect, paint);
        canvas.drawRect(rightRect, paint);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float deltaX = 0;
        float x2;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:

                if (smoothAnim != null && smoothAnim.isRunning()) {
                    smoothAnim.cancel();
                }

                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1, mMaximumVelocity);
                xVel = mVelocityTracker.getXVelocity();

                x2 = event.getX();
                deltaX = x2 - x1;
                slideRight = deltaX > 0;
                Log.i("deltaX ", slideRight + " " + deltaX);
                x1 = x2;
                canvasTranslate += deltaX;
                if (canvasTranslate > getMeasuredWidth() / 2) {
                    canvasTranslate = getMeasuredWidth() / 2;
                } else if (canvasTranslate < -getMeasuredWidth() / 2) {
                    canvasTranslate = -getMeasuredWidth() / 2;
                }

                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                float endValue = 0;
                long flingDuration = 200;
                Log.i("deltaX up ", slideRight + " " + deltaX);
                if ((canvasTranslate > getMeasuredWidth() / 16 && slideRight) || canvasTranslate > 7 * getMeasuredWidth() / 16) {
                    flingDuration = (long) Math.abs((getMeasuredWidth() / 2 - canvasTranslate) / xVel);
                    flingDuration = flingDuration > 200 ? 200 : flingDuration;
                    endValue = getMeasuredWidth() / 2;
                } else if (canvasTranslate > 0 && canvasTranslate < 7 * getMeasuredWidth() / 16 && !slideRight) {
                    endValue = 0;
                } else if ((canvasTranslate < -getMeasuredWidth() / 16 && !slideRight) || canvasTranslate < -7 * getMeasuredWidth() / 16) {
                    flingDuration = (long) Math.abs((-getMeasuredWidth() / 2 - canvasTranslate) / xVel);
                    flingDuration = flingDuration > 200 ? 200 : flingDuration;
                    endValue = -getMeasuredWidth() / 2;
                } else if (canvasTranslate < 0 && canvasTranslate > -7 * getMeasuredWidth() / 16 && slideRight) {
                    endValue = 0;
                }
                smoothToEnd(canvasTranslate, endValue, flingDuration);
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
                canvasTranslate = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        smoothAnim.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
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
}
