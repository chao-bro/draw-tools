package com.example.tools.ruler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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

    private static final String TAG = "RULER_LAYOUT";

    public RulerLayout(Context context) {
        super(context);
    }

    public RulerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private float defaultX, defaultY;

    protected void init() {
        paint.setAlpha(127);
        //设置背景色，防止上边距被裁掉
        setBackgroundColor(Color.TRANSPARENT);
        inflateAndFindViews();
        //设置直尺容器在屏幕中的位置
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        //获取屏幕的像素高度和宽度
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        LayoutParams layoutParams = (LayoutParams) transfer.getLayoutParams();
        layoutParams.width = widthPixels / 2;
        transfer.setLayoutParams(layoutParams);
        defaultX = (float) (widthPixels - transfer.getWidth()) / 2;
        defaultY = heightPixels / 2f - dp2px(50f) / 2;
        transfer.setTranslationX(defaultX);
        transfer.setTranslationY(defaultY);
        setViewTouchListeners();
    }

    //旋转中心点
    float cenX = 0.0f, cenY = 0.0f;
    //直尺斜率k1 垂线斜率k2
    double k1 = 0, k2 = 0;
    //开始画线的坐标
    float sx, sy;
    float ex, ey;
    //画线长度
    double drawLen = 0;
    //填充文本
    String text = "";

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

    private int lastX, lastY;

    @SuppressLint("ClickableViewAccessibility")
    private void moveRulerTouchListener() {
        rulerView.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int rawX = (int) event.getRawX();
                    int rawY = (int) event.getRawY();
                    defaultX += (rawX - lastX);
                    defaultY += (rawY - lastY);
                    lastX = rawX;
                    lastY = rawY;
                    transfer.setTranslationX(defaultX);
                    transfer.setTranslationY(defaultY);
                    break;
                default:
                    Log.d(TAG, "touch to move ruler to (" +
                            transfer.getTranslationX() +
                            "," +
                            transfer.getTranslationY() +
                            ")");
                    break;
            }
            return true;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void drawAboveRulerTouchListener() {
        drawAreaView.setOnTouchListener((view, motionEvent) -> {
            cenX = transfer.getTranslationX();
            cenY = transfer.getTranslationY() + dp2px(20);
            float x = motionEvent.getRawX();
            float y = motionEvent.getRawY();
            k1 = Math.tan(Math.toRadians(transfer.getRotation()));
            k2 = -1 / k1;
            double b1 = cenY - k1 * cenX;
            double b2, xj;
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (Double.isInfinite(k1) || Double.isInfinite(k2)) {
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
                            drawLen = (motionEvent.getRawY() - sy) / interval / 5;
                        } else if (Double.isInfinite(k2)) {
                            path.lineTo(motionEvent.getRawX(), cenY);
                            drawLen = (motionEvent.getRawX() - sx) / interval / 5;
                        }
                        text = String.format(Locale.getDefault(), "%.2f", drawLen);
                        result.setText(text);
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
                    drawLen = (Math.sqrt(Math.pow(ex - sx, 2) + Math.pow(ey - sy, 2))) / interval / 5;
                    text = String.format(Locale.getDefault(), "%.2f", drawLen);
                    result.setText(text);
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "touch to draw from (" + sx +","+ sy +") to ("+ ex +","+ ey +")");
                    onDeleteListener.copyPath(path);
                    path.reset();
                    break;
            }
            return true;
        });
    }

    private void deleteRulerTouchListener() {
        deleteV.setOnClickListener(view -> {
            onDeleteListener.copyPath(path);
            ((FrameLayout) RulerLayout.this.getParent()).removeView(RulerLayout.this);
            Log.d(TAG, "click to close ruler");
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void rotateRulerTouchListener() {
        rotateV.setOnTouchListener((view, motionEvent) -> {
            float offsetX = rotateV.getLeft() - transfer.getScrollX();
            float offsetY = rotateV.getTop() - transfer.getScrollY();
            motionEvent.offsetLocation(offsetX, offsetY);
            transfer.setPivotX(getLeft());
            transfer.setPivotY(getTop() + dp2px(20));
            transfer.rotateLayout(motionEvent);
            return true;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addRulerLenTouchListener() {
        addLenV.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) motionEvent.getRawX();
                    lastY = (int) motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int rawX = (int) motionEvent.getRawX();
                    int rawY = (int) motionEvent.getRawY();
                    int dx = rawX - lastX;
                    int dy = rawY - lastY;
                    double radians = Math.toRadians(transfer.getRotation());
                    int wid = (int) (dx * Math.cos(radians) + dy * Math.sin(radians));
                    //直接修改直尺容器的宽度
                    LayoutParams layoutParams = (LayoutParams) transfer.getLayoutParams();
                    layoutParams.width = transfer.getMeasuredWidth() + wid;
                    layoutParams.height = transfer.getMeasuredHeight();
                    transfer.setLayoutParams(layoutParams);
                    //每次触发都更新至上一次触发点
                    lastX = rawX;
                    lastY = rawY;
                default:
                    Log.d(TAG, "touch to enlarge ruler, current length is "+ transfer.getWidth());
                    break;
            }
            return true;
        });
    }

    private RulerView rulerView;
    private View deleteV, addLenV, rotateV, drawAreaView;
    private TransferLayout transfer;
    private TextView result;

    private void inflateAndFindViews() {
        LayoutInflater.from(context).inflate(R.layout.ruler_view, this);
        Log.d(TAG, "add ruler as draw tool");
        rulerView = findViewById(R.id.ruler_view);
        addLenV = findViewById(R.id.add_length);
        deleteV = findViewById(R.id.close_view);
        rotateV = findViewById(R.id.rotate_view);
        transfer = findViewById(R.id.transfer);
        result = findViewById(R.id.result);
        drawAreaView = findViewById(R.id.draw_area);
    }
}
