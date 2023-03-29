package com.example.simpletracker

import androidx.room.*
import java.util.*


@Entity(tableName = "tag_table")
data class Tag(
    @PrimaryKey(autoGenerate = true) var tagId: Int,
    var tagName: String,
    var tagNote: String?,
    var dateAdded: Date,
    var reminder: Calendar,
    var reminderOn: Boolean = false
){

    @Entity(tableName = "point_table")
    data class Point(
        @PrimaryKey(autoGenerate = true) val pointId: Long,
        val tagIdForeignKey: Int,
        val time: Date,
        val severity: Int )
}

//https://johncodeos.com/how-to-use-room-in-android-using-kotlin/
//https://developer.android.com/training/data-storage/room#kotlin