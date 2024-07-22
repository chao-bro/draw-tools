package com.example.tools.protractor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tools.adapters.AbstractBasicView;

public class ProtractorView extends AbstractBasicView {
    private Paint bgPaint;

    public ProtractorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.WHITE);
        bgPaint.setAlpha(120);
        bgPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        //将矩形缩小到原来的0.9倍
        float rectFCenX = getWidth() / 2f, rectFCenY = getHeight() / 2f,
                newWid = 0.9f * getWidth(), newHei = 0.9f * getHeight();
        @SuppressLint("DrawAllocation") RectF rectF = new RectF();
        rectF.set(rectFCenX - newWid / 2f, rectFCenY - newHei / 2,
                rectFCenX + newWid / 2f, rectFCenY + newHei / 2);
        float radius = Math.min(rectF.height(), rectF.width() / 2);
        float lineLen = radius / 14;
        float centerX = getWidth() / 2f;
        float centerY = rectF.height();
        canvas.drawCircle(centerX,centerY,5,bgPaint);
        bgPaint.setAlpha(50);
        path.reset();
        canvas.drawCircle(centerX, centerY, radius, bgPaint);
        canvas.save();
        canvas.clipOutRect(0f, centerY, getWidth(), getHeight());
        canvas.restore();

        //画刻度
        double unit = Math.PI / 180;
        float angle = 0f;
        while (angle <= 180f) {
            boolean needDrawText = false,isSpecial = false; // 是否需要绘制文本
            // 计算线段起始点的坐标
            float startX = centerX + radius * (float) Math.cos(angle * unit);
            float startY = centerY - radius * (float) Math.sin(angle * unit);

            float endX, endY; // 线段结束点的坐标
            float lineWidth; // 根据角度确定线段宽度

            if (angle % 5 == 0) {
                if (angle % 10 == 0) {
                    //长线
                    lineWidth = 2.5f * lineLen;
                    needDrawText = true;
                    if (angle % 180 == 0) {
                        //画满
                        isSpecial = true;
                    }
                } else {
                    //中长线
                    lineWidth = 1.5f * lineLen;
                }
            } else {
                //普通短线
                lineWidth = lineLen;
            }
            endX = centerX + (radius - lineWidth) * (float) Math.cos(angle * unit);
            endY = centerY - (radius - lineWidth) * (float) Math.sin(angle * unit);
            canvas.drawLine(startX,startY,endX,endY,paint);

            String valueString = (int)angle + "°"; // 构造角度值的字符串
            float textWidth = paint.measureText(valueString); // 测量文本的宽度
            float textHeight = paint.descent() - paint.ascent(); // 测量文本的高度
            if (isSpecial) {
                canvas.drawLine(centerX,centerY,
                        centerX + (radius - lineWidth * 1.5f - textHeight) * (float) Math.cos(angle * unit),
                        centerY - (radius - lineWidth * 1.5f - textHeight) * (float) Math.sin(angle * unit),
                        paint);
            }

            if (needDrawText) { // 如果需要绘制文本
                float startTextX = centerX + (radius - lineWidth * 1.5f) * (float) Math.cos(angle * unit);
                float startTextY = centerY - (radius - lineWidth * 1.5f) * (float) Math.sin(angle * unit);
                // 计算文本中心点的坐标
                float textCenterX = startTextX + textWidth / 2 * (float) Math.cos(angle * unit);
                float textCenterY = startTextY - textHeight / 2 * (float) Math.sin(angle * unit);
                float textX = textCenterX - textWidth / 2;
                float textY = textCenterY + textHeight / 2;
                // 绘制旋转后的文本
                paint.setTextSize(10);
                canvas.save();
                canvas.rotate(90f - angle, textCenterX, textCenterY);
                canvas.drawText(valueString, textX, textY, paint);
                canvas.restore();
            }
            angle++;
        }
    }
}
