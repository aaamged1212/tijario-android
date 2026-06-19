package app.tijario.config

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.postgrest.Postgrest
import app.tijario.data.remote.BackendApiClient

object Supabase {
    val client: SupabaseClient by lazy {
        val config = loadAppConfig()
        createSupabaseClient(
            supabaseUrl = config.supabaseUrl,
            supabaseKey = config.supabaseAnonKey
        ) {
            install(Auth)
            install(ComposeAuth) {
                if (config.googleWebClientId.isNotBlank()) {
                    googleNativeLogin(serverClientId = config.googleWebClientId)
                }
            }
            install(Postgrest)
        }
    }

    val apiClient: BackendApiClient by lazy {
        val config = loadAppConfig()
        BackendApiClient(
            config = config,
            accessTokenProvider = {
                client.auth.currentAccessTokenOrNull()
            }
        )
    }
}
