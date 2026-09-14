package com.example.ui.screens

import android.telecom.Call
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.telecom.ActiveCallInfo
import com.example.ui.components.Keypad
import com.example.ui.theme.BgLavender
import com.example.ui.theme.BgLilacPink

@Composable
fun InCallScreen(
    callInfo: ActiveCallInfo,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleHold: () -> Unit,
    onSendDtmf: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    var showInCallKeypad by remember { mutableStateOf(false) }

    val stateText = when (callInfo.state) {
        Call.STATE_RINGING -> "Incoming call..."
        Call.STATE_DIALING -> "Dialing..."
        Call.STATE_CONNECTING -> "Connecting..."
        Call.STATE_HOLDING -> "On Hold"
        Call.STATE_ACTIVE -> {
            val minutes = callInfo.elapsedSeconds / 60
            val seconds = callInfo.elapsedSeconds % 60
            String.format("%02d:%02d", minutes, seconds)
        }
        Call.STATE_DISCONNECTED -> "Call Ended"
        else -> "Calling..."
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        BgLavender,
                        Color(0xFFF3E7F2),
                        BgLilacPink
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top caller identity
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                // Circular Monster Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(12.dp, CircleShape, spotColor = Color(0x30BE8EF7))
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.avatar_monster_blue),
                        contentDescription = "Caller Avatar",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = callInfo.callerName ?: callInfo.number.ifEmpty { "Unknown Caller" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E24)
                )

                if (!callInfo.callerName.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = callInfo.number,
                        fontSize = 15.sp,
                        color = Color(0xFF7A7A85)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stateText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (callInfo.state == Call.STATE_HOLDING) Color(0xFFE57373) else Color(0xFF9075A8)
                )
            }

            // In-call keypad popup if requested
            AnimatedVisibility(visible = showInCallKeypad) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xF5FFFFFF),
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Keypad(
                        onDigitPress = onSendDtmf,
                        onZeroLongPress = { onSendDtmf('0') },
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // In-Call Action Grid (Mute, Keypad, Speaker, Hold)
            if (!showInCallKeypad && callInfo.state != Call.STATE_RINGING) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        InCallActionButton(
                            icon = if (callInfo.isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                            label = if (callInfo.isMuted) "Unmute" else "Mute",
                            isActive = callInfo.isMuted,
                            onClick = onToggleMute
                        )
                        InCallActionButton(
                            icon = Icons.Filled.Dialpad,
                            label = "Keypad",
                            isActive = showInCallKeypad,
                            onClick = { showInCallKeypad = !showInCallKeypad }
                        )
                        InCallActionButton(
                            icon = Icons.Filled.VolumeUp,
                            label = if (callInfo.isSpeakerOn) "Earpiece" else "Speaker",
                            isActive = callInfo.isSpeakerOn,
                            onClick = onToggleSpeaker
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        InCallActionButton(
                            icon = if (callInfo.isHolding) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            label = if (callInfo.isHolding) "Unhold" else "Hold",
                            isActive = callInfo.isHolding,
                            onClick = onToggleHold
                        )
                    }
                }
            }

            // Bottom controls: Answer/Decline if ringing, or End Call if connected
            if (callInfo.state == Call.STATE_RINGING) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decline button (Red)
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(8.dp, CircleShape, spotColor = Color(0x40FF3B30))
                            .clip(CircleShape)
                            .background(Color(0xFFFF3B30))
                            .clickable(onClick = onDecline)
                            .testTag("decline_call_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CallEnd,
                            contentDescription = "Decline call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Answer button (Green)
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(8.dp, CircleShape, spotColor = Color(0x4034C759))
                            .clip(CircleShape)
                            .background(Color(0xFF34C759))
                            .clickable(onClick = onAnswer)
                            .testTag("answer_call_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Answer call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // End call button (Red)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(10.dp, CircleShape, spotColor = Color(0x40FF3B30))
                            .clip(CircleShape)
                            .background(Color(0xFFFF3B30))
                            .clickable(onClick = onEndCall)
                            .testTag("end_call_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InCallActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isActive) Color(0xFF4A4458) else Color(0xCCFFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else Color(0xFF2C2C34),
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF5A5A65),
            fontWeight = FontWeight.Medium
        )
    }
}
