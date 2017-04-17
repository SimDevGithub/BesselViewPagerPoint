package com.yueshen.testproject;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Android on 17/4/17.
 */

public class CirclePoint {

    private P p;
    private Paint paint;

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public void setP(P p) {
        this.p = p;
    }

    public P getP() {
        return p;
    }

    public void onDraw(Canvas canvas) {
        if (p == null) return;
        if (paint == null) return;
        canvas.drawCircle(p.X, p.Y, p.radius, paint);
    }

    public void onDraw(Canvas canvas, int color) {
        if (p == null) return;
        if (paint == null) return;
        paint.setColor(color);
        canvas.drawCircle(p.X, p.Y, p.radius, paint);
    }

}
