package com.example.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tools.ruler.RulerLayout;

public class DrawAreaView extends View {

    private Paint paint;
    private Path path;

    public DrawAreaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawAreaView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setAlpha(127);
        path = new Path();
    }

    private int startX,startY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) event.getRawX();
                startY = (int) event.getRawY();
                path.moveTo(startX,startY);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(event.getRawX(),event.getRawY());
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
    }

    public void drawOnMe(Path path){
        this.path.addPath(path);
        invalidate();
    }
}
