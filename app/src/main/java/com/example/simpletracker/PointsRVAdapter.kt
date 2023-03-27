package com.example.simpletracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class PointsRVAdapter : ListAdapter<Tag.Point, PointsRVAdapter.PointHolder>(com.example.simpletracker.PointsRVAdapter.DiffCallback()) {

    class PointHolder(view: View) : RecyclerView.ViewHolder(view)

    private lateinit var listener: RecyclerClickListener
    fun setItemListener(listener: RecyclerClickListener) {
        this.listener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.point_row, parent, false)
        val pointHolder = PointHolder(v)

        val savePointNow = pointHolder.itemView.findViewById<ImageButton>(R.id.point_delete)
        savePointNow.setOnClickListener {
            listener.onSavePointClick(pointHolder.adapterPosition, 0)
        }

        val point = pointHolder.itemView.findViewById<CardView>(R.id.point)
        point.setOnClickListener {
            listener.onItemClick(pointHolder.adapterPosition)
        }

        return pointHolder
    }

    override fun onBindViewHolder(holder: PointHolder, position: Int) {
        val currentItem = getItem(position)
        val pointText = holder.itemView.findViewById<TextView>(R.id.point_text)
        pointText.text = currentItem.severity.toString() +" on "+ currentItem.time.toString()
    }

    class DiffCallback : DiffUtil.ItemCallback<Tag.Point>() {
        override fun areItemsTheSame(oldItem: Tag.Point, newItem: Tag.Point) =
            oldItem.pointId == newItem.pointId

        override fun areContentsTheSame(oldItem: Tag.Point, newItem: Tag.Point) =
            oldItem == newItem
    }
}