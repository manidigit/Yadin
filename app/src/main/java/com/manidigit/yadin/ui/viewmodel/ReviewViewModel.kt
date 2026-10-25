package com.manidigit.yadin.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manidigit.yadin.R
import com.manidigit.yadin.data.repository.ReviewFilters
import com.manidigit.yadin.data.repository.ReviewRepository
import com.manidigit.yadin.domain.algorithm.FsrsAlgorithm
import com.manidigit.yadin.domain.algorithm.Sm2Quality
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.QuizQuestion
import com.manidigit.yadin.domain.model.ReviewCard
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewSession
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.SessionStatus
import com.manidigit.yadin.domain.model.SpacedRepetitionAlgorithm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * مدیریت نشست‌های مرور (فلش‌کارت و آزمون ۴گزینه‌ای) - ReviewViewModel
 *
 * مسئولیت‌ها (اصل تک‌مسئولیتی - SRP):
 * - مدیریت چرخه حیات نشست‌های مطالعه (شروع، کارت جاری، پایان، رهاسازی).
 * - ناوبری و سوایپ کارت‌ها در حالت فلش‌کارت.
 * - ارزیابی پاسخ‌های آزمون بر اساس شاخص دقیق گزینه انتخابی (حل ISS-16).
 * - مدیریت خطاها و پایداری کورتین‌ها بدون بلعیدن استثناها (حل ISS-22).
 *
 * @property reviewRepo مخزن عملیات مرور و آزمون
 * @property externalScope دامنه کورتین اختیاری در صورت استفاده به عنوان نماینده در ViewModel ارشد
 * @property application کانتکست اختیاری اپلیکیشن جهت استخراج پیام‌های خطای دوزبانه
 */
class ReviewViewModel(
    val reviewRepo: ReviewRepository,
    private val externalScope: CoroutineScope? = null,
    private val application: Application? = null
) : ViewModel() {

    private val scope: CoroutineScope
        get() = externalScope ?: viewModelScope

    private fun formatError(resId: Int, fallbackPrefix: String, detail: String?): String {
        val msg = detail ?: ""
        return application?.getString(resId, msg) ?: "$fallbackPrefix: $msg"
    }

    // --- وضعیت فیلترها و کاندیداها ---
    private val _setupCandidateCount = MutableStateFlow(0)
    /** تعداد کارت‌های کاندید برای مرور با فیلترهای جاری */
    val setupCandidateCount: StateFlow<Int> = _setupCandidateCount.asStateFlow()

    // --- وضعیت نشست فعال ---
    private val _activeSession = MutableStateFlow<ReviewSession?>(null)
    /** نشست فعال جاری (در صورت وجود) */
    val activeSession: StateFlow<ReviewSession?> = _activeSession.asStateFlow()

    private val _activeAlgorithm = MutableStateFlow(SpacedRepetitionAlgorithm.YADIN)
    /** الگوریتم فعال در نشست جاری (یادین، SM-2 یا FSRS) */
    val activeAlgorithm: StateFlow<SpacedRepetitionAlgorithm> = _activeAlgorithm.asStateFlow()

    private val _activeFsrsRetention = MutableStateFlow(FsrsAlgorithm.DEFAULT_DESIRED_RETENTION)
    /** نرخ هدف ماندگاری حافظه برای الگوریتم FSRS در نشست جاری */
    val activeFsrsRetention: StateFlow<Double> = _activeFsrsRetention.asStateFlow()

    private val _sessionCards = MutableStateFlow<List<ReviewCard>>(emptyList())
    /** کارت‌های فلش‌کارت بارگذاری‌شده برای نشست فعال */
    val sessionCards: StateFlow<List<ReviewCard>> = _sessionCards.asStateFlow()

    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    /** سؤالات آزمون چهارگزینه‌ای بارگذاری‌شده */
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    /** شاخص کارت یا سؤال فعال در نشست */
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    /** وضعیت چرخش کارت در حالت فلش‌کارت */
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    private val _quizSelectedOption = MutableStateFlow<Int?>(null)
    /** شاخص گزینه انتخاب‌شده برای سؤال فعلی در آزمون */
    val quizSelectedOption: StateFlow<Int?> = _quizSelectedOption.asStateFlow()

    private val _quizUserAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    /** نگاشت شاخص هر سؤال به شاخص گزینه انتخاب‌شده توسط کاربر */
    val quizUserAnswers: StateFlow<Map<Int, Int>> = _quizUserAnswers.asStateFlow()

    private val _sessionCorrectCount = MutableStateFlow(0)
    /** تعداد پاسخ‌های درست در نشست جاری */
    val sessionCorrectCount: StateFlow<Int> = _sessionCorrectCount.asStateFlow()

    private val _sessionWrongCount = MutableStateFlow(0)
    /** تعداد پاسخ‌های نادرست در نشست جاری */
    val sessionWrongCount: StateFlow<Int> = _sessionWrongCount.asStateFlow()

    private val _reviewErrorMessage = MutableStateFlow<String?>(null)
    /** پیام خطای آخرین عملیات نشست مرور (جهت نمایش به کاربر) */
    val reviewErrorMessage: StateFlow<String?> = _reviewErrorMessage.asStateFlow()

    private var isSubmittingFlashcard = false
    private var lastSubmitFlashcardJob: Job? = null
    private var lastSubmitQuizJob: Job? = null

    private var countJob: Job? = null

    /**
     * به‌روزرسانی تعداد کلمات واجد شرایط برای فیلترهای تنظیمی کاربر.
     * با لغو کار قبلی (Debounce / Job cancellation) جهت جلوگیری از Race Condition.
     */
    fun updateSetupFilters(filters: ReviewFilters) {
        countJob?.cancel()
        countJob = scope.launch {
            try {
                _setupCandidateCount.value = reviewRepo.countCandidates(filters)
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Ignore cancellation
            } catch (e: Exception) {
                _reviewErrorMessage.value = formatError(R.string.err_count_candidates_fmt, "خطا در شمارش کارت‌های آماده", e.message)
            }
        }
    }

    /**
     * ایجاد و راه‌اندازی نشست مرور بر اساس فیلترهای سفارشی‌شده.
     *
     * @param filters تنظیمات مرحله، دسته‌بندی، جهت و سطح آزمون
     * @param onSessionStarted کالبک اختیاری اعلام نوع نشست جهت تغییر صفحه ناوبری
     */
    fun startFilteredSession(
        filters: ReviewFilters,
        onSessionStarted: ((ReviewMode) -> Unit)? = null
    ) {
        scope.launch {
            try {
                _reviewErrorMessage.value = null
                val resolvedAlgo = when (filters.reviewType) {
                    ReviewType.SM2 -> SpacedRepetitionAlgorithm.SM2
                    ReviewType.FSRS -> SpacedRepetitionAlgorithm.FSRS
                    else -> filters.algorithm
                }
                _activeAlgorithm.value = resolvedAlgo
                _activeFsrsRetention.value = filters.fsrsDesiredRetention
                val session = reviewRepo.createFilteredSession(filters.copy(algorithm = resolvedAlgo))
                _activeSession.value = session
                _currentCardIndex.value = 0
                _isCardFlipped.value = false
                _quizSelectedOption.value = null
                _quizUserAnswers.value = emptyMap()
                _sessionCorrectCount.value = 0
                _sessionWrongCount.value = 0

                if (filters.mode == ReviewMode.FLASHCARD) {
                    val cards = reviewRepo.fetchCardsForSession(session.id)
                    _sessionCards.value = cards
                    onSessionStarted?.invoke(ReviewMode.FLASHCARD)
                } else {
                    val questions = reviewRepo.generateQuizQuestions(session.id)
                    if (questions.isEmpty()) {
                        // Fallback to flashcard if insufficient distractors in database (Spec 6.6 & Decision D0.2)
                        val cards = reviewRepo.fetchCardsForSession(session.id)
                        _sessionCards.value = cards
                        onSessionStarted?.invoke(ReviewMode.FLASHCARD)
                    } else {
                        _quizQuestions.value = questions
                        onSessionStarted?.invoke(ReviewMode.QUIZ)
                    }
                }
            } catch (e: Exception) {
                _reviewErrorMessage.value = formatError(R.string.err_start_session_fmt, "خطا در شروع نشست مرور", e.message)
            }
        }
    }

    /**
     * شروع سریع نشست مرور با پارامترهای پیش‌فرض یا انتخابی.
     */
    fun startSession(
        type: ReviewType,
        mode: ReviewMode,
        direction: CardDirection = CardDirection.NORMAL,
        quizLevel: QuizLevel = QuizLevel.NORMAL,
        limit: Int = 20,
        onSessionStarted: ((ReviewMode) -> Unit)? = null
    ) {
        startFilteredSession(
            ReviewFilters(
                reviewType = type,
                mode = mode,
                direction = direction,
                quizLevel = quizLevel,
                maxCards = limit
            ),
            onSessionStarted = onSessionStarted
        )
    }

    /**
     * چرخاندن کارت در حالت فلش‌کارت (پشت‌ورو کردن).
     */
    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    /**
     * ثبت پاسخ فلش‌کارت (یادم بود / فراموش کردم) با مدیریت همزمانی و جلوگیری از دابل‌تپ.
     *
     * @param isCorrect درست یا نادرست بودن پاسخ کاربر
     * @param difficultyThreshold آستانه سختی فعال
     * @param onSessionCompleted کالبک فراخوانی‌شده پس از رسیدن به آخرین کارت
     */
    fun submitFlashcardAnswer(
        isCorrect: Boolean,
        difficultyThreshold: Int = 3,
        sm2Quality: Sm2Quality? = null,
        onAnswerRecorded: (() -> Unit)? = null,
        onSessionCompleted: (() -> Unit)? = null
    ) {
        if (isSubmittingFlashcard) return
        val session = _activeSession.value ?: return
        val cards = _sessionCards.value
        val idx = _currentCardIndex.value
        if (idx >= cards.size) return

        isSubmittingFlashcard = true
        val currentCard = cards[idx]
        val effectiveIsCorrect = sm2Quality?.isCorrect ?: isCorrect
        val attemptId = "${session.id}_${currentCard.conceptId}_${currentCard.direction}_$idx"
        lastSubmitFlashcardJob = scope.launch {
            try {
                _reviewErrorMessage.value = null
                val submitRes = reviewRepo.submitAnswer(
                    sessionId = session.id,
                    conceptId = currentCard.conceptId,
                    direction = currentCard.direction,
                    isCorrect = effectiveIsCorrect,
                    mode = ReviewMode.FLASHCARD,
                    difficultyThreshold = difficultyThreshold,
                    reviewAttemptId = attemptId,
                    sm2Quality = sm2Quality,
                    algorithm = _activeAlgorithm.value,
                    fsrsDesiredRetention = _activeFsrsRetention.value
                )

                val currentCardsList = _sessionCards.value
                if (idx in currentCardsList.indices) {
                    val updatedList = currentCardsList.toMutableList()
                    updatedList[idx] = updatedList[idx].copy(
                        stage = submitRes.newStage,
                        difficulty = submitRes.newDifficulty,
                        consecutiveCorrect = submitRes.consecutiveCorrect,
                        consecutiveWrong = submitRes.consecutiveWrong
                    )
                    _sessionCards.value = updatedList
                }

                if (effectiveIsCorrect) {
                    _sessionCorrectCount.value += 1
                } else {
                    _sessionWrongCount.value += 1
                }

                onAnswerRecorded?.invoke()

                if (idx + 1 < cards.size) {
                    _currentCardIndex.value = idx + 1
                    _isCardFlipped.value = false
                } else {
                    reviewRepo.completeSession(session.id)
                    withContext(Dispatchers.Main) {
                        onSessionCompleted?.invoke()
                    }
                }
            } catch (e: Exception) {
                _reviewErrorMessage.value = formatError(R.string.err_submit_flashcard_fmt, "خطا در ثبت پاسخ فلش‌کارت", e.message)
            } finally {
                isSubmittingFlashcard = false
            }
        }
    }

    /**
     * ثبت پاسخ در آزمون چهارگزینه‌ای.
     *
     * حل نقص بحرانی ISS-16:
     * نمره‌دهی صرفاً بر اساس انطباق ایندکس گزینه انتخاب‌شده با ایندکس گزینه صحیح انجام می‌شود
     * (`optionIndex == q.correctIndex`) و ریشه‌یابی نامطمئن معنایی به طور کامل حذف شده است.
     *
     * @param optionIndex شاخص گزینه کلیک‌شده (۰ تا ۳)
     * @param difficultyThreshold آستانه سختی فعال
     */
    fun submitQuizAnswer(
        optionIndex: Int,
        difficultyThreshold: Int = 3,
        onAnswerRecorded: (() -> Unit)? = null
    ) {
        val idx = _currentCardIndex.value
        if (_quizUserAnswers.value.containsKey(idx)) return // پاسخ قبلاً ثبت شده است

        val session = _activeSession.value ?: return
        val questions = _quizQuestions.value
        if (idx >= questions.size) return

        val q = questions[idx]

        // تصمیم استاندارد معماری D24: صحت فقط با تطابق مستقیم شاخص گزینه ارزیابی می‌شود
        val isCorrect = (optionIndex == q.correctIndex)

        val previousSelectedOption = _quizSelectedOption.value
        val previousUserAnswers = _quizUserAnswers.value

        _quizSelectedOption.value = optionIndex
        _quizUserAnswers.value = previousUserAnswers + (idx to optionIndex)

        val serializedOptions = runCatching {
            JSONArray(q.options.map { it.text }).toString()
        }.getOrNull()

        val attemptId = "${session.id}_${q.conceptId}_${q.direction}_$idx"
        lastSubmitQuizJob = scope.launch {
            try {
                _reviewErrorMessage.value = null
                val submitRes = reviewRepo.submitAnswer(
                    sessionId = session.id,
                    conceptId = q.conceptId,
                    direction = q.direction,
                    isCorrect = isCorrect,
                    mode = ReviewMode.QUIZ,
                    selectedIndex = optionIndex,
                    correctIndex = q.correctIndex,
                    optionsJson = serializedOptions,
                    difficultyThreshold = difficultyThreshold,
                    reviewAttemptId = attemptId,
                    algorithm = _activeAlgorithm.value,
                    fsrsDesiredRetention = _activeFsrsRetention.value
                )
                val currentQuestionsList = _quizQuestions.value
                if (idx in currentQuestionsList.indices) {
                    val updatedList = currentQuestionsList.toMutableList()
                    updatedList[idx] = updatedList[idx].copy(
                        stage = submitRes.newStage,
                        difficulty = submitRes.newDifficulty,
                        consecutiveCorrect = submitRes.consecutiveCorrect,
                        consecutiveWrong = submitRes.consecutiveWrong
                    )
                    _quizQuestions.value = updatedList
                }
                // حل نقص ISS-90: هماهنگ‌سازی زمان‌بندی افزایش شمارنده‌ها دقیقاً پس از ثبت موفق در هر دو حالت
                if (isCorrect) {
                    _sessionCorrectCount.value += 1
                } else {
                    _sessionWrongCount.value += 1
                }
                onAnswerRecorded?.invoke()
            } catch (e: Exception) {
                // Rollback optimistic UI selection if database persistence fails
                if (_currentCardIndex.value == idx) {
                    _quizSelectedOption.value = previousSelectedOption
                }
                _quizUserAnswers.value = previousUserAnswers
                _reviewErrorMessage.value = formatError(R.string.err_submit_quiz_fmt, "خطا در ثبت پاسخ آزمون", e.message)
            }
        }
    }

    /**
     * رفتن به سؤال بعدی آزمون یا اتمام نشست در صورت رسیدن به انتها.
     *
     * حل نقص ISS-41:
     * در سوال پایانی، با انتظار برای تکمیل lastSubmitQuizJob تضمین می‌شود که ثبت پاسخ
     * و تغییرات دیتابیس قبل از فراخوانی completeSession نهایی شده و ریس وضعیتی رخ ندهد.
     */
    fun nextQuizQuestion(onSessionCompleted: (() -> Unit)? = null) {
        val questions = _quizQuestions.value
        val idx = _currentCardIndex.value
        val session = _activeSession.value

        if (idx + 1 < questions.size) {
            val nextIdx = idx + 1
            _currentCardIndex.value = nextIdx
            _quizSelectedOption.value = _quizUserAnswers.value[nextIdx]
        } else {
            session?.let {
                scope.launch {
                    try {
                        // حل نقص بحرانی ISS-41: منتظر پایان کار ثبت آخرین پاسخ می‌مانیم
                        lastSubmitQuizJob?.join()
                        reviewRepo.completeSession(it.id)
                        withContext(Dispatchers.Main) {
                            onSessionCompleted?.invoke()
                        }
                    } catch (e: Exception) {
                        _reviewErrorMessage.value = formatError(R.string.err_complete_session_fmt, "خطا در اتمام نشست آزمون", e.message)
                        withContext(Dispatchers.Main) {
                            onSessionCompleted?.invoke()
                        }
                    }
                }
            } ?: run {
                onSessionCompleted?.invoke()
            }
        }
    }

    /**
     * بازگشت به سؤال قبلی در آزمون.
     */
    fun previousQuizQuestion() {
        val idx = _currentCardIndex.value
        if (idx > 0) {
            val prevIdx = idx - 1
            _currentCardIndex.value = prevIdx
            _quizSelectedOption.value = _quizUserAnswers.value[prevIdx]
        }
    }

    /**
     * پرش مستقیم به شاخص دلخواه در آزمون از روی نوار شاخص سوالات.
     */
    fun goToQuizQuestion(targetIndex: Int) {
        val questions = _quizQuestions.value
        if (targetIndex in questions.indices) {
            _currentCardIndex.value = targetIndex
            _quizSelectedOption.value = _quizUserAnswers.value[targetIndex]
        }
    }

    /**
     * خروج اضطراری و رهاسازی نشست فعال در دیتابیس.
     */
    fun exitSession(onExited: (() -> Unit)? = null) {
        val session = _activeSession.value
        if (session != null && session.status == SessionStatus.ACTIVE) {
            scope.launch {
                try {
                    lastSubmitQuizJob?.join()
                    lastSubmitFlashcardJob?.join()
                    reviewRepo.abandonSession(session.id)
                } catch (e: Exception) {
                    _reviewErrorMessage.value = formatError(R.string.err_abandon_session_fmt, "خطا در لغو نشست", e.message)
                }
            }
        }
        _activeSession.value = null
        isSubmittingFlashcard = false
        onExited?.invoke()
    }

    /**
     * پاکسازی پیام خطا پس از نمایش توسط اسنک‌بار یا دیالوگ.
     */
    fun clearErrorMessage() {
        _reviewErrorMessage.value = null
    }
}
