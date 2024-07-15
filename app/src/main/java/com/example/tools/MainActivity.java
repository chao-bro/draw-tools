package com.example.tools;

import android.graphics.Path;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tools.listener.OnDeleteListener;
import com.example.tools.ruler.RulerLayout;

public class MainActivity extends AppCompatActivity {

    private FrameLayout main;
    private boolean show = false;
    private View toolV;
    private LinearLayout tools;
    private DrawAreaView drawAreaView;
    private Button btAdd,btRuler,btSanJiao,btLiangJiao,btYuanGui;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setEvents();
        setOnDeleteEvent(toolV);
    }

    private void setEvents() {
        btAdd.setOnClickListener(view -> {
            if (! show) {
                tools.setVisibility(View.VISIBLE);
                show = true;
            } else {
                tools.setVisibility(View.GONE);
                show = false;
            }
        });

        btRuler.setOnClickListener(view -> {
            main.removeView(toolV);
            toolV = new RulerLayout(MainActivity.this);
            setOnDeleteEvent(toolV);
            main.addView(toolV);
            tools.setVisibility(View.GONE);
            show = false;
        });

        btSanJiao.setOnClickListener(view -> {
            main.removeView(toolV);
            toolV = new RulerLayout(MainActivity.this);
            setOnDeleteEvent(toolV);
            main.addView(toolV);
            tools.setVisibility(View.GONE);
            show = false;
        });

        btLiangJiao.setOnClickListener(view -> {
            main.removeView(toolV);
            toolV = new RulerLayout(MainActivity.this);
            setOnDeleteEvent(toolV);
            main.addView(toolV);
            tools.setVisibility(View.GONE);
            show = false;
        });

        btYuanGui.setOnClickListener(view -> {
            main.removeView(toolV);
            toolV = new RulerLayout(MainActivity.this);
            setOnDeleteEvent(toolV);
            main.addView(toolV);
            tools.setVisibility(View.GONE);
            show = false;
        });
    }

    private void initViews() {
        btAdd  = findViewById(R.id.add_tools);
        tools = findViewById(R.id.tool_list);
        tools.setVisibility(View.GONE);

        drawAreaView = findViewById(R.id.draw_area);
        toolV = findViewById(R.id.ruler);
        main = findViewById(R.id.main);
        btRuler = findViewById(R.id.bt_ruler);
        btSanJiao = findViewById(R.id.bt_san_jiao);
        btLiangJiao = findViewById(R.id.bt_liang_jiao);
        btYuanGui = findViewById(R.id.bt_yuan_gui);
    }

    private void setOnDeleteEvent(View toolV) {
        if (toolV instanceof RulerLayout) {
            RulerLayout ruler = (RulerLayout) toolV;
            ruler.setOnDeleteListener(path -> {
                MainActivity.this.drawAreaView.drawOnMe(path);
            });
        }
    }


}