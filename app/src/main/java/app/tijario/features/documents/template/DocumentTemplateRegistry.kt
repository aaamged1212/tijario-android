package app.tijario.features.documents.template

import app.tijario.features.documents.model.DocumentTemplateDefinition

object DocumentTemplateRegistry {
    const val defaultTemplateId: String = "tijario-classic"

    val templates: List<DocumentTemplateDefinition> = listOf(
        DocumentTemplateDefinition("tijario-classic", "Tijario Classic", 1, "documents/templates/tijario-classic", "classic", "#0F766E", "Balanced header, bordered cards, and traditional invoice rhythm."),
        DocumentTemplateDefinition("tijario-modern", "Tijario Modern", 1, "documents/templates/tijario-modern", "modern", "#0B5F59", "Solid header band and high contrast metadata treatment."),
        DocumentTemplateDefinition("tijario-minimal", "Tijario Minimal", 1, "documents/templates/tijario-minimal", "minimal", "#334155", "Quiet monochrome layout with reduced borders."),
        DocumentTemplateDefinition("tijario-compact", "Tijario Compact", 1, "documents/templates/tijario-compact", "compact", "#0F766E", "Dense layout for documents with many items."),
        DocumentTemplateDefinition("tijario-corporate", "Tijario Corporate", 1, "documents/templates/tijario-corporate", "corporate", "#1E40AF", "Structured blue business layout with squared cards."),
        DocumentTemplateDefinition("tijario-retail", "Tijario Retail", 1, "documents/templates/tijario-retail", "retail", "#0891B2", "Retail-friendly side accent and alternating item rows."),
        DocumentTemplateDefinition("tijario-elegant", "Tijario Elegant", 1, "documents/templates/tijario-elegant", "elegant", "#7C3AED", "Centered identity and softer hierarchy for polished quotes."),
        DocumentTemplateDefinition("tijario-bold", "Tijario Bold", 1, "documents/templates/tijario-bold", "bold", "#B91C1C", "Large title, strong total treatment, and assertive accents."),
        DocumentTemplateDefinition("tijario-service", "Tijario Service", 1, "documents/templates/tijario-service", "service", "#15803D", "Single-column party treatment for service providers."),
        DocumentTemplateDefinition("tijario-premium", "Tijario Premium", 1, "documents/templates/tijario-premium", "premium", "#92400E", "Premium top rule and warm metadata surfaces."),
    )

    fun requireTemplate(id: String?): DocumentTemplateDefinition =
        templates.firstOrNull { it.id == id } ?: templates.first { it.id == defaultTemplateId }
}
