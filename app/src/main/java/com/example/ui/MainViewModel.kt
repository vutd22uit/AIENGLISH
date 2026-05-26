package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.api.GeminiRetrofitClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    // --- State Flows ---
    val allVocabulary: StateFlow<List<VocabularyWord>> = repository.allVocabulary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatHistory: StateFlow<List<ChatMessage>> = repository.chatHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allArticles: StateFlow<List<Article>> = repository.allArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProgress: StateFlow<UserProgress> = repository.userProgress
        .map { it ?: UserProgress() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProgress())

    // --- Active Vocabulary Review list ---
    val wordsForReview: StateFlow<List<VocabularyWord>> = allVocabulary
        .map { words ->
            val now = System.currentTimeMillis()
            words.filter { it.nextReviewTimeMillis <= now }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI/UX States ---
    private val _currentTab = MutableStateFlow(0) // 0: Home, 1: AI Chat, 2: Flashcards, 3: Reports
    val currentTab = _currentTab.asStateFlow()

    private val _selectedArticle = MutableStateFlow<Article?>(null)
    val selectedArticle = _selectedArticle.asStateFlow()

    private val _selectedWordDetails = MutableStateFlow<VocabularyWord?>(null)
    val selectedWordDetails = _selectedWordDetails.asStateFlow()

    private val _isTranslatingWord = MutableStateFlow(false)
    val isTranslatingWord = _isTranslatingWord.asStateFlow()

    private val _isChatSending = MutableStateFlow(false)
    val isChatSending = _isChatSending.asStateFlow()

    // --- App Quiz States ---
    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions = _quizQuestions.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex = _currentQuizIndex.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore = _quizScore.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished = _isQuizFinished.asStateFlow()

    private val _isQuizStarted = MutableStateFlow(false)
    val isQuizStarted = _isQuizStarted.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndPrepopulate()
            // Increment studied hours or trigger day streak increase if appropriate
            updateDailyActivity()
        }
    }

    fun selectTab(tab: Int) {
        _currentTab.value = tab
    }

    // --- Auth Actions ---
    fun loginWithEmail(email: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateProgress { current ->
                current.copy(
                    isLoggedIn = true,
                    email = email,
                    username = name.ifEmpty { email.substringBefore("@") },
                    loginProvider = "email"
                )
            }
            onSuccess()
        }
    }

    fun signupWithEmail(username: String, email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateProgress { current ->
                current.copy(
                    isLoggedIn = true,
                    email = email,
                    username = username,
                    loginProvider = "email"
                )
            }
            onSuccess()
        }
    }

    fun loginWithGoogle(email: String = "nobetjk1@gmail.com", username: String = "Nguyễn Văn Trưởng", onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateProgress { current ->
                current.copy(
                    isLoggedIn = true,
                    email = email,
                    username = username.ifEmpty { email.substringBefore("@") },
                    loginProvider = "google"
                )
            }
            onSuccess()
        }
    }

    fun loginWithFacebook(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateProgress { current ->
                current.copy(
                    isLoggedIn = true,
                    email = "vutruongdoan@facebook.com",
                    username = "Đoàn Vũ Trường",
                    loginProvider = "facebook"
                )
            }
            onSuccess()
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.updateProgress { current ->
                current.copy(
                    isLoggedIn = false,
                    email = "",
                    username = "",
                    loginProvider = ""
                )
            }
        }
    }

    fun selectArticle(article: Article?) {
        _selectedArticle.value = article
        if (article != null) {
            viewModelScope.launch {
                repository.markArticleRead(article.id)
                repository.updateProgress { current ->
                    current.copy(minutesStudied = current.minutesStudied + 3)
                }
            }
        }
    }

    fun closeWordPopup() {
        _selectedWordDetails.value = null
    }

    fun addMinutesStudied(minutes: Int) {
        viewModelScope.launch {
            repository.updateProgress { current ->
                current.copy(minutesStudied = current.minutesStudied + minutes)
            }
        }
    }

    fun incrementWordsLearned(amount: Int) {
        viewModelScope.launch {
            repository.updateProgress { current ->
                current.copy(totalWordsLearned = current.totalWordsLearned + amount)
            }
        }
    }

    private suspend fun updateDailyActivity() {
        repository.updateProgress { progress ->
            val now = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000
            val isNewDay = (now - progress.lastActiveTimeMillis) > dayMillis
            val streakVal = if (isNewDay) {
                if ((now - progress.lastActiveTimeMillis) < (2 * dayMillis)) {
                    progress.streak + 1
                } else {
                    1 // streak broke
                }
            } else {
                if (progress.streak == 0) 1 else progress.streak
            }
            progress.copy(
                streak = streakVal,
                lastActiveTimeMillis = now
            )
        }
    }

    // --- Word Click Translation & Explanation ---
    fun explainWordUsingAI(word: String, articleTitle: String) {
        val cleanWord = word.trim().replace(Regex("[^a-zA-Z]"), "").lowercase()
        if (cleanWord.isEmpty()) return

        _isTranslatingWord.value = true
        viewModelScope.launch {
            // Check local DB first to avoid unnecessary remote API calls
            val locallySaved = repository.getWord(cleanWord)
            if (locallySaved != null) {
                _selectedWordDetails.value = locallySaved
                _isTranslatingWord.value = false
                return@launch
            }

            // Word not cached, query Gemini
            val prompt = """
                Bạn là một giáo viên dạy tiếng Anh nhiệt tình và chuyên nghiệp.
                Hãy giải nghĩa chi tiết từ tiếng Anh sau: "$cleanWord".
                Trả về kết quả ở định dạng JSON tiêu chuẩn với các trường chính xác như sau (tuyệt đối không thêm text hay markdown mở đầu/kết thúc, chỉ trả về JSON thuần):
                {
                  "word": "$cleanWord",
                  "ipa": "/phiên âm IPA của từ/",
                  "definition": "Định nghĩa tiếng Việt rõ ràng, ngắn gọn.",
                  "example": "An English example sentence demonstrating correct usage.",
                  "exampleTranslation": "Dịch nghĩa tiếng Việt của câu ví dụ đó."
                }
            """.trimIndent()

            val aiResponse = GeminiRetrofitClient.generateResponse(
                prompt = prompt,
                systemInstruction = "You are a precise JSON response generator. Only return a valid JSON object matching the requested schema.",
                isJson = true
            )

            try {
                // Parse the JSON representation
                val rawJson = aiResponse.trim().removeSurrounding("```json", "```").trim()
                val json = JSONObject(rawJson)
                val wordText = json.optString("word", cleanWord)
                val ipaText = json.optString("ipa", "/.../")
                val defText = json.optString("definition", "Không rõ định nghĩa")
                val exText = json.optString("example", "")
                val exTransText = json.optString("exampleTranslation", "")

                val formattedExample = if (exTransText.isNotEmpty()) "$exText\n($exTransText)" else exText

                val vocabularyWord = VocabularyWord(
                    word = wordText,
                    ipa = ipaText,
                    definition = defText,
                    example = formattedExample,
                    level = userProgress.value.assessmentLevel.ifEmpty { "Intermediate" },
                    intervalDays = 1,
                    nextReviewTimeMillis = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // Next review in 1 day
                )

                // Save to local database flashcards immediately
                repository.saveWord(vocabularyWord)
                _selectedWordDetails.value = vocabularyWord

                // Update total learned count
                repository.updateProgress { progress ->
                    progress.copy(totalWordsLearned = progress.totalWordsLearned + 1)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Fail-safe manual parsing or fallback definition
                val fallbackWord = VocabularyWord(
                    word = cleanWord,
                    ipa = "/.../",
                    definition = "Định nghĩa tạm thời, vui lòng kết nối mạng và thử lại.",
                    example = "Error trying to explain via AI: " + e.localizedMessage,
                    level = "Intermediate"
                )
                _selectedWordDetails.value = fallbackWord
            } finally {
                _isTranslatingWord.value = false
            }
        }
    }

    // --- Save word manually from dictionary ---
    fun saveWordManually(word: VocabularyWord) {
        viewModelScope.launch {
            repository.saveWord(word)
        }
    }

    // --- Remove word from saved list ---
    fun removeWord(word: VocabularyWord) {
        viewModelScope.launch {
            repository.deleteWord(word)
        }
    }

    // --- AI Chatbot features ---
    fun sendChatMessage(messageText: String) {
        if (messageText.isBlank()) return

        val userMessage = messageText.trim()
        _isChatSending.value = true

        viewModelScope.launch {
            // 1. Save user message in local list
            repository.saveChatMessage(role = "user", message = userMessage)

            // 2. Query Gemini chat partner
            val level = userProgress.value.assessmentLevel
            val chatPrompt = """
                You are Alex, an expert English language tutor and partner. The user's English level is: $level.
                Engage in an authentic, natural conversation on diverse topics (daily life, plans, goals, etc.).
                Ensure that:
                1. You respond in natural English matching their proficiency level.
                2. Keep your response friendly, clear, and focused (no more than 3-4 sentences), to invite their reply.
                3. If the user made grammatical mistakes in their previous message, gently and supportively point them out and show how to say it correctly in English, with a small Vietnamese explanation if appropriate. Include it inside a 'Tutor Tip:' section.
                
                The user just said: "$userMessage".
            """.trimIndent()

            // Standardize context from past 6 chat turns
            val currentHistory = chatHistory.value.takeLast(6)
            val historyContext = currentHistory.joinToString("\n") {
                if (it.role == "user") "Learner: ${it.message}" else "Alex: ${it.message}"
            }

            val finalPrompt = if (historyContext.isNotEmpty()) {
                "Here is our dialogue history:\n$historyContext\n\n$chatPrompt"
            } else {
                chatPrompt
            }

            val systemInstr = "You are a warm, supportive English learning partner. Give short and engaging communicative answers."
            val aiReply = GeminiRetrofitClient.generateResponse(
                prompt = finalPrompt,
                systemInstruction = systemInstr
            )

            // 3. Save AI message in local list
            repository.saveChatMessage(role = "model", message = aiReply)
            _isChatSending.value = false

            // Update user study duration
            repository.updateProgress { current ->
                current.copy(minutesStudied = current.minutesStudied + 2)
            }
        }
    }

    fun resetChat() {
        viewModelScope.launch {
            repository.clearChat()
            repository.saveChatMessage(
                role = "model",
                message = "Xin chào! Mình là Alex, bạn học giao tiếp tiếng Anh bằng AI của bạn. Bạn muốn trò chuyện về chủ đề gì ngày hôm nay? (Ví dụ: Du lịch, công việc, sở thích...)"
            )
        }
    }

    // --- Spaced Repetition Grading (Flashcards) ---
    fun gradeFlashcard(word: VocabularyWord, grade: String) {
        viewModelScope.launch {
            val nextInterval = when (grade) {
                "HARD" -> 1 // review again tomorrow
                "GOOD" -> word.intervalDays * 2
                "EASY" -> word.intervalDays * 3
                else -> 1
            }

            val nextReviewTime = System.currentTimeMillis() + (nextInterval * 24L * 60 * 60 * 1000)
            val updatedWord = word.copy(
                intervalDays = nextInterval,
                nextReviewTimeMillis = nextReviewTime,
                isLearned = (grade == "EASY"),
                difficultyScore = if (grade == "HARD") 5 else 2
            )
            repository.saveWord(updatedWord)

            // Complete mini reward
            repository.updateProgress { current ->
                current.copy(minutesStudied = current.minutesStudied + 1)
            }
        }
    }

    // --- Set assessment level manually ---
    fun updateLevelAssessment(level: String) {
        viewModelScope.launch {
            repository.updateProgress { progress ->
                progress.copy(assessmentLevel = level)
            }
        }
    }

    // --- Vocabulary Game Generator ---
    fun generateQuiz() {
        viewModelScope.launch {
            _isQuizStarted.value = true
            _isQuizFinished.value = false
            _currentQuizIndex.value = 0
            _quizScore.value = 0

            val savedWords = allVocabulary.value
            val buildLevel = userProgress.value.assessmentLevel

            val questionsList = mutableListOf<QuizQuestion>()

            if (savedWords.size >= 4) {
                // Generate quiz dynamically using user's saved vocabulary words!
                for (vocab in savedWords.shuffled().take(5)) {
                    val otherAnswers = savedWords
                        .filter { it.word != vocab.word }
                        .shuffled()
                        .take(3)
                        .map { it.definition }

                    val options = (otherAnswers + vocab.definition).shuffled()
                    questionsList.add(
                        QuizQuestion(
                            wordText = vocab.word,
                            ipa = vocab.ipa,
                            correctAnswer = vocab.definition,
                            options = options
                        )
                    )
                }
            }

            // Fallback default list if user hasn't saved enough words
            if (questionsList.size < 3) {
                val sampleTargetPool = getLevelDefaultVocabulary(buildLevel)
                for (item in sampleTargetPool.shuffled().take(5)) {
                    val otherAnswers = sampleTargetPool
                        .filter { it.word != item.word }
                        .shuffled()
                        .take(3)
                        .map { it.definition }

                    val options = (otherAnswers + item.definition).shuffled()
                    questionsList.add(
                        QuizQuestion(
                            wordText = item.word,
                            ipa = item.ipa,
                            correctAnswer = item.definition,
                            options = options
                        )
                    )
                }
            }

            _quizQuestions.value = questionsList
        }
    }

    fun submitQuizAnswer(selectedOption: String) {
        val currentQuestion = _quizQuestions.value.getOrNull(_currentQuizIndex.value) ?: return

        if (selectedOption == currentQuestion.correctAnswer) {
            _quizScore.value++
        }

        if (_currentQuizIndex.value + 1 < _quizQuestions.value.size) {
            _currentQuizIndex.value++
        } else {
            _isQuizFinished.value = true
            // Update studied stats
            viewModelScope.launch {
                repository.updateProgress { current ->
                    current.copy(
                        minutesStudied = current.minutesStudied + 5,
                        totalWordsLearned = current.totalWordsLearned + (_quizScore.value)
                    )
                }
            }
        }
    }

    fun endQuiz() {
        _isQuizStarted.value = false
        _isQuizFinished.value = false
    }

    private fun getLevelDefaultVocabulary(level: String): List<VocabularySample> {
        return when (level) {
            "Beginner" -> listOf(
                VocabularySample("Active", "/ˈæktɪv/", "Năng động, hoạt bát", "She is a very active child."),
                VocabularySample("Journey", "/ˈdʒɜːni/", "Hành trình, chuyến đi", "A long journey by train."),
                VocabularySample("Support", "/səˈpɔːt/", "Hỗ trợ, nâng đỡ", "Thank you for your support."),
                VocabularySample("Vibrant", "/ˈvaɪbrənt/", "Sôi động, đầy sức sống", "New York is a vibrant city."),
                VocabularySample("Establish", "/ɪˈstæblɪʃ/", "Thiết lập, thành lập", "They established the company in 2012."),
                VocabularySample("Cognitive", "/ˈkɒɡnətɪv/", "Thuộc về nhận thức", "Cognitive development in infants.")
            )
            "Advanced" -> listOf(
                VocabularySample("Ubiquity", "/juːˈbɪkwəti/", "Sự có mặt khắp nơi", "The ubiquity of mobile phones is unquestionable."),
                VocabularySample("Ambiguity", "/ˌæmbɪˈɡjuːəti/", "Sự mơ hồ, không rõ ràng", "Avoid ambiguity in legal written documents."),
                VocabularySample("Hydrostatic", "/ˌhaɪdrəʊˈstætɪk/", "Thuộc về thủy tĩnh", "Deep sea creates massive hydrostatic pressure."),
                VocabularySample("Comprehensive", "/ˌkɒmprɪˈhensɪv/", "Toàn diện, bao hàm toàn bộ", "The study offers a comprehensive analysis."),
                VocabularySample("Bioluminescent", "/ˌbaɪəʊˌluːmɪˈnesnt/", "Phát quang sinh học", "Deep sea creatures are often bioluminescent."),
                VocabularySample("Persevere", "/ˌpɜːsɪˈvɪə(r)/", "Kiên trì, bền bỉ", "You must persevere to learn English perfectly.")
            )
            else -> listOf( // Intermediate Default
                VocabularySample("Assist", "/əˈsɪst/", "Hỗ trợ, giúp đỡ", "AI models assist in medical research."),
                VocabularySample("Cognate", "/ˈkɒɡneɪt/", "Cùng nguồn gốc, từ tương ứng", "Languages sharing the same root share cognate vocabulary."),
                VocabularySample("Endeavor", "/ɪnˈdevə(r)/", "Nỗ lực gánh vác, cố gắng", "A long-term cognitive endeavor."),
                VocabularySample("Fatigue", "/fəˈtiːɡ/", "Sự mệt mỏi, suy nhược", "Rote memorization causes mental fatigue."),
                VocabularySample("Reinforce", "/ˌriːɪnˈfɔːs/", "Tăng cường, củng cố", "Gamified tasks reinforce learning habits."),
                VocabularySample("Profound", "/prəˈfaʊnd/", "Sâu sắc, thâm thúy", "The book made a profound impact on my perspective.")
            )
        }
    }
}

data class QuizQuestion(
    val wordText: String,
    val ipa: String,
    val correctAnswer: String,
    val options: List<String>
)

data class VocabularySample(
    val word: String,
    val ipa: String,
    val definition: String,
    val example: String
)

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
