package ptit.e23cnpm01.hotrongbach.test.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ptit.e23cnpm01.hotrongbach.test.model.MinesweeperBoardConfig
import ptit.e23cnpm01.hotrongbach.test.model.ParticipantState
import ptit.e23cnpm01.hotrongbach.test.model.Player
import ptit.e23cnpm01.hotrongbach.test.model.PvpMatchOutcome
import ptit.e23cnpm01.hotrongbach.test.ui.theme.TestTheme
import ptit.e23cnpm01.hotrongbach.test.util.MinesweeperEngine
import ptit.e23cnpm01.hotrongbach.test.viewmodel.PvpGameState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PvpMatchScreen(
    pvpState: PvpGameState,
    onCellClick: (playerNumber: Int, row: Int, col: Int) -> Unit,
    onToggleFlagMode: (playerNumber: Int) -> Unit,
    onPlayerDisconnect: (playerNumber: Int) -> Unit,
    onRematchDecision: (playerNumber: Int, wantsRematch: Boolean) -> Unit,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(1) } // 0 = P1 View, 1 = Dual Split View, 2 = P2 View

    val totalSec = pvpState.elapsedTimeSec
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Trận Đối Kháng 2 Người",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Module 6 - Trọng tài hệ thống điều hành",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onExitGame) {
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
            // Live Score & Progress Header Comparison
            PvpProgressComparisonHeader(
                p1 = pvpState.player1,
                p2 = pvpState.player2,
                p1State = pvpState.player1State,
                p2State = pvpState.player2State,
                config = pvpState.config
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Disconnection Simulation Toolbar (Per Module 6 Requirements)
            DisconnectionSimulationToolbar(
                p1Name = pvpState.player1.name,
                p2Name = pvpState.player2.name,
                onP1Disconnect = { onPlayerDisconnect(1) },
                onP2Disconnect = { onPlayerDisconnect(2) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Perspective Tabs (P1 / Dual Split View / P2)
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Góc nhìn P1", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("👥 Chia Đôi Màn Hình", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Góc nhìn P2", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Minesweeper Boards based on Selected Perspective
            when (selectedTab) {
                0 -> { // Player 1 View
                    SinglePlayerBoardView(
                        playerNumber = 1,
                        player = pvpState.player1,
                        participantState = pvpState.player1State,
                        isFlagMode = pvpState.player1FlagMode,
                        config = pvpState.config,
                        onCellClick = { r, c -> onCellClick(1, r, c) },
                        onToggleFlagMode = { onToggleFlagMode(1) }
                    )
                }
                2 -> { // Player 2 View
                    SinglePlayerBoardView(
                        playerNumber = 2,
                        player = pvpState.player2,
                        participantState = pvpState.player2State,
                        isFlagMode = pvpState.player2FlagMode,
                        config = pvpState.config,
                        onCellClick = { r, c -> onCellClick(2, r, c) },
                        onToggleFlagMode = { onToggleFlagMode(2) }
                    )
                }
                else -> { // Dual Split View (Both Boards Side by Side or Stacked)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SinglePlayerBoardView(
                            playerNumber = 1,
                            player = pvpState.player1,
                            participantState = pvpState.player1State,
                            isFlagMode = pvpState.player1FlagMode,
                            config = pvpState.config,
                            onCellClick = { r, c -> onCellClick(1, r, c) },
                            onToggleFlagMode = { onToggleFlagMode(1) }
                        )

                        SinglePlayerBoardView(
                            playerNumber = 2,
                            player = pvpState.player2,
                            participantState = pvpState.player2State,
                            isFlagMode = pvpState.player2FlagMode,
                            config = pvpState.config,
                            onCellClick = { r, c -> onCellClick(2, r, c) },
                            onToggleFlagMode = { onToggleFlagMode(2) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Module 6 End-Game & Rematch Outcome Dialog
    if (pvpState.showResultDialog && pvpState.outcome != null) {
        PvpResultDialog(
            outcome = pvpState.outcome,
            elapsedTimeFormatted = timeFormatted,
            pvpState = pvpState,
            onRematchBoth = {
                onRematchDecision(1, true)
                onRematchDecision(2, true)
            },
            onP1Stop = { onRematchDecision(1, false) },
            onP2Stop = { onRematchDecision(2, false) },
            onExitBoth = onExitGame
        )
    }
}

@Composable
fun PvpProgressComparisonHeader(
    p1: Player,
    p2: Player,
    p1State: ParticipantState,
    p2State: ParticipantState,
    config: MinesweeperBoardConfig
) {
    val totalSafe = config.totalSafeCells

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player 1 Progress
            PvpParticipantCard(
                player = p1,
                state = p1State,
                totalSafe = totalSafe,
                totalMines = config.totalMines,
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )

            // VS Divider Badge
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(32.dp)
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

            // Player 2 Progress
            PvpParticipantCard(
                player = p2,
                state = p2State,
                totalSafe = totalSafe,
                totalMines = config.totalMines,
                accentColor = Color(0xFFE65100),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PvpParticipantCard(
    player: Player,
    state: ParticipantState,
    totalSafe: Int,
    totalMines: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val progressFraction = if (totalSafe > 0) (state.revealedSafeCellsCount.toFloat() / totalSafe).coerceIn(0f, 1f) else 0f
    val percentText = (progressFraction * 100).toInt()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = player.name,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = accentColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${state.revealedSafeCellsCount}/$totalSafe ô ($percentText%)",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "🚩 ${state.flagsCount}/$totalMines",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        val (statusText, statusBg, statusFg) = when (state.status) {
            GameParticipantStatus.PLAYING -> Triple("Đang thi đấu...", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
            GameParticipantStatus.WON -> Triple("ĐÃ HOÀN THÀNH 🏆", Color(0xFFE8F5E9), Color(0xFF2E7D32))
            GameParticipantStatus.LOST_HIT_MINE -> Triple("BỊ NỔ MÌN 💣", Color(0xFFFFEBEE), Color(0xFFC62828))
            GameParticipantStatus.DISCONNECTED_EXITED -> Triple("ĐÃ THOÁT / MẤT KN ⚡", Color(0xFFFFF3E0), Color(0xFFE65100))
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
fun DisconnectionSimulationToolbar(
    p1Name: String,
    p2Name: String,
    onP1Disconnect: () -> Unit,
    onP2Disconnect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF3E0),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PowerOff,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Giả lập Thoát giữa chừng / Mất kết nối (Module 6)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFE65100)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onP1Disconnect,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("$p1Name Thoát", fontSize = 11.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onP2Disconnect,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("$p2Name Thoát", fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun SinglePlayerBoardView(
    playerNumber: Int,
    player: Player,
    participantState: ParticipantState,
    isFlagMode: Boolean,
    config: MinesweeperBoardConfig,
    onCellClick: (row: Int, col: Int) -> Unit,
    onToggleFlagMode: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = if (playerNumber == 1) MaterialTheme.colorScheme.primary else Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bàn chơi: ${player.name}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = onToggleFlagMode,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFlagMode) Color(0xFFE53935) else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (isFlagMode) Icons.Default.Flag else Icons.Default.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFlagMode) "ĐẶT CỜ 🚩" else "MỞ Ô 🔨",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            MinesweeperGridView(
                grid = participantState.grid,
                config = config,
                onCellClick = onCellClick,
                isBotView = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PvpResultDialog(
    outcome: PvpMatchOutcome,
    elapsedTimeFormatted: String,
    pvpState: PvpGameState,
    onRematchBoth: () -> Unit,
    onP1Stop: () -> Unit,
    onP2Stop: () -> Unit,
    onExitBoth: () -> Unit
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
                        text = "Cộng điểm: ${pvpState.player1.name} (+${outcome.p1Points}đ) | ${pvpState.player2.name} (+${outcome.p2Points}đ)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        textAlign = TextAlign.Center
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
                            Text("Tiến độ ${pvpState.player1.name}:", style = MaterialTheme.typography.bodySmall)
                            Text("${pvpState.player1State.revealedSafeCellsCount}/${pvpState.config.totalSafeCells} ô", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tiến độ ${pvpState.player2.name}:", style = MaterialTheme.typography.bodySmall)
                            Text("${pvpState.player2State.revealedSafeCellsCount}/${pvpState.config.totalSafeCells} ô", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Bạn có muốn tiếp tục thi đấu không?",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onRematchBoth,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cả 2 chọn Tiếp Tục (Ván mới)")
                }
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onP1Stop,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("${pvpState.player1.name} Dừng", fontSize = 10.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onExitBoth,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Thoát về trang chủ", fontSize = 10.sp, maxLines = 1)
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PvpMatchScreenPreview() {
    val sampleConfig = MinesweeperBoardConfig(9, 9, 10)
    val sampleBoard = MinesweeperEngine.generateMasterBoard(sampleConfig)

    TestTheme {
        PvpMatchScreen(
            pvpState = PvpGameState(
                config = sampleConfig,
                player1 = Player("p1", "Nguyễn Văn A (Bạn)"),
                player2 = Player("p2", "Trần Thị B"),
                player1State = ParticipantState(grid = sampleBoard, revealedSafeCellsCount = 20, flagsCount = 3),
                player2State = ParticipantState(grid = sampleBoard, revealedSafeCellsCount = 18, flagsCount = 2),
                elapsedTimeSec = 110,
                isGameActive = true
            ),
            onCellClick = { _, _, _ -> },
            onToggleFlagMode = {},
            onPlayerDisconnect = {},
            onRematchDecision = { _, _ -> },
            onExitGame = {}
        )
    }
}
