package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary")
data class VocabularyWord(
    @PrimaryKey val word: String,
    val definition: String,
    val ipa: String = "",
    val example: String = "",
    val level: String = "Intermediate", // Beginner, Intermediate, Advanced
    val intervalDays: Int = 1, // Spaced Repetition interval
    val nextReviewTimeMillis: Long = System.currentTimeMillis(),
    val isLearned: Boolean = false,
    val difficultyScore: Int = 0 // 1 to 5
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "user" or "model"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // News, Science, Tech, Life, etc.
    val content: String,
    val summary: String = "",
    val difficulty: String = "Intermediate", // Beginner, Intermediate, Advanced
    val imageSeed: String = "general",
    val isRead: Boolean = false
)

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1, // Single row configuration
    val assessmentLevel: String = "Not Checked", // Beginner, Intermediate, Advanced
    val streak: Int = 0,
    val lastActiveTimeMillis: Long = 0L,
    val totalWordsLearned: Int = 0,
    val minutesStudied: Int = 0
)
