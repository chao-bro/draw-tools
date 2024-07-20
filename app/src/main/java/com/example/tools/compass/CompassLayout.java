package com.example.tools.compass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.tools.R;
import com.example.tools.adapters.AbstractStrokeViewGroup;

public class CompassLayout extends AbstractStrokeViewGroup {

    private final String TAG = "CompassLayout";

    public CompassLayout(Context context) {
        super(context);
    }

    public CompassLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        setBackgroundColor(Color.TRANSPARENT);
        inflateAndFindViews();
        setDefaultLoc();
        setEvents();
    }

    float sx, sy, ex, ey;

    @SuppressLint("ClickableViewAccessibility")
    private void setEvents() {
        //关闭
        vClose.setOnClickListener(view -> {
            ((FrameLayout) CompassLayout.this.getParent()).removeView(CompassLayout.this);
            Log.d(TAG, "click to close compass");
        });

        //移动全部
        vMoveAll.setOnTouchListener((view, motionEvent) -> {
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
                    //头部与两个脚的偏移
                    compassHead.setTranslationX(compassHead.getTranslationX() + dx);
                    leftLegContainer.setTranslationX(leftLegContainer.getTranslationX() + dx);
                    rightLegContainer.setTranslationX(rightLegContainer.getTranslationX() + dx);
                    compassHead.setTranslationY(compassHead.getTranslationY() + dy);
                    leftLegContainer.setTranslationY(leftLegContainer.getTranslationY() + dy);
                    rightLegContainer.setTranslationY(rightLegContainer.getTranslationY() + dy);
                    sx = rawX;
                    sy = rawY;
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "touch to move entire compass");
                    break;
            }
            return true;
        });

        //旋转右脚
        vRotateRight.setOnTouchListener((view, motionEvent) -> {
            int left = vRotateRight.getLeft();
            int top = vRotateRight.getTop();
            motionEvent.offsetLocation(left, top);
            rightLegContainer.setPivotX(getLeft());
            rightLegContainer.setPivotY(getTop());
            rightLegContainer.rotateLimitedAngle(motionEvent);
            return true;
        });

        //TODO 旋转全部和画线

    }

    private void setDefaultLoc() {
        //圆规高度
        int height = screenHeight / 2;
        //圆规宽度
        int width = height / 4;

        //圆规头部
        LayoutParams params = (LayoutParams) compassHead.getLayoutParams();
        params.height = height / 4;
        params.width = width;
        compassHead.setLayoutParams(params);

        //圆规两个脚
        //左、右脚容器宽度、高度、偏移
        LayoutParams paramLeftContainer = (LayoutParams) leftLegContainer.getLayoutParams();
        paramLeftContainer.height = height * 4 / 5;
        paramLeftContainer.width = width / 2 - 10;
        leftLegContainer.setLayoutParams(paramLeftContainer);

        LayoutParams paramRightContainer = (LayoutParams) rightLegContainer.getLayoutParams();
        paramRightContainer.width = width / 2 - 10;
        paramRightContainer.height = height * 4 / 5;
        rightLegContainer.setLayoutParams(paramRightContainer);

        LayoutParams paramClose = (LayoutParams) vClose.getLayoutParams();
        paramClose.width = height / 16;
        paramClose.height = height / 16;
        paramClose.setMargins(0, height / 16, 0, 0);
        vClose.setLayoutParams(paramClose);

        LayoutParams paramMoveAll = (LayoutParams) vMoveAll.getLayoutParams();
        paramMoveAll.width = height / 16;
        paramMoveAll.height = height / 16;
        paramMoveAll.setMargins(0, height / 8, 0, 0);
        vMoveAll.setLayoutParams(paramMoveAll);

        LayoutParams paramRotateAll = (LayoutParams) vRotateAll.getLayoutParams();
        paramRotateAll.width = width / 2 - 10;
        paramRotateAll.height = width / 2 - 10;
        paramRotateAll.setMargins(0, 0, 0, height * 2 / 5);
        vRotateAll.setLayoutParams(paramRotateAll);

        LayoutParams paramRotateRight = (LayoutParams) vRotateRight.getLayoutParams();
        paramRotateRight.width = width / 2 - 10;
        paramRotateRight.height = width / 2 - 10;
        paramRotateRight.setMargins(0, 0, 0, height / 5);
        vRotateRight.setLayoutParams(paramRotateRight);

        float transX = (screenWidth - width) / 2f;
        float transY = (screenHeight - height) / 2f;
        compassHead.setTranslationX(transX);
        compassHead.setTranslationY(transY);
        leftLegContainer.setTranslationX(transX + 5f);
        leftLegContainer.setTranslationY(transY + height / 5f);
        rightLegContainer.setTranslationX(transX + width / 2f + 5f);
        rightLegContainer.setTranslationY(transY + height / 5f);
    }

    private CompassHead compassHead;
    private RightLegContainer rightLegContainer;
    private LeftLegContainer leftLegContainer;
    private View vClose, vMoveAll, vRotateRight, vRotateAll;

    private void inflateAndFindViews() {
        LayoutInflater.from(context).inflate(R.layout.compass_view, this);
        compassHead = findViewById(R.id.compass_head);
        rightLegContainer = findViewById(R.id.right_container);
        leftLegContainer = findViewById(R.id.left_container);
        vClose = findViewById(R.id.close);
        vMoveAll = findViewById(R.id.move_all);
        vRotateRight = findViewById(R.id.rotate_right);
        vRotateAll = findViewById(R.id.rotate_all);
    }

    private float angle(Point cen, Point first, Point second) {
        float dx1 = first.x - cen.x;//oa x
        float dy1 = first.y - cen.y;//oa y
        float dx2 = second.x - cen.x;//ob x
        float dy2 = second.y - cen.y;//ob y
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
}
