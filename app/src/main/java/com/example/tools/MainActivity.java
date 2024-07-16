package com.example.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tools.adapters.AbstractStrokeViewGroup;
import com.example.tools.ruler.RulerLayout;

public class MainActivity extends AppCompatActivity {

    private FrameLayout main;
    private boolean show = false;
    private AbstractStrokeViewGroup toolV;
    private LinearLayout tools,option;
    private DrawAreaView drawAreaView;
    private Button btAdd,btRuler,btTriangle,btProtractor,btCompass,btUndo, btRedo, btClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setEvents();
    }

//    setEvents 方法为按钮设置点击事件。btAdd 按钮用于显示或隐藏工具列表。
//    其他按钮 (btRuler, btTriangle, btProtractor, btCompass) 则用于添加相应的工具视图
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

        btRuler.setOnClickListener(view -> addNewTool(new RulerLayout(MainActivity.this)));

        btTriangle.setOnClickListener(view -> addNewTool(new RulerLayout(MainActivity.this)));

        btProtractor.setOnClickListener(view -> addNewTool(new RulerLayout(MainActivity.this)));

        btCompass.setOnClickListener(view -> addNewTool(new RulerLayout(MainActivity.this)));

        btUndo.setOnClickListener(view -> drawAreaView.undo());
        btRedo.setOnClickListener(view -> drawAreaView.redo());
        btClear.setOnClickListener(view -> drawAreaView.clear());
    }
//addNewTool 方法用于添加新的工具视图。它首先移除当前的工具视图 (toolV)，然后添加新的工具视图，并设置删除监听器。
// 当新工具视图被添加后，隐藏工具列表，并将选项布局 (option) 移到前面。
    private void addNewTool(AbstractStrokeViewGroup tool) {
        main.removeView(toolV);
        toolV = tool;
        toolV.setOnDeleteListener(path -> MainActivity.this.drawAreaView.drawOnMe(path));
        main.addView(toolV);
        tools.setVisibility(View.GONE);
        main.bringChildToFront(option);
        show = false;
    }

//    initViews 方法通过 findViewById 方法获取布局文件中的视图，并初始化它们。
    private void initViews() {
        btAdd  = findViewById(R.id.add_tools);

        tools = findViewById(R.id.tool_list);
        tools.setVisibility(View.GONE); // 初始状态下隐藏工具列表
        option = findViewById(R.id.option);

        drawAreaView = findViewById(R.id.draw_area);

        toolV = findViewById(R.id.ruler);
        toolV.setOnDeleteListener(path -> MainActivity.this.drawAreaView.drawOnMe(path));

        main = findViewById(R.id.main);
        main.bringChildToFront(option);

        btRuler = findViewById(R.id.bt_ruler);
        btTriangle = findViewById(R.id.bt_triangle);
        btProtractor = findViewById(R.id.bt_protractor);
        btCompass = findViewById(R.id.bt_compass);

        btUndo = findViewById(R.id.bt_undo);
        btRedo = findViewById(R.id.bt_redo);
        btClear = findViewById(R.id.bt_clear);
    }
}