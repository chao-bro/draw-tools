package com.example.tools;

import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tools.adapters.AbstractStrokeViewGroup;
import com.example.tools.listener.OnDeleteListener;
import com.example.tools.protractor.ProtractorLayout;
import com.example.tools.ruler.RulerLayout;
import com.example.tools.triangle.TriangleRulerLayout;

public class MainActivity extends AppCompatActivity {

    private FrameLayout main;
    private boolean show = false;
    private AbstractStrokeViewGroup toolV;
    private LinearLayout tools,option;
    private DrawAreaView drawAreaView;
    private Button btAdd,btRuler,btTriangle,btProtractor,btCompass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setEvents();
        addNewTool(new ProtractorLayout(this));
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

        btRuler.setOnClickListener(view -> addNewTool(new RulerLayout(MainActivity.this)));

        btTriangle.setOnClickListener(view -> addNewTool(new TriangleRulerLayout(MainActivity.this)));

        btProtractor.setOnClickListener(view -> addNewTool(new ProtractorLayout(MainActivity.this)));

        btCompass.setOnClickListener(view -> addNewTool(new RulerLayout(MainActivity.this)));
    }

    private void addNewTool(AbstractStrokeViewGroup tool) {
        main.removeView(toolV);
        toolV = tool;
        toolV.setOnDeleteListener(new OnDeleteListener() {
            @Override
            public void copyPath(Path path) {
                MainActivity.this.drawAreaView.drawOnMe(path);
            }

            @Override
            public void copyPath(Path path, Paint paint) {
                MainActivity.this.drawAreaView.drawOnMe(path,paint);
            }
        });
        main.addView(toolV);
        tools.setVisibility(View.GONE);
        main.bringChildToFront(option);
        show = false;
    }

    private void initViews() {
        btAdd  = findViewById(R.id.add_tools);

        tools = findViewById(R.id.tool_list);
        tools.setVisibility(View.GONE);
        option = findViewById(R.id.option);

        drawAreaView = findViewById(R.id.draw_area);

//        toolV = new TriangleRulerLayout(this)
        main = findViewById(R.id.main);
        btRuler = findViewById(R.id.bt_ruler);
        btTriangle = findViewById(R.id.bt_triangle);
        btProtractor = findViewById(R.id.bt_protractor);
        btCompass = findViewById(R.id.bt_compass);
    }
}