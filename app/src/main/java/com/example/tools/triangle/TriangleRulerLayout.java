package com.example.tools.triangle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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

    public TriangleRulerLayout(Context context) {
        super(context);
    }

    public TriangleRulerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void init() {
        inflateAndFindViews();
        setBackgroundColor(Color.TRANSPARENT);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int heightPixels = dm.heightPixels;
        int widthPixels = dm.widthPixels;
        float transX = (widthPixels - dp2px(300)) / 2;
        float transY = (heightPixels - dp2px(300)) / 2;
        transformer.setTranslationX(transX);
        transformer.setTranslationY(transY);
        setTouchEvents();
    }

    float dsx = 0f, dsy = 0f, dex = 0f, dey = 0f;

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchEvents() {
        //关闭
        close.setOnClickListener(view -> {
            ((FrameLayout) TriangleRulerLayout.this.getParent()).removeView(TriangleRulerLayout.this);
            onDeleteListener.copyPath(path);
        });

        //移动
        triangle.setOnTouchListener(new OnTouchListener() {
            float sx, sy;
            boolean inside = true;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        sx = motionEvent.getRawX();
                        sy = motionEvent.getRawY();
                        //判断按下区域是否在三角尺的范围上
                        Point point = new Point((int) motionEvent.getX(), (int) motionEvent.getY());
                        if (!isPointInPath(triangle.outPath, point)) {
                            inside = false;
                        } else if (isPointInPath(triangle.outPath, point) && isPointInPath(triangle.inPath, point)) {
                            inside = false;
                        }
//                        Log.d(TAG, "onTouch: is in out ?" + isPointInPath(triangle.outPath, point));
//                        Log.d(TAG, "onTouch: is in in ? " + isPointInPath(triangle.inPath, point));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float rawX = motionEvent.getRawX();
                        float rawY = motionEvent.getRawY();
                        if (!inside) {
                            //画线
                            path.moveTo(sx, sy);
                            path.lineTo(rawX, rawY);
                            invalidate();
                        } else {
                            float offsetX = transformer.getTranslationX();
                            float offsetY = transformer.getTranslationY();
                            float dx = rawX - sx;
                            float dy = rawY - sy;
                            transformer.setTranslationX(offsetX + dx);
                            transformer.setTranslationY(offsetY + dy);
                        }
                        sx = rawX;
                        sy = rawY;
                        break;
                    case MotionEvent.ACTION_UP:
                        inside = true;
                        break;
                }
                return true;
            }
        });

        //旋转
        rotate.setOnTouchListener((view, motionEvent) -> {
            float offsetX = rotate.getLeft();
            float offsetY = rotate.getTop();
            motionEvent.offsetLocation(offsetX, offsetY);
            transformer.setPivotX(getLeft() + dp2px(20));
            transformer.setPivotY(getTop() + dp2px(20));
            transformer.rotateLayout(motionEvent);
            return true;
        });

        //变大
        enlarge.setOnTouchListener(new OnTouchListener() {
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
                        double radians = Math.toRadians(transformer.getRotation());
                        double len = (x - sx) * Math.cos(radians) + (y - sy) * Math.sin(radians);
                        ViewGroup.LayoutParams params = transformer.getLayoutParams();
                        params.height += (int) len;
                        params.width += (int) len;
                        transformer.setLayoutParams(params);
                        sx = x;
                        sy = y;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });


        //顶端画线
        drawHor.setOnTouchListener((view, motionEvent) -> {
            double k1,k2,b1,b2;
            float xj, rawX, rawY, cenX, cenY, res = 0f;
            String text = "";
            cenX = transformer.getTranslationX() + dp2px(20);
            cenY = transformer.getTranslationY() + dp2px(20);
            rawX = motionEvent.getRawX();
            rawY = motionEvent.getRawY();
            k1 = (float) Math.tan(Math.toRadians(transformer.getRotation()));
            k2 = -1 / k1;
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (Double.isInfinite(k1)) {
                        //垂直
                        dsx = cenX;
                        dsy = rawY;
                        break;
                    } else if (Double.isInfinite(k2)) {
                        //水平
                        dsx = rawX;
                        dsy = cenY;
                        break;
                    }
                    b1 = cenY - k1 * cenX;
                    b2 = rawY - k2 * rawX;
                    xj = (float) ((b2 - b1) / (k1 - k2));
                    dsx = xj;
                    dsy = (float) (xj * k1 + b1);
                    break;
                case MotionEvent.ACTION_MOVE:
                    path.reset();
                    path.moveTo(dsx, dsy);
                    if (Double.isInfinite(k1) || Double.isInfinite(k2)) {
                        if (Double.isInfinite(k1)) {
                            //垂直
                            path.lineTo(cenX, rawY);
                            res = (rawY - dsy) / 40;
                        } else if (Double.isInfinite(k2)) {
                            //水平
                            path.lineTo(rawX, cenY);
                            res = (rawX - dsx) / 40;
                        }
                    } else {
                        b1 = cenY - k1 * cenX;
                        b2 = rawY - k2 * rawX;
                        xj = (float) ((b2 - b1) / (k1 - k2));
                        dex = xj;
                        dey = (float) (k1 * xj + b1);
                        path.lineTo(dex, dey);
                        res = (float) Math.sqrt(Math.pow(dex - dsx, 2) + Math.pow(dey - dsy, 2)) / 40;
                    }
                    text = String.format(Locale.getDefault(), "%.2f", Math.abs(res));
                    result.setText(text);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    onDeleteListener.copyPath(path);
                    break;
            }
            return true;
        });

        //纵向画线
        drawVer.setOnTouchListener((view, motionEvent) -> {
            double k1,k2,b1,b2;
            float  xj, rawX, rawY, cenX, cenY, res = 0f;
            String text = "";
            cenX = transformer.getTranslationX() + dp2px(20);
            cenY = transformer.getTranslationY() + dp2px(20);
            rawX = motionEvent.getRawX();
            rawY = motionEvent.getRawY();
            k1 =  Math.tan(Math.toRadians(transformer.getRotation()));
            k2 = -1 / k1;
//            Log.d(TAG, "setTouchEvents: k1 ???" + Double.isInfinite(k1));
//            Log.d(TAG, "setTouchEvents: k2 ???" + Double.isInfinite(k2));
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (Double.isInfinite(k1)) {
                        //水平
                        dsx = rawX;
                        dsy = cenY;
                        break;
                    } else if (Double.isInfinite(k2)) {
                        //垂直
                        dsx = cenX;
                        dsy = rawY;
                        break;
                    }
                    b2 = cenY - k2 * cenX;
                    b1 = rawY - k1 * rawX;
                    xj = (float) ((b2 - b1) / (k1 - k2));
                    dsx = xj;
                    dsy = (float) (xj * k1 + b1);
                    break;
                case MotionEvent.ACTION_MOVE:
                    path.reset();
                    path.moveTo(dsx, dsy);
                    if (Double.isInfinite(k1) || Double.isInfinite(k2)) {
                        if (Double.isInfinite(k1)) {
                            //纵向画线区域水平
                            path.lineTo(rawX, cenY);
                            res = (rawX - dsx) / 40;

                        } else if (Double.isInfinite(k2)) {
                            //纵向画线区域垂直
                            path.lineTo(cenX, rawY);
                            res = (rawY - dsy) / 40;
                        }
                    } else {
                        b2 = cenY - k2 * cenX;
                        b1 = rawY - k1 * rawX;
                        xj = (float) ((b2 - b1) / (k1 - k2));
                        dex = xj;
                        dey = (float) (k1 * xj + b1);
                        path.lineTo(dex, dey);
                        res = (float) Math.sqrt(Math.pow(dex - dsx, 2) + Math.pow(dey - dsy, 2)) / 40;
                    }
                    text = String.format(Locale.getDefault(), "%.2f", Math.abs(res));
                    result.setText(text);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    onDeleteListener.copyPath(path);
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

    private View close, rotate, enlarge, drawHor, drawVer;
    private TriangleRulerView triangle;
    private TransformTriangle transformer;
    private TextView result;

    private void inflateAndFindViews() {
        LayoutInflater.from(context).inflate(R.layout.triangle_ruler_view, this);
        close = findViewById(R.id.close);
        rotate = findViewById(R.id.rotate);
        enlarge = findViewById(R.id.enlarge);
        drawHor = findViewById(R.id.draw_area_h);
        drawVer = findViewById(R.id.draw_area_v);
        transformer = findViewById(R.id.transformer);
        triangle = findViewById(R.id.triangle_ruler);
        result = findViewById(R.id.result);
    }
}
