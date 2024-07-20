package com.example.tools.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tools.adapters.AbstractBasicView;

public class RightLegView extends AbstractBasicView {
    @Override
    protected void init() {
        paint.setStyle(Paint.Style.FILL);
    }

    public RightLegView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        path.moveTo(getLeft(),getBottom() - 20);
        path.lineTo(getLeft() + 10,getBottom() - 20);
        path.lineTo(getLeft(),getBottom());
        path.close();
        canvas.drawPath(path,paint);
        paint.setAlpha(20);
        canvas.drawRect(getLeft(),getTop(),getRight(),getBottom() - 20,paint);
    }
}
