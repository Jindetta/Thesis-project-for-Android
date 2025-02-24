package com.piceasoft.thesis_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class DrawableView extends View {

    private Paint mPaint;
    private Path mPath;

    public DrawableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(75);

        mPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawToCanvas(canvas);
        super.onDraw(canvas);
    }

    @Override
    @SuppressWarnings("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(event.getX(), event.getY());
                break;
            default:
                return super.onTouchEvent(event);
        }

        invalidate();
        return true;
    }

    public boolean isEmpty() {
        return mPath.isEmpty();
    }

    public void reset() {
        mPath.reset();
        invalidate();
    }

    public Bitmap getRenderView() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);

        drawToCanvas(new Canvas(bitmap));

        return bitmap;
    }

    private void drawToCanvas(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        canvas.drawPath(mPath, mPaint);
    }
}
