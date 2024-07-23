package com.example.tools.protractor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private Paint linePaint, toolPaint;
    private Path linePath, toolPath;
    private float centerX, centerY;
    private boolean isFirstLoad;
    private float c1CX, c2CX, c1CY, c2CY;

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
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(2);
        linePaint.setStyle(Paint.Style.STROKE);
        toolPath = new Path();
        toolPaint = new Paint();

        //设置屏幕自适应大小
        setDefaultLocAndSize();
        setListeners();
    }

    private void setDefaultLocAndSize() {
        height = screenHeight / 3;
        width = screenWidth / 4;

        //容器
        LayoutParams params = (LayoutParams) transformer.getLayoutParams();
        if (height > width) {
            height = width / 2;
        } else {
            width = height * 2;
        }
        params.height = height;
        params.width = width;
        transformer.setLayoutParams(params);

        //删除按钮
        LayoutParams paramClose = (LayoutParams) vClose.getLayoutParams();
        paramClose.height = height / 12;
        paramClose.width = height / 12;
        paramClose.setMargins(height / 10, 0, 0, 0);
        vClose.setLayoutParams(paramClose);

        //旋转按钮
        LayoutParams paramRotate = (LayoutParams) vRotate.getLayoutParams();
        paramRotate.height = height / 12;
        paramRotate.width = height / 12;
        paramRotate.setMargins(0, 0, height / 10, 0);
        vRotate.setLayoutParams(paramRotate);

        //角度文本
        LayoutParams paramTvAngle = (LayoutParams) tvAngle.getLayoutParams();
        paramTvAngle.height = height / 10;
        paramTvAngle.width = height / 5;
        paramTvAngle.setMargins(width / 2 - height / 10, height / 3, 0, 0);
        tvAngle.setLayoutParams(paramTvAngle);

        //长度1
        LayoutParams paramLen1 = (LayoutParams) tvLen1.getLayoutParams();
        paramLen1.width = width / 6;
        paramLen1.height = height / 12;
        paramLen1.setMargins(height / 4, 0, 0, 0);
        tvLen1.setLayoutParams(paramLen1);

        //长度2
        LayoutParams paramLen2 = (LayoutParams) tvLen2.getLayoutParams();
        paramLen2.width = width / 6;
        paramLen2.height = height / 12;
        paramLen2.setMargins(height * 3 / 5, 0, 0, 0);
        tvLen2.setLayoutParams(paramLen2);

        //画角，画弧度，画扇形。画三角，画圆，画填充圆
        int size = width * 2 / 3;
        LayoutParams paramLl = (LayoutParams) options.getLayoutParams();
        paramLl.width = size;
        paramLl.height = Math.max(size / 10, 25);
        paramLl.setMargins(width / 6, height * 3 / 4, 0, 20);
        options.setLayoutParams(paramLl);

        //设置偏移
        float transX = (screenWidth - width) / 2f;
        float transY = (screenHeight - height) / 2f;
        transformer.setTranslationX(transX);
        transformer.setTranslationY(transY);
        centerX = width / 2f + transformer.getTranslationX();
        centerY = 0.9f * height + transformer.getTranslationY();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //根据height和width求得两个圆的中心
        if (isFirstLoad) {
            float cx = transformer.getTranslationX() + width + vCircle2.getWidth() / 2f;
            tvLen1.setText(String.format(Locale.getDefault(), "len1: %.1f",
                    (width / 2f + vCircle2.getWidth() / 2f) / 10 / interval));
            tvLen2.setText(String.format(Locale.getDefault(), "len2: %.1f",
                    (width / 2f + vCircle2.getWidth() / 2f) / 10 / interval));
            float cy = 0.9f * height + transformer.getTranslationY();
            c1CX = cx;
            c1CY = cy;
            c2CX = cx;
            c2CY = cy;
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
        linePath.lineTo(c1CX, c1CY);
        linePath.moveTo(centerX, centerY);
        linePath.lineTo(c2CX, c2CY);
        canvas.drawPath(linePath, linePaint);
    }

    float sx, sy, sx1, sy1, sx2, sy2;
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
        vCircle1.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    sx1 = motionEvent.getRawX();
                    sy1 = motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float rawX = motionEvent.getRawX();
                    float rawY = motionEvent.getRawY();
                    float dx = rawX - sx1;
                    float dy = rawY - sy1;
                    c1CX += dx;
                    c1CY += dy;
                    invalidate();
                    float tx = view.getTranslationX();
                    float ty = view.getTranslationY();
                    view.setTranslationX(tx + dx);
                    view.setTranslationY(ty + dy);
                    //计算夹角
                    angle = calculateAngle(centerX, centerY, c1CX, c1CY, c2CX, c2CY);
                    text = (int) (angle + 0.5) + "°";
                    tvAngle.setText(text);

                    sx1 = rawX;
                    sy1 = rawY;
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        });
        vCircle2.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    sx2 = motionEvent.getRawX();
                    sy2 = motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float rawX = motionEvent.getRawX();
                    float rawY = motionEvent.getRawY();
                    float dx = rawX - sx2;
                    float dy = rawY - sy2;
                    c2CX += dx;
                    c2CY += dy;
                    invalidate();
                    float tx = view.getTranslationX();
                    float ty = view.getTranslationY();
                    view.setTranslationX(tx + dx);
                    view.setTranslationY(ty + dy);
                    //计算夹角
                    angle = calculateAngle(centerX, centerY, c1CX, c1CY, c2CX, c2CY);
                    text = (int) (angle + 0.5) + "°";
                    tvAngle.setText(text);

                    sx2 = rawX;
                    sy2 = rawY;
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        });

        OnClickListener toolOnClickListener = view -> {
            int id = view.getId();
            toolPath.reset();
            double r1 = Math.sqrt(Math.pow(c1CX - centerX, 2) + Math.pow(c1CY - centerY, 2));
            double r2 = Math.sqrt(Math.pow(c2CX - centerX, 2) + Math.pow(c2CY - centerY, 2));
            float radius = (float) Math.min(r1, r2);
            double deg1 = Math.toDegrees(Math.atan2(c1CY - centerY, c1CX - centerX));
            RectF rectF = new RectF(
                    centerX - radius, centerY - radius,
                    centerX + radius, centerY + radius);
            // 计算圆弧的起始角度和扫过的角度
            float startAngle = (float) deg1;
            float sweepAngle = (float) -angle;
            if (id == ivDegree.getId()) {
                toolPaint.setStyle(Paint.Style.STROKE);
                toolPath.moveTo(centerX, centerY);
                toolPath.lineTo(c1CX, c1CY);
                toolPath.moveTo(centerX, centerY);
                toolPath.lineTo(c2CX, c2CY);
            } else if (id == ivRadians.getId()) {
                toolPaint.setStyle(Paint.Style.STROKE);
                // 添加圆弧到路径中
                toolPath.arcTo(rectF, startAngle, sweepAngle);
            } else if (id == ivTriangle.getId()) {
                toolPaint.setStyle(Paint.Style.STROKE);
                toolPath.moveTo(centerX, centerY);
                toolPath.lineTo(c1CX, c1CY);
                toolPath.lineTo(c2CX, c2CY);
                toolPath.close();
            } else if (id == ivSector.getId()) {
                toolPaint.setStyle(Paint.Style.FILL);
                toolPath.moveTo(centerX, centerY);
                toolPath.arcTo(rectF, startAngle, sweepAngle);
                toolPath.close();
            } else if (id == ivCircle.getId()) {
                toolPaint.setStyle(Paint.Style.STROKE);
                toolPath.addCircle(centerX, centerY, radius, Path.Direction.CW);
                toolPath.addCircle(centerX, centerY, 1, Path.Direction.CW);
            } else if (id == ivCircleFilled.getId()) {
                toolPaint.setStyle(Paint.Style.FILL);
                toolPath.addCircle(centerX, centerY, radius, Path.Direction.CW);
            }
            onDeleteListener.copyPath(toolPath, toolPaint);
        };
        ivDegree.setOnClickListener(toolOnClickListener);
        ivRadians.setOnClickListener(toolOnClickListener);
        ivTriangle.setOnClickListener(toolOnClickListener);
        ivSector.setOnClickListener(toolOnClickListener);
        ivCircle.setOnClickListener(toolOnClickListener);
        ivCircleFilled.setOnClickListener(toolOnClickListener);

        //旋转
        vRotate.setOnTouchListener((view, motionEvent) -> {
            int left = vRotate.getLeft();
            int top = vRotate.getTop();
            motionEvent.offsetLocation(left, top);
            transformer.setPivotX(width / 2f);
            transformer.setPivotY(0.9f * height);
            transformer.rotateLayout(motionEvent);
            return true;
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
        tvLen1.setText(String.format(Locale.getDefault(), "len1: %.1f", vector1Magnitude / 10 / interval));
        tvLen2.setText(String.format(Locale.getDefault(), "len2: %.1f", vector2Magnitude / 10 / interval));
        // 计算向量 OA 和 OB 的点乘
        double dotProduct = vector1X * vector2X + vector1Y * vector2Y;
        // 计算夹角的余弦值
        double cosAngle = Math.max(-1.0, Math.min(1.0, dotProduct / (vector1Magnitude * vector2Magnitude)));
        // 使用反余弦函数计算夹角的弧度
        double angleInRadians = Math.acos(cosAngle);
        // 将弧度转换为角度，并确保角度在0到180度之间
        double angleInDegree = Math.toDegrees(angleInRadians);

        double crossProduct = vector1X * vector2Y - vector1Y * vector2X;
        if (crossProduct > 0) {
            angleInDegree = 360 - angleInDegree;
        }

        return angleInDegree;
    }

    private TextView tvAngle, tvLen1, tvLen2;
    private View vClose, vCircle1, vCircle2, vRotate;
    private ProtractorTransformer transformer;
    private ProtractorView protractor;
    private ImageView ivDegree, ivRadians, ivTriangle, ivSector, ivCircle, ivCircleFilled;
    private LinearLayout options;

    private void inflateAndInitViews() {
        LayoutInflater.from(context).inflate(R.layout.protractor_view, this);
        setBackgroundColor(Color.TRANSPARENT);
        tvAngle = findViewById(R.id.degree);
        tvLen1 = findViewById(R.id.len1);
        tvLen2 = findViewById(R.id.len2);
        vClose = findViewById(R.id.close);
        transformer = findViewById(R.id.transformer);
        protractor = findViewById(R.id.protractor);
        vCircle1 = findViewById(R.id.circle1);
        vCircle2 = findViewById(R.id.circle2);
        vRotate = findViewById(R.id.v_rotate);
        options = findViewById(R.id.ll_options);
        ivDegree = findViewById(R.id.iv_degree);
        ivRadians = findViewById(R.id.iv_radians);
        ivTriangle = findViewById(R.id.iv_triangle);
        ivSector = findViewById(R.id.iv_sector);
        ivCircle = findViewById(R.id.iv_circle);
        ivCircleFilled = findViewById(R.id.iv_circle_filled);
    }
}
