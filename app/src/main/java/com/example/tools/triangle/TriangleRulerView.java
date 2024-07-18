package com.example.tools.triangle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tools.adapters.AbstractBasicView;
import com.example.tools.application.MyApplication;

/**
 * 三角尺的样式绘制
 */
public class TriangleRulerView extends AbstractBasicView {

    public TriangleRulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    protected Path outPath, inPath;

    @Override
    protected void init() {
        outPath = new Path();
        inPath = new Path();
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
        canvas.drawPath(outPath, paint);
        //减去内三角
        float padding = sideLen / 3f;
        float inSideLen = (float) ((sideLen - padding) / (2 + Math.sqrt(2)));
        float insideXie = (float) (inSideLen * Math.sqrt(2) + inSideLen);
        inPath.moveTo(inSideLen, inSideLen);
        inPath.lineTo(sideLen - insideXie, inSideLen);
        inPath.lineTo(inSideLen, sideLen - insideXie);
        inPath.close();
        canvas.drawPath(inPath, paint);
        //绘制刻度线 两条
        float lineLen = inSideLen / 10;//取边宽的 1 / 10 为最短线的长度
        int num = getWidth() / interval - 10;//左右留白各一个单位长度
        Paint.FontMetrics fm = paint.getFontMetrics();
        float fh = fm.bottom - fm.top;
        for (int i = 0; i < num; i++) {
            int x = (5 + i) * interval;
            if (i % 5 == 0) {
                String text = i / 5 + "";
                canvas.drawLine(x, 0f, x, lineLen * 2, paint);
                canvas.drawLine(0f, getWidth() - x,
                        lineLen * 2, getWidth() - x,
                        paint);
                canvas.drawText(text,
                        x - paint.measureText(text) / 2, lineLen * 2 + fh / 2,
                        paint);
                canvas.save();
                canvas.rotate(-90, 0, 0);
                float textX = x - getWidth() - paint.measureText(text) / 2;
                float textY = lineLen * 2 + fh / 2;
                canvas.drawText(text,
                        textX,
                        textY,
                        paint);
                canvas.restore();
            } else {
                canvas.drawLine(x, 0f, x, lineLen, paint);
                canvas.drawLine(0f, getWidth() - x,
                        lineLen, getWidth() - x,
                        paint);
            }
        }
    }

}
