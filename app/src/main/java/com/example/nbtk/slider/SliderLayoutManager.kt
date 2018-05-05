package com.example.nbtk.slider

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView

/**
 * Created by nbtk on 5/4/18.
 */
class SliderLayoutManager(context: Context?) : LinearLayoutManager(context) {

    init {
         orientation = HORIZONTAL;
    }

    var callback: OnItemSelectedListener? = null
    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToWindow(view: RecyclerView?) {
        super.onAttachedToWindow(view)
        recyclerView = view!!

        // Smart snapping
        LinearSnapHelper().attachToRecyclerView(recyclerView)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        return super.scrollHorizontallyBy(dx, recycler, state)
    }

    override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int) {

        // smoothScrollToPosition(...) scrolls the recyclerview to the beginning (or end) of the view
        // And then snapping creates another scroll.
        // We can prevent the second scroll (snap) if we calculate and scroll directly to the view's center.

        try {

            // findViewByPosition(...) gives us the View instance that will be in the given adapter position.
            val child = findViewByPosition(position)

            // Child center related to recyclerView dimensions - including padding, margin, scaling, whatever.
            val childCenter = (getDecoratedRight(child) - getDecoratedLeft(child))/2 + getDecoratedLeft(child)

            // RecyclerView center.
            val recyclerViewCenter = getRecyclerViewCenterX()

            // The exact distance we need to scroll.
            val distanceToScroll = childCenter - recyclerViewCenter

            // Smooth scroll
            recyclerView?.smoothScrollBy(distanceToScroll, 0)

        } catch (e: Exception) {
            super.smoothScrollToPosition(recyclerView, state, position)
        }
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)

        // When scroll stops we notify on the selected item
        if (state.equals(RecyclerView.SCROLL_STATE_IDLE)) {

            // Find the closest child to the recyclerView center --> this is the selected item.
            val recyclerViewCenterX = getRecyclerViewCenterX()
            var minDistance = recyclerView.width
            var position = -1
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                val childCenterX = getDecoratedLeft(child) + (getDecoratedRight(child) - getDecoratedLeft(child)) / 2
                var newDistance = Math.abs(childCenterX - recyclerViewCenterX)
                if (newDistance < minDistance) {
                    minDistance = newDistance
                    position = recyclerView.getChildLayoutPosition(child)
                }
            }

            // Notify the developer which item is centered
            callback?.onItemSelected(position)
        }
    }

    private fun getRecyclerViewCenterX() : Int {
        return (recyclerView.right - recyclerView.left)/2 + recyclerView.left
    }

    interface OnItemSelectedListener {
        fun onItemSelected(layoutPosition: Int)
    }
}