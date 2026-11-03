package ptit.e23cnpm01.hotrongbach.test.model

import java.util.Locale

enum class Screen {
    MAIN,
    LEADERBOARD,
    VS_BOT_GAME,
    PVP_MATCH_GAME
}

enum class LeaderboardCriteria(val title: String, val description: String) {
    DEFAULT("Ưu tiên chuẩn", "Sắp xếp: Tổng điểm ↓ -> TB đối thủ ↓ -> TB thời gian ↑"),
    TOTAL_POINTS("Tổng số điểm", "Sắp xếp theo Tổng số điểm (Thắng 1đ, Hòa 0.5đ, Thua 0đ)"),
    OPPONENT_AVG("TB điểm đối thủ", "Sắp xếp theo Trung bình điểm của các đối thủ đã gặp"),
    AVG_MATCH_TIME("TB thời gian kết thúc", "Sắp xếp theo Trung bình thời gian trận thắng/hòa (thấp hơn xếp trên)")
}

data class Player(
    val id: String,
    val name: String,
    val avatarUrl: String = "",
    val isOnline: Boolean = true,
    val statusText: String = "Sẵn sàng"
)

data class MatchResult(
    val id: String,
    val player1Id: String,
    val player2Id: String,
    val score1: Double, // 1.0 = Win, 0.5 = Draw, 0.0 = Loss
    val score2: Double, // 1.0 = Win, 0.5 = Draw, 0.0 = Loss
    val durationSeconds: Long // Match duration in seconds
)

data class LeaderboardEntry(
    val rank: Int,
    val player: Player,
    val totalMatches: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val totalPoints: Double?,            // Null if 0 matches
    val avgOpponentPoints: Double?,     // Null if 0 matches
    val avgCompletionTimeSec: Double?   // Null if 0 matches or no win/draw matches
) {
    val formattedPoints: String
        get() = totalPoints?.let { if (it % 1.0 == 0.0) "${it.toInt()} đ" else "$it đ" } ?: "-"

    val formattedAvgOpponent: String
        get() = avgOpponentPoints?.let { String.format(Locale.getDefault(), "%.2f đ", it) } ?: "-"

    val formattedAvgTime: String
        get() = avgCompletionTimeSec?.let {
            val totalSec = it.toLong()
            val minutes = totalSec / 60
            val seconds = totalSec % 60
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        } ?: "-"
}
