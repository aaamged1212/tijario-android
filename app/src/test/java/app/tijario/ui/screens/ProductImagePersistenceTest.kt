package app.tijario.ui.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductImagePersistenceTest {
    @Test
    fun deletesStoredImageOnlyWhenNoReplacementWasSelected() {
        assertTrue(shouldDeleteStoredProductImage(hasSelectedImage = false, imageDeleted = true))
        assertFalse(shouldDeleteStoredProductImage(hasSelectedImage = true, imageDeleted = true))
        assertFalse(shouldDeleteStoredProductImage(hasSelectedImage = false, imageDeleted = false))
    }
}
