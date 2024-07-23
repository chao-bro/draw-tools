package com.example.tools.ruler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tools.adapters.AbstractBasicView;

public class RulerView extends AbstractBasicView {

    //不透明度
    private final int BACKGROUND_PAINT_ALPHA = 60;
    //圆角
    private final float RULER_VIEW_RADIUS = 0.5f;
    //留白宽度
    private final int SPACING = 5;
    private Paint bgPaint;

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        bgPaint = new Paint();
        bgPaint.setColor(PAINT_COLOR_WHITE);
        bgPaint.setStyle(PAINT_STYLE_FILL);
        bgPaint.setAlpha(BACKGROUND_PAINT_ALPHA);
        bgPaint.setAntiAlias(ANTIALIAS_TRUE);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect(0f, 0f, getWidth(), getHeight(),
                RULER_VIEW_RADIUS, RULER_VIEW_RADIUS,
                bgPaint);
        int numOfIntervals = getWidth() / interval - SPACING * 2;
        numOfIntervals = numOfIntervals - numOfIntervals % 10 + 1;
        float minLength = getHeight() / 8f;
        //画刻度
        for (int i = 0; i < numOfIntervals; i++) {
            int wid = (i + SPACING) * interval;
            float lineLen;
            boolean drawText = false;
            String text = i / 10 + "";
            if (i % 5 == 0) {
                if (i % 10 == 0) {
                    drawText = true;
                    lineLen = 2.5f * minLength;
                } else {
                    lineLen = 1.5f * minLength;
                }
            } else {
                //小刻度
                lineLen = minLength;
            }
            canvas.drawLine(wid,
                    0f,
                    wid,
                    lineLen,
                    paint);
            if (drawText) {
                canvas.drawText(text,
                        wid - fontPaint.measureText(text) / 2,
                        lineLen + textHeight / 2f + 2,
                        fontPaint);
            }

        }

    }
}
