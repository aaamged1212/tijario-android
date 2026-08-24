package app.tijario.config

import android.content.Context
import android.content.res.Configuration
import app.tijario.data.model.UserPlanUsage
import java.util.Locale
import java.util.UUID

private const val PREFS_NAME = "tijario_app_preferences"
private const val KEY_LANGUAGE = "language"
private const val KEY_DARK_MODE = "dark_mode"
private const val KEY_THEME_MODE = "theme_mode"
private const val KEY_PLAN_CODE = "plan_code"
private const val KEY_PLAN_NAME = "plan_name"
private const val KEY_PERIOD_MONTH = "period_month"
private const val KEY_DOCUMENTS_USED = "documents_used"
private const val KEY_DOCUMENTS_LIMIT = "documents_limit"
private const val KEY_AI_USED = "ai_used"
private const val KEY_AI_LIMIT = "ai_limit"
private const val KEY_CUSTOMERS_USED = "customers_used"
private const val KEY_CUSTOMERS_LIMIT = "customers_limit"
private const val KEY_PRODUCTS_USED = "products_used"
private const val KEY_PRODUCTS_LIMIT = "products_limit"
private const val KEY_RESET_AT = "reset_at"
private const val KEY_RENEWAL_AT = "renewal_at"
private const val KEY_ALLOWED_TEMPLATE_IDS = "allowed_template_ids"
private const val KEY_REMOVE_TIJARIO_BRANDING = "remove_tijario_branding"
private const val KEY_PLAN_USAGE_UPDATED_AT = "plan_usage_updated_at"
private const val KEY_ANNOUNCEMENTS_SYNCED_AT = "announcements_synced_at"
private const val KEY_PUSH_ENABLED = "push_enabled"
private const val KEY_NOTIFICATION_EXPLAINED = "notification_explained"
private const val KEY_SUBSCRIBED_TOPIC = "subscribed_topic"
private const val KEY_INSTALLATION_ID = "installation_id"
private const val KEY_PHONE_BACKUP_TREE_URI = "phone_backup_tree_uri"
private const val KEY_PHONE_BACKUP_TREE_NAME = "phone_backup_tree_name"
private const val KEY_PHONE_BACKUP_DESTINATION_MODE = "phone_backup_destination_mode"
private const val KEY_BUSINESS_SETTINGS_MIRROR_FINGERPRINT = "business_settings_mirror_fingerprint"
private const val KEY_PENDING_ACCOUNT_DELETION_CLEANUP_USER_ID = "pending_account_deletion_cleanup_user_id"

private fun planKey(userId: String, suffix: String) = "plan_usage_${userId}_$suffix"

internal fun resolveInitialLanguage(savedValue: String?, systemLanguage: String): AppLanguage =
    savedValue
        ?.let { value -> runCatching { AppLanguage.valueOf(value) }.getOrNull() }
        ?: if (systemLanguage.equals("ar", ignoreCase = true)) AppLanguage.AR else AppLanguage.EN

internal fun resolveInitialDarkMode(savedValue: Boolean?, systemDarkMode: Boolean): Boolean =
    savedValue ?: systemDarkMode

enum class AppLanguageMode {
    SYSTEM,
    ARABIC,
    ENGLISH,
}

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

internal fun resolveLanguageMode(savedValue: String?): AppLanguageMode =
    when (savedValue?.uppercase()) {
        AppLanguage.AR.name -> AppLanguageMode.ARABIC
        AppLanguage.EN.name -> AppLanguageMode.ENGLISH
        else -> AppLanguageMode.SYSTEM
    }

internal fun resolveThemeMode(savedMode: String?, legacyDarkMode: Boolean?): AppThemeMode =
    savedMode
        ?.let { value -> runCatching { AppThemeMode.valueOf(value) }.getOrNull() }
        ?: legacyDarkMode?.let { isDark -> if (isDark) AppThemeMode.DARK else AppThemeMode.LIGHT }
        ?: AppThemeMode.SYSTEM

object AppPreferences {
    fun getSystemLanguage(context: Context): AppLanguage {
        val systemLanguage = context.resources.configuration.locales
            .takeIf { !it.isEmpty }
            ?.get(0)
            ?.language
            ?: Locale.getDefault().language
        return if (systemLanguage.equals("ar", ignoreCase = true)) AppLanguage.AR else AppLanguage.EN
    }

    fun getSystemDarkMode(context: Context): Boolean =
        context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    fun getLanguageMode(context: Context): AppLanguageMode {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return resolveLanguageMode(preferences.getString(KEY_LANGUAGE, null))
    }

    fun getInstallationId(context: Context): String {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.getString(KEY_INSTALLATION_ID, null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { return it }

        val generated = UUID.randomUUID().toString()
        preferences.edit().putString(KEY_INSTALLATION_ID, generated).commit()
        return preferences.getString(KEY_INSTALLATION_ID, generated) ?: generated
    }

    fun getLanguage(context: Context): AppLanguage {
        return when (getLanguageMode(context)) {
            AppLanguageMode.SYSTEM -> getSystemLanguage(context)
            AppLanguageMode.ARABIC -> AppLanguage.AR
            AppLanguageMode.ENGLISH -> AppLanguage.EN
        }
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        setLanguageMode(
            context = context,
            mode = if (language == AppLanguage.AR) AppLanguageMode.ARABIC else AppLanguageMode.ENGLISH,
        )
    }

    fun setLanguageMode(context: Context, mode: AppLanguageMode) {
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        when (mode) {
            AppLanguageMode.SYSTEM -> editor.remove(KEY_LANGUAGE)
            AppLanguageMode.ARABIC -> editor.putString(KEY_LANGUAGE, AppLanguage.AR.name)
            AppLanguageMode.ENGLISH -> editor.putString(KEY_LANGUAGE, AppLanguage.EN.name)
        }
        editor.apply()
    }

    fun getThemeMode(context: Context): AppThemeMode {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val legacyDarkMode = if (preferences.contains(KEY_DARK_MODE)) {
            preferences.getBoolean(KEY_DARK_MODE, false)
        } else {
            null
        }
        return resolveThemeMode(
            savedMode = preferences.getString(KEY_THEME_MODE, null),
            legacyDarkMode = legacyDarkMode,
        )
    }

    fun setThemeMode(context: Context, mode: AppThemeMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode.name)
            .remove(KEY_DARK_MODE)
            .apply()
    }

    fun getDarkMode(context: Context): Boolean {
        return when (getThemeMode(context)) {
            AppThemeMode.SYSTEM -> getSystemDarkMode(context)
            AppThemeMode.LIGHT -> false
            AppThemeMode.DARK -> true
        }
    }

    fun setDarkMode(context: Context, enabled: Boolean) {
        setThemeMode(context, if (enabled) AppThemeMode.DARK else AppThemeMode.LIGHT)
    }

    fun getPlanUsage(context: Context, userId: String): UserPlanUsage? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val planCode = prefs.getString(planKey(userId, KEY_PLAN_CODE), null) ?: return null
        val planName = prefs.getString(planKey(userId, KEY_PLAN_NAME), null).orEmpty()
        val periodMonth = prefs.getString(planKey(userId, KEY_PERIOD_MONTH), null).orEmpty()
        val documentsUsed = prefs.getInt(planKey(userId, KEY_DOCUMENTS_USED), -1)
        val documentsLimit = prefs.getInt(planKey(userId, KEY_DOCUMENTS_LIMIT), -1)
        val aiUsed = prefs.getInt(planKey(userId, KEY_AI_USED), -1)
        val aiLimit = prefs.getInt(planKey(userId, KEY_AI_LIMIT), -1)
        val customersUsed = prefs.getInt(planKey(userId, KEY_CUSTOMERS_USED), 0)
        val customersLimitRaw = prefs.getInt(planKey(userId, KEY_CUSTOMERS_LIMIT), -1)
        val productsUsed = prefs.getInt(planKey(userId, KEY_PRODUCTS_USED), 0)
        val productsLimitRaw = prefs.getInt(planKey(userId, KEY_PRODUCTS_LIMIT), -1)
        val resetAt = prefs.getString(planKey(userId, KEY_RESET_AT), null)
        val renewalAt = prefs.getString(planKey(userId, KEY_RENEWAL_AT), null)
        val allowedTemplateIds = prefs
            .getString(planKey(userId, KEY_ALLOWED_TEMPLATE_IDS), "")
            .orEmpty()
            .split("|")
            .filter { it.isNotBlank() }
        val removeTijarioBranding = prefs.getBoolean(planKey(userId, KEY_REMOVE_TIJARIO_BRANDING), false)
        if (documentsUsed < 0 || documentsLimit < 0 || aiUsed < 0 || aiLimit < 0 || periodMonth.isBlank()) {
            return null
        }
        return UserPlanUsage(
            planCode = planCode,
            planName = planName.ifBlank { planCode },
            periodMonth = periodMonth,
            documentsUsed = documentsUsed,
            documentsLimit = documentsLimit,
            aiUsed = aiUsed,
            aiLimit = aiLimit,
            customersUsed = customersUsed,
            customersLimit = customersLimitRaw.takeIf { it >= 0 },
            productsUsed = productsUsed,
            productsLimit = productsLimitRaw.takeIf { it >= 0 },
            resetAt = resetAt,
            renewalAt = renewalAt,
            allowedTemplateIds = allowedTemplateIds,
            removeTijarioBranding = removeTijarioBranding,
        )
    }

    fun setPlanUsage(context: Context, userId: String, usage: UserPlanUsage) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(planKey(userId, KEY_PLAN_CODE), usage.planCode)
            .putString(planKey(userId, KEY_PLAN_NAME), usage.planName)
            .putString(planKey(userId, KEY_PERIOD_MONTH), usage.periodMonth)
            .putInt(planKey(userId, KEY_DOCUMENTS_USED), usage.documentsUsed)
            .putInt(planKey(userId, KEY_DOCUMENTS_LIMIT), usage.documentsLimit)
            .putInt(planKey(userId, KEY_AI_USED), usage.aiUsed)
            .putInt(planKey(userId, KEY_AI_LIMIT), usage.aiLimit)
            .putInt(planKey(userId, KEY_CUSTOMERS_USED), usage.customersUsed)
            .putInt(planKey(userId, KEY_CUSTOMERS_LIMIT), usage.customersLimit ?: -1)
            .putInt(planKey(userId, KEY_PRODUCTS_USED), usage.productsUsed)
            .putInt(planKey(userId, KEY_PRODUCTS_LIMIT), usage.productsLimit ?: -1)
            .putString(planKey(userId, KEY_RESET_AT), usage.resetAt)
            .putString(planKey(userId, KEY_RENEWAL_AT), usage.renewalAt)
            .putString(planKey(userId, KEY_ALLOWED_TEMPLATE_IDS), usage.allowedTemplateIds.joinToString("|"))
            .putBoolean(planKey(userId, KEY_REMOVE_TIJARIO_BRANDING), usage.removeTijarioBranding)
            .putLong(planKey(userId, KEY_PLAN_USAGE_UPDATED_AT), System.currentTimeMillis())
            .apply()
    }

    fun isPlanUsageFresh(context: Context, userId: String, maxAgeMs: Long): Boolean {
        val updatedAt = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(planKey(userId, KEY_PLAN_USAGE_UPDATED_AT), 0L)
        return updatedAt > 0L && System.currentTimeMillis() - updatedAt < maxAgeMs
    }

    fun isAnnouncementsFresh(context: Context, userId: String, maxAgeMs: Long): Boolean {
        val updatedAt = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(planKey(userId, KEY_ANNOUNCEMENTS_SYNCED_AT), 0L)
        return updatedAt > 0L && System.currentTimeMillis() - updatedAt < maxAgeMs
    }

    fun setAnnouncementsSynced(context: Context, userId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(planKey(userId, KEY_ANNOUNCEMENTS_SYNCED_AT), System.currentTimeMillis())
            .apply()
    }

    fun clearAnnouncementsSynced(context: Context, userId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(planKey(userId, KEY_ANNOUNCEMENTS_SYNCED_AT))
            .apply()
    }

    fun isPushEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_PUSH_ENABLED, true)

    fun setPushEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PUSH_ENABLED, enabled)
            .apply()
    }

    fun wasNotificationExplained(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_NOTIFICATION_EXPLAINED, false)

    fun setNotificationExplained(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_NOTIFICATION_EXPLAINED, true)
            .apply()
    }

    fun getSubscribedTopic(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SUBSCRIBED_TOPIC, null)

    fun setSubscribedTopic(context: Context, topic: String?) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SUBSCRIBED_TOPIC, topic)
            .apply()
    }

    fun getPhoneBackupTreeUri(context: Context, userId: String): android.net.Uri? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(planKey(userId, KEY_PHONE_BACKUP_TREE_URI), null)
            ?.let(android.net.Uri::parse)

    fun setPhoneBackupTreeUri(context: Context, userId: String, uri: String?) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(planKey(userId, KEY_PHONE_BACKUP_TREE_URI), uri)
            .apply()
    }

    fun getPhoneBackupTreeName(context: Context, userId: String): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(planKey(userId, KEY_PHONE_BACKUP_TREE_NAME), null)
            ?.trim()
            ?.takeIf(String::isNotEmpty)

    fun setPhoneBackupTreeName(context: Context, userId: String, name: String?) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(planKey(userId, KEY_PHONE_BACKUP_TREE_NAME), name?.trim()?.takeIf(String::isNotEmpty))
            .apply()
    }

    fun getPhoneBackupDestinationMode(context: Context, userId: String): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(planKey(userId, KEY_PHONE_BACKUP_DESTINATION_MODE), null)
            ?: if (getPhoneBackupTreeUri(context, userId) != null) "CUSTOM_SAF_TREE" else "DEFAULT_DOWNLOADS"

    fun setPhoneBackupDestinationMode(context: Context, userId: String, mode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(planKey(userId, KEY_PHONE_BACKUP_DESTINATION_MODE), mode)
            .apply()
    }

    fun getBusinessSettingsMirrorFingerprint(context: Context, userId: String): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(planKey(userId, KEY_BUSINESS_SETTINGS_MIRROR_FINGERPRINT), null)

    fun setBusinessSettingsMirrorFingerprint(context: Context, userId: String, fingerprint: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(planKey(userId, KEY_BUSINESS_SETTINGS_MIRROR_FINGERPRINT), fingerprint)
            .apply()
    }

    fun pendingAccountDeletionCleanupUserId(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PENDING_ACCOUNT_DELETION_CLEANUP_USER_ID, null)

    fun markAccountDeletionCleanupPending(context: Context, userId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING_ACCOUNT_DELETION_CLEANUP_USER_ID, userId)
            .apply()
    }

    fun clearPendingAccountDeletionCleanup(context: Context, userId: String) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (preferences.getString(KEY_PENDING_ACCOUNT_DELETION_CLEANUP_USER_ID, null) == userId) {
            preferences.edit().remove(KEY_PENDING_ACCOUNT_DELETION_CLEANUP_USER_ID).apply()
        }
    }
}
