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

    protected final int PAINT_COLOR_WHITE = Color.WHITE;
    protected final Paint.Style PAINT_STYLE_STROKE = Paint.Style.STROKE;
    protected final Paint.Style PAINT_STYLE_FILL = Paint.Style.FILL;
    protected final int PAINT_STROKE_WIDTH = 3;
    protected final boolean ANTIALIAS_TRUE = true;
    protected final int FONT_PAINT_TEXT_SIZE = 20;
    protected Paint paint,fontPaint;
    protected Path path;
    protected Context context;
    protected int interval = 0;
    protected int textHeight = 0;

    public AbstractBasicView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        unPublicInit();
    }

    private void unPublicInit() {
        paint = new Paint();
        path = new Path();
        paint.setColor(PAINT_COLOR_WHITE);
        paint.setStyle(PAINT_STYLE_STROKE);
        paint.setStrokeWidth(PAINT_STROKE_WIDTH);
        paint.setAntiAlias(ANTIALIAS_TRUE);

        fontPaint = new TextPaint();
        fontPaint.setColor(PAINT_COLOR_WHITE);
        fontPaint.setTextSize(FONT_PAINT_TEXT_SIZE);
        fontPaint.setStyle(PAINT_STYLE_FILL);
        fontPaint.setAntiAlias(ANTIALIAS_TRUE);
        Paint.FontMetrics fm = fontPaint.getFontMetrics();
        textHeight = (int) (fm.descent - fm.ascent);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        interval = (int) (dm.density * 4 + 0.5);
        init();
    }

    protected void init(){}
}
