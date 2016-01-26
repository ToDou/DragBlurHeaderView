package com.loopeer.android.librarys.dragblurheader;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

public class DragBlurHeaderView extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {

    private View mHeaderView;
    private View mContent;
    private PullIndicator mPullIndicator;
    private ScrollChecker mScrollChecker;
    private boolean mDisableWhenHorizontalMove = false;
    private boolean mPreventForHorizontal = false;
    private int mPagingTouchSlop;
    private MotionEvent mLastMoveEvent;
    private boolean mHasSendCancelEvent = false;
    private int mDurationToGoBack = 300;
    private View mScrollTarget;

    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private ScrollerCompat mScroller;

    public DragBlurHeaderView(Context context) {
        this(context, null);
    }

    public DragBlurHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragBlurHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPullIndicator = new PullIndicator();
        mScrollChecker = new ScrollChecker();

        final ViewConfiguration conf = ViewConfiguration.get(getContext());
        mPagingTouchSlop = conf.getScaledTouchSlop() * 2;
        init();
    }

    private void init() {
        mScroller = ScrollerCompat.create(getContext());
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    protected void onFinishInflate() {
        final int childCount = getChildCount();
        if (childCount > 2) {
            throw new IllegalStateException("PullSwitchView only can host 2 elements");
        } else {
            createViews();
        }
        if (mHeaderView != null) {
            mHeaderView.bringToFront();
        }

        super.onFinishInflate();
    }

    private void createViews() {
        final int childCount = getChildCount();
        if (childCount == 1) {
            mContent = getChildAt(0);
        }

        if (childCount == 2) {
            mHeaderView = getChildAt(0);
            mContent = getChildAt(1);
        }

        if (mHeaderView == null) {
            createDefaultHeader();
        }
    }

    private void createDefaultHeader() {
        setHeaderView(new DefaultHeader(getContext()));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mHeaderView != null) {
            measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            int headerHeight = mHeaderView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin
                    + (mPullIndicator.getCurrentPosY() > 0 ? mPullIndicator.getCurrentPosY() : 0);
            if (mPullIndicator.getCurrentPosY() == 0) {
                mPullIndicator.setHeaderHeight(headerHeight);
            }
        }

        if (mContent != null) {
            measureContentView(mContent, widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void measureContentView(View content, int widthMeasureSpec, int heightMeasureSpec) {
        final MarginLayoutParams lp = (MarginLayoutParams) content.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
        final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                getPaddingTop() + getPaddingBottom() + lp.topMargin, lp.height);
        content.measure(childWidthMeasureSpec, childHeightMeasureSpec);

        int contentHeight = mContent.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
        mPullIndicator.setContentHeight(contentHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildren();
    }

    private void layoutChildren() {
        int offsetY = mPullIndicator.getCurrentPosY();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        if (mHeaderView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin + offsetY > 0 ? 0 : offsetY;
            final int right = left + mHeaderView.getMeasuredWidth();
            final int bottom = top + mHeaderView.getMeasuredHeight();
            mHeaderView.layout(left, top, right, bottom);
        }

        if (mContent != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mContent.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin + offsetY + mPullIndicator.getHeaderHeight();
            final int right = left + mContent.getMeasuredWidth();
            final int bottom = top + mContent.getMeasuredHeight();
            mContent.layout(left, top, right, bottom);
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {

        VelocityTracker mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(e);

        if (!isEnabled() || mContent == null || mHeaderView == null) {
            return dispatchTouchEventSupper(e);
        }
        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                // 初始化速率的单位
                mVelocityTracker.computeCurrentVelocity(1000, 0.01f);
                int velocityY = (int) mVelocityTracker.getYVelocity();
                mScrollChecker.fling(0, velocityY);
                // 回收
                mVelocityTracker.recycle();


                mPullIndicator.onRelease();
                if (mPullIndicator.hasLeftStartPosition()) {
                    if (mPullIndicator.hasBelowStartPosition()) {
                        tryScrollBackToTop();
                        sendCancelEvent();
                        return true;
                    }
                    return dispatchTouchEventSupper(e);
                } else {
                    return dispatchTouchEventSupper(e);
                }
            case MotionEvent.ACTION_DOWN:
                mHasSendCancelEvent = false;
                mPullIndicator.onPressDown(e.getX(), e.getY());
                mScrollChecker.abortIfWorking();

                mPreventForHorizontal = false;

                dispatchTouchEventSupper(e);
                return true;
            case MotionEvent.ACTION_MOVE:
                mLastMoveEvent = e;
                mPullIndicator.onMove(e.getX(), e.getY());
                float offsetY = mPullIndicator.getOffsetY();
                float offsetX = mPullIndicator.getOffsetX();

                if (!mPreventForHorizontal && (Math.abs(offsetX) > mPagingTouchSlop && Math.abs(offsetX) > Math.abs(offsetY))) {
                    mPreventForHorizontal = true;
                }
                if (mPreventForHorizontal) {
                    return dispatchTouchEventSupper(e);
                }

                if (canMovePos(offsetY)) {
                    movePos(offsetY);
                    return true;
                }
        }
        return dispatchTouchEventSupper(e);
    }

    private boolean canMovePos(float offsetY) {
        boolean moveDown = offsetY > 0;
        return (moveDown && checkCanDoPullDown()) ||
                mPullIndicator.hasLeftStartPosition();
    }

    private boolean checkCanDoPullDown() {
        if (mScrollTarget != null) {
            return !ViewCompat.canScrollVertically(mScrollTarget, -1);
        }
        return true;
    }

    public boolean dispatchTouchEventSupper(MotionEvent e) {
        return super.dispatchTouchEvent(e);
    }

    private void movePos(float deltaY) {

        int to = mPullIndicator.getCurrentPosY() + (int) deltaY;

        if (isReDirect(to)) {
            to = -mPullIndicator.getHeaderHeight();
        }

        mPullIndicator.setCurrentPos(to);
        if (to > 0) {
            mPullIndicator.setHeaderCurrentPos(0);
        } else {
            mPullIndicator.setHeaderCurrentPos(to);
        }
        int change = to - mPullIndicator.getLastPosY();
        updatePos(change);
    }

    private boolean isReDirect(int to) {
        return Math.abs(to + mPullIndicator.getCurrentPosY() + 2 * mPullIndicator.getHeaderHeight())
                < Math.abs(to + mPullIndicator.getHeaderHeight()) + Math.abs(mPullIndicator.getCurrentPosY() + mPullIndicator.getHeaderHeight());
    }

    private void updatePos(int change) {
        if (change == 0) {
            return;
        }

        boolean isUnderTouch = mPullIndicator.isUnderTouch();

        if (isUnderTouch && !mHasSendCancelEvent && mPullIndicator.hasMovedAfterPressedDown()) {
            mHasSendCancelEvent = true;
            sendCancelEvent();
        }

        applySwitchShowText();

        if (isUnderTouch) {
            sendDownEvent();
        }

        updateHeaderOffset(change);
        mContent.offsetTopAndBottom(change);
        invalidate();
    }

    private void updateHeaderOffset(int change) {
        if (mPullIndicator.getHeaderCurrentPosY() >= 0) {
            mHeaderView.offsetTopAndBottom(0 - mPullIndicator.getHeaderLastPosY());

            ViewGroup.LayoutParams layoutParams = mHeaderView.getLayoutParams();
            layoutParams.height = mPullIndicator.getHeaderHeight() + mPullIndicator.getCurrentPosY();
            mHeaderView.setLayoutParams(layoutParams);
        } else {
            mHeaderView.offsetTopAndBottom(change);
        }

    }

    private void applySwitchShowText() {
//        mPullIndicator.applyMoveStatus();
    }

    private void tryScrollBackToTop() {
        if (!mPullIndicator.isUnderTouch()) {
            mScrollChecker.tryToScrollTo(mPullIndicator.POS_START, mDurationToGoBack);
        }
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        mScrollTarget = target;
        return false;
    }

/*
    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (!consumed) {
            flingWithNestedDispatch((int) velocityY);
            return true;
        }
        return false;
    }

    private boolean flingWithNestedDispatch(int velocityY) {
        final boolean canFling = true;*//*(mHeaderController.canScrollUp() && velocityY > 0) ||
                (mHeaderController.canScrollDown() && velocityY < 0)*//*;
        if (!dispatchNestedPreFling(0, velocityY)) {
            dispatchNestedFling(0, velocityY, canFling);
            if (canFling) {
                fling(velocityY);
            }
        }
        return canFling;
    }

    public void fling(int velocityY) {
//        mPullState = STATE_FLING;
        mScroller.abortAnimation();
        mScroller.fling(0, 10, 0, velocityY, 0, 0,
                10, 40,
                0, 0);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return flingWithNestedDispatch((int) velocityY);
    }

    */

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return super.onNestedFling(target, velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return super.onNestedPreFling(target, velocityX, velocityY);
    }

    public void disableWhenHorizontalMove(boolean disable) {
        mDisableWhenHorizontalMove = disable;
    }

    public void setHeaderView(View header) {
        if (!(header instanceof Header)) {
            throw new IllegalStateException("header must implements HeaderImpl");
        }
        if (mHeaderView != null && header != null && mHeaderView != header) {
            removeView(mHeaderView);
        }
        ViewGroup.LayoutParams lp = header.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(-1, -2);
            header.setLayoutParams(lp);
        }
        mHeaderView = header;
        //mPullIndicator.setHeaderImpl((Header) mHeaderView);
        addView(header);
    }

    public void setShowText(PullIndicator.ShowText showText) {
        mPullIndicator.setShowText(showText);
    }

    private void sendCancelEvent() {
        if (mLastMoveEvent == null) {
            return;
        }
        MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime() + ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        dispatchTouchEventSupper(e);
    }

    private void sendDownEvent() {
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(), MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
        dispatchTouchEventSupper(e);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    class ScrollChecker implements Runnable {

        private int mLastFlingY;
        private Scroller mScroller;
        private boolean mIsRunning = false;
        private int mStart;
        private int mTo;

        public ScrollChecker() {
            mScroller = new Scroller(getContext());
        }

        public void run() {
            boolean finish = !mScroller.computeScrollOffset() || mScroller.isFinished();
            int curY = mScroller.getCurrY();
            int deltaY = curY - mLastFlingY;

            if (!finish) {
                mLastFlingY = curY;
                movePos(deltaY);
                post(this);
            } else {
                finish();
            }
        }

        private void finish() {
            reset();
            //onPtrScrollFinish();
        }

        private void reset() {
            mIsRunning = false;
            mLastFlingY = 0;
            removeCallbacks(this);
        }

        private void destroy() {
            reset();
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
        }

        public void abortIfWorking() {
            if (mIsRunning) {
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
                reset();
            }
        }

        public void tryToScrollTo(int to, int duration) {
            if (mPullIndicator.isAlreadyHere(to)) {
                return;
            }
            mStart = mPullIndicator.getCurrentPosY();
            mTo = to;
            int distance = to - mStart;
            removeCallbacks(this);

            mLastFlingY = 0;

            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            mScroller.startScroll(0, 0, 0, distance, duration);
            post(this);
            mIsRunning = true;
        }


        public void fling(int velocityX, int velocityY) {
            //setScrollState(SCROLL_STATE_SETTLING);
            //mLastFlingX = mLastFlingY = 0;
            //mStart = mPullIndicator.getCurrentPosY();

            mScroller.fling(0, 0, velocityX, velocityY,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            removeCallbacks(this);
            mLastFlingY = 0;
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            post(this);
            mIsRunning = true;
        }
    }

}
