package ptit.e23cnpm01.hotrongbach.test.model

data class MinesweeperCell(
    val row: Int,
    val col: Int,
    val isMine: Boolean = false,
    val neighborMines: Int = 0,
    val isRevealed: Boolean = false,
    val isFlagged: Boolean = false
)

enum class GameParticipantStatus {
    PLAYING,
    WON,
    LOST_HIT_MINE,
    DISCONNECTED_EXITED
}

enum class MatchOutcome(val title: String, val points: Double, val description: String) {
    WIN("BẠN THẮNG! 🏆", 1.0, "Bạn đã mở hết tất cả các ô an toàn trước đối thủ (+1 điểm)"),
    DRAW("HÒA! 🤝", 0.5, "Cả hai bên cùng kết thúc tại một thời điểm (+0.5 điểm)"),
    LOSS("BẠN THUA! ❌", 0.0, "Đối thủ đã mở hết các ô an toàn trước hoặc bạn mở trúng mìn (+0 điểm)"),
    FORFEIT_WIN("THẮNG DO ĐỐI THỦ THOÁT! 🏆", 1.0, "Đối thủ bị mất kết nối hoặc thoát giữa chừng (+1 điểm)")
}

data class ParticipantState(
    val grid: List<List<MinesweeperCell>>,
    val status: GameParticipantStatus = GameParticipantStatus.PLAYING,
    val revealedSafeCellsCount: Int = 0,
    val flagsCount: Int = 0,
    val hitMineCell: Pair<Int, Int>? = null,
    val completionTimeSec: Long? = null
)

data class MinesweeperBoardConfig(
    val rows: Int = 9,
    val cols: Int = 9,
    val totalMines: Int = 10
) {
    val totalSafeCells: Int
        get() = (rows * cols) - totalMines
}

data class ChallengeRequest(
    val challenger: Player,
    val opponent: Player
)

data class PvpMatchOutcome(
    val winnerId: String?, // Null if Draw
    val p1Points: Double,
    val p2Points: Double,
    val title: String,
    val description: String
)
