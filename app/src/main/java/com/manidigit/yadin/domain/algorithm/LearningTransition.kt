package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.time.ClockAndDayMath

data class TransitionResult(
    val newStage: Stage,
    val nextReviewDay: String?
)

object LearningTransition {

    fun calculateNextStage(
        currentStage: Stage,
        isCorrect: Boolean,
        todayDayString: String = ClockAndDayMath.todayDayString()
    ): TransitionResult {
        return when (currentStage) {
            Stage.DAILY -> {
                if (isCorrect) {
                    TransitionResult(
                        newStage = Stage.WEEKLY,
                        nextReviewDay = ClockAndDayMath.addDays(todayDayString, 7)
                    )
                } else {
                    TransitionResult(
                        newStage = Stage.DAILY,
                        nextReviewDay = ClockAndDayMath.addDays(todayDayString, 1)
                    )
                }
            }
            Stage.WEEKLY -> {
                if (isCorrect) {
                    TransitionResult(
                        newStage = Stage.MONTHLY,
                        nextReviewDay = ClockAndDayMath.addDays(todayDayString, 30)
                    )
                } else {
                    TransitionResult(
                        newStage = Stage.DAILY,
                        nextReviewDay = ClockAndDayMath.addDays(todayDayString, 1)
                    )
                }
            }
            Stage.MONTHLY -> {
                if (isCorrect) {
                    TransitionResult(
                        newStage = Stage.LEARNED,
                        nextReviewDay = null
                    )
                } else {
                    TransitionResult(
                        newStage = Stage.DAILY,
                        nextReviewDay = ClockAndDayMath.addDays(todayDayString, 1)
                    )
                }
            }
            Stage.LEARNED -> {
                TransitionResult(
                    newStage = Stage.LEARNED,
                    nextReviewDay = null
                )
            }
        }
    }
}
