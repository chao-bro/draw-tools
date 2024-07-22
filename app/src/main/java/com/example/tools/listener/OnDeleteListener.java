package com.example.tools.listener;

import android.graphics.Paint;
import android.graphics.Path;

public interface OnDeleteListener {
    void copyPath(Path path);
    void copyPath(Path path, Paint paint);
}
