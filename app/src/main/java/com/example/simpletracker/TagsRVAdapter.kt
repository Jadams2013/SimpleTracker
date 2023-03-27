package com.example.simpletracker

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


class TagsRVAdapter : ListAdapter<Tag, TagsRVAdapter.TagHolder>(DiffCallback()) {

    class TagHolder(view: View) : RecyclerView.ViewHolder(view)

    private lateinit var listener: RecyclerClickListener
    fun setItemListener(listener: RecyclerClickListener) {
        this.listener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.tags_row, parent, false)
        val tagHolder = TagHolder(v)

        /*val tagDelete = tagHolder.itemView.findViewById<ImageView>(R.id.tag_delete)
        tagDelete.setOnClickListener {
            //listener.onItemRemoveClick(tagHolder.adapterPosition)
        }// */

        val savePointNow = tagHolder.itemView.findViewById<Button>(R.id.savePointNow)
        savePointNow.setOnClickListener {
            var severity = tagHolder.itemView.findViewById<SeekBar>(R.id.severity_seekbar).progress
            severity++
            listener.onSavePointClick(tagHolder.adapterPosition, severity)
        }

        val tag = tagHolder.itemView.findViewById<CardView>(R.id.tag)
        tag.setOnClickListener {
            listener.onItemClick(tagHolder.adapterPosition)
        }

        return tagHolder
    }

    override fun onBindViewHolder(holder: TagHolder, position: Int) {
        val currentItem = getItem(position)
        val tagName = holder.itemView.findViewById<TextView>(R.id.tag_text)
        tagName.text = currentItem.tagName
    }

    class DiffCallback : DiffUtil.ItemCallback<Tag>() {
        override fun areItemsTheSame(oldItem: Tag, newItem: Tag) =
            oldItem.tagName == newItem.tagName

        override fun areContentsTheSame(oldItem: Tag, newItem: Tag) =
            oldItem == newItem
    }
}