package app.tijario.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveLayoutTests {
    @Test
    fun widthBoundariesUseCurrentContainerWidth() {
        assertEquals(AdaptiveWidthClass.ExtraCompact, adaptiveLayoutInfo(299.dp).widthClass)
        assertEquals(AdaptiveWidthClass.Compact, adaptiveLayoutInfo(300.dp).widthClass)
        assertEquals(AdaptiveWidthClass.Standard, adaptiveLayoutInfo(360.dp).widthClass)
        assertEquals(AdaptiveWidthClass.Wide, adaptiveLayoutInfo(480.dp).widthClass)
    }

    @Test
    fun extraCompactHidesInactiveBottomLabelsAndStacksActions() {
        val extraCompact = adaptiveLayoutInfo(280.dp)
        val standard = adaptiveLayoutInfo(390.dp)

        assertTrue(extraCompact.shouldStackActions)
        assertFalse(extraCompact.showInactiveBottomLabels)
        assertFalse(standard.shouldStackActions)
        assertTrue(standard.showInactiveBottomLabels)
    }
}
