package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val appDao: AppDao) {

    // --- Flows ---
    val allVocabulary: Flow<List<VocabularyWord>> = appDao.getAllVocabulary()
    val chatHistory: Flow<List<ChatMessage>> = appDao.getChatHistory()
    val allArticles: Flow<List<Article>> = appDao.getAllArticles()
    val userProgress: Flow<UserProgress?> = appDao.getUserProgressFlow()

    fun getWordsForReview(now: Long): Flow<List<VocabularyWord>> = appDao.getWordsForReview(now)

    // --- Vocabulary operations ---
    suspend fun saveWord(word: VocabularyWord) {
        appDao.insertVocabulary(word)
    }

    suspend fun getWord(word: String): VocabularyWord? {
        return appDao.getWord(word)
    }

    suspend fun deleteWord(word: VocabularyWord) {
        appDao.deleteVocabulary(word)
    }

    // --- Chat operations ---
    suspend fun saveChatMessage(role: String, message: String) {
        val msg = ChatMessage(role = role, message = message)
        appDao.insertChatMessage(msg)
    }

    suspend fun clearChat() {
        appDao.clearChatHistory()
    }

    // --- Article operations ---
    suspend fun getArticleById(id: Long): Article? {
        return appDao.getArticleById(id)
    }

    suspend fun markArticleRead(id: Long) {
        val article = appDao.getArticleById(id)
        if (article != null) {
            appDao.updateArticle(article.copy(isRead = true))
        }
    }

    // --- Progress operations ---
    suspend fun updateProgress(updater: (UserProgress) -> UserProgress) {
        val current = appDao.getUserProgressDirect() ?: UserProgress()
        val updated = updater(current)
        appDao.insertUserProgress(updated)
    }

    // --- Prepopulation logic ---
    suspend fun checkAndPrepopulate() {
        // 1. Prepopulate default progress if not present
        val currentProgress = appDao.getUserProgressDirect()
        if (currentProgress == null) {
            appDao.insertUserProgress(UserProgress(id = 1))
        }

        // 2. Prepopulate articles if empty
        val currentArticles = appDao.getAllArticles().firstOrNull() ?: emptyList()
        if (currentArticles.isEmpty()) {
            val defaultArticles = listOf(
                Article(
                    title = "Developing a Healthy Morning Routine",
                    category = "Lifestyle",
                    content = "A consistent morning routine can greatly improve mental clarity and daily productivity. Simple habits like drinking water, stretching, and reading for ten minutes help prepare your mind for the tasks ahead. Reducing screen time during the first hour of the day is also highly recommended by psychologists to avoid immediate cognitive stress.",
                    summary = "Learn simple, scientifically-backed habits to kickstart your day with high energy and focus.",
                    difficulty = "Beginner",
                    imageSeed = "nature"
                ),
                Article(
                    title = "The Ubiquity and Influence of AI in Daily Life",
                    category = "Technology",
                    content = "Artificial Intelligence is no longer just a concept in science fiction. Today, AI models actively assist in complex medical diagnoses, optimize city traffic light systems, and power conversational assistants. As technology advances rapidly, understanding how to interact productively with machine intelligence is becoming an essential skill for the modern global workforce.",
                    summary = "Discover how machine learning algorithms quietly shape our choices, tools, and professional environments.",
                    difficulty = "Intermediate",
                    imageSeed = "tech"
                ),
                Article(
                    title = "Unlocking Secrets of Eternal Deep Ocean Mysteries",
                    category = "Science",
                    content = "The deep sea remains one of the least explored and most hostile environments on Earth. Crushing hydrostatic pressure, absolute darkness, and freezing temperatures make manned exploration extremely dangerous. Yet, marine biotechs continue to discover fascinating species and vibrant bioluminescent organisms that thrive near hydrothermal vents, challenging conventional biochemical limits.",
                    summary = "Explore extreme biospheres where unique, light-generating creatures adapt to darkness and immense pressure.",
                    difficulty = "Advanced",
                    imageSeed = "ocean"
                ),
                Article(
                    title = "The Psychology of Sustained Language Learning Motivation",
                    category = "Education",
                    content = "Acquiring a second language is a long-term cognitive endeavor that requires persistence. Standard rote memorization often leads to cognitive fatigue and rapid abandonment. Cognitive scientists emphasize the importance of comprehensible input, spaced repetition, and gamified reinforcement to activate the brain's natural reward systems and maintain consistent progress.",
                    summary = "Understand how the human brain processes and retains language, and how to build strong learning habits.",
                    difficulty = "Intermediate",
                    imageSeed = "book"
                )
            )
            for (article in defaultArticles) {
                appDao.insertArticle(article)
            }
        }
    }
}
