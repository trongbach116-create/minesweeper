package ptit.e23cnpm01.hotrongbach.test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ptit.e23cnpm01.hotrongbach.test.model.LeaderboardCriteria
import ptit.e23cnpm01.hotrongbach.test.model.MatchResult
import ptit.e23cnpm01.hotrongbach.test.model.Player
import ptit.e23cnpm01.hotrongbach.test.util.LeaderboardCalculator

class LeaderboardCalculatorTest {

    @Test
    fun testModule4ScoringAndTieBreakers() {
        val p1 = Player("p1", "Player A")
        val p2 = Player("p2", "Player B")
        val p3 = Player("p3", "Player C")
        val p4 = Player("p4", "Player D (No Matches)")

        val players = listOf(p1, p2, p3, p4)

        // Matches setup:
        // Match 1: p1 vs p2 -> p1 wins (1.0 pt), p2 loses (0.0 pt), duration 100s
        // Match 2: p2 vs p3 -> draw (0.5 pt each), duration 200s
        // Match 3: p1 vs p3 -> draw (0.5 pt each), duration 300s
        val matches = listOf(
            MatchResult("m1", "p1", "p2", score1 = 1.0, score2 = 0.0, durationSeconds = 100),
            MatchResult("m2", "p2", "p3", score1 = 0.5, score2 = 0.5, durationSeconds = 200),
            MatchResult("m3", "p1", "p3", score1 = 0.5, score2 = 0.5, durationSeconds = 300)
        )

        val leaderboard = LeaderboardCalculator.calculateLeaderboard(players, matches, LeaderboardCriteria.DEFAULT)

        // Expectations:
        // p1: 1 win (1.0), 1 draw (0.5) -> Total points = 1.5. Opponents: p2 (0.5), p3 (1.0) -> Opp Avg = 0.75. Avg Time for win/draw = (100 + 300)/2 = 200s (03:20)
        // p3: 2 draws (0.5, 0.5) -> Total points = 1.0. Opponents: p2 (0.5), p1 (1.5) -> Opp Avg = 1.0. Avg Time for win/draw = (200 + 300)/2 = 250s (04:10)
        // p2: 1 draw (0.5), 1 loss (0.0) -> Total points = 0.5. Opponents: p1 (1.5), p3 (1.0) -> Opp Avg = 1.25. Avg Time for win/draw = 200s (03:20) (loss not included)
        // p4: 0 matches -> Total points = null, Opp Avg = null, Avg Time = null

        assertEquals(4, leaderboard.size)

        // Rank 1 should be p1
        val r1 = leaderboard[0]
        assertEquals("p1", r1.player.id)
        assertEquals(1, r1.rank)
        assertEquals(1.5, r1.totalPoints!!, 0.001)
        assertEquals(0.75, r1.avgOpponentPoints!!, 0.001)
        assertEquals(200.0, r1.avgCompletionTimeSec!!, 0.001)
        assertEquals("1.5 đ", r1.formattedPoints)
        assertEquals("03:20", r1.formattedAvgTime)

        // Rank 2 should be p3
        val r2 = leaderboard[1]
        assertEquals("p3", r2.player.id)
        assertEquals(2, r2.rank)
        assertEquals(1.0, r2.totalPoints!!, 0.001)
        assertEquals(1.0, r2.avgOpponentPoints!!, 0.001)
        assertEquals(250.0, r2.avgCompletionTimeSec!!, 0.001)

        // Rank 3 should be p2
        val r3 = leaderboard[2]
        assertEquals("p2", r3.player.id)
        assertEquals(3, r3.rank)
        assertEquals(0.5, r3.totalPoints!!, 0.001)
        assertEquals(1.25, r3.avgOpponentPoints!!, 0.001)
        assertEquals(200.0, r3.avgCompletionTimeSec!!, 0.001)

        // Rank 4 should be p4 (No matches - empty values "-")
        val r4 = leaderboard[3]
        assertEquals("p4", r4.player.id)
        assertEquals(4, r4.rank)
        assertNull(r4.totalPoints)
        assertNull(r4.avgOpponentPoints)
        assertNull(r4.avgCompletionTimeSec)
        assertEquals("-", r4.formattedPoints)
        assertEquals("-", r4.formattedAvgOpponent)
        assertEquals("-", r4.formattedAvgTime)
    }

    @Test
    fun testTieBreakerWhenTotalPointsAreEqual() {
        val p1 = Player("p1", "Player 1")
        val p2 = Player("p2", "Player 2")
        val p3 = Player("p3", "Player 3")
        val p4 = Player("p4", "Player 4")

        val players = listOf(p1, p2, p3, p4)

        // p1 played against strong opponent p3 (2.0 pts).
        // p2 played against weak opponent p4 (0.0 pts).
        // Both p1 and p2 have 1.0 total point.
        val matches = listOf(
            MatchResult("m1", "p3", "p4", score1 = 1.0, score2 = 0.0, durationSeconds = 100),
            MatchResult("m2", "p3", "p4", score1 = 1.0, score2 = 0.0, durationSeconds = 100), // p3 now has 2.0 pts, p4 has 0.0 pts
            MatchResult("m3", "p1", "p3", score1 = 1.0, score2 = 0.0, durationSeconds = 120), // p1 wins against p3
            MatchResult("m4", "p2", "p4", score1 = 1.0, score2 = 0.0, durationSeconds = 120)  // p2 wins against p4
        )

        val leaderboard = LeaderboardCalculator.calculateLeaderboard(players, matches)

        // p1 and p2 both have 1 win = 1.0 pt.
        // p1's opponent p3 has 2.0 pts -> p1 opp avg = 2.0
        // p2's opponent p4 has 0.0 pts -> p2 opp avg = 0.0
        // Therefore, p1 should rank higher than p2 due to opponent avg tie-breaker!
        val p1Entry = leaderboard.first { it.player.id == "p1" }
        val p2Entry = leaderboard.first { it.player.id == "p2" }

        assertEquals(1.0, p1Entry.totalPoints!!, 0.001)
        assertEquals(1.0, p2Entry.totalPoints!!, 0.001)

        assertEquals(2.0, p1Entry.avgOpponentPoints!!, 0.001)
        assertEquals(0.0, p2Entry.avgOpponentPoints!!, 0.001)

        assertEquals(true, p1Entry.rank < p2Entry.rank)
    }
}
