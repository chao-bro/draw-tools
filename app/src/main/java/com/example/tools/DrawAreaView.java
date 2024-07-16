package com.example.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Stack;

public class DrawAreaView extends View {

    private Paint paint;
    private Path currentPath;
    private Stack<Path> pathStack;
    private Stack<Path> undonePathStack;

    public DrawAreaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawAreaView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        currentPath = new Path();
        pathStack = new Stack<>();
        undonePathStack = new Stack<>();
    }

    int startX, startY;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) event.getRawX();
                startY = (int) event.getRawY();
                currentPath = new Path();
                currentPath.moveTo(startX, startY);
                undonePathStack.clear();
                break;
            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(event.getRawX(), event.getRawY());
                invalidate();
                startX = (int) event.getRawX();
                startY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                pathStack.push(new Path(currentPath));
                currentPath.reset();
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        for (Path path : pathStack) {
            canvas.drawPath(path, paint);
        }
        canvas.drawPath(currentPath, paint);

    }

    public void drawOnMe(Path path) {

        Path newPath = new Path(path); // 创建一个新的 Path 对象
        pathStack.push(newPath); // 将新路径添加到栈中
        invalidate();
    }

    public void undo() {
        if (!pathStack.isEmpty()) {
            undonePathStack.push(pathStack.pop());
            invalidate();
        }
    }

    public void redo() {
        if (!undonePathStack.isEmpty()) {
            pathStack.push(undonePathStack.pop());
            invalidate();
        }
    }

    public void clear() {
        pathStack.clear();
        undonePathStack.clear();
        invalidate();
    }
}
