package com.example.tools.triangle;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class TriangleRulerTransformer extends RelativeLayout {

    private static final String TAG = "TriangleRulerTransformer";

    public TriangleRulerTransformer(Context context) {
        super(context);
    }

    public TriangleRulerTransformer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public float layoutDegree = 0f;
    private float startRotateX = 0f;
    private float startRotateY = 0f;
    public void rotateLayout(MotionEvent event) {
        //处理旋转事件
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startRotateX = event.getX();
                startRotateY = event.getY();
                layoutDegree = getRotation();
                break;
            case MotionEvent.ACTION_MOVE:
                float tempRawX = event.getX();
                float tempRawY = event.getY();
                Point first = new Point((int) startRotateX, (int) startRotateY);
                Point second = new Point((int) tempRawX, (int) tempRawY);
                Point cen = new Point( getLeft(),getTop());
                // 计算旋转角度
                float angle = angle(cen, first, second);
                layoutDegree += angle;
                setRotation(layoutDegree);
                break;
            default:
                Log.d(TAG, "touch to rotate triangle , current rotation degree is " +
                        getRotation() % 360);
                break;
        }
    }

    // 计算两向量之间的旋转角度
    private float angle(Point cen, Point first, Point second) {
        float dx1 = first.x - cen.x;
        float dy1 = first.y - cen.y;
        float dx2 = second.x - cen.x;
        float dy2 = second.y - cen.y;
        float ab2 = (float) ((second.x - first.x) * (second.x - first.x) + (second.y - first.y) * (second.y - first.y));
        float oa2 = dx1 * dx1 + dy1 * dy1;
        float ob2 = dx2 * dx2 + dy2 * dy2;
        boolean isClockwise = (first.x - cen.x) * (second.y - cen.y) - (first.y - cen.y) * (second.x - cen.x) > 0;
        float cosDegree = (float) ((oa2 + ob2 - ab2) / (2 * Math.sqrt(oa2) * Math.sqrt(ob2)));
        if (cosDegree > 1) {
            cosDegree = 1.0f;
        } else if (cosDegree < -1) {
            cosDegree = -1.0f;
        }
        float radian = (float) Math.acos(cosDegree);
        return isClockwise ? (float) Math.toDegrees(radian) : -(float) Math.toDegrees(radian);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

}
