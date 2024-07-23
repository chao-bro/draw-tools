package com.example.tools;

import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tools.adapters.AbstractStrokeViewGroup;
import com.example.tools.listener.OnDeleteListener;
import com.example.tools.protractor.ProtractorLayout;
import com.example.tools.ruler.RulerLayout;
import com.example.tools.triangle.TriangleRulerLayout;

import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private FrameLayout main;
    private boolean show = false;
    private AbstractStrokeViewGroup toolV;
    private HashSet<AbstractStrokeViewGroup> viewSet;
    private LinearLayout tools, option;
    private DrawAreaView drawAreaView;
    private Button btAdd, btRuler, btTriangle, btProtractor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initViews();
        setEvents();
        viewSet = new HashSet<>();
    }

    private void setEvents() {
        btAdd.setOnClickListener(view -> {
            if (!show) {
                tools.setVisibility(View.VISIBLE);
                show = true;
            } else {
                tools.setVisibility(View.GONE);
                show = false;
            }
        });

        btRuler.setOnClickListener(view -> addNewTool(new RulerLayout(MainActivity.this)));

        btTriangle.setOnClickListener(view -> addNewTool(new TriangleRulerLayout(MainActivity.this)));

        btProtractor.setOnClickListener(view -> addNewTool(new ProtractorLayout(MainActivity.this)));

    }

    private void addNewTool(AbstractStrokeViewGroup tool) {
        tool.setOnDeleteListener(new OnDeleteListener() {
            @Override
            public void copyPath(Path path) {
                MainActivity.this.drawAreaView.drawOnMe(path);
            }

            @Override
            public void copyPath(Path path, Paint paint) {
                MainActivity.this.drawAreaView.drawOnMe(path, paint);
            }
        });
        toolV = tool;
        for (AbstractStrokeViewGroup t : viewSet) {
            if (tool.getClass() == t.getClass()) {
                //存在
                main.removeView(t);
                viewSet.remove(t);
                break;
            }
        }
        main.addView(toolV);
        viewSet.add(toolV);
        tools.setVisibility(View.GONE);
        main.bringChildToFront(option);
        show = false;
    }

    private void initViews() {
        btAdd = findViewById(R.id.rulerset_add_tools);
        tools = findViewById(R.id.rulerset_tool_list);
        tools.setVisibility(View.GONE);
        option = findViewById(R.id.rulerset_option);
        drawAreaView = findViewById(R.id.draw_area);
        main = findViewById(R.id.main);
        btRuler = findViewById(R.id.rulerset_bt_ruler);
        btTriangle = findViewById(R.id.rulerset_bt_triangle);
        btProtractor = findViewById(R.id.rulerset_bt_protractor);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!super.dispatchTouchEvent(ev)) {
            if (!toolV.dispatchTouchEvent(ev)) {
                for (AbstractStrokeViewGroup t : viewSet) {
                    if (t.dispatchTouchEvent(ev)) {
                        toolV = t;
                        main.bringChildToFront(toolV);
                        break;
                    }
                }
            }
        }
        return true;
    }
}