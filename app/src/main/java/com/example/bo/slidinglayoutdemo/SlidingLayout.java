package com.example.bo.slidinglayoutdemo;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by bo on 25/12/15.
 */
public class SlidingLayout extends ViewGroup {

    private final Set<View> mNewViews = new HashSet<>();
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

    public void setVelocity(int velocity) {
        mVelocity = velocity;
    }

    public void setDividerSize(int size) {
        mDividerSize = size;
    }

    private void preAddView(View child) {
        mNewViews.add(child);
    }

    @Override
    public void addView(View child) {
        preAddView(child);
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        preAddView(child);
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        preAddView(child);
        super.addView(child, index, params);
    }

    @Override
    public void addView(View child, LayoutParams params) {
        preAddView(child);
        super.addView(child, params);
    }

    @Override
    public void addView(View child, int width, int height) {
        preAddView(child);
        super.addView(child, width, height);
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
        postRemoveView(view);
    }

    @Override
    public void removeViewAt(int index) {
        View view = getChildAt(index);
        super.removeViewAt(index);
        postRemoveView(view);
    }

    @Override
    public void removeViews(int start, int count) {
        View[] removedViews = new View[count];
        for (int i = start; i < count; i++) {
            removedViews[i] = getChildAt(i);
        }
        super.removeViews(start, count);
        for (View v : removedViews) {
            postRemoveView(v);
        }
    }

    @Override
    public void removeAllViews() {
        int count = getChildCount();
        View[] removedViews = new View[count];
        for (int i = 0; i < count; i++) {
            removedViews[i] = getChildAt(i);
        }
        super.removeAllViews();
        for (View v : removedViews) {
            postRemoveView(v);
        }
    }

    private void postRemoveView(View view) {
        mNewViews.remove(view);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxHeight = 0;
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
        }

        maxHeight = Math.max(maxHeight + getPaddingTop() + getPaddingBottom(), getSuggestedMinimumHeight());
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), resolveSize(maxHeight,
                heightMeasureSpec));
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

        int left = getPaddingLeft() + mFirstChildLeft;
        boolean deleteFirstChild = false;
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                left += mDividerSize;
            }

            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            left += lp.leftMargin;
            int w = child.getMeasuredWidth();
            if (i == 0 && -left > w) {
                deleteFirstChild = true;
            }

            if (mNewViews.contains(child)) {
                mNewViews.remove(child);
                if (left >= 0 && left < getMeasuredWidth()) {
                    lp.leftMargin += getMeasuredWidth() - left;
                    left = getMeasuredWidth();
                }
            }

            // try to center child vertically
            int h = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
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
            removeViewAt(0);
        } else {
            mFirstChildLeft -= mVelocity;
        }
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
}
