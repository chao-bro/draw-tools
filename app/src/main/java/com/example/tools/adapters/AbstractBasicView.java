package com.example.tools.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

public abstract class AbstractBasicView extends View {

    protected Paint paint;
    protected Path path;
    protected Context context;
    protected int interval;
    protected int textHeight;

    public AbstractBasicView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        interval = (int) (dm.density * 4 + 0.5);
        unPublicInit();
    }

    private void unPublicInit() {
        paint = new Paint();
        path = new Path();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(14);
        paint.setAntiAlias(true);
        Paint.FontMetrics fm = paint.getFontMetrics();
        //计算字体的高度
        textHeight = (int) (fm.bottom - fm.top);
        init();
    }

    protected void init(){}

    protected float dp2px(float dpValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float scale = metrics.density;
        return (dpValue * scale + 0.5f); // 加上0.5f是为了四舍五入
    }
}
