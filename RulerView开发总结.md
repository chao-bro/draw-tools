## 功能概述

        直尺被添加时，显示在屏幕正中央，右下角的加号可以用来像屏幕中添加工具。
直尺功能：删除、旋转、拖拽移动、拖拽拉长、直尺上方小区域内滑动可绘制直线，绘制完成之后会在支持的中央显示此次绘制的长度（绘制时跟手暂未实现）
整体的设计思路参考网址为[https://juejin.cn/post/7288963080855781413#heading-5](https://juejin.cn/post/7288963080855781413#heading-5)，需要功能的实现也是参照原博主`gitee`仓库的代码，后续对其部分功能进行了调整，原博客是实现了缩放功能的，这里并没有实现那个功能。可以参考上述网址，了解下偏移的概念。还有`path`绘制的相关基础知识也建议先了解一下。

## 总体架构

关于这一个项目的架构：

```java
主程序（帧布局，铺满全屏）{
    一个画板（铺满全屏）
    操作按钮（wrap_content 显示在屏幕对应位置）
    被添加的工具绘制区域，也是工具的可移动区域（铺满全屏）{
        工具容器的显示限制区域（固定初始大小、可以移动和旋转）{
            工具（match_parent）
            工具的操作按钮<删除、旋转、拉长区域>（显示在工具容器的对应位置）
        }
    }
}
```

        主页面使用`FrameLayout`铺满全屏，去除标题栏，全屏显示画板；
根布局下使用自定义画板`DrawAreaView`铺满根布局，并在右下角添加可以增加工具的一系列按钮，加号按钮旁边的工具按钮默认不显示，点击加号之后会显示，尽可能增加屏幕可绘制区域；
        根布局下与画板同级添加工具容器`RulerLayoutView`直接铺满整个画板，设置背景为透明，避免遮挡已绘制的内容。`RulerLayoutView`为直尺工具的可操作范围；
`RulerLayoutView`容器使用一个`Merge`标签的`xml`文件进行布局，布局内容包括，一个自定义的直尺容器`TransferView`，一个自定义的直尺`RulerView`，删除按钮，旋转按钮，拉动区域，可画线区域。
        `TransferView`是用来控制整个直尺工具的显示位置和大小的，因为在直尺工具被拉长或者被旋转之后，旋转删除这些按钮也要对应的修改显示位置，如果全部放在直尺工具中绘制容易出现变形的问题，`RulerView`就是一个用来绘制直尺内需要实现的样式的自定义`View`。
先把布局代码先展示一下，共两个布局文件：`activity_main.xml`、`ruler_view.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout
  xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:app="http://schemas.android.com/apk/res-auto"
  xmlns:tools="http://schemas.android.com/tools"
  android:id="@+id/main"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  tools:context=".MainActivity">

  <com.example.tools.DrawAreaView
    android:id="@+id/draw_area"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />

  <com.example.tools.ruler.RulerLayout
    android:id="@+id/ruler"
    android:background="@color/black"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />

  <Button
    android:id="@+id/add_tools"
    android:layout_gravity="bottom|end"
    android:text="+"
    android:textSize="28sp"
    android:layout_width="48dp"
    android:layout_marginBottom="24dp"
    android:layout_marginEnd="24dp"
    android:insetTop="0dp"
    android:insetBottom="0dp"
    android:layout_height="wrap_content"/>

  <LinearLayout
    android:id="@+id/tool_list"
    android:visibility="gone"
    android:orientation="horizontal"
    android:layout_width="wrap_content"
    android:layout_gravity="end|bottom"
    android:layout_marginBottom="24dp"
    android:layout_marginEnd="76dp"
    android:layout_height="wrap_content">

    <Button
      android:id="@+id/bt_yuan_gui"
      android:layout_width="64dp"
      android:layout_height="wrap_content"
      android:layout_gravity="bottom|end"
      android:layout_marginEnd="8dp"
      android:insetTop="0dp"
      android:insetBottom="0dp"
      android:text="ruler"
      android:textSize="12sp" />

    <Button
      android:id="@+id/bt_ruler"
      android:layout_gravity="bottom|end"
      android:text="sanjiao"
      android:textSize="12sp"
      android:layout_marginEnd="8dp"
      android:layout_width="64dp"
      android:insetTop="0dp"
      android:insetBottom="0dp"
      android:layout_height="wrap_content"/>

    <Button
      android:id="@+id/bt_san_jiao"
      android:layout_gravity="bottom|end"
      android:text="liangjiaoqi"
      android:textSize="10sp"
      android:layout_width="72dp"
      android:layout_marginEnd="8dp"
      android:insetTop="0dp"
      android:insetBottom="0dp"
      android:layout_height="wrap_content"/>

    <Button
      android:id="@+id/bt_liang_jiao"
      android:layout_gravity="bottom|end"
      android:text="yuangui"
      android:textSize="11sp"
      android:layout_width="64dp"
      android:layout_marginEnd="8dp"
      android:insetTop="0dp"
      android:insetBottom="0dp"
      android:layout_height="wrap_content"/>
    </LinearLayout>
</FrameLayout>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<merge
  xmlns:android="http://schemas.android.com/apk/res/android"
  android:layout_width="match_parent"
  android:layout_height="match_parent">

  <com.example.tools.ruler.RulerViewTransformer
    android:id="@+id/transfer"
    android:layout_width="560dp"
    android:layout_height="70dp">

    <View
      android:id="@+id/draw_area"
      android:layout_width="match_parent"
      android:layout_height="20dp" />

    <com.example.tools.ruler.RulerView
      android:layout_marginTop="20dp"
      android:id="@+id/ruler_view"
      android:layout_width="match_parent"
      android:layout_height="match_parent" />

    <TextView
      android:id="@+id/result"
      android:textColor="@color/black"
      android:layout_centerHorizontal="true"
      android:layout_alignParentBottom="true"
      android:textSize="12sp"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"/>

    <View
      android:id="@+id/add_length"
      android:layout_marginTop="20dp"
      android:layout_width="30dp"
      android:layout_height="match_parent"
      android:layout_alignParentEnd="true" />

    <View
      android:id="@+id/close_view"
      android:layout_alignParentBottom="true"
      android:layout_width="15dp"
      android:layout_height="15dp"
      android:layout_marginStart="5dp"
      android:layout_marginTop="25dp"
      android:background="@drawable/close_btn" />

    <View
      android:id="@+id/rotate_view"
      android:layout_width="15dp"
      android:layout_height="15dp"
      android:layout_alignParentEnd="true"
      android:layout_alignParentBottom="true"
      android:layout_marginStart="4dp"
      android:layout_marginBottom="4dp"
      android:background="@drawable/rotate_btn" />

  </com.example.tools.ruler.RulerViewTransformer>

</merge>
```

## 实现

### 最内层：`RulerView`

        在这个`view`当中需要继承`View`类，然后重写`onDraw`方法，因为直尺工具可以直接在布局文件当中指定铺满父容器，所以就可以不用考虑`onMeasured`方法这些，专心绘制支持的样式即可。直尺类的代码也比较简单就可以看懂：

```java
public class RulerView extends View {
    //成员变量和构造方法...
    //初始化数据
    private void initData() {
        //画笔的其他属性设置...
        //设置不透明度 0 - 255，透明 - 不透明
        bgPaint.setAlpha(125);

        //设置字体字号等信息调用这个方法来计算字体的高度
        Paint.FontMetrics fm = paint.getFontMetrics();
        //计算字体的高度，顶-底
        textHeight = (int) (fm.bottom - fm.top);
    }
    //被父容器安排时会调用，此时当前view的位置、大小都已确定
    @Override
    protected void onLayout
        (boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //设置刻度数目，-2是为了在前面和后面各留一点点空间，好看一点
        numKD = getWidth() / KE_DU_WIDTH - 2;
        minLength = getHeight() / 8f;
    }
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        //画直尺背景和边框（也可以只用一支画笔画）
        canvas.drawRoundRect(0f, 0f, getWidth(), getHeight(), 0.5f, 0.5f, bgPaint);
        canvas.drawRoundRect(0f, 0f, getWidth(), getHeight(), 0.5f, 0.5f, paint);
        //画刻度，drawLine方法需要绘制的线的起点和终点，还有画笔的样式
        for (int i = 1; i <= numKD; i++) {
            int wid = i * KE_DU_WIDTH;
            if (i % 5 == 1) {
                canvas.drawLine(wid,0f,wid,minLength * 2.5f,paint);
                //在长线的下面需要显示的数字，我这里设置的是每五个空格算作一个单位长度
                String text = (i - 1) / 5 + "";
                canvas.drawText(text,wid - paint.measureText(text) / 2,
                                minLength * 2.5f + textHeight / 2,paint);
            } else {
                //小刻度
                canvas.drawLine(wid,0f,wid,minLength,paint);
            }
        }
    }
```

### 直尺外层（可变化容器）TransferView

        这一层需要提供的代码也比较简单，主要区别就是`TransferView`需要继承一个布局容器类，我这里继承的是`RelativeLayout`，（网上找的资料使用的是`RelativeLayout`，应该随便一个容器对象也行的），在这个类里面重点需要实现的方法是旋转方法，因为拉长和移动这个容器是需要在外层来操作实现的，但是旋转之后的展示需要自己实现，为了节约篇幅，只提供关键代码：其中`angle`方法在别的场合也能用到，旋转方向与旋转角度的判断都可以使用这个思路。

```java
//旋转方法，外层的容器需要调用这个方法来旋转
public boolean rotateLayout(MotionEvent event) {
//处理旋转事件
    switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
            //获取当前点击位置在容器当中的位置
            oriX = event.getX();
            oriY = event.getY();
            //记录容器当前的旋转角度（角度不是弧度）
            layoutDegree = getRotation();
            break;
        case MotionEvent.ACTION_MOVE:
            //移动中的当前坐标
            float tempRawX = event.getX();
            float tempRawY = event.getY();
            //first为容器内初始按下点、
            //second为容器内触摸移动点、
            //cen为一个参考点，这里使用的是容器左上角
            Point first = new Point((int) oriX, (int) oriY);
            Point second = new Point((int) tempRawX, (int) tempRawY);
            Point cen = new Point(getLeft(), getTop());
            // 计算旋转角度
            float angle = angle(cen, first, second);
            //累加每次旋转的角度，达到跟手的目的
            layoutDegree += angle;
            //设置旋转角度后就会显示旋转之后组件
            setRotation(layoutDegree);
            break;
        case MotionEvent.ACTION_UP:
            break;
        default:
            break;
    }
    return true;
}
// 计算两向量之间的旋转角度
private float angle(Point cen, Point first, Point second) {
    //oa(dx1,dy1)  ob(dx2,dy2)  两个向量
    float dx1 = first.x - cen.x;
    float dy1 = first.y - cen.y;
    float dx2 = second.x - cen.x;
    float dy2 = second.y - cen.y;
    // 计算三条边的平方
    //向量计算 ab = ob - oa
    float ab2 = (float) ((second.x - first.x) * (second.x - first.x) + 
                         (second.y - first.y) * (second.y - first.y));
    float oa2 = dx1 * dx1 + dy1 * dy1;
    float ob2 = dx2 * dx2 + dy2 * dy2;

    // 根据两向量的叉乘判断顺逆时针
    boolean isClockwise = (first.x - cen.x) * (second.y - cen.y) - 
    (first.y - cen.y) * (second.x - cen.x) > 0;
    // 计算旋转角度的余弦值
    float cosDegree = (float) ((oa2 + ob2 - ab2) / (2 * Math.sqrt(oa2) * Math.sqrt(ob2)));
    // 处理余弦值超出范围的情况
    if (cosDegree > 1) {
        cosDegree = 1.0f;
    } else if (cosDegree < -1) {
        cosDegree = -1.0f;
    }
    // 计算弧度
    float radian = (float) Math.acos(cosDegree);
    // 计算旋转角度，顺时针为正，逆时针为负
    return isClockwise ? (float) Math.toDegrees(radian) : -(float) Math.toDegrees(radian);
}
```

#### 方法解析：

在 Android 的 `MotionEvent` 类中，除了 `event.getActionMasked()` 方法外，还有一个 `event.getAction()` 方法。它们之间的区别在于：

1. `getAction()` 方法：
   - `getAction()` 方法返回的是原始的动作代码，包括主要动作和附加信息。
   - 它返回的值可能是一个整数，需要通过位掩码（bitmask）进行解析以获取主要动作和附加信息。
   - 例如，`ACTION_POINTER_DOWN` 和 `ACTION_POINTER_UP` 是多点触控中的额外动作，可以通过位掩码解析获取触摸点的索引等信息。
2. `getActionMasked()` 方法：
   - `getActionMasked()` 方法返回的是主要的动作代码，忽略掉位掩码中的附加信息。
   - 它返回的值只包括主要的动作类型，例如 `ACTION_DOWN`, `ACTION_MOVE`, `ACTION_UP`, `ACTION_CANCEL` 等。

举个例子来说，假设一个 `MotionEvent` 对象 `event` 表示有两个手指同时按下，此时 `getAction()` 和 `getActionMasked()` 的返回值如下：

- `getAction()` 可能返回 `ACTION_POINTER_DOWN | 0x0100 | 1`，其中 `ACTION_POINTER_DOWN` 表示一个手指按下，`0x0100` 表示第二个触摸点的索引，`1` 是触摸点的序号。
- `getActionMasked()` 则只会返回 `ACTION_POINTER_DOWN`，表示有一个手指按下。

因此，通常情况下，如果你只需要处理主要的动作类型（如按下、移动、抬起、取消），可以使用 `getActionMasked()` 方法来获取简化后的动作类型；而如果需要处理复杂的多点触控信息，可以使用 `getAction()` 方法来获取完整的动作信息，然后使用位掩码解析具体的动作和触摸点信息。

#### MotionEvent 相关的两种坐标方法：

- `getX/Y()`: 获取触摸点距离自身 View 左/上边界的距离。因此如果控件跟随手指移动后，其 `getX/Y()` 方法获取的坐标值依旧是距离自身边界的距离，不会变。

- `getRawX/Y()`: 获取触摸点距离屏幕左/上边界的距离。如果控件跟随手指移动，那该方法获取的值会随之变化。
  
  #### 关于angle方法的详细解释：
  
  ```java
  private float angle(Point cen, Point first, Point second) {
    //点 o a b
    float dx1 = first.x - cen.x;//oa x
    float dy1 = first.y - cen.y;//oa y
    float dx2 = second.x - cen.x;//ob x
    float dy2 = second.y - cen.y;//ob y
  
    // 计算三条边的平方
    float ab2 = (float) ((second.x - first.x) * (second.x - first.x) + (second.y - first.y) * (second.y - first.y));
    float oa2 = dx1 * dx1 + dy1 * dy1;
    float ob2 = dx2 * dx2 + dy2 * dy2;
  
    // 根据两向量的叉乘判断顺逆时针和 <右手定则>
    /*
  
                       |i,     j,     k|
            oa x ob =  |oax,   oay,   0|   =  (0, 0, (oax * oby - oay * obx) * k)法向量
                       |obx,   oby,   0|
  
            其中(i,j,k) = (1,1,1)
  
            法向量k值大于零为逆时针，法向量小于0为顺时针
  
    */
    boolean isClockwise = 
        (first.x - cen.x) * (second.y - cen.y) - 
        (first.y - cen.y) * (second.x - cen.x) > 0;
  
    // 计算旋转角度的余弦值（余弦定理）
    /*
                      a2 + b2 - c2
           cos α = ------------------
                       2 · a · b
    */
    float cosDegree = (float) ((oa2 + ob2 - ab2) / (2 * Math.sqrt(oa2) * Math.sqrt(ob2)));
  
    // 处理余弦值超出范围的情况 <不懂为什么会超出范围，但加上对代码没啥影响>
    if (cosDegree > 1) {
        cosDegree = 1.0f;
    } else if (cosDegree < -1) {
        cosDegree = -1.0f;
    }
    // 计算弧度
    float radian = (float) Math.acos(cosDegree);
    // 计算旋转角度，顺时针为正，逆时针为负
    //（屏幕的y轴是向下的，所以旋转方向取反之后才能获取到在屏幕上实际的旋转方向）
    return isClockwise ? (float) Math.toDegrees(radian) : -(float) Math.toDegrees(radian);
  }
  ```
  
  ### 直尺工具最外层RulerLayout
  
  这个类是一个自定义容器类，众多核心功能都在这个类里面实现，代码很多，但是我认为有优化的空间，但是我懒得检查了，毕竟功能都实现了 ^v^，代码行数有点多，部分无难度内容就直接省略。
  
  #### 关于直尺的顶部画直线功能的设计思路：
  
  首先为识别区域添加触摸事件，在按下时计算出当前直尺顶部的直线表达式，`k1=tanα`，旋转中心的坐标代入求得`b`，与直尺垂直的斜率`k2 = -1 / k1`，然后根据按下的点的位置就可以求得出垂线的直线表达式，然后两个直线联立解的交点坐标，然后就可以求得按下点在直尺顶端的投影点了，滑动时获取的点也是通过同样的方法计算，然后华东还加了一个实时计算绘制距离，就直接将两个投影点的距离算出来。然后除以`5`（我自己设置的时是`5`格算作一个单位长度），再除以`8`（这是每一个刻度占的宽，这样就可以和直尺上的刻度达到对应的目的）。
  
  ```java
  public class RulerLayout extends RelativeLayout {
  
    //成员变量和构造方法/...
  
    @SuppressLint("ClickableViewAccessibility")
    private void init() {
  
        //画笔对象、路径对象等初始化...
  
        //设置背景色
        setBackgroundColor(Color.TRANSPARENT);
        //加载布局文件
        LayoutInflater.from(mContext).inflate(R.layout.ruler_view, this);
  
        //findViewById...
  
        //设置直尺容器在屏幕中的位置
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        //获取屏幕的像素高度和宽度
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        //TransferLayout的初始化位置的偏移量
        defaultX = widthPixels / 2f - dp2px(560f) / 2;
        defaultY = heightPixels / 2f - dp2px(50f) / 2;
        transfer.setTranslationX(defaultX);
        transfer.setTranslationY(defaultY);
        //我将按钮的触摸事件都放在了这个方法里面，免得堆在一堆太长了
        setViewTouchListeners();
    }
  
    //下面的方法会用到的一些变量...
  
    @SuppressLint("ClickableViewAccessibility")
    private void setViewTouchListeners() {
        //直尺移动，比较基础的一个触摸事件流程
        rulerView.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int rawX = (int) event.getRawX();
                    int rawY = (int) event.getRawY();
                    //在默认偏移量上面增减然后更新transfer的偏移量就可以达到跟手移动的效果
                    defaultX += (rawX - lastX);
                    defaultY += (rawY - lastY);
                    lastX = rawX;
                    lastY = rawY;
                    transfer.setTranslationX(defaultX);
                    transfer.setTranslationY(defaultY);
                    break;
                default:
                    break;
            }
            return true;
        });
  
        //直尺顶部画线（这个是自己写的，所以应该是有优化空间的）
        drawAreaView.setOnTouchListener((view, motionEvent) -> {
            //首先获取旋转中心，y加20的原因是因为transfer内部在最顶上有一个
            //高度为20dp的识别区域，dp2px方法是将dp转换成像素的方法，
            //屏幕的位置使用的是像素来衡量的
            cenX = transfer.getTranslationX();
            cenY = transfer.getTranslationY() + dp2px(20);
            //这个应该可以不用取余，直接用transfer.getRotation()进行后续的计算
            //不会导致结果出错，但是我觉得数小一点对后面的计算应该好那么一点点吧
            float alpha = transfer.getRotation() % 360;
            //斜率
            k1 = Math.tan(Math.toRadians(transfer.getRotation()));
            k2 = -1 / k1;
            //直线顶端所在直线的方程就是 y1 = k1 * x + b1
            double b1 = cenY - k1 * cenX;
            switch (motionEvent.getAction()) {
                //触摸事件
                case MotionEvent.ACTION_DOWN:
                    if (Double.isInfinite(k1)) {
                        //此时直尺垂直
                        sx = cenX;
                        sy = motionEvent.getRawY();
                        path.moveTo(sx, sy);
                        break;
                    } else if (Double.isInfinite(k2)) {
                        //此时直尺水平
                        sx = motionEvent.getRawX();
                        sy = cenY;
                        path.moveTo(sx, sy);
                        break;
                    }
                    //既不水平也不垂直，直接算交点
                    //计算按下点所在的垂线的偏移 b
                    //这里使用的都是再屏幕当中的坐标
                    float x = motionEvent.getRawX();
                    float y = motionEvent.getRawY();
                    double b2 = -k2 * x + y;
  
                    //计算垂线与直尺的焦点的 x 坐标，即垂直映射点的 x 值
                    double xj = (b1 - b2) / (k2 - k1);
                    //此次绘制直线的起点坐标
                    sx = (float) xj;
                    sy = (float) (k1 * xj + b1);
                    path.moveTo(sx, sy);
                    break;
                case MotionEvent.ACTION_MOVE:
                    //与上面的过程判断与处理一样，直接省略...
                    invalidate();//通知该页面的绘制对象进行重绘
                    //计算长度然后显示到textview里面
                    result.setText(text);
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        });
  
        //删除
        deleteV.setOnClickListener(view -> {
            //调用监听器，让上一层的父容器来把我当前页面的绘制信息放到底部的画板上面
            onDeleteListener.onDelete(path);
            //将自身从父容器中移除
            ((FrameLayout) RulerLayout.this.getParent()).removeView(RulerLayout.this);
        });
  
        //旋转
        rotateV.setOnTouchListener((view, motionEvent) -> {
            //偏移量获取
            float offsetX = rotateV.getLeft() - transfer.getScrollX();
            float offsetY = rotateV.getTop() - transfer.getScrollY();
            motionEvent.offsetLocation(offsetX, offsetY);
            //设置transfer的旋转中心
            transfer.setPivotX(getLeft());
            transfer.setPivotY(getTop() + dp2px(20));
            //调用transfer的旋转方法
            transfer.rotateLayout(motionEvent);
            return true;
        });
  
        //拖动拉长直尺
        addLenV.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) motionEvent.getRawX();
                    lastY = (int) motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int rawX = (int) motionEvent.getRawX();
                    int rawY = (int) motionEvent.getRawY();
                    int dx = rawX - lastX;
                    int dy = rawY - lastY;
                    double radians = Math.toRadians(transfer.getRotation());
                    //根据当前手指一动的距离来计算需要拉长的长度（wid为矢量）
                    int wid = (int) (dx * Math.cos(radians) + dy * Math.sin(radians));
                    //直接修改直尺容器的宽度，直尺是match_parent，会自动填充满的
                    LayoutParams layoutParams = (LayoutParams) transfer.getLayoutParams();
                    layoutParams.width = transfer.getMeasuredWidth() + wid;
                    layoutParams.height = transfer.getMeasuredHeight();
                    transfer.setLayoutParams(layoutParams);
                    //每次触发都更新至上一次触发点
                    lastX = rawX;
                    lastY = rawY;
                default:
                    break;
            }
            return true;
        });
    }
  
    //实现画板效果，当前view也是可绘制的，只是在被销毁时会将当前页面的path信息添加到最底部的画板
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, linePaint);
    }
  
    //这一部分也是实现画板的逻辑，比较基础的功能
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                path.moveTo(startX, startY);
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                path.lineTo(x, y);
                startX = x;
                startY = y;
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }
  
    //通过DisplayMetrics来将dp换算成像素，因为不同设备的像素密度不同
    public float dp2px(float dpValue) {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        float scale = metrics.density;
        return (dpValue * scale + 0.5f); // 加上0.5f是为了四舍五入
    }
  
    //监听器对象可以在父容器中通过setter注入来实现父容器中设定监听器方法的逻辑
    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }
  }
  ```
  
  #### 部分方法解析
1. `getTranslationX()` 方法
   `getTranslationX()` 方法用于获取视图在 X 轴方向的平移距离。这个平移距离是相对于视图自身原始位置的偏移量。如果视图没有显式设置过平移量，默认为 0。
- 返回值类型: `float`
- 返回值含义: 视图在 X 轴方向的平移距离。
2. `getScrollX()` 方法
   `getScrollX()` 方法用于获取视图内容在 X 轴方向的滚动偏移量。这个方法通常在包含可滚动内容的视图（例如 `ScrollView` 或 `HorizontalScrollView`）中使用，用于确定内容在水平方向上的滚动位置。所以源代码当中应该是可以不用加这个滚动距离的，因为整个直尺没有滚动的功能，不过加了也不影响。
- 返回值类型: `int`
- 返回值含义: 视图内容在 X 轴方向的滚动偏移量。

区别和用途：

- 视图类型不同：`getTranslationX()` 主要用于一般视图（View）的平移偏移量，而 `getScrollX()` 主要用于可滚动视图（如 `ScrollView`）的内容滚动偏移量。
- 返回值类型不同：`getTranslationX()` 返回 `float` 类型，用于精确的平移值；`getScrollX()` 返回 `int` 类型，表示内容的整数像素偏移量。
- 用途不同：`getTranslationX()` 通常用于动画或手动设置视图的平移效果，而 `getScrollX()` 则用于确定用户是否滚动了视图中的内容，以及在何处进行了滚动。
3. `offsetLocation()`
- `motionEvent.offsetLocation(offsetX, offsetY)` 是一个方法调用，它将当前触摸事件 `motionEvent` 的位置坐标偏移指定的 `offsetX` 和 `offsetY`。
- 调用 `MotionEvent.offsetLocation(offsetX, offsetY)` 方法后，`MotionEvent` 对象中所有的触摸点坐标都会按照指定的偏移量进行调整。这意味着，之后获取触摸点的坐标时，这些坐标将是经过偏移调整后的结果。
- 这意味着，通过这一行代码，你正在调整触摸事件的位置，以使其相对于视图 `transfer` 的可见区域正确显示，而不是相对于整个屏幕或父容器的坐标。
4. 关于`float offsetX = rotateV.getLeft() - transfer.getScrollX();`这行代码的解释
- 由于在绘制实际的直尺视图是偏移过后的，所以这个`rotateV.getLeft()`是直尺容器实际的边界位置，它和我们看到的现实的直尺位置是不一样的，我们是通过偏移和旋转来改变了直尺容器的最终显示位置，但是这个`getLeft()`所获取到的结果是固定不变的。
  
  ### DrawAreaView
  
  这个自定义view就是一个简单的画板，唯一需要注意的是需要在这个view当中提供一个可以访问的添加path的方法，可以将用户传递的path路径添加到当前view中
  
  ```java
  public void drawOnMe(Path path){
    this.path.addPath(path);
    invalidate();
  }
  ```
  
  ### MainActivity中部分逻辑
  
  `MainActivity`也没有什么特别难的点，就是在新建工具`view`的时候记得设置他的删除监听器对象，然后将将挺起对象传递的path路径传递给画板，其他的都是基础功能。这里由于其他三个工具还未开发，目前只开发了直尺工具，后续其他工具如果完成的话也会同步到仓库中。
  
  ```java
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
  ```

}

```

```
