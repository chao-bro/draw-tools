package com.example.tools.ruler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.example.tools.adapters.AbstractStrokeViewGroup;
import com.example.tools.R;
import java.util.Locale;

public class RulerLayout extends AbstractStrokeViewGroup {

    private final String TAG = "RULER_LAYOUT";
    //直尺的宽度
    private final float RULER_HEIGHT_DP = 50f;
    //用于识别画线区域的高度
    private final int DRAW_AREA_WIDTH = 20;
    //最长长度
    private final int MAX_LEN = screenWidth * 3 / 4;
    //最短长度
    private final int MIN_LEN = screenWidth / 5;

    //布局组件
    private RulerView mCustomRulerView;
    private View mCloseView, mProlongView, mRotateView, mDrawAreaView;
    private RulerViewTransformer mTransformerView;
    private TextView resultTv;

    //移动直尺时的前一个坐标
    private int lastMoveX = 0, lastMoveY = 0;
    //拉长直尺时的前一个坐标
    private int lastProlongX, lastProlongY;
    //开始画线的坐标
    float sx, sy;
    //结束画线的坐标
    float ex, ey;
    //画线的长度
    double drawLen = 0;
    //填充文本字符串
    String text = "";

    public RulerLayout(Context context) {
        super(context);
    }

    public RulerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    protected void init() {
        //设置背景色，防止上边距被裁掉
        setBackgroundColor(Color.TRANSPARENT);
        inflateAndFindViews();
        LayoutParams layoutParams = (LayoutParams) mTransformerView.getLayoutParams();
        layoutParams.width = screenWidth / 3;
        mTransformerView.setLayoutParams(layoutParams);
        float transX = screenWidth / 3f;
        float transY = screenHeight / 2f - dp2px(RULER_HEIGHT_DP) / 2;
        mTransformerView.setTranslationX(transX);
        mTransformerView.setTranslationY(transY);
        setViewTouchListeners();
    }

    private void setViewTouchListeners() {
        //直尺移动
        moveRulerTouchListener();
        //直尺顶部画线
        drawAboveRulerTouchListener();
        //删除
        deleteRulerTouchListener();
        //旋转
        rotateRulerTouchListener();
        //拖动拉长直尺
        addRulerLenTouchListener();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void moveRulerTouchListener() {
        mCustomRulerView.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastMoveX = (int) event.getRawX();
                    lastMoveY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int rawX = (int) event.getRawX();
                    int rawY = (int) event.getRawY();
                    float transX = mTransformerView.getTranslationX() + (rawX - lastMoveX);
                    float transY = mTransformerView.getTranslationY() + (rawY - lastMoveY);
                    lastMoveX = rawX;
                    lastMoveY = rawY;
                    mTransformerView.setTranslationX(transX);
                    mTransformerView.setTranslationY(transY);
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "touch to move ruler to (" +
                            mTransformerView.getTranslationX() +
                            "," +
                            mTransformerView.getTranslationY() +
                            ")");
                    break;
            }
            return true;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void drawAboveRulerTouchListener() {
        mDrawAreaView.setOnTouchListener((view, motionEvent) -> {
            float cenX = mTransformerView.getTranslationX();
            float cenY = mTransformerView.getTranslationY() + dp2px(DRAW_AREA_WIDTH);
            float x = motionEvent.getRawX();
            float y = motionEvent.getRawY();
            double k1 = Math.tan(Math.toRadians(mTransformerView.getRotation()));
            double k2 = -1 / k1;
            double b1 = cenY - k1 * cenX;
            double b2, xj;
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (Double.isInfinite(k2) || Double.isInfinite(k1)) {
                        if (Double.isInfinite(k1)) {
                            //直尺垂直
                            sx = cenX;
                            sy = motionEvent.getRawY();
                        } else if (Double.isInfinite(k2)) {
                            sx = motionEvent.getRawX();
                            sy = cenY;
                        }
                        path.moveTo(sx, sy);
                        break;
                    }
                    //计算按下点所在的垂线的偏移 b
                    b2 = -k2 * x + y;
                    //计算垂线与直尺的焦点的 x 坐标，即垂直映射点的 x 值
                    xj = (b1 - b2) / (k2 - k1);
                    sx = (float) xj;
                    sy = (float) (k1 * xj + b1);
                    path.moveTo(sx, sy);
                    break;
                case MotionEvent.ACTION_MOVE:
                    path.reset();
                    path.moveTo(sx, sy);
                    if (Double.isInfinite(k1) || Double.isInfinite(k2)) {
                        if (Double.isInfinite(k1)) {
                            path.lineTo(cenX, motionEvent.getRawY());
                            drawLen = (motionEvent.getRawY() - sy) / interval / 10;
                        } else if (Double.isInfinite(k2)) {
                            path.lineTo(motionEvent.getRawX(), cenY);
                            drawLen = (motionEvent.getRawX() - sx) / interval / 10;
                        }
                        text = String.format(Locale.getDefault(), "%.2f", Math.abs(drawLen));
                        resultTv.setText(text);
                        break;
                    }
                    x = motionEvent.getRawX();
                    y = motionEvent.getRawY();
                    b2 = -k2 * x + y;
                    //计算交点的x值
                    xj = (b1 - b2) / (k2 - k1);
                    ex = (float) xj;
                    ey = (float) (k1 * xj + b1);
                    path.lineTo(ex, ey);
                    invalidate();
                    drawLen = (Math.sqrt(Math.pow(ex - sx, 2) + Math.pow(ey - sy, 2))) / interval / 10;
                    text = String.format(Locale.getDefault(), "%.2f", Math.abs(drawLen));
                    resultTv.setText(text);
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "touch to draw from (" + sx + "," + sy + ") to (" + ex + "," + ey + ")");
                    onDeleteListener.copyPath(path);
                    path.reset();
                    break;
            }
            return true;
        });
    }

    private void deleteRulerTouchListener() {
        mCloseView.setOnClickListener(view -> {
            onDeleteListener.copyPath(path);
            ((FrameLayout) RulerLayout.this.getParent()).removeView(RulerLayout.this);
            Log.d(TAG, "click to close ruler");
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void rotateRulerTouchListener() {
        mRotateView.setOnTouchListener((view, motionEvent) -> {
            float offsetX = mRotateView.getLeft();
            float offsetY = mRotateView.getTop();
            motionEvent.offsetLocation(offsetX, offsetY);
            mTransformerView.setPivotX(getLeft());
            mTransformerView.setPivotY(getTop() + dp2px(DRAW_AREA_WIDTH));
            mTransformerView.rotateLayout(motionEvent);
            return true;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addRulerLenTouchListener() {
        mProlongView.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastProlongX = (int) motionEvent.getRawX();
                    lastProlongY = (int) motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int rawX = (int) motionEvent.getRawX();
                    int rawY = (int) motionEvent.getRawY();
                    int dx = rawX - lastProlongX;
                    int dy = rawY - lastProlongY;
                    double radians = Math.toRadians(mTransformerView.getRotation());
                    int wid = (int) (dx * Math.cos(radians) + dy * Math.sin(radians));
                    //直接修改直尺容器的宽度
                    LayoutParams layoutParams = (LayoutParams) mTransformerView.getLayoutParams();
                    layoutParams.width = mTransformerView.getMeasuredWidth() + wid;
                    if (layoutParams.width >= MAX_LEN) {
                        layoutParams.width = MAX_LEN;
                    } else if (layoutParams.width <= MIN_LEN) {
                        layoutParams.width = MIN_LEN;
                    }
                    mTransformerView.setLayoutParams(layoutParams);
                    //每次触发都更新至上一次触发点
                    lastProlongX = rawX;
                    lastProlongY = rawY;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "touch to enlarge ruler, current length is " + mTransformerView.getWidth());
                    break;
            }
            return true;
        });
    }

    private void inflateAndFindViews() {
        LayoutInflater.from(context).inflate(R.layout.rulerset_ruler_view, this);
        Log.d(TAG, "add ruler as draw tool");
        mCustomRulerView = findViewById(R.id.rulerset_ruler_view);
        mProlongView = findViewById(R.id.rulerset_ruler_add_length);
        mCloseView = findViewById(R.id.rulerset_ruler_close_view);
        mRotateView = findViewById(R.id.rulerset_ruler_rotate_view);
        mTransformerView = findViewById(R.id.rulerset_ruler_transfer);
        resultTv = findViewById(R.id.rulerset_ruler_result);
        mDrawAreaView = findViewById(R.id.rulerset_ruler_draw_area);
    }
}
