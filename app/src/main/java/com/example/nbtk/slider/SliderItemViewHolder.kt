package com.example.nbtk.slider

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

/**
 * Created by nbtk on 5/4/18.
 */
class SliderItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    val tvItem: TextView? = itemView?.findViewById(R.id.tv_item)
}