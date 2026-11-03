package ptit.e23cnpm01.hotrongbach.test.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ptit.e23cnpm01.hotrongbach.test.model.GameParticipantStatus
import ptit.e23cnpm01.hotrongbach.test.model.MatchOutcome
import ptit.e23cnpm01.hotrongbach.test.model.MinesweeperBoardConfig
import ptit.e23cnpm01.hotrongbach.test.model.MinesweeperCell
import ptit.e23cnpm01.hotrongbach.test.model.ParticipantState
import ptit.e23cnpm01.hotrongbach.test.ui.theme.TestTheme
import ptit.e23cnpm01.hotrongbach.test.util.MinesweeperEngine
import ptit.e23cnpm01.hotrongbach.test.viewmodel.BotGameState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinesweeperVsBotScreen(
    gameState: BotGameState,
    onPlayerCellClick: (row: Int, col: Int) -> Unit,
    onToggleFlagMode: () -> Unit,
    onRestartGame: () -> Unit,
    onShowExitConfirm: (Boolean) -> Unit,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSec = gameState.elapsedTimeSec
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Dò Mìn vs Máy (Bot AI)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (gameState.isGameActive && gameState.matchOutcome == null) {
                                onShowExitConfirm(true)
                            } else {
                                onExitGame()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Thoát"
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = timeFormatted,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Side-by-Side Progress Header
            ProgressComparisonHeader(
                playerState = gameState.playerState,
                botState = gameState.botState,
                config = gameState.config
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bot Realtime Action Log Banner
            BotLogBanner(logText = gameState.lastBotLog)

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Mode Toggle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bàn chơi của Bạn",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = onToggleFlagMode,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (gameState.isFlagMode) Color(0xFFE53935) else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (gameState.isFlagMode) Icons.Default.Flag else Icons.Default.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (gameState.isFlagMode) "Chế độ: ĐẶT CỜ 🚩" else "Chế độ: MỞ Ô 🔨",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Player's Interactive Minesweeper Grid
            MinesweeperGridView(
                grid = gameState.playerState.grid,
                config = gameState.config,
                onCellClick = onPlayerCellClick,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Bot's Live Miniature Board View
            Text(
                text = "🤖 Bàn chơi của Máy (Bot AI - Theo dõi thời gian thực)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            MinesweeperGridView(
                grid = gameState.botState.grid,
                config = gameState.config,
                onCellClick = { _, _ -> }, // Bot grid is view-only for player
                isBotView = true,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Exit Confirm Dialog
    if (gameState.showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { onShowExitConfirm(false) },
            title = {
                Text("Hủy Ván Đấu Hiện Tại?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Text("Trận đấu đang diễn ra. Bạn có chắc chắn muốn thoát về Trang chủ không?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onShowExitConfirm(false)
                        onExitGame()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hủy trận & Thoát")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { onShowExitConfirm(false) }) {
                    Text("Tiếp tục chơi")
                }
            }
        )
    }

    // End-Game Outcome Dialog
    if (gameState.showResultDialog && gameState.matchOutcome != null) {
        MatchResultDialog(
            outcome = gameState.matchOutcome,
            elapsedTimeFormatted = timeFormatted,
            playerState = gameState.playerState,
            botState = gameState.botState,
            config = gameState.config,
            onPlayAgain = onRestartGame,
            onExit = onExitGame
        )
    }
}

@Composable
fun ProgressComparisonHeader(
    playerState: ParticipantState,
    botState: ParticipantState,
    config: MinesweeperBoardConfig
) {
    val totalSafe = config.totalSafeCells

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Side Progress
            ParticipantProgressCard(
                title = "Người chơi (Bạn)",
                icon = Icons.Default.SportsEsports,
                iconColor = MaterialTheme.colorScheme.primary,
                revealedSafe = playerState.revealedSafeCellsCount,
                totalSafe = totalSafe,
                flags = playerState.flagsCount,
                totalMines = config.totalMines,
                status = playerState.status,
                modifier = Modifier.weight(1f)
            )

            // VS Divider
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Bot Side Progress
            ParticipantProgressCard(
                title = "Máy (Bot AI)",
                icon = Icons.Default.SmartToy,
                iconColor = MaterialTheme.colorScheme.secondary,
                revealedSafe = botState.revealedSafeCellsCount,
                totalSafe = totalSafe,
                flags = botState.flagsCount,
                totalMines = config.totalMines,
                status = botState.status,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ParticipantProgressCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    revealedSafe: Int,
    totalSafe: Int,
    flags: Int,
    totalMines: Int,
    status: GameParticipantStatus,
    modifier: Modifier = Modifier
) {
    val progressFraction = if (totalSafe > 0) (revealedSafe.toFloat() / totalSafe).coerceIn(0f, 1f) else 0f
    val percentText = (progressFraction * 100).toInt()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress Bar
        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = iconColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$revealedSafe / $totalSafe ô ($percentText%)",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "🚩 $flags/$totalMines",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Status Badge
        val (statusText, statusBg, statusFg) = when (status) {
            GameParticipantStatus.PLAYING -> Triple("Đang thi đấu...", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
            GameParticipantStatus.WON -> Triple("ĐÃ HOÀN THÀNH 🏆", Color(0xFFE8F5E9), Color(0xFF2E7D32))
            GameParticipantStatus.LOST_HIT_MINE -> Triple("BỊ NỔ MÌN 💣", Color(0xFFFFEBEE), Color(0xFFC62828))
            GameParticipantStatus.DISCONNECTED_EXITED -> Triple("ĐÃ THOÁT ⚡", Color(0xFFFFF3E0), Color(0xFFE65100))
        }

        Surface(
            shape = RoundedCornerShape(4.dp),
            color = statusBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = statusFg,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
fun BotLogBanner(logText: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PrecisionManufacturing,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = logText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MinesweeperGridView(
    grid: List<List<MinesweeperCell>>,
    config: MinesweeperBoardConfig,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier,
    isBotView: Boolean = false
) {
    if (grid.isEmpty()) return

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (r in 0 until config.rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (c in 0 until config.cols) {
                        val cell = grid[r][c]
                        CellViewItem(
                            cell = cell,
                            isBotView = isBotView,
                            onClick = { if (!isBotView) onCellClick(r, c) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CellViewItem(
    cell: MinesweeperCell,
    isBotView: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(6.dp)

    if (!cell.isRevealed) {
        // Unrevealed Cell
        Surface(
            shape = shape,
            color = if (isBotView) Color(0xFFD0D7DE) else MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = if (isBotView) 0.dp else 2.dp,
            modifier = modifier.clickable(enabled = !isBotView, onClick = onClick)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (cell.isFlagged) {
                    Text(text = "🚩", fontSize = if (isBotView) 10.sp else 14.sp)
                }
            }
        }
    } else {
        // Revealed Cell
        val bgColor = if (cell.isMine) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.surface

        Surface(
            shape = shape,
            color = bgColor,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = modifier
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (cell.isMine) {
                    Text(text = "💣", fontSize = if (isBotView) 10.sp else 14.sp)
                } else if (cell.neighborMines > 0) {
                    val numColor = getMineNumberColor(cell.neighborMines)
                    Text(
                        text = "${cell.neighborMines}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = numColor
                        ),
                        fontSize = if (isBotView) 10.sp else 13.sp
                    )
                }
            }
        }
    }
}

fun getMineNumberColor(number: Int): Color {
    return when (number) {
        1 -> Color(0xFF1976D2) // Blue
        2 -> Color(0xFF388E3C) // Green
        3 -> Color(0xFFD32F2F) // Red
        4 -> Color(0xFF7B1FA2) // Purple
        5 -> Color(0xFFFF8F00) // Orange
        6 -> Color(0xFF0097A7) // Cyan
        else -> Color.Black
    }
}

@Composable
fun MatchResultDialog(
    outcome: MatchOutcome,
    elapsedTimeFormatted: String,
    playerState: ParticipantState,
    botState: ParticipantState,
    config: MinesweeperBoardConfig,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = outcome.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Điểm nhận được: +${outcome.points} điểm",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = outcome.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Thời gian kết thúc:", style = MaterialTheme.typography.bodySmall)
                            Text(elapsedTimeFormatted, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tiến độ Bạn:", style = MaterialTheme.typography.bodySmall)
                            Text("${playerState.revealedSafeCellsCount}/${config.totalSafeCells} ô", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tiến độ Máy:", style = MaterialTheme.typography.bodySmall)
                            Text("${botState.revealedSafeCellsCount}/${config.totalSafeCells} ô", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Bạn có muốn chơi lại không?",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onPlayAgain,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Chơi lại ván mới")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onExit,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Thoát về trang chủ")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MinesweeperVsBotScreenPreview() {
    val sampleConfig = MinesweeperBoardConfig(9, 9, 10)
    val sampleBoard = MinesweeperEngine.generateMasterBoard(sampleConfig)

    TestTheme {
        MinesweeperVsBotScreen(
            gameState = BotGameState(
                config = sampleConfig,
                playerState = ParticipantState(grid = sampleBoard, revealedSafeCellsCount = 12, flagsCount = 2),
                botState = ParticipantState(grid = sampleBoard, revealedSafeCellsCount = 15, flagsCount = 3),
                elapsedTimeSec = 95,
                isGameActive = true,
                lastBotLog = "Máy vừa mở ô (3, 4): Ô chắc chắn an toàn"
            ),
            onPlayerCellClick = { _, _ -> },
            onToggleFlagMode = {},
            onRestartGame = {},
            onShowExitConfirm = {},
            onExitGame = {}
        )
    }
}
