package com.example.simpletracker

import android.content.Context
import androidx.room.*


@Database(
    entities = [Tag::class, Tag.Point::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(TagConverters::class)
abstract class TagDatabase : RoomDatabase() {

    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: TagDatabase? = null

        fun getDatabase(context: Context): TagDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            if (INSTANCE == null) {
                synchronized(this) {
                    // Pass the database to the INSTANCE
                    INSTANCE = buildDatabase(context)
                }
            }
            // Return database.
            return INSTANCE!!
        }


        private fun buildDatabase(context: Context): TagDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                TagDatabase::class.java,
                "tag_database"
            )
                .build()
        }
    }
}
