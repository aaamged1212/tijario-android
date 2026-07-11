package app.tijario.features.business.logo

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class LogoAssetManagerTests {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context = mockk<Context>()
    private lateinit var logoAssetManager: LogoAssetManager
    private lateinit var filesDir: File

    @Before
    fun setUp() {
        filesDir = tempFolder.newFolder("filesDir")
        every { context.filesDir } returns filesDir
        logoAssetManager = LogoAssetManager(context)
    }

    @Test
    fun getLocalLogoFile_returnsNullIfNoMetadata() {
        val result = logoAssetManager.getLocalLogoFile("user_1")
        assertNull(result)
    }
}
