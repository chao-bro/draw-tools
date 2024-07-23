package com.example.tools.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

public abstract class AbstractBasicView extends View {

    protected Paint paint,fontPaint;
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
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setAntiAlias(true);

        fontPaint = new TextPaint();
        fontPaint.setColor(Color.WHITE);
        fontPaint.setTextSize(20);
        fontPaint.setStyle(Paint.Style.FILL);
        fontPaint.setAntiAlias(true);
        Paint.FontMetrics fm = fontPaint.getFontMetrics();
        //计算字体的高度
        textHeight = (int) (fm.descent - fm.ascent);
        init();
    }

    protected void init(){}
}
