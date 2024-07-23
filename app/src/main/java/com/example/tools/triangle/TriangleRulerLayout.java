package com.example.tools.triangle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.example.tools.R;
import com.example.tools.adapters.AbstractStrokeViewGroup;
import java.util.Locale;

public class TriangleRulerLayout extends AbstractStrokeViewGroup {

    private final String TAG = "TriangleRulerLayout";
    //用于识别画线区域的高度
    private final int DRAW_AREA_WIDTH = 20;

    private final int MAX_SIZE = screenHeight;
    private final int MIN_SIZE = screenHeight / 3;

    private View mCloseView, mRotateView, mEnlargeView, mHorDrawArea, mVerDrawArea;
    private TriangleRulerView mTriangleRulerView;
    private TriangleRulerTransformer mTransformerView;
    private TextView mResultTv;

    float startDrawX = 0f, startDrawY = 0f, endDrawX = 0f, endDrawY = 0f;
    double drawLen = 0;

    public TriangleRulerLayout(Context context) {
        super(context);
    }

    public TriangleRulerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        inflateAndFindViews();
        setDefaultViewSizeAndLocation();
        setViewTouchEvents();
    }

    private void setDefaultViewSizeAndLocation() {
        int min = Math.min(screenWidth, screenHeight) / 2;
        LayoutParams layoutParams = (LayoutParams) mTransformerView.getLayoutParams();
        layoutParams.width = min;
        layoutParams.height = min;
        mTransformerView.setLayoutParams(layoutParams);
        LayoutParams paramsTv = (LayoutParams) mResultTv.getLayoutParams();
        paramsTv.setMargins(min / 5, min / 5, 0,0);
        mResultTv.setLayoutParams(paramsTv);
        float transX = (screenWidth - min) / 2f;
        float transY = (screenHeight - min) / 2f;
        mTransformerView.setTranslationX(transX);
        mTransformerView.setTranslationY(transY);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setViewTouchEvents() {
        //关闭
        mCloseView.setOnClickListener(view -> {
            ((FrameLayout) TriangleRulerLayout.this.getParent()).
                    removeView(TriangleRulerLayout.this);
            onDeleteListener.copyPath(path);
            Log.d(TAG, "click to close triangle");
        });
        //移动
        mTriangleRulerView.setOnTouchListener(new OnTouchListener() {
            float lastMoveX, lastMoveY;
            boolean isInsideTriangle = true;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        lastMoveX = motionEvent.getRawX();
                        lastMoveY = motionEvent.getRawY();
                        //判断按下区域是否在三角尺的范围上
                        Point point = new Point((int) motionEvent.getX(), (int) motionEvent.getY());
                        if (!isPointInPath(mTriangleRulerView.outPath, point)) {
                            isInsideTriangle = false;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float rawX = motionEvent.getRawX();
                        float rawY = motionEvent.getRawY();
                        if (!isInsideTriangle) {
                            //画线
                            path.moveTo(lastMoveX, lastMoveY);
                            path.lineTo(rawX, rawY);
                            invalidate();
                        } else {
                            float dx = rawX - lastMoveX;
                            float dy = rawY - lastMoveY;
                            mTransformerView.setTranslationX(mTransformerView.getTranslationX() + dx);
                            mTransformerView.setTranslationY(mTransformerView.getTranslationY() + dy);
                        }
                        lastMoveX = rawX;
                        lastMoveY = rawY;
                        break;
                    case MotionEvent.ACTION_UP:
                        if (isInsideTriangle) {
                            Log.d(TAG, "touch triangle to move triangle , current translation is (" +
                                    mTransformerView.getTranslationX() +
                                    "," +
                                    mTransformerView.getTranslationY() +
                                    ")");
                        } else {
                            isInsideTriangle = true;
                        }
                        break;
                }
                return true;
            }
        });
        //旋转
        mRotateView.setOnTouchListener((view, motionEvent) -> {
            float offsetX = mRotateView.getLeft();
            float offsetY = mRotateView.getTop();
            motionEvent.offsetLocation(offsetX, offsetY);
            mTransformerView.setPivotX(getLeft() + dp2px(DRAW_AREA_WIDTH));
            mTransformerView.setPivotY(getTop() + dp2px(DRAW_AREA_WIDTH));
            mTransformerView.rotateLayout(motionEvent);
            return true;
        });
        //变大
        mEnlargeView.setOnTouchListener(new OnTouchListener() {
            float sx, sy;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        sx = motionEvent.getRawX();
                        sy = motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float x = motionEvent.getRawX();
                        float y = motionEvent.getRawY();
                        double radians = Math.toRadians(mTransformerView.getRotation());
                        double len = (x - sx) * Math.cos(radians) + (y - sy) * Math.sin(radians);
                        ViewGroup.LayoutParams params = mTransformerView.getLayoutParams();
                        params.height += (int) len;
                        params.width += (int) len;
                        if(params.height <= MIN_SIZE){
                            params.height = MIN_SIZE;
                            params.width = MIN_SIZE;
                        }
                        if (params.height >= MAX_SIZE) {
                            params.height = MAX_SIZE;
                            params.width = MAX_SIZE;
                        }
                        mTransformerView.setLayoutParams(params);
                        sx = x;
                        sy = y;
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "touch to enlarge triangle, current container width is " +
                                mTransformerView.getWidth());
                        break;
                }
                return true;
            }
        });
        //顶端画线
        mHorDrawArea.setOnTouchListener((view, motionEvent) -> {
            double k1, k2, b1, b2;
            float xj, rawX, rawY, cenX, cenY;
            cenX = mTransformerView.getTranslationX() + dp2px(DRAW_AREA_WIDTH);
            cenY = mTransformerView.getTranslationY() + dp2px(DRAW_AREA_WIDTH);
            rawX = motionEvent.getRawX();
            rawY = motionEvent.getRawY();
            k1 = (float) Math.tan(Math.toRadians(mTransformerView.getRotation()));
            k2 = -1 / k1;
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (Double.isInfinite(k1)) {
                        //垂直
                        startDrawX = cenX;
                        startDrawY = rawY;
                        break;
                    } else if (Double.isInfinite(k2)) {
                        //水平
                        startDrawX = rawX;
                        startDrawY = cenY;
                        break;
                    }
                    b1 = cenY - k1 * cenX;
                    b2 = rawY - k2 * rawX;
                    xj = (float) ((b2 - b1) / (k1 - k2));
                    startDrawX = xj;
                    startDrawY = (float) (xj * k1 + b1);
                    break;
                case MotionEvent.ACTION_MOVE:
                    path.reset();
                    path.moveTo(startDrawX, startDrawY);
                    if (Double.isInfinite(k1) || Double.isInfinite(k2)) {
                        if (Double.isInfinite(k1)) {
                            //垂直
                            path.lineTo(cenX, rawY);
                            drawLen = (rawY - startDrawY) / interval / 10;
                        } else if (Double.isInfinite(k2)) {
                            //水平
                            path.lineTo(rawX, cenY);
                            drawLen = (rawX - startDrawX) / interval / 10;
                        }
                    } else {
                        b1 = cenY - k1 * cenX;
                        b2 = rawY - k2 * rawX;
                        xj = (float) ((b2 - b1) / (k1 - k2));
                        endDrawX = xj;
                        endDrawY = (float) (k1 * xj + b1);
                        path.lineTo(endDrawX, endDrawY);
                        drawLen = (float) Math.sqrt(Math.pow(endDrawX - startDrawX, 2) +
                                Math.pow(endDrawY - startDrawY, 2)) / interval / 10;
                    }
                    String text = String.format(Locale.getDefault(), "%.2f", Math.abs(drawLen));
                    mResultTv.setText(text);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    onDeleteListener.copyPath(path);
                    Log.d(TAG, "touch to draw from (" +
                            startDrawX +","+ startDrawY +
                            ") to ("+
                            endDrawX +","+ endDrawY +
                            ")");
                    break;
            }
            return true;
        });
        //纵向画线
        mVerDrawArea.setOnTouchListener((view, motionEvent) -> {
            double k1, k2, b1, b2;
            float xj, rawX, rawY, cenX, cenY, res = 0f;
            cenX = mTransformerView.getTranslationX() + dp2px(DRAW_AREA_WIDTH);
            cenY = mTransformerView.getTranslationY() + dp2px(DRAW_AREA_WIDTH);
            rawX = motionEvent.getRawX();
            rawY = motionEvent.getRawY();
            k1 = Math.tan(Math.toRadians(mTransformerView.getRotation()));
            k2 = -1 / k1;
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (Double.isInfinite(k1)) {
                        //水平
                        startDrawX = rawX;
                        startDrawY = cenY;
                        break;
                    } else if (Double.isInfinite(k2)) {
                        //垂直
                        startDrawX = cenX;
                        startDrawY = rawY;
                        break;
                    }
                    b2 = cenY - k2 * cenX;
                    b1 = rawY - k1 * rawX;
                    xj = (float) ((b2 - b1) / (k1 - k2));
                    startDrawX = xj;
                    startDrawY = (float) (xj * k1 + b1);
                    break;
                case MotionEvent.ACTION_MOVE:
                    path.reset();
                    path.moveTo(startDrawX, startDrawY);
                    if (Double.isInfinite(k1) || Double.isInfinite(k2)) {
                        if (Double.isInfinite(k1)) {
                            //纵向画线区域水平
                            path.lineTo(rawX, cenY);
                            res = (rawX - startDrawX) / interval / 10;
                        } else if (Double.isInfinite(k2)) {
                            //纵向画线区域垂直
                            path.lineTo(cenX, rawY);
                            res = (rawY - startDrawY) / interval / 10;
                        }
                    } else {
                        b2 = cenY - k2 * cenX;
                        b1 = rawY - k1 * rawX;
                        xj = (float) ((b2 - b1) / (k1 - k2));
                        endDrawX = xj;
                        endDrawY = (float) (k1 * xj + b1);
                        path.lineTo(endDrawX, endDrawY);
                        res = (float) Math.sqrt(Math.pow(endDrawX - startDrawX, 2) + Math.pow(endDrawY - startDrawY, 2)) / interval / 10;
                    }
                    String text = String.format(Locale.getDefault(), "%.2f", Math.abs(res));
                    mResultTv.setText(text);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    onDeleteListener.copyPath(path);
                    Log.d(TAG, "touch to draw from (" + startDrawX +","+ startDrawY +") to ("+ endDrawX +","+ endDrawY +")");
                    break;
            }
            return true;
        });
    }

    private boolean isPointInPath(Path path, Point point) {
        RectF rect = new RectF();
        path.computeBounds(rect, true);
        Region region = new Region();
        region.setPath(path, new Region((int) rect.left, (int) rect.top, (int) rect.right, (int) rect.bottom));
        return region.contains(point.x, point.y);
    }

    private void inflateAndFindViews() {
        LayoutInflater.from(context).inflate(R.layout.triangle_ruler_view, this);
        Log.d(TAG, "add triangle as draw tool");
        mCloseView = findViewById(R.id.close);
        mRotateView = findViewById(R.id.rotate);
        mEnlargeView = findViewById(R.id.enlarge);
        mHorDrawArea = findViewById(R.id.draw_area_h);
        mVerDrawArea = findViewById(R.id.draw_area_v);
        mTransformerView = findViewById(R.id.transformer);
        mTriangleRulerView = findViewById(R.id.triangle_ruler);
        mResultTv = findViewById(R.id.tv_result);
    }
}