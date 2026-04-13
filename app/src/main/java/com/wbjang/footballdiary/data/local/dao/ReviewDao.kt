package com.wbjang.footballdiary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wbjang.footballdiary.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("SELECT * FROM reviews WHERE matchId = :matchId LIMIT 1")
    fun getReviewByMatchId(matchId: Int): Flow<ReviewEntity?>

    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Query("DELETE FROM reviews WHERE matchId = :matchId")
    suspend fun deleteReviewByMatchId(matchId: Int)

    @Query("UPDATE reviews SET homeScore = :homeScore, awayScore = :awayScore WHERE matchId = :matchId")
    suspend fun updateScore(matchId: Int, homeScore: Int, awayScore: Int)
}
