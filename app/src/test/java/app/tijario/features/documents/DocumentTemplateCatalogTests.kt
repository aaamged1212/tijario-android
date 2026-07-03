package app.tijario.features.documents

import app.tijario.features.documents.template.DocumentTemplateRegistry
import app.tijario.features.documents.ui.isTemplateAvailableForSelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentTemplateCatalogTests {
    @Test
    fun canonicalCatalogKeepsClassicAsFreeTemplate() {
        val templates = DocumentTemplateRegistry.templates
        assertTrue(templates.size >= 20)
        assertTrue(templates.any { it.id == "tijario-classic" })
        assertEquals("tijario-classic", DocumentTemplateRegistry.defaultTemplateId)
    }

    @Test
    fun legacyAliasesNormalizeToCanonicalIds() {
        assertEquals("tijario-classic", DocumentTemplateRegistry.normalizeId("classic"))
        assertEquals("tijario-classic", DocumentTemplateRegistry.normalizeId("tijario-basic"))
        assertEquals("tijario-modern", DocumentTemplateRegistry.normalizeId("modern/teal"))
        assertEquals("tijario-minimal", DocumentTemplateRegistry.normalizeId("minimal/slate"))
        assertEquals("tijario-classic", DocumentTemplateRegistry.normalizeId("unknown"))
    }

    @Test
    fun templateAvailabilityRequiresLoadedEntitlements() {
        assertFalse(isTemplateAvailableForSelection(false, listOf("tijario-classic"), "tijario-classic"))
        assertTrue(isTemplateAvailableForSelection(true, listOf("tijario-classic"), "tijario-classic"))
        assertTrue(isTemplateAvailableForSelection(true, listOf("tijario-classic", "tijario-modern"), "tijario-modern"))
        assertFalse(isTemplateAvailableForSelection(true, listOf("tijario-classic"), "tijario-modern"))
    }
}
