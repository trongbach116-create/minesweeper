package ptit.e23cnpm01.hotrongbach.test.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ptit.e23cnpm01.hotrongbach.test.model.LeaderboardCriteria
import ptit.e23cnpm01.hotrongbach.test.model.LeaderboardEntry
import ptit.e23cnpm01.hotrongbach.test.model.Player
import ptit.e23cnpm01.hotrongbach.test.ui.theme.TestTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    entries: List<LeaderboardEntry>,
    selectedCriteria: LeaderboardCriteria,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCriteriaSelected: (LeaderboardCriteria) -> Unit,
    onNavigateBack: () -> Unit,
    selectedPlayerDetail: LeaderboardEntry?,
    onSelectPlayerDetail: (LeaderboardEntry?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Bảng Xếp Hạng Toàn Hệ Thống",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = selectedCriteria.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = !showInfoDialog }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Hướng dẫn tính điểm",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
        ) {
            // Explanation Info Banner (Collapsible)
            AnimatedVisibility(visible = showInfoDialog) {
                RulesInfoCard(onClose = { showInfoDialog = false })
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Tìm kiếm tên người chơi...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filter Chips for Criteria ("xem lần lượt theo từng tiêu chí")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Tiêu chí:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                LeaderboardCriteria.entries.forEach { criteria ->
                    val isSelected = criteria == selectedCriteria
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCriteriaSelected(criteria) },
                        label = {
                            Text(
                                text = criteria.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Top 3 Podium (If entries >= 3 and no search active)
            if (entries.size >= 3 && searchQuery.isBlank() && selectedCriteria == LeaderboardCriteria.DEFAULT) {
                Top3PodiumSection(top3 = entries.take(3), onItemClick = onSelectPlayerDetail)
            }

            // Table Header
            TableHeaderRow()

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Scrollable Leaderboard Table Rows
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Không tìm thấy người chơi phù hợp" else "Bảng xếp hạng trống",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(entries, key = { it.player.id }) { entry ->
                        LeaderboardRowItem(entry = entry, onClick = { onSelectPlayerDetail(entry) })
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Player Detail Statistics Dialog
    if (selectedPlayerDetail != null) {
        PlayerDetailDialog(
            entry = selectedPlayerDetail,
            onClose = { onSelectPlayerDetail(null) }
        )
    }
}

@Composable
fun Top3PodiumSection(top3: List<LeaderboardEntry>, onItemClick: (LeaderboardEntry) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "🏆 Top 3 Dẫn Đầu (Bấm để xem chi tiết)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Rank 2 (Silver) - Left
                if (top3.size > 1) {
                    PodiumCard(
                        entry = top3[1],
                        rankColor = Color(0xFFC0C0C0),
                        medalIcon = "🥈",
                        onClick = { onItemClick(top3[1]) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Rank 1 (Gold) - Center (Taller)
                if (top3.isNotEmpty()) {
                    PodiumCard(
                        entry = top3[0],
                        rankColor = Color(0xFFFFD700),
                        medalIcon = "🥇",
                        onClick = { onItemClick(top3[0]) },
                        modifier = Modifier.weight(1.1f),
                        isGold = true
                    )
                }

                // Rank 3 (Bronze) - Right
                if (top3.size > 2) {
                    PodiumCard(
                        entry = top3[2],
                        rankColor = Color(0xFFCD7F32),
                        medalIcon = "🥉",
                        onClick = { onItemClick(top3[2]) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun PodiumCard(
    entry: LeaderboardEntry,
    rankColor: Color,
    medalIcon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGold: Boolean = false
) {
    Column(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isGold) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
                width = if (isGold) 1.5.dp else 1.dp,
                color = rankColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = medalIcon, fontSize = if (isGold) 26.sp else 20.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = entry.player.name,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = entry.formattedPoints,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "TB ĐT: ${entry.formattedAvgOpponent}",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "TB T: ${entry.formattedAvgTime}",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TableHeaderRow() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hạng",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.width(42.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Người chơi",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1.3f)
            )
            Text(
                text = "Tổng điểm",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(0.9f),
                textAlign = TextAlign.End
            )
            Text(
                text = "TB Đối thủ",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(0.9f),
                textAlign = TextAlign.End
            )
            Text(
                text = "TB T.Gian",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(0.8f),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun LeaderboardRowItem(entry: LeaderboardEntry, onClick: () -> Unit) {
    val rank = entry.rank
    val rankBadgeBg = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val rankBadgeTextColor = when (rank) {
        1, 2, 3 -> Color.Black
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (rank <= 3) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceContainer
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Badge
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = rankBadgeBg,
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$rank",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = rankBadgeTextColor
                    )
                }
            }
        }

        // Player Name & Match Count
        Column(
            modifier = Modifier
                .weight(1.3f)
                .padding(end = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.player.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (rank <= 3) FontWeight.Bold else FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.player.isOnline) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                }
            }
            if (entry.totalMatches == 0) {
                Text(
                    text = "Chưa thi đấu",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 10.sp
                )
            } else {
                Text(
                    text = "${entry.totalMatches} trận (${entry.wins}T-${entry.draws}H-${entry.losses}B)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 10.sp
                )
            }
        }

        // Total Points Column
        Text(
            text = entry.formattedPoints,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (entry.totalPoints != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.weight(0.9f),
            textAlign = TextAlign.End
        )

        // Avg Opponent Points Column
        Text(
            text = entry.formattedAvgOpponent,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (entry.avgOpponentPoints != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.weight(0.9f),
            textAlign = TextAlign.End
        )

        // Avg Completion Time Column
        Text(
            text = entry.formattedAvgTime,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (entry.avgCompletionTimeSec != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.weight(0.8f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun PlayerDetailDialog(entry: LeaderboardEntry, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Thống Kê: ${entry.player.name}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Thứ hạng hiện tại",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Hạng #${entry.rank}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = entry.formattedPoints,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        DetailRow(label = "Tổng số trận đã đấu:", value = "${entry.totalMatches} trận")
                        DetailRow(label = "Thắng - Hòa - Thua:", value = "${entry.wins}Thắng / ${entry.draws}Hòa / ${entry.losses}Thua")
                        DetailRow(label = "TB điểm đối thủ:", value = entry.formattedAvgOpponent)
                        DetailRow(label = "TB thời gian thắng/hòa:", value = entry.formattedAvgTime)
                        DetailRow(label = "Trạng thái online:", value = if (entry.player.isOnline) "Đang Online 🟢" else "Ngoại tuyến 🔴")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onClose) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun RulesInfoCard(onClose: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF0D47A1)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quy Tắc Bảng Xếp Hạng (Module 4)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF0D47A1)
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color(0xFF0D47A1)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "• 1. Tổng điểm: Thắng = 1 điểm, Hòa = 0.5 điểm, Thua = 0 điểm.\n" +
                        "• 2. TB điểm đối thủ: Trung bình cộng tổng điểm các đối thủ đã thi đấu cùng.\n" +
                        "• 3. TB thời gian kết thúc: Trung bình thời gian hoàn thành các trận THẮNG và HÒA.\n" +
                        "• Thứ tự ưu tiên sắp xếp:\n" +
                        "   Tổng điểm giảm dần ➔ TB điểm đối thủ giảm dần ➔ TB thời gian thắng/hòa tăng dần.\n" +
                        "• Người chơi chưa thi đấu trận nào vẫn hiển thị trong bảng với giá trị trống (-).",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF0D47A1),
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LeaderboardScreenPreview() {
    val sampleEntries = listOf(
        LeaderboardEntry(
            rank = 1,
            player = Player("p1", "Nguyễn Văn A"),
            totalMatches = 3,
            wins = 2,
            draws = 1,
            losses = 0,
            totalPoints = 2.5,
            avgOpponentPoints = 1.83,
            avgCompletionTimeSec = 150.0
        ),
        LeaderboardEntry(
            rank = 2,
            player = Player("p2", "Trần Thị B"),
            totalMatches = 3,
            wins = 2,
            draws = 0,
            losses = 1,
            totalPoints = 2.0,
            avgOpponentPoints = 1.5,
            avgCompletionTimeSec = 145.0
        ),
        LeaderboardEntry(
            rank = 3,
            player = Player("p6", "Vũ Đức F"),
            totalMatches = 0,
            wins = 0,
            draws = 0,
            losses = 0,
            totalPoints = null,
            avgOpponentPoints = null,
            avgCompletionTimeSec = null
        )
    )

    TestTheme {
        LeaderboardScreen(
            entries = sampleEntries,
            selectedCriteria = LeaderboardCriteria.DEFAULT,
            searchQuery = "",
            onSearchQueryChange = {},
            onCriteriaSelected = {},
            onNavigateBack = {},
            selectedPlayerDetail = null,
            onSelectPlayerDetail = {}
        )
    }
}
