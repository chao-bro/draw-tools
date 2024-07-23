package com.example.tools.ruler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tools.adapters.AbstractBasicView;
import com.example.tools.adapters.AbstractStrokeViewGroup;
import com.example.tools.application.MyApplication;

public class RulerView extends AbstractBasicView {


    private Paint bgPaint;

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        bgPaint = new Paint();
        bgPaint.setColor(Color.WHITE);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAlpha(60);
        bgPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        //画直尺
        canvas.drawRoundRect(0f, 0f, getWidth(), getHeight(), 0.5f, 0.5f, bgPaint);
        int numKD = getWidth() / interval - 10;
        numKD = numKD - numKD %10 + 1;
        float minLength = getHeight() / 8f;
        //画刻度
        for (int i = 0; i < numKD; i++) {
            int wid = (i + 5) * interval;
            float lineLen = 0f;
            boolean drawText = false;
            String text = i / 10 + "";
            if (i % 5 == 0) {
                if(i % 10 == 0){
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
            if(drawText){
                canvas.drawText(text,
                        wid - fontPaint.measureText(text) / 2,
                        lineLen + textHeight / 2f + 2,
                        fontPaint);
            }

        }

    }
}
