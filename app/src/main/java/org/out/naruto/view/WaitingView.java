package org.out.naruto.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import org.out.naruto.utils.MyPoint;
import org.out.naruto.waitingview.R;

/**
 * Created by Hao_S on 2016/6/1.
 */

public class WaitingView extends FrameLayout {

    private static final String TAG = "WaitingView";

    private Context context;

    private int viewHeight, viewWidth;

    private int viewColor = Color.RED; // ViewGroup里面的背景色，也是空心CircleView里面的颜色，默认红色。
    private int circleSize = 100; // CircleView的大小，默认100像素。
    private int spacing = 50; // CircleView之间的间隔，默认50像素。
    private int strokeColor = Color.WHITE; // CircleView的圆形边框颜色，默认白色。
    private int strokeSize = 5; // 边框的宽度
    private boolean autoStart = false; // 是否自动执行动画

    private int circleNum = 5; // CircleView的数量，默认5个。
    private CircleView[] circleViews; // 所有的CircleView
    private MyPoint[] myPoints; // 所有的坐标点
    private CircleView targetView; // 那个实心的CircleView

    public WaitingView(Context context) {
        this(context, null);
    }

    public WaitingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaitingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WaitingView, defStyleAttr, 0);
        // 第一个参数是属性集 第二个是我们自定义的属性集
        // 第三个是默认的Style，这里可以以Button为例，看Button两个参数构造方法的源码实现
        // 第四个是我们自定义属性的默认值，R.style....

        int num = a.getIndexCount(); // 获取到自定义属性的数量。

        for (int i = 0; i < num; i++) {
            int attr = a.getIndex(i); // 自定义属性的id
            // 用switch获取并设置，如果没有获取到（即在xml里面没有写）,则为默认值。
            switch (attr) {
                case R.styleable.WaitingView_viewColor:
                    this.viewColor = a.getColor(attr, viewColor);
                    break;
                case R.styleable.WaitingView_strokeColor:
                    this.strokeColor = a.getColor(attr, strokeColor);
                    break;
                case R.styleable.WaitingView_viewSpacing:
                    this.spacing = a.getInteger(attr, spacing);
                    break;
                case R.styleable.WaitingView_circleNum:
                    Log.i(TAG, "circleNum0 = " + circleNum);
                    this.circleNum = a.getInteger(attr, circleNum);
                    Log.i(TAG, "circleNum1 = " + circleNum);
                    break;
                case R.styleable.WaitingView_circleSize:
                    this.circleSize = a.getInt(attr, circleSize);
                    break;
                case R.styleable.WaitingView_AutoStart:
                    this.autoStart = a.getBoolean(attr, autoStart);
                    if (autoStart) {
                        Log.i(TAG, "autoStart is true");
                    }
                    break;
                case R.styleable.WaitingView_strokeSize:
                    int tempInt = a.getInteger(attr, strokeSize);
                    if (tempInt * 2 <= circleSize) {
                        strokeSize = tempInt;
                    }
                    break;
            }

        }

        a.recycle(); // 释放资源

        circleViews = new CircleView[circleNum];
        myPoints = new MyPoint[circleNum];

        setWillNotDraw(false); // 声明要调用onDraw方法。

    }

    private boolean first = true; // 用于标识只添加一次CircleView

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(viewColor); // 绘制背景色
        if (first) { // 是否第一次执行
            viewHeight = this.getHeight(); // 获取view的尺寸
            viewWidth = this.getWidth();
            creatCircle(); // 绘制小圆圈
            first = false;
            if (autoStart) { // 是否自动开始
                startAnim();
            }

        }
    }

    private void creatCircle() {
        int top = (viewHeight - circleSize) / 2; // view的上边界距父View上边界的距离，单位是px（下同）。ViewGroup的高与CircleView的高之差的一半。
        int left = (int) (viewWidth / 2 - ((circleNum / 2f) * circleSize + (circleNum - 1) / 2f * spacing));
        // int left = view左边界距父view左边界的距离，这里先算出了最左边view的数值，看着这么长，实在不想看。
        // 总之就是，ViewGroup的宽的一半，减去一半数量的CircleView的宽和一半数量的CircleView间距，能理解级理解，不能理解我也没办法了。
        int increats = circleSize + spacing; // left的增加量，每次增加一个CircleView的宽度和一个间距。

        for (int i = 0; i < circleNum; i++) {
            CircleView circleView = new CircleView(context, i != 0, viewColor, strokeColor, strokeSize); // new出来，除了第一个是实心圆，其他都是空心的。
            circleViews[i] = circleView; // 添加到数组中，动画执行的时候要用。
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(circleSize, circleSize); // 这里就是确定CircleView大小的地方。
            int realLeft = left + i * increats; // 实际的left值
            layoutParams.setMargins(realLeft, top, 0, 0); // 设置坐标
            MyPoint myPoint = new MyPoint(realLeft, top); // 把该坐标保存起来，动画执行的时候会用到。
            myPoints[i] = myPoint;
            circleView.setLayoutParams(layoutParams);
            addView(circleView); // 添加
        }

        this.targetView = circleViews[0]; // 那个白的实心圆

    }

    /**
     * 先说一下动画规律吧，实心白色圆不断依次和剩下的空心圆做半个双星运动。
     * 每次一轮运动结束后，最先在前面的空心圆到了最后，就像一个循环队列一样。
     * 但是这里我没有使用队列来实现，而是使用了数组，利用模除运算来计算出运动规律，这一点可能是这动画的短板，改进之后估计会解决自适应CircleView数量问题。
     * 2016/6/4 1:00 解决了动画自适应CircleView的数量问题，是我之前的写法有点死板。
     */

    private int position = 0; // CircleView动画执行次数
    private int duration = 500; // 一次动画的持续时间
    private AnimatorSet animatorSet; // AnimatorSet，使动画同时进行
    private ObjectAnimator targetAnim, otherAnim; // 两个位移属性动画

    public void startAnim() {

        animatorSet = new AnimatorSet();
        // 添加一个监听，一小段动画结束之后立即开启下一小段动画
        // 这里
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startAnim();
            }
        });

        int targetPosition = position % circleNum; // 这是实心白色CircleView所在次序，变化规律 0..(circleNum-1）

        int otherPosition = (position + 1) % circleNum; // 即将和实心白色CircleView作圆周运动的空心圆所在次序，变化规律 1..(circleNum-1）0

        int tempInt = (position + 1) % (circleNum - 1); // 这是除掉实心白色圆之后，剩下空心圆的次序，变化规律 1..(circleNum-1）

        CircleView circleView = circleViews[tempInt == 0 ? (circleNum - 1) : tempInt]; // 获取即将和实心白色圆作圆周运动的CircleView对象

        MyPoint targetPoint = myPoints[targetPosition]; // 实心白色圆实际的坐标点

        MyPoint otherPoint = myPoints[otherPosition]; // 将要执行动画的空心圆坐标点

        PointEvaluator targetPointEvaluator, otherPointEvaluator; // 坐标计算对象

        // 这里有三种情况，第一种就是实心圆运动到了最后，和第一个空心圆交换
        // 第二种就是实心圆在上面，空心圆在下面的交换动画
        // 第三种是实心圆在下面，空心圆在上面的交换动画，除了第一种之外，其他都是实心圆往右移动，空心圆往左移动。
        if (targetPosition == circleNum - 1) {
            targetPointEvaluator = new PointEvaluator(MoveType.Left, MoveType.Down);
            otherPointEvaluator = new PointEvaluator(MoveType.Right, MoveType.Up);
        } else if ((targetPosition % 2) == 0) {
            targetPointEvaluator = new PointEvaluator(MoveType.Right, MoveType.Up);
            otherPointEvaluator = new PointEvaluator(MoveType.Left, MoveType.Down);
        } else {
            targetPointEvaluator = new PointEvaluator(MoveType.Right, MoveType.Down);
            otherPointEvaluator = new PointEvaluator(MoveType.Left, MoveType.Up);
        }

        // 创建ObjectAnimator对象
        // 第一个参数就是要做运动的view
        // 第二个是要调用的方法，可以看看CircleView里面会有一个setPoint方法，这里会根据你填入的参数去寻找同名的set方法。
        // 第三个是自定义的数值计算器，会根据运动状态的程度计算相应的结果
        // 第四个和第五个参数是运动初始坐标和运动结束坐标。
        targetAnim = ObjectAnimator.ofObject(this.targetView, "Point", targetPointEvaluator, targetPoint, otherPoint);

        otherAnim = ObjectAnimator.ofObject(circleView, "Point", otherPointEvaluator, targetPoint, otherPoint);

        animatorSet.playTogether(targetAnim, otherAnim); // 动画同时运行
        animatorSet.setDuration(duration); // 设置持续时间
        animatorSet.start(); // 执行动画
        position++;
    }



    public void CancleAnim() { // 取消动画
        if (animatorSet != null && animatorSet.isRunning())
            animatorSet.cancel();
    }

    public boolean isRunning() { // 判断动画是否在运行
        if (animatorSet == null)
            return false;
        return animatorSet.isRunning();
    }


    /**
     * 枚举型标识动画运动类型
     */
    public enum MoveType {
        Left, Right, Up, Down
    }

    /**
     * 运动算法：
     * 根据做双星运动的两个CircleView的坐标，首先求出两坐标的中心点作为运动圆心。
     * 根据运动的角度，结合cos与sin分别算出x轴与y轴的数值变化，然后返回当前运动坐标。
     * x = (运动中心x坐标 ± Cos（运动角度）X 运动半径);
     * y = (运动中心y坐标 ± Sin（运动角度）X 运动半径);
     */
    private class PointEvaluator implements TypeEvaluator {
        private MoveType LeftOrRight, UpOrDown;

        public PointEvaluator(MoveType LeftOrRight, MoveType UpOrDown) {
            this.LeftOrRight = LeftOrRight;
            this.UpOrDown = UpOrDown;
        }

        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {

            MyPoint startPoint = (MyPoint) startValue; // 运动开始时的坐标
            MyPoint endPoint = (MyPoint) endValue; // 运动结束时的坐标
            int R = (int) (Math.abs(startPoint.getX() - endPoint.getX()) / 2); // 运动圆周的半径
            double r = Math.PI * fraction; // 当前运动角度
            int circleX = (int) ((startPoint.getX() + endPoint.getX()) / 2); // 运动圆心坐标X
            int circleY = (int) endPoint.getY();// 运动圆心坐标Y
            float x = 0, y = 0; // 当前运动坐标

            switch (LeftOrRight) {
                case Left:
                    x = (float) (circleX + Math.cos(r) * R);
                    break;
                case Right:
                    x = (float) (circleX - Math.cos(r) * R);
                    break;
            }

            switch (UpOrDown) {
                case Up:
                    y = (float) (circleY - Math.sin(r) * R);
                    break;
                case Down:
                    y = (float) (circleY + Math.sin(r) * R);
                    break;
            }

            MyPoint myPoint = new MyPoint(x, y);

            return myPoint;
        }
    }

}
