package com.example.simpletracker

interface RecyclerClickListener {
    //fun onItemRemoveClick(position: Int)
    fun onItemClick(position: Int)
    fun onSavePointClick(position: Int, severity: Int)
}
