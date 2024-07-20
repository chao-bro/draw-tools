package com.example.tools.compass;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class RightLegContainer extends RelativeLayout {
    private static final String TAG = "RightLegContainer";
    private float oriX = 0f, oriY = 0f;

    public RightLegContainer(Context context) {
        super(context);
    }

    public RightLegContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    //这个是右脚自身的旋转角度(0° to -75°)
    private float rotateDegree = 0f;

    public void rotateLimitedAngle(MotionEvent event) {
        //只能旋转一定角度
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                oriX = event.getX();
                oriY = event.getY();
                rotateDegree = getRotation();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                Point cen = new Point(getLeft(),getTop());
                Point first = new Point((int) oriX, (int) oriY);
                Point second = new Point((int) x, (int) y);
                float angle = angle(cen, first, second);
                rotateDegree += angle;
                if(rotateDegree > 0){
                    rotateDegree = 0;
                } else if (rotateDegree < -75) {
                    rotateDegree = -75;
                }
                setRotation(rotateDegree);
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "move to rotate the right leg only");
                break;
        }
    }

    // 计算两向量之间的旋转角度
    private float angle(Point cen, Point first, Point second) {
        //点 o a b
        float dx1 = first.x - cen.x;//oa x
        float dy1 = first.y - cen.y;//oa y
        float dx2 = second.x - cen.x;//ob x
        float dy2 = second.y - cen.y;//ob y

        // 计算三条边的平方
        float ab2 = (float) ((second.x - first.x) * (second.x - first.x) + (second.y - first.y) * (second.y - first.y));
        float oa2 = dx1 * dx1 + dy1 * dy1;
        float ob2 = dx2 * dx2 + dy2 * dy2;

        // 根据两向量的叉乘判断顺逆时针和右手定则
        /*

                           |i,     j,     k|
                oa x ob =  |oax,   oay,   0|   =  (0, 0, (oax * oby - oay * obx) * k)法向量
                           |obx,   oby,   0|

                其中(i,j,k) = (1,1,1)

                法向量k值大于零为逆时针，法向量小于0为顺时针

        */
        boolean isClockwise = (first.x - cen.x) * (second.y - cen.y) - (first.y - cen.y) * (second.x - cen.x) > 0;

        // 计算旋转角度的余弦值（余弦定理）
        /*
                          a2 + b2 - c2
               cos α = ------------------
                           2 · a · b
        */
        float cosDegree = (float) ((oa2 + ob2 - ab2) / (2 * Math.sqrt(oa2) * Math.sqrt(ob2)));

        // 处理余弦值超出范围的情况
        if (cosDegree > 1) {
            cosDegree = 1.0f;
        } else if (cosDegree < -1) {
            cosDegree = -1.0f;
        }
        // 计算弧度
        float radian = (float) Math.acos(cosDegree);
        // 计算旋转角度，顺时针为正，逆时针为负（屏幕的y轴是向下的，所以旋转方向取反之后才能获取到在屏幕上实际的旋转方向）
        return isClockwise ? (float) Math.toDegrees(radian) : -(float) Math.toDegrees(radian);
    }
}
