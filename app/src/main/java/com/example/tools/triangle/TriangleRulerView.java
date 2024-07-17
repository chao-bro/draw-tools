package com.example.tools.triangle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 三角尺的样式绘制
 */
public class TriangleRulerView extends View {

    private final String TAG = "TriangleRulerView";

    public TriangleRulerView(Context context) {
        super(context);
        init();
    }

    public TriangleRulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected Path outPath, inPath;
    private Paint drawPaint;

    private void init() {
        outPath = new Path();
        inPath = new Path();
        drawPaint = new Paint();

        drawPaint.setColor(Color.BLACK);
        drawPaint.setTextSize(12);
        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.STROKE);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        //绘制外三角
        float sideLen = Math.min(getHeight(), getWidth());
        outPath.reset();
        inPath.reset();
        outPath.moveTo(0f, 0f);
        outPath.lineTo(sideLen, 0f);
        outPath.lineTo(0f, sideLen);
        outPath.close();
        canvas.drawPath(outPath, drawPaint);
        //减去内三角
        float padding = sideLen / 3f;
        float inSideLen = (float) ((sideLen - padding) / (2 + Math.sqrt(2)));
        float insideXie = (float) (inSideLen * Math.sqrt(2) + inSideLen);
        inPath.moveTo(inSideLen, inSideLen);
        inPath.lineTo(sideLen - insideXie, inSideLen);
        inPath.lineTo(inSideLen, sideLen - insideXie);
        inPath.close();
        canvas.drawPath(inPath, drawPaint);

        //绘制刻度线 两条
        int widthKD = 8;
        float lineLen = inSideLen / 10;//取边宽的 1 / 10 为最短线的长度
        int num = getWidth() / widthKD - 10;//左右留白各一个单位长度
        Log.d(TAG, "onDraw: num = " + num);
        Paint.FontMetrics fm = drawPaint.getFontMetrics();
        float fh = fm.bottom - fm.top;
        for (int i = 0; i < num; i++) {
            int x = (int) (40f + 8 * i);
            if (i % 5 == 0) {
                String text = i / 5 + "";
                canvas.drawLine(x, 0f, x, lineLen * 2, drawPaint);
                canvas.drawLine(0f, getWidth() - x,
                        lineLen * 2, getWidth() - x,
                        drawPaint);
                canvas.drawText(text,
                        x - drawPaint.measureText(text) / 2, lineLen * 2 + fh / 2,
                        drawPaint);
                canvas.save();
                canvas.rotate(-90, 0, 0);
                float textX = x - getWidth() - drawPaint.measureText(text) / 2;
                float textY = lineLen * 2 + fh / 2;
                canvas.drawText(text,
                        textX,
                        textY,
                        drawPaint);
                canvas.restore();
            } else {
                canvas.drawLine(x, 0f, x, lineLen, drawPaint);
                canvas.drawLine(0f, getWidth() - x,
                        lineLen, getWidth() - x,
                        drawPaint);
            }
        }
    }

}
