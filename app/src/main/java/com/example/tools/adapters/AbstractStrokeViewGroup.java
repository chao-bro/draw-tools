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

    //画笔颜色
    protected final int PAINT_COLOR = Color.RED;
    //画笔样式
    protected final Paint.Style PAINT_STYLE = Paint.Style.STROKE;
    //抗锯齿
    protected final boolean PAINT_ANTIALIAS_TRUE = true;
    //画笔宽度
    protected final int PAINT_STROKE_WIDTH = 3;
    //两个刻度之间的宽度
    protected int interval = 0;
    protected final int BACKGROUND_COLOR = Color.TRANSPARENT;

    protected Context context;
    protected Paint paint;
    protected Path path;
    protected OnDeleteListener onDeleteListener;
    protected int screenHeight, screenWidth;

    protected abstract void init();

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

    private void unPublicInit() {
        paint = new Paint();
        paint.setColor(PAINT_COLOR);
        paint.setStrokeWidth(PAINT_STROKE_WIDTH);
        paint.setStyle(PAINT_STYLE);
        paint.setAntiAlias(PAINT_ANTIALIAS_TRUE);
        path = new Path();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;
        interval = (int) (dm.density * 4 + 0.5);
        //设置背景色，防止上边距被裁掉
        setBackgroundColor(BACKGROUND_COLOR);
        init();
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
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
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

}
