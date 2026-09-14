package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.CountryCode
import com.example.ui.AvatarMonsterType
import com.example.ui.ContactSuggestion
import com.example.ui.screens.DialerScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun dialer_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme(themeMode = "LIGHT") {
        DialerScreen(
          number = "9841234567",
          selectedCountry = CountryCode("Nepal", "+977", "NP", "🇳🇵"),
          suggestionCards = Pair(
            ContactSuggestion("Sarah Jenkins", "+977 9841 234567", true, AvatarMonsterType.BLUE_PURPLE),
            ContactSuggestion("Alex Rivera", "+977 9812 876543", false, AvatarMonsterType.PINK)
          ),
          isDarkTheme = false,
          onDigitPress = {},
          onZeroLongPress = {},
          onBackspace = {},
          onClearAll = {},
          onPasteNumber = {},
          onSelectCountry = {},
          onCallClick = {},
          onSuggestionCardClick = {},
          onOpenRecents = {},
          onOpenContacts = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dialer.png")
  }
}
