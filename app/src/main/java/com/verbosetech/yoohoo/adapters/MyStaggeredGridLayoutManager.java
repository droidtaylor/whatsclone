package com.verbosetech.whatsclone.adapters;

import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by a_man on 31-12-2017.
 */

public class MyStaggeredGridLayoutManager extends StaggeredGridLayoutManager {

    private Point mMeasuredDimension = new Point();

    public MyStaggeredGridLayoutManager() {
        super(2, HORIZONTAL);
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
                          int widthSpec, int heightSpec) {

        final int widthSize = View.MeasureSpec.getSize(widthSpec) - (getPaddingRight() + getPaddingLeft());

        int width = 0;
        int height = 0;
        int row = 0;

        for (int i = 0; i < getItemCount(); i++) {

            if (!measureScrapChild(recycler, i,
                    View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                    mMeasuredDimension)) continue;

            if (width + mMeasuredDimension.x > widthSize || mMeasuredDimension.x > widthSize) {
                row++;
                width = mMeasuredDimension.x;
            } else {
                width += mMeasuredDimension.x;
            }

            height += mMeasuredDimension.y;
        }

        setSpanCount(row);
        setMeasuredDimension(View.MeasureSpec.getSize(widthSpec), height);
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    private boolean measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec, int heightSpec, Point measuredDimension) {

        View view = null;
        try {
            view = recycler.getViewForPosition(position);
        } catch (Exception ex) {
            // try - catch is needed since support library version 24
        }

        if (view != null) {

            RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();

            int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                    getPaddingLeft() + getPaddingRight(), p.width);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                    getPaddingTop() + getPaddingBottom(), p.height);

            view.measure(childWidthSpec, childHeightSpec);

            measuredDimension.set(
                    view.getMeasuredWidth() + p.leftMargin + p.rightMargin,
                    view.getMeasuredHeight() + p.bottomMargin + p.topMargin
            );

            recycler.recycleView(view);

            return true;
        }

        return false;
    }

}
