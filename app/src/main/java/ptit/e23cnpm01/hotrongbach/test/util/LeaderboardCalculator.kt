package ptit.e23cnpm01.hotrongbach.test.util

import ptit.e23cnpm01.hotrongbach.test.model.LeaderboardCriteria
import ptit.e23cnpm01.hotrongbach.test.model.LeaderboardEntry
import ptit.e23cnpm01.hotrongbach.test.model.MatchResult
import ptit.e23cnpm01.hotrongbach.test.model.Player

object LeaderboardCalculator {

    fun calculateLeaderboard(
        players: List<Player>,
        matches: List<MatchResult>,
        criteria: LeaderboardCriteria = LeaderboardCriteria.DEFAULT
    ): List<LeaderboardEntry> {
        // Step 1: Compute total points, match counts, wins, draws, losses, and win/draw durations per player
        val playerStats = players.associateWith { player ->
            var matchCount = 0
            var wins = 0
            var draws = 0
            var losses = 0
            var pointsSum = 0.0
            var winDrawDurationSum = 0L
            var winDrawCount = 0

            matches.forEach { match ->
                when (player.id) {
                    match.player1Id -> {
                        matchCount++
                        pointsSum += match.score1
                        when (match.score1) {
                            1.0 -> {
                                wins++
                                winDrawDurationSum += match.durationSeconds
                                winDrawCount++
                            }
                            0.5 -> {
                                draws++
                                winDrawDurationSum += match.durationSeconds
                                winDrawCount++
                            }
                            else -> losses++
                        }
                    }
                    match.player2Id -> {
                        matchCount++
                        pointsSum += match.score2
                        when (match.score2) {
                            1.0 -> {
                                wins++
                                winDrawDurationSum += match.durationSeconds
                                winDrawCount++
                            }
                            0.5 -> {
                                draws++
                                winDrawDurationSum += match.durationSeconds
                                winDrawCount++
                            }
                            else -> losses++
                        }
                    }
                }
            }

            val totalPts = if (matchCount > 0) pointsSum else null
            val avgTime = if (winDrawCount > 0) winDrawDurationSum.toDouble() / winDrawCount else null

            RawPlayerStats(
                matchCount = matchCount,
                wins = wins,
                draws = draws,
                losses = losses,
                totalPoints = totalPts,
                avgWinDrawDurationSec = avgTime
            )
        }

        // Map player id to computed total points (or null if no matches)
        val playerTotalPointsMap = playerStats.mapKeys { it.key.id }

        // Step 2: Compute average points of opponents faced for each player
        val rawEntries = players.map { player ->
            val stats = playerStats[player] ?: RawPlayerStats(0, 0, 0, 0, null, null)

            val avgOpponentPts = if (stats.matchCount > 0) {
                var opponentPtsSum = 0.0
                var opponentCount = 0

                matches.forEach { match ->
                    val opponentId = when (player.id) {
                        match.player1Id -> match.player2Id
                        match.player2Id -> match.player1Id
                        else -> null
                    }

                    if (opponentId != null) {
                        val oppStats = playerTotalPointsMap[opponentId]
                        val oppPts = oppStats?.totalPoints ?: 0.0
                        opponentPtsSum += oppPts
                        opponentCount++
                    }
                }

                if (opponentCount > 0) opponentPtsSum / opponentCount else null
            } else {
                null
            }

            UnrankedEntry(
                player = player,
                totalMatches = stats.matchCount,
                wins = stats.wins,
                draws = stats.draws,
                losses = stats.losses,
                totalPoints = stats.totalPoints,
                avgOpponentPoints = avgOpponentPts,
                avgCompletionTimeSec = stats.avgWinDrawDurationSec
            )
        }

        // Step 3: Sort according to criteria priority rules
        val sortedEntries = when (criteria) {
            LeaderboardCriteria.DEFAULT, LeaderboardCriteria.TOTAL_POINTS -> {
                rawEntries.sortedWith { e1, e2 ->
                    var cmp = compareDescendingNullsLast(e1.totalPoints, e2.totalPoints)
                    if (cmp != 0) return@sortedWith cmp

                    cmp = compareDescendingNullsLast(e1.avgOpponentPoints, e2.avgOpponentPoints)
                    if (cmp != 0) return@sortedWith cmp

                    cmp = compareAscendingNullsLast(e1.avgCompletionTimeSec, e2.avgCompletionTimeSec)
                    if (cmp != 0) return@sortedWith cmp

                    e1.player.name.compareTo(e2.player.name)
                }
            }
            LeaderboardCriteria.OPPONENT_AVG -> {
                rawEntries.sortedWith { e1, e2 ->
                    var cmp = compareDescendingNullsLast(e1.avgOpponentPoints, e2.avgOpponentPoints)
                    if (cmp != 0) return@sortedWith cmp

                    cmp = compareDescendingNullsLast(e1.totalPoints, e2.totalPoints)
                    if (cmp != 0) return@sortedWith cmp

                    cmp = compareAscendingNullsLast(e1.avgCompletionTimeSec, e2.avgCompletionTimeSec)
                    if (cmp != 0) return@sortedWith cmp

                    e1.player.name.compareTo(e2.player.name)
                }
            }
            LeaderboardCriteria.AVG_MATCH_TIME -> {
                rawEntries.sortedWith { e1, e2 ->
                    var cmp = compareAscendingNullsLast(e1.avgCompletionTimeSec, e2.avgCompletionTimeSec)
                    if (cmp != 0) return@sortedWith cmp

                    cmp = compareDescendingNullsLast(e1.totalPoints, e2.totalPoints)
                    if (cmp != 0) return@sortedWith cmp

                    cmp = compareDescendingNullsLast(e1.avgOpponentPoints, e2.avgOpponentPoints)
                    if (cmp != 0) return@sortedWith cmp

                    e1.player.name.compareTo(e2.player.name)
                }
            }
        }

        // Step 4: Assign 1-based ranks
        return sortedEntries.mapIndexed { index, entry ->
            LeaderboardEntry(
                rank = index + 1,
                player = entry.player,
                totalMatches = entry.totalMatches,
                wins = entry.wins,
                draws = entry.draws,
                losses = entry.losses,
                totalPoints = entry.totalPoints,
                avgOpponentPoints = entry.avgOpponentPoints,
                avgCompletionTimeSec = entry.avgCompletionTimeSec
            )
        }
    }

    private fun compareDescendingNullsLast(a: Double?, b: Double?): Int {
        if (a == null && b == null) return 0
        if (a == null) return 1   // a is null, so a comes after b
        if (b == null) return -1  // b is null, so a comes before b
        return b.compareTo(a)     // descending
    }

    private fun compareAscendingNullsLast(a: Double?, b: Double?): Int {
        if (a == null && b == null) return 0
        if (a == null) return 1   // a is null, so a comes after b
        if (b == null) return -1  // b is null, so a comes before b
        return a.compareTo(b)     // ascending
    }

    private data class RawPlayerStats(
        val matchCount: Int,
        val wins: Int,
        val draws: Int,
        val losses: Int,
        val totalPoints: Double?,
        val avgWinDrawDurationSec: Double?
    )

    private data class UnrankedEntry(
        val player: Player,
        val totalMatches: Int,
        val wins: Int,
        val draws: Int,
        val losses: Int,
        val totalPoints: Double?,
        val avgOpponentPoints: Double?,
        val avgCompletionTimeSec: Double?
    )
}
