package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Vocabulary Queries ---
    @Query("SELECT * FROM vocabulary ORDER BY word ASC")
    fun getAllVocabulary(): Flow<List<VocabularyWord>>

    @Query("SELECT * FROM vocabulary WHERE nextReviewTimeMillis <= :now ORDER BY nextReviewTimeMillis ASC")
    fun getWordsForReview(now: Long): Flow<List<VocabularyWord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(word: VocabularyWord)

    @Delete
    suspend fun deleteVocabulary(word: VocabularyWord)

    @Query("SELECT * FROM vocabulary WHERE word = :word LIMIT 1")
    suspend fun getWord(word: String): VocabularyWord?

    // --- Chat Queries ---
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatHistory(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(msg: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()

    // --- Article Queries ---
    @Query("SELECT * FROM articles")
    fun getAllArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: Long): Article?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article)

    @Update
    suspend fun updateArticle(article: Article)

    // --- User Progress Queries ---
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgressFlow(): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getUserProgressDirect(): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(progress: UserProgress)
}
