package com.cyt.mylistview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

/**
 * Created by Tao on 2016/4/16.
 */
public class RefreshUpdateListView extends ListView implements AbsListView.OnScrollListener {
    private static final int DONE = 0; // 刷新完毕状态
    private static final int PULL_TO_REFRESH = 1; // 下拉刷新状态
    private static final int OK_TO_REFRESH = 2; // 可以刷新的状态
    private static final int REFRESHING = 3; // 正在刷新状态
    private static final int PULL_TO_UPDATE = 4; // 上拉更新状态
    private static final int OK_TO_UPDATE = 5; // 可以更新的状态
    private static final int UPDATING = 6; // 正在更新状态
    private static final int RATIO = 3; //用户手指的拖动距离与实际控件的滑动距离的比例
    private int state; //保存当前的状态量
    private RelativeLayout headerView; //头部布局
    private RelativeLayout footerView; //尾部布局
    private ProgressWheel progressWheel; //头部旋转轮子
    private ProgressBar progressBar; //尾部进度条
    private int headerViewHeight; //头部高度
    private boolean isRefreable; //判断是否可以下拉刷新
    private boolean isUpdatable; //判断是否可以上拉更新
    private boolean isRecord; //判断是否已经记录了初始Y坐标
    private float startY; //初始Y坐标
    private float offsetY; //Y方向的偏移量
    private OnRefreshUpdateListener listener; //回调接口

    public interface OnRefreshUpdateListener {
        void onRefresh();

        void onUpdate();
    }
    
    public void setOnRefreshUpdateListener(OnRefreshUpdateListener onRefreshUpdateListener) {
        listener = onRefreshUpdateListener;
    }

    /**
     * 刷新完毕之后，主线程调用该方法隐藏头部布局
     */
    public void setRefreshComplete() {
        //将状态量设置为已完成
        state = DONE;
        //隐藏头部布局
        headerView.setPadding(0, -headerViewHeight, 0, 0);
    }

    /**
     * 更新完毕之后，主线程调用该方法隐藏尾部布局
     */
    public void setUpdateComplete() {
        //将状态量设置为已完成
        state = DONE;
        //隐藏尾部布局
        footerView.setVisibility(GONE);
        //停止进度条动画
        progressBar.stopAnimation();
    }

    public RefreshUpdateListView(Context context) {
        super(context);
        initView(context);
        return;
    }

    public RefreshUpdateListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        return;
    }

    public RefreshUpdateListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        return;
    }

    public void initView(Context context) {
        //关闭View的OverScroll
        setOverScrollMode(OVER_SCROLL_NEVER);
        //设置滑动监听
        setOnScrollListener(this);
        //加载头尾布局
        headerView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.layout_header,this,false);
        footerView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.layout_footer, this,false);
        progressWheel = (ProgressWheel) headerView.findViewById(R.id.progressWheel);
        progressBar = (ProgressBar) footerView.findViewById(R.id.progressBar);
        //测量头布局的高度
        measureView(headerView);
        headerViewHeight = headerView.getMeasuredHeight();
        //添加到ListView
        addHeaderView(headerView);
        addFooterView(footerView);
        //隐藏headerView到第一项里面
        headerView.setPadding(0, -headerViewHeight, 0, 0);
        //直接隐藏footerView
        footerView.setVisibility(View.GONE);
        //初始化默认状态量
        state = DONE;
        isRefreable = false;
        isRecord = false;
        isUpdatable = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //如果当前状态是正在刷新或正在更新，则返回
        if (state == REFRESHING || state == UPDATING) {
            return super.onTouchEvent(ev);
        }
        switch (ev.getAction()) {
            //当用户按下手指
            case MotionEvent.ACTION_DOWN:
                //如果没有记录过初始Y坐标，则记录
                if (!isRecord) {
                    isRecord = true;
                    startY = ev.getY();
                }
                break;
            //当用户拖动手指
            case MotionEvent.ACTION_MOVE:
                //再次判断是否记录了初始Y坐标，若没记录则记录
                if (!isRecord) {
                    isRecord = true;
                    startY = ev.getY();
                }
                //计算出y的偏移量
                offsetY = ev.getY() - startY;
                //如果处于已完成状态
                if (state == DONE && isRecord) {
                    if (offsetY < 0) {
                        if (isUpdatable) {
                            // 将状态改为上拉更新状态
                            state = PULL_TO_UPDATE;
                            //显示尾部
                            footerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (isRefreable) {
                            //将状态改为下拉更新状态
                            state = PULL_TO_REFRESH;
                        }
                    }
                }
                //如果可以更新，并且处在上拉状态或可以更新状态中
                if (state == PULL_TO_UPDATE || state == OK_TO_UPDATE && isUpdatable && isRecord) {
                    //如果偏移量＜0 （即手指往上拖动）
                    if (offsetY < 0) {
                        //根据偏移量计算出进度条的进度
                        int progress = Math.abs((int) (offsetY / RATIO));
                        //如果进度条的值超过了120（100为刚好合并的状态）,就将状态量设置为
                        if (progress > 120) {
                            progress = 120;
                            state = OK_TO_UPDATE;
                        } else {
                            state = PULL_TO_UPDATE;
                        }
                        //根据进度值绘制进度条（让进度条跟随手指的拖动变化）
                        progressBar.setProgress(progress);
                    }
                }
                //如果可以刷新，并且处于下拉状态或者可以刷新状态中
                if (state == PULL_TO_REFRESH || state == OK_TO_REFRESH && isRefreable && isRecord) {
                    setSelection(0);
                    //如果当前滑动的距离小于headerView的高度
                    if (offsetY / RATIO < headerViewHeight) {
                        state = PULL_TO_REFRESH;
                    } else {
                        state = OK_TO_REFRESH;
                    }
                    headerView.setPadding(0, (int) (-headerViewHeight + offsetY
                            / RATIO), 0, 0);
                }
                break;
            // 当用户手指抬起时
            case MotionEvent.ACTION_UP:
                // 如果当前状态为下拉未到刷新位置状态，则不刷新
                if (state == PULL_TO_REFRESH) {
                    //将状态设置为已完成
                    state = DONE;
                    // 隐藏headerView
                    headerView.setPadding(0, -headerViewHeight, 0, 0);
                }
                // 如果当前状态为可以刷新状态
                if (state == OK_TO_REFRESH) {
                    //将状态设置为正在刷新
                    state = REFRESHING;
                    //将headerView放到刚好刷新的位置
                    headerView.setPadding(0,0,0,0);
                }
                // 如果当前状态为上拉未到刷新位置状态，则不更新
                if (state == PULL_TO_UPDATE) {
                    //将状态设置为已完成
                    state = DONE;
                    // 隐藏footerView
                    footerView.setVisibility(View.GONE);
                    //重置progressBar
                    progressBar.setProgress(0);
                    progressBar.stopAnimation();
                }
                //如果当前状态为可以更新状态
                if (state == OK_TO_UPDATE) {
                    //将状态设置为正在刷新
                    state = UPDATING;
                    //开启进度条动画
                    progressBar.startAnimation();
                }
                //将记录y坐标的isRecord改为false，以便于下一次手势的执行
                isRecord = false;
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    /**
     * 判断是否ListView已经到顶部或者是否已经到了底部
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        View lastVisibleItemView = this.getChildAt(this.getChildCount() - 1);
        View firstVisibleItemView = this.getChildAt(0);
        if (firstVisibleItem == 0 && firstVisibleItemView != null // 判断是否已经到顶
                && firstVisibleItemView.getTop() == 0) {
            isRefreable = true;
        } else if ((firstVisibleItem + visibleItemCount) == totalItemCount
                && lastVisibleItemView != null) {
            isUpdatable = true;
        } else {
            isRefreable = false;
            isUpdatable = false;
        }
    }


    /**
     * 测量View
     *
     * @param child
     */
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
                    MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

}
