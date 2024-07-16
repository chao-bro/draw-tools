package com.example.tools.ruler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RulerView extends View {

    private final int KE_DU_WIDTH = 8;

    private int numKD;

    private final Paint paint = new Paint();
    private final Paint bgPaint = new Paint();
    private float textHeight = 0f;
    private float minLength = 0f;


    public RulerView(Context context) {
        super(context);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    private void initData() {
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        bgPaint.setColor(Color.WHITE);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setStrokeWidth(3);
        bgPaint.setAntiAlias(true);
        bgPaint.setAlpha(100);
        Paint.FontMetrics fm = paint.getFontMetrics();
        //计算字体的高度
        textHeight = (int) (fm.bottom - fm.top);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        numKD = getWidth() / KE_DU_WIDTH - 2;
        minLength = getHeight() / 8f;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        //画直尺
        canvas.drawRoundRect(0f, 0f, getWidth(), getHeight(), 0.5f, 0.5f, bgPaint);
        canvas.drawRoundRect(0f, 0f, getWidth(), getHeight(), 0.5f, 0.5f, paint);
        //画刻度
        for (int i = 1; i <= numKD; i++) {
            int wid = i * KE_DU_WIDTH;
            if (i % 5 == 1) {
                canvas.drawLine(wid,
                        0f,
                        wid,
                        minLength * 2.5f,
                        paint);
                String text = (i - 1) / 5 + "";
                canvas.drawText(text,
                        wid - paint.measureText(text) / 2,
                        minLength * 2.5f + textHeight / 2,
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
