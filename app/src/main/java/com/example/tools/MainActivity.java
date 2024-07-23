package com.example.tools;

import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.MotionEvent;
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
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private FrameLayout mMainContainer;
    private boolean isToolsVisible = false;
    private HashSet<AbstractStrokeViewGroup> viewSet;
    private LinearLayout mToolsBtnLl, mOptionBtnLl;
    private DrawAreaView mDrawAreaView;
    private Button mAddToolsBtn, mAddRulerBtn, mAddTriangleBtn, mAddProtractorBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewSet = new HashSet<>();
        initViews();
        setEvents();
    }

    private void setEvents() {
        mAddToolsBtn.setOnClickListener(view -> {
            if (!isToolsVisible) {
                mToolsBtnLl.setVisibility(View.VISIBLE);
                isToolsVisible = true;
            } else {
                mToolsBtnLl.setVisibility(View.GONE);
                isToolsVisible = false;
            }
        });

        mAddRulerBtn.setOnClickListener(view -> addNewTool(new RulerLayout(MainActivity.this)));

        mAddTriangleBtn.setOnClickListener(view -> addNewTool(new TriangleRulerLayout(MainActivity.this)));

        mAddProtractorBtn.setOnClickListener(view -> addNewTool(new ProtractorLayout(MainActivity.this)));

    }

    private void addNewTool(AbstractStrokeViewGroup tool) {
        tool.setOnDeleteListener(new OnDeleteListener() {
            @Override
            public void copyPath(Path path) {
                MainActivity.this.mDrawAreaView.drawOnMe(path);
            }

            @Override
            public void copyPath(Path path, Paint paint) {
                MainActivity.this.mDrawAreaView.drawOnMe(path, paint);
            }
        });
        for (AbstractStrokeViewGroup t : viewSet) {
            if (tool.getClass() == t.getClass()) {
                //存在
                mMainContainer.removeView(t);
                viewSet.remove(t);
                break;
            }
        }
        mMainContainer.addView(tool);
        viewSet.add(tool);
        mToolsBtnLl.setVisibility(View.GONE);
        mMainContainer.bringChildToFront(mOptionBtnLl);
        isToolsVisible = false;
    }

    private void initViews() {
        mAddToolsBtn = findViewById(R.id.rulerset_add_tools);
        mToolsBtnLl = findViewById(R.id.rulerset_tool_list);
        mToolsBtnLl.setVisibility(View.GONE);
        mOptionBtnLl = findViewById(R.id.rulerset_option);
        mDrawAreaView = findViewById(R.id.draw_area);
        mMainContainer = findViewById(R.id.main);
        mAddRulerBtn = findViewById(R.id.rulerset_bt_ruler);
        mAddTriangleBtn = findViewById(R.id.rulerset_bt_triangle);
        mAddProtractorBtn = findViewById(R.id.rulerset_bt_protractor);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!super.dispatchTouchEvent(ev)) {
            for (AbstractStrokeViewGroup t : viewSet) {
                if (t.dispatchTouchEvent(ev)) {
                    mMainContainer.bringChildToFront(t);
                    break;
                }
            }
        }
        return true;
    }
}