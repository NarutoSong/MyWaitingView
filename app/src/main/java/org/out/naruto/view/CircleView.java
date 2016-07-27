package org.out.naruto.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import org.out.naruto.utils.MyPoint;

/**
 * Created by Hao_S on 2016/6/1.
 */

public class CircleView extends View {

    private static final String TAG = "CircleView";

    private boolean isHollow = true; // 是否是空心圆
    private int circleColor; // 颜色
    private int strokeColor; // 边框颜色
    private int mSize = 0; // view大小
    private int strokeSize; // 边框宽度，单位 px


    public CircleView(Context context) {
        super(context);
    }

    public CircleView(Context context, Boolean isHollow, int circleColor, int strokeColor, int strokeSize) {
        super(context);
        this.isHollow = isHollow;
        this.circleColor = circleColor;
        this.strokeColor = strokeColor;
        this.strokeSize = strokeSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mSize = this.getHeight();
        Paint paint = new Paint(); // 画笔
        paint.setAntiAlias(true); // 抗锯齿
        paint.setColor(strokeColor);
        canvas.drawCircle(mSize / 2, mSize / 2, mSize / 2, paint); // 四个参数，分别是x坐标 y坐标 半径？？ 画笔
        if (isHollow) { // 如果是空心的，在里面再绘制一个圆
            paint.setColor(this.circleColor);
            canvas.drawCircle(mSize / 2, mSize / 2, (mSize - mSize / (strokeSize * 2)) / 2, paint);
        }
    }

    /**
     * @param myPoint 包含xy坐标的对象
     *                这就是具体让小圆圈动起来的函数
     *                view.animate()函数是Android 3.1 提供的，返回的是ViewPropertyAnimator，简单来说就是对animator的封装。
     */

    public void setPoint(MyPoint myPoint) {
        this.animate().y(myPoint.getY()).x(myPoint.getX()).setDuration(0);
    }

}
