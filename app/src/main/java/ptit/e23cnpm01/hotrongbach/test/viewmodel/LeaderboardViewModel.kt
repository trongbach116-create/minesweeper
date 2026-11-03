package ptit.e23cnpm01.hotrongbach.test.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ptit.e23cnpm01.hotrongbach.test.model.ChallengeRequest
import ptit.e23cnpm01.hotrongbach.test.model.GameParticipantStatus
import ptit.e23cnpm01.hotrongbach.test.model.LeaderboardCriteria
import ptit.e23cnpm01.hotrongbach.test.model.LeaderboardEntry
import ptit.e23cnpm01.hotrongbach.test.model.MatchOutcome
import ptit.e23cnpm01.hotrongbach.test.model.MatchResult
import ptit.e23cnpm01.hotrongbach.test.model.MinesweeperBoardConfig
import ptit.e23cnpm01.hotrongbach.test.model.ParticipantState
import ptit.e23cnpm01.hotrongbach.test.model.Player
import ptit.e23cnpm01.hotrongbach.test.model.PvpMatchOutcome
import ptit.e23cnpm01.hotrongbach.test.model.Screen
import ptit.e23cnpm01.hotrongbach.test.util.BotMoveAction
import ptit.e23cnpm01.hotrongbach.test.util.LeaderboardCalculator
import ptit.e23cnpm01.hotrongbach.test.util.MinesweeperBotAI
import ptit.e23cnpm01.hotrongbach.test.util.MinesweeperEngine

data class BotGameState(
    val config: MinesweeperBoardConfig = MinesweeperBoardConfig(rows = 9, cols = 9, totalMines = 10),
    val playerState: ParticipantState = ParticipantState(grid = emptyList()),
    val botState: ParticipantState = ParticipantState(grid = emptyList()),
    val elapsedTimeSec: Long = 0,
    val isGameActive: Boolean = false,
    val isFlagMode: Boolean = false,
    val matchOutcome: MatchOutcome? = null,
    val lastBotLog: String = "Máy đang chuẩn bị...",
    val showResultDialog: Boolean = false,
    val showExitConfirmDialog: Boolean = false
)

data class PvpGameState(
    val config: MinesweeperBoardConfig = MinesweeperBoardConfig(rows = 9, cols = 9, totalMines = 10),
    val player1: Player = Player("p1", "Nguyễn Văn A (Bạn)"),
    val player2: Player = Player("p2", "Trần Thị B"),
    val player1State: ParticipantState = ParticipantState(grid = emptyList()),
    val player2State: ParticipantState = ParticipantState(grid = emptyList()),
    val elapsedTimeSec: Long = 0,
    val isGameActive: Boolean = false,
    val player1FlagMode: Boolean = false,
    val player2FlagMode: Boolean = false,
    val outcome: PvpMatchOutcome? = null,
    val showResultDialog: Boolean = false,
    val notificationBanner: String? = null,
    val p1WantsRematch: Boolean? = null,
    val p2WantsRematch: Boolean? = null
)

data class LeaderboardUiState(
    val currentScreen: Screen = Screen.MAIN,
    val players: List<Player> = emptyList(),
    val matches: List<MatchResult> = emptyList(),
    val leaderboardEntries: List<LeaderboardEntry> = emptyList(),
    val selectedCriteria: LeaderboardCriteria = LeaderboardCriteria.DEFAULT,
    val searchQuery: String = "",
    val botGameState: BotGameState = BotGameState(),
    val pvpGameState: PvpGameState = PvpGameState(),
    val activeChallengeRequest: ChallengeRequest? = null,
    val selectedPlayerDetail: LeaderboardEntry? = null,
    val activeChatPlayer: Player? = null,
    val chatMessages: List<Pair<String, String>> = emptyList()
) {
    val onlinePlayers: List<Player>
        get() = players.filter { it.isOnline && it.id != "p1" }

    val filteredLeaderboardEntries: List<LeaderboardEntry>
        get() = if (searchQuery.isBlank()) {
            leaderboardEntries
        } else {
            leaderboardEntries.filter { it.player.name.contains(searchQuery, ignoreCase = true) }
        }
}

class LeaderboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var botJob: Job? = null
    private var pvpTimerJob: Job? = null

    init {
        loadMockData()
    }

    private fun loadMockData() {
        val p1 = Player("p1", "Nguyễn Văn A (Bạn)", isOnline = true, statusText = "Rảnh")
        val p2 = Player("p2", "Trần Thị B", isOnline = true, statusText = "Sẵn sàng thi đấu")
        val p3 = Player("p3", "Lê Hoàng C", isOnline = true, statusText = "Rảnh")
        val p4 = Player("p4", "Phạm Minh D", isOnline = true, statusText = "Đang chờ đối thủ")
        val p5 = Player("p5", "Hoàng Anh E", isOnline = false, statusText = "Ngoại tuyến")
        val p6 = Player("p6", "Vũ Đức F", isOnline = true, statusText = "Mới tham gia (Chưa có trận)")
        val bot = Player("bot", "Máy (Bot AI)", isOnline = true, statusText = "Đối thủ máy")

        val playersList = listOf(p1, p2, p3, p4, p5, p6, bot)

        val matchesList = mutableListOf(
            MatchResult("m1", "p1", "p2", score1 = 1.0, score2 = 0.0, durationSeconds = 180),
            MatchResult("m2", "p1", "p3", score1 = 0.5, score2 = 0.5, durationSeconds = 120),
            MatchResult("m3", "p2", "p4", score1 = 1.0, score2 = 0.0, durationSeconds = 150),
            MatchResult("m4", "p3", "p4", score1 = 1.0, score2 = 0.0, durationSeconds = 100),
            MatchResult("m5", "p1", "p4", score1 = 1.0, score2 = 0.0, durationSeconds = 210),
            MatchResult("m6", "p2", "p3", score1 = 1.0, score2 = 0.0, durationSeconds = 140),
            MatchResult("m7", "p3", "p5", score1 = 1.0, score2 = 0.0, durationSeconds = 90),
            MatchResult("m8", "p4", "p5", score1 = 0.5, score2 = 0.5, durationSeconds = 200)
        )

        val entries = LeaderboardCalculator.calculateLeaderboard(
            players = playersList,
            matches = matchesList,
            criteria = LeaderboardCriteria.DEFAULT
        )

        _uiState.value = LeaderboardUiState(
            players = playersList,
            matches = matchesList,
            leaderboardEntries = entries
        )
    }

    fun navigateToLeaderboard() {
        _uiState.value = _uiState.value.copy(currentScreen = Screen.LEADERBOARD)
    }

    fun navigateToMain() {
        stopGameTimers()
        stopPvpTimer()
        _uiState.value = _uiState.value.copy(
            currentScreen = Screen.MAIN,
            activeChallengeRequest = null
        )
    }

    fun selectCriteria(criteria: LeaderboardCriteria) {
        val currentState = _uiState.value
        val updatedEntries = LeaderboardCalculator.calculateLeaderboard(
            players = currentState.players,
            matches = currentState.matches,
            criteria = criteria
        )
        _uiState.value = currentState.copy(
            selectedCriteria = criteria,
            leaderboardEntries = updatedEntries
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectPlayerDetail(entry: LeaderboardEntry?) {
        _uiState.value = _uiState.value.copy(selectedPlayerDetail = entry)
    }

    fun openChatWithPlayer(player: Player?) {
        if (player == null) {
            _uiState.value = _uiState.value.copy(activeChatPlayer = null, chatMessages = emptyList())
        } else {
            val initialMsgs = listOf(
                player.name to "Xin chào! Bạn có muốn giao lưu Dò Mìn không?",
                "Bạn" to "Chào bạn, mình sẵn sàng thi đấu!"
            )
            _uiState.value = _uiState.value.copy(
                activeChatPlayer = player,
                chatMessages = initialMsgs
            )
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val currentMsgs = _uiState.value.chatMessages
        val updatedMsgs = currentMsgs + Pair("Bạn", text)
        _uiState.value = _uiState.value.copy(chatMessages = updatedMsgs)

        // Simulate opponent reply
        val opponentName = _uiState.value.activeChatPlayer?.name ?: "Đối thủ"
        viewModelScope.launch {
            delay(1200)
            val replyMsg = listOf(
                "Được rồi, bấm nút Thách đấu nhé!",
                "Tớ sẵn sàng rồi nè!",
                "Thách đấu tớ đi!"
            ).random()
            _uiState.value = _uiState.value.copy(
                chatMessages = _uiState.value.chatMessages + Pair(opponentName, replyMsg)
            )
        }
    }

    // ==================== MODULE 5: MINESWEEPER VS BOT ====================

    fun startVsBotGame() {
        val config = MinesweeperBoardConfig(rows = 9, cols = 9, totalMines = 10)
        val masterBoard = MinesweeperEngine.generateMasterBoard(config)

        val initialPlayerState = ParticipantState(grid = masterBoard)
        val initialBotState = ParticipantState(grid = masterBoard)

        _uiState.value = _uiState.value.copy(
            currentScreen = Screen.VS_BOT_GAME,
            botGameState = BotGameState(
                config = config,
                playerState = initialPlayerState,
                botState = initialBotState,
                elapsedTimeSec = 0,
                isGameActive = true,
                isFlagMode = false,
                matchOutcome = null,
                lastBotLog = "Máy đang tính toán nước đi...",
                showResultDialog = false,
                showExitConfirmDialog = false
            )
        )

        startGameTimers()
    }

    fun showBotExitConfirm(show: Boolean) {
        val current = _uiState.value.botGameState
        _uiState.value = _uiState.value.copy(
            botGameState = current.copy(showExitConfirmDialog = show)
        )
    }

    fun toggleFlagMode() {
        val current = _uiState.value.botGameState
        _uiState.value = _uiState.value.copy(
            botGameState = current.copy(isFlagMode = !current.isFlagMode)
        )
    }

    fun onPlayerCellClick(row: Int, col: Int) {
        val gameState = _uiState.value.botGameState
        if (!gameState.isGameActive || gameState.matchOutcome != null) return

        val config = gameState.config
        val currentPlayerState = gameState.playerState

        val updatedPlayerState = if (gameState.isFlagMode) {
            MinesweeperEngine.toggleFlag(currentPlayerState, row, col)
        } else {
            MinesweeperEngine.revealCell(currentPlayerState, row, col, config)
        }

        val checkedPlayerState = checkParticipantStatus(updatedPlayerState, config)

        _uiState.value = _uiState.value.copy(
            botGameState = gameState.copy(playerState = checkedPlayerState)
        )

        checkGameOutcome()
    }

    private fun startGameTimers() {
        stopGameTimers()

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _uiState.value.botGameState
                if (current.isGameActive && current.matchOutcome == null) {
                    _uiState.value = _uiState.value.copy(
                        botGameState = current.copy(elapsedTimeSec = current.elapsedTimeSec + 1)
                    )
                } else {
                    break
                }
            }
        }

        botJob = viewModelScope.launch {
            delay(1000)
            while (true) {
                val current = _uiState.value.botGameState
                if (current.isGameActive && current.matchOutcome == null) {
                    executeBotMove()
                    delay(1200)
                } else {
                    break
                }
            }
        }
    }

    private fun executeBotMove() {
        val gameState = _uiState.value.botGameState
        if (!gameState.isGameActive || gameState.matchOutcome != null) return

        val config = gameState.config
        val currentBotState = gameState.botState

        val botAction = MinesweeperBotAI.decideNextMove(currentBotState.grid, config)

        val updatedBotState = when (botAction) {
            is BotMoveAction.Open -> {
                MinesweeperEngine.revealCell(currentBotState, botAction.row, botAction.col, config)
            }
            is BotMoveAction.Flag -> {
                MinesweeperEngine.toggleFlag(currentBotState, botAction.row, botAction.col)
            }
            BotMoveAction.None -> currentBotState
        }

        val checkedBotState = checkParticipantStatus(updatedBotState, config)
        val logText = when (botAction) {
            is BotMoveAction.Open -> "Máy mở ô (${botAction.row + 1}, ${botAction.col + 1}): ${botAction.reason}"
            is BotMoveAction.Flag -> "Máy cắm cờ (${botAction.row + 1}, ${botAction.col + 1}): ${botAction.reason}"
            BotMoveAction.None -> "Máy chưa tìm thấy nước đi phù hợp"
        }

        _uiState.value = _uiState.value.copy(
            botGameState = gameState.copy(
                botState = checkedBotState,
                lastBotLog = logText
            )
        )

        checkGameOutcome()
    }

    private fun checkParticipantStatus(
        state: ParticipantState,
        config: MinesweeperBoardConfig
    ): ParticipantState {
        if (state.hitMineCell != null) {
            return state.copy(
                status = GameParticipantStatus.LOST_HIT_MINE,
                grid = MinesweeperEngine.revealAllMines(state.grid)
            )
        }
        if (state.revealedSafeCellsCount >= config.totalSafeCells) {
            return state.copy(status = GameParticipantStatus.WON)
        }
        return state
    }

    private fun checkGameOutcome() {
        val gameState = _uiState.value.botGameState
        if (gameState.matchOutcome != null) return

        val pStatus = gameState.playerState.status
        val bStatus = gameState.botState.status

        val outcome: MatchOutcome? = when {
            (pStatus == GameParticipantStatus.LOST_HIT_MINE && bStatus == GameParticipantStatus.LOST_HIT_MINE) ||
                    (pStatus == GameParticipantStatus.WON && bStatus == GameParticipantStatus.WON) -> MatchOutcome.DRAW

            pStatus == GameParticipantStatus.WON || bStatus == GameParticipantStatus.LOST_HIT_MINE -> MatchOutcome.WIN

            bStatus == GameParticipantStatus.WON || pStatus == GameParticipantStatus.LOST_HIT_MINE -> MatchOutcome.LOSS

            else -> null
        }

        if (outcome != null) {
            stopGameTimers()

            val pScore = outcome.points
            val bScore = 1.0 - pScore
            val duration = gameState.elapsedTimeSec

            val newMatch = MatchResult(
                id = "m_${System.currentTimeMillis()}",
                player1Id = "p1",
                player2Id = "bot",
                score1 = pScore,
                score2 = bScore,
                durationSeconds = duration
            )

            val updatedMatches = _uiState.value.matches + newMatch
            val updatedEntries = LeaderboardCalculator.calculateLeaderboard(
                players = _uiState.value.players,
                matches = updatedMatches,
                criteria = _uiState.value.selectedCriteria
            )

            _uiState.value = _uiState.value.copy(
                matches = updatedMatches,
                leaderboardEntries = updatedEntries,
                botGameState = gameState.copy(
                    isGameActive = false,
                    matchOutcome = outcome,
                    showResultDialog = true
                )
            )
        }
    }

    private fun stopGameTimers() {
        timerJob?.cancel()
        timerJob = null
        botJob?.cancel()
        botJob = null
    }

    fun restartBotGame() {
        stopGameTimers()
        startVsBotGame()
    }

    // ==================== MODULE 6: MULTIPLAYER PVP MATCH ====================

    fun initiateChallenge(targetPlayer: Player) {
        val currentPlayer = _uiState.value.players.firstOrNull { it.id == "p1" } ?: Player("p1", "Nguyễn Văn A (Bạn)")
        _uiState.value = _uiState.value.copy(
            activeChallengeRequest = ChallengeRequest(
                challenger = targetPlayer,
                opponent = currentPlayer
            )
        )
    }

    fun declineChallenge() {
        _uiState.value = _uiState.value.copy(activeChallengeRequest = null)
    }

    fun acceptChallenge() {
        val req = _uiState.value.activeChallengeRequest
        val p1 = _uiState.value.players.firstOrNull { it.id == "p1" } ?: Player("p1", "Nguyễn Văn A (Bạn)")
        val p2 = req?.challenger ?: Player("p2", "Trần Thị B")

        _uiState.value = _uiState.value.copy(activeChallengeRequest = null)
        startPvpMatch(p1, p2)
    }

    fun startPvpMatch(player1: Player, player2: Player) {
        val config = MinesweeperBoardConfig(rows = 9, cols = 9, totalMines = 10)
        val masterBoard = MinesweeperEngine.generateMasterBoard(config)

        val p1State = ParticipantState(grid = masterBoard)
        val p2State = ParticipantState(grid = masterBoard)

        _uiState.value = _uiState.value.copy(
            currentScreen = Screen.PVP_MATCH_GAME,
            pvpGameState = PvpGameState(
                config = config,
                player1 = player1,
                player2 = player2,
                player1State = p1State,
                player2State = p2State,
                elapsedTimeSec = 0,
                isGameActive = true,
                player1FlagMode = false,
                player2FlagMode = false,
                outcome = null,
                showResultDialog = false,
                notificationBanner = "Trận đối kháng đã bắt đầu! Cả hai người chơi thi đấu trên cùng đề bài.",
                p1WantsRematch = null,
                p2WantsRematch = null
            )
        )

        startPvpTimer()
    }

    private fun startPvpTimer() {
        stopPvpTimer()
        pvpTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _uiState.value.pvpGameState
                if (current.isGameActive && current.outcome == null) {
                    _uiState.value = _uiState.value.copy(
                        pvpGameState = current.copy(elapsedTimeSec = current.elapsedTimeSec + 1)
                    )
                } else {
                    break
                }
            }
        }
    }

    private fun stopPvpTimer() {
        pvpTimerJob?.cancel()
        pvpTimerJob = null
    }

    fun togglePvpFlagMode(playerNumber: Int) {
        val current = _uiState.value.pvpGameState
        _uiState.value = _uiState.value.copy(
            pvpGameState = if (playerNumber == 1) {
                current.copy(player1FlagMode = !current.player1FlagMode)
            } else {
                current.copy(player2FlagMode = !current.player2FlagMode)
            }
        )
    }

    fun onPvpCellClick(playerNumber: Int, row: Int, col: Int) {
        val gameState = _uiState.value.pvpGameState
        if (!gameState.isGameActive || gameState.outcome != null) return

        val config = gameState.config

        if (playerNumber == 1) {
            val currentState = gameState.player1State
            if (currentState.status != GameParticipantStatus.PLAYING) return

            val updatedState = if (gameState.player1FlagMode) {
                MinesweeperEngine.toggleFlag(currentState, row, col)
            } else {
                MinesweeperEngine.revealCell(currentState, row, col, config)
            }
            val checkedState = checkParticipantStatus(updatedState, config)
            val finalState = if (checkedState.status == GameParticipantStatus.WON) {
                checkedState.copy(completionTimeSec = gameState.elapsedTimeSec)
            } else checkedState

            _uiState.value = _uiState.value.copy(
                pvpGameState = gameState.copy(player1State = finalState)
            )
        } else {
            val currentState = gameState.player2State
            if (currentState.status != GameParticipantStatus.PLAYING) return

            val updatedState = if (gameState.player2FlagMode) {
                MinesweeperEngine.toggleFlag(currentState, row, col)
            } else {
                MinesweeperEngine.revealCell(currentState, row, col, config)
            }
            val checkedState = checkParticipantStatus(updatedState, config)
            val finalState = if (checkedState.status == GameParticipantStatus.WON) {
                checkedState.copy(completionTimeSec = gameState.elapsedTimeSec)
            } else checkedState

            _uiState.value = _uiState.value.copy(
                pvpGameState = gameState.copy(player2State = finalState)
            )
        }

        checkPvpGameOutcome()
    }

    fun onPvpPlayerDisconnect(playerNumber: Int) {
        val gameState = _uiState.value.pvpGameState
        if (!gameState.isGameActive || gameState.outcome != null) return

        val disconnectedPlayer = if (playerNumber == 1) gameState.player1 else gameState.player2
        val winningPlayer = if (playerNumber == 1) gameState.player2 else gameState.player1

        val updatedP1State = if (playerNumber == 1) {
            gameState.player1State.copy(status = GameParticipantStatus.DISCONNECTED_EXITED)
        } else {
            gameState.player1State.copy(status = GameParticipantStatus.WON)
        }

        val updatedP2State = if (playerNumber == 2) {
            gameState.player2State.copy(status = GameParticipantStatus.DISCONNECTED_EXITED)
        } else {
            gameState.player2State.copy(status = GameParticipantStatus.WON)
        }

        stopPvpTimer()

        val p1Pts = if (playerNumber == 2) 1.0 else 0.0
        val p2Pts = if (playerNumber == 1) 1.0 else 0.0
        val winnerId = winningPlayer.id
        val bannerMsg = "${disconnectedPlayer.name} đã thoát giữa chừng / mất kết nối! ${winningPlayer.name} được xử THẮNG (+1 điểm)!"

        savePvpMatchResult(
            p1Id = gameState.player1.id,
            p2Id = gameState.player2.id,
            p1Score = p1Pts,
            p2Score = p2Pts,
            duration = gameState.elapsedTimeSec
        )

        _uiState.value = _uiState.value.copy(
            pvpGameState = gameState.copy(
                player1State = updatedP1State,
                player2State = updatedP2State,
                isGameActive = false,
                outcome = PvpMatchOutcome(
                    winnerId = winnerId,
                    p1Points = p1Pts,
                    p2Points = p2Pts,
                    title = "THẮNG DO ĐỐI THỦ THOÁT! 🏆",
                    description = bannerMsg
                ),
                notificationBanner = bannerMsg,
                showResultDialog = true
            )
        )
    }

    private fun checkPvpGameOutcome() {
        val gameState = _uiState.value.pvpGameState
        if (gameState.outcome != null) return

        val p1Status = gameState.player1State.status
        val p2Status = gameState.player2State.status

        var outcome: PvpMatchOutcome? = null

        if (p1Status == GameParticipantStatus.WON && p2Status == GameParticipantStatus.WON) {
            val t1 = gameState.player1State.completionTimeSec ?: gameState.elapsedTimeSec
            val t2 = gameState.player2State.completionTimeSec ?: gameState.elapsedTimeSec
            outcome = when {
                t1 < t2 -> PvpMatchOutcome(
                    winnerId = gameState.player1.id,
                    p1Points = 1.0,
                    p2Points = 0.0,
                    title = "${gameState.player1.name} THẮNG! 🏆",
                    description = "${gameState.player1.name} hoàn thành nhanh hơn (${t1}s vs ${t2}s)!"
                )
                t2 < t1 -> PvpMatchOutcome(
                    winnerId = gameState.player2.id,
                    p1Points = 0.0,
                    p2Points = 1.0,
                    title = "${gameState.player2.name} THẮNG! 🏆",
                    description = "${gameState.player2.name} hoàn thành nhanh hơn (${t2}s vs ${t1}s)!"
                )
                else -> PvpMatchOutcome(
                    winnerId = null,
                    p1Points = 0.5,
                    p2Points = 0.5,
                    title = "TRẬN ĐẤU HÒA! 🤝",
                    description = "Cả hai người chơi hoàn thành cùng một thời điểm!"
                )
            }
        } else if (p1Status == GameParticipantStatus.LOST_HIT_MINE && p2Status == GameParticipantStatus.LOST_HIT_MINE) {
            outcome = PvpMatchOutcome(
                winnerId = null,
                p1Points = 0.5,
                p2Points = 0.5,
                title = "TRẬN ĐẤU HÒA! 🤝",
                description = "Cả hai người chơi cùng mở trúng mìn!"
            )
        } else if (p1Status == GameParticipantStatus.WON || p2Status == GameParticipantStatus.LOST_HIT_MINE) {
            outcome = PvpMatchOutcome(
                winnerId = gameState.player1.id,
                p1Points = 1.0,
                p2Points = 0.0,
                title = "${gameState.player1.name} THẮNG! 🏆",
                description = if (p1Status == GameParticipantStatus.WON)
                    "${gameState.player1.name} đã mở hết các ô an toàn trước!"
                else "${gameState.player2.name} mở trúng mìn!"
            )
        } else if (p2Status == GameParticipantStatus.WON || p1Status == GameParticipantStatus.LOST_HIT_MINE) {
            outcome = PvpMatchOutcome(
                winnerId = gameState.player2.id,
                p1Points = 0.0,
                p2Points = 1.0,
                title = "${gameState.player2.name} THẮNG! 🏆",
                description = if (p2Status == GameParticipantStatus.WON)
                    "${gameState.player2.name} đã mở hết các ô an toàn trước!"
                else "${gameState.player1.name} mở trúng mìn!"
            )
        }

        if (outcome != null) {
            stopPvpTimer()

            savePvpMatchResult(
                p1Id = gameState.player1.id,
                p2Id = gameState.player2.id,
                p1Score = outcome.p1Points,
                p2Score = outcome.p2Points,
                duration = gameState.elapsedTimeSec
            )

            _uiState.value = _uiState.value.copy(
                pvpGameState = gameState.copy(
                    isGameActive = false,
                    outcome = outcome,
                    showResultDialog = true,
                    notificationBanner = outcome.description
                )
            )
        }
    }

    private fun savePvpMatchResult(
        p1Id: String,
        p2Id: String,
        p1Score: Double,
        p2Score: Double,
        duration: Long
    ) {
        val newMatch = MatchResult(
            id = "pvp_${System.currentTimeMillis()}",
            player1Id = p1Id,
            player2Id = p2Id,
            score1 = p1Score,
            score2 = p2Score,
            durationSeconds = duration
        )

        val updatedMatches = _uiState.value.matches + newMatch
        val updatedEntries = LeaderboardCalculator.calculateLeaderboard(
            players = _uiState.value.players,
            matches = updatedMatches,
            criteria = _uiState.value.selectedCriteria
        )

        _uiState.value = _uiState.value.copy(
            matches = updatedMatches,
            leaderboardEntries = updatedEntries
        )
    }

    fun onPvpRematchDecision(playerNumber: Int, wantsRematch: Boolean) {
        val gameState = _uiState.value.pvpGameState

        val newP1Rematch = if (playerNumber == 1) wantsRematch else gameState.p1WantsRematch
        val newP2Rematch = if (playerNumber == 2) wantsRematch else gameState.p2WantsRematch

        val updatedGameState = gameState.copy(
            p1WantsRematch = newP1Rematch,
            p2WantsRematch = newP2Rematch
        )

        _uiState.value = _uiState.value.copy(pvpGameState = updatedGameState)

        if (newP1Rematch == true && newP2Rematch == true) {
            startPvpMatch(gameState.player1, gameState.player2)
        } else if (newP1Rematch == false || newP2Rematch == false) {
            stopPvpTimer()
            _uiState.value = _uiState.value.copy(
                currentScreen = Screen.MAIN,
                activeChallengeRequest = null
            )
        }
    }
}
