package app.tijario.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class AppHeaderUiContractTest {
    private val source = File("src/main/java/app/tijario/ui/TijarioApp.kt").readText()

    @Test
    fun homeUsesBrandNameAndCorePagesUseBrandLogo() {
        val header = source
            .substringAfter("val currentPage = pagerState.currentPage")
            .substringBefore("bottomBar =")

        assertTrue(header.contains("0 -> t(\"app_name\")"))
        assertTrue(header.contains("val usesBrandLogo = currentPage == 0 || currentPage == 1 || currentPage == 3 || currentPage == 4"))
        assertTrue(header.contains("R.drawable.tijario_splash_logo"))
    }

    @Test
    fun nonHomeTitlesAreCenteredWhileHomeRemainsStartAligned() {
        val header = source
            .substringAfter("val currentPage = pagerState.currentPage")
            .substringBefore("bottomBar =")

        assertTrue(header.contains(".align(Alignment.CenterStart)"))
        assertTrue(header.contains("if (currentPage == 0)"))
        assertTrue(header.contains("if (currentPage != 0)"))
        assertTrue(header.contains(".align(Alignment.Center)"))
    }
}
