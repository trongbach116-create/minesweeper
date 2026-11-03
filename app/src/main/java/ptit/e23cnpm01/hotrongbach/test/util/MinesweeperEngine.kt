package ptit.e23cnpm01.hotrongbach.test.util

import ptit.e23cnpm01.hotrongbach.test.model.MinesweeperBoardConfig
import ptit.e23cnpm01.hotrongbach.test.model.MinesweeperCell
import ptit.e23cnpm01.hotrongbach.test.model.ParticipantState
import java.util.ArrayDeque
import kotlin.random.Random

object MinesweeperEngine {

    fun generateMasterBoard(config: MinesweeperBoardConfig, seed: Long? = null): List<List<MinesweeperCell>> {
        val random = if (seed != null) Random(seed) else Random.Default
        val totalCells = config.rows * config.cols
        val minePositions = mutableSetOf<Int>()

        while (minePositions.size < config.totalMines && minePositions.size < totalCells) {
            val pos = random.nextInt(totalCells)
            minePositions.add(pos)
        }

        val tempGrid = MutableList(config.rows) { r ->
            MutableList(config.cols) { c ->
                val index = r * config.cols + c
                MinesweeperCell(row = r, col = c, isMine = minePositions.contains(index))
            }
        }

        // Calculate neighbor mines
        for (r in 0 until config.rows) {
            for (c in 0 until config.cols) {
                if (!tempGrid[r][c].isMine) {
                    var minesCount = 0
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            if (dr == 0 && dc == 0) continue
                            val nr = r + dr
                            val nc = c + dc
                            if (nr in 0 until config.rows && nc in 0 until config.cols) {
                                if (tempGrid[nr][nc].isMine) minesCount++
                            }
                        }
                    }
                    tempGrid[r][c] = tempGrid[r][c].copy(neighborMines = minesCount)
                }
            }
        }

        return tempGrid
    }

    private fun ensureSafeFirstClick(
        grid: List<List<MinesweeperCell>>,
        safeRow: Int,
        safeCol: Int,
        config: MinesweeperBoardConfig
    ): List<List<MinesweeperCell>> {
        if (!grid[safeRow][safeCol].isMine) {
            return grid
        }

        // Find all empty non-mine spots
        val emptySpots = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until config.rows) {
            for (c in 0 until config.cols) {
                if (!grid[r][c].isMine && !(r == safeRow && c == safeCol)) {
                    emptySpots.add(Pair(r, c))
                }
            }
        }

        if (emptySpots.isEmpty()) return grid

        // Relocate mine from (safeRow, safeCol) to a random empty spot
        val newMineSpot = emptySpots.random()

        val mutableGrid = grid.map { rList ->
            rList.map { cell ->
                when {
                    cell.row == safeRow && cell.col == safeCol -> cell.copy(isMine = false)
                    cell.row == newMineSpot.first && cell.col == newMineSpot.second -> cell.copy(isMine = true)
                    else -> cell
                }
            }.toMutableList()
        }.toMutableList()

        // Recalculate neighbor mine counts for all cells
        for (r in 0 until config.rows) {
            for (c in 0 until config.cols) {
                if (!mutableGrid[r][c].isMine) {
                    var minesCount = 0
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            if (dr == 0 && dc == 0) continue
                            val nr = r + dr
                            val nc = c + dc
                            if (nr in 0 until config.rows && nc in 0 until config.cols) {
                                if (mutableGrid[nr][nc].isMine) minesCount++
                            }
                        }
                    }
                    mutableGrid[r][c] = mutableGrid[r][c].copy(neighborMines = minesCount)
                } else {
                    mutableGrid[r][c] = mutableGrid[r][c].copy(neighborMines = 0)
                }
            }
        }

        return mutableGrid
    }

    fun revealCell(
        currentState: ParticipantState,
        row: Int,
        col: Int,
        config: MinesweeperBoardConfig
    ): ParticipantState {
        var grid = currentState.grid
        val isFirstClick = grid.none { rList -> rList.any { cell -> cell.isRevealed } }

        if (isFirstClick) {
            grid = ensureSafeFirstClick(grid, row, col, config)
        }

        val cell = grid[row][col]

        if (cell.isRevealed || cell.isFlagged) {
            return currentState.copy(grid = grid)
        }

        if (cell.isMine) {
            // Hit a mine! Reveal mine cell
            val newGrid = grid.map { rList ->
                rList.map { c ->
                    if (c.row == row && c.col == col) c.copy(isRevealed = true) else c
                }
            }
            return currentState.copy(
                grid = newGrid,
                hitMineCell = Pair(row, col)
            )
        }

        // Reveal safe cell + Flood fill if neighborMines == 0
        val newGrid = grid.map { it.toMutableList() }.toMutableList()
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(Pair(row, col))

        var newlyRevealedCount = 0

        while (queue.isNotEmpty()) {
            val (r, c) = queue.removeFirst()
            val current = newGrid[r][c]

            if (current.isRevealed || current.isFlagged) continue

            newGrid[r] = newGrid[r].toMutableList().also {
                it[c] = current.copy(isRevealed = true)
            }
            newlyRevealedCount++

            if (current.neighborMines == 0) {
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        if (dr == 0 && dc == 0) continue
                        val nr = r + dr
                        val nc = c + dc
                        if (nr in 0 until config.rows && nc in 0 until config.cols) {
                            val neighbor = newGrid[nr][nc]
                            if (!neighbor.isRevealed && !neighbor.isFlagged && !neighbor.isMine) {
                                queue.add(Pair(nr, nc))
                            }
                        }
                    }
                }
            }
        }

        val totalRevealedSafe = currentState.revealedSafeCellsCount + newlyRevealedCount

        return currentState.copy(
            grid = newGrid,
            revealedSafeCellsCount = totalRevealedSafe
        )
    }

    fun toggleFlag(currentState: ParticipantState, row: Int, col: Int): ParticipantState {
        val cell = currentState.grid[row][col]
        if (cell.isRevealed) return currentState

        val newFlagged = !cell.isFlagged
        val newGrid = currentState.grid.map { rList ->
            rList.map { c ->
                if (c.row == row && c.col == col) c.copy(isFlagged = newFlagged) else c
            }
        }

        val newFlagCount = if (newFlagged) currentState.flagsCount + 1 else currentState.flagsCount - 1

        return currentState.copy(
            grid = newGrid,
            flagsCount = newFlagCount
        )
    }

    fun revealAllMines(grid: List<List<MinesweeperCell>>): List<List<MinesweeperCell>> {
        return grid.map { row ->
            row.map { cell ->
                if (cell.isMine) cell.copy(isRevealed = true) else cell
            }
        }
    }
}
