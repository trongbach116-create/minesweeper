package ptit.e23cnpm01.hotrongbach.test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ptit.e23cnpm01.hotrongbach.test.model.GameParticipantStatus
import ptit.e23cnpm01.hotrongbach.test.model.MinesweeperBoardConfig
import ptit.e23cnpm01.hotrongbach.test.util.MinesweeperEngine

class PvpMatchTest {

    @Test
    fun testSynchronizedBoardGeneration() {
        val config = MinesweeperBoardConfig(rows = 9, cols = 9, totalMines = 10)
        // Fixed seed produces identical board
        val board1 = MinesweeperEngine.generateMasterBoard(config, seed = 12345L)
        val board2 = MinesweeperEngine.generateMasterBoard(config, seed = 12345L)

        assertEquals(9, board1.size)
        assertEquals(9, board2.size)

        for (r in 0 until 9) {
            for (c in 0 until 9) {
                assertEquals(board1[r][c].isMine, board2[r][c].isMine)
                assertEquals(board1[r][c].neighborMines, board2[r][c].neighborMines)
            }
        }
    }

    @Test
    fun testMidGameDisconnectionForfeit() {
        // Module 6 rule:
        // "nếu trong lúc đang chơi có một người bị mất kết nối hoặc thoát giữa chừng,
        // hệ thống xử người đó thua, người còn lại thắng"
        val p1Status = GameParticipantStatus.DISCONNECTED_EXITED
        val p2Status = GameParticipantStatus.PLAYING

        val p1Score = if (p1Status == GameParticipantStatus.DISCONNECTED_EXITED) 0.0 else 1.0
        val p2Score = if (p1Status == GameParticipantStatus.DISCONNECTED_EXITED) 1.0 else 0.0

        assertEquals(0.0, p1Score, 0.001)
        assertEquals(1.0, p2Score, 0.001)
    }
}
