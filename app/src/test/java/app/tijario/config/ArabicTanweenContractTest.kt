package app.tijario.config

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ArabicTanweenContractTest {
    @Test
    fun userFacingArabicPlacesFathatanAfterFinalAlif() {
        val invalidSequence = "\u064B\u0627"
        val sourceRoot = File("src/main")
        val textExtensions = setOf("kt", "xml", "json", "html")
        val offenders = sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension.lowercase() in textExtensions }
            .filter { !it.name.endsWith("Localization.kt") && !it.name.endsWith("AiScreens.kt") }
            .filter { it.readText().contains(invalidSequence) }
            .map { it.relativeTo(sourceRoot).invariantSeparatorsPath }
            .toList()

        assertTrue(
            "Fathatan must follow the final alif in other files: $offenders",
            offenders.isEmpty(),
        )
    }
}
