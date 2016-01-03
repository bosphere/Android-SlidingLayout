package com.example.bo.slidinglayoutdemo;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by bo on 25/12/15.
 */
public class SlidingLayout extends ViewGroup {

    private final Queue<SlideItem> mQueue = new LinkedList<>();
    private int mFirstChildLeft;
    private View mFirstChild;
    private int mVelocity;
    private int mDividerSize;

    public SlidingLayout(Context context) {
        super(context);
        init();
    }

    public SlidingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlidingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SlidingLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mVelocity = 2;
        mDividerSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                getResources().getDisplayMetrics());
    }

    public void enqueue(SlideItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Slide item cannot be null");
        }
        item.state = SlideItem.QUEUING;
        mQueue.add(item);
        if (getChildCount() == 0) {
            appendNext();
        }
    }

    public void setVelocity(int velocity) {
        mVelocity = velocity;
    }

    public void setDividerSize(int size) {
        mDividerSize = size;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int maxHeight = 0;
        int childMeasureSpec = getDefaultChildMeasureSpec();
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = getChildAt(i);
            measureChild(child, childMeasureSpec, childMeasureSpec);

            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
        }

        maxHeight = Math.max(maxHeight + getPaddingTop() + getPaddingBottom(), getSuggestedMinimumHeight());
        int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int h;
        if (heightMode == MeasureSpec.EXACTLY) {
            h = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        } else {
            h = resolveSize(maxHeight, heightMeasureSpec);
        }

        setMeasuredDimension(w, h);
    }

    private int getDefaultChildMeasureSpec() {
        return MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        doChildrenLayout();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (getChildCount() > 0) {
            doChildrenLayout();
            invalidate();
        }
    }

    private void doChildrenLayout() {
        int count = getChildCount();
        if (count == 0) {
            appendNext();
            return;
        }

        View first = getChildAt(0);
        if (mFirstChild != first) {
            if (mFirstChild == null) {
                mFirstChildLeft = 0;
            } else {
                mFirstChildLeft = mDividerSize;
            }
            mFirstChild = first;
        }

        int vw = getMeasuredWidth();
        int vh = getMeasuredHeight();

        int left = getPaddingLeft() + mFirstChildLeft;
        boolean deleteFirstChild = false;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            Object tag = child.getTag();
            SlideItem item = null;
            if (tag instanceof SlideItem) {
                item = (SlideItem) tag;
            }

            if (item == null || (item.state != SlideItem.DISPLAYING && item.state != SlideItem.NEW)) {
                // invalid child view, ignore
                child.layout(0, 0, 0, 0);
                continue;
            }

            if (i > 0) {
                left += mDividerSize;
            }

            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            left += lp.leftMargin;
            int w = child.getMeasuredWidth();
            if (i == 0 && -left > w) {
                deleteFirstChild = true;
            }

            if (item.state == SlideItem.NEW) {
                item.state = SlideItem.DISPLAYING;
                if (left >= 0 && left < vw) {
                    lp.leftMargin += vw - left;
                    left = vw;
                }
            }

            // try to center child vertically
            int h = vh - getPaddingTop() - getPaddingBottom();
            int ch = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            int extraTopPadding = 0;
            if (ch < h) {
                extraTopPadding = (int) ((h - ch) / 2.0);
            }

            // layout in action
            int cl = left;
            int ct = getPaddingTop() + lp.topMargin + extraTopPadding;
            int cr = left + w;
            int cb = ct + child.getMeasuredHeight();
            child.layout(cl, ct, cr, cb);

            left += w + lp.rightMargin;
        }

        if (deleteFirstChild) {
            // first child already out of sight, release
            releaseFirst();
        } else {
            mFirstChildLeft -= mVelocity;
        }

        if (left <= vw) {
            appendNext();
        }
    }

    private void releaseFirst() {
        View first = getChildAt(0);
        if (first == null) {
            return;
        }
        Object tag = first.getTag();
        if (tag instanceof SlideItem) {
            SlideItem item = (SlideItem) tag;
            item.state = SlideItem.REMOVED;
        }
        removeViewAt(0);
    }

    private void appendNext() {
        SlideItem item = mQueue.poll();
        if (item == null || item.state != SlideItem.QUEUING) {
            return;
        }
        View view = item.onCreateView();
        if (view == null) {
            return;
        }

        item.state = SlideItem.NEW;

        view.setTag(item);
        super.addView(view);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    public static abstract class SlideItem {

        private static final int QUEUING    = 1;
        private static final int NEW        = 2;
        private static final int DISPLAYING = 3;
        private static final int REMOVED    = 4;

        private int state;

        protected abstract View onCreateView();
    }
}
