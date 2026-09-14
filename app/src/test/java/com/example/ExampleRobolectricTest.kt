package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.PhoneUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Dialer", appName)
  }

  @Test
  fun `test T9 conversion and matching`() {
    val t9 = PhoneUtils.charToT9('m')
    assertEquals('6', t9)

    val matches = PhoneUtils.matchesT9("666", "Mom", "+1234567890")
    assertTrue(matches)
  }

  @Test
  fun `test phone number normalization`() {
    val normalized = PhoneUtils.normalizeNumber("+1 (555) 123-4567")
    assertEquals("+15551234567", normalized)
  }
}
