package app.tijario.features.documents.template

object DocumentTemplateValidator {
    fun validateRegistry(): List<String> {
        val errors = mutableListOf<String>()
        val templates = DocumentTemplateRegistry.templates
        if (templates.size < 10) errors += "Expected at least 10 templates."
        val duplicateIds = templates.groupBy { it.id }.filterValues { it.size > 1 }.keys
        if (duplicateIds.isNotEmpty()) errors += "Duplicate template IDs: ${duplicateIds.joinToString()}"
        val duplicateFamilies = templates.groupBy { it.family to it.palette }.filterValues { it.size > 1 }.keys
        if (duplicateFamilies.isNotEmpty()) errors += "Duplicate family/palette pairs: ${duplicateFamilies.joinToString()}"
        if (templates.any { it.version <= 0 }) errors += "Every template needs a positive version."
        return errors
    }
}
