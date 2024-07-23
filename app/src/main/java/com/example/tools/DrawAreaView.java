package com.example.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DrawAreaView extends View {

    private Paint paint, fillPaint, strokePaint;
    private Path path, fillPath, strokePath;

    public DrawAreaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawAreaView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setBackgroundColor(Color.BLACK);
        fillPath = new Path();
        strokePath = new Path();
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        path = new Path();
        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.RED);
        fillPaint.setStrokeWidth(3);
        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.RED);
        strokePaint.setStrokeWidth(3);
    }

    int startX, startY;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) event.getRawX();
                startY = (int) event.getRawY();
                path.moveTo(startX, startY);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(event.getRawX(), event.getRawY());
                invalidate();
                startX = (int) event.getRawX();
                startY = (int) event.getRawY();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(strokePath, strokePaint);
    }

    public void drawOnMe(Path path) {
        this.path.addPath(path);
        invalidate();
    }

    public void drawOnMe(Path path, Paint paint) {
        switch (paint.getStyle()) {
            case STROKE:
                strokePath.addPath(path);
                break;
            case FILL:
                fillPath.addPath(path);
                break;
        }
        invalidate();
    }
}
