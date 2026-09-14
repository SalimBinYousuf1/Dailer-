package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusGrayLight
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusArea(
    modifier: Modifier = Modifier,
    contentColor: Color = StatusGrayLight
) {
    var currentTime by remember {
        mutableStateOf(SimpleDateFormat("h:mm", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("h:mm", Locale.getDefault()).format(Date())
            delay(30000)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status bar time
        Text(
            text = currentTime,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            letterSpacing = 0.2.sp
        )

        // Status bar indicators (Cellular, WiFi, Battery)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NetworkCell,
                contentDescription = "Cellular signal",
                modifier = Modifier.size(13.dp),
                tint = contentColor
            )
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Wi-Fi signal",
                modifier = Modifier.size(13.dp),
                tint = contentColor
            )
            Icon(
                imageVector = Icons.Default.BatteryFull,
                contentDescription = "Battery status",
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
        }
    }
}
