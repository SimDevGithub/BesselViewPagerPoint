package com.yueshen.testproject;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Android on 17/4/17.
 */

public class CirclePoint {

    private P p;
    private Paint paint;

    public CirclePoint() {
        initPaint();
    }

    public void setP(P p) {
        this.p = p;
    }

    public P getP() {
        return p;
    }


    public void onDraw(Canvas canvas, int color) {
        if (p == null) return;
        paint.setColor(color);
        canvas.drawCircle(p.X, p.Y, p.radius, paint);
    }

    //初始化画笔
    private void initPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(2);
    }

}
