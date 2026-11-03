package ptit.e23cnpm01.hotrongbach.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ptit.e23cnpm01.hotrongbach.test.model.Screen
import ptit.e23cnpm01.hotrongbach.test.ui.LeaderboardScreen
import ptit.e23cnpm01.hotrongbach.test.ui.MainScreen
import ptit.e23cnpm01.hotrongbach.test.ui.MinesweeperVsBotScreen
import ptit.e23cnpm01.hotrongbach.test.ui.PvpMatchScreen
import ptit.e23cnpm01.hotrongbach.test.ui.theme.TestTheme
import ptit.e23cnpm01.hotrongbach.test.viewmodel.LeaderboardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LeaderboardApp()
                }
            }
        }
    }
}

@Composable
fun LeaderboardApp(
    viewModel: LeaderboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState.currentScreen) {
        Screen.MAIN -> {
            MainScreen(
                onlinePlayers = uiState.onlinePlayers,
                onNavigateToLeaderboard = { viewModel.navigateToLeaderboard() },
                onStartVsBotGame = { viewModel.startVsBotGame() },
                onChallengePlayer = { targetPlayer -> viewModel.initiateChallenge(targetPlayer) },
                activeChallengeRequest = uiState.activeChallengeRequest,
                onAcceptChallenge = { viewModel.acceptChallenge() },
                onDeclineChallenge = { viewModel.declineChallenge() },
                activeChatPlayer = uiState.activeChatPlayer,
                chatMessages = uiState.chatMessages,
                onOpenChat = { player -> viewModel.openChatWithPlayer(player) },
                onSendChatMessage = { msg -> viewModel.sendChatMessage(msg) }
            )
        }
        Screen.LEADERBOARD -> {
            LeaderboardScreen(
                entries = uiState.filteredLeaderboardEntries,
                selectedCriteria = uiState.selectedCriteria,
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                onCriteriaSelected = { viewModel.selectCriteria(it) },
                onNavigateBack = { viewModel.navigateToMain() },
                selectedPlayerDetail = uiState.selectedPlayerDetail,
                onSelectPlayerDetail = { entry -> viewModel.selectPlayerDetail(entry) }
            )
        }
        Screen.VS_BOT_GAME -> {
            MinesweeperVsBotScreen(
                gameState = uiState.botGameState,
                onPlayerCellClick = { row, col -> viewModel.onPlayerCellClick(row, col) },
                onToggleFlagMode = { viewModel.toggleFlagMode() },
                onRestartGame = { viewModel.restartBotGame() },
                onShowExitConfirm = { show -> viewModel.showBotExitConfirm(show) },
                onExitGame = { viewModel.navigateToMain() }
            )
        }
        Screen.PVP_MATCH_GAME -> {
            PvpMatchScreen(
                pvpState = uiState.pvpGameState,
                onCellClick = { playerNum, row, col -> viewModel.onPvpCellClick(playerNum, row, col) },
                onToggleFlagMode = { playerNum -> viewModel.togglePvpFlagMode(playerNum) },
                onPlayerDisconnect = { playerNum -> viewModel.onPvpPlayerDisconnect(playerNum) },
                onRematchDecision = { playerNum, choice -> viewModel.onPvpRematchDecision(playerNum, choice) },
                onExitGame = { viewModel.navigateToMain() }
            )
        }
    }
}
