package app.tijario.config

import app.tijario.BuildConfig

data class AppConfig(
    val supabaseUrl: String,
    val supabaseAnonKey: String,
    val apiBaseUrl: String,
) {
    val isComplete: Boolean =
        supabaseUrl.isNotBlank() && supabaseAnonKey.isNotBlank() && apiBaseUrl.isNotBlank()
}

fun loadAppConfig(): AppConfig =
    AppConfig(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY,
        apiBaseUrl = BuildConfig.API_BASE_URL,
    )
