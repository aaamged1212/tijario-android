package app.tijario.data

import android.content.Context
import app.tijario.config.Supabase
import app.tijario.data.local.TijarioDatabase
import app.tijario.data.repository.TijarioRepository

object AppContainer {
    @Volatile
    private var repositoryInstance: TijarioRepository? = null

    fun repository(context: Context): TijarioRepository =
        repositoryInstance ?: synchronized(this) {
            repositoryInstance ?: TijarioRepository(
                database = TijarioDatabase.getInstance(context),
                supabaseClient = Supabase.client,
                backendApiClient = Supabase.apiClient,
            ).also { repositoryInstance = it }
        }
}
