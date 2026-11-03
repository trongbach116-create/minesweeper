package ptit.e23cnpm01.hotrongbach.test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ptit.e23cnpm01.hotrongbach.test.model.MinesweeperBoardConfig
import ptit.e23cnpm01.hotrongbach.test.model.MinesweeperCell
import ptit.e23cnpm01.hotrongbach.test.model.ParticipantState
import ptit.e23cnpm01.hotrongbach.test.util.BotMoveAction
import ptit.e23cnpm01.hotrongbach.test.util.MinesweeperBotAI
import ptit.e23cnpm01.hotrongbach.test.util.MinesweeperEngine

class MinesweeperBotTest {

    @Test
    fun testFirstClickIsNeverAMine() {
        val config = MinesweeperBoardConfig(rows = 3, cols = 3, totalMines = 1)

        // Board generated where (0,0) is explicitly set as a mine
        val gridWithMineAtFirstClick = listOf(
            listOf(MinesweeperCell(0, 0, isMine = true), MinesweeperCell(0, 1, isMine = false), MinesweeperCell(0, 2, isMine = false)),
            listOf(MinesweeperCell(1, 0, isMine = false), MinesweeperCell(1, 1, isMine = false), MinesweeperCell(1, 2, isMine = false)),
            listOf(MinesweeperCell(2, 0, isMine = false), MinesweeperCell(2, 1, isMine = false), MinesweeperCell(2, 2, isMine = false))
        )

        val initialState = ParticipantState(grid = gridWithMineAtFirstClick)

        // First click is at (0,0)
        val resultState = MinesweeperEngine.revealCell(initialState, row = 0, col = 0, config = config)

        // First click must NOT hit a mine!
        assertNull(resultState.hitMineCell)
        assertTrue(resultState.grid[0][0].isRevealed)
        assertTrue(!resultState.grid[0][0].isMine)
        assertTrue(resultState.revealedSafeCellsCount > 0)
    }

    @Test
    fun testFloodFillOnZeroNeighborMineCell() {
        val config = MinesweeperBoardConfig(rows = 3, cols = 3, totalMines = 1)

        val grid = listOf(
            listOf(MinesweeperCell(0, 0, isMine = false, neighborMines = 0), MinesweeperCell(0, 1, isMine = false, neighborMines = 0), MinesweeperCell(0, 2, isMine = false, neighborMines = 0)),
            listOf(MinesweeperCell(1, 0, isMine = false, neighborMines = 0), MinesweeperCell(1, 1, isMine = false, neighborMines = 1), MinesweeperCell(1, 2, isMine = false, neighborMines = 1)),
            listOf(MinesweeperCell(2, 0, isMine = false, neighborMines = 0), MinesweeperCell(2, 1, isMine = false, neighborMines = 1), MinesweeperCell(2, 2, isMine = true, neighborMines = 0))
        )

        val initialState = ParticipantState(grid = grid)
        val updatedState = MinesweeperEngine.revealCell(initialState, row = 0, col = 0, config = config)

        assertTrue(updatedState.revealedSafeCellsCount > 1)
        assertTrue(updatedState.grid[0][0].isRevealed)
        assertTrue(updatedState.grid[0][1].isRevealed)
        assertTrue(updatedState.grid[1][0].isRevealed)
    }

    @Test
    fun testBotAIDetectsGuaranteedMineAndSafeCells() {
        val config = MinesweeperBoardConfig(rows = 2, cols = 2, totalMines = 1)

        val grid = listOf(
            listOf(MinesweeperCell(0, 0, isRevealed = true, neighborMines = 1), MinesweeperCell(0, 1, isFlagged = true, isMine = true)),
            listOf(MinesweeperCell(1, 0, isRevealed = false), MinesweeperCell(1, 1, isRevealed = false))
        )

        val move = MinesweeperBotAI.decideNextMove(grid, config)

        assertTrue(move is BotMoveAction.Open)
        val openMove = move as BotMoveAction.Open
        assertEquals(1, openMove.row)
        assertEquals(0, openMove.col)
    }
}
