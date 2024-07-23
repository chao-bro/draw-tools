package com.example.tools.protractor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.tools.R;
import com.example.tools.adapters.AbstractStrokeViewGroup;
import java.util.Locale;

public class ProtractorLayout extends AbstractStrokeViewGroup {
    private static final String TAG = "ProtractorLayout";

    private final int LINE_PAINT_COLOR = Color.WHITE;
    private final int LINE_PAINT_STROKE_WIDTH = 2;

    private TextView mAngleTv, mCircleLength1, mCircleLength2;
    private View mCloseView, mCircleView1, mCircleView2, mRotateView;
    private ProtractorTransformer mTransformerView;
    private ProtractorView mProtractorView;
    private ImageView mDrawDegreeIv, mDrawRadiansIv, mDrawTriangleIv,
            mDrawSectorIv, mDrawStrokeCircleIv, mDrawFillCircleIv;
    private LinearLayout mToolsIvContainer;

    private int protractorWidth, protractorHeight;
    private Paint linePaint, toolPaint;
    private Path linePath, toolPath;
    private float centerX, centerY;
    private boolean isFirstLoad;
    private float circleView1CX, circleView2CX, circleView1CY, circleView2CY;

    float startMoveX, startMoveY, c1StartMoveX, c1StartMoveY, c2StartMoveX, c2StartMoveY;
    String text = "";
    double angle;

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
        toolPath = new Path();
        toolPaint = new Paint();
        linePaint = new Paint();
        linePaint.setAntiAlias(PAINT_ANTIALIAS_TRUE);
        linePaint.setColor(LINE_PAINT_COLOR);
        linePaint.setStrokeWidth(LINE_PAINT_STROKE_WIDTH);
        linePaint.setStyle(Paint.Style.STROKE);
        //设置屏幕自适应大小
        setDefaultLocAndSize();
        setListeners();
    }

    private void setDefaultLocAndSize() {
        protractorHeight = screenHeight / 3;
        protractorWidth = screenWidth / 4;
        //容器
        LayoutParams params = (LayoutParams) mTransformerView.getLayoutParams();
        if (protractorHeight > protractorWidth) {
            protractorHeight = protractorWidth / 2;
        } else {
            protractorWidth = protractorHeight * 2;
        }
        params.height = protractorHeight;
        params.width = protractorWidth;
        mTransformerView.setLayoutParams(params);

        int iconSize = protractorHeight / 12;
        //删除按钮
        LayoutParams paramClose = (LayoutParams) mCloseView.getLayoutParams();
        paramClose.height = iconSize;
        paramClose.width = iconSize;
        paramClose.setMargins(protractorHeight / 10, 0, 0, 0);
        mCloseView.setLayoutParams(paramClose);

        //旋转按钮
        LayoutParams paramRotate = (LayoutParams) mRotateView.getLayoutParams();
        paramRotate.height = iconSize;
        paramRotate.width = iconSize;
        paramRotate.setMargins(0, 0, protractorHeight / 10, 0);
        mRotateView.setLayoutParams(paramRotate);

        //角度文本
        LayoutParams paramTvAngle = (LayoutParams) mAngleTv.getLayoutParams();
        paramTvAngle.height = protractorHeight / 8;
        paramTvAngle.width = protractorHeight / 5;
        paramTvAngle.setMargins(protractorWidth / 2 - protractorHeight / 10, protractorHeight / 3, 0, 0);
        mAngleTv.setLayoutParams(paramTvAngle);

        //长度1
        LayoutParams paramLen1 = (LayoutParams) mCircleLength1.getLayoutParams();
        paramLen1.width = iconSize * 2;
        paramLen1.height = iconSize;
        paramLen1.setMargins(protractorHeight / 4, 0, 0, 0);
        mCircleLength1.setLayoutParams(paramLen1);

        //长度2
        LayoutParams paramLen2 = (LayoutParams) mCircleLength2.getLayoutParams();
        paramLen2.width = iconSize * 2;
        paramLen2.height = iconSize;
        paramLen2.setMargins(protractorHeight * 3 / 5, 0, 0, 0);
        mCircleLength2.setLayoutParams(paramLen2);

        //画角，画弧度，画扇形。画三角，画圆，画填充圆
        int size = protractorWidth * 2 / 3;
        LayoutParams paramLl = (LayoutParams) mToolsIvContainer.getLayoutParams();
        paramLl.width = size;
        paramLl.height = Math.max(size / 10, 25);
        paramLl.setMargins(protractorWidth / 6, protractorHeight * 3 / 4, 0, 20);
        mToolsIvContainer.setLayoutParams(paramLl);

        //设置偏移
        float transX = (screenWidth - protractorWidth) / 2f;
        float transY = (screenHeight - protractorHeight) / 2f;
        mTransformerView.setTranslationX(transX);
        mTransformerView.setTranslationY(transY);
        centerX = protractorWidth / 2f + mTransformerView.getTranslationX();
        centerY = 0.9f * protractorHeight + mTransformerView.getTranslationY();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //根据height和width求得两个圆的中心
        if (isFirstLoad) {
            float cx = mTransformerView.getTranslationX() + protractorWidth + mCircleView2.getWidth() / 2f;
            mCircleLength1.setText(String.format(Locale.getDefault(), "len1: %.1f",
                    (protractorWidth / 2f + mCircleView2.getWidth() / 2f) / 10 / interval));
            mCircleLength2.setText(String.format(Locale.getDefault(), "len2: %.1f",
                    (protractorWidth / 2f + mCircleView2.getWidth() / 2f) / 10 / interval));
            float cy = 0.9f * protractorHeight + mTransformerView.getTranslationY();
            circleView1CX = cx;
            circleView1CY = cy;
            circleView2CX = cx;
            circleView2CY = cy;
            mCircleView1.setTranslationX(circleView1CX - mCircleView1.getWidth() / 2f);
            mCircleView1.setTranslationY(circleView1CY - mCircleView1.getHeight() / 2f);
            mCircleView2.setTranslationX(circleView2CX - mCircleView2.getWidth() / 2f);
            mCircleView2.setTranslationY(circleView2CY - mCircleView2.getHeight() / 2f);
            isFirstLoad = false;
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        linePath.reset();
        linePath.moveTo(centerX, centerY);
        linePath.lineTo(circleView1CX, circleView1CY);
        linePath.moveTo(centerX, centerY);
        linePath.lineTo(circleView2CX, circleView2CY);
        canvas.drawPath(linePath, linePaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListeners() {
        //关闭
        mCloseView.setOnClickListener(view -> {
            ((FrameLayout) ProtractorLayout.this.getParent()).removeView(ProtractorLayout.this);
            Log.d(TAG, "click to close protractor");
        });
        //移动
        mProtractorView.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startMoveX = motionEvent.getRawX();
                    startMoveY = motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float rawX = motionEvent.getRawX();
                    float rawY = motionEvent.getRawY();
                    float dx = rawX - startMoveX;
                    float dy = rawY - startMoveY;
                    mTransformerView.setTranslationX(mTransformerView.getTranslationX() + dx);
                    mTransformerView.setTranslationY(mTransformerView.getTranslationY() + dy);
                    mCircleView1.setTranslationX(mCircleView1.getTranslationX() + dx);
                    circleView1CX += dx;
                    mCircleView1.setTranslationY(mCircleView1.getTranslationY() + dy);
                    circleView1CY += dy;
                    mCircleView2.setTranslationX(mCircleView2.getTranslationX() + dx);
                    circleView2CX += dx;
                    mCircleView2.setTranslationY(mCircleView2.getTranslationY() + dy);
                    circleView2CY += dy;
                    centerX += dx;
                    centerY += dy;
                    startMoveX = rawX;
                    startMoveY = rawY;
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        });
        //两个点的拖动
        mCircleView1.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    c1StartMoveX = motionEvent.getRawX();
                    c1StartMoveY = motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float rawX = motionEvent.getRawX();
                    float rawY = motionEvent.getRawY();
                    float dx = rawX - c1StartMoveX;
                    float dy = rawY - c1StartMoveY;
                    circleView1CX += dx;
                    circleView1CY += dy;
                    invalidate();
                    float tx = view.getTranslationX();
                    float ty = view.getTranslationY();
                    view.setTranslationX(tx + dx);
                    view.setTranslationY(ty + dy);
                    //计算夹角
                    angle = calculateAngle(centerX, centerY,
                            circleView1CX, circleView1CY,
                            circleView2CX, circleView2CY);
                    text = (int) (angle + 0.5) + "°";
                    mAngleTv.setText(text);
                    c1StartMoveX = rawX;
                    c1StartMoveY = rawY;
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        });
        mCircleView2.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    c2StartMoveX = motionEvent.getRawX();
                    c2StartMoveY = motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float rawX = motionEvent.getRawX();
                    float rawY = motionEvent.getRawY();
                    float dx = rawX - c2StartMoveX;
                    float dy = rawY - c2StartMoveY;
                    circleView2CX += dx;
                    circleView2CY += dy;
                    invalidate();
                    float tx = view.getTranslationX();
                    float ty = view.getTranslationY();
                    view.setTranslationX(tx + dx);
                    view.setTranslationY(ty + dy);
                    //计算夹角
                    angle = calculateAngle(centerX, centerY,
                            circleView1CX, circleView1CY,
                            circleView2CX, circleView2CY);
                    text = (int) (angle + 0.5) + "°";
                    mAngleTv.setText(text);

                    c2StartMoveX = rawX;
                    c2StartMoveY = rawY;
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        });
        //绘制图标
        OnClickListener toolOnClickListener = view -> {
            int id = view.getId();
            toolPath.reset();
            double r1 = Math.sqrt(Math.pow(circleView1CX - centerX, 2) + Math.pow(circleView1CY - centerY, 2));
            double r2 = Math.sqrt(Math.pow(circleView2CX - centerX, 2) + Math.pow(circleView2CY - centerY, 2));
            float radius = (float) Math.min(r1, r2);
            double deg1 = Math.toDegrees(Math.atan2(circleView1CY - centerY, circleView1CX - centerX));
            RectF rectF = new RectF(
                    centerX - radius, centerY - radius,
                    centerX + radius, centerY + radius);
            // 计算圆弧的起始角度和扫过的角度
            float startAngle = (float) deg1;
            float sweepAngle = (float) -angle;
            if (id == mDrawDegreeIv.getId()) {
                toolPaint.setStyle(Paint.Style.STROKE);
                toolPath.moveTo(centerX, centerY);
                toolPath.lineTo(circleView1CX, circleView1CY);
                toolPath.moveTo(centerX, centerY);
                toolPath.lineTo(circleView2CX, circleView2CY);
            } else if (id == mDrawRadiansIv.getId()) {
                toolPaint.setStyle(Paint.Style.STROKE);
                // 添加圆弧到路径中
                toolPath.arcTo(rectF, startAngle, sweepAngle);
            } else if (id == mDrawTriangleIv.getId()) {
                toolPaint.setStyle(Paint.Style.STROKE);
                toolPath.moveTo(centerX, centerY);
                toolPath.lineTo(circleView1CX, circleView1CY);
                toolPath.lineTo(circleView2CX, circleView2CY);
                toolPath.close();
            } else if (id == mDrawSectorIv.getId()) {
                toolPaint.setStyle(Paint.Style.FILL);
                toolPath.moveTo(centerX, centerY);
                toolPath.arcTo(rectF, startAngle, sweepAngle);
                toolPath.close();
            } else if (id == mDrawStrokeCircleIv.getId()) {
                toolPaint.setStyle(Paint.Style.STROKE);
                toolPath.addCircle(centerX, centerY, radius, Path.Direction.CW);
                toolPath.addCircle(centerX, centerY, 1, Path.Direction.CW);
            } else if (id == mDrawFillCircleIv.getId()) {
                toolPaint.setStyle(Paint.Style.FILL);
                toolPath.addCircle(centerX, centerY, radius, Path.Direction.CW);
            }
            onDeleteListener.copyPath(toolPath, toolPaint);
        };
        mDrawDegreeIv.setOnClickListener(toolOnClickListener);
        mDrawRadiansIv.setOnClickListener(toolOnClickListener);
        mDrawTriangleIv.setOnClickListener(toolOnClickListener);
        mDrawSectorIv.setOnClickListener(toolOnClickListener);
        mDrawStrokeCircleIv.setOnClickListener(toolOnClickListener);
        mDrawFillCircleIv.setOnClickListener(toolOnClickListener);
        //旋转
        mRotateView.setOnTouchListener((view, motionEvent) -> {
            int left = mRotateView.getLeft();
            int top = mRotateView.getTop();
            motionEvent.offsetLocation(left, top);
            mTransformerView.setPivotX(protractorWidth / 2f);
            mTransformerView.setPivotY(0.9f * protractorHeight);
            mTransformerView.rotateLayout(motionEvent);
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
        mCircleLength1.setText(String.format(Locale.getDefault(), "len1: %.1f", vector1Magnitude / 10 / interval));
        mCircleLength2.setText(String.format(Locale.getDefault(), "len2: %.1f", vector2Magnitude / 10 / interval));
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

    private void inflateAndInitViews() {
        LayoutInflater.from(context).inflate(R.layout.rulerset_protractor_view, this);
        setBackgroundColor(Color.TRANSPARENT);
        mAngleTv = findViewById(R.id.rulerset_protractor_degree);
        mCircleLength1 = findViewById(R.id.rulerset_protractor_len1);
        mCircleLength2 = findViewById(R.id.rulerset_protractor_len2);
        mCloseView = findViewById(R.id.rulerset_protractor_close);
        mTransformerView = findViewById(R.id.rulerset_protractor_transformer);
        mProtractorView = findViewById(R.id.rulerset_protractor);
        mCircleView1 = findViewById(R.id.rulerset_protractor_circle1);
        mCircleView2 = findViewById(R.id.rulerset_protractor_circle2);
        mRotateView = findViewById(R.id.rulerset_protractor_v_rotate);
        mToolsIvContainer = findViewById(R.id.rulerset_protractor_ll_options);
        mDrawDegreeIv = findViewById(R.id.rulerset_protractor_iv_degree);
        mDrawRadiansIv = findViewById(R.id.rulerset_protractor_iv_radians);
        mDrawTriangleIv = findViewById(R.id.rulerset_protractor_iv_triangle);
        mDrawSectorIv = findViewById(R.id.rulerset_protractor_iv_sector);
        mDrawStrokeCircleIv = findViewById(R.id.rulerset_protractor_iv_circle);
        mDrawFillCircleIv = findViewById(R.id.rulerset_protractor_iv_circle_filled);
    }
}
