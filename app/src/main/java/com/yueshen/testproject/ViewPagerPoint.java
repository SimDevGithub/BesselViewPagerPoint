package com.yueshen.testproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

/**
 * Created by Android on 17/4/14.
 */

public class ViewPagerPoint extends View {

    private Context context;
    private int ScreenWidth;//屏幕的宽度
    private int ViewWidth;//当前View的宽度
    private float scale;//屏幕宽度与当前View宽度的比例

    private int itemWidth;//每个点所占的宽度
    private int itemCenter;//每个点所占宽度的中心位置距离

    private Paint paint;//画笔
    private Path path;//绘制的路径

    private int selectColor = Color.parseColor("#8ad7ff");
    private int normalColor = Color.parseColor("#caedff");

    private ArrayList<CirclePoint> circlePoints;//定点圆点的集合
    private P p1, p2, p3, p4, p5;//坐标点实例
    private P animP;//动点圆点的坐标
    private CirclePoint animCirclePoint;//动点圆点的实例

    private float pax, pay, pbx, pby;//当前计算的两圆的圆心坐标
    private double circleDistance;//两圆心的距离

    private int currentIndex = 0;//当前计算的定圆的下标
    private int count = 4;//默认圆点个数
    private int circleRadius = 10;//默认圆点半径
    private int pointY = 30;//所有圆点的Y的坐标
    private int offSet = 10;//取消绘制贝塞尔曲线的偏移量

    public ViewPagerPoint(Context context) {
        super(context);
        init(context);
    }

    public ViewPagerPoint(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewPagerPoint(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        //初始化画笔
        initPaint();
        //初始化定圆坐标实例
        initP();
        //计算屏幕尺寸
        CalculationScreen();
        //动画路径
        path = new Path();
        //定圆的集合
        circlePoints = new ArrayList<>();
        //动画圆实例
        animCirclePoint = new CirclePoint();
        //动画圆的坐标实例
        animP = new P();
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setCircleRadius(int radius) {
        circleRadius = radius;
        pointY = radius * 2;//设置所有圆形的Y坐标为半径的两倍
    }

    public void setOffSet(int offSet) {
        this.offSet = offSet;
    }

    public void setSelectColor(int selectColor) {
        this.selectColor = selectColor;
    }

    public void setNormalColor(int normalColor) {
        this.normalColor = normalColor;
    }

    public void setTranslateX(int x) {
        if (animCirclePoint != null && animCirclePoint.getP() != null) {
            animCirclePoint.getP().X = x / scale + itemCenter + circleRadius;
        }
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (circlePoints == null || circlePoints.size() <= 0) return;
        //绘制每个圆
        for (int i = 0; i < circlePoints.size(); i++) {
            if (currentIndex == i) {
                circlePoints.get(i).onDraw(canvas, selectColor);
            } else {
                circlePoints.get(i).onDraw(canvas, normalColor);
            }
        }
        //绘制移动的圆
        animCirclePoint.onDraw(canvas, selectColor);
        //计算Path路径
        CalculationData();
        //绘制Path路径
        canvas.drawPath(path, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ViewWidth = getMeasuredWidth();
        initData();
        setMeasuredDimension(widthMeasureSpec, pointY * 2);  //设置当前View的宽高
    }


    //计算每个圆的信息
    private void initData() {
        if (count > 0) {
            itemWidth = ViewWidth / count;
            scale = (float) ScreenWidth / (float) itemWidth;
            itemCenter = itemWidth / 2;
            circlePoints.clear();
            for (int i = 0; i < count; i++) {
                CirclePoint circlePoint = new CirclePoint();
                P p = new P();
                p.X = itemCenter + itemWidth * i;
                p.Y = pointY;
                p.radius = circleRadius;
                circlePoint.setP(p);
                circlePoints.add(circlePoint);
            }
            animP.X = itemCenter + circleRadius;
            animP.Y = pointY;
            animP.radius = circleRadius;
            animCirclePoint.setP(animP);
        }
    }


    //计算贝塞尔曲线路径
    private void CalculationData() {

        pax = circlePoints.get(currentIndex).getP().X;
        pay = circlePoints.get(currentIndex).getP().Y;
        pbx = animCirclePoint.getP().X;
        pby = animCirclePoint.getP().Y;
        CalculationCirclePoint(pax, pay, pbx, pby);

        circleDistance = Math.abs(Math.sqrt(Math.pow(pax - pbx, 2) + Math.pow(pay - pby, 2)));

        path.reset();//重置路径
        if (circleDistance <= circleRadius * 2) {
            //绘制闭合方框
            CalculationFramePath();
        } else if (circleDistance > circleRadius * 2 && circleDistance < itemCenter - offSet) {
            //绘制贝塞尔曲线
            CalculationBezierPath();
        } else if (circleDistance > itemCenter - offSet && circleDistance < itemCenter + offSet) {
            //取消绘制贝塞尔曲线

        } else if (circleDistance >= itemCenter + offSet) {
            //切换下一个圆，以此圆为基础计算Path路径，然后绘制
            if (currentIndex < circlePoints.size() - 1) {
                if (pax > pbx) {//动圆位于当前圆的左侧
                    currentIndex = currentIndex - 1;
                } else {//动圆位于当前圆的右侧
                    currentIndex = currentIndex + 1;
                }
            }
        }
    }

    //计算两圆心连线且过两圆心的垂线与园的交点的坐标
    private void CalculationCirclePoint(float pax, float pay, float pbx, float pby) {
        double a = Math.atan((pbx - pax) / (pby - pay));
        double sin = Math.sin(a);
        double cos = Math.cos(a);
        p1.Y = (float) (pay + (sin * circleRadius));
        p1.X = (float) (pax - (cos * circleRadius));

        p2.X = (float) (pax + cos * circleRadius);
        p2.Y = (float) (pay - sin * circleRadius);

        p3.X = (float) (pbx - cos * circleRadius);
        p3.Y = (float) (pby + sin * circleRadius);

        p4.X = (float) (pbx + cos * circleRadius);
        p4.Y = (float) (pby - sin * circleRadius);

        p5.X = (pax + pbx) / 2;
        p5.Y = (pay + pby) / 2;
    }


    //计算方框Path
    private void CalculationFramePath() {
        path.moveTo(p1.X, p1.Y);
        path.lineTo(p3.X, p3.Y);
        path.lineTo(p4.X, p4.Y);
        path.lineTo(p2.X, p2.Y);
        path.close();
    }

    //计算贝塞尔曲线Path
    private void CalculationBezierPath() {
        path.moveTo(p1.X, p1.Y);
        path.quadTo(p5.X, p5.Y, p3.X, p3.Y);
        path.lineTo(p4.X, p4.Y);
        path.quadTo(p5.X, p5.Y, p2.X, p2.Y);
        path.lineTo(p1.X, p1.Y);

    }

    //初始化画笔
    private void initPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(selectColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(2);
    }

    //初始化点实例
    private void initP() {
        p1 = new P();//定圆上的点
        p2 = new P();//定圆上的点
        p3 = new P();//动圆上的点
        p4 = new P();//动圆上的点
        p5 = new P();//两圆圆心上的中心点
    }

    //计算屏幕的尺寸
    private void CalculationScreen() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        ScreenWidth = displayMetrics.widthPixels;
    }

}
