package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.domain.model.Stage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewQueueLogicTest {

    private fun isEligibleForReview(
        stage: Stage,
        lastReviewedDay: String?,
        nextReviewDay: String?,
        today: String
    ): Boolean {
        // Invariant 1: Same-day lock: A card reviewed today is locked until tomorrow
        if (lastReviewedDay != null && lastReviewedDay == today) {
            return false
        }

        // Invariant 2: Scheduling
        return when (stage) {
            Stage.DAILY -> nextReviewDay == null || nextReviewDay <= today
            Stage.WEEKLY, Stage.MONTHLY -> nextReviewDay == null || nextReviewDay <= today
            Stage.LEARNED -> false // Learned cards do not appear in normal due queue
        }
    }

    @Test
    fun `card reviewed today is locked and ineligible for review`() {
        val today = "2026-10-05"
        val eligible = isEligibleForReview(
            stage = Stage.DAILY,
            lastReviewedDay = today,
            nextReviewDay = today,
            today = today
        )
        assertFalse("Card reviewed today must be locked by same-day lock", eligible)
    }

    @Test
    fun `card reviewed yesterday with nextReviewDay today is eligible`() {
        val today = "2026-10-05"
        val eligible = isEligibleForReview(
            stage = Stage.DAILY,
            lastReviewedDay = "2026-10-04",
            nextReviewDay = "2026-10-05",
            today = today
        )
        assertTrue("Card due today reviewed yesterday must be eligible", eligible)
    }

    @Test
    fun `card with future nextReviewDay is not eligible`() {
        val today = "2026-10-05"
        val eligible = isEligibleForReview(
            stage = Stage.WEEKLY,
            lastReviewedDay = "2026-10-04",
            nextReviewDay = "2026-10-11",
            today = today
        )
        assertFalse("Card with future nextReviewDay must not be in due queue", eligible)
    }

    @Test
    fun `brand new card with no review history is immediately eligible`() {
        val today = "2026-10-05"
        val eligible = isEligibleForReview(
            stage = Stage.DAILY,
            lastReviewedDay = null,
            nextReviewDay = null,
            today = today
        )
        assertTrue("Fresh card must be immediately eligible for review", eligible)
    }

    @Test
    fun `learned card is not in due queue`() {
        val today = "2026-10-05"
        val eligible = isEligibleForReview(
            stage = Stage.LEARNED,
            lastReviewedDay = "2026-09-01",
            nextReviewDay = "2026-10-05",
            today = today
        )
        assertFalse("Learned cards should not be in daily due queue", eligible)
    }
}
