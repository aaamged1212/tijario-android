package app.tijario.config

import android.content.Context
import app.tijario.data.model.UserPlanUsage

private const val PREFS_NAME = "tijario_app_preferences"
private const val KEY_LANGUAGE = "language"
private const val KEY_DARK_MODE = "dark_mode"
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
private const val KEY_ALLOWED_TEMPLATE_IDS = "allowed_template_ids"
private const val KEY_PUSH_ENABLED = "push_enabled"
private const val KEY_NOTIFICATION_EXPLAINED = "notification_explained"
private const val KEY_SUBSCRIBED_TOPIC = "subscribed_topic"

private fun planKey(userId: String, suffix: String) = "plan_usage_${userId}_$suffix"

object AppPreferences {
    fun getLanguage(context: Context): AppLanguage {
        val value = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, AppLanguage.AR.name)
            .orEmpty()

        return runCatching { AppLanguage.valueOf(value) }.getOrDefault(AppLanguage.AR)
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.name)
            .apply()
    }

    fun getDarkMode(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_MODE, enabled)
            .apply()
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
        val allowedTemplateIds = prefs
            .getString(planKey(userId, KEY_ALLOWED_TEMPLATE_IDS), "")
            .orEmpty()
            .split("|")
            .filter { it.isNotBlank() }
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
            allowedTemplateIds = allowedTemplateIds,
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
            .putString(planKey(userId, KEY_ALLOWED_TEMPLATE_IDS), usage.allowedTemplateIds.joinToString("|"))
            .apply()
    }

    fun isPushEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_PUSH_ENABLED, false)

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
}
