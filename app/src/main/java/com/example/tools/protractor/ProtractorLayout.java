package com.example.tools.protractor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tools.R;
import com.example.tools.adapters.AbstractStrokeViewGroup;

public class ProtractorLayout extends AbstractStrokeViewGroup {
    private static final String TAG = "ProtractorLayout";
    private int width, height;
    private Point circle1, circle2;
    private final int CIRCLE_RADIUS = 20;
    private Paint circlePaint, linePaint;
    private Path linePath;

    public ProtractorLayout(Context context) {
        super(context);
    }

    public ProtractorLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        inflateAndInitViews();
        circle1 = new Point();
        circle2 = new Point();
        circlePaint = new Paint();
        linePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.FILL);
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.BLUE);
        linePaint.setStrokeWidth(3);
        linePaint.setStyle(Paint.Style.STROKE);
        //设置屏幕自适应大小
        height = screenHeight / 2;
        width = screenWidth / 3;
        LayoutParams params = (LayoutParams) transformer.getLayoutParams();
        if (height > width) {
            height = (int) (width / 2f + dp2px(20));
        } else {
            width = (int) ((height - dp2px(20)) * 2);
        }
        params.height = height;
        params.width = width;
        transformer.setLayoutParams(params);
        //设置偏移
        float transX = (screenWidth - width) / 2f;
        float transY = (screenHeight - height) / 2f;
        transformer.setTranslationX(transX);
        transformer.setTranslationY(transY);
        setListeners();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw: protractor height is = " + protractor.getHeight());
        int ptcHeight = protractor.getHeight();
        float centerY = ptcHeight * 0.9f + transformer.getTranslationY();
        float centerX = transformer.getTranslationX() + width / 2f;
        circle1.set((int) (centerX),
                (int) (transformer.getTranslationY() - CIRCLE_RADIUS));
        circle2.set((int) (transformer.getTranslationX() + width + CIRCLE_RADIUS),
                (int) (centerY));
        canvas.drawCircle(circle1.x, circle1.y, CIRCLE_RADIUS - 1, circlePaint);
        canvas.drawCircle(circle2.x, circle2.y, CIRCLE_RADIUS - 1, circlePaint);
        linePath.reset();
        linePath.moveTo(centerX, centerY);
        linePath.lineTo(circle1.x, circle1.y);
        linePath.moveTo(centerX, centerY);
        linePath.lineTo(circle2.x, circle2.y);
        canvas.drawPath(linePath, linePaint);
    }

    float sx, sy;
    @SuppressLint("ClickableViewAccessibility")
    private void setListeners() {
        //关闭
        vClose.setOnClickListener(view -> {
            ((FrameLayout) ProtractorLayout.this.getParent()).removeView(ProtractorLayout.this);
            Log.d(TAG, "click to close protractor");
        });

        //移动
        protractor.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    sx = motionEvent.getRawX();
                    sy = motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float rawX = motionEvent.getRawX();
                    float rawY = motionEvent.getRawY();
                    float dx = rawX - sx;
                    float dy = rawY - sy;
                    transformer.setTranslationX(transformer.getTranslationX() + dx);
                    transformer.setTranslationY(transformer.getTranslationY() + dy);
                    invalidate();
                    sx = rawX;
                    sy = rawY;
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        });

        //两个点的拖动

        //确认按钮


    }

    private TextView tvAngle;
    private View vClose, vConfirm;
    private ProtractorTransformer transformer;
    private ProtractorView protractor;

    private void inflateAndInitViews() {
        LayoutInflater.from(context).inflate(R.layout.protractor_view, this);
        setBackgroundColor(Color.TRANSPARENT);
        tvAngle = findViewById(R.id.degree);
        vClose = findViewById(R.id.close);
        vConfirm = findViewById(R.id.confirm);
        transformer = findViewById(R.id.transformer);
        protractor = findViewById(R.id.protractor);
    }
}
