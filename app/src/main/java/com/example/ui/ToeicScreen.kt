package com.example.ui

import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.api.GeminiRetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ToeicScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedFeatureIndex by remember { mutableStateOf(-1) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        if (selectedFeatureIndex == -1) {
            ToeicHubDashboard(
                onSelectFeature = { selectedFeatureIndex = it }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header returning to hub
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedFeatureIndex = -1 },
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = when (selectedFeatureIndex) {
                            0 -> "Luyện tập Part 5"
                            1 -> "Từ vựng TOEIC Thiết yếu"
                            2 -> "Trợ lý TOEIC AI"
                            3 -> "Thi thử TOEIC Rút gọn"
                            else -> "Ôn thi TOEIC"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedFeatureIndex) {
                        0 -> ToeicPart5Screen(viewModel)
                        1 -> ToeicVocabularyScreen(viewModel)
                        2 -> ToeicAiTutorScreen()
                        3 -> ToeicMockTestScreen(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ToeicHubDashboard(
    onSelectFeature: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "Luyện Thi TOEIC AI 🎯",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bứt phá điểm số TOEIC của bạn với 4 công cụ ôn luyện tối ưu kết hợp Trợ lý Trí tuệ Nhân tạo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            ToeicFeatureCard(
                title = "1. Luyện tập Part 5 Ngữ pháp",
                description = "Tổng hợp ngân hàng câu hỏi điền từ vào câu TOEIC kèm đáp án giải thích ngữ pháp chi tiết tiếng Việt.",
                iconEmoji = "✍️",
                actionLabel = "Bắt đầu luyện",
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = { onSelectFeature(0) }
            )
        }

        item {
            ToeicFeatureCard(
                title = "2. Từ vựng TOEIC Thiết yếu",
                description = "Chuyên đề 5 chủ đề từ vựng văn phòng, kinh doanh, tài chính phổ biến nhất kèm phát âm bản xứ chuẩn TTS.",
                iconEmoji = "📚",
                actionLabel = "Học từ vựng",
                iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { onSelectFeature(1) }
            )
        }

        item {
            ToeicFeatureCard(
                title = "3. Trợ lý Giải đề TOEIC AI",
                description = "Hỏi AI phân tích bất kỳ cấu trúc, ngữ pháp, mẫu câu khó nào hoặc yêu cầu AI tạo câu hỏi mới tức thì.",
                iconEmoji = "🤖",
                actionLabel = "Hỏi gia sư AI",
                iconContainerColor = Color(0xFFFFECEF),
                onClick = { onSelectFeature(2) }
            )
        }

        item {
            ToeicFeatureCard(
                title = "4. Đề thi thử Rút gọn (Mini Mock Test)",
                description = "Bài thi thử 15 câu tổng hợp áp lực thời gian thực giúp bạn tự tin căn giờ trước kỳ thi thật.",
                iconEmoji = "⏱️",
                actionLabel = "Bắt đầu thi thử",
                iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                onClick = { onSelectFeature(3) }
            )
        }
    }
}

@Composable
fun ToeicFeatureCard(
    title: String,
    description: String,
    iconEmoji: String,
    actionLabel: String,
    iconContainerColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(color = iconContainerColor, shape = RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = iconEmoji, fontSize = 28.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ==========================================
// FEATURE 1: TOEIC PART 5 PRACTICE SCREEN
// ==========================================
data class Part5Question(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val category: String
)

@Composable
fun ToeicPart5Screen(viewModel: MainViewModel) {
    val questions = remember {
        listOf(
            Part5Question(
                question = "The marketing manager decided to _______ the presentation materials until later in the afternoon.",
                options = listOf("postpone", "postponing", "postponed", "postpones"),
                correctIndex = 0,
                explanation = "Sau cấu trúc 'decide to + Verb-infinitive' cần chọn động từ nguyên mẫu không chia. 'Postpone' (trì hoãn) là động từ nguyên mẫu phù hợp nhất.",
                category = "Động từ định dạng (Verb Form)"
            ),
            Part5Question(
                question = "The executive board unanimously _______ the new strategic plan proposed by the technology consultant.",
                options = listOf("approve", "approved", "approving", "approval"),
                correctIndex = 1,
                explanation = "Câu đã có chủ ngữ lớn là 'The executive board' và chưa có động từ chính chia thì cho câu. 'Approved' chia ở thì quá khứ đơn đóng vai trò vị ngữ tuyệt vời.",
                category = "Thì của động từ (Verb Tenses)"
            ),
            Part5Question(
                question = "Our department must complete the inventory review because the audit is scheduled _______ next Wednesday morning.",
                options = listOf("at", "on", "for", "with"),
                correctIndex = 2,
                explanation = "Giới từ phù hợp đi cùng 'scheduled' chỉ mục đích thời gian trong tiếng Anh là 'scheduled for + time' (được lên lịch vào lúc…).",
                category = "Giới từ (Prepositions)"
            ),
            Part5Question(
                question = "The shipping department packaged the fragile crystal glassware _______ to guarantee it would not crack during transit.",
                options = listOf("careful", "carefulness", "carefully", "more careful"),
                correctIndex = 2,
                explanation = "Để bổ nghĩa cho động từ chính 'packaged' (đóng gói), ta cần sử dụng một Trạng từ chỉ cách thức (Adverb). Vì vậy 'carefully' (một cách cẩn thận) là đáp án đúng.",
                category = "Trạng từ và Tính từ (Modifiers)"
            ),
            Part5Question(
                question = "Ms. Alvarez negotiated the contract terms very _______, ensuring a massive discount on bulk raw material supplies.",
                options = listOf("skillful", "skillfulness", "skilled", "skillfully"),
                correctIndex = 3,
                explanation = "Tương tự, sau động từ 'negotiated' bổ ngữ là trạng từ 'skillfully' đứng cuối để diễn đạt hành động đàm phán một cách khéo léo.",
                category = "Loại từ (Word Form)"
            ),
            Part5Question(
                question = "The legal team reviewed the contract thoroughly, _______ that all terms of agreement comply with regional laws.",
                options = listOf("ensure", "ensuring", "ensured", "ensures"),
                correctIndex = 1,
                explanation = "Sử dụng phân từ hiện tại (present participle) dạng rút gọn mệnh đề quan hệ chủ động: ', ensuring that...' có nghĩa là 'đồng thời đảm bảo rằng...'.",
                category = "Rút gọn mệnh đề (Participles)"
            ),
            Part5Question(
                question = "Please submit your completed mileage reimbursement forms _______ to the accounting supervisor by Friday noon.",
                options = listOf("directly", "direct", "direction", "directed"),
                correctIndex = 0,
                explanation = "Để bổ ngữ cho hành động hành vi 'submit directly to' (gửi trực tiếp tới ai đó), ta chọn Trạng từ 'directly'.",
                category = "Học Trạng từ (Adverbs)"
            ),
            Part5Question(
                question = "Employees who participate in the advanced language workshop are eligible _______ full financial assistance.",
                options = listOf("for", "to", "with", "by"),
                correctIndex = 0,
                explanation = "Cấu trúc cố định rất hay xuất hiện trong TOEIC: 'be eligible for something' (đủ điều kiện, có quyền nhận cái gì đó).",
                category = "Giới từ cố định (Collocations)"
            )
        )
    }

    var currentIndex by remember { mutableStateOf(0) }
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var showResults by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!showResults) {
            val q = questions[currentIndex]

            // Question status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Chủ đề: ${q.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Câu ${currentIndex + 1}/${questions.size}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Question Box card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                )
            ) {
                Text(
                    text = q.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(20.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Choices list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                q.options.forEachIndexed { index, option ->
                    val isSelected = selectedAnswerIndex == index
                    val isCorrectChoice = q.correctIndex == index

                    val containerColor = when {
                        isAnswered && isCorrectChoice -> Color(0xFFD4EDDA) // Green for correct
                        isAnswered && isSelected && !isCorrectChoice -> Color(0xFFF8D7DA) // Red for wrong selected
                        isSelected -> MaterialTheme.colorScheme.primaryContainer // Blue selected
                        else -> MaterialTheme.colorScheme.surface
                    }

                    val borderColor = when {
                        isAnswered && isCorrectChoice -> Color(0xFF28A745)
                        isAnswered && isSelected && !isCorrectChoice -> Color(0xFFDC3545)
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }

                    val textColor = when {
                        isAnswered && isCorrectChoice -> Color(0xFF155724)
                        isAnswered && isSelected && !isCorrectChoice -> Color(0xFF721C24)
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    val optionLetter = when (index) {
                        0 -> "A"
                        1 -> "B"
                        2 -> "C"
                        else -> "D"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isAnswered) {
                                selectedAnswerIndex = index
                            },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, borderColor),
                        colors = CardDefaults.cardColors(containerColor = containerColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Letter indicator
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = optionLetter,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )

                            // Status Icons
                            if (isAnswered) {
                                if (isCorrectChoice) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Đúng",
                                        tint = Color(0xFF28A745)
                                    )
                                } else if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Sai",
                                        tint = Color(0xFFDC3545)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Button showing check or next
            AnimatedVisibility(visible = selectedAnswerIndex != null) {
                Button(
                    onClick = {
                        if (!isAnswered) {
                            isAnswered = true
                            if (selectedAnswerIndex == q.correctIndex) {
                                score++
                                viewModel.incrementWordsLearned(1)
                            }
                            viewModel.addMinutesStudied(1)
                        } else {
                            // Go to next
                            if (currentIndex < questions.size - 1) {
                                currentIndex++
                                selectedAnswerIndex = null
                                isAnswered = false
                            } else {
                                showResults = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (!isAnswered) "KIỂM TRA ĐÁP ÁN" else if (currentIndex < questions.size - 1) "CÂU TIẾP THEO" else "XEM KẾT QUẢ",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Explanation Card
            AnimatedVisibility(visible = isAnswered) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Giải thích chi tiết",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = q.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Results Panel
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🎉", fontSize = 80.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Hoàn Thành Luyện Tập!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Số câu đúng: $score / ${questions.size}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Linear rating indicator
                LinearProgressIndicator(
                    progress = { score.toFloat() / questions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .height(8.dp)
                        .clip(CircleShape)
                )

                Text(
                    text = if (score >= 6) "Thật tuyệt vời! Kiến thức ngữ pháp của bạn cực kỳ vững chắc." else "Khá tốt! Hãy luyện thêm để tối ưu phản xạ câu hỏi ngắn nhé.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        currentIndex = 0
                        selectedAnswerIndex = null
                        isAnswered = false
                        score = 0
                        showResults = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("LUYỆN TẬP LẠI", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// FEATURE 2: TOEIC ESSENTIAL VOCABULARY
// ==========================================
data class ToeicVocabWord(
    val word: String,
    val pos: String,
    val ipa: String,
    val definition: String,
    val translation: String,
    val exampleEn: String,
    val exampleVi: String
)

data class ToeicVocabTopic(
    val title: String,
    val emoji: String,
    val list: List<ToeicVocabWord>
)

@Composable
fun ToeicVocabularyScreen(viewModel: MainViewModel) {
    val topics = remember {
        listOf(
            ToeicVocabTopic(
                title = "Đời sống Văn phòng (Office Life)",
                emoji = "🏢",
                list = listOf(
                    ToeicVocabWord("collaborate", "v", "/kəˈlæb.ə.reɪt/", "to work jointly with others on a task", "Hợp tác, cộng tác", "The researchers collaborate with colleagues abroad.", "Các nhà nghiên cứu cộng tác với những đồng nghiệp nước ngoài."),
                    ToeicVocabWord("mandate", "n, v", "/ˈmæn.deɪt/", "an official order or commission to do something", "Mệnh lệnh, chỉ thị / ủy thác", "All staff members must comply with the new safety mandate.", "Tất cả nhân viên phải tuân thủ chỉ thị an toàn mới."),
                    ToeicVocabWord("implement", "v", "/ˈɪm.plɪ.ment/", "put (a decision, plan, agreement) into effect", "Triển khai, thực thi", "The board voted to implement the plan immediately.", "Hội đồng quản trị đã biểu quyết triển khai kế hoạch ngay lập tức."),
                    ToeicVocabWord("agenda", "n", "/əˈdʒen.də/", "a list of items to be discussed at a formal meeting", "Chương trình nghị sự, lịch trình", "The issue of salary increases is on the agenda.", "Vấn đề tăng lương có trong chương trình nghị sự."),
                    ToeicVocabWord("delegate", "v, n", "/ˈdel.ɪ.ɡət/", "entrust (a task or responsibility) to another person", "Phân công, ủy thác / người đại diện", "Effective managers know how to delegate duties.", "Những người quản lý hiệu quả biết cách ủy thác nhiệm vụ.")
                )
            ),
            ToeicVocabTopic(
                title = "Tiếp thị & Bán hàng (Marketing)",
                emoji = "📢",
                list = listOf(
                    ToeicVocabWord("consume", "v", "/kənˈsjuːm/", "use up (resources or materials)", "Tiêu thụ, tiêu dùng", "Efficient lights consume 70% less energy.", "Bóng đèn hiệu suất cao tiêu thụ ít hơn 70% điện năng."),
                    ToeicVocabWord("retail", "n, v", "/ˈriː.teɪl/", "the sale of goods to the public in small quantities", "Bán lẻ", "Many retail stores offer massive discounts in winter.", "Nhiều cửa hàng bán lẻ có những chương trình giảm giá lớn vào mùa đông."),
                    ToeicVocabWord("compete", "v", "/kəmˈpiːk/", "strive to gain or win something by defeating others", "Cạnh tranh, thi đua", "Small businesses must compete with massive hypermarkets.", "Các doanh nghiệp nhỏ phải cạnh tranh với những đại siêu thị khổng lồ."),
                    ToeicVocabWord("evaluate", "v", "/ɪˈvæl.ju.eɪt/", "form an idea of the amount, number, or value of", "Đánh giá, định giá", "Researchers must evaluate the effectiveness of the campaign.", "Các nhà nghiên cứu phải đánh giá hiệu quả của chiến dịch."),
                    ToeicVocabWord("launch", "v, n", "/lɔːntʃ/", "start or set in motion (an activity or enterprise)", "Ra mắt, khởi chạy / buổi lễ ra mắt", "We plan to launch our new product line early next month.", "Chúng tôi dự định ra mắt dòng sản phẩm mới vào đầu tháng tới.")
                )
            ),
            ToeicVocabTopic(
                title = "Hợp đồng & Tài chính (Contracts)",
                emoji = "✍️",
                list = listOf(
                    ToeicVocabWord("comply", "v", "/kəmˈplaɪ/", "act in accordance with a wish or command", "Tuân thủ, làm theo", "The company succeeded because they comply with local laws.", "Công ty đã thành công vì họ tuân thủ luật pháp địa phương."),
                    ToeicVocabWord("negotiate", "v", "/nɪˈɡəʊ.ʃi.eɪt/", "try to reach an agreement or compromise by discussion", "Thương lượng, đàm phán", "We managed to negotiate a lower deposit amount.", "Chúng tôi đã thương lượng thành công khoản tiền đặt cọc thấp hơn."),
                    ToeicVocabWord("generate", "v", "/ˈdʒen.ə.reɪt/", "produce or create something", "Tạo ra, đem lại (lợi nhuận/doanh thu)", "The advertisement helped to generate high sales numbers.", "Quảng cáo đã giúp mang lại doanh số bán hàng cao."),
                    ToeicVocabWord("asset", "n", "/ˈæs.et/", "a useful or valuable quality, person, or thing", "Tài sản, vốn quý", "Her fluent English accent is a massive asset to our team.", "Giọng Anh lưu loát của cô ấy là tài sản lớn cho đội ngũ của chúng tôi."),
                    ToeicVocabWord("audit", "n, v", "/ˈɔː.dɪt/", "an official inspection of an organization's accounts", "Kiểm toán, thanh tra kiểm tra", "An independent firm conducted a complete financial audit.", "Một công ty độc lập đã tiến hành cuộc kiểm toán tài chính toàn diện.")
                )
            ),
            ToeicVocabTopic(
                title = "Du lịch & Công tác (Travel)",
                emoji = "✈️",
                list = listOf(
                    ToeicVocabWord("accommodate", "v", "/əˈkɒm.ə.deɪt/", "provide lodging or sufficient space for", "Đáp ứng chỗ ở, chứa được", "The historic hotel can accommodate up to five hundred guests.", "Khách sạn cổ kính có thể chứa tối đa 500 khách."),
                    ToeicVocabWord("itinerary", "n", "/aɪˈtɪn.ər.ər.i/", "a planned route or journey", "Lộ trình, lịch trình chuyến đi", "We will send your flight itinerary via email.", "Chúng tôi sẽ gửi lịch trình chuyến bay của bạn qua email."),
                    ToeicVocabWord("transit", "n, v", "/ˈtræn.zɪt/", "the carrying of people or goods from one place to another", "Quá cảnh, sự vận chuyển", "The delicate supplies were delayed in transit.", "Hàng tiếp tế dễ hỏng đã bị trì hoãn trong quá trình vận chuyển."),
                    ToeicVocabWord("terminal", "n", "/ˈtɜː.mɪ.nəl/", "a departure and arrival building at an airport", "Ga sân bay, nhà ga hành khách", "Flight 427 leaves from terminal three.", "Chuyến bay 427 xuất phát từ nhà ga số 3."),
                    ToeicVocabWord("embark", "v", "/ɪmˈbɑːk/", "go on board a ship, aircraft, or other vehicle", "Lên tàu, lên máy bay", "Passengers are waiting under protection to embark the ship.", "Hành khách đang đợi ở sảnh để chuẩn bị lên tàu.")
                )
            )
        )
    }

    var selectedTopic by remember { mutableStateOf<ToeicVocabTopic?>(null) }
    val context = LocalContext.current
    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    // Initialize Android Speech TTS API
    DisposableEffect(Unit) {
        val speech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        tts = speech
        onDispose {
            speech.shutdown()
        }
    }

    if (selectedTopic == null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Chuyên Đề Từ Vựng TOEIC",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(topics) { topic ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTopic = topic },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = topic.emoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${topic.list.size} từ vựng cốt lõi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Xem",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Context header for current topic
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedTopic = null }) {
                    Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Trở về")
                }
                Text(
                    text = selectedTopic!!.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(selectedTopic!!.list) { v ->
                    var isMemorized by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMemorized) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = v.word,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "(${v.pos})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Text(
                                        text = v.ipa,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Speak TTS Button
                                    IconButton(
                                        onClick = {
                                            tts?.speak(v.word, TextToSpeech.QUEUE_FLUSH, null, null)
                                        },
                                        modifier = Modifier.background(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = CircleShape
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Phát âm English",
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    
                                    // Mark as learned button
                                    IconButton(
                                        onClick = {
                                            if (!isMemorized) {
                                                isMemorized = true
                                                viewModel.incrementWordsLearned(1)
                                                viewModel.addMinutesStudied(1)
                                            }
                                        },
                                        modifier = Modifier.background(
                                            color = if (isMemorized) Color(0xFFD4EDDA) else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = CircleShape
                                        )
                                    ) {
                                        Icon(
                                            imageVector = if (isMemorized) Icons.Default.Check else Icons.Outlined.Check,
                                            contentDescription = "Thuộc từ",
                                            tint = if (isMemorized) Color(0xFF28A745) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Nghĩa: ${v.translation}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Định nghĩa Anh: ${v.definition}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Example Panel
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Ví dụ: ${v.exampleEn}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = v.exampleVi,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// FEATURE 3: AI TOEIC TUTOR SCREEN
// ==========================================
@Composable
fun ToeicAiTutorScreen() {
    var queryText by remember { mutableStateOf("") }
    var aiResponse by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val suggestedPrompts = remember {
        listOf(
            "Phân biệt mẫu câu Affect và Effect trong đề thi TOEIC cô đọng nhất.",
            "Cho tôi 1 ví dụ câu hỏi TOEIC điền từ Part 5 kèm lời giải chi tiết bằng tiếng Việt.",
            "Giải thích cấu trúc 'Be eligible for' và 'Be eligible to' kèm ví dụ."
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🤖", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Tôi là gia sư TOEIC AI của bạn! Hãy hỏi tôi phân tích bất kì ngữ pháp tiếng Anh phức tạp nào.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item {
            Column {
                Text(
                    text = "Gợi ý câu hỏi phổ biến:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    suggestedPrompts.forEach { prompt ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    queryText = prompt
                                    isLoading = true
                                    scope.launch {
                                        aiResponse = GeminiRetrofitClient.generateResponse(
                                            prompt = prompt,
                                            systemInstruction = "You are a specialized and highly professional academic TOEIC exam companion and grammar teacher who explains English lessons clearly in Vietnamese.",
                                            isJson = false
                                        )
                                        isLoading = false
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    label = { Text("Nhập nội dung cần giải đáp...") },
                    placeholder = { Text("Ví dụ: Phân biệt cấu trúc Unless vs If not...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (queryText.isNotEmpty() && !isLoading) {
                            IconButton(onClick = { queryText = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )

                Button(
                    onClick = {
                        if (queryText.trim().isNotEmpty()) {
                            isLoading = true
                            scope.launch {
                                aiResponse = GeminiRetrofitClient.generateResponse(
                                    prompt = queryText,
                                    systemInstruction = "You are an expert TOEIC academic English teacher and consultant who explains complex patterns or mock sentences in flawless Vietnamese.",
                                    isJson = false
                                )
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = queryText.trim().isNotEmpty() && !isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("YÊU CẦU GIẢI ĐÁP BỞI AI", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (aiResponse.isNotEmpty() || isLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Phân tích từ TOEIC AI Coach & Tutor",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isLoading) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "AI đang phân tích ngữ nghĩa và kết cấu đề thi...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = aiResponse,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// FEATURE 4: TOEIC SHORT MOCK TEST
// ==========================================
data class MockTestQuestion(
    val type: String, // "part5" or "part7"
    val passage: String? = null,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val motivation: String
)

@Composable
fun ToeicMockTestScreen(viewModel: MainViewModel) {
    val examQuestions = remember {
        listOf(
            MockTestQuestion(
                type = "part5",
                question = "The board of directors agreed _______ the environmental policy regulations at the next meeting.",
                options = listOf("approve", "approving", "to approve", "approval"),
                correctIndex = 2,
                motivation = "Cấu trúc 'agree + to Verb' (đồng ý làm việc gì đó)."
            ),
            MockTestQuestion(
                type = "part5",
                question = "Most analysts predict that the merger with Dynasty Tech will boost stock values _______.",
                options = listOf("significant", "significance", "signifying", "significantly"),
                correctIndex = 3,
                motivation = "Cần một trạng từ 'significantly' (một cách đáng kể) đứng ở cuối để bổ nghĩa cho động từ 'boost'."
            ),
            MockTestQuestion(
                type = "part5",
                question = "All prospective employees should fill out their medical screening forms as _______ as possible.",
                options = listOf("quick", "quickly", "quickness", "more quick"),
                correctIndex = 1,
                motivation = "Mẫu câu so sánh bằng 'as... as possible' kẹp trạng từ 'quickly' bổ nghĩa cho động từ 'fill out'."
            ),
            MockTestQuestion(
                type = "part5",
                question = "Due to the unexpected storm, the flight dispatch department must _______ evaluate airport safety guidelines.",
                options = listOf("continuous", "continuously", "continuation", "continue"),
                correctIndex = 1,
                motivation = "Trạng từ 'continuously' bổ nghĩa cho động từ hành vi 'evaluate' đứng phía sau."
            ),
            MockTestQuestion(
                type = "part5",
                question = "The maintenance crew had to shut down the cooling system temporarily _______ a technical problem.",
                options = listOf("because of", "owing to", "in spite of", "whereas"),
                correctIndex = 0,
                motivation = "'Because of' dùng kèm danh từ 'a technical problem' thể hiện quan hệ nguyên nhân - kết quả."
            ),
            MockTestQuestion(
                type = "part7",
                passage = "--- MEMORANDUM ---\nTo: All Marketing Representatives\nFrom: Julia Geller, Senior VP of Public Relations\nDate: May 24\nSubject: Business Card Layout Update\n\nPlease submit all requests for new employee business cards by the end of next Monday. The design team has added a new professional modern logo that fits the digital branding transition. Applications sent past the deadline will be delayed until the subsequent month.",
                question = "What is the main purpose of this memorandum?",
                options = listOf("To announce a logo upgrade on business cards", "To fire Julia Geller", "To change office hours on Monday", "To advertise a new customer survey"),
                correctIndex = 0,
                motivation = "Ý nghĩa chính nằm ở tiêu đề và câu đầu: 'Subject: Business Card Layout Update... design team added a new modern logo'."
            ),
            MockTestQuestion(
                type = "part7",
                passage = "--- MEMORANDUM ---\nTo: All Marketing Representatives\nFrom: Julia Geller, Senior VP of Public Relations\nDate: May 24\nSubject: Business Card Layout Update\n\nPlease submit all requests for new employee business cards by the end of next Monday. The design team has added a new professional modern logo that fits the digital branding transition. Applications sent past the deadline will be delayed until the subsequent month.",
                question = "What will happen to employees requesting cards after the deadline next Monday?",
                options = listOf("They are fired immediately", "Their cards request will be shifted to the next month", "They get extra payment", "They must use old paper logos"),
                correctIndex = 1,
                motivation = "Đọc câu cuối: 'Applications sent past the deadline will be delayed until the subsequent month'."
            )
        )
    }

    var isTestStarted by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(0) }
    var selectedValIndex by remember { mutableStateOf<Int?>(null) }
    val userAnswers = remember { mutableStateMapOf<Int, Int>() }
    var isTestFinished by remember { mutableStateOf(false) }
    var timeRemainingSec by remember { mutableStateOf(300) } // 5 minutes test

    // Launch countdown timer
    LaunchedEffect(isTestStarted, isTestFinished) {
        if (isTestStarted && !isTestFinished) {
            while (timeRemainingSec > 0) {
                delay(1000)
                timeRemainingSec--
            }
            isTestFinished = true
        }
    }

    if (!isTestStarted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Đề Thi Thử TOEIC Rút Gọn", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bài thi thử bao gồm các câu hỏi Part 5 và Part 7 Đọc hiểu với thời gian đếm ngược 5 phút giúp tăng tốc độ phản xạ.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { isTestStarted = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("BẮT ĐẦU LÀM BÀI", fontWeight = FontWeight.Bold)
            }
        }
    } else if (!isTestFinished) {
        val q = examQuestions[currentIndex]
        val minutes = timeRemainingSec / 60
        val seconds = timeRemainingSec % 60
        val timerText = String.format("%02d:%02d", minutes, seconds)
        val isTimerLow = timeRemainingSec < 60

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Stats & Timer header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Câu ${currentIndex + 1}/${examQuestions.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Timer badge
                Card(
                     colors = CardDefaults.cardColors(
                         containerColor = if (isTimerLow) Color(0xFFF8D7DA) else MaterialTheme.colorScheme.secondaryContainer
                     ),
                     shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Timer",
                            tint = if (isTimerLow) Color(0xFFDC3545) else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = timerText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isTimerLow) Color(0xFFDC3545) else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Content area (especially for Part 7 passage layout)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (q.type == "part7" && q.passage != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Đoạn Văn Đọc Hiểu (Passage):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = q.passage,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                    ) {
                        Text(
                            text = q.question,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                itemsIndexed(q.options) { index, option ->
                    val isSelected = selectedValIndex == index
                    val optionLetter = when (index) {
                        0 -> "A"
                        1 -> "B"
                        2 -> "C"
                        else -> "D"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedValIndex = index
                                userAnswers[currentIndex] = index
                            },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = optionLetter,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation bar between questions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (currentIndex > 0) {
                            currentIndex--
                            selectedValIndex = userAnswers[currentIndex]
                        }
                    },
                    enabled = currentIndex > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Lùi Lại")
                }

                if (currentIndex < examQuestions.size - 1) {
                    Button(
                        onClick = {
                            currentIndex++
                            selectedValIndex = userAnswers[currentIndex]
                        },
                        enabled = selectedValIndex != null,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Tiếp theo")
                    }
                } else {
                    Button(
                        onClick = {
                            isTestFinished = true
                            viewModel.addMinutesStudied(5)
                        },
                        enabled = userAnswers.size == examQuestions.size,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745), contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("NỘP BÀI THI")
                    }
                }
            }
        }
    } else {
        // Evaluate score
        var correctCount = 0
        examQuestions.forEachIndexed { idx, q ->
            if (userAnswers[idx] == q.correctIndex) {
                correctCount++
            }
        }
        val pct = correctCount.toFloat() / examQuestions.size
        // Simulated Listening & Reading Scale (total max score 990 / scaled score)
        val toeicScaled = (pct * 990).toInt().coerceIn(10, 990)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(text = "🎖️", fontSize = 72.sp)
                    Text(
                        text = "Kết Quả Đề Thi Thử",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ước lượng điểm thi quy đổi TOEIC:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$toeicScaled / 990",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Đúng $correctCount trên tổng số ${examQuestions.size} câu hỏi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Danh sách lời giải đính kèm:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }

                itemsIndexed(examQuestions) { idx, q ->
                    val userSelect = userAnswers[idx] ?: -1
                    val isCorrect = userSelect == q.correctIndex

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isCorrect) Color(0xFF28A745).copy(alpha = 0.4f) else Color(0xFFDC3545).copy(alpha = 0.4f)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect) Color(0xFFD4EDDA).copy(alpha = 0.1f) else Color(0xFFF8D7DA).copy(alpha = 0.1f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Câu hỏi ${idx + 1}:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (isCorrect) Color(0xFF28A745) else Color(0xFFDC3545),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isCorrect) "ĐÚNG" else "SAI",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = q.question,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Lựa chọn của bạn: ${if (userSelect != -1) q.options[userSelect] else "Không trả lời"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCorrect) Color(0xFF28A745) else Color(0xFFDC3545),
                                fontWeight = FontWeight.Bold
                            )
                            if (!isCorrect) {
                                Text(
                                    text = "Đáp án đúng: ${q.options[q.correctIndex]}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF28A745),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Giải thích ngữ pháp: ${q.motivation}",
                                style = MaterialTheme.typography.labelSmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    isTestStarted = false
                    currentIndex = 0
                    selectedValIndex = null
                    userAnswers.clear()
                    isTestFinished = false
                    timeRemainingSec = 300
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("QUAY TRỞ LẠI", fontWeight = FontWeight.Bold)
            }
        }
    }
}
