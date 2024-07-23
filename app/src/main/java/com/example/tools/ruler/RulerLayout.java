package com.example.tools.ruler;

import android.annotation.SuppressLint;
import android.content.Context;
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
    //直尺的高度
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
    private TextView mResultTextView;

    //移动直尺时的前一个坐标
    private int lastMoveX = 0, lastMoveY = 0;
    //拉长直尺时的前一个坐标
    private int lastProlongX, lastProlongY;
    //开始画线的坐标
    float startDrawLineX, startDrawLineY;
    //结束画线的坐标
    float endDrawLineX, endDrawLineY;


    public RulerLayout(Context context) {
        super(context);
    }

    public RulerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    protected void init() {
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
        setRulerViewTouchListener();
        //直尺顶部画线
        setDrawAreaViewTouchListener();
        //删除
        setCloseViewTouchListener();
        //旋转
        setRotateViewTouchListener();
        //拖动拉长直尺
        setProlongViewTouchListener();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setRulerViewTouchListener() {
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
    private void setDrawAreaViewTouchListener() {
        mDrawAreaView.setOnTouchListener((view, motionEvent) -> {
            float cenX = mTransformerView.getTranslationX();
            float cenY = mTransformerView.getTranslationY() + dp2px(DRAW_AREA_WIDTH);
            float x = motionEvent.getRawX();
            float y = motionEvent.getRawY();
            double k1 = Math.tan(Math.toRadians(mTransformerView.getRotation()));
            double k2 = -1 / k1;
            double b1 = cenY - k1 * cenX;
            double b2, xj;
            double drawLen = 0;
            String text;
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (Double.isInfinite(k2) || Double.isInfinite(k1)) {
                        if (Double.isInfinite(k1)) {
                            //直尺垂直
                            startDrawLineX = cenX;
                            startDrawLineY = motionEvent.getRawY();
                        } else if (Double.isInfinite(k2)) {
                            startDrawLineX = motionEvent.getRawX();
                            startDrawLineY = cenY;
                        }
                        path.moveTo(startDrawLineX, startDrawLineY);
                        break;
                    }
                    //计算按下点所在的垂线的偏移 b
                    b2 = -k2 * x + y;
                    //计算垂线与直尺的焦点的 x 坐标，即垂直映射点的 x 值
                    xj = (b1 - b2) / (k2 - k1);
                    startDrawLineX = (float) xj;
                    startDrawLineY = (float) (k1 * xj + b1);
                    path.moveTo(startDrawLineX, startDrawLineY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    path.reset();
                    path.moveTo(startDrawLineX, startDrawLineY);
                    if (Double.isInfinite(k1) || Double.isInfinite(k2)) {
                        if (Double.isInfinite(k1)) {
                            path.lineTo(cenX, motionEvent.getRawY());
                            drawLen = (motionEvent.getRawY() - startDrawLineY) / interval / 10;
                        } else if (Double.isInfinite(k2)) {
                            path.lineTo(motionEvent.getRawX(), cenY);
                            drawLen = (motionEvent.getRawX() - startDrawLineX) / interval / 10;
                        }
                        text = String.format(Locale.getDefault(), "%.2f", Math.abs(drawLen));
                        mResultTextView.setText(text);
                        break;
                    }
                    x = motionEvent.getRawX();
                    y = motionEvent.getRawY();
                    b2 = -k2 * x + y;
                    //计算交点的x值
                    xj = (b1 - b2) / (k2 - k1);
                    endDrawLineX = (float) xj;
                    endDrawLineY = (float) (k1 * xj + b1);
                    path.lineTo(endDrawLineX, endDrawLineY);
                    invalidate();
                    drawLen = (Math.sqrt(Math.pow(endDrawLineX - startDrawLineX, 2) + Math.pow(endDrawLineY - startDrawLineY, 2))) / interval / 10;
                    text = String.format(Locale.getDefault(), "%.2f", Math.abs(drawLen));
                    mResultTextView.setText(text);
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "touch to draw from (" + startDrawLineX + "," + startDrawLineY + ") to (" + endDrawLineX + "," + endDrawLineY + ")");
                    onDeleteListener.copyPath(path);
                    path.reset();
                    break;
            }
            return true;
        });
    }

    private void setCloseViewTouchListener() {
        mCloseView.setOnClickListener(view -> {
            onDeleteListener.copyPath(path);
            ((FrameLayout) RulerLayout.this.getParent()).removeView(RulerLayout.this);
            Log.d(TAG, "click to close ruler");
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setRotateViewTouchListener() {
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
    private void setProlongViewTouchListener() {
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
        mResultTextView = findViewById(R.id.rulerset_ruler_result);
        mDrawAreaView = findViewById(R.id.rulerset_ruler_draw_area);
    }
}
