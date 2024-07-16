package com.example.tools.ruler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.tools.R;
import com.example.tools.listener.OnDeleteListener;

public class RulerLayout extends RelativeLayout {

    private static final String TAG = "RULER_LAYOUT";
    private RulerView rulerView;
    private Context mContext;
    private View deleteV, addLenV, rotateV, drawAreaView;
    private TransferLayout transfer;
    private int lastX, lastY;
    private TextView result;

    private Path path;
    private Paint linePaint;

    private OnDeleteListener onDeleteListener;

    public RulerLayout(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public RulerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    private float defaultX, defaultY;

    @SuppressLint("ClickableViewAccessibility")
    private void init() {

        path = new Path();
        linePaint = new Paint();
        linePaint.setColor(Color.RED);
        //只绘制成线条。不会填充
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3);
        linePaint.setAntiAlias(true);
        linePaint.setAlpha(127);

        //设置背景色，防止上边距被裁掉
        setBackgroundColor(Color.TRANSPARENT);

        LayoutInflater.from(mContext).inflate(R.layout.ruler_view, this);

        rulerView = findViewById(R.id.ruler_view);
        addLenV = findViewById(R.id.add_length);
        deleteV = findViewById(R.id.close_view);
        rotateV = findViewById(R.id.rotate_view);
        transfer = findViewById(R.id.transfer);
        result = findViewById(R.id.result);
        drawAreaView = findViewById(R.id.draw_area);

        //设置直尺容器在屏幕中的位置
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        //获取屏幕的像素高度和宽度
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;

        defaultX = widthPixels / 2f - dp2px(560f) / 2;
        defaultY = heightPixels / 2f - dp2px(50f) / 2;
        transfer.setTranslationX(defaultX);
        transfer.setTranslationY(defaultY);

        setViewTouchListeners();
    }


    float cenX = 0.0f;
    float cenY = 0.0f;
    //直尺斜率
    double k1 = 0;
    //映射直线斜率
    double k2 = 0;
    float sx, sy;
    double drawLen = 0;
    String text = "";

    @SuppressLint("ClickableViewAccessibility")
    private void setViewTouchListeners() {
        //直尺移动
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
                    break;
            }
            return true;
        });

        //直尺顶部画线
        drawAreaView.setOnTouchListener((view, motionEvent) -> {
            cenX = transfer.getTranslationX();
            cenY = transfer.getTranslationY() + dp2px(20);
            float alpha = transfer.getRotation() % 360;
            k1 = Math.tan(Math.toRadians(transfer.getRotation()));
            k2 = -1 / k1;
            double b1 = cenY - k1 * cenX;
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (Double.isInfinite(k1)) {
                        //直尺垂直
                        sx = cenX;
                        sy = motionEvent.getRawY();
                        path.moveTo(sx, sy);
                        break;
                    } else if (Double.isInfinite(k2)) {
                        sx = motionEvent.getRawX();
                        sy = cenY;
                        path.moveTo(sx, sy);
                        break;
                    }
                    //计算按下点所在的垂线的偏移 b
                    float x = motionEvent.getRawX();
                    float y = motionEvent.getRawY();
                    double b2 = -k2 * x + y;

                    //计算垂线与直尺的焦点的 x 坐标，即垂直映射点的 x 值
                    double xj = (b1 - b2) / (k2 - k1);
                    Log.w(TAG, "setViewTouchListeners: xj = " + xj);

                    sx = (float) xj;
                    sy = (float) (k1 * xj + b1);
                    path.moveTo(sx, sy);

                    Log.d(TAG, "setViewTouchListeners: sx,sy" + sx + "," + sy);
                    break;
                case MotionEvent.ACTION_MOVE:
                    path.reset();
                    path.moveTo(sx,sy);
                    if (Double.isInfinite(k1)) {
                        path.lineTo(cenX, motionEvent.getRawY());
                        drawLen = (motionEvent.getRawY() - sy) / 40;
                        text = String.format("%.2f", drawLen);
                        result.setText(text);
                        break;
                    } else if (Double.isInfinite(k2)) {
                        path.lineTo(motionEvent.getRawX(), cenY);
                        drawLen = (motionEvent.getRawX() - sx) / 40;
                        text = String.format("%.2f", drawLen);
                        result.setText(text);
                        break;
                    }
                    x = motionEvent.getRawX();
                    y = motionEvent.getRawY();
                    b2 = -k2 * x + y;
                    //计算交点的x值
                    xj = (b1 - b2) / (k2 - k1);
                    float ex, ey;
                    ex = (float) xj;
                    ey = (float) (k1 * xj + b1);
                    path.lineTo(ex, ey);
                    invalidate();
                    drawLen = (Math.sqrt(Math.pow(ex - sx, 2) + Math.pow(ey - sy, 2))) / 40;
                    text = String.format("%.2f", drawLen);
                    result.setText(text);
                    break;
                case MotionEvent.ACTION_UP:
                    onDeleteListener.onDelete(path);
                    path.reset();
                    break;
            }
            return true;
        });

        //删除
        deleteV.setOnClickListener(view -> {
            onDeleteListener.onDelete(path);
            ((FrameLayout) RulerLayout.this.getParent()).removeView(RulerLayout.this);
        });

        /*
         * 旋转
         * */
        rotateV.setOnTouchListener((view, motionEvent) -> {
            float offsetX = rotateV.getLeft() - transfer.getScrollX();
            float offsetY = rotateV.getTop() - transfer.getScrollY();
            motionEvent.offsetLocation(offsetX, offsetY);
            transfer.setPivotX(getLeft());
            transfer.setPivotY(getTop() + dp2px(20));
            transfer.rotateLayout(motionEvent);
            return true;
        });

        /*
          拖动拉长直尺
         */
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
                    break;
            }
            return true;
        });
    }


    //实现画板效果
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, linePaint);
    }

    private float startX, startY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                path.moveTo(startX, startY);
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                path.lineTo(x, y);
                startX = x;
                startY = y;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                onDeleteListener.onDelete(path);
                path.reset();
                break;
            default:
                break;
        }
        return true;
    }
    public float dp2px(float dpValue) {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        float scale = metrics.density;
        return (dpValue * scale + 0.5f); // 加上0.5f是为了四舍五入
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }
}
