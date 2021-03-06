package com.github.nukc.stateview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

/**
 * Created by C on 4/3/2016.
 * https://github.com/nukc
 */
public class StateView extends View {

    private int mEmptyResource;
    private int mRetryResource;
    private int mLoadingResource;

    private View mEmptyView;
    private View mRetryView;
    private View mLoadingView;

    private LayoutInflater mInflater;
    private OnRetryClickListener mRetryClickListener;

    private RelativeLayout.LayoutParams mLayoutParams;

    /**
     * 注入到activity中
     *
     * @param activity Activity
     * @return StateView
     */
    public static StateView inject(@NonNull Activity activity) {
        return inject(activity, false);
    }

    /**
     * 注入到activity中
     *
     * @param activity Activity
     * @param hasActionBar 是否有actionbar/toolbar,
     *                     true: 会setMargin top, margin大小是状态栏高度 + 工具栏高度
     *                     false: not set
     * @return StateView
     */
    public static StateView inject(@NonNull Activity activity, boolean hasActionBar) {
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
        return inject(rootView, hasActionBar, true);
    }

    /**
     * 注入到ViewGroup中
     *
     * @param parent extends ViewGroup
     * @return StateView
     */
    public static StateView inject(@NonNull ViewGroup parent) {
        return inject(parent, false);
    }

    /**
     * 注入到ViewGroup中
     *
     * @param parent extends ViewGroup
     * @param hasActionBar 是否有actionbar/toolbar,
     *                     true: 会setMargin top, margin大小是actionbarSize
     *                     false: not set
     * @return StateView
     */
    public static StateView inject(@NonNull ViewGroup parent, boolean hasActionBar) {
        return inject(parent, hasActionBar, false);
    }

    /**
     * 注入到ViewGroup中
     *
     * @param parent extends ViewGroup
     * @param hasActionBar 是否有actionbar/toolbar
     * @param isActivity 是否注入到Activity
     * @return StateView
     */
    private static StateView inject(@NonNull ViewGroup parent, boolean hasActionBar, boolean isActivity) {
        StateView stateView = new StateView(parent.getContext());
        parent.addView(stateView);
        if (hasActionBar) {
            stateView.setTopMargin(isActivity);
        }
        return stateView;
    }

    /**
     * 注入到View中
     *
     * @param view instanceof ViewGroup
     * @return StateView
     */
    public static StateView inject(@NonNull View view) {
        if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            return inject(parent);
        } else {
            throw new ClassCastException("view must be ViewGroup");
        }
    }

    /**
     * 注入到View中
     *
     * @param view instanceof ViewGroup
     * @param hasActionBar 是否有actionbar/toolbar
     * @return StateView
     */
    public static StateView inject(@NonNull View view, boolean hasActionBar) {
        if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            return inject(parent, hasActionBar);
        } else {
            throw new ClassCastException("view must be ViewGroup");
        }
    }

    public StateView(Context context) {
        this(context, null);
    }

    public StateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StateView);
        mEmptyResource = a.getResourceId(R.styleable.StateView_emptyResource, 0);
        mRetryResource = a.getResourceId(R.styleable.StateView_retryResource, 0);
        mLoadingResource = a.getResourceId(R.styleable.StateView_loadingResource, 0);
        a.recycle();

        if (mEmptyResource == 0){
            mEmptyResource = R.layout.base_empty;
        }
        if (mRetryResource == 0){
            mRetryResource = R.layout.base_retry;
        }
        if (mLoadingResource == 0) {
            mLoadingResource = R.layout.base_loading;
        }

        if (attrs == null) {
            mLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            mLayoutParams = new RelativeLayout.LayoutParams(context, attrs);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(0, 0);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void draw(Canvas canvas) {
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
    }

    @Override
    public void setVisibility(int visibility) {
        setVisibility(mEmptyView, visibility);
        setVisibility(mRetryView, visibility);
        setVisibility(mLoadingView, visibility);
    }

    private void setVisibility(View view, int visibility){
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    public void showContent(){
       setVisibility(GONE);
    }

    public View showEmpty(){
        if (mEmptyView == null) {
            mEmptyView = inflate(mEmptyResource);
        }

        showView(mEmptyView);
        return mEmptyView;
    }

    public View showRetry(){
        if (mRetryView == null) {
            mRetryView = inflate(mRetryResource);
            mRetryView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRetryClickListener != null){
                        showLoading();
                        mRetryView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mRetryClickListener.onRetryClick();
                            }
                        }, 200);
                    }
                }
            });
        }

        showView(mRetryView);
        return mRetryView;
    }

    public View showLoading(){
        if (mLoadingView == null) {
            mLoadingView = inflate(mLoadingResource);
        }

        showView(mLoadingView);
        return mLoadingView;
    }

    private void showView(View view){
        setVisibility(view, VISIBLE);
        if (mEmptyView == view){
            setVisibility(mLoadingView, GONE);
            setVisibility(mRetryView, GONE);
        }else if (mLoadingView == view){
            setVisibility(mEmptyView, GONE);
            setVisibility(mRetryView, GONE);
        }else {
            setVisibility(mEmptyView, GONE);
            setVisibility(mLoadingView, GONE);
        }
    }

    public View inflate(@LayoutRes int layoutResource) {
        final ViewParent viewParent = getParent();

        if (viewParent != null && viewParent instanceof ViewGroup) {
            if (layoutResource != 0) {
                final ViewGroup parent = (ViewGroup) viewParent;
                final LayoutInflater factory;
                if (mInflater != null) {
                    factory = mInflater;
                } else {
                    factory = LayoutInflater.from(getContext());
                }
                final View view = factory.inflate(layoutResource, parent, false);

                final int index = parent.indexOfChild(this);
                //防止还能触摸底下的View
                view.setClickable(true);

                final ViewGroup.LayoutParams layoutParams = getLayoutParams();
                if (layoutParams != null) {
                    if (parent instanceof RelativeLayout) {
                        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) layoutParams;
                        mLayoutParams.setMargins(lp.leftMargin, lp.topMargin,
                                lp.rightMargin, lp.bottomMargin);

                        parent.addView(view, index, mLayoutParams);
                    }else {
                        parent.addView(view, index, layoutParams);
                    }
                } else {
                    parent.addView(view, index);
                }

                if (mLoadingView != null && mRetryView != null && mEmptyView != null){
                    parent.removeViewInLayout(this);
                }

                return view;
            } else {
                throw new IllegalArgumentException("StateView must have a valid layoutResource");
            }
        } else {
            throw new IllegalStateException("StateView must have a non-null ViewGroup viewParent");
        }
    }

    /**
     * 设置topMargin, 当有actionbar/toolbar的时候
     * @param isActivity if true: 注入到Activity, 需要加上状态栏的高度
     */
    public void setTopMargin(boolean isActivity){
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
        layoutParams.topMargin = isActivity ?
                getStatusBarHeight() + getActionBarHeight() : getActionBarHeight();
    }

    /**
     * @return 状态栏的高度
     */
    private int getStatusBarHeight() {
        int height = 0;
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            height = getResources().getDimensionPixelSize(resId);
        }
        return height;
    }

    /**
     * @return actionBarSize
     */
    private int getActionBarHeight() {
        int height = 0;
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return height;
    }

    /**
     * 设置emptyView的自定义Layout
     * @param emptyResource emptyView的layoutResource
     */
    public void setEmptyResource(@LayoutRes int emptyResource) {
        this.mEmptyResource = emptyResource;
    }

    /**
     * 设置retryView的自定义Layout
     * @param retryResource retryView的layoutResource
     */
    public void setRetryResource(@LayoutRes int retryResource) {
        this.mRetryResource = retryResource;
    }

    /**
     * 设置loadingView的自定义Layout
     * @param loadingResource loadingView的layoutResource
     */
    public void setLoadingResource(@LayoutRes int loadingResource) {
        mLoadingResource = loadingResource;
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    public void setInflater(LayoutInflater inflater) {
        this.mInflater = inflater;
    }

    /**
     * 监听重试
     * @param listener
     */
    public void setOnRetryClickListener(OnRetryClickListener listener){
        this.mRetryClickListener = listener;
    }

    public interface OnRetryClickListener{
        void onRetryClick();
    }
}
