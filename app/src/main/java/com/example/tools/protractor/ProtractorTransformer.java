package com.example.tools.protractor;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class ProtractorTransformer extends RelativeLayout {
    public ProtractorTransformer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
    }
}
