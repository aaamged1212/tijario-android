package app.tijario.ui.screens

import app.tijario.config.AppLanguage
import app.tijario.config.Localization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentIdentityLockTests {
    @Test
    fun editModeLocksIdentityFields() {
        assertTrue(isDocumentIdentityEditable(false))
        assertFalse(isDocumentIdentityEditable(true))
    }

    @Test
    fun identityLockStringsAreLocalized() {
        assertEquals(
            "لا يمكن تعديله بعد حفظ المستند",
            documentIdentityLockedHint(AppLanguage.AR),
        )
        assertEquals(
            "Cannot be changed after the document is saved",
            documentIdentityLockedHint(AppLanguage.EN),
        )
        assertEquals(
            "بيانات هوية المستند مقفلة بعد الحفظ",
            documentIdentityLockedDescription(AppLanguage.AR),
        )
        assertEquals(
            "Document identity is locked after saving",
            documentIdentityLockedDescription(AppLanguage.EN),
        )
        assertEquals(
            "لعميل آخر، أنشئ نسخة جديدة من المستند",
            Localization.getString("duplicate_document_suggestion", AppLanguage.AR),
        )
        assertEquals(
            "For another customer, create a new copy of the document",
            Localization.getString("duplicate_document_suggestion", AppLanguage.EN),
        )
    }
}
