package com.example.tools.adapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tools.listener.OnDeleteListener;

public abstract class AbstractStrokeViewGroup extends RelativeLayout {

    protected Context context;
    protected Paint paint;
    protected Path path;
    protected int strokeWid = 3;
    protected OnDeleteListener onDeleteListener;
    protected abstract void init();
    protected float startX, startY;

    public AbstractStrokeViewGroup(Context context) {
        super(context);
        this.context = context;
        unPublicInit();
    }

    public AbstractStrokeViewGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        unPublicInit();
    }

    private void unPublicInit(){
        paint = new Paint();
        paint .setColor(Color.RED);
        paint.setStrokeWidth(strokeWid);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        path = new Path();
        init();
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener){
        this.onDeleteListener = onDeleteListener;
    }

    protected float dp2px(float dpValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float scale = metrics.density;
        return (dpValue * scale + 0.5f); // 加上0.5f是为了四舍五入
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path,paint);
        onDeleteListener.copyPath(path);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                path.moveTo(startX, startY);
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                path.lineTo(x, y);
                startX = x;
                startY = y;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                onDeleteListener.copyPath(path);
                path.reset();
                break;
            default:
                break;
        }
        return true;
    }
}
