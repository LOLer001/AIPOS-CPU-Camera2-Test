package com.techvision.aipos.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class DrawView extends View {
    private Paint mPaint = new Paint();
    private Rect mRect = new Rect();
    private float mX1, mY1, mX2, mY2;

    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f);
        mPaint.setColor(Color.RED);
    }

    /**
     * 设置矩形框的起始点和结束点的坐标
     */
    public void setPoints(float x1, float y1, float x2, float y2) {
        mX1 = x1;
        mY1 = y1;
        mX2 = x2;
        mY2 = y2;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int bitmapWidth = getWidth();
        int bitmapHeight = getHeight();
        Log.d("LCCC123", "onDraw: " + bitmapWidth + "x" + bitmapHeight);
        float left = Math.min(mX1, mX2) * bitmapWidth;
        float top = Math.min(mY1, mY2) * bitmapHeight;
        float right = Math.max(mX1, mX2) * bitmapWidth;
        float bottom = Math.max(mY1, mY2) * bitmapHeight;
        Log.d("LCCC123", "onDraw: " + (int) left + "x" + (int) right + "x" +(int) top + "x" +(int) bottom);

        mRect.left = (int) left;
        mRect.top = (int) top;
        mRect.right = (int) right;
        mRect.bottom = (int) bottom;

        canvas.drawRect(mRect, mPaint);
    }
}