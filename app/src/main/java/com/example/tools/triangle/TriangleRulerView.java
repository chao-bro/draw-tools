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
    protected Paint bgPaint;

    @Override
    protected void init() {
        outPath = new Path();
        inPath = new Path();
        bgPaint = new Paint();
        bgPaint.setColor(Color.WHITE);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAlpha(60);
        bgPaint.setAntiAlias(true);
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
        canvas.drawPath(outPath, bgPaint);

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
        float minLen = inSideLen / 10;//取边宽的 1 / 10 为最短线的长度
        int num = getWidth() / interval - 16;//左右留白各半个单位长度
        num = num - num % 10 + 1;
        float lineLen = 0f;
        for (int i = 0; i < num; i++) {
            int x = (8 + i) * interval;
            String text = i / 10 + "";
            boolean needDrawText = false;
            if (i % 5 == 0) {
                if (i % 10 == 0) {
                    lineLen = minLen * 2f;
                    needDrawText = true;
                } else {
                    lineLen = minLen * 1.3f;
                }
            } else {
                lineLen = minLen;
            }

            canvas.drawLine(x, 0f, x, lineLen, paint);//横轴刻度
            canvas.drawLine(0f, getWidth() - x,
                    lineLen, getWidth() - x,
                    paint);//纵轴刻度

            if(needDrawText){
                canvas.drawText(text,
                        x - fontPaint.measureText(text) / 2, lineLen + textHeight / 2f,
                        fontPaint);
                canvas.save();
                canvas.rotate(-90, 0, 0);
                float textX = x - getWidth() - fontPaint.measureText(text) / 2;
                float textY = lineLen + textHeight / 2f + 2;
                canvas.drawText(text,
                        textX,
                        textY,
                        fontPaint);
                canvas.restore();
            }
        }
    }

}
