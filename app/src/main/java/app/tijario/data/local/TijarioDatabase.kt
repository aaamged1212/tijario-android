package app.tijario.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BusinessSettingsEntity::class,
        CustomerEntity::class,
        ProductEntity::class,
        DocumentEntity::class,
        LocalTaxEntity::class,
        LocalPaymentMethodEntity::class,
        LocalSignatureEntity::class,
        LocalTermsEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class TijarioDatabase : RoomDatabase() {
    abstract fun tijarioDao(): TijarioDao

    companion object {
        @Volatile
        private var instance: TijarioDatabase? = null

        fun getInstance(context: Context): TijarioDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TijarioDatabase::class.java,
                    "tijario-local-cache.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
