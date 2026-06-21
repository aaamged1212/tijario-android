package app.tijario.config

import android.content.Context

private const val PREFS_NAME = "tijario_app_preferences"
private const val KEY_LANGUAGE = "language"
private const val KEY_DARK_MODE = "dark_mode"

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
}
