package ptit.e23cnpm01.hotrongbach.test.util

import ptit.e23cnpm01.hotrongbach.test.model.MinesweeperBoardConfig
import ptit.e23cnpm01.hotrongbach.test.model.MinesweeperCell

sealed class BotMoveAction {
    data class Open(val row: Int, val col: Int, val reason: String) : BotMoveAction()
    data class Flag(val row: Int, val col: Int, val reason: String) : BotMoveAction()
    object None : BotMoveAction()
}

object MinesweeperBotAI {

    fun decideNextMove(
        grid: List<List<MinesweeperCell>>,
        config: MinesweeperBoardConfig
    ): BotMoveAction {
        val rows = config.rows
        val cols = config.cols

        val revealedCells = mutableListOf<MinesweeperCell>()
        val unrevealedUnflagged = mutableListOf<MinesweeperCell>()
        var totalFlags = 0

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = grid[r][c]
                if (cell.isRevealed) {
                    if (cell.neighborMines > 0) {
                        revealedCells.add(cell)
                    }
                } else {
                    if (cell.isFlagged) {
                        totalFlags++
                    } else {
                        unrevealedUnflagged.add(cell)
                    }
                }
            }
        }

        if (unrevealedUnflagged.isEmpty()) {
            return BotMoveAction.None
        }

        // Priority 1: Check for guaranteed Flags and guaranteed Safe Openings
        val guaranteedFlagCandidates = mutableListOf<Pair<Int, Int>>()
        val guaranteedSafeCandidates = mutableListOf<Pair<Int, Int>>()

        for (cell in revealedCells) {
            val r = cell.row
            val c = cell.col
            val neighbors = getNeighbors(grid, r, c, rows, cols)

            val unrevealedNeighbors = neighbors.filter { !it.isRevealed && !it.isFlagged }
            val flaggedNeighbors = neighbors.filter { it.isFlagged }

            val remainingMinesNeeded = cell.neighborMines - flaggedNeighbors.size

            // Rule 1: Guaranteed Mines (Remaining needed mines == Unrevealed neighbors count)
            if (remainingMinesNeeded > 0 && remainingMinesNeeded == unrevealedNeighbors.size) {
                unrevealedNeighbors.forEach {
                    guaranteedFlagCandidates.add(Pair(it.row, it.col))
                }
            }

            // Rule 2: Guaranteed Safe (Flagged count == cell neighborMines count)
            if (remainingMinesNeeded == 0 && unrevealedNeighbors.isNotEmpty()) {
                unrevealedNeighbors.forEach {
                    guaranteedSafeCandidates.add(Pair(it.row, it.col))
                }
            }
        }

        // Execute guaranteed Safe Open first if available
        if (guaranteedSafeCandidates.isNotEmpty()) {
            val target = guaranteedSafeCandidates.first()
            return BotMoveAction.Open(target.first, target.second, "Ô chắc chắn an toàn (100% safe)")
        }

        // Execute guaranteed Flag if available
        if (guaranteedFlagCandidates.isNotEmpty()) {
            val target = guaranteedFlagCandidates.first()
            return BotMoveAction.Flag(target.first, target.second, "Ô chắc chắn có mìn (100% mine)")
        }

        // Priority 2: Calculate lowest mine probability for unrevealed cells
        val cellProbabilities = mutableMapOf<Pair<Int, Int>, Double>()

        val remainingUnflaggedMines = (config.totalMines - totalFlags).coerceAtLeast(0)
        val globalProb = if (unrevealedUnflagged.isNotEmpty()) {
            remainingUnflaggedMines.toDouble() / unrevealedUnflagged.size
        } else {
            0.5
        }

        for (cell in unrevealedUnflagged) {
            val r = cell.row
            val c = cell.col
            val neighbors = getNeighbors(grid, r, c, rows, cols)
            val revealedNeighbors = neighbors.filter { it.isRevealed && it.neighborMines > 0 }

            if (revealedNeighbors.isEmpty()) {
                cellProbabilities[Pair(r, c)] = globalProb
            } else {
                var maxLocalProb = 0.0
                for (rev in revealedNeighbors) {
                    val revNeighbors = getNeighbors(grid, rev.row, rev.col, rows, cols)
                    val unrevCount = revNeighbors.count { !it.isRevealed && !it.isFlagged }
                    val flagCount = revNeighbors.count { it.isFlagged }
                    val needed = (rev.neighborMines - flagCount).coerceAtLeast(0)

                    val prob = if (unrevCount > 0) needed.toDouble() / unrevCount else globalProb
                    if (prob > maxLocalProb) {
                        maxLocalProb = prob
                    }
                }
                cellProbabilities[Pair(r, c)] = maxLocalProb
            }
        }

        // Pick candidate with minimum probability of containing a mine
        val bestCandidate = cellProbabilities.minByOrNull { it.value }

        return if (bestCandidate != null) {
            val probPercent = (bestCandidate.value * 100).toInt()
            BotMoveAction.Open(
                bestCandidate.key.first,
                bestCandidate.key.second,
                "Khả năng chứa mìn thấp nhất (~$probPercent%)"
            )
        } else {
            val randomTarget = unrevealedUnflagged.random()
            BotMoveAction.Open(randomTarget.row, randomTarget.col, "Mở ngẫu nhiên")
        }
    }

    private fun getNeighbors(
        grid: List<List<MinesweeperCell>>,
        row: Int,
        col: Int,
        maxRows: Int,
        maxCols: Int
    ): List<MinesweeperCell> {
        val result = mutableListOf<MinesweeperCell>()
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nr = row + dr
                val nc = col + dc
                if (nr in 0 until maxRows && nc in 0 until maxCols) {
                    result.add(grid[nr][nc])
                }
            }
        }
        return result
    }
}
