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
            .apply()
    }
}
