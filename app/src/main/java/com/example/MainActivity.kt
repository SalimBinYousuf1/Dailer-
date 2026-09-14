package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.telecom.DialerCaller
import com.example.ui.DialerViewModel
import com.example.ui.ScreenTab
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.DialerScreen
import com.example.ui.screens.InCallScreen
import com.example.ui.screens.RecentsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DialerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)

        setContent {
            val settings by viewModel.settings.collectAsState()
            val dialerInput by viewModel.dialerInput.collectAsState()
            val selectedCountry by viewModel.selectedCountry.collectAsState()
            val suggestionCards by viewModel.suggestionCards.collectAsState()
            val allContacts by viewModel.allContacts.collectAsState()
            val callLogs by viewModel.callLogs.collectAsState()
            val activeCall by viewModel.activeCall.collectAsState()
            val activeTab by viewModel.activeTab.collectAsState()
            val blockedNumbers by viewModel.blockedNumbers.collectAsState()

            val context = LocalContext.current
            var showSimDialog by remember { mutableStateOf(false) }
            var pendingCallNumber by remember { mutableStateOf("") }
            var availableSims by remember { mutableStateOf<List<SubscriptionInfo>>(emptyList()) }

            // Runtime Permissions Request
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) {
                viewModel.loadData()
            }

            LaunchedEffect(Unit) {
                val permsToRequest = mutableListOf(
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.WRITE_CALL_LOG,
                    Manifest.permission.READ_PHONE_STATE
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }

                val missing = permsToRequest.filter {
                    ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                }
                if (missing.isNotEmpty()) {
                    permissionLauncher.launch(missing.toTypedArray())
                }
            }

            val isDark = when (settings.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            fun initiateCall(numberToCall: String) {
                val sims = DialerCaller.getAvailableSims(context)
                if (sims.size > 1) {
                    pendingCallNumber = numberToCall
                    availableSims = sims
                    showSimDialog = true
                } else {
                    viewModel.makeCall(context, numberToCall)
                }
            }

            MyApplicationTheme(themeMode = settings.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialThemeColor(isDark)
                ) {
                    // Active Ongoing / Incoming Call Screen
                    if (activeCall != null) {
                        InCallScreen(
                            callInfo = activeCall!!,
                            onAnswer = { viewModel.answerCall() },
                            onDecline = { viewModel.declineCall() },
                            onEndCall = { viewModel.endCall() },
                            onToggleMute = { viewModel.toggleMute() },
                            onToggleSpeaker = { viewModel.toggleSpeaker() },
                            onToggleHold = { viewModel.toggleHold() },
                            onSendDtmf = { viewModel.sendInCallDtmf(it) }
                        )
                    } else {
                        when (activeTab) {
                            ScreenTab.DIALER -> {
                                DialerScreen(
                                    number = dialerInput,
                                    selectedCountry = selectedCountry,
                                    suggestionCards = suggestionCards,
                                    isDarkTheme = isDark,
                                    onDigitPress = { viewModel.onDigitPress(it) },
                                    onZeroLongPress = { viewModel.onZeroLongPress() },
                                    onBackspace = { viewModel.onBackspace() },
                                    onClearAll = { viewModel.onClearDigits() },
                                    onPasteNumber = { viewModel.setNumber(it) },
                                    onSelectCountry = { viewModel.selectCountry(it) },
                                    onCallClick = { initiateCall(dialerInput) },
                                    onSuggestionCardClick = { suggestion ->
                                        if (suggestion.number.contains(Regex("[0-9]"))) {
                                            initiateCall(suggestion.number)
                                        } else {
                                            viewModel.switchTab(ScreenTab.CONTACTS)
                                        }
                                    },
                                    onOpenRecents = { viewModel.switchTab(ScreenTab.RECENTS) },
                                    onOpenContacts = { viewModel.switchTab(ScreenTab.CONTACTS) }
                                )
                            }
                            ScreenTab.RECENTS -> {
                                RecentsScreen(
                                    callLogs = callLogs,
                                    onBack = { viewModel.switchTab(ScreenTab.DIALER) },
                                    onCallNumber = { initiateCall(it) },
                                    onDeleteCallLog = { viewModel.deleteCallLogItem(it) },
                                    onClearAllLogs = { viewModel.clearAllCallHistory() },
                                    onBlockNumber = { viewModel.blockNumber(it) }
                                )
                            }
                            ScreenTab.CONTACTS -> {
                                ContactsScreen(
                                    contacts = allContacts,
                                    onBack = { viewModel.switchTab(ScreenTab.DIALER) },
                                    onCallContact = { initiateCall(it) },
                                    onOpenSettings = { viewModel.switchTab(ScreenTab.SETTINGS) }
                                )
                            }
                            ScreenTab.SETTINGS -> {
                                SettingsScreen(
                                    settings = settings,
                                    blockedNumbers = blockedNumbers,
                                    onBack = { viewModel.switchTab(ScreenTab.DIALER) },
                                    onToggleHaptics = { viewModel.toggleHaptics(it) },
                                    onToggleDtmf = { viewModel.toggleDtmf(it) },
                                    onSelectTheme = { viewModel.setThemeMode(it) },
                                    onBlockNumber = { num, reason -> viewModel.blockNumber(num, reason) },
                                    onUnblockNumber = { viewModel.unblockNumber(it) }
                                )
                            }
                        }
                    }

                    // Multi-SIM selection dialog
                    if (showSimDialog) {
                        AlertDialog(
                            onDismissRequest = { showSimDialog = false },
                            title = { Text("Call using SIM", fontWeight = FontWeight.Bold) },
                            text = {
                                Column {
                                    Text("Select SIM card for $pendingCallNumber")
                                    Spacer(modifier = Modifier.height(12.dp))
                                    LazyColumn {
                                        items(availableSims) { sim ->
                                            Surface(
                                                color = Color(0xFFF2EDFA),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable {
                                                        showSimDialog = false
                                                        viewModel.makeCall(context, pendingCallNumber)
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(14.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(
                                                            sim.displayName?.toString() ?: "SIM ${sim.simSlotIndex + 1}",
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Text(
                                                            sim.carrierName?.toString() ?: "",
                                                            fontSize = 12.sp,
                                                            color = Color(0xFF8E8E93)
                                                        )
                                                    }
                                                    Text(
                                                        "SIM ${sim.simSlotIndex + 1}",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFFBE8EF7),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showSimDialog = false }) {
                                    Text("Cancel")
                                }
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val data: Uri? = intent.data

        if (action == Intent.ACTION_DIAL || action == Intent.ACTION_VIEW) {
            val scheme = data?.scheme
            if (scheme == "tel") {
                val number = data.schemeSpecificPart
                if (!number.isNullOrEmpty()) {
                    viewModel.setNumber(number)
                }
            }
        }
    }

    private fun MaterialThemeColor(isDark: Boolean): Color {
        return if (isDark) Color(0xFF14131A) else Color(0xFFECE7F8)
    }
}
