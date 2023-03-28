package com.example.simpletracker

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: Tag)

    @Query("SELECT * FROM tag_table ORDER BY tagName ASC")
    fun getAlphabetizedTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tag_table WHERE tagId = :tag LIMIT 1")
    fun getTagById(tag: Int): Flow<Tag>

    @Query("SELECT * FROM tag_table WHERE reminderOn = :b")
    fun getTagByAlarm(b: Boolean): Flow<List<Tag>>

    @Query("SELECT * FROM tag_table ORDER BY dateAdded ASC")
    fun getTagsByDateAdded(): Flow<List<Tag>>

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("DELETE FROM tag_table")
    suspend fun clearAllTags()


    /*@Query("SELECT CORR(severity) FROM point_table " +
            "WHERE point_table.tagIdForeignKey LIKE :tag1 " ) //TODO FIRST do this
    fun calculateCorrelation(tag1: Int, tag2: Int): Flow<Double>
    // */

    @Query("SELECT AVG(severity) FROM point_table " +
            "WHERE point_table.tagIdForeignKey LIKE :tag " )
    fun meanSeverity(tag: Int): Flow<Double?>

    @Query("SELECT COUNT(pointId) FROM point_table " +
            "WHERE point_table.tagIdForeignKey LIKE :tag " )
    fun numPoints(tag: Int): Flow<Int>


    //Point stuff
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(point: Tag.Point)

    @Query("SELECT * FROM point_table  " +
            "INNER JOIN tag_table ON point_table.tagIdForeignKey = tag_table.tagId " +
            "WHERE tag_table.tagId LIKE :tag " +
            "ORDER BY point_table.time ASC")
    fun getPointsByTag(tag: Int): Flow<List<Tag.Point>>

    @Update
    suspend fun updatePoint(point: Tag.Point)

    @Delete
    suspend fun deletePoint(point: Tag.Point)

    @Query("DELETE FROM point_table")
    suspend fun clearAllPoints()

    @Query("DELETE FROM point_table " +
            "WHERE point_table.tagIdForeignKey LIKE :tag" )
    suspend fun deletePointsByTag(tag: Int)

}