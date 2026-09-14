package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CountryCode
import com.example.ui.ContactSuggestion
import com.example.ui.components.BottomControls
import com.example.ui.components.CountryPickerDialog
import com.example.ui.components.Keypad
import com.example.ui.components.NumberDisplay
import com.example.ui.components.StatusArea
import com.example.ui.components.SuggestionCards
import com.example.ui.theme.BgLavender
import com.example.ui.theme.BgLilacPink
import com.example.ui.theme.DarkBgLavender
import com.example.ui.theme.DarkBgPink
import com.example.ui.theme.KeypadLettersDark
import com.example.ui.theme.KeypadLettersLight
import com.example.ui.theme.KeypadNumberDark
import com.example.ui.theme.KeypadNumberLight
import com.example.ui.theme.PhoneSurfaceDark
import com.example.ui.theme.PhoneSurfaceLight
import com.example.ui.theme.StatusGrayDark
import com.example.ui.theme.StatusGrayLight

@Composable
fun DialerScreen(
    number: String,
    selectedCountry: CountryCode,
    suggestionCards: Pair<ContactSuggestion, ContactSuggestion>,
    isDarkTheme: Boolean,
    onDigitPress: (Char) -> Unit,
    onZeroLongPress: () -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    onPasteNumber: (String) -> Unit,
    onSelectCountry: (CountryCode) -> Unit,
    onCallClick: () -> Unit,
    onSuggestionCardClick: (ContactSuggestion) -> Unit,
    onOpenRecents: () -> Unit,
    onOpenContacts: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCountryPicker by remember { mutableStateOf(false) }

    // Full-screen background: very light pastel lavender transitioning to pale pink/lilac
    val bgColors = if (isDarkTheme) {
        listOf(DarkBgLavender, DarkBgPink)
    } else {
        listOf(BgLavender, BgLilacPink)
    }

    val phoneBg = if (isDarkTheme) PhoneSurfaceDark else PhoneSurfaceLight
    val numberColor = if (isDarkTheme) KeypadNumberDark else KeypadNumberLight
    val lettersColor = if (isDarkTheme) KeypadLettersDark else KeypadLettersLight
    val statusColor = if (isDarkTheme) StatusGrayDark else StatusGrayLight

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = Brush.horizontalGradient(bgColors)),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val isCompact = maxWidth < 480.dp

            // Phone surface styling: centered inside a tall white phone-shaped surface
            // Approximately 9:19 proportion, large rounded corners, subtle elevation
            Surface(
                modifier = Modifier
                    .then(
                        if (isCompact) {
                            Modifier
                                .fillMaxWidth(0.92f)
                                .fillMaxHeight(0.96f)
                        } else {
                            Modifier
                                .widthIn(min = 340.dp, max = 380.dp)
                                .aspectRatio(9f / 19f)
                        }
                    )
                    .shadow(
                        elevation = if (isDarkTheme) 20.dp else 16.dp,
                        shape = RoundedCornerShape(42.dp),
                        spotColor = if (isDarkTheme) Color(0x60000000) else Color(0x286B5E87),
                        ambientColor = if (isDarkTheme) Color(0x30000000) else Color(0x18000000)
                    )
                    .clip(RoundedCornerShape(42.dp)),
                color = phoneBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Top status area (Time, Cellular, WiFi, Battery)
                    StatusArea(
                        contentColor = statusColor
                    )

                    // 2. Contact suggestion cards (immediately below status area)
                    SuggestionCards(
                        card1 = suggestionCards.first,
                        card2 = suggestionCards.second,
                        onCardClick = onSuggestionCardClick
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3. Number display (centered country code & phone number)
                    NumberDisplay(
                        number = number,
                        countryCode = selectedCountry,
                        onCountryClick = { showCountryPicker = true },
                        onBackspace = onBackspace,
                        onClearAll = onClearAll,
                        onPasteNumber = onPasteNumber,
                        textColor = numberColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 4. Keypad (3x4 telephone keypad matching reference exactly)
                    Keypad(
                        onDigitPress = onDigitPress,
                        onZeroLongPress = onZeroLongPress,
                        numberColor = numberColor,
                        lettersColor = lettersColor,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 5. Minimal bottom navigation & call button
                    BottomControls(
                        onCallClick = onCallClick,
                        onRecentsClick = onOpenRecents,
                        onContactsClick = onOpenContacts,
                        iconTint = statusColor
                    )
                }
            }
        }
    }

    if (showCountryPicker) {
        CountryPickerDialog(
            onDismiss = { showCountryPicker = false },
            onSelectCountry = {
                onSelectCountry(it)
                showCountryPicker = false
            },
            selectedCode = selectedCountry.code
        )
    }
}
