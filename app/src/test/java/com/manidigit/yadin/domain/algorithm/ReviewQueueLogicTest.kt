package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.data.repository.ReviewRepository
import com.manidigit.yadin.domain.model.Stage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

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

    @Test
    fun `resolveOppositeProgressTier classifies learned, practiced, and new words in exact priority order`() {
        assertEquals(
            ReviewRepository.REVERSE_PRIORITY_TIER_LEARNED,
            ReviewRepository.resolveOppositeProgressTier(Stage.LEARNED, "2026-10-01")
        )
        assertEquals(
            ReviewRepository.REVERSE_PRIORITY_TIER_LEARNED,
            ReviewRepository.resolveOppositeProgressTier(Stage.LEARNED, null)
        )
        assertEquals(
            ReviewRepository.REVERSE_PRIORITY_TIER_PRACTICED,
            ReviewRepository.resolveOppositeProgressTier(Stage.DAILY, "2026-10-04")
        )
        assertEquals(
            ReviewRepository.REVERSE_PRIORITY_TIER_PRACTICED,
            ReviewRepository.resolveOppositeProgressTier(Stage.WEEKLY, "2026-10-02")
        )
        assertEquals(
            ReviewRepository.REVERSE_PRIORITY_TIER_PRACTICED,
            ReviewRepository.resolveOppositeProgressTier(Stage.MONTHLY, "2026-09-20")
        )
        assertEquals(
            ReviewRepository.REVERSE_PRIORITY_TIER_NEW,
            ReviewRepository.resolveOppositeProgressTier(Stage.DAILY, null)
        )
        assertEquals(
            ReviewRepository.REVERSE_PRIORITY_TIER_NEW,
            ReviewRepository.resolveOppositeProgressTier(null, null)
        )
    }

    @Test
    fun `selectPrioritizedInterleavedTiers fills exclusively from tier 1 when learned pool meets maxCards`() {
        val tier1 = (1..120).map { i ->
            ReviewRepository.InterleaveCandidate("learned_$i", "cat_${i % 4}", "WORD")
        }
        val tier2 = (1..50).map { i ->
            ReviewRepository.InterleaveCandidate("practiced_$i", "cat_${i % 4}", "WORD")
        }
        val tier3 = (1..200).map { i ->
            ReviewRepository.InterleaveCandidate("new_$i", "cat_${i % 4}", "WORD")
        }

        val selected = ReviewRepository.selectPrioritizedInterleavedTiers(
            tierCandidateGroups = listOf(tier1, tier2, tier3),
            maxCards = 100,
            random = Random(42L)
        )

        assertEquals(100, selected.size)
        assertEquals(100, selected.distinct().size)
        assertTrue("All 100 selected items must come from tier 1", selected.all { it.startsWith("learned_") })

        val catMap = tier1.associate { it.conceptId to it.categoryKey }
        for (i in 1 until selected.size) {
            assertNotEquals("Adjacent tier-1 cards should avoid same category", catMap[selected[i - 1]], catMap[selected[i]])
        }
    }

    @Test
    fun `selectPrioritizedInterleavedTiers cascades from learned to practiced to new words`() {
        val tier1 = (1..20).map { i ->
            ReviewRepository.InterleaveCandidate("learned_$i", "cat_${i % 4}", "WORD")
        }
        val tier2 = (1..30).map { i ->
            ReviewRepository.InterleaveCandidate("practiced_$i", "cat_${i % 4}", "PHRASE")
        }
        val tier3 = (1..100).map { i ->
            ReviewRepository.InterleaveCandidate("new_$i", "cat_${i % 4}", "SENTENCE")
        }

        val selected = ReviewRepository.selectPrioritizedInterleavedTiers(
            tierCandidateGroups = listOf(tier1, tier2, tier3),
            maxCards = 100,
            random = Random(7L)
        )

        assertEquals(100, selected.size)
        assertEquals(100, selected.distinct().size)

        val first20 = selected.subList(0, 20)
        val next30 = selected.subList(20, 50)
        val last50 = selected.subList(50, 100)

        assertTrue("First 20 must all be learned in NORMAL", first20.all { it.startsWith("learned_") })
        assertTrue("Next 30 must all be practiced in NORMAL", next30.all { it.startsWith("practiced_") })
        assertTrue("Remaining 50 must all come from new words", last50.all { it.startsWith("new_") })
    }

    @Test
    fun `selectPrioritizedInterleavedTiers falls back cleanly when learned and practiced pools are empty`() {
        val tier3 = (1..40).map { i ->
            ReviewRepository.InterleaveCandidate("new_$i", "cat_${i % 3}", "WORD")
        }

        val selected = ReviewRepository.selectPrioritizedInterleavedTiers(
            tierCandidateGroups = listOf(emptyList(), emptyList(), tier3),
            maxCards = 25,
            random = Random(13L)
        )

        assertEquals(25, selected.size)
        assertEquals(25, selected.distinct().size)
        assertTrue(selected.all { it.startsWith("new_") })
    }

    @Test
    fun `selectPrioritizedInterleavedTiers deduplicates across tiers and respects maxCards`() {
        val tier1 = listOf(
            ReviewRepository.InterleaveCandidate("shared_1", "cat_a", "WORD"),
            ReviewRepository.InterleaveCandidate("t1_2", "cat_b", "WORD")
        )
        val tier2 = listOf(
            ReviewRepository.InterleaveCandidate("shared_1", "cat_a", "WORD"),
            ReviewRepository.InterleaveCandidate("t2_1", "cat_c", "WORD")
        )
        val tier3 = listOf(
            ReviewRepository.InterleaveCandidate("t3_1", "cat_d", "WORD")
        )

        val selected = ReviewRepository.selectPrioritizedInterleavedTiers(
            tierCandidateGroups = listOf(tier1, tier2, tier3),
            maxCards = 10,
            random = Random(1L)
        )

        assertEquals(4, selected.size)
        assertEquals(4, selected.distinct().size)
        assertEquals(setOf("shared_1", "t1_2"), selected.take(2).toSet())
        assertEquals("t2_1", selected[2])
        assertEquals("t3_1", selected[3])
        assertTrue(ReviewRepository.selectPrioritizedInterleavedTiers(listOf(tier1), 0).isEmpty())
    }
}
