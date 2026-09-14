package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KeypadLettersLight
import com.example.ui.theme.KeypadNumberLight

data class KeypadKey(
    val digit: Char,
    val letters: String? = null
)

@Composable
fun Keypad(
    onDigitPress: (Char) -> Unit,
    onZeroLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    numberColor: Color = KeypadNumberLight,
    lettersColor: Color = KeypadLettersLight
) {
    val rows = remember {
        listOf(
            listOf(KeypadKey('1'), KeypadKey('2', "ABC"), KeypadKey('3', "DEF")),
            listOf(KeypadKey('4', "GHI"), KeypadKey('5', "JKL"), KeypadKey('6', "MNO")),
            listOf(KeypadKey('7', "PQRS"), KeypadKey('8', "TUV"), KeypadKey('9', "WXYZ")),
            listOf(KeypadKey('*'), KeypadKey('0', "+"), KeypadKey('#'))
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (key in row) {
                    KeypadButton(
                        key = key,
                        onPress = { onDigitPress(key.digit) },
                        onLongPress = {
                            if (key.digit == '0') {
                                onZeroLongPress()
                            } else {
                                onDigitPress(key.digit)
                            }
                        },
                        numberColor = numberColor,
                        lettersColor = lettersColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeypadButton(
    key: KeypadKey,
    onPress: () -> Unit,
    onLongPress: () -> Unit,
    numberColor: Color,
    lettersColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "key_scale"
    )

    Box(
        modifier = Modifier
            .size(width = 76.dp, height = 64.dp)
            .scale(scale)
            .clip(CircleShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null, // Floating borderless aesthetic matching reference
                onClick = onPress,
                onLongClick = onLongPress
            )
            .testTag("keypad_key_${key.digit}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = key.digit.toString(),
                fontSize = if (key.digit == '*' || key.digit == '#') 28.sp else 29.sp,
                fontWeight = FontWeight.Normal,
                color = numberColor,
                lineHeight = 30.sp
            )

            if (!key.letters.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = key.letters,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = lettersColor,
                    letterSpacing = 2.sp,
                    lineHeight = 10.sp
                )
            } else {
                Spacer(modifier = Modifier.height(11.dp))
            }
        }
    }
}
