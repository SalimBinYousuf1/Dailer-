package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CallGradientEnd
import com.example.ui.theme.CallGradientStart
import com.example.ui.theme.StatusGrayLight

@Composable
fun BottomControls(
    onCallClick: () -> Unit,
    onRecentsClick: () -> Unit,
    onContactsClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = StatusGrayLight
) {
    val callInteraction = remember { MutableInteractionSource() }
    val isCallPressed by callInteraction.collectIsPressedAsState()

    val callScale by animateFloatAsState(
        targetValue = if (isCallPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "call_btn_scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: menu/recents-style control (tiny light-gray icon, no container)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onRecentsClick)
                .testTag("nav_recents_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = "Call History",
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        // Center: Circular call button with soft purple-to-pink gradient
        Box(
            modifier = Modifier
                .size(62.dp)
                .scale(callScale)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = Color(0x38BE8EF7),
                    ambientColor = Color(0x20F795B4)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            CallGradientStart,
                            CallGradientEnd
                        )
                    )
                )
                .clickable(
                    interactionSource = callInteraction,
                    indication = null,
                    onClick = onCallClick
                )
                .testTag("dialer_call_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = "Place call",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // Right: keypad/contacts/navigation control (tiny light-gray icon, no container)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onContactsClick)
                .testTag("nav_contacts_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Contacts & Settings",
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
