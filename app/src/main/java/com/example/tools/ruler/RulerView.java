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
        bgPaint.setStrokeWidth(3);
        bgPaint.setAntiAlias(true);
        bgPaint.setAlpha(100);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        //画直尺
        canvas.drawRoundRect(0f, 0f, getWidth(), getHeight(), 0.5f, 0.5f, bgPaint);
        canvas.drawRoundRect(0f, 0f, getWidth(), getHeight(), 0.5f, 0.5f, paint);
        int numKD = getWidth() / interval - 2;
        float minLength = getHeight() / 8f;
        //画刻度
        for (int i = 1; i <= numKD; i++) {
            int wid = i * interval;
            if (i % 5 == 1) {
                canvas.drawLine(wid,
                        0f,
                        wid,
                        minLength * 2.5f,
                        paint);
                String text = (i - 1) / 5 + "";
                canvas.drawText(text,
                        wid - paint.measureText(text) / 2,
                        minLength * 2.5f + (float) textHeight / 2,
                        paint);
            } else {
                //小刻度
                canvas.drawLine(wid,
                        0f,
                        wid,
                        minLength,
                        paint);
            }
        }

    }
}
