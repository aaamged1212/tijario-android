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
        assertEquals("tijario-modern", DocumentTemplateRegistry.normalizeId("modern"))
        assertEquals("tijario-minimal", DocumentTemplateRegistry.normalizeId("minimal"))
        assertEquals("tijario-classic", DocumentTemplateRegistry.normalizeId("tijario-basic"))
        assertEquals("tijario-classic", DocumentTemplateRegistry.normalizeId("basic/teal"))
        assertEquals("tijario-modern", DocumentTemplateRegistry.normalizeId("modern/teal"))
        assertEquals("tijario-minimal", DocumentTemplateRegistry.normalizeId("minimal/slate"))
        assertEquals("tijario-classic", DocumentTemplateRegistry.normalizeId("tijario-invoice-maker"))
        assertEquals("tijario-classic", DocumentTemplateRegistry.normalizeId("unknown"))
    }

    @Test
    fun templateAvailabilityNormalizesAllowedTemplateIds() {
        assertFalse(isTemplateAvailableForSelection(false, listOf("classic"), "tijario-classic"))
        assertTrue(isTemplateAvailableForSelection(true, listOf("classic"), "tijario-classic"))
        assertTrue(isTemplateAvailableForSelection(true, listOf("modern"), "tijario-modern"))
        assertTrue(isTemplateAvailableForSelection(true, listOf("minimal"), "tijario-minimal"))
        assertTrue(isTemplateAvailableForSelection(true, listOf("tijario-classic"), "tijario-classic"))
        assertTrue(
            isTemplateAvailableForSelection(
                true,
                listOf(
                    "tijario-classic",
                    "tijario-modern",
                    "tijario-minimal",
                    "tijario-compact",
                    "tijario-corporate",
                    "tijario-retail",
                    "tijario-elegant",
                    "tijario-bold",
                    "tijario-service",
                    "tijario-premium",
                    "tijario-aqua",
                    "tijario-lime",
                    "tijario-ruby",
                    "tijario-ink",
                    "tijario-blue-band",
                    "tijario-navy-band",
                    "tijario-gold-band",
                    "tijario-orange-band",
                    "tijario-cyan-band",
                    "tijario-violet-band",
                    "tijario-forest-band",
                    "tijario-block",
                    "tijario-outline",
                    "tijario-slate-cover",
                    "tijario-emerald-cover",
                    "tijario-leaf",
                ),
                "tijario-modern",
            )
        )
        assertFalse(isTemplateAvailableForSelection(true, listOf("tijario-classic"), "tijario-modern"))
    }
}
