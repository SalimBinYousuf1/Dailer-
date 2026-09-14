package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CallLogItem
import com.example.data.model.CallType
import com.example.ui.theme.BgLavender
import com.example.ui.theme.CallGradientEnd
import com.example.ui.theme.CallGradientStart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentsScreen(
    callLogs: List<CallLogItem>,
    onBack: () -> Unit,
    onCallNumber: (String) -> Unit,
    onDeleteCallLog: (Long) -> Unit,
    onClearAllLogs: () -> Unit,
    onBlockNumber: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var showClearDialog by remember { mutableStateOf(false) }

    val filteredLogs = remember(callLogs, selectedFilter) {
        when (selectedFilter) {
            "Missed" -> callLogs.filter { it.type == CallType.MISSED || it.type == CallType.REJECTED }
            "Incoming" -> callLogs.filter { it.type == CallType.INCOMING }
            "Outgoing" -> callLogs.filter { it.type == CallType.OUTGOING }
            else -> callLogs
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Call History", fontWeight = FontWeight.SemiBold, fontSize = 19.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (callLogs.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = BgLavender,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips (All, Missed, Incoming, Outgoing)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Missed", "Incoming", "Outgoing").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFC48BF8),
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            if (filteredLogs.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(R.drawable.avatar_monster_pink),
                            contentDescription = "Empty",
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No ${selectedFilter.lowercase()} calls found",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF5C5765)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Calls made and received will appear here",
                            fontSize = 13.sp,
                            color = Color(0xFF8E8E93)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        CallLogRow(
                            log = log,
                            onCall = { onCallNumber(log.number) },
                            onDelete = { onDeleteCallLog(log.id) },
                            onBlock = { onBlockNumber(log.number) }
                        )
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Call History") },
            text = { Text("Are you sure you want to remove all call history records?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllLogs()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear All", color = Color(0xFFFF3B30))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CallLogRow(
    log: CallLogItem,
    onCall: () -> Unit,
    onDelete: () -> Unit,
    onBlock: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var showMenu by remember { mutableStateOf(false) }

    val dateFormatted = remember(log.timestamp) {
        val now = System.currentTimeMillis()
        if (DateUtils.isToday(log.timestamp)) {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(log.timestamp))
        } else {
            SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(log.timestamp))
        }
    }

    val (typeIcon, typeTint) = when (log.type) {
        CallType.MISSED, CallType.REJECTED -> Pair(Icons.AutoMirrored.Filled.CallMissed, Color(0xFFFF3B30))
        CallType.OUTGOING -> Pair(Icons.AutoMirrored.Filled.CallMade, Color(0xFF007AFF))
        CallType.INCOMING -> Pair(Icons.AutoMirrored.Filled.CallReceived, Color(0xFF34C759))
        else -> Pair(Icons.AutoMirrored.Filled.CallReceived, Color(0xFF8E8E93))
    }

    Surface(
        color = Color(0xF0FFFFFF),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clickable(onClick = onCall)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Call type indicator icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(typeTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = log.type.name,
                        tint = typeTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.cachedName ?: log.number.ifEmpty { "Unknown" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E1E24),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = dateFormatted,
                            fontSize = 12.sp,
                            color = Color(0xFF8E8E93)
                        )
                        if (log.durationSeconds > 0) {
                            val durationText = "${log.durationSeconds / 60}m ${log.durationSeconds % 60}s"
                            Text(
                                text = " • $durationText",
                                fontSize = 12.sp,
                                color = Color(0xFF8E8E93)
                            )
                        }
                    }
                }
            }

            // Right action buttons: Call & More
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCall) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color(0xFF34C759),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Copy Number") },
                            onClick = {
                                clipboardManager.setText(AnnotatedString(log.number))
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Block Number") },
                            onClick = {
                                onBlock()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Entry", color = Color(0xFFFF3B30)) },
                            onClick = {
                                onDelete()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}
