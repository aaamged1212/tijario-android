package app.tijario.ui.state

import org.junit.Assert.assertEquals
import org.junit.Test

class AppShellDataStateTest {
    @Test
    fun shellStateIgnoresNonShellSyncDetails() {
        val loading = TijarioDataUiState(userId = "user-1", isInitialLoading = true)

        assertEquals(
            loading.toAppShellDataState(),
            loading.copy(
                isRefreshing = true,
                lastSyncedAt = 123L,
                errorMessage = "safe error",
            ).toAppShellDataState(),
        )
    }

    @Test
    fun shellStateRetainsRoutingAndStartupSignals() {
        val state = TijarioDataUiState(userId = "user-1", isInitialLoading = true)

        assertEquals(
            AppShellDataState(
                userId = "user-1",
                isInitialLoading = true,
                hasCachedData = false,
            ),
            state.toAppShellDataState(),
        )
    }
}
