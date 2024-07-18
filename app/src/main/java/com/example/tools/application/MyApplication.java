package com.example.tools.application;

import android.app.Application;
import android.util.DisplayMetrics;

public class MyApplication extends Application {
    //目前没啥用捏？

    private static MyApplication app = null;

    private MyApplication() {
    }

    public static MyApplication getInstance() {
        if (app == null) {
            app = new MyApplication();
        }
        return app;
    }

}
