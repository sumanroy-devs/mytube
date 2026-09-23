package io.github.aedev.flow.ui.components.shared

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Pins the badge's three states: hidden at zero (so an empty bell never shows a bubble), the
 * exact count below ten, and the fixed "9+" cap that keeps the circle from widening.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], application = Application::class)
class FlowCountBadgeTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `badge stays hidden at zero`() {
        rule.setContent { MaterialTheme { FlowCountBadge(count = 0) } }

        assertThat(rule.onRoot().fetchSemanticsNode().children).isEmpty()
    }

    @Test
    fun `badge shows the exact count below ten`() {
        rule.setContent { MaterialTheme { FlowCountBadge(count = 7) } }

        rule.onNodeWithText("7").assertExists()
    }

    @Test
    fun `badge caps at nine plus`() {
        rule.setContent { MaterialTheme { FlowCountBadge(count = 42) } }

        rule.onNodeWithText("9+").assertExists()
        rule.onNodeWithText("42").assertDoesNotExist()
    }
}
