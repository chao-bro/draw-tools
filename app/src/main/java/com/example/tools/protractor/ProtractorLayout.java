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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tools.R;
import com.example.tools.adapters.AbstractStrokeViewGroup;

import java.util.Locale;

public class ProtractorLayout extends AbstractStrokeViewGroup {
    private static final String TAG = "ProtractorLayout";

    //量角器的长宽
    private int width, height;
    private Paint linePaint;
    private Path linePath;
    private float centerX, centerY;
    private boolean isFirstLoad;

    public ProtractorLayout(Context context) {
        super(context);
    }

    public ProtractorLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        inflateAndInitViews();

        isFirstLoad = true;
        linePath = new Path();
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.RED);
        linePaint.setStrokeWidth(3);
        linePaint.setAlpha(120);
        linePaint.setStyle(Paint.Style.STROKE);

        //设置屏幕自适应大小
        height = screenHeight / 3;
        width = screenWidth / 3;
        LayoutParams params = (LayoutParams) transformer.getLayoutParams();
        if (height > width) {
            height = width / 2;
        } else {
            width = height * 2;
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

    private float c1CX, c2CX, c1CY, c2CY;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //根据height和width求得两个圆的中心
        if(isFirstLoad){
            c1CX = width / 2f + transformer.getTranslationX();
            centerX = c1CX;
            c1CY = transformer.getTranslationY() - vCircle1.getHeight() / 2f;
            c2CX = transformer.getTranslationX() + width + vCircle2.getWidth() / 2f;
            c2CY = 0.9f * height + transformer.getTranslationY();
            centerY = c2CY;
            vCircle1.setTranslationX(c1CX - vCircle1.getWidth() / 2f);
            vCircle1.setTranslationY(c1CY - vCircle1.getHeight() / 2f);
            vCircle2.setTranslationX(c2CX - vCircle2.getWidth() / 2f);
            vCircle2.setTranslationY(c2CY - vCircle2.getHeight() / 2f);
            isFirstLoad = false;
        }
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        linePath.reset();
        linePath.moveTo(centerX, centerY);
        Log.d(TAG, "onDraw: c1cx = " + c1CX);
        linePath.lineTo(c1CX, c1CY);
        linePath.moveTo(centerX, centerY);
        linePath.lineTo(c2CX, c2CY);
        canvas.drawPath(linePath, linePaint);
        if (paintToParent) {
            onDeleteListener.copyPath(linePath);
            paintToParent = false;
        }
    }

    float sx, sy;
    boolean paintToParent = false;
    String text = "";
    double angle;

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

                    vCircle1.setTranslationX(vCircle1.getTranslationX() + dx);
                    c1CX += dx;
                    vCircle1.setTranslationY(vCircle1.getTranslationY() + dy);
                    c1CY += dy;
                    vCircle2.setTranslationX(vCircle2.getTranslationX() + dx);
                    c2CX += dx;
                    vCircle2.setTranslationY(vCircle2.getTranslationY() + dy);
                    c2CY += dy;

                    centerX += dx;
                    centerY += dy;

                    sx = rawX;
                    sy = rawY;
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        });

        //两个点的拖动
        OnTouchListener circleTouchListener = (view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    sx = motionEvent.getRawX();
                    sy = motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float rawX = motionEvent.getRawX();
                    float rawY = motionEvent.getRawY();
                    float dx = rawX - sx;
                    float dy = rawY - sy;
                    if (view.getId() == vCircle1.getId()) {
                        c1CX += dx;
                        c1CY += dy;
                    } else {
                        c2CX += dx;
                        c2CY += dy;
                    }
                    sx = rawX;
                    sy = rawY;
                    invalidate();
                    float tx = view.getTranslationX();
                    float ty = view.getTranslationY();
                    view.setTranslationX(tx + dx);
                    view.setTranslationY(ty + dy);
                    //计算夹角
                    angle = calculateAngle(centerX, centerY, c1CX, c1CY, c2CX, c2CY);
                    text = (int) angle + "°";
                    tvAngle.setText(text);
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        };
        vCircle1.setOnTouchListener(circleTouchListener);
        vCircle2.setOnTouchListener(circleTouchListener);

        //确认按钮
        vConfirm.setOnClickListener(view -> {
            //画线
            paintToParent = true;
            invalidate();
        });
    }

    //计算两条线的夹角
    private double calculateAngle(float centerX, float centerY, float c1CX, float c1CY, float c2CX, float c2CY) {
        // 计算向量 OA 和 OB 的坐标差
        double vector1X = c1CX - centerX;
        double vector1Y = c1CY - centerY;
        double vector2X = c2CX - centerX;
        double vector2Y = c2CY - centerY;
        // 计算向量 OA 和 OB 的模
        double vector1Magnitude = Math.sqrt(vector1X * vector1X + vector1Y * vector1Y);
        double vector2Magnitude = Math.sqrt(vector2X * vector2X + vector2Y * vector2Y);
        // 计算向量 OA 和 OB 的点乘
        double dotProduct = vector1X * vector2X + vector1Y * vector2Y;
        // 计算夹角的余弦值
        double cosAngle = dotProduct / (vector1Magnitude * vector2Magnitude);
        // 使用反余弦函数计算夹角的弧度
        double angleInRadians = Math.acos(cosAngle);
        // 将弧度转换为角度，并确保角度在0到180度之间
        return Math.toDegrees(angleInRadians);
    }


    private TextView tvAngle;
    private View vClose, vConfirm, vCircle1, vCircle2;
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
        vCircle1 = findViewById(R.id.circle1);
        vCircle2 = findViewById(R.id.circle2);
    }

}
