# Create Your Own Horizontal (& Vertical) Slider/Picker
<p>Recently at Reali we needed to create a Horizontal Slider. I was looking around the web and learned what's the minimum skeleton your code need to have. All the rest - you can customize.</p>

<img src="https://cdn-images-1.medium.com/max/800/1*ekbM1nDCBDeU6GO8gb3u2w.gif" width=200/>

## What is the Picker functionality?
<p>A Slider must have the following functionality:
<ul><li>Scroll / Fling with snapping (snapping = putting the item's center in the Slider's center)</li>
  <li>Clicking on an item will smoothly-scroll the item to the Slider's center</li>
</ul></p>

## The bare minimum we need
<p>There minimal parts that we need to create are:
<ul>
  <li>RecyclerView: Nothing new here. Adapter, Views…</li>
<li>Extending LinearLayoutManager: Here we will handle the smooth-scrolling & snapping. We will also expose a callback for when item is being selected: click / scroll / fling.</li>
</ul></p>

## First Step: boilerplate code
<p>Need to setup an activity, layout, RecyclerView, adapter and all that. After adding the boilerplate, with some styling, our the result will look like:<br/></p>
<img src="https://cdn-images-1.medium.com/max/800/1*klBzzIv6PHfLMIgURXzatQ.gif"/>

<p>As you can see, there are multiple problems here:
<ol>
<li>The items near the edges ("1" and "20" and more) cannot be in the center, since the RecyclerView is attached to parent-left and parent-right.</li>
  <li>Clicks doesn't put the item in the center.</li>
  <li>Scrolling / Flinging can stop between two items (no snapping)</li>
  <li>Notifying on item selection.</li>
</ol></p>

## Second Step: padding to items "1" and "20"
<p>This simple - we only need to set "clipToPadding=false" in the XML, and give left-padding and right-padding to the RecyclerView:</p>

### clipToPadding:
```
<android.support.v7.widget.RecyclerView
    android:id="@+id/rv_horizontal_picker"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintLeft_toLeftOf="parent"
    app:layout_constraintRight_toRightOf="parent"
    app:layout_constraintTop_toTopOf="parent"
    android:clipToPadding="false" />
```

### Padding for "1" and "20"
<p>In order to have "1" and "20" in the center we need to give padding to the RecyclerView. But because the screen-sizes and density variety, the padding needs to be calculated. In our example, the slider stretches to the parent edges, so the formula is:</p>

```
val padding: Int = ScreenUtils.getScreenWidth(this)/2
rvHorizontalPicker.setPadding(padding, 0, padding, 0)
```

As you can see it's not enough because we moved the "1" and "20" too much:<br/>
<img src="https://cdn-images-1.medium.com/max/800/1*9xe2qD_PAsRHra_0JrrtnA.gif"/>

This is because we need to consider the padding of the slider-item, which currently is set to 40dp. So let's modify the formula above:

```
val padding: Int = ScreenUtils.getScreenWidth(this)/2 - ScreenUtils.dpToPx(this, 40)
rvHorizontalPicker.setPadding(padding, 0, padding, 0)
```

And now we're much better:<br/>
<img src="https://cdn-images-1.medium.com/max/800/1*0Eu4DLKnV8LvsjdZbXsAyQ.gif"/>

Still there's some padding work to do, but the concept is clear.

## Third Step: click handling
On a click event, we want the slider to have the clicked-item in the center. We just call the RecyclerView smoothScrollToPosition(…) function in the onClick():
```
// This code is in the adapter onCreateViewHolder(...):
itemView.setOnClickListener(object : View.OnClickListener {
    override fun onClick(v: View?) {
        v?.let { callback?.onItemClicked(it) }
    }
}
...
...
...
// This code is in the activity / fragment
override fun onItemClicked(view: View) {
    val position = rvHorizontalPicker.getChildLayoutPosition(view)
    rvHorizontalPicker.smoothScrollToPosition(position)
}
```

And the result is:<br/>
<img src="https://cdn-images-1.medium.com/max/800/1*FRTiKyoPv1xpNRQdjQpW0g.gif" />

## Fourth Step: Snapping
When the user scrolls the screen, we want the selected item to be exactly in the RecyclerView's center. Here LinearSnapHelper comes to our aid:
```
// Smart snapping
LinearSnapHelper().attachToRecyclerView(recyclerView)
```
You can add it in the RecyclerView creation. The result is:<br/>
<img src="https://cdn-images-1.medium.com/max/800/1*wZMX7YjhqPiO3eyrivd2Fw.gif"/>

Now you can see, that the slider isn't stopping between to items. This is what the LinearSnapHelper is doing.

## Fifth Step: notifying on item selection

Here's where our custom LinearLayoutManager comes handy. The concept is: when scroll is finished, we search which item is closest to the slider center, and return it's position:
```
class SliderLayoutManager(context: Context?) : LinearLayoutManager(context) {

    var callback: OnItemSelectedListener? = null

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
                var childDistanceFromCenter = Math.abs(childCenterX - recyclerViewCenterX)
                if (childDistanceFromCenter < minDistance) {
                    minDistance = childDistanceFromCenter
                    position = recyclerView.getChildLayoutPosition(child)
                }
            }

            // Notify on the selected item
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
```
<p>All the logic happen inside onScrollStateChanged(…). We search the slider item, which his center is closest to the slider's center (AKA, the item in the center) and notify it via callback?.onItemSelected(position).</p>
<p>And in the activity we handle the click. We set the callback when creating the layout manager:</p>
```
// Setting layout manager
rvHorizontalPicker.layoutManager = SliderLayoutManager(this).apply {
    callback = object : SliderLayoutManager.OnItemSelectedListener {
        override fun onItemSelected(layoutPosition: Int) {
            tvSelectedItem.setText(data[layoutPosition])
        }
    }
}
```
And the result is:</br>
<img src="https://cdn-images-1.medium.com/max/800/1*XPJJTDjvmbtFpCatQ1aO9g.gif"/>

## Extra Effects
### scaling:
Scaling is simple: the item in the center has scale 1.0f, and further items are scaled according to their distance from the center. During a scroll we calculate the scaling (inside the LayoutManager):
```
class SliderLayoutManager(context: Context?) : LinearLayoutManager(context) {

    ...

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
        super.onLayoutChildren(recycler, state)
        scaleDownView()
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        if (orientation == LinearLayoutManager.HORIZONTAL) {
            val scrolled = super.scrollHorizontallyBy(dx, recycler, state)
            scaleDownView()
            return scrolled
        } else {
            return 0
        }
    }

    private fun scaleDownView() {
        val mid = width / 2.0f
        for (i in 0 until childCount) {
        
            // Calculating the distance of the child from the center
            val child = getChildAt(i)
            val childMid = (getDecoratedLeft(child) + getDecoratedRight(child)) / 2.0f
            val distanceFromCenter = Math.abs(mid - childMid)
        
            // The scaling formula
            val scale = 1-Math.sqrt((distanceFromCenter/width).toDouble()).toFloat()*0.66f
        
            // Set scale to view
            child.scaleX = scale
            child.scaleY = scale
        }
    }
    ...
    ...
}
```
I chose the squared root function to determine the scale. The result is:<br/>
<img src="https://cdn-images-1.medium.com/max/800/1*ekbM1nDCBDeU6GO8gb3u2w.gif"/>
Because of it's shape, the items that are closer the the center are scaled down less, and further items are scaled harder. This is just me. A linear approach would be:
```
val scale = 1-distanceFromCenter/width
```
# How to make the slider vertical?
Very simple: instead of caluclating the center and width using "centerX" and "width" we will use their vertical counterparts - "centerY" and "height". The rest of the logic stays the same.

## Summary
I hope you enjoyed and learned from this post. Instead of bloating your code with unnecessary code, here you have the basic functionality you need. Happy coding :)
