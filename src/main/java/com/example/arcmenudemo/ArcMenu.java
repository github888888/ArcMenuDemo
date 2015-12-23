package com.example.arcmenudemo;

import java.util.ArrayList;
import java.util.List;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.VoiceInteractor.PickOptionRequest.Option;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.ViewAnimator;

public class ArcMenu extends ViewGroup {
    @SuppressWarnings("unused")
    private static final String TAG = "ArcMenu";
    private Context context;
    private int mWidth;
    private int mHeight;
    private int screenSize;
    private Scroller scroller;

    private ViewInfo viewinfo;
    private ViewInfo menuinfo;


    private boolean isOpen = false;
    private List<ViewInfo> list;
    private ItemSelectListener listener;
    private int[] resids;

    private double avg;
    private double startAngle;
    int radius;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (ViewInfo item : list) {
            item.view.layout(menuinfo.x - item.intrinsicWidth / 2,
                    menuinfo.y - item.intrinsicHeight / 2,
                    menuinfo.x + item.intrinsicWidth / 2,
                    menuinfo.y + item.intrinsicHeight / 2);
            item.view.setRotation(item.curAngle);
            item.view.setTranslationX(item.disX);
            item.view.setTranslationY(item.disY);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    public ArcMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    private void init() {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        screenSize = metrics.widthPixels <= metrics.heightPixels ? metrics.widthPixels
                : metrics.heightPixels;

        scroller = new Scroller(context, new BounceInterpolator());
        list = new ArrayList<ViewInfo>();
    }

    public ArcMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcMenu(Context context) {
        this(context, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthsize = MeasureSpec.getSize(widthMeasureSpec);
        int widthmode = MeasureSpec.getMode(widthMeasureSpec);
        int heightsize = MeasureSpec.getSize(heightMeasureSpec);
        int heightmode = MeasureSpec.getMode(heightMeasureSpec);

        if (MeasureSpec.EXACTLY == widthmode) {
            mWidth = widthsize;
        } else {
            mWidth = screenSize * 2 / 3;
            if (MeasureSpec.AT_MOST == widthmode) {
                mWidth = Math.min(widthsize, mWidth);
            }
        }

        if (MeasureSpec.EXACTLY == heightmode) {
            mHeight = heightsize;
        } else {
            mHeight = screenSize * 2 / 3;
            if (MeasureSpec.AT_MOST == heightmode) {
                mHeight = Math.min(heightsize, mHeight);
            }
        }

        setMeasuredDimension(mWidth, mHeight);
        prepareData();
    }

    /**
     * 准备菜单数据
     */
    private void prepareData() {
        if (null == menuinfo) { // 菜单
            menuinfo = createViewInfo(R.drawable.main);
            menuinfo.startdegree = 0;
            menuinfo.enddegree = 90;
            menuinfo.x = getResources().getDimensionPixelSize(R.dimen.padding) + menuinfo.intrinsicWidth / 2;
            menuinfo.y = mHeight - getResources().getDimensionPixelSize(R.dimen.padding) -  menuinfo.intrinsicWidth / 2;
            menuinfo.view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    isOpen ^= true;
                    scroller.startScroll(0, 0, 1000, 0, 1000);
                    postInvalidate();
                }
            });
        }
        if (null != resids && resids.length > 0) {
            // 避免多次添加
            if (list.size() == 1 + resids.length) return;

            // 如果卫星个数小于4，那么就将90度平均分
            if (resids.length < 4) {
                avg = Math.PI / 2 / resids.length;
                startAngle = -avg / 2;
            } else { // 如果卫星个数大于等于4，那么需要将90度分成 （个数 - 1）份
                     // 第一颗卫星和最后一颗卫星对应的x坐标和y坐标与menu的坐标相同
                avg = Math.PI / 2  / (resids.length - 1);
                startAngle = -avg;
            }

            // 求出卫星的半径，确定卫星中心与菜单中心的距离
            Options op = new Options();
            op.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), resids[0], op);
            // 考虑padding值
            radius = mWidth - getResources().getDimensionPixelSize(R.dimen.padding) * 2 - menuinfo.intrinsicWidth / 2 - op.outWidth / 2;
            for (int i = 0; i < resids.length; i++) {
                viewinfo = createViewInfo(resids[i]);

                startAngle += avg;
                viewinfo.startdegree = 90;
                viewinfo.enddegree = 0;
                viewinfo.x = (int) (menuinfo.x + radius * Math.sin(startAngle));
                viewinfo.y = (int) (menuinfo.y - radius * Math.cos(startAngle));
                viewinfo.view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int resid = (int) v.getTag();
                        if (null != ArcMenu.this.listener) {
                            ArcMenu.this.listener.onItemSelect(resid);
                        }
                        isOpen = false;
                        scroller.startScroll(0, 0, 1000, 0, 1000);
                        postInvalidate();
                    }
                });
                list.add(viewinfo);
                addView(viewinfo.view);
            }
            addView(menuinfo.view);
            list.add(menuinfo);
        }
    }

    /**
     * 资源id数组，资源id选中监听
     *
     * @param resids   资源数组
     * @param listener 选择监听
     */
    public void addViewAndListener(int[] resids, ItemSelectListener listener) {
        if (resids == null || resids.length == 0) return;
        this.resids = resids;
        this.listener = listener;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            for (ViewInfo item : list) { // 打开状态从0增长，关闭状态从1减小
                item.setValueByFraction(isOpen ? scroller.getCurrX() * 1f / 1000 : 1 - scroller.getCurrX() * 1f / 1000);
            }
            // 重新布局
            requestLayout();
            // 重新刷新
            postInvalidate();
        }
    }

    private ViewInfo createViewInfo(int resid) {
        Options op = new Options();
        op.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resid, op);

        LayoutParams lp = new LayoutParams(op.outWidth, op.outHeight);
        ImageView iv = new ImageView(context);
        iv.setImageResource(resid);
        iv.setLayoutParams(lp);
        ViewInfo info = new ViewInfo();
        info.tag = R.drawable.main;
        info.intrinsicWidth = op.outWidth;
        info.intrinsicHeight = op.outHeight;
        info.view = iv;
        info.view.setTag(resid);
        return info;
    }

    private class ViewInfo {
        public int tag; // 提供卫星控件的标示，回调使用
        public int x; // 控件展开状态下，显示的x位置
        public int y; // 控件展开状态下，显示的y位置
        public int startdegree; // 控件开始旋转角度
        public int enddegree; // 控件结束旋转角度
        public int disX; // 控件需要移动的x距离
        public int disY; // 控件需要移动的y距离
        public int curAngle; // 控件需要移动的角度
        public int intrinsicWidth; // 控件内部图片内在宽度
        public int intrinsicHeight; // 控件内部图片内在高度
        public View view; // 具体的控件

        public void setValueByFraction(float fraction) {
            disX = (int) ((x - menuinfo.x) * fraction);
            disY = (int) ((y - menuinfo.y) * fraction);
            curAngle = (int) (startdegree + (enddegree - startdegree) * fraction);
        }
    }

    public interface ItemSelectListener {
        void onItemSelect(int resID);
    }
}
