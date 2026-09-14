package com.example.ui.screens

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BlockedNumberEntity
import com.example.data.local.DialerSettingsEntity
import com.example.telecom.DialerCaller
import com.example.ui.theme.BgLavender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: DialerSettingsEntity,
    blockedNumbers: List<BlockedNumberEntity>,
    onBack: () -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleDtmf: (Boolean) -> Unit,
    onSelectTheme: (String) -> Unit,
    onBlockNumber: (String, String) -> Unit,
    onUnblockNumber: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isDefaultDialer by remember { mutableStateOf(DialerCaller.isDefaultDialer(context)) }
    var showAddBlockDialog by remember { mutableStateOf(false) }
    var blockInputNumber by remember { mutableStateOf("") }

    val roleRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isDefaultDialer = DialerCaller.isDefaultDialer(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", fontWeight = FontWeight.SemiBold, fontSize = 19.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = BgLavender,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Default Phone App Card
            item {
                SettingsSectionHeader("Default Phone App")
                Surface(
                    color = Color(0xF2FFFFFF),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE2D6F6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = Color(0xFF8B5CF6),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Default Dialer Role",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        if (isDefaultDialer) "Active default phone app" else "Not set as default phone",
                                        color = if (isDefaultDialer) Color(0xFF34C759) else Color(0xFF8E8E93),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        if (!isDefaultDialer) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        val roleManager = context.getSystemService(RoleManager::class.java)
                                        roleManager?.let { rm ->
                                            val intent = rm.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                                            roleRequestLauncher.launch(intent)
                                        }
                                    } else {
                                        val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                                            putExtra(
                                                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                                                context.packageName
                                            )
                                        }
                                        roleRequestLauncher.launch(intent)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE8EF7)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Set as Default Phone App")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Appearance & Themes
            item {
                SettingsSectionHeader("Appearance")
                Surface(
                    color = Color(0xF2FFFFFF),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = Color(0xFF8B5CF6))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Theme Mode", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("SYSTEM", "LIGHT", "DARK").forEach { mode ->
                                val isSelected = settings.themeMode == mode
                                Surface(
                                    color = if (isSelected) Color(0xFFBE8EF7) else Color(0xFFF3F3F7),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSelectTheme(mode) }
                                ) {
                                    Text(
                                        text = mode.lowercase().replaceFirstChar { it.uppercase() },
                                        color = if (isSelected) Color.White else Color(0xFF1E1E24),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Feedback & Audio
            item {
                SettingsSectionHeader("Feedback & Sounds")
                Surface(
                    color = Color(0xF2FFFFFF),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        SettingsToggleRow(
                            icon = Icons.Default.Vibration,
                            title = "Haptic Feedback",
                            subtitle = "Vibrate on keypad press and call actions",
                            checked = settings.hapticsEnabled,
                            onCheckedChange = onToggleHaptics
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SettingsToggleRow(
                            icon = Icons.Default.VolumeUp,
                            title = "Dial-Pad Sounds",
                            subtitle = "Play DTMF audio tones when tapping numbers",
                            checked = settings.dtmfToneEnabled,
                            onCheckedChange = onToggleDtmf
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Blocked Numbers & Spam
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsSectionHeader("Blocked Numbers & Spam")
                    TextButton(onClick = { showAddBlockDialog = true }) {
                        Text("+ Add", color = Color(0xFFBE8EF7), fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    color = Color(0xF2FFFFFF),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (blockedNumbers.isEmpty()) {
                        Text(
                            text = "No blocked numbers. Calls from blocked numbers will be declined.",
                            color = Color(0xFF8E8E93),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        Column(modifier = Modifier.padding(8.dp)) {
                            blockedNumbers.forEach { blocked ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(blocked.number, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text(blocked.reason, fontSize = 12.sp, color = Color(0xFF8E8E93))
                                    }
                                    IconButton(onClick = { onUnblockNumber(blocked.number) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Unblock", tint = Color(0xFFFF3B30))
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showAddBlockDialog) {
        AlertDialog(
            onDismissRequest = { showAddBlockDialog = false },
            title = { Text("Block a Number") },
            text = {
                OutlinedTextField(
                    value = blockInputNumber,
                    onValueChange = { blockInputNumber = it },
                    placeholder = { Text("Enter phone number") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (blockInputNumber.isNotBlank()) {
                            onBlockNumber(blockInputNumber.trim(), "Spam / Blocked")
                            blockInputNumber = ""
                            showAddBlockDialog = false
                        }
                    }
                ) {
                    Text("Block", color = Color(0xFFFF3B30))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBlockDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF7A7585),
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF8E8E93))
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFBE8EF7)
            )
        )
    }
}
