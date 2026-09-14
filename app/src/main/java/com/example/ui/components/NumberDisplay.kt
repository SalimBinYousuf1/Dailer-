package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CountryCode
import com.example.ui.theme.KeypadNumberLight
import com.example.ui.theme.StatusGrayLight
import com.example.util.PhoneUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NumberDisplay(
    number: String,
    countryCode: CountryCode,
    onCountryClick: () -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    onPasteNumber: (String) -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = KeypadNumberLight
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showContextMenu by remember { mutableStateOf(false) }

    // Dynamic typography scaling based on length
    val fontSize = when {
        number.length > 14 -> 20.sp
        number.length > 11 -> 24.sp
        number.length > 8 -> 28.sp
        else -> 32.sp
    }

    val formattedNumber = PhoneUtils.formatDisplayNumber(number)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Country Code & Flag Indicator
        Surface(
            onClick = onCountryClick,
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            modifier = Modifier.testTag("country_code_selector")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${countryCode.flag} ${countryCode.code}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = StatusGrayLight
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select country code",
                    tint = StatusGrayLight,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Typed phone number with copy/paste combinedClickable & Backspace button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .combinedClickable(
                        onClick = {
                            if (number.isEmpty()) {
                                clipboardManager.getText()?.text?.let { clipText ->
                                    val cleaned = clipText.replace("[^0-9+]".toRegex(), "")
                                    if (cleaned.isNotEmpty()) {
                                        showContextMenu = true
                                    }
                                }
                            } else {
                                showContextMenu = true
                            }
                        },
                        onLongClick = {
                            showContextMenu = true
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (number.isEmpty()) " " else formattedNumber,
                    fontSize = fontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    modifier = Modifier.testTag("typed_phone_number_display")
                )

                // Context menu for Copy / Paste / Cut
                DropdownMenu(
                    expanded = showContextMenu,
                    onDismissRequest = { showContextMenu = false }
                ) {
                    if (number.isNotEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Copy") },
                            onClick = {
                                clipboardManager.setText(AnnotatedString(number))
                                Toast.makeText(context, "Number copied", Toast.LENGTH_SHORT).show()
                                showContextMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cut") },
                            onClick = {
                                clipboardManager.setText(AnnotatedString(number))
                                onClearAll()
                                Toast.makeText(context, "Number cut", Toast.LENGTH_SHORT).show()
                                showContextMenu = false
                            }
                        )
                    }
                    val clipboardText = clipboardManager.getText()?.text
                    if (!clipboardText.isNullOrBlank()) {
                        DropdownMenuItem(
                            text = { Text("Paste") },
                            onClick = {
                                val cleaned = clipboardText.replace("[^0-9+]".toRegex(), "")
                                if (cleaned.isNotEmpty()) {
                                    onPasteNumber(cleaned)
                                }
                                showContextMenu = false
                            }
                        )
                    }
                }
            }

            // Backspace icon button positioned on the right
            if (number.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp)
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .combinedClickable(
                            onClick = onBackspace,
                            onLongClick = onClearAll
                        )
                        .testTag("dialer_backspace_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace / Clear",
                        tint = StatusGrayLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
